package org.openmrs.module.ncd.output.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.StringUtilities;

/**
 * A DataFeedSink that writes HL7 message files.
 */
public class DataFeedSinkHL7 implements DataFeedSink {

    private static Log logger = LogFactory.getLog(DataFeedSinkHL7.class);
    @SuppressWarnings("unused")
    private Map<String, String> properties;
    private DataFeedLog dataFeedLog;
	private File outputFile;
	private FileWriter fwriter;
	private BufferedWriter writer;
	private boolean empty = true;
	private boolean stripForRHITs = false;
	protected Set<String> adtKeepSegmentTypes = new HashSet<String>();
	protected Set<String> oruKeepSegmentTypes = new HashSet<String>();
	
	private static final int MSH_MESSAGE_TYPE_FIELD_INDEX = 8;
	
	private static String CR = "\r";
	private static String LF = "\n";
	private static String CRLF = "\r\n";
    
    public void create(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog) {

    	adtKeepSegmentTypes.add("MSH");
    	adtKeepSegmentTypes.add("PID");
    	adtKeepSegmentTypes.add("PV1");
    	adtKeepSegmentTypes.add("ZVX");

    	oruKeepSegmentTypes.add("MSH");
    	oruKeepSegmentTypes.add("PID");
    	oruKeepSegmentTypes.add("ZVX");

    	this.empty = true;
    	
    	try {
    		this.properties = properties;
    		this.dataFeedLog = dataFeedLog;
    		
    		String stripForRHITsStr = properties.get(DataFeedSinkFactory.PROP_SINK_STRIP_FOR_RHITS);
    		stripForRHITs = false;
    		if (stripForRHITsStr != null && stripForRHITsStr.equals("true")) {
    			stripForRHITs = true;
    		}
    		
    		// Record the temporary file
	    	outputFile = tempFile;
	    	
	    	// Create a buffered writer for the temporary file
	    	fwriter = new FileWriter(outputFile);
	    	writer = new BufferedWriter(fwriter);
    	}
    	catch (IOException ioe) {
            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
    		dataFeedLog.error("Sink error creating file: " + ioe.toString());
    	}
    }

    /**
     * RHITS wants an export file with most of the message segments removed, and all ADT messages transformed
     * to ORU^R01.
     * 
     * @param eol The end of line (segment) delimiter to use.
     * @param message The unmodified source message to construct a transformed copy of.
     * @return A copy of the source message with the RHITS transformations done.
     */
    protected String transformMessageForRHITS(String eol, String message) {

    	StringBuilder strippedMessage = new StringBuilder();
    	
    	String [] segments = message.split(eol);
    	logger.debug("segment 0=\"" + segments[0] + "\"");
    	
    	// First segment will start with MSH followed by the field delimiter
    	String fieldDelimiter = segments[0].substring(3, 4);
    	logger.debug("field delimiter=\"" + fieldDelimiter + "\"");

    	String[] fields = StringUtilities.strSplit(segments[0], fieldDelimiter);
    	logger.debug("segment 0 fields=" + Arrays.asList(fields));
		
		Set<String> keepSegmentTypes = oruKeepSegmentTypes;
		
		// If this is an ADT message of some type
		if (fields[MSH_MESSAGE_TYPE_FIELD_INDEX].startsWith("ADT^")) {
			logger.debug("message is type ADT, convert to ORU^R01");
			
			// Use the ADT keep segment table instead
			keepSegmentTypes = adtKeepSegmentTypes;
			
			// Change the message type to ORU^R01
			fields[MSH_MESSAGE_TYPE_FIELD_INDEX] = "ORU^R01";
			segments[0] = StringUtilities.merge(fields, fieldDelimiter);
		}
		else {
			logger.debug("message is type \"" + fields[MSH_MESSAGE_TYPE_FIELD_INDEX] + "\"");
		}

    	for (String segment : segments) {
    		
    		int delimiterIndex = segment.indexOf(fieldDelimiter);
    		String segmentType = segment.substring(0, delimiterIndex);
    		//logger.debug("segmentType=" + segmentType);

    		if (keepSegmentTypes.contains(segmentType)) {
    			strippedMessage.append(segment);
    			strippedMessage.append(eol);
    		}
    	}
    	
    	return strippedMessage.toString();
    }

    public void append(Map<String, Object> row) {

    	this.empty = false;
    	
    	String hl7 = (String) row.get(DataFeedExtractorFactory.COLUMN_HL7);
    	
    	// Determine the line separator
    	String separator = null;
    	String separator2 = null;
    	if (hl7.indexOf(CRLF) != -1) {
    		separator = CRLF;
    		separator2 = LF;
    	} else if (hl7.indexOf(CR) != -1) {
    		separator = CR;
    		separator2 = CR;
    	} else {
    		separator = LF;
    		separator2 = LF;
    	}
    	
    	// If this export is for RHITs, discard segments it doesn't want
    	if (stripForRHITs) {
    		hl7 = transformMessageForRHITS(separator, hl7);
    	}

    	// Get the sequence number on the last OBR segment
    	int sequence = 0;
    	int offset = hl7.lastIndexOf(separator2 + "OBR|");
    	if (offset >= 0) {
    		sequence = getInt(hl7, offset + 5); 
    	}

    	// Create a new OBR segment using the next sequence number
    	//		OBR-3 = Unique sequential record ID from tblReportableResults + "^NCD"
    	//		OBR-4 = 54217-5^Public health notifiable condition^LN
    	//		OBR-29 = test placer order number + "^" + test filler order number
    	Object mpqSeqNumber = row.get(DataFeedExtractorFactory.COLUMN_TEST_MPQ_SEQ_NUMBER);
    	Object placerOrderNumber = row.get(DataFeedExtractorFactory.COLUMN_TEST_PLACER_ORDER_NUM);
    	if (placerOrderNumber == null) {
    		placerOrderNumber = "";
    	}
    	Object fillerOrderNumber = row.get(DataFeedExtractorFactory.COLUMN_TEST_FILLER_ORDER_NUM);
    	if (fillerOrderNumber == null) {
    		fillerOrderNumber = "";
    	}
    	String obr29 = "";
    	if (!StringUtilities.isNullEmptyOrWhitespace((String) placerOrderNumber) ||
    		!StringUtilities.isNullEmptyOrWhitespace((String) fillerOrderNumber)) {
    		obr29 = placerOrderNumber + "^" + fillerOrderNumber;
    	}
    	String newOBR = "OBR|" + (sequence + 1)
    				 + "||" + mpqSeqNumber + "^NCD"
    				 + "|54217-5^Public health notifiable condition^LN"
    				 + "|||||||||||||||||||||||||" + obr29
    				 + separator;

    	// Create a new OBX segment
    	//		OBX-2 = "ST"
    	//		OBX-3 = 54217-5^Public health notifiable condition^LN
    	//		OBX-5 = Notifiable Condition from the Dwyer Table (e.g., "Hepatitis A", "Salmonella", etc.)
    	//		OBX-11 = "F" (final result)
    	//		OBX-14 = YYYYMMDDHHMMSS (test date)
    	//		OBX-15 = NCD
    	Object conditionName = row.get(DataFeedExtractorFactory.COLUMN_DWYER_CONDITION_NAME);
    	Object testDate = row.get(DataFeedExtractorFactory.COLUMN_TEST_DATE);
    	String fmtTestDate = "";
    	if (testDate != null) {
    		fmtTestDate = new SimpleDateFormat("yyyyMMddHHmmss").format((Date) testDate);
    	}
    	String newOBX = "OBX|1|ST|54217-5^Public health notifiable condition^LN||" + conditionName
    				  + "||||||F|||" + fmtTestDate
    				  + "|NCD||||"
    				  + separator;
    	
    	// Create a new NTE segmente
    	//		NTE-1 = 1
    	//		NTE-3 = "OBR=xx,OBX=yy-zz"
    	Object obrSetId = row.get(DataFeedExtractorFactory.COLUMN_OBR_SET_ID);
    	Object obxFirstId = row.get(DataFeedExtractorFactory.COLUMN_OBX_START_SET_ID);
    	Object obxLastId = row.get(DataFeedExtractorFactory.COLUMN_OBX_END_SET_ID);
    	String newNTE = "NTE|1||OBR=" + obrSetId + ",OBX=" + obxFirstId + "-" + obxLastId + separator;
    	
    	try {
	    	writer.append(hl7);
	    	if (hl7.charAt(hl7.length() - 1) != '\r' && hl7.charAt(hl7.length() - 1) != '\n') {
	    		writer.append(separator);
	    	}
	    	writer.append(newOBR);
	    	writer.append(newOBX);
	    	writer.append(newNTE);
    	}
    	catch (IOException ioe) {
            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
    		dataFeedLog.error("Sink error appending file: " + ioe.toString());
    	}
    }

    public void close() {
    	try {
    		writer.close();
    		fwriter.close();
    	}
    	catch (IOException ioe) {
            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
    		dataFeedLog.error("Sink error closing file: " + ioe.toString());
    	}
    	finally {
    		writer = null;
    		fwriter = null;
    		outputFile = null;
    	}
    }
    
    private int getInt(String s, int offset) {
    	int offset2 = offset;
    	try {
        	while (Character.isDigit(s.charAt(offset2))) {
        		offset2++;
        	}
        	return Integer.parseInt(s.substring(offset, offset2));
    	}
    	catch (NumberFormatException nfe) {
    		// assume there is one OBR segment 
    		return 1;
    	}
    }
    
    public boolean isEmpty() {
    
    	return this.empty;
    }
    
    /** Gets the MIME type for the type of "file" created by this sink. */
    public String getMIMEType() {

    	// There doesn't appear to be any commonly used HL7-specific MIME type.
    	return "text/plain";
    }
}

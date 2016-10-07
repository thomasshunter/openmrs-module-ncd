package org.openmrs.module.ncd.output.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.openmrs.module.ncd.utilities.StringUtilities;

/**
 * A DataFeedSink that writes delimited text files.
 */
public class DataFeedSinkDelimited implements DataFeedSink {

    @SuppressWarnings("unused")
    private Map<String, String> properties;
    private DataFeedLog dataFeedLog;
    private String[] outputColumnNames;
	private SimpleDateFormat dateFormat;
	private Writer writer;
	private boolean empty = true;

	// create overload used by the export task
    public void create(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog) {

    	try {

            create(new BufferedWriter(new FileWriter(tempFile)), 
                    properties, dataFeedLog);
    	}
    	catch (IOException ioe) {
    		dataFeedLog.error("Sink error creating file: " + ioe.toString());
    	}
    }

    // create overload used by the export button on list pages
    public void create(Writer out, Map<String, String> properties, DataFeedLog dataFeedLog) {
        
    	this.empty = true;
    	
        try {

            this.properties = properties;
            this.dataFeedLog = dataFeedLog;
            
            // Record the output column names
            outputColumnNames = StringUtilities.fromCSV(properties.get(DataFeedSinkFactory.PROP_SINK_OUTPUT_COLS_TO_INCL));
            
            // Construct and cache a SimpleDateFormat for later use
            String theFormat = properties.get(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT);
            if (theFormat != null && theFormat.length()>0) {
                dateFormat = new SimpleDateFormat(theFormat);
            }
            else {
                // default date/time format
                dateFormat = new SimpleDateFormat(DataFeedSinkFactory.getDefaultDateTimeFormat());
            }
            
            writer = out;
            
            // Write the header row with the output column names
            writeHeaderRow();
        }
        catch (IOException ioe) {
            dataFeedLog.error("Sink error creating file: " + ioe.toString());
        }
    }
    
    public void writeHeaderRow() throws IOException {
        
        for (int i=0; i < outputColumnNames.length; i++) {
            writer.append(outputColumnNames[i]);
            if (i < outputColumnNames.length - 1) {
                writer.append(",");
            }
        }
        writer.append("\r\n");
    }

    public void append(Map<String, Object> row) {
    	
    	this.empty = false;
    	
    	int i=0;
    	try {
	    	for (i=0; i < outputColumnNames.length; i++) {
	    		
	    		Object columnValue = row.get(outputColumnNames[i]);
	    		
	    		if (columnValue == null) {
	    			// output null column value as empty string, enclosed by double quotes
	    			writer.append("\"\"");	
	    		} else if (columnValue instanceof Date) {
	    			writer.append(StringUtilities.csvEscape(dateFormat.format(columnValue)));
	    		}
	    		else {
		    		writer.append(StringUtilities.csvEscape(columnValue.toString()));
	    		}
	    		
	    		if (i < outputColumnNames.length - 1) {
	    			writer.append(",");
	    		}
	    	}
	    	writer.append("\r\n");
    	}
    	catch (IOException ioe) {
    		dataFeedLog.error("Sink error appending file: " + ioe.toString());
    	}
    }

    public void close() {
        
    	try {
    	    
    		writer.close();
    	}
    	catch (IOException ioe) {
    		dataFeedLog.error("Sink error closing file: " + ioe.toString());
    	}
    	finally {
    		writer = null;
    	}
    }
    
    public boolean isEmpty() {
    
    	return this.empty;
    }
    
    /** Gets the MIME type for the type of "file" created by this sink. */
    public String getMIMEType() {

    	return "text/csv";
    }
}

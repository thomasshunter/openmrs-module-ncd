package org.openmrs.module.ncd.output.extract;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** A DataFeedSender implementation that copies the file to a locally
 * accessible folder.
 */
public class DataFeedSenderCopy implements DataFeedSender {

	private static Log logger = LogFactory.getLog(DataFeedSenderCopy.class);
    private Map<String, String> properties;
    File sourceFile = null;
	private DataFeedLog dataFeedLog;

    public void configure(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog) {
    	sourceFile = tempFile;
        this.properties = properties;
        this.dataFeedLog = dataFeedLog;
    }

    public void send() {

		FileInputStream sourceFileStream = null;
		BufferedInputStream sourceStream = null;
		FileOutputStream targetFileStream = null;
    	OutputStream targetStream = null;
    	try {

    		// Create a buffered stream for the source file
    		sourceFileStream = new FileInputStream(sourceFile);
    		sourceStream = new BufferedInputStream(sourceFileStream);

    		// Get the target File, and its parent
    		File targetFile = new File(DataFeedSenderFactory.constructTargetPathname(properties));
    		File targetFileParent = targetFile.getParentFile();
    		
    		// Create the parent directories as necessary
    		if (targetFileParent != null) {
    			targetFileParent.mkdirs();
    		}

    		// Create a buffered stream for the target file
    		targetFileStream = new FileOutputStream(targetFile);
	    	targetStream = new BufferedOutputStream(targetFileStream);
	    	
	    	// Copy from source to target
	    	int theByte;
	    	while ((theByte = sourceStream.read()) != -1) {
	    		targetStream.write(theByte);
	    	}
	    	
	    	// Flush the output stream
	    	targetStream.flush();
	    	targetFileStream.flush();
	    	
    		// Log some information about the file copy
    		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy K:mm:ss a");
    		dataFeedLog.info("");
    		dataFeedLog.info("Transfer completed at " + dateFormat.format(new Date()));
    		dataFeedLog.info("");
    		if (targetFileParent != null) {
    			dataFeedLog.info("     Directory: " + targetFileParent.getPath());
    		}
    		else {
    			dataFeedLog.info("     Directory: ");
    		}
    		dataFeedLog.info("          File: " + targetFile.getName());
    		dataFeedLog.info("First row sent: " + Long.toString(dataFeedLog.getFirstRowSent()));
    		dataFeedLog.info(" Last row sent: " + Long.toString(dataFeedLog.getLastRowSent()));
    	}
    	catch (FileNotFoundException fnfe) {
            logger.error("Unexpected exception: " + fnfe.getMessage(), fnfe);
    		dataFeedLog.error("Send error: " + fnfe.toString());
    	}
    	catch (IOException ioe) {
            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
    		dataFeedLog.error("Send error: " + ioe.toString());
    	}
    	finally {

    		if (sourceStream != null) {
    			try {
    				sourceStream.close();
    			}
    			catch (Exception e) {
    				// log and do nothing
    	            logger.error("Unexpected exception: " + e.getMessage(), e);
    			}
    			sourceStream = null;
    		}
    		
    		if (sourceFileStream != null) {
    			try {
    				sourceFileStream.close();
    			}
    			catch (Exception e) {
    				// log and do nothing
    	            logger.error("Unexpected exception: " + e.getMessage(), e);
    			}
    			sourceFileStream = null;
    		}
    		
    		sourceFile = null;
    		
    		if (targetStream != null) {
    			try {
    				targetStream.close();
    			}
    			catch (Exception e) {
    				// log and do nothing
    	            logger.error("Unexpected exception: " + e.getMessage(), e);
    			}
    			targetStream = null;
    		}
    		
    		if (targetFileStream != null) {
    			try {
    				targetFileStream.close();
    			}
    			catch (Exception e) {
    				// log and do nothing
    	            logger.error("Unexpected exception: " + e.getMessage(), e);
    			}
    			targetFileStream = null;
    		}
    	}
    }
    
    /** Tests if the sender has already sent the status email */
    public boolean isStatusEmailSent() {
    	return false;
    }
}

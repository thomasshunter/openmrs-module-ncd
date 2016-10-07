package org.openmrs.module.ncd.output.extract;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/** A DataFeedSender implementation that sends the file to a folder on
 * an FTP server.
 */
public class DataFeedSenderFTP implements DataFeedSender {

	private static Log logger = LogFactory.getLog(DataFeedSenderFTP.class);
    private Map<String, String> properties;
    File sourceFile = null;
	private DataFeedLog dataFeedLog;

    public void configure(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog) {
    	sourceFile = tempFile;
        this.properties = properties;
        this.dataFeedLog = dataFeedLog;
    }

    public void send() {

    	// Complain and exit if any required properties are missing or empty
    	if (!hasProperty(DataFeedSenderFactory.PROP_SENDER_FTP_HOST)) {
    		dataFeedLog.error("Missing required property: " + DataFeedSenderFactory.PROP_SENDER_FTP_HOST);
    		return;
    	}

    	FTPClient ftp = new FTPClient();
		FileInputStream sourceFileStream = null;
		BufferedInputStream sourceStream = null;
    	try {
    		// Get the connect string (host:port) for the FTP server
    		String server = new String(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_HOST));
    		if (hasProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PORT)) {
    			server += ":" + properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_PORT);
    		}
    		
    		// Connect to the FTP server
    		if (!hasProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PORT)) {
    			ftp.connect(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_HOST));
    		}
    		else
    		{
    			ftp.connect(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_HOST), 
    					Integer.valueOf(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_PORT)));
    		}

    		// Verify a successful reply code
    		int reply = ftp.getReplyCode();
    		if(!FTPReply.isPositiveCompletion(reply)) {
    			dataFeedLog.error("FTP server refused connection.");
    			return;
    		}

    		// Authenticate
    		if (hasProperty(DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME)) {

    			if (!hasProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD)) {
    				dataFeedLog.error("Missing required property: " + DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD);
    				return;
    			}

    			if (!ftp.login(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME), properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD))) {
    				logError("Failed to authenticate.  Server reply:", ftp.getReplyStrings());
    				return;
    			}
    		}

    		// Get the target File, and its parent
    		File targetFile = new File(DataFeedSenderFactory.constructTargetPathname(properties));
    		File targetFileParent = targetFile.getParentFile();

    		// Change to the specified path (if any), making any directories that do not exist
    		if (targetFileParent != null) {

    			// If unable to switch to the target directory
        		if (!ftp.changeWorkingDirectory(targetFileParent.getPath())) {

        			String[] directories=DataFeedSenderFactory.splitPath(targetFileParent.getPath());
	    			for (int i=0; i < directories.length; i++) {
	        			
	    				// If unable to switch to this directory
	    				if (!ftp.changeWorkingDirectory(directories[i])) {

	    					// Create the directory
		    				if (!ftp.makeDirectory(directories[i])) {
		        				logError("Failed to make directory: " + directories[i] + ".  Server reply:", ftp.getReplyStrings());
		        				return;
		    				}
		    				
		    				// Change to the directory
		            		if (!ftp.changeWorkingDirectory(directories[i])) {
		        				logError("Failed to change working directory to: " + directories[i] + ".  Server reply:", ftp.getReplyStrings());
		        				return;
		            		}
	    				}
	    			}
        		}
    		}
    		
    		// Set binary file type
    		if (!ftp.setFileType(FTP.BINARY_FILE_TYPE)) {
				logError("Failed to set binary file type.  Server reply:", ftp.getReplyStrings());
				return;
    		}

    		// Store the file on the server
    		sourceFileStream = new FileInputStream(sourceFile);
    		sourceStream = new BufferedInputStream(sourceFileStream);
    		if (!ftp.storeFile(targetFile.getName(), sourceStream)) {
				logError("Failed to store file on server.  Server reply:", ftp.getReplyStrings());
				return;
    		}

    		// Logout
    		if (!ftp.logout()) {
				logError("Failed to logout.  Server reply:", ftp.getReplyStrings());
				return;
    		}
    		
    		// Log some information about the FTP file transfer
    		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy K:mm:ss a");
    		dataFeedLog.info("");
    		dataFeedLog.info("Transfer completed at " + dateFormat.format(new Date()));
    		dataFeedLog.info("");
    		dataFeedLog.info("          Host: " + server);
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
    	catch(IOException e) {

            logger.error("Unexpected exception: " + e.getMessage(), e);
    		dataFeedLog.error(e.toString());
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

    		if (ftp.isConnected()) {
    			try
    			{
    				ftp.disconnect();
    			}
    			catch(IOException ioe)
    			{
    				// log and do nothing
    	            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
    			}
    		}
    	}
    }
    
    private boolean hasProperty(String key) {
    	return (properties.get(key) != null && properties.get(key).length() >= 1);
    }
    
    private void logError(String summary, String[] replyStrings) {
    	dataFeedLog.error(summary);
    	if (replyStrings != null) {
    		for (int i=0; i < replyStrings.length; i++) {
    			dataFeedLog.error(replyStrings[i]);
    		}
    	}
    }
    
    /** Tests if the sender has already sent the status email */
    public boolean isStatusEmailSent() {
    	return false;
    }
}

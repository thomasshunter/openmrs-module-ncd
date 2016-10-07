package org.openmrs.module.ncd.output.extract;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

/** A DataFeedSender implementation that sends the file to a folder on
 * an SFTP server.
 */
public class DataFeedSenderSFTP implements DataFeedSender {

	private static Log logger = LogFactory.getLog(DataFeedSenderSFTP.class);
    private Map<String, String> properties;
    File sourceFile = null;
	private DataFeedLog dataFeedLog;

	public class SftpUserInfo implements UserInfo {
		private String password;
		
		public SftpUserInfo(String password) {
			this.password = password;
		}
		
		public String getPassphrase() {
			return null;
		}

		public String getPassword() {
			return password;
		}

		public boolean promptPassword(String password) {
			return true;
		}

		public boolean promptPassphrase(String passphrase) {
			return false;
		}

		public boolean promptYesNo(String str) {
			return true;
		}

		public void showMessage(String message) {

		}
	}
	
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

    	if (!hasProperty(DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME)) {
    		dataFeedLog.error("Missing required property: " + DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME);
    		return;
    	}
    	
    	if (!hasProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD)) {
    		dataFeedLog.error("Missing required property: " + DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD);
    		return;
    	}
    	
    	ChannelSftp client = null; 
		FileInputStream sourceFileStream = null;
		BufferedInputStream sourceStream = null;
    	try {
			JSch jsch = new JSch();
			
    		// Get the connect string (host:port) for the SFTP server
    		String server = new String(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_HOST));
    		if (hasProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PORT)) {
    			server += ":" + properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_PORT);
    		}

			// Get the session
			Session session = null;
			if (hasProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PORT)) {
				session = jsch.getSession(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME),
											properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_HOST),
											Integer.valueOf(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_PORT)));
			} else {
				session = jsch.getSession(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME),
						properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_HOST));
			}

			// Set the UserInfo with the password, and connect/authenticate the session
			UserInfo userInfo = new SftpUserInfo(properties.get(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD));
			session.setUserInfo(userInfo);
			session.connect();

			// Get the channel
			Channel channel = session.openChannel("sftp");
			channel.connect();
			client = (ChannelSftp) channel;

    		// Get the target File, and its parent
    		File targetFile = new File(DataFeedSenderFactory.constructTargetPathname(properties));
    		File targetFileParent = targetFile.getParentFile();

    		// Change to the specified path (if any), making any directories that do not exist
    		if (targetFileParent != null) {

    			// If unable to switch to the target directory
    			try {
    				client.cd(targetFileParent.getPath());
    			}
    			catch (SftpException se) {
        			String[] directories=DataFeedSenderFactory.splitPath(targetFileParent.getPath());
	    			for (int i=0; i < directories.length; i++) {
	    				try {
	        				client.cd(directories[i]);
	    				}
	    				catch (SftpException se2) {
	    					client.mkdir(directories[i]);
	    					client.cd(directories[i]);
	    				}
	    			}
    			}
    		}
    		
    		// Store the file on the server
    		sourceFileStream = new FileInputStream(sourceFile);
    		sourceStream = new BufferedInputStream(sourceFileStream);
			client.put(sourceStream, targetFile.getName(), ChannelSftp.OVERWRITE);

    		// Log some information about the SFTP file transfer
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
    	catch (JSchException je) {
            logger.error("Unexpected exception: " + je.getMessage(), je);
    		dataFeedLog.error(je.toString());
    	}
    	catch (SftpException se) {
            logger.error("Unexpected exception: " + se.getMessage(), se);
    		dataFeedLog.error(se.toString());
    	}
    	catch (FileNotFoundException fnfe) {
            logger.error("Unexpected exception: " + fnfe.getMessage(), fnfe);
    		dataFeedLog.error(fnfe.toString());
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

			if (client != null && client.isConnected()) {
				client.quit();
				client.disconnect();		// session.disconnect()? (per example: http://www.jcraft.com/jsch/examples/Sftp.java) 
			}
		}
    }

    private boolean hasProperty(String key) {
    	return (properties.get(key) != null && properties.get(key).length() >= 1);
    }
    
    /** Tests if the sender has already sent the status email */
    public boolean isStatusEmailSent() {
    	return false;
    }
}

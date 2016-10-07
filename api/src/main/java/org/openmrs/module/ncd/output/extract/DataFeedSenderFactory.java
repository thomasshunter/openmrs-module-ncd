package org.openmrs.module.ncd.output.extract;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.MapUtilities;

public class DataFeedSenderFactory {


    private static Log logger = LogFactory.getLog(DataFeedSenderFactory.class);

    // DataFeedSender properties
    //
    // PROP_SENDER_CLASS: the implementation class to be created by the factory
    // PROP_SENDER_DEST_PATHNAME: full pathname and base file name (but no file extension) for the destination file 
    // PROP_SENDER_DEST_EXT: extension for the destination file 
    // PROP_SENDER_ADD_TIMESTAMP: Boolean (true, false): add time stamp to destination pathname?
    // PROP_SENDER_TIMESTAMP_FORMAT: the date/time format string for formatting the time stamp added to destination pathname
    // PROP_SENDER_FTP_HOST: For FTP file transfer, the host name 
    // PROP_SENDER_FTP_PORT: For FTP file transfer, the port 
    // PROP_SENDER_FTP_USERNAME: For FTP file transfer, the user name 
    // PROP_SENDER_FTP_PASSWORD: For FTP file transfer, the password
    // PROP_SENDER_SENDER: alert sender's email address
    // PROP_SENDER_SUCCESS_ALERT_LIST: CSV of email addresses to alert on successful send
    // PROP_SENDER_ERROR_ALERT_LIST: CSV of email addresses to alert on error
    public final static String PROP_SENDER_CLASS = "DataFeedSenderFactory.class";
    public final static String PROP_SENDER_DEST_PATHNAME = "DataFeedSenderFactory.destinationPathname";
    public final static String PROP_SENDER_DEST_EXT = "DataFeedSenderFactory.destinationExtension";
    public final static String PROP_SENDER_ADD_TIMESTAMP = "DataFeedSenderFactory.addTimestamp";
    public final static String PROP_SENDER_TIMESTAMP_FORMAT = "DataFeedSenderFactory.timestampFormat";
    public final static String PROP_SENDER_FTP_HOST = "DataFeedSenderFactory.ftpHost";
    public final static String PROP_SENDER_FTP_PORT = "DataFeedSenderFactory.ftpPort";
    public final static String PROP_SENDER_FTP_USERNAME = "DataFeedSenderFactory.ftpUsername";
    public final static String PROP_SENDER_FTP_PASSWORD = "DataFeedSenderFactory.ftpPassword";
    public final static String PROP_SENDER_SENDER = "DataFeedSenderFactory.sender";
    public final static String PROP_SENDER_SUCCESS_ALERT_LIST = "DataFeedSenderFactory.successAlertList";
    public final static String PROP_SENDER_ERROR_ALERT_LIST = "DataFeedSenderFactory.errorAlertList";

    /** Creates and returns an instance of a class implementing
     * DataFeedSender as selected by the properties supplied.
     */
    public static DataFeedSender getInstance(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        String className = MapUtilities.get(properties, PROP_SENDER_CLASS, DataFeedSenderCopy.class.getName());
        Class<?> clazz = Class.forName(className);
        DataFeedSender inst = (DataFeedSender) clazz.newInstance();
        inst.configure(tempFile, properties, dataFeedLog);

        return inst;
    }
    
    /**
     * Constructs and returns the full pathname for the target file per the properties.
     * 
     * @param properties The DataFeedSender properties.
     * @return The full pathname for the target file.
     */
    public static String constructTargetPathname(Map<String, String> properties) {

    	// The target file pathname is: pathname + [optional: time stamp] + extension  
		String pathname = properties.get(PROP_SENDER_DEST_PATHNAME);
		if (StringUtils.isEmpty(pathname)) {
			logger.error("Property: " + PROP_SENDER_DEST_PATHNAME + " is not specified or empty.");
		}
		String addTimestamp = properties.get(PROP_SENDER_ADD_TIMESTAMP); 
		if (addTimestamp != null && addTimestamp.equalsIgnoreCase("true")) {
			// Get the date format string set by the user, or the default
    		String theFormat = properties.get(PROP_SENDER_TIMESTAMP_FORMAT);
    		SimpleDateFormat dateFormat;
    		if (theFormat != null && theFormat.length()>0) {
        		dateFormat = new SimpleDateFormat(theFormat);
    		}
    		else {
    			// default date/time format
    			dateFormat = new SimpleDateFormat("MMddyyyyHHmmss");
    		}
    		
			pathname += "_" + dateFormat.format(new Date());
		}
		String extension = properties.get(PROP_SENDER_DEST_EXT);
		if (extension != null) {
			if (extension.length() >= 1 && extension.charAt(0) != '.') {
				pathname  += ".";
			}
			pathname += extension;
		}
		
		return pathname;
    }

    /**
     * Splits the given pathname into its constituent paths and returns them.
     *
     * @param path The path to be split.
     * @return String[] containing the constituent paths.
     */
    public static String[] splitPath(String path) {
		String[] subpaths;
		if (path.indexOf('\\') >= 0) {
			subpaths = path.split("\\\\");
		}
		else {
			subpaths = path.split("/");
		}
		return subpaths;
    }
}

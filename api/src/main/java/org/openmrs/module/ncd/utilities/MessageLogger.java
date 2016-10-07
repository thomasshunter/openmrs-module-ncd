package org.openmrs.module.ncd.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;

/**
 * A singleton class that appends HL7 messages (any string, really) to a log
 * file.
 * 
 * Logging works much like log4j's rollover logger. There are global properties to configure:
 * - Whether logging is enabled or disabled.
 * - The pathname of the "current" log file.
 * - The log file size at which a rollover to a new log file occurs. Log files may actually be one message larger
 *   than this size.
 * - The number of log files to keep.
 * 
 * If a change is made to one or more of these properties (via the OpenMRS Manage Global Properties page), the
 * change takes effect more or less immediately. No server restart is required. 
 * 
 * @author ehorstkotte
 */
public class MessageLogger implements GlobalPropertyListener {

    /** Debugging log */
    private static Log log = LogFactory.getLog(MessageLogger.class);

    /* Property names */
	private static final String PNAME_LOG_FILE_PATHNAME = "ncd.nonreportableMessageLog";
	private static final String PNAME_MAX_LOG_FILE_SIZE = "ncd.nonreportableMessageLogMaxSize";
	private static final String PNAME_MAX_LOG_FILES = "ncd.nonreportableMessageLogMaxFiles";
	private static final String PNAME_NONREPORTABLE_LOGGING_ON = "ncd.nonreportableLoggingOn";
	private Set<String> propertyNames;
	
	/* The log file we're currently writing to, if any */
	protected File logFile = null;
	
	/* The Writer used to actually write the messages */
	protected FileWriter logFileWriter = null;

	protected boolean loggingEnabled = false;
	protected String logFilePathname = "";
	protected long maxLogFileSize = 0;
	protected int maxLogFiles = 0;
	
	public MessageLogger() {

		// Create our properties, if necessary
		createProperties();
		
		// Build the set of interesting property names for supportsPropertyName below.
		propertyNames = new HashSet<String>();
		propertyNames.add(PNAME_LOG_FILE_PATHNAME);
		propertyNames.add(PNAME_MAX_LOG_FILE_SIZE);
		propertyNames.add(PNAME_MAX_LOG_FILES);
		propertyNames.add(PNAME_NONREPORTABLE_LOGGING_ON);

		// Register this as a listener for our property.
		NCDUtilities.authenticate();
		AdministrationService adminService = Context.getAdministrationService();
		adminService.addGlobalPropertyListener(this);
		
		// Initialize the state of logging
		try {
			loggingEnabled = PropertyManager.getBoolean(PNAME_NONREPORTABLE_LOGGING_ON);
			logFilePathname = PropertyManager.get(PNAME_LOG_FILE_PATHNAME);
			logFile = new File(logFilePathname);
			maxLogFileSize = StringUtilities.parseSize(PropertyManager.get(PNAME_MAX_LOG_FILE_SIZE));
			maxLogFiles = Integer.parseInt(PropertyManager.get(PNAME_MAX_LOG_FILES));
		}
		catch (PropertyNotFoundException e) {
			log.error("Missing global property: " + e.getMessage(), e);
		}
		
		// We don't have a log file writer until logMessage is called.
		logFileWriter = null;
	}

	public void createProperties() {
		
		PropertyManager.create(PNAME_LOG_FILE_PATHNAME, "nonreportable", "The base full path and filename to log nonreportable messages to. An integer index will be appended to make the filename unique when a rollover occurs.");
		PropertyManager.create(PNAME_MAX_LOG_FILE_SIZE, "10MB", "The maximum size a log file is allowed to grow to. When this size is exceeded, a new log file is created. This is called a rollover.");
		PropertyManager.create(PNAME_MAX_LOG_FILES, "10", "The maximum number of log files to retain. If a rollover occurs and more than this many log files already exist, the oldest log file will be removed.");
		PropertyManager.create(PNAME_NONREPORTABLE_LOGGING_ON, "false", "True to turn logging of nonreportable messages on, false to turn it off.");
	}

	@Override
	public void globalPropertyChanged(GlobalProperty arg0) {

		if (arg0.getProperty().equals(PNAME_NONREPORTABLE_LOGGING_ON)) {
			loggingEnabled = Boolean.parseBoolean(arg0.getPropertyValue());
		}
		else if (arg0.getProperty().equals(PNAME_LOG_FILE_PATHNAME)) {
			logFilePathname = arg0.getPropertyValue();
			logFile = new File(logFilePathname);
		}
		else if (arg0.getProperty().equals(PNAME_MAX_LOG_FILE_SIZE)) {
			maxLogFileSize = StringUtilities.parseSize(arg0.getPropertyValue());
		}
		else if (arg0.getProperty().equals(PNAME_MAX_LOG_FILES)) {
			maxLogFiles = Integer.parseInt(arg0.getPropertyValue());
		}
		
		closeLogFileWriter();
	}

	@Override
	public void globalPropertyDeleted(String arg0) {
		log.error("Operator error: global property \"" + arg0 + "\" removed.");
	}
	
	@Override
	public boolean supportsPropertyName(String arg0) {
		return propertyNames.contains(arg0);
	}

	/** Close the current log file FileWriter, if one is open. */
	protected void closeLogFileWriter() {
	
		if (logFileWriter != null) {

			log.debug("close log file writer.");

			try {
				FileWriter tempFileWriter = logFileWriter;
				logFileWriter = null;
				if (tempFileWriter != null) {
					tempFileWriter.close();
				}
			}
			catch (IOException ioe) {
				
				log.error("cannot close log file writer: " + ioe.getMessage(), ioe);
			}
		}
	}

	/** Open a FileWriter for the current log file, if one is not already open. */
	protected void openLogFileWriter() {

		if (logFileWriter == null) {

			log.debug("open log file writer.");

			FileWriter newLogFileWriter = null;
			try {
				newLogFileWriter = new FileWriter(logFile, true);
			}
			catch (IOException ioe) {
				
				log.error("cannot open new log file writer: " + ioe.getMessage(), ioe);
			}
			logFileWriter = newLogFileWriter;
		}
	}

	/** Get the name of a log file by index.
	 * 
	 * @param index The index of the log file whose name is to be generated.
	 * @return The name of the indexed log file.
	 */
	protected String getLogFilePathname(int index) {
		
		if (index == 0) {
			return logFilePathname;
		}
		else {
			return logFilePathname + "." + index;
		}
	}

	/** Remove a log file by index. Used during rollover handling.
	 * 
	 * @param index The index of the log file to remove.
	 */
	protected void removeLogFile(int index) {
		
		String fileName = getLogFilePathname(index);
		log.debug("remove " + fileName);
		File f = new File(fileName);
		if (f.exists()) {
			f.delete();
		}
	}

	/** Rename a log file from one index to another. Used during rollover handling.
	 * 
	 * @param fromIndex The current log file index.
	 * @param toIndex The log file index to rename the log file to.
	 */
	protected void renameLogFile(int fromIndex, int toIndex) {

		String fromFileName = getLogFilePathname(fromIndex);
		String toFileName = getLogFilePathname(toIndex);
		
		log.debug("rename " + fromFileName + " to " + toFileName);
		
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);
		removeLogFile(toIndex);
		fromFile.renameTo(toFile);
	}

	/** Handles log file "rollover", logging configuration changes and server restarts. */
	protected void handleRollover() {

		// If logging is enabled
		if (loggingEnabled) {

			// If the size of the current log file exceeds the limit
			if (logFile.length() >= maxLogFileSize) {
				
				log.debug("rollover required.");

				// Close the current log file writer, if one is open.
				closeLogFileWriter();

				if (maxLogFiles > 0) {
					
					// Delete the log file that "rolls off" if it exists
					removeLogFile(maxLogFiles - 1);
					
					// Rename existing log files to higher indexes
					for (int index = maxLogFiles - 2; index >= 0; index--) {
					
						renameLogFile(index, index + 1);
					}
				}
			}

			// Get a new writer for the new log file.
			openLogFileWriter();
		}
	}
	
	/** Handle changes to the logging configuration, possible rollovers, and
	 * return a Writer for the current log file.
	 * 
	 * @return A Writer for the log file for the next message to be logged,
	 * or null if the current configuration is invalid or logging is disabled.
	 */
	protected Writer getWriter() {

		handleRollover();

		return logFileWriter;
	}

	/** Creates a copy of a String where any occurrences of the standard HL7
	 * end-of-segment delimiter ('\r') are replaced by the end-of-line
	 * delimiter for the environment the NCD is running in ('\n' or "\r\n").
	 * 
	 * @param message The source message 
	 * @return
	 */
	protected String replaceSegmentDelimiters(String message) {
		String lineSeparator = System.getProperty("line.separator");
		log.debug("line.separator=\"" + StringUtilities.dump(lineSeparator) + "\"");
		return message.replace("\r", lineSeparator);
	}
	
	/** Handle a possible rollover, and append a message to the log file, if logging is enabled.
	 * 
	 * @param message The HL7 message to be logged.
	 */
	public synchronized void logMessage(String message) {

		if (loggingEnabled) {
			
			try {
				Writer writer = getWriter();
				if (writer != null) {
					getWriter().append(replaceSegmentDelimiters(message));
					getWriter().flush();
				}
			}
			catch (IOException ioe) {
				
				log.error("cannot write to message log file: " + ioe.getMessage(), ioe);
			}
		}
	}
}

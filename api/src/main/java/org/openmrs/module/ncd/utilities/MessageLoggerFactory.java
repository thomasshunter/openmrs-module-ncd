package org.openmrs.module.ncd.utilities;

/**
 * Singleton factory wrapper for MessageLogger.
 * 
 * @author ehorstkotte
 */
public class MessageLoggerFactory {

	protected static MessageLogger logger = null;
	
	public static MessageLogger getInstance() {
		
		if (logger == null) {
			
			logger = new MessageLogger();
		}
		
		return logger;
	}
}

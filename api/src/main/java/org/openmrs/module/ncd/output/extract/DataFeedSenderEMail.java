package org.openmrs.module.ncd.output.extract;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.utilities.FileUtilities;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageService;

/** A DataFeedSender implementation that sends the file as an email
 * (MIME) attachment via SMTP.
 */
public class DataFeedSenderEMail implements DataFeedSender {

    private static Log log = LogFactory.getLog(DataFeedSenderEMail.class);

    public static final String PROPNAME_SUBJECT = "DataFeedSenderEmail.subject";
    public static final String PROPNAME_MIMETYPE = "DataFeedSenderEmail.mimetype";

    /** The configured task definition properties */
    private Map<String,String> config;
    
    /** The temp File containing the exported output */
    private File exportedFile;
    
    /** The data feed log */
    private DataFeedLog dataFeedLog;
    
    /** True iff the status email was successfully sent */
    private boolean statusEmailSent = false; 


    public void configure(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog) {
    	
    	this.exportedFile = tempFile;
    	this.config = properties;
    	this.dataFeedLog = dataFeedLog;
    	statusEmailSent = false;
    }

    public void send() {
    	
    	log.debug("send: enter");
    	
		try {
			String sender;
			String recipients;
			String subject;

			sender = getSender();
			recipients = getSuccessRecipients();
			subject = config.get(PROPNAME_SUBJECT);
			if (subject == null) {
				subject = "BUG: PROPNAME_SUBJECT not set.";
			}

			if (recipients != null && recipients.trim().length() > 0) {

		    	log.debug("send: building and sending message.");
		    	
				// Use the OpenMRS message service to create and send the email
				MessageService messageService = Context.getMessageService();
				Message msg = messageService.createMessage(
						recipients,
						sender,
						subject,
						dataFeedLog.messagesToString(),	// body content
						getFileContent(),
						getMIMEType(),
						getFileName()
						);
				messageService.sendMessage(msg);
				
		    	log.debug("send: message sent.");
			}
			
			statusEmailSent = true;
		}
		catch (MessageException me) {
            log.error("Error sending email: " + me.getMessage(), me);
		}
		
    	log.debug("send: exit");
    }

    /** Tests if the sender has already sent the status email */
    public boolean isStatusEmailSent() {
    	return statusEmailSent;
    }

    /** Gathers the entire content of the exported file up into a String and
     * returns that String.
     * @return A String containing the entire content of the exported file.
     */
    private String getFileContent() {
    	
    	try {
    		return FileUtilities.readContentToString(new FileReader(exportedFile));
    	}
    	catch (IOException ioe) {
    		log.error("IOException reading exported file:" + ioe.getMessage(), ioe);
    		return "";
    	}
    }

    /** Gets the MIME type to use for the email attachment containing the
     * exported content.
     * 
     * @return The MIME type to use for the attachment.
     */
    private String getMIMEType() {
    	return config.get(PROPNAME_MIMETYPE);
    }

    /** Gets the filename to encode in the attachment.
     * 
     * @return The filename for the attachment.
     */
    private String getFileName() {
    	
    	return DataFeedSenderFactory.constructTargetPathname(config);
    }
    
    private String getSender() {

    	return config.get(DataFeedSenderFactory.PROP_SENDER_SENDER);
    }

    private String getSuccessRecipients() {
    	
    	return config.get(DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST);
    }
}

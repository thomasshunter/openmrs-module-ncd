package org.openmrs.module.ncd.output.aggrpt;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageService;

public class ReportSenderEmailImpl implements ReportSender {

    public static final String PROP_RECIPIENTS = "ReportSenderEmail.recipients";
    public static final String PROP_SENDER = "ReportSenderEmail.sender";
    public static final String PROP_SUBJECT = "ReportSenderEmail.subject";
    
    private static Log logger = LogFactory.getLog(ReportSenderEmailImpl.class);

    private String recipients = null;
    private String sender = null;
    private String subject = null;
    
    public void configure(Map<String, String> properties) {
        
        recipients = mustHaveProperty(properties, PROP_RECIPIENTS);
        sender = mustHaveProperty(properties, PROP_SENDER);
        subject = mustHaveProperty(properties, PROP_SUBJECT);
    }
    
	public void send(String message) {
		
		try {
			// Use the OpenMRS message service to create and send the email
			MessageService messageService = Context.getMessageService();
			messageService.sendMessage(messageService.createMessage(recipients, sender, subject, message));
		}
		catch (MessageException me) {
			logger.error("Error sending email to: " + recipients);
            logger.error("Error sending email: " + me.getMessage(), me);
		}
	}
	
	private String mustHaveProperty(Map<String, String> properties, String propName) {
	    
	    String value = properties.get(propName);
	    if (value == null) {

	        logger.error("A value for property " + propName + " is required.");
	    }
	    return value;
	}
}

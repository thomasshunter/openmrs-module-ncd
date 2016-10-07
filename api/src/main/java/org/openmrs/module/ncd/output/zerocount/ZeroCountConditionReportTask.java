package org.openmrs.module.ncd.output.zerocount;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.ExportRecipient;
import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.model.ZeroCountCondition;
import org.openmrs.module.ncd.output.aggrpt.DataFormatter;
import org.openmrs.module.ncd.output.aggrpt.DataFormatterFactory;
import org.openmrs.module.ncd.output.aggrpt.ReportSender;
import org.openmrs.module.ncd.output.aggrpt.ReportSenderEmailImpl;
import org.openmrs.module.ncd.output.aggrpt.ReportSenderFactory;
import org.openmrs.module.ncd.output.extract.DataFeedLog;
import org.openmrs.module.ncd.output.extract.DataFeedSender;
import org.openmrs.module.ncd.output.extract.DataFeedSenderEMail;
import org.openmrs.module.ncd.output.extract.DataFeedSenderFactory;
import org.openmrs.module.ncd.output.extract.DataFeedSink;
import org.openmrs.module.ncd.output.extract.DataFeedSinkFactory;
import org.openmrs.module.ncd.utilities.DateRange;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.MapUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageService;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

public class ZeroCountConditionReportTask implements Task {

    private static Log log = LogFactory.getLog(ZeroCountConditionReportTask.class);
    
    // Using an int nesting counter for this is almost certainly overkill
    private int executeNesting = 0;
    
    /** The OpenMRS definition of this task */
    TaskDefinition config = null;
    
    /** The task definition property under which the sampling period is stored */
    public final static String PROP_NAME_PERIOD = "ZeroCountConditionReportTask.period";
    /** The default sampling period, in hours */
    public final static int DEFAULT_PERIOD = 24;
    
    /** The task definition property under which the cutoff date/time for the
     * report/export is stored. */
    public final static String PROP_NAME_CUTOFF_DATETIME = "ZeroCountConditionReportTask.cutoffDateTime";

    // Constants for fiddlage
    private static final String DEFAULT_FAILURE_SUBJECT = "[NCD-ERROR] Zero count condition export failed";
    private static final String DEFAULT_SUCCESS_SUBJECT = "[NCD-SUCCESS] Zero count condition export succeeded";

    private DataFeedLog dataFeedLog = null;

    public void initialize(TaskDefinition config) {

        log.debug("initialize: config=" + config);
        this.config = config;
    }

    public boolean isExecuting() {
        
        // Test if this task is currently executing
        synchronized (this) {
            return executeNesting != 0;
        }
    }

    public void execute() {

        synchronized (this) {
            executeNesting++;
        }
        
		long startTime = System.currentTimeMillis();
		long stopTime = 0;
		boolean error = true;
		boolean statusEmailSent = false;
		dataFeedLog = new DataFeedLog();
        File tempFile = null;
        DataFeedSink dataSink = null;
        
        try {

            NCDUtilities.authenticate();

            log.debug("execute: enter");
    
            int samplingPeriod = getSamplingPeriod();
            Date runDateTime = new Date();
            Date cutoffDateTime = getCutoffDate();
            DateRange samplingWindow = new DateRange();
            samplingWindow.setHigh(cutoffDateTime);
            samplingWindow.setLow(DateUtilities.adjust(samplingWindow.getHigh(), Calendar.HOUR_OF_DAY, -samplingPeriod));
            log.debug("execute: runDateTime=" + runDateTime);
            log.debug("execute: cutoffDateTime=" + cutoffDateTime);
            log.debug("execute: samplingWindow=" + samplingWindow);
            
            // In case we're exporting, sending by email, and it succeeds
            config.setProperty(DataFeedSenderEMail.PROPNAME_SUBJECT, DEFAULT_SUCCESS_SUBJECT);
            
            ConditionDetectorService cds = NCDUtilities.getService();

            log.debug("execute: gather zero count conditions for window=" + samplingWindow);
            
            // Get the zero-count (app, loc, loinc) triples for this window.
            List<ZeroCountCondition> zeroCountConditions = 
                cds.getZeroCountConditions(config, samplingWindow);
            
            // If we're emailing a report
            if (isReport()) {
            	
            	// Build the report parameter map
            	Map<String, Object> data = new HashMap<String, Object>();
            	data.put("period", samplingWindow);
            	data.put("rows", zeroCountConditions);

            	// Format the report
                String report = format(data);

                // Send the report
                send(report);
            }
            // We're exporting
            else {
            
            	// Otherwise we're exporting a CSV file.
            	
                dataFeedLog.info("Report: " + config.getName() + " (id=" + config.getId() + ")");
                
    	    	// Get a temporary file for this export
                tempFile = File.createTempFile("ncdexport", null);

    	        // Set a property to define the output columns as everything the extractor returns
            	config.setProperty(DataFeedSinkFactory.PROP_SINK_OUTPUT_COLS_TO_INCL, ZeroCountCondition.getColumnNames());

        		// Set a property to define the date/time format (overrides the default date/time format)
            	if (StringUtils.isEmpty(config.getProperty(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT))) {
            		config.setProperty(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT, "MM/dd/yyyy");
            	}
        		
    	        // Create and open a data sink to consume the records
    	        dataSink = DataFeedSinkFactory.getInstance(tempFile, config.getProperties(), dataFeedLog);
    	        
    	        // In case we're sending by email and succeed
    	        config.setProperty(DataFeedSenderEMail.PROPNAME_MIMETYPE, dataSink.getMIMEType());
    	        
    	        // Pass the records to the sink
    	        for (ZeroCountCondition row : zeroCountConditions) {

    	        	dataSink.append(row.toMap());
    	        }
    	        
    	        // Close the data sink
    	        dataSink.close();
    	        stopTime = System.currentTimeMillis();

    	        // If there are no rows to send
    	        if (dataSink.isEmpty()) {
    	        	
            		dataFeedLog.info("   No rows to report.");
    	        }
    	        else {

    	        	// Create the data sender, and send the file
        	        dataSink = null;
	    	        DataFeedSender dataSender = DataFeedSenderFactory.getInstance(tempFile, config.getProperties(), dataFeedLog);
	    	        dataSender.send();
        	        statusEmailSent = dataSender.isStatusEmailSent();
    	        }

    	        dataSink = null;
    	        dataFeedLog.info("   Export Time: " + (stopTime - startTime)/1000 + "." + (stopTime - startTime) % 1000 + " (secs)");
            }
            
	        stopTime = System.currentTimeMillis();
	        error = false;
    
            log.debug("execute: exit");
        }
        catch (Exception e) {
            
        	log.error("Unexpected exception: " + e.getMessage(), e);
        }
        finally {
        	
    		if (stopTime == 0) {
    			stopTime = System.currentTimeMillis();
    		}
    		
        	recordStatus(startTime, stopTime, error);
        	if (!statusEmailSent) {
        		sendStatusEmail();
        	}
        }
        
        synchronized (this) {
            executeNesting--;
        }
    }

    public void shutdown() {
        
        // If the task is executing, stop it as soon as possible.
        log.debug("shutdown: enter");
        log.debug("shutdown: exit");
    }

    /** Helper to construct the report body as a String based on the report
     * source data.
     * 
     * @param data The source data for the report - The app/loc/condition
     * triples with unusual rates, along with their sampled count, historical
     * count, sampled rate and historical rate.
     * @return A String containing the formatted report body.
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     */
    private String format(Map<String, Object> data) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        
        DataFormatter formatter = DataFormatterFactory.getInstance(config.getProperties(), "zerocount.template");
        return formatter.format(data);
    }
    
    /** Helper to send the report as configured. Currently this always means
     * as an email message.
     * 
     * @param report The body of the report to be sent.
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     */
    private void send(String report) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        
        // Create a report sender and send the formatted report
        ReportSender reportSender = ReportSenderFactory.getInstance(config.getProperties());
        reportSender.send(report);
    }
    
    /** Add a run completion status record to the database for an (attempted)
     * execution of this reporting task.
     * 
     * @param taskdef
     * @param startTime
     * @param stopTime
     * @param error
     */
    private void recordStatus(long startTime, long stopTime, boolean error) {

    	String message;
    	if (!error) {
    		message = "Zero count report succeeded: generated report in " + ((stopTime - startTime)/1000) + " seconds";
    	} else {
    		message = "Zero count rate report failed (check ncd.log for exceptions)";
    	}

    	ConditionDetectorService cds = NCDUtilities.getService();
    	TaskRunStatus status = new TaskRunStatus(config, new Date(startTime),  new Date(stopTime), !error, message);
    	cds.addTaskStatus(status);

    	if (error) {
        	AlertSummary alertSummary = new AlertSummary(cds.findAlertTypeById(ConditionDetectorService.alertTypeReportError),
        	        config.getName() + ": " + message, null, ConditionDetectorService.alertIdentityZeroCount + "[" + config.getId() + "]");
        	cds.addAlertSummary(alertSummary);
    	}
    	
    	recordRecipientStatus(status);
    }
    
    private void recordRecipientStatus(TaskRunStatus status) {
    	
		// If export successfully generated
		if (!dataFeedLog.hasError()) {
			String recipients = getSuccessRecipients();
			String[] exportRecipients = StringUtils.split(recipients, ',');

			// For each export recipient
			ConditionDetectorService cds = NCDUtilities.getService();
			for (String recipient : exportRecipients) {
				
				// Record the successful export recipient in association with the task run status 
				cds.addExportRecipient(new ExportRecipient(status, recipient.trim()));
			}
		}
    }
    
    public Date getCutoffDate() {

    	String cutoffString = StringUtilities.trim(
    			config.getProperties().get(PROP_NAME_CUTOFF_DATETIME));

        log.debug("cutoffString=" + cutoffString);

        Date cutoff = new Date();
    	if (cutoffString != null) {

    		cutoff = DateUtilities.parseDateTime(cutoffString);
    		
    		if (cutoff == null) {

        		cutoff = DateUtilities.parseDate(cutoffString);
    		}
    	}

    	log.debug("cutoff=" + cutoff);
    	
    	return cutoff;
    }

    private int getSamplingPeriod() {
    	
    	int period = MapUtilities.get(config.getProperties(), PROP_NAME_PERIOD, DEFAULT_PERIOD);

        log.debug("period=" + period);

        return period;
    }
    
    private void sendStatusEmail() {
    	
		try {
			String sender;
			String recipients;
			String subject;

			sender = getSender();
			
			if (dataFeedLog.hasError()) {
				recipients = getErrorRecipients();
				subject = DEFAULT_FAILURE_SUBJECT;
			}
			else {
				recipients = getSuccessRecipients();
				subject = DEFAULT_SUCCESS_SUBJECT;
			}

			if (recipients != null && recipients.trim().length() > 0) {

				if (dataFeedLog.hasError() || !isReport()) {
					
					// Use the OpenMRS message service to create and send the email
					MessageService messageService = Context.getMessageService();
					messageService.sendMessage(messageService.createMessage(recipients, sender, subject, dataFeedLog.messagesToString()));
				}
			}
		}
		catch (MessageException me) {
            log.error("Error sending email alert: " + me.getMessage(), me);
		}
    }

    /** Tests if we are writing a report or not */
    private boolean isReport() {

    	return config.getProperty(DataFeedSinkFactory.PROP_SINK_CLASS).equals("Report");
    }

    private String getSender() {

    	String sender;
    	if (isReport()) {
    		
    		sender = config.getProperty(ReportSenderEmailImpl.PROP_SENDER);
    	}
    	else {
    		
    		sender = config.getProperty(DataFeedSenderFactory.PROP_SENDER_SENDER);
    	}
    	
    	return sender;
    }

    private String getSuccessRecipients() {
    	
    	String recipients;
    	if (isReport()) {
    		
    		recipients = config.getProperty(ReportSenderEmailImpl.PROP_RECIPIENTS);
    	}
    	else {

    		recipients = config.getProperty(DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST);
    	}

    	return recipients;
    }
    
    private String getErrorRecipients() {
    	
    	String recipients;
    	if (isReport()) {
    		
    		recipients = config.getProperty(ReportSenderEmailImpl.PROP_RECIPIENTS);
    	}
    	else {

    		recipients = config.getProperty(DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST);
    	}

    	return recipients;
    }

	@Override
	public TaskDefinition getTaskDefinition() {
		return config;
	}
}

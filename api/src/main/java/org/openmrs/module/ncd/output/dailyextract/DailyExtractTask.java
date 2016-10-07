package org.openmrs.module.ncd.output.dailyextract;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.output.extract.DataFeedExtractor;
import org.openmrs.module.ncd.output.extract.DataFeedLog;
import org.openmrs.module.ncd.output.extract.DataFeedSender;
import org.openmrs.module.ncd.output.extract.DataFeedSenderFactory;
import org.openmrs.module.ncd.output.extract.DataFeedSink;
import org.openmrs.module.ncd.output.extract.DataFeedSinkFactory;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageService;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

public class DailyExtractTask implements Task {

    private static Log logger = LogFactory.getLog(DailyExtractTask.class);

    // Using an int nesting counter for this is almost certainly overkill
    private int executeNesting = 0;
    
    private DataFeedLog dataFeedLog = null;
    
    TaskDefinition config;

    public void initialize(TaskDefinition config) {
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

        File tempFile = null;
    	DataFeedExtractor dataExtractor = null;
        DataFeedSink dataSink = null;
		long startTime = System.currentTimeMillis();
		long stopTime = 0;

		dataFeedLog = new DataFeedLog();
		TaskRunStatus status = null;
        try {
            
            NCDUtilities.authenticate();
    		status = addStatus(config);

            dataFeedLog.info("Report: " + config.getName() + " (id=" + config.getId() + ")");
            
	    	// Get a temporary file for this export
            tempFile = File.createTempFile("ncdexport", null);

	        // Create and open data extractor that fetches the reportable results
	        dataExtractor = DailyExtractorFactory.getInstance(config, config.getProperties(), dataFeedLog);

	        // Set a property to define the output columns as everything the extractor returns
        	config.setProperty(DataFeedSinkFactory.PROP_SINK_OUTPUT_COLS_TO_INCL, DailyExtractorFactory.getOutputColumnNames());

    		// Set a property to define the date/time format (overrides the default date/time format)
        	if (StringUtils.isEmpty(config.getProperty(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT))) {
        		config.setProperty(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT, "MM/dd/yyyy");
        	}
    		
	        // Create and open a data sink to consume the records
	        dataSink = DataFeedSinkFactory.getInstance(tempFile, config.getProperties(), dataFeedLog);
	        
	        // Extract the records and pass them to the sink
	        dataExtractor.extract(dataSink, status);
	        
	        // Close the data sink
	        dataSink.close();
	        stopTime = System.currentTimeMillis();

	        // Create the data sender, and send the file (if any rows were extracted)
	        if (!dataSink.isEmpty()) {
		        dataSink = null;
		        DataFeedSender dataSender = DataFeedSenderFactory.getInstance(tempFile, config.getProperties(), dataFeedLog);
		        dataSender.send();
	        }
	        dataSink = null;
    		dataFeedLog.info("   Export Time: " + (stopTime - startTime)/1000 + "." + (stopTime - startTime) % 1000 + " (secs)");
        }
        catch (IOException ioe) {
            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
        	dataFeedLog.error(ioe.toString());
        }
        catch (IllegalAccessException iae) {
            logger.error("Unexpected exception: " + iae.getMessage(), iae);
        	dataFeedLog.error(iae.toString());
        }
        catch (InstantiationException ie) {
            logger.error("Unexpected exception: " + ie.getMessage(), ie);
        	dataFeedLog.error(ie.toString());
        }
        catch (ClassNotFoundException cnfe) {
            logger.error("Unexpected exception: " + cnfe.getMessage(), cnfe);
        	dataFeedLog.error(cnfe.toString());
        }
        finally {

        	try {
	            
	        	if (dataExtractor != null) {
	        		dataExtractor.close();
	        	}
	        	
	        	if (dataSink != null) {
	        		dataSink.close();
	        	}
	        	
	        	if (tempFile != null) {
	        		if (!tempFile.delete()) {
	        			logger.warn("Warning: failed to delete temporary file: " + tempFile.getAbsolutePath());
	        			dataFeedLog.info("Warning: failed to delete temporary file: " + tempFile.getAbsolutePath());
	        			tempFile.deleteOnExit();
	        		}
	        	}
        	}
        	catch (Exception e) {
        	
	            logger.error("Unexpected exception: " + e.getMessage(), e);
        		dataFeedLog.error(e.toString());
        	}
        	finally {
        		if (stopTime == 0) {
        			stopTime = System.currentTimeMillis();
        		}
	        	recordStatus(status, dataFeedLog, startTime, stopTime);
	        	alert(status);
        	}
        }
        
        synchronized (this) {

            executeNesting--;
        }
    }

    private TaskRunStatus addStatus(TaskDefinition taskdef) {

    	String message = "Place holder to be updated after task completion.";
    	
    	ConditionDetectorService cds = NCDUtilities.getService();
    	TaskRunStatus status = new TaskRunStatus(taskdef, false, message);
    	cds.addTaskStatus(status);
    	return status;
    }
    
    private void recordStatus(TaskRunStatus status, DataFeedLog dataFeedLog, long startTime, long stopTime) {

    	String message;
    	if (!dataFeedLog.hasError()) {
    		message = "Daily Export succeeded: " + dataFeedLog.getRowCount() + " records in " + ((stopTime - startTime)/1000) + " seconds";
    	} else {
    		message = "Daily Export failed (see error alert email for details)";
    	}
    	
    	ConditionDetectorService cds = NCDUtilities.getService();
    	status.setStarted(new Date(startTime));
    	status.setEnded(new Date(stopTime));
    	status.setSucceeded(!dataFeedLog.hasError());
    	status.setMessage(message);
    	
    	cds.addTaskStatus(status);

    	if (dataFeedLog.hasError()) {
	    	AlertSummary alertSummary = new AlertSummary(cds.findAlertTypeById(ConditionDetectorService.alertTypeReportError),
	    							status.getTask().getName() + ": " + message, dataFeedLog.messagesToString(), ConditionDetectorService.alertIdentityDailyExpRpt + "[" + status.getTask().getId() + "]");
	    	cds.addAlertSummary(alertSummary);
    	}
    }
    
    private void alert(TaskRunStatus status) {
    	
		try {
			String sender;
			String recipients;
			String subject;

			sender = config.getProperty(DataFeedSenderFactory.PROP_SENDER_SENDER);
			
			if (dataFeedLog.hasError()) {
				recipients = config.getProperty(DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST);
				subject = "[NCD-ERROR] Daily export failed";
			}
			else {
				recipients = config.getProperty(DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST);
				subject = "[NCD-SUCCESS] Daily export succeeded";
			}
			
			if (recipients != null && recipients.trim().length() > 0) {
				
				// Use the OpenMRS message service to create and send the email
				MessageService messageService = Context.getMessageService();
				messageService.sendMessage(messageService.createMessage(recipients, sender, subject, dataFeedLog.messagesToString()));
			}
		}
		catch (MessageException me) {
            logger.error("Error sending email alert: " + me.getMessage(), me);
		}
    }
    
    public void shutdown() {
        
        // TODO If the task is executing, stop it as soon as possible.
    }

	@Override
	public TaskDefinition getTaskDefinition() {
		return config;
	}
}

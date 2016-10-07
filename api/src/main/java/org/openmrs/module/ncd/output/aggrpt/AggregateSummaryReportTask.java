package org.openmrs.module.ncd.output.aggrpt;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

public class AggregateSummaryReportTask implements Task {

    private static Log logger = LogFactory.getLog(AggregateSummaryReportTask.class);

    // Using an int nesting counter for this is almost certainly overkill
    private int executeNesting = 0;
    
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

    /** Execute the task once */
    public void execute() {
        
        synchronized (this) {

            executeNesting++;
        }

        long startTime = System.currentTimeMillis();
        long stopTime = 0;
        boolean error = true;
        try {
            
            NCDUtilities.authenticate();
            
            DataExtractor dataExtractor = null; 
            try {
                // Create a data extractor and extract the report data
                dataExtractor = 
                    DataExtractorFactory.getInstance(config.getProperties());
            }
            catch (Exception e) {

                logger.error("Unexpected exception: " + e.getMessage(), e);
                return;
            }

            // Create a report formatter and format the report data
            DataFormatter dataFormatter = null;
            try {
                dataFormatter = DataFormatterFactory.getInstance(config.getProperties(), "aggrpt.template");
            }
            catch (Exception e) {
                logger.error("Unexpected exception: " + e.getMessage(), e);
                return;
            }

            String report = dataFormatter.format(dataExtractor.extract());
            //System.out.print(report);
            
            // Create a report sender and send the formatted report
            ReportSender reportSender = null;
            try {
                reportSender = ReportSenderFactory.getInstance(config.getProperties());
            }
            catch (Exception e) {
                logger.error("Unexpected exception: " + e.getMessage(), e);
                return;
            }

            reportSender.send(report);
            
            stopTime = System.currentTimeMillis();
            error = false;
        }
        catch (Exception e) {
            
            logger.error("Unexpected exception: " + e.getMessage(), e);
        }
        finally {
        
            if (stopTime == 0) {
                stopTime = System.currentTimeMillis();
            }
            recordStatus(config, startTime, stopTime, error);
        }
        
        synchronized (this) {

            executeNesting--;
        }
    }

    private void recordStatus(TaskDefinition taskdef, long startTime, long stopTime, boolean error) {

        String message;
        if (!error) {
            message = "Aggregate summary report succeeded: generated report in " + ((stopTime - startTime)/1000) + " seconds";
        } else {
            message = "Aggregate summary report failed (check ncd.log for exceptions)";
        }

        ConditionDetectorService cds = NCDUtilities.getService();
        TaskRunStatus status = new TaskRunStatus(taskdef, new Date(startTime),  new Date(stopTime), !error, message);
        cds.addTaskStatus(status);

        if (error) {
            AlertSummary alertSummary = new AlertSummary(cds.findAlertTypeById(ConditionDetectorService.alertTypeReportError),
                                    taskdef.getName() + ": " + message, null, ConditionDetectorService.alertIdentityAggRpt + "[" + taskdef.getId() + "]");
            cds.addAlertSummary(alertSummary);
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

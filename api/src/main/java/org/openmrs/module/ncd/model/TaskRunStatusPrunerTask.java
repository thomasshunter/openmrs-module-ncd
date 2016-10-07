package org.openmrs.module.ncd.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

public class TaskRunStatusPrunerTask implements Task {

    private static Log logger = LogFactory.getLog(TaskRunStatusPrunerTask.class);

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

    public void execute() {
        synchronized (this) {

            executeNesting++;
        }

        try {
            
            // Start the clock
            long startTime = System.currentTimeMillis();

            NCDUtilities.authenticate();

            // Prune the task run status
            AdministrationService adminService = Context.getAdministrationService();
            int maxAgeDays = Integer.parseInt(adminService.getGlobalProperty("ncd.maxTaskRunStatusAge")); 
            NCDUtilities.getService().pruneTaskStatus(maxAgeDays);

            // wave bye-bye
            logger.info("Pruned task run status in " + (System.currentTimeMillis() - startTime) + " ms.");
        }
        catch (Exception e) {
            
            logger.error("Unexpected Exception:" + e.getMessage(), e);
        }
        
        synchronized (this) {

            executeNesting--;
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

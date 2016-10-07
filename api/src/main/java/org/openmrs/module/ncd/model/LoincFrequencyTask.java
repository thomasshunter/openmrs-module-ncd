package org.openmrs.module.ncd.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

/** Simple OpenMRS task to peridiocally flush the loinc frequency map to
 * the database.
 */
public class LoincFrequencyTask implements Task {

    private static Log logger = LogFactory.getLog(LoincFrequencyTask.class);

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

            // Flush the map
            NCDUtilities.getService().saveCodeFrequencyMap();

            // wave bye-bye
            logger.info("Flushed the loinc frequency map in " + (System.currentTimeMillis() - startTime) + " ms.");
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

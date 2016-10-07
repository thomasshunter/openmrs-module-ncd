package org.openmrs.module.ncd.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.utilities.ContextUtilities;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

public class AdhocReport extends Thread {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
                
	private TaskDefinition taskdef;
	
	AdhocReport(TaskDefinition taskdef) {
		this.taskdef = taskdef;
	}

	public void run() {
		
	    try {
	        Task task = (Task) Class.forName(taskdef.getTaskClass()).newInstance();
	        task.initialize(taskdef);
	        ContextUtilities.openSession();
	        task.execute();
	    }
	    catch (Exception e) {
	        
	        log.error("creating task class instance: " + e.getMessage(), e);
	    }
	    finally {
	    	Context.closeSession();
	    }
	}
}

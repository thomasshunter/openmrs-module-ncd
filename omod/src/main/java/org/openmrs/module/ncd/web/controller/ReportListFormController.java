/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ncd.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.MonitoredCondition;
import org.openmrs.module.ncd.output.aggrpt.AggregateSummaryReportTask;
import org.openmrs.module.ncd.output.condrates.ConditionRateReportTask;
import org.openmrs.module.ncd.output.dailyextract.DailyExtractTask;
import org.openmrs.module.ncd.output.datasource.DataSourceReportTask;
import org.openmrs.module.ncd.output.extract.ExtractTask;
import org.openmrs.module.ncd.output.zerocount.ZeroCountConditionReportTask;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/basicmoduleForm.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
@SuppressWarnings("deprecation")
public class ReportListFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

    	// this method doesn't return any extra data right now, just an empty map
		return new HashMap<String,Object>();
	}


	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {		

		@SuppressWarnings("unused")
        HttpSession httpSession = request.getSession();
	    
        MessageSourceAccessor msa = getMessageSourceAccessor();
        String action = request.getParameter("action");
        
        ReportList reportList = (ReportList) object;
        
        SchedulerService scheduler = Context.getSchedulerService();
        ConditionDetectorService ncd = NCDUtilities.getService();
        
        // TODO validation and errors
        //
        // Copy - exactly one selection required
        // Delete - greater than or equal to one selection required
        // Disable - greater than or equal to one selection required
        // Enable - greater than or equal to one selection required
        // Run Now - greater than or equal to one selection required
        //
        // TODO - "Are you sure?" on delete?

        // If the select all button was clicked
        if (action.equals(msa.getMessage("ncd.pages.reportlist.list.buttons.selectall"))) {

        	for (ReportSummary summary : reportList.getReportSummaries()) {
        		summary.setSelected(true);
        	}

       		return showForm(request, response, exceptions);
        } 
        // Else if the select none button was clicked
        else if (action.equals(msa.getMessage("ncd.pages.reportlist.list.buttons.selectnone"))) {

        	for (ReportSummary summary : reportList.getReportSummaries()) {
        		summary.setSelected(false);
        	}

       		return showForm(request, response, exceptions);
        } 
        // Else if the copy button was clicked
        else if (action.equals(msa.getMessage("ncd.pages.reportlist.list.buttons.copy"))) {

        	for (ReportSummary summary : reportList.getReportSummaries()) {
        		if (summary.getSelected().equals(true)) {
        			
        			TaskDefinition oldtask = scheduler.getTask(summary.getId());
        			TaskDefinition newtask = new TaskDefinition();

        			newtask.setName("[Copy of] " + oldtask.getName());
        			newtask.setDescription(oldtask.getDescription());
        			newtask.setTaskClass(oldtask.getTaskClass());
        			newtask.setRepeatInterval(oldtask.getRepeatInterval());
        			newtask.setStartTimePattern(oldtask.getStartTimePattern());
        			newtask.setStartOnStartup(false);
        			newtask.setStarted(false);
        			newtask.getProperties().putAll(oldtask.getProperties());

        			List<MonitoredCondition> monitoredConditions =
        				ncd.getMonitoredConditions(oldtask);
        			
        			ncd.saveReport(newtask, monitoredConditions);
        		}
        	}

            // Refresh the list page
            return new ModelAndView(new RedirectView(getSuccessView()));
        } 
        // If the delete button was clicked
        else if (action.equals(msa.getMessage("ncd.pages.reportlist.list.buttons.delete"))) {

        	for (ReportSummary summary : reportList.getReportSummaries()) {
        		if (summary.getSelected().equals(true)) {
        			scheduler.deleteTask(summary.getId());
        		}
        	}

            // Refresh the list page
            return new ModelAndView(new RedirectView(getSuccessView()));
        } 
        // Else if the disable button was clicked
        else if (action.equals(msa.getMessage("ncd.pages.reportlist.list.buttons.disable"))) {

        	for (ReportSummary summary : reportList.getReportSummaries()) {
        		if (summary.getSelected().equals(true)) {

        			// shutdownTask() = "cancel a scheduled task"
        			// my interpretation: stop executing the task until it is scheduled (started) again
        			TaskDefinition task = scheduler.getTask(summary.getId());
        			task.setStartOnStartup(new Boolean(false));
        			scheduler.saveTask(task);
        			scheduler.shutdownTask(task);
        		}
        	}

            // Refresh the list page
            return new ModelAndView(new RedirectView(getSuccessView()));
        } 
        // Else if the enable button was clicked
        else if (action.equals(msa.getMessage("ncd.pages.reportlist.list.buttons.enable"))) {

        	for (ReportSummary summary : reportList.getReportSummaries()) {
        		if (summary.getSelected().equals(true)) {

        			// scheduleTask() = "start a scheduled task"
        			// my interpretation: start executing the task per the specified start time and repeat interval
        			TaskDefinition task = scheduler.getTask(summary.getId());
        			task.setStartOnStartup(new Boolean(true));
        			scheduler.saveTask(task);
        			scheduler.scheduleTask(task);
        		}
        	}

            // Refresh the list page
            return new ModelAndView(new RedirectView(getSuccessView()));
        } 
        // Else if the run now button was clicked
        else if (action.equals(msa.getMessage("ncd.pages.reportlist.list.buttons.runnow"))) {

        	for (ReportSummary summary : reportList.getReportSummaries()) {
        		if (summary.getSelected().equals(true)) {
        			
        			// Run the adhoc report asynchronously in a new thread
        			TaskDefinition taskdef = scheduler.getTask(summary.getId());
        			AdhocReport adhocReport = new AdhocReport(taskdef);
        			adhocReport.start();
        		}
        	}

            // Refresh the list page
            return new ModelAndView(new RedirectView(getSuccessView()));
        }
        else {

            // I don't know what the heck happened. Ignore it and refresh.
            return new ModelAndView(new RedirectView(getFormView()));
        }
    }


    /**
     * This class returns the form backing object.  This can be a string, a boolean, or a normal
     * java pojo.
     * 
     * The type can be set in the /config/moduleApplicationContext.xml file or it can be just
     * defined by the return type of this method
     * 
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    @Override
	protected ReportList formBackingObject(HttpServletRequest request) throws Exception { 
    	
        ReportList reportList = new ReportList();
        
        ArrayList<ReportSummary> summaries = new ArrayList<ReportSummary>();
        
        Collection<TaskDefinition> tasks =
            Context.getSchedulerService().getRegisteredTasks();
        
        Map<String, String> reportTypeByClass = new HashMap<String, String>();
        reportTypeByClass.put(AggregateSummaryReportTask.class.getName(), "ncd.refdata.report.type.summary");
        reportTypeByClass.put(ExtractTask.class.getName(), "ncd.refdata.report.type.export");
        reportTypeByClass.put(ConditionRateReportTask.class.getName(), "ncd.refdata.report.type.condrate");
        reportTypeByClass.put(DailyExtractTask.class.getName(), "ncd.refdata.report.type.daily");
        reportTypeByClass.put(ZeroCountConditionReportTask.class.getName(), "ncd.refdata.report.type.zerocount");
        reportTypeByClass.put(DataSourceReportTask.class.getName(), "ncd.refdata.report.type.datasource");
       
        for (TaskDefinition taskDef : tasks) {
            
            String taskClass = taskDef.getTaskClass();
            String reportType = reportTypeByClass.get(taskClass);
            if (reportType != null) {
                
                ReportSummary reportSummary = new ReportSummary();
                if (taskDef.getStarted().equals(true)) {
                	reportSummary.setEnabled("ncd.pages.reportlist.list.enabled.true");
                } else {
                	reportSummary.setEnabled("ncd.pages.reportlist.list.enabled.false");
                }
                reportSummary.setId(taskDef.getId());
                reportSummary.setName(taskDef.getName());
                reportSummary.setType(reportType);
                summaries.add(reportSummary);
            }
        }

        reportList.setReportSummaries(summaries);
        reportList.sortBy(ReportSummary.getComparatorByName());
        
		// this object will be made available to the jsp page under the variable name
		// that is defined in the /metadata/moduleApplicationContext.xml file 
		// under the "commandName" tag
        return reportList;
    }
}
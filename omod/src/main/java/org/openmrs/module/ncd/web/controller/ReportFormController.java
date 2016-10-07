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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.critic.ReportResult.ReportResultStatus;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.MonitoredCondition;
import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.output.aggrpt.AggregateSummaryReportTask;
import org.openmrs.module.ncd.output.aggrpt.DataExtractorFakeAll;
import org.openmrs.module.ncd.output.aggrpt.DataExtractorFakeCount;
import org.openmrs.module.ncd.output.aggrpt.DataExtractorImpl;
import org.openmrs.module.ncd.output.condrates.ConditionRateReportTask;
import org.openmrs.module.ncd.output.dailyextract.DailyExtractTask;
import org.openmrs.module.ncd.output.datasource.DataSourceReportTask;
import org.openmrs.module.ncd.output.extract.DataFeedExtractorFake;
import org.openmrs.module.ncd.output.extract.DataFeedExtractorOld;
import org.openmrs.module.ncd.output.extract.DataFeedSenderCopy;
import org.openmrs.module.ncd.output.extract.DataFeedSenderEMail;
import org.openmrs.module.ncd.output.extract.DataFeedSenderFTP;
import org.openmrs.module.ncd.output.extract.DataFeedSenderSFTP;
import org.openmrs.module.ncd.output.extract.DataFeedSinkAccess;
import org.openmrs.module.ncd.output.extract.DataFeedSinkDelimited;
import org.openmrs.module.ncd.output.extract.DataFeedSinkHL7;
import org.openmrs.module.ncd.output.extract.ExtractTask;
import org.openmrs.module.ncd.output.zerocount.ZeroCountConditionReportTask;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/basicmoduleForm.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
@SuppressWarnings("deprecation")
public class ReportFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    // Move this to message.properties or OpenmrsConstants
    private static String DEFAULT_DATE_PATTERN = "MM/dd/yyyy HH:mm:ss";
    private static DateFormat DEFAULT_DATE_FORMAT = null;
    
    private static Long nextFakeId = -1L;
    private static final String noConditionName = "-Choose condition-";
    private static final String anyConditionName = "*";

    @Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        
        NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
        binder.registerCustomEditor(java.lang.Double.class,
                new CustomNumberEditor(java.lang.Double.class, nf, true));
        binder.registerCustomEditor(java.util.Date.class, 
                new CustomDateEditor(DateUtilities.getDateTimeFormat(), true));
    }

    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

        Map<String, Object> refData = new HashMap<String,Object>();

        refData.put("repeatIntervalUnits", getRepeatIntervalUnits());
        refData.put("reportTypes", getReportTypes());
        refData.put("dataExtractors", getDataExtractors());
        refData.put("dataFeedExtractors", getDataFeedExtractors());
        refData.put("crrDataFeedSinks", getCrrDataFeedSinks());
        refData.put("dataFeedSinks", getDataFeedSinks());
        refData.put("dataFeedSenders", getDataFeedSenders());
        refData.put("crrDataFeedSenders", getCrrDataFeedSenders());
        refData.put("recentResultIntervalUnits", getRecentResultIntervalUnits());
        refData.put("debugging", new Boolean(NCDUtilities.debugging()));
        refData.put("conditions", getConditions());
        refData.put("anyConditions", getAnyConditions());
        refData.put("institutions", getInstitutions());
        refData.put("lastrundate", getLastSuccessfulRunDate(obj));
        refData.put("reportStatusTypes", getReportStatusTypes());
        refData.put("booleans", getBooleans());

       	return refData;
	}

    private Map<String, String> getRepeatIntervalUnits() {

    	MessageSourceAccessor msa = getMessageSourceAccessor();

        Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put("s",
                msa.getMessage("ncd.refdata.repeat.interval.unit.seconds"));
        data.put("m",
                msa.getMessage("ncd.refdata.repeat.interval.unit.minutes"));
        data.put("h",
                msa.getMessage("ncd.refdata.repeat.interval.unit.hours"));
        data.put("d",
                msa.getMessage("ncd.refdata.repeat.interval.unit.days"));
        
        return data;
    }
    
    private Map<String, String> getRecentResultIntervalUnits() {

    	MessageSourceAccessor msa = getMessageSourceAccessor();

        Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put("m",
                msa.getMessage("ncd.refdata.repeat.interval.unit.minutes"));
        data.put("h",
                msa.getMessage("ncd.refdata.repeat.interval.unit.hours"));
        data.put("d",
                msa.getMessage("ncd.refdata.repeat.interval.unit.days"));
        
        return data;
    }
    
    private Map<String, String> getReportTypes() {
        
        MessageSourceAccessor msa = getMessageSourceAccessor();

        Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put(AggregateSummaryReportTask.class.getName(),
                msa.getMessage("ncd.refdata.report.type.summary"));
        data.put(DailyExtractTask.class.getName(), 
                msa.getMessage("ncd.refdata.report.type.daily"));
        data.put(DataSourceReportTask.class.getName(), 
                msa.getMessage("ncd.refdata.report.type.datasource"));
        data.put(ExtractTask.class.getName(), 
                msa.getMessage("ncd.refdata.report.type.export"));
        data.put(ConditionRateReportTask.class.getName(), 
                msa.getMessage("ncd.refdata.report.type.condrate"));
        data.put(ZeroCountConditionReportTask.class.getName(), 
                msa.getMessage("ncd.refdata.report.type.zerocount"));
        
        return data;
    }

    private Map<String, String> getDataExtractors() {
        
        MessageSourceAccessor msa = getMessageSourceAccessor();

        Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put(DataExtractorImpl.class.getName(), 
                msa.getMessage("ncd.refdata.report.extractor.normal"));

        if (NCDUtilities.debugging()) {
	        data.put(DataExtractorFakeAll.class.getName(), 
	                msa.getMessage("ncd.refdata.report.extractor.fakeall"));
	        
	        data.put(DataExtractorFakeCount.class.getName(), 
	                msa.getMessage("ncd.refdata.report.extractor.fakecount"));
        }

        return data;
    }

    private Map<String, String> getDataFeedExtractors() {
        
        MessageSourceAccessor msa = getMessageSourceAccessor();

        Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put(DataFeedExtractorOld.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedextractor.old"));
        
        if (NCDUtilities.debugging()) {
	        data.put(DataFeedExtractorFake.class.getName(), 
	        	msa.getMessage("ncd.refdata.report.feedextractor.fake"));
        }

        return data;
    }

    private Map<String, String> getDataFeedSinks() {
        
        MessageSourceAccessor msa = getMessageSourceAccessor();

        Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put(DataFeedSinkDelimited.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsink.delimited"));
        
        data.put(DataFeedSinkHL7.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsink.hl7"));
        
        data.put(DataFeedSinkAccess.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsink.access"));

        return data;
    }

    private Map<String, String> getCrrDataFeedSinks() {
        
        MessageSourceAccessor msa = getMessageSourceAccessor();

        Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put("Report", 
                msa.getMessage("ncd.refdata.report.feedsink.report"));
        
        data.put(DataFeedSinkDelimited.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsink.delimited"));

        return data;
    }

    private Map<String, String> getDataFeedSenders() {
        
        MessageSourceAccessor msa = getMessageSourceAccessor();

        Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put(DataFeedSenderCopy.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsender.copy"));
        
        data.put(DataFeedSenderFTP.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsender.ftp"));
        
        data.put(DataFeedSenderSFTP.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsender.sftp"));

//        data.put(DataFeedSenderEMail.class.getName(), 
//                msa.getMessage("ncd.refdata.report.feedsender.email"));

        return data;
    }

    private Map<String, String> getCrrDataFeedSenders() {
        
        MessageSourceAccessor msa = getMessageSourceAccessor();

        Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put(DataFeedSenderCopy.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsender.copy"));
        
        data.put(DataFeedSenderFTP.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsender.ftp"));
        
        data.put(DataFeedSenderSFTP.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsender.sftp"));

        data.put(DataFeedSenderEMail.class.getName(), 
                msa.getMessage("ncd.refdata.report.feedsender.email"));

        return data;
    }

    private List<String> getConditions() {
    	
        List<String> data = new ArrayList<String>();

        data.add(noConditionName);
        data.addAll(NCDUtilities.getService().getAllConditionNames());

        return data;
    }

    private List<String> getAnyConditions() {
    	
        List<String> data = new ArrayList<String>();

        data.add(anyConditionName);
        data.addAll(NCDUtilities.getService().getAllConditionNames());

        return data;
    }
    
    private List<String> getInstitutions() {
    	
        List<String> data = new ArrayList<String>();
    
        data.add("-Choose institution-");
        data.addAll(NCDUtilities.getService().getAllInstitutionNames());
    	
        return data;
    }
    
    private List<String> getBooleans() {
    	
        List<String> data = new ArrayList<String>();
        
        data.add("-Choose a Value-");
        data.add("true");
        data.add("false");

        return data;
    }
    
    private String getLastSuccessfulRunDate(Object obj) {
    	
    	// Get the form backing object
    	ReportDefinition reportDef = (ReportDefinition) obj;

    	// If there is an existing task definition with an id
    	if (reportDef != null && reportDef.getTaskdef() != null && reportDef.getTaskdef().getId() != null) {
    		
    		// Get the last successful task status
    		ConditionDetectorService cds = NCDUtilities.getService();
    		TaskRunStatus lastStatus = cds.getLatestTaskStatus(reportDef.getTaskdef());
    		
    		// If there is a last successful status, format the start date and return it
    		if (lastStatus != null) {
	    		return getDefaultDateFormat().format(lastStatus.getStarted());
    		}
    	}
    		
    	// There is no last successful run status
   		return "(none)";
    }
    
    private List<String> getReportStatusTypes() {
    	
        List<String> data = new ArrayList<String>();

        data.add("-Choose report status-");
        data.add(ReportResultStatus.REPORT.getText());
        data.add(ReportResultStatus.DO_NOT_REPORT.getText());

        return data;
    }
    
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {

    	HttpSession httpSession = request.getSession();
	    
        MessageSourceAccessor msa = getMessageSourceAccessor();
        String action = request.getParameter("action");
        
        ReportDefinition reportDef = (ReportDefinition) object;
        
        SchedulerService scheduler = Context.getSchedulerService();
        
        // If the save button was clicked
        if (action.equals(msa.getMessage("ncd.buttons.save"))) {
        	
        	// If form data is not valid, display the errors without saving
        	if (!reportDef.valid(exceptions)) {
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.report.error.error");
                return showForm(request, response, exceptions);
        	}

        	// Create or update the task definition
        	TaskDefinition taskdef = reportDef.toTask();
        	try {
        		NCDUtilities.getService().saveReport(taskdef, reportDef.getMonitoredConditions());
        	}
        	catch (Exception e) {

        		// If creating or updating the task definition fails, assume
        		// that it's because the report is already running.
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.report.error.running");
                return showForm(request, response, exceptions);
        	}

        	// TODO: The following should really be part of saveReport.
       		// If the enabled state changed
       		if (!reportDef.getEnabled().equals(reportDef.getOldEnabled())) {
       			
       			// If starting the task
       			if (reportDef.getEnabled().equals(true)) {
       				taskdef.setStartOnStartup(new Boolean(true));
       				scheduler.saveTask(taskdef);
       				scheduler.scheduleTask(taskdef);
       			} else {
       				// Stopping the task
       				taskdef.setStartOnStartup(new Boolean(false));
       				scheduler.saveTask(taskdef);
       				scheduler.shutdownTask(taskdef);
       			}
       		}
        	
            // Back to the list page
            return new ModelAndView(new RedirectView(getSuccessView()));
        }
        // If the cancel button was clicked
        else if (action.equals(msa.getMessage("ncd.buttons.cancel"))) {
            
            // Back to the list page
            return new ModelAndView(new RedirectView(getSuccessView()));
        }
        else if (action.equals(msa.getMessage("ncd.buttons.add"))) {
        	
        	log.debug("action=add monitored condition");

        	// Validate the new monitored condition fields
        	// NOTE: There is no longer the possibility of a validation
        	// error.
        	//validateNewMonitoredCondition(reportDef, exceptions);
 
        	// If there are no problems
        	if (!exceptions.hasErrors()) {
        		
        		Condition cond = null;
        		if (!anyConditionName.equals(reportDef.getNewCondition())) {
        			cond = NCDUtilities.getService().findConditionByName(reportDef.getNewCondition());
        		}
        		
        		// create a MonitoredEntity with a fake ID and add it to the collection
        		MonitoredCondition newMC = new MonitoredCondition();
        		newMC.setId(nextFakeId--);
        		newMC.setTask(reportDef.getTaskdef());
        		newMC.setApplication(reportDef.getNewApplication());
        		newMC.setFacility(reportDef.getNewFacility());
        		newMC.setLocation(reportDef.getNewLocation());
        		newMC.setCondition(cond);
        		reportDef.getMonitoredConditions().add(newMC);

        		// LATER: resort the list
        		
        		log.debug("monitored conditions=" + reportDef.getMonitoredConditions());
        	}
        	
            return showForm(request, response, exceptions);
        }
        else if (action.equals(msa.getMessage("ncd.buttons.deleteselected"))) {

        	log.debug("action=delete selected monitored conditions");

        	List<MonitoredCondition> monitoredConditionsCopy =
        		new ArrayList<MonitoredCondition>(reportDef.getMonitoredConditions());

        	for (MonitoredCondition mc : monitoredConditionsCopy) {
        	
        		for (Long selectedId : reportDef.getSelectedMonitoredConditions()) {
        			
        			if (selectedId.equals(mc.getId())) {
        				
        	        	log.debug("delete MC=" + mc);
        				reportDef.getMonitoredConditions().remove(mc);
        			}
        		}
        	}
        	
        	reportDef.setSelectedMonitoredConditions(new ArrayList<Long>());
    		
    		log.debug("monitored conditions=" + reportDef.getMonitoredConditions());
        	
            return showForm(request, response, exceptions);
        }
        else {

            // I don't know what the heck happened. Ignore it and refresh.
            return showForm(request, response, exceptions);
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
	protected ReportDefinition formBackingObject(HttpServletRequest request) throws Exception { 

    	log.debug("request=" + request);

        TaskDefinition task = new TaskDefinition();
        
        String taskId = request.getParameter("reportId");
    	if (taskId != null) {
    		
            task = Context.getSchedulerService().getTask(Integer.valueOf(taskId));  
    	}
    	
        // Date format pattern for new and existing (currently disabled,
        // but visible)
        if (task.getStartTimePattern() == null) { 
            task.setStartTimePattern(DEFAULT_DATE_PATTERN);
        }
        
        ReportDefinition report = new ReportDefinition();
        report.fromTask(task);

        List<MonitoredCondition> monitoredConditions = NCDUtilities.getService().getMonitoredConditions(task);
        report.setMonitoredConditions(monitoredConditions);

        return report;
    }

    private DateFormat getDefaultDateFormat() {
        
        if (DEFAULT_DATE_FORMAT == null) {
            DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
        }
        
        return DEFAULT_DATE_FORMAT;
    }
}

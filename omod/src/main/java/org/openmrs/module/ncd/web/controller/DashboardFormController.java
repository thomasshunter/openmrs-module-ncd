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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.MessageCountSummary;
import org.openmrs.module.ncd.database.filter.SearchFilterAlertSummary;
import org.openmrs.module.ncd.database.filter.SearchFilterMessageCountSummary;
import org.openmrs.module.ncd.database.filter.SystemEventFilter;
import org.openmrs.module.ncd.database.filter.SearchFilterAlertSummary.SortKeys;
import org.openmrs.module.ncd.model.SystemActivityEvent;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.PropertyNotFoundException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * This controller backs the /web/module/basicmoduleForm.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
public class DashboardFormController extends SessionFormController 
{	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    public class PageForm extends ListPageForm<SearchFilterAlertSummary, AlertSummary>
    {
    	private List<MessageCountSummary> messageCountSummaries;
    	private List<SystemActivityEvent> systemActivityEvents;
    	private String dismissReason;
    	
        public PageForm(SearchFilterAlertSummary filter) 
        {
            super(filter);
        }

		public List<MessageCountSummary> getMessageCountSummaries() 
		{
			return messageCountSummaries;
		}

		public void setMessageCountSummaries( List<MessageCountSummary> messageCountSummaries) 
		{
			this.messageCountSummaries = messageCountSummaries;
		}

		public List<SystemActivityEvent> getSystemActivityEvents() 
		{
			return systemActivityEvents;
		}

		public void setSystemActivityEvents( List<SystemActivityEvent> systemActivityEvents) 
		{
			this.systemActivityEvents = systemActivityEvents;
		}

		public String getDismissReason() 
		{
			return dismissReason;
		}

		public void setDismissReason(String dismissReason) 
		{
			this.dismissReason = dismissReason;
		}

		public int getAlertSummariesSize() 
		{
			return getVisibleRows().size();
		}

		public int getSystemActivityEventsSize() 
		{
			return systemActivityEvents.size();
		}

		/**
		 * Dismiss all currently selected alert summaries
		 */
		public void dismissAlertSummaries() 
		{
			ConditionDetectorService cds = NCDUtilities.getService();

			// Dismiss the selected alert summaries
			for (AlertSummary alertSummary : getVisibleRows()) 
			{
				if (isSelected(alertSummary)) 
				{
					cds.dismissAlertSummary(alertSummary, dismissReason);
				}
			}
		}
		
		/**
		 * Reset the message counts
		 */
		public void resetCounts() 
		{
			ConditionDetectorService cds = NCDUtilities.getService();
			cds.resetMessageCountSummaries();
		}
    }

    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception 
    {
    	Map<String, Object> refdata = new HashMap<String, Object>();
	
    	return refdata;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@SuppressWarnings("deprecation")
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception 
	{		
    	@SuppressWarnings("unused")
        HttpSession httpSession = request.getSession();
        MessageSourceAccessor msa = getMessageSourceAccessor();
        PageForm theForm = (PageForm) object;
        
        String action = request.getParameter("action");

        // If the dismiss button was clicked
        if (action.equals(msa.getMessage("ncd.pages.dashboard.alerts.dismiss"))) 
        {
        	theForm.dismissAlertSummaries();
            return showForm(request, response, exceptions);
        }        
        else if (action.equals(msa.getMessage("ncd.pages.dashboard.messages.reset.counts"))) // If the reset counts button was clicked
        {
        	theForm.resetCounts();
            return showForm(request, response, exceptions);
        }
        else 
        {
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
	public PageForm formBackingObject(HttpServletRequest request) throws Exception 
    {
    	// NOTE: this method always refreshes the form before returning it, because every
    	// visit to the dashboard page should always display the most recent information.
    	PageForm theForm = (PageForm) super.formBackingObject(request);
        refreshList(theForm);
	    return theForm;
    }

	@Override
	public Object getNewForm(HttpServletRequest request) throws Exception 
	{
		return new PageForm(getDefaultFilter());
	}

    protected SearchFilterAlertSummary getDefaultFilter() throws PropertyNotFoundException 
    {
    	// Build the default filter
        SearchFilterAlertSummary filter = new SearchFilterAlertSummary();
        filter.getDismissed().setValue("false");
        filter.setRowsPerPage(10);
        filter.setMaxRows(10);
        filter.setSortKey(SortKeys.LASTOCCURRED);
        filter.setSortAscending(false);
        
        return filter;
    }

    protected void refreshList(PageForm theForm) throws Exception 
    {
		Date beginTimeInit = new Date();
		ConditionDetectorService cds = NCDUtilities.getService();
		
		// Get the alerts section data
		Date beginTimeAlerts = new Date(); 
        SearchFilterAlertSummary filter = theForm.getFilter();
        theForm.setLastSearchResult(cds.findAlertSummaries(filter));
    	Date endTimeAlerts = new Date();
        
        // Get the errors section data
    	theForm.setSystemActivityEvents(cds.findSystemEvents(new SystemEventFilter()));
    	Date endTimeEvents = new Date();
        
        // Build the message processed section data
        SearchFilterMessageCountSummary messageCountSummaryFilter = new SearchFilterMessageCountSummary();
        theForm.setMessageCountSummaries(cds.findMessageCountSummaries(messageCountSummaryFilter));
        Date endTimeCounts = new Date();

        if (log.isDebugEnabled()) 
        {
        	log.debug("Dashboard queries completed in " + elapsedTime(beginTimeInit, endTimeCounts) + " ms.");
        	log.debug("\talerts: " + elapsedTime(beginTimeAlerts, endTimeAlerts) + " ms.");
        	log.debug("\tevents: " + elapsedTime(endTimeAlerts, endTimeEvents) + " ms.");
        	log.debug("\tcounts: " + elapsedTime(endTimeEvents, endTimeCounts) + " ms.");
        }
    }
	
	private long elapsedTime(Date beginTime, Date endTime) 
	{
		return endTime.getTime() - beginTime.getTime();
	}
}

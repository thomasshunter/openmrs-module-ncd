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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.filter.SearchFilterCodes;
import org.openmrs.module.ncd.database.filter.SearchTermOpString;
import org.openmrs.module.ncd.database.filter.SearchFilterCodes.SortKeys;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.PropertyNotFoundException;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/** A template for new form controller classes */
public class CodeListFormController extends MultiLoginFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    public class PageForm 
        extends ListPageForm<SearchFilterCodes, Code>
    {
        public PageForm(SearchFilterCodes filter) {
            super(filter);
        }
    }

    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

        Map<String, Object> refData = new HashMap<String, Object>();
        refData.put("equalOperators", getEqualOperators());
        refData.put("numericOperators", getNumericOperators());
        refData.put("stringOperators", SearchTermOpString.getOperators());
        return refData;
	}
    
    protected List<String> getEqualOperators() {
        
        List<String> data = new ArrayList<String>();
        
        data.add(" ");
        data.add("=");
        data.add("<>");

        return data;
    }
    
    protected List<String> getNumericOperators() {
        
        List<String> data = new ArrayList<String>();
        
        data.add(" ");
        data.add("=");
        data.add("<>");
        data.add("<");
        data.add("<=");
        data.add(">");
        data.add(">=");

        return data;
    }
    
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@SuppressWarnings("deprecation")
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {

        HttpSession httpSession = request.getSession();
        MessageSourceAccessor msa = getMessageSourceAccessor();
        PageForm theForm = (PageForm) object;
        
        String filteraction = request.getParameter("filteraction");
        if (filteraction == null) {
            filteraction = "";
        }
        
        String listaction = request.getParameter("listaction");
        if (listaction == null) {
            listaction = "";
        }
        
        String batchaction = request.getParameter("batchaction");
        if (batchaction == null) {
            batchaction = "";
        }
        
        String sortaction = request.getParameter("sortaction");
        if (sortaction == null) {
        	sortaction = "";
        }
        
        if (filteraction.equals(msa.getMessage("ncd.buttons.apply"))) {

            // If the entered filter is valid
            if (theForm.getFilter().validate(exceptions)) {

                // Hide the filterEdit div
                theForm.setEditFilter(false);

                // Clear any old error status
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);

                // Update the list of decided results
                refreshList(httpSession, theForm);
            }
            else {

                // Show the filterEdit div
                theForm.setEditFilter(true);
                
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.filter.error");
            }
            
            return showForm(request, response, exceptions);
        }
        else if (filteraction.equals(msa.getMessage("ncd.buttons.clear"))) {
            
            theForm.getFilter().clear();

            // Show the filterEdit div
            theForm.setEditFilter(true);
            
            return showForm(request, response, exceptions);
        }
        else if (filteraction.equals(msa.getMessage("ncd.buttons.default"))) {
            
            theForm.setFilter(getDefaultFilter());

            // Show the filterEdit div
            theForm.setEditFilter(true);
            
            return showForm(request, response, exceptions);
        }
        else if (filteraction.equals(msa.getMessage("ncd.filter.buttons.updatevisible"))) {

        	// Just rerender the HTML.
            // Show the filterEdit div
            theForm.setEditFilter(true);
            
            return showForm(request, response, exceptions);
        }
        else if (filteraction.equals(msa.getMessage("ncd.filter.buttons.showall"))) {

        	theForm.getFilter().setShowAll(true);

            // Show the filterEdit div
            theForm.setEditFilter(true);
            
            return showForm(request, response, exceptions);
        }
        else if (filteraction.equals(msa.getMessage("ncd.filter.buttons.showvisible"))) {

        	theForm.getFilter().setShowAll(false);

            // Show the filterEdit div
            theForm.setEditFilter(true);
            
            return showForm(request, response, exceptions);
        }
        else if (filteraction.equals(msa.getMessage("ncd.filter.buttons.checkall"))) {

        	theForm.getFilter().setAllVisibles(true);

            // Show the filterEdit div
            theForm.setEditFilter(true);
            
            return showForm(request, response, exceptions);
        }
        else if (filteraction.equals(msa.getMessage("ncd.filter.buttons.uncheckall"))) {

        	theForm.getFilter().setShowAll(true);
        	theForm.getFilter().setAllVisibles(false);

            // Show the filterEdit div
            theForm.setEditFilter(true);
            
            return showForm(request, response, exceptions);
        }
        else if (listaction.equals(msa.getMessage("ncd.buttons.refresh"))) {
            
            // Update the list of decided results
            refreshList(httpSession, theForm);
            
            return showForm(request, response, exceptions);
        }
        else if (listaction.equals(msa.getMessage("ncd.buttons.selectall"))) {

            // select all entries
            theForm.selectAll();

            return showForm(request, response, exceptions);
        }
        else if (listaction.equals(msa.getMessage("ncd.buttons.selectnone"))) {

            // select no entries
            theForm.selectNone();

            return showForm(request, response, exceptions);
        }
        else if (listaction.equals(msa.getMessage("ncd.buttons.firstpage"))) {

            // step back to the first page
            theForm.firstPage();
            setPageMessage(httpSession, theForm);

            return showForm(request, response, exceptions);
        }
        else if (listaction.equals(msa.getMessage("ncd.buttons.prevpage"))) {

            // step back to the previous page, if any
            theForm.previousPage();
            setPageMessage(httpSession, theForm);

            return showForm(request, response, exceptions);
        }
        else if (listaction.equals(msa.getMessage("ncd.buttons.nextpage"))) {

            // advance to the next page, if any
            theForm.nextPage();
            setPageMessage(httpSession, theForm);

            return showForm(request, response, exceptions);
        }
        else if (listaction.equals(msa.getMessage("ncd.buttons.lastpage"))) {

            // advance to the last page, if any
            theForm.lastPage();
            setPageMessage(httpSession, theForm);

            return showForm(request, response, exceptions);
        }
        else if (batchaction.equals("apply")) {
            
            // If the entered filter is valid
            if (theForm.getFilter().validate(exceptions)) {

                // Hide the filterEdit div
                theForm.setEditFilter(false);

                // Clear any old error status
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);

                // Update the list of decided results
                refreshList(httpSession, theForm);
            }
            else {

                // Show the filterEdit div
                theForm.setEditFilter(true);
                
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.filter.error");
            }

            return showForm(request, response, exceptions);
        }
        else if (sortaction.length() > 0) {
        	
        	// sort the list per the selected sort column
      		SortKeys sortKey = SortKeys.valueOf(sortaction);
        	theForm.getFilter().setSortKey(sortKey);
        	refreshList(httpSession, theForm);
            
            return showForm(request, response, exceptions);
        }
        else {
            
            // How did you get here?
            log.error("impossible: no action in request");
            return showForm(request, response, exceptions);
        }
    }

	@Override
	public Object getNewForm(HttpServletRequest request) throws Exception {
        PageForm theForm = new PageForm(getDefaultFilter());
        refreshList(request.getSession(), theForm);
        return theForm;
	}
    
    protected SearchFilterCodes getDefaultFilter() throws PropertyNotFoundException {
        
        SearchFilterCodes filter = new SearchFilterCodes();
        
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, -24);
        
        // Build the default filter.
        filter.setSortKey(SortKeys.CODE);
        
        filter.setRowsPerPage(NCDUtilities.getRowsPerPage());
        filter.setMaxRows(NCDUtilities.getMaxRowsToFetch());
        
        return filter;
    }

    protected void refreshList(HttpSession session, PageForm theForm) {

        SearchFilterCodes filter = theForm.getFilter();
        theForm.setLastSearchResult(NCDUtilities.getService().findCodes(filter));
        
        // If the list update failed
        if (theForm.getLastSearchResult().isSuccessful()) {
            
            setPageMessage(session, theForm);
        }
        else {
            
            // Set the top of page error message
        	session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.filter.queryerror");
        }
    }
    
    private void setPageMessage(HttpSession session, PageForm theForm) {
        
        // Set the top of page status message
        if (theForm.getLastSearchResult().getRowCount() == 0) {
            session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.filter.querynone");
        }
        else {
            session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.filter.queryall");
        }

        Long[] counts = new Long[3];
        counts[0] = new Long(theForm.getFirstRowShown() + 1);
        counts[1] = new Long(theForm.getLastRowShown() + 1);
        counts[2] = theForm.getLastSearchResult().getRowCount();
        session.setAttribute(WebConstants.OPENMRS_MSG_ARGS, counts);
    }
}

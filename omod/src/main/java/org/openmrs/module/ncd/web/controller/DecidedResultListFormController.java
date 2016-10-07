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
import java.util.ArrayList;
import java.util.Calendar;
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
import org.openmrs.module.ncd.database.CriticDef;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.database.filter.SearchFilterDecidedResults;
import org.openmrs.module.ncd.database.filter.SearchTermOpString;
import org.openmrs.module.ncd.database.filter.SearchFilterDecidedResults.SortKeys;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.PropertyNotFoundException;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * This controller backs the /web/module/basicmoduleForm.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
public class DecidedResultListFormController extends MultiLoginFormController {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    // Move this to message.properties or OpenmrsConstants
    @SuppressWarnings("unused")
    private static String DEFAULT_DATE_PATTERN = "MM/dd/yyyy";
    @SuppressWarnings("unused")
    private static DateFormat DEFAULT_DATE_FORMAT = null;

    public class PageForm 
        extends ListPageForm<SearchFilterDecidedResults, 
                             DecidedResult>
    {        
        private String batchReportableOp;
        private String batchConditionOp;
        private boolean batchRemoveOp;
        
        public PageForm(SearchFilterDecidedResults filter) {
            
            super(filter);
            
            this.batchReportableOp = "";
            this.batchConditionOp = "";
            this.batchRemoveOp = false;
        }
        
        public String getBatchReportableOp() {
            return batchReportableOp;
        }

        public void setBatchReportableOp(String batchReportableOp) {
            this.batchReportableOp = batchReportableOp;
        }

        public String getBatchConditionOp() {
            return batchConditionOp;
        }

        public void setBatchConditionOp(String batchConditionOp) {
            this.batchConditionOp = batchConditionOp;
        }

        /**
         * @return the batchRemoveOp
         */
        public boolean isBatchRemoveOp() {
            return batchRemoveOp;
        }

        /**
         * @param batchRemoveOp the batchRemoveOp to set
         */
        public void setBatchRemoveOp(boolean batchRemoveOp) {
            this.batchRemoveOp = batchRemoveOp;
        }
    }

    @SuppressWarnings("deprecation")
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        
        NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
        binder.registerCustomEditor(java.lang.Long.class,
                new CustomNumberEditor(java.lang.Long.class, nf, true));
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

        refData.put("equalOperators", getEqualOperators());
        refData.put("numericOperators", getNumericOperators());
        refData.put("stringOperators", SearchTermOpString.getOperators());
        refData.put("conditionNames", getConditionNames());
        refData.put("allCritics", getCriticNames());

        refData.put("booleans", getBooleans());

        refData.put("reportableOps", getReportableOps());
        refData.put("conditionOps", getConditionOps());
        
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

    protected List<String> getConditionNames() {
        
        List<String> data = new ArrayList<String>();
        data.add(" ");
        data.add("None");
        data.add("Unknown");
        data.addAll(NCDUtilities.getService().getAllConditionNames());
        return data;
    }
    
    protected List<String> getCriticNames() {
        
        List<String> data = new ArrayList<String>();
        data.add(" ");
        for (CriticDef critic : NCDUtilities.getService().getAllCritics()) {
            data.add(critic.getName());
        }
        return data;
    }

    private Collection<String> getBooleans() {
        
        Collection<String> data = new ArrayList<String>();

        data.add(" ");
        data.add("false");
        data.add("true");
        
        return data;
    }

    private Collection<String> getReportableOps() {
        
        Collection<String> data = new ArrayList<String>();

        data.add(" ");
        data.add("report");
        data.add("not-reported");
        
        return data;
    }

    private List<String> getConditionOps() {
        
        List<String> data = new ArrayList<String>();

        data.add(" ");
        data.addAll(NCDUtilities.getService().getAllConditionNames());

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

                // If any batch operations are selected, apply them
                doBatchOperations(request);
                
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
	public PageForm formBackingObject(HttpServletRequest request) throws Exception { 

    	HttpSession session = request.getSession();
    	PageForm theForm = (PageForm) super.formBackingObject(request);
        setPageMessage(session, theForm);
        return theForm;
    }

	@Override
	public Object getNewForm(HttpServletRequest request) throws Exception {
        PageForm theForm = new PageForm(getDefaultFilter());
        refreshList(request.getSession(), theForm);
		return theForm;
	}
    
    protected SearchFilterDecidedResults getDefaultFilter() throws PropertyNotFoundException {
        
        SearchFilterDecidedResults filter = new SearchFilterDecidedResults();
        
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, -24);
        
        // Build the default filter.
        filter.getDateadded().setLow(DateUtilities.formatDateTime(now.getTime()));
        filter.setSortKey(SortKeys.DATEADDED);
        filter.setSortAscending(false);
        
        filter.setRowsPerPage(NCDUtilities.getRowsPerPage());
        filter.setMaxRows(NCDUtilities.getMaxRowsToFetch());
        
        return filter;
    }

    protected void refreshList(HttpSession httpSession, PageForm theForm) {

        SearchFilterDecidedResults filter = theForm.getFilter();
        theForm.setLastSearchResult(NCDUtilities.getService().findDecidedResults(filter));
        
        // Set the top of page status message
        setPageMessage(httpSession, theForm);
    }
    
	private boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }

    protected void doBatchOperations(HttpServletRequest request) throws Exception {

    	PageForm theForm = (PageForm) getCurrentForm(request);
        log.debug("set condition: " + theForm.getBatchConditionOp());
        log.debug("set reportable: " + theForm.getBatchReportableOp());
        log.debug("remove: " + theForm.isBatchRemoveOp());

        ArrayList<DecidedResult> modifiedResults = new ArrayList<DecidedResult>();
        ArrayList<DecidedResult> deletedResults = new ArrayList<DecidedResult>();
        ConditionDetectorService ncd = NCDUtilities.getService();
        for (DecidedResult thisResult : theForm.getVisibleRows()) {
            
            if (theForm.isSelected(thisResult)) {
                if (theForm.isBatchRemoveOp()) {
                    
                    deletedResults.add(thisResult);
                }
                    
                if (isSet(theForm.getBatchConditionOp())) {
                    
                    thisResult.setConditionName(theForm.getBatchConditionOp());
                    modifiedResults.add(thisResult);
                }
                
                if (isSet(theForm.getBatchReportableOp())) {
                    
                    thisResult.setReportable(theForm.getBatchReportableOp());
                    modifiedResults.add(thisResult);
                }
            }
        }
        
        if (!modifiedResults.isEmpty()) {

            ncd.saveDecidedResults(modifiedResults);
        }
        
        if (!deletedResults.isEmpty()) {

        	ncd.removeDecidedResults(deletedResults);
        }
        
        theForm.selectNone();
    }
    
    private void setPageMessage(HttpSession session, PageForm theForm) {
        
        // If the list update failed
        if (!theForm.getLastSearchResult().isSuccessful()) {

            // Set the top of page error message
            session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.filter.queryerror");
        }
        else {

	        // Set the top of page status message
	        if (theForm.getLastSearchResult().getRowCount() == 0) {
	            session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.filter.querynone");
	        }
	        else {
	            session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.filter.queryall");
		        Long[] counts = new Long[3];
		        counts[0] = new Long(theForm.getFirstRowShown() + 1);
		        counts[1] = new Long(theForm.getLastRowShown() + 1);
		        counts[2] = theForm.getLastSearchResult().getRowCount();
		        session.setAttribute(WebConstants.OPENMRS_MSG_ARGS, counts);
	        }
        }
    }
}

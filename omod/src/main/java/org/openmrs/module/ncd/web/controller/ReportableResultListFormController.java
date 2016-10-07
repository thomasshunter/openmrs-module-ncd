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
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.CriticDef;
import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.database.filter.SearchFilterReportableResults;
import org.openmrs.module.ncd.database.filter.SearchTermOpString;
import org.openmrs.module.ncd.database.filter.SearchFilterReportableResults.SortKeys;
import org.openmrs.module.ncd.utilities.CodeTypeListEntry;
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
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/basicmoduleForm.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
public class ReportableResultListFormController extends MultiLoginFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    public class PageForm 
        extends ListPageForm<SearchFilterReportableResults, 
                             ReportableResult>
    {
        private String batchSentInErrorOp;
        private String reviewOps;
        
        public PageForm(SearchFilterReportableResults filter) {
            
            super(filter);

            log.debug("construct new instance");

            this.batchSentInErrorOp = "";
            this.reviewOps = "";
        }

        public String getBatchSentInErrorOp() {
            return batchSentInErrorOp;
        }

        public void setBatchSentInErrorOp(String batchSentInErrorOp) {
            this.batchSentInErrorOp = batchSentInErrorOp;
        }

        public String getReviewOps() {
            return reviewOps;
        }

        public void setReviewOps(String reviewOps) {
            this.reviewOps = reviewOps;
        }
    }
    
    public class DelimitedView extends AbstractView {

    	protected PageForm theForm;
    
        public DelimitedView(PageForm theForm) {

        	this.theForm = theForm;
            setContentType("text/csv");
        }
        
        protected boolean generatesDownloadContent() {

            return true;
        }
        
        @Override
        protected void renderMergedOutputModel(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception
        {
        	String content = null;
            try {
                
                // Do the export
                log.debug("render started");
                content = NCDUtilities.getService().exportReportableResults(theForm.getFilter());
            }
            catch (Exception e) {
                
                log.error("Exception: " + e.getMessage(), e);
            }

            try {
                
                // Build the response.
                response.setContentType(getContentType());
                response.setHeader("Content-Disposition","attachment; filename=\"export.csv\"");
                response.setHeader("cache-control", "must-revalidate");
                response.getWriter().write(content);
                response.flushBuffer();
                log.debug("render done");
            }
            catch (Exception e) {
                
                log.debug("Exception: " + e.getMessage(), e);
            }
        }
    }
 
    public ReportableResultListFormController() {
    	super();
    	log.debug("construct new form controller instance");
    }

    // Set by Spring based on moduleApplicationContext.xml
    protected String detailView = null;

    public void setDetailView(String detailView) {
    	this.detailView = detailView;
    }
    
    public String getDetailView() {
    	return this.detailView;
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
        refData.put("loincOperators", getLoincOperators());
        refData.put("numericOperators", getNumericOperators());
        refData.put("stringOperators", SearchTermOpString.getOperators());
        refData.put("conditionNames", getConditionNames());
        refData.put("allCritics", getCriticNames());

        refData.put("booleans", getBooleans());

        refData.put("sentInErrorOps", getSentInErrorOps());
        refData.put("reviewOps", getReviewOps());
        
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
    
    protected List<String> getLoincOperators() {
        
        List<String> data = new ArrayList<String>();
        
        data.add(" ");
        data.add("=");
        data.add("<>");
        data.add("<");
        data.add("<=");
        data.add(">");
        data.add(">=");
        data.add("contains");

        return data;
    }
    
    protected List<String> getConditionNames() {
        
        List<String> data = new ArrayList<String>();
        data.add(" ");
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

    @SuppressWarnings("unused")
    private Collection<CodeTypeListEntry> getCodeTypeDropList(String codeTypeName) {
        
        Collection<CodeTypeListEntry> data = new ArrayList<CodeTypeListEntry>();

        data.add(new CodeTypeListEntry(" ", " "));

        MessageSourceAccessor msa = getMessageSourceAccessor();
        List<Code> typeCodeList = NCDUtilities.getService().findCodes(codeTypeName, "NCD");

        for (Code code : typeCodeList) {
            
            data.add(new CodeTypeListEntry(code.getCode(), msa.getMessage(code.getDisplayText())));
        }
        
        // TODO: sort entries by display text?
        
        return data;
    }

    private Collection<String> getSentInErrorOps() {
        
        Collection<String> data = new ArrayList<String>();

        data.add("-No change-");
        data.add("false");
        data.add("true");
        
        return data;
    }

    private Collection<String> getReviewOps() {
        
        Collection<String> data = new ArrayList<String>();

        data.add("-No change-");
        data.add("No manual review required");
        data.add("Awaiting manual review");
        data.add("Reviewed - error, ignore");
        data.add("Reviewed - correct, send");
        
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
        
        log.debug("filteraction=" + filteraction);
        log.debug("listaction=" + listaction);
        log.debug("batchaction=" + batchaction);
        
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
                doBatchOperations(theForm);
                
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

	protected ModelAndView getDetailModelAndView(String id) {
		
		log.debug("detailView=" + detailView);

		RedirectView view = new RedirectView(detailView + "?edit=" + id + "&shownav=1", true);
		return new ModelAndView(view);
	}

	@Override
	public Object getNewForm(HttpServletRequest request) throws Exception {
		
        PageForm theForm = new PageForm(getDefaultFilter());
        refreshList(request.getSession(), theForm);
        return theForm;
	}
	
    protected SearchFilterReportableResults getDefaultFilter() throws PropertyNotFoundException {
        
        SearchFilterReportableResults filter = new SearchFilterReportableResults();
        
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, -24);

        // Build the default filter.
        filter.getMessageReceivedDateTime().setLow(DateUtilities.formatDateTime(now.getTime()));
        filter.setSortKey(SortKeys.DATETIMERCVD);
        filter.setSortAscending(false);

        filter.setRowsPerPage(NCDUtilities.getRowsPerPage());
        filter.setMaxRows(NCDUtilities.getMaxRowsToFetch());
        
        return filter;
    }

    protected void refreshList(HttpSession httpSession, PageForm theForm) {

        SearchFilterReportableResults filter = theForm.getFilter();
        theForm.setLastSearchResult(NCDUtilities.getService().findReportableResults(filter));
        
        // Set the top of page status message
        setPageMessage(httpSession, theForm);
    }
    
    private boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }

    protected void doBatchOperations(PageForm theForm) {

    	log.debug("set sent in error: " + theForm.getBatchSentInErrorOp());

    	// Build up the list of selected reportable results 
    	ArrayList<ReportableResult> selectedResults = new ArrayList<ReportableResult>();
    	ConditionDetectorService cds = NCDUtilities.getService();
    	for (ReportableResult thisResult : theForm.getVisibleRows()) {

    		if (theForm.isSelected(thisResult)) {
    			selectedResults.add(thisResult);
    		}
    	}

    	if (!selectedResults.isEmpty()) {
    		if (isSet(theForm.getBatchSentInErrorOp())) {

    			// As a single transaction, set or clear the sent in error flag for the selected reportable results
    			if (theForm.getBatchSentInErrorOp().equals("true")) {
    				cds.reportableResultSentInError(selectedResults);
    			}
    			else {
    				cds.reportableResultNotSentInError(selectedResults);
    			}
    		}
    	}
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
    
    /**
     * This shouldn't be necessary, but I can't find an easier way to handle a
     * GET request. When I tried to export from onSubmit (via a POST), the export
     * would sometimes (10% of the time) save the page source instead of the
     * expected export output.
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	String export = request.getParameter("export");
    	if (export != null) {
    		
    		PageForm theForm = (PageForm) getCurrentForm(request);;
            return new ModelAndView(new DelimitedView(theForm));
    	}
    	else {
	        String autorefresh = request.getParameter("refresh");
	        if (autorefresh == null) {
	        	autorefresh = "";
	        }
	        
            Object commandObject = getCurrentForm(request);
            
	        if (autorefresh.equals("1")) {
	            
	            // Update the list
	            refreshList(request.getSession(), (PageForm) getCurrentForm(request));
	        }
	        else {

	            if (commandObject != null) {

	    	        PageForm theForm = (PageForm) getCurrentForm(request);
	    	        
		        	// Display the page message without refreshing the list
		            setPageMessage(request.getSession(), theForm);
	
		    		String currentId = request.getParameter("currentId");
		    		if (request.getParameter("first") != null) {
		    			
		            	String id = theForm.first(currentId);
		            	return getDetailModelAndView(id);
		    		}
		    		else if (request.getParameter("previous") != null) {
		    			
		            	String id = theForm.previous(currentId);
		            	return getDetailModelAndView(id);
		    		}
		    		else if (request.getParameter("next") != null) {
		    			
		            	String id = theForm.next(currentId);
		            	return getDetailModelAndView(id);
		    		}
		    		else if (request.getParameter("last") != null) {
		    			
		            	String id = theForm.last(currentId);
		            	return getDetailModelAndView(id);
		    		}
	            }
	        }
    		return super.handleRequest(request, response);
    	}
    }
}

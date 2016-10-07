package org.openmrs.module.ncd.web.controller;

import java.text.NumberFormat;
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
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.filter.SearchFilterAlertSummary;
import org.openmrs.module.ncd.database.filter.SearchFilterAlertSummary.SortKeys;
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

public class AlertListFormController extends MultiLoginFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    public class PageForm 
    extends ListPageForm<SearchFilterAlertSummary, AlertSummary>
    {
        private String batchDismissedOp;
        private String batchReason;
        
        public PageForm(SearchFilterAlertSummary filter) {
            
            super(filter);

            this.batchDismissedOp = "";
            this.batchReason = "";
        }

        public String getBatchDismissedOp() {
            return batchDismissedOp;
        }

        public void setBatchDismissedOp(String batchDismissedOp) {
            this.batchDismissedOp = batchDismissedOp;
        }

		public String getBatchReason() {
			return batchReason;
		}

		public void setBatchReason(String batchReason) {
			this.batchReason = batchReason;
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
        refData.put("stringOperators", getStringOperators());

        refData.put("booleans", getBooleans());

        refData.put("dismissedOps", getDismissedOps());
        
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
    
    protected List<String> getStringOperators() {
        
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
    
    private Collection<String> getBooleans() {
        
        Collection<String> data = new ArrayList<String>();

        data.add(" ");
        data.add("false");
        data.add("true");
        
        return data;
    }

    private Collection<String> getDismissedOps() {
        
        Collection<String> data = new ArrayList<String>();

        data.add("-No change-");
        data.add("false");
        data.add("true");
        
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

                // Update the list
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
            
            // Update the list
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
                
                // Update the list
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
    
    /**
     * This shouldn't be necessary, but I can't find an easier way to handle a
     * GET request.
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
        String autorefresh = request.getParameter("refresh");
        if (autorefresh == null) {
        	autorefresh = "";
        }

        
        PageForm theForm = (PageForm) getCurrentForm(request);
        
        HttpSession session = request.getSession();
        if (autorefresh.equals("1")) {
            
            // Update the list
        	log.debug("refresh=1, refresh the list");
            refreshList(session, theForm);
        }
        else {
        	// Display the page message without refreshing the list
        	log.debug("refresh!=1, don't refresh the list, just update the page message.");
            setPageMessage(session, theForm);
        }
        
		return super.handleRequest(request, response);
    }
    
    protected SearchFilterAlertSummary getDefaultFilter() throws PropertyNotFoundException {

    	// Build the default filter (returns all alerts without filtering)
        SearchFilterAlertSummary filter = new SearchFilterAlertSummary();
        filter.setRowsPerPage(NCDUtilities.getRowsPerPage());
        filter.setMaxRows(NCDUtilities.getMaxRowsToFetch());
        filter.setSortKey(SortKeys.FIRSTOCCURRED);
        filter.setSortAscending(false);
        
        return filter;
    }

    protected void refreshList(HttpSession httpSession, PageForm theForm) {

        SearchFilterAlertSummary filter = theForm.getFilter();
        theForm.setLastSearchResult(NCDUtilities.getService().findAlertSummaries(filter));

        // Set the top of page status message
        setPageMessage(httpSession, theForm);
    }
    
    private boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }

    protected void doBatchOperations(PageForm theForm) {

    	log.debug("set dismissed: " + theForm.getBatchDismissedOp());

    	// Build up the list of selected items 
    	List<AlertSummary> selectedAlertSummaries = new ArrayList<AlertSummary>();
    	ConditionDetectorService cds = NCDUtilities.getService();
    	for (AlertSummary thisAlertSummary : theForm.getVisibleRows()) {

    		if (theForm.isSelected(thisAlertSummary)) {
    			selectedAlertSummaries.add(thisAlertSummary);
    		}
    	}

    	if (!selectedAlertSummaries.isEmpty()) {
    		if (isSet(theForm.getBatchDismissedOp())) {

    			// As a single transaction, dismiss or undismiss the selected alert groups
    			if (theForm.getBatchDismissedOp().equals("true")) {
    				String reason = null;
    				if (theForm.getBatchReason() != null && theForm.getBatchReason().trim().length() > 0) {
    					reason = theForm.getBatchReason();
    				}
    				cds.dismissAlertSummaries(selectedAlertSummaries, reason);
    			}
    			else {
    				cds.undismissAlertSummaries(selectedAlertSummaries);
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
}

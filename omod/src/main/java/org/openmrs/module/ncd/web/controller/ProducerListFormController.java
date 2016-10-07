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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.filter.SearchFilterProducers;
import org.openmrs.module.ncd.database.filter.SearchFilterProducers.SortKeys;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/producerList.jsp page.
 */
@SuppressWarnings("deprecation")
public class ProducerListFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    public class PageForm extends ListPageForm<SearchFilterProducers, HL7Producer> {

        public PageForm(SearchFilterProducers filter) {

        	super(filter);
        }

        /** Deletes the MessageSources */
        public void deleteSelected() {

            for (HL7Producer src : getLastSearchResult().getResultRows()) {
                if (isSelected(src)) {
                    NCDUtilities.getService().deleteProducer(src);
                }
            }
        }

        /**
         * @return the producers
         */
        public List<HL7Producer> getProducers() {
            return getLastSearchResult().getResultRows();
        }
    }

    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

    	Map<String, Object> refData = new HashMap<String,Object>();
    	
    	// none

    	return refData;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {

	    HttpSession httpSession = request.getSession();
        MessageSourceAccessor msa = getMessageSourceAccessor();
	    PageForm theForm = (PageForm) object;

        String listAction = request.getParameter("listaction");
        if (listAction == null) {
        	listAction = "";
        }

        String sortAction = request.getParameter("sortaction");
        if (sortAction == null) {
        	sortAction = "";
        }

        if (msa.getMessage("ncd.buttons.refresh").equals(listAction)) {
        	
        	refreshList(httpSession, theForm);
            
            return showForm(request, response, exceptions);
        }
        else if (listAction.equals(msa.getMessage("ncd.buttons.firstpage"))) {

            // step back to the first page
            theForm.firstPage();
            setPageMessage(httpSession, theForm);

            return showForm(request, response, exceptions);
        }
        else if (listAction.equals(msa.getMessage("ncd.buttons.prevpage"))) {

            // step back to the previous page, if any
            theForm.previousPage();
            setPageMessage(httpSession, theForm);

            return showForm(request, response, exceptions);
        }
        else if (listAction.equals(msa.getMessage("ncd.buttons.nextpage"))) {

            // advance to the next page, if any
            theForm.nextPage();
            setPageMessage(httpSession, theForm);

            return showForm(request, response, exceptions);
        }
        else if (listAction.equals(msa.getMessage("ncd.buttons.lastpage"))) {

            // advance to the last page, if any
            theForm.lastPage();
            setPageMessage(httpSession, theForm);

            return showForm(request, response, exceptions);
        }
        else if (sortAction.length() > 0) {
        	
        	// sort the list per the selected sort column
      		SortKeys sortKey = SortKeys.valueOf(sortAction);
        	theForm.getFilter().setSortKey(sortKey);
        	refreshList(httpSession, theForm);
            
            return showForm(request, response, exceptions);
        }

        return new ModelAndView(new RedirectView(getSuccessView()));
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
	protected PageForm formBackingObject(HttpServletRequest request) throws Exception {

    	SearchFilterProducers filter = new SearchFilterProducers();
    	filter.setSortKey(SearchFilterProducers.SortKeys.APPLICATION);
    	filter.setSortAscending(true);
    	// minus one because 1 gets added to it in the DAO
    	filter.setMaxRows(Integer.MAX_VALUE - 1);
    	
    	PageForm theForm = new PageForm(filter);
    	refreshList(request.getSession(), theForm);
    	
        return theForm;
    }
    
    protected void refreshList(HttpSession httpSession, PageForm theForm) {

        SearchFilterProducers filter = theForm.getFilter();
    	theForm.setLastSearchResult(NCDUtilities.getService().findProducers(filter));
        
        // If the list update failed
        if (theForm.getLastSearchResult().isSuccessful()) {
            
            setPageMessage(httpSession, theForm);
        }
        else {
            
            // Set the top of page error message
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.filter.queryerror");
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

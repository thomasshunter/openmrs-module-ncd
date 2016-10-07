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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.Pair;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/basicmoduleForm.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
public class ReportableResultDetailFormController extends SessionFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /** The URL of the list form controller */
    protected String listView;

    public String getListView() {
    	return listView;
    }
    
    public void setListView(String listView) {
    	this.listView = listView;
    }

    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

        Map<String, Object> refData = new HashMap<String,Object>();
        
        refData.put("manualreviewstatustypes", getManualReviewStatusTypes());
        refData.put("allConditionNames", getAllConditionNames());

        return refData;
	}

    protected ArrayList<Pair<Integer, String>> getManualReviewStatusTypes() {
    	return NCDUtilities.getService().getReviewStatusTypes();
    }
    
    protected List<String> getAllConditionNames() {
        
        List<String> data = new ArrayList<String>();
        data.add("Unknown");
        data.addAll(NCDUtilities.getService().getAllConditionNames());
        return data;
    }
    
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@SuppressWarnings("deprecation")
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {		

        ReportableResultDetailForm theForm = (ReportableResultDetailForm) object;
        String action = request.getParameter("action");
        MessageSourceAccessor msa = getMessageSourceAccessor();
        
        if (action == null) {

            log.debug("onSubmit: no action");
            return showForm(request, response, exceptions);
        }
        else if (action.equals(msa.getMessage("ncd.buttons.close"))) {

            log.debug("onSubmit: action=close");
            return new ModelAndView(new RedirectView(getReturnView(false, theForm)));
        }
        else if (action.equals(msa.getMessage("ncd.pages.reportableresultdetail.buttons.release"))) {

            log.debug("onSubmit: action=release");
            NCDUtilities.getService().releaseReportableResult(theForm.getResult());
            return new ModelAndView(new RedirectView(getReturnView(true, theForm)));
        }
        else if (action.equals(msa.getMessage("ncd.pages.reportableresultdetail.buttons.reject"))) {

            log.debug("onSubmit: action=reject");
            NCDUtilities.getService().rejectReportableResult(theForm.getResult());
            return new ModelAndView(new RedirectView(getReturnView(true, theForm)));
        }
        else if (action.equals(msa.getMessage("ncd.pages.reportableresultdetail.buttons.sentinerror"))) {

            log.debug("onSubmit: action=sent in error");
            ArrayList<ReportableResult> theList = new ArrayList<ReportableResult>();
            theList.add(theForm.getResult());
            NCDUtilities.getService().reportableResultSentInError(theList);
            return new ModelAndView(new RedirectView(getReturnView(true, theForm)));
        }
        else if (action.equals(msa.getMessage("ncd.pages.reportableresultdetail.buttons.notsentinerror"))) {

            log.debug("onSubmit: action=not sent in error");
            ArrayList<ReportableResult> theList = new ArrayList<ReportableResult>();
            theList.add(theForm.getResult());
            NCDUtilities.getService().reportableResultNotSentInError(theList);
            return new ModelAndView(new RedirectView(getReturnView(true, theForm)));
        }
        else if (action.equals(msa.getMessage("ncd.buttons.first"))) {

        	log.debug("go to first");
        	return getNavigationView("first", theForm.getResult().getId());
        }
        else if (action.equals(msa.getMessage("ncd.buttons.previous"))) {
        	
        	log.debug("go to previous");
        	return getNavigationView("previous", theForm.getResult().getId());
        }
        else if (action.equals(msa.getMessage("ncd.buttons.next"))) {
        	
        	log.debug("go to next");
        	return getNavigationView("next", theForm.getResult().getId());
        }
        else if (action.equals(msa.getMessage("ncd.buttons.last"))) {
        	
        	log.debug("go to last");
        	return getNavigationView("last", theForm.getResult().getId());
        }
        else {

            log.debug("onSubmit: action=" + action);
            return showForm(request, response, exceptions);
        }
    }

	protected ModelAndView getNavigationView(String movement, Long currentId) {
		
		RedirectView view = new RedirectView(listView + "?" + movement +"&currentId=" + currentId, true);
		return new ModelAndView(view);
	}

	/**
	 * Gets the URL of the form controller to return to, and clears
	 * the form (ick)
	 * 
	 * @param refreshReferrer Pass true to auto refresh the referring page being returned to. 
	 * @return
	 */
	@SuppressWarnings("deprecation")
    protected String getReturnView(boolean refreshReferrer, ReportableResultDetailForm theForm) {
	    
	    if (theForm == null || theForm.getReturnURL() == null) {
	        return getSuccessView();
	    }
	    else {
	        String returnURL = theForm.getReturnURL();
            theForm = null;
            if (refreshReferrer && !returnURL.contains("refresh=1")) {
            	if (!returnURL.contains("?")) {
            		returnURL += "?";
            	}
            	else {
            		returnURL += "&";
            	}
            	returnURL += "refresh=1";
            }
	        return returnURL;
	    }
	}

	@Override
	public Object getNewForm(HttpServletRequest request) throws Exception {
		
    	// Get the value of the id being edited from the URL parameter, or 0 if not present
        String editIdStr = request.getParameter("edit");
        long editId = 0;
        if (editIdStr != null && editIdStr.length() > 0) {
        	editId = Long.parseLong(editIdStr);
        }
        
        // Check if we should show list navigation buttons
        String showNavButtons = request.getParameter("shownav");

        // Load the specified result
        ReportableResult result = NCDUtilities.getService().getReportableResult(editId);
        ReportableResultDetailForm theForm = new ReportableResultDetailForm();
        theForm.setResult(result);

        // If we didn't get here from ourselves (happens when the navigation buttons are used)
        String returnURL = request.getHeader("Referer");
        log.debug("Referer=" + returnURL);
        if (!returnURL.contains("reportableResultDetail")) {
        	
            // Record the return URL
            theForm.setReturnURL(returnURL);
        }
        
        // Record whether to show and/or enable navigation buttons
        theForm.setShowNavigationButtons(showNavButtons != null);

        // Remove any top of page messages set by the list page
        request.getSession().removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
        request.getSession().removeAttribute(WebConstants.OPENMRS_MSG_ATTR);

    	return theForm;
	}

	@Override
	public boolean isNewFormNeeded(HttpServletRequest request, Object form) {
		
		ReportableResultDetailForm theForm = (ReportableResultDetailForm) form;
		
        String editIdStr = request.getParameter("edit");
        long editId = 0;
        if (editIdStr != null && editIdStr.length() > 0) {
        	editId = Long.parseLong(editIdStr);
        }
        
        return theForm == null || !theForm.getResult().getId().equals(editId);
	}
}

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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.CodeType;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/** A template for new form controller classes */
@SuppressWarnings("deprecation")
public class CodeTypeDetailFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

        Map<String, Object> refData = new HashMap<String, Object>();
        return refData;
	}
    
    protected boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }
    
    protected boolean isDuplicate(CodeType form) {

        CodeType codeType = NCDUtilities.getService().getCodeType(form.getName());
        return codeType != null && codeType.getId() != form.getId();
    }

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {
	    
        CodeType theForm = (CodeType) object;
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        log.debug("onSubmit: action=" + action);
        MessageSourceAccessor msa = getMessageSourceAccessor();
        
        if (action.equals(msa.getMessage("ncd.buttons.cancel"))) {

            theForm = null;
            return new ModelAndView(new RedirectView(getSuccessView()));
        }
        else if (action.equals(msa.getMessage("ncd.buttons.save"))) {
            
            HttpSession httpSession = request.getSession();

            // TODO: form validation here
            if (!isSet(theForm.getName())) {

                exceptions.rejectValue("name", "ncd.error.required");
            }
            
            if (!isSet(theForm.getDisplayTextKey())) {

                exceptions.rejectValue("displayTextKey", "ncd.error.required");
            }

            boolean success = !exceptions.hasErrors();
    
            // If any field fails validation
            if (!success) {
    
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.codeTypeDetail.error.validation");
                
                return showForm(request, response, exceptions);
            }
            // If a distinct duplicate exists
            else if (isDuplicate(theForm)) {
    
                // Report that a duplicate exists
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.codeTypeDetail.error.duplicate");
                
                return showForm(request, response, exceptions);
            }
            else {

                // persist the form
                NCDUtilities.getService().saveCodeType(theForm);
                
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                if (theForm.getId() == null) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.codeTypeDetail.error.added");
                }
                else {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.codeTypeDetail.error.updated");
                }
                
                theForm = null;
                return new ModelAndView(new RedirectView(getSuccessView()));
            }
        }
        else {

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
	protected CodeType formBackingObject(HttpServletRequest request) throws Exception {

        // If the request has an "edit" parameter
        String idStr = request.getParameter("edit");
        if (idStr != null) {

            return new CodeType(NCDUtilities.getService().getCodeType(Long.parseLong(idStr)));
        }
        else {

            return new CodeType();
        }
    }
}

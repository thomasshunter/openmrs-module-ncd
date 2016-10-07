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
import org.openmrs.module.ncd.database.ConditionGroup;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/criticList.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
@SuppressWarnings("deprecation")
public class ConditionGroupDetailFormController extends SimpleFormController {

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
    
    protected boolean isDuplicate(ConditionGroup newGroup) {
        
        ConditionGroup group = NCDUtilities.getService().findConditionGroupByName(newGroup.getDisplayText());

        return group != null && group.getId() != newGroup.getId();
    }

    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {
        
        ConditionGroup theForm = (ConditionGroup) object;
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
            boolean success = true;
            boolean isNew = theForm.getId() == null;
    
            if (isNew) {
    
                if (!isSet(theForm.getDisplayText())) {

                    exceptions.rejectValue("displayText", "ncd.error.required");
                }
                
                if (theForm.getDisplayOrder() == null) {
                    
                    exceptions.rejectValue("displayOrder", "ncd.error.required");
                }
            }
    
            success = !exceptions.hasErrors();
    
            // If any field fails validation
            if (!success) {
    
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.conditiondetail.error.validation");
                
                return showForm(request, response, exceptions);
            }
            // If a distinct duplicate exists
            else if (isNew && isDuplicate(theForm)) {
    
                // Report that a duplicate exists
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.conditiongroupdetail.error.duplicate");
                
                return showForm(request, response, exceptions);
            }
            else {
    
                NCDUtilities.getService().saveConditionGroup(theForm);
    
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                if (isNew) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.conditiongroupdetail.error.added");
                }
                else {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.conditiongroupdetail.error.updated");
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
    protected ConditionGroup formBackingObject(HttpServletRequest request) throws Exception {

        // If the request has an "edit" parameter
        String editGroupIdStr = request.getParameter("edit");
        log.debug("edit=" + editGroupIdStr);
        
        if (editGroupIdStr != null) {

            Integer editGroupId = Integer.valueOf(editGroupIdStr);
            
            ConditionGroup group = NCDUtilities.getService().findConditionGroupById(editGroupId);
            group = new ConditionGroup(group);
            return group;
        }
        else {

            // The FBO is a blank condition group
            return new ConditionGroup();
        }
    }
}

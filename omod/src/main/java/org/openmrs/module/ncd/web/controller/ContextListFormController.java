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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.NlpCriticContext;
import org.openmrs.module.ncd.database.NlpCriticContextGroup;
import org.openmrs.module.ncd.database.NlpCriticContextType;
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
public class ContextListFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    public class PageForm {

        String contextTypeName;
        String newContextValue;
        String newContextGroup;
        List<Long> selectedContexts;
        List<NlpCriticContext> contexts;
        
        public PageForm(String contextTypeName) {
            
            this.contextTypeName = contextTypeName;
            this.contexts = new ArrayList<NlpCriticContext>();
            this.newContextValue = "";
            this.newContextGroup = "";
            clearSelection();
            loadContexts();
        }

        public void clearSelection() {
            
            this.selectedContexts = new ArrayList<Long>();
        }
        
        public void deleteSelectedContexts() {
            
            for (NlpCriticContext context : contexts) {
                
                for (Long id : selectedContexts) {

                    if (context.getId().equals(id)) {
                        
                        context.getContextType().getNlpContexts().remove(context);
                        NCDUtilities.getService().deleteNlpCriticContext(context);
                    }
                }
            }
            
            loadContexts();
        }

        public void loadContexts() {
            
            clearSelection();
            
            // Get the named context type
            NlpCriticContextType type = NCDUtilities.getService().findContextTypeByName(contextTypeName);

            // Get the contexts from the type;
            contexts = new ArrayList<NlpCriticContext>(type.getNlpContexts());
            
            // Sort the list by context value
            Collections.sort(contexts, new ContextNameComparator());
        }
        
        public boolean exists(String contextValue) {

            for (NlpCriticContext context : contexts) {
                
                if (context.getContextValue().equals(contextValue)) {

                    return true;
                }
            }
            
            return false;
        }

        public String getContextTypeName() {
            return contextTypeName;
        }

        public void setContextTypeName(String contextTypeName) {
            this.contextTypeName = contextTypeName;
        }

        public List<NlpCriticContext> getContexts() {
            return contexts;
        }

        public void setContexts(List<NlpCriticContext> phrases) {
            this.contexts = phrases;
        }

        /**
         * @return the selectedContexts
         */
        public List<Long> getSelectedContexts() {
            return selectedContexts;
        }

        /**
         * @param selectedContexts the selectedContexts to set
         */
        public void setSelectedContexts(List<Long> selectedContexts) {
            this.selectedContexts = selectedContexts;
        }

        /**
         * @return the newContextValue
         */
        public String getNewContextValue() {
            return newContextValue;
        }

        /**
         * @param newContextValue the newContextValue to set
         */
        public void setNewContextValue(String newContextValue) {
            this.newContextValue = newContextValue;
        }

        /**
         * @return the newContextGroup
         */
        public String getNewContextGroup() {
            return newContextGroup;
        }

        /**
         * @param newContextGroup the newContextGroup to set
         */
        public void setNewContextGroup(String newContextGroup) {
            this.newContextGroup = newContextGroup;
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

        refData.put("allContextGroups", getContextGroups());
        
        return refData;
	}

    private List<String> getContextGroups() {
    
        List<NlpCriticContextGroup> groups = NCDUtilities.getService().listContextGroups();
        List<String> data = new ArrayList<String>();
        for (NlpCriticContextGroup group : groups) {
            data.add(group.getDisplayText());
        }
        return data;
    }
    
    protected boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }

    protected String normalize(String contextValue) {
        
        String ret = contextValue.toLowerCase();

        if (!ret.startsWith(" ")) {
            ret = " " + ret;
        }

        if (!ret.endsWith(" ")) {
            ret = ret + " ";
        }

        while (ret.indexOf("  ") != -1) {
            ret = ret.replaceAll("  ", " ");
        }

        return ret;
    }
    
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {

        PageForm theForm = (PageForm) object;
        String action = request.getParameter("action");
        MessageSourceAccessor msa = getMessageSourceAccessor();
        
        if (action == null) {

            log.debug("onSubmit: no action");
            return showForm(request, response, exceptions);
        }
        else if (action.equals(msa.getMessage("ncd.buttons.cancel"))) {

            log.debug("onSubmit: action=cancel");
            return new ModelAndView(new RedirectView(getSuccessView()));
        }
        else if (action.equals(msa.getMessage("ncd.buttons.remove"))) {

            log.debug("onSubmit: action=remove");
            theForm.deleteSelectedContexts();
            
            return showForm(request, response, exceptions);
        }
        else if (action.equals(msa.getMessage("ncd.pages.contextList.add"))) {
            
            log.debug("onSubmit: action=add");

            HttpSession httpSession = request.getSession();
            boolean success = true;
    
            if (!isSet(theForm.getNewContextValue())) {

                exceptions.rejectValue("newContextValue", "ncd.error.required"); 
            }
    
            success = !exceptions.hasErrors();
    
            // If any field fails validation
            if (!success) {
    
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.contextList.error.validation");
                
                return showForm(request, response, exceptions);
            }
            // If a distinct duplicate exists
            else if (theForm.exists(theForm.getNewContextValue())) {
    
                // Report that a duplicate exists
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.contextList.error.duplicate");
                
                return showForm(request, response, exceptions);
            }
            else {
    
                // Create the new context
                NlpCriticContext newContext = new NlpCriticContext();
                newContext.setContextValue(normalize(theForm.getNewContextValue()));
                newContext.setContextGroup(theForm.getNewContextGroup());
                
                // Get the context type, add the new context, and save the
                // context type.
                NlpCriticContextType type = NCDUtilities.getService().findContextTypeByName(theForm.contextTypeName);
                type.getNlpContexts().add(newContext);
                newContext.setContextType(type);
                NCDUtilities.getService().saveNlpCriticContext(newContext);
                NCDUtilities.getService().saveNlpCriticContextType(type);
    
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.contextList.error.added");

                // Refresh the list
                theForm.loadContexts();
                
                return showForm(request, response, exceptions);
            }
        }
        else {

            log.debug("onSubmit: action=" + action);
            return showForm(request, response, exceptions);
        }
    }
	
	public class ContextNameComparator implements Comparator<NlpCriticContext> {

        public int compare(NlpCriticContext o1, NlpCriticContext o2) {

            return o1.getContextValue().compareTo(o2.getContextValue());
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
	protected PageForm formBackingObject(HttpServletRequest request) throws Exception {

        // If the request has an "edit" parameter
        String editContextTypeName = request.getParameter("edit");

        // Build an FBO for the named critic
        return new PageForm(editContextTypeName);
    }
}

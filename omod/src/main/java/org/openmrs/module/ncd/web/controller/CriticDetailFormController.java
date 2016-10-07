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
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.cache.NlpCriticConceptCache;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.NlpCriticConcept;
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
public class CriticDetailFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    public class PageForm {

        boolean newConcept;
        NlpCriticConcept concept;
        String conditionName;
        List<String> contexts;
        
        public PageForm(boolean newConcept, NlpCriticConcept concept, String conditionName, List<String> contexts) {
            
            this.newConcept = newConcept;
            this.concept = concept;
            this.conditionName = conditionName;
            this.contexts = contexts;
        }

        public boolean isNewConcept() {
        
            return this.newConcept;
        }
        
        public void setNewConcept(boolean newConcept) {
            
            this.newConcept = newConcept;
        }
        
        public NlpCriticConcept getConcept() {
            return concept;
        }

        public void setConcept(NlpCriticConcept concept) {
            this.concept = concept;
        }

        public String getConditionName() {
            return conditionName;
        }

        public void setConditionName(String conditionName) {
            this.conditionName = conditionName;
        }

        public List<String> getContexts() {
            return contexts;
        }

        public void setContexts(List<String> contexts) {
            this.contexts = contexts;
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
        
        refData.put("conditionnames", getConditionNames());
        refData.put("allcontexts", getContexts());

        return refData;
	}

    protected List<String> getConditionNames() {
        
        return NCDUtilities.getService().getAllConditionNames();
    }

    protected List<String> getContexts() {
        
        List<NlpCriticContextType> types = NCDUtilities.getService().listContextTypes();
        ArrayList<String> contexts = new ArrayList<String>(types.size());
        for (NlpCriticContextType type : types) {
            contexts.add(type.getTypeName());
        }
        return contexts;
    }

    protected boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }
    
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {		

        log.debug("onSubmit");

        PageForm theForm = (PageForm) object;
        HttpSession httpSession = request.getSession();
        MessageSourceAccessor msa = getMessageSourceAccessor();

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        
        if (action.equals(msa.getMessage("ncd.buttons.cancel"))) {

            log.debug("action=cancel");
            return new ModelAndView(new RedirectView(getSuccessView()));
        }
        else if (action.equals(msa.getMessage("ncd.buttons.save"))) {
            
            log.debug("action=save");
            
            boolean isNew = theForm.isNewConcept();
            log.debug("isNew=" + isNew);
            
            // The concept name is required
            if (!isSet(theForm.getConcept().getConceptName())) {
                exceptions.rejectValue("concept.conceptName", "ncd.error.required"); 
            }
            else if (isNew && NlpCriticConceptCache.get(theForm.getConcept().getConceptName()) != null) {
                exceptions.rejectValue("concept.conceptName", "ncd.pages.criticdetail.error.duplicate"); 
            }
            // - code is required
            if (!isSet(theForm.getConcept().getCode())) {
                exceptions.rejectValue("concept.code", "ncd.error.required"); 
            }
            // - nameCode is required
            if (!isSet(theForm.getConcept().getNameCode())) {
                exceptions.rejectValue("concept.nameCode", "ncd.error.required"); 
            }

            // If validation failed
            if (exceptions.hasErrors()) {
                
                log.debug("validation errors");
                
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.error.validation");
                
                return showForm(request, response, exceptions);
            }
            else {
                
                log.debug("new validation errors");
                
                // Get the modified concept
                NlpCriticConcept concept = theForm.getConcept();

                // Store the condition to the concept
                Condition condition = NCDUtilities.getService().findConditionByName(theForm.getConditionName());
                concept.setCondition(condition);

                // Store the contexts to the concept
                log.debug("selected contexts=" + theForm.getContexts());
                concept.setContexts(StringUtilities.merge(theForm.getContexts(), "_"));

                log.debug("concept to persist=" + concept);
                
                // Add or update the critic (concept)
                NlpCriticConceptCache.save(concept);
                
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                if (isNew) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.criticdetail.error.added");
                }
                else {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.criticdetail.error.updated");
                }
                
                return new ModelAndView(new RedirectView(getSuccessView()));
            }
        }
        else {
            
            log.debug("unexpected action=" + action);
            return new ModelAndView(new RedirectView(getSuccessView()));
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
        String editConceptName = request.getParameter("edit");
        log.debug("edit=" + editConceptName);

        if (editConceptName != null) {

            log.debug("edit existing concept");
            
            NlpCriticConcept editConcept = NlpCriticConceptCache.get(editConceptName);
            log.debug("concept from db=" + editConcept);
            
            editConcept = new NlpCriticConcept(editConcept);
            log.debug("copy of concept=" + editConcept);
            
            String editConditionName = "";
            if (editConcept.getCondition() != null) {
                editConditionName = editConcept.getCondition().getDisplayText();
            }
            
            ArrayList<String> contexts = new ArrayList<String>();
            String[] temp = editConcept.getContexts().split("_");
            for (String context : temp) {
                contexts.add(context);
            }

            // Build an FBO for the named critic
            return new PageForm(false, editConcept, editConditionName, contexts);
        }
        else {

            // Build an FBO for a new critic
            return new PageForm(true, new NlpCriticConcept(), "", new ArrayList<String>());
        }
    }
}

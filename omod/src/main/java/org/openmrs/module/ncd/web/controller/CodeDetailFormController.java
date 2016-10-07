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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.CodeSystem;
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
public class CodeDetailFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    public class PageForm {
    
        private Long codeSystemId;
        private Long codeTypeId;
        private Code code;
        
        public PageForm(Code code) {
            
            this.code = code;
            if (this.code.getCodeSystem() != null) {
                this.codeSystemId = this.code.getCodeSystem().getId();
            }
            if (this.code.getCodeType() != null) {
                this.codeTypeId = this.code.getCodeType().getId();
            }
        }

        /**
         * @return the codeSystemId
         */
        public Long getCodeSystemId() {
            return codeSystemId;
        }

        /**
         * @param codeSystemId the codeSystemId to set
         */
        public void setCodeSystemId(Long codeSystemId) {
            this.codeSystemId = codeSystemId;
        }

        /**
         * @return the codeTypeId
         */
        public Long getCodeTypeId() {
            return codeTypeId;
        }

        /**
         * @param codeTypeId the codeTypeId to set
         */
        public void setCodeTypeId(Long codeTypeId) {
            this.codeTypeId = codeTypeId;
        }

        /**
         * @return the code
         */
        public Code getCode() {
            return code;
        }

        /**
         * @param code the code to set
         */
        public void setCode(Code code) {
            this.code = code;
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
        refData.put("allCodeTypes", getCodeTypes());
        refData.put("allCodeSystems", getCodeSystems());
        refData.put("allScaleTypes", getScaleTypes());
        refData.put("mappedConditionNames", getDistinctMappedConditionNames((PageForm) obj));
        return refData;
	}

    private List<CodeType> getCodeTypes() {
    
        return NCDUtilities.getService().getAllCodeTypes();
    }

    private List<CodeSystem> getCodeSystems() {
    
        return NCDUtilities.getService().getAllCodeSystems();
    }

    private List<String> getScaleTypes() {

    	List<String> results = new ArrayList<String>();
    	results.add("");
    	results.add("Doc");
    	results.add("Multi");
    	results.add("Nar");
    	results.add("Nom");
    	results.add("Ord");
    	results.add("OrdQn");
    	results.add("Qn");
    	results.add("Set");
    	return results;
    }

    private String getDistinctMappedConditionNames(PageForm form) {

    	Set<String> conditionsSeen = new HashSet<String>();
    	Code code = form.getCode();
    	StringBuilder results = new StringBuilder();
    	for (CodeCondition mapping : NCDUtilities.getService().findByCodeAndSystem(code.getCode(), code.getCodeSystem().getName())) {
    		String conditionName = mapping.getCondition().getDisplayText();
    		if (!conditionsSeen.contains(conditionName)) {
	    		if (results.length() != 0) {
	    			results.append("; ");
	    		}
	    		results.append(conditionName);
	    		conditionsSeen.add(conditionName);
    		}
    	}
    	return results.toString();
    }

    protected boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }
    
    protected boolean isDuplicate(PageForm form) {

        CodeSystem codeSystem = NCDUtilities.getService().getCodeSystem(form.getCodeSystemId());
        Code newCode = form.getCode();
        Code other = NCDUtilities.getService().getCode(codeSystem, newCode.getCode());
        return (other != null && other.getId() != newCode.getId());
    }

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {
	    
        PageForm theForm = (PageForm) object;
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

            if (!isSet(theForm.getCode().getCode())) {
                
                exceptions.rejectValue("code.code", "ncd.error.required");
            }
            
            boolean success = !exceptions.hasErrors();
    
            // If any field fails validation
            if (!success) {
    
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.codeDetail.error.validation");
                
                return showForm(request, response, exceptions);
            }
            // If a distinct duplicate exists
            else if (isDuplicate(theForm)) {
    
                // Report that a duplicate exists
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.codeDetail.error.duplicate");
                
                return showForm(request, response, exceptions);
            }
            else {

                Code newCode = theForm.getCode();
                
                CodeSystem codeSystem = NCDUtilities.getService().getCodeSystem(theForm.getCodeSystemId());
                newCode.setCodeSystem(codeSystem);
                
                CodeType codeType = NCDUtilities.getService().getCodeType(theForm.getCodeTypeId());
                newCode.setCodeType(codeType);
                
                NCDUtilities.getService().saveCode(newCode);
                
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                if (newCode.getId() == null) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.codeDetail.error.added");
                }
                else {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.codeDetail.error.updated");
                }
                
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
	protected PageForm formBackingObject(HttpServletRequest request) throws Exception {

        // If the request has an "edit" parameter
        String idStr = request.getParameter("edit");
        if (idStr != null) {

            Long id = Long.valueOf(idStr);
            Code code = NCDUtilities.getService().getCode(id);
            return new PageForm(new Code(code));
        }
        else {

            return new PageForm(new Code());
        }
    }
}

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
import org.openmrs.module.ncd.database.Condition;
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

@SuppressWarnings("deprecation")
public class ConditionDetailFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/** The name of the session attribute that will record the form instance. Configured by spring. */
	protected String formAttributeName;

	public class RelatedCodeComparator implements Comparator<CodeCondition> {

        public int compare(CodeCondition o1, CodeCondition o2) {

            // Sort by code system display text first, code length second, code value third, code indicator last
            String csName1 = o1.getCode().getCodeSystem().getName();
            String csName2 = o2.getCode().getCodeSystem().getName();
            int retval = csName1.compareTo(csName2);
            if (retval != 0) {
                return retval;
            }
            
            // shorter codes sort first
            String code1 = o1.getCode().getCode();
            String code2 = o2.getCode().getCode();
            if (code1.length() < code2.length()) {
                
                return -1;
            }
            else if (code1.length() > code2.length()) {

                return 1;
            }

            // Equal length LOINC codes and other codes sort by code text
            retval = code1.compareTo(code2);
            if (retval == 0) {

                retval = StringUtilities.compareTo(o1.getConditionIndicator(), o2.getConditionIndicator());
            }
            return retval;
        }
    }
    
    public class PageForm {
    
        private Condition condition;
        private String conditionGroupName;
        private List<CodeCondition> relatedCodes;
        private List<Long> selectedCodeConditions;
        private String newCode;
        private String newCodeSystem;
        private String newIndicator;
        private long fakeCodeConditionIdGenerator = -1;

        public PageForm(Condition condition) {
            
            this.condition = condition;
            if (condition.getConditionGroup() != null) {
            	this.conditionGroupName = condition.getConditionGroup().getDisplayText();
            }

            refreshCodeConditions();
            clearSelectedCodeConditions();
        }
        
        public void clearSelectedCodeConditions() {
            
            selectedCodeConditions = new ArrayList<Long>();
        }
        
        public void refreshCodeConditions() {
            
            relatedCodes = new ArrayList<CodeCondition>(condition.getCodeConditions());
            log.debug("#related codes before sorting: " + relatedCodes.size());
            Collections.sort(relatedCodes, new RelatedCodeComparator());
            log.debug("#related codes after sorting: " + relatedCodes.size());
        }
        
        public void deleteSelectedCodeConditions() {

            log.debug("code conditions=" + condition.getCodeConditions());

            // Make a copy of the collect to avoid concurrent modification
            Set<CodeCondition> temp = new HashSet<CodeCondition>(condition.getCodeConditions());

            log.debug("temp code conditions=" + temp);
            log.debug("selected code conditions=" + selectedCodeConditions);

            for (CodeCondition cc : condition.getCodeConditions()) {
                
            	if (selectedCodeConditions != null) {
	                for (Long selectedId : selectedCodeConditions) {
	                    
	                    if (cc.getId().equals(selectedId)) {
	                        
	                        log.debug("remove code condition=" + cc);
	                        temp.remove(cc);
	                    }
	                }
            	}
            }
            
            condition.setCodeConditions(temp);
            refreshCodeConditions();
            clearSelectedCodeConditions();
        }

        public void addCodeCondition(BindException exceptions) {
            
            // Look the code system up.
            CodeSystem codeSystem = NCDUtilities.getService().getCodeSystem(newCodeSystem);
            if (codeSystem == null) {
                
                exceptions.rejectValue("newCodeSystem", "ncd.pages.conditiondetail.error.codesystem");
                return;
            }

            // Look the code up.
            Code code = NCDUtilities.getService().getCode(codeSystem, newCode);
            if (code == null) {

                exceptions.rejectValue("newCode", "ncd.pages.conditiondetail.error.code");
                return;
            }
            
            // If the (code, code system, code indicator) already appears in the list, it's a
            // duplicate.
            for (CodeCondition oldcc : condition.getCodeConditions()) {
                
                if (oldcc.getCode().getCode().equals(newCode) &&
                    oldcc.getCode().getCodeSystem().getName().equals(newCodeSystem) &&
                    StringUtilities.equals(oldcc.getConditionIndicator(), newIndicator))
                {
                    exceptions.rejectValue("newCode", "ncd.pages.conditiondetail.error.codeduplicate");
                    return;
                }
            }

            // Construct the CodeCondition and add it to the condition
            CodeCondition cc = new CodeCondition();
            cc.setId(new Long(fakeCodeConditionIdGenerator--));
            cc.setCode(code);
            cc.setCondition(condition);
            cc.setConditionIndicator(newIndicator);
            condition.getCodeConditions().add(cc);

            refreshCodeConditions();
        }

        /**
         * @return the condition
         */
        public Condition getCondition() {
            return condition;
        }

        /**
         * @param condition the condition to set
         */
        public void setCondition(Condition condition) {
            this.condition = condition;
        }

        /**
         * @return the selectedCodeConditions
         */
        public List<Long> getSelectedCodeConditions() {
            return selectedCodeConditions;
        }

        /**
         * @param selectedCodeConditions the selectedCodeConditions to set
         */
        public void setSelectedCodeConditions(List<Long> selectedCodeConditions) {
            this.selectedCodeConditions = selectedCodeConditions;
        }

        /**
         * @return the relatedCodes
         */
        public List<CodeCondition> getRelatedCodes() {
            return relatedCodes;
        }

        /**
         * @param relatedCodes the relatedCodes to set
         */
        public void setRelatedCodes(List<CodeCondition> relatedCodes) {
            this.relatedCodes = relatedCodes;
        }

        /**
         * @return the newCode
         */
        public String getNewCode() {
            return newCode;
        }

        /**
         * @param newCode the newCode to set
         */
        public void setNewCode(String newCode) {
            this.newCode = newCode;
        }

        /**
         * @return the newCodeSystem
         */
        public String getNewCodeSystem() {
            return newCodeSystem;
        }

        /**
         * @param newCodeSystem the newCodeSystem to set
         */
        public void setNewCodeSystem(String newCodeSystem) {
            this.newCodeSystem = newCodeSystem;
        }

        /**
         * @return the newIndicator
         */
        public String getNewIndicator() {
            return newIndicator;
        }

        /**
         * @param newIndicator the newIndicator to set
         */
        public void setNewIndicator(String newIndicator) {
            this.newIndicator = newIndicator;
        }

        /**
         * @return the conditionGroupName
         */
        public String getConditionGroupName() {
            return conditionGroupName;
        }

        /**
         * @param conditionGroupName the conditionGroupName to set
         */
        public void setConditionGroupName(String conditionGroupName) {
            this.conditionGroupName = conditionGroupName;
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
        refData.put("allCodeSystems", getCodeSystems());
        refData.put("allConditionGroups", getConditionGroups());
        return refData;
	}

    protected List<String> getCodeSystems() {
        
        List<String> data = new ArrayList<String>();
        List<CodeSystem> systems = NCDUtilities.getService().getAllCodeSystems();
        for (CodeSystem system : systems) {
            data.add(system.getName());
        }
        return data;
    }
    
    protected List<String> getConditionGroups() {
        
        List<String> data = new ArrayList<String>();
        List<ConditionGroup> groups = NCDUtilities.getService().getAllConditionGroups();
        for (ConditionGroup group : groups) {
            data.add(group.getDisplayText());
        }
        return data;
    }
    
    protected boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }
    
    protected boolean isDuplicate(String name) {
        
        Condition condition = NCDUtilities.getService().findConditionByName(name);

        return condition != null;
    }

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {
	    
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        log.debug("onSubmit: action=" + action);
        PageForm theForm = getCurrentForm(request);
        MessageSourceAccessor msa = getMessageSourceAccessor();
        
        if (action.equals(msa.getMessage("ncd.buttons.cancel"))) {

            request.getSession().removeAttribute(formAttributeName);
            return new ModelAndView(new RedirectView(getSuccessView()));
        }
        else if (action.equals(msa.getMessage("ncd.buttons.save"))) {
            
            HttpSession httpSession = request.getSession();
            boolean success = true;
            boolean isNew = theForm.getCondition().getId() == null;
    
            if (isNew) {
    
                if (!isSet(theForm.getCondition().getDisplayText())) {

                    exceptions.rejectValue("condition.displayText", "ncd.error.required");
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
            else if (isNew && isDuplicate(theForm.getCondition().getDisplayText())) {
    
                // Report that a duplicate exists
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.conditiondetail.error.duplicate");
                
                return showForm(request, response, exceptions);
            }
            else {
    
                // NOTE: Don't try to modify Condition.CodeConditions inline
                // It's a Set; CodeCondition.id drives equals and hashCode, which messes Sets up
                List<CodeCondition> codeConditions = new ArrayList<CodeCondition>(theForm.getCondition().getCodeConditions());
                for (CodeCondition cc : codeConditions) {
                    
                    if (cc.getId() < 0) {
                        
                        cc.setId(null);
                    }
                }
                theForm.getCondition().setCodeConditions(new HashSet<CodeCondition>(codeConditions));

                ConditionGroup group = NCDUtilities.getService().findConditionGroupByName(theForm.getConditionGroupName());
                theForm.getCondition().setConditionGroup(group);
                
                log.debug("saving condition=" + theForm.getCondition());
                log.debug("codeConditions=" + theForm.getCondition().getCodeConditions());
                log.debug("#codeConditions=" + theForm.getCondition().getCodeConditions().size());
                
                NCDUtilities.getService().saveCondition(theForm.getCondition());
    
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                if (isNew) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.conditiondetail.error.added");
                }
                else {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.conditiondetail.error.updated");
                }
                
                request.getSession().removeAttribute(formAttributeName);
                return new ModelAndView(new RedirectView(getSuccessView()));
            }
        }
        else if (action.equals(msa.getMessage("ncd.buttons.remove"))) {
            
            theForm.deleteSelectedCodeConditions();
            return showForm(request, response, exceptions);
        }
        else if (action.equals(msa.getMessage("ncd.pages.conditiondetail.add"))) {
            
            HttpSession httpSession = request.getSession();

            theForm.addCodeCondition(exceptions);
            
            boolean success = !exceptions.hasErrors();
    
            // If any field fails validation
            if (!success) {
    
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.conditiondetail.error.validation");
                
                return showForm(request, response, exceptions);
            }
            else {
    
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.conditiondetail.error.codeadded");
                
                return showForm(request, response, exceptions);
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
	public PageForm formBackingObject(HttpServletRequest request) throws Exception {

        // Found that adding multiple codes to a condition before saving the condition
        // would not work correctly.  Only the most recent code added would be remembered
        // due to the form being reloaded.  So, kept the logic that does not reload the
        // fbo() while working on the same id.
        boolean newForm = false;
        HttpSession session = request.getSession();
        PageForm theForm = (PageForm) session.getAttribute(formAttributeName);
        if (theForm == null) {
        	newForm = true;
        } else if (request.getParameter("edit") != null) {
        	
        	Long id = Long.parseLong(request.getParameter("edit"));
        	
        	if (!id.equals(theForm.getCondition().getId())) {
        		newForm = true;
        	}
        	
        } else if (theForm.getCondition().getId() != null) {
        	newForm = true;
        }

        if (newForm) {
	        if (request.getParameter("edit") != null) {
	
	        	long id = Long.parseLong(request.getParameter("edit"));
	            Condition editCondition = NCDUtilities.getService().findConditionById(id);
	            editCondition = new Condition(editCondition);
	            
	            log.debug("condition to edit=" + editCondition);
	            log.debug("codeConditions=" + editCondition.getCodeConditions());
	            log.debug("#related codes: " + editCondition.getCodeConditions().size());
	            
	            // The condition is the FBO
	            theForm = new PageForm(editCondition);
	        }
	        else {
	            
	            log.debug("edit a new condition");
	
	            // The FBO is a blank condition
	            theForm = new PageForm(new Condition());
	        }
	       	session.setAttribute(formAttributeName, theForm);
        }
        
        return theForm;
    }

	public PageForm getCurrentForm(HttpServletRequest request) throws Exception {
		
        return formBackingObject(request);
	}

	public String getFormAttributeName() {
		return formAttributeName;
	}

	public void setFormAttributeName(String formAttributeName) {
		this.formAttributeName = formAttributeName;
	}
}

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
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.ConditionGroup;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * This controller backs the /web/module/criticList.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
@SuppressWarnings("deprecation")
public class ConditionListFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    public class PageForm {

        private List<Condition> conditions;
        private List<Long> selectedConditions;
        private List<ConditionGroup> groups;
        private List<Long> selectedGroups;

        public PageForm() {
            
            clearSelected();
            load();
        }

        public void clearSelected() {
            
            clearSelectedConditions();
            clearSelectedGroups();
        }

        public void clearSelectedConditions() {
            
            selectedConditions = new ArrayList<Long>();
        }
        
        public void clearSelectedGroups() {
            
            selectedGroups = new ArrayList<Long>();
        }
        
        public void load() {

            loadConditions();
            loadGroups();
        }
        
        public void loadConditions() {

            clearSelectedConditions();
            conditions = NCDUtilities.getService().getAllConditions();
        }
        
        public void loadGroups() {

            clearSelectedGroups();
            groups = NCDUtilities.getService().getAllConditionGroups();
        }
        
        public void deleteSelectedConditions() {
            
            for (Condition cond : conditions) {
                
                for (Long id : selectedConditions) {
                    
                    if (id.equals(cond.getId())) {
                        
                        NCDUtilities.getService().deleteCondition(cond);
                    }
                }
            }

            clearSelectedConditions();
            loadConditions();
        }
        
        public void deleteSelectedGroups() {
            
            for (ConditionGroup group : groups) {
                
                for (Long id : selectedConditions) {
                    
                    if (id.equals(group.getId())) {
                        
                        NCDUtilities.getService().deleteConditionGroup(group);
                    }
                }
            }

            clearSelected();
            load();
        }

        /**
         * @return the conditions
         */
        public List<Condition> getConditions() {
            return conditions;
        }

        /**
         * @param conditions the conditions to set
         */
        public void setConditions(List<Condition> conditions) {
            this.conditions = conditions;
        }

        /**
         * @return the selectedConditions
         */
        public List<Long> getSelectedConditions() {
            return selectedConditions;
        }

        /**
         * @param selectedConditions the selectedConditions to set
         */
        public void setSelectedConditions(List<Long> selectedConditions) {
            this.selectedConditions = selectedConditions;
        }

        /**
         * @return the groups
         */
        public List<ConditionGroup> getGroups() {
            return groups;
        }

        /**
         * @param groups the groups to set
         */
        public void setGroups(List<ConditionGroup> groups) {
            this.groups = groups;
        }

        /**
         * @return the selectedGroups
         */
        public List<Long> getSelectedGroups() {
            return selectedGroups;
        }

        /**
         * @param selectedGroups the selectedGroups to set
         */
        public void setSelectedGroups(List<Long> selectedGroups) {
            this.selectedGroups = selectedGroups;
        }
    }
    
    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

    	// this method doesn't return any extra data right now, just an empty map
		return new HashMap<String,Object>();
	}


	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {

    	HttpSession httpSession = request.getSession();
        PageForm theForm = (PageForm) object;
        String condaction = request.getParameter("condaction");
        if (condaction == null) {
            condaction = "";
        }
        String groupaction = request.getParameter("groupaction");
        if (groupaction == null) {
            groupaction = "";
        }
        MessageSourceAccessor msa = getMessageSourceAccessor();

        if (condaction.equals(msa.getMessage("ncd.buttons.remove"))) {

            // remove the selected conditions
            theForm.deleteSelectedConditions();

            httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
            httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.conditionList.status.conditions.deleted");
            
            return showForm(request, response, exceptions);
        }
        else if (groupaction.equals(msa.getMessage("ncd.buttons.remove"))) {

            // remove the selected groups
            theForm.deleteSelectedGroups();

            httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
            httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.conditionList.status.groups.deleted");
            
            return showForm(request, response, exceptions);
        }
        else {

            log.debug("onSubmit: condaction=" + condaction);
            log.debug("onSubmit: groupaction=" + groupaction);

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

        return new PageForm();
    }
}

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.CodeSystem;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/** A template for new form controller classes */
@SuppressWarnings("deprecation")
public class CodeSystemListFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    public class PageForm {
    
        private List<CodeSystem> systems;
        private String newSystem;

        // Build a form instance for a new object
        public PageForm() {
            
            systems = NCDUtilities.getService().getAllCodeSystems();
        }
        
        /**
         * @return the systems
         */
        public List<CodeSystem> getSystems() {
            return systems;
        }

        /**
         * @param systems the systems to set
         */
        public void setSystems(List<CodeSystem> systems) {
            this.systems = systems;
        }

        /**
         * @return the newSystem
         */
        public String getNewSystem() {
            return newSystem;
        }

        /**
         * @param newSystem the newSystem to set
         */
        public void setNewSystem(String newSystem) {
            this.newSystem = newSystem;
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
        return refData;
	}
    
    protected boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }
    
    protected boolean isDuplicate(String name) {
        
        CodeSystem system = NCDUtilities.getService().getCodeSystem(name);

        return system != null;
    }
    
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {
        
        //PageForm theForm = (PageForm) object;
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        log.debug("onSubmit: action=" + action);
        //MessageSourceAccessor msa = getMessageSourceAccessor();
       
        return showForm(request, response, exceptions);
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

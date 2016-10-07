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
import org.openmrs.module.ncd.cache.NlpCriticConceptCache;
import org.openmrs.module.ncd.database.NlpCriticConcept;
import org.openmrs.module.ncd.utilities.NCDUtilities;
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
public class CriticListFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    public class PageForm {
        
        private List<NlpCriticConcept> concepts;
        private List<String> selectedConcepts;
        private String exportImportPathname;

        public PageForm(List<NlpCriticConcept> concepts) {
            
            this.concepts = concepts;
            this.selectedConcepts = new ArrayList<String>();
        }

        public List<NlpCriticConcept> getConcepts() {
            return concepts;
        }

        public void setConcepts(List<NlpCriticConcept> concepts) {
            this.concepts = concepts;
        }

        public List<String> getSelectedConcepts() {
            return selectedConcepts;
        }

        public void setSelectedConcepts(List<String> selectedConcepts) {
            this.selectedConcepts = selectedConcepts;
        }

		public String getExportImportPathname() {
			return exportImportPathname;
		}

		public void setExportImportPathname(String exportImportPathname) {
			this.exportImportPathname = exportImportPathname;
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

	    log.debug("onSubmit");

        MessageSourceAccessor msa = getMessageSourceAccessor();
        PageForm pageForm = (PageForm) object;
        
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        
        if (action.equals(msa.getMessage("ncd.pages.criticlist.delete"))) {
            
            log.debug("action=delete");
            
            // for each selected critic
            List<String> selectedConceptNames = pageForm.getSelectedConcepts();

            log.debug("selectedConceptNames=" + selectedConceptNames);

            // delete the selected critics
            NlpCriticConceptCache.delete(selectedConceptNames);
            
            return new ModelAndView(new RedirectView(getSuccessView()));
        }
        else {

            log.debug("unexpected action=" + action);
            // TODO: can't happen
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

        log.debug("formBackingObject");

        return new PageForm(NCDUtilities.getService().listNlpCriticConcepts());
    }
    
    /**
     * This shouldn't be necessary, but I can't find an easier way to handle a
     * GET request. When I tried to export from onSubmit (via a POST), the export
     * would sometimes (10% of the time) save the page source instead of the
     * expected export output.
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	String export = request.getParameter("export");
    	if (export != null) {
    		
            return new ModelAndView(new ExportAllConceptsView());
    	}
    	else {

    		return super.handleRequest(request, response);
    	}
    }
}

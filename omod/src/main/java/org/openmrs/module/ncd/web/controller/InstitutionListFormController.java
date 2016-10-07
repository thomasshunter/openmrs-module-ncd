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
import org.openmrs.module.ncd.database.Institution;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/institutionList.jsp page.
 */
@SuppressWarnings("deprecation")
public class InstitutionListFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    public class PageForm {

        private List<Institution> institutions;
        private List<Long> selectedInstitutionIDs;
        
        public PageForm(List<Institution> institutions) {
            
            this.institutions = institutions;
            selectedInstitutionIDs = new ArrayList<Long>();
        }

        /** Deletes the MessageSources */
        public void deleteSelected() {

            for (Institution src : institutions) {
                if (selectedInstitutionIDs.contains(src.getId())) {
                    NCDUtilities.getService().deleteInstitution(src);
                }
            }
        }

        /**
         * @return the institutions
         */
        public List<Institution> getInstitutions() {
            return institutions;
        }

        /**
         * @param institutions the institutions to set
         */
        public void setInstitutions(List<Institution> institutions) {
            this.institutions = institutions;
        }

        /**
         * @return the selectedInstitutionIDs
         */
        public List<Long> getSelectedInstitutionIDs() {
            return selectedInstitutionIDs;
        }

        /**
         * @param selectedInstitutionIDs the selectedInstitutionIDs to set
         */
        public void setSelectedInstitutionIDs(List<Long> selectedInstitutionIDs) {
            this.selectedInstitutionIDs = selectedInstitutionIDs;
        }
    }

    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

    	Map<String, Object> refData = new HashMap<String,Object>();
    	
    	// none

    	return refData;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {

	    //HttpSession httpSession = request.getSession();
        MessageSourceAccessor msa = getMessageSourceAccessor();
	    PageForm theForm = (PageForm) object;

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        
        if (action.equals(msa.getMessage("ncd.buttons.remove"))) {

            // Delete the selected types
            theForm.deleteSelected();
            
            // Refresh the list
            theForm.setInstitutions(NCDUtilities.getService().getAllInstitutions());
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

        return new PageForm(NCDUtilities.getService().getAllInstitutions());
    }
}

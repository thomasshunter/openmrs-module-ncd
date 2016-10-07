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
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.Institution;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/** Form controller for the producerDetail.jsp page */
@SuppressWarnings("deprecation")
public class ProducerDetailFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    public class PageForm {

    	private HL7Producer producer;
    	private Long institutionId;
    	
    	public PageForm(HL7Producer producer) {
    	
    		this.producer = producer;
    		if (producer.getInstitution() != null) {
    			this.institutionId = producer.getInstitution().getId();
    		}
    		else {
    			this.institutionId = 0L;
    		}
    	}

		public HL7Producer getProducer() {
			return producer;
		}

		public void setProducer(HL7Producer producer) {
			this.producer = producer;
		}

		public Long getInstitutionId() {
			return institutionId;
		}

		public void setInstitutionId(Long institutionId) {
			this.institutionId = institutionId;
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
    	
    	refData.put("allInstitutions", getActiveInstitutions());

    	return refData;
	}

    private List<Institution> getActiveInstitutions() {
    	
    	// TODO: add a new DAO method to return only active institutions
    	return NCDUtilities.getService().getAllActiveInstitutions();
    }

    protected boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }
    
    protected boolean isDuplicate(PageForm form) {

    	HL7Producer formProducer = form.getProducer();

    	String formLocationName = formProducer.getLocationname();
    	if (formLocationName.isEmpty()) {
    		formLocationName = null;
    	}
    	
    	HL7Producer producer = 
    		NCDUtilities.getService().getProducerExact(
    				formProducer.getApplicationname(),
    				formProducer.getFacilityname(), 
    				formLocationName);
        return producer != null && !producer.getId().equals(formProducer.getId());
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

            if (!isSet(theForm.getProducer().getApplicationname())) {

                exceptions.rejectValue("producer.applicationname", "ncd.error.required");
            }
            
            if (!isSet(theForm.getProducer().getFacilityname())) {

                exceptions.rejectValue("producer.facilityname", "ncd.error.required");
            }

            if (theForm.getProducer().isExcluded() && 
            	theForm.getProducer().isReportall()) {
            	
                exceptions.rejectValue("producer.excluded", "ncd.pages.producerDetail.error.excludedreportall");
                exceptions.rejectValue("producer.reportall", "ncd.pages.producerDetail.error.excludedreportall");
            }

            boolean success = !exceptions.hasErrors();
    
            // If any field fails validation
            if (!success) {
    
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.error.validation");
                
                return showForm(request, response, exceptions);
            }
            // If a distinct duplicate exists
            else if (isDuplicate(theForm)) {
    
                // Report that a duplicate exists
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.producerDetail.error.duplicate");
                
                return showForm(request, response, exceptions);
            }
            else {

            	if (theForm.getProducer().getLocationname().isEmpty()) {
            		theForm.getProducer().setLocationname(null);
            	}
            	
            	// Set or update the Institution.
        		theForm.getProducer().setInstitution(NCDUtilities.getService().getInstitution(theForm.getInstitutionId()));

                // persist the form
                NCDUtilities.getService().saveProducer(theForm.getProducer());
                
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                if (theForm.getProducer().getId() == null) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.producerDetail.error.added");
                }
                else {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.producerDetail.error.updated");
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
	protected PageForm formBackingObject(HttpServletRequest request) throws Exception {

        // If the request has an "edit" parameter
        String idStr = request.getParameter("edit");
        if (idStr != null) {

            return new PageForm(new HL7Producer(NCDUtilities.getService().getProducer(Long.parseLong(idStr))));
        }
        else {

            return new PageForm(new HL7Producer());
        }
    }
}

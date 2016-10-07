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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/basicmoduleForm.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
@SuppressWarnings("deprecation")
public class DecidedResultDetailFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /** The display value used for the condition name on a decided result whose report status is not-reported */
    protected String noneCondition = "None";
        	    
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        
        NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
        binder.registerCustomEditor(java.lang.Long.class,
                new CustomNumberEditor(java.lang.Long.class, nf, true));
        binder.registerCustomEditor(java.lang.Double.class,
                new CustomNumberEditor(java.lang.Double.class, nf, true));
        binder.registerCustomEditor(java.util.Date.class, 
                new CustomDateEditor(DateUtilities.getDateTimeFormat(), true));
    }

    public class PageForm {
        
        DecidedResult result;
        String codeName;
        
        public PageForm() {
        }

        /**
         * @return the result
         */
        public DecidedResult getResult() {
            return result;
        }

        /**
         * @param result the result to set
         */
        public void setResult(DecidedResult result) {
            this.result = result;
        }

        /**
         * @return the code
         */
        public String getCodeName() {
            return codeName;
        }

        /**
         * @param codeName the name code to set
         */
        public void setCodeName(String codeName) {
            this.codeName = codeName;
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
        
        // reportable states
        refData.put("reportableStates", getReportableStates());
        refData.put("datatypes", getDatatypes());
        refData.put("conditionnames", getConditionNames());

		return refData;
	}

    protected List<String> getReportableStates() {
        
        List<String> data = new ArrayList<String>();
        data.add("report");
        data.add("not-reported");
        return data;
    }

    protected List<String> getDatatypes() {
        
        List<String> data = new ArrayList<String>();
        data.add("CE");
        data.add("NM");
        data.add("ST");
        data.add("TX");
        return data;
    }
    
    protected List<String> getConditionNames() {
        
        List<String> data = new ArrayList<String>();
        data.add(noneCondition);
        data.addAll(NCDUtilities.getService().getAllConditionNames());
        return data;
    }
    
    protected boolean isSet(String s) {
     
        return !StringUtilities.isNullEmptyOrWhitespace(s);
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

            // TODO: change the JSP to use gotoURL
            log.debug("onSubmit: action=cancel");
            return new ModelAndView(new RedirectView(getSuccessView()));
        }
        else if (action.equals(msa.getMessage("ncd.pages.decidedresultdetail.buttons.getloinc"))) {

            log.debug("onSubmit: action=get loinc");
            
            getLoincName(theForm);
           
            return showForm(request, response, exceptions);
        }
        else if (action.equals(msa.getMessage("ncd.buttons.save"))) {
            
            log.debug("onSubmit: action=save");
            
        	HttpSession httpSession = request.getSession();
            boolean success = true;
            boolean isNew = theForm.getResult().getId() == null;
            Date now = new Date();
    
            if (!isSet(theForm.getResult().getDisposition())) { 
                exceptions.rejectValue("result.disposition", "ncd.error.required"); 
            }
            Date testDate = theForm.getResult().getDateClassified();
            if (testDate == null) {
                exceptions.rejectValue("result.dateClassified", "ncd.error.required"); 
            }
            if (testDate != null && testDate.after(now)) {
                exceptions.rejectValue("result.dateClassified", "ncd.error.datetime.mustbeinpast"); 
            }
            if (!isSet(theForm.getResult().getClassifiedByWhom()) && !noneCondition.equals(theForm.getResult().getConditionName())) { 
                exceptions.rejectValue("result.classifiedByWhom", "ncd.error.required"); 
            }
            if (!isSet(theForm.getResult().getObr())) {
                exceptions.rejectValue("result.obr", "ncd.error.required"); 
            }
            if (!isSet(theForm.getResult().getObrText())) {
                exceptions.rejectValue("result.obrText", "ncd.error.required"); 
            }
            if (!isSet(theForm.getResult().getObrCodeSystem())) {
                exceptions.rejectValue("result.obrCodeSystem", "ncd.error.required"); 
            }
            if (!isSet(theForm.getResult().getObx())) {
                exceptions.rejectValue("result.obx", "ncd.error.required"); 
            }
            if (!isSet(theForm.getResult().getObxText())) {
                exceptions.rejectValue("result.obxText", "ncd.error.required"); 
            }
            if (!isSet(theForm.getResult().getObxCodeSystem())) {
                exceptions.rejectValue("result.obxCodeSystem", "ncd.error.required"); 
            }
            if (!isSet(theForm.getResult().getLoincCode())) {
                exceptions.rejectValue("result.loincCode", "ncd.error.required"); 
            }
            if (isNew) {
                
                theForm.getResult().setDateAdded(now);
                
                List<CodeCondition> matchingCodes = findCode(theForm.getResult().getLoincCode());
                if (matchingCodes == null || matchingCodes.size() <= 0) {
                    exceptions.rejectValue("result.loincCode", "ncd.pages.decidedresultdetail.error.badloinc"); 
                }
            }
            success = !exceptions.hasErrors();
    
            // If any field fails validation
            if (!success) {
    
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.decidedresultdetail.error.validation");
                
                return showForm(request, response, exceptions);
            }
            // If a distinct duplicate exists
            else if (hasDuplicate(theForm.getResult())) {
    
                // Report that a duplicate exists
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.decidedresultdetail.error.duplicate");
                
                return showForm(request, response, exceptions);
            }
            else {
    
                theForm.getResult().setLastModified(now);
    
                ArrayList<DecidedResult> decidedResults 
                    = new ArrayList<DecidedResult>();
                decidedResults.add(theForm.getResult());
                NCDUtilities.getService().saveDecidedResults(decidedResults);
    
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                if (isNew) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.decidedresultdetail.error.added");
                }
                else {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.decidedresultdetail.error.updated");
                }
                
                return new ModelAndView(new RedirectView(getSuccessView()));
            }
        }
        else {

            log.debug("onSubmit: action=" + action);
            return showForm(request, response, exceptions);
        }
    }

	/**
	 * Tests if the specified uncommitted result has at least one duplicate
	 * with a different primary key.
	 * 
	 * @param result The decided result to check for duplicates.
	 * @return True iff at least one distinct duplicate exists.
	 */
	protected boolean hasDuplicate(DecidedResult result) {
	    
        // Look the decided result up by business key
        DecidedResult key = new DecidedResult();
        key.setDataType(result.getDataType());
        key.setLoincCode(result.getLoincCode());
        key.setObr(result.getObr());
        key.setObrCodeSystem(result.getObrCodeSystem());
        key.setObrText(result.getObrText());
        key.setResultCode(result.getResultCode());
        key.setObx(result.getObx());
        key.setObxCodeSystem(result.getObxCodeSystem());
        key.setResultValue(result.getResultValue());
        key.setObxText(result.getObxText());
        key.setNte(result.getNte());

        List<DecidedResult> dups = NCDUtilities.getService().findDecidedResults(key);
        
        // If the uncommitted result is new
        if (result.getId() == null) {

            // Any matching business key is a dupe
            return dups.size() > 0;
        }
        
        // If the uncommitted result is old, a result with a different primary
        // key is a dupe
        for (DecidedResult thisDupe : dups) {

            if (thisDupe.getId() != result.getId()) {
                
                return true;
            }
        }

        return false;
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
    	
        PageForm fbo = new PageForm();
        
        // If the request has an "edit" parameter
        String editResultId = request.getParameter("edit");
        if (editResultId != null) {
            
            // Load that decided result for editing
            int id = Integer.parseInt(editResultId);
            fbo.setResult(NCDUtilities.getService().getDecidedResult(id));
        }
        else {

            // Load a new manual decided result for creation
            fbo.setResult(new DecidedResult());
            fbo.getResult().setResultCount(new Integer(0));
            fbo.getResult().setDisposition("new");
        }

        getLoincName(fbo);

    	return fbo;
    }
    
    public List<CodeCondition> findCode(String code) {
        
        return NCDUtilities.getService().findByCodeAndSystem(code, "LN");
    }

    public void getLoincName(PageForm fbo) {
        
        DecidedResult result = fbo.getResult();
        List<CodeCondition> codes = findCode(result.getLoincCode());
        if (codes != null && codes.size() > 0) {
            CodeCondition code = codes.get(0);
            
            fbo.setCodeName(code.getCode().getDisplayText());
        }
        else {
            
            fbo.setCodeName("(unknown)");
        }
    }
}

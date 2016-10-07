package org.openmrs.module.ncd.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.CodeSystem;
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
public class CodeSystemDetailFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    public class PageForm {
    
        private CodeSystem codeSystem;
        
        public PageForm(CodeSystem codeSystem) {
            
            this.codeSystem = codeSystem;
        }

        /**
         * @return the code system
         */
        public CodeSystem getCodeSystem() {
            return codeSystem;
        }

        /**
         * @param code the code to set
         */
        public void setCodeSystem(CodeSystem codeSystem) {
            this.codeSystem = codeSystem;
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
    
    protected boolean isDuplicate(PageForm form) {

        CodeSystem codeSystem = null;
        if (form.getCodeSystem().getId() == null) {
        	codeSystem = NCDUtilities.getService().getCodeSystem(form.getCodeSystem().getName());
        }

        return codeSystem != null;
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

            if (!isSet(theForm.getCodeSystem().getName())) {
                
                exceptions.rejectValue("codeSystem.name", "ncd.error.required");
            }
            
            boolean success = !exceptions.hasErrors();
    
            // If any field fails validation
            if (!success) {
    
                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.codeSystemDetail.error.validation");
                
                return showForm(request, response, exceptions);
            }
            // If a distinct duplicate exists
            else if (isDuplicate(theForm)) {
    
                // Report that a duplicate exists
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.codeSystemDetail.error.duplicate");
                
                return showForm(request, response, exceptions);
            }
            else {

                CodeSystem newCodeSystem = theForm.getCodeSystem();
                Long id = newCodeSystem.getId(); 
                
                NCDUtilities.getService().saveCodeSystem(newCodeSystem);
                
                // Clear any old error status, report success
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                if (id == null) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.codeSystemDetail.error.added");
                }
                else {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.codeSystemDetail.error.updated");
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
            CodeSystem codeSystem = NCDUtilities.getService().getCodeSystem(id);
            return new PageForm(new CodeSystem(codeSystem));
        }
        else {

            return new PageForm(new CodeSystem());
        }
    }
}

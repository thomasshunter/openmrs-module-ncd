package org.openmrs.module.ncd.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.IMessageProcessor;
import org.openmrs.module.ncd.MesssageProcessorFactory;
import org.openmrs.module.ncd.database.Error;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/errorDetail.jsp page.
 * This controller is tied to that jsp page in the /metadata/moduleApplicationContext.xml file
 * 
 */
public class ErrorDetailFormController extends SessionFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /** The form instance */
    protected ErrorDetailForm theForm = null;

    /**
     * Returns any extra data in a key-->value pair kind of way
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {

        Map<String, Object> refData = new HashMap<String,Object>();
        
        return refData;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@SuppressWarnings("deprecation")
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {		

        ErrorDetailForm theForm = (ErrorDetailForm) object;
        String action = request.getParameter("action");
        MessageSourceAccessor msa = getMessageSourceAccessor();
        
        if (action == null) {

            log.debug("onSubmit: no action");
            return showForm(request, response, exceptions);
        }
        else if (action.equals(msa.getMessage("ncd.buttons.close"))) {

            log.debug("onSubmit: action=close");
            return new ModelAndView(new RedirectView(getReturnView(false)));
        }
        else if (action.equals(msa.getMessage("ncd.pages.errordetail.buttons.reprocess"))) {

            log.debug("onSubmit: action=reprocess");
            
    		IMessageProcessor messageProcessor = MesssageProcessorFactory.createInstance();
			log.debug("reprocessing error (id=" + theForm.getError().getId() + ")");
			messageProcessor.reprocessMessage(theForm.getError());
            return new ModelAndView(new RedirectView(getReturnView(true)));
        }
        else {

            log.debug("onSubmit: action=" + action);
            return showForm(request, response, exceptions);
        }
    }

	/**
	 * Gets the URL of the form controller to return to, and clears
	 * the form (ick)
	 * 
	 * @param refreshReferrer Pass true to auto refresh the referring page being returned to. 
	 * @return
	 */
	@SuppressWarnings("deprecation")
    protected String getReturnView(boolean refreshReferrer) {
	    
	    if (theForm == null || theForm.getReturnURL() == null) {
	        return getSuccessView();
	    }
	    else {
	        String returnURL = theForm.getReturnURL();
            theForm = null;
            if (refreshReferrer && !returnURL.contains("refresh=1")) {
            	if (!returnURL.contains("?")) {
            		returnURL += "?";
            	}
            	else {
            		returnURL += "&";
            	}
            	returnURL += "refresh=1";
            }
	        return returnURL;
	    }
	}

	@Override
	public boolean isNewFormNeeded(HttpServletRequest request, Object form) {
		
		ErrorDetailForm theForm = (ErrorDetailForm) form;
        String editIdStr = request.getParameter("edit");
        long editId = 0;
        if (editIdStr != null && editIdStr.length() > 0) {
        	editId = Long.parseLong(editIdStr);
        }
        
        // If there is no form allocated, or the id has changed, we need a new form
        return (theForm == null || !theForm.getError().getId().equals(editId));
	}

	@Override
	public Object getNewForm(HttpServletRequest request) throws Exception {
        
        String editIdStr = request.getParameter("edit");
        long editId = 0;
        if (editIdStr != null && editIdStr.length() > 0) {
        	editId = Long.parseLong(editIdStr);
        }

        // Load the specified error
        Error error = NCDUtilities.getService().findErrorById(editId);
        theForm = new ErrorDetailForm();
        theForm.setError(error);
        
        // Get the return URL
        String returnURL = request.getHeader("Referer");
        log.debug("Referer=" + returnURL);
        theForm.setReturnURL(returnURL);
        
        return theForm;
	}
}

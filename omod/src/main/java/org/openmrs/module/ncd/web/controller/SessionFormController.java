package org.openmrs.module.ncd.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * A SimpleFormController that retains the same per-session form instance across multiple GET operations.
 * 
 * @author erik horstkotte
 */
@SuppressWarnings("deprecation")
public abstract class SessionFormController extends SimpleFormController {

	/** The name of the session attribute that will record the form instance. Configured by spring. */
	protected String formAttributeName;

	/**
	 * Gets the form instance for this form controller and session, creating one and remembering it if none
	 * already exists.
	 */
	public Object formBackingObject(HttpServletRequest request) throws Exception { 
        
        HttpSession session = request.getSession();
        Object form = session.getAttribute(formAttributeName);
        if (form == null || isNewFormNeeded(request, form)) {
        	form = getNewForm(request);
        	session.setAttribute(formAttributeName, form);
        }
        return form;
    }
	
	/**
	 * Called by formBackingObject to create a new form instance if none already exists. Must be implemented
	 * by the derived class.
	 * 
	 * @param request The HTTP request being served.
	 * @return The new form instance.
	 * @throws Exception
	 */
	abstract public Object getNewForm(HttpServletRequest request) throws Exception;

	/**
	 * Tests if a new form is needed for some reason other than not having a form at all.
	 * 
	 * @param request The HTTP request being served.
	 * @param form The current form.
	 * @return true iff a new form is needed
	 */
	public boolean isNewFormNeeded(HttpServletRequest request, Object form) {
		
		return false;
	}

	/** Get the current form, creating one if there is none
	 * 
	 * @param request The HTTP request being served
	 * @return The current form
	 * @throws Exception
	 */
	public Object getCurrentForm(HttpServletRequest request) throws Exception {
		
        return formBackingObject(request);
	}

	/** Get the name of the session attribute that will record the form instance.
	 * 
	 * @return the name of the session attribute that will record the form instance.
	 */
	public String getFormAttributeName() {
		return formAttributeName;
	}

	/** 
	 * Sets the name of the session attribute that will record the form instance for this controller. Normally
	 * only called by spring based on the formAttributeName parameter clause in the form controller definition.
	 *  
	 * @param formAttributeName The name of the session attribute.
	 */
	public void setFormAttributeName(String formAttributeName) {
		this.formAttributeName = formAttributeName;
	}
}

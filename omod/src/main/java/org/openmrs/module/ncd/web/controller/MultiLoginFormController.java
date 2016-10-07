package org.openmrs.module.ncd.web.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * A SimpleFormController that retains the same form instance across multiple login sessions, per user.
 * 
 * @author erik horstkotte
 */
@SuppressWarnings("deprecation")
public abstract class MultiLoginFormController extends SimpleFormController {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/** The name of the session attribute that will record the form instance. Configured by spring. */
	protected String formAttributeName;

	/** A key to a form instance: (user, formAttributeName) */
	protected static class FormKey {
		
		protected Integer userId;
		protected String formName;
		
		public FormKey(Integer userId, String formName) {
			this.userId = userId;
			this.formName = formName;
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public String getFormName() {
			return formName;
		}

		public void setFormName(String formName) {
			this.formName = formName;
		}

		@Override
		public boolean equals(Object obj) {
			
			if (!(obj instanceof FormKey)) {
				return false;
			}

			FormKey that = (FormKey) obj;
			return (this.getUserId().equals(that.getUserId()) &&
					this.getFormName().equals(that.getFormName()));
		}

		@Override
		public int hashCode() {
			return getUserId().hashCode() + 23 * getFormName().hashCode();
		}
		
		@Override
		public String toString() {
			
			return "FormKey(" +
						"userId=" + userId +
						"formName=" + formName +
				   ")";
		}
	}

	/** The map of form instances by (user, formAttributeName). */
	protected static Map<FormKey, Object> forms = new HashMap<FormKey, Object>();
	
	/**
	 * Gets the form instance for this form controller and session, creating one and remembering it if none
	 * already exists.
	 */
	public Object formBackingObject(HttpServletRequest request) throws Exception { 
        
		User user = Context.getAuthenticatedUser();
		if (user == null) {
			log.debug("no authenticated user!");
			return null;
		}
		else {
			log.debug("authenticated user = " + user.getUsername());
			log.debug("authenticated user id = " + user.getUserId());
			log.debug("formAttributeName = " + formAttributeName);
			
			FormKey key = new FormKey(user.getUserId(), formAttributeName);
			
			Object form = getForm(key);

			log.debug("form = " + form);

	        if (form == null || isNewFormNeeded(request, form)) {
	        	log.debug("new form.");
	        	form = getNewForm(request);
	        	setForm(key, form);
	        	log.debug("forms=" + Arrays.asList(forms.entrySet().toArray()));
	        }
	        else {
	        	log.debug("keep existing form.");
	        }
	        return form;
		}
    }

	/** HashMap isn't thread-safe, so wrap it in a synchronized method */
	private synchronized Object getForm(FormKey key) {
		return forms.get(key);
	}

	/** HashMap isn't thread-safe, so wrap it in a synchronized method */
	private synchronized void setForm(FormKey key, Object form) {
		forms.put(key, form);
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

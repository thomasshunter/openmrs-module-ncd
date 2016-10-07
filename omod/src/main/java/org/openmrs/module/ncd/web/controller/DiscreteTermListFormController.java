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
import org.openmrs.module.ncd.database.NlpDiscreteTerm;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

@SuppressWarnings("deprecation")
public class DiscreteTermListFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    static class Replacement {

    	private String from;
    	private String to;
    	
    	public Replacement(String from, String to) {
    		
    		this.from = from;
    		this.to = to;
    	}

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public String getTo() {
			return to;
		}

		public void setTo(String to) {
			this.to = to;
		}
    }
    
    private static final Replacement[] replacements = { 
    	new Replacement("-", " - "), 
    	new Replacement("<", " < "),
    	new Replacement(">", " > "),
    	// must be last
    	new Replacement("  ", " "),
    };
    
    public class PageForm {
    	
        private String newTerm;
        private boolean newTermNegative;
        private List<NlpDiscreteTerm> terms;
        private List<String> selectedTerms;
        
        public PageForm() {
            
            this.newTerm = "";
            this.newTermNegative = false;
            this.terms = new ArrayList<NlpDiscreteTerm>();
            selectedTerms = new ArrayList<String>();
            refresh();
        }

        public boolean validateAddNewTerm(BindException exceptions) {

        	// The new term text cannot be blank
        	if (!isSet(newTerm)) {

        		exceptions.rejectValue("newTerm", "ncd.error.required");
        		return false;
        	}

        	// The new term text cannot duplicate an existing term
        	boolean duplicate = false;
        	String standardizedNewTerm = standardizeNewTerm();
        	for (NlpDiscreteTerm term : terms) {
        		if (standardizedNewTerm.equals(term.getTerm())) {
        			duplicate = true;
        			break;
        		}
        	}
        	if (duplicate) {
        		
        		exceptions.rejectValue("newTerm", "ncd.pages.discreteTermList.error.duplicate");
        		return false;
        	}
        	
        	// Must be ok to add it, then
        	return true;
        }

        // Performs some standard conversions on the entered term text.
        private String standardizeNewTerm() {

        	String fixed = " " + newTerm + " ";
        	for (Replacement r : replacements) {
        		
        		fixed = fixed.replaceAll(r.getFrom(), r.getTo());
        	}
        	
        	return fixed;
        }

        public void addNewTerm() {
        	
        	NlpDiscreteTerm term = new NlpDiscreteTerm(standardizeNewTerm(), newTermNegative);
        	NCDUtilities.getService().saveNlpDiscreteTerm(term);
        }

        public void deleteSelectedTerms() {

        	if (selectedTerms != null) {
	        	for (NlpDiscreteTerm thisTerm : terms) {
	        		if (selectedTerms.contains(thisTerm.getTerm())) {
	        			NCDUtilities.getService().deleteNlpDiscreteTerm(thisTerm);
	        		}
	        	}
        	}
        }
        
        public void refresh() {

        	terms = NCDUtilities.getService().getAllNlpDiscreteTerms();

        	List<String> allTerms = new ArrayList<String>();
        	for (NlpDiscreteTerm thisTerm : terms) {
        		allTerms.add(thisTerm.getTerm());
        	}
        	
        	List<String> newSelectedTerms = new ArrayList<String>();
        	if (this.selectedTerms != null) {
	        	for (String selectedTerm : this.selectedTerms) {
	        		if (allTerms.contains(selectedTerm)) {
	        			newSelectedTerms.add(selectedTerm);
	        		}
	        	}
        	}
        	this.selectedTerms = newSelectedTerms;
        }
        
        public void selectAll() {

        	this.selectedTerms = new ArrayList<String>();
        	for (NlpDiscreteTerm thisTerm : terms) {
        		this.selectedTerms.add(thisTerm.getTerm());
        	}
        }
        
        public void selectNone() {
        	
        	this.selectedTerms = new ArrayList<String>();
        }
        
		public String getNewTerm() {
			return newTerm;
		}

		public void setNewTerm(String newTerm) {
			this.newTerm = newTerm;
		}

		public boolean isNewTermNegative() {
			return newTermNegative;
		}

		public void setNewTermNegative(boolean newTermNegative) {
			this.newTermNegative = newTermNegative;
		}

		public List<NlpDiscreteTerm> getTerms() {
			return terms;
		}

		public void setTerms(List<NlpDiscreteTerm> terms) {
			this.terms = terms;
		}

		public List<String> getSelectedTerms() {
			return selectedTerms;
		}

		public void setSelectedTerms(List<String> selectedTerms) {
			this.selectedTerms = selectedTerms;
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
        return refData;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException exceptions) throws Exception {		

        HttpSession httpSession = request.getSession();
        MessageSourceAccessor msa = getMessageSourceAccessor();
        PageForm theForm = (PageForm) object;
        
        String action = request.getParameter("action");
        if (action == null) {
        	action = "";
        }
        
        if (action.equals(msa.getMessage("ncd.pages.discreteTermList.add"))) {

        	// If the new term parameters are valid
        	if (theForm.validateAddNewTerm(exceptions)) {
        		
        		// Add the new term
        		theForm.addNewTerm();

                // Clear any old error status
                httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
                
                // Update the list
                theForm.refresh();

                // Update the status
            	updateStatus(request.getSession(), theForm);
            }
            else {

                // Set the top of page error
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ncd.pages.discreteTermList.add.error");
            }
            
            return showForm(request, response, exceptions);
        }
        else if (action.equals(msa.getMessage("ncd.pages.discreteTermList.delete"))) {
            
        	// Delete the selected terms
        	theForm.deleteSelectedTerms();

            // Clear any old error status
            httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);

            // Update the list
            theForm.refresh();

            // Update the status
        	updateStatus(request.getSession(), theForm);
        	
            return showForm(request, response, exceptions);
        }
        else {
            
            // How did you get here?
            log.error("impossible: no action in request");
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
        
    	PageForm theForm = new PageForm();
    	updateStatus(request.getSession(), theForm);
    	return theForm;
    }
    
    private void updateStatus(HttpSession session, PageForm theForm) {
    	
    	session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ncd.pages.discreteTermList.banner");
        Long[] counts = new Long[1];
        counts[0] = new Long(theForm.getTerms().size());
        session.setAttribute(WebConstants.OPENMRS_MSG_ARGS, counts);
    }
    
    private boolean isSet(String s) {
        
        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }
}

package org.openmrs.module.ncd.web.controller;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.cache.NlpCriticConceptCache;
import org.openmrs.module.ncd.database.NlpCriticConcept;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

@SuppressWarnings("deprecation")
public class ConceptImportFormController extends SimpleFormController {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    public static class PageForm {

	    private MultipartFile file;

	    public void setFile(MultipartFile file) {
	        this.file = file;
	    }

	    public MultipartFile getFile() {
	        return file;
	    }
	}
    
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {

        // Get the form
    	PageForm bean = (PageForm) command;

        // If a file was uploaded
        MultipartFile file = bean.getFile();
        if (file != null) {

        	// Load export objects from the file
        	XMLDecoder d = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(file.getBytes())));
        	ExportedConcept[] importedConcepts = (ExportedConcept[]) d.readObject();
        	d.close();
        	log.debug("importedConcepts=" + importedConcepts);

        	// Convert export objects to real objects and insert or update the real objects
        	for (ExportedConcept concept : importedConcepts) {
        		
        		// Convert back to a real object
        		NlpCriticConcept realConcept = concept.toNlpCriticConcept();
        		
        		// Insert or update the object
        		NCDUtilities.getService().saveNlpCriticConcept(realConcept);
        	}
        	
        	// Clear the concept cache
        	NlpCriticConceptCache.flush();
        	
        	log.debug("concept import done.");
        }
        
        return new ModelAndView(new RedirectView(getSuccessView()));
    }

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		
		return new PageForm();
	}
}

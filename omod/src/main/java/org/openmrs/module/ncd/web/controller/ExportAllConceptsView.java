package org.openmrs.module.ncd.web.controller;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.NlpCriticConcept;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.springframework.web.servlet.view.AbstractView;

/**
 * A Spring web UI framework view class to export all NlpCriticConcepts as an XML file.
 */
public class ExportAllConceptsView extends AbstractView {
	
    /** Log for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    public ExportAllConceptsView() {

        setContentType("text/xml");
    }
    
    protected boolean generatesDownloadContent() {

        return true;
    }
    
    @Override
    protected void renderMergedOutputModel(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        buildResponse(response, getContent(request));
    }
    
    protected String getContent(HttpServletRequest request) {
    	
    	String content = null;
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	XMLEncoder e = new XMLEncoder(new BufferedOutputStream(baos));
        try {
            
        	// Get all concepts
        	List<NlpCriticConcept> allConcepts = NCDUtilities.getService().listNlpCriticConcepts();
        	
        	// Convert the objects to export objects
        	ExportedConcept[] exportedConcepts = new ExportedConcept[allConcepts.size()];
        	int index = 0;
        	for (NlpCriticConcept concept : allConcepts) {
        		exportedConcepts[index++] = ExportedConcept.fromNlpCriticConcept(concept);
        	}
        	
        	// Write the export objects to the file
        	e.writeObject(exportedConcepts);
        	e.flush();
        	content = baos.toString();
        }
        catch (Exception ex) {
            
            log.error("Exception: " + ex.getMessage(), ex);
        }
        finally {
        	e.close();
        }
        
        return content;
    }
    
    protected void buildResponse(HttpServletResponse response, String content) {
        
        try {
            
            // Build the response.
            response.setContentType(getContentType());
            response.setHeader("Content-Disposition","attachment; filename=\"concepts.xml\"");
            response.setHeader("cache-control", "must-revalidate");
            response.getWriter().write(content);
            response.flushBuffer();
        }
        catch (Exception ex) {
            
            log.debug("Exception: " + ex.getMessage(), ex);
        }
    }
}

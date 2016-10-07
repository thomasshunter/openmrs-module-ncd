/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.AlertType;
import org.openmrs.module.ncd.model.Zvx;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.ValidationContext;
import ca.uhn.hl7v2.validation.impl.NoValidation;


// import org.openmrs.module.ncd.parser.Segment;

/**
 * This class works to find HL7 segments which will be examined for
 * reportable conditions.
 * 
 * @author John Brown
 * 
 */
public class CandidateResultFinder implements ICandidateResultFinder
{
    private static Log logger = LogFactory.getLog(CandidateResultFinder.class);        
    
    private final static Parser hl7Parser;
    private final static DefaultXMLParser xmlParser;    
    private final static AlertType parseAlert;
    
    static
    {
        hl7Parser = new GenericParser();
        ValidationContext validator = new NoValidation();
        hl7Parser.setValidationContext(validator);
        xmlParser = new DefaultXMLParser();
        parseAlert = NCDUtilities.getService().findAlertTypeById(ConditionDetectorService.alertTypeParseError);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmrs.module.ncd.ICandidateResultFinder#findCandidateResults(java.lang.String)
     */
    public List<Node> findCandidateResults(String message) throws CandidateResultFinderException
    {        
        logger.debug("Entering findCandidateResults.");
        List<Node> candidateSegments = new ArrayList<Node>();

        try
        {
        	if (logger.isDebugEnabled()) 
        	{
        		logger.debug("message=\n" + message);
        	}
            
            logger.debug("Parsing message...");            
            Message msg = hl7Parser.parse(message);
            
            logger.debug("Extracting candidate results...");        
            Document xmlDoc = xmlParser.encodeDocument(msg);
            
        	if (logger.isDebugEnabled()) 
        	{
        		logger.debug("parsed message=" + XmlUtilities.toString(xmlDoc));
        	}
            
            List<String> candidateSegmentNames = NCDUtilities.getCandidateSegmentNames();
            if (candidateSegmentNames == null || candidateSegmentNames.size() == 0)
            {
                candidateSegmentNames = loadDefaultSegmentNames();
            }
            for (String segmentName : candidateSegmentNames)
            {
                extractAndAddSpecifiedSegment(candidateSegments, xmlDoc, segmentName);
            }
            
            return candidateSegments;
        }
        catch(Exception e)
        {
            logger.debug("Error extracting candidate results.");
            logger.debug("\tDetails: " + e.getMessage());
            addParseAlertSummary(e, message);
            throw new CandidateResultFinderException(e);
        }
        finally
        {
            logger.debug("Leaving findCandidateResults.");
        }
    }

    /**
     * Extracts all segments from the passed in document and add them to the passed in list of nodes.
     * 
     * @param candidateSegments The list of nodes to which the segment nods will be added.
     * @param xmlDoc The document from which the segment nodes will be extracted.
     * @param segmentName The name of the segment to extract.
     */
    private void extractAndAddSpecifiedSegment(List<Node> candidateSegments, Document xmlDoc, String segmentName)
    {
        NodeList segmentNodeList = xmlDoc.getElementsByTagName(segmentName);            
        int nodesAdded = 0;
        for(int nodeIdx = 0; nodeIdx < segmentNodeList.getLength(); nodeIdx++)
        {
            Node obxNode = segmentNodeList.item(nodeIdx);
            // we don't want an empty OBX node
            if (obxNode.hasChildNodes())
            {
                candidateSegments.add(obxNode);
                nodesAdded++;
            }
        }
        logger.debug("Found " + nodesAdded + " " + segmentName + " candidate results.");
    }
    
    private List<String> loadDefaultSegmentNames()
    {
        ArrayList<String> defaultSegmentNames = new ArrayList<String>();
        defaultSegmentNames.add("OBX");
        defaultSegmentNames.add("DG1");
        return defaultSegmentNames;
    }
    
    private void addParseAlertSummary(Exception e, String message) {
    	AlertSummary alertSummary = new AlertSummary();
    	Date d = new Date();
    	alertSummary.setFirstDate(d);
    	alertSummary.setLastDate(d);
    	alertSummary.setOccurrences(1);
    	alertSummary.setAlertType(parseAlert);
    	    	
    	String mpqNum = Zvx.getMpq(message);
    	
    	alertSummary.setSummary("Error parsing message with MPQ #" + mpqNum);
    	alertSummary.setDetails(e.getMessage());    	
    	alertSummary.setIdentity(ConditionDetectorService.alertIdentityCandidateResult + "[" + mpqNum + "]");
    	NCDUtilities.getService().addAlertSummary(alertSummary);
    }
}

/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.storage;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.critic.ReportResult;
import org.openmrs.module.ncd.database.Institution;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.model.IResultSegment;
import org.openmrs.module.ncd.model.MessageHeader;
import org.openmrs.module.ncd.model.Provider;
import org.openmrs.module.ncd.model.ResultSegmentFactory;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * Class to work with the institition.
 * 
 * @author jlbrown
 *
 */
public class InstitutionStorageHelper
{
	private static Log log = LogFactory.getLog(InstitutionStorageHelper.class);
	
    /**
     * Retrieve the institution based on the MSH segment.
     * @param msgSegment The result segment under examination.
     * @param currentResult The object that persists the facts already discovered.
     * @return The institution DB row.
     */
    public static Institution retrieveInstitution(Node msgSegment, ReportResult currentResult)
    {   
    	Institution institute = null;    
    	
    	try 
    	{
	        IResultSegment resultSegment   = ResultSegmentFactory.getResultSegment(msgSegment);	        
	        Provider pv1                   = resultSegment.getProvider();
	        String applicationName         = MessageHeader.getSendingApplication(currentResult.getMessage());        
	        String facilityName            = MessageHeader.getSendingFacility(currentResult.getMessage());
	        String locationName            = pv1.getSendingLocation();
	
	        InstitutionStorageHelper.log.debug("applicationName=" + applicationName);
	        InstitutionStorageHelper.log.debug("facilityName=" + facilityName);
	        InstitutionStorageHelper.log.debug("locationName=" + locationName);
	        
	        HL7Producer appFac             = retrieveHL7Producer(applicationName, facilityName, locationName);        
	        
	        if (appFac != null)
	        {
	            InstitutionStorageHelper.log.debug("producer=" + appFac);
	            InstitutionStorageHelper.log.debug("institution=" + appFac.getInstitution());
		        
	            currentResult.setApplicationFacility(appFac);
	            currentResult.setInstitution(appFac.getInstitution());
	            institute = currentResult.getInstitution();
	        }
    	} 
    	catch (Exception e) 
    	{
    		// nothing we can do except log the exception and move on.
    		InstitutionStorageHelper.log.debug( "InstitutionStorageHelper.retrieveInstitution() encountered an Error retrieving institution.  Reason - " + e.getMessage());
    	}
        
    	return institute;
    }        
    
    public static HL7Producer retrieveHL7Producer(String message) 
    {
    	String sendingApp = MessageHeader.getSendingApplication(message);
    	String sendingFac = MessageHeader.getSendingFacility(message);
    	String sendingLoc = Provider.getSendingLocation(message);
    
    	return retrieveHL7Producer(sendingApp, sendingFac, sendingLoc);
    }
    
    public static HL7Producer retrieveHL7Producer(String app, String fac, String loc) 
    {
    	ConditionDetectorService cds = NCDUtilities.getService();
    	HL7Producer theProducer = cds.getProducer(app, fac, loc);    	
    
    	return theProducer;
    }
    
    // Stores the HL7Producer and Institution in the ReportResult object and returns it.
    public static ReportResult storeAppFacLocAndInstitution(List<Node> msgSegments, ReportResult reportResult) 
    {
		ReportResult newResult    = new ReportResult(reportResult);
		Node firstSegment         = msgSegments.get(0);
		HL7Producer hl7Producer   = InstitutionStorageHelper.retrieveHL7Producer(reportResult.getMessage());
		Institution institution   = InstitutionStorageHelper.retrieveInstitution(firstSegment, newResult);
	
		newResult.setApplicationFacility(hl7Producer);
		newResult.setInstitution(institution);
	
		return newResult;
	}
}

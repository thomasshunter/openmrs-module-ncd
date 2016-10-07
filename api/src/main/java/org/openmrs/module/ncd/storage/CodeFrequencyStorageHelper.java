/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.storage;

import java.util.Date;
import java.util.List;

import org.openmrs.module.ncd.critic.ReportResult;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.model.MessageHeader;
import org.openmrs.module.ncd.model.Observation;
import org.openmrs.module.ncd.model.Provider;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;


/**
 * This class pulls together the test date, institution, and loinc code.
 * The call is then made to increment the loinc frequency based on that
 * information.
 * 
 * @author jlbrown
 *
 */
public class CodeFrequencyStorageHelper
{
    /**
     * Helper method to increment the frequency of a loinc code
     * by date and institution.
     * @param msgSegments The message segments under consideration.
     * @param currentResult The current ReportResult.
     * @throws CodeFrequencyStorageException
     */
    @Transactional(rollbackFor = Exception.class)
    public static void incrementCodeFrequency(List<Node> msgSegments, ReportResult currentResult) throws CodeFrequencyStorageException
    {
    	String message     = currentResult.getMessage();
        String application = MessageHeader.getSendingApplication(message);
        
        if (application == null) 
        {
    		application = "";
    	}
        
        String facility = MessageHeader.getSendingFacility(message);
    	
        if (facility == null) 
        {
    		facility = "";
    	}	                               
        String location = Provider.getSendingLocation(message);
        
        if (location == null) 
        {
        	location = "";
        }
        
        HL7Producer producer = InstitutionStorageHelper.retrieveHL7Producer(application, facility, location);
        
        // If the HL7 producer is ignored, we do not collect LOINC frequency data from the message.
        if (producer != null && ! producer.isIgnored() ) 
        {        
	     	for (Node msgSegment : msgSegments) 
	     	{
	    		if (! msgSegment.getNodeName().equals("OBX")) 
	    		{
	    			continue;
	    		}
	    		
		        Observation obx = new Observation(msgSegment);
		        String patientZipCode = obx.getPatientInfo().getZip();
		        
		        if (patientZipCode == null)
		        {
		            patientZipCode = "";
		        }
		        
		        String instituteZipCode = obx.getProvider().getProviderZip();
		        
		        if (instituteZipCode == null)
		        {
		            instituteZipCode = "";
		        }
		        
		        String doctorZipCode = obx.getOrderCommon().getOrderingZip();
		        
		        if (doctorZipCode == null)
		        {
		            doctorZipCode = "";
		        }
		                
		        String loincCode = null;        
		        
		        try
		        {
		        	loincCode = obx.getLoincCode();
		            
		            // If we have a loinc code expression, collate the data and increment
		            // the frequency.
		            // If we do not have a loinc code expression, we have nothing to
		            // increment and can exit.
		            if( loincCode != null )
		            {
		            	
		                Date testDate = null;
		        
		                if (obx != null && obx.getOrderObservation() != null) 
		                {
		                	testDate = obx.getOrderObservation().getTestDate();
		                }
		                
		                if( testDate != null )
		                {
		                    NCDUtilities.getService().incrementCodeFrequency(
		                            testDate, application, facility, location,
		                            loincCode, "LN", patientZipCode,
		                            instituteZipCode, doctorZipCode);
		                }	
		            }
		        }
		        catch (CodeFrequencyStorageException e)
		        {
		            throw e;
		        }
		        catch (Exception e)
		        {
		            throw new CodeFrequencyStorageException(e);
		        }
	    	}
        }
    }
}

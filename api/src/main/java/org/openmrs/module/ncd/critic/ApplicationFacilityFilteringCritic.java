/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.critic;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.storage.InstitutionStorageHelper;
import org.w3c.dom.Node;

/**
 * A message critic which filters messages based on the application/facility. If
 * the message is not in the database all results in the message will be given a
 * DO_NOT_REPORT determination.
 * 
 * @author jlbrown
 * 
 */
public class ApplicationFacilityFilteringCritic extends MessageCritic
{
    private static Log logger = LogFactory
            .getLog(ApplicationFacilityFilteringCritic.class);
    
    private static final Long criticId;
    
    static {
    	criticId = ConditionDetectorService.appFacFilteringCritic;
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.critic.IResultsProcessor#doesApply(java.util.List)
     */
    public boolean doesApply(List<Node> msgSegments)
            throws ResultCriticException
    {
        // This is a message critic, so it is always applicable.
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.critic.IResultsProcessor#shouldReport(java.util.List, org.openmrs.module.ncd.critic.ReportResult)
     */
    public ReportResult shouldReport(List<Node> msgSegments, ReportResult currentResult) throws ResultCriticException
    {
        // this should happen before candidate segments have been extracted, so the msgSegments parameter should be null.
        assert(msgSegments == null || msgSegments.isEmpty()); 
        logger.debug("Entering shouldReport");
        
        ReportResult result = new ReportResult(currentResult);
        
        // set the default result status to UNKNOWN
        result.setReportResultStatus(ReportResult.ReportResultStatus.UNKNOWN);        
        HL7Producer producer = InstitutionStorageHelper.retrieveHL7Producer(currentResult.getMessage());
        
        if( producer != null && producer.isIgnored() )
        {
            result.setReportResultStatus(ReportResult.ReportResultStatus.DO_NOT_REPORT);
            result.setIndicatingCriticId(criticId);
            result.setReasonForStatus("Made a DO_NOT_REPORT determination due to the application/facility.");
            
            logger.debug("Made a DO_NOT_REPORT determination due to the application/facility.");                
        }        
        else if (producer == null)
        {
            logger.info("No HL7Producer record found for message application, facility and location.");
        }
        
        logger.debug("Leaving shouldReport");
        
        return result;
    }    
}

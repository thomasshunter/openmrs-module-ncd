/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.critic;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;


/**
 * This message critic excludes HL7 messages that don't fall with a certain set
 * of processing identifiers. This set of processing identifiers is
 * configurable.
 * 
 * @author jlbrown
 * 
 */
public class ProcessingIdCritic extends MessageCritic
{
    private static Log logger = LogFactory.getLog(ProcessingIdCritic.class);        
    private static final Long criticId;
    
    static
    {
        criticId = ConditionDetectorService.processingIdCritic;        
    }

    /**
     * @see org.openmrs.module.ncd.critic.IResultsCritic#shouldReport(java.util.List<org.w3c.dom.Node>,
     *      org.openmrs.module.ncd.critic.ReportResult)
     */
    public ReportResult shouldReport(List<Node> resultNodes, ReportResult result)
            throws ResultCriticException
    {
        logger.debug("entering shouldReport");
        
        if( resultNodes == null || resultNodes.isEmpty() )
        {
            throw new ResultCriticException(
                    "A null or empty list of segment nodes was passed into shouldReport.");
        }
        
        try
        {                    	
        	String processingId = getMessageHeader(resultNodes.get(0)).getMessageProcessingId();        	
            
            List<String> allowedProcessingIds = NCDUtilities.getAllowedProcessingIds();
            
            if (allowedProcessingIds != null && ! allowedProcessingIds.contains(processingId))                
            {
                result.setReportResultStatus(ReportResult.ReportResultStatus.DO_NOT_REPORT);
                result.setIndicatingCriticId(criticId);
                logger.debug("Made a DO_NOT_REPORT determination due to an invalid processing ID.");
            }
            else
            {
                result.setReportResultStatus(ReportResult.ReportResultStatus.UNKNOWN);
            }
            
            logger.debug("leaving shouldReport");
            return result;
        }
        catch (Throwable e)
        {
            throw new ResultCriticException(e);
        }        
    }
}

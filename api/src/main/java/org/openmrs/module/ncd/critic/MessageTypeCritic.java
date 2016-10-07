/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.critic;


import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;


/**
 * This message critic excludes a message if it is not
 * an HL7 type that produces reportable conditions.
 * 
 * @author jlbrown
 * 
 */
public class MessageTypeCritic extends MessageCritic
{
    private static Log logger = LogFactory.getLog(MessageTypeCritic.class);        
    private static final Long criticId;
    
    static
    {
    	criticId = ConditionDetectorService.messageTypeCritic;
    }

    /**
     * @see org.openmrs.module.ncd.critic.IResultsCritic#shouldReport(java.util.List<org.w3c.dom.Node>,
     *      org.openmrs.module.ncd.critic.ReportResult)
     */
    public ReportResult shouldReport(List<Node> resultNodes, ReportResult result) throws ResultCriticException
    {
        logger.debug("entering shouldReport");
        
        if( resultNodes == null || resultNodes.isEmpty() )
        {
            throw new ResultCriticException("A null or empty list of segment nodes was passed into shouldReport.");
        }
        
        try
        {                    	
            String msgType                  = getMessageHeader(resultNodes.get(0)).getMessageType();         
            List<String> allowableMsgTypes  = NCDUtilities.getAllowableHL7MessageTypes();
            
            if( allowableMsgTypes == null )
            {
                result.setReportResultStatus( ReportResult.ReportResultStatus.UNKNOWN );
            }
            else
            {          
                Iterator<String> allowableMsgTypesIt    = allowableMsgTypes.iterator();
                boolean containsType                    = false;
                
                while( allowableMsgTypesIt.hasNext() )
                {
                    String anAllowableMsgType = allowableMsgTypesIt.next();
                    
                    if( anAllowableMsgType.equals( msgType ) )
                    {
                        containsType = true;
                        break;
                    }
                }
                
                if( !containsType )                
                {
                    result.setReportResultStatus(ReportResult.ReportResultStatus.DO_NOT_REPORT);
                    result.setIndicatingCriticId(criticId);
                    logger.debug("Made a DO_NOT_REPORT determination due to the message type.");
                }
                else    
                {
                    result.setReportResultStatus(ReportResult.ReportResultStatus.UNKNOWN);
                }
            }
            
            return result;
        }
        catch (Throwable e)
        {
            throw new ResultCriticException(e);
        }
        finally
        {
            logger.debug("leaving shouldReport");
        }       
    }
}

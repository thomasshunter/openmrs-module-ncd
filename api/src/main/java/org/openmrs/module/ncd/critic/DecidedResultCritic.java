/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.critic;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.critic.ReportResult.ReportResultStatus;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.storage.DecidedResultStorageHelper;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * This critic uses the previously determined results to see if we've already
 * seen a message with the particular data before.  If so, the previous
 * result is applies to this result.
 * @author jlbrown
 *
 */
public class DecidedResultCritic implements IResultsCritic
{
    private static Log logger = LogFactory.getLog(DecidedResultCritic.class);
    private static final Long criticId;
    
    static 
    {
    	criticId = ConditionDetectorService.decidedResultsCritic;
    }
    
    public boolean doesApply(List<Node> msgSegments) throws ResultCriticException
    {
        if (msgSegments == null || msgSegments.isEmpty())
        {
            throw new ResultCriticException( "A null or empty list of segment nodes was passed into doesApply.");
        }
        
        Node firstNode  = msgSegments.get(0);
        String nodeName = firstNode.getNodeName();
        
        return (nodeName.equals("OBX") ? true : false);
    }

    public ReportResult shouldReport(List<Node> msgSegments, ReportResult currentResult) throws ResultCriticException
    {
        logger.debug("Entering shouldReport");
        ReportResult result = new ReportResult(currentResult);
        
        try
        {            
            DecidedResult decidedResultTemplate                     = DecidedResultStorageHelper.createDecidedResultTemplate(msgSegments);
            List<DecidedResult> decidedResults                      = NCDUtilities.getService().findDecidedResults(decidedResultTemplate);
            ReportResult.ReportResultStatus decidedResultStatus     = ReportResult.ReportResultStatus.UNKNOWN;
            Iterator<DecidedResult> iter                            = decidedResults.iterator();
            
            while (iter.hasNext())
            {
                DecidedResult curDecidedResult  = iter.next();
                String reportable               = curDecidedResult.getReportable();
                
                if (reportable != null)
                {                       
                    ReportResult.ReportResultStatus curResultStatus = ReportResult.ReportResultStatus.textToStatus(reportable);
                    
                    if (curResultStatus == ReportResult.ReportResultStatus.REPORT) 
                    {
                        String conditionName    = curDecidedResult.getConditionName();
                        Condition condition     = NCDUtilities.getService().findConditionByName(conditionName);
                        result.addCondition(condition, msgSegments, criticId, curDecidedResult); 
                        result.setIndicatingCriticId(criticId);
                    }
                    
                    decidedResultStatus = ReportResultStatus.joinResultStatus(curResultStatus, decidedResultStatus);                                                            
                }
            }
            
            result.setReportResultStatus(decidedResultStatus);
            
            if (decidedResultStatus == ReportResult.ReportResultStatus.DO_NOT_REPORT || decidedResultStatus == ReportResult.ReportResultStatus.REPORT  )
            {
                String reason = "Made a " + decidedResultStatus + " determination due to a previously decided result.";
                result.setReasonForStatus(reason);
                logger.debug(reason);
            }
        }
        catch (Exception e)
        {
            throw new ResultCriticException(e);
        }
        
        logger.debug("Leaving shouldReport");
        return result;
    }

    public boolean isDecidedResultCritic() 
    {
        return true;
    }
}

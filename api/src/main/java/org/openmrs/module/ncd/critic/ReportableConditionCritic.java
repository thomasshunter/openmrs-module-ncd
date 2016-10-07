/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.critic;


import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.Condition;
import org.w3c.dom.Node;


/**
 * This segment critic determines if the candidate segement Node contains a
 * reportable condtion based on the code and observation value. Any condition
 * indicators that would trigger a reportable condition are loaded based on the
 * code and then the condition indicators are compared to the observation value.
 * If a match is found, a determination is made based on the particular condition
 * indicator, which is usually REPORT.
 * 
 * NOTE: [eah] Well, not really. This is actually the DG1 critic.
 * 
 * @author John Brown
 * 
 */
public class ReportableConditionCritic implements IResultsCritic
{
    private static Log logger = LogFactory.getLog(ReportableConditionCritic.class);
    
    private static final Long criticId;
    
    static 
    {
    	criticId = ConditionDetectorService.reportableConditionCritic;
    }
    
    /**
     * @see org.openmrs.module.ncd.critic.IResultsCritic#shouldReport(java.util.List<org.w3c.dom.Node>, 
     * 																  org.openmrs.module.ncd.critic.ReportResult)
     */
    public ReportResult shouldReport(List<Node> msgSegments, ReportResult currentResult) throws ResultCriticException
    {
        logger.debug("Entering shouldReport.");
        
        if( msgSegments == null || msgSegments.isEmpty() )
        {
            throw new ResultCriticException(  "A null or empty list of segment nodes was passed into shouldReport.");
        }        
        
        ReportResult result = new ReportResult(currentResult);
        //set to the default
        result.setReportResultStatus(ReportResult.ReportResultStatus.UNKNOWN);  
        
        try
        {                                                                     
            Set<CodeCondition> conditionsFound = ConditionExtractor.determineConditions(msgSegments);
           
//---------------diagnostic start
Iterator<CodeCondition> conditionsFoundIt   = conditionsFound.iterator();
while( conditionsFoundIt.hasNext() )
{
    CodeCondition aCond = conditionsFoundIt.next();
    
    ReportableConditionCritic.logger.info( "aCondition=" + aCond );
}
            
//---------------diagnostic end
            
            
            if( conditionsFound.size() == 0 )
            {
                result.setReportResultStatus(ReportResult.ReportResultStatus.UNKNOWN);
                logger.debug("No condition found.");
            }
            else
            {
                for (CodeCondition codeCondition : conditionsFound)
                {
                	Condition condition = codeCondition.getCondition();
       
                	if (condition.isReportable() )
                    {
                        result.setReportResultStatus(ReportResult.ReportResultStatus.REPORT);  
                        result.setReasonForStatus("ReportableConditionCritic found a reportable condition in this candidate.");
                        StringBuilder loggerMsg = new StringBuilder();
                        loggerMsg.append("Made a ");
                        loggerMsg.append(result.getReportResultStatus().toString());
                        loggerMsg.append(" determination due to condition found - ");
                        loggerMsg.append(condition.getDisplayText());
                        logger.debug(loggerMsg.toString());
                        result.addCondition(condition, msgSegments, criticId);
                        result.setIndicatingCriticId(criticId);
                        result.setLoincCode(codeCondition);                                
                    }                        
                }
            }                  
        }
        catch (Throwable e)
        {
            throw new ResultCriticException(e);
        }
        
        logger.debug("Leaving shouldReport.");
        return result;
    }

    /**
     *  This message critic applies to specific segments such as OBX or DG1.
     *  
     * @see org.openmrs.module.ncd.critic.IResultsCritic#doesApply(java.util.List<org.w3c.dom.Node>)
     */
    public boolean doesApply(List<Node> msgSegments)
            throws ResultCriticException
    {
        if (msgSegments == null || msgSegments.isEmpty())
        {
            throw new ResultCriticException(
                    "A null or empty list of segment nodes was passed into doesApply.");
        }
        Node firstNode = msgSegments.get(0);
        String nodeName = firstNode.getNodeName();
        return (nodeName.equals("DG1") ? true : false);        
    }

    public boolean isDecidedResultCritic() {
        return false;
    }
}

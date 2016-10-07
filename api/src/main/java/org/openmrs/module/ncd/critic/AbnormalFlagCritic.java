/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.critic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.model.Observation;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * This critic does processing on the abnormal flag in an OBX result
 * and uses that flag to determine if the result is reportable,
 * not reportable, or unknown.
 * 
 * @author jlbrown
 *
 */
public class AbnormalFlagCritic extends ObxCritic
{
    private static Log logger = LogFactory.getLog(AbnormalFlagCritic.class);    
    private static ArrayList<Character> obx11Values;
    private final static long criticId; 
    
    static {    	
    	obx11Values = new ArrayList<Character>();
    	obx11Values.add('F');
    	obx11Values.add('f');
    	obx11Values.add('C');
    	obx11Values.add('c'); 
    	criticId = ConditionDetectorService.abnormalFlagCritic;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.critic.IResultsProcessor#shouldReport(java.util.List, org.openmrs.module.ncd.critic.ReportResult)
     */
    public ReportResult shouldReport(List<Node> msgSegments,
            ReportResult currentResult) throws ResultCriticException
    {
        logger.debug("Entering shouldReport");
        ReportResult result = new ReportResult(currentResult);
        result.setReportResultStatus(ReportResult.ReportResultStatus.UNKNOWN);
        try
        {
        	List<String> reportableFlags = NCDUtilities.getReportableAbnormalFlags();
        	List<String> notReportableFlags = NCDUtilities.getNotReportableAbnormalFlags();
            for( Node msgSegment : msgSegments )
            {
                Observation obx = new Observation(msgSegment); 
                
                // First, check the OBX-11 for F, C, or null.
                Character obx11 = obx.getTestResultStatus();
                if (obx11 != null && ! obx11Values.contains(obx11)) {
                	// if we don't have a valid OBX-11, skip to the next segment.
                	continue;
                }
                
                // Check for not-reportable flags.
                String abnormalFlag = obx.getAbnormalFlag();
                
                if (notReportableFlags != null && notReportableFlags.contains(abnormalFlag)) {
                	// if we have a not-reportable flag, skip to the next segment
                	continue;
                }
                
                // Now check the reportable abnormal flags.
                                               
                // Uses the list of reportable flags from configuration to determine 
                // if the result is reportable.
                if( reportableFlags != null && reportableFlags.contains(abnormalFlag) )
                {                	                    
                    Set<Condition> conditionSet = getReportableConditionSetForSegment(msgSegment);
                    if (conditionSet.size() == 1) {
                		// If the code in this segment maps to zero non-reportable conditions and
                		// one reportable condition, we have a positive.
                    	Condition conditionFound = null;                    	
                   		// We only have one condition in the reportable condition set, we can 
                   		// specify the condition; otherwise, we'll specify "Unknown" as the
                   		// condition (aka null).
                   		conditionFound = conditionSet.iterator().next();
                		result = addAbnormalFlagReportableCondition(msgSegments, result, conditionFound);                    	
                    }
                }
            }            
            
            logger.debug("Leaving shouldReport");
            return result;            
        }
        catch (Exception e)
        {
            throw new ResultCriticException(e);
        }        
    }

	private ReportResult addAbnormalFlagReportableCondition(List<Node> msgSegments,
			ReportResult result, Condition conditionFound) {
		// If we have one and only one condition, we can set the result status to REPORT.
		result.setReportResultStatus(ReportResult.ReportResultStatus.REPORT);                                        
		result.setReasonForStatus("Made a REPORT determination due to abnormal flag.");  
		
		// We only have one condition in the condition set, so add that condition;
		
		result.addCondition(conditionFound, msgSegments, criticId);     
		result.setIndicatingCriticId(criticId);
		logger.debug("Made a REPORT determination due to abnormal flag.");
		return result;
	}
}

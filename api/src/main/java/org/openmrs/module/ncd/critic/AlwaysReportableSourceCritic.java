/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */

package org.openmrs.module.ncd.critic;


import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.model.IResultSegment;
import org.openmrs.module.ncd.model.ResultSegmentFactory;
import org.openmrs.module.ncd.storage.InstitutionStorageHelper;
import org.openmrs.module.ncd.utilities.ConditionUtilities;
import org.w3c.dom.Node;


/**
 * This segment critic determines if the message source is one
 * that only sends reportable conditions.
 * 
 * @author jlbrown
 *
 */
public class AlwaysReportableSourceCritic extends MessageCritic
{
    private static Log logger = LogFactory
        .getLog(AlwaysReportableSourceCritic.class);
    
    private static final Long criticId;
    
    static {
    	criticId = ConditionDetectorService.alwaysReportableSourceCritic;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.critic.IResultsProcessor#shouldReport(java.util.List, org.openmrs.module.ncd.critic.ReportResult)
     */
    public ReportResult shouldReport(List<Node> msgSegments, ReportResult currentResult)
            throws ResultCriticException
    {
        logger.debug("Entering shouldReport");
        if( msgSegments == null || msgSegments.isEmpty())
        {
            throw new ResultCriticException(
                    "A null or empty list of segment nodes was passed into shouldReport.");
        }

        ReportResult.ReportResultStatus result = ReportResult.ReportResultStatus.UNKNOWN;
        // Try the ReportResult for the app/fac first.
        HL7Producer appFac = currentResult.getApplicationFacility();
        
        if ( appFac == null && msgSegments != null && ! msgSegments.isEmpty())
        {
        	// Next, we'll try to locate the app/fac in the database.        	
        	try {        		
        		appFac = InstitutionStorageHelper.retrieveHL7Producer(currentResult.getMessage());
        	} catch (Exception e) {
        		throw new ResultCriticException(e);
        	}
        }
        
        if (appFac != null && appFac.isReportall())
        {
        	// We found an app/fac and it's listed as an always reportable source.
        	// Set the appropriate fields.
            result = ReportResult.ReportResultStatus.REPORT;
            currentResult.setIndicatingCriticId(criticId);
            logger.debug("Made a REPORT determination due to an always reportable message source."); 
            
            currentResult = searchSegmentsForCode(msgSegments, currentResult);
        }                
        
        if (appFac != null && currentResult.getApplicationFacility() == null) {
        	// If we have an app/fac and we didn't already have the app/fac stored
        	// in the ReportResult, store it now.
        	currentResult.setApplicationFacility(appFac);
        }

        currentResult.setReportResultStatus(result);
        logger.debug("Leaving shouldReport");
        return currentResult;
    }

	private ReportResult searchSegmentsForCode(List<Node> msgSegments,
			ReportResult currentResult) throws ResultCriticException {
		
		for (Node msgSegment : msgSegments) {
			IResultSegment resultSegment = null;
			try {
				resultSegment = ResultSegmentFactory.getResultSegment(msgSegment);
			} catch (Exception e) {
				throw new ResultCriticException(e);
			}
			if (resultSegment == null) {
				continue;
			}
			
			String code = resultSegment.getTestResultCode();
			String codeSystem = resultSegment.getTestResultCodeSystem();
			Set<Condition> conditionSet = ConditionUtilities.getConditionSet(code, codeSystem);
			
			if (conditionSet.isEmpty()) {
				String altCode = resultSegment.getTestResultAltCode();
				String altCodeSystem = resultSegment.getTestResultAltCodeSys();
				conditionSet = ConditionUtilities.getConditionSet(altCode, altCodeSystem);
			}
			
			if (conditionSet.size() == 1) {
				currentResult.addCondition(conditionSet.iterator().next(), msgSegments, criticId);
			} else {
				currentResult.addCondition(null, msgSegments, criticId);
			}
		}
		
		return currentResult;
	}		
}

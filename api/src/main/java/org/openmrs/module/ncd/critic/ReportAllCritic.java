/**
 * 
 */
package org.openmrs.module.ncd.critic;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.critic.ConditionExtractor.CodeSystemPair;
import org.openmrs.module.ncd.critic.ReportResult.ReportResultStatus;
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.model.IResultSegment;
import org.openmrs.module.ncd.model.ResultSegmentFactory;
import org.openmrs.module.ncd.utilities.ConditionUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * @author jlbrown
 *
 */
public class ReportAllCritic implements IResultsCritic {

	private static final Long criticId;
	private static Log logger = LogFactory.getLog(ReportAllCritic.class);
	
	static {
		criticId = ConditionDetectorService.reportAllCritic;
	}
	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.critic.ObxCritic#shouldReport(java.util.List, org.openmrs.module.ncd.critic.ReportResult)
	 */	
	public ReportResult shouldReport(List<Node> msgSegments,
			ReportResult currentResult) throws ResultCriticException {
		ReportResult newResult = new ReportResult(currentResult);
		if (msgSegments != null && ! msgSegments.isEmpty()) {
			try {
				IResultSegment resultSegment = ResultSegmentFactory.getResultSegment(msgSegments.get(0));
				if (resultSegment != null) {
					CodeSystemPair csPair = resultSegment.getCodeSystemPair();								
					Set<Condition> conditionSet = 
						ConditionUtilities.getConditionSet(csPair.getCode(), csPair.getCodeSystem());
					checkForAlwaysReportableCode(msgSegments, csPair, conditionSet, newResult);
					checkForAlwaysReportableCondition(msgSegments, newResult,
							conditionSet);
				}
			} catch (Exception e) {
				throw new ResultCriticException(e);
			}
		}
		return newResult;
	}		

	public boolean doesApply(List<Node> msgSegments)
			throws ResultCriticException {
		boolean retVal = false;	
		if (msgSegments != null && ! msgSegments.isEmpty()) {
			String nodeName = msgSegments.get(0).getNodeName();
			List<String> candidateSegments = NCDUtilities.getCandidateSegmentNames();
			if (candidateSegments.contains(nodeName)) {
				retVal = true;
			}
		}
		return retVal;
	}

	public boolean isDecidedResultCritic() {
		return false;
	}
	
	private void checkForAlwaysReportableCode(List<Node> msgSegments, CodeSystemPair csPair,
			Set<Condition> conditionSet, ReportResult newResult) {
		String code = csPair.getCode();
		String codeSystem = csPair.getCodeSystem();
		if (code != null && codeSystem != null) {						
			
			Code codeRow = NCDUtilities.getService().getCode(codeSystem, code);
			if (codeRow.isReportAll() && conditionSet.size() == 1) {
				logger.debug("Found report all code: " + code + ".");
				Condition condition = conditionSet.iterator().next();
				newResult.addCondition(condition, msgSegments, criticId);
				newResult.setReportResultStatus(ReportResult.ReportResultStatus.REPORT);
			}
		}
		
	}
	
	private void checkForAlwaysReportableCondition(List<Node> msgSegments,
			ReportResult newResult, Set<Condition> conditionSet) {		
		if (conditionSet != null) {
			for (Condition condition : conditionSet) {				
				if (condition != null && condition.isReportAll()) {					
					logger.debug("Found report all condition: " + condition.getDisplayText() + ".");
					newResult.addCondition(condition, msgSegments, criticId);
					newResult.setReportResultStatus(ReportResultStatus.REPORT);
					newResult.setReasonForStatus("Report All Condition found.");
				}
			}
		}
	}

}

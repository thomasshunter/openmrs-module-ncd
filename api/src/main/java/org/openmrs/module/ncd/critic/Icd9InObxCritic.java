/**
 * 
 */
package org.openmrs.module.ncd.critic;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.critic.ReportResult.ReportResultStatus;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.model.Observation;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * @author jlbrown
 *
 */
public class Icd9InObxCritic extends ObxCritic {
	
	private final static String ICD9_CODE_TYPE = "I9";
	
	public ReportResult shouldReport(List<Node> msgSegments, 
			ReportResult currentResult) throws ResultCriticException {
		ReportResult newResult = new ReportResult(currentResult);
		newResult.setReportResultStatus(ReportResultStatus.UNKNOWN);
		for (Node msgSegment : msgSegments) {
			Observation obx = new Observation(msgSegment);
			String obx5 = obx.getTestResultValue();
			Pattern icd9InObxPattern = Pattern.compile("(.*?) .* I9");
			Matcher icd9InObxMatcher = icd9InObxPattern.matcher(obx5);
			if (icd9InObxMatcher.find()) {
				String code = icd9InObxMatcher.group(1);						
				
				List<CodeCondition> possibleConditions = 
					NCDUtilities.getService().findByCodeAndSystem(code, ICD9_CODE_TYPE);
				if (possibleConditions != null) {
					for (CodeCondition possibleCondition : possibleConditions) {
						if (possibleCondition.getCondition().isReportable()) {
							newResult.setReportResultStatus(ReportResultStatus.REPORT);
							newResult.setReasonForStatus("ICD9 In Obx Critic found a reportable ICD9/value combination.");
							newResult.addCondition(possibleCondition.getCondition(), msgSegments, 
									ConditionDetectorService.icd9InObxCritic);
						}
					}				
				}
			}
		}	
		return newResult;
	}

}

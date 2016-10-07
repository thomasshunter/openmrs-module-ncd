/**
 *  This class finds "interesting" candidate results, where interesting
 *  is defined as having a reportable code (e.g. LOINC code) within
 *  the segment.
 */
package org.openmrs.module.ncd;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.critic.ConditionExtractor.CodeSystemPair;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.model.IResultSegment;
import org.openmrs.module.ncd.model.ResultSegmentFactory;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * A version of the CandidateResultFinder that prunes segments that
 * do not contain a reportable code. 
 * 
 * @author jlbrown
 *
 */
public class InterestingResultFinder extends CandidateResultFinder 
{
	private static Log logger = LogFactory.getLog(InterestingResultFinder.class);
	
	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.ICandidateResultFinder#findCandidateResults(java.lang.String)
	 */
	public List<Node> findCandidateResults(String message) throws CandidateResultFinderException 
	{
		List<Node> interestingCandidates  = new ArrayList<Node>();		
		List<Node> possibleCandidates     = super.findCandidateResults(message);
		
		try 
		{
			for (Node possibleCandidate : possibleCandidates) 
			{
				if (isInterestingCandidate(possibleCandidate)) 
				{
					interestingCandidates.add(possibleCandidate);
				}				
			}
			
			logger.debug("Found " + interestingCandidates.size() + " interesting candidate segments.");
		} 
		catch (Exception e) 
		{
			logger.warn("An exception was generated while attempting to find interesting candidate results.");
			throw new CandidateResultFinderException(e);
		}
		
		return interestingCandidates;
	}
	
	private boolean isInterestingCandidate(Node candidate) throws Exception 
	{
		boolean retVal = false;
				
		IResultSegment resultSegment = ResultSegmentFactory.getResultSegment(candidate); 
	
		if (resultSegment != null) 
		{
			CodeSystemPair csPair = resultSegment.getCodeSystemPair();
			List<CodeCondition> possibleConditions = null;
		
			if (csPair != null) 
			{
                String code         = csPair.getCode();
                String codeSystem   = csPair.getCodeSystem();
                possibleConditions  = NCDUtilities.getService().findByCodeAndSystem( code, codeSystem );
			}
			
			if (possibleConditions != null) 
			{
				for (CodeCondition codeCondition : possibleConditions) 
				{
				    Condition theCondition  = codeCondition.getCondition();
				    boolean isReportable    = theCondition.isReportable(); 
				    
					if( isReportable ) 
					{
						retVal = true;
						break;
					}
				}
			}
		}
		
		return retVal;
	}
}

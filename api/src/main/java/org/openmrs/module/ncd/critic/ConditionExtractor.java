/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.critic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.model.IResultSegment;
import org.openmrs.module.ncd.model.Note;
import org.openmrs.module.ncd.model.ResultSegmentFactory;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * Helper class to extract the condition from a candidate result Node.
 * 
 * @author jlbrown
 * 
 */
public class ConditionExtractor 
{
    public static class CodeSystemPair 
    {
        String code         = null;
        String codeSystem   = null;

        public String getCode() 
        {
            return code;
        }
        public void setCode(String code) 
        {
            this.code = code;
        }

        public String getCodeSystem() 
        {
            return codeSystem;
        }
        public void setCodeSystem(String codeSystem) 
        {
            this.codeSystem = codeSystem;
        }
    }                    

    /**
     * This method extracts the condition name from a given OBX node.
     * 
     * @param msgSegments OBX nodes from which the condition will be extracted.
     * @return A set of CodeCondition objects that were matched.
     */
    public static Set<CodeCondition> determineConditions(List<Node> msgSegments) throws Exception 
    {
        String testResultValues     = getTestResultValues(msgSegments);          
        Node firstSegment           = msgSegments.get(0);        
    	
        Set<CodeCondition> retVal   = new HashSet<CodeCondition>();
        CodeSystemPair cvPair       = getCodeSystemPair(firstSegment);
        String code                 = cvPair.getCode();
        
        if (StringUtils.isNotEmpty(code)) 
        {
            ConditionDetectorService ncdService     = NCDUtilities.getService();
            List<CodeCondition> possibleConditions  = ncdService.findByCodeAndSystem(code, cvPair.getCodeSystem());
            
            if (possibleConditions != null && ! possibleConditions.isEmpty())
            {            		            
	            retVal.addAll(findMatchWithValue(possibleConditions, testResultValues, msgSegments));
	
	            if (NCDUtilities.isNTEProcessingFlagOn()) 
	            {
                    // we didn't get a condition from the result segment value, so look at the NTE if there is one.	                	
                	for (Node msgSegment : msgSegments) 
                	{
                		if (! msgSegment.getNodeName().equals("NTE")) 
                		{
		                    String nteValue = Note.getConcatenatedNoteValues(msgSegment);
		                    retVal.addAll(findMatchWithValue(possibleConditions, nteValue, msgSegments));
                		}
	                }
                }
            }
        }

        return retVal;
    }              

    private static List<CodeCondition> findMatchWithValue(List<CodeCondition> possibleConditions, String observationValue, List<Node> msgSegments) 
    {
        List<CodeCondition> retVal      = new ArrayList<CodeCondition>();
        int numPossibleConditions       = possibleConditions.size();
    
        for (int conditionIdx = 0; conditionIdx < numPossibleConditions; conditionIdx++) 
        {
            CodeCondition codeConditionRow = possibleConditions.get(conditionIdx);
            if (! codeConditionRow.getCondition().isReportable()) {
            	continue;
            }            
                                                           
            retVal.add(codeConditionRow);            
        }
        return retVal;
    }

    public static CodeSystemPair getCodeSystemPair(Node msgSegment) throws Exception 
    {        
        IResultSegment resultSegment = ResultSegmentFactory.getResultSegment(msgSegment);
    
        return resultSegment.getCodeSystemPair();
    }    

    /**
     * Determine if the observation value contains a negation string.
     * 
     * @param observationValue The observation value from the result under
     *        examination.
     * @return True if the observation value contains a negation string and
     *         false if it does not.
     * @throws XPathExpressionException
     */
    public static boolean isNegative(String observationValue) 
    {
        boolean hasANegative = false;
    
        if (StringUtils.isNotEmpty(observationValue)) 
        {
            List<String> negationStrings = NCDUtilities.getNegationStrings();
        
            if (negationStrings != null)
            {
                for (int stringIdx = 0; stringIdx < negationStrings.size(); stringIdx++) 
                {
                    Pattern negationPattern = Pattern.compile("[\\s(]" + negationStrings.get(stringIdx) + "[\\s)]", Pattern.CASE_INSENSITIVE);
                    // We inject spaces here so that we can see a match at the
                    // beginning or end
                    // of the observation value.
                    Matcher negationMatcher = negationPattern.matcher(" " + observationValue + " ");
                    
                    if (negationMatcher.find()) 
                    {
                        hasANegative = true;
                        break;
                    }
                }
            }
        }
        
        return hasANegative;
    }
    
    public static String getTestResultValues(List<Node> msgSegments) throws Exception 
    {
    	String testResultValues        = null;
    	Node firstSegment              = msgSegments.get(0);        
        IResultSegment resultSegment   = ResultSegmentFactory.getResultSegment(firstSegment); 
    
        if (resultSegment != null) 
        {
        	testResultValues = resultSegment.getConcatenatedTestResultValues(msgSegments);
        }
        
        return testResultValues;
    }                
}

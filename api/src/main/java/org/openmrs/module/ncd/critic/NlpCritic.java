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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.NlpDiscreteTerm;
import org.openmrs.module.ncd.model.Note;
import org.openmrs.module.ncd.model.Observation;
import org.openmrs.module.ncd.nlp.INlpAnalyzer;
import org.openmrs.module.ncd.nlp.StringNormalizer;
import org.openmrs.module.ncd.preprocessing.TransformEmbeddedCarriageReturns;
import org.openmrs.module.ncd.utilities.HL7Utilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * Critic to wrap the RexAnalzyer.  Does some pre-processing of the data
 * to transform it into a format understood by the NLP analyzers.
 * 
 * @author jlbrown
 *
 */
public class NlpCritic extends ObxCritic
{
    private static Log logger                           = LogFactory.getLog(NlpCritic.class);  
    private static HashSet<Character> obx11FinalIndications;
    
    private final static String TEXT                    = "text";
    private final static String NUMERIC                 = "numeric";
    private final static String DISCRETE                = "discrete";
    private final static Long textCriticId;
    private final static Long numericCriticId;    
    private final static Long discreteCriticId;
    private final static String OBX                     = "OBX";
    private final static String NTE                     = "NTE";
    private final static String LOINC_CODE_SYSTEM       = "LN";
    public final static String SENTENCE_SEPARATOR       = ".BR. ";
    protected final static String numberPattern         = "\\d*[.]?\\d*";
	// Per HL7 spec definition of data type SN, should this include = and - as well?
	//protected final static String quasiNumberPattern  = "[0-9\\.:></]+";
	protected final static String QUASI_NUMBER_PATTERN  = "(<|>|<=|>=)?\\d+([.]\\d*)?([:/]\\d+([.]\\d*)?)?";
    
    static 
    {
        // Add the indicators that a OBX.11 value indicates that
        // the report is final (and not, for instance, preliminary).
        obx11FinalIndications   = new HashSet<Character>();
        obx11FinalIndications.add('F');        
        obx11FinalIndications.add('C');
        obx11FinalIndications.add('Z');
        obx11FinalIndications.add(null);
        textCriticId            = ConditionDetectorService.nlpCriticTextAnalyzer;
        numericCriticId         = ConditionDetectorService.nlpCriticNumericAnalyzer;
        discreteCriticId        = ConditionDetectorService.nlpCriticDiscreteAnalyzer;
    }     

    /**
     * Gets the LOINC scale type for the LOINC code in the OBX segment we're processing,
     * or "Other" if the code isn't LOINC, or not in our tables.
     * 
     * @param obx
     * @return
     */
    protected String getScaleType(Observation obx) 
    {
        String loincCode = obx.getLoincCode();
     
        if (loincCode != null) 
        {
        	Code code = NCDUtilities.getService().getCode(NlpCritic.LOINC_CODE_SYSTEM, loincCode);
        	
        	if (code != null) 
        	{
        		if (code.getScaleType() != null) 
        		{
        			return code.getScaleType();
        		}
        	}
        }
        
        return "Other";
    }
	
	/**
	 * Tests if the resultValue contains only a quasi-numeric value appropriate for scale type "Qn".
	 * This include values like "54", ">1", or "2:250".
	 * 
	 * public for testability
	 * @param resultValue
	 * @return
	 */
	public boolean isQuasiNumeric(String resultValue) 
	{	
		return Pattern.matches(NlpCritic.QUASI_NUMBER_PATTERN, resultValue);
	}

	/**
	 * Skips leading whitespace, then returns the quasi-number at the start of the remainder of the string, or null
	 * if the remainder does not start with a quasi-number.
	 * 
	 * public for testability.
	 * @param resultValue
	 * @return
	 */
	public String getLeadingQuasiNumericValue(String resultValue) 
	{
		// Discard leading whitespace
		String trimmed    = resultValue.trim();

		// Find the first digit
		Pattern p         = Pattern.compile(NlpCritic.QUASI_NUMBER_PATTERN);
		Matcher m         = p.matcher(trimmed);
		
		if (m.find()) 
		{
			String match = m.group();
		
			if (m.start() == 0) 
			{
				return match;
			}
		}
		
		return null;
	}
	
	/**
	 * Tests if the resultValues contain at least one instance of a discrete term match.
	 * 
	 * @param resultValues
	 * @return
	 */
	protected boolean containsDiscreteTerm(String resultValues) 
	{
        String obx5Lower    = resultValues.toLowerCase();
        obx5Lower           = StringNormalizer.removePunctuationAndPutSpacesAroundDashPercentAndAngleBrackets(obx5Lower);
        obx5Lower           = " " + obx5Lower + " ";

		// For each defined discrete term
        List<NlpDiscreteTerm> discreteTermList = NCDUtilities.getService().getAllNlpDiscreteTerms();
        
        for(NlpDiscreteTerm discreteTerm : discreteTermList) 
        {	
            // is discrete term in obx or obx in discrete term? 
            // check if 'this is positive' has 'positive' in it or 'pos' (equals avoids none = none detected error)
        	String term        = discreteTerm.getTerm();
        	int indexOfTerm    = obx5Lower.indexOf( term.trim() );
         
        	if(indexOfTerm > -1 || obx5Lower.contains(term) || term.equals(obx5Lower)) 
        	{ 
                return true;
            }
        }

        return false;
	}
	
    protected void analyze(String reportFormat, String resultValues, Set<Condition> possibleConditionSet, ArrayList<Condition> foundConditions)
	{
        if (StringUtils.isNotEmpty(resultValues)) 
        {
	        INlpAnalyzer analyzer = NCDUtilities.getNlpAnalyzer(reportFormat);
	        
	        if (analyzer != null) 
	        {                           
	            for( Condition condition : possibleConditionSet )
	            {
	                if( condition.isReportable() )
	                {
	                    String displayText     = condition.getDisplayText();	                    
	                    boolean addConditions  = analyzer.analyze( displayText, resultValues );
	                    
	                    if( addConditions )
	                    {
	                        foundConditions.add(condition);
	                    }
	                }
	            }
	        }
	        else
	        {
	            logger.error("Analyzer not found for " + reportFormat + " format results.");
	        }
        }
	}
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.critic.IResultsCritic#shouldReport(java.util.List, org.openmrs.module.ncd.critic.ReportResult)
     */
    public ReportResult shouldReport(List<Node> msgSegments, ReportResult currentResult) throws ResultCriticException
    {
        ReportResult result                     = new ReportResult(currentResult);
        result.setReportResultStatus(ReportResult.ReportResultStatus.UNKNOWN);
        Node firstSegment                       = msgSegments.get(0);
        
        // Determine the conditions we might find, based on the LOINC code in the first OBX segment
        Set<Condition> possibleConditionSet     = getReportableConditionSetForSegment(firstSegment);                               
        ArrayList<Condition> foundConditions    = new ArrayList<Condition>(); 

        // Determine the LOINC scale type of the LOINC code in the first OBX
        Observation firstObx                    = new Observation(msgSegments.get(0));
        String scaleType                        = getScaleType(firstObx);
    	logger.debug("scaleType=" + scaleType);

    	// Get the test result text from the first OBX segment.
        String testResultValue                  = firstObx.getTestResultValue();
    	logger.debug("testResultValue=\"" + testResultValue + "\"");
    	
        String reportFormat                     = TEXT;
    	
        if ("Ord".equals(scaleType) || "OrdQn".equals(scaleType)) 
        {
        	if (isQuasiNumeric(testResultValue)) 
        	{	
        		logger.debug("testResultValue is a number");
        		reportFormat = NUMERIC;
        	}
        	else 
        	{
        		logger.debug("testResultValue is discrete");
        		reportFormat = DISCRETE;
        	}

        	String resultValues = getNumericOrDiscreteResultValues(msgSegments);
    		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
        }
        else if ("Qn".equals(scaleType)) 
        {	
        	String resultValues = getNumericOrDiscreteResultValues(msgSegments);
        	
        	if (isQuasiNumeric(testResultValue)) 
        	{
        		logger.debug("testResultValue is a quasi-number");
        		reportFormat = NUMERIC;
        		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
        	}
        	else 
        	{
        		String firstNumericResultValue = getLeadingQuasiNumericValue(testResultValue);
    			logger.debug("first numeric value in result=" + firstNumericResultValue);
        		
    			if (firstNumericResultValue != null) 
    			{	
        			resultValues = HL7Utilities.replaceField(resultValues, 3, firstNumericResultValue);
            		logger.debug("rtestRsultValues starts with a number");
            		reportFormat = NUMERIC;
            		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
            	}
            	else if (containsDiscreteTerm(testResultValue) && possibleConditionSet.size() < 3) 
            	{
            		logger.debug("try discrete first");
            		reportFormat = DISCRETE;
            		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
            		
            		if (foundConditions.size() <= 0) 
            		{	
            			logger.debug("discrete anaylzer failed, use the text analyzer");
            			reportFormat = TEXT;
                    	resultValues = getTextResultValues(msgSegments);
                		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
            		}
            	}
            	else 
            	{	
            		logger.debug("use the text analyzer");
            		reportFormat = TEXT;
                	resultValues = getTextResultValues(msgSegments);
            		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
            	}
        	}
        }
        else if ("Nom".equals(scaleType)) 
        {	
        	String resultValues = getNumericOrDiscreteResultValues(msgSegments);
        	
        	if (isQuasiNumeric(testResultValue)) 
        	{	
        		logger.debug("testResultValue is a number");
        		reportFormat = NUMERIC;
        		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
        	}
        	else if (containsDiscreteTerm(testResultValue) && possibleConditionSet.size() < 3) 
        	{
        		logger.debug("try discrete first");
        		reportFormat = DISCRETE;
        		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
        		
        		if (foundConditions.size() <= 0) 
        		{	
        			logger.debug("discrete analyzer failed, use the text analyzer");
        			reportFormat = TEXT;
                	resultValues = getTextResultValues(msgSegments);
            		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
        		}
        	}
        	else 
        	{	
        		logger.debug("use the text analyzer");
        		reportFormat = TEXT;
            	resultValues = getTextResultValues(msgSegments);
        		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
        	}
        }
        else 
        {	
        	String resultValues = getNumericOrDiscreteResultValues(msgSegments);
        	
        	if (isQuasiNumeric(testResultValue)) 
        	{	
        		logger.debug("testResultValue is a number");
        		reportFormat = NUMERIC;
        		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
        	}
        	else 
        	{		
        		logger.debug("use the text analyzer");
        		reportFormat = TEXT;
            	resultValues = getTextResultValues(msgSegments);
        		analyze(reportFormat, resultValues, possibleConditionSet, foundConditions);
        	}
        }
        
        if (foundConditions.size() > 0)
        {
            result.setReportResultStatus(ReportResult.ReportResultStatus.REPORT);
            
            for (int i = 0; i < foundConditions.size(); i++)
            {
                Long indicatingCriticId = null;
                
                if (reportFormat.equals(TEXT)) 
                {                            
                	indicatingCriticId = textCriticId;
                } 
                else if (reportFormat.equals(NUMERIC)) 
                {
                	indicatingCriticId = numericCriticId;
                } 
                else if (reportFormat.equals(DISCRETE)) 
                {
                	indicatingCriticId = discreteCriticId;
                }
                
                result.addCondition(foundConditions.get(i), msgSegments, indicatingCriticId);
                result.setIndicatingCriticId(indicatingCriticId);
            }
        }
        
        return result;
    }        

    private String getTextResultValues(List<Node> msgSegments) throws ResultCriticException
    {
        StringBuilder resultValues  = new StringBuilder();
        Node firstSegment           = msgSegments.get(0);
        Observation firstObx        = new Observation(firstSegment);        
        resultValues.append(firstObx.getTestResultCodeByCodeSystem(LOINC_CODE_SYSTEM) + " ");
        
        for (Node msgSegment : msgSegments)
        {            
            String resultValue  = null;
            String nodeName     = msgSegment.getNodeName();
        
            if (nodeName.equals(OBX))
            {
                Observation obx = new Observation(msgSegment);
                
                if (obx11FinalIndications.contains(obx.getTestResultStatus()))
                {
                    // If the obx11 is one of the statuses that indicate
                    // a final result, then add this segment.                 
                    resultValue = obx.getTestResultValue();
                }
            }
            else if (nodeName.equals(NTE))
            {
                Note nte = new Note(msgSegment);
                resultValue = nte.getNoteValue();
            }
            else
            {
                throw new ResultCriticException("Unknown segment type received.");
            }
            
            if (resultValue != null) 
            {
            	resultValues.append(resultValue);
            }
            
            resultValues.append(" ");
        }
        
        // make sure we have a sentence separator at the end of the sequence.
        resultValues.append(SENTENCE_SEPARATOR);
        
        // 1/13/2011 eah: the \.br\ escape sequence used by the transformer is not technically allowed
        // for all data types (specified in OBX.2), but it's easier to just look for it in all types.
        String resultValuesWithBreaks = resultValues.toString().replace(TransformEmbeddedCarriageReturns.LINE_BREAK, SENTENCE_SEPARATOR);
    	logger.debug("getTextResultValues=\"" + resultValuesWithBreaks + "\"");
        
    	return resultValuesWithBreaks;
    }
    
    private String getNumericOrDiscreteResultValues(List<Node> msgSegments) 
    {
        final String partSeparator = "|";
        StringBuilder resultValues = new StringBuilder();
        
        for(Node msgSegment : msgSegments) 
        {
            Observation obx = new Observation(msgSegment);
        
            if (obx11FinalIndications.contains(obx.getTestResultStatus()))
            {
                String obx3 = obx.getTestResultCode();
            
                if (StringUtils.isEmpty(obx3)) 
                {
                    obx3 = obx.getTestResultAltCode();
                }
                
                resultValues.append(obx3);
                resultValues.append(partSeparator);
                resultValues.append(partSeparator);
                // 1/13/2011 eah: the \.br\ escape sequence used by the transformer is not technically allowed
                // for all data types (specified in OBX.2), but it's easier to just look for it in all types.
                resultValues.append(obx.getTestResultValue().replace(TransformEmbeddedCarriageReturns.LINE_BREAK, SENTENCE_SEPARATOR));
                resultValues.append(partSeparator);
                resultValues.append(obx.getTestResultUnits());
                resultValues.append(partSeparator);
                resultValues.append(obx.getNormalRange());
                resultValues.append(partSeparator);
                resultValues.append(obx.getAbnormalFlag());
                resultValues.append(SENTENCE_SEPARATOR);
            }
        }

        String resultValueString = resultValues.toString();
        logger.debug("getNumericOrDiscreteResultValues=\"" + resultValueString + "\"");
        
        return resultValueString;
    }

    public boolean isDecidedResultCritic() 
    {
        return false;
    }
}

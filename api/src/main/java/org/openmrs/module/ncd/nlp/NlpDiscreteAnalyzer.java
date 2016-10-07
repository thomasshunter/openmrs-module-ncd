/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.critic.NlpCritic;
import org.openmrs.module.ncd.database.NlpCriticContext;
import org.openmrs.module.ncd.database.NlpCriticContextType;
import org.openmrs.module.ncd.database.NlpDiscreteTerm;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/**
 *
 */
public class NlpDiscreteAnalyzer implements INlpAnalyzer {

	private static Log log = LogFactory.getLog(NlpDiscreteAnalyzer.class);
    static private final String OBX_PARTS_SEPARATOR = "\\|";
    static private final int OBX_5_INDEX = 2;
    static private final int OBX_8_INDEX = 5;    
        
    private static final String DISCRETE_NEGATION_CONTEXT_TYPE_NAME = "discreteNegation";
    
    /**
     * @see org.openmrs.module.ncd.nlp.INlpAnalyzer#analyze(java.lang.String, java.lang.String)
     */
    public boolean analyze(String condition, String resultChunk) {
        boolean conditionFound = false;
        
        List<String> sentences = getSentences(resultChunk);
        for (String sentence : sentences) {
            conditionFound = processBinaryReports(sentence);
            if (conditionFound) {
                break;
            }
        }
        
        return conditionFound;
    }
    
    protected List<String> getSentences(String obxChunk) {
        String[] sentenceAry = obxChunk.split(Pattern.quote(NlpCritic.SENTENCE_SEPARATOR));
        List<String> list = Arrays.asList(sentenceAry); 
        log.debug("sentences=" + list);
        return list;
    }
    
    protected boolean processBinaryReports(String results) {
        boolean positiveFlag = false;// reset
        String[] obxParts = results.split(OBX_PARTS_SEPARATOR, 6);        
        String obx5 = obxParts.length > OBX_5_INDEX ? obxParts[OBX_5_INDEX] : "";
        String obx8 = obxParts.length > OBX_8_INDEX ? obxParts[OBX_8_INDEX] : "";

        // check abnormal flag
        List<String> abnormalFlags = NCDUtilities.getReportableAbnormalFlags();
        if (abnormalFlags != null && abnormalFlags.contains(obx8)) {
            positiveFlag = true;
            log.debug("Discrete analyzer reports positive due to an abnormal flag.");
        } else {
            positiveFlag = determinePositivityBinary(obx5);
        }

        return positiveFlag;
    }
    
    private static class DecreasingTermLengthComparator implements Comparator<NlpDiscreteTerm> {

		@Override
		public int compare(NlpDiscreteTerm o1, NlpDiscreteTerm o2) {
			return o2.getTerm().length() - o1.getTerm().length();
		}
    }

    protected boolean determinePositivityBinary(String obx5) {

        // Make the OBX5 lowercase, normalize it, then pad it with spaces at the beginning and end.
        obx5 = obx5.toLowerCase();
        obx5 = StringNormalizer.removePunctuationAndPutSpacesAroundDashPercentAndAngleBrackets(obx5);
        obx5 = " " + obx5 + " ";

        if (log.isDebugEnabled()) {
        	log.debug("obx5=\"" + obx5 + "\"");
        }
    	
        //        original negative terms: " not detected | non reactive ";
        //        original positive terms: " positive | present | active | confirmed | reactive | detected ";
        
        ConditionDetectorService cdService = NCDUtilities.getService();
        List<NlpDiscreteTerm> discreteTerms = cdService.getAllNlpDiscreteTerms();

        // Sort the discreteTerms list in decreasing length of getTerm() value, so we check
        // longer terms before shorter terms.
        Collections.sort(discreteTerms, new DecreasingTermLengthComparator());
        if (log.isDebugEnabled()) {
        	log.debug("discreteTerms sorted=" + discreteTerms);
        }
        
        boolean leftmostTermSense = false;
        int leftmostMatchIndex = obx5.length() + 1;

        // Look for a matching discrete term
        for (NlpDiscreteTerm term : discreteTerms) {
        	int matchIndex = obx5.indexOf(term.getTerm());

        	// If this term matches
        	if (matchIndex >= 0) {

                if (log.isDebugEnabled()) {
                	log.debug("matches \"" + term.getTerm() + "\" at index " + matchIndex);
                }
        		
            	boolean positiveTerm = !term.isNegative();
            	
            	// Look for a negation preceding (or including) the matching term
            	// The leading space of the match is the last character of the possible negation as well.
            	String prefix = obx5.substring(0, matchIndex + term.getTerm().length());
            	if (containsNegation(prefix)) {

            		// If we found one, it reverses the sense (positivity) of the discrete term we found.
            		positiveTerm = !positiveTerm;
            	}

            	// Turn false positives into negatives
            	if (positiveTerm) {
            		positiveTerm = isNotFalsePositive(prefix);
            	}

            	if (matchIndex < leftmostMatchIndex) {
            		leftmostTermSense = positiveTerm;
            		leftmostMatchIndex = matchIndex;
            	}
        	}
        }

        // Return the sense of the leftmost term found, or false if none was found
        return leftmostTermSense;
    }
    
    /** Tests if the specified candidate contains a false positive string (e.g., " gram positive ".)
     * 
     * @param obx5
     * @return true if the candidate is a valid positive, false if the candidate contains a false positive.
     */
    protected boolean isNotFalsePositive(String obx5) {

    	for (String falsePositive : getFalsePositivePatterns()) {
    		if (obx5.contains(falsePositive)) {
    			log.debug("found the false positive \"" + falsePositive + "\"");
    			return false;
    		}
    	}

    	return true;
    }

    private static String[] falsePositivePatterns = null;
    
    protected synchronized String[] getFalsePositivePatterns() {

    	if (falsePositivePatterns == null) {
    		
    		NlpCriticContextType type = NCDUtilities.getService().findContextTypeByName("discreteFalsePositives");
    		List<NlpCriticContext> contexts = NCDUtilities.getService().findContextByType(type);
    		ArrayList<String> patterns = new ArrayList<String>(contexts.size());
    		for (NlpCriticContext context : contexts) {
    			patterns.add(context.getContextValue());
    		}
    		falsePositivePatterns = patterns.toArray(new String[0]);
    		
    		log.debug("loaded falsePositivePatterns=" + patterns);
    	}
    	
    	return falsePositivePatterns;
    }

    protected static class DecreasingContextLengthComparator implements Comparator<NlpCriticContext> {

		@Override
		public int compare(NlpCriticContext o1, NlpCriticContext o2) {
			return o2.getContextValue().length() - o1.getContextValue().length();
		}
    }
    
    protected boolean containsNegation(String str) {
    	
    	log.debug("searching \"" + str + "\" for negations.");
    	
        ConditionDetectorService cdService = NCDUtilities.getService();
        NlpCriticContextType discreteNegationContextType = cdService.findContextTypeByName(DISCRETE_NEGATION_CONTEXT_TYPE_NAME);
        log.debug("discrete negation context type=" + discreteNegationContextType);
        List<NlpCriticContext> discreteNegations = cdService.findContextByType(discreteNegationContextType);
        Collections.sort(discreteNegations, new DecreasingContextLengthComparator());
        if (log.isDebugEnabled()) {
        	log.debug("discreteNegations sorted=" + discreteNegations);
        }

        for (NlpCriticContext context : discreteNegations) {
        	
        	if (str.contains(context.getContextValue())) {
            	log.debug("found negation \"" + context.getContextValue() + "\"");
        		return true;
        	}
        }

        // The tail of the string didn't match any negation
        return false;
    }
}

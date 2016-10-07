/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.nlp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/**
 *
 */
public class NlpNumericAnalyzer implements INlpAnalyzer {    
    
	private static Log log = LogFactory.getLog(NlpNumericAnalyzer.class);
	
    static private final int OBX_3_INDEX = 0;
    static private final int OBX_5_INDEX = 2;
    static private final int OBX_6_INDEX = 3;
    static private final int OBX_7_INDEX = 4;
    static private final int OBX_8_INDEX = 5;
    static private final int OBX_PARTS_LENGTH = 6;
    static private final String REFERENCE_RANGE_SEPARATOR = "-";
    static private final String TITRE_SEPARATOR1 = ":";
    static private final String TITRE_SEPARATOR2 = "/";
    static private final String OBX_PARTS_SEPARATOR = "\\|";
    static private final String SENTENCE_SEPARATOR = "\\. ";
    
    public static class NormalRange {
    	private Double lowEndOfNormalRange = null;
    	private Double highEndOfNormalRange = null;   
    	private boolean lowEndIsInclusive = false;
    	private boolean highEndIsInclusive = false;
    	
    	public NormalRange(Double lowEnd, Double highEnd) {
    		lowEndOfNormalRange = lowEnd;
    		highEndOfNormalRange = highEnd;
    	}
    	
    	public NormalRange(Integer lowEnd, Integer highEnd) {
    		lowEndOfNormalRange = new Double(lowEnd);
    		highEndOfNormalRange = new Double(highEnd);
    	}
    	
    	public NormalRange(Double lowEnd, Double highEnd, boolean lowEndInclusive, boolean highEndInclusive) {
    		lowEndOfNormalRange = lowEnd;
    		highEndOfNormalRange = highEnd;
    		lowEndIsInclusive = lowEndInclusive;
    		highEndIsInclusive = highEndInclusive;
    	}
    	
    	public NormalRange(Integer lowEnd, Integer highEnd, boolean lowEndInclusive, boolean highEndInclusive) {
    		if (lowEnd == null) { 
    			lowEndOfNormalRange = null;
    		} else { 
    			lowEndOfNormalRange = new Double(lowEnd);
    		}
    		if (highEnd == null) {
    			highEndOfNormalRange = null;
    		}
    		else {
        		highEndOfNormalRange = new Double(highEnd);
    		}
    		lowEndIsInclusive = lowEndInclusive;
    		highEndIsInclusive = highEndInclusive;
    	}
    	
    	public Double getLowEndOfNormalRange() {
    		return lowEndOfNormalRange;
    	}
    	
    	public Double getHighEndOfNormalRange() {
    		return highEndOfNormalRange;
    	}
    	
    	public boolean isLowEndInclusive() {
    		return lowEndIsInclusive;
    	}
    	
    	public boolean isHighEndInclusive() {
    		return highEndIsInclusive;
    	}
    	
    	public String toString() {
    		
    		String str = "";
    		if (lowEndOfNormalRange != null) {
    			str = Double.toString(lowEndOfNormalRange);
    			if (lowEndIsInclusive) {
    				str += " <= ";
    			}
    			else {
    				str += " < ";
    			}
    		}
    		str += "x";
    		if (highEndOfNormalRange != null) {
    			if (highEndIsInclusive) {
    				str += " <= ";
    			}
    			else {
    				str += " < ";
    			}
    			str += Double.toString(highEndOfNormalRange);
    		}
    		return str;
    	}
    	
    	public boolean contains(Double value) {
    		if (lowEndOfNormalRange != null) {
    			if (lowEndIsInclusive) {
    				if (value < lowEndOfNormalRange) {
    					return false;
    				}
    			}
    			else {
    				if (value <= lowEndOfNormalRange) {
    					return false;
    				}
    			}
    		}
    		if (highEndOfNormalRange != null) {
    			if (highEndIsInclusive) {
    				if (value > highEndOfNormalRange) {
    					return false;
    				}
    			}
    			else {
    				if (value >= highEndOfNormalRange) {
    					return false;
    				}
    			}
    		}
    		return true;
    	}
    }
    
    /**
     * @see org.openmrs.module.ncd.nlp.INlpAnalyzer#analyze(java.lang.String, java.lang.String)
     */
    public boolean analyze(String condition, String resultChunk) {
        boolean conditionFound = false;
        
        List<String> sentences = getSentences(resultChunk);
        Iterator<String> sentenceIter = sentences.iterator();
        while (! conditionFound && sentenceIter.hasNext()) {
            String sentence = sentenceIter.next();
            Map<Integer, String> obxPartsMap = getObxParts(sentence);
            conditionFound = processNumericReports(obxPartsMap);            
        }
        
        return conditionFound;
    }
    
    private List<String> getSentences(String obxChunk) {
        String[] sentenceAry = obxChunk.split(SENTENCE_SEPARATOR);        
        return Arrays.asList(sentenceAry);
    }
    
    private Map<Integer, String> getObxParts(String sentence) {
        String[] parts = sentence.split(OBX_PARTS_SEPARATOR, 6);
        if (parts.length == OBX_PARTS_LENGTH) {            
            Map<Integer, String> obxPartsMap = new HashMap<Integer, String>();;        
            obxPartsMap.put(3, parts[OBX_3_INDEX]);
            obxPartsMap.put(5, parts[OBX_5_INDEX]);
            obxPartsMap.put(6, parts[OBX_6_INDEX]);
            obxPartsMap.put(7, parts[OBX_7_INDEX]);
            obxPartsMap.put(8, parts[OBX_8_INDEX]);
            return obxPartsMap;
        }
        else {
        	return null;
        }
    }
    
    private boolean processNumericReports(Map<Integer, String> obxParts) {
        
        if (obxParts == null) {
            return false;
        }
        boolean positiveFlag = false;

        String obx3 = obxParts.get(3);
        String obx5 = obxParts.get(5);        
        String obx7 = obxParts.get(7);
        String obx8 = obxParts.get(8);

        // check abnormal flag
        if (NCDUtilities.getReportableAbnormalFlags().contains(obx8)) {
        	log.debug("Numeric analyzer reports positive due to an abnormal flag.");
            positiveFlag = true;
        } else if (StringUtils.isNotEmpty(obx7)) { 
            // abnormal flag not present, ref range not blank
            boolean normalRangePresent = StringUtils.isNotEmpty(obx7);
            if (normalRangePresent && obx5.length() < 12) {
            	
            	if (obx5.contains(TITRE_SEPARATOR1) && obx7.contains(TITRE_SEPARATOR1)) {
            		
            		// Probable titre
            		positiveFlag = processTitre(obx5, obx7);
            	}
            	else if (obx5.contains(TITRE_SEPARATOR2) && obx7.contains(TITRE_SEPARATOR2)) {
            		
            		// Probable titre
            		positiveFlag = processTitre(obx5, obx7);
            	}
            	else {
	                // has both ref range and 
	                NormalRange normalRange = parseNormalRange(obx7);
	                if (normalRange != null) {
		                positiveFlag = isNumericOutsideRange(obx5, normalRange); 
	                }
            	}
                if (positiveFlag) {
                	log.debug("Numeric analyzer reports positive due to result being outside the normal range.");
                }
            }            
        } else { // empty ref range, so create one
            String obx3Lower = obx3.toLowerCase();
            // get these #'s externally??
            if (obx3Lower.contains("lead")) { // condition specific
                NormalRange leadNormalRange = new NormalRange(0,9);
                positiveFlag = isNumericOutsideRange(obx5, leadNormalRange);
                if (positiveFlag) {
                	log.debug("Numeric analyzer reports positive due to lead result being outside the normal range.");
                }
            }            
        }

        return positiveFlag;
    }

    public static class TitreValue {
    	
    	private String indicator;
    	private double value;
    	
    	private TitreValue(String indicator, double value) {
    		this.indicator = indicator;
    		this.value = value;
    	}
    	
    	public static TitreValue parse(String value) {
    		
        	String patternStr = "\\s*(<|>|<=|>=)?(\\d+(?:[.]\\d*)?)[:/](\\d+(?:[.]\\d*)?).*";
        	Pattern pattern = Pattern.compile(patternStr);
        	Matcher matcher = pattern.matcher(value);
        	if (matcher.matches()) {
        		String outOfRangeIndicator = matcher.group(1);
        		String numeratorStr = matcher.group(2);
        		String denominatorStr = matcher.group(3);
        		if (numeratorStr != null && denominatorStr != null) {
        			Double numerator = Double.parseDouble(numeratorStr);
        			Double denominator = Double.parseDouble(denominatorStr);
        			if (denominator > 0.0) {
        				return new TitreValue(outOfRangeIndicator, numerator / denominator);
        			}
        		}
        	}
        	
        	return null;
    	}
    	
    	public String getIndicator() {
    		return this.indicator;
    	}
    	
    	public double getRawValue() {
    		return value;
    	}

    	public double getAsValue() {

    		double retValue = value;
    		
        	if (">".equals(indicator)) {
        		retValue *= 1.001;
        	}
        	else if ("<".equals(indicator)) {
        		retValue *= 0.999;
        	}
        	
        	return retValue;
    	}
    	
    	public NormalRange getAsNormalRange() {
    		
    		if (indicator == null) {
    			return new NormalRange(value, value, true, true);
    		}
    		else if ("<".equals(indicator)) {
    			return new NormalRange(null, value, false, false);
    		}
    		else if ("<=".equals(indicator)) {
    			return new NormalRange(null, value, false, true);
    		}
    		else if (">".equals(indicator)) {
    			return new NormalRange(value, null, false, false);
    		}
    		else if (">=".equals(indicator)) {
    			return new NormalRange(value, null, true, false);
    		}
    		else {
    			return null;
    		}
    	}
    }

    public boolean processTitre(String obx5, String obx7) {

    	try {
	    	// Get the test result value and normal range
	    	double testResultValue = TitreValue.parse(obx5).getAsValue();
	    	NormalRange normalRange = TitreValue.parse(obx7).getAsNormalRange();
	
	    	return !normalRange.contains(testResultValue);
    	}
    	catch (Exception e) {
    		log.warn("error parsing titre value or range.");
    		return false;
    	}
    }

    public NormalRange parseNormalRange(String obx7) {
    	String lowEndString = null;
    	String highEndString = null;
    	NormalRange retVal = null;
    	try {
			if (obx7.contains(REFERENCE_RANGE_SEPARATOR)) {
				lowEndString = StringUtils.substringBefore(obx7, REFERENCE_RANGE_SEPARATOR).trim();
		        highEndString = StringUtils.substringAfter(obx7, REFERENCE_RANGE_SEPARATOR).trim();        	        
		        highEndString = StringNormalizer.fixHtmlFontPlusRemovePunctuationExceptPeriodAndTilde(highEndString);	        
		        retVal = new NormalRange(Double.parseDouble(lowEndString), Double.parseDouble(highEndString), true, true);
			} else if (obx7.contains(">=")) {
				lowEndString = StringUtils.substringAfter(obx7, ">=").trim();
				retVal = new NormalRange(Double.parseDouble(lowEndString), null, true, false);
			} else if (obx7.contains("<=")) {
				highEndString = StringUtils.substringAfter(obx7, "<=").trim();
				retVal = new NormalRange(null, Double.parseDouble(highEndString), false, true);
			} else if (obx7.contains(">")) {
				lowEndString = StringUtils.substringAfter(obx7, ">").trim();
				retVal = new NormalRange(Double.parseDouble(lowEndString), null, false, false);
			} else if (obx7.contains("<")) {
				highEndString = StringUtils.substringAfter(obx7, "<").trim();
				retVal = new NormalRange(null, Double.parseDouble(highEndString), false, false);
			}
    	} catch (NumberFormatException e) {
    		log.warn("Could not get a normal range from the following OBX-7 text: " + obx7 +".");
    		log.warn("Reason: " + e.getMessage());
    	}
		
		return retVal;
	}
    
    static private Double parseTestResultValue(String obx5) {
    	Double retVal = null;
    	String anyNumber = "\\d+(\\.\\d{1,})?"; // number somewhere
        Pattern pExc3 = Pattern.compile(anyNumber);
        Matcher m2 = pExc3.matcher(obx5);
        boolean numberFound = m2.find();
        if (numberFound) {
        	double obxNumericValue = Double.parseDouble(m2.group());
        	if (obx5.startsWith(">=") || obx5.startsWith("<=")) {
        		retVal = obxNumericValue;
        	} else if (obx5.startsWith(">")) {
        		retVal = obxNumericValue + 0.5;
        	} else if (obx5.startsWith("<")) {
        		retVal = obxNumericValue - 0.5;
        	} else {
        		retVal = obxNumericValue;
        	}
        }
        	
        return retVal;
    }

	static public boolean isNumericOutsideRange(String obx5, NormalRange normalRange ) {
        boolean numericIsOutsideRange = false;
        Double obxNumericValue = parseTestResultValue(obx5);
        if (obxNumericValue != null) {
            Double lowEndOfNormalRange = normalRange.getLowEndOfNormalRange();
            Double highEndOfNormalRange = normalRange.getHighEndOfNormalRange();
            // set to true if the OBX.5 numeric is outside the normal range.
            if (lowEndOfNormalRange != null) {
            	if (normalRange.isLowEndInclusive()) {
            		numericIsOutsideRange = obxNumericValue < lowEndOfNormalRange;
            	} else {
            		numericIsOutsideRange = obxNumericValue <= lowEndOfNormalRange;
            	}
            }
            
            if (! numericIsOutsideRange && highEndOfNormalRange != null) {
            	if (normalRange.isHighEndInclusive()) {
            		numericIsOutsideRange = obxNumericValue > highEndOfNormalRange;
            	} else {
            		numericIsOutsideRange = obxNumericValue >= highEndOfNormalRange;
            	}            		
            }                        
        }

        return numericIsOutsideRange;
    }
}

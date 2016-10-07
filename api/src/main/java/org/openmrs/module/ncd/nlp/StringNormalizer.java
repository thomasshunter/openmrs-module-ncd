/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.nlp;

import org.openmrs.module.ncd.utilities.ReplaceMap;

/**
 * Normalizes a string by replacing various punctuation or special characters with spaces. 
 */
public class StringNormalizer {

    private static ReplaceMap removeSingleQuoteAndParenthesisMap = null;
    private static ReplaceMap fixHtmlFontPlusRemovePunctuationMap = null;
    private static ReplaceMap fixHtmlFontPlusRemovePunctuationExceptTildeMap = null;
    private static ReplaceMap fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap = null;
    private static ReplaceMap removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap = null;
    private static ReplaceMap preSentenceCreationReplacementMap = null;
    
    static
    {
        // begin order matters
        createRemoveSingleQuoteAndParenthesisMap();
        createFixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap();        
        createFixHtmlFontPlusRemovePunctuationExceptTildeMap();
        createFixHtmlFontPlusRemovePunctuationMap();
        // end order matters
        createRemoveMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap();
        createPreSentenceCreationReplacementMap();
    }
    
    static private void createRemoveSingleQuoteAndParenthesisMap()
    {
        removeSingleQuoteAndParenthesisMap = new ReplaceMap();
        removeSingleQuoteAndParenthesisMap.put("\\'", " "); 
        removeSingleQuoteAndParenthesisMap.put("\\(", "  "); 
        removeSingleQuoteAndParenthesisMap.put("\\)", "  ");            
    }
    
    static private void createFixHtmlFontPlusRemovePunctuationMap()
    {
        if (fixHtmlFontPlusRemovePunctuationExceptTildeMap == null)
        {
            throw new IllegalStateException("normal2Map must be created after normal3Map");
        }
        fixHtmlFontPlusRemovePunctuationMap = new ReplaceMap(fixHtmlFontPlusRemovePunctuationExceptTildeMap);
        fixHtmlFontPlusRemovePunctuationMap.put("~", " ");
    }
    
    static private void createFixHtmlFontPlusRemovePunctuationExceptTildeMap()
    {
        if (fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap == null)
        {
            throw new IllegalStateException("normal3Map must be created after normal4Map");
        }
        fixHtmlFontPlusRemovePunctuationExceptTildeMap = new ReplaceMap(fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap);
        fixHtmlFontPlusRemovePunctuationExceptTildeMap.put("\\.", " ");        
    }
    
    static private void createFixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap()
    {
        if (removeSingleQuoteAndParenthesisMap == null)
        {
            throw new IllegalStateException("normal4Map must be created after normalMap");
        }
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap = new ReplaceMap(removeSingleQuoteAndParenthesisMap);        
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("\\,", " ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("\\^", " "); 
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("\\+", "  ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("\\/", ""); 
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("< font>", "</font>");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("\\{", " ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("\\}", " ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("\\-", "  ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("\\:", " ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("%", "  ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put(">", "  ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("<", "  ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("\\*", " ");
        fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.put("^", " ");
    }
    
    static private void createRemoveMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap()
    {
    	removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap = new ReplaceMap();
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\'", " "); // removing any single quotes in LOINC co
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\,", " "); // removing any single quotes in LOINC co
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\.", " "); // removing any single quotes in LOINC co
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\^", " "); // removing any single quotes in LOINC co
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\+", "  "); // removing any single quotes in LOINC co
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\(", "  "); // removing any single quotes in LOINC co
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\)", "  "); // removing any single quotes in LOINC co
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\/", "  "); // removing any single quotes in LOINC co
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\{", " ");
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\}", " ");
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\-", " - ");
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\\:", " ");
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("%", " % ");
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put(">", " > ");
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("<", " < ");
        removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.put("\"", " "); // removing any single quotes in LOINC all
    }
    
    static private void createPreSentenceCreationReplacementMap() {
    	preSentenceCreationReplacementMap = new ReplaceMap();
    	preSentenceCreationReplacementMap.put("detected", "detected.");
    	preSentenceCreationReplacementMap.put("=", " = ");
    	preSentenceCreationReplacementMap.put("negative", " negative ");
    }
    
    // formerly normal
    static public String removeSingleQuoteAndParentheses(String component) {
        component = removeSingleQuoteAndParenthesisMap.applyMap(component);
        component = StringCompressor.compress(component);
        component = component.trim();
        return component;
    }
    
    // formerly normal2
    //  replaces charcs. with mostly spaces
    static public String fixHtmlFontPlusRemovePunctuation(String component) {
        component = fixHtmlFontPlusRemovePunctuationMap.applyMap(component);
        component = StringCompressor.compress(component);
        component = component.trim();
        return component;
    }
    
    // formerly normal3
    static public String fixHtmlFontPlusRemovePunctuationExceptTilde(String component) {
        component = fixHtmlFontPlusRemovePunctuationExceptTildeMap.applyMap(component);
        component = StringCompressor.compress(component);
        component = component.trim();
        return component;
    }
    
    // formerly normal4
    static public String fixHtmlFontPlusRemovePunctuationExceptPeriodAndTilde(String component) {
        component = fixHtmlFontPlusRemovePunctuationExceptPeriodAndTildeMap.applyMap(component);
        component = StringCompressor.compress(component);
        component = component.trim();
        return component;
    }
    
    // formerly normalizeComp2    
    static public String removePunctuationAndPutSpacesAroundDashPercentAndAngleBrackets(String co) {
        co = removeMostPunctuationAndPutSpacesAroundDashPercentAndAngleBracketsMap.applyMap(co);
        co = co.toLowerCase();
        co = StringCompressor.compress(co);
        co = co.trim();
        return co;
    }
    
    static public String applyPreSentenceCreationReplacementMap(String co) {
    	co = co.toLowerCase();
    	co = preSentenceCreationReplacementMap.applyMap(co);    	
    	return co;
    }
}

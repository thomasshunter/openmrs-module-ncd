/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 *  Class to get from a string the window around a particular word or sentence.  A window
 *  is a set of words or sentences before and/or after the word or sentence in question. 
 */
public class StringWindow {
    static public String[] getWordWindow(String cs, String matchedConcept, int lookBack, int lookAhead) {
        String[] ww = null;

        // getting pre and post strings and getting lengths of each
        List<String[]> beforeAfterResult = beforeAfter(cs, matchedConcept);
        String[] preWords = beforeAfterResult.get(0);
        String[] postWords = beforeAfterResult.get(1);
        // reforming appropiately lengthed pre and post strings
        String[] reformedStrings = reformingStrings(cs, preWords, postWords, lookBack, 
                lookAhead);
        String preString = normalizeWordWindow(reformedStrings[0]);
        String postString = normalizeWordWindow(reformedStrings[1]);
        
        ww = new String[2];
        ww[0] = preString;
        ww[1] = postString;
                
        return ww;
    }
    
    static public String getCombinedWordWindow(String cs, String matchedConcept, int lookBack, int lookAhead) {
        String[] wordWindow = getWordWindow(cs, matchedConcept, lookBack, lookAhead);
        return wordWindow[0] + wordWindow[1];
    }
    
    static public String getParaWindow(List<String> sentences, String curSentence) { // called when conc not negated
        int curSentIdx = sentences.indexOf(curSentence);
        StringBuilder paraWindow = new StringBuilder();
        paraWindow.append(" ");
        
        String[] sentAry = new String[10];
        sentAry = sentences.toArray(sentAry);
        if (sentAry == null) {
            return null;
        }
        
        // Concatenate the sentences up to 3 before the current sentence, the current sentence, 
        // and the sentences 2 after the current sentence.
        // Example: If the list of sentences includes 10 sentences and the current sentence is sentence #5,
        // we will get and concatenate sentences 2 through 7.
        int preIdx = curSentIdx - 3;
        if (preIdx < 0) {
            preIdx = 0;
        }
        for (; preIdx != curSentIdx; preIdx++) {
            paraWindow.append(sentAry[preIdx]);
            paraWindow.append(" ");
        }
        paraWindow.append(sentAry[curSentIdx]);
        paraWindow.append(" ");
        
        for (int postIdx = curSentIdx + 1; 
             postIdx != sentAry.length && postIdx != curSentIdx + 3; 
             postIdx++) {
            paraWindow.append(sentAry[postIdx]);
            paraWindow.append(" ");
        }
        
        return paraWindow.toString();
    }

    
    static private List<String[]> beforeAfter(String fullSentence, String matchedConcept) {
        List<String[]> beforeAfterResult = new ArrayList<String[]>();
        // getting string before concept        
        String preString = StringUtils.substringBefore(fullSentence, matchedConcept);        
        String[] preWords = preString.split(" "); // all words obtained here
        beforeAfterResult.add(preWords);        
        
        // getting string after concept minus concept        
        String postString = StringUtils.substringAfter(fullSentence, matchedConcept);        
        String[] postWords = postString.split(" "); // all words obtained here
        beforeAfterResult.add(postWords);
        
        return beforeAfterResult;
    }    
    
    static private String[] reformingStrings(String preString, String[] preWords, String[] postWords,
            int backAmount, int aheadAmount) {
        StringBuilder reformedPreStringBuilder = new StringBuilder(" ");       

        String regConj = "( and | or )";
        Pattern pconj = Pattern.compile(regConj);
        Matcher mconj = pconj.matcher(preString); // get a matcher object
        boolean conjunction = mconj.find();
        if (conjunction == true) { 
            // commas in sentence or list already determined to be present
            backAmount = backAmount + 3; // add 3 more to the current backAmount
            conjunction = false;
        }
        int maxPreWords = preWords.length - 1;
        if (maxPreWords > backAmount + 1) { // more pre concept words than the backAmount
             // get subset of the prewords
            for (int preStart = preWords.length - backAmount; preStart <= preWords.length - 1; preStart++) {
                reformedPreStringBuilder.append(" ");
                reformedPreStringBuilder.append(preWords[preStart]);
            }
        } else { // the preconcept words are less than the backAmount, so just get all pre words
            for (int preStart = 0; preStart <= preWords.length - 1; preStart++) {
                reformedPreStringBuilder.append(" ");
                reformedPreStringBuilder.append(preWords[preStart]);
            }
        }
        // "padding" so each word has a space around it
        reformedPreStringBuilder.append(" ");
        
        // reforming postString
        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        StringBuilder reformedPostStringBuilder = new StringBuilder(" ");
        
        int maxPostWords = postWords.length - 1;
        if (maxPostWords > aheadAmount) { // more post concept words than aheadAmount
            int postEnd = aheadAmount + 1; // using 7 here because length is artifically 1 high
            for (int postCount = 0; postCount < postEnd; postCount++) { // get a subset of the total words ahead
                reformedPostStringBuilder.append(" ");
                reformedPostStringBuilder.append(postWords[postCount]);
            }
        } else { // post concept words less than aheadAmount so just get all post words
            for (int postCount = 0; postCount <= postWords.length - 1; postCount++) {
                reformedPostStringBuilder.append(" ");
                reformedPostStringBuilder.append(postWords[postCount]);
            }
        }
        // "padding" so each word has a space around it
        reformedPostStringBuilder.append(" "); 
        String[] retVal = {reformedPreStringBuilder.toString(), reformedPostStringBuilder.toString()};
        return retVal;
    }
    
    static private String normalizeWordWindow(String aString) {
        aString = StringNormalizer.fixHtmlFontPlusRemovePunctuationExceptTilde(aString); // normalize for below
        aString = " " + aString + " ";        
        return aString;
    }
}

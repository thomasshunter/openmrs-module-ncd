/**
 * Copyright 2008 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.cache.NlpCriticConceptCache;
import org.openmrs.module.ncd.database.NlpCriticConcept;
import org.openmrs.module.ncd.utilities.ReplaceMap;
import org.openmrs.module.ncd.utilities.StringUtilities;

/**
 *  This class implements the INlpAnalyzer interface and uses Natural Language Processing
 *  to determine if the condition is found within the section of text passed into it.
 */
public class NlpTextAnalyzer implements INlpAnalyzer 
{    
    private static Log log                                      = LogFactory.getLog(NlpTextAnalyzer.class);	
    static private ReplaceMap conFlagMap                        = null;
    static private ReplaceMap preProcessingMap1                 = null;
    static private ReplaceMap preProcessingMap2                 = null;
    static private HashMap<String, NlpCriticConcept> conceptMap = null;
    static private int MIN_SENTENCE_LENGTH                      = 5;
    static private final String splitExpr                       = "_";            
   
    private List<String> positives                              = new ArrayList<String>();
    private List<String> negatives                              = new ArrayList<String>();
    private List<String> pseudos                                = new ArrayList<String>();
    
    private String lastMatch                                    = null;        

    static 
    {               
        createConFlagMap();
        createPreProcessingMaps();
    }
    
    static private void createConFlagMap()
    {
        conFlagMap = new ReplaceMap();
        conFlagMap.put("<br/>", "bbb");
        conFlagMap.put("</p>", "ppp");
        conFlagMap.put("<p>", "qqq");
        conFlagMap.put("<h3>", "hhh");
        conFlagMap.put("</h3>", "ggg");
    }
    
    static private void createPreProcessingMaps()
    {
        preProcessingMap1 = new ReplaceMap();
        preProcessingMap1.put("~", " ");
        preProcessingMap1.put("\\."," \\. ");
        preProcessingMap1.put("\n", "");
        preProcessingMap1.put(",", "");
        preProcessingMap1.put(":", " : ");
        preProcessingMap1.put("\\(", " ( ");  // eah 10/31/2008
        preProcessingMap1.put("\\)", " ) ");  // eah 10/31/2008
        
        preProcessingMap2 = new ReplaceMap();
        preProcessingMap2.put(">", "> ");
        preProcessingMap2.put("<", " <");        
    }
        
    static private void loadConcepts()
    {
        conceptMap = NlpCriticConceptCache.getMap();
    }  
    
    private void reset()
    {
        positives.clear();
        negatives.clear();
        pseudos.clear();
    }

    /**
     * @see org.openmrs.module.ncd.nlp.INlpAnalyzer#analyze(java.lang.String, java.lang.String)
     */
    public boolean analyze(String condition, String resultChunk) 
    {
        reset();        
        loadConcepts();
        
        // arrays with proper values       
        // get all sentences in report first
        String tempObx                  = resultChunk.replaceAll("~", " ");
        tempObx                         = StringCompressor.compress(tempObx);
        ArrayList<String> sentences     = getSentences(tempObx);

        NlpCriticConcept concept        = conceptMap.get(condition);        
        
        if (concept == null)
        {
            // If the concept isn't found in the map, we return false (condition not found).
            return false;
        }        
        // sentence loop
        
        return checkingPrimaryConcepts(sentences, concept);
    }        

    private List<String> padNegationMembers(List<String> negationList) 
    {
        List<String> paddedNegationList = new ArrayList<String>();
    
        if (negationList != null)
        {
            for (String negationString : negationList) 
            {
            	if(negationString != null && !negationString.trim().isEmpty()) 
            	{
            		paddedNegationList.add(" " + negationString + " ");
            	}
            }
        }
        
        return paddedNegationList;
    }

    private ArrayList<String> getSentences(String obxChunk) 
    {
        ArrayList<String> sentences     = new ArrayList<String>();
        obxChunk                        = StringNormalizer.applyPreSentenceCreationReplacementMap(obxChunk);
        String[] sentenceAry            = obxChunk.split("\\.[ \"]");
    
        for (String sentence : sentenceAry) 
        {
            sentences.add(sentence);
        }

        return sentences;
    }

    private boolean  checkingPrimaryConcepts(List<String> thisSentence, NlpCriticConcept concept) 
    {        
        // preprocess
        List<String> sentences = preProcessSentence(thisSentence);

        // check concept for each sentence
        // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // send sentences for processing with new concept data
        boolean conditionFound = primeConcept3(sentences, concept);

        return conditionFound;
    }

    private List<String> preProcessSentence(List<String> sentences) 
    {
        ArrayList<String> preProcessedSentences = new ArrayList<String>();
    
        for (String sentence : sentences) 
        {
            String preProcessedSentence     = sentence.toLowerCase();
            preProcessedSentence            = preProcessingMap1.applyMap(preProcessedSentence);
            preProcessedSentence            = StringCompressor.compress(preProcessedSentence);
            preProcessedSentence            = preProcessingMap2.applyMap(preProcessedSentence);
            preProcessedSentence            = StringCompressor.compress(preProcessedSentence);
            preProcessedSentence            = preProcessedSentence.trim();
            preProcessedSentence            = " " + preProcessedSentence + " "; // pad front and back
            
            preProcessedSentences.add(preProcessedSentence);
        }

        return preProcessedSentences;
    }    

    private boolean primeConcept3(List<String> sentences, NlpCriticConcept concept) 
    {       
        String preString        = "";
        String postString       = "";     
        boolean matchFound      = false;
        boolean conceptNegated  = false;        
      
        log.debug("Current concept is " + concept.getConceptName() + ".");
    
        for (String conThisSentence : sentences) 
        {
            if (conThisSentence.length() < MIN_SENTENCE_LENGTH) 
            {
                // if this sentence isn't long enough, skip it.
                continue;
            }
            
            String origSentence     = conThisSentence;    
            conThisSentence         = conFlagMap.applyMap(conThisSentence);
            conThisSentence         = StringNormalizer.fixHtmlFontPlusRemovePunctuationExceptTilde(conThisSentence);
            conThisSentence         = " " + conThisSentence + " ";
    
            // initializing            
            log.debug("Checking for absolute match...");
            matchFound              = checkAbsolutes(concept, conThisSentence);
                
            if( !matchFound )
            {            	
            	log.debug("Absolute match not found.");
            	log.debug("Checking alternate concepts 1 and 2...");
                // take first two alternate concepts and place into an array
                String allAlts              = concept.getAltCon1() + "_" + concept.getAltCon2();
                List<String> allAltsList    = StringUtilities.splitStringToList(allAlts, splitExpr);
                matchFound                  = checkAlternatives(concept, conThisSentence, allAltsList);
                log.debug("Alternate match " + (matchFound ? "found" : "not found") + ".");
            }
            
            // paragraph processing                        
            if (matchFound) 
            {
            	log.debug("Aboslute or alternate match found.");
                // some match occurred and no exception occurred
                // check the parag for inclusion criteria
                String parag                    = StringWindow.getParaWindow(sentences, origSentence);
                String paragraph                = concept.getParagraph();
                List<String> paragraphList      = (StringUtils.isEmpty(paragraph) ? null : StringUtilities.splitStringToList(paragraph, splitExpr));
                log.debug("Checking paragraph window...");
                
                if (paragraphList == null || checkForParagraphWindowInclusions(parag, paragraphList)) 
                {                	
                    // have whether a match occurred by here
                    // concept was found, check for exceptions                    
                    String trimmedLastMatch         = lastMatch.trim(); // no longer need starting space
                    log.debug("Getting word windows.");
                    String[] smallWordWindow        = StringWindow.getWordWindow(conThisSentence, trimmedLastMatch, 3, 3);
                    String smallWindowPreString     = smallWordWindow[0];
                    String smallWindowPostString    = smallWordWindow[1];
                    
                    String[] wordWindow             = StringWindow.getWordWindow(conThisSentence, trimmedLastMatch, 12, 12);                                        
                    preString                       = wordWindow[0];
                    postString                      = wordWindow[1];
    
                    // check contexts
                    // NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
                    String contexts                 = concept.getContexts();
                    
                    if (contexts != null) 
                    {
                    	log.debug("Checking contexts...");
                        List<String> listOfContextsToCheck  = StringUtilities.splitStringToList(contexts, splitExpr);
                        // Context, negations, and pseudos
                        conceptNegated                      = checkForContextOrNegationOrPseudo(listOfContextsToCheck, concept, preString, postString, smallWindowPreString, smallWindowPostString);
                                                                
                        if (conceptNegated) 
                        {
                        	log.debug("Concept was negated.");
                        	log.debug("Double checking that this is a true negation...");
                            conceptNegated = doubleCheckNegation(concept, conThisSentence, smallWindowPreString, smallWindowPostString, listOfContextsToCheck);
                        } 
                        else 
                        {
                        	log.debug("Concept was not negated.");
                        	log.debug("Checking for other negations...");
                            conceptNegated = checkForOtherNegations(conThisSentence, concept, smallWordWindow[0] + smallWordWindow[1]);
                        }
                    }                                           
                    
                    // NNNNNNNNNNNNNNNNNN Context/negation determined by here
                    // NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
                    
                    // If we found a false positive or concept negation, negate the match.
                    if (conceptNegated) 
                    {
                    	log.debug("Match not found due to false positive or negation.");
                        matchFound = false;
                    }
                } 
                else 
                {
                	log.debug("Paragraph window inclusion not found indicating this wasn't a true match.");
                    matchFound = false;
                }                                
            }
            
            if (matchFound) 
            {
            	log.debug("Match found.");
                break;
            }
        }
        
        return matchFound;
    }
    
    private boolean checkAbsolutes(NlpCriticConcept concept, String sentence) 
    {
        // absolutes
        String absolutes    = concept.getAbsolutes();
        boolean matchFound  = false;
    
        if (StringUtils.isNotEmpty(absolutes) ) 
        {
            boolean regExMatches = StringUtilities.doesRegExMatch(absolutes, sentence, false, 0);
            
            if( regExMatches )
            {
                log.debug("Match found on term: " + StringUtilities.getGroupFromLastMatch());
                matchFound          = true;
                lastMatch           = StringUtilities.getGroupFromLastMatch();
                // get word 'window'
                String wordWindow   = StringWindow.getCombinedWordWindow(sentence, lastMatch, 12, 12);                        

                // exceptions to absolutes
                String absExc       = concept.getAbsExcept();
            
                if( StringUtils.isNotEmpty( absExc ) )
                {
                    boolean doesRegExMatch = StringUtilities.doesRegExMatch(absExc, wordWindow, 0);
                
                    if( doesRegExMatch )
                    {
                        log.debug("Absolute exception found on term: " + StringUtilities.getGroupFromLastMatch());
                        matchFound = false; // turn mat back to false if exception found               
                    }
                }
            }
        }
        
        return matchFound;
    }
    
    private boolean checkAlternatives(NlpCriticConcept concept, String sentence, List<String> allAltsList) 
    {
        // check each alternate
        // AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        boolean matchedAlternatives     = false;
        String wordWindow               = null;
        String preConcept               = concept.getPreConcept();
        String postConcept              = concept.getPostConcept();
    
        if (StringUtils.isNotEmpty(preConcept) && doesMatchAlternatives(sentence, preConcept, postConcept, allAltsList) ) 
        {            
            // only do below if something in preConcept (which implies something in others) 
            // no absolute match was found and an alternate was matched. 
            matchedAlternatives = true;
            lastMatch = StringUtilities.getGroupFromLastMatch();
            
            // get window of words (varies in size depnding on condition)
            wordWindow = StringWindow.getCombinedWordWindow(sentence, lastMatch, 12, 12);               
        }
        // check special alternate (3rd)
        else 
        {  // we didn't match an alternate and no match was found, check the 3rd alternate
            String altConcept3 = concept.getAltCon3();
            
            if (StringUtils.isNotEmpty(altConcept3) && StringUtilities.doesRegExMatch(altConcept3, sentence)) 
            {
                // first try pre+conc                    
                // true if only a concept can be negated - i.e. heart size
                lastMatch           = StringUtilities.getGroupFromLastMatch();
                wordWindow          = StringWindow.getCombinedWordWindow(sentence, lastMatch, 12, 12);           
                matchedAlternatives = true; // an alternative (3rd special) was found 
            }
        }
        
        // exceptions to alternates
        boolean matchExceptionToAlternate = false;
        String altExc = concept.getAltExcept();

        if (StringUtils.isNotEmpty(altExc) && matchedAlternatives && StringUtilities.doesRegExMatch(altExc, wordWindow)) 
        {
            // alternate matched - either 2nd or 3rd special                   
            // true if an exception was matched above was matched
            matchExceptionToAlternate = true; 
        }
        
        // Report

        // Exceptions to exceptions
        String conceptName = concept.getConceptName();
        
        if ( conceptName != null && conceptName.equals("mrsa") && matchExceptionToAlternate && sentence.contains("screen is positive")) 
        { 
            // alt 3 found and exception found
            // if this exception then reverse exception
            matchExceptionToAlternate = false;
        }
        
        return (matchedAlternatives && ! matchExceptionToAlternate);
    }
    
    private boolean doubleCheckNegation(NlpCriticConcept concept, String sentence, String preString, String postString, List<String> contextList2) 
    {        
        // exceptions to negation
        String negExceptCon             = concept.getNegExceptCon();
        List<String> negExConcArray     = StringUtilities.splitStringToList(negExceptCon, splitExpr);
                
        String negExNeg                 = concept.getNegExNeg();
        List<String> negExNegationArray = StringUtilities.splitStringToList(negExNeg, splitExpr);
        boolean conceptNegated          = checkForNegationExceptions(concept, sentence, negExConcArray, negExNegationArray);        
        
        return conceptNegated;
    }
    
    private boolean checkForOtherNegations(String sentence, NlpCriticConcept concept, String smallWordWindow) 
    {
        boolean hasSpecialNegation  = false;
        String lowerNorm            = sentence.toLowerCase();
        String specialNegs          = concept.getSpecialNegs();
    
        if (specialNegs != null) 
        {
            List<String> specN  = StringUtilities.splitStringToList(specialNegs, splitExpr);
            specN               = padNegationMembers(specN);
            hasSpecialNegation  = checkForSpecialNegations(sentence, specN);            
        }
               
        String smallWindowNegs = concept.getSmallWindowNegs();
        
        if (! hasSpecialNegation && smallWindowNegs != null) 
        {
            List<String> smlN = StringUtilities.splitStringToList(smallWindowNegs, splitExpr);
            smlN = padNegationMembers(smlN);
            hasSpecialNegation = checkForSmallWindowNegations(smallWordWindow, smlN);            
        }
                
        String absoluteNegs = concept.getAbsoluteNegs();
        
        if (! hasSpecialNegation && absoluteNegs != null) 
        {
            List<String> absN = StringUtilities.splitStringToList(absoluteNegs, splitExpr);
            absN = padNegationMembers(absN);
            hasSpecialNegation = checkForAbsoluteNegations(lowerNorm, absN);            
        }
        
        // Special Rules for negation (attempt to negate)- global report criteria
        if (! hasSpecialNegation && concept.getConceptName().equals("mrsa")) 
        {
            hasSpecialNegation = checkForMRSANegations(lowerNorm);            
        }
        
        return hasSpecialNegation;
    }
    
    private boolean doesMatchAlternatives(String conThisSentence, String preConcept, String postConcept, List<String> allAltsArray) 
    {
        boolean matchedAlternatives = false;
        // count thru each alt concept from database (array length will
        // be 1,2,or3) check pre2 2post pre3, 3post,
    
        for (String curAlt : allAltsArray) 
        {
            // first try pre+conc
            String altWithPre = preConcept + curAlt;             
            matchedAlternatives = StringUtilities.doesRegExMatch(altWithPre, conThisSentence, 1);
            // mat true if the above was matched
            
            if (matchedAlternatives == false) 
            { 
                // if no match, try
                // conc=post
                String altPattern = curAlt + postConcept;
                // mat true if the above was matched
                matchedAlternatives = StringUtilities.doesRegExMatch(altPattern, conThisSentence, 1); 
            }
        }
        
        return matchedAlternatives;
    }
    
    //  CONTEXT FUNCTIONS CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC
    
    private boolean checkForContextOrNegationOrPseudo(List<String> commandList, NlpCriticConcept concept, String preString, String postString, String smallWindowPreString, String smallWindowPostString) 
    {
    	boolean conceptNegated = false;  // start out assuming the match is still valid
    	boolean previousNegationState = conceptNegated;
    	
    	if (commandList != null && StringUtils.isNotEmpty(preString) && StringUtils.isNotEmpty(postString)) 
    	{
    		for (String command : commandList) 
    		{
    			if (StringUtils.isNotEmpty(command)) 
    			{    				
    				NlpContextChecker contextChecker    = new NlpContextChecker(command, concept);
    				conceptNegated                      = contextChecker.checkContext(command, preString, postString, smallWindowPreString, smallWindowPostString, conceptNegated);    
    				
    				if( !previousNegationState && conceptNegated ) 
    				{   
    					log.debug("Found negation while checking for " + command);
    				} 
    				else if( previousNegationState && !conceptNegated ) 
    				{
    					log.debug("Found false or pseudo negation while checking for " + command);
    				}
    				
    				previousNegationState = conceptNegated;
    			}
    		}
    	}

    	return conceptNegated;
    }	                           
    
    private boolean checkForNegationExceptions(NlpCriticConcept concept, String conThisSentence, List<String> negExConcList, List<String> negExNegationList)
    {
        boolean conceptNegated = true;
        String negExConc = concept.getNegExceptCon();
        
        if (StringUtils.isNotEmpty(negExConc) && negExConcList != null && negExNegationList != null && conThisSentence != null) 
        {
            // count thru each concept/negexception pair
            for (String negExConcString : negExConcList) 
            {                             
                for (String negExNegationString : negExNegationList) 
                {
                    // count thru each negexception for each concept
                    if (StringUtils.isNotEmpty(negExConcString) && conThisSentence.contains(negExConcString) && StringUtils.isNotEmpty(negExNegationString) && conThisSentence.contains(negExNegationString)) 
                    {
                    	log.debug("Negation exception found: " + negExConcString + " - " + negExNegationString );
                        conceptNegated = false; // convert the negation from true to false
                    
                        break;
                    }
                }
            }
        }
        
        return conceptNegated;
    }
    
    private boolean checkForSpecialNegations(String conThisSentence, List<String> specN)
    {
        boolean conceptNegated = false;
        
        if (specN != null && conThisSentence != null)
        {
            for (String specNString : specN) 
            {
                // count thru each special negation for each concept
                if (StringUtils.isNotEmpty(specNString) && conThisSentence.contains(specNString)) 
                { 
                    // if in sentence, negate
                	log.debug("Special negation found: " + specNString);
                    conceptNegated = true; // convert the negation from false to true
                  
                    break;
                }
            }
        }

        return conceptNegated;
    }
    
    private boolean checkForSmallWindowNegations(String smallWordWindow, List<String> smlN)
    {
        boolean conceptNegated = false;
        
        if (smlN != null && smallWordWindow != null)
        {
            for (String smlNString : smlN) 
            {
                if (StringUtils.isNotEmpty(smlNString) && smallWordWindow.contains(smlNString)) 
                {
                    // if these in small word window, negate
                	log.debug("Small window negation found: " + smlNString);
                    conceptNegated = true; // convert the negation from false to true
                
                    break;
                }
            }
        }
        
        return conceptNegated;
    }
    
    private boolean checkForParagraphWindowInclusions(String paragraphWindow, List<String>paragraphList)
    {
        boolean paraExc = false;        
        
        for (String para : paragraphList) 
        { 
            // count thru each special negation for each concept
            if (StringUtils.isNotEmpty(para) && ! para.equals(" ") ) 
            { 
                // not null, empty, or blank                     
                if (StringUtilities.doesRegExMatch(para, paragraphWindow)) 
                { 
                    // This paragraph has at least one inclusion criteria.   
                	log.debug("Paragraph window inclusion found.");
                    paraExc = true;
                
                    break;
                }
            }
        }
        
        return paraExc;
    }
    
    private boolean checkForAbsoluteNegations(String lowerNorm, List<String> absN)
    {
        boolean conceptNegated = false;
        
        if (absN != null && lowerNorm != null)
        {
            // normalize lower-cased report        
            lowerNorm = StringNormalizer.fixHtmlFontPlusRemovePunctuation(lowerNorm);                   
        
            for (String absNString : absN) 
            {
                if (lowerNorm.contains(absNString)) 
                { 
                    // if these words anywhere in report, negate
                	log.debug("Absolute negation found: " + absNString);
                    conceptNegated = true; // convert the negation from false to true
                
                    break;
                }
            }
        }
        
        return conceptNegated;
    }
    
    private boolean checkForMRSANegations(String lowerNorm)
    {
        boolean conceptNegated  = false;
        lowerNorm               = StringNormalizer.fixHtmlFontPlusRemovePunctuation(lowerNorm);
        
        if (lowerNorm.contains(" epidermidis ") || lowerNorm.contains(" haemolyticus ") || lowerNorm.contains(" hominis ")) 
        {
            // report has other organisms
            if (lowerNorm.contains(" aureus ")) 
            {
                // report has aureus
                if (lowerNorm.contains(" not aureus")) 
                {
                    // i.e report has epidermatis + methicillin +
                    // aureus AND 'not aureus' --> negated                	
                    conceptNegated = true;
                }
            } 
            else 
            {
                // epidermi + meth but No aureus
                conceptNegated = true;
            }
        }   
        
        if (conceptNegated) 
        {
        	log.debug("MRSA negation found.");
        }
        
        return conceptNegated;
    }        
}
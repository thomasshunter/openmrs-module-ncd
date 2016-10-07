/**
 * 
 */
package org.openmrs.module.ncd.nlp;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.NlpCriticConcept;
import org.openmrs.module.ncd.database.NlpCriticContext;
import org.openmrs.module.ncd.database.NlpCriticContextType;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/**
 * @author jlbrown
 *
 * Checks an NLP context.
 */
public class NlpContextChecker {
	
	private static Log log = LogFactory.getLog(NlpContextChecker.class);
	
	private NlpCriticContextType contextType = null;
	private NlpCriticConcept concept = null;
	private List<NlpCriticContext> contextTerms = null;
	
	public NlpContextChecker(String command, NlpCriticConcept concept) {
		this.concept = concept; 
		this.contextType = NCDUtilities.getService().findContextTypeByName(command);
		this.contextTerms = getContextList();
	}
	
	public boolean checkContext(String command, String preString, String postString,
			String smallWindowPreString, String smallWindowPostString, boolean conceptNegated) {
		log.debug("Checking " + command + " context...");
		boolean contextNegatesConcept = conceptNegated;
		if (contextType == null || concept == null || contextTerms == null) {
			throw new IllegalStateException("Cannot use context before the concept, " +
					"contextType, or contextTerms have been loaded.");
		} else {			
			String stringToCheckForContext = getStringToCheckForContext(preString, postString, 
					smallWindowPreString, smallWindowPostString);
			log.debug("Checking context versus the following window: " + stringToCheckForContext);
			if (conceptNegated != contextType.isMatchNegation()) {
				// only do the check if:
				// a) the concept has not been negated and this context would negate the concept -or-
				// b) the concept has been negated and this context would un-negate the concept.
				contextNegatesConcept = 
					checkContextOrNegation(stringToCheckForContext);
				if (contextNegatesConcept == contextType.isMatchNegation()) {					
					log.debug("The " + command + " context has made the concept " + 
							getNegatedString(contextNegatesConcept));
				} else {
					log.debug("The " + command + " context was not found.");
				}
			} else {
				log.debug("No need to check the " + command + " context.  " +
						"The concept is already " + getNegatedString(contextNegatesConcept) + ".");
			}			
		}
		return contextNegatesConcept;
	}
	
	private String getNegatedString(boolean value) {
		return (value ? "negated" : "not negated");
	}
	
	private List<NlpCriticContext> getContextList() {      	            	    	
    	List<NlpCriticContext> contextList;
    	if (contextType.isUsingContextGroup()) {
    		String negationGroup = concept.getNegationGroup();
    		if (StringUtils.isEmpty(negationGroup.trim())) {
    			negationGroup = "standard";
    		}    		
    		contextList = NCDUtilities.getService().findContextByTypeAndGroup(contextType, negationGroup);
    	} else {
    		contextList = NCDUtilities.getService().findContextByType(contextType);
    	}
        	    
    	return contextList;
    }
	
	private String getStringToCheckForContext(String preString, String postString, 
			String smallWindowPreString, String smallWindowPostString) {
		String stringToCheckForContext = null;
		boolean usesSmallWindow = contextType.isUsingSmallWindow();				
		boolean usesPreString = contextType.isUsingPreString();		
		boolean usesPostString = contextType.isUsingPostString();
		if (usesPreString && usesPostString) {			
			stringToCheckForContext = 
				usesSmallWindow ? smallWindowPreString + smallWindowPostString : preString + postString;	    					
		} else if (usesPreString) {			
			stringToCheckForContext = usesSmallWindow ? smallWindowPreString : preString;
		} else if (usesPostString) {			
			stringToCheckForContext = usesSmallWindow ? smallWindowPostString: postString;
		}
		return stringToCheckForContext;
	}		
	
	private boolean checkContextOrNegation(String sentencePart) {
		boolean matchNegation = contextType.isMatchNegation();
    	boolean contextOrNegationFound = !matchNegation;
		if (StringUtils.isNotEmpty(sentencePart)) {		
			//log.debug("Checking " + contextTerms.size() + " terms.");
	    	for (NlpCriticContext contextTerm : contextTerms) {
	    		String contextString = contextTerm.getContextValue();
	    		//log.debug("Checking for " + contextString + ".");
	    		if (sentencePart.contains(contextString))
	    		{	    			
	    			log.debug("Context or negation found using the term: " + contextString + ".");
	    			contextOrNegationFound = matchNegation;
	    			break;
	    		}    	
	    	}
		}
    	return contextOrNegationFound;
    }
}

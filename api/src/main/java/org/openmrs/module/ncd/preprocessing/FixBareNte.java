/**
 * 
 */
package org.openmrs.module.ncd.preprocessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jlbrown
 *
 */
public class FixBareNte implements MessagePreProcessor {
	private static Log logger = LogFactory.getLog(FixBareNte.class);

	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.preprocessing.MessagePreProcessor#preProcessMessage(java.lang.String)
	 */
	public String preProcessMessage(String message) {
		Pattern bearNtePattern = Pattern.compile("^NTE$", Pattern.MULTILINE);
		Matcher bearNteMatcher = bearNtePattern.matcher(message);
		String transformedMessage = message;
		if (bearNteMatcher.find()){
			transformedMessage = bearNteMatcher.replaceAll("NTE\\|\\|");
			logger.warn("Replacing bare NTE with NTE||.");
		}
		return transformedMessage;
	}
}

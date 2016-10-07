/**
 * 
 */
package org.openmrs.module.ncd.preprocessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jlbrown
 *
 */
public class FixEmptyOBX2 implements MessagePreProcessor {

	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.preprocessing.MessagePreProcessor#preProcessMessage(java.lang.String)
	 */
	@Override
	public String preProcessMessage(String message) {
		StringBuffer transformedMessage = new StringBuffer();
		Pattern emptyObx2Pattern = Pattern.compile("^OBX\\|(\\d*?\\|)(\\|)", Pattern.MULTILINE);
		Matcher emptyObx2Matcher = emptyObx2Pattern.matcher(message);
		while(emptyObx2Matcher.find()) {
			String preObx2 = emptyObx2Matcher.group(1).replace("$", "\\$");
			String postObx2 = emptyObx2Matcher.group(2).replace("$", "\\$");
			emptyObx2Matcher.appendReplacement(transformedMessage,
					"OBX|" + preObx2 + "ST" + postObx2);			
		}
		emptyObx2Matcher.appendTail(transformedMessage);
		return transformedMessage.toString();
	}

}

/**
 * 
 */
package org.openmrs.module.ncd.preprocessing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.model.MessageHeader;

/**
 * @author jlbrown
 *
 */
public class MPQGenerator implements MessagePreProcessor {

	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.preprocessing.MessagePreProcessor#preProcessMessage(java.lang.String)
	 */
	public String preProcessMessage(String message) {
		if (message.length() < 4) {
			return message;
		}
		String fieldSeparator = MessageHeader.getFieldSeparator(message);
		Pattern zvxPattern = Pattern.compile("^ZVX\\" + fieldSeparator, Pattern.MULTILINE);
		Matcher zvxMatcher = zvxPattern.matcher(message);		
		if (! zvxMatcher.find()) {			
			// we don't have a ZVX so generate an MPQ and add a ZVX segment.
						
			StringBuilder transformedMessage = new StringBuilder();
			String createdZvx = createZvxSegment(fieldSeparator);			
			transformedMessage.append(StringUtils.substringBefore(message, "\r"));
			transformedMessage.append("\r");
			transformedMessage.append(createdZvx);
			transformedMessage.append(StringUtils.substringAfter(message, "\r"));
			return transformedMessage.toString();
		} else {
			return message;
		}
	}

	private String createZvxSegment(String fieldSeparator) {
		// Using the below as the template except leaving the ZVX-2 and ZVX-3 blank.
		// ZVX||K|PF|150629425|16-Nov-08|04:46 PM
		StringBuilder generatedZvx = new StringBuilder();
		generatedZvx.append("ZVX");
		// Add 4 field separators after the ZVX segment name.
		for (int i = 1; i <= 3; i++) {
			generatedZvx.append(fieldSeparator);			
		}
		// Prepend NCD to the MPQ #
		generatedZvx.append("NCD");
		generatedZvx.append(fieldSeparator);
		// Generate the MPQ #
		Date now = new Date();
		generatedZvx.append(now.getTime());		
		generatedZvx.append(fieldSeparator);
		// Put the date in ZVX-5
		SimpleDateFormat dateFormatter = new SimpleDateFormat("d-MMM-yy");			
		generatedZvx.append(dateFormatter.format(now));
		generatedZvx.append(fieldSeparator);
		// Put the time in ZVX-6
		SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
		generatedZvx.append(timeFormatter.format(now));
		generatedZvx.append("\\r");
		return generatedZvx.toString();
	}
}

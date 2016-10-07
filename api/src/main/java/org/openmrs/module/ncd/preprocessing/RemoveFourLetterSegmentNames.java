package org.openmrs.module.ncd.preprocessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.model.MessageHeader;

public class RemoveFourLetterSegmentNames implements MessagePreProcessor {

	private static Log logger = LogFactory.getLog(RemoveFourLetterSegmentNames.class);
	
	public String preProcessMessage(String message) {
		if (message.length() < 4) {
			return message;
		}
		String fieldSep = "\\" + MessageHeader.getFieldSeparator(message);		
		Pattern pattern = Pattern.compile("^SKIP" + fieldSep + "(.*)$", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(message);
		StringBuffer transformedMessage = new StringBuffer();		
		while (matcher.find()) {
			//Find out if we have an NTE or OBX.
			String segmentMinusName = matcher.group(1).replace("\\","\\\\").replace("$", "\\$");						
			String[] fields = segmentMinusName.split(fieldSep);
			if (fields.length < 5) {
				String replacement = "NTE" + fieldSep + segmentMinusName;				
				matcher.appendReplacement(transformedMessage, replacement);
				logger.warn("Changing a segment from SKIP to NTE.");
			} else {	
				String replacement = "OBX" + fieldSep + segmentMinusName;				
				matcher.appendReplacement(transformedMessage, replacement);
				logger.warn("Changing a segment from SKIP to OBX.");
			}
		}			
		matcher.appendTail(transformedMessage);
		return transformedMessage.toString();
	}

}

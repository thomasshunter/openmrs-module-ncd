/**
 * 
 */
package org.openmrs.module.ncd.preprocessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Transforms carriage return characters in message segments (other than the segment terminator)
 * to another string (\.br\) to work around HAPI's inability to parse such segments.
 * 
 * @author jlbrown
 */
public class TransformEmbeddedCarriageReturns implements MessagePreProcessor 
{	
	private static Log logger              = LogFactory.getLog(TransformEmbeddedCarriageReturns.class);
	public static final String LINE_BREAK  = "\\.br\\";
	
	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.preprocessing.MessagePreProcessor#preProcessMessage(java.lang.String)
	 */
	public String preProcessMessage(String message) 
	{
		String encoded = encode(message);
	
		if (!encoded.equals(message)) 
		{
			logger.warn("replaced at least one embedded carriage return in message.");
		}
		
		return encoded;
	}

	/**
	 * Transform a message, possibly with embedded carriage returns, to one without embedded carriage returns
	 * that can be parsed by HAPI, and returned to its original state via decode().
	 * 
	 * @param message
	 * @return
	 */
	public static String encode(String message) 
	{
		Pattern embeddedCRPattern = Pattern.compile("\\r^(?![A-Z0-9]{3}\\|)", Pattern.MULTILINE);
	
		return embeddedCRPattern.matcher(message).replaceAll(Matcher.quoteReplacement(LINE_BREAK));
	}
	
	/**
	 * Transform a message, or part of one, back to its state as it was passed to encode().
	 * 
	 * @param message
	 * @return
	 */
	public static String decode(String message) 
	{
		return message.replace(LINE_BREAK, "\r");
	}
}

/**
 * 
 */
package org.openmrs.module.ncd.preprocessing;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.model.MessageHeader;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/**
 * @author John Brown
 *
 */
public class MakeValidMSH9 implements MessagePreProcessor 
{		
	private static Log logger = LogFactory.getLog(MakeValidMSH9.class);
	private static final String META_SEPARATOR = "_";
	private static final String BACKSLASH = "\\";
	private static final String DOUBLE_BACKSLASH = "\\\\";
	private static final String EMPTY_STRING = "";	
	private static final String DOLLAR_SIGN = "$";
	private static final String ESCAPED_DOLLAR_SIGN="\\$";
	
	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.preprocessing.MessagePreprocessor#preprocessMessage(java.lang.String)
	 */
	public String preProcessMessage(String message) 
	{
		if (message.length() < 5) 
		{
			return message;
		}
		
		String componentSeparator = MessageHeader.getComponentSeparator(message);
		Pattern pattern = Pattern.compile("^MSH\\|(.*?\\|.*?\\|.*?\\|.*?\\|.*?\\|.*?\\|.*?\\|)(.*?)\\|", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(message);
		boolean transformFound = false;
		String transformedMessage = message;
		
		if (matcher.find()) 
		{
			String originalMsh9 = matcher.group(2);
			Map<String,String> msh9TransformMap = getMSH9TransformMap();						
			Iterator<String> msh9TransformIter = msh9TransformMap.keySet().iterator();
			// If it's not the component separator, remove any backslashes
			// and then replace the component separator with our meta-separator
			// the underscore ("_") character.
			String newMsh9 = originalMsh9;
		
			if (componentSeparator != BACKSLASH) 
			{
				newMsh9 = newMsh9.replace(BACKSLASH, EMPTY_STRING);
			}
			
			newMsh9 = newMsh9.replace(componentSeparator, META_SEPARATOR);
			
			while (! transformFound && msh9TransformIter.hasNext()) 
			{
				String msgTypeToTransformFrom = msh9TransformIter.next();
				Matcher msgTypeMatcher = Pattern.compile(msgTypeToTransformFrom).matcher(newMsh9);
				StringBuffer sb = new StringBuffer();
			
				if (msgTypeToTransformFrom.contains(META_SEPARATOR)) 
				{
					// We have a complete message type, so check for partial matches.
				    if (msgTypeMatcher.find()) 
				    {					
						msgTypeMatcher.appendReplacement(sb, msh9TransformMap.get(msgTypeToTransformFrom));
						msgTypeMatcher.appendTail(sb);
						newMsh9 = sb.toString();						
						transformFound = true;						
					}
				} 
				else 
				{
					// We have just the message type code, so check for a complete match.
					if (msgTypeMatcher.matches()) 
					{
						newMsh9 = msh9TransformMap.get(msgTypeToTransformFrom);						
						transformFound = true;						
					}
				}
				
				if (transformFound) 
				{
					// If we found a transform, change the underscore back to the 
					// designated component separator.  Then send the log message.
					newMsh9 = newMsh9.replace(META_SEPARATOR, componentSeparator);
					logger.warn("Transforming from message type " + originalMsh9 + " to " + newMsh9 + ".");
				}
			}			
			
			if (transformFound && newMsh9 != null) 
			{
				String group1 = matcher.group(1);
				// we need to make sure any "\" and "$" in the first 8 MSH fields 
				// get escaped before doing the replace.
				group1 = group1.replace(BACKSLASH, DOUBLE_BACKSLASH);
				group1 = group1.replace(DOLLAR_SIGN, ESCAPED_DOLLAR_SIGN);
				transformedMessage = matcher.replaceFirst("MSH|" + group1 + newMsh9 + "|");				
			}							
		}	
		
		return transformedMessage;
	}
	
	private Map<String,String> getMSH9TransformMap() 
	{
		Map<String,String> msh9TransformMap = new LinkedHashMap<String,String>();
		//TODO get the transform map from NCDUtilites
		String rawMap = NCDUtilities.getMessageTypeTransforms();
		String[] mapEntries = rawMap.split("\\,");
		
		for (String mapEntry : mapEntries) 
		{
			String[] entryParts = mapEntry.split("\\=");
		
			if (entryParts.length >= 2) 
			{
				msh9TransformMap.put(entryParts[0], entryParts[1]);
			}
		}
		
		return msh9TransformMap;
	}		
}
/**
 * 
 */
package org.openmrs.module.ncd.preprocessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/**
 * @author jlbrown
 *
 */
public class TransformOBX2DataType implements MessagePreProcessor {

	private static final String TRANSFORM_SEPARATOR = ",";
	private static final String DATATYPE_SEPARATOR = "=";
	private static Log logger = LogFactory.getLog(TransformOBX2DataType.class);
	
	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.preprocessing.MessagePreprocessor#preprocessMessage(java.lang.String)
	 */
	public String preProcessMessage(String message) {
		String transformedMessage = message;
		String transforms = NCDUtilities.getDataTypeTransforms();		
		if (transforms != null) {					
			String[] transformArray = transforms.split(TRANSFORM_SEPARATOR);
			
			for (String transform : transformArray) {
				String originalDataType = StringUtils.substringBefore(transform, DATATYPE_SEPARATOR);
				String newDataType = StringUtils.substringAfter(transform, DATATYPE_SEPARATOR);
				Pattern pattern = Pattern.compile("^OBX\\|(.*?\\|)" + originalDataType + "\\|", Pattern.MULTILINE);
				Matcher matcher = pattern.matcher(transformedMessage);			
				if (matcher.find()) {
					String group1 = matcher.group(1);
					// we need to make sure any "\" in the first 8 MSH fields get escaped before doing the replace.
					group1= group1.replace("\\", "\\\\");
					transformedMessage = matcher.replaceAll("OBX|" + group1 + newDataType + "|");
					logger.warn("Transforming all OBX-2 " + originalDataType + 
							" data types to the " + newDataType + " data type.");
				}
			}
		}
		return transformedMessage;
	}

}

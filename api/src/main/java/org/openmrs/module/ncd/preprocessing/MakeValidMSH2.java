/**
 * 
 */
package org.openmrs.module.ncd.preprocessing;

import org.openmrs.module.ncd.model.MessageHeader;

/**
 * @author jlbrown
 *
 */
public class MakeValidMSH2 implements MessagePreProcessor {

	private static final String segmentSeparator = "\\r";
	private static final int mshSegmentIndex = 0;
	private static final int msh2FieldIndex = 1;
	private static final String defaultMsh2 = "^~\\&";
	private static final int minMsh2Length = 4;
	private static final int msh2StartIndex = 4;
	
	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.preprocessing.MessagePreProcessor#preProcessMessage(java.lang.String)
	 */
	public String preProcessMessage(String message) 
	{
		String[] segments = message.split(segmentSeparator);
		String fieldSeparator = MessageHeader.getFieldSeparator(message);
		StringBuilder transformedMessage = new StringBuilder(message);
	
		if (segments != null && segments[mshSegmentIndex] != null) 
		{
			String[] mshFields = segments[mshSegmentIndex].split("\\" + fieldSeparator);
			String msh2 = mshFields[msh2FieldIndex];	
			int msh2Length = msh2.length();
		
			if (msh2Length < minMsh2Length) 
			{
				String msh2StuffToAdd = defaultMsh2.substring(msh2Length);
				msh2 = msh2.concat(msh2StuffToAdd);
				transformedMessage.replace(msh2StartIndex, msh2StartIndex + msh2Length, msh2);
			}
		}
		
		return transformedMessage.toString();
	}

}

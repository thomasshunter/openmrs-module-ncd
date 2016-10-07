/**
 * 
 */
package org.openmrs.module.ncd.preprocessing;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.model.MessageHeader;

/**
 * @author jlbrown
 *
 */
public class FixEmptyMSH12 implements MessagePreProcessor {

	private static final String segmentSeparator = "\\r";
	private static final int mshSegmentIndex = 0;
	private static final int msh12FieldIndex = 11;
	private static final String defaultHL7Version = "2.5";
	
	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.preprocessing.MessagePreProcessor#preProcessMessage(java.lang.String)
	 */
	public String preProcessMessage(String message) {
		String[] segments = message.split(segmentSeparator);
		String fieldSeparator = MessageHeader.getFieldSeparator(message);
		StringBuilder transformedMessage = new StringBuilder(message);
		if (segments != null && segments[mshSegmentIndex] != null) {
			String[] mshFields = segments[mshSegmentIndex].split("\\" + fieldSeparator, 13);
			String msh12 = null;
			if (mshFields.length >= 12) {
				msh12 = mshFields[msh12FieldIndex];						
				if (StringUtils.isEmpty(msh12)) {
					int msh12StringIndex = getMsh12StringIndex(message, fieldSeparator);				
					transformedMessage.replace(msh12StringIndex,msh12StringIndex + 1, defaultHL7Version + fieldSeparator);
				}
			} else {
				// We don't have 12 fields, so let's extend the MSH until we do.
				// First, find out how much we need to extend the MSH.
				String mshSegment = segments[mshSegmentIndex];
				int fieldSepCount = 0;
				int fromIndex = mshSegment.indexOf(fieldSeparator);
				while (fromIndex != -1) {
					fieldSepCount++;
					fromIndex = mshSegment.indexOf(fieldSeparator, fromIndex + 1);
				}
				
				// Second, create the extension string.
				String toConcat = "";
				for (int newFieldSepNum = fieldSepCount + 1; newFieldSepNum <= 11; newFieldSepNum++) {
					toConcat = toConcat.concat(fieldSeparator);					
				}
				toConcat = toConcat.concat(defaultHL7Version);
				toConcat = toConcat.concat(fieldSeparator);
				
				// Finally, replace the MSH with the MSH + the extension string.
				message = message.replace(mshSegment, mshSegment + toConcat);
				transformedMessage = new StringBuilder(message);
			}
		}
		
		return transformedMessage.toString();
	}
	
	private int getMsh12StringIndex(String mshSegment, String fieldSeparator) {
		int fromIndex = 0;
		// Find the 12th segment separator.
		for (int i = 1; i <= 12; i++) {			
			fromIndex = mshSegment.indexOf(fieldSeparator, fromIndex + 1);
		}
		return fromIndex;
	}

}

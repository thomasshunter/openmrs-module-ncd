/**
 * 
 */
package org.openmrs.module.ncd.model;

import org.w3c.dom.Node;

/**
 * @author jlbrown
 *
 */
public abstract class HL7NameHandler {
	public enum NamePart {
		ID,
		FAMILY_NAME,
		GIVEN_NAME,
		MIDDLE_NAME,
		SUFFIX_NAME,
		PREFIX_NAME,
		DEGREE_NAME
	};
	
	private static final String NO_VALUE = "";
	
	abstract String getIdentifier(Node node);
	abstract String getFamilyName(Node node);
	abstract String getGivenName(Node node);
	abstract String getMiddleName(Node node);
	abstract String getSuffixName(Node node);
	abstract String getPrefixName(Node node);
	abstract String getDegreeName(Node node);
	
	public static String useNameHandler(HL7NameHandler nameHandler, Node node, NamePart part) {
		String retVal = NO_VALUE;
		if (nameHandler != null && node != null) {
			switch(part) {
    		case ID:
    			retVal = nameHandler.getIdentifier(node);
    			break;
    		case FAMILY_NAME:
    			retVal = nameHandler.getFamilyName(node);
    			break;
    		case GIVEN_NAME:
    			retVal = nameHandler.getGivenName(node);
    			break;
    		case MIDDLE_NAME:
    			retVal = nameHandler.getMiddleName(node);
    			break;
    		case SUFFIX_NAME:    		
    			retVal = nameHandler.getSuffixName(node);
    			break;    		
			case PREFIX_NAME:
				retVal = nameHandler.getPrefixName(node);
    			break;
			case DEGREE_NAME:
				retVal = nameHandler.getDegreeName(node);
    			break;
			}
		}
		return retVal;
	}
}

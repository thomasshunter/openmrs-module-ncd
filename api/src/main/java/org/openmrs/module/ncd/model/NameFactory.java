/**
 * 
 */
package org.openmrs.module.ncd.model;

import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Node;

/**
 * @author jlbrown
 *
 */
public class NameFactory {
	
	private static final String COMPOSITE_NAME_EXPRESSION = "./*CN.1";
	private static final String PERSON_NAME_EXPRESSION = "./*PN.1";
	private static final String EXTENDED_COMPOSITE_NAME_EXPRESSION = "./*XCN.1";
	private static final String EXTENDED_PERSON_NAME_EXPRESSION = "./*XPN.1";	
	private static final String UNKNOWN_NAME_EXPRESSION = "./*UNKNOWN.1";
	
	public static HL7NameHandler getNameHandler(Node node) {		
		HL7NameHandler retVal = null;		
		Node xcnNode = XmlUtilities.findHL7Part(EXTENDED_COMPOSITE_NAME_EXPRESSION, node);
		Node xpnNode = XmlUtilities.findHL7Part(EXTENDED_PERSON_NAME_EXPRESSION, node);
		Node cnNode = XmlUtilities.findHL7Part(COMPOSITE_NAME_EXPRESSION, node);
		Node pnNode = XmlUtilities.findHL7Part(PERSON_NAME_EXPRESSION, node);
		Node unkNode = XmlUtilities.findHL7Part(UNKNOWN_NAME_EXPRESSION, node);
		if (xcnNode != null) {
			retVal = new ExtendedCompositeName();
		} else if (xpnNode != null) {
			retVal = new ExtendedPersonName();
		} else if (cnNode != null) {
			retVal = new CompositeName();
		} else if (pnNode != null) {
			retVal = new PersonName();
		} else if (unkNode != null) {
			retVal = new UnknownName();
		}
		
		return retVal;
	}
}

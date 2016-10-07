/**
 * 
 */
package org.openmrs.module.ncd.model;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Node;

/**
 * @author jlbrown
 *
 */
public class ExtendedCompositeName extends HL7NameHandler {
	
	private static final String IDENTIFIER_EXPRESSION;
	private static final String FAMILY_NAME_EXPRESSION;
	private static final String FAMILY_NAME_ALT_EXPRESSION;
	private static final String GIVEN_NAME_EXPRESSION;
	private static final String MIDDLE_NAME_EXPRESSION;
	private static final String SUFFIX_NAME_EXPRESSION;
	private static final String PREFIX_NAME_EXPRESSION;
	private static final String DEGREE_NAME_EXPRESSION;	
	
	static {
		IDENTIFIER_EXPRESSION = "./XCN.1/text()";
		FAMILY_NAME_EXPRESSION = "./XCN.2/FN.1/text()";
		FAMILY_NAME_ALT_EXPRESSION = "./XCN.2/text()";
		GIVEN_NAME_EXPRESSION = "./XCN.3/text()";
		MIDDLE_NAME_EXPRESSION = "./XCN.4/text()";
		SUFFIX_NAME_EXPRESSION = "./XCN.5/text()";
		PREFIX_NAME_EXPRESSION = "./XCN.6/text()";
		DEGREE_NAME_EXPRESSION = "./XCN.7/text()";
	}
	
	
	public ExtendedCompositeName() {		
	}
	
	public String getIdentifier(Node node) {
		return XmlUtilities.findFieldValue(IDENTIFIER_EXPRESSION, node);
	}
	
	public String getFamilyName(Node node) {
		String familyName = XmlUtilities.findFieldValue(FAMILY_NAME_EXPRESSION, node);
		if (StringUtils.isEmpty(familyName)) {
			familyName = XmlUtilities.findFieldValue(FAMILY_NAME_ALT_EXPRESSION, node);
		}
		return familyName;
	}
	
	public String getGivenName(Node node) {
		return XmlUtilities.findFieldValue(GIVEN_NAME_EXPRESSION, node);
	}
	
	public String getMiddleName(Node node) {
		return XmlUtilities.findFieldValue(MIDDLE_NAME_EXPRESSION, node);
	}
	
	public String getSuffixName(Node node) {
		return XmlUtilities.findFieldValue(SUFFIX_NAME_EXPRESSION, node);
	}
	
	public String getPrefixName(Node node) {
		return XmlUtilities.findFieldValue(PREFIX_NAME_EXPRESSION, node);
	}
	
	public String getDegreeName(Node node) {
		return XmlUtilities.findFieldValue(DEGREE_NAME_EXPRESSION, node);
	}
}

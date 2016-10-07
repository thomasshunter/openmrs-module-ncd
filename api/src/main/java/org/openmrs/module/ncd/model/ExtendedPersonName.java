package org.openmrs.module.ncd.model;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Node;

public class ExtendedPersonName extends HL7NameHandler {
	
	private static final String FAMILY_NAME_EXPRESSION;
	private static final String FAMILY_NAME_ALT_EXPRESSION;
	private static final String GIVEN_NAME_EXPRESSION;
	private static final String MIDDLE_NAME_EXPRESSION;
	private static final String SUFFIX_NAME_EXPRESSION;
	private static final String PREFIX_NAME_EXPRESSION;
	private static final String DEGREE_NAME_EXPRESSION;	
	private static final String EMPTY_VALUE="";
	
	static {		
		FAMILY_NAME_EXPRESSION = "./XPN.1/FN.1/text()";
		FAMILY_NAME_ALT_EXPRESSION = "./XPN.1/text()";
		GIVEN_NAME_EXPRESSION = "./XPN.2/text()";
		MIDDLE_NAME_EXPRESSION = "./XPN.3/text()";
		SUFFIX_NAME_EXPRESSION = "./XPN.4/text()";
		PREFIX_NAME_EXPRESSION = "./XPN.5/text()";
		DEGREE_NAME_EXPRESSION = "./XPN.6/text()";
	}
	
	
	public ExtendedPersonName() {		
	}
	
	public String getIdentifier(Node node) {
		return EMPTY_VALUE;
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

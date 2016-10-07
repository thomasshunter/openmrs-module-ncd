package org.openmrs.module.ncd.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UnknownName extends HL7NameHandler {
				
	private static final String EMPTY_VALUE = "";
	private static final NamePart[] partArray = 
		new NamePart[] {NamePart.ID, NamePart.FAMILY_NAME, NamePart.GIVEN_NAME, NamePart.MIDDLE_NAME, 
					   NamePart.SUFFIX_NAME, NamePart.PREFIX_NAME, NamePart.DEGREE_NAME};
	
	private Map<NamePart, String> unknownItemMap = null;
		
		
	
	public UnknownName() {		
	}
	
	public String getIdentifier(Node node) {
		createItemMap(node);
		String id = unknownItemMap.get(NamePart.ID);
		return (StringUtils.isNotEmpty(id) ? id : EMPTY_VALUE);
	}
	
	public String getFamilyName(Node node) {
		createItemMap(node);
		String familyName = unknownItemMap.get(NamePart.FAMILY_NAME);
		return (StringUtils.isNotEmpty(familyName) ? familyName : EMPTY_VALUE);
	}
	
	public String getGivenName(Node node) {
		createItemMap(node);
		String givenName = unknownItemMap.get(NamePart.GIVEN_NAME);
		return (StringUtils.isNotEmpty(givenName) ? givenName : EMPTY_VALUE);
	}
	
	public String getMiddleName(Node node) {
		createItemMap(node);
		String middleName = unknownItemMap.get(NamePart.MIDDLE_NAME);
		return (StringUtils.isNotEmpty(middleName) ? middleName : EMPTY_VALUE);
	}
	
	public String getSuffixName(Node node) {
		createItemMap(node);
		String suffixName = unknownItemMap.get(NamePart.SUFFIX_NAME);
		return (StringUtils.isNotEmpty(suffixName) ? suffixName : EMPTY_VALUE);
	}
	
	public String getPrefixName(Node node) {
		createItemMap(node);
		String prefixName = unknownItemMap.get(NamePart.PREFIX_NAME);
		return (StringUtils.isNotEmpty(prefixName) ? prefixName : EMPTY_VALUE);
	}
	
	public String getDegreeName(Node node) {
		createItemMap(node);
		String degreeName = unknownItemMap.get(NamePart.DEGREE_NAME);
		return (StringUtils.isNotEmpty(degreeName) ? degreeName : EMPTY_VALUE);
	}

	private void createItemMap(Node node) {
		if (unknownItemMap == null) {
			unknownItemMap = new HashMap<NamePart, String>();
			NodeList childNodes = node.getChildNodes();
			int partArrayIdx = 0;
			int childIdx = 0;
			while (partArrayIdx < partArray.length && childIdx < childNodes.getLength()) {
				Node childNode = childNodes.item(childIdx);
				if (childNode != null) {
					String childNodeValue = childNode.getTextContent();
					if (partArray[partArrayIdx] == NamePart.ID) {
						// assumption - an ID needs to have at least 1 digit
						if (! hasDigit(childNodeValue)) {	
							// If we don't have a digit, assume the first item is a not an ID and
							// move to the next item in the partArray.
							partArrayIdx++;
						}						
					}
					if (StringUtils.isNotEmpty(childNodeValue)) {
						unknownItemMap.put(partArray[partArrayIdx], childNodeValue);
						partArrayIdx++;
					}
					childIdx++;
				}
			}
		}
	}
	
	private static boolean hasDigit(String value) {
		Pattern digitPattern = Pattern.compile("\\d+$");
		Matcher digitMatcher = digitPattern.matcher(value);
		return digitMatcher.find();
	}
}

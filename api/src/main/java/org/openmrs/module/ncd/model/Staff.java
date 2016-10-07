/**
 * 
 */
package org.openmrs.module.ncd.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.model.HL7NameHandler.NamePart;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author jlbrown
 *
 */
public class Staff {
	private static Log log = LogFactory.getLog(Staff.class);
	
	private static List<String> STAFF_ID_EXPRESSIONS;
	private final static String STAFF_NAME_EXPRESSION;
	private final static String STAFF_PHONE_EXPRESSION;
	private final static String STAFF_PHONE_ALT_EXPRESSION;
	private final static String STAFF_STREET_EXPRESSION;
	private final static String STAFF_STREET_ALT_EXPRESSION;
	private final static String STAFF_CITY_EXPRESSION;
	private final static String STAFF_CITY_ALT_EXPRESSION;
	private final static String STAFF_STATE_EXPRESSION;
	private final static String STAFF_STATE_ALT_EXPRESSION;
	private final static String STAFF_ZIP_EXPRESSION;
	private final static String STAFF_ZIP_ALT_EXPRESSION;
	private final static String STAFF_COUNTY_EXPRESSION;
	private final static String STAFF_COUNTY_ALT_EXPRESSION;
	private final static String STAFF_COUNTRY_EXPRESSION;
	private final static String STAFF_COUNTRY_ALT_EXPRESSION;
	
	private final static String STAFF_TAG_NAME = "STF";
	
	private Node stfNode;
	private HL7NameHandler nameHandler;
    private Node staffNameNode;
	
	static {
		STAFF_ID_EXPRESSIONS = new ArrayList<String>();		
		STAFF_ID_EXPRESSIONS.add("./STF.2/CE.1/text()");
		STAFF_ID_EXPRESSIONS.add("./STF.2/CX.1/text()");
		STAFF_ID_EXPRESSIONS.add("./STF.2/text()");
		STAFF_NAME_EXPRESSION = "./STF.3";
		STAFF_PHONE_EXPRESSION = "./STF.10/XTN.1/text()";
		STAFF_PHONE_ALT_EXPRESSION = "./STF.10/text()";
		STAFF_STREET_EXPRESSION = "./STF.11/AD.1/text()";
		STAFF_STREET_ALT_EXPRESSION = "./STF.11/XAD.1/SAD.1/text()";
		STAFF_CITY_EXPRESSION = "./STF.11/AD.3/text()";
		STAFF_CITY_ALT_EXPRESSION = "./STF.11/XAD.3/text()";
		STAFF_STATE_EXPRESSION = "./STF.11/AD.4/text()";
		STAFF_STATE_ALT_EXPRESSION = "./STF.11/XAD.4/text()";
		STAFF_ZIP_EXPRESSION = "./STF.11/AD.5/text()";
		STAFF_ZIP_ALT_EXPRESSION = "./STF.11/XAD.5/text()";
		STAFF_COUNTY_EXPRESSION = "./STF.11/AD.9/text()";
		STAFF_COUNTY_ALT_EXPRESSION = "./STF.11/XAD.9/text()";
		STAFF_COUNTRY_EXPRESSION = "./STF.11/AD.6/text()";
		STAFF_COUNTRY_ALT_EXPRESSION = "./STF.11/XAD.6/text()";
	}
	
	public static Map<String, Staff> getStaffMap(Node msgSegment) {
		NodeList stfNodes = msgSegment.getOwnerDocument().getElementsByTagName(STAFF_TAG_NAME);
		Map<String, Staff> retVal = new HashMap<String, Staff>();
		for (int nodeIdx = 0; nodeIdx < stfNodes.getLength(); nodeIdx++) {
			Node curStfNode = stfNodes.item(nodeIdx);
			Staff staffItem = new Staff(curStfNode);
			String staffId = staffItem.getIdentifier();
			retVal.put(staffId, staffItem);
			log.debug("Added Staff ID - " + staffId);
		}
		return retVal;
	}
	
	public Staff(Node stfSegment) {
		stfNode = stfSegment;
		nameHandler = NameFactory.getNameHandler(stfNode);
		staffNameNode = XmlUtilities.findHL7Part(STAFF_NAME_EXPRESSION, stfNode);
	}
	
	public String getIdentifier() {
		String id = null;
		Iterator<String> idExpressionIterator = STAFF_ID_EXPRESSIONS.iterator();
		while (idExpressionIterator.hasNext() && StringUtils.isEmpty(id)) {
			id = XmlUtilities.findFieldValue(idExpressionIterator.next(), stfNode);
		}				
		return id;
	}
	
	public String getFamilyName() {
		return HL7NameHandler.useNameHandler(nameHandler, staffNameNode, NamePart.FAMILY_NAME);
	}
	
	public String getGivenName() {
		return HL7NameHandler.useNameHandler(nameHandler, staffNameNode, NamePart.GIVEN_NAME);
	}
	
	public String getMiddleName() {
		return HL7NameHandler.useNameHandler(nameHandler, staffNameNode, NamePart.MIDDLE_NAME);
	}
	
	public String getSuffixName() {
		return HL7NameHandler.useNameHandler(nameHandler, staffNameNode, NamePart.SUFFIX_NAME);
	}
	
	public String getName() {
		String lastName = getFamilyName();
        String firstName = getGivenName();
        String middleName = getMiddleName();
        String suffixName = getSuffixName();
        StringBuilder nameBuilder = new StringBuilder();
        if (StringUtils.isNotEmpty(lastName) || 
        	StringUtils.isNotEmpty(firstName))         	
        {
            nameBuilder.append(lastName);
            nameBuilder.append(", ");
            nameBuilder.append(firstName);
            if (StringUtils.isNotEmpty(middleName)) {
            	nameBuilder.append(" ");
            	nameBuilder.append(middleName);
            }
            if (StringUtils.isNotEmpty(suffixName)) {
            	nameBuilder.append(" ");
            	nameBuilder.append(suffixName);
            }
        }
        return nameBuilder.toString();
	}
	
	public String getPhoneNumber() {
		String phoneNumber = XmlUtilities.findFieldValue(STAFF_PHONE_EXPRESSION, stfNode);
		if (StringUtils.isEmpty(phoneNumber)) {
			phoneNumber = XmlUtilities.findFieldValue(STAFF_PHONE_ALT_EXPRESSION, stfNode);
		}
		return phoneNumber;
	}
	
	public String getStreet() {
		String street = XmlUtilities.findFieldValue(STAFF_STREET_EXPRESSION, stfNode);
		if (StringUtils.isEmpty(street)) {
			street = XmlUtilities.findFieldValue(STAFF_STREET_ALT_EXPRESSION, stfNode);
		}
		return street;
	}
	
	public String getCity() {
		String city = XmlUtilities.findFieldValue(STAFF_CITY_EXPRESSION, stfNode);
		if (StringUtils.isEmpty(city)) {
			city = XmlUtilities.findFieldValue(STAFF_CITY_ALT_EXPRESSION, stfNode);
		}
		return city;
	}
	
	public String getState() {
		String state = XmlUtilities.findFieldValue(STAFF_STATE_EXPRESSION, stfNode);
		if (StringUtils.isEmpty(state)) {
			state = XmlUtilities.findFieldValue(STAFF_STATE_ALT_EXPRESSION, stfNode);
		}
		return state;
	}
	
	public String getZip() {
		String zip = XmlUtilities.findFieldValue(STAFF_ZIP_EXPRESSION, stfNode);
		if (StringUtils.isEmpty(zip)) {
			zip = XmlUtilities.findFieldValue(STAFF_ZIP_ALT_EXPRESSION, stfNode);
		}
		return zip;
	}
	
	public String getCounty() {
		String county = XmlUtilities.findFieldValue(STAFF_COUNTY_EXPRESSION, stfNode);
		if (StringUtils.isEmpty(county)) {
			county = XmlUtilities.findFieldValue(STAFF_COUNTY_ALT_EXPRESSION, stfNode);
		}
		return county;
	}
	
	public String getCountry() {
		String country = XmlUtilities.findFieldValue(STAFF_COUNTRY_EXPRESSION, stfNode);
		if (StringUtils.isEmpty(country)) {
			country = XmlUtilities.findFieldValue(STAFF_COUNTRY_ALT_EXPRESSION, stfNode);
		}
		return country;
	}
}

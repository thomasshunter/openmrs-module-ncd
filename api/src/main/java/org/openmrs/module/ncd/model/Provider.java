/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.model.HL7NameHandler.NamePart;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class allows easy retrieval of Provider data.
 * @author jlbrown
 *
 */
public class Provider
{
    private Node pv1Node = null;
    private Node zlrNode = null;
    private HL7NameHandler nameHandler;
    private Node attendingDoctorNode;
    private Node referringDoctorNode;
    private Node consultingDoctorNode;
    private Node admittingDoctorNode;
    private Node otherProviderNode;
        
    private static List<String> PROVIDER_PATIENT_LOCATION_EXPRESSIONS;    
    private static String PROVIDER_PATIENT_FACILITY_LOCATION_EXPRESSION;
    private static List<String> PROVIDER_FAMILY_NAME_EXPRESSIONS;
    private static String PROVIDER_FIRST_NAME_EXPRESSION;
    private static String PROVIDER_FIRST_NAME_ALT_EXPRESSION;
    private static String PROVIDER_MIDDLE_NAME_EXPRESSION;
    private static String PROVIDER_MIDDLE_NAME_ALT_EXPRESSION;
    private static String PROVIDER_SUFFIX_NAME_EXPRESSION;
    private static String PROVIDER_SUFFIX_NAME_ALT_EXPRESSION;
    private static String PROVIDER_NAME_ZLR_EXPRESSION;
    private static String ATTENDING_DOCTOR_EXPRESSION;    
    private static String REFERRING_DOCTOR_EXPRESSION;    
    private static String CONSULTING_DOCTOR_EXPRESSION;    
    private static String ADMITTING_DOCTOR_EXPRESSION;    
    private static String OTHER_PROVIDER_EXPRESSION;    
    private static String PROVIDER_LOCAL_ID_ALT_PART1_EXPRESSION;
    private static String PROVIDER_LOCAL_ID_ALT_PART2_EXPRESSION;
    private static String PROVIDER_STREET1_EXPRESSION;
    private static String PROVIDER_STREET1_ALT_EXPRESSION;
    private static String PROVIDER_STREET2_EXPRESSION;
    private static String PROVIDER_STREET2_ALT_EXPRESSION;
    private static String PROVIDER_CITY_EXPRESSION;
    private static String PROVIDER_CITY_ALT_EXPRESSION;
    private static String PROVIDER_STATE_EXPRESSION;
    private static String PROVIDER_STATE_ALT_EXPRESSION;
    private static String PROVIDER_ZIP_EXPRESSION;
    private static String PROVIDER_ZIP_ALT_EXPRESSION;
    private static String PROVIDER_COUNTY_EXPRESSION;
    private static String PROVIDER_COUNTY_ALT_EXPRESSION;
    private static String PROVIDER_PHONE_AREACODE_EXPRESSION;
    private static String PROVIDER_PHONE_NUMBER_EXPRESSION;
    
    private static String LOCATION_PREFIX = "^^^";
    
    static
    {
        try
        {        	
        	PROVIDER_PATIENT_LOCATION_EXPRESSIONS = new ArrayList<String>();
        	// used by HL7 v2.4 and v2.5
        	PROVIDER_PATIENT_LOCATION_EXPRESSIONS.add("./PV1.3/PL.4/text()");
        	PROVIDER_PATIENT_LOCATION_EXPRESSIONS.add("./PV1.3/PL.1/text()"); 
        	// used by HL7 v2.2 and v2.3
        	PROVIDER_PATIENT_LOCATION_EXPRESSIONS.add("./PV1.3/CM_INTERNAL_LOCATION.4/text()");
        	PROVIDER_PATIENT_LOCATION_EXPRESSIONS.add("./PV1.3/CM_INTERNAL_LOCATION.1/text()"); 
        	// used by HL7 v2.1
        	PROVIDER_PATIENT_LOCATION_EXPRESSIONS.add("./PV1.3/text()"); 
        	PROVIDER_PATIENT_FACILITY_LOCATION_EXPRESSION = "./PV1.3/PL.4/HD.2/text()";
            PROVIDER_FAMILY_NAME_EXPRESSIONS = new ArrayList<String>();
            PROVIDER_FAMILY_NAME_EXPRESSIONS.add("./PV1.7/XCN.2/FN.1/text()");
            PROVIDER_FAMILY_NAME_EXPRESSIONS.add("./PV1.7/XCN.2/text()");
            PROVIDER_FAMILY_NAME_EXPRESSIONS.add("./PV1.7/CN.1/text()");
            PROVIDER_FIRST_NAME_EXPRESSION = "./PV1.7/XCN.3/text()";
            PROVIDER_FIRST_NAME_ALT_EXPRESSION = "./PV1.7/CN.2/text()";
            PROVIDER_MIDDLE_NAME_EXPRESSION = "./PV1.7/XCN.4/text()";
            PROVIDER_MIDDLE_NAME_ALT_EXPRESSION = "./PV1.7/CN.3/text()";
            PROVIDER_SUFFIX_NAME_EXPRESSION = "./PV1.7/XCN.5/text()";
            PROVIDER_SUFFIX_NAME_ALT_EXPRESSION = 
                    "./PV1.7/CN.4/text()";            
            ATTENDING_DOCTOR_EXPRESSION = "./PV1.7";            
            REFERRING_DOCTOR_EXPRESSION = "./PV1.8";            
            CONSULTING_DOCTOR_EXPRESSION ="./PV1.9";            
            ADMITTING_DOCTOR_EXPRESSION ="./PV1.17";                       
            OTHER_PROVIDER_EXPRESSION ="./PV1.52";            
            PROVIDER_LOCAL_ID_ALT_PART1_EXPRESSION = 
                    "./ZLR.2/UNKNOWN.3/text()";
            PROVIDER_LOCAL_ID_ALT_PART2_EXPRESSION = 
                    "./ZLR.2/UNKNOWN.4/text()";
            PROVIDER_NAME_ZLR_EXPRESSION = 
                    "./ZLR.2/UNKNOWN.1/text()";
            PROVIDER_STREET1_EXPRESSION = 
                    "./ZLR.1/UNKNOWN.1/text()";
            PROVIDER_STREET1_ALT_EXPRESSION = 
                    "./XLR.3/UNKNOWN.1/text()";
            PROVIDER_STREET2_EXPRESSION = 
                    "./ZLR.1/UNKNOWN.2/text()";
            PROVIDER_STREET2_ALT_EXPRESSION = 
                    "./ZLR.3/UNKNOWN.2/text()";
            PROVIDER_CITY_EXPRESSION = 
                    "./ZLR.1/UNKNOWN.3/text()";
            PROVIDER_CITY_ALT_EXPRESSION = 
                    "./ZLR.3/UNKNOWN.3/text()";
            PROVIDER_STATE_EXPRESSION = 
                    "./ZLR.1/UNKNOWN.4/text()";
            PROVIDER_STATE_ALT_EXPRESSION = 
                    "./ZLR.3/UNKNOWN.4/text()";
            PROVIDER_ZIP_EXPRESSION = 
                    "./ZLR.1/UNKNOWN.5/text()";
            PROVIDER_ZIP_ALT_EXPRESSION = 
                    "./ZLR.3/UNKNOWN.5/text()";
            PROVIDER_COUNTY_EXPRESSION= 
                    "./ZLR.1/UNKNOWN.9/text()";
            PROVIDER_COUNTY_ALT_EXPRESSION= 
                    "./ZLR.3/UNKNOWN.9/text()";
            PROVIDER_PHONE_AREACODE_EXPRESSION = 
                    "./ZLR.4/UNKNOWN.6/text()";
            PROVIDER_PHONE_NUMBER_EXPRESSION = 
                    "./ZLR.4/UNKNOWN.7/text()";
            
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public Provider(Node pv1Node, Node zlrNode)
    {
        this.pv1Node = pv1Node;
        this.zlrNode = zlrNode;
        nameHandler = NameFactory.getNameHandler(pv1Node);
        attendingDoctorNode = XmlUtilities.findHL7Part(ATTENDING_DOCTOR_EXPRESSION, pv1Node);
        referringDoctorNode = XmlUtilities.findHL7Part(REFERRING_DOCTOR_EXPRESSION, pv1Node);
        consultingDoctorNode = XmlUtilities.findHL7Part(CONSULTING_DOCTOR_EXPRESSION, pv1Node);
        admittingDoctorNode = XmlUtilities.findHL7Part(ADMITTING_DOCTOR_EXPRESSION, pv1Node);
        otherProviderNode = XmlUtilities.findHL7Part(OTHER_PROVIDER_EXPRESSION, pv1Node);
    }
    
    public static String getSendingLocation(String msg) {
    	String sendingLocation = "";
    	String[] segments = msg.split(Pattern.quote("\r"));
    	for( String segment : segments ) {
    		if (segment.startsWith("PV1|")) {
    			String fieldSeparator = MessageHeader.getFieldSeparator(msg);
    			String[] fields = segment.split(Pattern.quote(fieldSeparator));
    			// index 3 is the PV1_3 (since index 0 is the segment name)
    			if (fields.length >= 4) {
	    			String componentSeparator = MessageHeader.getComponentSeparator(msg);
	    			String[] components = fields[3].split(Pattern.quote(componentSeparator));
	    			String pv1_3_1 = null;
	    			if (components.length > 0) {
	    				pv1_3_1 = components[0];	// zero based vs. one based
	    			}
	    			String pv1_3_4 = "";
	    			if (components.length > 3) {
	    				pv1_3_4 = components[3];
	    			}
	    			if (StringUtils.isNotEmpty(pv1_3_4)) {
	    				sendingLocation = pv1_3_4;
	    			} else if (pv1_3_1 != null) {
	    				sendingLocation = pv1_3_1;    			
	    			}
    			}
    			
    			break;
    		} else {
    			continue;
    		}
    	}
    	
    	if (StringUtils.isEmpty(sendingLocation)) {
    		sendingLocation = null;
    	} else {
    		sendingLocation = LOCATION_PREFIX + sendingLocation;
    	}
    	
    	return sendingLocation;
    }
    
    public String getSendingLocation() {
    	
		return LOCATION_PREFIX + getPatientLocationId();
    }
    
    private String getPatientLocationId()
    {
    	String location = null;
        Iterator<String> iter = PROVIDER_PATIENT_LOCATION_EXPRESSIONS.iterator();
        while (StringUtils.isEmpty((location)) && iter.hasNext())
        {
            location = XmlUtilities.findFieldValue(iter.next(), (Element)pv1Node);
        }
        
        return location;
    }
    
    public String getPatientFacilityLocationId() 
    {
        return XmlUtilities.findFieldValue(PROVIDER_PATIENT_FACILITY_LOCATION_EXPRESSION, (Element)pv1Node);       
    }
    
    public String getProviderFirstName() {
        String firstName = XmlUtilities.findFieldValue(PROVIDER_FIRST_NAME_EXPRESSION, (Element)pv1Node);
        if (StringUtils.isEmpty(firstName))
        {
            firstName = XmlUtilities.findFieldValue(PROVIDER_FIRST_NAME_ALT_EXPRESSION, (Element)pv1Node);
        }
        return firstName;
    }
    
    public String getProviderMiddleName() {
        String middleName = XmlUtilities.findFieldValue(PROVIDER_MIDDLE_NAME_EXPRESSION, (Element)pv1Node);
        if (StringUtils.isEmpty((middleName)))
        {
            middleName = XmlUtilities.findFieldValue(PROVIDER_MIDDLE_NAME_ALT_EXPRESSION, (Element)pv1Node);
        }
        return middleName;
    }
    
    public String getProviderFamilyName() {

        String familyName = null;
        Iterator<String> iter = PROVIDER_FAMILY_NAME_EXPRESSIONS.iterator();
        while (StringUtils.isEmpty((familyName)) && iter.hasNext())
        {
            familyName = XmlUtilities.findFieldValue(iter.next(), (Element)pv1Node);
        }
        
        return familyName;
    }
    
    public String getProviderSuffixName() {
        String suffixName = XmlUtilities.findFieldValue(PROVIDER_SUFFIX_NAME_EXPRESSION, (Element)pv1Node);
        if (StringUtils.isEmpty((suffixName)))
        {
            suffixName = XmlUtilities.findFieldValue(PROVIDER_SUFFIX_NAME_ALT_EXPRESSION, (Element)pv1Node);
        }
        return suffixName;
    }
    
    public String getProviderName() 
    {
        String familyName = getProviderFamilyName();
        String firstName = getProviderFirstName();
        String middleName = getProviderMiddleName();
        String suffixName = getProviderSuffixName();
        
        String providerName = null;
        if (StringUtils.isNotEmpty((familyName)) ||
            StringUtils.isNotEmpty((firstName)) ||
            StringUtils.isNotEmpty((middleName)) ||
            StringUtils.isNotEmpty((suffixName)))
        {
            providerName = familyName + ", " + firstName + " " + middleName + " " + suffixName;
            providerName = providerName.trim();
        }
        return providerName;
    }
    
    public String getZlrProviderName() 
    {
        String zlrProviderName = null;
        if (zlrNode != null)
        {
            zlrProviderName = XmlUtilities.findFieldValue(PROVIDER_NAME_ZLR_EXPRESSION, (Element)zlrNode);
        }
        return zlrProviderName;
    }
    
    public String getAttendingDoctorLocalIdentifier() 
    {
    	return HL7NameHandler.useNameHandler(nameHandler, attendingDoctorNode, NamePart.ID);
    }
    
    public String getAttendingDoctorLastName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, attendingDoctorNode, NamePart.FAMILY_NAME);
    }
    
    public String getAttendingDoctorFirstName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, attendingDoctorNode, NamePart.GIVEN_NAME);
    }
    
    public String getAttendingDoctorMiddleName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, attendingDoctorNode, NamePart.MIDDLE_NAME);
    }
    
    public String getAttendingDoctorSuffixName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, attendingDoctorNode, NamePart.SUFFIX_NAME);
    }
    
    public String getReferringDoctorLocalIdentifier()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, referringDoctorNode, NamePart.ID);
    }
    
    public String getReferringDoctorLastName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, referringDoctorNode, NamePart.FAMILY_NAME);
    }
    
    public String getReferringDoctorFirstName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, referringDoctorNode, NamePart.GIVEN_NAME);
    }
    
    public String getReferringDoctorMiddleName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, referringDoctorNode, NamePart.MIDDLE_NAME);
    }
    
    public String getReferringDoctorSuffixName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, referringDoctorNode, NamePart.SUFFIX_NAME);
    }
    
    public String getConsultingDoctorLocalIdentifier()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, consultingDoctorNode, NamePart.ID);
    }
    
    public String getConsultingDoctorLastName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, consultingDoctorNode, NamePart.FAMILY_NAME);
    }
    
    public String getConsultingDoctorFirstName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, consultingDoctorNode, NamePart.GIVEN_NAME);
    }
    
    public String getConsultingDoctorMiddleName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, consultingDoctorNode, NamePart.MIDDLE_NAME);
    }
    
    public String getConsultingDoctorSuffixName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, consultingDoctorNode, NamePart.SUFFIX_NAME);
    }
    
    public String getAdmittingDoctorLocalIdentifier()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, admittingDoctorNode, NamePart.ID);
    }
    
    public String getAdmittingDoctorLastName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, admittingDoctorNode, NamePart.FAMILY_NAME);
    }
    
    public String getAdmittingDoctorFirstName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, admittingDoctorNode, NamePart.GIVEN_NAME);
    }
    
    public String getAdmittingDoctorMiddleName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, admittingDoctorNode, NamePart.MIDDLE_NAME);
    }
    
    public String getAdmittingDoctorSuffixName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, admittingDoctorNode, NamePart.SUFFIX_NAME);
    }
    
    public String getOtherProviderLocalIdentifier()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, otherProviderNode, NamePart.ID);
    }
    
    public String getOtherProviderLastName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, otherProviderNode, NamePart.FAMILY_NAME);
    }
    
    public String getOtherProviderFirstName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, otherProviderNode, NamePart.GIVEN_NAME);
    }
    
    public String getOtherProviderMiddleName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, otherProviderNode, NamePart.MIDDLE_NAME);
    }
    
    public String getOtherProviderSuffixName()
    {
    	return HL7NameHandler.useNameHandler(nameHandler, otherProviderNode, NamePart.SUFFIX_NAME);
    }
    
    public String getProviderAltLocalIdentifier() 
    {
        String providerAltLocalId = null;
        if( zlrNode != null )
        {
            providerAltLocalId = XmlUtilities.findFieldValue(PROVIDER_LOCAL_ID_ALT_PART1_EXPRESSION
                    , (Element)zlrNode)
                    + " "
                    + XmlUtilities.findFieldValue(PROVIDER_LOCAL_ID_ALT_PART2_EXPRESSION, (Element)zlrNode);
        }
        return providerAltLocalId;
    }
    
    public String getProviderStreet1() 
    {
        String street1 = null;
        if (zlrNode != null)
        {
            street1 = useStandardOrAlternateExpression(zlrNode,
                    PROVIDER_STREET1_EXPRESSION,
                    PROVIDER_STREET1_ALT_EXPRESSION);
        }
        return street1;
    }
    
    public String getProviderStreet2() 
    {
        String street2 = null;
        if( zlrNode != null )
        {
            street2 = useStandardOrAlternateExpression(zlrNode,
                    PROVIDER_STREET2_EXPRESSION,
                    PROVIDER_STREET2_ALT_EXPRESSION);
        }
        return street2;
    }
    
    public String getProviderStreet() 
    {
        return getProviderStreet1() + " " + getProviderStreet2();
    }
    
    public String getProviderCity() 
    {
        String city = null;
        if( zlrNode != null )
        {
            city = useStandardOrAlternateExpression(zlrNode,
                    PROVIDER_CITY_EXPRESSION, PROVIDER_CITY_ALT_EXPRESSION);
        }
        return city;
    }
    
    public String getProviderState() 
    {
        String state = null;
        if( zlrNode != null )
        {
            state = useStandardOrAlternateExpression(zlrNode,
                    PROVIDER_STATE_EXPRESSION, PROVIDER_STATE_ALT_EXPRESSION);
        }
        return state;
    }
    
    public String getProviderZip() 
    {
        String zip = null;
        if( zlrNode != null )
        {
            zip = useStandardOrAlternateExpression(zlrNode,
                    PROVIDER_ZIP_EXPRESSION, PROVIDER_ZIP_ALT_EXPRESSION);
        }
        return zip;
    }
    
    public String getProviderCounty() 
    {
        String county = null;
        if( zlrNode != null )
        {
            county = useStandardOrAlternateExpression(zlrNode,
                    PROVIDER_COUNTY_EXPRESSION, PROVIDER_COUNTY_ALT_EXPRESSION);
        }
        return county;
    }
    
    public String getProviderAreaCode() 
    {
        String providerAreaCode = null;
        if (zlrNode != null)
        {
            providerAreaCode = XmlUtilities.findFieldValue(PROVIDER_PHONE_AREACODE_EXPRESSION, (Element)zlrNode);
        }
        return providerAreaCode;
    }
    
    public String getProviderPhoneNumber() 
    {
        String providerPhoneNumber = null;
        if (zlrNode != null)
        {
            providerPhoneNumber = XmlUtilities.findFieldValue(PROVIDER_PHONE_NUMBER_EXPRESSION, (Element)zlrNode);
        }
        return providerPhoneNumber;
    }
    
    public String getProviderFullPhoneNumber() 
    {   
        String areaCode = StringUtilities.trim(getProviderAreaCode());
        String phoneNumber = StringUtilities.trim(getProviderPhoneNumber());
        if (areaCode != null && phoneNumber != null) {
            return "(" + areaCode + ")" + phoneNumber;
        }
        else {
            return null;
        }
    }
    
    private String useStandardOrAlternateExpression(Node node,
            String stdExpression, String altExpression)
            
    {
        String value;
        value = XmlUtilities.findFieldValue(stdExpression, (Element)node);
        if( StringUtils.isEmpty((value)) )
        {
            value = XmlUtilities.findFieldValue(altExpression, (Element)node);
        }
        return value;
    }        
}

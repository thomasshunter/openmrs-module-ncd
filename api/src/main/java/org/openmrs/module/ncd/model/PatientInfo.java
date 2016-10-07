/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class allows easy access to Patient Info data.
 * @author jlbrown
 *
 */
public class PatientInfo implements PersonNameInfo, PersonAddressInfo
{
    private Node pidNode                                = null;
    private Node zidNode                                = null;
    protected static final String PATIENT_BIRTH         = "birth";
	protected static final String PATIENT_CITY          = "city";
    protected static final String PATIENT_COUNTRY       = "country";
    protected static final String PATIENT_COUNTY        = "county";
    protected static final String PATIENT_LAST_NAME     = "last name";
    protected static final String PATIENT_FIRST_NAME    = "first name";
    protected static final String PATIENT_MIDDLE_NAME   = "middle name";
    protected static final String PATIENT_RACE          = "race";
    protected static final String PATIENT_SEX           = "sex";
    protected static final String PATIENT_SSN           = "ssn";
    protected static final String PATIENT_STATE         = "state";
    protected static final String PATIENT_STREET1       = "street1";
    protected static final String PATIENT_STREET2       = "street2";
    protected static final String PATIENT_ZIP           = "zip";
    
    static Map<String, List<String>> xpathsByKey        = new HashMap<String, List<String>>();
    
    static String PATIENT_MEDICAL_RECORD_ID_EXPRESSION;
    static String PATIENT_MEDICAL_RECORD_ID_PART2_EXPRESSION;
    static String PATIENT_MEDICAL_RECORD_ID_ALT_EXPRESSION;
    static String PATIENT_MEDICAL_RECORD_ID_PART2_ALT_EXPRESSION;
    static String PATIENT_MEDICAL_RECORD_ID_ALT2_EXPRESSION;
    static String PATIENT_MEDICAL_RECORD_ID_PART2_ALT2_EXPRESSION;
    
    static String PATIENT_PHONE_EXPRESSION;
    static String PATIENT_PHONE_EXPRESSION_ALT_PART1;
    static String PATIENT_PHONE_EXPRESSION_ALT_PART2;
    static String PATIENT_PHONE_ALT_EXPRESSION;
    static String PATIENT_PHONE_ALT2_EXPRESSION;
    static String PATIENT_PHONE_ALT3_EXPRESSION;
    static String PATIENT_PHONE_EXPRESSION_ALT2_PART1;
    static String PATIENT_PHONE_EXPRESSION_ALT2_PART2;
    static String PATIENT_PHONE_ALT4_EXPRESSION;
    static String PATIENT_PHONE_ALT5_EXPRESSION;
    
    static void addPath(String key, String xpath) 
    {
    	List<String> keyXpaths = xpathsByKey.get(key);
    	
    	if( keyXpaths == null ) 
    	{
    		keyXpaths = new ArrayList<String>();
    		xpathsByKey.put(key, keyXpaths);
    	}
    	
    	keyXpaths.add(xpath);
    }

    static
    {
        try
        {
        	addPath(PATIENT_BIRTH, "./PID.7/TS.1/text()");
        	addPath(PATIENT_BIRTH, "./ZID.6/UNKNOWN.1/text()");

        	addPath(PATIENT_CITY, "./PID.11/XAD.3/text()");
        	addPath(PATIENT_CITY, "./PID.11/AD.3/text()");
        	addPath(PATIENT_CITY, "./ZID.10/UNKNOWN.3/text()");
        	
        	addPath(PATIENT_COUNTRY, "./PID.11/XAD.6/text()");
        	addPath(PATIENT_COUNTRY, "./PID.11/AD.6/text()");
        	addPath(PATIENT_COUNTRY, "./ZID.10/UNKNOWN.6/text()");
            
        	addPath(PATIENT_COUNTY, "./PID.11/XAD.9/text()");
        	addPath(PATIENT_COUNTY, "./PID.12/text()");
        	addPath(PATIENT_COUNTY, "./ZID.10/UNKNOWN.9/text()");
        	addPath(PATIENT_COUNTY, "./ZID.11/text()");
            
        	addPath(PATIENT_LAST_NAME, "./PID.5/XPN.1/FN.1/text()");
        	addPath(PATIENT_LAST_NAME, "./PID.5/XPN.1/text()");
        	addPath(PATIENT_LAST_NAME, "./PID.5/PN.1/text()");
        	addPath(PATIENT_LAST_NAME, "./ZID.4/UNKNOWN.1/UNKNOWN.1/text()");
        	addPath(PATIENT_LAST_NAME, "./ZID.4/UNKNOWN.1/text()");
            
        	addPath(PATIENT_FIRST_NAME, "./PID.5/XPN.2/text()");
        	addPath(PATIENT_FIRST_NAME, "./PID.5/PN.2/text()");
        	addPath(PATIENT_FIRST_NAME, "./ZID.4/UNKNOWN.2/text()");
            
        	addPath(PATIENT_MIDDLE_NAME, "./PID.5/XPN.3/text()");
        	addPath(PATIENT_MIDDLE_NAME, "./PID.5/PN.3/text()");
        	addPath(PATIENT_MIDDLE_NAME, "./ZID.4/UNKNOWN.3/text()");
            
            addPath(PATIENT_RACE, "./PID.10/CE.2/text()");
            addPath(PATIENT_RACE, "./PID.10/text()");
            addPath(PATIENT_RACE, "./ZID.9/UNKNOWN.2/text()");
            addPath(PATIENT_RACE, "./ZID.9/text()");
            
            addPath(PATIENT_SEX, "./PID.8/text()");
            addPath(PATIENT_SEX, "./ZID.7/text()");
            
            addPath(PATIENT_SSN, "./PID.19/text()");
            addPath(PATIENT_SSN, "./ZID.18/text()");
            
            addPath(PATIENT_STATE, "./PID.11/XAD.4/text()");
            addPath(PATIENT_STATE, "./PID.11/AD.4/text()");
            addPath(PATIENT_STATE, "./ZID.10/UNKNOWN.4/text()");
            
            addPath(PATIENT_STREET1, "./PID.11/XAD.1/SAD.1/text()");
            addPath(PATIENT_STREET1, "./PID.11/XAD.1/text()");
            addPath(PATIENT_STREET1, "./PID.11/AD.1/text()");
            addPath(PATIENT_STREET1, "./ZID.10/UNKNOWN.1/UNKNOWN.1/text()");
            addPath(PATIENT_STREET1, "./ZID.10/UNKNOWN.1/text()");
            
            addPath(PATIENT_STREET2, "./PID.11/XAD.2/text()");
            addPath(PATIENT_STREET2, "./PID.11/AD.2/text()");
            addPath(PATIENT_STREET2, "./ZID.10/UNKNOWN.2/text()");
            
            addPath(PATIENT_ZIP, "./PID.11/XAD.5/text()");
            addPath(PATIENT_ZIP, "./PID.11/AD.5/text()");
            addPath(PATIENT_ZIP, "./ZID.10/UNKNOWN.5/text()");
            
            PATIENT_MEDICAL_RECORD_ID_EXPRESSION            = "./PID.3/CX.1/text()";
            PATIENT_MEDICAL_RECORD_ID_PART2_EXPRESSION      = "./PID.3/CX.2/text()";
            PATIENT_MEDICAL_RECORD_ID_ALT_EXPRESSION        = "./PID.3/CM_PAT_ID.1/text()";
            PATIENT_MEDICAL_RECORD_ID_PART2_ALT_EXPRESSION  = "./PID.3/CM_PAT_ID.2/text()";
            PATIENT_MEDICAL_RECORD_ID_ALT2_EXPRESSION       = "./ZID.2/UNKNOWN.1/text()";
            PATIENT_MEDICAL_RECORD_ID_PART2_ALT2_EXPRESSION = "./ZID.2/UNKNOWN.2/text()";
            
            PATIENT_PHONE_EXPRESSION                        = "./PID.13/XTN.1/text()";
            PATIENT_PHONE_EXPRESSION_ALT_PART1              = "./PID.13/XTN.6/text()";
            PATIENT_PHONE_EXPRESSION_ALT_PART2              = "./PID.13/XTN.7/text()";
            PATIENT_PHONE_ALT_EXPRESSION                    = "./PID.13/text()";
            PATIENT_PHONE_ALT2_EXPRESSION                   = "./PID.14/text()";
            
            PATIENT_PHONE_ALT3_EXPRESSION                   = "./ZID.12/UNKNOWN.1/text()";
            PATIENT_PHONE_EXPRESSION_ALT2_PART1             = "./ZID.12/UNKNOWN.6/text()";
            PATIENT_PHONE_EXPRESSION_ALT2_PART2             = "./ZID.12/UNKNOWN.7/text()";
            PATIENT_PHONE_ALT4_EXPRESSION                   = "./ZID.12/text()";
            PATIENT_PHONE_ALT5_EXPRESSION                   = "./ZID.13/text()";
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public PatientInfo(Node pidNode, Node zidNode)
    {
        this.pidNode = pidNode;
        this.zidNode = zidNode;
    }

    public String findFieldValueByKey(String key) 
    {
    	List<String> keyXpaths = xpathsByKey.get(key);
    
    	if (keyXpaths != null) 
    	{
    		for (String path : keyXpaths) 
    		{
    	    	String value;
    			
    	    	if (path.startsWith("./PID")) 
    	    	{
    				value = XmlUtilities.findFieldValue(path, pidNode);
    			}
    			else 
    			{
    				value = XmlUtilities.findFieldValue(path, zidNode);
    			}
    			
    	    	if (!StringUtils.isEmpty(value)) 
    	    	{
    				return value;
    			}
    		}
    	}
    	
    	return "";
    }

    public Date getPatientBirth() throws ParseException
    {
        Date retVal         = null;
        String birthString  = findFieldValueByKey(PATIENT_BIRTH);
        
        if (StringUtils.isNotEmpty((birthString)))
        {        
        	// truncate incoming date string to at most 8 chars to avoid parse exception if there is a time component 
            retVal = new SimpleDateFormat("yyyyMMdd").parse(birthString.substring(0, Math.min(birthString.length(), 8)));
        }
        
        return retVal;
    }

    /**
     * @see org.openmrs.module.ncd.model.PersonAddressInfo#getCity()
     */
    public String getCity() 
    {
    	return findFieldValueByKey(PATIENT_CITY);
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonAddressInfo#getCountry()
     */
    public String getCountry() 
    {
    	return findFieldValueByKey(PATIENT_COUNTRY);
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonAddressInfo#getCounty()
     */
    public String getCounty() 
    {
    	return findFieldValueByKey(PATIENT_COUNTY);
    }
    
    public String getPatientInstitutionMedicalRecordId() 
    {
    	String mrn1 = XmlUtilities.findFieldValue(PATIENT_MEDICAL_RECORD_ID_EXPRESSION, (Element)pidNode);
    	String mrn2 = XmlUtilities.findFieldValue(PATIENT_MEDICAL_RECORD_ID_PART2_EXPRESSION, (Element)pidNode);
    	
    	if (StringUtils.isEmpty(mrn1)) 
    	{
        	mrn1 = XmlUtilities.findFieldValue(PATIENT_MEDICAL_RECORD_ID_ALT_EXPRESSION, (Element)pidNode);
        	mrn2 = XmlUtilities.findFieldValue(PATIENT_MEDICAL_RECORD_ID_PART2_ALT_EXPRESSION, (Element)pidNode);
    	}
    	
    	if (StringUtils.isEmpty(mrn1)) 
    	{
        	mrn1 = XmlUtilities.findFieldValue(PATIENT_MEDICAL_RECORD_ID_ALT2_EXPRESSION, (Element)zidNode);
        	mrn2 = XmlUtilities.findFieldValue(PATIENT_MEDICAL_RECORD_ID_PART2_ALT2_EXPRESSION, (Element)zidNode);
    	}

    	String mrn = mrn1 + " " + mrn2;
    	
    	return mrn;
    }    
    
    public String getLastName() 
    {
        return findFieldValueByKey(PATIENT_LAST_NAME);
    }
    
    public String getFirstName() 
    {
        String firstName = findFieldValueByKey(PATIENT_FIRST_NAME);
        
        if (StringUtils.isEmpty((firstName)))
        {
            firstName = "";
        }
        
        return firstName;
    }
    
    public String getMiddleName() 
    {
        String middleName = findFieldValueByKey(PATIENT_MIDDLE_NAME);
        
        if (StringUtils.isEmpty((middleName)))
        {
            middleName = "";
        }
        
        return middleName;
    }
    
    public String getFullName() 
    {        
        String lastName = getLastName();
        String firstName = getFirstName();
        String middleName = getMiddleName();
        String fullName = "";
        
        if (StringUtils.isNotEmpty((lastName)) || StringUtils.isNotEmpty((firstName)) || StringUtils.isNotEmpty((middleName)))
        {
            fullName = lastName + ", " + firstName + " " + middleName;
        }
        
        return fullName;
    }        
    
    public String getPatientPhone() 
    {
        String patientPhoneNumber = XmlUtilities.findFieldValue(PATIENT_PHONE_EXPRESSION, (Element)pidNode);
        
        if (patientPhoneNumber == null)
        {
            String patientAreaCode = XmlUtilities.findFieldValue(PATIENT_PHONE_EXPRESSION_ALT_PART1, (Element)pidNode);
            String patient7DigitPhone = XmlUtilities.findFieldValue(PATIENT_PHONE_EXPRESSION_ALT_PART2, (Element)pidNode);
        
            if (StringUtils.isNotEmpty(patientAreaCode) || StringUtils.isNotEmpty(patient7DigitPhone)) 
            {
            	patientPhoneNumber = "(" + patientAreaCode + ")" + patient7DigitPhone;
            }
        }
        
        if (StringUtils.isEmpty(patientPhoneNumber)) 
        {
        	patientPhoneNumber = XmlUtilities.findFieldValue(PATIENT_PHONE_ALT_EXPRESSION, (Element)pidNode);
        }
        
        if (StringUtils.isEmpty(patientPhoneNumber)) 
        {
        	patientPhoneNumber = XmlUtilities.findFieldValue(PATIENT_PHONE_ALT2_EXPRESSION, (Element)pidNode);
        }
        
        if (StringUtils.isEmpty(patientPhoneNumber)) 
        {
        	patientPhoneNumber = XmlUtilities.findFieldValue(PATIENT_PHONE_ALT3_EXPRESSION, (Element)zidNode);
        }
        
        if (patientPhoneNumber == null)
        {
            String patientAreaCode = XmlUtilities.findFieldValue(PATIENT_PHONE_EXPRESSION_ALT2_PART1, (Element)zidNode);
            String patient7DigitPhone = XmlUtilities.findFieldValue(PATIENT_PHONE_EXPRESSION_ALT2_PART2, (Element)zidNode);
        
            if (StringUtils.isNotEmpty(patientAreaCode) || StringUtils.isNotEmpty(patient7DigitPhone)) 
            {
            	patientPhoneNumber = "(" + patientAreaCode + ")" + patient7DigitPhone;
            }
        }
        
        if (StringUtils.isEmpty(patientPhoneNumber)) 
        {
        	patientPhoneNumber = XmlUtilities.findFieldValue(PATIENT_PHONE_ALT4_EXPRESSION, (Element)zidNode);
        }
        
        if (StringUtils.isEmpty(patientPhoneNumber)) 
        {
        	patientPhoneNumber = XmlUtilities.findFieldValue(PATIENT_PHONE_ALT5_EXPRESSION, (Element)zidNode);
        }
        
        return patientPhoneNumber;
    }
    
    public String getPatientRace() 
    {
        return findFieldValueByKey(PATIENT_RACE);
    }
    
    public Character getPatientSex() 
    {
        String patientSexString = findFieldValueByKey(PATIENT_SEX);
        
        if (StringUtils.isEmpty((patientSexString)))
        {
            return null;
        }
        else
        {
            return patientSexString.charAt(0);
        }
    }
    
    public String getPatientSSN() 
    {
        return findFieldValueByKey(PATIENT_SSN);
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonAddressInfo#getState()
     */
    public String getState() 
    {
    	return findFieldValueByKey(PATIENT_STATE);
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonAddressInfo#getStreet1()
     */
    public String getStreet1() 
    {
    	return findFieldValueByKey(PATIENT_STREET1);
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonAddressInfo#getStreet2()
     */
    public String getStreet2() 
    {
    	return findFieldValueByKey(PATIENT_STREET2);
    }
    
    /**
     * @see org.openmrs.module.ncd.model.PersonAddressInfo#getZip()
     */
    public String getZip() 
    {
    	return findFieldValueByKey(PATIENT_ZIP);
    }

    public String getSuffixName() {
        
        // Apparently patients don't have this information.
        return null;
    }
}

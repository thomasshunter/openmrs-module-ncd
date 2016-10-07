/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.critic.ConditionExtractor.CodeSystemPair;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Class to contain a DG1 segment node and access the information from the
 * segment.
 * 
 * @author jlbrown
 * 
 */
public class Diagnosis implements IResultSegment
{
    private Node dg1Node = null;
    
    private final static String DIAGNOSIS_CODE_EXPRESSION;
    private final static String DIAGNOSIS_CODE_ALT_EXPRESSION;
    private final static String DIAGNOSIS_CODE_ALT2_EXPRESSION;
    private final static String DIAGNOSIS_TEXT_EXPRESSION;
    private final static String DIAGNOSIS_TEXT_ALT_EXPRESSION;
    private final static String DIAGNOSIS_CODE_SYSTEM_EXPRESSION;
    private final static String DIAGNOSIS_CODE_SYSTEM_ALT_EXPRESSION;
    private final static String DIAGNOSIS_CODE_SYSTEM_ALT2_EXPRESSION;
    private final static String DIAGNOSIS_DATE_EXPRESSION;
    private final static String DIAGNOSIS_ID_EXPRESSION;
    
    private final static String PID_EXPRESSION;
    private final static String PD1_EXPRESSION;
    private final static String PV1_EXPRESSION;
    private final static String MSH_EXPRESSION;
    private final static String MSH_ALT_EXPRESSION;
    private final static String MSH_ALT2_EXPRESSION;
    private final static String ZID_EXPRESSION;
    
    private final static String ICD9_CODE_SYSTEM = "I9";
    
    static
    {
        try
        {
            DIAGNOSIS_CODE_EXPRESSION = "./DG1.3/CE.1/text()";
            DIAGNOSIS_CODE_ALT_EXPRESSION = "./DG1.3/CE.4/text()";
            DIAGNOSIS_CODE_ALT2_EXPRESSION = "./DG1.3/text()";
            DIAGNOSIS_TEXT_EXPRESSION = "./DG1.3/CE.2/text()";
            DIAGNOSIS_TEXT_ALT_EXPRESSION = "./DG1.3/CE.5/text()";
            DIAGNOSIS_CODE_SYSTEM_EXPRESSION = "./DG1.3/CE.3/text()";
            DIAGNOSIS_CODE_SYSTEM_ALT_EXPRESSION = "./DG1.3/CE.6/text()";
            DIAGNOSIS_CODE_SYSTEM_ALT2_EXPRESSION = "./DG1.2/text()";
            DIAGNOSIS_DATE_EXPRESSION = "./DG1.5/TS.1/text()";
            DIAGNOSIS_ID_EXPRESSION = "./DG1.20/EI.1/text()";
            
            PID_EXPRESSION = "../PID";
            PD1_EXPRESSION = "../PD1";
            PV1_EXPRESSION = "../PV1";
            MSH_EXPRESSION = "../MSH";
            MSH_ALT_EXPRESSION = "../../MSH";
            MSH_ALT2_EXPRESSION = "../../../MSH";
            ZID_EXPRESSION = "../ZID";
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }        
    
    public Diagnosis(Node dg1Node)
    {
        this.dg1Node = dg1Node;
    }
    
    public String getConcatenatedTestResultValues(List<Node> msgSegments) {
    	StringBuilder dg1Values = new StringBuilder();
    	for(Node msgSegment : msgSegments) {
    		if (msgSegment.getNodeName().equals("DG1")) {
	    		Diagnosis dg1 = new Diagnosis(msgSegment);
	    		dg1Values.append(dg1.getTestResultValue());
    		}
    	}
    	return dg1Values.toString();
    }
    
    public String getIcd9Code() {
    	String system = getTestResultCodeSystem();
    	String altSystem = getTestResultAltCodeSys();
    	String icd9Code = null;
    	if (system.equals("I9")) {
    		icd9Code = getTestResultCode();
    	} else if (altSystem.equals("I9")) {
    		icd9Code = getTestResultAltCode();
    	} else {
    		//icd9Code = getTestResultCode(); //hxiao: if no ICD9 code is available, return null instead of a wrong ICD9 code
    	}
    	return icd9Code;
    }

    public OrderCommon getOrderCommon() 
    {
        // A DG1 segment from an ADT_A01 message will not have an ORC segment.
        return null;
    }

    public OrderObservation getOrderObservation() 
    {
        // A DG1 segment from an ADT_A01 message will not have an OBR segment.
        return null;
    }

    public PatientInfo getPatientInfo() 
    {
        Node pidNode = XmlUtilities.findHL7Part(PID_EXPRESSION, (Element) dg1Node);
        Node zidNode = XmlUtilities.findHL7Part(ZID_EXPRESSION, (Element) dg1Node);
        return new PatientInfo(pidNode, zidNode);
    }

    public Provider getProvider() 
    {
        Node pv1Node = XmlUtilities.findHL7Part(PV1_EXPRESSION, (Element)dg1Node);
        // A DG1 segment from an ADT_A01 message will not have a ZLR segment.
        return new Provider(pv1Node, null);
    }
    
    public MessageHeader getMessageHeader()
    {
        Node mshNode = XmlUtilities.findHL7Part(MSH_EXPRESSION, (Element)dg1Node);
        if (mshNode == null) {
            mshNode = XmlUtilities.findHL7Part(MSH_ALT_EXPRESSION, (Element)dg1Node);
        }
        if (mshNode == null) {
            mshNode = XmlUtilities.findHL7Part(MSH_ALT2_EXPRESSION, (Element)dg1Node);
        }
        return new MessageHeader(mshNode);
    }

    public String getAbnormalFlag() 
    {
        // A DG1 segment does not have an abnormal flag
        return null;
    }

    public String getLabIdentifier() 
    {
        // A DG1 segment does not have a lab identifier
        return null;
    }

    public String getNormalRange() 
    {
        // A DG1 segment does not have a normal range.
        return null;
    }

    public String getTestResultAltCode() 
    {
        return XmlUtilities.findFieldValue(DIAGNOSIS_CODE_ALT_EXPRESSION, (Element)dg1Node);
    }

    public String getTestResultAltCodeSys() 
    {
        return XmlUtilities.findFieldValue(DIAGNOSIS_CODE_SYSTEM_ALT_EXPRESSION, (Element)dg1Node);
    }

    public String getTestResultAltCodeText() 
    {
        return XmlUtilities.findFieldValue(DIAGNOSIS_TEXT_ALT_EXPRESSION, (Element)dg1Node);
    }

    public String getTestDataType() 
    {
        // A DG1 segment does not have a test data type.
        return null;
    }

    public Date getTestDate() throws ParseException
    {
        Date diagnosisDate = null;
        String date = XmlUtilities.findFieldValue(DIAGNOSIS_DATE_EXPRESSION, (Element)dg1Node);
        if (! StringUtils.isEmpty(date))
        {
        	// truncate incoming date string to at most 8 chars to avoid parse exception if there is a time component 
            diagnosisDate = new SimpleDateFormat("yyyyMMdd").parse(date.substring(0, Math.min(date.length(), 8)));
        }
        return diagnosisDate;
    }

    public String getTestResultCode() 
    {
        String code = XmlUtilities.findFieldValue(DIAGNOSIS_CODE_EXPRESSION, (Element)dg1Node);
        
        if (StringUtils.isEmpty(code)) 
        {
        	code = XmlUtilities.findFieldValue(DIAGNOSIS_CODE_ALT2_EXPRESSION, (Element)dg1Node);
        }
        
        return code;
    }

    public String getTestResultCodeSystem() 
    {
        String codeSystem = XmlUtilities.findFieldValue(DIAGNOSIS_CODE_SYSTEM_EXPRESSION, (Element)dg1Node);
        
        if (StringUtils.isEmpty(codeSystem)) 
        {
        	codeSystem = XmlUtilities.findFieldValue(DIAGNOSIS_CODE_SYSTEM_ALT2_EXPRESSION, (Element)dg1Node);
        }
        
        return codeSystem;
    }

    public String getTestResultIdentifier() 
    {
        return XmlUtilities.findFieldValue(DIAGNOSIS_ID_EXPRESSION, (Element)dg1Node);
    }

    public String getTestResultName() 
    {
        // A DG1 segment does not have a test result name, we'll .
    	return XmlUtilities.findFieldValue(DIAGNOSIS_TEXT_EXPRESSION, (Element)dg1Node);
    }

    public Character getTestResultStatus() 
    {
        // A DG1 segment does not have a test result status.
        return null;
    }

    public String getTestResultSubIdentifier() 
    {
        // A DG1 segment does not have a test result sub identifier
        return null;
    }

    public String getTestResultUnits() 
    {
        // A DG1 segment does not have test result units.
        return null;
    }

    public String getTestResultValue() 
    {
    	// A DG1 segment does not have a test result value...we'll use the DG1-3.2 value.
        return getTestResultName();
    }
    
    public String getTestResultValueCode() 
    {
    	return null;
    }

	public void addDecidedResultIndexInfo(DecidedResult template) 
	{
		template.setObr("N/A");
		template.setObrCodeSystem("N/A");
		template.setObx(getTestResultCode());
		template.setObxCodeSystem(getTestResultCodeSystem());	
	}

	public void addDecidedResultSpecifics(DecidedResult template) 
	{
		template.setLoincCode(getTestResultCode());
		template.setResultCode(getTestResultCode());
		template.setObxText(getTestResultIdentifier());
		template.setObxAltText(getTestResultAltCodeText());
	}

	public CodeSystemPair getCodeSystemPair() 
	{
		CodeSystemPair retVal = new CodeSystemPair();
		retVal.setCode(getIcd9Code());            
        retVal.setCodeSystem(ICD9_CODE_SYSTEM);
        return retVal;
	}
	
	public Map<String, Staff> getStaffMap() 
	{
		return Staff.getStaffMap(dg1Node);
	}

	public PatientAdditionalDemographics getPatientAdditionalDemographics() 
	{
		Node pd1Node = XmlUtilities.findHL7Part(PD1_EXPRESSION, dg1Node);
		return new PatientAdditionalDemographics(pd1Node);
	}
    
}

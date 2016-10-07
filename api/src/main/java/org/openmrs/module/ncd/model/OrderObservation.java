/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.model;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.model.HL7NameHandler.NamePart;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class allows easy retrieval of Order Observation data
 * items.
 * 
 * @author jlbrown
 *
 */
public class OrderObservation
{        
    private final static String TEST_ID_EXPRESSION;
    private final static String TEST_NAME_EXPRESSION;
    private final static String TEST_CODE_SYS_EXPRESSION;
    private final static List<String> testPlacerOrderNumberExpressions = new ArrayList<String>();
    private final static List<String> testFillerOrderNumberExpressions = new ArrayList<String>();
    private final static String TEST_DATE_EXPRESSION;
    private final static String SPECIMEN_TEXT_EXPRESSION;
    private final static String OBR_ALT_CODE_EXPRESSION;
    private final static String OBR_ALT_CODE_TEXT_EXPRESSION;
    private final static String OBR_ALT_CODE_SYS_EXPRESSION;
    private final static String OBR_PROVIDER_EXPRESSION;    
    private final static String OBR_SET_ID_EXPRESSION;
    private final static String PRINCIPAL_RESULT_INTERPRETER_ID_EXPRESSION;
    private final static String PRINCIPAL_RESULT_INTERPRETER_ID_ALT_EXPRESSION;
    private final static String ASSISTANT_RESULT_INTERPRETER_ID_EXPRESSION;
    private final static String ASSISTANT_RESULT_INTERPRETER_ID_ALT_EXPRESSION;
    private final static String RESULT_COPY_TO_EXPRESSION;    
    
    private Node obrNode;  
    private HL7NameHandler nameHandler;
    private Node providerNode;
    private Node resultCopyToNode;
    
    static
    {
        try
        {
            TEST_ID_EXPRESSION = "./OBR.4/CE.1/text()";
            TEST_NAME_EXPRESSION = "./OBR.4/CE.2/text()";
            TEST_CODE_SYS_EXPRESSION = "./OBR.4/CE.3/text()";
            testPlacerOrderNumberExpressions.add("./OBR.2/EI.1/text()");		// Original
            testPlacerOrderNumberExpressions.add("./OBR.2/CM.1/text()");		// HL7 2.1
            testPlacerOrderNumberExpressions.add("./OBR.2/CM_PLACER.1/text()");	// HL7 2.2
            testFillerOrderNumberExpressions.add("./OBR.3/EI.1/text()");		// Original
            testFillerOrderNumberExpressions.add("./OBR.3/CM.1/text()");		// HL7 2.1
            testFillerOrderNumberExpressions.add("./OBR.3/CM_FILLER.1/text()");	// HL7 2.2
            TEST_DATE_EXPRESSION = "./OBR.7/TS.1/text()";
            SPECIMEN_TEXT_EXPRESSION = "./OBR.15/SPS.1/CE.1/text()";
            OBR_ALT_CODE_EXPRESSION = "./OBR.4/CE.4/text()";
            OBR_ALT_CODE_TEXT_EXPRESSION = "./OBR.4/CE.5/text()";
            OBR_ALT_CODE_SYS_EXPRESSION = "./OBR.4/CE.6/text()";
            OBR_PROVIDER_EXPRESSION = "./OBR.16";
            OBR_SET_ID_EXPRESSION = "./OBR.1/text()";
            PRINCIPAL_RESULT_INTERPRETER_ID_EXPRESSION = "./OBR.32/NDL.1/CNN.1/text()";
            PRINCIPAL_RESULT_INTERPRETER_ID_ALT_EXPRESSION = "./OBR.32/CM_NDL.1/CN.1/text()";
            ASSISTANT_RESULT_INTERPRETER_ID_EXPRESSION = "./OBR.33/NDL.1/CNN.1/text()";
            ASSISTANT_RESULT_INTERPRETER_ID_ALT_EXPRESSION = "./OBR.33/CM_NDL.1/CN.1/text()";
            RESULT_COPY_TO_EXPRESSION = "./OBR.28";
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public OrderObservation(Node node)
    {
        obrNode = node;        
        nameHandler = NameFactory.getNameHandler(node);
        providerNode = XmlUtilities.findHL7Part(OBR_PROVIDER_EXPRESSION, obrNode);
        resultCopyToNode = XmlUtilities.findHL7Part(RESULT_COPY_TO_EXPRESSION, obrNode);
    }
    
    public boolean equals(Object obj)
    {
        boolean retVal = false;
        if (obj instanceof OrderObservation) {        
            OrderObservation otherObr = (OrderObservation)obj;
            retVal = obrNode.equals(otherObr.obrNode);
        }
        return retVal;
    }
    
    public int hashCode()
    {
        return obrNode.hashCode();
    }
    
    public int getTestSetId()
    {
        String setId = XmlUtilities.findFieldValue(OBR_SET_ID_EXPRESSION, (Element)obrNode);
        int retVal = 0;
        try
        {
            retVal = Integer.valueOf(setId);
        }
        catch (NumberFormatException e)
        {
            // not a valid integer (probably ""), so we'll use a set ID of 0.            
        }
        return retVal;
    }

    public String getTestIdentifier() 
    {
        return XmlUtilities.findFieldValue(TEST_ID_EXPRESSION, (Element)obrNode);
    }

    public String getTestName() 
    {
        return XmlUtilities.findFieldValue(TEST_NAME_EXPRESSION, (Element)obrNode);
    }

    public String getTestCodeSystem() 
    {
        return XmlUtilities.findFieldValue(TEST_CODE_SYS_EXPRESSION, (Element)obrNode);
    }

    public String getTestPlacerOrderNumber() 
    {
    	return XmlUtilities.findFieldValue(testPlacerOrderNumberExpressions, obrNode);
    }

    public String getTestFillerOrderNumber() 
    {
    	return XmlUtilities.findFieldValue(testFillerOrderNumberExpressions, obrNode);
    }

    public Date getTestDate() throws ParseException
    {
        Date testDate = null;
        String dateString = XmlUtilities.findFieldValue(TEST_DATE_EXPRESSION, (Element)obrNode);
        if (StringUtils.isNotEmpty((dateString)))
        {    
        	testDate = DateUtilities.tryParseDate("yyyyMMddHHmmss", dateString);
        	if (testDate == null) {
        		testDate = DateUtilities.tryParseDate("yyyyMMddHHmm", dateString);
        	}
            if (testDate == null)
            {
                testDate = DateUtilities.tryParseDate("yyyyMMdd", dateString);
            }            
        }
        return testDate;
    }
        

    public String getSpecimenText() 
    {
        return XmlUtilities.findFieldValue(SPECIMEN_TEXT_EXPRESSION, (Element)obrNode);
    }

    public String getOrderObservationAltCode() 
    {
        return XmlUtilities.findFieldValue(OBR_ALT_CODE_EXPRESSION, (Element)obrNode);
    }

    public String getOrderObservationAltCodeText() 
    {
        return XmlUtilities.findFieldValue(OBR_ALT_CODE_TEXT_EXPRESSION, (Element)obrNode);
    }

    public String getOrderObservationAltCodeSystem() 
    {
        return XmlUtilities.findFieldValue(OBR_ALT_CODE_SYS_EXPRESSION, (Element)obrNode);
    }
    
    public String getProviderLocalId() 
    {    	
        return HL7NameHandler.useNameHandler(nameHandler, providerNode, NamePart.ID);
    }
    
    public String getProviderLastName() 
    {    	
    	return HL7NameHandler.useNameHandler(nameHandler, providerNode, NamePart.FAMILY_NAME);
    }
    
    public String getProviderFirstName() 
    {    	
    	return HL7NameHandler.useNameHandler(nameHandler, providerNode, NamePart.GIVEN_NAME);
    }
    
    public String getProviderMiddleName() 
    {    	
    	return HL7NameHandler.useNameHandler(nameHandler, providerNode, NamePart.MIDDLE_NAME);
    }
    
    public String getProviderSuffixName() 
    {    	
    	return HL7NameHandler.useNameHandler(nameHandler, providerNode, NamePart.SUFFIX_NAME);
    }
    
    public String getProviderFullName() 
    {
        String familyName = getProviderLastName();
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
    
    public String getPrincipalResultInterpreterIdentifier() {
    	String principalResultInterpreter = XmlUtilities.findFieldValue(PRINCIPAL_RESULT_INTERPRETER_ID_EXPRESSION, obrNode);
    	if (StringUtils.isEmpty(principalResultInterpreter)) {
    		principalResultInterpreter = 
    			XmlUtilities.findFieldValue(PRINCIPAL_RESULT_INTERPRETER_ID_ALT_EXPRESSION, obrNode);
    	}
    	return principalResultInterpreter;
    }
    
    public String getAssistantResultInterpreterIdentifier() {
    	String assistantResultInterpreter = XmlUtilities.findFieldValue(ASSISTANT_RESULT_INTERPRETER_ID_EXPRESSION, obrNode);
    	if (StringUtils.isEmpty(assistantResultInterpreter)) {
    		assistantResultInterpreter = 
    			XmlUtilities.findFieldValue(ASSISTANT_RESULT_INTERPRETER_ID_ALT_EXPRESSION, obrNode);
    	}
    	return assistantResultInterpreter;
    }
    
    public String getResultCopyToIdentifier() {    	
    	return HL7NameHandler.useNameHandler(nameHandler, resultCopyToNode, NamePart.ID);
    }
    
    public String getResultCopyToLastName() {    	
    	return HL7NameHandler.useNameHandler(nameHandler, resultCopyToNode, NamePart.FAMILY_NAME);
    }
    
    public String getResultCopyToFirstName() {    	
    	return HL7NameHandler.useNameHandler(nameHandler, resultCopyToNode, NamePart.GIVEN_NAME);
    }
    
    public String getResultCopyToMiddleName() {    	
    	return HL7NameHandler.useNameHandler(nameHandler, resultCopyToNode, NamePart.MIDDLE_NAME);
    }
    
    public String getResultCopyToSuffixName() {    	
    	return HL7NameHandler.useNameHandler(nameHandler, resultCopyToNode, NamePart.SUFFIX_NAME);
    }
}


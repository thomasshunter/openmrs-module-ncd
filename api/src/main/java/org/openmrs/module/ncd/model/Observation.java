/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.model;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.critic.ConditionExtractor.CodeSystemPair;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class allows easy retrieval of Observation data (OBX).
 * 
 * @author jlbrown
 *
 */
public class Observation implements IResultSegment 
{       
    private Node obxNode;
    private final static String LAB_ID_EXPRESSION;
    private final static String TEST_DATE_EXPRESSION;
    private final static String TEST_DATA_TYPE_EXPRESSION;
    private final static String TEST_NORMAL_RANGE_EXPRESSION;
    private final static String TEST_ABNORMAL_FLAG_EXPRESSION;
    private final static String TEST_RESULT_ID_EXPRESSION;
    private final static String TEST_RESULT_NAME_EXPRESSION;
    private final static String TEST_RESULT_CODE_EXPRESSION;
    private final static String TEST_RESULT_CODE_SYS_EXPRESSION;
    private final static String TEST_RESULT_SUB_ID_EXPRESSION;        
    private final static String TEST_RESULT_UNITS_EXPRESSION;
    private final static String TEST_RESULT_STATUS_EXPRESSION;
    private final static String OBX_ALT_CODE_EXPRESSION;
    private final static String OBX_ALT_CODE_TEXT_EXPRESSION;
    private final static String OBX_ALT_CODE_SYS_EXPRESSION;
    private final static String OBX_SET_ID_EXPRESSION;
    private final static String OBX5_EXPRESSION;
    private final static String OBR_EXPRESSION;
    private final static String ORC_EXPRESSION;
    private final static String MSH_EXPRESSION;
    private final String PID_EXPRESSION;
    private final String PID_ALT_EXPRESSION;
    private final String PID_TOM_TEST;
    private final String PD1_EXPRESSION;
    private final String PD1_ALT_EXPRESSION;
    private final String PV1_EXPRESSION; 
    private final String PV1_ALT_EXPRESSION;
    private final String PV1_ALT2_EXPRESSION;
    private final String PV1_ALT3_EXPRESSION;
    private final String ZID_EXPRESSION;
    private final String ZID_ALT1_EXPRESSION;
    private final String ZLR_EXPRESSION;    
    
    private final static String LOINC_CODE_SYSTEM = "LN";
    
    static
    {
        try
        {
            LAB_ID_EXPRESSION               = "./OBX.15/CE.1/text()";
            TEST_DATE_EXPRESSION            = "./OBX.14/TS.1/text()";
            TEST_DATA_TYPE_EXPRESSION       = "./OBX.2/text()";
            TEST_NORMAL_RANGE_EXPRESSION    = "./OBX.7/text()";
            TEST_ABNORMAL_FLAG_EXPRESSION   = "./OBX.8/text()";
            TEST_RESULT_ID_EXPRESSION       = "./OBX.3/CE.1/text()";
            TEST_RESULT_CODE_EXPRESSION     = "./OBX.3/CE.1/text()";
            TEST_RESULT_NAME_EXPRESSION     = "./OBX.3/CE.2/text()";
            TEST_RESULT_CODE_SYS_EXPRESSION = "./OBX.3/CE.3/text()";
            TEST_RESULT_SUB_ID_EXPRESSION   = "./OBX.4/CE.1/text()";
            TEST_RESULT_UNITS_EXPRESSION    = "./OBX.6/CE.1/text()";
            TEST_RESULT_STATUS_EXPRESSION   = "./OBX.11/text()";
            OBX_ALT_CODE_EXPRESSION         = "./OBX.3/CE.4/text()";
            OBX_ALT_CODE_TEXT_EXPRESSION    = "./OBX.3/CE.5/text()";
            OBX_ALT_CODE_SYS_EXPRESSION     = "./OBX.3/CE.6/text()";
            OBX_SET_ID_EXPRESSION           = "./OBX.1/text()";
            OBX5_EXPRESSION                 = "./OBX.5";                              
            OBR_EXPRESSION                  = "../../OBR";
            ORC_EXPRESSION                  = "../../ORC";
            MSH_EXPRESSION                  = "../../../../MSH";
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
            
    public Observation(Node node)
    {
        obxNode                 = node;
        
        Node msh9               = XmlUtilities.findHL7Part("//MSH/MSH.9", obxNode.getOwnerDocument().getDocumentElement());
        String msgType          = msh9.getFirstChild().getTextContent() + "_" + msh9.getLastChild().getTextContent();
        
        // NOTE: The PIDPD1NK1NTEPV1PV2 alternates below are apparently necessary for HL7 v2.3.1 and 2.4.
        // Unlike all other HL7 versions, the RIM documents use weird names instead of .PATIENT, and
        // HAPI slavishly follows suit. (erik horstkotte 9/9/2010)
        
        PID_EXPRESSION          = "../../../" + msgType + ".PATIENT/PID";
        PID_ALT_EXPRESSION      = "../../../" + msgType + ".PIDPD1NK1NTEPV1PV2/PID";
        PID_TOM_TEST            = "//PID";
        
        PD1_EXPRESSION          = "../../../" + msgType + ".PATIENT/" + msgType + ".VISIT/PD1";
        PD1_ALT_EXPRESSION      = "../../../" + msgType + ".PIDPD1NK1NTEPV1PV2/" + msgType + ".VISIT/PD1";
        PV1_EXPRESSION          = "../../../" + msgType + ".PATIENT/" + msgType + ".VISIT/PV1";
        PV1_ALT_EXPRESSION      = "../../../" + msgType + ".PATIENT/PV1";
        PV1_ALT2_EXPRESSION     = "../../../" + msgType + ".PIDPD1NK1NTEPV1PV2/" + msgType + ".VISIT/PV1";
        PV1_ALT3_EXPRESSION     = "../../../" + msgType + ".PIDPD1NK1NTEPV1PV2/PV1";
        ZID_EXPRESSION          = "../../../" + msgType + ".PATIENT/ZID";		// 2.1, 2.2
        ZID_ALT1_EXPRESSION     = "../../../" + msgType + ".VISIT/ZID";	// 2.3
        ZLR_EXPRESSION          = "../../"    + msgType + ".OBSERVATION/ZLR";
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getPatientInfo()
     */
    public PatientInfo getPatientInfo() 
    {
        Node pidNode = XmlUtilities.findHL7Part(PID_EXPRESSION, (Element)obxNode);
        
        if (pidNode == null) 
        {
            pidNode = XmlUtilities.findHL7Part(PID_ALT_EXPRESSION, (Element)obxNode);
        }
        
        if( pidNode == null )
        {
            pidNode = XmlUtilities.findHL7Part(PID_TOM_TEST,  (Element)obxNode ); // //PID/PID.3
        }
        
        Node zidNode = XmlUtilities.findHL7Part(ZID_EXPRESSION, (Element)obxNode);
        
        if (zidNode == null) 
        {
            zidNode = XmlUtilities.findHL7Part(ZID_ALT1_EXPRESSION, (Element)obxNode);
        }
        
        if( zidNode == null )
        {
            zidNode = XmlUtilities.findHL7Part( "//ZID", (Element)obxNode );
        }
        
        return new PatientInfo(pidNode, zidNode);
    }
       
    public String getConcatenatedTestResultValues(List<Node> msgSegments) 
    {
    	StringBuilder obx5Values = new StringBuilder();
    	
    	for(Node msgSegment : msgSegments) 
    	{
    		if (msgSegment.getNodeName().equals("OBX")) 
    		{
	    		Observation obx = new Observation(msgSegment);
	    		obx5Values.append(obx.getTestResultValue());
    		}
    	}
    	
    	return obx5Values.toString();
    }
    
    public String getNodeText()
    {        
        try
        {
            return XmlUtilities.translateNodeToString(obxNode);
        }
        catch (TransformerException e)
        {
            return ""; 
        }
    }
    
    public int getTestSetId()
    {
        String setId = XmlUtilities.findFieldValue(OBX_SET_ID_EXPRESSION, (Element)obxNode);
        int retVal = 0;
        
        try
        {
            retVal = Integer.valueOf(setId);
        }
        catch (NumberFormatException e)
        {
            // nothing we can do, so we'll use a set ID of 0.            
        }
        
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getLabIdentifier()
     */
    public String getLabIdentifier() 
    {
        return XmlUtilities.findFieldValue(LAB_ID_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestDate()
     */
    public Date getTestDate() throws ParseException
    {
        Date testDate       = null;
        String dateString   = XmlUtilities.findFieldValue(TEST_DATE_EXPRESSION, (Element)obxNode);
        
        if( StringUtils.isNotEmpty( dateString ) )
        {    
        	testDate = DateUtilities.tryParseDate("yyyyMMddHHmmss", dateString);
        	
        	if (testDate == null) 
        	{
        		testDate = DateUtilities.tryParseDate("yyyyMMddHHmm", dateString);
        	}
            
        	if (testDate == null)
            {
                testDate = DateUtilities.tryParseDate("yyyyMMdd", dateString);
            }
        }
        
        return testDate;
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestDataType()
     */
    public String getTestDataType() 
    {
        return XmlUtilities.findFieldValue(TEST_DATA_TYPE_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getNormalRange()
     */
    public String getNormalRange() 
    {
        return XmlUtilities.findFieldValue(TEST_NORMAL_RANGE_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getAbnormalFlag()
     */
    public String getAbnormalFlag() 
    {
        return XmlUtilities.findFieldValue(TEST_ABNORMAL_FLAG_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestResultIdentifier()
     */
    public String getTestResultIdentifier() 
    {
        return XmlUtilities.findFieldValue(TEST_RESULT_ID_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestResultName()
     */
    public String getTestResultName() 
    {
        return XmlUtilities.findFieldValue(TEST_RESULT_NAME_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestResultCodeSystem()
     */
    public String getTestResultCodeSystem() 
    {
        return XmlUtilities.findFieldValue(TEST_RESULT_CODE_SYS_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestResultSubIdentifier()
     */
    public String getTestResultSubIdentifier() 
    {
        return XmlUtilities.findFieldValue(TEST_RESULT_SUB_ID_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestResultCode()
     */
    public String getTestResultCode() 
    {        
        return XmlUtilities.findFieldValue(TEST_RESULT_CODE_EXPRESSION, obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestResultValue()
     */
    public String getTestResultValue() 
    {
        //We can't just use an XPath expression because of the differing
        //datatypes that affect the XML structure of the OBX 5 node.
        //We'll get the OBX 5 and then iterate through the children
        //and get each child node's text.
        
        StringBuffer retVal     = new StringBuffer();
        Node obx5Node           = XmlUtilities.findHL7Part(OBX5_EXPRESSION, (Element)obxNode);
        
        while (obx5Node != null && obx5Node.getNodeName().equals("OBX.5"))        
        {           
            NodeList obx5ChildNodes = obx5Node.getChildNodes();
            
            for (int idx = 0; idx < obx5ChildNodes.getLength(); idx++ )
            {
                String textContent = obx5ChildNodes.item(idx).getTextContent();
                
                if( StringUtils.isNotEmpty((textContent)) )
                {
                    retVal.append(obx5ChildNodes.item(idx).getTextContent());
                    retVal.append(" ");
                }
            }
            
            obx5Node = obx5Node.getNextSibling();
        }
        
        return retVal.toString().trim();
    }
    
    public String getTestResultValueCode() 
    {
    	//We can't just use an XPath expression because of the differing
        //datatypes that affect the XML structure of the OBX 5 node.
        //We'll get the OBX 5.1 by getting the OBX 5, then getting the
        //first child, and then getting the text content inside that child.
    
        Node obx5Node   = XmlUtilities.findHL7Part(OBX5_EXPRESSION, (Element)obxNode); 
        Node obx51Node  = (obx5Node != null ? obx5Node.getFirstChild() : null);
        
        return (obx51Node != null ? obx5Node.getFirstChild().getTextContent() : null);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestResultUnits()
     */
    public String getTestResultUnits() 
    {
        return XmlUtilities.findFieldValue(TEST_RESULT_UNITS_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getTestResultStatus()
     */
    public Character getTestResultStatus() 
    {
        String testResultStatus = XmlUtilities.findFieldValue(TEST_RESULT_STATUS_EXPRESSION, (Element)obxNode);
        
        if (StringUtils.isEmpty(testResultStatus))
        {
            return null;
        }
        else
        {
            return testResultStatus.charAt(0);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getObservationAltCode()
     */
    public String getTestResultAltCode() 
    {
        return XmlUtilities.findFieldValue(OBX_ALT_CODE_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getObservationAltCodeText()
     */
    public String getTestResultAltCodeText() 
    {
        return XmlUtilities.findFieldValue(OBX_ALT_CODE_TEXT_EXPRESSION, (Element)obxNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getObservationAltCodeSys()
     */
    public String getTestResultAltCodeSys() 
    {
        return XmlUtilities.findFieldValue(OBX_ALT_CODE_SYS_EXPRESSION, (Element)obxNode);
    }
    
    /**
     * Get the result code (OBX-3) that corresponds to the specified code
     * system.
     * 
     * @param codeSystem
     *            The code system for which you want the code.
     * @return The code for the specified code system or null.
     */
    public String getTestResultCodeByCodeSystem(String codeSystem)
    {
        String loincCode = null;
        
        if (getTestResultCodeSystem().equals(codeSystem))
        {
            loincCode = getTestResultIdentifier();
        }
        else if (getTestResultAltCodeSys().equals(codeSystem))
        {
            loincCode = getTestResultAltCode();
        }
        
        return loincCode;
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getOrderObservation()
     */
    public OrderObservation getOrderObservation() 
    {
        Node obrNode = XmlUtilities.findHL7Part(OBR_EXPRESSION, (Element)obxNode);
        
        return new OrderObservation(obrNode);
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getProvider()
     */
    public Provider getProvider() 
    {
        Node pv1Node = XmlUtilities.findHL7Part(PV1_EXPRESSION, (Element)obxNode);
        
        if (pv1Node == null)
        {
            pv1Node = XmlUtilities.findHL7Part(PV1_ALT_EXPRESSION, (Element)obxNode);
        }
        
        if (pv1Node == null)
        {
            pv1Node = XmlUtilities.findHL7Part(PV1_ALT2_EXPRESSION, (Element)obxNode);
        }
        
        if (pv1Node == null)
        {
            pv1Node = XmlUtilities.findHL7Part(PV1_ALT3_EXPRESSION, (Element)obxNode);
        }
        
        if( pv1Node == null )
        {
            pv1Node = XmlUtilities.findHL7Part( "//PV1", (Element)obxNode);
        }
                
        Node zlrNode = XmlUtilities.findHL7Part(ZLR_EXPRESSION, (Element)obxNode);
        
        if( zlrNode == null )
        {
            zlrNode = XmlUtilities.findHL7Part( "//ZLR", (Element)obxNode);
        }
        
        Provider theProvider        = new Provider( pv1Node, zlrNode );
                
        return theProvider;
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.model.IResultSegment#getOrderCommon()
     */
    public OrderCommon getOrderCommon() 
    {
        Node orcNode = XmlUtilities.findHL7Part(ORC_EXPRESSION, (Element)obxNode);
        
        return new OrderCommon(orcNode);
    }
    
    public MessageHeader getMessageHeader()
    {
        Node mshNode = XmlUtilities.findHL7Part(MSH_EXPRESSION, (Element)obxNode);
        
        return new MessageHeader(mshNode);
    }   
    
    public String getLoincCode()
    {
        String loincCode            = null;
        String loincCodeExpression  = determineLoincCodeExpression();
        
        if (StringUtils.isNotEmpty((loincCodeExpression)))
        {
            loincCode = XmlUtilities.findFieldValue(loincCodeExpression, (Element)obxNode);
        }
        
        return loincCode;
    }
    
    public String getNonLoincObxValue() 
    {
        String obxValue = null;
        String loincCodeExpression = determineLoincCodeExpression();
        // The "obx" value comes from whichever of the OBX3.1 or OBX3.4
        // that doesn't contain the loinc code.
    
        if (loincCodeExpression != null && loincCodeExpression.equals(TEST_RESULT_ID_EXPRESSION)) 
        {
            // The loinc code is in OBX3.1, so use the OBX 3.4 value.             
            obxValue = getTestResultAltCode();
        } 
        else 
        {
            // Since there's no loinc code or the loinc code is in OBX3.4, use the OBX3.1 value.
            obxValue = getTestResultIdentifier();
        }
        
        return obxValue;
    }
    
    private String determineLoincCodeExpression() 
    {        
        String primaryCodeType      = getTestResultCodeSystem();
        String secondaryCodeType    = getTestResultAltCodeSys();
        String LOINC_CODE_TYPE      = "LN";
        String OBX3_FORMAT_STRING   = "./OBX.3/CE.%1$d/text()";

        int loincCodePosition;

        if (primaryCodeType != null && primaryCodeType.equals(LOINC_CODE_TYPE)) 
        {
            loincCodePosition = 1;
        } 
        else if (secondaryCodeType != null && secondaryCodeType.equals(LOINC_CODE_TYPE)) 
        {
            loincCodePosition = 4;
        } 
        else 
        {
            return null;
        }

        String xPathString = String.format(OBX3_FORMAT_STRING, loincCodePosition);
        
        return xPathString;
    }

	public void addDecidedResultIndexInfo(DecidedResult template) 
	{ 
		OrderObservation obr = getOrderObservation();
	
		if (obr != null) 
		{
			template.setObr(obr.getTestIdentifier());             //OBR-4.1
			template.setObrCodeSystem(obr.getTestCodeSystem());   //OBR-4.3
		}
		
		template.setObx(getNonLoincObxValue());                   //OBX-3.1 or OBX-3.4
		String obxCodeSystem = getTestResultCodeSystem();         //OBX-3.3
		String obxAltCodeSystem = getTestResultAltCodeSys();      //OBX-3.6
		
		template.setObxCodeSystem(obxCodeSystem.equals("LN") ? obxAltCodeSystem : obxCodeSystem);
	}

	public void addDecidedResultSpecifics(DecidedResult template) 
	{
		template.setDataType(getTestDataType());
		String loincCode      = getLoincCode();
		template.setLoincCode(loincCode);
		OrderObservation obr  = getOrderObservation();
	
		if (obr != null) 
		{
			template.setObrText(obr.getTestName());
			template.setObrAltText(obr.getOrderObservationAltCodeText());
		}
		
		template.setResultCode(getNonLoincObxValue());
		template.setObxText(getTestResultName());
		template.setObxAltText(getTestResultAltCodeText());
	}

	public CodeSystemPair getCodeSystemPair() 
	{
		CodeSystemPair retVal     = new CodeSystemPair();
		String loincCode          = getLoincCode();
    
		if( StringUtils.isNotEmpty(loincCode) ) 
		{
            retVal.setCode(loincCode);
            retVal.setCodeSystem(LOINC_CODE_SYSTEM);                
        }
        
		return retVal;
	}

	public Map<String, Staff> getStaffMap() 
	{		
		return Staff.getStaffMap(obxNode);
	}
	
	public PatientAdditionalDemographics getPatientAdditionalDemographics() 
	{
		Node pd1Node = XmlUtilities.findHL7Part(PD1_EXPRESSION, obxNode);
    
		if (pd1Node == null)
        {
        	pd1Node = XmlUtilities.findHL7Part(PD1_ALT_EXPRESSION, (Element)obxNode);
        }
		
		return new PatientAdditionalDemographics(pd1Node);
	}
}

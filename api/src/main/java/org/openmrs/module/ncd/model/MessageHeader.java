/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Node;

/**
 *
 */
public class MessageHeader {

    private Node mshNode;
        
    private static final String MESSAGE_TYPE_EXPRESSION = "./MSH.9";
    private static final String MESSAGE_TYPE_PART1_EXPRESSION = "./MSG.1/text()";
    private static final String MESSAGE_TYPE_PART2_EXPRESSION = "./MSG.2/text()";
    private static final String MESSAGE_TYPE_PART1_ALT_EXPRESSION = "./CM_MSG.1/text()";
    private static final String MESSAGE_TYPE_PART2_ALT_EXPRESSION = "./CM_MSG.2/text()";
    private static final String MESSAGE_CONTROL_ID_EXPRESSION ="./MSH.10/text()";
    private static final String PROCESSING_ID_EXPRESSION = "//MSH/MSH.11/text()";
    private static final String PROCESSING_ID_ALT_EXPRESSION = "//MSH/MSH.11/PT.1/text()";
    
    public MessageHeader(Node mshNode) {
        this.mshNode = mshNode; 
    }
    
    public static String getFieldSeparator (String message) {
    	return message.substring(3, 4);
    }
    
    public static String getComponentSeparator(String message) {
		return message.substring(4, 5);
	}
    
    public static String getRepetitionSeparator(String message) {
    	return message.substring(5, 6);
    }
    
    public static String getEscapeCharacter(String message) {
    	return message.substring(6, 7);
    }
    
    public static String getSubcomponentSeparator(String message) {
    	return message.substring(7, 8);
    }
    
    public static String getSendingApplication(String message) {
    	String fieldSep = MessageHeader.getFieldSeparator(message);
    	Pattern appPattern = Pattern.compile("MSH\\" + fieldSep + ".*?\\" + fieldSep + "(.*?)\\" + fieldSep);        
        String retVal = null;
        if (message != null) {
	        Matcher appMatcher = appPattern.matcher(message);
	        if (appMatcher.find())
	        {
	             retVal = appMatcher.group(1);
	        }
        }
        
        return retVal;
    }
    
    public static String getSendingFacility(String message) {
    	String fieldSep = MessageHeader.getFieldSeparator(message);
    	Pattern appPattern = Pattern.compile("MSH\\" + fieldSep + ".*?\\" + fieldSep + ".*?\\" + 
    			fieldSep + "(.*?)\\" + fieldSep);        
        String retVal = null;
        if (message != null) {
	        Matcher appMatcher = appPattern.matcher(message);
	        if (appMatcher.find())
	        {
	             retVal = appMatcher.group(1);
	        }
        }
        
        return retVal;
    }        	
    
    public String getMessageType() {
    	String msgType = null;
    	Node msh9Node = XmlUtilities.findHL7Part(MESSAGE_TYPE_EXPRESSION, mshNode);
    	
    	msgType = XmlUtilities.findFieldValue(MESSAGE_TYPE_PART1_EXPRESSION, msh9Node)
        	+ XmlUtilities.findFieldValue(MESSAGE_TYPE_PART2_EXPRESSION, msh9Node);
    	if (StringUtils.isEmpty(msgType))
    	{
    		msgType = XmlUtilities.findFieldValue(MESSAGE_TYPE_PART1_ALT_EXPRESSION, msh9Node)
    			+ XmlUtilities.findFieldValue(MESSAGE_TYPE_PART2_ALT_EXPRESSION, msh9Node);
    	}
    	
    	return msgType;
    }
    
    public String getMessageControlIdentifier() {
    	return XmlUtilities.findFieldValue(MESSAGE_CONTROL_ID_EXPRESSION, mshNode);
    }
    
    public String getMessageProcessingId() {
    	String processingId = XmlUtilities.findFieldValue(PROCESSING_ID_EXPRESSION, mshNode);
    	if (StringUtils.isEmpty(processingId)) {
    		processingId = XmlUtilities.findFieldValue(PROCESSING_ID_ALT_EXPRESSION, mshNode);
    	}
    	
    	return processingId;
    }
}

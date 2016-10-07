/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 */
public class Zvx {

    private Node zvxNode;
    private static final String MPQ_EXPRESSION = "./ZVX.3/text()";
    private static final String MPQ_ALT_EXPRESSION = "./ZVX.4/text()";
    
    public Zvx(Node node) {
        zvxNode = node;
    }
    
    /**
     * Use this method if you only have the string message and not the
     * parsed-to-XML message.  Looks for the MPQ# in the ZVX-3 with the caveat that the
     * ZVX-3 must have at least one digit to be consider a valid MPQ.  If nothing is there
     * or no digit is present, and the ZVX-4 is not empty, use the ZVX-4 no matter what the 
     * format.  If the ZVX-4 is empty, null is returned.
     * @param message A string containing the HL7 message
     * @return The MPQ# from the HL7 message's ZVX segment or null if no MPQ# is present.
     */
    public static String getMpq(String message) {
    	final String ZVX_REGEXP = "ZVX\\|.*?\\|.*?\\|(.*?)\\|(.*?)\\|";
    	Pattern mpqPattern = Pattern.compile(ZVX_REGEXP, Pattern.MULTILINE);
    	Matcher mpqMatcher = mpqPattern.matcher(message);
    	String mpqNum = null;

    	if (mpqMatcher.find()) {
    		String zvx3 = mpqMatcher.group(1);
    		String zvx4 = mpqMatcher.group(2);
    		
    		if (StringUtils.isNotEmpty(zvx3) && hasDigit(zvx3)) {
    			mpqNum = zvx3;
    		} else {
    			mpqNum = zvx4;
    		}
    	}
    	
    	return mpqNum;
    }
        
    public String getMpq() {
        String mpq = XmlUtilities.findFieldValue(MPQ_EXPRESSION, (Element)zvxNode);
        if (StringUtils.isEmpty(mpq) || ! hasDigit(mpq)) {
            mpq = XmlUtilities.findFieldValue(MPQ_ALT_EXPRESSION, (Element)zvxNode);
        }
        return mpq;
    }
    
    private static boolean hasDigit(String value) {
        Pattern digitPattern = Pattern.compile("\\d+$");
        Matcher digitMatcher = digitPattern.matcher(value);
        return digitMatcher.find();
    }
}

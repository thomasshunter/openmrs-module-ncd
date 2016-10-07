/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * @author jlbrown
 *
 */
public class HL7Utilities
{
    /**
     * Index in HL7 string of the field separator character.
     */
    public final static int FIELD_SEPARATOR_INDEX = 3;
    
    public final static String SEGMENT_SEPARATOR = "\r";
    
    private static StringBuilder msgBuilder = new StringBuilder();
    
    /**
     * Reads one HL7 message from a file. Further calls will read each message
     * in succession from the file until the end of file is reached.
     * 
     * @param fileReader
     *            A BufferedReader linked to the file to be read.
     * @param isFirstMsg
     *            A boolean to indicate if this is the first message read from
     *            the file.
     * @return A String containing the message or null if the end of file for
     *         the specified BufferedReader was reached during a this or a
     *         previous call.
     * @throws IOException
     */
    public static String readMessageFromFile(BufferedReader fileReader, boolean isFirstMsg) throws IOException
    {
        String retVal = null;
        boolean atLeastOneLineRead = false;
        
        String line = fileReader.readLine();
        
        // Keep reading lines from the file until one of the following:
        // a) The end of file is reached.
        // b) The start of the next message is reached.
        while (line != null && (isFirstMsg || ! line.startsWith("MSH|")))
        {
            atLeastOneLineRead = true;
            isFirstMsg = false;
            msgBuilder.append(line + "\r");
            line = fileReader.readLine();
        }
        
        if (atLeastOneLineRead)
        {
            // Save off the current message for return.
            retVal = msgBuilder.toString();            
        }
        
        // Reset the state of the class in preparation for 
        // future calls to this utility.
        msgBuilder = new StringBuilder();
        
        if (line != null)
        {            
            // We ended the current message because we found
            // the start of the next message, so save the
            // start of the next message for the next call
            // to this utility.
            msgBuilder.append(line + "\r");
        }
        
        return retVal;
    }
    
    public static Parser createNoValidationParser()
    {
        Parser parser = new PipeParser();
        parser.setValidationContext(new NoValidation());        
        return parser;
    }

    /**
     * Convert a String containing an HL7 message into a form more suitable
     * for display on a web page.
     * 
     * - Converts &, < and > to &amp; &lt; and &gt; respectively.
     * - Removes \r\n, \r and \n.
     * - Wraps message segments in <p class="hl7">.
     * - Wraps the entire formatted message in <code class="hl7">.
     * 
     * TODO:
     * - Wraps long segments onto multiple lines, indenting the wrapped
     *   lines by enclosing them in <p class="hl7,hl7continuation">.
     * 
     * @param msg The HL7 message to be reformatted.
     * @param maxwidth The maximum number of characters in a segment before
     * the segment gets wrapped.
     * @return A prettified version of the message suitable for display on
     * a web page.
     */
    public static String toHTML(String msg, int maxwidth) {

        StringBuilder buf = new StringBuilder();

        String modified = msg.replaceAll("\r\n", "\r");
        modified = modified.replaceAll("\n", "\r");
        String[] segments = modified.split("\r");

        for (String segment : segments) {

            while (segment.length() > maxwidth) {
                
                // chip off a piece from the start of the segment
                int wrapindex = segment.lastIndexOf('|', maxwidth - 1);
                if (wrapindex <= 0) {
                    wrapindex = segment.indexOf('|', 1);
                    if (wrapindex <= 0) {
                        break;
                    }
                }

                buf.append(escapeHTML(segment.substring(0, wrapindex)));
                
                buf.append("<br/>\n&nbsp;&nbsp;");
                segment = segment.substring(wrapindex);                
            }
            
            buf.append(escapeHTML(segment));

            buf.append("<br/>\n");
        }

        return "<code class=\"hl7\">" + buf.toString() + "</code>";
    }

    /**
     * Creates a copy of the supplied text with all characters that have
     * special meaning in HTML replaced with "safe" substitutions.
     * 
     * @param text
     * @return The escaped copy of the String
     */
    private static String escapeHTML(String text) {
        
        // NOTE: The order of the replacements matters
        
        String modified = text.replaceAll("&", "&amp;");
        modified = modified.replaceAll(">", "&gt;");
        return modified.replaceAll("<", "&lt;");
    }
    
    public static String replaceField(String src, int index, String value) {
    	
    	final String fieldSeparator = "|";
    	String[] fields = src.split(Pattern.quote(fieldSeparator));
    	if (index < fields.length) {
    		fields[index] = value;
    	}
    	return StringUtilities.merge(fields, fieldSeparator);
    }
}

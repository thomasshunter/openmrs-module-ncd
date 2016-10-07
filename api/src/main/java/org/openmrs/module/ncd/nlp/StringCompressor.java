/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.nlp;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

/**
 * Compresses a string by removing spaces, question marks, and commas.
 */
public class StringCompressor {
    
    private static ArrayList<Character> compressableCharsList = null;
    
    static
    {
        createCompressableCharsList();
    }
    
    static private void createCompressableCharsList()
    {
        compressableCharsList = new ArrayList<Character>();
        compressableCharsList.add(' ');
        compressableCharsList.add('?');
        compressableCharsList.add(',');
    }
    
    static public String compress(String stringToCompress) {
        if (StringUtils.isEmpty(stringToCompress)) {
            return stringToCompress;
        }
        StringBuilder compressedString = new StringBuilder();        
        char curChar = stringToCompress.charAt(0);
        char nextChar = ' ';
        for (int i = 1; i < stringToCompress.length(); i++) {
            nextChar = stringToCompress.charAt(i);
            if (curChar != ' ' || (! compressableCharsList.contains(nextChar))) {
                compressedString.append(curChar);
            }
            curChar = nextChar;
        }
        compressedString.append(curChar);
        return compressedString.toString();
    }
}

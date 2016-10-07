/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.utilities;


/**
 * This class contains string matching methods.  Most of this
 * code is from the LongestCommonNameMatch method provided
 * by Regenstrief.  Changes from this method are noted.
 * 
 * @author jlbrown
 * 
 */
public class StringMatching
{
    
    //Have to have this many characters in the two
    //string we are trying to match in order to
    //compare with character sequences instead of
    //individual characters.
    private static int MINIMUM_NAME_SEQUENCE_MATCH_SIZE = 5;    
    
    private static char[] string1Char;
    private static char[] string2Char;
    private static int[][] longestCommonString;

    /**
     * matches one string with a second string by
     * matching the longest possible sequence of characters from each name,
     * removing the matched sequence, and repeating the process. The returned
     * <code>int</code> value represents the match character "percentage".
     * 
     * @param string1 first string to be matched
     * @param string2 second string to be matched
     * @return the match character "percentage"
     */
    public static int longestCommonStringMatch(String string1, String string2)
    {
        if( string1 == null || string2 == null )
        {
            return 0;
        }

        int matchPercent = 0;
        int matchSize;
        int string1Len;
        int string2Len;
        int stringLenMin;
        int stringLenRemaining;
        char currChar;
        int lenThreshold;
        int commonTotal = 0;
        int commonMax = 0;
        int commonTmp1 = 0;
        int commonTmp2 = 0;
        int repetitionThreshold = 3;

        string1Len = string1.length();
        string2Len = string2.length();
        if( string1Len < MINIMUM_NAME_SEQUENCE_MATCH_SIZE
                || string2Len < MINIMUM_NAME_SEQUENCE_MATCH_SIZE )
        {
            return 0;
        }
        string1Char = new char[string1Len + 1];
        string2Char = new char[string2Len + 1];
        // The following two lines are changed from the provided code.
        string1Char = makeCharArrayFromString(string1);
        string2Char = makeCharArrayFromString(string2);
        stringLenMin = Math.min(string1Len, string2Len);
        // *Find smallest of the two names, that's all we can hope to match!
        stringLenRemaining = stringLenMin; // *Keep track of largest sequence we
                                        // can still hope to match
        if( stringLenMin == 0 )
        {
            return 0;
        }
        longestCommonString = new int[string1Len + 1][string2Len + 1]; // *Array for keeping track
                                                    // of char sequences matched
                                                    // so far
        if( stringLenRemaining < MINIMUM_NAME_SEQUENCE_MATCH_SIZE )
        // *IF matching two short names
        {
            lenThreshold = 0; // *THEN can match on even one letter
        }
        else
        {
            lenThreshold = 2; // *ELSE require sequence of 3 chars for a match
        }
        for( int repetition = 0; repetition < repetitionThreshold; repetition++ ) // *Make sequence repetition
                                                                                  // loop
        {
            if( stringLenRemaining < 1 )
            {
                break;
            }
            if( stringLenRemaining <= lenThreshold )
            {
                lenThreshold = stringLenRemaining - 1;
            }
            commonMax = lenThreshold;
            for( int string1Idx = 1; string1Idx <= string1Len; string1Idx++ ) // *Loop to try and match on
                                                // char from string1
            {
                currChar = string1Char[string1Idx];
                for( int string2Idx = 1; string2Idx <= string2Len; string2Idx++ )
                // *Loop to find char match in string2
                {
                    if( string2Char[string2Idx] == currChar )
                    {
                        matchSize = longestCommonString[string1Idx - 1][string2Idx - 1] + 1;
                        longestCommonString[string1Idx][string2Idx] = matchSize;
                        if( matchSize > commonMax )
                        {
                            commonMax = matchSize;
                            commonTmp1 = string1Idx;
                            commonTmp2 = string2Idx;
                        }
                    }
                }
            }
            if( commonMax == lenThreshold ) // *IF no char sequences of
                                            // sufficient len matched,
            {
                break; // *THEN quit searching for sequences
            }
            commonTotal = commonTotal + commonMax;
            // *Incr total numb chars matched
            stringLenRemaining -= commonMax; // *Subtract number remaining chars
                                            // to match
            for( int string1Idx = commonTmp1 - commonMax + 1; string1Idx <= commonTmp1; string1Idx++ )
            {
                string1Char[string1Idx] = 0; // *Replace name1 matched char with filler
                                    // that will never match
            }
            for( int string2Idx = commonTmp2 - commonMax + 1; string2Idx <= commonTmp2; string2Idx++ )
            {
                string2Char[string2Idx] = 1; // *Replace matched name2 char with char
                                    // diff from name1 filler
            }
            resetLongestCommonString();
        }
        matchPercent = (100 * commonTotal) / stringLenMin; // *Calc percentage
                                                            // of matching chars
        return matchPercent;
    }

    /**
     * Makes a character array from a String.  This method
     * differs from the provided code in that it does not remove
     * non-alphanumeric characters.
     * @param stringToClean 
     * @return
     */
    private static char[] makeCharArrayFromString(String stringToClean)
    {        
        char[] cleanCharArray = new char[stringToClean.length() + 1];        
        char[] stringAsCharArray = stringToClean.toUpperCase().toCharArray();  
        for (int charIdx = cleanCharArray.length - 1; charIdx > 0; charIdx--)
        {
            cleanCharArray[charIdx] = stringAsCharArray[charIdx - 1];
        }
        return cleanCharArray;
    }
    
    private static void resetLongestCommonString()
    {
        for( int idx1 = 1; idx1 < string1Char.length; idx1++ )
        {
            for( int idx2 = 1; idx2 < string2Char.length; idx2++ )
            {
                longestCommonString[idx1][idx2] = 0;
            }
        }
    }
}

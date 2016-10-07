/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.utilities;

import java.util.HashMap;

/**
 * This class is used to build up a map of strings.  The key
 * in the map is the value to be replaced and the value is the
 * that is used as a replacement.
 */
public class ReplaceMap extends HashMap<String,String> {

    private static final long serialVersionUID = -1010614797420108459L;
    
    public ReplaceMap()
    {
        // default constructor left empty on purpose
    }
    
    public ReplaceMap(ReplaceMap mapToClone)
    {
        super(mapToClone);
    }

    public String applyMap(String stringToReplace)
    {
        String retVal = stringToReplace;
        for(String toReplace : keySet()) {
            retVal = retVal.replaceAll(toReplace, get(toReplace));
        }
        return retVal;
    }
}

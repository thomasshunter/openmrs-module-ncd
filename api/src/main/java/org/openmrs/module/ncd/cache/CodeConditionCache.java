/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.Condition;

/**
 * Map to contain the code and system data from CodeCondition.
 * @author jlbrown
 *
 */
public class CodeConditionCache
{
    private HashMap<String, List<CodeCondition>> codeMap;
    
    public CodeConditionCache()
    {
        codeMap = new HashMap<String, List<CodeCondition>>();
    }
    
    /**
     * Get a list of code rows based on the code and coding system.
     * @param code The code to look for.
     * @param system The coding system to look for.
     * @return A list of code rows or an empty list if no rows correspond
     * to the code and coding system pair.
     */
    public List<CodeCondition> findByCodeAndSystem(String code, String system)
    {   
        List<CodeCondition> codeList = codeMap.get(code + ":" + system);        
        return codeList;
    }
    
    public CodeCondition findByCodeAndCondition(String code, String system, String condition) 
    {
        CodeCondition retVal            = null;
        List<CodeCondition> codeList    = findByCodeAndSystem(code, system);
    
        if (codeList != null) 
        {
	        for (CodeCondition loincRow : codeList) 
	        {
	            Condition cond     = loincRow.getCondition();
	            String displayText = cond.getDisplayText(); 
	            
	            if( displayText.equals( condition ) ) 
	            {
	                retVal = loincRow;
	                break;
	            }
	        }
        }
        
        return retVal;
    }
    
    public void addItem(CodeCondition item)
    {
        String key                      = item.getCode().getCode() + ":" + item.getCode().getCodeSystem().getName();
        List<CodeCondition> codeList    = null;
        
        if (codeMap.containsKey(key))
        {
            codeList = codeMap.get(key);            
        }
        else
        {
            codeList = new ArrayList<CodeCondition>();                        
        }
        
        codeList.add(item);
        codeMap.put(key, codeList);
    }
    
    public void addCollection(Collection<CodeCondition> col)
    {
        for(CodeCondition item : col)
        {
            addItem(item);
        }
    }

    
}

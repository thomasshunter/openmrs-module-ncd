/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;

import java.util.Collection;
import java.util.HashMap;

/**
 * This class is the default container for the map of names
 * to ICandidateResultFinder implementations. 
 * 
 * @author John Brown
 *
 */
public class DefaultResultFinderMap implements IResultFinderMap {

	private HashMap<String, ICandidateResultFinder> resultFinderMap =
		new HashMap<String, ICandidateResultFinder>();
	
	public void setResultFinderMap(HashMap<String, ICandidateResultFinder> finderMap)
	{
		resultFinderMap = finderMap;
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.IResultFinderMap#getFinders(java.lang.String)
	 */
	public ICandidateResultFinder getFinder(String finderName)
	{
		ICandidateResultFinder finder = null;
		
		if (resultFinderMap.containsKey(finderName))
		{
			finder = resultFinderMap.get(finderName);
		}
		
		return finder;
	}

    public Collection<ICandidateResultFinder> getFinders()
    {
        return resultFinderMap.values();
    }
}

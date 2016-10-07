/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;

import java.util.Collection;

/**
 * This interface determines the way ICandidateResultFinder implementations
 * are accessed.
 * 
 * @author jlbrown
 *
 */
public interface IResultFinderMap
{

    public ICandidateResultFinder getFinder(String finderName);
    public Collection<ICandidateResultFinder> getFinders();
}

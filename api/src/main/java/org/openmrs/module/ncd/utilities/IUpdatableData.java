/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.utilities;

/**
 * Interface to allow period update of data.  This interface does
 * not specify the means by which the updates occur; it only specifies
 * a method to implement that will be called to update data.
 * 
 * @author jlbrown
 *
 */
public interface IUpdatableData
{
    public void updateData();
}

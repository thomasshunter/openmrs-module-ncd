/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.storage;

import org.openmrs.module.ncd.MessageProcessorException;

/**
 * Exception class for all DecidedResultStorageHelper errors.
 * 
 * @author jlbrown
 *
 */
public class DecidedResultStorageException extends
        MessageProcessorException
{
    static final long serialVersionUID = -2419084627564083720L;
    
    public DecidedResultStorageException(String arg0)
    {
        super(arg0);
    }

    public DecidedResultStorageException(Throwable arg0)
    {
        super(arg0);
    }

    public DecidedResultStorageException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }
}

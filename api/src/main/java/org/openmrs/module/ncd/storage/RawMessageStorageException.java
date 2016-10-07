/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.storage;

import org.openmrs.module.ncd.MessageProcessorException;

/**
 * @author jlbrown
 *
 */
public class RawMessageStorageException extends
        MessageProcessorException
{
    private static final long serialVersionUID = 8230916367150520688L;
    
    /**
     * @param arg0
     */
    public RawMessageStorageException(String message)
    {
        super(message);
    }

    /**
     * @param arg0
     */
    public RawMessageStorageException(Throwable innerException)
    {
        super(innerException);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public RawMessageStorageException(String message, Throwable innerException)
    {
        super(message, innerException);
    }

}

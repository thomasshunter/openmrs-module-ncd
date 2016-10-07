/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.storage;

import org.openmrs.module.ncd.MessageProcessorException;

/**
 * Exception class for all CodeFrequencyStorageHelper errors.
 * 
 * @author jlbrown
 *
 */
public class CodeFrequencyStorageException extends
        MessageProcessorException
{
    static final long serialVersionUID = -5001234344845949299L;
    
    public CodeFrequencyStorageException(String arg0)
    {
        super(arg0);
    }

    public CodeFrequencyStorageException(Throwable arg0)
    {
        super(arg0);
    }

    public CodeFrequencyStorageException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }
}

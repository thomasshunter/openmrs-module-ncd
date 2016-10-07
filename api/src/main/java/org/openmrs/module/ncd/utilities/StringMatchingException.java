/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.utilities;

import org.openmrs.module.ncd.MessageProcessorException;

/**
 * Exception class for all StringMatching errors.
 * 
 * @author jlbrown
 *
 */
public class StringMatchingException extends
        MessageProcessorException
{
    static final long serialVersionUID = 7736191837716115868L;
    
    /**
     * @param arg0 A message describing the reason for the exception.
     */
    public StringMatchingException(String arg0)
    {
        super(arg0);
    }

    /**
     * @param arg0 The exception which caused this exception to be thrown.
     */
    public StringMatchingException(Throwable arg0)
    {
        super(arg0);
    }

    /**
     * @param arg0 A message describing the reason for the exception.
     * @param arg1 The exception which caused this exception to be thrown.
     */
    public StringMatchingException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }

}

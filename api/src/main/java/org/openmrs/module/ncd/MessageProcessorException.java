/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;


/**
 * Base exception class for all CommunicableDiseaseProcessor exceptions.
 * 
 * @author John Brown
 */
public class MessageProcessorException extends Exception
{

    private static final long serialVersionUID = -8323648247213513247L;    

    public MessageProcessorException(String arg0)
    {
        super(arg0);
    }

    public MessageProcessorException(Throwable arg0)
    {
        super(arg0);
    }

    public MessageProcessorException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }

}

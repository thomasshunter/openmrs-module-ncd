/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.nlp;

import org.openmrs.module.ncd.MessageProcessorException;

/**
 * Exception for classes implementing INlpAnalyzer.
 * 
 * @author jlbrown
 *
 */
public class NlpAnalyzerException extends MessageProcessorException
{
    private static final long serialVersionUID = 3176001798254581550L;

    public NlpAnalyzerException(String message)
    {
        super(message);
    }
    
    public NlpAnalyzerException(Exception innerException)
    {
        super(innerException);
    }
    
    public NlpAnalyzerException(String message, Exception innerException)
    {
        super(message, innerException);
    }
}

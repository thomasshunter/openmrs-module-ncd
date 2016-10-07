/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;

/**
 * Exception class for all CandidateResultFinder errors.
 * 
 * @author jlbrown
 *
 */
public class CandidateResultFinderException extends
        MessageProcessorException
{

    private static final long serialVersionUID = -8323648247213513246L;

    public CandidateResultFinderException(String arg0)
    {
        super(arg0);      
    }

    public CandidateResultFinderException(Throwable arg0)
    {
        super(arg0);
    }

    public CandidateResultFinderException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }

}

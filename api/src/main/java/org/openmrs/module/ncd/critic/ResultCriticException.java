package org.openmrs.module.ncd.critic;

import org.openmrs.module.ncd.MessageProcessorException;

/**
 * Exception class used by classes implementing IResultCritic.
 * This class should be by a critic if there are any errors processing
 * a result.
 * 
 * @author jlbrown
 *
 */
public class ResultCriticException extends
        MessageProcessorException
{
    static final long serialVersionUID = 1143871489562495479L;
    
    public ResultCriticException(String arg0)
    {
        super(arg0);
    }

    public ResultCriticException(Throwable arg0)
    {
        super(arg0);
    }

    public ResultCriticException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }

}

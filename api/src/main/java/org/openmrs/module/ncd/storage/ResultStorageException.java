package org.openmrs.module.ncd.storage;

import org.openmrs.module.ncd.MessageProcessorException;

public class ResultStorageException extends
        MessageProcessorException
{
    static final long serialVersionUID = -7907504372050367026L;
    
    public ResultStorageException(String arg0)
    {
        super(arg0);
    }

    public ResultStorageException(Throwable arg0)
    {
        super(arg0);
    }

    public ResultStorageException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }

}

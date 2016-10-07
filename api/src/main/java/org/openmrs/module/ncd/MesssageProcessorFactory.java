/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;

import java.util.HashMap;

/**
 * Used to generate a new object that implements the
 * ICommunicableDiseaseProcessor interface.  You can
 * optionally name an instance and retrieve an instance
 * using the name.
 * 
 * @author jlbrown
 * 
 */
public class MesssageProcessorFactory
{
    static HashMap<String, IMessageProcessor> cdpMap = new HashMap<String, IMessageProcessor>();
    
    public static IMessageProcessor createInstance()
    {
        return new HL7MessageProcessor();
    }
    
    public static IMessageProcessor createInstance(String instanceName)
    {
        IMessageProcessor cdp = getInstance(instanceName);
        if ( cdp == null )
        {
            cdp = new HL7MessageProcessor();
            cdpMap.put(instanceName, cdp);
        }
        return cdp;
    }
    
    public static IMessageProcessor getInstance(String instanceName)
    {
        return cdpMap.get(instanceName);
    }
}

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ncd;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.ncd.utilities.ContextUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.PersonAttributeTypeCache;

/**
 * This class contains the logic that is run every time this module is either
 * started or shutdown
 */
@SuppressWarnings("deprecation")
public class NCDActivator implements Activator, Runnable {

    /** Debugging log */
    private static Log log = LogFactory.getLog(NCDActivator.class);

    /** The NCD server itself - there is one and only one instance. */
    private static NCDServer server = new NCDServer();

    public static void main(String[] args) {
        NCDActivator activator = new NCDActivator();
        activator.startup();
    }

    /**
     * @see org.openmrs.module.Activator#startup()
     */
    @Override
    public void startup() {

    	new Thread(this).start();
    }

    @Override
    public void run() {

    	// Stall until the service has been created. This is being used a 
    	// proxy for the Context being ready for calls, which is not obviously
    	// correct.
    	ConditionDetectorService cds = null;
    	while (cds == null) {

    		try {
    			Thread.sleep(20000);
    	        log.info("Deferring NCD Module startup until Context is ready");
    			cds = Context.getService(ConditionDetectorService.class);
    		}
    		catch (APIException apiEx) {
    		}
    		catch (InterruptedException iEx) {
    		}
    	}
    	
        log.info("Initializing the NCD Module");
        initialize();
    }
    
    private void initialize() {
    	
        Collection<Privilege> privileges = null;
        try {
        	
            ContextUtilities.openSession();
        	privileges = ContextUtilities.addNCDProxyPrivileges();
            NCDUtilities.start();
            PersonAttributeTypeCache.startup();
            server.startServer();
	    } catch (Exception e) {
	        log.error("Exception: " + e.getMessage(), e);
	    } finally {

        	if (privileges != null) {
        		ContextUtilities.removeProxyPrivileges(privileges);
        	}
        	
            Context.closeSession();
        }
    }

    /**
     * @see org.openmrs.module.Activator#shutdown()
     */
    @Override
    public void shutdown() {

        log.info("Shutting down NCD Module");
        
        Collection<Privilege> privileges = null;
        try {

        	NCDUtilities.authenticate();
        	privileges = ContextUtilities.addNCDProxyPrivileges();
            NCDUtilities.stop();
            server.stopServer(NCDServer.STOP_WAIT_TIME);
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage(), e);
        } finally {
        	
        	if (privileges != null) {
        		ContextUtilities.removeProxyPrivileges(privileges);
        	}
        }
    }
}

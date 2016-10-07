package org.openmrs.module.ncd.utilities;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

public class ContextUtilities {

    private static Log logger = LogFactory.getLog(ContextUtilities.class);

    public static Collection<Privilege> addNCDProxyPrivileges() {
		
		Collection<Privilege> privileges = null;
			
		// Bootstrap one privilege, so we can lookup the configured NCD user and its privileges
		Context.addProxyPrivilege("View Users");
		Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
		
		User ncdUser = NCDUtilities.getNcdUser();
		privileges = ncdUser.getPrivileges();
		for (Privilege privilege : privileges) {
			Context.addProxyPrivilege(privilege.getPrivilege());
		}
		
		return privileges;
	}
	
	/**
	 * Removes the set of proxy privileges previously added by
	 * addProxyPrivilegeRole.
	 * 
	 * This method should be called from a finally block matching the try block
	 * wrapping the call to addProxyPrivilege and the code that requires the
	 * elevated privileges, and should be passed the set of proxy privileges
	 * returned by that call to addProxyPrivilege.
	 * 
	 * @param privileges The set of proxy privileges to be removed, as returned
	 * by the matching call to addProxyPrivilegeRole.
	 */
	public static void removeProxyPrivileges(Collection<Privilege> privileges) {
		
		for (Privilege privilege : privileges) {
			
			Context.removeProxyPrivilege(privilege.getPrivilege());
		}
	}
	
	public static void openSession() throws APIException {
		
		if (Context.isSessionOpen()) {

			logger.error("openSession called with an active session");
			throw new APIException("openSession called with an active session");
		}
		
		Context.openSession();
	}
}

package org.openmrs.module.ncd.events;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.utilities.MultiMap;

/**
 * A dispatcher from global property names to mappings for changes to the
 * corresponding global property. Somewhat simplifies the OpenMRS global
 * property listener interface.
 */
public class PropertyEventDispatcher implements GlobalPropertyListener 
{
    private Log logger = LogFactory.getLog(PropertyEventDispatcher.class);

    /** The current property name to handler mapping */
    private MultiMap<String, PropertyEventHandler> mappings = new MultiMap<String, PropertyEventHandler>();

    /**
     * Adds a handler for the specified property, and calls the handler with the
     * initial value of the property.
     * 
     * Auto generated method comment
     * 
     * @param propertyName The name of the global property to listen for changes
     *        to.
     * @param handler The class to call when changes to the property occur.
     */
    public void add(String propertyName, PropertyEventHandler handler) 
    {
        // TODO remove ContextUtilities.openSession();
        try 
        {
            AdministrationService adminService = Context.getAdministrationService();
            add(propertyName, handler, adminService);
        } 
        finally 
        {
        	// TODO remove Context.closeSession();
        }
    }

    /**
     * Adds a handler for the specified property, and calls the handler with the
     * initial value of the property.
     * 
     * Auto generated method comment
     * 
     * @param propertyName The name of the global property to listen for changes
     *        to.
     * @param handler The class to call when changes to the property occur.
     * @param adminService The OpenMRS administration service, to get the
     *        initial value of the global property.
     */
    public void add(String propertyName, PropertyEventHandler handler, AdministrationService adminService) 
    {
        logger.debug("adding handler for property " + propertyName);
        mappings.add(propertyName, handler);
        
        String propValue    = adminService.getGlobalProperty(propertyName);
        GlobalProperty prop = new GlobalProperty(propertyName, propValue);
        globalPropertyChanged(prop);
    }

    /**
     * Called by OpenMRS when the value of the specified global property
     * changes.
     * 
     * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
     */
    public void globalPropertyChanged(GlobalProperty arg0) 
    {
        logger.debug("globalPropertyChanged, property=" + arg0.getProperty());
        Collection<PropertyEventHandler> handlers = mappings.get(arg0.getProperty());
        
        if (handlers != null) 
        {
            for (PropertyEventHandler handler : handlers) 
            {
                handler.propertyChanged(arg0);
            }
        }
        else 
        {
            logger.debug("no registered handlers");
        }
    }

    /**
     * Called by OpenMRS when the specified global property is deleted.
     * 
     * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
     */
    public void globalPropertyDeleted(String arg0) 
    {
        logger.debug("globalPropertyDeleted, property=" + arg0);
    }

    /**
     * Called by OpenMRS when any global property is changed, to check if this
     * listener cares.
     * 
     * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
     */
    public boolean supportsPropertyName(String arg0) 
    {
        return mappings.containsKey(arg0);
    }
}

package org.openmrs.module.ncd.utilities;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.GlobalProperty;

/**
 * A friendlier face for the OpenMRS global properties system. Provides
 * the ability to easily create and modify the value of global properties
 * programmatically.
 *  
 * @author Erik Horstkotte (erikh@webreachinc.com)
 */
public class PropertyManager {

    private static Log log = LogFactory.getLog(PropertyManager.class);
    private static Map<String, GlobalProperty> properties = null;
    
    /**
     * If the named global property does not already exist, creates it with
     * the specified default value and description.
     * 
     * @param name The name of the property to create.
     * @param value The default value for the property.
     * @param description The human-readable description for the property.
     */
    public static void create(String name, String value, String description) {

        log.debug("create: enter");
        log.debug("create: name=" + name);
        log.debug("create: value=" + value);
        log.debug("create: description=" + description);

        initialize();

        GlobalProperty prop = properties.get(name);
        if (prop == null) {
            
            log.debug("create: new global property");
            
            prop = new GlobalProperty(name, value, description);
            properties.put(name, prop);
            Context.getAdministrationService().saveGlobalProperty(prop);
        }
        
        log.debug("create: exit");
    }
    
    /**
     * Gets the current value of the named global property.
     * 
     * @param name The name of the global property for which the value is
     * to be returned.
     * @return The value of the named global property.
     * @throws PropertyNotFoundException If there is no such property.
     */
    public static String get(String name) throws PropertyNotFoundException {

        initialize();

        return findProperty(name).getPropertyValue();
    }
    
    /**
     * Gets the current value of the named global property converted to a
     * boolean.
     * 
     * @param name The name of the global property for which the value is
     * to be returned.
     * @return The value of the named global property.
     * @throws PropertyNotFoundException If there is no such property.
     */
    public static boolean getBoolean(String name) throws PropertyNotFoundException {

        String value = get(name);
        return Boolean.parseBoolean(value);
    }

    /**
     * Gets the current value of the named global property converted to an
     * int.
     * 
     * @param name The name of the global property for which the value is
     * to be returned.
     * @return The value of the named global property.
     * @throws PropertyNotFoundException If there is no such property.
     */
    public static int getInt(String name) throws PropertyNotFoundException {

        String value = get(name);
        return Integer.parseInt(value);
    }

    /**
     * Gets the current value of the named global property converted to an
     * long, applying any multiplier suffix (kb, KB, mb, MB, gb or GB).
     * 
     * @param name The name of the global property for which the value is
     * to be returned.
     * @return The value of the named global property.
     * @throws PropertyNotFoundException If there is no such property.
     */
    public static long getSize(String name) throws PropertyNotFoundException {

        return StringUtilities.parseSize(get(name));
    }
    
    /**
     * Replaces the value of the named global property.
     * 
     * @param name The name of the global property for which the value is
     * to be replaced.
     * @param value The new value for the named global property.
     * @throws PropertyNotFoundException If there is no such property.
     */
    public static void set(String name, String value) throws PropertyNotFoundException {

        initialize();

        GlobalProperty prop = findProperty(name);
        prop.setPropertyValue(value);
        Context.getAdministrationService().saveGlobalProperty(prop);
    }
    
    /**
     * Replaces the value of the named global property.
     * 
     * @param name The name of the global property for which the value is
     * to be replaced.
     * @param value The new value for the named global property.
     * @throws PropertyNotFoundException If there is no such property.
     */
    public static void set(String name, boolean value) throws PropertyNotFoundException {

        set(name, Boolean.toString(value));
    }
    
    /**
     * Replaces the value of the named global property.
     * 
     * @param name The name of the global property for which the value is
     * to be replaced.
     * @param value The new value for the named global property.
     * @throws PropertyNotFoundException If there is no such property.
     */
    public static void set(String name, int value) throws PropertyNotFoundException {

        set(name, Integer.toString(value));
    }
    
    /**
     * Helper method to find a global property by name.
     * 
     * @param name The name of the global property to find.
     * @return The global property with the specified name.
     * @throws PropertyNotFoundException If there is no such property.
     */
    private static GlobalProperty findProperty(String name) throws PropertyNotFoundException {
        
        log.debug("findProperty: enter");
        log.debug("findProperty: name=" + name);

        GlobalProperty prop = properties.get(name);

        if (prop == null) {
            
            log.debug("findProperty: no such property, throw");

            throw new PropertyNotFoundException();
        }
        
        log.debug("findProperty: prop=" + prop);
        log.debug("findProperty: exit");
        
        return prop;
    }

    private static void initialize() {
        
        if (properties == null) {

            log.debug("initialize: enter");

            properties = new HashMap<String, GlobalProperty>();
            
            // TODO: remove ContextUtilities.openSession();
            try {
	            Context.addProxyPrivilege("View Global Properties");
	            for (GlobalProperty prop : Context.getAdministrationService().getAllGlobalProperties()) {
	                
	                properties.put(prop.getProperty(), prop);
	            }
	            
	            log.debug("initialize: loaded " + properties.size() + " properties");
            }
            finally {
            	
            	Context.removeProxyPrivilege("View Global Properties");
            }
            // TODO: remove Context.closeSession();

            log.debug("initialize: exit");
        }
    }
}

package org.openmrs.module.ncd.output.aggrpt;

import java.util.Map;

import org.openmrs.module.ncd.utilities.MapUtilities;

public class DataFormatterFactory {

	// DataFormatter Properties
	// PROP_FORMATTER_CLASS: selects which DataFormatter implementation is returned by the factory
	// TEMPLATE: the template that controls report formatting
	// PROP_RECIPIENT: the name of the entity for which this report is being generated, and will be sent to
    public final static String PROP_FORMATTER_CLASS = "DataFormatterFactory.class";
    public final static String PROP_RECIPIENT = "DataFormatterFactory.recipient";

    /** Creates and returns an instance of a class implementing
     * DataFormatter as selected by the properties supplied.
     */
    public static DataFormatter getInstance(Map<String, String> properties, String resourceName)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        String className = MapUtilities.get(properties, PROP_FORMATTER_CLASS, DataFormatterImpl.class.getName());
        Class<?> clazz = Class.forName(className);
        DataFormatter inst = (DataFormatter) clazz.newInstance();
        inst.configure(properties, resourceName);
    
        return inst;
    }
}

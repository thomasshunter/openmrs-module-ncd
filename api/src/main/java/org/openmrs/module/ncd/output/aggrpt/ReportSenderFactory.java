package org.openmrs.module.ncd.output.aggrpt;

import java.util.Map;

import org.openmrs.module.ncd.utilities.MapUtilities;

public class ReportSenderFactory {

    public final static String PROP_SENDER_CLASS = "ReportSenderFactory.class";

    /** Creates and returns an instance of a class implementing
     * ReportSender as selected by the properties supplied.
     */
    public static ReportSender getInstance(Map<String, String> properties)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        String className = MapUtilities.get(properties, PROP_SENDER_CLASS, ReportSenderEmailImpl.class.getName());
        Class<?> clazz = Class.forName(className);
        ReportSender inst = (ReportSender) clazz.newInstance();
        inst.configure(properties);

        return inst;
    }
}

package org.openmrs.module.ncd.output.aggrpt;

import java.util.Map;

public interface DataFormatter {

    /**
     * Configures the formatter
     * 
     * @param properties A map containing properties that control the report formatting 
     * (see the DataFormatterFactory for more information on available properties).
     */
    public void configure(Map<String, String> properties, String resourceName);
    
    /**
     * Formats the report data using the specified report data and configured
     * properties.
     * 
     * @param data The report data to be formatted.
     * @return The formatted report as a String.
     */ 
    public String format(Object data);
}

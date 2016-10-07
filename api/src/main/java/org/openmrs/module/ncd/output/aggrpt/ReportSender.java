package org.openmrs.module.ncd.output.aggrpt;

import java.util.Map;

public interface ReportSender {

    /**
     * Configures the report sender
     * 
     * @param properties A map containing properties that control the
     * sending of the report. (see the ReportSenderFactory for more
     * information on available properties).
     */
    public void configure(Map<String, String> properties);
	
    /**
     * Actually send the report, based on the configured properties.
     * @param message
     */
	public void send(String message);
}

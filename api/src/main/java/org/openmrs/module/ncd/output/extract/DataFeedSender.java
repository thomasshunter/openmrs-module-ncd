package org.openmrs.module.ncd.output.extract;

import java.io.File;
import java.util.Map;

/**
 * The interface presented by a class that transfers a data feed file to
 * its consumer.
 */
public interface DataFeedSender {

    /** Record the name of the source file to transfer and the
     * transfer properties
     */
    public void configure(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog);
    
    /** Transfer the file */
    public void send();
    
    /** Tests if the sender has already sent the status email */
    public boolean isStatusEmailSent();
}

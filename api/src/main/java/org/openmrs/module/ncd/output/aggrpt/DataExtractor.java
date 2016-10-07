package org.openmrs.module.ncd.output.aggrpt;

import java.util.Date;
import java.util.Map;

public interface DataExtractor {

    /** Configures the extractor
     *
     * @param lastDate The test date for the last (rightmost) date bucket.
     * @param properties The per-consumer parameters for the report we
     * are extracting data for.
     */
    public void configure(Date lastDate, Map<String, String> properties);

    /** Actually extract and return the data */
    public ReportData extract();
}

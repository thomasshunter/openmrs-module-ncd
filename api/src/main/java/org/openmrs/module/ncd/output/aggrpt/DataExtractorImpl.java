package org.openmrs.module.ncd.output.aggrpt;

import java.util.Date;
import java.util.Map;

import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.MapUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;

public class DataExtractorImpl implements DataExtractor {

    private Date lastDate;
    private Map<String, String> properties;
    
    public DataExtractorImpl() {
    }

    /** Configures the extractor
    *
    * @param lastDate The test date for the last (rightmost) date bucket.
    * @param properties The per-consumer parameters for the report we
    * are extracting data for.
    */
   public void configure(Date lastDate, Map<String, String> properties) {
       
       this.lastDate = lastDate;
       this.properties = properties;
   }

    public ReportData extract() {

        int dateBucketCount = MapUtilities.get(properties, DataExtractorFactory.DATE_BUCKET_COUNT, DataExtractorFactory.DEFAULT_DATE_BUCKET_COUNT);
        Date[] bucketDates = DateUtilities.computeBucketDates(dateBucketCount, lastDate);
        return NCDUtilities.getService().getAggregateSummaryData(bucketDates, properties);
    }
}

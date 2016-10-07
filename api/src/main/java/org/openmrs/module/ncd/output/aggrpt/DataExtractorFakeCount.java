package org.openmrs.module.ncd.output.aggrpt;

import java.util.Date;
import java.util.Map;

import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.MapUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/** An implementation of DataExtractor that returns a ReportData with
 * the right number of date buckets, the right bucket dates, all condition
 * groups and conditions from the database, and fake counts.
 */
public class DataExtractorFakeCount implements DataExtractor {

    private Date lastDate;
    private Map<String, String> properties;
    
    public void configure(Date lastDate, Map<String, String> properties) {
        
        this.lastDate = lastDate;
        this.properties = properties;
    }

    public ReportData extract() {

        int dateBucketCount = MapUtilities.get(properties, DataExtractorFactory.DATE_BUCKET_COUNT, DataExtractorFactory.DEFAULT_DATE_BUCKET_COUNT);
        Date[] bucketDates = DateUtilities.computeBucketDates(dateBucketCount, lastDate);
        return NCDUtilities.getService().getFakeCountAggregateSummaryData(bucketDates, properties);
    }
}

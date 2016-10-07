package org.openmrs.module.ncd.output.aggrpt;

import java.util.Date;
import java.util.Map;

import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.MapUtilities;

/** An implementation of DataExtractor that returns a ReportData with
 * the right number of date buckets, the right bucket dates, hard-coded
 * condition groups and conditions, and fake counts.
 */
public class DataExtractorFakeAll implements DataExtractor {
    
    public final static String GROUP_COUNT = "DataExtractorFakeAll.groupCount";
    public final static int DEFAULT_GROUP_COUNT = 10;
    public final static String CONDITION_COUNT = "DataExtractorFakeAll.conditionCount";
    public final static int DEFAULT_CONDITION_COUNT = 10;
    
    private Date lastDate;
    private Map<String, String> properties;
    
    public void configure(Date lastDate, Map<String, String> properties) {
        
        this.lastDate = lastDate;
        this.properties = properties;
    }

    public ReportData extract() {

        int dateBucketCount = MapUtilities.get(properties, DataExtractorFactory.DATE_BUCKET_COUNT, DataExtractorFactory.DEFAULT_DATE_BUCKET_COUNT);
        
        Date[] bucketDates = DateUtilities.computeBucketDates(dateBucketCount, lastDate);
        
        int numGroups = MapUtilities.get(properties, GROUP_COUNT, DEFAULT_GROUP_COUNT);
        int numConditions = MapUtilities.get(properties, CONDITION_COUNT, DEFAULT_CONDITION_COUNT);
        
        ReportDataBuilder data = new ReportDataBuilder(bucketDates);
        for (int group = 1; group <= numGroups; group++) {
            
            String groupName = "Condition Group " + group;
            for (int condition = 1; condition <= numConditions; condition++) {
                
                String conditionName = "Condition " + condition;
                for (int bucket = 0; bucket < bucketDates.length; bucket++) {
                    
                    data.add(groupName, conditionName, bucketDates[bucket]);
                }
            }
        }
        
        return data.getReportData();
    }
}

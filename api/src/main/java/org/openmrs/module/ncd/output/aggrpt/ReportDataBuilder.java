package org.openmrs.module.ncd.output.aggrpt;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.Pair;

public class ReportDataBuilder {
    
    private static Log logger = LogFactory.getLog(ReportDataBuilder.class);

    private Map<String, ReportDataConditionGroup> groupByName;
    private Map<Pair<String, String>, ReportDataCondition> conditionByName;
    private ReportData reportData;
    private Date[] bucketDates; // dates, no times

    public ReportDataBuilder(Date[] bucketDates) {
        
        reportData = new ReportData(bucketDates);
        groupByName = new HashMap<String, ReportDataConditionGroup>();
        conditionByName = new HashMap<Pair<String, String>, ReportDataCondition>();
        this.bucketDates = new Date[bucketDates.length];
        for (int i = 0; i < bucketDates.length; i++) {
            this.bucketDates[i] = DateUtilities.truncate(bucketDates[i]);
        }
    }
    
    public void add(String groupName, String conditionName, Date bucketDate) {

        add(groupName, conditionName, bucketDate, 1);
    }
    
    public void add(String groupName, String conditionName, Date bucketDate, Integer count) {

        bucketDate = DateUtilities.truncate(bucketDate);
        
        // Find or create the group and condition
        ReportDataCondition condition = getCondition(groupName, conditionName);
        
        // Map the bucket date to an index
        boolean foundBucket = false;
        for (int bucket = 0; bucket < bucketDates.length; bucket++) {
            
            if (bucketDate.equals(bucketDates[bucket])) {

                condition.increment(bucket, count);
                foundBucket = true;
                break;
            }
        }
        if (!foundBucket) {
            logger.error("could not find a bucket for date \"" + bucketDate + "\" in " + Arrays.toString(bucketDates));
        }
    }

    private ReportDataCondition getCondition(String groupName, String conditionName) {
        
        // If we haven't seen this group before
        ReportDataConditionGroup group = groupByName.get(groupName);
        if (group == null) {
            
            // Add it
            group = new ReportDataConditionGroup(groupName);
            groupByName.put(groupName, group);
            reportData.addConditionGroup(group);
        }
        
        // If we haven't seen this condition in this group before
        Pair<String, String> conditionKey = new Pair<String, String>(groupName, conditionName);
        ReportDataCondition condition = conditionByName.get(conditionKey);
        if (condition == null) {
            
            // Add it
            condition = new ReportDataCondition(conditionName, reportData.getDateBucketCount());
            conditionByName.put(conditionKey, condition);
            group.addCondition(condition);
        }
        
        return condition;
    }

    public ReportData getReportData() {
        
        return reportData;
    }
}

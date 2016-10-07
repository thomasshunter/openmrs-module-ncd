package org.openmrs.module.ncd.output.aggrpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReportData {

    /** The highest date and time of data to include */ 
    private Date[] bucketDates;
    private List<ReportDataConditionGroup> conditionGroups;

    public ReportData(Date[] bucketDates) {
        
        this.bucketDates = bucketDates;
        conditionGroups = new ArrayList<ReportDataConditionGroup>();
    }

    public void addConditionGroup(ReportDataConditionGroup group) {
        conditionGroups.add(group);
    }

    public int getDateBucketCount() {
        return bucketDates.length;
    }

    public Date[] getBucketDates() {
        return bucketDates;
    }

    public void setBucketDates(Date[] bucketDates) {
        this.bucketDates = bucketDates;
    }

    public List<ReportDataConditionGroup> getConditionGroups() {
        return conditionGroups;
    }

    public void setConditionGroups(List<ReportDataConditionGroup> conditionGroups) {
        this.conditionGroups = conditionGroups;
    }
    
    public String toString() {

        return "ReportData(" +
                    "bucketDates=" + Arrays.asList(bucketDates) +
                    ", conditionGroups=" + conditionGroups +
                ")";
    }
}

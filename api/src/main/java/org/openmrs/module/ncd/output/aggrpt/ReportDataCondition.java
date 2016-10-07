package org.openmrs.module.ncd.output.aggrpt;

import java.util.Arrays;

public class ReportDataCondition {

    private String conditionName;
    private int[] dateBucketCounts;
    
    public ReportDataCondition(String conditionName, int dateBucketCount) {
        
        this.conditionName = conditionName;
        this.dateBucketCounts = new int[dateBucketCount];
        for (int i = 0; i < dateBucketCount; i++) {
            dateBucketCounts[i] = 0;
        }
    }

    public void increment(int bucketIndex) {
        increment(bucketIndex, 1);
    }

    public void increment(int bucketIndex, int count) {
        dateBucketCounts[bucketIndex] += count;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public int[] getDateBucketCounts() {
        return dateBucketCounts;
    }

    public void setDateBucketCounts(int[] dateBucketCounts) {
        this.dateBucketCounts = dateBucketCounts;
    }
    
    public String toString() {

        return "ReportDataCondition(" +
                    "conditionGroupName=" + conditionName +
                    ", dateBucketCounts=" + Arrays.toString(dateBucketCounts) +
                ")";
    }
}

/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.module.ncd.database.MonitoredCondition;
import org.openmrs.module.ncd.output.aggrpt.ReportData;
import org.openmrs.scheduler.TaskDefinition;

public interface IReportingDAO {
    
    /**
     * Save a new report, or the changes to an existing report.
     * 
     * @param task The scheduled task representing the report.
     * @param monitoredConditions The List of monitored conditions for the
     * report, if any.
     */
    public void saveReport(TaskDefinition task, List<MonitoredCondition> monitoredConditions);
    
    /**
     * Extract "mock" aggregate summary report data based on the supplied
     * filtering parameters. This data depends only on the parameters,
     * and the condition and condition group data in the database, not
     * the actual reported results for the specified date ranges.
     */
    public ReportData getFakeCountAggregateSummaryData(Date[] bucketDates, Map<String, String> properties);
    
    /**
     * Extract real aggregate summary report data based on the supplied
     * filtering parameters.
     */
    public ReportData getAggregateSummaryData(Date[] bucketDates, Map<String, String> properties);
}

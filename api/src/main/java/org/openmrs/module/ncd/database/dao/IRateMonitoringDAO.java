package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.MonitoredCondition;
import org.openmrs.module.ncd.database.filter.UnusualConditionRateFilter;
import org.openmrs.module.ncd.model.ConditionCount;
import org.openmrs.module.ncd.model.ZeroCountCondition;
import org.openmrs.module.ncd.utilities.DateRange;
import org.openmrs.scheduler.TaskDefinition;

public interface IRateMonitoringDAO {

    /**
     * Find a monitored Entity:Condition mapping by entity and code.
     * 
     * @param task The task that owns the the mapping to be searched for.
     * @param application The application to be searched for.
     * @param facility The facility to be searched for.
     * @param location The location to be searched for.
     * @param condition The condition to be searched for.
     */
    public MonitoredCondition getMonitoredCondition(TaskDefinition task, String application, String facility, String location, Condition condition);

    /**
     * Find all MonitoredConditions for the specified task.
     * 
     * @param task The task for which the MonitoredConditions are to be fetched.
     * @return A List of the MonitoredConditions for the task.
     */
    public List<MonitoredCondition> getMonitoredConditions(TaskDefinition task);

    /**
     * Replace all existing monitored conditions for a task by a new collection.
     * 
     * @param task The task whose monitored conditions are to be replaced.
     * @param monitoredConditions The new collection of monitored conditions.
     */
    public void setMonitoredConditions(TaskDefinition task, List<MonitoredCondition> monitoredConditions);
    
    /**
     * Create or modify a monitored Entity:Condition mapping. 
     * 
     * @param entry The mapping to be created or modified.
     */
    public void saveMonitoredCondition(MonitoredCondition entry);

    /**
     * Delete a monitored Entity:Condition mapping.
     * 
     * @param entry The mapping to be deleted.
     */
    public void deleteMonitoredCondition(MonitoredCondition entry);

    /**
     * Gets a list containing all the app/loc/condition combinations that are
     * flagged for monitoring, and for which _no_ reportable results were
     * detected in the specified date/time window.
     * 
     * @param task The task whose MonitoredConditions should be used.
     * @param window The date/time window over which to search.
     * @return The list of app/loc/condition combinations not seen.
     */
    public List<ZeroCountCondition> getZeroCountConditions(TaskDefinition task, DateRange window);
    
    /**
     * Gather the number of occurrences of each (app, loc, condition) triple
     * in reportable results in the two specified date/time windows, for
     * triples that appear at all.
     * 
     * @param currentWindow
     * @param historicalWindow
     * @return A List of each (app, loc, condition) triple that occurred at
     * least once in at least one of the two date/time windows, with the
     * number of times that triple occurred in each window.
     */
    public List<ConditionCount> getConditionCounts(DateRange currentWindow, DateRange historicalWindow);

    /**
     * Gather the number of occurrences of each (app, loc, condition) triple
     * in reportable results in the two specified date/time windows, for
     * triples that appear at all, returning only those for which the
     * occurrence rate is "unusual".
     * 
     * @param sampleWindow
     * @param filter
     * @return
     */
    public List<ConditionCount> getUnusualConditionRates(DateRange sampleWindow, UnusualConditionRateFilter filter);
}
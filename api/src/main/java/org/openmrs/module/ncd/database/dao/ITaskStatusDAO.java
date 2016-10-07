package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.scheduler.TaskDefinition;

public interface ITaskStatusDAO {

    /** Add a status message for a task
     * 
     * @param status The new status message to be added.
     */
    public abstract void addTaskStatus(TaskRunStatus status);

    /** Gets the most recent successful status for the specified task.
     * 
     * @param task The task for which to get the status.
     * @return The most recent successful status for the specified task, or
     * null if the task has no recorded successful statuses.
     */
    public abstract TaskRunStatus getLatestTaskStatus(TaskDefinition task);

    /** Get the most recent task status records for the dashboard.
     * 
     * @param statusCount The number of status returns to return.
     * @return A list of at most the specified number of status records,
     * most recent first.
     */
    public abstract List<TaskRunStatus> getRecentTaskStatus(int statusCount);

    /** Prunes task run status history older than the specified age.
     * 
     * @param maxAgeDays The maximum age (in days) to keep task run status
     * history.  Task run status older than this is removed from the database.  
     */
    public abstract void pruneTaskStatus(int maxAgeDays);
}
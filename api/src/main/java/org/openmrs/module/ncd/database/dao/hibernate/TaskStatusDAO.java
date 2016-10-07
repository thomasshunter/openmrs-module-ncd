/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.ExportRecipient;
import org.openmrs.module.ncd.database.ExportedResult;
import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.database.dao.ITaskStatusDAO;
import org.openmrs.scheduler.TaskDefinition;

public class TaskStatusDAO implements ITaskStatusDAO {

    /** Hibernate session factory */
    private SessionFactory sessionFactory;

    // **********************
    // Spring-only methods *
    // **********************

    /**
     * Set session factory. Spring calls this based on the
     * moduleApplicationContext.xml
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // ***************************
    // Public interface methods *
    // ***************************

    /**
     * @see org.openmrs.module.ncd.database.dao.hibernate.ITaskStatusDAO#addTaskStatus(org.openmrs.module.ncd.database.TaskRunStatus)
     */
    public void addTaskStatus(TaskRunStatus status) {
        
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(status);
    }

    /**
     * @see org.openmrs.module.ncd.database.dao.hibernate.ITaskStatusDAO#getLatestTaskStatus(org.openmrs.scheduler.TaskDefinition)
     */
    @SuppressWarnings("unchecked")
    public TaskRunStatus getLatestTaskStatus(TaskDefinition task) {

        Query query = sessionFactory.getCurrentSession().createQuery(
            "from TaskRunStatus st" +
            " where st.task = :task" +
            " and st.succeeded = true" +
            " order by st.ended desc")
                .setParameter("task", task)
                .setMaxResults(1);
        List<TaskRunStatus> statuses = (List<TaskRunStatus>) query.list();
        if (statuses.isEmpty()) {
            return null;
        }
        else {
            return statuses.get(0);
        }
    }

    /**
     * @see org.openmrs.module.ncd.database.dao.hibernate.ITaskStatusDAO#getRecentTaskStatus(int)
     */
    @SuppressWarnings("unchecked")
    public List<TaskRunStatus> getRecentTaskStatus(int statusCount) {

        Query query = sessionFactory.getCurrentSession().createQuery(
            "from TaskRunStatus st order by st.ended desc")
                .setMaxResults(statusCount);
        return (List<TaskRunStatus>) query.list();
    }

    /**
     * @see org.openmrs.module.ncd.database.dao.hibernate.ITaskStatusDAO#pruneTaskStatus(int)
     */
    @SuppressWarnings("unchecked")
    public void pruneTaskStatus(int maxAgeDays) {

        Session session = sessionFactory.getCurrentSession();

        // Calculate the cutoff date as today - maxAgeDays
    	Calendar calendar = new GregorianCalendar();
    	calendar.setTime(new Date());
   		calendar.add(Calendar.DAY_OF_MONTH, -maxAgeDays);
    	Date cutoffDate = calendar.getTime();
    	
    	// Create query to fetch all the task run status to be pruned
        HQLQueryBuilder builder = new HQLQueryBuilder("TaskRunStatus");
    	builder.add("started", "<", cutoffDate);

    	// Execute query, and iterate over the results
    	Query query = builder.getQuery(session);
    	for (TaskRunStatus status : (List<TaskRunStatus>) query.list()) {
    		
    		for (ExportedResult exportedResult : status.getExportedResults()) {
    			session.delete(exportedResult);
    		}
    		
    		for (ExportRecipient exportRecipient : status.getExportRecipients()) {
    			session.delete(exportRecipient);
    		}
    		
    		session.delete(status);
    	}
    }
}

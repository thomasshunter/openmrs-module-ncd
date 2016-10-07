/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.MonitoredCondition;
import org.openmrs.module.ncd.database.dao.IReportingDAO;
import org.openmrs.module.ncd.output.aggrpt.ReportData;
import org.openmrs.module.ncd.output.aggrpt.ReportDataBuilder;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

public class ReportingDAO implements IReportingDAO {

    private static Log logger = LogFactory.getLog(ReportingDAO.class);

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

    /*  */
    /**
     * Save a new report, or the changes to an existing report.
     * 
     * @param task The scheduled task representing the report.
     * @param monitoredConditions The List of monitored conditions for the
     * report, if any.
     */
    public void saveReport(TaskDefinition task, List<MonitoredCondition> monitoredConditions) {

    	// NOTE: If we don't stop and start the task like this, and the task is currently started,
    	// when it finishes it will overwrite our changes.

    	SchedulerService ss = Context.getSchedulerService();
    	try {
    		if (task.getId() != null) {
    			ss.shutdownTask(task);
    		}
		} catch (SchedulerException e) {
			// we don't care - it likely means that task wasn't started in the first place.
		}

		/*
		if (task.getUuid() == null) {
			task.setUuid(UUID.randomUUID().toString());
		}
		*/

    	ss.saveTask(task);
    	
    	if (task.getStartOnStartup()) {
    		try {
				ss.scheduleTask(task);
			} catch (SchedulerException e) {
				logger.error("Exception attempting to start task: " + e.getMessage(), e);
			}
    	}
   		
   		NCDUtilities.getService().setMonitoredConditions(task, monitoredConditions);
    }
    
    /**
     * Extract aggregate summary report data based on the supplied
     * filtering parameters.
     */

    @SuppressWarnings("unchecked")
    public ReportData getFakeCountAggregateSummaryData(Date[] bucketDates, Map<String, String> properties)
    {
        ReportDataBuilder data = new ReportDataBuilder(bucketDates);

        try {
            Session session = sessionFactory.getCurrentSession();

            String queryText =       
            "select cg.displayText, c.displayText"
            + " from Condition as c join c.conditionGroup as cg"
            + " order by cg.displayOrder, cg.displayText, c.displayText";

            Query query = session.createQuery(queryText);
            Iterator<Object[]> results = query.iterate();
            while (results.hasNext()) {
                Object[] resultRow = results.next();
                String groupName = (String) resultRow[0];
                String conditionName = (String) resultRow[1];
                for (int i = 0; i < bucketDates.length; i++) {
                    data.add(groupName, conditionName, bucketDates[i]);
                }
            }
        }
        catch (Exception e) {
            logger.error("Exception: " + e.getMessage(), e);
        }

        return data.getReportData();
    }
    
    /**
     * Extract real aggregate summary report data based on the supplied
     * filtering parameters.
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public ReportData getAggregateSummaryData(Date[] bucketDates, Map<String, String> properties) {

        long startTime = System.currentTimeMillis();

        // Get the bounds of the date/time range for reportable results
        // to be included in the report.
        Date lowDateTime = DateUtilities.truncate(bucketDates[bucketDates.length - 1]);
        Date highDateTime = bucketDates[0];
        
        logger.debug("lowDateTime=" + lowDateTime);
        logger.debug("highDateTime=" + highDateTime);
        
        ReportDataBuilder data = new ReportDataBuilder(bucketDates);

        try {

        	// The following query counts the number of instances of a particular reportable condition that
        	// have occurred on a particular date for a particular patient.
        	//
        	// There is no system requirement that the incoming messages identify the associated patient 
        	// with a unique, global id, and the NCD currently has no mechanism, such as a MPI (master
        	// patient index) built into it for reliably resolving patient identity.  As a result, it is 
        	// possible for the NCD to count instances of the same condition on the same day for the same
        	// patient more than once.  This is a known system limitation.
        	//
        	// The counting logic will correctly count (only once) the same condition on the same day
        	// for the same patient, but only if the lab consistently sends the same internal patient 
        	// id on all messages for the patient.
        	//
        	// Note: the counts are ordered by group name, rather than the group display sequence, 
        	// since this column isn't populated with useful sequence values as of the time of this writing.
        	//
        	// TODO: Build MPI capability into the NCD for resolving patient identity 
            // TODO: consider forcing the use of the index on ncd_reportable_result.releaseDate with a FORCE INDEX hint
            // TODO: Rewrite this using HQL instead of a native SQL query.
            String queryText =
            	"select  groupname, conditionname, datebucket, count(*) 'instances'" +
            	" from (" +
            	" select condgroup.groupname, result.conditionname, date(result.releasedate) datebucket," +
            	" if(patientinstitutionmedicalrecordid is null or length(trim(patientinstitutionmedicalrecordid))=0, convert(result.id,char), patientinstitutionmedicalrecordid) patientid" +
            	" from ncd_reportable_result result" +
            	" join ncd_condition cond on (result.conditionname=cond.conditionname)" +
            	" join ncd_condition_group condgroup on (cond.conditiongroupid=condgroup.id)" +
            	" where result.reviewStatusTypeId in (" + ConditionDetectorService.reviewStatusTypeNoReviewRequired + "," + 
          			ConditionDetectorService.reviewStatusTypeReleased + ")" +
            	" and result.releaseDate>=:lowDateTime" +
            	" and result.releaseDate<=:highDateTime" +
            	" and cond.reportable != 0" +
            	" group by condgroup.groupname, cond.conditionname, datebucket, patientid" +
            	" ) temp" +
            	" group by groupname, conditionname, datebucket" +
            	" order by groupname, conditionname, datebucket";            	

            Session session = sessionFactory.getCurrentSession();
            Query query = 
                session.createSQLQuery(queryText)
                .addScalar("GROUPNAME", Hibernate.STRING)
                .addScalar("CONDITIONNAME", Hibernate.STRING)
                .addScalar("DATEBUCKET", Hibernate.DATE)
                .addScalar("INSTANCES", Hibernate.INTEGER)
                .setParameter("lowDateTime", lowDateTime)
                .setParameter("highDateTime", highDateTime)
                ;

            Iterator<Object[]> results = query.list().iterator();
            
            while (results.hasNext()) {
                
                Object[] result = results.next();
                
                String groupName = (String) result[0];
                String conditionName = (String) result[1];
                Date bucketDate = (Date) result[2];
                Integer count = (Integer) result[3];
                
                logger.debug("adding (" 
                        + groupName 
                        + "," 
                        + conditionName 
                        + "," 
                        + bucketDate 
                        + "," 
                        + count 
                        + ").");

                data.add(groupName, conditionName, bucketDate, count);
            }
        }
        catch (Exception e) {

            logger.error("Exception: " + e.getMessage(), e);
        }

        logger.info("extract took " + (System.currentTimeMillis() - startTime) + " ms.");

        return data.getReportData();
    }
}

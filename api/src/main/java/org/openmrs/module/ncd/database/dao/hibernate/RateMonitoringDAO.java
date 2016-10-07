package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.MonitoredCondition;
import org.openmrs.module.ncd.database.ZeroCountElement;
import org.openmrs.module.ncd.database.ZeroCountUniverse;
import org.openmrs.module.ncd.database.dao.IRateMonitoringDAO;
import org.openmrs.module.ncd.database.filter.UnusualConditionRateFilter;
import org.openmrs.module.ncd.hql.Conjunction;
import org.openmrs.module.ncd.hql.Disjunction;
import org.openmrs.module.ncd.hql.HQL;
import org.openmrs.module.ncd.model.ConditionCount;
import org.openmrs.module.ncd.model.ZeroCountCondition;
import org.openmrs.module.ncd.utilities.DateRange;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.NCDConstants;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.scheduler.TaskDefinition;

public class RateMonitoringDAO implements IRateMonitoringDAO {

    /** Debugging log */
    private static Log log = LogFactory.getLog(RateMonitoringDAO.class);

    private static final String WILDCARD = "*";
    
    /** Hibernate session factory, set by spring. */
    private SessionFactory sessionFactory;

    /**
     * Set session factory. Spring calls this based on the
     * moduleApplicationContext.xml
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Find a monitored Entity:Condition mapping by entity and code.
     * 
     * @param task The task that owns the the mapping to be searched for.
     * @param application The application to be searched for.
     * @param facility The facility to be searched for.
     * @param location The location to be searched for.
     * @param condition The condition to be searched for.
     */
    public MonitoredCondition getMonitoredCondition(TaskDefinition task, String application, String facility, String location, Condition condition) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from MonitoredCondition" +
                " where task=:task" +
                " and application = :app" +
                " and facility = :fac" +
                " and location = :loc" +
                " and condition = :condition")
                .setParameter("task", task)
                .setParameter("app", application)
                .setParameter("fac", facility)
                .setParameter("loc", location)
                .setParameter("condition", condition);
        return (MonitoredCondition) query.uniqueResult();
    }

    /**
     * Find all MonitoredConditions for the specified task.
     * 
     * @param task The task for which the MonitoredConditions are to be fetched.
     * @return A List of the MonitoredConditions for the task.
     */
    @SuppressWarnings("unchecked")
	@Authorized( { NCDConstants.PRIV_VIEW_MONITORED_CODES } )
    public List<MonitoredCondition> getMonitoredConditions(TaskDefinition task) {
    	
    	if (task.getId() != null) {
	        Query query = sessionFactory.getCurrentSession().createQuery(
	                "from MonitoredCondition" +
	                " where task=:task" +
	                " order by application, facility, location")
	                .setParameter("task", task);
	        return (List<MonitoredCondition>) query.list();
    	}
    	else {
    		return new ArrayList<MonitoredCondition>();
    	}
    }
    
    /**
     * Replace all existing monitored conditions for a task by a new collection.
     * 
     * @param task The task whose monitored conditions are to be replaced.
     * @param monitoredConditions The new collection of monitored conditions.
     */
    public void setMonitoredConditions(TaskDefinition task, List<MonitoredCondition> monitoredConditions) {

    	log.debug("task=" + task + ", monitoredConditions=" + monitoredConditions);
    	
        Session session = sessionFactory.getCurrentSession();
        
    	// delete any current monitored conditions for the task
        Query query = session.createQuery(
                "delete from MonitoredCondition" +
                " where task=:task")
                .setParameter("task", task);
        query.executeUpdate();

    	if (monitoredConditions != null) {
    	
    		// Insert the new monitored conditions for the task
    		for (MonitoredCondition mc : monitoredConditions) {
    			
    	    	log.debug("adding mc=" + mc);

    	    	MonitoredCondition mcCopy = new MonitoredCondition(mc);
    	    	mcCopy.setId(null);
    	    	mcCopy.setTask(task);
    			session.save(mcCopy);
    		}
    	}
    }

    /**
     * Create or modify a monitored Entity:Condition mapping. 
     * 
     * @param entry The mapping to be created or modified.
     */
    public void saveMonitoredCondition(MonitoredCondition entry) {
        sessionFactory.getCurrentSession().saveOrUpdate(entry);
    }

    /**
     * Delete a monitored Entity:Condition mapping.
     * 
     * @param entry The mapping to be deleted.
     */
    public void deleteMonitoredCondition(MonitoredCondition entry) {
        sessionFactory.getCurrentSession().delete(entry);
    }

    /** Creates the elements for a default universe. The default
     * universe is the set of all application / facility / location / 
     * conditionName combinations found in the recorded reportable
     * results from before the end of the sampling period.
     * 
     * CASE WHEN rr.producer IS NULL THEN ''
     *      ELSE rr.producer.description
     *      END 
     * 
     * @param universe The universe to which the elements are to be
     * added.
     * @param window The date/time range we will later sample.
     */
    private void createDefaultElements(ZeroCountUniverse universe, DateRange window) {
    	
    	String queryText = 
    		"INSERT INTO ZeroCountElement (universe, application, facility," +
               " location, conditionName)" +
            " SELECT DISTINCT u, rr.sendingApplication, rr.sendingFacility," +
               " rr.sendingLocation, rr.conditionName" +
               " FROM ZeroCountUniverse u, ReportableResult rr" +
               " WHERE u.id=:uid" +
               " AND rr.messageReceivedDateTime < :high";
    	int elementCount = sessionFactory.getCurrentSession()
    							.createQuery(queryText)
    							.setParameter("uid", universe.getId())
    							.setParameter("high", window.getHigh())
    							.executeUpdate();
    	log.debug("inserted " + elementCount + " elements");
    }
    
    /** Creates the universe elements that match the MonitoredConditions
     * associated with the task the universe is associated with.
     * 
     * @param universe The universe to attach the elements to.
     * @param patterns The list of MonitoredConditions for this task.
     */
    private void createSpecifiedElements(ZeroCountUniverse universe, List<MonitoredCondition> patterns, DateRange window) {

    	Disjunction conditionFilters = HQL.disjunction();
    	HQL hql = HQL.bulkInsert()
    				 .into("ZeroCountElement")
    				 .column("universe")
    				 .column("application")
    				 .column("facility")
    				 .column("location")
    				 .column("conditionName")
    				 .select(HQL.select().setDistinct(true)
    						 			 .column("u")
    						 			 .column("rr.sendingApplication")
    						 			 .column("rr.sendingFacility")
    						 			 .column("rr.sendingLocation")
    						 			 .column("rr.conditionName")
    						 			 .from("ZeroCountUniverse u")
    						 			 .from("ReportableResult rr")
    						 			 .where(HQL.and(
    						 					 HQL.equalValue("u.id", universe.getId()), 
    						 					 HQL.and(
    						 						HQL.lessThanValue("rr.messageReceivedDateTime", window.getHigh()),
    						 						conditionFilters))));
    	for (MonitoredCondition pattern : patterns) {
    		Conjunction patternFilter = HQL.conjunction();
    		if (!WILDCARD.equals(pattern.getApplication())) {
    			patternFilter.add(HQL.equalValue("rr.sendingApplication", pattern.getApplication()));
    		}
    		if (!WILDCARD.equals(pattern.getFacility())) {
    			patternFilter.add(HQL.equalValue("rr.sendingFacility", pattern.getFacility()));
    		}
    		if (!WILDCARD.equals(pattern.getLocation())) {
    			patternFilter.add(HQL.equalValue("rr.sendingLocation", pattern.getLocation()));
    		}
    		if (pattern.getCondition() != null) {
    			patternFilter.add(HQL.equalValue("rr.conditionName", pattern.getCondition().getDisplayText()));
    		}
    		conditionFilters.add(patternFilter);
    	}
    	int elementCount = hql.query(sessionFactory.getCurrentSession()).executeUpdate();
    	log.debug("inserted " + elementCount + " elements");
    }

    /** searches for monitored app/fac/loc/condition combinations in
     * ReportableResult which don't appear in the specified date/time
     * window.
     * 
     * @param universe The universe of app/fac/loc/condition
     * combinations to search for.
     * @param window The date/time range of ReportableResults to
     * consider.
     * @return A List<ZeroCountElement> containing those combinations
     * in the specified universe which did not appear in any
     * ReportableResult in the specified date/time range.
     */
    @SuppressWarnings("unchecked")
	private List<ZeroCountElement> findZeroCountElements(ZeroCountUniverse universe, DateRange window) {

        String text = 
            "from ZeroCountElement zero" +
            " where zero not in (" +
                " select distinct nonzero" +
                " from ReportableResult rr," +
                     " ZeroCountElement nonzero" +
                " where rr.messageReceivedDateTime >= :low" +
                " and rr.messageReceivedDateTime < :high" +
                " and nonzero.universe.id=:uid" +
                " and rr.sendingApplication = nonzero.application" +
                " and rr.sendingFacility = nonzero.facility" +
                " and rr.sendingLocation = nonzero.location" +
                " and rr.conditionName = nonzero.conditionName" +
            ") order by zero.conditionName, zero.application, zero.facility, zero.location";

        Query query = sessionFactory.getCurrentSession().createQuery(text)
            .setParameter("low", window.getLow())
            .setParameter("high", window.getHigh())
            .setParameter("uid", universe.getId())
          ;

        List<ZeroCountElement> results = 
            (List<ZeroCountElement>) query.list();
        
        log.debug("zero count elements=" + results);

        return results;
    }
    
    /**
     * Attaches the producer and mpq seq number and received date time of the
     * most recent reportable result that matches each ZeroCountElement, if any.
     * Also discards ZCEs that map to an excluded producer.
     *  
     * @param zeroCounts
     * @param window
     * @return
     */
    @SuppressWarnings("unchecked")
	private List<ZeroCountCondition> findLatestReport(List<ZeroCountElement> zeroCounts, DateRange window) {
    	
		String queryText = 
			"select rr.messageReceivedDateTime, rr.mpqSeqNumber" +
			" from ReportableResult rr" +
			" where rr.sendingApplication = :app" +
			" and rr.sendingFacility = :fac" +
			" and rr.sendingLocation = :loc" +
			" and rr.conditionName = :cond" +
			" and rr.messageReceivedDateTime = (" +
				"select max(rr.messageReceivedDateTime)" +
				" from ReportableResult rr" +
				" where rr.sendingApplication = :app" +
				" and rr.sendingFacility = :fac" +
				" and rr.sendingLocation = :loc" +
				" and rr.conditionName = :cond" +
				" and rr.messageReceivedDateTime < :high" +
			")";
		
		Query query = sessionFactory.getCurrentSession().createQuery(queryText)
		    		.setParameter("high", window.getHigh())
		    		.setMaxResults(1)
		    		;
        
		ConditionDetectorService cds = NCDUtilities.getService();
        List<ZeroCountCondition> zeroRates = new ArrayList<ZeroCountCondition>(zeroCounts.size());
        for (ZeroCountElement element : zeroCounts) {
     	   
        	String app = element.getApplication();
        	String fac = element.getFacility();
        	String loc = element.getLocation();
        	HL7Producer producer = cds.getProducer(app, fac, loc);
        	
        	if (producer == null || !producer.isExcluded()) {

	     	    ZeroCountCondition zrc = new ZeroCountCondition();
	     	    zrc.setApplication(app);
	     	    zrc.setFacility(fac);
	     	    zrc.setLocation(loc);
	     	    zrc.setProducer(producer);
	     	    zrc.setConditionName(element.getConditionName());
	     	   
	     	    query.setParameter("app", zrc.getApplication())
	     	   		 .setParameter("fac", zrc.getFacility())
	     	   		 .setParameter("loc", zrc.getLocation())
	     	   		 .setParameter("cond", zrc.getConditionName());
	
	     	    List<Object[]> results = query.list(); 
	     	    if (results != null && results.size() > 0) {
	     		   
	     		    Object[] lastResult = results.get(0);
	     		    Date dateLastReceived = (Date) lastResult[0];
	     		    String lastMpqSeqNumber = (String) lastResult[1];
	     		   
	     		    log.debug("dateLastReceived=" + dateLastReceived);
	     		    log.debug("lastMpqSeqNumber=" + lastMpqSeqNumber);
	
	         	    zrc.setDateLastReceived(dateLastReceived);
	         	    zrc.setLastMpqSeqNumber(lastMpqSeqNumber);
	     	    }
	
	     	    zeroRates.add(zrc);
        	}
        }
        
        log.debug("zeroRateConditions=" + zeroRates);
        
        return zeroRates;
    }

    /**
     * Gets a list containing all the app/fac/loc/condition combinations that are
     * selected for monitoring, and for which _no_ reportable results were
     * detected in the specified date/time window.
     *
     * @param task The task whose MonitoredConditions should be used.
     * @param window The date/time window over which to search.
     * @return The list of app/loc/condition combinations not seen.
     */
    public List<ZeroCountCondition> getZeroCountConditions(TaskDefinition task, DateRange window) {

    	Session session = sessionFactory.getCurrentSession();
    	
    	// Insert a ZeroCountUniverse for this run
    	ZeroCountUniverse universe = new ZeroCountUniverse(task, new Date());
        session.save(universe);
        log.debug("inserted universe=" + universe);
    	
    	// Phase 1 - determine the set of app/fac/loc/condition
    	// combinations to monitor, i.e., the universe.
        List<MonitoredCondition> patterns = getMonitoredConditions(task);
        if (patterns.size() > 0) {
        	
        	createSpecifiedElements(universe, patterns, window);
        }
        else {
        	
        	createDefaultElements(universe, window);
        }
        
    	// Phase 2 - determine the set of app/fac/loc/condition
    	// combinations that did not appear in the specified date/time
    	// window. These are the zero count conditions.
        List<ZeroCountElement> results = findZeroCountElements(universe, window);
       
	   	// Phase 3 - for each zero count condition, find the last
	   	// matching reportable result, if any, and attach its received
        // date/time and mpqSeqNumber to the ZeroCountCondition.
        List<ZeroCountCondition> zeroCounts = findLatestReport(results, window);
        
        // Phase 4 - Delete the ZeroCountUniverse for this run
        session.delete(universe);

        return zeroCounts;
    }
    
    public static class ConditionCountComparator implements Comparator<ConditionCount> {

        public int compare(ConditionCount o1, ConditionCount o2) {
            
            ConditionCount left = (ConditionCount) o1;
            ConditionCount right = (ConditionCount) o2;
            
            int result = left.getConditionname().compareTo(right.getConditionname());
            if (result == 0) {
                result = left.getApplication().compareTo(right.getApplication());
                if (result == 0) {
                    result = left.getLocation().compareTo(right.getLocation());
                    if (result == 0) {
                        result = left.getFacility().compareTo(right.getFacility());
                    }
                }
            }
            
            return result;
        }
    }

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
    public List<ConditionCount> getConditionCounts(DateRange currentWindow, DateRange historicalWindow) {

        long currentMillis = currentWindow.getHigh().getTime() - 
                             currentWindow.getLow().getTime();
        long historicalMillis = historicalWindow.getHigh().getTime() - 
                                historicalWindow.getLow().getTime();
        double currentDays = currentMillis / (24.0 * 60.0 * 60.0 * 1000.0);
        double historicalDays = historicalMillis / (24.0 * 60.0 * 60.0 * 1000.0);

        Map<ConditionCount, ConditionCount> counts = new HashMap<ConditionCount, ConditionCount>();

        // Create ConditionCounts in the set for the "current window"
        Iterator<Object[]> iterator = getConditionCounts(currentWindow);
        while (iterator.hasNext()) {
            
            Object[] row = iterator.next();
            ConditionCount newCount = new ConditionCount();
            newCount.setApplication((String) row[0]);
            newCount.setLocation((String) row[1]);
            newCount.setFacility((String) row[2]);
            newCount.setConditionname((String) row[3]);
            int count = ((Long) row[4]).intValue(); 
            newCount.setRecentCount(count);
            newCount.setRecentRate(count / currentDays);
            
            counts.put(newCount, newCount);
        }

        // Create or update ConditionCounts in the set for the "historical window"
        iterator = getConditionCounts(historicalWindow);
        while (iterator.hasNext()) {
            
            Object[] row = (Object[]) iterator.next();
            ConditionCount newCount = new ConditionCount();
            newCount.setApplication((String) row[0]);
            newCount.setLocation((String) row[1]);
            newCount.setFacility((String) row[2]);
            newCount.setConditionname((String) row[3]);
            int count = ((Long) row[4]).intValue(); 

            ConditionCount oldCount = counts.get(newCount);
            if (oldCount != null) {

                oldCount.setHistoricalCount(count);
                oldCount.setHistoricalRate(count / historicalDays);
            }
            else {
                
                newCount.setHistoricalCount(count);
                newCount.setHistoricalRate(count / historicalDays);
                
                counts.put(newCount, newCount);
            }
        }

        List<ConditionCount> sortedCounts = new ArrayList<ConditionCount>(counts.values());
        Collections.sort(sortedCounts, new ConditionCountComparator());

        return sortedCounts;
    }

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
    public List<ConditionCount> getUnusualConditionRates(DateRange sampleWindow, UnusualConditionRateFilter filter) {

        log.info("enter");
        log.debug("sampleWindow=" + sampleWindow);
        log.debug("filter=" + filter);

        DateRange historicalWindow = new DateRange();
        historicalWindow.setHigh(sampleWindow.getLow());
        historicalWindow.setLow(DateUtilities.adjust(
                historicalWindow.getHigh(), 
                Calendar.DAY_OF_YEAR, -filter.getHistoryDays()));

        log.debug("historicalWindow=" + sampleWindow);

        // NOTE: Originally, this code tried to delete "normal" counts from
        // the list returned by getConditionCounts and return that, but it
        // failed with a concurrent modification exception.

        // Get the counts and rates in the window
        List<ConditionCount> counts = getConditionCounts(sampleWindow, historicalWindow);
        log.debug("counts=" + counts);

        // Include the ones that are "unusual"
        List<ConditionCount> unusualCounts = new ArrayList<ConditionCount>();
        for (ConditionCount count : counts) {
            
            double lowRateBound = filter.getLowRateRatio() * count.getHistoricalRate();
            double highRateBound = filter.getHighRateRatio() * count.getHistoricalRate();
            if ((count.getRecentRate() < lowRateBound) ||
                (count.getRecentRate() > highRateBound))
            {
                log.debug("unusual count=" + count);
                unusualCounts.add(count);
            }
        }

        log.debug("unusualCounts=" + unusualCounts);
        log.info("exit");
        
        // Return the unusual counts
        return unusualCounts;
    }

    @SuppressWarnings("unchecked")
    private Iterator<Object[]> getConditionCounts(DateRange window) {

        log.info("enter");
        log.debug("window=" + window);

        String text = 
            "select rr.sendingApplication, rr.sendingLocation," +
            " rr.sendingFacility, rr.conditionName, count(*)" +
            " from ReportableResult rr" +
            " where rr.messageReceivedDateTime >= :low" +
            " and rr.messageReceivedDateTime < :high" +
            " group by rr.sendingApplication, rr.sendingLocation, rr.sendingFacility, rr.conditionName";

        Query query = sessionFactory.getCurrentSession().createQuery(text)
            .setParameter("low", window.getLow())
            .setParameter("high", window.getHigh())
          ;
        
        log.info("exit");
        
        return (Iterator<Object[]>) query.iterate();
    }
}

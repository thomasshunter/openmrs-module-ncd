package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.dao.DataSourceInfo;
import org.openmrs.module.ncd.database.dao.IDataSourceReportDAO;
import org.openmrs.module.ncd.database.filter.DataSourceReportFilter;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.scheduler.TaskDefinition;

public class DataSourceReportDAO implements IDataSourceReportDAO {

    private static Log log = LogFactory.getLog(DataSourceReportDAO.class);
    
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

    public class DataSourceInfoComparator implements Comparator<DataSourceInfo> {

    	@SuppressWarnings("unused")
		private DataSourceReportFilter filter;
    	
    	public DataSourceInfoComparator(DataSourceReportFilter filter) {
    	
    		// Just in case we allow the filter to control the sort later.
    		this.filter = filter;
    	}
    	
		@Override
		public int compare(DataSourceInfo left, DataSourceInfo right) {

			int cmp = 0;
			if (left.getProducer() == null && right.getProducer() != null) {

				// Missing producers sort first
				return -1;
			}
			else if (left.getProducer() != null && right.getProducer() == null) {

				// Missing producers sort first
				return 1;
			}
			else if (left.getProducer() != null && right.getProducer() != null) {

				cmp = StringUtilities.compareTo(left.getProducer().getDescription(), 
												right.getProducer().getDescription());
			}

			if (cmp == 0) {
			
				cmp = StringUtilities.compareTo(left.getApplication(),
											    right.getApplication());
			}

			if (cmp == 0) {
			
				cmp = StringUtilities.compareTo(left.getFacility(),
											    right.getFacility());
			}

			if (cmp == 0) {
			
				cmp = StringUtilities.compareTo(left.getLocation(),
											    right.getLocation());
			}
			
			return cmp;
		}
    }
    
    public class DataSourceInfoSet {

    	private Map<DataSourceInfo, DataSourceInfo> infoMap
    		= new HashMap<DataSourceInfo, DataSourceInfo>();
    	
    	public DataSourceInfoSet() {
    	}
    	
    	public boolean contains(String application, String facility, String location) {
    		
    		DataSourceInfo info = buildDataSourceInfo(application, facility, location);
    		
    		return infoMap.containsKey(info);
    	}
    	
    	public void add(String application, String facility, String location) {
    		
    		DataSourceInfo info = buildDataSourceInfo(application, facility, location);
    		
			if (info.getProducer() == null || !info.getProducer().isExcluded()) {
    		
				infoMap.put(info, info);
			}
    	}
    	
    	public DataSourceInfo get(String application, String facility, String location) {
    		
    		DataSourceInfo info = buildDataSourceInfo(application, facility, location);
    		
    		return infoMap.get(info);
    	}
    	
    	public Collection<DataSourceInfo> getEntries() {

    		return infoMap.values();
    	}

        private DataSourceInfo buildDataSourceInfo(String application,
        		String facility, String location)
        {
        	HL7Producer producer = 
        		NCDUtilities.getService().getProducer(application, facility, 
        				location);
        	
    		DataSourceInfo info = new DataSourceInfo();
    		info.setApplication(application);
    		info.setFacility(facility);
    		info.setProducer(producer);
    		
    		if (producer != null && producer.getLocationname() == null) {
    			info.setLocation(null);
    		}
    		else {
    			info.setLocation(location);
    		}

    		return info;
        }
    }

    public List<DataSourceInfo> findDataSourceInfo(TaskDefinition task, DataSourceReportFilter filter) {
        
    	// TODO: There is probably a more efficient way to do this, that uses a
    	// single more complex query.

    	log.debug("filter=" + filter);

        // Phase 1 - find the app/fac/loc combinations to look for
    	DataSourceInfoSet dataSourceSet = getDataSources();
    	
    	if (log.isDebugEnabled()) {
    		log.debug("phase 1 dataSources=" + dataSourceSet);
    	}

    	// Phase 2 - for each combination, find the number of matching processed
    	// messages in the sampling window
    	findProcessedMessageCounts(filter, dataSourceSet);
        
    	if (log.isDebugEnabled()) {
    		log.debug("phase 2 dataSources=" + dataSourceSet);
    	}

        // Phase 3 - attach the count of reportable results with the same
        // app/fac/loc and non-null release dates in the sampling window.
        findReportableResultCounts(filter, dataSourceSet);

    	if (log.isDebugEnabled()) {
    		log.debug("phase 3 dataSources=" + dataSourceSet);
    	}

    	List<DataSourceInfo> dataSources = new ArrayList<DataSourceInfo>();
    	dataSources.addAll(dataSourceSet.getEntries());

        // Phase 4 - sort the rows
        Collections.sort(dataSources, new DataSourceInfoComparator(filter));
        
    	if (log.isDebugEnabled()) {
    		log.debug("phase 4 dataSources=" + dataSources);
    	}

        return dataSources;
    }
    
    // Find all app/fac/loc combinations we want to report on
    @SuppressWarnings("unchecked")
	private DataSourceInfoSet getDataSources() {
    	
    	String queryText = 
    		"SELECT DISTINCT pmc.application, pmc.facility, pmc.location" +
               " FROM ProcessedMessageCount pmc";
    	List<Object[]> combos =
    		(List<Object[]>) sessionFactory.getCurrentSession()
    							.createQuery(queryText)
    							.list();

    	log.debug("searching for " + combos.size() + " combinations");
    	
    	DataSourceInfoSet sources = new DataSourceInfoSet();
    	
    	for (Object[] combo : combos) {
    		
    		String application = (String) combo[0];
    		String facility = (String) combo[1];
    		String location = (String) combo[2];
    		
    		if (!sources.contains(application, facility, location)) {
    			
				sources.add(application, facility, location);
    		}
    	}
    	
    	return sources;
    }
    
    @SuppressWarnings("unchecked")
	private void findProcessedMessageCounts(DataSourceReportFilter filter,
			DataSourceInfoSet dataSourceSet)
    {
    	// Find the matching ProcessedMessageCount app/fac/loc combinations in the
    	// sampling window, and the count of each.
        Query query = sessionFactory.getCurrentSession().createQuery(
                "select pm.application, pm.facility, pm.location, pm.messageCount" +
                " from ProcessedMessageCount pm" +
                " where pm.processedDateTime >= :low" +
                " and   pm.processedDateTime <= :high")
                .setParameter("low", filter.getSamplingWindow().getLow())
                .setParameter("high", filter.getSamplingWindow().getHigh());

        List<Object[]> messageInfos = (List<Object[]>) query.list();

        // For each row returned
        for (Object[] elements : messageInfos) {
        	
        	// Create a matching DataSourceInfo
        	String application = (String) elements[0];
        	String facility = (String) elements[1];
        	String location = (String) elements[2];
        	long messageCount = (Long) elements[3];

        	// Find the matching entry in the dataSourceSet
        	DataSourceInfo entry = dataSourceSet.get(application, facility, location);
        	if (entry != null) {

        		// Add to its message count
        		entry.setMessageCount(entry.getMessageCount() + messageCount);
        	}
        }
    }
    
    @SuppressWarnings("unchecked")
	private void findReportableResultCounts(DataSourceReportFilter filter, DataSourceInfoSet dataSourceSet) {

        Query query = sessionFactory.getCurrentSession().createQuery(
                "select sendingApplication, sendingFacility, sendingLocation, count(*)" +
                " from ReportableResult" +
                " where releaseDate IS NOT NULL" +
                " and releaseDate >= :low" +
                " and releaseDate <= :high" +
                " group by sendingApplication, sendingFacility, sendingLocation")
                .setParameter("low", filter.getSamplingWindow().getLow())
                .setParameter("high", filter.getSamplingWindow().getHigh());
        
        List<Object[]> counts = (List<Object[]>) query.list();
        
    	for (Object[] sourceCount : counts) {
    		
    		String application = (String) sourceCount[0];
    		String facility = (String) sourceCount[1];
    		String location = (String) sourceCount[2];
    		long resultCount = (Long) sourceCount[3];

        	// Find the matching entry in the dataSourceSet
        	DataSourceInfo entry = dataSourceSet.get(application, facility, location);
        	if (entry != null) {

        		// Add to its message count
        		entry.setResultCount(entry.getResultCount() + resultCount);
        	}
    	}
    }
}

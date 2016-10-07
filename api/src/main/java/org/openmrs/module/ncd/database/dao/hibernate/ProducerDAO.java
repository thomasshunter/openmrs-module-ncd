/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.cache.HL7ProducerCache;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.dao.IProducerDAO;
import org.openmrs.module.ncd.database.filter.SearchFilterProducers;
import org.openmrs.module.ncd.database.filter.SearchResult;
import org.openmrs.module.ncd.utilities.IUpdatableData;
import org.springframework.transaction.annotation.Transactional;

public class ProducerDAO implements IProducerDAO, IUpdatableData {

    /** Debugging log */
    private static Log log = LogFactory.getLog(ProducerDAO.class);

    /** Hibernate session factory */
    private SessionFactory sessionFactory;

    private static HL7ProducerCache hl7ProducerCache = null;

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

    /*
     * (non-Javadoc)
     * 
     * @see org.openmrs.module.ncd.database.dao.IProducerDAO#getReportAllForApplicationFacility(java.lang.String,
     *      java.lang.String)
     */
    public HL7Producer getProducer(String applicationName, 
    		String facilityName, String locationName) {
        initCache();

        return hl7ProducerCache.find(applicationName, facilityName, locationName);
    }

    public HL7Producer getProducerExact(String applicationName, 
    		String facilityName, String locationName) {
        initCache();

        return hl7ProducerCache.findExact(applicationName, facilityName, locationName);
    }

    public HL7Producer getProducer(long id) {
    	
        Query query = sessionFactory.getCurrentSession().createQuery(
    		"from HL7Producer where id=:id")
    		.setParameter("id", new Long(id));

        return (HL7Producer) query.uniqueResult();
    }

    public void saveProducer(HL7Producer src) {
    	
        if (src.getId() == null) {

            sessionFactory.getCurrentSession().save(src);
        }
        else {
            
            sessionFactory.getCurrentSession().merge(src);
        }
    	
        updateData();
    }
    
    public void deleteProducer(HL7Producer src) {
    	
    	sessionFactory.getCurrentSession().delete(src);
        updateData();
    }

    @SuppressWarnings("unchecked")
	public List<HL7Producer> getAllProducers() {

        Query query = sessionFactory.getCurrentSession().createQuery(
        	"from HL7Producer p left join fetch p.institution" +
        	" order by p.applicationname, p.facilityname, p.locationname");

        return (List<HL7Producer>) query.list();
    }

    @SuppressWarnings("unchecked")
	public List<HL7Producer> getAllUnretiredProducers() {

        Query query = sessionFactory.getCurrentSession().createQuery(
        	"from HL7Producer p left join fetch p.institution" +
        	" where p.retired = false" +
        	" order by p.applicationname, p.facilityname, p.locationname");

        return (List<HL7Producer>) query.list();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmrs.module.ncd.utilities.IUpdatableData#updateData()
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void updateData() {
        hl7ProducerCache = new HL7ProducerCache();
        hl7ProducerCache.addCollection(getAllUnretiredProducers());
    }

    private synchronized void initCache() {
        if (hl7ProducerCache == null) {
            updateData();
        }
    }

	@SuppressWarnings("unchecked")
	public SearchResult<HL7Producer> findProducers(SearchFilterProducers filter) {
		

        Session dbSession = sessionFactory.getCurrentSession();

        SearchResult<HL7Producer> results = new SearchResult<HL7Producer>();
        results.setSuccessful(false);
        
        try {

            HQLQueryBuilder builder = new HQLQueryBuilder("HL7Producer p");
            builder.addPrefetchJoin("left join fetch p.institution");
            
            // add filter terms here, none currently
            
        	String sortFieldName = filter.getSortFieldName();
            if (sortFieldName != null) {
                
            	// NCD-215 without the "p." prefix on the sort key, sorting on the description column blows up
            	// because it's ambiguous (both HL7Producer and Institution have description columns). Sorting
            	// on institution.name also blows up for less obvious reasons.
            	if (!sortFieldName.startsWith("p.")) {
            		sortFieldName = "p." + sortFieldName;
            	}
                builder.setSort(sortFieldName);
                builder.setSortAscending(filter.isSortAscending());
            }

            Query query = builder.getQuery(dbSession)
                .setMaxResults(filter.getMaxRows() + 1);
            
            List<HL7Producer> rows = (List<HL7Producer>) query.list();
            
            results.setSuccessful(true);
            results.setThrowable(null);
            results.setLimited(rows.size() > filter.getMaxRows());
            results.setRowCount(rows.size());   // bogus
            
            if (results.isLimited()) {
                
                results.setResultRows(rows.subList(0, filter.getMaxRows()));

                // Rerun the query without the row limit, only
                // counting rows, to correctly set rowCount.

                Query query2 = builder.getCountQuery(dbSession);
                results.setRowCount((Long) query2.uniqueResult());
            }
            else {

                results.setResultRows(rows);
            }
        }
        catch (Exception e) {

            results.setSuccessful(false);
            results.setThrowable(e);
            results.setLimited(false);
            results.setRowCount(0);
            results.setResultRows(new ArrayList<HL7Producer>());
            
            log.error("exception: " + e.getMessage(), e);
        }

        return results;
	}
}

/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.database.DecidedResultArchive;
import org.openmrs.module.ncd.database.dao.IDecidedResultDAO;
import org.openmrs.module.ncd.database.filter.SearchFilterDecidedResults;
import org.openmrs.module.ncd.database.filter.SearchResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class DecidedResultDAO implements IDecidedResultDAO {

    /** Debugging log */
    private static Log log = LogFactory.getLog(DecidedResultDAO.class);

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

    @SuppressWarnings("unchecked")
    public List<DecidedResult> findDecidedResults(DecidedResult decidedResultTemplate) {
        Session dbSession = sessionFactory.getCurrentSession();
        DecidedResult copy = new DecidedResult(decidedResultTemplate);
        copy.setManuallyReviewed(null);
        Criteria crit = dbSession.createCriteria(copy.getClass());
        crit.add(Example.create(copy));        

        return (List<DecidedResult>) crit.list();
    }

    public DecidedResultArchive saveDecidedResult(DecidedResult result) {
    	
    	if (result != null) {
	        Session session = sessionFactory.getCurrentSession();
	        session.saveOrUpdate(result);
	        DecidedResultArchive archiveResult = new DecidedResultArchive(result);
	        archiveResult.setId(result.getId());
	        archiveResult = (DecidedResultArchive) session.merge(archiveResult);
	        return archiveResult;
    	}
    	else {
    		return null;
    	}
    }

    public List<DecidedResultArchive> saveDecidedResults(List<DecidedResult> decidedResults) {

    	List<DecidedResultArchive> archivedResults = new ArrayList<DecidedResultArchive>();
        for (DecidedResult result : decidedResults) {
        	archivedResults.add(saveDecidedResult(result));
        }

        return archivedResults;
    }

    public DecidedResult getDecidedResult(long id) {
        
        Session dbSession = sessionFactory.getCurrentSession();
        Query query = 
            dbSession.createQuery("from DecidedResult where id = :id")
                .setParameter("id", id);
        return (DecidedResult) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public SearchResult<DecidedResult> findDecidedResults(SearchFilterDecidedResults filter) {

        Session dbSession = sessionFactory.getCurrentSession();

        SearchResult<DecidedResult> results = new SearchResult<DecidedResult>();
        results.setSuccessful(false);

        try {

            HQLQueryBuilder builder = new HQLQueryBuilder("DecidedResult dr");
            builder.add("dr.resultCode", filter.getResultcode());
            builder.add("dr.resultValue", filter.getResultvalue());
            builder.add("dr.resultCount", filter.getResultcount());
            builder.add("dr.dateAdded", filter.getDateadded());
            builder.add("dr.dateClassified", filter.getDateclassified());
            builder.add("dr.classifiedByWhom", filter.getClassifiedbywhom());
            builder.add("dr.classifiedByWhom", filter.getCritic());
            builder.add("dr.conditionName", filter.getConditionname());
            builder.add("dr.lastModified", filter.getLastmodified());
            builder.add("dr.mpqSequenceNumber", filter.getMpqsequencenumber());
            builder.add("dr.obr", filter.getObr());
            builder.add("dr.obrCodeSystem", filter.getObrCodeSystem());
            builder.add("dr.obrText", filter.getObrtext());
            builder.add("dr.obx", filter.getObx());
            builder.add("dr.obxCodeSystem", filter.getObxCodeSystem());
            builder.add("dr.obxText", filter.getObxtext());
            builder.add("dr.nte", filter.getNte());
            builder.add("dr.loincCode", filter.getLoinccode());
            builder.add("dr.disposition", filter.getDisposition());
            builder.add("dr.reportable", filter.getReportable());
            builder.add("dr.voided", filter.getVoided());
            builder.add("dr.dateVoided", filter.getDateVoided());
            
            if (filter.getSortFieldName() != null) {
                
                builder.setSort(filter.getSortFieldName());
                builder.setSortAscending(filter.isSortAscending());
            }

            Query query = builder.getQuery(dbSession)
                .setMaxResults(filter.getMaxRows() + 1);
            
            List<DecidedResult> rows = (List<DecidedResult>) query.list();
            
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
            results.setResultRows(new ArrayList<DecidedResult>());
            
            log.error("exception: " + e.getMessage(), e);
        }

        return results;
    }
    
    /**
     * Removes (delete) the listed DecidedResults.
     * @param resultsToRemove The list of decided results to be removed.
     */
    public void removeDecidedResults(List<DecidedResult> resultsToRemove) {

    	Session dbSession = sessionFactory.getCurrentSession();
    	for (DecidedResult result : resultsToRemove) {
    		dbSession.delete(result);
    	}
    }
}

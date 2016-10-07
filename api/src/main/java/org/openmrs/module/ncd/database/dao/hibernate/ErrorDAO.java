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
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.Error;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.database.dao.IErrorDAO;
import org.openmrs.module.ncd.database.filter.SearchFilterAlertSummary;
import org.openmrs.module.ncd.database.filter.SearchFilterError;
import org.openmrs.module.ncd.database.filter.SearchResult;
import org.openmrs.module.ncd.model.Zvx;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jlbrown
 *
 */
public class ErrorDAO implements IErrorDAO
{
    /** Debugging log */
    private static Log log = LogFactory.getLog(ErrorDAO.class);

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

    @Transactional(rollbackFor = Exception.class)
    public void storeError(Error errorRow)
    {
        sessionFactory.getCurrentSession().save(errorRow);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void updateError(Error errorRow)
    {
    	sessionFactory.getCurrentSession().update(errorRow);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteError(Error errorRow, String dismissReason)
    {
        log.debug("Dismissing alert(s) associated with this error (if any).");

        ConditionDetectorService cds = NCDUtilities.getService();
    	SearchFilterAlertSummary filter = new SearchFilterAlertSummary();
    	filter.getDismissed().setValue("false");
    	filter.setIdentity(ConditionDetectorService.alertIdentityCandidateResult + "[" + errorRow.getMpqSeqNumber() + "]");
        SearchResult<AlertSummary> alertSummaries = cds.findAlertSummaries(filter);

        // Dismiss the associated alert summary (filter will return 1 in a list)
        cds.dismissAlertSummaries(alertSummaries.getResultRows(), dismissReason);

        // now delete the error
    	sessionFactory.getCurrentSession().delete(errorRow);
    }
    
    @SuppressWarnings("unchecked")
    public SearchResult<Error> findErrors(SearchFilterError filter) {

        Session dbSession = sessionFactory.getCurrentSession();

        SearchResult<Error> results = new SearchResult<Error>();
        results.setSuccessful(false);
        
        try {

            HQLQueryBuilder builder = new HQLQueryBuilder("err", "Error err");
            builder.add("err.lastErrorDate", filter.getLastErrorDate());
            builder.add("err.description", filter.getDescription());
            builder.add("err.rawMessage.messageText", filter.getHl7Message());
            builder.add("err.hidden", filter.getHidden());

            if (filter.getSortFieldName() != null) {
                
                builder.setSort("err." + filter.getSortFieldName());
                builder.setSortAscending(filter.isSortAscending());
            }

            Query query = builder.getQuery(dbSession)
                .setMaxResults(filter.getMaxRows() + 1);
            
            List<Error> rows = (List<Error>) query.list();
            
            results.setSuccessful(true);
            results.setThrowable(null);
            results.setLimited(rows.size() > filter.getMaxRows());
            results.setRowCount(rows.size());
            
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
            results.setResultRows(new ArrayList<Error>());
            
            log.error("exception: " + e.getMessage(), e);
        }
        
        return results;
    }

    /**
     * Get an error by id
     */
    public Error findErrorById(Long id) {
    	try {
	        Query query = sessionFactory.getCurrentSession().createQuery(
	        "from Error where id = :id")
	        .setLong("id", id);
	
	        Error err = (Error) query.uniqueResult();
	        return err;
    	}
    	catch (Exception e) {
    		log.error(e);
    		return null;
    	}
    }
    
    /**
     * Get an error by raw message (by matching mpqSeqNumber)
     */
    public Error findErrorByRawMessage(RawMessage message) {
    	try {
    		String mpqSeqNumber = Zvx.getMpq(message.getMessageText());
    		if (mpqSeqNumber == null) {
    			// Not possible to find a matching error without the mpqSeqNumber
    			return null;
    		}
    		// Lookup by mpqSeqNumber, to match up errors for the same message even
    		// if duplicate copies of message text are being stored in ncd_raw_message.
	        Query query = sessionFactory.getCurrentSession().createQuery(
	        "from Error where mpqSeqNumber = :mpqseqnumber")
	        .setString("mpqseqnumber", mpqSeqNumber);
	
	        Error err = (Error) query.uniqueResult();
	        return err;
    	}
    	catch (Exception e) {
    		log.error(e);
    		return null;
    	}
    }
}

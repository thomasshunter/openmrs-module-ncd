package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.AlertType;
import org.openmrs.module.ncd.database.dao.IAlertSummaryDAO;
import org.openmrs.module.ncd.database.filter.SearchFilterAlertSummary;
import org.openmrs.module.ncd.database.filter.SearchResult;
import org.openmrs.module.ncd.utilities.Pair;

public class AlertSummaryDAO implements IAlertSummaryDAO {

	/** Debugging log */
    private static Log log = LogFactory.getLog(AlertSummaryDAO.class);

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

    public void addAlertSummary(AlertSummary alertSummary) {
    	try {
    		Session session = sessionFactory.getCurrentSession();
    		
            // Find the existing, undismissed alert summary for this identity
            Query query = session.createQuery("from AlertSummary where identity=:identity and dismissed=0")
            	.setParameter("identity", alertSummary.getIdentity());
            AlertSummary oldSummary = (AlertSummary) query.uniqueResult();
            
            // If there is an undismissed alert summary for this identity
            if (oldSummary != null) {
            	
            	// Update the last date and occurrences count
            	oldSummary.setLastDate(alertSummary.getLastDate());
            	oldSummary.setOccurrences(oldSummary.getOccurrences() + 1);
            	
            	// Update the summary and details (they are permitted to change in the latest occurrence)
            	oldSummary.setSummary(alertSummary.getSummary());
            	oldSummary.setDetails(alertSummary.getDetails());
            	
            	// All other fields retain the initial values (they are not permitted to change for the same identity)
            	
            	// Update it
                session.saveOrUpdate(oldSummary);
            }
            else {
            	// Add a new alert summary
                session.saveOrUpdate(alertSummary);
            }
    	}
    	catch (Exception e) {
    		log.error(e);
    	}
    }

    /** Find alert summaries which match a filter
     * @param filter The search criteria.
     * @return A list of alert summaries that match the search criteria.
     */
    @SuppressWarnings("unchecked")
    public SearchResult<AlertSummary> findAlertSummaries(SearchFilterAlertSummary filter) {
        Session dbSession = sessionFactory.getCurrentSession();

        SearchResult<AlertSummary> results = new SearchResult<AlertSummary>();
        results.setSuccessful(false);
        
        try {

            HQLQueryBuilder builder = new HQLQueryBuilder("AlertSummary");
            // identity filter is a special case used only internally by the system, and assumes equality operator
            if (filter.getIdentity() != null) {
            	builder.add("identity", "=", filter.getIdentity());
            }
            builder.add("dismissed", filter.getDismissed());
            builder.add("alertType.id", filter.getAlertType());
            builder.add("firstDate", filter.getFirstOccurred());
            builder.add("lastDate", filter.getLastOccurred());
            
            if (filter.getSortFieldName() != null) {
	            builder.setSort(filter.getSortFieldName());
	            builder.setSortAscending(filter.isSortAscending());
            }
            
            Query query = builder.getQuery(dbSession)
            	.setMaxResults(filter.getMaxRows() + 1);

            List<AlertSummary> rows = (List<AlertSummary>) query.list();
            
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
            results.setResultRows(new ArrayList<AlertSummary>());
            
            log.error("exception: " + e.getMessage(), e);
        }

        return results;
	}

    /** Dismisses an alert summary with an optional reason
     * 
     * @param alertSummary The alert summary to dismiss.
     * @param user The current logged in user.
     * @param reason An optional reason for the dismissal.
     */
    public void dismissAlertSummary(AlertSummary alertSummary, User user, String reason) {
    	
    	try {
    		Session session = sessionFactory.getCurrentSession();
    		
    		alertSummary.setDismissed(true);
    		alertSummary.setDismissedDate(new Date());
    		alertSummary.setDismissedUser(user);
    		alertSummary.setDismissedReason(reason);
    		
    		// Store a denormalized copy of the user name for sorting and display purposes
    		alertSummary.setDisplayDismissedUserName(getName(user));

    		session.saveOrUpdate(alertSummary);
    	}
    	catch (Exception e) {
    		log.error(e);
    	}
    }
    
    /** Dismiss several alert summaries with an optional reason
     * 
     * @param alertSummaries The alert summaries to dismiss.
     * @param user The current logged in user.
     * @param reason An optional reason for the dismissal.
     */
    public void dismissAlertSummaries(List<AlertSummary> alertSummaries, User user, String reason) {
    	
		// For each alert summary
		for (AlertSummary alertSummary : alertSummaries) {
    		dismissAlertSummary(alertSummary, user, reason);
		}
    }
    
    /** Undismisses an alert summary
     * 
     * @param alertSummary The alert summary to undismiss.
     */
    public void undismissAlertSummary(AlertSummary alertSummary) {
    	
    	try {
    		Session session = sessionFactory.getCurrentSession();
    		
    		alertSummary.setDismissed(false);
    		alertSummary.setDismissedDate(null);
    		alertSummary.setDismissedUser(null);
    		alertSummary.setDismissedReason(null);
    		alertSummary.setDisplayDismissedUserName(null);
    		session.saveOrUpdate(alertSummary);
    	}
    	catch (Exception e) {
    		log.error(e);
    	}
    }
    
    /** Undismiss several alert summaries
     * 
     * @param alertSummaries The alert summaries to undismiss.
     */
    public void undismissAlertSummaries(List<AlertSummary> alertSummaries) {
    	
		// For each alert summary
		for (AlertSummary alertSummary : alertSummaries) {
			undismissAlertSummary(alertSummary);
		}
    }
    
    /**
     * Get an alert summary by id
     */
    protected AlertSummary findAlertSummaryById(int id) {
    	try {
	        Query query = sessionFactory.getCurrentSession().createQuery(
	        "from AlertSummary where id = :id")
	        .setInteger("id", id);
	
	        return (AlertSummary) query.uniqueResult();
    	}
    	catch (Exception e) {
    		log.error(e);
    		return null;
    	}
    }
    
    /**
     * Find an alert type by id
     * @param id
     * @return The AlertType for the specified id.
     */
    public AlertType findAlertTypeById(int id) {
        HQLQueryBuilder builder = new HQLQueryBuilder("AlertType");
        builder.add("id", "=", id);
        Query query = builder.getQuery(sessionFactory.getCurrentSession());
        return (AlertType) query.uniqueResult();
    }

    /**
     * Return all review status types
     * @return The review statuses as a ArrayList<Pair<Integer,String>>.
     */
    @SuppressWarnings("unchecked")
	public ArrayList<Pair<Integer, String>> getAlertTypes() {
    	ArrayList<Pair<Integer, String>> result = new ArrayList<Pair<Integer, String>>();
    	
        HQLQueryBuilder builder = new HQLQueryBuilder("AlertType");
        Query query = builder.getQuery(sessionFactory.getCurrentSession());
    	List<AlertType> rows = (List<AlertType>) query.list();
    	for (AlertType row : rows) {
    		result.add(new Pair<Integer, String>(new Integer(row.getId()), row.getAlertType()));
    	}
    	return result;
    }

    /**
     * Return the constructed name of the person with precedence for the preferred name,
     * otherwise, the first name in the set returned by OpenMRS.
     * 
     * @param user
     * @return The constructed name (given name and family name).
     */
    private String getName(User user) {
    	
    	if (user == null) {
    		// No user, no name
    		return null;
    	}
    	
    	if (user.getGivenName() != null) {
    		// Return the preferred person name
    		return user.getGivenName() + " " + user.getFamilyName();
    	}
    	else {
    		// Return the first person name in the set returned by OpenMRS
    		Set<PersonName> names = user.getNames();
    		if (names != null && names.size() >= 1) {
    			for (PersonName pname : names) {
    				return pname.getGivenName() + " " + pname.getFamilyName();
    			}
    		}
    	}
    	
    	// A nameless person!
    	return null;
    }
}

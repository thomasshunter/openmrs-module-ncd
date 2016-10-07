package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.ManualReviewStatusType;
import org.openmrs.module.ncd.database.dao.IReviewStatusDAO;
import org.openmrs.module.ncd.utilities.Pair;

public class ReviewStatusDAO implements IReviewStatusDAO {

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
     * Find a review status type by id
     * @param id
     * @return The ManualReviewStatusType for the specified id.
     */
	public ManualReviewStatusType findReviewStatusTypeById(int id) {
        HQLQueryBuilder builder = new HQLQueryBuilder("ManualReviewStatusType");
        builder.add("id", "=", id);
        Query query = builder.getQuery(sessionFactory.getCurrentSession());
        return (ManualReviewStatusType) query.uniqueResult();
	}

    /**
     * Return all review status types
     * @return The review statuses as a ArrayList<Pair<Integer,String>>.
     */
    @SuppressWarnings("unchecked")
	public ArrayList<Pair<Integer, String>> getReviewStatusTypes() {
    	ArrayList<Pair<Integer, String>> result = new ArrayList<Pair<Integer, String>>();
    	
        HQLQueryBuilder builder = new HQLQueryBuilder("ManualReviewStatusType");
        Query query = builder.getQuery(sessionFactory.getCurrentSession());
    	List<ManualReviewStatusType> rows = (List<ManualReviewStatusType>) query.list();
    	for (ManualReviewStatusType row : rows) {
    		result.add(new Pair<Integer, String>(new Integer(row.getId()), row.getReviewStatus()));
    	}
    	return result;
    }
}

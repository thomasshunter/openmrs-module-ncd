/**
 * 
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.NlpDiscreteTerm;
import org.openmrs.module.ncd.database.dao.INlpDiscreteTermDAO;

/**
 * @author jlbrown
 *
 */
public class NlpDiscreteTermDAO implements INlpDiscreteTermDAO {	

	/** Debugging log */
    private static Log log = LogFactory.getLog(ConditionDAO.class);
	
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
	
	/* (non-Javadoc)
	 * @see org.openmrs.module.ncd.database.dao.IDiscreteTermDAO#getAllNegativeTerms()
	 */
	@SuppressWarnings("unchecked")
	public List<NlpDiscreteTerm> getNlpDiscreteTermsByNegative(boolean isNegative) {
		Session dbSession = sessionFactory.getCurrentSession();
		Query dbQuery = dbSession.createQuery("from NlpDiscreteTerm where negative=:neg");
		dbQuery.setBoolean("neg", isNegative);
		return (List<NlpDiscreteTerm>)dbQuery.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<NlpDiscreteTerm> getAllNlpDiscreteTerms() {
		Session dbSession = sessionFactory.getCurrentSession();
		Query dbQuery = dbSession.createQuery("from NlpDiscreteTerm order by term");
		return (List<NlpDiscreteTerm>)dbQuery.list();
	}
	
	public void saveNlpDiscreteTerm(NlpDiscreteTerm term) {
		Session dbSession = sessionFactory.getCurrentSession();
		if (term.getId() == null) {

            log.debug("save a new discrete term");
            dbSession.save(term);
        }
        else {

            log.debug("update an existing discrete term");            
            dbSession.merge(term);
        }
	}
	
	public void deleteNlpDiscreteTerm(NlpDiscreteTerm term) {
		sessionFactory.getCurrentSession().delete(term);		
	}

	
}

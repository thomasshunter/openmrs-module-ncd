package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.County;
import org.openmrs.module.ncd.database.dao.ICountyDAO;

public class CountyDAO implements ICountyDAO {
	private static Log logger = LogFactory.getLog(CountyDAO.class);
	
    /** Hibernate session factory */
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
     * @see org.openmrs.module.ncd.database.dao.ICountyDAO#findCountyByName(java.lang.String)
     */
    public County findCountyByName(String name) {
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from County where county = :name")
                .setString("name", name);
        County result = null;
        
        try {
        	result = (County)query.uniqueResult();
        } catch (NonUniqueResultException e) {
        	// log this result and then return null.  
        	logger.warn("The county, " + name + ", returned more than one result.");
        }
        return result;
    }
    
    /**
     * @see org.openmrs.module.ncd.database.dao.ICountyDAO#findCountyByNameAndState(java.lang.String,java.lang.String)
     */
    public County findCountyByNameAndState(String name, String state) {
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from County where county = :name and state = :state")
                .setString("name", name)
                .setString("state", state);
        
        return (County)query.uniqueResult();
    }
    
    /**
     * @see org.openmrs.module.ncd.database.dao.ICountyDAO#listCounties()
     */
    @SuppressWarnings("unchecked")
    public List<County> listCounties() {
        Query query = sessionFactory.getCurrentSession().createQuery("from County");
        return (List<County>)query.list();
    }
}

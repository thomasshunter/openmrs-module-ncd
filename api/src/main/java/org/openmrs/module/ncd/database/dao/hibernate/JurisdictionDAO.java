package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.Jurisdiction;
import org.openmrs.module.ncd.database.dao.IJurisdictionDAO;

public class JurisdictionDAO implements IJurisdictionDAO {
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
     * @see org.openmrs.module.ncd.database.dao.IJurisdictionDAO#findJurisdictionByName(java.lang.String)
     */
    public Jurisdiction findJurisdictionByName(String name) {
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from Jurisdiction where jurisdiction = :name")
                .setString("name", name);
        
        return (Jurisdiction)query.uniqueResult();
    }
    
    /**
     * @see org.openmrs.module.ncd.database.dao.IJurisdictionDAO#listJurisdictions()
     */
    @SuppressWarnings("unchecked")
    public List<Jurisdiction> listJurisdictions() {
        Query query = sessionFactory.getCurrentSession().createQuery("from Jurisdiction");
        return (List<Jurisdiction>)query.list();
    }
}

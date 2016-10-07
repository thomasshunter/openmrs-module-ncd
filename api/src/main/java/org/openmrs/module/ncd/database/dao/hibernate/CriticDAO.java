/**
 * Copyright 2009 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.CriticDef;
import org.openmrs.module.ncd.database.dao.ICriticDAO;

public class CriticDAO implements ICriticDAO {

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
     * @see org.openmrs.module.ncd.database.dao.ICriticDAO#getCriticById(java.lang.Long)
     */
    public CriticDef findCriticById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("from CriticDef where id=:id").setLong("id", id);
        return (CriticDef)query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<CriticDef> getAllCritics() {
        
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("from CriticDef order by name");
        return (List<CriticDef>) query.list();
    }
}

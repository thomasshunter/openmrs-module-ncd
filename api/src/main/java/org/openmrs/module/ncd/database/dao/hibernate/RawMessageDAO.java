/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.io.Serializable;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.database.dao.IRawMessageDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class RawMessageDAO implements IRawMessageDAO
{

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
     * @see org.openmrs.module.ncd.database.dao.IRawMessageDAO#saveRawHL7(org.openmrs.module.ncd.database.RawMessage)
     */
    public Serializable saveRawMessage(RawMessage rawHL7)
    {
        return sessionFactory.getCurrentSession().save(rawHL7);
    }
    
    public void updateRawMessage(RawMessage rawHL7)
    {
    	sessionFactory.getCurrentSession().update(rawHL7);
    }
    
    /**
     * Finds the raw HL7 database row based on the raw hl7 row id.
     * @param id The raw HL7 row id which is being looked up.
     * @return The HL7 database row that contains the
     * specified message.
     */
    @Transactional(readOnly=true)
    public RawMessage findRawMessageById(Long id) {

        Query query = sessionFactory.getCurrentSession().createQuery("from RawMessage where id=:id");
        query.setLong("id", id);
        return (RawMessage) query.uniqueResult();
    }
}

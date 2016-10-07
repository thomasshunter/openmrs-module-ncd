/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.NlpCriticConcept;
import org.openmrs.module.ncd.database.dao.INlpCriticConceptDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 */
public class NlpCriticConceptDAO implements INlpCriticConceptDAO {

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
    @Transactional
    public List<NlpCriticConcept> list() {
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from NlpCriticConcept");
        return (List<NlpCriticConcept>) query.list();
    }

    public void save(NlpCriticConcept concept) {
        sessionFactory.getCurrentSession().merge(concept);
    }
    
    public void delete(NlpCriticConcept concept) {
        sessionFactory.getCurrentSession().delete(concept);
    }
}

/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.NlpCriticContext;
import org.openmrs.module.ncd.database.NlpCriticContextGroup;
import org.openmrs.module.ncd.database.NlpCriticContextType;
import org.openmrs.module.ncd.database.dao.INlpCriticContextDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO to get the NLP contexts
 */
public class NlpCriticContextDAO implements INlpCriticContextDAO {

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
     * @see org.openmrs.module.ncd.nlp.INlpCriticContextDAO.dao.IContextDAO#findContextByType()
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public List<NlpCriticContext> findContextByType(NlpCriticContextType type) {

        Query query = sessionFactory.getCurrentSession()
        	.createQuery("from NlpCriticContext where contextTypeId=:ctxType")
        	.setParameter("ctxType", type);
        return (List<NlpCriticContext>)query.list();
    }

    /**
     * @see org.openmrs.module.ncd.nlp.INlpCriticContextDAO.dao.IContextDAO#findContextByTypeAndGroup()
     */
    @SuppressWarnings("unchecked")
	@Transactional
    public List<NlpCriticContext> findContextByTypeAndGroup(NlpCriticContextType type, String group) {
    	Query query = sessionFactory.getCurrentSession()
    		.createQuery("from NlpCriticContext where contextTypeId=:ctxType and contextGroup=:ctxGroup")
    		.setParameter("ctxType", type)
    		.setParameter("ctxGroup", group);
    	return (List<NlpCriticContext>)query.list();
    }
    
    @SuppressWarnings("unchecked")
    @Transactional
	public List<NlpCriticContextType> listContextTypes() {
    	Query query = sessionFactory.getCurrentSession().createQuery("from NlpCriticContextType order by typeName");
    	return (List<NlpCriticContextType>)query.list();
    }
    
    @Transactional
    public NlpCriticContextType findContextTypeByName(String name) {
    	Query query = sessionFactory.getCurrentSession()
    		.createQuery("from NlpCriticContextType where typeName=:typeName")
    		.setParameter("typeName", name);
    	return (NlpCriticContextType)query.uniqueResult();
    }
    
    @Transactional
    public void saveNlpCriticContextType(NlpCriticContextType type) {
        
        sessionFactory.getCurrentSession().merge(type);
    }

    @Transactional
    public void deleteNlpCriticContextType(NlpCriticContextType type) {
        
        sessionFactory.getCurrentSession().delete(type);
    }
    
    @Transactional
    public void saveNlpCriticContext(NlpCriticContext context) {
        
        sessionFactory.getCurrentSession().save(context);
    }

    @Transactional
    public void deleteNlpCriticContext(NlpCriticContext context) {
        
        sessionFactory.getCurrentSession().delete(context);
    }
    
    @SuppressWarnings("unchecked")
    @Transactional
    public List<NlpCriticContextGroup> listContextGroups() {
        
        Query query = sessionFactory.getCurrentSession().createQuery("from NlpCriticContextGroup order by displayText");
        return (List<NlpCriticContextGroup>) query.list();
    }
}

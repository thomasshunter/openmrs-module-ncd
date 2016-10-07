/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.Institution;
import org.openmrs.module.ncd.database.dao.IInstitutionDAO;

/**
 *
 */
public class InstitutionDAO implements IInstitutionDAO {

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
     * @see org.openmrs.module.ncd.database.dao.IInstitutionDAO#findInstitutionByName(java.lang.String)
     */
    public Institution findInstitutionByName(String name) {
        Query institutionQuery = 
        	sessionFactory.getCurrentSession().createQuery("from Institution where name = :name and retired = false")
            .setParameter("name", name);
        return (Institution)institutionQuery.uniqueResult();
    }

    /** Get a list of all defined institutions, in increasing
     * lexicographic order.
     * @return a List<String> containing all institutions.
     */
    @SuppressWarnings("unchecked")
    public List<String> getAllInstitutionNames() {                        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "select name from Institution where retired = false order by name");

        return (List<String>) query.list();
    }
    
    @SuppressWarnings("unchecked")
	public List<Institution> getAllInstitutions() {
    	
        Query query = sessionFactory.getCurrentSession().createQuery(
        	"from Institution order by name");

        return (List<Institution>) query.list();
    }
    
    @SuppressWarnings("unchecked")
	public List<Institution> getAllActiveInstitutions() {
    	
        Query query = sessionFactory.getCurrentSession().createQuery(
        	"from Institution where retired = false order by name");

        return (List<Institution>) query.list();
    }
    
    public Institution getInstitution(long id) {
    	
        Query query = sessionFactory.getCurrentSession().createQuery(
        	"from Institution where id=:id")
        	.setParameter("id", new Long(id));

        return (Institution) query.uniqueResult();
    }
    
    public void saveInstitution(Institution institution) {
    	
        if (institution.getId() == null) {

            sessionFactory.getCurrentSession().save(institution);
        }
        else {
            
            sessionFactory.getCurrentSession().merge(institution);
        }
    }

	public void deleteInstitution(Institution institution) {
		
    	sessionFactory.getCurrentSession().delete(institution);
	}
}

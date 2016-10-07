package org.openmrs.module.ncd.database.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.ExportedResult;
import org.openmrs.module.ncd.database.dao.IExportedResultDAO;

public class ExportedResultDAO implements IExportedResultDAO {

    /** Hibernate session factory */
    private SessionFactory sessionFactory;

    // **********************
    // Spring-only methods *
    // **********************

    /**
     * Set session factory. Spring calls this based on the
     * moduleApplicationContext.xml
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // ***************************
    // Public interface methods *
    // ***************************

    /**
     * @see org.openmrs.module.ncd.database.dao.hibernate.IExportedResultDAO#addExportedResult(org.openmrs.module.ncd.database.ExportedResult)
     */
	public void addExportedResult(ExportedResult exportedResult) {

		Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(exportedResult);
	}
}

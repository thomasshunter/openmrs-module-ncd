package org.openmrs.module.ncd.database.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.ExportRecipient;
import org.openmrs.module.ncd.database.dao.IExportRecipientDAO;

public class ExportRecipientDAO implements IExportRecipientDAO {

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
     * @see org.openmrs.module.ncd.database.dao.hibernate.IExportRecipientDAO#addExportRecipient(org.openmrs.module.ncd.database.ExportRecipient)
     */
	public void addExportRecipient(ExportRecipient exportRecipient) {

		Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(exportRecipient);
	}
}

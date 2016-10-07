/**
 * Copyright 2009 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.County;
import org.openmrs.module.ncd.database.Jurisdiction;
import org.openmrs.module.ncd.database.ZipCode;
import org.openmrs.module.ncd.database.dao.IZipcodeDAO;

public class ZipcodeDAO implements IZipcodeDAO {
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
     * @see org.openmrs.module.ncd.database.dao.IZipcodeDAO#findCountyByZipcode(java.lang.String)
     */
    public County findCountyByZipcode(String zipcode) {
        Query query = sessionFactory.getCurrentSession()
            .createQuery("from ZipCode where zipcode = :zip")
            .setString("zip", zipcode);
        
        ZipCode zipcodeObj = (ZipCode)query.uniqueResult();
        County retVal = (zipcodeObj == null ? null : zipcodeObj.getCounty()); 
        return retVal;
    }
    
    /**
     * @see org.openmrs.module.ncd.database.dao.IZipcodeDAO#findJurisdictionByZipcode(java.lang.String)
     */
    public Jurisdiction findJurisdictionByZipcode(String zipcode) {
        Query query = sessionFactory.getCurrentSession()
        .createQuery("from ZipCode where zipcode = :zip")
        .setString("zip", zipcode);
    
        ZipCode zipcodeObj = (ZipCode)query.uniqueResult();
        Jurisdiction retVal = (zipcodeObj == null ? null : zipcodeObj.getJurisdiction());
        return retVal;
    }
}

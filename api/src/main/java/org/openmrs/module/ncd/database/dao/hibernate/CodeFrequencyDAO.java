/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.Date;
import java.util.EventObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.NCDServer;
import org.openmrs.module.ncd.cache.CodeFrequencyCache;
import org.openmrs.module.ncd.database.CodeFrequency;
import org.openmrs.module.ncd.database.dao.ICodeFrequencyDAO;
import org.openmrs.module.ncd.events.IStopEventListener;
import org.openmrs.module.ncd.storage.CodeFrequencyStorageException;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.springframework.transaction.annotation.Transactional;

public class CodeFrequencyDAO implements ICodeFrequencyDAO, IStopEventListener {

    private static Log logger = LogFactory.getLog(CodeFrequencyDAO.class);

    /** Hibernate session factory, set by spring. */
    private SessionFactory sessionFactory;

    private static CodeFrequencyCache codeFrequencyMap = null;

    public CodeFrequencyDAO() {
    	NCDServer.addStopEventListener(this);
        try {
        	initCodeFrequencyMap();
        } catch (Throwable e) {
        	logger.warn("CodeFrequencyDAO was unable to initialize code frequency map.");
        }
    }

    /**
     * Set session factory. Spring calls this based on the
     * moduleApplicationContext.xml
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmrs.module.ncd.database.dao.ICodeFrequencyDAO#incrementLoincFrequency(java.util.Date,
     *      org.openmrs.module.ncd.database.Institution,
     *      org.openmrs.module.ncd.database.CodeCondition)
     */
    public synchronized void incrementCodeFrequency(Date date, 
            String application, String facility, String location,
            String code, String codeSystem, 
            String patientZipCode, String instituteZipCode, 
            String doctorZipCode)
    throws CodeFrequencyStorageException {
        try {
            initCodeFrequencyMap();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        logger.debug("application=" + application);
        logger.debug("facility=" + facility);
        logger.debug("location=" + location);
        
        codeFrequencyMap.incrementCodeFrequency(code, codeSystem, application, 
                facility, location, date, patientZipCode, instituteZipCode, 
                doctorZipCode);

        if (codeFrequencyMap.getSize() >= NCDUtilities.getMaxLoincFrequencyCacheSize()) {
            saveCodeFrequencyMap();
        }
    }

    public synchronized CodeFrequency findCodeFrequency(Date date,
            String application, String facility, String location, String code, 
            String codeSystem, String patitentZipCode, 
            String instituteZipCode, String doctorZipCode) {
        try {
            initCodeFrequencyMap();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return codeFrequencyMap.findCodeFrequency(code, codeSystem,
                application, facility, location, date, patitentZipCode, 
                instituteZipCode, doctorZipCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized void saveCodeFrequencyMap() throws CodeFrequencyStorageException {
        
        initCodeFrequencyMap();

        logger.info("begin saving.");
        
        Session session = sessionFactory.getCurrentSession();
        
        // TODO: Is there a more efficient way to do this?
        for (CodeFrequency freq : codeFrequencyMap.getMapValues()) {
            session.merge(freq);
        }
        codeFrequencyMap.clearMap();
        
        logger.info("end saving.");
    }

    public void handleStopEvent(EventObject eventObj)
    {
        try
        {
            saveCodeFrequencyMap();
        }
        catch (CodeFrequencyStorageException e)
        {
            // we're stopping so there's not much we can do to recover
        }
    }

    private synchronized void initCodeFrequencyMap()
            throws CodeFrequencyStorageException {
        if (codeFrequencyMap == null) {
            codeFrequencyMap = new CodeFrequencyCache();
        }
    }
}

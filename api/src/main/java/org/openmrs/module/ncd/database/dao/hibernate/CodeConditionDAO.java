/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.cache.CodeConditionCache;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.dao.ICodeConditionDAO;
import org.openmrs.module.ncd.utilities.IUpdatableData;
import org.springframework.transaction.annotation.Transactional;

public class CodeConditionDAO implements ICodeConditionDAO, IUpdatableData {

    /** Debugging log */
    private static Log log = LogFactory.getLog(CodeConditionDAO.class);
	
    /** Hibernate session factory */
    private SessionFactory sessionFactory;

    /** Cached map from loinc code to ncd_code row */
    private static CodeConditionCache codeConditionCache = null;

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
     * @see org.openmrs.module.ncd.database.dao.ICodeConditionDAO#findByLoincCode(java.lang.String)
     */
    public List<CodeCondition> findByCodeAndSystem(String code, String system) {
        initCodeMap();

        return codeConditionCache.findByCodeAndSystem(code, system);
    }
    
    public CodeCondition findByCodeAndCondition(String code, String system, String condition) {
        initCodeMap();
        
        return codeConditionCache.findByCodeAndCondition(code, system, condition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmrs.module.ncd.utilities.IUpdatableData#updateData()
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public synchronized void updateData() {
    	
    	Long timerStart = System.currentTimeMillis();

    	// Update the cache of codes from the database
        codeConditionCache = new CodeConditionCache();
        
        // Preload the Hibernate cache for Code and Condition rows by fetching
        // them all right now. This hopefully will keep the CodeCondition
        // query below from having to load them individually.
        preloadCodes();
        preloadConditions();
        
        Long timerValue = System.currentTimeMillis() - timerStart;
        log.info("Code and Condition cache preload took " + timerValue + " ms.");
        
        // Note, we want to ignore code-condition mappings that refer to
        // retired codes or conditions.
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from CodeCondition where code.retired=false and condition.retired=false");

        List<CodeCondition> codeList = (List<CodeCondition>) query.list();

        codeConditionCache.addCollection(codeList);
        
        timerValue = System.currentTimeMillis() - timerStart;
        log.info("Total CodeCondition cache load took " + timerValue + " ms.");
    }

    private void preloadCodes() {
    	
        Query query = sessionFactory.getCurrentSession().createQuery("from Code");
        query.list();
    }
    
    private void preloadConditions() {
    	
        Query query = sessionFactory.getCurrentSession().createQuery("from Condition");
        query.list();
    }
    
    private synchronized void initCodeMap() {
        if (codeConditionCache == null) {
            updateData();
        }
    }
}

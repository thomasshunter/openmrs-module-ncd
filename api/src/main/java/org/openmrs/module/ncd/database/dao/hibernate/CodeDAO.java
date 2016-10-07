package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.CodeSystem;
import org.openmrs.module.ncd.database.CodeType;
import org.openmrs.module.ncd.database.dao.ICodeDAO;
import org.openmrs.module.ncd.database.filter.SearchFilterCodes;
import org.openmrs.module.ncd.database.filter.SearchResult;

public class CodeDAO implements ICodeDAO {

    /** Debugging log */
    private static Log log = LogFactory.getLog(CodeDAO.class);

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
    public List<Code> findCodes(String typeName, String systemName) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from Code" +
            " where codeType.name = :typeName" +
            " and codeSystem.name = :systemName")
            .setParameter("typeName", typeName)
            .setParameter("systemName", systemName);

        return (List<Code>) query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<Code> findCodesExcludeRetired(String typeName, String systemName) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from Code" +
            " where codeType.name = :typeName" +
            " and codeSystem.name = :systemName" +
            " and retired = 0")
            .setString("typeName", typeName)
            .setString("systemName", systemName);

        return (List<Code>) query.list();
    }

    public Code getCode(String systemName, String codeValue) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from Code" +
            " where codeSystem.name = :systemName" +
            " and code = :codeValue")
            .setParameter("systemName", systemName)
            .setParameter("codeValue", codeValue);

        return (Code) query.uniqueResult();
    }

    public Code getCode(CodeSystem codeSystem, String codeValue) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from Code" +
            " where codeSystem.name = :system" +
            " and code = :codeValue")
            .setParameter("system", codeSystem.getName())
            .setParameter("codeValue", codeValue);

        return (Code) query.uniqueResult();
    }
    
    public CodeType getCodeType(String name) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from CodeType" +
            " where name = :name")
            .setParameter("name", name);

        return (CodeType) query.uniqueResult();
    }
    
    public CodeSystem getCodeSystem(String name) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from CodeSystem" +
            " where name = :name")
            .setParameter("name", name);

        return (CodeSystem) query.uniqueResult();
    }
    
    @SuppressWarnings("unchecked")
    public List<CodeType> getAllCodeTypes() {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from CodeType order by name");

        return (List<CodeType>) query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<CodeType> getAllCodeTypesExcludeRetired() {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from CodeType where retired = 0 order by name");

        return (List<CodeType>) query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<CodeSystem> getAllCodeSystems() {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from CodeSystem order by name");

        return (List<CodeSystem>) query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<CodeSystem> getAllCodeSystemsExcludeRetired() {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from CodeSystem where retired=0 order by name");

        return (List<CodeSystem>) query.list();
    }

    public void saveCode(Code code) {
        
        if (code.getId() == null) {

            sessionFactory.getCurrentSession().save(code);
        }
        else {
            
            sessionFactory.getCurrentSession().merge(code);
        }
    }
    
    public void saveCodeSystem(CodeSystem codeSystem) {
        
        if (codeSystem.getId() == null) {

            sessionFactory.getCurrentSession().save(codeSystem);
        }
        else {
            
            sessionFactory.getCurrentSession().merge(codeSystem);
        }
    }

    public void saveCodeType(CodeType codeType) {
        
        if (codeType.getId() == null) {

            sessionFactory.getCurrentSession().save(codeType);
        }
        else {
            
            sessionFactory.getCurrentSession().merge(codeType);
        }
    }

    public Code getCode(Long id) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from Code" +
                " where id = :id")
                .setParameter("id", id);

            return (Code) query.uniqueResult();
    }
    
    public CodeSystem getCodeSystem(Long id) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from CodeSystem" +
                " where id = :id")
                .setParameter("id", id);

            return (CodeSystem) query.uniqueResult();
    }
    
    public CodeType getCodeType(Long id) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from CodeType" +
                " where id = :id")
                .setParameter("id", id);

            return (CodeType) query.uniqueResult();
    }

    /** Find codes which match a filter
     * @param filter The search criteria.
     * @return A list of codes that match the search criteria.
     */
    @SuppressWarnings("unchecked")
    public SearchResult<Code> findCodes(SearchFilterCodes filter) {

        Session dbSession = sessionFactory.getCurrentSession();

        SearchResult<Code> results = new SearchResult<Code>();
        results.setSuccessful(false);
        
        try {

            HQLQueryBuilder builder = new HQLQueryBuilder("Code c");
            builder.add("c.code", filter.getCode());
            builder.add("c.displayText", filter.getDisplayText());
            builder.add("c.codeSystem.id", filter.getCodeSystem());
            builder.add("c.codeType.id", filter.getCodeType());
            
            if (filter.getSortFieldName() != null) {
                
                builder.setSort(filter.getSortFieldName());
                builder.setSortAscending(filter.isSortAscending());
            }

            Query query = builder.getQuery(dbSession)
                .setMaxResults(filter.getMaxRows() + 1);
            
            List<Code> rows = (List<Code>) query.list();
            
            results.setSuccessful(true);
            results.setThrowable(null);
            results.setLimited(rows.size() > filter.getMaxRows());
            results.setRowCount(rows.size());   // bogus
            
            if (results.isLimited()) {
                
                results.setResultRows(rows.subList(0, filter.getMaxRows()));

                // Rerun the query without the row limit, only
                // counting rows, to correctly set rowCount.

                Query query2 = builder.getCountQuery(dbSession);
                results.setRowCount((Long) query2.uniqueResult());
            }
            else {

                results.setResultRows(rows);
            }
        }
        catch (Exception e) {

            results.setSuccessful(false);
            results.setThrowable(e);
            results.setLimited(false);
            results.setRowCount(0);
            results.setResultRows(new ArrayList<Code>());
            
            log.error("exception: " + e.getMessage(), e);
        }

        return results;
    }
}

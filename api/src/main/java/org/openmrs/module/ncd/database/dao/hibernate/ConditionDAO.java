/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.ConditionGroup;
import org.openmrs.module.ncd.database.dao.IConditionDAO;

/**
 *
 */
public class ConditionDAO implements IConditionDAO {

    /** Debugging log */
    private static Log log = LogFactory.getLog(ConditionDAO.class);

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
     * @see org.openmrs.module.ncd.database.dao.IConditionDAO#findConditionByName(java.lang.String)
     */
    public Condition findConditionByName(String name) {
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from Condition where displayText = :name")
                .setString("name", name);
        
        return (Condition)query.uniqueResult();
    }
    
    public Condition findConditionById(Long id) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
        "from Condition c where id = :id")
            .setLong("id", id);

        return (Condition) query.uniqueResult();
    }
    
    public ConditionGroup findConditionGroupByName(String name) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
        "from ConditionGroup where displayText = :name")
            .setString("name", name);

        return (ConditionGroup) query.uniqueResult();
    }
    
    public ConditionGroup findConditionGroupById(Integer id) {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
        "from ConditionGroup where id = :id")
            .setLong("id", id);

        return (ConditionGroup) query.uniqueResult();
    }

    /** Get a list of all defined condition names, in increasing
     * lexicographic order.  Voided condition names are not returned.
     * @return a List<String> containing all default condition names.
     */
    @SuppressWarnings("unchecked")
    public List<String> getAllConditionNames() {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "select displayText from Condition order by displayText");

        return (List<String>) query.list();
    }
    
    @SuppressWarnings("unchecked")
	public List<String> getAllConditionNamesExcludeRetired() {
    	Query query = sessionFactory.getCurrentSession().createQuery(
        	"select displayText from Condition where retired = 0 order by displayText");

    	return (List<String>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Condition> getAllConditions() {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from Condition order by displayText");

        return (List<Condition>) query.list();
    }
    
    @SuppressWarnings("unchecked")
	public List<Condition> getAllConditionsExcludeRetired() {
    	Query query = sessionFactory.getCurrentSession().createQuery(
        	"from Condition where retired = 0 order by displayText");

    	return (List<Condition>) query.list();
    }

    /**
     * Saves CodeConditions which have been added to the condition's
     * CodeCondition collection, and deletes those which have been
     * removed.
     * 
     * @param condition
     */
    @SuppressWarnings("unchecked")
    private void syncChildObjects(Condition condition) {
        
        log.debug("condition=" + condition);
        Session session = sessionFactory.getCurrentSession();

        // A list of the ids of code conditions related to this condition
        List<Long> relatedCodeConditionIds = new ArrayList<Long>();
        for (CodeCondition cc : condition.getCodeConditions()) {
            if (cc.getId() != null) {
                relatedCodeConditionIds.add(cc.getId());
            }
        }
        
        log.debug("relatedCodeConditionIds=" + relatedCodeConditionIds);
        log.debug("#relatedCodeConditionIds=" + relatedCodeConditionIds.size());
        
        // Get the list of all CodeConditions for this condition that
        // have been removed from its CodeConditions set.
        List<CodeCondition> removedCodeConditions=null;
        if (relatedCodeConditionIds.size()==0) {
        	
        	// Special case: sometimes there are no related code conditions.
        	// This can happen when the user deleted all of them, or if the
        	// condition doesn't currently have any code conditions.  For this
        	// case, we cannot do the query below which would try to execute:
        	//		where id NOT IN ()
        	// and HQL doesn't allow the empty list in the query.  Instead,
        	// we return the list of all code conditions currently associated
        	// with the condition in the database; all of them need to be
        	// removed.
	        removedCodeConditions =
	            (List<CodeCondition>)
	        session.createQuery("from CodeCondition where condition=:cond")
	            .setParameter("cond", condition)
	            .list();
        } else {
	        removedCodeConditions =
	            (List<CodeCondition>)
	        session.createQuery("from CodeCondition where condition=:cond and id NOT IN (:ids)")
	            .setParameter("cond", condition)
	            .setParameterList("ids", relatedCodeConditionIds)
	            .list();
        }
        
        // Delete DecidedResults related to each removed CodeCondition,
        // then delete the removed CodeCondition
        for (CodeCondition cc : removedCodeConditions) {
            
            removeMatchingDecidedResults(cc);
            session.delete(cc);
        }

        log.debug("" + removedCodeConditions.size() + " code conditions deleted.");

        // Add the new CodeConditions. This cannot be trivially combined
        // with the code above because we don't know the ids that will
        // be assigned to the new rows.
        for (CodeCondition cc : condition.getCodeConditions()) {
            if (cc.getId() == null) {
                session.save(cc);
            }
        }
    }

    /**
     * Void all decided results with condition name and code that
     * match the specified CodeCondition.
     */
    private void removeMatchingDecidedResults(CodeCondition cc) {
        
        log.debug("code condition=" + cc);
        
        Session session = sessionFactory.getCurrentSession();

        int rowCount = 
        	session.createQuery("delete from DecidedResult where conditionName=:name and loincCode=:code)")
            .setParameter("name", cc.getCondition().getDisplayText())
            .setParameter("code", cc.getCode().getCode())
            .executeUpdate();
        
        log.debug("" + rowCount + " decided results voided.");
    }
    
    public void saveCondition(Condition condition) {

        log.debug("condition=" + condition);
        
        if (condition.getId() == null) {

            log.debug("save a new condition");
            sessionFactory.getCurrentSession().save(condition);
        }
        else {

            log.debug("update an existing condition");
            syncChildObjects(condition);
            sessionFactory.getCurrentSession().merge(condition);
        }
    }
    
    public void deleteCondition(Condition condition) {
        
        Session session = sessionFactory.getCurrentSession();
        for (CodeCondition cc : condition.getCodeConditions()) {
            
            session.delete(cc);
        }
        session.delete(condition);
    }
    
    @SuppressWarnings("unchecked")
    public List<ConditionGroup> getAllConditionGroups() {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from ConditionGroup order by displayText");

        return (List<ConditionGroup>) query.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<ConditionGroup> getAllConditionGroupsExcludeRetired() {
        
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from ConditionGroup where retired = 0 order by displayText");

        return (List<ConditionGroup>) query.list();
    }
    
    public void saveConditionGroup(ConditionGroup group) {
        
        if (group.getId() == null) {
            
            sessionFactory.getCurrentSession().save(group);
        }
        else {
            
            sessionFactory.getCurrentSession().merge(group);
        }
    }

    public void deleteConditionGroup(ConditionGroup group) {
        
        Session session = sessionFactory.getCurrentSession();
        for (Condition cond : group.getTblconditionnames()) {
            
            deleteCondition(cond);
        }

        session.delete(group);
    }
}

/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.ConditionGroup;

/**
 *
 */
public interface IConditionDAO {
    /**
     * 
     * Find the condition object using the name of the condition.
     * 
     * @param name The name of the condition
     * @return The condition as a Condition object.
     */
    public Condition findConditionByName(String name);
    public Condition findConditionById(Long id);
    public ConditionGroup findConditionGroupByName(String name);
    public ConditionGroup findConditionGroupById(Integer id);
    
    /** Get a list of all defined condition names, in increasing
     * lexicographic order.
     * @return a List<String> containing all default condition names.
     */
    public List<String> getAllConditionNames();
    public List<String> getAllConditionNamesExcludeRetired();
    
    public List<Condition> getAllConditions();
    public List<Condition> getAllConditionsExcludeRetired();
    public void saveCondition(Condition condition);
    public void deleteCondition(Condition condition);
    
    public List<ConditionGroup> getAllConditionGroups();
    public List<ConditionGroup> getAllConditionGroupsExcludeRetired();
    public void saveConditionGroup(ConditionGroup group);
    public void deleteConditionGroup(ConditionGroup group);
}

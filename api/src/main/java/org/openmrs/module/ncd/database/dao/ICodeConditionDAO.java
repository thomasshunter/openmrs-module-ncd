/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.CodeCondition;

public interface ICodeConditionDAO
{
    /**
     * Retrieves the database rows that coincide with the
     * specified code and code system.
     * @param code The code to retrieve.
     * @param system The code system of the code to retrieve.
     * @return A list of CodeCondition objects that contain
     * the information associated with the code.
     */
    public List<CodeCondition> findByCodeAndSystem(String code, String system);
    
    /**
     * 
     * Retrieves a first database row that matches the
     * specified code, system, and condition.
     * 
     * @param code The code to retrieve.
     * @param system The code system of the code to retrieve.
     * @param condition The condition to retrieve.
     * @return A CodeCondition object that matches the specified code, system, and condition.
     */
    public CodeCondition findByCodeAndCondition(String code, String system, String condition);
}

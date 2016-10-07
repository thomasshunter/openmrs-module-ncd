package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.County;

public interface ICountyDAO {

    /**
     * Find the county object using the name of the county.
     * 
     * @param name The name of the county
     * @return The county as a County object.
     */
    public County findCountyByName(String name);

    /**
     * Find the county object using the name of the county, and the state name.
     * 
     * @param name The name of the county
     * @param state The two letter state abbreviation (in upper case)
     * @return The county as a County object.
     */
    public County findCountyByNameAndState(String name, String state);

    public List<County> listCounties();

}
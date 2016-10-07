package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.Jurisdiction;

public interface IJurisdictionDAO {

    /**
     * Find the jurisdiction object using the name of the jurisdiction.
     * 
     * @param name The name of the jurisdiction
     * @return The jurisdiction as a Jurisdiction object.
     */
    public Jurisdiction findJurisdictionByName(String name);

    public List<Jurisdiction> listJurisdictions();

}
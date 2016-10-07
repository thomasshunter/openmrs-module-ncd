/**
 * Copyright 2009 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database;

import java.util.HashSet;
import java.util.Set;

public class Jurisdiction implements java.io.Serializable {

    private static final long serialVersionUID = 1934746649890318111L;
    /** The synthetic primary key */
    private Long id;
    /** The name of the jurisdiction */
    private String jurisdiction;
    /** The set of postal codes that define the jurisdiction */
    private Set<ZipCode> zipcodes = new HashSet<ZipCode>(0);
    
    public Jurisdiction() {
        
    }
    
    public Jurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }
    
    public Jurisdiction(Jurisdiction toClone) {
        if (toClone != null) {
            this.jurisdiction = toClone.getJurisdiction();
        }
    }
    
    public String toString() {
    
    	return jurisdiction;
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getJurisdiction() {
        return jurisdiction;
    }
    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public Set<ZipCode> getZipcodes() {
        return zipcodes;
    }

    public void setZipcodes(Set<ZipCode> zipcodes) {
        this.zipcodes = zipcodes;
    }
}

/**
 * Copyright 2009 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database;

import java.util.HashSet;
import java.util.Set;

/**
 * A county (or similar geopolitical area) in a state. Used to allow
 * reportable results to be filtered by the agency that should receive them.
 */
public class County implements java.io.Serializable {

    private static final long serialVersionUID = -6427134819132416783L;
    /** The synthetic primary key */
    private Long id;
    /** The name of the county */
    private String county;    
    /** The name of the state containing the county (for disambiguation) */
    private String state;
    /** The set of zip codes in the county */
    private Set<ZipCode> zipcodes = new HashSet<ZipCode>(0);
    
    public County() {
        
    }
    
    public County (String county) {
        this.county = county;
    }
    
    public County (String county, String state) {
        this.county = county;        
        this.state = state;
    }
        
    public County (County toClone) {
        if (toClone != null) {
            this.county = toClone.getCounty();        
            this.state = toClone.getState();
        }
    }
    
    public String toString() {

    	return county + "(" + state + ")";
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCounty() {
        return county;
    }
    
    public void setCounty(String county) {
        this.county = county;
    }        
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }

    public Set<ZipCode> getZipcodes() {
        return zipcodes;
    }

    public void setZipcodes(Set<ZipCode> zipcodes) {
        this.zipcodes = zipcodes;
    }
    
    
}

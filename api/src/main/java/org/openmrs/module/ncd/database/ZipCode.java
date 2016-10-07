/**
 * Copyright 2009 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database;

/**
 * A postal code and the county and health agency jurisdiction of which it
 * is a part.
 * 
 * NOTE: This assumes that postal codes never cross county or jurisdiction
 * boundaries, which is known to be untrue.
 */
public class ZipCode implements java.io.Serializable {
    
    private static final long serialVersionUID = 8640017314301789061L;
    
    /** The synthetic primary key */
    private Long id;
    /** The text of the postal code, such as 92592-4362 */
    private String zipcode;
    /** The county this zipcode is a part of */
    private County county;
    /** The health agency jurisdiction this zipcode is a part of, such as "ISDH" */
    private Jurisdiction jurisdiction;
    
    // Default constructor
    public ZipCode() {
        // This space left blank intentionally.
    }
    
    // Full constructor
    public ZipCode(String zipcode, County county, Jurisdiction jurisdiction) {
        this.zipcode = zipcode;        
        this.county = county;
        this.jurisdiction = jurisdiction;
    }
    
    // Clone constructor
    public ZipCode(ZipCode toClone) {
        if (toClone != null) {
            this.zipcode = toClone.getZipcode();        
            this.county = toClone.getCounty();
            this.jurisdiction = toClone.getJurisdiction();
        }
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getZipcode() {
        return zipcode;
    }
    
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
    
    public County getCounty() {
        return county;
    }
    
    public void setCounty(County county) {
        this.county = county;
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
    }
       
}

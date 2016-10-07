package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;

/**
 * An abstract Institution, i.e., an "owner" of HL7Producers, primarily
 * to record contact information to be used in case of problems.
 */
public class Institution implements java.io.Serializable {

    private static final long serialVersionUID = 3366180629957978725L;

    /** The synthetic primary key */
    private Long id;    
    /** The display name of the Institution */
    private String name;
    /** A more verbose description of this Institution */
    private String description;
    /** The first line of the Institution's address */
    private String addressline1;
    /** The second line of the Institution's address */
    private String addressline2;
    /** The city of the Institution's address */
    private String city;
    /** The state of the Institution's address */
    private String state;
    /** The postal code of the Institution's address */
    private String zip;
    /** The contact phone number for the Institution */
    private String phone;
    /** The Institution's web site. */
    private String www;
    /** True if and only if this Institution has been retired - i.e., existed
     * once in the past, but should not be used for processing new messages. */
    private boolean retired;
    /** The set of HL7Producers owned by this Institution */
    private Set<HL7Producer> producers = new HashSet<HL7Producer>(0);
    /** The set of reportable results originating from sources owned by
     * this Institution */
    private Set<ReportableResult> reportableResults = new HashSet<ReportableResult>(
            0);

    public Institution() {
    }

    public Institution(String name) {
        this.name = name;
    }    

    public Institution(Institution src) {
    	
    	this.id = src.id;
    	this.name = src.name;    	
    	this.description = src.description;
    	this.addressline1 = src.addressline1;
    	this.addressline2 = src.addressline2;
    	this.city = src.city;
    	this.state = src.state;
    	this.zip = src.zip;
    	this.phone = src.phone;
    	this.www = src.www;
    	this.retired = src.retired;
    	this.producers = new HashSet<HL7Producer>();
    	this.producers.addAll(src.producers);
    	this.reportableResults = new HashSet<ReportableResult>();
    	this.reportableResults.addAll(src.reportableResults);
    }
    
    public String toString() {
    	
    	return "Institution(" +
    				"id=" + id +
    				", name=" + name +    				
    				", description=" + description +
    				", addressline1=" + addressline1 +
    				", addressline2=" + addressline2 +
    				", city=" + city +
    				", state=" + state +
    				", zip=" + zip +
    				", phone=" + phone +
    				", www=" + www +
    				", retired=" + retired +
    		   ")";
    }
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddressline1() {
        return this.addressline1;
    }

    public void setAddressline1(String addressline1) {
        this.addressline1 = addressline1;
    }

    public String getAddressline2() {
        return this.addressline2;
    }

    public void setAddressline2(String addressline2) {
        this.addressline2 = addressline2;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return this.zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWww() {
        return this.www;
    }

    public void setWww(String www) {
        this.www = www;
    }

    public Set<HL7Producer> getProducers() {
        return this.producers;
    }

    public void setProducers(Set<HL7Producer> producers) {
        this.producers = producers;
    }

    public Set<ReportableResult> getReportableResults() {
        return this.reportableResults;
    }

    public void setReportableResults(
            Set<ReportableResult> tblreportableresults) {
        this.reportableResults = tblreportableresults;
    }

	public boolean isRetired() {
		return retired;
	}

	public void setRetired(boolean retired) {
		this.retired = retired;
	}

}

/**
 * Copyright 2009 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Descriptive information about a Critic (results or message) used in logging
 * statistic about message processing.
 */
public class CriticDef implements Serializable {

    private static final long serialVersionUID = 2510454500829471918L;

    /** The synthetic primary key */
    private Long id;
    /** The broad type of this critic */
    private CriticType criticType;
    /** The display name of this critic */
    private String name;
    /** The detailed description of this critic */
    private String description;
    /** The set of reportable results decided by this critic */
    private Set<ReportableResult> reportableResults = new HashSet<ReportableResult>(0);
    
    // default constructor
    public CriticDef() {
        
    }
    
    // full constructor
    public CriticDef(Long id, String name, CriticType type, String desc, Set<ReportableResult> reportableResults) { 
        this.id = id;
        this.name = name;
        this.criticType = type;
        this.description = desc;
        this.reportableResults = reportableResults;
    }
    
    // constructor w/ only essential information 
    public CriticDef(String name, CriticType type, String desc) {
        this.name = name;
        this.criticType = type;
        this.description = desc;
    }
    
    // copy constructor
    public CriticDef(CriticDef toClone) {
        if (toClone != null) {
            this.name = toClone.getName();            
            this.criticType = toClone.getCriticType();
            this.description = toClone.getDescription();
        }
    }    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CriticType getCriticType() {
        return criticType;
    }

    public void setCriticType(CriticType criticType) {
        this.criticType = criticType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ReportableResult> getReportableResults() {
        return reportableResults;
    }

    public void setReportableResults(
            Set<ReportableResult> reportableResults) {
        this.reportableResults = reportableResults;
    }
}

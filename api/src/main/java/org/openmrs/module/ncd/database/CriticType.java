/**
 * Copyright 2009 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The general type of a Critic. Currently only "message" or "result".
 */
public class CriticType implements Serializable {
    
    private static final long serialVersionUID = 2700067956618486756L;

    /** The synthetic primary key */
    private Long id;
    /** The name of this critic type */
    private String type;
    /** The set of critics of this type */
    private Set<CriticDef> critics = new HashSet<CriticDef>(0);
    
    // default constructor
    public CriticType() {
        
    }
    
    public CriticType(Long id, String type, Set<CriticDef> critics) {
        this.id = id;
        this.type = type;
        this.critics = critics;
    }
    
    public CriticType(String type) {
        this.type = type;
    }
    
    public CriticType(CriticType toClone) {
        if (toClone != null) {
            this.id = toClone.getId();
            this.type = toClone.getType();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<CriticDef> getCritics() {
        return critics;
    }

    public void setCritics(Set<CriticDef> critics) {
        this.critics = critics;
    }
}

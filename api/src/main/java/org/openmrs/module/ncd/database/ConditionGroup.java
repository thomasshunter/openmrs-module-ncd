package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;

/**
 * A named group of Conditions.
 */
public class ConditionGroup implements java.io.Serializable {

    private static final long serialVersionUID = -7605134700682453572L;

    /** The synthetic primary key */
    private Long id;
    /** The displayed text for this group */
    private String displayText;
    /** The sort order for this group in the aggregate summary report */
    private Integer displayOrder;
    /** True iff this condition group has been retired. */
    private boolean retired;
    /** The Set of conditions in this group */
    private Set<Condition> tblconditionnames = new HashSet<Condition>(
            0);

    public ConditionGroup() {
    }

    public ConditionGroup(String groupname) {
        this.displayText = groupname;
    }

    public ConditionGroup(String groupname, Integer displaysequence,
            Set<Condition> tblconditionnames) {
        this.displayText = groupname;
        this.displayOrder = displaysequence;
        this.tblconditionnames = tblconditionnames;
    }
    
    public ConditionGroup(ConditionGroup src) {
        
    	this.id = src.id;
        this.displayText = src.displayText;
        this.displayOrder = src.displayOrder;
        this.retired = src.retired;
        this.tblconditionnames = new HashSet<Condition>();
        this.tblconditionnames.addAll(src.tblconditionnames);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    public void setDisplayText(String groupname) {
        this.displayText = groupname;
    }

    public Integer getDisplayOrder() {
        return this.displayOrder;
    }

    public void setDisplayOrder(Integer displaysequence) {
        this.displayOrder = displaysequence;
    }

    public Set<Condition> getTblconditionnames() {
        return this.tblconditionnames;
    }

    public void setTblconditionnames(Set<Condition> tblconditionnames) {
        this.tblconditionnames = tblconditionnames;
    }

	public boolean isRetired() {
		return retired;
	}

	public void setRetired(boolean retired) {
		this.retired = retired;
	}
}

package org.openmrs.module.ncd.database.filter;

import java.util.List;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.openmrs.module.ncd.utilities.DropListEntryLong;
import org.springframework.validation.BindException;

public class SearchTermOpLookupLong extends SearchTerm {

    /** The selected operator */
    protected String op = "";
    /** The entered value */
    protected String value = "";
    /** The options for the drop down list */
    protected List<DropListEntryLong> options;
    /** The numeric value for the "none selected" option */
    protected String noneSelectedId = "0";
    
    public SearchTermOpLookupLong() {
        // should only be used by spring post handling
    }
    
    public SearchTermOpLookupLong(String editNameKey, List<DropListEntryLong> options, Long noneSelectedId) {
        super(editNameKey);
        this.options = options;
        this.noneSelectedId = noneSelectedId.toString();
    }
    
    public String getType() {
        
        return "OpLookup";
    }

    public boolean isSet() {

    	// don't count value="0" as set, because this is the blank ("not selection") option in the drop down box
        return isSet(op) || (isSet(value) && !value.equals(noneSelectedId));
    }

    public boolean validate(String path, BindException exceptions) {

        boolean isValid = true;
        
        // If the term has not been set, it's always valid
        if (!isSet()) {
            return true;
        }
        
        // An operator must be selected
        if (!isSet(op)) {
            
            exceptions.rejectValue(path, "ncd.searchterm.opforeignkey.error.noop");
            isValid = false;
            log.debug("validation error: value no operator");
        }

        // The drop list must have a selection (and the value "0" doesn't count as a selection, since it's the blank, "no selection" option)
        if (!isSet(value) || value.equals(noneSelectedId)) {
            
            exceptions.rejectValue(path, "ncd.searchterm.opforeignkey.error.novalue");
            isValid = false;
            log.debug("validation error: operator no value");
        }
        
        return isValid;
    }
    
    /** Adds clauses to a query under construction to evaluate this term */
    public void buildQueryClause(String propertyName, HQLQueryBuilder builder) {

        if (isSet(op) && isSet(value) && !value.equals(noneSelectedId)) {
            
            builder.add(propertyName, op, new Long(value));
        }
    }

    public void clear() {
        
        this.op = "";
        this.value = "";
    }
    
    /**
     * @return the op
     */
    public String getOp() {
        return op;
    }

    /**
     * @param op the op to set
     */
    public void setOp(String op) {
        this.op = op;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the value description
     */
    public String getValueDescription() {
    	if (options == null || value == null) {
    		return "";
    	} else {
    		Long key = new Long(value);
    		for (DropListEntryLong option: options) {
    			if (option.getValue().equals(key)) {
    				return option.getLabel();
    			}
    		}
    		return "";
    	}
    }

    /**
     * 
     * @return the options for the drop down list
     */
	public List<DropListEntryLong> getOptions() {
		return options;
	}

    /**
     * @return the noneSelectedId
     */
    public String getNoneSelectedId() {
        return noneSelectedId;
    }

    /**
     * @param noneSelectedId the noneSelectedId to set
     */
    public void setNoneSelectedId(String noneSelectedId) {
        this.noneSelectedId = noneSelectedId;
    }
}

package org.openmrs.module.ncd.database.filter;

import java.util.ArrayList;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.openmrs.module.ncd.utilities.Pair;
import org.springframework.validation.BindException;

public class SearchTermOpLookup extends SearchTerm {

    /** The selected operator */
    protected String op = "";
    /** The entered value */
    protected String value = "";
    /** The options for the drop down list */
    protected ArrayList<Pair<Integer, String>> options;
    /** The numeric value for the "none selected" option */
    protected String noneSelectedId = "0";
    /** True if the numeric id value should be passed Integer, false if it
     * should be passed as Long. */
    protected boolean selectedIdInteger = true;
    
    public SearchTermOpLookup() {
        // should only be used by spring post handling
    }
    
    public SearchTermOpLookup(String editNameKey, ArrayList<Pair<Integer, String>> options) {
        super(editNameKey);
        this.options = options;
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
            
            Object idValue = null;
            if (selectedIdInteger) {
                idValue = new Integer(value);
            }
            else {
                idValue = new Long(value);
            }
            
            builder.add(propertyName, op, idValue);
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
    		Integer key = new Integer(value);
    		for (Pair<Integer, String> option: options) {
    			if (option.getFirst().equals(key)) {
    				return option.getSecond();
    			}
    		}
    		return "";
    	}
    }

    /**
     * 
     * @return the options for the drop down list
     */
	public ArrayList<Pair<Integer, String>> getOptions() {
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

    /**
     * @return the selectedIdInteger
     */
    public boolean isSelectedIdInteger() {
        return selectedIdInteger;
    }

    /**
     * @param selectedIdInteger the selectedIdInteger to set
     */
    public void setSelectedIdInteger(boolean selectedIdInteger) {
        this.selectedIdInteger = selectedIdInteger;
    }
}

package org.openmrs.module.ncd.database.filter;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.springframework.validation.BindException;

/**
 * A search term that just picks a value from a list.
 */
public class SearchTermOpList extends SearchTerm {

    protected String opListName;
    protected String valueListName;
    protected String op = "";
    protected String value = "";

    public SearchTermOpList() {

        // should only be called by the spring framework
    }
    
    public SearchTermOpList(String editNameKey, String opListName, String valueListName) {

        super(editNameKey);
        this.opListName = opListName;
        this.valueListName = valueListName;
    }
    
    public String getType() {
        
        return "OpList";
    }

    public boolean isSet() {

        return isSet(op) || isSet(value);
    }

    public boolean validate(String path, BindException exceptions) {

        boolean isValid = true;
        
        // If the term has not been set, it's always valid
        if (!isSet()) {
            return true;
        }
        
        // An operator must be selected
        if (!isSet(op)) {
            
            exceptions.rejectValue(path, "ncd.searchterm.oplist.error.noop");
            isValid = false;
            log.debug("validation error: value no operation");
        }

        // The string must have at least one character
        if (!isSet(value)) {
            
            exceptions.rejectValue(path, "ncd.searchterm.oplist.error.novalue");
            isValid = false;
            log.debug("validation error: operation no value");
        }
        
        return isValid;
    }
    
    /** Adds clauses to a query under construction to evaluate this term */
    public void buildQueryClause(String propertyName, HQLQueryBuilder builder) {

        if (isSet(op) && isSet(value)) {
            builder.add(propertyName, op, value);
        }
    }

    public void clear() {
        
        this.op = "";
        this.value = "";
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
     * @return the valueListName
     */
    public String getValueListName() {
        return valueListName;
    }

    /**
     * @param valueListName the valueListName to set
     */
    public void setValueListName(String listName) {
        this.valueListName = listName;
    }

    /**
     * @return the opListName
     */
    public String getOpListName() {
        return opListName;
    }

    /**
     * @param opListName the opListName to set
     */
    public void setOpListName(String opListName) {
        this.opListName = opListName;
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
}

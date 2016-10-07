package org.openmrs.module.ncd.database.filter;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.springframework.validation.BindException;

public class SearchTermOpCondition extends SearchTerm {

    /** The selected operator */
    protected String op = "";
    /** The entered value */
    protected String value = "";
    
    public SearchTermOpCondition() {
        // should only be used by spring post handling
    }
    
    public SearchTermOpCondition(String editNameKey) {

        super(editNameKey);
    }
    
    public String getType() {
        
        return "OpCondition";
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
            
            exceptions.rejectValue(path, "ncd.searchterm.opcondition.error.noop");
            isValid = false;
            log.debug("validation error: value no operation");
        }

        // The string must have at least one character
        if (!isSet(value)) {
            
            exceptions.rejectValue(path, "ncd.searchterm.opcondition.error.novalue");
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
}

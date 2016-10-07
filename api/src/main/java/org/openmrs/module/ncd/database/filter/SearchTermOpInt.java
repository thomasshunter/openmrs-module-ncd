package org.openmrs.module.ncd.database.filter;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.springframework.validation.BindException;

public class SearchTermOpInt extends SearchTerm {

    /** The selected operator */
    protected String op = "";
    /** The entered value */
    protected String value = "";
    
    public SearchTermOpInt() {
        // should only be used by spring post handling
    }
    
    public SearchTermOpInt(String editNameKey) {
        
        super(editNameKey);
    }
    
    public String getType() {
        
        return "OpLong";
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
            
            exceptions.rejectValue(path, "ncd.searchterm.oplong.error.noop");
            isValid = false;
            log.debug("validation error: value no operator");
        }

        // The value must have at least one character
        if (!isSet(value)) {
            
            exceptions.rejectValue(path, "ncd.searchterm.oplong.error.novalue");
            isValid = false;
            log.debug("validation error: operator no value");
        }
        else {
        
            // The value must parse as a Long.
            try {
                Long.parseLong(value);
            }
            catch (Exception e) {
                
                exceptions.rejectValue(path, "ncd.searchterm.oplong.error.parsevalue");
                isValid = false;
                log.debug("validation error: value unparseable");
            }
        }
        
        return isValid;
    }
    
    /** Adds clauses to a query under construction to evaluate this term */
    public void buildQueryClause(String propertyName, HQLQueryBuilder builder) {

        if (isSet(op) && isSet(value)) {

            try {
                
                Integer v = Integer.valueOf(value);
                builder.add(propertyName, op, v);
            }
            catch (Exception e) {
                
            }
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

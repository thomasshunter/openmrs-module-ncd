package org.openmrs.module.ncd.database.filter;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.springframework.validation.BindException;

/**
 * A search term that is "true", "false" or not set.
 */
public class SearchTermBoolean extends SearchTerm {

    protected String value = "";

    public SearchTermBoolean() {

        // should only be called by the spring framework
    }
    
    public SearchTermBoolean(String editNameKey) {

        super(editNameKey);
    }
    
    public String getType() {
        
        return "Boolean";
    }

    public boolean isSet() {

        return isSet(value);
    }

    public boolean validate(String path, BindException exceptions) {

        // The value must parse as a Boolean.
        try {
            Boolean.parseBoolean(value);
            return true;
        }
        catch (Exception e) {
            
            exceptions.rejectValue(path, "ncd.searchterm.boolean.error.parsevalue");
            return false;
        }
    }
    
    /** Adds clauses to a query under construction to evaluate this term */
    public void buildQueryClause(String propertyName, HQLQueryBuilder builder) {

        if (isSet(value)) {
        
            try {
                
                Boolean b = Boolean.parseBoolean(value);
                builder.add(propertyName, "=", b.booleanValue());
            }
            catch (Exception e) {
            }
        }
    }

    public void clear() {
        
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
}

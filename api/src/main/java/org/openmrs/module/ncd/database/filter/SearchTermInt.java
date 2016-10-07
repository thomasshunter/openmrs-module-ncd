package org.openmrs.module.ncd.database.filter;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.springframework.validation.BindException;

public class SearchTermInt extends SearchTerm {

    protected String value = "";

    public SearchTermInt() {

        // should only be called by the spring framework
    }
    
    public SearchTermInt(String editNameKey) {

        super(editNameKey);
    }
    
    public String getType() {
        
        return "String";
    }

    public boolean isSet() {

        return isSet(value);
    }

    public boolean validate(String path, BindException exceptions) {
        
        // The value must parse as a Long.
        try {
            Long.parseLong(value);
            return true;
        }
        catch (Exception e) {
            
            exceptions.rejectValue(path, "ncd.searchterm.oplong.error.parsevalue");
            return false;
        }
    }
    
    /** Adds clauses to a query under construction to evaluate this term */
    public void buildQueryClause(String propertyName, HQLQueryBuilder builder) {

        if (isSet(value)) {
        
            try {
                
                Integer v = Integer.valueOf(value);
                builder.add(propertyName, "=", v);
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

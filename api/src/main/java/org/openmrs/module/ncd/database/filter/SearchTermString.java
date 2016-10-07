package org.openmrs.module.ncd.database.filter;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.springframework.validation.BindException;

public class SearchTermString extends SearchTerm {

    protected String value = "";

    public SearchTermString() {

        // should only be called by the spring framework
    }
    
    public SearchTermString(String editNameKey) {

        super(editNameKey);
    }
    
    public String getType() {
        
        return "String";
    }

    public boolean isSet() {

        return isSet(value);
    }

    public boolean validate(String path, BindException exceptions) {
        
        // Validation cannot actually fail for this term type
        return true;
    }
    
    /** Adds clauses to a query under construction to evaluate this term */
    public void buildQueryClause(String propertyName, HQLQueryBuilder builder) {

        if (isSet(value)) {
        
            builder.add(propertyName, "=", value);
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

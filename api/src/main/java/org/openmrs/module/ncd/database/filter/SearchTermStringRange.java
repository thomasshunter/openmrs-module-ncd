package org.openmrs.module.ncd.database.filter;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.springframework.validation.BindException;

public class SearchTermStringRange extends SearchTerm {

    protected String low = "";
    protected String high = "";

    public String getType() {
        
        return "StringRange";
    }
    
    public boolean isSet() {

        return isSet(low) || isSet(high);
    }
    
    public boolean validate(String path, BindException exceptions) {

        return true;
    }
    
    /** Adds clauses to a query under construction to evaluate this term */
    public void buildQueryClause(String propertyName, HQLQueryBuilder builder) {
        
        if (isSet(low)) {

            builder.add(propertyName, ">=", low);
        }

        // If the high date has been set
        if (isSet(high)) {
            
            builder.add(propertyName, "<=", high);
        }
    }

    public SearchTermStringRange(String editNameKey) {

        super(editNameKey);
    }
    
    public void clear() {
        
        this.low = "";
        this.high = "";
    }
    
    /**
     * @return the low
     */
    public String getLow() {
        return low;
    }

    /**
     * @param low the low to set
     */
    public void setLow(String low) {
        this.low = low;
    }

    /**
     * @return the high
     */
    public String getHigh() {
        return high;
    }

    /**
     * @param high the high to set
     */
    public void setHigh(String high) {
        this.high = high;
    }
}

package org.openmrs.module.ncd.database.filter;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.springframework.validation.BindException;

/**
 * A search term that just picks a value from a list.
 */
public class SearchTermList extends SearchTerm {

    protected String listName;
    protected String value = "";

    public SearchTermList() {

        // should only be called by the spring framework
    }
    
    public SearchTermList(String editNameKey, String listName) {

        super(editNameKey);
        this.listName = listName;
    }
    
    public String getType() {
        
        return "List";
    }

    public boolean isSet() {

        return isSet(value);
    }

    public boolean validate(String path, BindException exceptions) {
        // can't fail.
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

    /**
     * @return the listName
     */
    public String getListName() {
        return listName;
    }

    /**
     * @param listName the listName to set
     */
    public void setListName(String listName) {
        this.listName = listName;
    }
}

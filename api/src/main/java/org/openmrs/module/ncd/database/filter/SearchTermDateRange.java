package org.openmrs.module.ncd.database.filter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.springframework.validation.BindException;

public class SearchTermDateRange extends SearchTerm {

    protected String low = "";
    protected String high = "";

    public String getType() {
        
        return "DateRange";
    }
    
    public boolean isSet() {

        return isSet(low) || isSet(high);
    }
    
    public boolean validate(String path, BindException exceptions) {

        boolean isValid = true;
        DateFormat fmt = DateUtilities.getDateFormat();
        String pattern = ((SimpleDateFormat) fmt).toPattern();
        
        // If the term has not been set, it's always valid
        if (!isSet()) {
            return true;
        }
        
        // If the low date has been set
        Date cvtLow = null;
        if (isSet(low)) {

            // It must be a valid date
            try {
                cvtLow = fmt.parse(low);
            }
            catch (Exception e) {
                
                cvtLow = null;
                exceptions.rejectValue(path, "ncd.searchterm.daterange.error.badlowvalue");
                isValid = false;
                log.debug("validation error: low is invalid (" + pattern + ")");
            }
        }

        // If the high date has been set
        Date cvtHigh = null;
        if (isSet(high)) {
            
            // It must be a valid date
            try {
                cvtHigh = fmt.parse(high);

                // If the low date is set also, it must not be after the high date
                if (cvtLow != null && cvtLow.after(cvtHigh)) {
                    
                    exceptions.rejectValue(path, "ncd.searchterm.dateange.error.lowoverhigh");
                    isValid = false;
                    log.debug("validation error: low > high");
                }
            }
            catch (Exception e) {
                
                cvtHigh = null;
                exceptions.rejectValue(path, "ncd.searchterm.daterange.error.badhighvalue");
                isValid = false;
                log.debug("validation error: high is invalid (" + pattern + ")");
            }
        }

        return isValid;
    }
    
    /** Adds clauses to a query under construction to evaluate this term */
    public void buildQueryClause(String propertyName, HQLQueryBuilder builder) {
        
        DateFormat fmt = DateUtilities.getDateTimeFormat();

        if (isSet(low)) {

            // It must be a valid date
            try {
                
            	// Use a default time of midnight if no time specified
            	String temp = new String(low);
            	if (!temp.contains(":")) {
            		temp = temp + " 0:00 am";
            	}
                builder.add(propertyName, ">=", fmt.parse(temp));
            }
            catch (Exception e) {
                
                log.error("low is invalid");
            }
        }

        // If the high date has been set
        if (isSet(high)) {
            
            // It must be a valid date
            try {
                
            	// Use a default time of 23:59:59 if no time specified
            	String temp = new String(high);
            	if (!temp.contains(":")) {
            		temp = temp + " 11:59 pm";
            	}
                builder.add(propertyName, "<=", fmt.parse(temp));
            }
            catch (Exception e) {
                
                log.error("high is invalid");
            }
        }
    }

    public SearchTermDateRange(String editNameKey) {

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

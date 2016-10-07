package org.openmrs.module.ncd.database.filter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.Pair;
import org.springframework.validation.BindException;

public class SearchTermDateTimeRange extends SearchTerm {

    protected String low = "";
    protected String high = "";

    public String getType() {
        
        return "DateTimeRange";
    }
    
    public boolean isSet() {

        return isSet(low) || isSet(high);
    }
    
    public boolean validate(String path, BindException exceptions) {

        boolean isValid = true;
        DateFormat fmt = DateUtilities.getDateTimeFormat();
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
                exceptions.rejectValue(path, "ncd.searchterm.datetimerange.error.badlowvalue");
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
                    
                    exceptions.rejectValue(path, "ncd.searchterm.datetimerange.error.lowoverhigh");
                    isValid = false;
                    log.debug("validation error: low > high");
                }
            }
            catch (Exception e) {
                
                cvtHigh = null;
                exceptions.rejectValue(path, "ncd.searchterm.datetimerange.error.badhighvalue");
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
                
                builder.add(propertyName, ">=", fmt.parse(low));
                log.debug("adding filter: " + propertyName + " >= " + fmt.parse(low).toString());
            }
            catch (Exception e) {
                
                log.error("low is invalid");
            }
        }

        // If the high date has been set
        if (isSet(high)) {
            
            // It must be a valid date
            try {
                
                builder.add(propertyName, "<=", fmt.parse(high));
                log.debug("adding filter: " + propertyName + " <= " + fmt.parse(high).toString());
            }
            catch (Exception e) {
                
                log.error("high is invalid");
            }
        }
    }

    public SearchTermDateTimeRange(String editNameKey) {

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
    
    /**
     * Get the dates within this search term
     * 
     * @return the low and high dates as a Pair<Date,Date>
     */
    public Pair<Date,Date> getDates() {
    	Date lowDate=null;
    	Date highDate=null;
    	
        DateFormat fmt = DateUtilities.getDateTimeFormat();

        if (isSet(low)) {

            // It must be a valid date
            try {
                lowDate = fmt.parse(low);
            }
            catch (Exception e) {
                log.error("low is invalid");
            }
        }

        // If the high date has been set
        if (isSet(high)) {
            
            // It must be a valid date
            try {
                highDate = fmt.parse(high);
            }
            catch (Exception e) {
                log.error("high is invalid");
            }
        }

    	return new Pair<Date,Date>(lowDate, highDate);
    }
}

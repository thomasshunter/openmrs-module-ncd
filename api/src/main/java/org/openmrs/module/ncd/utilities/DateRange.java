package org.openmrs.module.ncd.utilities;

import java.util.Date;

/**
 * A date/time range.
 */
public class DateRange {

    private Date low;
    private Date high;
    
    public DateRange() {
    }
    
    public DateRange(Date low, Date high) {
        
        this.low = low;
        this.high = high;
    }

    /**
     * Tests if this date range is before the specified date and time.
     * 
     * @param test The date and time to compare this range to.
     * @return true iff high <= test
     */
    public boolean before(Date test) {
        return test.after(this.high) || test.equals(this.high);
    }
    
    /**
     * Tests if this date range contains the specified date and time
     * 
     * @param test The date and time to be compared to this range.
     * @return true iff low <= test < high.
     */
    public boolean contains(Date test) {
        return (this.low.before(test) || this.low.equals(test))
             && this.high.after(test);
    }
    
    /**
     * Tests if this date range is after the specified date and time.
     * 
     * @param test The date and time to compare this range to.
     * @return true iff low > test
     */
    public boolean after(Date test) {
        return this.low.after(test);
    }

    @Override
    public String toString() {
        
        return "DateRange[low=" + low + ", high=" + high + "]";
    }

    /**
     * @return the low
     */
    public Date getLow() {
        return low;
    }

    /**
     * @param low the low to set
     */
    public void setLow(Date low) {
        this.low = low;
    }

    /**
     * @return the high
     */
    public Date getHigh() {
        return high;
    }

    /**
     * @param high the high to set
     */
    public void setHigh(Date high) {
        this.high = high;
    }
}

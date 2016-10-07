package org.openmrs.module.ncd.output.zerocount;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.DateRange;
import org.openmrs.module.ncd.utilities.DateUtilities;

/**
 * A schedule for the ZeroCountReport task and methods to convert it to and
 * from a String.
 */
public class ReportSchedule {

    private static Log logger = LogFactory.getLog(ReportSchedule.class);

    private static final DateFormat timeFormat = new SimpleDateFormat("h:mm:ss a");
    private Date[] startTimes = null;
    private DateRange currentWindow;
    private int currentWindowIndex;

    /**
     * Private, so instances cannot be directly constructed. See getInstance.
     */
    private ReportSchedule(Date originDateTime, Date[] startTimes) {
        
        this.startTimes = startTimes;
        
        logger.debug("originDateTime=" + originDateTime);
        
        // Set the window and index so the next call to next() returns the
        // window containing the origin date and time.
        
        // Set to the first window starting on the origin date/time.
        currentWindowIndex = 0;
        currentWindow = getWindow(originDateTime, currentWindowIndex);
        
        logger.debug("origin zero window=" + currentWindow);
        
        // While the window is before the origin date / time
        while (currentWindow.before(originDateTime)) {
            
            // advance the window
            advance();
        }
        
        // While the window is after the origin date / time
        while (currentWindow.after(originDateTime)) {
            
            // back the window up
            retreat();
        }

        logger.debug("first window=" + currentWindow);
    }

    /**
     * Get an instance of a schedule based on a specified string-encoded
     * schedule string.
     * 
     * @param schedule The string-encoded schedule.
     * @return A schedule corresponding to the specified string-encoded
     * schedule.
     */
    public static ReportSchedule getInstance(Date originDateTime, String schedule)
        throws InvalidScheduleException
    {
        return new ReportSchedule(originDateTime, decode(schedule));
    }
    
    /**
     * Convert this schedule to a string-encoded schedule.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer buf = new StringBuffer();
        
        for (Date startTime : startTimes) {
            
            if (buf.length() != 0) {
                
                buf.append(",");
            }
            
            buf.append(timeFormat.format(startTime));
        }
        
        return buf.toString();
    }
    
    /**
     * Tests if the specified string-encoded schedule is valid.
     * 
     * @param schedule The string-encoded schedule to be tested.
     * @return True iff the schedule is valid.
     */
    public boolean isValid(String schedule) {
        
        try {
            
            decode(schedule);
            return true;
        }
        catch (InvalidScheduleException ise) {
            return false;
        }
    }

    public boolean hasNext() {

        // To infinity, and beyond!
        return false;
    }

    public DateRange next() {

        advance();
        
        logger.debug("window=" + currentWindow);
        
        return currentWindow;
    }

    public DateRange current() {
        
        logger.debug("window=" + currentWindow);
        
        return currentWindow;
    }

    public DateRange previous() {
        
        retreat();
        
        logger.debug("window=" + currentWindow);
        
        return currentWindow;
    }

    public void remove() {

        // Can't.
    }
    
    private void advance() {
        
        DateRange oldWindow = currentWindow;
        currentWindow = new DateRange();

        int dayOfYearAdjust = 0;
        currentWindowIndex += 1;
        if (currentWindowIndex >= startTimes.length) {
            currentWindowIndex -= startTimes.length;
        }
        int endWindowIndex = currentWindowIndex + 1;
        if (endWindowIndex >= startTimes.length) {
            dayOfYearAdjust = 1;
            endWindowIndex -= startTimes.length;
        }
        currentWindow.setLow(oldWindow.getHigh());
        currentWindow.setHigh(
            DateUtilities.mergeDateTime(oldWindow.getHigh(), 
                    startTimes[endWindowIndex], dayOfYearAdjust));
        
        logger.debug("final window=" + currentWindow);
    }

    private void retreat() {

        DateRange oldWindow = currentWindow;
        currentWindow = new DateRange();

        int dayOfYearAdjust = 0;
        currentWindowIndex -= 1;
        if (currentWindowIndex < 0) {
            dayOfYearAdjust = -1;
            currentWindowIndex += startTimes.length;
        }
        currentWindow.setHigh(oldWindow.getLow());
        currentWindow.setLow(
            DateUtilities.mergeDateTime(oldWindow.getLow(), 
                    startTimes[currentWindowIndex], dayOfYearAdjust));
        
        logger.debug("final window=" + currentWindow);
    }

    /**
     * Given an origin Date and a window index, return the starting
     * date and time of the corresponding window.
     * 
     * @param originDate The date part for windowIndex value zero.
     * @param windowIndex The index of the window starting on the
     * specified to be returned.
     * 
     * @return The selected window as a DateRange.
     */
    private DateRange getWindow(Date originDate, int windowIndex) {
        
        int dayOfYearAdjust = 0;
        int nextWindowIndex = currentWindowIndex + 1;
        if (nextWindowIndex >= startTimes.length) {
            dayOfYearAdjust = 1;
            nextWindowIndex -= startTimes.length;
        }

        return new DateRange(
            DateUtilities.mergeDateTime(originDate, 
                    startTimes[currentWindowIndex], 0),
            DateUtilities.mergeDateTime(originDate, 
                    startTimes[nextWindowIndex], dayOfYearAdjust));
    }

    /**
     * Helper method to decode a string-encoded schedule to an array of
     * window start times, validating the schedule at the same time.
     * 
     * @param schedule
     * @return
     * @throws InvalidScheduleException
     */
    private static Date[] decode(String schedule) throws InvalidScheduleException {

        // Split the encoded string at commas
        String[] items = schedule.split(",");

        // There must be at least one value
        if (items == null || items.length < 1) {
            
            throw new InvalidScheduleException("at least one value is required");
        }

        Date[] results = new Date[items.length];

        Date lastStartTime = null;
        for (int itemIndex = 0; itemIndex < items.length; itemIndex++) {
            
            String item = items[itemIndex];

            try {
                
                Date thisStartTime = timeFormat.parse(item);

                if (lastStartTime != null && 
                    thisStartTime.before(lastStartTime))
                {
                    throw new InvalidScheduleException("value is out of order");
                }
                
                results[itemIndex] = thisStartTime;
                lastStartTime = thisStartTime;
            }
            catch (Exception e) {
                
                throw new InvalidScheduleException("each value must be a decimal integer");
            }
        }
        
        return results;
    }
}

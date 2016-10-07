package org.openmrs.module.ncd.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateUtilities {

    private static Log logger = LogFactory.getLog(DateUtilities.class);

    /** Returns a copy of the specified date/time with the time
     * information replaced by "0" : 12 midnight.
     */
    public static Date truncate(Date date) {
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTime().getTime());
    }

    public static Date[] computeBucketDates(int dateBucketCount, Date lastDate) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(lastDate);

        Date[] buckets = new Date[dateBucketCount];
        buckets[0] = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        for (int bucket = 1; bucket < dateBucketCount; bucket++) {
            cal.add(Calendar.DATE, -1);
            buckets[bucket] = cal.getTime();
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("buckets=" + Arrays.asList(buckets));
        }

        return buckets;
    }
    
    /**
     * Formats a Date as mm/dd/yy (with leading zero fill in each subfield)
     * 
     * @param date The Date to be formatted.
     * @return The formatted date as a String.
     */
    public static String format(Date date) {
    	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
    	return formatter.format(date);
    }
    
    /**
     * Formats a Date as mm/dd/yy (with leading zero fill in each subfield)
     * If the date is null, returns an empty string.
     * 
     * @param date The Date to be formatted.
     * @return The formatted date as a String.
     */
    public static String formatNullDate(Date date) {
    	if (date == null) {
    		return "";
    	}
    	else {
    		return format(date);
    	}
    }
    
    /**
     * Formats a Date as hh:mm {AM|PM} (i.e. returning only the time component).
     * @param date The date to be formatted.
     * @return The formatted time component as a String.
     */
    public static String formatTimeOnly(Date date) {
    	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm a");
    	return formatter.format(date);
    }
    
    /**
     * Formats a Date as mm/dd/yyyy hh:mm (AM|PM).
     * 
     * @param date The date to be formatted.
     * @return The formatted date and time as a String.
     */
    public static String formatDateTime(Date date) {
        
        return getDateTimeFormat().format(date);
    }

    /** Get the standard NCD date-only format */
    public static DateFormat getDateFormat() {

        DateFormat fmt = DateFormat.getDateInstance(DateFormat.SHORT);
        fmt.setLenient(true);
        return fmt;
    }
    
    /** Get the standard NCD time-only format */
    public static DateFormat getTimeFormat() {

        DateFormat fmt = DateFormat.getTimeInstance(DateFormat.SHORT);
        fmt.setLenient(true);
        return fmt;
    }
    
    /** Get the standard NCD date+time format */
    public static DateFormat getDateTimeFormat() {

        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        fmt.setLenient(true);
        return fmt;
    }
    
    public static Date tryParseDate(String dateFormat, String dateString)
    {
        return tryParseDate(new SimpleDateFormat(dateFormat), dateString);
    }
    
    public static Date tryParseDate(DateFormat dateFormat, String dateString)
    {
        Date testDate = null;
        try
        {
        	dateFormat.setLenient(false);
            testDate = dateFormat.parse(dateString);
        }
        catch (ParseException e)
        {
            testDate = null;
        }
        return testDate;        
    }
    
    public static Date parseDate(String dateString) {
        
        return tryParseDate(getDateFormat(), dateString);
    }
    
    public static Date parseTime(String dateString) {
        
        return tryParseDate(getTimeFormat(), dateString);
    }
    
    public static Date parseDateTime(String dateString) {
        
        return tryParseDate(getDateTimeFormat(), dateString);
    }
    
    /**
     * Creates a new date by merging the date components of "date" with the
     * time components of "time".
     * 
     * @param date The source for the year, month and day of the result.
     * @param time The source for the hour, minute, second and millisecond of
     * the result.
     * @return The merged date.
     */
    public static Date mergeDateTime(Date date, Date time) {

        return mergeDateTime(date, time, 0);
    }
    
    /**
     * Creates a new date by merging the date components of "date" with the
     * time components of "time".
     * 
     * @param date The source for the year, month and day of the result.
     * @param time The source for the hour, minute, second and millisecond of
     * the result.
     * @param dayOfYearAdjust Added to the DAY_OF_YEAR field of the returned
     * date.
     * @return The merged date.
     */
    public static Date mergeDateTime(Date date, Date time, int dayOfYearAdjust) {

//      logger.debug("date=" + date + ", time=" + time + ", adjust=" + dayOfYearAdjust);

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        
//      logger.debug("dateCal=" + dateCal);
//      logger.debug("  aka " + formatDateTime(dateCal.getTime()));

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);
        
//      logger.debug("timeCal=" + timeCal);
//      logger.debug("  aka " + formatDateTime(timeCal.getTime()));

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(date);

//      logger.debug("initial resultCal=" + resultCal);
//      logger.debug("  aka " + formatDateTime(resultCal.getTime()));

        resultCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        resultCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        resultCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
        resultCal.set(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND));
        
//      logger.debug("unadjusted resultCal=" + resultCal);
//      logger.debug("  aka " + formatDateTime(resultCal.getTime()));
        
        resultCal.add(Calendar.DAY_OF_YEAR, dayOfYearAdjust);
        
//      logger.debug("modified resultCal=" + resultCal);
//      logger.debug("  aka " + formatDateTime(resultCal.getTime()));
        
        return resultCal.getTime();
    }
    
    public static Date adjust(Date src, int fieldIndex, int adjustment) {
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(src);
        cal.add(fieldIndex, adjustment);
        return cal.getTime();
    }
}

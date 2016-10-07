package org.openmrs.module.ncd.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.openmrs.module.ncd.model.SystemActivityEvent;

public class SystemActivityAppender implements Appender {

    private String name = "ncd";
    private ErrorHandler errorHandler = null;
    private Filter headFilter = null;
    private Filter tailFilter = null;
    private int nesting = 0;

    public SystemActivityAppender() {
    }

    public void addFilter(Filter arg0) {
        
        if (headFilter == null) {
            
            headFilter = arg0;
            tailFilter = arg0;
        }
        else {
            
            tailFilter.setNext(arg0);
            tailFilter = arg0;
        }
    }

    public void clearFilters() {
        
        headFilter = null;
        tailFilter = null;
    }

    public Filter getFilter() {
        return headFilter;
    }

    public void close() {
        // Nothing to do
    }

    public void doAppend(LoggingEvent arg0) {

        // Ignore reentrant calls from logging calls inside the code below
        if (nesting <= 0) {

            nesting++;

            SystemActivityEvent event = new SystemActivityEvent();
            event.setLevel(SystemActivityEvent.Level.valueOf(arg0.getLevel().toString()));
            event.setOccurred(new Date(arg0.getTimeStamp()));
            event.setStackTraceLines(arg0.getThrowableStrRep());
            event.setSummary(arg0.getRenderedMessage());
            addEvent(event);

            nesting--;
        }
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler arg0) {
        errorHandler = arg0;
    }

    public boolean requiresLayout() {
        return false;
    }

    public Layout getLayout() {
        // We don't have layouts.
        return null;
    }

    public void setLayout(Layout arg0) {
        // ignored, since we don't have layouts.
    }

    public String getName() {
        return name;
    }

    public void setName(String arg0) {
        name = arg0;
    }
    
    // -----------------------------------------------------------------
    // ncd module interface
    // -----------------------------------------------------------------
    
    private static final int MAX_EVENTS = 10;

    private static List<SystemActivityEvent> events = 
        new ArrayList<SystemActivityEvent>();
    
    public static boolean isLoggable(SystemActivityEvent event) {
        
        return event.getLevel() == SystemActivityEvent.Level.WARN ||
               event.getLevel() == SystemActivityEvent.Level.ERROR ||
               event.getLevel() == SystemActivityEvent.Level.FATAL;
    }
    
    public static void addEvent(SystemActivityEvent event) {
        
        // If the event isn't filtered out
        if (isLoggable(event)) {

            synchronized (events) {

                // Insert at the front of the list
                events.add(0, event);
                
                // If the list is now too long, keep the most recent entries
                if (events.size() > MAX_EVENTS) {
                    
                    events = new ArrayList<SystemActivityEvent>(events.subList(0, MAX_EVENTS));
                }
            }
        }
    }
    
    public static List<SystemActivityEvent> getEvents() {
        
        synchronized (events) {
            return new ArrayList<SystemActivityEvent>(events);
        }
    }
}

package org.openmrs.module.ncd.model;

import java.util.Date;

public class SystemActivityEvent {

    public enum Level { DEBUG, INFO, WARN, ERROR, FATAL };
    
    private Level level;
    private String summary;
    private Date occurred;
    private String[] stackTraceLines;

    public SystemActivityEvent() {
    }

    public SystemActivityEvent(Level level, String summary, Date occurred, String[] stackTraceLines) {
        
        this.level = level;
        this.summary = summary;
        this.occurred = occurred;
        this.stackTraceLines = stackTraceLines;
    }

    /**
     * @return the level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return the occurred
     */
    public Date getOccurred() {
        return occurred;
    }

    /**
     * @param occurred the occurred to set
     */
    public void setOccurred(Date occurred) {
        this.occurred = occurred;
    }

    /**
     * @return the stackTraceLines
     */
    public String[] getStackTraceLines() {
        return stackTraceLines;
    }

    /**
     * @param stackTraceLines the stackTraceLines to set
     */
    public void setStackTraceLines(String[] stackTraceLines) {
        this.stackTraceLines = stackTraceLines;
    }
    
    /**
     * @return the formatted stack trace lines
     * 
     */
    public String getFormattedStackTrace() {
    	if (stackTraceLines == null || stackTraceLines.length == 0) {
    		return null;
    	}
    	StringBuilder sb = new StringBuilder();
    	for (String line : stackTraceLines) {
    		sb.append(line);
    		sb.append("\\n");
    	}
    	// The stack trace cannot contain ", because that is the ONCLICK= value delimiter.
    	// The stack trace cannot contain ', because that is parameter value delimiter in a Javascript function call.
    	//
    	// So, convert " to &quot;
    	// Convert ' to &rsquo;
    	String stackTrace = sb.toString();
    	stackTrace = stackTrace.replace("\"", "&quot;");
    	stackTrace = stackTrace.replace("'", "&rsquo;");
    	return stackTrace;
    }
}

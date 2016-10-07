package org.openmrs.module.ncd.model;

import java.util.HashMap;
import java.util.Map;

public class ConditionCount implements java.io.Serializable {

    private static final long serialVersionUID = 26472655223863228L;
    private String application;
    private String location;
    private String facility;
    private String conditionname;
    private int recentCount = 0;
    private int historicalCount = 0;
    private double recentRate = 0.0;
    private double historicalRate = 0.0;
    
    public ConditionCount() {
    }

    public boolean equals(Object o) {
        
        if (!(o instanceof ConditionCount)) {
            
            return false;
        }
        
        ConditionCount that = (ConditionCount) o;
        return this.application.equals(that.application) &&
               this.location.equals(that.location) &&
               this.facility.equals(that.facility) &&
               this.conditionname.equals(that.conditionname);
    }

    public int hashCode() {
        
        
        int hash = 7;
        hash = 31 * hash + application.hashCode();
        hash = 31 * hash + location.hashCode();
        hash = 31 * hash + facility.hashCode();
        hash = 31 * hash + conditionname.hashCode();
        return hash;
    }
    
    public String toString() {
        
        return "ConditionCount(" +
                    "application=" + application +
                    ", location=" + location +
                    ", facility=" + facility +
                    ", conditionname=" + conditionname +
                    ", recentCount=" + recentCount +
                    ", historicalCount=" + historicalCount +
                    ", recentRate=" + recentRate +
                    ", historicalRate=" + historicalRate +
               ")";
    }
    
    private static final String columnNames = "APPLICATION,FACILITY,LOCATION,CONDITION,HISTORICAL_COUNT,HISTORICAL_RATE,RECENT_COUNT,RECENT_RATE";

    public static String getColumnNames() {
    	
    	return columnNames;
    }
    
    public Map<String,Object> toMap() {
    	
    	Map<String,Object> row = new HashMap<String,Object>();
    	row.put("APPLICATION", application);
    	row.put("FACILITY", facility);
    	row.put("LOCATION", location);
    	row.put("CONDITION", conditionname);
    	row.put("HISTORICAL_COUNT", historicalCount);
    	row.put("HISTORICAL_RATE", historicalRate);
    	row.put("RECENT_COUNT", recentCount);
    	row.put("RECENT_RATE", recentRate);
    	return row;
    }

    /**
     * @return the application
     */
    public String getApplication() {
        return application;
    }

    /**
     * @param application the application to set
     */
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the condition
     */
    public String getConditionname() {
        return conditionname;
    }

    /**
     * @param condition the condition to set
     */
    public void setConditionname(String conditionname) {
        this.conditionname = conditionname;
    }

    /**
     * @return the recentCount
     */
    public int getRecentCount() {
        return recentCount;
    }

    /**
     * @param recentCount the recentCount to set
     */
    public void setRecentCount(int recentCount) {
        this.recentCount = recentCount;
    }

    /**
     * @return the historicalCount
     */
    public int getHistoricalCount() {
        return historicalCount;
    }

    /**
     * @param historicalCount the historicalCount to set
     */
    public void setHistoricalCount(int historicalCount) {
        this.historicalCount = historicalCount;
    }

    /**
     * @return the recentRate
     */
    public double getRecentRate() {
        return recentRate;
    }

    /**
     * @param recentRate the recentRate to set
     */
    public void setRecentRate(double recentRate) {
        this.recentRate = recentRate;
    }

    /**
     * @return the historicalRate
     */
    public double getHistoricalRate() {
        return historicalRate;
    }

    /**
     * @param historicalRate the historicalRate to set
     */
    public void setHistoricalRate(double historicalRate) {
        this.historicalRate = historicalRate;
    }

	public String getFacility() {
		return facility;
	}

	public void setFacility(String facility) {
		this.facility = facility;
	}
}

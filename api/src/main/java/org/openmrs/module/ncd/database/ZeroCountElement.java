package org.openmrs.module.ncd.database;


/**
 * The header for the collection of ZeroCountElements for one call to
 * RateMonitoringDAO.getZeroCountConditions.
 */
public class ZeroCountElement implements java.io.Serializable {

	private static final long serialVersionUID = 4426304348397177303L;
	
	/** The synthetic primary key */
    private Integer id;
    /** The universe this element is a part */
    private ZeroCountUniverse universe;
    /** The application */
    private String application;
    /** The facility */
    private String facility;
    /** The location */
    private String location;
    /** The condition name */
    private String conditionName;
    
    public ZeroCountElement() {
    }
    
    public ZeroCountElement(ZeroCountUniverse universe, String application,
    		String facility, String location, String conditionName) {
        
        this.universe = universe;
        this.application = application;
        this.facility = facility;
        this.location = location;
        this.conditionName = conditionName;
    }
    
    public ZeroCountElement(Integer id, ZeroCountUniverse universe,
    		String application, String facility, String location,
    		String conditionName) {
        
        this.id = id;
        this.universe = universe;
        this.application = application;
        this.facility = facility;
        this.location = location;
        this.conditionName = conditionName;
    }

    public String toString() {
    
    	return "ZeroCountElement(" +
    				"id=" + id +
    				", universe=" + universe +
    				", application=" + application +
    				", facility=" + facility +
    				", location=" + location +
    				", conditionName=" + conditionName +
    			")";
    }
    
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

	public ZeroCountUniverse getUniverse() {
		return universe;
	}

	public void setUniverse(ZeroCountUniverse universe) {
		this.universe = universe;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getFacility() {
		return facility;
	}

	public void setFacility(String facility) {
		this.facility = facility;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getConditionName() {
		return conditionName;
	}

	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}
}

package org.openmrs.module.ncd.database;

import org.openmrs.scheduler.TaskDefinition;

/**
 * Represents a mapping between an Entity and a Condition, signalling that the
 * Zero Count Condition Report should monitor the rate of occurrence of the
 * specified condition from the specified Entity in reportable results.
 */
public class MonitoredCondition implements java.io.Serializable {

    private static final long serialVersionUID = -4835660656144676833L;
    
    /** The synthetic primary key */
    private Long id;
    /** The task to which this record belongs */
    private TaskDefinition task;
    /** The HL7 sending application value that reportable results must have to
     * match this MonitoredCondition. */
    private String application;
    /** The HL7 sending facility value that reportable results must have to
     * match this MonitoredCondition. */
    private String facility;
    /** The HL7 sending location value that reportable results must have to
     * match this MonitoredCondition. */
    private String location;
    /** The Entity for which the rate of reportable results from this Entity
     * should be monitored by the Rate Monitor Task. */
    private Condition condition;
    
    public MonitoredCondition() {
    }
    
    public MonitoredCondition(TaskDefinition task, String application, 
    		String facility, String location, Condition condition) {
        
    	this.task = task;
    	this.application = application;
    	this.facility = facility;
    	this.location = location;
        this.condition = condition;
    }
    
    public MonitoredCondition(MonitoredCondition src) {
    	
    	this.task = src.task;
    	this.application = src.application;
    	this.facility = src.facility;
    	this.location = src.location;
    	this.condition = src.condition;
    }

    public String toString() {
        
        return "MonitoredCondition(" +
                    "id=" + id +
                    ", task=" + task +
                    ", application=" + application +
                    ", facility=" + facility +
                    ", location=" + location +
                    ", condition=" + condition +
               ")";
    }
    
    public String toString(Condition condition, String indent) {
    
    	if (condition == null) {
    		
    		return null;
    	}
    	else {

    		return condition.toString(indent + "  ");
    	}
    }
    
    public String toString(String indent) {

        return "MonitoredCondition(\n" +
                    indent + "id=" + id + "\n" +
                    indent + "task=" + task.toString() + "\n" +
                    indent + "application=" + application + "\n" +
                    indent + "facility=" + facility + "\n" +
                    indent + "location=" + location + "\n" +
                    indent + "condition=" + toString(condition, indent + "  ") + "\n" +
                    indent + ")";
    }

    public boolean nullEquals(Condition left, Condition right) {

    	if (left == null && right == null) {
    		return true;
    	}
    	else if (left == null || right == null) {
    		return false;
    	}
    	else {
    		return left.getId().equals(right.getId());
    	}
    }
    
    @Override
    public boolean equals(Object other) {
        
        if (!(other instanceof MonitoredCondition)) {
            return false;
        }
        
        MonitoredCondition that = (MonitoredCondition) other;
        return this.task.getId().equals(that.task.getId()) &&
        	   this.application.equals(that.application) &&
        	   this.facility.equals(that.facility) &&
        	   this.location.equals(that.location) &&
        	   nullEquals(condition, that.condition);
    }
    
    @Override
    public int hashCode() {
        
        int hash = 7;
        hash = 31 * hash + task.getId().hashCode();
        hash = 31 * hash + application.hashCode();
        hash = 31 * hash + facility.hashCode();
        hash = 31 * hash + location.hashCode();
        if (condition != null) {
        	hash = 31 * hash + condition.getId().hashCode();
        }
        return hash;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

	public TaskDefinition getTask() {
		return task;
	}

	public void setTask(TaskDefinition task) {
		this.task = task;
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

    /**
     * @return the code
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * @param code the code to set
     */
    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}

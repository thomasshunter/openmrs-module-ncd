package org.openmrs.module.ncd.database;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.scheduler.TaskDefinition;

/**
 * The header for the collection of ZeroCountElements for one call to
 * RateMonitoringDAO.getZeroCountConditions.
 */
public class ZeroCountUniverse implements java.io.Serializable {

	private static final long serialVersionUID = 6728925696059491927L;
	
	/** The synthetic primary key */
    private Integer id;
    /** The task definition that created this universe */
    private TaskDefinition task;
    /** The date and time this universe was created */
    private Date dateCreated;
    /** The elements in this universe */
    private Set<ZeroCountElement> elements = new HashSet<ZeroCountElement>();
    
    public ZeroCountUniverse() {
    }
    
    public ZeroCountUniverse(TaskDefinition task, Date dateCreated) {
        
        this.task = task;
        this.dateCreated = dateCreated;
    }
    
    public ZeroCountUniverse(Integer id, TaskDefinition task, Date dateCreated) {
        
        this.id = id;
        this.task = task;
        this.dateCreated = dateCreated;
    }

    public String toString() {
    
    	return "ZeroCountUniverse(" +
    				"id=" + id +
    				", task=" + task +
    				", dateCreated=" + dateCreated +
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

	public TaskDefinition getTask() {
		return task;
	}

	public void setTask(TaskDefinition task) {
		this.task = task;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Set<ZeroCountElement> getElements() {
		return elements;
	}

	public void setElements(Set<ZeroCountElement> elements) {
		this.elements = elements;
	}
}

package org.openmrs.module.ncd.database;

import java.io.Serializable;

public class NlpCriticContextGroup implements Serializable {
    
    private static final long serialVersionUID = 8632208752127546840L;

    private Long id;
	private String displayText;
	
	public NlpCriticContextGroup() {
	}
	
	public NlpCriticContextGroup(String typeName) {
		this.displayText = typeName;
	}
	
	public NlpCriticContextGroup(NlpCriticContextGroup src) {
	    
	    this.id = src.id;
	    this.displayText = src.displayText;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getDisplayText() {
		return displayText;
	}		
	
	public void setDisplayText(String typeName) {
		this.displayText = typeName;
	}		
}

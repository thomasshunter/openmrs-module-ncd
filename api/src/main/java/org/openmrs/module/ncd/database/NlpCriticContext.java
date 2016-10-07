package org.openmrs.module.ncd.database;

public class NlpCriticContext implements java.io.Serializable {
	
	private static final long serialVersionUID = -1904011114437075664L;
	
	private Long id;
	private NlpCriticContextType contextType;
	private String contextValue;
	private String contextGroup;
	
	public NlpCriticContext() {
		// default constructor		
	}
	
	public NlpCriticContext(NlpCriticContextType type, String value) {
		// basic constructor
		this.contextType = type;
		this.contextValue = value;
	}
	
	public NlpCriticContext(NlpCriticContextType type, String value, String group) {
		// full constructor
		this.contextType = type;
		this.contextValue = value;
		this.contextGroup = group;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public NlpCriticContextType getContextType() {
		return contextType;
	}
	
	public void setContextType(NlpCriticContextType type) {
		this.contextType = type;
	}
	
	public String getContextValue() {
		return contextValue;
	}
	
	public void setContextValue(String value) {
		this.contextValue = value;
	}
	
	public String getContextGroup() {
		return contextGroup;
	}
	
	public void setContextGroup(String group) {
		this.contextGroup = group;
	}	
}

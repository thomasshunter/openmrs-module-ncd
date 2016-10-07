package org.openmrs.module.ncd.database;

import java.io.Serializable;
import java.util.Set;

public class NlpCriticContextType implements Serializable {	
	private static final long serialVersionUID = 1623969484169029179L;
	
	private Long id;
	private String typeName;
	private boolean usingContextGroup;
	private boolean usingPreString;
	private boolean usingPostString;
	private boolean matchNegation;
	private boolean usingSmallWindow;
	private String description;
	private Set<NlpCriticContext> nlpContexts;
	
	public NlpCriticContextType() {
		// default constructor
	}
	
	public NlpCriticContextType(String typeName) {
		this.typeName = typeName;
	}
	
	public NlpCriticContextType(String typeName, boolean usesContextGroup, boolean usesPreString, 
			boolean usesPostString, boolean matchNegation) {
		this.typeName = typeName;
		this.usingContextGroup = usesContextGroup;
		this.usingPreString = usesPreString;
		this.usingPostString = usesPostString;
		this.matchNegation = matchNegation;
	}
	
	public NlpCriticContextType(NlpCriticContextType src) {
	    
	    this.id = src.id;
	    this.typeName = src.typeName;
	    this.usingContextGroup = src.usingContextGroup;
	    this.usingPreString = src.usingPreString;
	    this.usingPostString = src.usingPostString;
	    this.matchNegation = src.matchNegation;
	    this.usingSmallWindow = src.usingSmallWindow;
        this.description = src.description;
	    this.nlpContexts = src.nlpContexts;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getTypeName() {
		return typeName;
	}		
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}		
	
	public void setUsingContextGroup(boolean usingContextGroup) {
		this.usingContextGroup = usingContextGroup;
	}

	public boolean isUsingContextGroup() {
		return usingContextGroup;
	}

	public void setNlpContexts(Set<NlpCriticContext> nlpContexts) {
		this.nlpContexts = nlpContexts;
	}

	public Set<NlpCriticContext> getNlpContexts() {
		return nlpContexts;
	}

	public void setUsingPreString(boolean usingPreString) {
		this.usingPreString = usingPreString;
	}

	public boolean isUsingPreString() {
		return usingPreString;
	}

	public void setUsingPostString(boolean usingPostString) {
		this.usingPostString = usingPostString;
	}

	public boolean isUsingPostString() {
		return usingPostString;
	}

	public void setMatchNegation(boolean matchNegation) {
		this.matchNegation = matchNegation;
	}

	public boolean isMatchNegation() {
		return matchNegation;
	}

	public void setUsingSmallWindow(boolean usingSmallWindow) {
		this.usingSmallWindow = usingSmallWindow;
	}

	public boolean isUsingSmallWindow() {
		return usingSmallWindow;
	}

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }		
}

package org.openmrs.module.ncd.database;

/**
 * A type of abstract code, such as "diagnosis", "observation" or
 * "patientrace" as seen in an HL7 message.
 */
public class CodeType implements java.io.Serializable {

    private static final long serialVersionUID = -4331298888286624573L;
    
    /** The synthetic primary key */
    private Long id;
    /** The internal name for this code type, such as "LN" or "I9" */
    private String name;
    /** A key into the message.properties file for the text to be displayed
     * for this code type, such as "LOINC" or "ICD9" */
    private String displayTextKey;
    /** True iff this code type has been retired */
    private boolean retired;
    
    public CodeType() {
        
    }

    public CodeType(CodeType src) {
        
        this.id = src.id;
        this.name = src.name;
        this.displayTextKey = src.displayTextKey;
        this.retired = src.retired;
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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the displayTextKey
     */
    public String getDisplayTextKey() {
        return displayTextKey;
    }

    /**
     * @param displayTextKey the displayTextKey to set
     */
    public void setDisplayTextKey(String displayTextKey) {
        this.displayTextKey = displayTextKey;
    }

    /**
     * @return the retired
     */
    public boolean isRetired() {
        return retired;
    }

    /**
     * @param retired the retired to set
     */
    public void setRetired(boolean retired) {
        this.retired = retired;
    }
}

package org.openmrs.module.ncd.utilities;

public class CodeTypeListEntry {

    private String code;
    private String displayText;
    
    public CodeTypeListEntry() {
    }
    
    public CodeTypeListEntry(String code, String displayText) {
        
        this.code = code;
        this.displayText = displayText;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the displayText
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     * @param displayText the displayText to set
     */
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}

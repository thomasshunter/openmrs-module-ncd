package org.openmrs.module.ncd.database;

import org.openmrs.module.ncd.database.filter.ListPageRow;

/**
 * An abstract code, such as "153.3", in a specified abstract code system,
 * such as "ICD9". Codes have a type, such as "diagnosis". Codes have
 * also have a display text key (a key into the messages.properties file),
 * and may be retired.
 */
public class Code implements java.io.Serializable, ListPageRow 
{
    private static final long serialVersionUID = -3274001397713743779L;
    
    /** The synthetic primary key */
    private Long id;
    /** The type of this code, such as "diagnosis" or "observation" */
    private CodeType codeType;
    /** The code system to which this code belongs, such as "LOINC" or "ICD10" */
    private CodeSystem codeSystem;
    /** The text form of this code, as it would be seen in messages */
    private String code;
    /** A key into the messages.properties file for the text to display for
     * this code, such as "India Ink Prep CSF" */
    private String displayText;
    /** True iff this code has been retired */
    private boolean retired;
    /** True iff this code should always be reported 
     *  assuming the code maps to one and only one condition. */
    private boolean reportAll;
    /** The LOINC scale type for this code, if this is a LOINC code */
    private String scaleType;

    public Code() 
    {
    }

    public Code(String code, CodeSystem codeSystem) 
    {    
        this.code       = code;
        this.codeSystem = codeSystem;        
    }

    public Code(Code src) 
    {    
        this.id             = src.id;
        this.codeType       = src.codeType;
        this.codeSystem     = src.codeSystem;
        this.code           = src.code;
        this.displayText    = src.displayText;
        this.retired        = src.retired;
        this.reportAll      = src.reportAll;
        this.scaleType      = src.scaleType;
    }

    public int hashCode() 
    {    
        int hash    = 7;
        hash        = 31 * hash + code.hashCode();
        hash        = 31 * hash + codeSystem.hashCode();
        
        return hash;
    }
    
    public boolean equals(Object o) 
    {    
        if (!(o instanceof Code)) 
        {    
            return false;
        }
        
        Code that = (Code) o;
        
        return this.code.equals(that.code) && this.codeSystem.equals(that.codeSystem);
    }

    /**
     * @return the id
     */
    public Long getId() 
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) 
    {
        this.id = id;
    }

    /**
     * @return the codeType
     */
    public CodeType getCodeType() 
    {
        return codeType;
    }

    /**
     * @param codeType the codeType to set
     */
    public void setCodeType(CodeType codeType) 
    {
        this.codeType = codeType;
    }

    /**
     * @return the codeSystem
     */
    public CodeSystem getCodeSystem() 
    {
        return codeSystem;
    }

    /**
     * @param codeSystem the codeSystem to set
     */
    public void setCodeSystem(CodeSystem codeSystem) 
    {
        this.codeSystem = codeSystem;
    }

    /**
     * @return the code
     */
    public String getCode() 
    {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) 
    {
        this.code = code;
    }

    /**
     * @return the displayText
     */
    public String getDisplayText() 
    {
        return displayText;
    }

    /**
     * @param displayText the displayText to set
     */
    public void setDisplayText(String displayText) 
    {
        this.displayText = displayText;
    }

    /**
     * @return the retired
     */
    public boolean isRetired() 
    {
        return retired;
    }

    /**
     * @param retired the retired to set
     */
    public void setRetired(boolean retired) 
    {
        this.retired = retired;
    }
    
    /**
     * @return the reportall
     */
    public boolean isReportAll() 
    {
    	return reportAll;
    }
    
    /**
     * 
     * @param reportAll the reportall to set.
     */
    public void setReportAll(boolean reportAll) 
    {
    	this.reportAll = reportAll;
    }

	public String getScaleType() 
	{
		return scaleType;
	}

	public void setScaleType(String scaleType) 
	{
		this.scaleType = scaleType;
	}
}

package org.openmrs.module.ncd.database;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.database.filter.ListPageRow;
import org.openmrs.module.ncd.model.Zvx;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

/**
 * A processing error recorded by the NCD description processor, including the
 * HL7 description that triggered the error.
 */
public class Error implements ListPageRow, java.io.Serializable {

    private static final long serialVersionUID = 7860158337725175174L;

    /** The synthetic primary key */
    private Long id;
    /** The HL7 message that triggered the error */
    private RawMessage rawMessage;
    /** The severity level of the error */
    private String level;
    /** The human readable text description of the error */
    private String description;
    /** Additional human readable text information about the error */
    private String additionalInfo;
    /** Date/time of last error while attempting to process or reprocess the message */
    private Date lastErrorDate;
    /** MPQ sequence number extracted from the raw message text */
    private String mpqSeqNumber;
    /** True iff this alert has been hidden by a user */
    private boolean hidden;

    public Error() {
    	this.lastErrorDate = new Date();
    	this.hidden = false;
    }

    public Error(RawMessage rawMessage, String level, String description) {
        this.rawMessage = rawMessage;
        initMpqSeqNumber();
        this.level = level;
        this.description = description;
        this.lastErrorDate = new Date();
    	this.hidden = false;
    }

    public Error(RawMessage rawMessage, String level, String description,
            String additionalInfo) {
        this.rawMessage = rawMessage;
        initMpqSeqNumber();
        this.level = level;
        this.description = description;
        this.additionalInfo = additionalInfo;
        this.lastErrorDate = new Date();
    	this.hidden = false;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RawMessage getRawMessage() {
        return this.rawMessage;
    }

    public void setRawMessage(RawMessage rawMessage) {
        this.rawMessage = rawMessage;
        initMpqSeqNumber();
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdditionalInfo() {
        return this.additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

	public Date getLastErrorDate() {
		return lastErrorDate;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * Setter for lastErrorDate.  lastErrorDate is set to the system timestamp when
	 * ever an Error is constructed, so it is only necessary to call this method
	 * when updating an existing Error with a new timestamp.
	 * 
	 * @param lastErrorDate
	 */
	public void setLastErrorDate(Date lastErrorDate) {
		this.lastErrorDate = lastErrorDate;
	}

	public String getMpqSeqNumber() {
		return mpqSeqNumber;
	}

	/**
	 * Setter for mpqSeqNumber.  Provided for completeness, however, mpqSeqNumber is
	 * initialized from the raw message when ever the raw message is set, so it shouldn't
	 * be necessary to call this directly.
	 * 
	 * @param mpqSeqNumber
	 */
	public void setMpqSeqNumber(String mpqSeqNumber) {
		this.mpqSeqNumber = mpqSeqNumber;
	}

	/**
     * @return the additional information lines formatted for display in an html alert box
     * 
     */
    public String getAlertFormattedAdditionalInfo() {
    	if (StringUtils.isEmpty(additionalInfo)) {
    		return null;
    	}

    	// The formatted string cannot contain ", because that is the ONCLICK= value delimiter.
    	// The formatted string cannot contain ', because that is parameter value delimiter in a Javascript function call.
    	// Escape \r and \n in the formatted string.
    	//
    	// So, convert " to &quot;
    	// Convert ' to &rsquo;
    	// Convert \r to \\r
    	// Convert \n to \\n
    	String temp = new String(additionalInfo);
    	temp = temp.replace("\"", "&quot;");
    	temp = temp.replace("'", "&rsquo;");
    	temp = temp.replace("\r", "\\r");
    	temp = temp.replace("\n", "\\n");
    	return temp;
    }
    
    /**
     * @return the additional information lines formatted for display as web page text
     * 
     */
    public String getWebFormattedAdditionalInfo() {
    	if (StringUtils.isEmpty(additionalInfo)) {
    		return null;
    	}

    	// Convert \r\n to <br>
    	// Convert \r to <br>
    	// Convert \n to <br>
    	String temp = new String(additionalInfo);
    	temp = temp.replace("\r\n", "<br>");
    	temp = temp.replace("\r", "<br>");
    	temp = temp.replace("\n", "<br>");
    	return temp;
    }
    
    protected void initMpqSeqNumber() {
    	String mpqSeqNumber = Zvx.getMpq(rawMessage.getMessageText());
    	if (mpqSeqNumber == null) {
    		setMpqSeqNumber("not found");
    	}
    	else {
    		setMpqSeqNumber(mpqSeqNumber);
    	}
    }
}

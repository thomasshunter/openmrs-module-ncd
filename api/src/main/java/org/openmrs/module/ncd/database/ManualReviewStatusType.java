package org.openmrs.module.ncd.database;

/**
 * A possible value of the "Manual Review Status" field of a reportable
 * result.
 */
public class ManualReviewStatusType implements java.io.Serializable {

	private static final long serialVersionUID = 6713324936921212627L;
	/** The synthetic primary key */
    private int id;
    /** The short display name for the status, such as "Hold for Review" */
    private String reviewStatus;
    /** A longer human readable description of the meaning of this status */
    private String description;
    
    public ManualReviewStatusType() {
        
    }
    
    public ManualReviewStatusType(int id, String reviewStatus, String description) {
        
        this.id = id;
        this.reviewStatus = reviewStatus;
        this.description = description;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the reviewStatus
     */
    public String getReviewStatus() {
        return reviewStatus;
    }

    /**
     * @param reviewStatus the reviewStatus to set
     */
    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
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

package org.openmrs.module.ncd.database;

/**
 * A type of NCDAlert, such as "Message Flow"
 */
public class AlertType implements java.io.Serializable {

    private static final long serialVersionUID = 980421637307979809L;
    /** The synthetic primary key */
    private int id;
    /** A short human readable name for the alert type, for display on
     * the dashboard */
    private String alertType;
    /** A longer, more detailed description of the alert type */
    private String description;
    
    public AlertType() {
    }
    
    public AlertType(int id, String alertType, String description) {
        
        this.id = id;
        this.alertType = alertType;
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
     * @return the alertType
     */
    public String getAlertType() {
        return alertType;
    }

    /**
     * @param alertType the alertType to set
     */
    public void setAlertType(String alertType) {
        this.alertType = alertType;
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

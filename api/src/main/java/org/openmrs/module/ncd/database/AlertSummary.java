package org.openmrs.module.ncd.database;

import java.util.Date;

import org.openmrs.User;
import org.openmrs.module.ncd.database.filter.ListPageRow;

/**
 * An alert summary - a signal from the NCD that something out of the ordinary has
 * occurred which a human should pay attention to. Alert summaries are displayed on
 * the Dashboard page, and managed on the Manage Alerts page.
 * 
 * Multiple occurrences of the same alert (i.e. with the same identity),
 * are summarized into a single row in the table with first and last date
 * of occurrence, and a count of occurrences. 
 */
public class AlertSummary implements java.io.Serializable, ListPageRow {

	private static final long serialVersionUID = -4697644411643452801L;

	/** The synthetic primary key */
    private Long id;
    /** The type of this alert, for example "Message Rate" */
    private AlertType alertType;
    /** An arbitrary unique string identifier that identifies the alert,
     * so that subsequent occurrences can be condensed and counted in the same 
     * database row. For example, all Message Flow alerts for the same application,
     * location and code will share the same identity. */
    private String identity;
    /** The date and time the alert condition was initially detected */
    private Date firstDate;
    /** The date and time the alert condition was most recently detected */
    private Date lastDate;
    /** The number of times the alert with this identity has occurred */
    int occurrences;
    /** A simple one-line summary of the alert for display on the dashboard */
    private String summary;
    /** A detailed description of the alert */
    private String details;
    /** An optional sending application from an HL7 message, for filtering */
    private String sendingApplication;
    /** An optional sending facility from an HL7 message, for filtering */
    private String sendingFacility;
    /** An optional sending location from an HL7 message, for filtering */
    private String sendingLocation;
    /** An optional reportable result code, for filtering */
    private String code;
    /** True iff this alert has been dismissed by a user or the system */
    private boolean dismissed;
    /** If dismissed, the OpenMRS user who dismissed the alert */
    private User dismissedUser;
    /** If dismissed, the date and time at which the alert was dismissed */
    private Date dismissedDate;
    /** If dismissed, an optional reason for dismissal */
    private String dismissedReason;
    /** If dismissed, the name of the OpenMRS user who dismissed the alert */
    private String displayDismissedUserName;
    
    public AlertSummary() {
    }

	public AlertSummary(AlertType alertType, String summary, String details, String identity) {
		this.alertType = alertType;
		this.firstDate = this.lastDate = new Date();
		this.occurrences = 1;
		this.dismissed = false;
		this.summary = summary;
		this.details = details;
		this.identity = identity;
	}
	
	public AlertSummary(AlertType alertType, String summary, String details, String identity, String sendingApplication, String sendingFacility, String sendingLocation, String code) {
		this.alertType = alertType;
		this.firstDate = this.lastDate = new Date();
		this.occurrences = 1;
		this.dismissed = false;
		this.summary = summary;
		this.details = details;
		this.identity = identity;
		this.sendingApplication = sendingApplication;
		this.sendingFacility = sendingFacility;
		this.sendingLocation = sendingLocation;
		this.code = code;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AlertType getAlertType() {
		return alertType;
	}

	public void setAlertType(AlertType alertType) {
		this.alertType = alertType;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public Date getFirstDate() {
		return firstDate;
	}

	public void setFirstDate(Date firstDate) {
		this.firstDate = firstDate;
	}

	public Date getLastDate() {
		return lastDate;
	}

	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

	public int getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(int occurrences) {
		this.occurrences = occurrences;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getSendingApplication() {
		return sendingApplication;
	}

	public void setSendingApplication(String sendingApplication) {
		this.sendingApplication = sendingApplication;
	}

	public String getSendingFacility() {
		return sendingFacility;
	}

	public void setSendingFacility(String sendingFacility) {
		this.sendingFacility = sendingFacility;
	}

	public String getSendingLocation() {
		return sendingLocation;
	}

	public void setSendingLocation(String sendingLocation) {
		this.sendingLocation = sendingLocation;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isDismissed() {
		return dismissed;
	}

	public void setDismissed(boolean dismissed) {
		this.dismissed = dismissed;
	}

	public User getDismissedUser() {
		return dismissedUser;
	}

	public void setDismissedUser(User dismissedUser) {
		this.dismissedUser = dismissedUser;
	}

	public Date getDismissedDate() {
		return dismissedDate;
	}

	public void setDismissedDate(Date dismissedDate) {
		this.dismissedDate = dismissedDate;
	}

	public String getDismissedReason() {
		return dismissedReason;
	}

	public void setDismissedReason(String dismissedReason) {
		this.dismissedReason = dismissedReason;
	}

	public String getDisplayDismissedUserName() {
		return displayDismissedUserName;
	}

	public void setDisplayDismissedUserName(String displayDismissedUserName) {
		this.displayDismissedUserName = displayDismissedUserName;
	}
}

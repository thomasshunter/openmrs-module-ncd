package org.openmrs.module.ncd.database;

import java.util.Date;

public class ProcessedMessageCount implements java.io.Serializable {

	private static final long serialVersionUID = 4759212262242773394L;

	private long id;
	private String application;
	private String facility;
	private String location;
	// note: the date/time this message was *processed*, not the testrcvddatetime
	// from the reportable result, truncated to an hour boundary.
	private Date processedDateTime;
	/** The number of messages processed in this hour bucket with matching
	 * application, facility and location. */
	private long messageCount;
	/** The MPQ sequence number of the last message in this bucket */
	private String lastMpqSeqNumber;
	
	public ProcessedMessageCount() {
	}
	
	public String toString() {
		
		return "ProcessedMessageCount(" +
					"id=" + id +
					", application=" + application +
					", facility=" + facility +
					", location=" + location +
					", processedDateTime=" + processedDateTime +
					", messageCount=" + messageCount +
					", lastMpqSeqNumber=" + lastMpqSeqNumber +
			   ")";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getFacility() {
		return facility;
	}

	public void setFacility(String facility) {
		this.facility = facility;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getProcessedDateTime() {
		return processedDateTime;
	}

	public void setProcessedDateTime(Date receivedDateTime) {
		this.processedDateTime = receivedDateTime;
	}

	public long getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(long messageCount) {
		this.messageCount = messageCount;
	}

	public void setLastMpqSeqNumber(String lastMpqSeqNumber) {
		this.lastMpqSeqNumber = lastMpqSeqNumber;
	}

	public String getLastMpqSeqNumber() {
		return lastMpqSeqNumber;
	}
}

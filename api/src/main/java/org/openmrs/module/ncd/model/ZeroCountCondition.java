package org.openmrs.module.ncd.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.ncd.database.HL7Producer;

public class ZeroCountCondition implements java.io.Serializable {

	private static final long serialVersionUID = -3924679332770803997L;
	private String application;
	private String facility;
	private String location;
	private HL7Producer producer;
	private String conditionName;
	private Date dateLastReceived;
	private String lastMpqSeqNumber;
	
	public ZeroCountCondition() {
	}
	
	public String toString() {
		
		return "ZeroCountCondition(" +
					"application=" + application +
					", facility=" + facility +
					", location=" + location +
					", producer=" + producer +
					", conditionName=" + conditionName +
					", dateLastReceived=" + dateLastReceived +
					", lastMpqSeqNumber=" + lastMpqSeqNumber +
			   ")";
	}

	public static String getColumnNames() {
		
		return "APPLICATION,FACILITY,LOCATION,LOCATION_DESCRIPTION,INSTITUTION,CONDITION,DATELASTRECEIVED,LASTMPQSEQNUM";
	}
	
	public Map<String, Object> toMap() {
		
		String locationDescription = "";
		if (producer != null) {
			locationDescription = producer.getDescription();
		}
		String institutionName = "";
		if (producer != null && producer.getInstitution() != null) {
			institutionName = producer.getInstitution().getName();
		}
		
		Map<String, Object> out = new HashMap<String, Object>();
		out.put("APPLICATION", application);
		out.put("FACILITY", facility);
		out.put("LOCATION", location);
		out.put("LOCATION_DESCRIPTION", locationDescription);
		out.put("INSTITUTION", institutionName);
		out.put("CONDITION", conditionName);
		out.put("DATELASTRECEIVED", dateLastReceived);
		out.put("LASTMPQSEQNUM", lastMpqSeqNumber);
		return out;
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

	public HL7Producer getProducer() {
		return producer;
	}

	public void setProducer(HL7Producer producer) {
		this.producer = producer;
	}

	public String getConditionName() {
		return conditionName;
	}

	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}

	public Date getDateLastReceived() {
		return dateLastReceived;
	}

	public void setDateLastReceived(Date dateLastReceived) {
		this.dateLastReceived = dateLastReceived;
	}

	public String getLastMpqSeqNumber() {
		return lastMpqSeqNumber;
	}

	public void setLastMpqSeqNumber(String lastMpqSeqNumber) {
		this.lastMpqSeqNumber = lastMpqSeqNumber;
	}
}

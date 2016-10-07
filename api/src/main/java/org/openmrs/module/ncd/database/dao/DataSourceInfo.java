package org.openmrs.module.ncd.database.dao;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.utilities.StringUtilities;

public class DataSourceInfo implements java.io.Serializable {

	private static final long serialVersionUID = 8792725229200834248L;
	
	private String application;
	private String facility;
	private String location;
	private HL7Producer producer;
	private long resultCount;
	private long messageCount;
	
    public DataSourceInfo() {
    }

    public DataSourceInfo(String application, String facility, String location,
    					  HL7Producer producer)
    {
    	this.application = application;
    	this.facility = facility;
    	this.location = location;
    	this.producer = producer;
    }

	public boolean equals(Object o) {

		if (!(o instanceof DataSourceInfo)) {
			return false;
		}
		
		DataSourceInfo that = (DataSourceInfo) o;
		
		return this.application.equals(that.application) &&
		       this.facility.equals(that.facility) &&
		       StringUtilities.compareTo(this.location, that.location) == 0;
	}
	
	public int hashCode() {
		
		return 37 * this.application.hashCode() + this.facility.hashCode();
	}
	
    public String toString() {

		return "DataSourceInfo(" +
					"application=" + application +
					", facility=" + facility +
					", location=" + location +
					", producer=" + producer +
					", resultCount=" + resultCount +
					", messageCount=" + messageCount +
			   ")";
	}

	static public String getColumnNames() {
		
		return "APPLICATION,FACILITY,LOCATION,LOCATION_DESCRIPTION," +
			   "RESULT_COUNT,MESSAGE_COUNT";
	}

	public Map<String, Object> toMap() {
		
		String locationDescription = "";
		if (producer != null) {
			locationDescription = producer.getDescription();
		}
		
		Map<String, Object> out = new HashMap<String, Object>();
		out.put("APPLICATION", application);
		out.put("FACILITY", facility);
		out.put("LOCATION", location);
		out.put("LOCATION_DESCRIPTION", locationDescription);
		out.put("RESULT_COUNT", (Long) resultCount);
		out.put("MESSAGE_COUNT", (Long) messageCount);
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

	public long getResultCount() {
		return resultCount;
	}

	public void setResultCount(long resultCount) {
		this.resultCount = resultCount;
	}

	public long getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(long messageCount) {
		this.messageCount = messageCount;
	}
}

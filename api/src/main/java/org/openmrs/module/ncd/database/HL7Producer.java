package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;

import org.openmrs.module.ncd.database.filter.ListPageRow;

/**
 * A producer of HL7 messages, based on the sending application, sending
 * facility and sending location fields of the HL7 message.
 * 
 * A null value for the locationname field indicates that the sending location
 * field of HL7 messages with matching sending application and sending facility
 * fields should be ignored when determining the HL7Producer for a given
 * message, since some abstract HL7Producers put non-location values in the
 * sending location field.
 * 
 * HL7Producers are grouped by the Institution that "owns" them.
 * 
 * HL7Producers can be marked as reportall, in which case all messages
 * from the producer will be reported, even if no specific condition can be
 * identified.
 * 
 * HL7Producers can be marked excluded, in which case all messages from
 * the producer will be completely ignored.
 * 
 * The behavior of the message processor is not specified if both the
 * reportall and excluded flags are set to true.
 * 
 * This table is normally *not* fully populated. It usually contains only
 * instances for producers that require special processing (the reportall and
 * excluded attributes). Messages for producers that don't appear in this table
 * are processed as if reportall=false and excluded=false.
 */
public class HL7Producer implements ListPageRow, java.io.Serializable {

    private static final long serialVersionUID = -6816907547530450146L;

    /** The synthetic primary key */
    private Long id;
    /** An optional human readable description */
    private String description;
    /** The parent Institution */
    private Institution institution;
    /** The HL7 sending application that defines this producer (AK1.1) */
    private String applicationname;
    /** The HL7 sending facility that defines this producer (AK1.2) */
    private String facilityname;
    /** The HL7 sending location that defines this producer (AK1.3) */
    private String locationname;
    /** True if all messages from this producer should be reported, false to
     * apply the normal critic processing */
    private boolean reportall;
    /** True if messages from this producer should be excluded from reports. */
    private boolean excluded;
    /** True if and only if this producer has been retired - i.e., existed
     * once in the past, but should not be used for processing new messages. */
    private boolean retired;
    /** True if the message processor should not process messages from this
     * producer, false to apply the normal critic processing. */
    private boolean ignored;
    /** The set of reportable results from this producer */
    private Set<ReportableResult> reportableResults = new HashSet<ReportableResult>(
            0);

    public HL7Producer() {
    }

    public HL7Producer(String description, Institution institution,
            String applicationname, String facilityname, String locationname,
            boolean reportall, boolean excluded, boolean retired,
            boolean ignored)
    {
    	this.description = description;
        this.institution = institution;
        this.applicationname = applicationname;
        this.facilityname = facilityname;
        this.locationname = locationname;
        this.reportall = reportall;
        this.excluded = excluded;
        this.retired = retired;
        this.ignored = ignored;
    }

    public HL7Producer(String description, Institution institution,
            String applicationname, String facilityname, String locationname,
            boolean reportall, boolean excluded, boolean retired,
            boolean ignored, Set<ReportableResult> tblreportableresults)
    {
    	this.description = description;
        this.institution = institution;
        this.applicationname = applicationname;
        this.facilityname = facilityname;
        this.locationname = locationname;
        this.reportall = reportall;
        this.excluded = excluded;
        this.retired = retired;
        this.ignored = ignored;
        this.reportableResults = tblreportableresults;
    }

    public HL7Producer(HL7Producer src) {

    	this.id = src.id;
    	this.description = src.description;
        this.institution = src.institution;
        this.applicationname = src.applicationname;
        this.facilityname = src.facilityname;
        this.locationname = src.locationname;
        this.reportall = src.reportall;
        this.excluded = src.excluded;
        this.retired = src.retired;
        this.ignored = src.ignored;
        this.reportableResults = new HashSet<ReportableResult>();
        this.reportableResults.addAll(src.reportableResults);
    }
    
    public String toString() {

    	return "HL7Producer(" +
    				"description=" + description +
    				", institution=" + institution +
    				", applicationname=" + applicationname +
    				", facilityname=" + facilityname +
    				", locationname=" + locationname +
    				", reportall=" + reportall +
    				", excluded=" + excluded +
    				", retired=" + retired +
    				", ignored=" + ignored +
    		   ")";
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public Institution getInstitution() {
        return this.institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getApplicationname() {
        return this.applicationname;
    }

    public void setApplicationname(String applicationname) {
        this.applicationname = applicationname;
    }

    public String getFacilityname() {
        return this.facilityname;
    }

    public void setFacilityname(String facilityname) {
        this.facilityname = facilityname;
    }

	public String getLocationname() {
		return locationname;
	}

	public void setLocationname(String locationname) {
		this.locationname = locationname;
	}

    public boolean isReportall() {
        return this.reportall;
    }

    public void setReportall(boolean reportall) {
        this.reportall = reportall;
    }

    public boolean isExcluded() {
        return this.excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public Set<ReportableResult> getReportableResults() {
        return this.reportableResults;
    }

    public void setReportableResults(
            Set<ReportableResult> reportableResults) {
        this.reportableResults = reportableResults;
    }

	public boolean isRetired() {
		return retired;
	}

	public void setRetired(boolean retired) {
		this.retired = retired;
	}

	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}

	public boolean isIgnored() {
		return ignored;
	}
}

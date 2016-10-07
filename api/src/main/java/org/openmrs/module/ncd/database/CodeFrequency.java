package org.openmrs.module.ncd.database;

import java.util.Date;

/**
 * Code frequency data from incoming messages, sliced by application,
 * facility, patient, institute and doctor zip code, and date.
 * 
 * NOTE: it is possible to have multiple rows in the database for the same
 * code, application facility, patient zip, institute zip, doctor zip and
 * date. 
 */
public class CodeFrequency implements java.io.Serializable {

    private static final long serialVersionUID = 4929700007158715531L;

    /** The synthetic primary key */
    private Long id;
    /** The sending application from the HL7 message (AK1) */
    private String application;
    /** The sending facility from the HL7 message (AK1) */
    private String facility;
    /** The sending location from the HL7 message */
    private String location;
    /** The abstract code from the HL7 message (AK1) */
    private String code;
    /** The code system from the HL7 message (AK1) */
    private String codeSystem;
    /** The patient zip code from the HL7 message (AK1) */
    private String patientZipCode;
    /** The institute zip code from the HL7 message (AK1) */
    private String instituteZipCode;
    /** The provider zip code from the HL7 message (AK1) */
    private String doctorZipCode;
    /** The date on which the HL7 message was processed (AK1) */
    private Date date;
    /** The number of matching message instances */
    private long count;

    public CodeFrequency() {
    }

    public CodeFrequency(String application, String facility,
            String location, String code, String codeSystem, Date date, long count) {
        this.application = application;
        this.facility = facility;
        this.location = location;
        this.code = code;
        this.codeSystem = codeSystem;
        this.date = date;
        this.count = count;
    }

    public CodeFrequency(String application, String facility,
            String location, String code, String codeSystem,
            String patientZipCode, String instituteZipCode, 
            String doctorZipCode, Date date, long count) {
        this.application = application;
        this.facility = facility;
        this.location = location;
        this.code = code;
        this.codeSystem = codeSystem;
        this.patientZipCode = patientZipCode;
        this.instituteZipCode = instituteZipCode;
        this.doctorZipCode = doctorZipCode;
        this.date = date;
        this.count = count;
    }
    
    public CodeFrequency(CodeFrequency toClone)
    {
        if (toClone != null)
        {
            this.application = toClone.getApplication();
            this.facility = toClone.getFacility();
            this.location = toClone.getLocation();
            this.code = toClone.getCode();
            this.codeSystem = toClone.getCodeSystem();
            this.patientZipCode = toClone.getPatientZipCode();
            this.instituteZipCode = toClone.getInstituteZipCode();
            this.doctorZipCode = toClone.getDoctorZipCode();
            this.date = toClone.getDate();
            this.count = toClone.getCount();
        }
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    /**
     * @return the codeSystem
     */
    public String getCodeSystem() {
        return codeSystem;
    }

    /**
     * @param codeSystem the codeSystem to set
     */
    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
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

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

    public void setCode(String code) {
        this.code = code;
    }

    public String getPatientZipCode() {
        return this.patientZipCode;
    }

    public void setPatientZipCode(String patientZipCode) {
        this.patientZipCode = patientZipCode;
    }

    public String getInstituteZipCode() {
        return this.instituteZipCode;
    }

    public void setInstituteZipCode(String instituteZipCode) {
        this.instituteZipCode = instituteZipCode;
    }

    public String getDoctorZipCode() {
        return this.doctorZipCode;
    }

    public void setDoctorZipCode(String doctorZipCode) {
        this.doctorZipCode = doctorZipCode;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

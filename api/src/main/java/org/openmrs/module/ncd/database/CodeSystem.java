package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;

/**
 * A system of abstract codes defined by a standard, such as LOINC or
 * ICD10.
 */
public class CodeSystem implements java.io.Serializable {

    private static final long serialVersionUID = 4892895369043989437L;

    /** The synthetic primary key */
    private Long id;
    /** The name of this code system, such as "LOINC" or "ICD10" */
    private String name;
    /** True iff this code system has been retired. */
    private boolean retired;
    /** The set of reporting criteria related to this code system. */
    private Set<ReportingCriterion> reportingCriteria = new HashSet<ReportingCriterion>(
            0);

    public CodeSystem() {
    }

    public CodeSystem(Long id, String codesystem) {
        this.id = id;
        this.name = codesystem;
    }

    public CodeSystem(CodeSystem src) {
        this.id = src.id;
        this.name = src.name;
        this.retired = src.retired;
        this.reportingCriteria = new HashSet<ReportingCriterion>();
        this.reportingCriteria.addAll(src.reportingCriteria);
    }

    public CodeSystem(Long id, String codesystem,
            Set<ReportingCriterion> tblreportingcriterias) {
        this.id = id;
        this.name = codesystem;
        this.reportingCriteria = tblreportingcriterias;
    }

    public int hashCode() {
        
        return name.hashCode();
    }
    
    public boolean equals(Object o) {
        
        if (!(o instanceof Code)) {
            
            return false;
        }
        
        CodeSystem that = (CodeSystem) o;
        return this.name.equals(that.name);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ReportingCriterion> getReportingCriteria() {
        return this.reportingCriteria;
    }

    public void setReportingCriteria(
            Set<ReportingCriterion> tblreportingcriterias) {
        this.reportingCriteria = tblreportingcriterias;
    }

	public boolean isRetired() {
		return retired;
	}

	public void setRetired(boolean retired) {
		this.retired = retired;
	}
}

/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao;


import java.util.Date;

import org.openmrs.module.ncd.database.CodeFrequency;
import org.openmrs.module.ncd.storage.CodeFrequencyStorageException;


/**
 * DAO interface for the loinc frequency table (CodeFrequency)
 * 
 * @author jlbrown
 * 
 */
public interface ICodeFrequencyDAO
{
    /**
     * Increment the frequency of an abstract code for the particular date and
     * institution.
     * 
     * @param date
     *            The date the loinc code was reported.
     * @param application
     *            The application id string from the HL7 message containing
     *            the reported loinc code.
     * @param facility
     *            The facility id string from the HL7 message containing
     *            the reported loinc code.
     * @param location
     * 			  The location id string from the HL7 message containing
     * 			  the reported loinc code.
     * @param code
     *            The code reported.
     * @param codeSystem
     *            The code system for the code reported.
     * @param patientZipCode
     *            The zipcode of the patient.
     * @param instituteZipCode
     *            The zipcode of the institute.
     * @param doctorZipCode
     *            The zipcode of the doctor.
     */
    public void incrementCodeFrequency(Date date, String application,
            String facility, String location, String code, String codeSystem, 
            String patientZipCode, String instituteZipCode, 
            String doctorZipCode)
            throws CodeFrequencyStorageException;

    /**
     * Find the code frequency row that corresponds to passed in date,
     * institute, and abstract code.
     * 
     * @param date
     *            The date the loinc code was reported.
     * @param application
     *            The application id string from the HL7 message containing
     *            the reported loinc code.
     * @param facility
     *            The facility id string from the HL7 message containing
     *            the reported loinc code.
     * @param location
     * 			  The location id string from the HL7 message containing
     * 			  the reported loinc code.
     * @param code
     *            The code reported.
     * @param codeSystem
     *            The code system for the code reported.
     * @param patientZipCode
     *            The zipcode of the patient.
     * @param instituteZipCode
     *            The zipcode of the institute.
     * @param doctorZipCode
     *            The zipcode of the doctor.
     * @return The loinc frequency row that corresponds to the date, institute,
     *         and loinc code.
     */
    public CodeFrequency findCodeFrequency(Date date,
            String application, String facility, String location,
            String code, String codeSystem,
            String patientZipCode, String instituteZipCode, 
            String doctorZipCode);

    /**
     * Stores any cached code frequency data.
     */
    public void saveCodeFrequencyMap() throws CodeFrequencyStorageException;
}

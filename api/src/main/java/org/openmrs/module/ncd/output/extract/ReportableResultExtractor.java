package org.openmrs.module.ncd.output.extract;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.Institution;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.utilities.NCDConcepts;
import org.openmrs.module.ncd.utilities.PersonAttributeTypeCache;

/**
 * A class to convert from ReportableResult to an "extract map".
 * 
 * TODO: to avoid breaking ExtractDAO at this late date, that class
 * has not been refactored to use this new common class.
 * 
 * NOTE: This class should only be used from within a hibernate session,
 * due to all the references to objects that may not have been fetched.
 * 
 * @author Erik Horstkotte
 */
public class ReportableResultExtractor {

    // ********************
    // One-time-init things
    // ********************

    /** The cached NCD concept RESULT_SEQUENCE_NUMBER */
    private static Concept resultSequenceConcept = null;
    /** The cached NCD concept CONDITION_NAME */
    private static Concept conditionNameConcept = null;
    /** The cached NCD concept INSTITUTION_NAME */
    private static Concept institutionNameConcept = null;
    /** The cached NCD concept RAW_HL7_ID */
    private static Concept rawHL7IdConcept = null;
    /** A map from NCD concept names to extract column names */
    private static Map<String,String> columnNameByConceptName = null;
    /** A map from patient attribute names to extract column names */
    private static Map<String,String> columnNameByPatientAttrName = null;
    /** A map from provider attribute names to extract column names */
    private static Map<String,String> columnNameByProviderAttrName = null;
    
    // ********************
    // Members only for convenience
    // ********************

    /** The row being constructed */
    private Map<String, Object> row = null;
    
    public ReportableResultExtractor(Map<String,String> properties) {
        
        init(properties);
        row = new HashMap<String, Object>();
    }
    
    public Map<String, Object> extract(ReportableResult reportableResult) {

        row.clear();
        extractReportableResult(reportableResult);
        extractRawHL7(reportableResult.getRawMessage());
        return row;
    }
    
    /** One-time initialization method */
    private void init(Map<String,String> properties) {
        
        // Break out the properties
        ConceptService conceptService = Context.getConceptService();
        
        if (resultSequenceConcept == null) {
            
            resultSequenceConcept = 
                conceptService.getConceptByName(NCDConcepts.RESULT_SEQUENCE_NUMBER);
        }
        
        if (conditionNameConcept == null) {
            
            conditionNameConcept = 
                conceptService.getConceptByName(NCDConcepts.CONDITION_NAME);
        }
        
        if (institutionNameConcept == null) {
            
            institutionNameConcept = 
                conceptService.getConceptByName(NCDConcepts.INSTITUTION_NAME);
        }

        if (rawHL7IdConcept == null) {
            
            rawHL7IdConcept = 
                conceptService.getConceptByName(NCDConcepts.RAW_HL7_ID);
        }

        // TODO: the commented-out rows appear in the extract, but not in the obs rows. Add concepts for them, even if they're never currently recorded.
        
        // TODO: Is there some way to encode this mapping as Concept attributes?
        if (columnNameByConceptName == null) {
            
            columnNameByConceptName = new HashMap<String,String>();
            
            columnNameByConceptName.put(NCDConcepts.APPLICATION,
                    DataFeedExtractorFactory.COLUMN_APPLICATION);
            columnNameByConceptName.put(NCDConcepts.CONDITION_NAME,
                    DataFeedExtractorFactory.COLUMN_DWYER_CONDITION_NAME);
            columnNameByConceptName.put(NCDConcepts.FACILITY,
                    DataFeedExtractorFactory.COLUMN_FACILITY);
            columnNameByConceptName.put(NCDConcepts.HEALTH_DEPT_AGENCY,
                    DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_AGENCY);
            columnNameByConceptName.put(NCDConcepts.HEALTH_DEPT_CASE_ID,
                    DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_CASE_ID);
            columnNameByConceptName.put(NCDConcepts.INSTITUTION_ID_TYPE,
                    DataFeedExtractorFactory.COLUMN_INSTITUTION_ID_TYPE);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_CITY,
                    DataFeedExtractorFactory.COLUMN_LAB_CITY);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_ID,
                    DataFeedExtractorFactory.COLUMN_LAB_IDENTIFIER);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_NAME,
                    DataFeedExtractorFactory.COLUMN_LAB_NAME);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_PHONE,
                    DataFeedExtractorFactory.COLUMN_LAB_PHONE);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_STATE,
                    DataFeedExtractorFactory.COLUMN_LAB_STATE);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_ADDRESS1,
                    DataFeedExtractorFactory.COLUMN_LAB_STREET1);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_ADDRESS2,
                    DataFeedExtractorFactory.COLUMN_LAB_STREET2);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_ZIP,
                    DataFeedExtractorFactory.COLUMN_LAB_ZIP);
            columnNameByConceptName.put(NCDConcepts.LOINC_CODE_ID,
                    DataFeedExtractorFactory.COLUMN_MAPPED_LOINC);
            columnNameByConceptName.put(NCDConcepts.OBR_ALT_CODE,
                    DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE);
            columnNameByConceptName.put(NCDConcepts.OBR_ALT_CODE_SYS,
                    DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_SYS);
            columnNameByConceptName.put(NCDConcepts.OBR_ALT_CODE_TEXT,
                    DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_TEXT);
            columnNameByConceptName.put(NCDConcepts.OBX_ALT_CODE,
                    DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE);
            columnNameByConceptName.put(NCDConcepts.OBX_ALT_CODE_SYS,
                    DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_SYS);
            columnNameByConceptName.put(NCDConcepts.OBX_ALT_CODE_TEXT,
                    DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_TEXT);
            columnNameByConceptName.put(NCDConcepts.INSTITUTION_NAME,
                    DataFeedExtractorFactory.COLUMN_SOURCE_INSTITUTION);
            columnNameByConceptName.put(NCDConcepts.TEST_ABNORMAL_FLAG,
                    DataFeedExtractorFactory.COLUMN_TEST_ABNORMAL_FLAG);
            columnNameByConceptName.put(NCDConcepts.TEST_CODE_SYSTEM,
                    DataFeedExtractorFactory.COLUMN_TEST_CODESYS);
            columnNameByConceptName.put(NCDConcepts.TEST_COMMENT,
                    DataFeedExtractorFactory.COLUMN_TEST_COMMENT);
            columnNameByConceptName.put(NCDConcepts.TEST_DATA_TYPE,
                    DataFeedExtractorFactory.COLUMN_TEST_DATA_TYPE);
            columnNameByConceptName.put(NCDConcepts.TEST_DATE,
                    DataFeedExtractorFactory.COLUMN_TEST_DATE);
            columnNameByConceptName.put(NCDConcepts.TEST_FILLER_ORDER_NUMBER,
                    DataFeedExtractorFactory.COLUMN_TEST_FILLER_ORDER_NUM);
            columnNameByConceptName.put(NCDConcepts.TEST_ID,
                    DataFeedExtractorFactory.COLUMN_TEST_IDENTIFIER);
            columnNameByConceptName.put(NCDConcepts.TEST_ID,
                    DataFeedExtractorFactory.COLUMN_TEST_LOINC_CODE);
            columnNameByConceptName.put(NCDConcepts.TEST_MPQ_SEQ_NUMBER,
                    DataFeedExtractorFactory.COLUMN_TEST_MPQ_SEQ_NUMBER);
            columnNameByConceptName.put(NCDConcepts.TEST_NAME,
                    DataFeedExtractorFactory.COLUMN_TEST_NAME);
            columnNameByConceptName.put(NCDConcepts.TEST_NORMAL_RANGE,
                    DataFeedExtractorFactory.COLUMN_TEST_NORMAL_RANGE);
            columnNameByConceptName.put(NCDConcepts.TEST_PARENT_FILLER,
                    DataFeedExtractorFactory.COLUMN_TEST_PARENT_FILLER);
            columnNameByConceptName.put(NCDConcepts.TEST_PARENT_PLACER,
                    DataFeedExtractorFactory.COLUMN_TEST_PARENT_PLACER);
            columnNameByConceptName.put(NCDConcepts.TEST_PLACER_ORDER_NUMBER,
                    DataFeedExtractorFactory.COLUMN_TEST_PLACER_ORDER_NUM);
            columnNameByConceptName.put(NCDConcepts.TEST_PREVIOUS_DATE,
                    DataFeedExtractorFactory.COLUMN_TEST_PREVIOUS_DATE);
            columnNameByConceptName.put(NCDConcepts.TEST_RECEIVED_DATE_TIME,
                    DataFeedExtractorFactory.COLUMN_TEST_RCVD_DATE_TIME);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_CODE,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODE);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_CODE_SYSTEM,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODESYS);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_ID,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_IDENTIFIER);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_CODE,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_LOINC_CODE);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_NAME,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_NAME);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_STATUS,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_STATUS);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_SUB_ID,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_SUBID);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_UNITS,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_UNITS);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_VALUE,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_VALUE);
            columnNameByConceptName.put(NCDConcepts.TEST_SPECIMEN_TEXT,
                    DataFeedExtractorFactory.COLUMN_TEST_SPECIMEN_TEXT);
            columnNameByConceptName.put(NCDConcepts.RESULT_SEQUENCE_NUMBER,
                    DataFeedExtractorFactory.COLUMN_UNIQUE_RECORD_NUM);
            columnNameByConceptName.put(NCDConcepts.UNIQUE_REGISTRY_NUM,
                    DataFeedExtractorFactory.COLUMN_UNIQUE_REGISTRY_NUM);
        }

        if (columnNameByPatientAttrName == null) {
            
            columnNameByPatientAttrName = new HashMap<String, String>();
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_GLOBAL_PATIENT_ID,
                    DataFeedExtractorFactory.COLUMN_PAT_GLOBAL_ID);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PATIENT_MEDREC_ID,
                    DataFeedExtractorFactory.COLUMN_PAT_INST_MED_REC_ID);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PHONE_NUMBER,
                    DataFeedExtractorFactory.COLUMN_PAT_PHONE);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_RACE,
                    DataFeedExtractorFactory.COLUMN_PAT_RACE);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_SSN,
                    DataFeedExtractorFactory.COLUMN_PAT_SOCSEC);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_HEALTH_DEPT_PATIENT_ID,
                    DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_PATIENT_ID);
        }
        
        if (columnNameByProviderAttrName == null) {
            
            columnNameByProviderAttrName = new HashMap<String, String>();
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_LOCAL_ID);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PHONE_NUMBER,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_PHONE);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_SSN,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_SSN);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_BIRTH,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_BIRTH);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_DEA_NUM,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_DEA_NUM);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_FAX,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_FAX);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_LICENSE,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_LICENSE);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_NAME_MATCHED,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_NAME_MATCHED);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_PRACTICE,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_PRACTICE);
        }
    }

    /** Extract export columns from the reportable result
     * @param reportableResult The reportable result to extract from. 
     */
    private void extractReportableResult(ReportableResult reportableResult) {

        HL7Producer appfac = reportableResult.getProducer();
        if (appfac != null) {
            addCol(DataFeedExtractorFactory.COLUMN_APPLICATION, appfac.getApplicationname());
            addCol(DataFeedExtractorFactory.COLUMN_FACILITY, appfac.getFacilityname());
        }

        Institution inst = reportableResult.getInstitution();
        if (inst != null) {
            addCol(DataFeedExtractorFactory.COLUMN_SOURCE_INSTITUTION, inst.getName());
        }
        
        if (reportableResult.getPatientBirth() != null) {
            addCol(DataFeedExtractorFactory.COLUMN_PAT_BIRTH, reportableResult.getPatientBirth());
        }

        if (reportableResult.getProviderBirth() != null) {
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_BIRTH, reportableResult.getProviderBirth());
        }
        
        if (reportableResult.getTestDate() != null) {
            addCol(DataFeedExtractorFactory.COLUMN_TEST_DATE, reportableResult.getTestDate());
        }
        
        Code testLoincCode = reportableResult.getCode();
        if (testLoincCode != null) {
            addCol(DataFeedExtractorFactory.COLUMN_TEST_LOINC_CODE, testLoincCode.getCode());
            addCol(DataFeedExtractorFactory.COLUMN_MAPPED_LOINC, testLoincCode.getId());
        }
        
        if (reportableResult.getMpqSeqNumber() != null) {
            addCol(DataFeedExtractorFactory.COLUMN_TEST_MPQ_SEQ_NUMBER, reportableResult.getMpqSeqNumber());
        }
        
        if (reportableResult.getTestPreviousDate() != null) {
            addCol(DataFeedExtractorFactory.COLUMN_TEST_PREVIOUS_DATE, reportableResult.getTestPreviousDate());
        }

        addCol(DataFeedExtractorFactory.COLUMN_UNIQUE_RECORD_NUM, reportableResult.getResultSeq().toString());
        addCol(DataFeedExtractorFactory.COLUMN_INSTITUTION_ID_TYPE, reportableResult.getInstitutionIdType());
        
        addCol(DataFeedExtractorFactory.COLUMN_PAT_INST_MED_REC_ID, reportableResult.getPatientInstitutionMedicalRecordId());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_GLOBAL_ID, reportableResult.getGlobalPatientId());
        //addCol(DataFeedExtractorFactory.COLUMN_UNIQUE_REGISTRY_NUM, ...);
        addCol(DataFeedExtractorFactory.COLUMN_PAT_SOCSEC, reportableResult.getPatientSSN());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_NAME, reportableResult.getPatientName());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_SEX, reportableResult.getPatientSex());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_RACE, reportableResult.getPatientRace());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_PHONE, reportableResult.getPatientPhone());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_STREET1, reportableResult.getPatientStreet1());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_STREET2, reportableResult.getPatientStreet2());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_CITY, reportableResult.getPatientCity());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_COUNTY, reportableResult.getPatientCounty());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_STATE, reportableResult.getPatientState());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_ZIP, reportableResult.getPatientZip());
        addCol(DataFeedExtractorFactory.COLUMN_PAT_COUNTRY, reportableResult.getPatientCountry());
        
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_NAME, reportableResult.getProviderName());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_NAME_MATCHED, reportableResult.getProviderNameMatched());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_SSN, reportableResult.getProviderSSN());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_PRACTICE, reportableResult.getProviderPractice());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_STREET, reportableResult.getProviderStreet());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_CITY, reportableResult.getProviderCity());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_STATE, reportableResult.getProviderState());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_ZIP, reportableResult.getProviderZip());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_COUNTY, reportableResult.getProviderCounty());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_PHONE, reportableResult.getProviderPhone());
        //addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_FAX, ...
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_LOCAL_ID, reportableResult.getProviderLocalId());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_DEA_NUM, reportableResult.getProviderDEANumber());
        addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_LICENSE, reportableResult.getProviderLicense());
        
        addCol(DataFeedExtractorFactory.COLUMN_LAB_NAME, reportableResult.getLabName());
        addCol(DataFeedExtractorFactory.COLUMN_LAB_IDENTIFIER, reportableResult.getLabId());
        addCol(DataFeedExtractorFactory.COLUMN_LAB_PHONE, reportableResult.getLabPhone());
        addCol(DataFeedExtractorFactory.COLUMN_LAB_STREET1, reportableResult.getLabStreet1());
        addCol(DataFeedExtractorFactory.COLUMN_LAB_STREET2, reportableResult.getLabStreet2());
        addCol(DataFeedExtractorFactory.COLUMN_LAB_CITY, reportableResult.getLabCity());
        addCol(DataFeedExtractorFactory.COLUMN_LAB_STATE, reportableResult.getLabState());
        addCol(DataFeedExtractorFactory.COLUMN_LAB_ZIP, reportableResult.getLabZip());
        
        addCol(DataFeedExtractorFactory.COLUMN_TEST_IDENTIFIER, reportableResult.getTestId());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_NAME, reportableResult.getTestName());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_CODESYS, reportableResult.getTestCodeSystem());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_PLACER_ORDER_NUM, reportableResult.getTestPlacerOrderNum());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_FILLER_ORDER_NUM, reportableResult.getTestFillerOrderNum());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_PARENT_PLACER, reportableResult.getTestParentPlacer());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_PARENT_FILLER, reportableResult.getTestParentFiller());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_SPECIMEN_TEXT, reportableResult.getTestSpecimenText());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_DATA_TYPE, reportableResult.getTestDataType());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_NORMAL_RANGE, reportableResult.getTestNormalRange());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_ABNORMAL_FLAG, reportableResult.getTestAbnormalFlag());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_COMMENT, reportableResult.getTestComment());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RCVD_DATE_TIME, reportableResult.getMessageReceivedDateTime());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_IDENTIFIER, reportableResult.getTestResultId());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_NAME, reportableResult.getTestResultName());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODESYS, reportableResult.getTestResultCodeSystem());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_SUBID, reportableResult.getTestResultSubId());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_LOINC_CODE, reportableResult.getTestResultCode());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODE, reportableResult.getTestResultCode());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_VALUE, reportableResult.getTestResultValue());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_UNITS, reportableResult.getTestResultUnits());
        addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_STATUS, reportableResult.getTestResultStatus());
        addCol(DataFeedExtractorFactory.COLUMN_DWYER_CONDITION_NAME, reportableResult.getConditionName());
        //addCol(DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_AGENCY, ...
        //addCol(DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_PATIENT_ID, ...
        //addCol(DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_CASE_ID, ...
        addCol(DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE, reportableResult.getObrAltCode());
        addCol(DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_TEXT, reportableResult.getObrAltCodeText());
        addCol(DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_SYS, reportableResult.getObrAltCodeSystem());
        addCol(DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE, reportableResult.getObxAltCode());
        addCol(DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_TEXT, reportableResult.getObxAltCodeText());
        addCol(DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_SYS, reportableResult.getObxAltCodeSystem());
    }

    /** Extract export columns from the raw HL7 message.
     * 
     * @param rawhl7 The raw HL7 message to extract from.
     */
    private void extractRawHL7(RawMessage rawhl7) {
        addCol(DataFeedExtractorFactory.COLUMN_HL7, rawhl7.getMessageText());
    }
    
    private void addCol(String colName, Object value) {

        if (value != null) {
            
            row.put(colName, value);
        }
    }
}

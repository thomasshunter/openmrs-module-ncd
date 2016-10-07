package org.openmrs.module.ncd.utilities;


/** A cache containing (after first call to findConcept) all NCD-specific
 * Concepts.
 * 
 * @author Erik Horstkotte
 */
public class NCDConcepts {
    
    // Concept database version 1.3 and earlier
    public static final String CONDITION_NAME = "NCD CONDITION NAME";
    public static final String APPLICATION = "NCD APPLICATION";
    public static final String FACILITY = "NCD FACILITY";
    public static final String INSTITUTION_NAME = "NCD INSTITUTION NAME";

    public static final String LABORATORY_NAME = "NCD LABORATORY NAME";
    public static final String LABORATORY_ID = "NCD LABORATORY ID";
    public static final String LABORATORY_PHONE = "NCD LABORATORY PHONE";
    public static final String LABORATORY_ADDRESS1 = "NCD LABORATORY ADDRESS 1";
    public static final String LABORATORY_ADDRESS2 = "NCD LABORATORY ADDRESS 2";
    public static final String LABORATORY_CITY = "NCD LABORATORY CITY";
    public static final String LABORATORY_STATE = "NCD LABORATORY STATE";
    public static final String LABORATORY_ZIP = "NCD LABORATORY ZIP";
    
    public static final String TEST_ID = "NCD TEST ID";
    public static final String TEST_NAME = "NCD TEST NAME";
    public static final String TEST_CODE_SYSTEM = "NCD TEST CODE SYSTEM";
    public static final String TEST_PLACER_ORDER_NUMBER = "NCD TEST PLACER ORDER NUMBER";
    public static final String TEST_FILLER_ORDER_NUMBER = "NCD TEST FILLER ORDER NUMBER";
    public static final String TEST_DATE = "NCD TEST DATE";
    public static final String TEST_PARENT_PLACER = "NCD TEST PARENT PLACER";
    public static final String TEST_PARENT_FILLER = "NCD TEST PARENT FILLER";
    public static final String TEST_SPECIMEN_TEXT = "NCD TEST SPECIMEN TEXT";
    public static final String TEST_DATA_TYPE = "NCD TEST DATA TYPE";
    public static final String TEST_NORMAL_RANGE = "NCD TEST NORMAL RANGE";
    public static final String TEST_ABNORMAL_FLAG = "NCD TEST ABNORMAL FLAG";
    public static final String TEST_COMMENT = "NCD TEST COMMENT";
    public static final String TEST_RECEIVED_DATE_TIME = "NCD TEST RECEIVED DATE TIME";
    public static final String TEST_MPQ_SEQ_NUMBER = "NCD TEST MPQ SEQ NUMBER";
    public static final String TEST_RESULT_ID = "NCD TEST RESULT ID";
    public static final String TEST_RESULT_NAME = "NCD TEST RESULT NAME";
    public static final String TEST_RESULT_CODE_SYSTEM = "NCD TEST RESULT CODE SYSTEM";
    public static final String TEST_RESULT_SUB_ID = "NCD TEST RESULT SUB ID";
    public static final String TEST_RESULT_CODE = "NCD TEST RESULT CODE";
    public static final String TEST_RESULT_VALUE = "NCD TEST RESULT VALUE";
    public static final String TEST_RESULT_UNITS = "NCD TEST RESULT UNITS";
    public static final String TEST_RESULT_STATUS = "NCD TEST RESULT STATUS";
    public static final String TEST_PREVIOUS_DATE = "NCD TEST PREVIOUS DATE";
    public static final String TEST_DATE_SOURCE = "NCD TEST DATE SOURCE";
    public static final String TEST_PLACER_ORDER_NUMBER_SOURCE = "NCD TEST PLACER ORDER NUMBER SOURCE";
    public static final String TEST_FILLER_ORDER_NUMBER_SOURCE = "NCD TEST FILLER ORDER NUMBER SOURCE";

    public static final String LOINC_CODE_ID = "NCD LOINC CODE ID";
    public static final String PREVIOUS_REPORTABLE_RESULT_ID = "NCD PREVIOUS REPORTABLE RESULT ID";
    public static final String RAW_HL7_ID = "NCD RAW HL7 ID";
    public static final String REPORTABLE_RESULT_ID = "NCD REPORTABLE RESULT ID";
    public static final String RESULT_SEQUENCE_NUMBER = "NCD REPORTABLE RESULT SEQUENCE NUMBER";
    public static final String HEALTH_DEPT_AGENCY = "NCD HEALTH DEPT AGENCY";
    public static final String HEALTH_DEPT_CASE_ID = "NCD HEALTH DEPT CASE ID";
    public static final String INSTITUTION_ID_TYPE = "NCD INSTITUTION ID TYPE";
    public static final String OBR_ALT_CODE = "NCD OBR ALT CODE";
    public static final String OBR_ALT_CODE_SYS = "NCD OBR ALT CODE SYS";
    public static final String OBR_ALT_CODE_TEXT = "NCD OBR ALT CODE TEXT";
    public static final String OBX_ALT_CODE = "NCD OBX ALT CODE";
    public static final String OBX_ALT_CODE_SYS = "NCD OBX ALT CODE SYS";
    public static final String OBX_ALT_CODE_TEXT = "NCD OBX ALT CODE TEXT";
    public static final String UNIQUE_REGISTRY_NUM = "NCD UNIQUE REGISTRY NUM";

    // Added in concept database version 1.4 and earlier
    public static final String REPORTABLE_RESULT = "NCD REPORTABLE RESULT";
}

package org.openmrs.module.ncd.output.extract;

import java.sql.Types;
import java.util.Map;

import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.utilities.MapUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.scheduler.TaskDefinition;

public class DataFeedExtractorFactory {

    // DataFeedExtractor properties
    //
    // PROP_EXTRACTOR_CLASS: the implementation class to be created by the factory
    // PROP_EXTRACTOR_LAST_RECORD_SENT: the last record sent (id)
    // PROP_EXTRACTOR_INST_TO_SEND: CSV of institution names to include in the extracted data
    // PROP_EXTRACTOR_COND_TO_SEND: CSV of condition names to include in the extracted data
    // PROP_EXTRACTOR_COND_TO_NOT_SEND: CSV of condition names to exclude from the extracted data
    // PROP_EXTRACTOR_INCREMENTAL: Boolean (true, false): Incremental extract?
    // PROP_EXTRACTOR_MAX_ROWS: the maximum number of rows to extract
    // PROP_EXTRACTOR_COUNTIES: CSV of county names.  Limits export to reportable results for patients residing in these counties.
    // PROP_EXTRACTOR_JURISDICTIONS: CSV of jurisdiction names.  Limits export to reportable results for these jurisdictions.
    // PROP_EXTRACTOR_CODES: CSV of codes.  Limits export to reportable results with these codes.
    // PROP_EXTRACTOR_RECENT_RESULT_INTERVAL: Interval for recent results.  Limits export to reportable results in the past n (units).  E.g. reportable results from the past 5 hours.
    // PROP_EXTRACTOR_RECENT_RESULT_INTERVAL_UNITS: Interval units for recent results (m=minutes, h=hours, d=days).  Used with PROP_EXTRACTOR_RECENT_RESULT_INTERVAL. 
    // PROP_EXTRACTOR_START_DATE: Returns reportable results with release date on or after the specified start date.  
    // PROP_EXTRACTOR_START_TIME: Returns reportable results with release date on or after the start date with specified start time.  If not specified, defaults to midnight.  
    // PROP_EXTRACTOR_END_DATE: Returns reportable results with release date on or before the specified end date.  
    // PROP_EXTRACTOR_END_TIME: Returns reportable results with release date on or before the end date with specified end time.  If not specified, defaults to 23:59:59.  
    // PROP_EXTRACTOR_EXCLUDE_CONDITION1: Excludes reportable results with this condition sent by the specified institution (PROP_EXTRACTOR_EXCLUDE_INSTITUTION1).
    // PROP_EXTRACTOR_EXCLUDE_CODES1 = CSV of codes.  Excludes reportable results with these code(s) sent by the specified institution (PROP_EXTRACTOR_EXCLUDE_INSTITUTION1).
    // PROP_EXTRACTOR_EXCLUDE_INSTITUTION1 = Excludes condition (PROP_EXTRACTOR_EXCLUDE_CONDITION1) or codes(s) (PROP_EXTRACTOR_EXCLUDE_CODES1) sent by this institution.
    // PROP_EXTRACTOR_EXCLUDE_CONDITION2: Same as PROP_EXTRACTOR_EXCLUDE_CONDITION1, but second bucket. 
    // PROP_EXTRACTOR_EXCLUDE_CODES2 = Same as PROP_EXTRACTOR_EXCLUDE_CODES1, but second bucket.
    // PROP_EXTRACTOR_EXCLUDE_INSTITUTION2 = Same as PROP_EXTRACTOR_EXCLUDE_INSTITUTION1, but second bucket.
    // PROP_EXTRACTOR_EXCLUDE_CONDITION3: Same as PROP_EXTRACTOR_EXCLUDE_CONDITION1, but third bucket. 
    // PROP_EXTRACTOR_EXCLUDE_CODES3 = Same as PROP_EXTRACTOR_EXCLUDE_CODES1, but third bucket.
    // PROP_EXTRACTOR_EXCLUDE_INSTITUTION3 = Same as PROP_EXTRACTOR_EXCLUDE_INSTITUTION1, but third bucket.
    // PROP_EXTRACTOR_EXCLUDE_CONDITION4: Same as PROP_EXTRACTOR_EXCLUDE_CONDITION1, but fourth bucket. 
    // PROP_EXTRACTOR_EXCLUDE_CODES4 = Same as PROP_EXTRACTOR_EXCLUDE_CODES1, but fourth bucket.
    // PROP_EXTRACTOR_EXCLUDE_INSTITUTION4 = Same as PROP_EXTRACTOR_EXCLUDE_INSTITUTION1, but fourth bucket.
    // PROP_EXTRACTOR_EXCLUDE_CONDITION5: Same as PROP_EXTRACTOR_EXCLUDE_CONDITION1, but fifth bucket. 
    // PROP_EXTRACTOR_EXCLUDE_CODES5 = Same as PROP_EXTRACTOR_EXCLUDE_CODES1, but fifth bucket.
    // PROP_EXTRACTOR_EXCLUDE_INSTITUTION5 = Same as PROP_EXTRACTOR_EXCLUDE_INSTITUTION1, but fifth bucket.
    public final static String PROP_EXTRACTOR_CLASS = "DataFeedExtractorFactory.class";
    public final static String PROP_EXTRACTOR_LAST_RECORD_SENT = "DataFeedExtractor.lastRecordSent";
    public final static String PROP_EXTRACTOR_INST_TO_SEND = "DataFeedExtractorFactory.institutionsToSend";
    public final static String PROP_EXTRACTOR_COND_TO_SEND = "DataFeedExtractorFactory.conditionNamesToSend";
    public final static String PROP_EXTRACTOR_COND_TO_NOT_SEND = "DataFeedExtractorFactory.conditionNamesToNotSend";
    public final static String PROP_EXTRACTOR_INCREMENTAL = "DataFeedExtractorFactory.incremental";
    public final static String PROP_EXTRACTOR_MAX_ROWS = "DataFeedExtractorFactory.maxRows";
    public final static String PROP_EXTRACTOR_COUNTIES = "DataFeedExtractorFactory.counties";
    public final static String PROP_EXTRACTOR_JURISDICTIONS = "DataFeedExtractorFactory.jurisdictions";
    public final static String PROP_EXTRACTOR_CODES = "DataFeedExtractorFactory.codes";
    public final static String PROP_EXTRACTOR_RECENT_RESULT_INTERVAL = "DataFeedExtractorFactory.recentResultInterval";
    public final static String PROP_EXTRACTOR_RECENT_RESULT_INTERVAL_UNITS = "DataFeedExtractorFactory.recentResultIntervalUnits";
    public final static String PROP_EXTRACTOR_START_DATE = "DataFeedExtractorFactory.startDate";
    public final static String PROP_EXTRACTOR_START_TIME = "DataFeedExtractorFactory.startTime";
    public final static String PROP_EXTRACTOR_END_DATE = "DataFeedExtractorFactory.endDate";
    public final static String PROP_EXTRACTOR_END_TIME = "DataFeedExtractorFactory.endTime";
    public final static String PROP_EXTRACTOR_EXCLUDE_CONDITION1 = "DataFeedExtractorFactory.excludeCondition1";
    public final static String PROP_EXTRACTOR_EXCLUDE_CODES1 = "DataFeedExtractorFactory.excludeCodes1";
    public final static String PROP_EXTRACTOR_EXCLUDE_INSTITUTION1 = "DataFeedExtractorFactory.excludeInstitution1";
    public final static String PROP_EXTRACTOR_EXCLUDE_CONDITION2 = "DataFeedExtractorFactory.excludeCondition2";
    public final static String PROP_EXTRACTOR_EXCLUDE_CODES2 = "DataFeedExtractorFactory.excludeCodes2";
    public final static String PROP_EXTRACTOR_EXCLUDE_INSTITUTION2 = "DataFeedExtractorFactory.excludeInstitution2";
    public final static String PROP_EXTRACTOR_EXCLUDE_CONDITION3 = "DataFeedExtractorFactory.excludeCondition3";
    public final static String PROP_EXTRACTOR_EXCLUDE_CODES3 = "DataFeedExtractorFactory.excludeCodes3";
    public final static String PROP_EXTRACTOR_EXCLUDE_INSTITUTION3 = "DataFeedExtractorFactory.excludeInstitution3";
    public final static String PROP_EXTRACTOR_EXCLUDE_CONDITION4 = "DataFeedExtractorFactory.excludeCondition4";
    public final static String PROP_EXTRACTOR_EXCLUDE_CODES4 = "DataFeedExtractorFactory.excludeCodes4";
    public final static String PROP_EXTRACTOR_EXCLUDE_INSTITUTION4 = "DataFeedExtractorFactory.excludeInstitution4";
    public final static String PROP_EXTRACTOR_EXCLUDE_CONDITION5 = "DataFeedExtractorFactory.excludeCondition5";
    public final static String PROP_EXTRACTOR_EXCLUDE_CODES5 = "DataFeedExtractorFactory.excludeCodes5";
    public final static String PROP_EXTRACTOR_EXCLUDE_INSTITUTION5 = "DataFeedExtractorFactory.excludeInstitution5";

    // The keys in the Map returned by DataFeedExtractor.next()
    public final static String COLUMN_UNIQUE_RECORD_NUM = "UNIQUE_RECORD_NUM";
    public final static String COLUMN_SOURCE_INSTITUTION = "SOURCE_INSTITUTION";
    public final static String COLUMN_INSTITUTION_ID_TYPE = "INSTITUTION_ID_TYPE";
    public final static String COLUMN_PAT_INST_MED_REC_ID = "PAT_INST_MED_REC_ID";
    public final static String COLUMN_PAT_GLOBAL_ID = "GLOBAL_ID";
    public final static String COLUMN_UNIQUE_REGISTRY_NUM = "UNIQUE_REGISTRY_NUM";
    public final static String COLUMN_PAT_SOCSEC = "PAT_SOCSEC";
    public final static String COLUMN_PAT_NAME = "PAT_NAME";
    public final static String COLUMN_PAT_BIRTH = "PAT_BIRTH";
    public final static String COLUMN_PAT_SEX = "PAT_SEX";
    public final static String COLUMN_PAT_RACE = "PAT_RACE";
    public final static String COLUMN_PAT_PHONE = "PAT_PHONE";
    public final static String COLUMN_PAT_STREET1 = "PAT_STREET1";
    public final static String COLUMN_PAT_STREET2 = "PAT_STREET2";
    public final static String COLUMN_PAT_CITY = "PAT_CITY";
    public final static String COLUMN_PAT_COUNTY = "PAT_COUNTY";
    public final static String COLUMN_PAT_STATE = "PAT_STATE";
    public final static String COLUMN_PAT_ZIP = "PAT_ZIP";
    public final static String COLUMN_PAT_COUNTRY = "PAT_COUNTRY";
    public final static String COLUMN_PROVIDER_NAME = "PROVIDER_NAME";
    public final static String COLUMN_PROVIDER_NAME_MATCHED = "PROVIDER_NAME_MATCHED";
    public final static String COLUMN_PROVIDER_SSN = "PROVIDER_SSN";
    public final static String COLUMN_PROVIDER_BIRTH = "PROVIDER_BIRTH";
    public final static String COLUMN_PROVIDER_PRACTICE = "PROVIDER_PRACTICE";
    public final static String COLUMN_PROVIDER_STREET = "PROVIDER_STREET";
    public final static String COLUMN_PROVIDER_CITY = "PROVIDER_CITY";
    public final static String COLUMN_PROVIDER_STATE = "PROVIDER_STATE";
    public final static String COLUMN_PROVIDER_ZIP = "PROVIDER_ZIP";
    public final static String COLUMN_PROVIDER_COUNTY = "PROVIDER_COUNTY";
    public final static String COLUMN_PROVIDER_PHONE = "PROVIDER_PHONE";
    public final static String COLUMN_PROVIDER_FAX = "PROVIDER_FAX";
    public final static String COLUMN_PROVIDER_LOCAL_ID = "PROVIDER_LOCAL_ID";
    public final static String COLUMN_PROVIDER_DEA_NUM = "PROVIDER_DEA_NUM";
    public final static String COLUMN_PROVIDER_LICENSE = "PROVIDER_LICENSE";
    public final static String COLUMN_LAB_NAME = "LAB_NAME";
    public final static String COLUMN_LAB_IDENTIFIER = "LAB_IDENTIFIER";
    public final static String COLUMN_LAB_PHONE = "LAB_PHONE";
    public final static String COLUMN_LAB_STREET1 = "LAB_STREET1";
    public final static String COLUMN_LAB_STREET2 = "LAB_STREET2";
    public final static String COLUMN_LAB_CITY = "LAB_CITY";
    public final static String COLUMN_LAB_STATE = "LAB_STATE";
    public final static String COLUMN_LAB_ZIP = "LAB_ZIP";
    public final static String COLUMN_TEST_IDENTIFIER = "TEST_IDENTIFIER";
    public final static String COLUMN_TEST_NAME = "TEST_NAME";
    public final static String COLUMN_TEST_CODESYS = "TEST_CODESYS";
    public final static String COLUMN_TEST_PLACER_ORDER_NUM = "TEST_PLACER_ORDER_NUM";
    public final static String COLUMN_TEST_FILLER_ORDER_NUM = "TEST_FILLER_ORDER_NUM";
    public final static String COLUMN_TEST_DATE = "TEST_DATE";
    public final static String COLUMN_TEST_PARENT_PLACER = "TEST_PARENT_PLACER";
    public final static String COLUMN_TEST_PARENT_FILLER = "TEST_PARENT_FILLER";
    public final static String COLUMN_TEST_SPECIMEN_TEXT = "TEST_SPECIMEN_TEXT";
    public final static String COLUMN_TEST_LOINC_CODE = "TEST_LOINC_CODE";
    public final static String COLUMN_TEST_DATA_TYPE = "TEST_DATA_TYPE";
    public final static String COLUMN_TEST_NORMAL_RANGE = "TEST_NORMAL_RANGE";
    public final static String COLUMN_TEST_ABNORMAL_FLAG = "TEST_ABNORMAL_FLAG";
    public final static String COLUMN_TEST_COMMENT = "TEST_COMMENT";
    public final static String COLUMN_TEST_RCVD_DATE_TIME = "TEST_RCVD_DATE_TIME";
    public final static String COLUMN_TEST_MPQ_SEQ_NUMBER = "TEST_MPQ_SEQ_NUMBER";
    public final static String COLUMN_TEST_RESULT_IDENTIFIER = "TEST_RESULT_IDENTIFIER";
    public final static String COLUMN_TEST_RESULT_NAME = "TEST_RESULT_NAME";
    public final static String COLUMN_TEST_RESULT_CODESYS = "TEST_RESULT_CODESYS";
    public final static String COLUMN_TEST_RESULT_SUBID = "TEST_RESULT_SUBID";
    public final static String COLUMN_TEST_RESULT_LOINC_CODE = "TEST_RESULT_LOINC_CODE";
    public final static String COLUMN_TEST_RESULT_CODE = "TEST_RESULT_CODE";
    public final static String COLUMN_TEST_RESULT_VALUE = "TEST_RESULT_VALUE";
    public final static String COLUMN_TEST_RESULT_UNITS = "TEST_RESULT_UNITS";
    public final static String COLUMN_TEST_RESULT_STATUS = "TEST_RESULT_STATUS";
    public final static String COLUMN_TEST_PREVIOUS_DATE = "TEST_PREVIOUS_DATE";
    public final static String COLUMN_DWYER_CONDITION_NAME = "DWYER_CONDITION_NAME";
    public final static String COLUMN_HEALTH_DEPT_AGENCY = "HEALTH_DEPT_AGENCY";
    public final static String COLUMN_HEALTH_DEPT_PATIENT_ID = "HEALTH_DEPT_PATIENT_ID";
    public final static String COLUMN_HEALTH_DEPT_CASE_ID = "HEALTH_DEPT_CASE_ID";
    public final static String COLUMN_MAPPED_LOINC = "MAPPED_LOINC";
    public final static String COLUMN_OBR_ALT_CODE = "OBR_ALT_CODE";
    public final static String COLUMN_OBR_ALT_CODE_TEXT = "OBR_ALT_CODE_TEXT";
    public final static String COLUMN_OBR_ALT_CODE_SYS = "OBR_ALT_CODE_SYS";
    public final static String COLUMN_OBX_ALT_CODE = "OBX_ALT_CODE";
    public final static String COLUMN_OBX_ALT_CODE_TEXT = "OBX_ALT_CODE_TEXT";
    public final static String COLUMN_OBX_ALT_CODE_SYS = "OBX_ALT_CODE_SYS";
    public final static String COLUMN_OBR_SET_ID = "COLUMN_OBR_SET_ID";
    public final static String COLUMN_OBX_START_SET_ID = "COLUMN_OBX_START_SET_ID";
    public final static String COLUMN_OBX_END_SET_ID = "COLUMN_OBX_END_SET_ID";
    public final static String COLUMN_HL7 = "HL7";
    public final static String COLUMN_APPLICATION = "APPLICATION";
    public final static String COLUMN_FACILITY = "FACILITY";
    public final static String COLUMN_HL7_LONG = "HL7_LONG";
    
    private final static Column[] outputColumns = {
        new Column(DataFeedExtractorFactory.COLUMN_UNIQUE_RECORD_NUM, Types.VARCHAR, 16),
        new Column(DataFeedExtractorFactory.COLUMN_SOURCE_INSTITUTION, Types.VARCHAR, 4),
        new Column(DataFeedExtractorFactory.COLUMN_INSTITUTION_ID_TYPE, Types.VARCHAR, 2),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_INST_MED_REC_ID, Types.VARCHAR, 16),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_GLOBAL_ID, Types.VARCHAR, 16),
        new Column(DataFeedExtractorFactory.COLUMN_UNIQUE_REGISTRY_NUM, Types.VARCHAR, 16),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_SOCSEC, Types.VARCHAR, 11),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_NAME, Types.VARCHAR, 32),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_BIRTH, Types.TIMESTAMP, 0),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_SEX, Types.VARCHAR, 1),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_RACE, Types.VARCHAR, 1),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_PHONE, Types.VARCHAR, 12),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_STREET1, Types.VARCHAR, 30),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_STREET2, Types.VARCHAR, 30),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_CITY, Types.VARCHAR, 24),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_COUNTY, Types.VARCHAR, 24),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_STATE, Types.VARCHAR, 2),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_ZIP, Types.VARCHAR, 9),
        new Column(DataFeedExtractorFactory.COLUMN_PAT_COUNTRY, Types.VARCHAR, 24),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_NAME, Types.VARCHAR, 32),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_NAME_MATCHED, Types.VARCHAR, 32),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_SSN, Types.VARCHAR, 11),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_BIRTH, Types.TIMESTAMP, 0),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_PRACTICE, Types.VARCHAR, 32),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_STREET, Types.VARCHAR, 32),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_CITY, Types.VARCHAR, 24),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_STATE, Types.VARCHAR, 2),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_ZIP, Types.VARCHAR, 9),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_COUNTY, Types.VARCHAR, 2),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_PHONE, Types.VARCHAR, 12),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_FAX, Types.VARCHAR, 12),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_LOCAL_ID, Types.VARCHAR, 15),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_DEA_NUM, Types.VARCHAR, 15),
        new Column(DataFeedExtractorFactory.COLUMN_PROVIDER_LICENSE, Types.VARCHAR, 15),
        new Column(DataFeedExtractorFactory.COLUMN_LAB_NAME, Types.VARCHAR, 32),
        new Column(DataFeedExtractorFactory.COLUMN_LAB_IDENTIFIER, Types.VARCHAR, 32),
        new Column(DataFeedExtractorFactory.COLUMN_LAB_PHONE, Types.VARCHAR, 12),
        new Column(DataFeedExtractorFactory.COLUMN_LAB_STREET1, Types.VARCHAR, 30),
        new Column(DataFeedExtractorFactory.COLUMN_LAB_STREET2, Types.VARCHAR, 30),
        new Column(DataFeedExtractorFactory.COLUMN_LAB_CITY, Types.VARCHAR, 24),
        new Column(DataFeedExtractorFactory.COLUMN_LAB_STATE, Types.VARCHAR, 2),
        new Column(DataFeedExtractorFactory.COLUMN_LAB_ZIP, Types.VARCHAR, 9),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_IDENTIFIER, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_NAME, Types.VARCHAR, 80),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_CODESYS, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_PLACER_ORDER_NUM, Types.VARCHAR, 22),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_FILLER_ORDER_NUM, Types.VARCHAR, 22),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_DATE, Types.TIMESTAMP, 0),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_PARENT_PLACER, Types.VARCHAR, 22),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_PARENT_FILLER, Types.VARCHAR, 22),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_SPECIMEN_TEXT, Types.VARCHAR, 80),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_LOINC_CODE, Types.VARCHAR, 8),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_DATA_TYPE, Types.VARCHAR, 2),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_NORMAL_RANGE, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_ABNORMAL_FLAG, Types.VARCHAR, 5),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_COMMENT, Types.VARCHAR, 200),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RCVD_DATE_TIME, Types.TIMESTAMP, 0),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_MPQ_SEQ_NUMBER, Types.VARCHAR, 16),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RESULT_IDENTIFIER, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RESULT_NAME, Types.VARCHAR, 80),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODESYS, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RESULT_SUBID, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RESULT_LOINC_CODE, Types.VARCHAR, 8),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODE, Types.VARCHAR, 80),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RESULT_VALUE, Types.VARCHAR, Integer.MAX_VALUE),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RESULT_UNITS, Types.VARCHAR, 60),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_RESULT_STATUS, Types.VARCHAR, 1),
        new Column(DataFeedExtractorFactory.COLUMN_TEST_PREVIOUS_DATE, Types.TIMESTAMP, 0),
        new Column(DataFeedExtractorFactory.COLUMN_DWYER_CONDITION_NAME, Types.VARCHAR, 80),
        new Column(DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_AGENCY, Types.VARCHAR, 30),
        new Column(DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_PATIENT_ID, Types.VARCHAR, 30),
        new Column(DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_CASE_ID, Types.VARCHAR, 30),
        new Column(DataFeedExtractorFactory.COLUMN_MAPPED_LOINC, Types.VARCHAR, 8),
        new Column(DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_TEXT, Types.VARCHAR, 80),
        new Column(DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_SYS, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_TEXT, Types.VARCHAR, 80),
        new Column(DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_SYS, Types.VARCHAR, 10),
        new Column(DataFeedExtractorFactory.COLUMN_HL7, Types.VARCHAR, 1),
        new Column(DataFeedExtractorFactory.COLUMN_APPLICATION, Types.VARCHAR, 180),
        new Column(DataFeedExtractorFactory.COLUMN_FACILITY, Types.VARCHAR, 180),
        new Column(DataFeedExtractorFactory.COLUMN_HL7_LONG, Types.VARCHAR, Integer.MAX_VALUE)
    };
        
    /** Creates and returns an instance of a class implementing
     * DataFeedExtractor as selected by the properties supplied.
     */
    public static DataFeedExtractor getInstance(TaskDefinition taskdef, Map<String, String> properties, DataFeedLog dataFeedLog)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        // If there is a taskdef, set the last record sent in the properties based on the last successful task run
    	if (taskdef != null) {
	        ConditionDetectorService cds = NCDUtilities.getService();
	        TaskRunStatus lastStatus = cds.getLatestTaskStatus(taskdef);
	        if (lastStatus != null) {
	        	properties.put(DataFeedExtractorFactory.PROP_EXTRACTOR_LAST_RECORD_SENT, Long.toString(lastStatus.getLast()));
	        }
    	}

        String className = MapUtilities.get(properties, PROP_EXTRACTOR_CLASS, DataFeedExtractorOld.class.getName());
        Class<?> clazz = Class.forName(className);
        DataFeedExtractor inst = (DataFeedExtractor) clazz.newInstance();
        inst.configure(properties, dataFeedLog);
        
        return inst;
    }

    public static Column[] getOutputColumns() {
        return outputColumns;
    }

    public static String getOutputColumnNames() {
        String[] columnNames = new String[outputColumns.length];
        for (int colNum = 0; colNum < outputColumns.length; colNum++) {
            columnNames[colNum] = outputColumns[colNum].getName();
        }
        return StringUtilities.toCSV(columnNames);
    }
}


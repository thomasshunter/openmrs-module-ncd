package org.openmrs.module.ncd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.AlertType;
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.CodeFrequency;
import org.openmrs.module.ncd.database.CodeSystem;
import org.openmrs.module.ncd.database.CodeType;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.ConditionGroup;
import org.openmrs.module.ncd.database.County;
import org.openmrs.module.ncd.database.CriticDef;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.database.DecidedResultArchive;
import org.openmrs.module.ncd.database.Error;
import org.openmrs.module.ncd.database.ExportRecipient;
import org.openmrs.module.ncd.database.ExportedResult;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.Institution;
import org.openmrs.module.ncd.database.Jurisdiction;
import org.openmrs.module.ncd.database.ManualReviewStatusType;
import org.openmrs.module.ncd.database.MessageCountSummary;
import org.openmrs.module.ncd.database.MonitoredCondition;
import org.openmrs.module.ncd.database.NlpCriticConcept;
import org.openmrs.module.ncd.database.NlpCriticContext;
import org.openmrs.module.ncd.database.NlpCriticContextGroup;
import org.openmrs.module.ncd.database.NlpCriticContextType;
import org.openmrs.module.ncd.database.NlpDiscreteTerm;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.database.dao.DataSourceInfo;
import org.openmrs.module.ncd.database.dao.IAlertSummaryDAO;
import org.openmrs.module.ncd.database.dao.ICodeConditionDAO;
import org.openmrs.module.ncd.database.dao.ICodeDAO;
import org.openmrs.module.ncd.database.dao.ICodeFrequencyDAO;
import org.openmrs.module.ncd.database.dao.IConditionDAO;
import org.openmrs.module.ncd.database.dao.IDataSourceReportDAO;
import org.openmrs.module.ncd.database.dao.IDecidedResultDAO;
import org.openmrs.module.ncd.database.dao.IErrorDAO;
import org.openmrs.module.ncd.database.dao.IInstitutionDAO;
import org.openmrs.module.ncd.database.dao.IMessageCountSummaryDAO;
import org.openmrs.module.ncd.database.dao.INlpCriticConceptDAO;
import org.openmrs.module.ncd.database.dao.INlpCriticContextDAO;
import org.openmrs.module.ncd.database.dao.INlpDiscreteTermDAO;
import org.openmrs.module.ncd.database.dao.IProcessedMessageCountDAO;
import org.openmrs.module.ncd.database.dao.IProducerDAO;
import org.openmrs.module.ncd.database.dao.IRateMonitoringDAO;
import org.openmrs.module.ncd.database.dao.IRawMessageDAO;
import org.openmrs.module.ncd.database.dao.IReportableResultDAO;
import org.openmrs.module.ncd.database.dao.IReviewStatusDAO;
import org.openmrs.module.ncd.database.dao.ISystemActivityDAO;
import org.openmrs.module.ncd.database.filter.DataSourceReportFilter;
import org.openmrs.module.ncd.database.filter.SearchFilterAlertSummary;
import org.openmrs.module.ncd.database.filter.SearchFilterCodes;
import org.openmrs.module.ncd.database.filter.SearchFilterDecidedResults;
import org.openmrs.module.ncd.database.filter.SearchFilterError;
import org.openmrs.module.ncd.database.filter.SearchFilterMessageCountSummary;
import org.openmrs.module.ncd.database.filter.SearchFilterProducers;
import org.openmrs.module.ncd.database.filter.SearchFilterReportableResults;
import org.openmrs.module.ncd.database.filter.SearchResult;
import org.openmrs.module.ncd.database.filter.SystemEventFilter;
import org.openmrs.module.ncd.database.filter.UnusualConditionRateFilter;
import org.openmrs.module.ncd.model.ConditionCount;
import org.openmrs.module.ncd.model.SystemActivityEvent;
import org.openmrs.module.ncd.model.ZeroCountCondition;
import org.openmrs.module.ncd.output.aggrpt.ReportData;
import org.openmrs.module.ncd.output.extract.DataFeedLog;
import org.openmrs.module.ncd.output.extract.DataFeedSink;
import org.openmrs.module.ncd.storage.CodeFrequencyStorageException;
import org.openmrs.module.ncd.utilities.DateRange;
import org.openmrs.module.ncd.utilities.NCDConstants;
import org.openmrs.module.ncd.utilities.Pair;
import org.openmrs.scheduler.TaskDefinition;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ConditionDetectorService {

	// Constants for AlertType id's
	public final static int alertTypeManualReview = 1;
	public final static int alertTypeReportError = 2;
	public final static int alertTypeMessageRate = 3;
	public final static int alertTypeParseError = 4;
	public final static int alertTypeAmbiguousConditionError = 5;
	
	// Constants for ManualReviewStatusType id's
	public final static int reviewStatusTypeNoReviewRequired = 1;
	public final static int reviewStatusTypeHold = 2;
	public final static int reviewStatusTypeReleased = 3;
	public final static int reviewStatusTypeRejected = 4;

	// Constants for AlertGroup identity prefixes
	public final static String alertIdentityAggRpt = "AGGRPT";
	public final static String alertIdentityExpRpt = "EXPRPT";
	public final static String alertIdentityCodeRate = "zrc";
    public final static String alertIdentityConditionRate = "zrco";
    public final static String alertIdentityManualReview = "MANRVW";
    public final static String alertIdentityCandidateResult = "CANRES";
	public final static String alertIdentityDailyExpRpt = "DEXPRPT";
	public final static String alertIdentityZeroCount = "zc";
	public final static String alertIdentityCodeError = "ce";
    
    // Constants for Critic ID
    public final static long abnormalFlagCritic = 1;
    public final static long alwaysReportableSourceCritic = 2;
    public final static long appFacFilteringCritic = 3;
    public final static long decidedResultsCritic = 4;
    public final static long messageTypeCritic = 5;
    public final static long nlpCriticTextAnalyzer = 6;
    public final static long nlpCriticNumericAnalyzer = 7;
    public final static long nlpCriticDiscreteAnalyzer = 8;
    public final static long processingIdCritic = 9;
    public final static long reportableConditionCritic = 10;
    public final static long icd9InObxCritic = 11;
    public final static long reportAllCritic = 12;

	// ---------------------------------------------------------------------
	// DAO getters & setters. These methods are part of the interface only
    // to support testing.
    // ---------------------------------------------------------------------
	
    /** The DAO class for tblapplicationfacility entities */
    public IProducerDAO getProducerDAO();
    public void setProducerDAO(IProducerDAO dao);

    /** The DAO class for CodeCondition entities */
    public ICodeConditionDAO getCodeConditionDAO();
    public void setCodeConditionDAO(ICodeConditionDAO dao);

    /** The DAO class for tblloincfrequency entities */
    public ICodeFrequencyDAO getCodeFrequencyDAO();
    public void setCodeFrequencyDAO(ICodeFrequencyDAO dao);

    /** The DAO class for decidedResult entities */
    public IDecidedResultDAO getDecidedResultDAO();
    public void setDecidedResultDAO(IDecidedResultDAO dao);

    /** The DAO class for error entities */    
    public IErrorDAO getErrorDAO();
    public void setErrorDAO(IErrorDAO dao);

    /** The DAO class for rawhl7 entities */
    public IRawMessageDAO getRawMessageDAO();
    public void setRawMessageDAO(IRawMessageDAO dao);

    /** The DAO class for reportableResult entities */
    public IReportableResultDAO getReportableResultDAO();
    public void setReportableResultDAO(IReportableResultDAO dao);

    /** The DAO class for institution entities */
    public IInstitutionDAO getInstitutionDAO();
    public void setInstitutionDAO(IInstitutionDAO dao);

    /** The DAO class for NlpCriticConcept entities for Rex */
    public INlpCriticConceptDAO getNlpCriticConceptDAO();
    public void setNlpCriticConceptDAO(INlpCriticConceptDAO dao);

    /** The DAO class for context entities for Rex */
    public INlpCriticContextDAO getNlpCriticContextDAO();
    public void setNlpCriticContextDAO(INlpCriticContextDAO dao);
    
    /** The DAO class for conditions */
    public IConditionDAO getConditionDAO();
    public void setConditionDAO(IConditionDAO dao);
    
    /** The DAO class for alert summaries */
    public IAlertSummaryDAO getAlertSummaryDAO();
    public void setAlertSummaryDAO(IAlertSummaryDAO dao);
    
    /** The DAO class for code rate monitoring */
    public void setRateMonitoringDAO(IRateMonitoringDAO dao);
    public IRateMonitoringDAO getRateMonitoringDAO();

    /** The DAO class for code, code system and code type operations */
    public void setCodeDAO(ICodeDAO dao);
    public ICodeDAO getCodeDAO();
    
    /** The DAO class for system activity */
    public ISystemActivityDAO getSystemActivityDAO();
    public void setSystemActivityDAO(ISystemActivityDAO dao);
    
    /** The DAO class for message count summaries */
    public IMessageCountSummaryDAO getMessageCountSummaryDAO();
    public void setMessageCountSummaryDAO(IMessageCountSummaryDAO dao);
    
    /** The DAO class for manual review statuses */
    public IReviewStatusDAO getReviewStatusDAO();
    public void setReviewStatusDAO(IReviewStatusDAO dao);
    
    public INlpDiscreteTermDAO getNlpDiscreteTermDAO();
    public void setNlpDiscreteTermDAO(INlpDiscreteTermDAO dao);

    //-----------------------------------------
    // Message Source operations
    //-----------------------------------------
    
    @Transactional(readOnly=true)
    @Authorized()
    //@Authorized({ NCDConstants.PRIV_VIEW_MESSAGE_SOURCES })
    public HL7Producer getProducer(String applicationName,
            String facilityName, String locationName);
    //@Authorized({ NCDConstants.PRIV_VIEW_MESSAGE_SOURCES })
    public HL7Producer getProducerExact(String applicationName, 
    		String facilityName, String locationName);

    @Transactional(readOnly=true)
    @Authorized({ NCDConstants.PRIV_VIEW_MESSAGE_SOURCES })
    public HL7Producer getProducer(long id);
    
    @Authorized({ NCDConstants.PRIV_ADD_MESSAGE_SOURCES, NCDConstants.PRIV_EDIT_MESSAGE_SOURCES })
    public void saveProducer(HL7Producer src);    
    
    @Authorized({ NCDConstants.PRIV_DELETE_MESSAGE_SOURCES })
    public void deleteProducer(HL7Producer src);
    
    @Transactional(readOnly=true)
    @Authorized({ NCDConstants.PRIV_VIEW_MESSAGE_SOURCES })
    public List<HL7Producer> getAllProducers();

    @Transactional(readOnly=true)
    @Authorized({ NCDConstants.PRIV_VIEW_MESSAGE_SOURCES })
    public List<HL7Producer> getAllUnretiredProducers();

    @Transactional(readOnly=true)
    @Authorized({ NCDConstants.PRIV_VIEW_MESSAGE_SOURCES })
    public SearchResult<HL7Producer> findProducers(SearchFilterProducers filter);

    //-----------------------------------------
    // LOINC code operations
    //-----------------------------------------
    
    @Transactional(readOnly=true)
    @Authorized()
    public List<CodeCondition> findByCodeAndSystem(String code, String system);
    
    @Transactional(readOnly=true)
    @Authorized()
    public CodeCondition findByCodeAndCondition(String code, String system, String condition);

    /**
     * Increment the frequency of the code for the particular date
     * and app/fac/loc.
     * 
     * @param date The date the code was reported.
     * @param application The HL7 application that reported the code.
     * @param facility The HL7 facility that reported the code.
     * @param location The HL7 location that reported the code.
     * @param code The code reported.
     * @param codeSystem The code system for the code reported.
     * @param patientZipCode The zipcode of the patient.
     * @param instituteZipCode The zipcode of the institute.
     * @param doctorZipCode The zipcode of the doctor.
     * @return The updated CodeFrequency object.
     */
    @Authorized()
    public void incrementCodeFrequency(Date date, String application, 
            String facility, String location, String code, String codeSystem,
            String patientZipCode, 
            String instituteZipCode, String doctorZipCode) throws CodeFrequencyStorageException;

    /**
     * Find the code frequency row that corresponds to passed in date,
     * app/fac/loc, code, code system, and zip codes.
     * 
     * @param date The date the code was reported.
     * @param application The HL7 application that reported the code.
     * @param facility The HL7 facility that reported the code.
     * @param location The HL7 location that reported the code.
     * @param code The code reported.
     * @param codeSystem The code system for the code reported.
     * @param patientZipCode The zipcode of the patient.
     * @param instituteZipCode The zipcode of the institute.
     * @param doctorZipCode The zipcode of the doctor.
     * @return The code frequency row that corresponds to the date, institute,
     *         and code.
     */
    @Transactional(readOnly=true)
    @Authorized()
    public CodeFrequency findCodeFrequency(Date date,
            String application, String facility, String location,
            String code, String codeSystem,
            String patientZipCode, String instituteZipCode,
            String doctorZipCode);

    /**
     * Stores any cached loinc frequency data.
     */
    @Authorized()
    public void saveCodeFrequencyMap() throws CodeFrequencyStorageException;

    //-----------------------------------------
    // HL7 storage operations
    //-----------------------------------------
    
    @Authorized( { NCDConstants.PRIV_ADD_ERRORS } )
    public void storeError(Error errorRow);
    
    @Authorized( { NCDConstants.PRIV_EDIT_ERRORS } )
    public void updateError(Error errorRow);
    
    @Authorized( { NCDConstants.PRIV_DELETE_ERRORS } )
    public void deleteError(Error errorRow, String dismissReason);
    
    @Authorized( { NCDConstants.PRIV_VIEW_ERRORS } )
    public SearchResult<Error> findErrors(SearchFilterError filter);
    
    @Authorized( { NCDConstants.PRIV_VIEW_ERRORS } )
    public Error findErrorById(Long id);
    
    @Authorized( { NCDConstants.PRIV_VIEW_ERRORS } )
    public Error findErrorByRawMessage(RawMessage message);
    
    /**
     * Persist the raw HL7 row in the database.
     * @param rawHL7 The HL7 data to be stored in the form
     * of a RawMessage object.
     * @return A Serializable that contains the stored row.
     */
    @Authorized()
    public Serializable saveRawMessage(RawMessage rawHL7);
    
    @Authorized()
    public void updateRawMessage(RawMessage rawHL7);
    
    /**
     * Finds the raw HL7 database row based on the raw hl7 row id.
     * @param id The raw HL7 row id which is being looked up.
     * @return The HL7 database row that contains the
     * specified message.
     */
    @Transactional(readOnly=true)
    @Authorized()
    public RawMessage findRawMessageById(Long id);

    //-----------------------------------------
    // Reportable results operations
    //-----------------------------------------
    
    /**
     * Persist the reportable result data to the database.
     * @param reportableResult The reportable result data in the form of
     * a Tblreporableresult object.
     * @return A Serializable that contains the stored data.
     */
    @Authorized( { NCDConstants.PRIV_ADD_REPORTABLE_RESULTS, NCDConstants.PRIV_EDIT_REPORTABLE_RESULTS })
    public Serializable saveReportableResults(List<ReportableResult> reportableResult);

    /** Get a reportable result by its id
     * @param id The primary key of the instance to get.
     * @return The reportable result with the matching primary key value, or
     * null if there is no such reportable result.
     */
    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_REPORTABLE_RESULTS })
    public ReportableResult getReportableResult(long id);

    /** Find reportable results which match a filter
     * @param filter The search criteria.
     * @return A list of reportable results that match the search criteria.
     */
    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_REPORTABLE_RESULTS })
    public SearchResult<ReportableResult> findReportableResults(SearchFilterReportableResults filter);

    /** Convert reportable results matching the filter to delimited
     * text.
     *  
     * @param filter
     * @return A String containing lines in delimited text (CSV)
     * format, representing the reportable results that match the
     * filter.
     */
    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_REPORTABLE_RESULTS })
    public String exportReportableResults(SearchFilterReportableResults filter);
    
    /**
     * Rejects a reportable result that has been held for manual review.
     * This updates its status, and dismisses the associated dashboard alert.
     * 
     * @param result The reportable result to be rejected.
     */
    @Authorized( { NCDConstants.PRIV_EDIT_REPORTABLE_RESULTS })
    public void rejectReportableResult(ReportableResult result);

    /**
     * Releases a reportable result that has been held for manual review.
     * This updates its status, sets its release date, dismisses the associated dashboard alert, 
     * and updates the resultSeq in both the reportable result, and in the associated OpenMRS observation.
     * 
     * @param result The reportable result to be released.
     */
    @Authorized( { NCDConstants.PRIV_EDIT_REPORTABLE_RESULTS })
    public void releaseReportableResult(ReportableResult result);
    
    
    /**
     * Marks all reportable results in the list as sent in error (if not already marked this way).
     * Also, sends an email to all recipients previously notified via email about an export containing
     * these reportable results to notify them the reportable results were sent in error.
     * 
     * The database updates are transactional, however, the email notification is not; it is done 
     * on a best effort basis, but will not attempt to recover from errors.
     *  
     * @param results The list of reportable results to be marked sent in error.
     */
    @Authorized( { NCDConstants.PRIV_EDIT_REPORTABLE_RESULTS })
    public void reportableResultSentInError(List<ReportableResult> results);
    
    /**
     * Marks all reportable results in the list as not sent in error (if not already marked this way).
     * 
     * @param results The list of reportable results to be marked not sent in error.
     */
    @Authorized( { NCDConstants.PRIV_EDIT_REPORTABLE_RESULTS })
    public void reportableResultNotSentInError(List<ReportableResult> results);

    //-----------------------------------------
    // NLP concept and context operations
    //-----------------------------------------
    
    /** To be called only by NlpCriticConceptCache.get */
    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_NLP_CONTEXTS })
    public List<NlpCriticConcept> listNlpCriticConcepts();
    
    /** To be called only by NlpCriticConceptCache.save */
    @Authorized( { NCDConstants.PRIV_ADD_NLP_CONTEXTS, NCDConstants.PRIV_EDIT_NLP_CONTEXTS })
    public void saveNlpCriticConcept(NlpCriticConcept concept);
    
    /** To be called only by NlpCriticConceptCache.delete */
    @Authorized( { NCDConstants.PRIV_DELETE_NLP_CONTEXTS })
    public void deleteNlpCriticConcept(NlpCriticConcept concept);
    
    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_NLP_CONTEXTS })
    public List<NlpCriticContext> findContextByType(NlpCriticContextType type);
    
    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_NLP_CONTEXTS })
    public List<NlpCriticContext> findContextByTypeAndGroup(NlpCriticContextType type, String group);   
    
    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_NLP_CONTEXTS })
    public List<NlpCriticContextType> listContextTypes();
    
    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_NLP_CONTEXTS })
    public NlpCriticContextType findContextTypeByName(String name);
    
    @Authorized( { NCDConstants.PRIV_ADD_NLP_CONTEXTS })
    public void saveNlpCriticContextType(NlpCriticContextType type);

    @Authorized( { NCDConstants.PRIV_DELETE_NLP_CONTEXTS })
    public void deleteNlpCriticContextType(NlpCriticContextType type);
    
    @Authorized( { NCDConstants.PRIV_ADD_NLP_CONTEXTS })
    public void saveNlpCriticContext(NlpCriticContext context);

    @Authorized( { NCDConstants.PRIV_DELETE_NLP_CONTEXTS })
    public void deleteNlpCriticContext(NlpCriticContext context);

    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_NLP_CONTEXTS })
    public List<NlpCriticContextGroup> listContextGroups();

    //-----------------------------------------
    // Aggregate summary report operations
    //-----------------------------------------

    /**
     * Save a new report, or the changes to an existing report.
     * 
     * @param task The scheduled task representing the report.
     * @param monitoredConditions The List of monitored conditions for the
     * report, if any.
     */
    public void saveReport(TaskDefinition task, List<MonitoredCondition> monitoredConditions);
    
    /**
     * Extract "mock" aggregate summary report data based on the supplied
     * filtering parameters. This data depends only on the parameters,
     * and the condition and condition group data in the database, not
     * the actual reported results for the specified date ranges.
     */
    @Transactional(readOnly=true)
    @Authorized()
    public ReportData getFakeCountAggregateSummaryData(Date[] bucketDates, Map<String, String> properties);
    
    /**
     * Extract real aggregate summary report data based on the supplied
     * filtering parameters.
     */
    @Transactional(readOnly=true)
    @Authorized()
    public ReportData getAggregateSummaryData(Date[] bucketDates, Map<String, String> properties);

    //-----------------------------------------
    // Export operations
    //-----------------------------------------
    
    /**
     * Extract real export data based on the supplied filtering
     * parameters, and pass the rows to the supplied sink. This version
     * extracts the data via the "old" tables, principally
     * ncd_reportable_result.
     *
     * @param properties The configured properties to control this extract
     * operation, from the TaskDefinition normally.
     * @param feedLog The DataFeedLog to send information about the status
     * of the extract operation to.
     * @param sink The DataFeedSink to which the extracted rows should be
     * @param status The TaskRunStatus used to record status information
     * about this extract.
     * passed. 
     */
    @Authorized( { NCDConstants.PRIV_VIEW_REPORTABLE_RESULTS })
    public void extractOld(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status);
    
    /**
     * Extract daily export data based on the supplied filtering
     * parameters, and pass the rows to the supplied sink.
     *
     * @param properties The configured properties to control this extract
     * operation, from the TaskDefinition normally.
     * @param feedLog The DataFeedLog to send information about the status
     * of the extract operation to.
     * @param sink The DataFeedSink to which the extracted rows should be
     * @param status The TaskRunStatus used to record status information
     * about this extract.
     * passed. 
     */
    @Authorized( { NCDConstants.PRIV_VIEW_REPORTABLE_RESULTS, NCDConstants.PRIV_VIEW_DECIDED_RESULTS })
    public void extractDaily(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status);
    
    //-----------------------------------------
    // Dwyer condition operations
    //-----------------------------------------
    
    /**
     * 
     * Find the condition object using the name of the condition.
     * 
     * @param name The name of the condition
     * @return The Condition object found using the name.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public Condition findConditionByName(String name);
    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public Condition findConditionById(Long id);
    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public ConditionGroup findConditionGroupByName(String name);
    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public ConditionGroup findConditionGroupById(Integer id);
    
    /** Get a list of all defined condition names, in increasing
     * lexicographic order.
     * @return a List<String> containing all default condition names.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public List<String> getAllConditionNames();
    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public List<String> getAllConditionNamesExcludeRetired();

    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public List<Condition> getAllConditions();
    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public List<Condition> getAllConditionsExcludeRetired();
    @Authorized( { NCDConstants.PRIV_ADD_CONDITIONS, NCDConstants.PRIV_EDIT_CONDITIONS } )
    public void saveCondition(Condition condition);
    @Authorized( { NCDConstants.PRIV_DELETE_CONDITIONS } )
    public void deleteCondition(Condition condition);
    
    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public List<ConditionGroup> getAllConditionGroups();
    @Authorized( { NCDConstants.PRIV_VIEW_CONDITIONS } )
    public List<ConditionGroup> getAllConditionGroupsExcludeRetired();
    @Authorized( { NCDConstants.PRIV_ADD_CONDITIONS, NCDConstants.PRIV_EDIT_CONDITIONS } )
    public void saveConditionGroup(ConditionGroup group);
    @Authorized( { NCDConstants.PRIV_DELETE_CONDITIONS } )
    public void deleteConditionGroup(ConditionGroup group);
    
    //-----------------------------------------
    // Institution operations
    //-----------------------------------------
    
    /** Find an institution by name */
    @Transactional(readOnly=true)
    @Authorized()
    public Institution findInstitutionByName(String name);
    
    /** Get a list of all defined institutions, in increasing
     * lexicographic order.
     * @return a List<String> containing all institutions.
     */
    @Transactional(readOnly=true)
    @Authorized()
    public List<String> getAllInstitutionNames();
    
    @Transactional(readOnly=true)
    @Authorized()
    public List<Institution> getAllInstitutions();
    
    @Transactional(readOnly=true)
    @Authorized()
    public List<Institution> getAllActiveInstitutions();
    
    @Transactional(readOnly=true)
    @Authorized()
    public Institution getInstitution(long id);
    
    @Authorized()
    public void saveInstitution(Institution institution);
    
    @Authorized()
    public void deleteInstitution(Institution institution);
    
    //-----------------------------------------
    // county and jurisdiction inference operations
    //-----------------------------------------
    
    @Transactional(readOnly=true)
    @Authorized()
    public County findCountyByZipcode(String zipcode);
    
    @Transactional(readOnly=true)
    @Authorized()
    public County findCountyByName(String name);
    
    @Transactional(readOnly=true)
    @Authorized()
    public County findCountyByNameAndState(String name, String state);
    
    @Transactional(readOnly=true)
    @Authorized()
    public Jurisdiction findJurisdictionByZipcode(String zipcode);
    
    @Transactional(readOnly=true)
    @Authorized()
    public Jurisdiction findJurisdictionByName(String name);
    
    @Transactional(readOnly=true)
    @Authorized()
    public List<County> listCounties();
    
    @Transactional(readOnly=true)
    @Authorized()
    public List<Jurisdiction> listJurisdictions();

    //-----------------------------------------
    // task status operations
    //-----------------------------------------
    
    /** Add a status message for a task
     * 
     * @param status The new status message to be added.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_SCHEDULED_REPORTS } )
    public void addTaskStatus(TaskRunStatus status);

    /** Gets the most recent successful status for the specified task.
     * 
     * @param task The task for which to get the status.
     * @return The most recent successful status for the specified task, or
     * null if the task has no recorded successful statuses.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_SCHEDULED_REPORTS } )
    public TaskRunStatus getLatestTaskStatus(TaskDefinition task);

    /** Get the most recent task status records for the dashboard.
     * 
     * @param statusCount The number of status returns to return.
     * @return A list of at most the specified number of status records,
     * most recent first.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_SCHEDULED_REPORTS } )
    public List<TaskRunStatus> getRecentTaskStatus(int statusCount);
    
    /** Prunes task run status history older than the specified age.
     * 
     * @param maxAgeDays The maximum age (in days) to keep task run status
     * history.  Task run status older than this is removed from the database.  
     */
    @Authorized( { NCDConstants.PRIV_VIEW_SCHEDULED_REPORTS } )
    public void pruneTaskStatus(int maxAgeDays);
    
    //-----------------------------------------
    // decided result operations
    //-----------------------------------------

    /** Persists a single decided result and its archived copy, returning
	 * the archived copy.
	 * 
	 * @param result
	 * @return
	 */
    @Authorized( { NCDConstants.PRIV_ADD_DECIDED_RESULTS, NCDConstants.PRIV_EDIT_DECIDED_RESULTS } )
	public DecidedResultArchive saveDecidedResult(DecidedResult result);
    
    /**
     * Persists a particular decided result to the DB.
     * @param decidedResult The decided result to store in the DB.
     * @return A copy of the saved DecidedResultArchive with the
     * changes made by the DB.
     */
    @Authorized( { NCDConstants.PRIV_ADD_DECIDED_RESULTS, NCDConstants.PRIV_EDIT_DECIDED_RESULTS } )
    public List<DecidedResultArchive> saveDecidedResults(List<DecidedResult> decidedResults);
    
    /**
     * Determine if the specified OBR, OBX, and NTE combination
     * have been seen before so we can reuse the answer.
     * @param obr The OBR related to the result under examination.
     * @param obx The OBX under examination.
     * @param nte The NTE related to the result under examination.
     * @return The DecidedResult object that corresponds to the
     * specified OBR, OBX, and NTE or null if no DB rows
     * correspond.
     */
    @Transactional(readOnly=true)
    @Authorized( { NCDConstants.PRIV_VIEW_DECIDED_RESULTS } )
    public List<DecidedResult> findDecidedResults(DecidedResult decidedResultTemplate);

    /** Get a decided result by its id
     * @param id The primary key of the instance to get.
     * @return The decided result with the matching primary key value, or
     * null if there is no such decided result.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_DECIDED_RESULTS } )
    public DecidedResult getDecidedResult(long id);

    /** Find decided results which match a filter
     * @param filter The search criteria.
     * @return A list of decided results that match the search criteria.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_DECIDED_RESULTS } )
    public SearchResult<DecidedResult> findDecidedResults(SearchFilterDecidedResults filter);

    /**
     * Removes (delete) the listed DecidedResults.
     * @param resultsToRemove The list of decided results to be removed.
     */
    @Authorized( { NCDConstants.PRIV_DELETE_DECIDED_RESULTS } )
    public void removeDecidedResults(List<DecidedResult> resultsToRemove);

    /**
     * 
     * Find a CriticDef object based on the id.
     * 
     * @param id The id of the critic to find.
     * @return The CriticDef object with the specified id.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_CRITICS } )
    public CriticDef findCriticById(Long id);

    @Authorized( { NCDConstants.PRIV_VIEW_CRITICS } )
    public List<CriticDef> getAllCritics();
    
    //-----------------------------------------
    // alert summary operations
    //-----------------------------------------
    
    /** Add an alert summary
     * 
     * @param alertSummary The new alert summary to be added.
     */
    @Authorized( { NCDConstants.PRIV_ADD_ALERTS } )
    public void addAlertSummary(AlertSummary alertSummary);

    /** Find alert summaries which match a filter
     * @param filter The search criteria.
     * @return A list of alert summaries that match the search criteria.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_ALERTS } )
    @Transactional(readOnly=true)
    public SearchResult<AlertSummary> findAlertSummaries(SearchFilterAlertSummary filter);
    
    /** Dismisses an alert summary with an optional reason
     * 
     * @param alertSummary The alert summary to dismiss.
     * @param reason An optional reason for the dismissal.
     */
    @Authorized( { NCDConstants.PRIV_EDIT_ALERTS } )
    public void dismissAlertSummary(AlertSummary alertSummary, String reason);
    
    /** Dismisses several alert summaries with an optional reason
     * 
     * @param alertSummaries The alert summaries to dismiss.
     * @param reason An optional reason for the dismissal.
     */
    @Authorized( { NCDConstants.PRIV_EDIT_ALERTS } )
    public void dismissAlertSummaries(List<AlertSummary> alertSummaries, String reason);
    
    /** Undismisses an alert summary
     * 
     * @param alertSummary The alert summary to dismiss.
     */
    @Authorized( { NCDConstants.PRIV_EDIT_ALERTS } )
    public void undismissAlertSummary(AlertSummary alertSummary);
    
    /** Undismisses several alert summaries
     * 
     * @param alert summaries The alert summaries to undismiss.
     */
    @Authorized( { NCDConstants.PRIV_EDIT_ALERTS } )
    public void undismissAlertSummaries(List<AlertSummary> alertSummaries);
    
    /**
     * Find an alert type by id
     * @param id
     * @return The AlertType for the specified id.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_ALERTS } )
    @Transactional(readOnly=true)
    public AlertType findAlertTypeById(int id);
    
    /**
     * Return all alert types
     * @return The alert types as a ArrayList<Pair<Integer,String>>.
     */
    @Authorized()
    public ArrayList<Pair<Integer, String>> getAlertTypes();

    //-----------------------------------------
    // Condition rate monitoring operations
    //-----------------------------------------

    /**
     * Find a monitored Entity:Condition mapping by entity and code.
     * 
     * @param task The task that owns the the mapping to be searched for.
     * @param application The application to be searched for.
     * @param facility The facility to be searched for.
     * @param location The location to be searched for.
     * @param condition The condition to be searched for.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_MONITORED_CODES } )
    public MonitoredCondition getMonitoredCondition(TaskDefinition task, String application, String facility, String location, Condition condition);

    /**
     * Find all MonitoredConditions for the specified task.
     * 
     * @param task The task for which the MonitoredConditions are to be fetched.
     * @return A List of the MonitoredConditions for the task.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_MONITORED_CODES } )
    public List<MonitoredCondition> getMonitoredConditions(TaskDefinition task);

    /**
     * Replace all existing monitored conditions for a task by a new collection.
     * 
     * @param task The task whose monitored conditions are to be replaced.
     * @param monitoredConditions The new collection of monitored conditions.
     */
    @Authorized( { NCDConstants.PRIV_ADD_MONITORED_CODES, NCDConstants.PRIV_EDIT_MONITORED_CODES } )
    public void setMonitoredConditions(TaskDefinition task, List<MonitoredCondition> monitoredConditions);
    
    /**
     * Create or modify a monitored Entity:Condition mapping. 
     * 
     * @param entry The mapping to be created or modified.
     */
    @Authorized( { NCDConstants.PRIV_ADD_MONITORED_CODES, NCDConstants.PRIV_EDIT_MONITORED_CODES } )
    public void saveMonitoredCondition(MonitoredCondition entry);

    /**
     * Delete a monitored Entity:Condition mapping.
     * 
     * @param entry The mapping to be deleted.
     */
    @Authorized( { NCDConstants.PRIV_DELETE_MONITORED_CODES } )
    public void deleteMonitoredCondition(MonitoredCondition entry);

    /**
     * Gets a list containing all the app/loc/condition combinations that are
     * flagged for monitoring, and for which _no_ reportable results were
     * detected in the specified date/time window.
     * 
     * @param task The task whose MonitoredConditions should be used.
     * @param window The date/time window over which to search.
     * @return The list of app/loc/condition combinations not seen.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_MONITORED_CODES } )
    public List<ZeroCountCondition> getZeroCountConditions(TaskDefinition task, DateRange window);
    
    /**
     * Gather the number of occurrences of each (app, loc, condition) triple
     * in reportable results in the two specified date/time windows, for
     * triples that appear at all.
     * 
     * @param currentWindow
     * @param historicalWindow
     * @return A List of each (app, loc, condition) triple that occurred at
     * least once in at least one of the two date/time windows, with the
     * number of times that triple occurred in each window.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_MONITORED_CODES } )
    public List<ConditionCount> getConditionCounts(DateRange currentWindow, DateRange historicalWindow);

    /**
     * Gather the number of occurrences of each (app, loc, condition) triple
     * in reportable results in the two specified date/time windows, for
     * triples that appear at all, returning only those for which the
     * occurrence rate is "unusual".
     * 
     * @param sampleWindow
     * @param historyDays The number of days prior to the sampleWindow to be
     * examined to determine what the "usual" rate is for each triple.
     * @return
     */
    @Authorized( { NCDConstants.PRIV_VIEW_MONITORED_CODES } )
    public List<ConditionCount> getUnusualConditionRates(DateRange sampleWindow, UnusualConditionRateFilter filter);
    
    //-----------------------------------------
    // Codes, code types and code systems operations
    //-----------------------------------------

    /**
     * Gets a list containing all defined codes with matching code type and
     * code system.
     * 
     * @param typeName The name of the code type for matching codes.
     * @param systemName The name of the code system for matching codes.
     * @return A list of matching codes, order unspecified.
     */
    @Authorized()
    public List<Code> findCodes(String typeName, String systemName);
    @Authorized()
    public List<Code> findCodesExcludeRetired(String typeName, String systemName);

    /**
     * Gets the unique code with the specified code system and code value.
     * 
     * @param systemName The name of the code system for the matching code.
     * @param codeValue The code value of the code for the matching code.
     * @return The matching code, or null if there is no matching code.
     */
    @Authorized()
    public Code getCode(String systemName, String codeValue);

    /**
     * Gets the unique code with the specified code system and code value.
     * 
     * @param systemName The name of the code system for the matching code.
     * @param codeValue The code value of the code for the matching code.
     * @return The matching code, or null if there is no matching code.
     */
    @Authorized()
    public Code getCode(CodeSystem codeSystem, String codeValue);
    
    /**
     * Gets the specified code type by internal name
     * 
     * @param name The name of the code type to get, such as "diagnosis",
     * "patientsex", etc.
     * @return The named code type, or null if there is no such code type.
     */
    @Authorized()
    public CodeType getCodeType(String name);
    
    /**
     * Gets the code system with the specified internal name.
     * 
     * @param name The name of the code system to get, such as "I9" or "LN".
     * @return The named code system, or null if there is no such code system.
     */
    @Authorized()
    public CodeSystem getCodeSystem(String name);

    /** Find codes which match a filter
     * @param filter The search criteria.
     * @return A list of codes that match the search criteria.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_CODES } )
    public SearchResult<Code> findCodes(SearchFilterCodes filter);
    
    /**
     * Gets a list of all the code types known, in ascending display text
     * order.
     * 
     * @return a List of all the code types known, in ascending display
     * text order.
     */
    @Authorized()
    public List<CodeType> getAllCodeTypes();
    @Authorized()
    public List<CodeType> getAllCodeTypesExcludeRetired();

    /**
     * Gets a list of all the code systems known, in ascending display text
     * order.
     * 
     * @return a List of all the code systems known, in ascending display
     * text order.
     */
    @Authorized()
    public List<CodeSystem> getAllCodeSystems();
    @Authorized()
    public List<CodeSystem> getAllCodeSystemsExcludeRetired();

    @Authorized( { NCDConstants.PRIV_ADD_CODES, NCDConstants.PRIV_EDIT_CODES } )
    public void saveCode(Code code);
    public void saveCodeSystem(CodeSystem codeSystem);
    public void saveCodeType(CodeType codeType);
    public Code getCode(Long id);
    public CodeSystem getCodeSystem(Long id);
    public CodeType getCodeType(Long id);
    
    //-----------------------------------------
    // message count summary operations
    //-----------------------------------------
    
    /**
     * Record a message count summary.
     * 
     * @param messageCountSummary The message count summary to be added.
     */
    @Authorized( { NCDConstants.PRIV_ADD_MESSAGE_COUNTS } )
    public void addMessageCountSummary(MessageCountSummary messageCountSummary);
    
    /** Find message count summaries which match a filter
     * @param filter The search criteria.
     * @return A list of message count summaries that match the search criteria.  Sorted in descending date order.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_MESSAGE_COUNTS } )
    public List<MessageCountSummary> findMessageCountSummaries(SearchFilterMessageCountSummary filter);
    
    /**
     * Resets the message count summaries
     * @return the number of rows deleted.
     */
    @Authorized( { NCDConstants.PRIV_RESET_MESSAGE_COUNTS } )
    public int resetMessageCountSummaries();

    //-----------------------------------------
    // System activity operations
    //-----------------------------------------
    
    @Authorized()
    public void addSystemEvent(SystemActivityEvent event);

    @Authorized()
    public List<SystemActivityEvent> findSystemEvents(SystemEventFilter filter);

    //-----------------------------------------
    // review status operations
    //-----------------------------------------
    
    /**
     * Find a review status type by id
     * @param id
     * @return The ManualReviewStatusType for the specified id.
     */
    @Authorized()
    public ManualReviewStatusType findReviewStatusTypeById(int id);

    /**
     * Return all review status types
     * @return The review statuses as a ArrayList<Pair<Integer,String>>.
     */
    @Authorized()
    public ArrayList<Pair<Integer, String>> getReviewStatusTypes();

    //-----------------------------------------
    // exported result operations
    //-----------------------------------------
    
    /** Add an exported result
     * 
     * @param status The new exported result to be added.
     */
    @Authorized()
    public void addExportedResult(ExportedResult exportedResult);

    //-----------------------------------------
    // export recipient operations
    //-----------------------------------------
    
    /** Add an export recipient
     * 
     * @param status The new export recipient to be added.
     */
    @Authorized()
    public void addExportRecipient(ExportRecipient exportRecipient);
    
    @Authorized()
    public List<NlpDiscreteTerm> getNlpDiscreteTermsByNegative(boolean isNegative);
    @Authorized()
    public List<NlpDiscreteTerm> getAllNlpDiscreteTerms();
    @Authorized()
    public void saveNlpDiscreteTerm(NlpDiscreteTerm term);
    @Authorized()
    public void deleteNlpDiscreteTerm(NlpDiscreteTerm term);

    //-----------------------------------------
    // ProcessedMessageCount operations
    //-----------------------------------------
    
    public IProcessedMessageCountDAO getProcessedMessageCountDAO();
    public void setProcessedMessageCountDAO(IProcessedMessageCountDAO dao);
    
    @Authorized()
    public void countProcessedMessage(String application, String facility,
    		String location, Date processedDateTime, String mpqSeqNumber);

    //-----------------------------------------
    // Data Source Report operations
    //-----------------------------------------
    
    public IDataSourceReportDAO getDataSourceReportDAO();
    public void setDataSourceReportDAO(IDataSourceReportDAO dao);
    
    @Authorized()
    public List<DataSourceInfo> findDataSourceInfo(TaskDefinition task, DataSourceReportFilter filter);
}

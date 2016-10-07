package org.openmrs.module.ncd.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
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
import org.openmrs.module.ncd.database.dao.ICountyDAO;
import org.openmrs.module.ncd.database.dao.ICriticDAO;
import org.openmrs.module.ncd.database.dao.IDataSourceReportDAO;
import org.openmrs.module.ncd.database.dao.IDecidedResultDAO;
import org.openmrs.module.ncd.database.dao.IErrorDAO;
import org.openmrs.module.ncd.database.dao.IExportRecipientDAO;
import org.openmrs.module.ncd.database.dao.IExportedResultDAO;
import org.openmrs.module.ncd.database.dao.IExtractDAO;
import org.openmrs.module.ncd.database.dao.IInstitutionDAO;
import org.openmrs.module.ncd.database.dao.IJurisdictionDAO;
import org.openmrs.module.ncd.database.dao.IMessageCountSummaryDAO;
import org.openmrs.module.ncd.database.dao.INlpCriticConceptDAO;
import org.openmrs.module.ncd.database.dao.INlpCriticContextDAO;
import org.openmrs.module.ncd.database.dao.INlpDiscreteTermDAO;
import org.openmrs.module.ncd.database.dao.IProcessedMessageCountDAO;
import org.openmrs.module.ncd.database.dao.IProducerDAO;
import org.openmrs.module.ncd.database.dao.IRateMonitoringDAO;
import org.openmrs.module.ncd.database.dao.IRawMessageDAO;
import org.openmrs.module.ncd.database.dao.IReportableResultDAO;
import org.openmrs.module.ncd.database.dao.IReportingDAO;
import org.openmrs.module.ncd.database.dao.IReviewStatusDAO;
import org.openmrs.module.ncd.database.dao.ISystemActivityDAO;
import org.openmrs.module.ncd.database.dao.ITaskStatusDAO;
import org.openmrs.module.ncd.database.dao.IZipcodeDAO;
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

public class ConditionDetectorServiceImpl implements ConditionDetectorService {
    
    /** The DAO class for HL7Producer entities */
    private IProducerDAO producerDAO;    

    /** The DAO class for CodeCondition entities */
    private ICodeConditionDAO codeConditionDAO;

    /** The DAO class for CodeFrequency entities */
    private ICodeFrequencyDAO codeFrequencyDAO;

    /** The DAO class for DecidedResult entities */
    private IDecidedResultDAO decidedResultDAO;

    /** The DAO class for Error entities */
    private IErrorDAO errorDAO;

    /** The DAO class for rawhl7 entities */
    private IRawMessageDAO rawMessageDAO;

    /** The DAO class for ReportableResult entities */
    private IReportableResultDAO reportableResultDAO;

    /** The DAO class for Institution entities */
    private IInstitutionDAO institutionDAO;

    /** The DAO class for NlpCriticConcept entities for Rex */
    private INlpCriticConceptDAO nlpCriticConceptDAO;

    /** The DAO class for NlpCriticContext entities for Rex */
    private INlpCriticContextDAO NlpCriticContextDAO;   

    /** The DAO class for reporting operations */
    private IReportingDAO reportingDAO;   

    /** The DAO class for data feed extraction operations */
    private IExtractDAO extractDAO;   

    /** The DAO class for Condition entities */
    private IConditionDAO conditionDAO;
    
    private IZipcodeDAO zipcodeDAO;
    
    private ICountyDAO countyDAO;
    
    private IJurisdictionDAO jurisdictionDAO;

    /** The DAO class for task status entities */
    private ITaskStatusDAO taskStatusDAO;
    
    private ICriticDAO criticDAO;

    /** The DAO class for alert summary entities */
    private IAlertSummaryDAO alertSummaryDAO;

    /** The DAO class for code rate monitoring entities */
    private IRateMonitoringDAO rateMonitoringDAO;
    
    /** The DAO class for code, code system and code type entities */
    private ICodeDAO codeDAO;

    /** The DAO class for message count summary entities */
    private IMessageCountSummaryDAO messageCountSummaryDAO;

    /** The DAO class for review status */
    private IReviewStatusDAO reviewStatusDAO;

    /** The DAO class for exported result entities */
    private IExportedResultDAO exportedResultDAO;

    /** The DAO class for export recipient entities */
    private IExportRecipientDAO exportRecipientDAO;
    
    /** The DAO class for discrete terms used by the NLPDiscreteAnalyzer */
    private INlpDiscreteTermDAO nlpDiscreteTermDAO;

    // **********************
    // Spring-only methods *
    // **********************

    /**
     * Sets the DAO object for HL7Producer entities. Called only by
     * the spring framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setProducerDAO(IProducerDAO dao) {
        this.producerDAO = dao;
    }
    
    public IProducerDAO getProducerDAO() {
        return this.producerDAO;
    }

    /**
     * Sets the DAO object for tblloinccode entities. Called only by the spring
     * framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setCodeConditionDAO(ICodeConditionDAO dao) {
        this.codeConditionDAO = dao;
    }
    
    public ICodeConditionDAO getCodeConditionDAO() {
        return this.codeConditionDAO;
    }

    /**
     * Sets the DAO object for tblloincfrequency entities. Called only by the
     * spring framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setCodeFrequencyDAO(ICodeFrequencyDAO dao) {
        this.codeFrequencyDAO = dao;
    }
    
    public ICodeFrequencyDAO getCodeFrequencyDAO() {
        return this.codeFrequencyDAO;
    }

    /**
     * Sets the DAO object for tbldecidedresult entities. Called only by the
     * spring framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setDecidedResultDAO(IDecidedResultDAO dao) {
        this.decidedResultDAO = dao;
    }
    
    public IDecidedResultDAO getDecidedResultDAO() {
        return this.decidedResultDAO;
    }

    /**
     * Sets the DAO object for tblerror entities. Called only by the
     * spring framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setErrorDAO(IErrorDAO dao) {
        this.errorDAO = dao;
    }
    
    public IErrorDAO getErrorDAO() {
        return this.errorDAO;
    }

    /**
     * Sets the DAO object for tblrawhl7 entities. Called only by the
     * spring framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setRawMessageDAO(IRawMessageDAO dao) {
        this.rawMessageDAO = dao;
    }
    
    public IRawMessageDAO getRawMessageDAO() {
        return this.rawMessageDAO;
    }

    /**
     * Sets the DAO object for tblreportableresult entities. Called only by the
     * spring framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setReportableResultDAO(IReportableResultDAO dao) {
        this.reportableResultDAO = dao;
    }
    
    public IReportableResultDAO getReportableResultDAO() {
        return this.reportableResultDAO;
    }

    /**
     * Sets the DAO object for tblinstitution entities. Called only by the
     * spring framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setInstitutionDAO(IInstitutionDAO dao) {
        this.institutionDAO = dao;
    }
    
    public IInstitutionDAO getInstitutionDAO() {
        return this.institutionDAO;
    }

    /**
     * Sets the DAO object for concept_con_cdp entities. Called only by the
     * spring framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setNlpCriticConceptDAO(INlpCriticConceptDAO dao) {
        this.nlpCriticConceptDAO = dao;
    }
    
    public INlpCriticConceptDAO getNlpCriticConceptDAO() {
        return this.nlpCriticConceptDAO;
    }

    /**
     * Sets the DAO object for context* entities. Called only by the
     * spring framework based on the moduleApplicationContext.
     * 
     * @param dao The DAO instance.
     */
    public void setNlpCriticContextDAO(INlpCriticContextDAO dao) {
        this.NlpCriticContextDAO = dao;
    }
    
    public INlpCriticContextDAO getNlpCriticContextDAO() {
        return this.NlpCriticContextDAO;
    }

    public IReportingDAO getReportingDAO() {
        return reportingDAO;
    }

    public void setReportingDAO(IReportingDAO reportingDAO) {
        this.reportingDAO = reportingDAO;
    }

    public IExtractDAO getExtractDAO() {
        return extractDAO;
    }

    public void setExtractDAO(IExtractDAO extractDAO) {
        this.extractDAO = extractDAO;
    }

    public void setConditionDAO(IConditionDAO dao) {
        this.conditionDAO = dao;
    }
    
    public IConditionDAO getConditionDAO() {
        return this.conditionDAO;
    }
    
    public IZipcodeDAO getZipcodeDAO() {
        return this.zipcodeDAO;
    }
    
    public void setZipcodeDAO(IZipcodeDAO dao) {
        this.zipcodeDAO = dao;
    }
    
    public ICountyDAO getCountyDAO() {
        return this.countyDAO;
    }
    
    public void setCountyDAO(ICountyDAO dao) {
        this.countyDAO = dao;
    }
    
    public IJurisdictionDAO getJurisdictionDAO() {
        return this.jurisdictionDAO;
    }
    
    public void setJurisdictionDAO(IJurisdictionDAO dao) {
        this.jurisdictionDAO = dao;
    }

    public void setTaskStatusDAO(ITaskStatusDAO dao) {
        this.taskStatusDAO = dao;
    }
    
    public ITaskStatusDAO getTaskStatusDAO() {
        return this.taskStatusDAO;
    }

    public void setAlertSummaryDAO(IAlertSummaryDAO dao) {
        this.alertSummaryDAO = dao;
    }
    
    public IAlertSummaryDAO getAlertSummaryDAO() {
        return this.alertSummaryDAO;
    }
    
    public void setRateMonitoringDAO(IRateMonitoringDAO dao) {
        this.rateMonitoringDAO = dao;
    }
    
    public IRateMonitoringDAO getRateMonitoringDAO() {
        return this.rateMonitoringDAO;
    }
    
    public void setCodeDAO(ICodeDAO dao) {
        this.codeDAO = dao;
    }
    
    public ICodeDAO getCodeDAO() {
        return this.codeDAO;
    }
    
    public void setCriticDAO(ICriticDAO dao) {
        this.criticDAO = dao;
    }
    
    public ICriticDAO getCriticDAO() {
        return this.criticDAO;
    }
    
	public IMessageCountSummaryDAO getMessageCountSummaryDAO() {
		return messageCountSummaryDAO;
	}

	public void setMessageCountSummaryDAO(IMessageCountSummaryDAO messageCountSummaryDAO) {
		this.messageCountSummaryDAO = messageCountSummaryDAO;
	}
	
	public IReviewStatusDAO getReviewStatusDAO() {
		return reviewStatusDAO;
	}

	public void setReviewStatusDAO(IReviewStatusDAO dao) {
		this.reviewStatusDAO = dao;
	}
	
	public IExportedResultDAO getExportedResultDAO() {
		return exportedResultDAO;
	}

	public void setExportedResultDAO(IExportedResultDAO exportedResultDAO) {
		this.exportedResultDAO = exportedResultDAO;
	}

	public IExportRecipientDAO getExportRecipientDAO() {
		return exportRecipientDAO;
	}

	public void setExportRecipientDAO(IExportRecipientDAO exportRecipientDAO) {
		this.exportRecipientDAO = exportRecipientDAO;
	}
		
	public INlpDiscreteTermDAO getNlpDiscreteTermDAO() {
		return nlpDiscreteTermDAO;
	}
	
	public void setNlpDiscreteTermDAO(INlpDiscreteTermDAO discreteTermDAO) {
		this.nlpDiscreteTermDAO = discreteTermDAO;		
	}

	// ***************************
    // Public interface methods *
    // ***************************    
	
    public List<CodeCondition> findByCodeAndSystem(String code, String system) {

        return codeConditionDAO.findByCodeAndSystem(code, system);
    }
    
    public CodeCondition findByCodeAndCondition(String code, String system, String condition) {
        return codeConditionDAO.findByCodeAndCondition(code, system, condition);
    }

    /**
     * Increment the frequency of the loinc code for the particular date and
     * institution.
     * 
     * @param date The date the loinc code was reported.
     * @param application The HL7 application that reported the loinc code.
     * @param facility The HL7 facility that reported the loinc code.
     * @param location The HL7 location that reported the loinc code.
     * @param code The code reported.
     * @param codeSystem The code system for the code reported.
     * @param patientZipCode The zipcode of the patient.
     * @param instituteZipCode The zipcode of the institute.
     * @param doctorZipCode The zipcode of the doctor.
     */
    public void incrementCodeFrequency(Date date, 
            String application, String facility, String location,
            String code, String codeSystem, 
            String patientZipCode, 
            String instituteZipCode, String doctorZipCode) throws CodeFrequencyStorageException {

        codeFrequencyDAO.incrementCodeFrequency(date, application, 
                facility, location, code, codeSystem, patientZipCode,
                instituteZipCode, doctorZipCode);
    }

    /**
     * Find the code frequency row that corresponds to passed in date,
     * institute, code, and zip codes.
     * 
     * @param date The date the loinc code was reported.
     * @param application The HL7 application that reported the loinc code.
     * @param facility The HL7 facility that reported the loinc code.
     * @param location The HL7 location that reported the loinc code.
     * @param code The code reported.
     * @param codeSystem The code system for the code reported.
     * @param patientZipCode The zipcode of the patient.
     * @param instituteZipCode The zipcode of the institute.
     * @param doctorZipCode The zipcode of the doctor.
     * @return The loinc frequency row that corresponds to the date, institute,
     *         and loinc code.
     */
    public CodeFrequency findCodeFrequency(Date date,
            String application, String facility, String location,
            String code, String codeSystem,
            String patientZipCode, String instituteZipCode, 
            String doctorZipCode) {

        return codeFrequencyDAO.findCodeFrequency(date, 
                application, facility, location, code, codeSystem,
                patientZipCode, instituteZipCode, doctorZipCode);
    }

    /**
     * Stores any cached loinc frequency data.
     */
    public void saveCodeFrequencyMap() throws CodeFrequencyStorageException {
        codeFrequencyDAO.saveCodeFrequencyMap();
    }

	public DecidedResultArchive saveDecidedResult(DecidedResult result) {
        return decidedResultDAO.saveDecidedResult(result);
	}
	
    /**
     * Persists a particular decided result to the DB.
     * 
     * @param decidedResult The decided result to store in the DB.
     * @return A copy of the saved DecidedResult with the changes made by the
     *         DB.
     */
    public List<DecidedResultArchive> saveDecidedResults(List<DecidedResult> decidedResults) {
        return decidedResultDAO.saveDecidedResults(decidedResults);
    }

    /**
     * Determine if the specified OBR, OBX, and NTE combination have been seen
     * before so we can reuse the answer.
     * 
     * @param obr The OBR related to the result under examination.
     * @param obx The OBX under examination.
     * @param nte The NTE related to the result under examination.
     * @return The DecidedResult object that corresponds to the specified
     *         OBR, OBX, and NTE or null if no DB rows correspond.
     */
    public List<DecidedResult> findDecidedResults(DecidedResult decidedResultTemplate) {
        return decidedResultDAO.findDecidedResults(decidedResultTemplate);
    }

    public DecidedResult getDecidedResult(long id) {
        
        return decidedResultDAO.getDecidedResult(id);
    }

    public SearchResult<DecidedResult> findDecidedResults(SearchFilterDecidedResults filter) {

        return decidedResultDAO.findDecidedResults(filter);
    }

    /**
     * Removes (delete) the listed DecidedResults.
     * @param resultsToRemove The list of decided results to be removed.
     */
    public void removeDecidedResults(List<DecidedResult> resultsToRemove) {
    	decidedResultDAO.removeDecidedResults(resultsToRemove);
    }

    public void storeError(Error errorRow) {

        errorDAO.storeError(errorRow);
    }
    
    public void updateError(Error errorRow) {
    	errorDAO.updateError(errorRow);
    }
    
    public void deleteError(Error errorRow, String dismissReason) {
    	errorDAO.deleteError(errorRow, dismissReason);
    }

    public SearchResult<Error> findErrors(SearchFilterError filter) {
    	return errorDAO.findErrors(filter);
    }
    
    public Error findErrorById(Long id) {
    	return errorDAO.findErrorById(id);
    }

    public Error findErrorByRawMessage(RawMessage message) {
    	return errorDAO.findErrorByRawMessage(message);
    }

    /**
     * Persist the raw HL7 row in the database.
     * 
     * @param rawHL7 The HL7 data to be stored in the form of a RawMessage
     *        object.
     * @return A Serializable that contains the stored row.
     */
    public Serializable saveRawMessage(RawMessage rawHL7) {

        return rawMessageDAO.saveRawMessage(rawHL7);
    }
    
    public void updateRawMessage(RawMessage rawHL7) {
    	rawMessageDAO.updateRawMessage(rawHL7);
    }

    /**
     * Finds the raw HL7 database row based on the raw hl7 row id.
     * @param id The raw HL7 row id which is being looked up.
     * @return The HL7 database row that contains the
     * specified message.
     */
    @Transactional(readOnly=true)
    public RawMessage findRawMessageById(Long id) {

        return rawMessageDAO.findRawMessageById(id);
    }
    
    /**
     * Persist the reportable result data to the database.
     * 
     * @param reportableResult The reportable result data in the form of a
     *        Tblreporableresult object.
     * @return A Serializable that contains the stored data.
     */
    public Serializable saveReportableResults(List<ReportableResult> reportableResult) 
    {
        return reportableResultDAO.saveReportableResults(reportableResult);
    }

    public ReportableResult getReportableResult(long id) {

        return reportableResultDAO.getReportableResult(id);
    }

    public SearchResult<ReportableResult> findReportableResults(SearchFilterReportableResults filter) {

        return reportableResultDAO.findReportableResults(filter);
    }

    public String exportReportableResults(SearchFilterReportableResults filter) {
    
        return reportableResultDAO.exportReportableResults(filter);
    }

    /**
     * Rejects a reportable result that has been held for manual review.
     * This updates its status, and dismisses the associated dashboard alert.
     * 
     * @param result The reportable result to be rejected.
     */
    public void rejectReportableResult(ReportableResult result) {
    	reportableResultDAO.rejectReportableResult(result);
    }

    /**
     * Releases a reportable result that has been held for manual review.
     * This updates its status, sets its release date, dismisses the associated dashboard alert, 
     * and updates the resultSeq in both the reportable result, and in the associated OpenMRS observation.
     * 
     * @param result The reportable result to be released.
     */
    public void releaseReportableResult(ReportableResult result) {
    	reportableResultDAO.releaseReportableResult(result);
    }
    
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
    public void reportableResultSentInError(List<ReportableResult> results) {
    	reportableResultDAO.reportableResultSentInError(results);
    }
    
    /**
     * Marks all reportable results in the list as not sent in error (if not already marked this way).
     * 
     * @param results The list of reportable results to be marked not sent in error.
     */
    public void reportableResultNotSentInError(List<ReportableResult> results) {
    	reportableResultDAO.reportableResultNotSentInError(results);
    }

    /** Getters for Rex configuration lists. */
    public List<NlpCriticConcept> listNlpCriticConcepts() {
        return nlpCriticConceptDAO.list();
    }

    public void saveNlpCriticConcept(NlpCriticConcept concept) {
        nlpCriticConceptDAO.save(concept);
    }
    
    public void deleteNlpCriticConcept(NlpCriticConcept concept) {
        nlpCriticConceptDAO.delete(concept);
    }
    
    public List<NlpCriticContext> findContextByType(NlpCriticContextType type) {
        return NlpCriticContextDAO.findContextByType(type);
    }

    public List<NlpCriticContext> findContextByTypeAndGroup(NlpCriticContextType type, String group) {
        return NlpCriticContextDAO.findContextByTypeAndGroup(type, group);
    }    
    
    public List<NlpCriticContextType> listContextTypes() {
    	return NlpCriticContextDAO.listContextTypes();
    }
    
    public NlpCriticContextType findContextTypeByName(String name) {
    	return NlpCriticContextDAO.findContextTypeByName(name);
    }
    
    public void saveNlpCriticContextType(NlpCriticContextType type) {
        
        NlpCriticContextDAO.saveNlpCriticContextType(type);
    }

    public void deleteNlpCriticContextType(NlpCriticContextType type) {
        
        NlpCriticContextDAO.deleteNlpCriticContextType(type);
    }
    
    public void saveNlpCriticContext(NlpCriticContext context) {
        
        NlpCriticContextDAO.saveNlpCriticContext(context);
    }

    public void deleteNlpCriticContext(NlpCriticContext context) {
        
        NlpCriticContextDAO.deleteNlpCriticContext(context);
    }
    
    public List<NlpCriticContextGroup> listContextGroups() {
        
        return NlpCriticContextDAO.listContextGroups();
    }

    /**
     * Save a new report, or the changes to an existing report.
     * 
     * @param task The scheduled task representing the report.
     * @param monitoredConditions The List of monitored conditions for the
     * report, if any.
     */
    public void saveReport(TaskDefinition task, List<MonitoredCondition> monitoredConditions) {

    	reportingDAO.saveReport(task, monitoredConditions);
    }
    
    /**
     * Extract "mock" aggregate summary report data based on the supplied
     * filtering parameters. This data depends only on the parameters,
     * and the condition and condition group data in the database, not
     * the actual reported results for the specified date ranges.
     */
    public ReportData getFakeCountAggregateSummaryData(Date[] bucketDates, Map<String, String> properties) {
        
        return reportingDAO.getFakeCountAggregateSummaryData(bucketDates, properties);
    }
    
    /**
     * Extract real aggregate summary report data based on the supplied
     * filtering parameters.
     */
    public ReportData getAggregateSummaryData(Date[] bucketDates, Map<String, String> properties) {
        
        return reportingDAO.getAggregateSummaryData(bucketDates, properties);
    }
    
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
     * passed. 
     * @param status The TaskRunStatus used to record status information
     * about this extract.
     */
    public void extractOld(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status) {
        
        extractDAO.extractOld(properties, feedLog, sink, status);
    }
    
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
    public void extractDaily(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status) {
        extractDAO.extractDaily(properties, feedLog, sink, status);
    }
    
    /**
     * 
     * Find the condition object using the name of the condition.
     * 
     * @param name The name of the condition
     * @return The condition as a Condition object.
     */
    public Condition findConditionByName(String name) {
        return conditionDAO.findConditionByName(name);
    }
    
    public Condition findConditionById(Long id) {
        
        return conditionDAO.findConditionById(id);
    }
    
    public ConditionGroup findConditionGroupByName(String name) {
        return conditionDAO.findConditionGroupByName(name);
    }
    
    public ConditionGroup findConditionGroupById(Integer id) {
        
        return conditionDAO.findConditionGroupById(id);
    }

    /** Get a list of all defined condition names, in increasing
     * lexicographic order.
     * @return a List<String> containing all default condition names.
     */
    public List<String> getAllConditionNames() {
        return conditionDAO.getAllConditionNames();
    }
    
    public List<String> getAllConditionNamesExcludeRetired() {
        return conditionDAO.getAllConditionNamesExcludeRetired();
    }
    

    public List<Condition> getAllConditions() {
        
        return conditionDAO.getAllConditions();
    }
    
    public List<Condition> getAllConditionsExcludeRetired() {
        
        return conditionDAO.getAllConditionsExcludeRetired();
    }

    public void saveCondition(Condition condition) {
        
        conditionDAO.saveCondition(condition);
    }
    
    public void deleteCondition(Condition condition) {
        
        conditionDAO.deleteCondition(condition);
    }
    
    public List<ConditionGroup> getAllConditionGroups() {
        
        return conditionDAO.getAllConditionGroups();
    }
    
    public List<ConditionGroup> getAllConditionGroupsExcludeRetired() {
        
        return conditionDAO.getAllConditionGroupsExcludeRetired();
    }
    
    public void saveConditionGroup(ConditionGroup group) {
        
        conditionDAO.saveConditionGroup(group);
    }
    
    public void deleteConditionGroup(ConditionGroup group) {
        
        conditionDAO.deleteConditionGroup(group);
    }

    /** Find an institution by name */
    public Institution findInstitutionByName(String name) {

        return institutionDAO.findInstitutionByName(name);
    }

    /** Get a list of all defined institutions, in increasing
     * lexicographic order.
     * @return a List<String> containing all institutions.
     */
    public List<String> getAllInstitutionNames() {
        return institutionDAO.getAllInstitutionNames();
    }
    
    public List<Institution> getAllInstitutions() {
    	return institutionDAO.getAllInstitutions();
    }
    
    public List<Institution> getAllActiveInstitutions() {
    	return institutionDAO.getAllActiveInstitutions();
    }
    
    public Institution getInstitution(long id) {
        return institutionDAO.getInstitution(id);
    }
    
    public void saveInstitution(Institution institution) {
    	institutionDAO.saveInstitution(institution);
    }

	public void deleteInstitution(Institution institution) {
		institutionDAO.deleteInstitution(institution);
	}
    
    public County findCountyByZipcode(String zipcode) {
        return zipcodeDAO.findCountyByZipcode(zipcode);
    }
    
    public County findCountyByName(String name) {
    	return countyDAO.findCountyByName(name);
    }
    
    public County findCountyByNameAndState(String name, String state) {
    	return countyDAO.findCountyByNameAndState(name, state);
    }
    
    public Jurisdiction findJurisdictionByZipcode(String zipcode) {
        return zipcodeDAO.findJurisdictionByZipcode(zipcode);
    }
    
    public Jurisdiction findJurisdictionByName(String name) {
        return jurisdictionDAO.findJurisdictionByName(name);
    }
    
    public List<County> listCounties() {
        return countyDAO.listCounties();
    }
    
    public List<Jurisdiction> listJurisdictions() {
        return jurisdictionDAO.listJurisdictions();
    }

    //-----------------------------------------------------------
    // Task status operations
    //-----------------------------------------------------------
    
    /**
     * @see org.openmrs.module.ncd.database.dao.hibernate.ITaskStatusDAO#addTaskStatus(org.openmrs.module.ncd.database.TaskRunStatus)
     */
    public void addTaskStatus(TaskRunStatus status) {
        
        taskStatusDAO.addTaskStatus(status);
    }

    /**
     * @see org.openmrs.module.ncd.database.dao.hibernate.ITaskStatusDAO#getLatestTaskStatus(org.openmrs.scheduler.TaskDefinition)
     */
    public TaskRunStatus getLatestTaskStatus(TaskDefinition task) {

        return taskStatusDAO.getLatestTaskStatus(task);
    }

    /**
     * @see org.openmrs.module.ncd.database.dao.hibernate.ITaskStatusDAO#getRecentTaskStatus(int)
     */
    public List<TaskRunStatus> getRecentTaskStatus(int statusCount) {
        
        return taskStatusDAO.getRecentTaskStatus(statusCount);
    }
    
    /** Prunes task run status history older than the specified age.
     * 
     * @param maxAgeDays The maximum age (in days) to keep task run status
     * history.  Task run status older than this is removed from the database.  
     */
    public void pruneTaskStatus(int maxAgeDays) {
    	taskStatusDAO.pruneTaskStatus(maxAgeDays);
    }

    //-----------------------------------------------------------
    // Critic definition operations
    //-----------------------------------------------------------
    
    public CriticDef findCriticById(Long id) {
        return criticDAO.findCriticById(id);
    }

    public List<CriticDef> getAllCritics() {
        return criticDAO.getAllCritics();
    }
    
    //-----------------------------------------------------------
    // Alert Summary operations
    //-----------------------------------------------------------
    
    /** Add an alert summary
     * 
     * @param alertSummary The new alert summary to be added.
     */
    public void addAlertSummary(AlertSummary alertSummary) {
    	
        alertSummaryDAO.addAlertSummary(alertSummary);
    }

    /** Find alert summaries which match a filter
     * @param filter The search criteria.
     * @return A list of alert summaries that match the search criteria.
     */
    public SearchResult<AlertSummary> findAlertSummaries(SearchFilterAlertSummary filter) {
    	return alertSummaryDAO.findAlertSummaries(filter);
    }
    
    /** Dismisses an alert summary with an optional reason
     * 
     * @param alertSummary The alert summary to dismiss.
     * @param reason An optional reason for the dismissal.
     */
    public void dismissAlertSummary(AlertSummary alertSummary, String reason) {
    	alertSummaryDAO.dismissAlertSummary(alertSummary, Context.getAuthenticatedUser(), reason);
    }
    
    /** Dismisses several alert summaries with an optional reason
     * 
     * @param alertSummaries The alert summaries to dismiss.
     * @param reason An optional reason for the dismissal.
     */
    public void dismissAlertSummaries(List<AlertSummary> alertSummaries, String reason) {
    	alertSummaryDAO.dismissAlertSummaries(alertSummaries, Context.getAuthenticatedUser(), reason);
    }
    
    /** Undismisses an alert summary
     * 
     * @param alertSummary The alert summary to undismiss.
     */
    public void undismissAlertSummary(AlertSummary alertSummary) {
    	alertSummaryDAO.undismissAlertSummary(alertSummary);
    }
    
    /** Undismisses several alert summaries
     * 
     * @param alertSummaries The alert summaries to undismiss.
     */
    public void undismissAlertSummaries(List<AlertSummary> alertSummaries) {
    	alertSummaryDAO.undismissAlertSummaries(alertSummaries);
    }
    
    /**
     * Find an alert type by id
     * @param id
     * @return The AlertType for the specified id.
     */
    public AlertType findAlertTypeById(int id) {
    	return alertSummaryDAO.findAlertTypeById(id);
    }

    /**
     * Return all alert types
     * @return The alert types as a ArrayList<Pair<Integer,String>>.
     */
    public ArrayList<Pair<Integer, String>> getAlertTypes() {
    	return alertSummaryDAO.getAlertTypes();
    }

    //-----------------------------------------------------------
    // Condition rate monitoring operations
    //-----------------------------------------------------------

    /**
     * Find a monitored Entity:Condition mapping by entity and code.
     * 
     * @param task The task that owns the the mapping to be searched for.
     * @param application The application to be searched for.
     * @param facility The facility to be searched for.
     * @param location The location to be searched for.
     * @param condition The condition to be searched for.
     */
    public MonitoredCondition getMonitoredCondition(TaskDefinition task, String application, String facility, String location, Condition condition) {
        return rateMonitoringDAO.getMonitoredCondition(task, application, facility, location, condition);
    }

    /**
     * Find all MonitoredConditions for the specified task.
     * 
     * @param task The task for which the MonitoredConditions are to be fetched.
     * @return A List of the MonitoredConditions for the task.
     */
    @Authorized( { NCDConstants.PRIV_VIEW_MONITORED_CODES } )
    public List<MonitoredCondition> getMonitoredConditions(TaskDefinition task) {
    	
        return rateMonitoringDAO.getMonitoredConditions(task);
    }

    /**
     * Replace all existing monitored conditions for a task by a new collection.
     * 
     * @param task The task whose monitored conditions are to be replaced.
     * @param monitoredConditions The new collection of monitored conditions.
     */
    public void setMonitoredConditions(TaskDefinition task, List<MonitoredCondition> monitoredConditions) {

    	rateMonitoringDAO.setMonitoredConditions(task, monitoredConditions);
    }
    
    /**
     * Create or modify a monitored Entity:Condition mapping. 
     * 
     * @param entry The mapping to be created or modified.
     */
    public void saveMonitoredCondition(MonitoredCondition entry) {
        rateMonitoringDAO.saveMonitoredCondition(entry);
   }
 
    /**
     * Delete a monitored Entity:Condition mapping.
     * 
     * @param entry The mapping to be deleted.
     */
    public void deleteMonitoredCondition(MonitoredCondition entry) {
        rateMonitoringDAO.deleteMonitoredCondition(entry);
    }

    /**
     * Gets a list containing all the app/loc/condition combinations that are
     * flagged for monitoring, and for which _no_ reportable results were
     * detected in the specified date/time window.
     * 
     * @param task The task whose MonitoredConditions should be used.
     * @param window The date/time window over which to search.
     * @return The list of app/loc/condition combinations not seen.
     */
    public List<ZeroCountCondition> getZeroCountConditions(TaskDefinition task, DateRange window) {
        return rateMonitoringDAO.getZeroCountConditions(task, window);
    }
    
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
    public List<ConditionCount> getConditionCounts(DateRange currentWindow, DateRange historicalWindow) {
        return rateMonitoringDAO.getConditionCounts(currentWindow, historicalWindow);
    }

    /**
     * Gather the number of occurrences of each (app, loc, condition) triple
     * in reportable results in the two specified date/time windows, for
     * triples that appear at all, returning only those for which the
     * occurrence rate is "unusual".
     * 
     * @param sampleWindow
     * @param filter
     * @return A List of the ConditionCounts with unusual occurrence rates.
     */
    public List<ConditionCount> getUnusualConditionRates(DateRange sampleWindow, UnusualConditionRateFilter filter) {
        return rateMonitoringDAO.getUnusualConditionRates(sampleWindow, filter);
    }
    
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
    public List<Code> findCodes(String typeName, String systemName) {
        return codeDAO.findCodes(typeName, systemName);
    }
    
    public List<Code> findCodesExcludeRetired(String typeName, String systemName) {
        return codeDAO.findCodesExcludeRetired(typeName, systemName);
    }

    /**
     * Gets the unique code with the specified code system and code value.
     * 
     * @param systemName The name of the code system for the matching code.
     * @param codeValue The code value of the code for the matching code.
     * @return The matching code, or null if there is no matching code.
     */
    public Code getCode(String systemName, String codeValue) {
        return codeDAO.getCode(systemName, codeValue);
    }

    /**
     * Gets the unique code with the specified code system and code value.
     * 
     * @param systemName The name of the code system for the matching code.
     * @param codeValue The code value of the code for the matching code.
     * @return The matching code, or null if there is no matching code.
     */
    public Code getCode(CodeSystem codeSystem, String codeValue) {
        return codeDAO.getCode(codeSystem, codeValue);
    }
    
    /**
     * Gets the specified code type by internal name
     * 
     * @param name The name of the code type to get, such as "diagnosis",
     * "patientsex", etc.
     * @return The named code type, or null if there is no such code type.
     */
    public CodeType getCodeType(String name) {
        return codeDAO.getCodeType(name);
    }
    
    /**
     * Gets the code system with the specified internal name.
     * 
     * @param name The name of the code system to get, such as "I9" or "LN".
     * @return The named code system, or null if there is no such code system.
     */
    public CodeSystem getCodeSystem(String name) {
        return codeDAO.getCodeSystem(name);
    }
    
    public List<CodeSystem> getAllCodeSystems() {        
        return codeDAO.getAllCodeSystems();
    }
    
    public List<CodeSystem> getAllCodeSystemsExcludeRetired() {        
        return codeDAO.getAllCodeSystemsExcludeRetired();
    }

    /** Find codes which match a filter
     * @param filter The search criteria.
     * @return A list of codes that match the search criteria.
     */
    public SearchResult<Code> findCodes(SearchFilterCodes filter) {
        
        return codeDAO.findCodes(filter);
    }
   
    public List<CodeType> getAllCodeTypes() {       
        return codeDAO.getAllCodeTypes();
    }
    
    public List<CodeType> getAllCodeTypesExcludeRetired() {        
        return codeDAO.getAllCodeTypesExcludeRetired();
    }
    
    public void saveCode(Code code) {
        
        codeDAO.saveCode(code);
    }
    
    public void saveCodeSystem(CodeSystem codeSystem) {
        
        codeDAO.saveCodeSystem(codeSystem);
    }
    
    public void saveCodeType(CodeType codeType) {
        
        codeDAO.saveCodeType(codeType);
    }

    public Code getCode(Long id) {
        
        return codeDAO.getCode(id);
    }

    public CodeSystem getCodeSystem(Long id) {
        
        return codeDAO.getCodeSystem(id);
    }
    
    public CodeType getCodeType(Long id) {
        
        return codeDAO.getCodeType(id);
    }
    
    //-----------------------------------------
    // message count summary operations
    //-----------------------------------------
    
    /**
     * Record a message count summary.
     * 
     * @param messageCountSummary The message count summary to be added.
     */
    public void addMessageCountSummary(MessageCountSummary messageCountSummary) {
    	messageCountSummaryDAO.addMessageCountSummary(messageCountSummary);
    }
    
    /** Find message count summaries which match a filter
     * @param filter The search criteria.
     * @return A list of message count summaries that match the search criteria.  Sorted in descending date order.
     */
    public List<MessageCountSummary> findMessageCountSummaries(SearchFilterMessageCountSummary filter) {
    	return messageCountSummaryDAO.findMessageCountSummaries(filter);
    }
    
    /**
     * Resets the message count summaries
     * @return the number of rows deleted.
     */
    public int resetMessageCountSummaries() {
    	return messageCountSummaryDAO.resetMessageCountSummaries();
    }

    //-----------------------------------------
    // System activity operations
    //-----------------------------------------

    /** The DAO class for system activity entities */
    private ISystemActivityDAO systemActivityDAO;

    public ISystemActivityDAO getSystemActivityDAO() {
        
        return systemActivityDAO;
    }

    public void setSystemActivityDAO(ISystemActivityDAO dao) {
        
        systemActivityDAO = dao;
    }
    
    public void addSystemEvent(SystemActivityEvent event) {
        
        systemActivityDAO.addSystemEvent(event);
    }

    public List<SystemActivityEvent> findSystemEvents(SystemEventFilter filter) {
        
        return systemActivityDAO.findSystemEvents(filter);
    }

    //-----------------------------------------
    // review status operations
    //-----------------------------------------
    
    /**
     * Find a review status type by id
     * @param id
     * @return The ManualReviewStatusType for the specified id.
     */
    public ManualReviewStatusType findReviewStatusTypeById(int id) {
    	return reviewStatusDAO.findReviewStatusTypeById(id);
    }

    /**
     * Return all review status types
     * @return The review statuses as a ArrayList<Pair<Integer,String>>.
     */
    public ArrayList<Pair<Integer, String>> getReviewStatusTypes() {
    	return reviewStatusDAO.getReviewStatusTypes();
    }

    //-----------------------------------------
    // exported result operations
    //-----------------------------------------
    
    /** Add an exported result
     * 
     * @param status The new exported result to be added.
     */
    public void addExportedResult(ExportedResult exportedResult) {
    	exportedResultDAO.addExportedResult(exportedResult);
    }

    //-----------------------------------------
    // export recipient operations
    //-----------------------------------------
    
    /** Add an export recipient
     * 
     * @param status The new export recipient to be added.
     */
    public void addExportRecipient(ExportRecipient exportRecipient) {
    	exportRecipientDAO.addExportRecipient(exportRecipient);
    }
    
    //-----------------------------------------
    // NLP Discrete Term operations
    //-----------------------------------------
		
	public List<NlpDiscreteTerm> getNlpDiscreteTermsByNegative(boolean isNegative) {
		return nlpDiscreteTermDAO.getNlpDiscreteTermsByNegative(isNegative);
	}
	
	public List<NlpDiscreteTerm> getAllNlpDiscreteTerms() {
		return nlpDiscreteTermDAO.getAllNlpDiscreteTerms();
	}

	public void saveNlpDiscreteTerm(NlpDiscreteTerm term) {
		nlpDiscreteTermDAO.saveNlpDiscreteTerm(term);
	}

	public void deleteNlpDiscreteTerm(NlpDiscreteTerm term) {
		nlpDiscreteTermDAO.deleteNlpDiscreteTerm(term);		
	}

	// *******************
	// HL7Producer methods
	// *******************
	
    public HL7Producer getProducer(
            String applicationName, String facilityName, String locationName) {
        return producerDAO.getProducer(applicationName,
                facilityName, locationName);
    }
    public HL7Producer getProducerExact(String applicationName, 
    		String facilityName, String locationName) {
    	
        return producerDAO.getProducerExact(applicationName,
                facilityName, locationName);
    }

	public HL7Producer getProducer(long id) {
		return producerDAO.getProducer(id);
	}

	public List<HL7Producer> getAllProducers() {
		return producerDAO.getAllProducers();
	}

	public List<HL7Producer> getAllUnretiredProducers() {
		return producerDAO.getAllUnretiredProducers();
	}
	
	public void deleteProducer(HL7Producer src) {
		producerDAO.deleteProducer(src);
	}

	public void saveProducer(HL7Producer src) {
		producerDAO.saveProducer(src);
	}

	public SearchResult<HL7Producer> findProducers(SearchFilterProducers filter) {
		return producerDAO.findProducers(filter);
	}

    //-----------------------------------------
    // ProcessedMessageCount operations
    //-----------------------------------------
    
	private IProcessedMessageCountDAO processedMessageCountDAO;
    public IProcessedMessageCountDAO getProcessedMessageCountDAO() {
    	return processedMessageCountDAO;
    }
    public void setProcessedMessageCountDAO(IProcessedMessageCountDAO dao) {
    	processedMessageCountDAO = dao;
    }
    
    @Authorized()
    public void countProcessedMessage(String application, String facility,
    		String location, Date processedDateTime, String mpqSeqNumber) {
    	processedMessageCountDAO.countProcessedMessage(application, facility,
    			location, processedDateTime, mpqSeqNumber);
    }

    //-----------------------------------------
    // Data Source Report operations
    //-----------------------------------------
    
	private IDataSourceReportDAO dataSourceReportDAO;
    public IDataSourceReportDAO getDataSourceReportDAO() {
    	return dataSourceReportDAO;
    }
    public void setDataSourceReportDAO(IDataSourceReportDAO dao) {
    	dataSourceReportDAO = dao;
    }
    
    @Authorized()
    public List<DataSourceInfo> findDataSourceInfo(TaskDefinition task, DataSourceReportFilter filter) {
    	return dataSourceReportDAO.findDataSourceInfo(task, filter);
    }
}

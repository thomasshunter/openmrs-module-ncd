/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.storage;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.CandidateResultFinderException;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.InterestingResultFinder;
import org.openmrs.module.ncd.critic.ConditionExtractor;
import org.openmrs.module.ncd.critic.ReportResult;
import org.openmrs.module.ncd.critic.ReportResult.ResultInfo;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.County;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.Institution;
import org.openmrs.module.ncd.database.Jurisdiction;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.jurisdiction.JurisdictionAlgorithm;
import org.openmrs.module.ncd.model.Diagnosis;
import org.openmrs.module.ncd.model.IResultSegment;
import org.openmrs.module.ncd.model.MessageHeader;
import org.openmrs.module.ncd.model.Note;
import org.openmrs.module.ncd.model.Observation;
import org.openmrs.module.ncd.model.OrderCommon;
import org.openmrs.module.ncd.model.OrderObservation;
import org.openmrs.module.ncd.model.PatientAdditionalDemographics;
import org.openmrs.module.ncd.model.PatientInfo;
import org.openmrs.module.ncd.model.PersonAddressInfo;
import org.openmrs.module.ncd.model.PersonNameInfo;
import org.openmrs.module.ncd.model.Provider;
import org.openmrs.module.ncd.model.ProviderInfo;
import org.openmrs.module.ncd.model.ResultSegmentFactory;
import org.openmrs.module.ncd.model.Staff;
import org.openmrs.module.ncd.model.Zvx;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.NCDConcepts;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.ObsBuilder;
import org.openmrs.module.ncd.utilities.PersonAttributeTypeCache;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;


/**
 * Class to fill in the ReportableResult object that will be persisted
 * in the database when a reportable condition is found.
 * 
 * @author jlbrown
 */
public class ResultStorageHelper
{       
    private static Log log                                          = LogFactory.getLog(ResultStorageHelper.class);
    private static final String LOINC_CODE_SYSTEM                   = "LN";
    private static final String UNKNOWN_PATIENT_ID                  = "UNKNOWN_PATIENT_ID";
    private final static String ZVX_NODE_EXPRESSION                 = "//ZVX";
    private final static String UNKNOWN_CONDITION                   = "Unknown";
    private static String DEFAULT_INSTITUTION_ID_TYPE               = null;
    
    private static final String MARKER_BAD_PHONE_PART1              = "Validation failed: Primitive value ";
    private static final String MARKER_BAD_PHONE_PART2              = "requires to be empty or a US phone number at PID-13";
    private static final String MARKER_BAD_PHONE_PART3              = "requires to be empty or a US phone number at ORC-14";
    private static final String MARKER_BAD_DATETIME_PART1           = "Validation failed: Primitive value '";
    private static final String MARKER_BAD_DATETIME_PART2           = "' requires to be empty or a HL7 datetime string at OBR-34";
    private static final String UNASSIGNED_PHYSICIAN_NAME           = "UNASSIGNED PHYSICIAN";
    
    private static final String MARKER_BAD_PRIMITIVE_VALUE_PART1    = "Validation failed: Primitive value ";
    private static final String MARKER_BAD_PRIMITIVE_VALUE_PART2    = "requires to be empty or a number with optional decimal digits";
    
    static
    {
        DEFAULT_INSTITUTION_ID_TYPE = "HN";
    }

    /** The date and time at which the message being processed was received */
    private Date receivedDateTime;
    /** The raw text of the HL7 message being processed */
    private String messageText;
    private static String messageTextStatic;
    /** The persisted raw HL7 message, if it has been persisted */
    private RawMessage rawhl7                               = null;
    /** The persisted Patient, if one has been persisted */
    private Patient patient                                 = null;

    public ResultStorageHelper(String messageText) 
    {    
        this.receivedDateTime   = new Date();
        this.messageText        = messageText;
        messageTextStatic       = messageText;
    }

    /** Gets a shared RawMessage for the current message */
    public RawMessage getRawHL7() throws RawMessageStorageException 
    {
        if (rawhl7 == null) 
        {
            if (StringUtils.isEmpty(messageText)) 
            {
            	messageText = "None";
            }
            
            rawhl7 = RawMessageStorageHelper.createRawHL7(messageText);
        }
        
        return rawhl7;
    }
    
    public void updateRawHL7(String message) 
    {
    	messageText = message;
    
    	if (rawhl7 != null) 
    	{
    		rawhl7.setMessageText(message);
    		NCDUtilities.getService().updateRawMessage(rawhl7);
    	}
    }

    /** Gets a shared Patient for the current message */
    public Patient getPatient(PatientInfo patientInfo) 
    {    
        if (patient == null) 
        {    
            patient = savePatient( patientInfo, this.messageText );
        }
        
        return patient;
    }

    @Transactional(rollbackFor = Exception.class)
    public void storeResult(ReportResult reportResult) throws CandidateResultFinderException, RawMessageStorageException, ResultStorageException 
    {    	
    	List<ReportableResult> newReportableResults = new ArrayList<ReportableResult>();
    	
    	for( Condition condition : reportResult.getConditions() ) 
        {	
	        ReportableResult newReportableResult   = new ReportableResult();
	        ResultInfo resultLoc                   = reportResult.getResultLocationForCondition( condition==null ? ResultStorageHelper.UNKNOWN_CONDITION : condition.getDisplayText());
	        List<Node> resultNodes                 = resultLoc.getResultNodes();
	        
	        if (resultNodes == null) 
	        {	        	
	            String reportResultMessage = reportResult.getMessage();
	            resultNodes                = new InterestingResultFinder().findCandidateResults( reportResultMessage );
	        }
	        
	        Node resultNode                        = resultNodes.get( 0 );
	        IResultSegment resultSegment           = getResultSegment( resultNode );
	        
	        parseOrderObservationInfoIntoReportableResult(newReportableResult, resultSegment);
	        parseObservationInfoIntoReportableResult(newReportableResult, resultNodes, resultSegment);
	        
	        PatientInfo patientInfo                = resultSegment.getPatientInfo();            
	        parsePatientInfoIntoReportableResult( newReportableResult, patientInfo  );
	        
	        ProviderInfo providerInfo              = getProviderInfo(newReportableResult, resultSegment, messageText );
	                
	        copyProviderInfoIntoReportableResult(newReportableResult, providerInfo);
	        newReportableResult.setMessageReceivedDateTime(receivedDateTime);
	        newReportableResult.setRawMessage(getRawHL7());
	        newReportableResult.setInstitutionIdType( ResultStorageHelper.DEFAULT_INSTITUTION_ID_TYPE );
	        doMPQNumber(newReportableResult, reportResult, resultNodes.get(0));
	        doCountyAndJurisdiction(newReportableResult, resultSegment);
	        parseAppFacLocIntoReportableResult(newReportableResult, resultSegment, reportResult);
	        
	        // Create or update the patient and provider
	        Patient patient                        = getPatient(patientInfo);
	        Person provider                        = saveProvider(providerInfo);
	        
	        // Create an encounter for this message
	        Encounter encounter                    = createEncounter(patient, provider, newReportableResult);
	
			//TODO: create or update the referenced laboratory (if we choose to persist it as an openmrs location).
			
			// Add a group of obs for each reportable result        
			storeInfoFromReportResult(newReportableResult, reportResult, condition);
			long resultSeq                         = NCDUtilities.nextReportableResultSeq();
			newReportableResult.setResultSeq(resultSeq);
			newReportableResults.add(newReportableResult);
			addResultObsGroup(encounter, newReportableResult);
			
			// Save the encounter and obs.
			encounter                            = Context.getEncounterService().saveEncounter(encounter);
			log.debug("save Encounter=" + encounter);
			
			// Remember the Encounter id, so we can tie the reportable result to its OpenMRS Encounter
			newReportableResult.setEncounterId(encounter.getEncounterId().longValue());
			
			newReportableResult                  = new ReportableResult(newReportableResult);        
			// TODO: convert tblreportableresult to a plain POJO, or merge into ReportResult.
			
			// log basic reportable result information
			logReportableResult(newReportableResults);    		
        }

        // Save the reportable result the old way
        ConditionDetectorService cds = NCDUtilities.getService(); 
        cds.saveReportableResults(newReportableResults);
        
        // Create the manual review alerts, if any
        for (ReportableResult result : newReportableResults) 
        {
        	if (result.getManualReviewStatusType().getId() == ConditionDetectorService.reviewStatusTypeHold) 
        	{
        		AlertSummary alertSummary = new AlertSummary(cds.findAlertTypeById(ConditionDetectorService.alertTypeManualReview), 
        										"Reportable result held for manual review (click <a href='reportableResultDetail.form?edit=" + result.getId() + "'>here</a> to review)",
        										"The system detected a notifiable condition, and recorded an associated reportable result (id=" + result.getId() + ") that requires manual review",
        										ConditionDetectorService.alertIdentityManualReview + "[id=" + result.getId() + "]");
        		cds.addAlertSummary(alertSummary);
        	}
        }
        
    }
    
    private static void logReportableResult( List<ReportableResult> results )
    {
        String mpqnum       = "";
        StringBuilder buf   = new StringBuilder();
        
        for (ReportableResult thisResult : results) 
        {
            if (buf.length() > 0) 
            {
                buf.append("; ");
            }
            else 
            {
                mpqnum = thisResult.getMpqSeqNumber();
            }
            
            buf.append(thisResult.getConditionName());
            
            ResultStorageHelper.log.debug( thisResult.diagnosticOutput() ); // For some reason I can't determine, this will not print.
            System.err.println( thisResult.diagnosticOutput() ); 
        }
        
        log.info("**Positive** msg mpq#" + mpqnum + " has reportable results: " + buf.toString());
    }
    
    private static IResultSegment getResultSegment(Node resultNode) throws ResultStorageException
    {                
    	try 
    	{
    		return ResultSegmentFactory.getResultSegment(resultNode);
    	} 
    	catch (Exception e) 
    	{
    		throw new ResultStorageException( "ResultStorageHelper.getResultSegment() threw an Exception while getting the result segment.", e);
    	}
    }    
    
    private static void storeInfoFromReportResult(ReportableResult reportableResult, ReportResult reportResult, Condition condition)
    {
    	ConditionDetectorService cds   = NCDUtilities.getService();
    	ResultInfo resultLoc           = null;
    	
        if (condition != null)
        {
            String conditionName    = condition.getDisplayText();
            resultLoc               = reportResult.getResultLocationForCondition(conditionName);
            reportableResult.setConditionName(conditionName);
            
            if (condition.isManualReviewRequired() && (resultLoc.getDecidedResult() == null || !resultLoc.getDecidedResult().isManuallyReviewed())) 
            {
                reportableResult.setManualReviewStatusType(cds.findReviewStatusTypeById(ConditionDetectorService.reviewStatusTypeHold));
                reportableResult.setReleaseDate(null);
            } 
            else 
            {
                reportableResult.setManualReviewStatusType(cds.findReviewStatusTypeById(ConditionDetectorService.reviewStatusTypeNoReviewRequired));
                reportableResult.setReleaseDate(reportableResult.getMessageReceivedDateTime());
            }
            
            DecidedResult decidedResult = resultLoc.getDecidedResult();
            reportableResult.setDecidedResult(NCDUtilities.getService().saveDecidedResult(decidedResult));            
        }
        else
        {
            // If the condition is null insert "Unknown" for the condition name.
            reportableResult.setConditionName(UNKNOWN_CONDITION);
            reportableResult.setManualReviewStatusType(cds.findReviewStatusTypeById(ConditionDetectorService.reviewStatusTypeHold));
            reportableResult.setReleaseDate(null);
            reportableResult.setReleaseDate(reportableResult.getMessageReceivedDateTime());
            resultLoc = reportResult.getResultLocationForCondition(UNKNOWN_CONDITION);            
        }        
        
        reportableResult.setObrSetId(resultLoc.getObrLoc());
        reportableResult.setObxStartSetId(resultLoc.getObxStartLoc());
        reportableResult.setObxEndSetId(resultLoc.getObxEndLoc());
        reportableResult.setCritic(resultLoc.getCriticThatFoundResult());
        
        HL7Producer hl7Producer = reportResult.getApplicationFacility();
        
        if (hl7Producer == null) 
        {
        	hl7Producer = InstitutionStorageHelper.retrieveHL7Producer(reportResult.getMessage());
        }
        
        reportableResult.setProducer(hl7Producer);
        
        Institution institution = reportResult.getInstitution();
        
        if (institution == null && resultLoc != null && resultLoc.getResultNodes() != null) 
        {
        	institution = InstitutionStorageHelper.retrieveInstitution( resultLoc.getResultNodes().get(0), reportResult);
        }
        
        reportableResult.setInstitution(institution);
        reportableResult.setSentInError(false);
        
        if (reportableResult.getDecidedResult() != null) 
        {
        	reportableResult.setCriticDisplay("Decided Result: " + reportableResult.getDecidedResult().getClassifiedByWhom());
        }
        else if (reportableResult.getCritic() != null) 
        {
        	reportableResult.setCriticDisplay(reportableResult.getCritic().getName());
        }

        CodeCondition loincCodeRow = reportResult.getLoincCode();
        
        if (loincCodeRow == null)
        {
            // If the report result doesn't have the LOINC code row, try
            // to get a row by bringing together the LOINC code and condition name.
            String loincCode        = null;
            String obxCodeSystem    = reportableResult.getTestResultCodeSystem();
            String obxAltCodeSystem = reportableResult.getObxAltCodeSystem();
        
            if (obxCodeSystem != null && obxCodeSystem.equals(LOINC_CODE_SYSTEM))
            {
                loincCode = reportableResult.getTestResultId();
            }
            else if (obxAltCodeSystem != null && obxAltCodeSystem.equals(LOINC_CODE_SYSTEM))
            {
                loincCode = reportableResult.getObxAltCode();
            }
            
            if (loincCode != null && condition != null)
            {
                loincCodeRow = NCDUtilities.getService().findByCodeAndCondition( loincCode, LOINC_CODE_SYSTEM, condition.getDisplayText());             
            }
        }

        if (loincCodeRow != null) 
        {
            reportableResult.setCode(loincCodeRow.getCode());
        	reportableResult.setCodeDisplay(loincCodeRow.getCode().getCode());
        }
    }
    
    private static void parsePatientInfoIntoReportableResult( ReportableResult reportableResult, PatientInfo patientInfo ) throws ResultStorageException
    {
        try
        {
            reportableResult.setPatientBirth( patientInfo.getPatientBirth() );
        }
        catch (ParseException e)
        {
            throw new ResultStorageException ("Error parsing patient birth date into reportable result.", e);
        }
                        
        reportableResult.setPatientCity(                        patientInfo.getCity());
        reportableResult.setPatientCountry(                     patientInfo.getCountry());
        reportableResult.setPatientCounty(                      patientInfo.getCounty());                
        reportableResult.setPatientInstitutionMedicalRecordId(  patientInfo.getPatientInstitutionMedicalRecordId());
        reportableResult.setPatientName(                        patientInfo.getFullName());
        reportableResult.setPatientPhone(                       patientInfo.getPatientPhone());
        reportableResult.setPatientRace(                        patientInfo.getPatientRace());
        reportableResult.setPatientSex(                         patientInfo.getPatientSex());
        reportableResult.setPatientSSN(                         patientInfo.getPatientSSN());
        reportableResult.setPatientState(                       patientInfo.getState());
        reportableResult.setPatientStreet1(                     patientInfo.getStreet1());
        reportableResult.setPatientStreet2(                     patientInfo.getStreet2());
        reportableResult.setPatientZip(                         patientInfo.getZip());
    }
    
    private static void parseOrderObservationInfoIntoReportableResult( ReportableResult reportableResult, IResultSegment resultSegment ) throws ResultStorageException
    {
        OrderObservation obr    = resultSegment.getOrderObservation();
        OrderCommon orc         = resultSegment.getOrderCommon();
        
        if (obr != null) 
        {
	        reportableResult.setTestId(            obr.getTestIdentifier());
	        reportableResult.setTestName(          obr.getTestName());
	        reportableResult.setTestCodeSystem(    obr.getTestCodeSystem());        
	        doTestPlacerOrderNumber(reportableResult, obr, orc);        
	        doTestFillerOrderNumber(reportableResult, obr, orc);        
	        doTestDate(obr, resultSegment, reportableResult);
	        reportableResult.setTestSpecimenText(  obr.getSpecimenText());
	        reportableResult.setObrAltCode(        obr.getOrderObservationAltCode());
	        reportableResult.setObrAltCodeText(    obr.getOrderObservationAltCodeText());
	        reportableResult.setObrAltCodeSystem(  obr.getOrderObservationAltCodeSystem());
	        reportableResult.setProviderName(      obr.getProviderFullName());
        }
    }
        
    private static void parseObservationInfoIntoReportableResult( ReportableResult reportableResult, List<Node> resultSegments, IResultSegment resultSegment ) throws ResultStorageException
    {            	    	
        reportableResult.setTestAbnormalFlag(       resultSegment.getAbnormalFlag() );
        reportableResult.setTestNormalRange(        resultSegment.getNormalRange() );
        reportableResult.setObxAltCode(             resultSegment.getTestResultAltCode() );
        reportableResult.setObxAltCodeSystem(       resultSegment.getTestResultAltCodeSys() );
        reportableResult.setObxAltCodeText(         resultSegment.getTestResultAltCodeText() );
        reportableResult.setTestDataType(           resultSegment.getTestDataType() );
        reportableResult.setTestResultCode(         resultSegment.getTestResultValueCode() );
        reportableResult.setTestResultCodeSystem(   resultSegment.getTestResultCodeSystem() );
        reportableResult.setTestResultId(           resultSegment.getTestResultCode() );
        reportableResult.setTestResultName(         resultSegment.getTestResultName() );
        reportableResult.setTestResultStatus(       resultSegment.getTestResultStatus() );
        reportableResult.setTestResultSubId(        resultSegment.getTestResultSubIdentifier() );
        reportableResult.setTestResultUnits(        resultSegment.getTestResultUnits() );                        
        
        try 
        {
        	reportableResult.setTestResultValue(ConditionExtractor.getTestResultValues(resultSegments));
        }
        catch (Exception e) 
        {
        	throw new ResultStorageException(e);
        }
        
        StringBuilder commentBuilder = new StringBuilder();
        
        for (Node segment : resultSegments) 
        {
        	commentBuilder.append(Note.getConcatenatedNoteValues(segment));
        }
        
        reportableResult.setTestComment(commentBuilder.toString());
    }
    
    private static ProviderInfo getProviderInfo(ReportableResult result, IResultSegment resultSegment, String messageText ) 
    {    
        Provider provider                                           = resultSegment.getProvider();        
        OrderObservation obr                                        = resultSegment.getOrderObservation();
        ProviderInfo providerInfo                                   = new ProviderInfo();
        PatientAdditionalDemographics patientAdditionalDemographics = resultSegment.getPatientAdditionalDemographics();
                
        Map<String, Staff> staffMap                                 = resultSegment.getStaffMap();
        Map<String, String> providerIdMap                           = getProviderLocalIdMap(obr, provider, providerInfo, patientAdditionalDemographics, staffMap);
        // as a side-effect, the getBestStaffEntry method sets the providerInfo localId and localIdSource.
        Staff providerStaff                                         = getBestStaffEntry(staffMap, providerIdMap, providerInfo);
        
        if (providerStaff != null) 
        {
        	providerInfo.setFirstName(     providerStaff.getGivenName());
        	providerInfo.setLastName(      providerStaff.getFamilyName());
        	providerInfo.setMiddleName(    providerStaff.getMiddleName());
        	providerInfo.setSuffixName(    providerStaff.getSuffixName());
        	providerInfo.setNameSource( "STF.3" );
        	providerInfo.setStreet1(       providerStaff.getStreet());
        	providerInfo.setCity(          providerStaff.getCity());
        	providerInfo.setState(         providerStaff.getState());
        	providerInfo.setZip(           providerStaff.getZip());
        	providerInfo.setCounty(        providerStaff.getCounty());        	
        	providerInfo.setPhoneNumber(   providerStaff.getPhoneNumber());        	
        } 
        else 
        {        	
        	String providerIdSource = addBestProviderIdAndSource(providerInfo, providerIdMap);
        	addBestProviderName(obr, provider, providerInfo, patientAdditionalDemographics, providerIdSource, messageTextStatic );
        	
        	providerInfo.setCity(              provider.getProviderCity());
        	providerInfo.setCounty(            provider.getProviderCounty());
        	providerInfo.setPhoneNumber(       provider.getProviderFullPhoneNumber());
        	providerInfo.setState(             provider.getProviderState());
        	providerInfo.setStreet1(           provider.getProviderStreet1());
        	providerInfo.setStreet2(           provider.getProviderStreet2());
        	providerInfo.setZip(               provider.getProviderZip());
        	providerInfo.setFacilityId(        provider.getPatientFacilityLocationId());
        
	        providerInfo.setBirthDate(     result.getProviderBirth());
	        providerInfo.setDeaNumber(     result.getProviderDEANumber());
	        providerInfo.setFax(null);
	        providerInfo.setLicense(       result.getProviderLicense());
	        providerInfo.setNameMatched(   result.getProviderNameMatched());	        
	        providerInfo.setSSN(           result.getProviderSSN());
	        providerInfo.setPractice(      result.getProviderPractice());
        } 
        
        if( providerInfo.getFirstName() == null || providerInfo.getFirstName().trim().length() == 0 || providerInfo.getLastName() == null || providerInfo.getLastName().trim().length() == 0 )
        {
            performHeroicsToGetTheProviderFromTheOriginalMessage( messageText, providerInfo );
        }
        
        return providerInfo;
    }

    private static String addBestProviderIdAndSource(ProviderInfo providerInfo, Map<String, String> providerIdMap) 
    {
		Iterator<String> providerIdKeyIter    = providerIdMap.keySet().iterator();
		String providerIdKey                  = "NONE";
		String providerId                     = null;
		
		if (providerIdKeyIter.hasNext()) 
		{
			providerIdKey = providerIdKeyIter.next();
			providerId = providerIdMap.get(providerIdKey);
    	}    	
		
		providerInfo.setLocalId(providerId);
		providerInfo.setLocalIdSource(providerIdKey);
		log.debug("Using provider ID - " + providerId + " from source - " + providerIdKey);
		
		return providerIdKey;
	}

	private static Staff getBestStaffEntry(Map<String, Staff> staffMap, Map<String, String> providerIdMap, ProviderInfo providerInfo) 
	{
		Staff bestStaffEntry = null;
		
		Iterator<String> providerIdKeyIter = providerIdMap.keySet().iterator();
		
		while (providerIdKeyIter.hasNext()) 
		{
			String providerIdKey     = providerIdKeyIter.next();
			String providerId        = providerIdMap.get(providerIdKey);
			log.debug("Checking provider ID - " + providerId + " from source - " + providerIdKey);
			Staff staffEntry         = staffMap.get(providerId);
			
			if (staffEntry != null) 
			{
				bestStaffEntry = staffEntry;
				providerInfo.setLocalId(providerId);
				providerInfo.setLocalIdSource(providerIdKey);
				log.debug("Using provider ID - " + providerId + " from source - " + providerIdKey);
				
				break;
			}
		}
		
		if (bestStaffEntry == null) 
		{
			log.debug("No staff entry found.");
		}
		
		return bestStaffEntry;
	}

	private static void copyProviderInfoIntoReportableResult( ReportableResult reportableResult, ProviderInfo providerInfo)
    {
        reportableResult.setProviderLocalId(            providerInfo.getLocalId());
        reportableResult.setProviderLocalIdSource(      providerInfo.getLocalIdSource());
        reportableResult.setProviderName(               providerInfo.getFullName());
        reportableResult.setProviderNameSource(         providerInfo.getNameSource());
        reportableResult.setProviderCity(               providerInfo.getCity());
        reportableResult.setProviderCounty(             providerInfo.getCounty());
        reportableResult.setProviderPhone(              providerInfo.getPhoneNumber());
        reportableResult.setProviderState(              providerInfo.getState());
        reportableResult.setProviderStreet(             providerInfo.getStreet1());
        reportableResult.setProviderZip(                providerInfo.getZip());
        reportableResult.setFacilityId(                 providerInfo.getFacilityId());
    }
    
    private static Map<String, String> getProviderLocalIdMap(OrderObservation obr, Provider provider, ProviderInfo providerInfo, PatientAdditionalDemographics patientDemographics, Map<String, Staff> staffMap)
    {
        Map<String, String> providerLocalIdMap      = new LinkedHashMap<String, String>();
        // OBR.16.1 or PD1.4 or PV1.52 or OBR.28 or PV1.8 or PV1.9 or PV1.7 or PV1.17 or OBR.32 or OBR.33
        String providerLocalId                      = null;
        String providerLocalIdSource                = "NONE";        
    	
        if (obr != null) 
        {
            providerLocalId         = obr.getProviderLocalId();       		
            providerLocalIdSource   = "OBR.16.1";
	    
	    	if (StringUtils.isNotEmpty(providerLocalId)) 
	    	{
	    		providerLocalIdMap.put(providerLocalIdSource, providerLocalId);
	    	}
        }
    	                          
        if (patientDemographics != null) 
        {
	        providerLocalId        = patientDemographics.getPatientPrimaryCareIdentifier();
	        providerLocalIdSource  = "PD1.4";
	    
	        if (StringUtils.isNotEmpty(providerLocalId)) 
	        {
	    		providerLocalIdMap.put(providerLocalIdSource, providerLocalId);
	    	}
        }
        
        if (provider != null) 
        {
	        providerLocalId        = provider.getOtherProviderLocalIdentifier();
	        providerLocalIdSource  = "PV1.52.1";
	      
	        if (StringUtils.isNotEmpty(providerLocalId)) 
	        {
	    		providerLocalIdMap.put(providerLocalIdSource, providerLocalId);
	    	}
        }
        
        if (obr != null) 
        {
	        providerLocalId        = obr.getResultCopyToIdentifier();
	        providerLocalIdSource  = "OBR.28.1";
	    
	        if (StringUtils.isNotEmpty(providerLocalId)) 
	        {
	    		providerLocalIdMap.put(providerLocalIdSource, providerLocalId);
	    	}
        }
        
        if (provider != null) 
        {
	        providerLocalId        = provider.getReferringDoctorLocalIdentifier();
	        providerLocalIdSource  = "PV1.8.1";
	    
	        if (StringUtils.isNotEmpty(providerLocalId)) 
	        {
	    		providerLocalIdMap.put(providerLocalIdSource, providerLocalId);
	    	}
	        
	        providerLocalId        = provider.getConsultingDoctorLocalIdentifier();
	        providerLocalIdSource  = "PV1.9.1";
	        
	        if (StringUtils.isNotEmpty(providerLocalId)) 
	        {
	    		providerLocalIdMap.put(providerLocalIdSource, providerLocalId);
	    	}
	        
	        providerLocalId        = provider.getAttendingDoctorLocalIdentifier();
	        providerLocalIdSource  = "PV1.7.1";
	        
	        if (StringUtils.isNotEmpty(providerLocalId)) 
	        {
	    		providerLocalIdMap.put(providerLocalIdSource, providerLocalId);
	    	}
	        
	        providerLocalId        = provider.getAdmittingDoctorLocalIdentifier();
	        providerLocalIdSource  = "PV1.17.1";
	        
	        if (StringUtils.isNotEmpty(providerLocalId)) 
	        {
	    		providerLocalIdMap.put(providerLocalIdSource, providerLocalId);
	    	}
        }
                
        return providerLocalIdMap;
    }       
    
    private static void addBestProviderName( OrderObservation obr, Provider provider, ProviderInfo providerInfo, PatientAdditionalDemographics patientDemographics, String providerIdSource, String messageText )
    {
    	// OBR.16 or PD1.4 or PV1.52 or OBR.28 or PV1.8 or PV1.9 or PV1.7 or PV1.17 or OBR.32 or OBR.33
        // PV1 7.2/7.3/7.4/7.5 or (ZV1 3.2/3.3/3.4) or OBR 16.2/16.3/16.4/16.5 or ZLR2.1
    	String firstName   = null;
    	String lastName    = null;
    	String middleName  = null;
    	String suffixName  = null;
    	String nameSource  = "NONE";
    	
        if (providerIdSource.equals("OBR.16.1")) 
        {
        	firstName  = obr.getProviderFirstName();
        	lastName   = obr.getProviderLastName();
        	middleName = obr.getProviderMiddleName();
        	suffixName = obr.getProviderSuffixName();
        	nameSource = "OBR.16.2/OBR.16.3/OBR.16.4/OBR.16.5";
        } 
        else if (providerIdSource.equals("PD1.4")) 
        {
        	firstName  = patientDemographics.getPatientPrimaryCareFirstName();
        	lastName   = patientDemographics.getPatientPrimaryCareLastName();
        	middleName = patientDemographics.getPatientPrimaryCareMiddleName();
        	suffixName = patientDemographics.getPatientPrimaryCareSuffixName();
        	nameSource = "PD1.4.2/PD1.4.3/PD1.4.4/PD1.4.5";
        } 
        else if (providerIdSource.equals("PV1.52.1")) 
        {
        	firstName  = provider.getOtherProviderFirstName();
        	lastName   = provider.getOtherProviderLastName();
        	middleName = provider.getOtherProviderMiddleName();
        	suffixName = provider.getOtherProviderSuffixName();
        	nameSource = "PV1.52.2/PV1.52.3/PV1.52.4/PV1.52.5";
        } 
        else if (providerIdSource.equals("OBR.28.1")) 
        {
        	firstName  = obr.getResultCopyToFirstName();
        	lastName   = obr.getResultCopyToLastName();
        	middleName = obr.getResultCopyToMiddleName();
        	suffixName = obr.getResultCopyToSuffixName();
        	nameSource = "OBR.28.2/OBR.28.3/OBR.28.4/OBR.28.5";
        } 
        else if (providerIdSource.equals("PV1.8.1")) 
        {
        	firstName  = provider.getReferringDoctorFirstName();
        	lastName   = provider.getReferringDoctorLastName();
        	middleName = provider.getReferringDoctorMiddleName();
        	suffixName = provider.getReferringDoctorSuffixName();
        	nameSource = "PV1.8.2/PV1.8.3/PV1.8.4/PV1.8.5";
        } 
        else if (providerIdSource.equals("PV1.9.1")) 
        {
        	firstName  = provider.getConsultingDoctorFirstName();
        	lastName   = provider.getConsultingDoctorLastName();
        	middleName = provider.getConsultingDoctorMiddleName();
        	suffixName = provider.getConsultingDoctorSuffixName();
        	nameSource = "PV1.9.2/PV1.9.3/PV1.9.4/PV1.9.5";
        } 
        else if (providerIdSource.equals("PV1.7.1")) 
        {
        	firstName  = provider.getAttendingDoctorFirstName();
        	lastName   = provider.getAttendingDoctorLastName();
        	middleName = provider.getAttendingDoctorMiddleName();
        	suffixName = provider.getAttendingDoctorSuffixName();
        	nameSource = "PV1.7.2/PV1.7.3/PV1.7.4/PV1.7.5";
        } 
        else if (providerIdSource.equals("PV1.17.1")) 
        {
        	firstName  = provider.getAdmittingDoctorFirstName();
        	lastName   = provider.getAdmittingDoctorLastName();
        	middleName = provider.getAdmittingDoctorMiddleName();
        	suffixName = provider.getAdmittingDoctorSuffixName();
        	nameSource = "PV1.17.2/PV1.17.3/PV1.17.4/PV1.17.5";
        }
        
        if( firstName == null || firstName.trim().length() == 0 )
        {
            performHeroicsToGetTheProviderFromTheOriginalMessage( messageText, providerInfo );
        }
        else
        {        
            lastName    = lastName.replaceAll( "\\(", "" );
            lastName    = lastName.replaceAll( "\\)", "" );
            
            providerInfo.setFirstName(firstName);
            providerInfo.setLastName(lastName);
            providerInfo.setMiddleName(middleName);
            providerInfo.setSuffixName(suffixName);
            providerInfo.setNameSource(nameSource);
        }
    }
    
    private static void performHeroicsToGetTheProviderFromTheOriginalMessage( String messageText, ProviderInfo providerInfo )
    {
        Message parsedMessage   = null;
        
        try
        {
            PipeParser parser               = new PipeParser();
            parsedMessage                   = parser.parse( messageText );
            Terser parsedTerser             = new Terser( parsedMessage );
            
            String attendingPhysicianLast   = parsedTerser.get( "/.PV1-7-2" );
            String attendingPhysicianFirst  = parsedTerser.get( "/.PV1-7-3" );
            String attendingPhysicianMiddle = parsedTerser.get( "/.PV1-7-4" );
            String attendingPhysicianSuffix = null;
            
            if( attendingPhysicianLast != null )
            {
                attendingPhysicianLast          = attendingPhysicianLast.replaceAll( "\\(", "" );
                attendingPhysicianLast          = attendingPhysicianLast.replaceAll( "\\)", "" );
            }
            
            if( ResultStorageHelper.UNASSIGNED_PHYSICIAN_NAME.equals( attendingPhysicianLast ) && attendingPhysicianFirst == null )
            {
                String[] unassignedProviderNames    = attendingPhysicianLast.split( " " );
                
                if( unassignedProviderNames.length == 2 )
                {
                    attendingPhysicianFirst = unassignedProviderNames[ 0 ];
                    attendingPhysicianLast  = unassignedProviderNames[ 1 ];
                }
            }

            boolean keepGoing               = true;
            
            for( int i = 0; keepGoing; i++ )
            {
                String obxValue = "";
                
                try
                {                
                    obxValue = parsedTerser.get( "/.OBX(" + i + ")-25" );
                }
                catch( Exception e )
                {
                    // Not an error
                }
                
                if( obxValue == null || obxValue.trim().length() == 0 )
                {
                    keepGoing = false;
                }
                else
                {
                    String[] names = obxValue.split( " " );
                    
                    if( names.length == 2 )
                    {
                        attendingPhysicianFirst = names[0].trim();
                        attendingPhysicianLast  = names[1].trim();
                    }
                    else if( names.length == 3 )
                    {
                        attendingPhysicianFirst = names[0].trim();
                        attendingPhysicianLast  = names[1].trim();
                        
                        if( "MD".equals( names[2] ) )
                        {
                            attendingPhysicianSuffix = names[2].trim();
                        }
                    }
                }
            }

            if( attendingPhysicianLast != null && attendingPhysicianLast.trim().length() > 0 )
            {
                providerInfo.setLastName( attendingPhysicianLast );
                providerInfo.setFirstName( attendingPhysicianFirst );
                providerInfo.setMiddleName( attendingPhysicianMiddle );
                providerInfo.setSuffixName( attendingPhysicianSuffix );
            }
        }
        catch( Exception e )
        {
            ResultStorageHelper.log.error( "ResultStorageHelper.performHeroicsToGetTheProviderFromTheOriginalMessage() threw an Exception while attempting to parse:\n\tmessageText=" 
                                                + messageText 
                                                + "\n\tproviderInfo=" 
                                                + providerInfo 
                                                + "\n\tyielding Exception e=" 
                                                + e 
                                         );
        }
    }
    
    private static void doTestDate(OrderObservation obr, IResultSegment resultSegment, ReportableResult reportableResult) throws ResultStorageException
    {
        // OBR.7 or OBX.14 or DG1.5
        Date testDate           = null;
        String testDateSource   = null;
        
        try
        {
            testDate        = obr.getTestDate();
            testDateSource  = "OBR.7";
        
            if (testDate == null)
            {
                testDate = resultSegment.getTestDate();
            
                if (resultSegment instanceof Observation)
                {
                    testDateSource = "OBX.14";
                }
                else if (resultSegment instanceof Diagnosis)
                {
                    testDateSource = "DG1.5";
                }            
            }
        }
        catch (ParseException e)
        {
            throw new ResultStorageException( "ResultStorageHelper.doTestDate() threw an Error parsing test date into reportable result.", e);
        }
        
        if (testDate == null)
        {
            testDateSource = "NONE";
        }
        
        reportableResult.setTestDate(testDate);
        reportableResult.setTestDateSource(testDateSource);
    }
    
    private static void doTestPlacerOrderNumber( ReportableResult reportableResult, OrderObservation obr, OrderCommon orc)
    {
        // ORC.2.1 or OBR.2
    	String testPlacerOrderNum          = null;
    	String testPlacerOrderNumSource    = "NONE";
    	
    	if (orc != null) 
    	{
    		testPlacerOrderNum        = orc.getTestPlacerOrderNumber();
    		testPlacerOrderNumSource  = "ORC.2.1";
    	}
    	
        if (StringUtils.isEmpty(testPlacerOrderNum))
        {
            testPlacerOrderNum          = obr.getTestPlacerOrderNumber();
            testPlacerOrderNumSource    = "OBR.2";
        }        
            
        reportableResult.setTestPlacerOrderNum(testPlacerOrderNum);
        reportableResult.setTestPlacerOrderNumSource(testPlacerOrderNumSource);
    }

    private static void doTestFillerOrderNumber( ReportableResult reportableResult, OrderObservation obr, OrderCommon orc)
    {
        // ORC.3.1 or OBR.3
    	String testFillerOrderNum          = null;
    	String testFillerOrderNumSource    = "NONE";
    	
    	if (orc != null) 
    	{
    		testFillerOrderNum        = orc.getTestFillerOrderNumber();
    		testFillerOrderNumSource  = "ORC.3.1";
    	}
    	
        if (StringUtils.isEmpty(testFillerOrderNum))
        {
            testFillerOrderNum          = obr.getTestFillerOrderNumber();
            testFillerOrderNumSource    = "OBR.3";
        }
        
        reportableResult.setTestFillerOrderNum(testFillerOrderNum);
        reportableResult.setTestFillerOrderNumSource(testFillerOrderNumSource);
    }
    
    private static void doMPQNumber(ReportableResult reportableResult, ReportResult result, Node resultNode)
    {
        // MPQ Number
        String mpqNumber = result.getSequenceId();
        
        if (StringUtils.isEmpty(mpqNumber))
        {
            Node zvxNode = XmlUtilities.findHL7Part(ZVX_NODE_EXPRESSION, (Element)resultNode);
        
            if (zvxNode != null) 
            {
                mpqNumber = new Zvx(zvxNode).getMpq();
            }            
        }
        
        reportableResult.setMpqSeqNumber(mpqNumber);
    }
    
    private static void doCountyAndJurisdiction(ReportableResult reportableResult, IResultSegment resultSegment) 
    {
        PatientInfo pid     = resultSegment.getPatientInfo();
        String zipcode      = pid.getZip();
        
        if (StringUtils.isEmpty(zipcode)) 
        {
            Provider pv1    = resultSegment.getProvider();
            zipcode         = pv1.getProviderZip();
        }
        
        if (StringUtils.isEmpty(zipcode)) 
        {
            OrderCommon orc = resultSegment.getOrderCommon();
            
            if (orc != null) 
            {
            	zipcode = orc.getOrderingZip();
            }
        }        
                
        JurisdictionAlgorithm jurisdictionAlgo  = NCDUtilities.getJurisdictionAlgorithm();
        County county                           = jurisdictionAlgo.determineCountyOrRegion(zipcode);
        Jurisdiction jurisdiction               = jurisdictionAlgo.determineJurisdiction(zipcode);
        
        reportableResult.setCounty(county);
        reportableResult.setJurisdiction(jurisdiction);
    }
    
    private static void parseAppFacLocIntoReportableResult( ReportableResult reportableResult, IResultSegment resultSegment, ReportResult reportResult) 
    {
        Provider pv1    = resultSegment.getProvider();
        String loc      = pv1.getSendingLocation();
                
        String app      = MessageHeader.getSendingApplication(reportResult.getMessage());
        String fac      = MessageHeader.getSendingFacility(reportResult.getMessage());
        
        reportableResult.setSendingApplication(app);
        reportableResult.setSendingFacility(fac);
        reportableResult.setSendingLocation(loc);
    }
    
    /** Create the patient referred to by the result
    *
    * @param result The reportable result for which patient information
    * should be saved.
    */
   private static Patient savePatient(PatientInfo patientInfo, String messageText ) 
   {
       //PersonService personService = Context.getPersonService();
       PatientService patientService = Context.getPatientService();
       
       Patient patient = new Patient();
       
       try 
       {
           patient.setBirthdate(patientInfo.getPatientBirth());
       }
       catch (Throwable t) 
       {
           log.error("error getting patient birth date: " + t.getMessage(), t);
       }
       
       Character patientSexAsChar = patientInfo.getPatientSex();
       String patientSex = "U";
       
       if (patientSexAsChar!=null)
       {
           patientSex = patientSexAsChar.toString();
       }       
       
       patient.setGender(patientSex);

       if (!savePersonAddress(patient, patientInfo)) 
       {
           log.debug("reportable result contains no patient address information.");
       }

       //savePersonAttribute(patient, PersonAttributeTypeCache.PERSON_ATTR_TYPE_GLOBAL_PATIENT_ID, patientInfo.getGlobalPatientId());
       savePersonAttribute(patient, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PATIENT_MEDREC_ID, patientInfo.getPatientInstitutionMedicalRecordId());
       savePersonAttribute(patient, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PHONE_NUMBER, patientInfo.getPatientPhone());
       savePersonAttribute(patient, PersonAttributeTypeCache.PERSON_ATTR_TYPE_RACE, patientInfo.getPatientRace());
       savePersonAttribute(patient, PersonAttributeTypeCache.PERSON_ATTR_TYPE_SSN, patientInfo.getPatientSSN());
       
       if (!savePersonName(patient, patientInfo)) 
       {
           log.debug("reportable result contains no patient name information.");
       }

       savePatientIdentifiers(patient, patientInfo, messageText );

       patientService.savePatient(patient);
       
       log.debug("saved patient=" + patient);
       
       return patient;
   }
   
   /** Create the provider referred to by providerInfo.
    * 
    * @param providerInfo The provider information to be saved, parsed
    * from the HL7 message.
    * @return The provider (Person) that was created or updated.
    */
   private static Person saveProvider(ProviderInfo providerInfo) 
   {
       PersonService personService      = Context.getPersonService();
       ProviderService providerService  = Context.getProviderService();
       Person providerPerson            = new Person();

       if (!savePersonAddress(providerPerson, providerInfo)) 
       {
           log.debug("reportable result contains no provider address information.");
       }

       // TODO: provideraddresssource is never currently set. We always
       // get the provider address from the same source.

       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID,           providerInfo.getLocalId());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID_SOURCE,    providerInfo.getLocalIdSource());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_NAME_SOURCE,        providerInfo.getNameSource());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PHONE_NUMBER,                providerInfo.getPhoneNumber());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_BIRTH,              providerInfo.getBirthDate());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_DEA_NUM,            providerInfo.getDeaNumber());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_FAX,                providerInfo.getFax());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_LICENSE,            providerInfo.getLicense());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_NAME_MATCHED,       providerInfo.getNameMatched());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_PRACTICE,           providerInfo.getPractice());
       savePersonAttribute(providerPerson, PersonAttributeTypeCache.PERSON_ATTR_TYPE_SSN,                         providerInfo.getSSN());

       // TODO: can we get the gender of providers somehow?
       providerPerson.setGender("U");
       
       User ncdUser                     = Context.getUserContext().getAuthenticatedUser();
       Date now                         = new Date();
       providerPerson.setCreator(ncdUser);
       providerPerson.setDateCreated(now);
       providerPerson.setPersonCreator(ncdUser);
       providerPerson.setPersonDateCreated(now);

       if (!savePersonName(providerPerson, providerInfo)) 
       {
           log.debug("reportable result contains no provider name information.");
       }
       else
       {
           Person savedPerson               = personService.savePerson( providerPerson );
           log.debug("saved savedPerson=" + savedPerson );           

           String providerFullname          = providerInfo.getFullName();
           org.openmrs.Provider provider    = new org.openmrs.Provider();
           provider.setName( providerFullname );
           provider.setCreator( ncdUser );
           provider.setDateCreated( now );
           provider.setPerson( savedPerson );
           provider.setId( null ); 
           provider.setProviderId( null );
           provider.setUuid( null ); // Trigger an insert.
       
           try
           {
               providerService.saveProvider(provider);
           }
           catch( Exception e )
           {
               ResultStorageHelper.log.error( "ResultStorageHelper.saveProvider() threw an Exception while attempting to save the Provider. e=" + e );
           }
       }
       
       return providerPerson;
   }

   /** Create an encounter between the specified patient and provider.
    * 
    * @param patient The patient involved in the encounter.
    * @param provider The provider involved in the encounter.
    * @param result The result being reported.
    * @return The Encounter.
    */
   @SuppressWarnings("deprecation")
   private static Encounter createEncounter(Patient patient, Person provider, ReportableResult result) 
   {  
       Encounter encounter  = new Encounter();
       User ncdUser         = Context.getUserContext().getAuthenticatedUser();
       Date now             = new Date();
       encounter.setCreator(ncdUser);
       encounter.setDateCreated(now);

       if (result.getTestDate() != null) 
       {
           encounter.setEncounterDatetime(result.getTestDate());
       }
       else 
       {
           encounter.setEncounterDatetime(now);
       }
       
       encounter.setEncounterType(findOrCreateNcdEncounterType());
       encounter.setLocation(Context.getLocationService().getLocation(1));
       encounter.setPatient(patient);
       encounter.setProvider(provider);
               
       //log.debug("stub encounter=" + encounter);
       return encounter;
   }

   /** Adds a group of obs rows representing one reportable result to
    * the supplied Encounter.
    * 
    * @param encounter The encounter to add the obs rows to.
    * @param result The reportable result to be recorded.
    */
   private static void addResultObsGroup(Encounter encounter, ReportableResult result) 
   {    
       ObsBuilder ob = new ObsBuilder(encounter);
       ob.addPrimaryObs(NCDConcepts.REPORTABLE_RESULT);
       ob.addObs(NCDConcepts.CONDITION_NAME, result.getConditionName());
       ob.addObs(NCDConcepts.REPORTABLE_RESULT_ID, result.getId());
    
       if (result.getProducer() != null) 
       {
           ob.addObs(NCDConcepts.APPLICATION, result.getProducer().getApplicationname());
           ob.addObs(NCDConcepts.FACILITY, result.getProducer().getFacilityname());
       }
       
       if (result.getInstitution() != null) 
       {
           ob.addObs(NCDConcepts.INSTITUTION_NAME, result.getInstitution().getName());
       }
       
       ob.addObs(NCDConcepts.LABORATORY_ADDRESS1, result.getLabStreet1());
       ob.addObs(NCDConcepts.LABORATORY_ADDRESS2, result.getLabStreet2());
       ob.addObs(NCDConcepts.LABORATORY_CITY, result.getLabCity());
       ob.addObs(NCDConcepts.LABORATORY_ID, result.getLabId());
       ob.addObs(NCDConcepts.LABORATORY_NAME, result.getLabName());
       ob.addObs(NCDConcepts.LABORATORY_PHONE, result.getLabPhone());
       ob.addObs(NCDConcepts.LABORATORY_STATE, result.getLabState());
       ob.addObs(NCDConcepts.LABORATORY_ZIP, result.getLabZip());
       
       if (result.getCode() != null) 
       {
           ob.addObs(NCDConcepts.LOINC_CODE_ID, result.getCode().getId().toString());
       }
       
       ob.addObs(NCDConcepts.OBR_ALT_CODE, result.getObrAltCode());
       ob.addObs(NCDConcepts.OBR_ALT_CODE_SYS, result.getObrAltCodeSystem());
       ob.addObs(NCDConcepts.OBR_ALT_CODE_TEXT, result.getObrAltCodeText());
       ob.addObs(NCDConcepts.OBX_ALT_CODE, result.getObxAltCode());
       ob.addObs(NCDConcepts.OBX_ALT_CODE_SYS, result.getObxAltCodeSystem());
       ob.addObs(NCDConcepts.OBX_ALT_CODE_TEXT, result.getObxAltCodeText());
       
       if (result.getPreviousReportableResult() != null) 
       {
           ob.addObs(NCDConcepts.PREVIOUS_REPORTABLE_RESULT_ID, result.getPreviousReportableResult().getId().toString());
       }
       
       if (result.getRawMessage() != null && result.getRawMessage().getId() != null) 
       {
           ob.addObs(NCDConcepts.RAW_HL7_ID, result.getRawMessage().getId().toString());
       }
       
       ob.addObs(NCDConcepts.TEST_ABNORMAL_FLAG, result.getTestAbnormalFlag());
       ob.addObs(NCDConcepts.TEST_CODE_SYSTEM, result.getTestCodeSystem());
       ob.addObs(NCDConcepts.TEST_COMMENT, result.getTestComment());
       ob.addObs(NCDConcepts.TEST_DATA_TYPE, result.getTestDataType());
       ob.addObs(NCDConcepts.TEST_DATE, result.getTestDate());
       ob.addObs(NCDConcepts.TEST_DATE_SOURCE, result.getTestDateSource());
       ob.addObs(NCDConcepts.TEST_FILLER_ORDER_NUMBER, result.getTestFillerOrderNum());
       ob.addObs(NCDConcepts.TEST_FILLER_ORDER_NUMBER_SOURCE, result.getTestFillerOrderNumSource());
       ob.addObs(NCDConcepts.TEST_ID, result.getTestId());
       ob.addObs(NCDConcepts.TEST_MPQ_SEQ_NUMBER, result.getMpqSeqNumber());
       ob.addObs(NCDConcepts.TEST_NAME, result.getTestName());
       ob.addObs(NCDConcepts.TEST_NORMAL_RANGE, result.getTestNormalRange());
       ob.addObs(NCDConcepts.TEST_PARENT_FILLER, result.getTestParentFiller());
       ob.addObs(NCDConcepts.TEST_PARENT_PLACER, result.getTestParentPlacer());
       ob.addObs(NCDConcepts.TEST_PLACER_ORDER_NUMBER, result.getTestPlacerOrderNum());
       ob.addObs(NCDConcepts.TEST_PLACER_ORDER_NUMBER_SOURCE, result.getTestPlacerOrderNumSource());
       ob.addObs(NCDConcepts.TEST_PREVIOUS_DATE, result.getTestPreviousDate());
       ob.addObs(NCDConcepts.TEST_RECEIVED_DATE_TIME, result.getMessageReceivedDateTime());
       ob.addObs(NCDConcepts.TEST_RESULT_CODE, result.getTestResultCode());
       ob.addObs(NCDConcepts.TEST_RESULT_CODE_SYSTEM, result.getTestResultCodeSystem());
       ob.addObs(NCDConcepts.TEST_RESULT_ID, result.getTestResultId());
       ob.addObs(NCDConcepts.TEST_RESULT_NAME, result.getTestResultName());
       ob.addObs(NCDConcepts.TEST_RESULT_STATUS, result.getTestResultStatus());
       ob.addObs(NCDConcepts.TEST_RESULT_SUB_ID, result.getTestResultSubId());
       ob.addObs(NCDConcepts.TEST_RESULT_UNITS, result.getTestResultUnits());
       ob.addObs(NCDConcepts.TEST_RESULT_VALUE, result.getTestResultValue());
       ob.addObs(NCDConcepts.TEST_SPECIMEN_TEXT, result.getTestSpecimenText());
   }

   /** Helper to add an address for the patient, if one is available
    *
    * @param person The person to be modified.
    * @param addressInfo The address information parsed from the HL7
    * message to be saved.
    * @return True if an address was saved, false if no address
    * information was provided.
    */
   private static boolean savePersonAddress(Person person, PersonAddressInfo addressInfo) 
   {
       final String street1     = StringUtilities.truncateString(StringUtilities.trim(addressInfo.getStreet1()),50);
       final String street2     = StringUtilities.truncateString(StringUtilities.trim(addressInfo.getStreet2()),50);
       final String city        = StringUtilities.truncateString(StringUtilities.trim(addressInfo.getCity()),50);
       final String country     = StringUtilities.truncateString(StringUtilities.trim(addressInfo.getCountry()),50);
       final String county      = StringUtilities.truncateString(StringUtilities.trim(addressInfo.getCounty()),50);
       final String zip         = StringUtilities.truncateString(StringUtilities.trim(addressInfo.getZip()),50);
       final String state       = StringUtilities.truncateString(StringUtilities.trim(addressInfo.getState()),50);
       
       // If we know any part of the patient address
       if (
               street1 != null 
               ||
               street2 != null 
               ||
               city != null 
               ||
               country != null 
               ||
               county != null 
               ||
               zip != null 
               || 
               state != null
          ) 
       {
           // Add the address (even if partial) to the patient
           PersonAddress address    = new PersonAddress();
           User ncdUser             = Context.getUserContext().getAuthenticatedUser();
           Date now                 = new Date();
           
           address.setCreator(ncdUser);
           address.setDateCreated(now);
           address.setAddress1(street1);
           address.setAddress2(street2);
           address.setCityVillage(city);
           address.setCountry(country);
           address.setCountyDistrict(county);
           address.setPostalCode(zip);
           address.setStateProvince(state);
           
           address.setPerson(person);
           person.addAddress(address);

           log.debug("added address=" + address);
           
           return true;
       }
       else 
       {
           return false;
       }
   }

   /** Helper to add an attribute to a person
    *
    * @param person The Person to be modified.
    * @param attributeType The name of the attribute to be added, e.g., "Race".
    * @param value The value of the attribute to be added, e.g. "W".
    */
   private static void savePersonAttribute(Person person, String attributeType, String value) 
   {
       final String trimmedValue = StringUtilities.trim(value);
       
       if (trimmedValue != null) 
       {
           PersonAttribute attr = new PersonAttribute();
           attr.setAttributeType(PersonAttributeTypeCache.find(attributeType));
           attr.setValue(trimmedValue);
   
           User ncdUser = Context.getUserContext().getAuthenticatedUser();
           Date now = new Date();
           attr.setCreator(ncdUser);
           attr.setDateCreated(now);
           
           person.addAttribute(attr);
           attr.setPerson(person);

           log.debug("adding PersonAttribute=" + attr);
       }
   }

   /** Helper to add an attribute to a person
   *
   * @param person The Person to be modified.
   * @param attributeType The name of the attribute to be added, e.g., "Race".
   * @param value The value of the attribute to be added, e.g. "W".
   */
  private static void savePersonAttribute(Person person, String attributeType, Date value) 
  {
      if (value != null) 
      {
          savePersonAttribute(person, attributeType, DateUtilities.format(value));
      }
  }

   /** Helper to add identifiers for the patient, if any are available.
    * 
    * TODO: add real patient identifiers.
    * 
    * @param patient The patient to be modified.
    * @param patientInfo The available information about the patient.
    */
   private static void savePatientIdentifiers(Patient patient, PersonAddressInfo patientInfo, String messageText ) 
   {    
       Message parserMessage    = null;
       String patientIdentifier = ResultStorageHelper.UNKNOWN_PATIENT_ID;
              
       try
       {
           PipeParser parser    = new PipeParser();
           messageText          = messageText.replaceAll( "\\\\.br\\\\", "" );
           parserMessage        = parser.parse( messageText );
       }
       catch( Exception e )
       {
           ResultStorageHelper.log.error( "ResultStorageHelper.savePatientIdentifiers() encountered a failure on messageText=\n" 
                                               + messageText 
                                               + ", e=" 
                                               + e );
           
           int markerBadPhone1of2   = e.getMessage().indexOf( ResultStorageHelper.MARKER_BAD_PHONE_PART1 );
           int markerBadPhone2of2   = e.getMessage().indexOf( ResultStorageHelper.MARKER_BAD_PHONE_PART2 );
                      
           if( markerBadPhone1of2 > -1 && markerBadPhone2of2 > -1 )
           {
               parserMessage = performHeroicsToFixPhoneNumberSoMessageCanBeParsed( messageText, e.getMessage() );
           }                      
           
           int markerBadPrim1of2    = e.getMessage().indexOf( ResultStorageHelper.MARKER_BAD_PRIMITIVE_VALUE_PART1 );
           int markerBadPrim2of2    = e.getMessage().indexOf( ResultStorageHelper.MARKER_BAD_PRIMITIVE_VALUE_PART2 );

           if( markerBadPrim1of2 > -1 && markerBadPrim2of2 > -1 )
           {
               parserMessage = performHeroicsToFixBadPrimitiveSoMessageCanBeParsed( messageText, e.getMessage(), markerBadPrim1of2, markerBadPrim2of2 );
           }
           
       }
              
       try
       {
           Terser parserTerser      = new Terser( parserMessage );
           patientIdentifier        = parserTerser.get( "/.PID-3" );
                   
           if( patientIdentifier.equals( ResultStorageHelper.UNKNOWN_PATIENT_ID ) )
           {
               ResultStorageHelper.log.error( "ResultStorageHelper.savePatientIdentifiers() did not find the patientIdentifier for messageText=\n" + messageText );
           }
                      
           int checkDigit           = ResultStorageHelper.checkdigit( patientIdentifier );
           patientIdentifier        = patientIdentifier + "-" + checkDigit;
       }
       catch( Exception e )
       {
           ResultStorageHelper.log.error( "ResultStorageHelper.savePatientIdentifiers() threw an Exception while attempting to parse messageText=\n" 
                                               + messageText 
                                               + ", e=" 
                                               + e );
       }
       
       PatientService patientService            = Context.getPatientService();

       // Find the "OpenMRS Identification Number" type
       PatientIdentifierType patientIdType      = patientService.getPatientIdentifierTypeByName("OpenMRS Identification Number");

       // Find the "unknown location" location
       Location unknownLocation                 = Context.getLocationService().getLocation(1);
       
       // Add it. The mrngen module wraps PatientService.savePatient
       // and replaces the identifier value.
       PatientIdentifier patientId              = new PatientIdentifier();
       User ncdUser                             = Context.getUserContext().getAuthenticatedUser();
       Date now                                 = new Date();
       
       patientId.setCreator(ncdUser);
       patientId.setDateCreated(now);
       patientId.setIdentifierType(patientIdType);
       patientId.setIdentifier( patientIdentifier );
       patientId.setLocation(unknownLocation);
       
       patient.addIdentifier(patientId);
       
       if( ResultStorageHelper.UNKNOWN_PATIENT_ID.equals( patientIdentifier ) )
       {
           ResultStorageHelper.log.error( "ResultStorageHelper.savePatientIdentifiers() unable to find patientId." );
       }
       
   }

   /** Helper to add a name for the patient, if one is available
    * 
    * @param person The patient to be modified.
    * @param result The result to take the patient name from.
    * @return true if enough name information was supplied to save a
    * name, false if no name was saved.
    */
   private static boolean savePersonName( Person person, PersonNameInfo nameInfo ) 
   {    
       String firstName   = StringUtilities.trim(nameInfo.getFirstName());
       String middleName  = StringUtilities.trim(nameInfo.getMiddleName());
       String lastName    = StringUtilities.trim(nameInfo.getLastName());
       String suffixName  = StringUtilities.trim(nameInfo.getSuffixName());
       
       // If we have at least some name information
       if( firstName != null || middleName != null || lastName != null ) 
       {
           if( firstName != null )
           {
               int indexOfPeriod = firstName.trim().indexOf( "." );
               int indexOfSpace  = firstName.trim().indexOf( " " );
               
               if( indexOfPeriod > -1 && indexOfSpace > -1 && (middleName == null || middleName.trim().length() == 0) )
               {
                   middleName   = firstName.substring( indexOfSpace + 1, indexOfPeriod );
                   firstName    = firstName.substring( 0, indexOfSpace );
               }
               else if( indexOfPeriod > -1 && (middleName == null || middleName.trim().length() == 0))
               {
                   firstName = firstName.replaceAll( "\\.", "" );
               }
               else if( indexOfPeriod > -1 )
               {
                   firstName = firstName.replaceAll( "\\.", "" );                   
               }
               else if ( indexOfPeriod == -1 && indexOfSpace > -1 )
               {
                   middleName   = firstName.substring( indexOfSpace + 1, firstName.length() );
                   firstName    = firstName.substring( 0, indexOfSpace );                   
               }
           }
           
           if( middleName != null )
           {
               int indexOfPeriod = middleName.trim().indexOf( "." );
               
               if( indexOfPeriod > -1 )
               {
                   middleName = middleName.replaceAll( "\\.", "" ).trim();
               }
           }
           
           if( lastName != null )
           {
               int indexOfPeriod        = lastName.trim().indexOf( "." );

               if( indexOfPeriod > -1 )
               {
                   lastName = lastName.replaceAll( "\\.", "" ).trim();
               }
               
               int indexOfApostrophe    = lastName.trim().indexOf( "'" );
               if( indexOfApostrophe > -1 )
               {
                   lastName = lastName.replaceAll( "'", "" ).trim();
               }
           }
           
           
           // Record it, even if partial
           PersonName name  = new PersonName();
           User ncdUser     = Context.getUserContext().getAuthenticatedUser();
           Date now         = new Date();
           
           name.setCreator(ncdUser);
           name.setDateCreated(now);
           name.setGivenName(firstName);
           name.setMiddleName(middleName);
           name.setFamilyName(lastName);
           name.setFamilyNameSuffix(suffixName);
           
           name.setPerson(person);
           person.addName(name);

           log.debug("added name=" + name + "\nPerson:" + person.toString( "" ) ); // Using an alternate fancy toString().
           
           return true;
       }
       else 
       {
           return false;
       }
   }

   /** Finds the ncd encounter type, if it exists, otherwise creates it.
    * 
    * @return The existing or created ncd encounter type.
    */ 
   private static EncounterType findOrCreateNcdEncounterType() 
   {
       final String ncdEncounterTypeName    = "NCD Reportable Result";
       EncounterService encounterService    = Context.getEncounterService();
       List <EncounterType> types           = encounterService.findEncounterTypes(ncdEncounterTypeName);
    
       if (types != null && types.size() > 0) 
       {    
           return types.get(0);
       }
       
       User ncdUser                         = Context.getUserContext().getAuthenticatedUser();
       Date now                             = new Date();

       EncounterType ncdType                = new EncounterType();
       ncdType.setCreator(ncdUser);
       ncdType.setDateCreated(now);
       ncdType.setDescription("Reportable Result detected by the NCD");
       ncdType.setName(ncdEncounterTypeName);
       encounterService.saveEncounterType(ncdType);
       
       return ncdType;
   }
   
   public static int checkdigit(String idWithoutCheckdigit) throws Exception
   {    
       // allowable characters within identifier
       String validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVYWXZ_";
     
       // remove leading or trailing whitespace, convert to uppercase
       idWithoutCheckdigit = idWithoutCheckdigit.trim().toUpperCase();
     
       // this will be a running total
       int sum = 0;
     
       // loop through digits from right to left
       for (int i = 0; i < idWithoutCheckdigit.length(); i++) 
       {
           //set ch to "current" character to be processed
           char ch = idWithoutCheckdigit.charAt(idWithoutCheckdigit.length() - i - 1);
     
           // throw exception for invalid characters
           if (validChars.indexOf(ch) == -1)
           {
               throw new Exception( "\"" + ch + "\" is an invalid character");
           }
           
           // our "digit" is calculated using ASCII value - 48
           int digit = (int)ch - 48;
     
           // weight will be the current digit's contribution to the running total
           int weight;
    
           if (i % 2 == 0) 
           { 
               // for alternating digits starting with the rightmost, we
               // use our formula this is the same as multiplying x 2 and
               // adding digits together for values 0 to 9.  Using the
               // following formula allows us to gracefully calculate a
               // weight for non-numeric "digits" as well (from their
               // ASCII value - 48).
               weight = (2 * digit) - (int) (digit / 5) * 9;
           } 
           else 
           {
               // even-positioned digits just contribute their ascii
               // value minus 48
               weight = digit;
           }
     
           // keep a running total of weights
           sum += weight;
       }
     
       // avoid sum less than 10 (if characters below "0" allowed,
       // this could happen)
       sum = Math.abs(sum) + 10;
     
       // check digit is amount needed to reach next number
       // divisible by ten
       return (10 - (sum % 10)) % 10;
   }
   
   private static Message performHeroicsToFixBadPrimitiveSoMessageCanBeParsed( String messageText, String exceptionText, int beforePrim, int afterPrim )
   {
       Message parsedMessage    = null;
       String primitiveValue    = null;
       
       try
       {
           int countOfStart     = ResultStorageHelper.MARKER_BAD_PRIMITIVE_VALUE_PART1.length(); 
           primitiveValue       = exceptionText.substring( beforePrim + countOfStart, afterPrim );
           primitiveValue       = primitiveValue.replaceAll( "\\'", "" );
           String updatedValue  = primitiveValue.replaceAll( ">", "" );
           updatedValue         = updatedValue.replaceAll( ">", "" );
           messageText          = messageText.replaceAll( primitiveValue.trim(), updatedValue.trim() );
           PipeParser parser    = new PipeParser();
           parsedMessage        = parser.parse( messageText );
       }
       catch( Exception e )
       {
           ResultStorageHelper.log.error( "ResultStorageHelper.performHeroicsToFixBadPrimitiveSoMessageCanBeParsed() encountered an Exception while attempting to fix exceptionText=" 
                                               + exceptionText 
                                               + ", beforePrim=" 
                                               + beforePrim 
                                               + ", afterPrim=" 
                                               + afterPrim 
                                               + "\nmessageText=" 
                                               + messageText );
       }
              
       return parsedMessage;
   }
   
   private static Message performHeroicsToFixPhoneNumberSoMessageCanBeParsed( String messageText, String exceptionText )
   {
       Message parsedMessage    = null;
       String phone             = null;
       boolean foundError       = true;
       int beforePhone          = -1;
       int afterPhonePID13      = -1;
       int afterPhoneORC14      = -1;
       int beforeDatetimeOBR34  = -1;
       int afterDatetimeOBR34   = -1;
       
       while( foundError )
       {
           try
           {
               beforePhone              = exceptionText.indexOf( ResultStorageHelper.MARKER_BAD_PHONE_PART1 );
               afterPhonePID13          = exceptionText.indexOf( ResultStorageHelper.MARKER_BAD_PHONE_PART2 );
               afterPhoneORC14          = exceptionText.indexOf( ResultStorageHelper.MARKER_BAD_PHONE_PART3 );
               beforeDatetimeOBR34      = exceptionText.indexOf( ResultStorageHelper.MARKER_BAD_DATETIME_PART1 );
               afterDatetimeOBR34       = exceptionText.indexOf( ResultStorageHelper.MARKER_BAD_DATETIME_PART2 );               
               
               if( beforePhone > -1 && afterPhonePID13 > -1 )
               {
                   phone = exceptionText.substring( beforePhone + ResultStorageHelper.MARKER_BAD_PHONE_PART1.length(), afterPhonePID13 - 2 );
               }
               else if( beforePhone > -1 && afterPhoneORC14 > -1 )
               {
                   phone = exceptionText.substring( beforePhone + ResultStorageHelper.MARKER_BAD_PHONE_PART1.length(), afterPhoneORC14 - 2 );                   
               }
               else if( beforeDatetimeOBR34 > -1 && afterDatetimeOBR34 > -1 )
               {
                   phone = exceptionText.substring( beforeDatetimeOBR34 + ResultStorageHelper.MARKER_BAD_DATETIME_PART1.length(), afterDatetimeOBR34 );
               }
               else
               {
                   foundError = false;
                   continue;
               }

               int numberOfPhoneDigits  = -1;
               
               if( phone != null )
               {
                   phone                = phone.trim().replaceAll( "'", "" );
                   numberOfPhoneDigits  = phone.length();
               }
                   
               if( numberOfPhoneDigits == 10 ) // This is just a raw phone number--get rid of it.
               {
                   messageText          = messageText.replaceAll( phone, "" ); 
                   PipeParser parser    = new PipeParser();
                   parsedMessage        = parser.parse( messageText );
               }
               else if( numberOfPhoneDigits == 12 ) // This one may contain parentheses, so we need to escape them for the regex to work.
               {
                   int indexOfOpenParen     = phone.indexOf( "(" );
                   int indexOfCloseParen    = phone.indexOf( ")" );
               
                   if( indexOfOpenParen == 0 && indexOfCloseParen == 4 )
                   {                                      
                       phone = "\\" + phone.substring( 0, 4 ) + "\\)"; // Escaping for open parenthesis for RegEx
                   }
                   
                   messageText          = messageText.replaceAll( phone, "" ); 
                   PipeParser parser    = new PipeParser();
                   parsedMessage        = parser.parse( messageText );
               }
               else if( numberOfPhoneDigits == 14 )
               {
                   int indexOfSpace     = phone.indexOf( " " );
                   String replacement   = null;
                   
                   if( indexOfSpace != -1 )
                   {
                       replacement = phone.replaceAll( " ", "" );
                   }
                   
                   phone                = "\\" + phone.substring( 0, 4 ) + "\\)" + phone.substring( 5, phone.length() ); // Escaping for open parenthesis for RegEx
                   messageText          = messageText.replaceAll( phone, replacement.trim() );
                   PipeParser parser    = new PipeParser();
                   parsedMessage        = parser.parse( messageText );
               }
               else
               {
                   ResultStorageHelper.log.error( "ResultStorageHelper.performHeroicsToFixPhoneNumberSoMessageCanBeParsed() encountered a fourth phone variation:" 
                                                       + "\n\tphone=" 
                                                       + phone 
                                                       + "\n\texceptionText=" 
                                                       + exceptionText 
                                                       + "\n\tmessageText=" 
                                                       + messageText );
               }
           }
           catch( DataTypeException dte )
           {
               String dteError  = dte.getMessage();
               int indexPhone   = dteError.indexOf( "US phone number" );
               int indexPrimVal = dteError.indexOf( "Primitive value " );
               int indexObr34   = dteError.indexOf( "requires to be empty or a HL7 datetime string at OBR-34" );
                              
               if( indexObr34 > -1 && indexPrimVal > -1 )
               {
                   String obr34Value    = dteError.substring( indexPrimVal + 17, indexObr34 );
                   obr34Value           = obr34Value.replaceAll( "\\'", "").trim();
                   messageText          = messageText.replaceAll( obr34Value, "" );
                   PipeParser parser    = new PipeParser();
                   
                   try
                   {
                       parsedMessage    = parser.parse( messageText );
                       foundError       = false;
                   }
                   catch( Exception e )
                   {
                       ResultStorageHelper.log.error( "ResultStorageHelper, messageText=" + messageText + ", e=" + e );

                   }
               }
               
               exceptionText = dte.getMessage(); // Get the next phone number.
           }
           catch( Exception e )
           {
               ResultStorageHelper.log.error( "ResultStorageHelper.performHeroicsToFixPhoneNumberSoMessageCanBeParsed() threw an Exception while attempting to fix the phone: beforePhone=" 
                                               + beforePhone 
                                               + ", afterPhonePID13=" 
                                               + afterPhonePID13
                                               + ", afterPhoneORC14="
                                               + afterPhoneORC14
                                               + ", phone="
                                               + phone
                                               + "\nexceptionText=" 
                                               + exceptionText 
                                               + "\nmessageText=" 
                                               + messageText );
               
               exceptionText = e.getMessage(); // Get the next phone number.
           }
       }
       
       return parsedMessage;
   }
   
   
   /**
    * @return the rawhl7
    */
   public RawMessage getRawMessage() 
   {
       return rawhl7;
   }

   /**
    * @param rawhl7 the rawhl7 to set
    */
   public void setRawMessage(RawMessage rawhl7) 
   {
       this.rawhl7 = rawhl7;
   }
   
}
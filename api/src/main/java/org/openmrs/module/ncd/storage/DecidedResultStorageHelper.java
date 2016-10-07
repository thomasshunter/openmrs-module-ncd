/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.critic.ReportResult;
import org.openmrs.module.ncd.critic.ReportResult.ReportResultStatus;
import org.openmrs.module.ncd.critic.ReportResult.ResultInfo;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.CriticDef;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.database.DecidedResultArchive;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.model.IResultSegment;
import org.openmrs.module.ncd.model.Note;
import org.openmrs.module.ncd.model.ResultSegmentFactory;
import org.openmrs.module.ncd.model.Zvx;
import org.openmrs.module.ncd.storage.ErrorStorageHelper.ErrorLevel;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class helps pull together all the information needed for a
 * DecidedResult object before persisting it.
 * 
 * @author jlbrown
 * 
 */
public class DecidedResultStorageHelper {
    
    private static Log logger = LogFactory.getLog(DecidedResultStorageHelper.class);
    
    private final static String ZVX_NODE_EXPRESSION = "//ZVX";    
    private final static String NEW_DISPOSITION = "new";     
    private final static String NO_CONDITION = "None";
    private final static String UNK_CONDITION = "Unknown";    
    private final static String CLASSIFIED_BY_NONE = "None";

    @Transactional(rollbackFor = Exception.class)
    public static void storeDecidedResultInformation(List<Node> msgSegments,
            ReportResult result, ResultStorageHelper resultStorage) throws DecidedResultStorageException, RawMessageStorageException {        
        DecidedResult decidedResultTemplate = createDecidedResultTemplate(msgSegments);
        List<DecidedResult> previousResults = getPreviousDecidedResults(decidedResultTemplate);

        if (previousResults == null || previousResults.isEmpty()) {

        	decidedResultTemplate = addSpecificsToTemplate(msgSegments, result, decidedResultTemplate);
            RawMessage rawMessage = null;
            if (ReportResultStatus.REPORT.equals(result.getReportResultStatus())) {
                rawMessage = resultStorage.getRawHL7();
            }
            previousResults = createNewDecidedResults(result, decidedResultTemplate, rawMessage);
            
        } else {
        	
            // We already have one or more results, so just update the
            // resultCount, dateModified and mpqSequenceNumber for each result.
            for (DecidedResult previousResult : previousResults) {
            	
                previousResult.setLastModified(new Date());
                Integer previousResultCount = previousResult.getResultCount();                
                previousResult.setResultCount(previousResultCount != null ? previousResultCount + 1 : 1);
                previousResult.setMpqSequenceNumber(getMpqSequenceNumber(msgSegments));
            }
        }

        ConditionDetectorService ncdService = NCDUtilities.getService();
        ncdService.saveDecidedResults(previousResults);
    }
    
    /**
     * 
     * This method gathers all the decided results information from the message.  This is the same 
     * information that is used to query by example for previous decided results.  Note, this
     * method purposefully excludes voided Decided Results entries.
     * 
     * @param msgSegments The message segments for which a decided result is being created.
     * @return A DecidedResult object containing all the information available in the message.
     */ 
    public static DecidedResult createDecidedResultTemplate(List<Node> msgSegments) throws DecidedResultStorageException
    {
        DecidedResult template = new DecidedResult();
        
        try {
	        Node firstSegment = msgSegments.get(0); 
	        IResultSegment resultSegment = ResultSegmentFactory.getResultSegment(firstSegment);
	        // Get the segment specific index info.
	        resultSegment.addDecidedResultIndexInfo(template);	        	        
	        
	        // Now get the stuff common to any result segment,
	        // i.e. the result value and any NTE values.
	        StringBuilder resultValue = new StringBuilder();
	        StringBuilder nteValue = new StringBuilder();
        
	        for (Node msgSegment : msgSegments)
	        {	        	
	        	IResultSegment result = ResultSegmentFactory.getResultSegment(msgSegment);
	        	
	        	if (result != null) {
	        		resultValue.append(result.getTestResultValue());
	        	}
	            nteValue.append(Note.getConcatenatedNoteValues(msgSegment));
	        }
	        template.setResultValue(resultValue.toString()); //OBX-5        
	        template.setNte(nteValue.toString()); //NTE	        
        } catch (Exception e) {
        	throw new DecidedResultStorageException(e);
        }
        
        return template;
    }	
    
    private static String getMpqSequenceNumber(List<Node> msgSegments) {
    	
        Node firstSegment = msgSegments.get(0);
        Node zvxSegment = XmlUtilities.findHL7Part(ZVX_NODE_EXPRESSION, (Element)firstSegment);
        if (zvxSegment != null) {
            return new Zvx(zvxSegment).getMpq();
        }
        return null;
    }

    // This adds some more information to the decided result template mainly based on the
    // findings in the ReportResult object.  Also adds the MPQ number.
    private static DecidedResult addSpecificsToTemplate(List<Node> msgSegments, ReportResult result, 
            DecidedResult template) throws DecidedResultStorageException
    {
        Node firstSegment = msgSegments.get(0);        
        IResultSegment resultSegment = null;
        // Get the segment specifics.
        try {
			resultSegment = ResultSegmentFactory.getResultSegment(firstSegment);
			resultSegment.addDecidedResultSpecifics(template);
		} catch (Exception e) {
			throw new DecidedResultStorageException(e);
		}                
        
        // Add the non-segment specifics.
        Date currentDate = new Date();
        template.setDateAdded(currentDate);
        template.setDateClassified(currentDate);        
        template.setLastModified(currentDate);
        
        template.setDisposition(NEW_DISPOSITION);        
        
        template.setMpqSequenceNumber(getMpqSequenceNumber(msgSegments));
        
        ReportResult.ReportResultStatus reportable = result.getReportResultStatus();
        template.setReportable(reportable.getText());
        
        return template;
    }

    private static List<DecidedResult> getPreviousDecidedResults(DecidedResult decidedResultTemplate)
            throws DecidedResultStorageException {
        try {       
            ConditionDetectorService ncdService = NCDUtilities.getService();
            List<DecidedResult> decidedResults = ncdService.findDecidedResults(decidedResultTemplate);

            return decidedResults;
        } catch (Exception e) {
            throw new DecidedResultStorageException(e);
        }
    }
   
    // Convert the report result from the critics and a template version
    // of the decided result (a partial "key", if you will), create the
    // decided results to be persisted.
    private static List<DecidedResult> createNewDecidedResults(ReportResult result, DecidedResult template, RawMessage message) 
    	throws DecidedResultStorageException
    {        
        ArrayList<DecidedResult> newResults = new ArrayList<DecidedResult>();
        Set<String> conditionNamesAdded = new HashSet<String>();
        Set<Condition> conditionList = result.getConditions();
        if (conditionList == null || conditionList.isEmpty()) {
            // We always want to add at least one decided result, even if
            // there are no conditions found.
            DecidedResult newNoConditionResult = new DecidedResult(template);
            newNoConditionResult.setResultCount(1);
            newNoConditionResult.setConditionName(NO_CONDITION);
            newNoConditionResult.setRawMessage(message);
            newResults.add(newNoConditionResult);
        } else { 
            for (Condition condition : result.getConditions()) {
                DecidedResult newResult = new DecidedResult(template);
                
                String conditionName = null;
                if (condition != null) {
                    conditionName = condition.getDisplayText();
                } else {            
                    logger.debug("report result has an unknown condition: " + result.toString());
                    conditionName = UNK_CONDITION;
                }
                
                // If we haven't already added a decided result for this
                // condition name, add one and remember we added it
                if (!conditionNamesAdded.contains(conditionName)) {                    
                    conditionNamesAdded.add(conditionName);
                    newResult.setConditionName(conditionName);
                    newResult.setResultCount(1);
                    newResult = setClassifiedBy(conditionName, result, newResult);
                    newResult.setRawMessage(message);
                    newResults.add(newResult);
                }
                
                
            }
        }

        return newResults;
    }
    
    private static DecidedResult setClassifiedBy(String conditionName, ReportResult result, DecidedResult decidedResult)
    	throws DecidedResultStorageException 
    {
    	String classifiedBy = CLASSIFIED_BY_NONE;
        if (result.getReportResultStatus() == ReportResult.ReportResultStatus.REPORT) {
        	ResultInfo resultInfo = result.getResultLocationForCondition(conditionName);        	
        	CriticDef critic = resultInfo.getCriticThatFoundResult();
        	if (critic != null) {        		
        		classifiedBy = critic.getName();
        	} else {
        		storeNoIndicatingCriticError(resultInfo, result);
        	}
        	
        }
        decidedResult.setClassifiedByWhom(classifiedBy);
        return decidedResult;
    }
    
    private static void storeNoIndicatingCriticError(ResultInfo resultInfo, ReportResult result)
    	throws DecidedResultStorageException
    {
    	String errorMsg = "No indicating critic for this reportable result.";
		StringBuilder additionalInfo = new StringBuilder();
		additionalInfo.append("Obr#: ");
		additionalInfo.append(resultInfo.getObrLoc());
		additionalInfo.append(" Obx#: ");
		additionalInfo.append(resultInfo.getObxStartLoc());
		additionalInfo.append(" - ");
		additionalInfo.append(resultInfo.getObxEndLoc());        		
		try {
			ErrorStorageHelper.storeError(errorMsg, additionalInfo.toString(), result.getMessage(), ErrorLevel.ERROR);
		} catch (RawMessageStorageException e) {
			throw new DecidedResultStorageException(e);
		}
    }
    
    /**
     * Based on a reportable result, either creates or updates a
     * "matching" decided result. Used when a reportable result flagged
     * for manual review is marked Report or Reject.
     * 
     * Basically a duplicate of storeDecidedResultInformation, but
     * using a reportable result as the information source.
     * 
     * @param reportableResult The source reportable result.
     * @return The created or updated decided result
     */
    public static DecidedResultArchive storeReviewedDecidedResult(ReportableResult reportableResult)
        throws DecidedResultStorageException
    {
        logger.info("enter");

        // Get the reportable status for the new DR.
        Date currentDate = new Date();
        logger.debug("manual review status=" + reportableResult.getManualReviewStatusType().getReviewStatus());
        String reportable = ReportResult.ReportResultStatus.DO_NOT_REPORT.getText();
        if (reportableResult.getManualReviewStatusType().getId() == ConditionDetectorService.reviewStatusTypeReleased) {
            reportable = ReportResult.ReportResultStatus.REPORT.getText();
        }
        logger.debug("reportable=" + reportable);

        // Create a decided result "key" to find "matching" rows
        DecidedResult decidedResultTemplate = createDecidedResultTemplate(reportableResult);
        List<DecidedResult> previousResults = getPreviousDecidedResults(decidedResultTemplate);

        if (previousResults == null || previousResults.isEmpty()) {

            logger.debug("no previous decided result, creating a new one.");
            
            decidedResultTemplate.setDataType(reportableResult.getTestDataType());
            decidedResultTemplate.setResultCode(reportableResult.getTestResultId());
            decidedResultTemplate.setResultCount(1);
            decidedResultTemplate.setDateAdded(currentDate);
            decidedResultTemplate.setDateClassified(currentDate);
            String classifier = Context.getAuthenticatedUser().getUsername();
            if (classifier == null || classifier.trim().length() == 0) {
                classifier = Context.getAuthenticatedUser().getSystemId();
            }
            decidedResultTemplate.setClassifiedByWhom(classifier);
            decidedResultTemplate.setConditionName(reportableResult.getConditionName());
            decidedResultTemplate.setLastModified(currentDate);
            decidedResultTemplate.setMpqSequenceNumber(reportableResult.getMpqSeqNumber());
            decidedResultTemplate.setObrText(reportableResult.getTestName());
            decidedResultTemplate.setObxText(reportableResult.getTestResultName());
            decidedResultTemplate.setLoincCode(reportableResult.getCode().getCode());
            decidedResultTemplate.setDisposition("new");
            decidedResultTemplate.setReportable(reportable);
            decidedResultTemplate.setObrAltText(reportableResult.getObrAltCodeText());
            decidedResultTemplate.setObxAltText(reportableResult.getObxAltCodeText());
            decidedResultTemplate.setRawMessage(reportableResult.getRawMessage());
            decidedResultTemplate.setManuallyReviewed(true);
            previousResults.add(decidedResultTemplate);
        }
        else {

            logger.debug("" + previousResults.size() + " previous decided results, updating them all.");

            // We already have one or more results, so just update the
            // resultCount, dateModified and reviewed flag for each
            // result.
            for (DecidedResult previousResult : previousResults) {
                
                logger.debug("  updating decided result with id " + previousResult.getId());
                
                previousResult.setLastModified(currentDate);
                previousResult.setManuallyReviewed(true);
                previousResult.setReportable(reportable);
            }
        }

        ConditionDetectorService ncdService = NCDUtilities.getService();
        List<DecidedResultArchive> archivedResults = ncdService.saveDecidedResults(previousResults);
        logger.info("exit");
        return archivedResults.get(0);
    }

    public static DecidedResult createDecidedResultTemplate(ReportableResult reportableResult) {
        
        DecidedResult decidedResultKey = new DecidedResult();
        decidedResultKey.setResultValue(reportableResult.getTestResultValue());
        decidedResultKey.setObr(reportableResult.getTestId());
        decidedResultKey.setObrCodeSystem(reportableResult.getTestCodeSystem());

        // Use the non-LOINC code and code system
        if ("LN".equals(reportableResult.getTestResultCodeSystem())) {

            decidedResultKey.setObx(reportableResult.getObxAltCode());
            decidedResultKey.setObxCodeSystem(reportableResult.getObxAltCodeSystem());
        }
        else {
            
            decidedResultKey.setObx(reportableResult.getTestResultId());
            decidedResultKey.setObxCodeSystem(reportableResult.getTestResultCodeSystem());
        }
        
        decidedResultKey.setNte(reportableResult.getTestComment());
        
        return decidedResultKey;
    }
}

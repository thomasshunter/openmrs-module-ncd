/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.openmrs.module.ncd.critic.AmbiguousConditionException;
import org.openmrs.module.ncd.critic.ApplicationFacilityFilteringCritic;
import org.openmrs.module.ncd.critic.IResultsCritic;
import org.openmrs.module.ncd.critic.ReportResult;
import org.openmrs.module.ncd.critic.ReportResult.ReportResultStatus;
import org.openmrs.module.ncd.critic.ResultCriticException;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.AlertType;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.MessageCountSummary;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.model.IResultSegment;
import org.openmrs.module.ncd.model.MessageHeader;
import org.openmrs.module.ncd.model.Provider;
import org.openmrs.module.ncd.model.ResultSegmentFactory;
import org.openmrs.module.ncd.model.Zvx;
import org.openmrs.module.ncd.preprocessing.MessagePreProcessor;
import org.openmrs.module.ncd.storage.CodeFrequencyStorageHelper;
import org.openmrs.module.ncd.storage.DecidedResultStorageException;
import org.openmrs.module.ncd.storage.DecidedResultStorageHelper;
import org.openmrs.module.ncd.storage.ErrorStorageHelper;
import org.openmrs.module.ncd.storage.InstitutionStorageHelper;
import org.openmrs.module.ncd.storage.ResultStorageHelper;
import org.openmrs.module.ncd.utilities.ContextUtilities;
import org.openmrs.module.ncd.utilities.MessageLoggerFactory;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author John Brown
 * 
 */
public class HL7MessageProcessor implements IMessageProcessor 
{
    private static Log logger = LogFactory.getLog(HL7MessageProcessor.class);
    
    private final static String ZVX_NODE_EXPRESSION = "//ZVX";

    private ResultStorageHelper resultStorage = null;
    
    private static AlertType ambiguousConditionAlertType = null;
    
    /**
     * Default constructor. Logs the message and result critics specified in configuration.
     */
    public HL7MessageProcessor() 
    {
    	resultStorage = null;
        logger.debug("messageCritics=" + NCDUtilities.getMessageCritics());
        logger.debug("resultsCritics=" + NCDUtilities.getResultsCritics());
    }

    /**
     * Analyzes and persists the results found in the message.
     * 
     * @param message The message to be processed.
     * @param routeId The routing identifier of the message.  Pass in null if there is
     *  no routing identifier available for the message.
     * @param sequenceId The sequence identifier of the message.  Pass in null if there
     *  is no sequence identifier available for the message.
     * @throws InvalidArgumentException Thrown if the message parameter is null or empty.
     * @throws MessageProcessorException Thrown if any error occurs during analysis or persistence.
     * 
     */
    public boolean processMessage(String message, String routeId, String sequenceId) throws MessageProcessorException 
    {
        logger.debug("enter processMessage ****************************.");
        // Before we do anything, check if this is a valid message                                                
        
        try 
        {
        	initializeContext();
        	resultStorage = new ResultStorageHelper(message); 
        	messageProcessingGuts(message);
            
            logger.debug("Done processing message ***********************.");
            return true;
        }
        catch (AmbiguousConditionException ace) 
        {
            logger.warn("Error processing message. Reason: " + ace.getMessage(), ace);
        	addAlert(ace, resultStorage);
            ErrorStorageHelper.storeError(ace, resultStorage.getRawHL7(), ErrorStorageHelper.ErrorLevel.ERROR);
            throw new MessageProcessorException(ace);
        }
        catch (Exception e) 
        {
            logger.warn("Error processing message. Reason: " + e.getMessage(), e);
            ErrorStorageHelper.storeError(e, resultStorage.getRawHL7(), ErrorStorageHelper.ErrorLevel.WARNING);
            throw new MessageProcessorException(e);
        }        
    }
    
    public void addAlert(AmbiguousConditionException ex, ResultStorageHelper resultStorage) 
    {
    	if (ambiguousConditionAlertType == null) 
    	{
    		ambiguousConditionAlertType = NCDUtilities.getService().findAlertTypeById(ConditionDetectorService.alertTypeAmbiguousConditionError);
    	}
    	
    	AlertSummary alertSummary = new AlertSummary();
    	Date d = new Date();
    	alertSummary.setFirstDate(d);
    	alertSummary.setLastDate(d);
    	alertSummary.setOccurrences(1);
    	alertSummary.setAlertType(ambiguousConditionAlertType);
    	alertSummary.setSummary(ex.getMessage());
    	alertSummary.setDetails(ex.getMessage());    	
    	alertSummary.setIdentity(ConditionDetectorService.alertTypeAmbiguousConditionError + "[" + ex.getCode() + "]");
    	NCDUtilities.getService().addAlertSummary(alertSummary);
    }
    
    public boolean reprocessMessage(org.openmrs.module.ncd.database.Error error) 
    {
    	boolean retVal = true;
    	
    	logger.debug("enter reprocessMessage ****************************.");
        // Before we do anything, check if this is a valid message
        logger.debug("Pre-processing message...");
        Collection<Privilege> proxyPrivileges = null;
    
        try 
        {
        	proxyPrivileges = ContextUtilities.addNCDProxyPrivileges();
        	RawMessage rawMessage = error.getRawMessage();
        	String message = rawMessage.getMessageText();
        	resultStorage = new ResultStorageHelper(message);      
        	resultStorage.setRawMessage(rawMessage);
        	messageProcessingGuts(message);
            
            logger.debug("Done re-processing message ***********************.");
            
            logger.debug("We successfully reprocessed the message that generated the error.  Deleting error entry...");
            
            try 
            {
            	NCDUtilities.getService().deleteError(error, "Message successfully reprocessed");            
            	logger.debug("Error entry deleted.");
            } 
            catch (Exception e) 
            {
            	// not much we can do except log that we weren't able to delete the error.
            	logger.error("Unable to delete the error entry for error ID # " + 
            			error.getId() + 
            			".  Reason: " + e.getMessage());
            }
            
            retVal = true;            
        }
        catch (Exception e) 
        {
            logger.warn("Error processing message. Reason: " + e.getMessage(), e);
            try 
            {
            	ErrorStorageHelper.updateError(error, e, resultStorage.getRawHL7());
            } 
            catch (MessageProcessorException ex) 
            {
            	// not much we can do except log that we weren't able to update the error.
            	logger.error("Unable to update the error with the new error information.  Reason: " + ex.getMessage());
            }
            
            retVal = false;
        }
        finally 
        {
            ContextUtilities.removeProxyPrivileges(proxyPrivileges);
        }    	  
        
        return retVal;
    }

	/**
	 * This method handles the common processing tasks for processMessage and reprocessMessage. 
	 */
	private void messageProcessingGuts(String message) throws MessageProcessorException 
	{	  	
		isValidMessage(message);                            
		ReportResult result = new ReportResult(message, ReportResultStatus.NONE);

		ApplicationFacilityFilteringCritic appFacFilterCritic = new ApplicationFacilityFilteringCritic();
		result = appFacFilterCritic.shouldReport(null, result);
		
		if (result.getReportResultStatus() != ReportResultStatus.DO_NOT_REPORT) 
		{                
		    result = analyzeMessage(message, null, result);
		}                        
		
		ReportResultStatus resultStatus = result.getReportResultStatus();
		
		if (resultStatus == ReportResultStatus.REPORT) 
		{
			persistResults(result);
		} 
		else if (resultStatus == null || resultStatus == ReportResultStatus.NONE || resultStatus == ReportResultStatus.UNKNOWN) 
		{
		    // If by this point, the status is still unknown, then the 
		    // message does not contain a condition requiring notification.
		    result.setReportResultStatus(ReportResultStatus.DO_NOT_REPORT);
		    result.setReasonForStatus("Marking segment DO_NOT_REPORT due to null, NONE, " +
		    		"or UNKNOWN status after all processing.");
		    // Flag this as a Critic negative since all critics passed on making this set of
		    // segments positive.
		    result.setFlaggedNegativeByCritic(true);
		}            
		
		resultStatus = result.getReportResultStatus();
		logger.debug("Segment is " + resultStatus.name());
		logger.debug("Reason: " + result.getReasonForStatus());

		recordProcessedMessage(message, result);
		recordMessageCountSummary(message, result);
	}

    /**
     * Analyzes a message and returns the results of the analysis.  Use this method
     * if you have previous results that you need to retain while also adding new results.
     * 
     * @param message The message to analyze.
     * @param sequenceId The sequence identifier of the message.  Pass in null if there
     *  is no sequence identifier available for the message.
     * @param result The ReportResult object that contains previously identified results.
     * @return A ReportResult object with any identified results.
     * @throws MessageProcessorException Thrown if an error occurs during result segment extraction.
     * @throws ResultCriticException Thrown if an error occurs during analysis.
     */
//	public ReportResult analyzeMessage(String message, String sequenceId,
//			ReportResult result) throws MessageProcessorException,
//			ResultCriticException {
//		isValidMessage(message);
//		logger.debug("Pre-processing message...");
//		String preProcessedMessage = doPreProcessing(message);
//		if (! preProcessedMessage.equals(message)) {
//			resultStorage.updateRawHL7(preProcessedMessage);
//		}
//		ReportResult newResult = new ReportResult(preProcessedMessage, ReportResultStatus.UNKNOWN);
//		logger.debug("Extracting candidate results...");
//		List<Node> candidateResults = extractCandidateResults(preProcessedMessage);
//		// 00000000000000000000000000000
//		if (candidateResults != null && candidateResults.size() != 0) {
//		    logger.debug("Doing message processing");
//		    newResult = executeMessageCritics(candidateResults, newResult);
//		    if (newResult.getReportResultStatus() != ReportResultStatus.DO_NOT_REPORT) {
//		        logger.debug("Iterating through candidate results...");
//		        newResult = iterateThroughCandidateResults(candidateResults, newResult, sequenceId);
//		    }
//		}
//		return newResult;
//	}  
// Decompile code from production	
    public ReportResult analyzeMessage(String message, String sequenceId, ReportResult result) throws MessageProcessorException, ResultCriticException
    {
            isValidMessage(message);
            logger.debug("Pre-processing message...");
            String preProcessedMessage = doPreProcessing(message);
        
            if(!preProcessedMessage.equals(message))
            {       
                resultStorage.updateRawHL7(preProcessedMessage);
            }
            
            ReportResult newResult = new ReportResult(preProcessedMessage, org.openmrs.module.ncd.critic.ReportResult.ReportResultStatus.UNKNOWN);
            
            if(preProcessedMessage.contains("MSH|^~\\&#|"))
            {
                preProcessedMessage = preProcessedMessage.replace("MSH|^~\\&#|", "MSH|^~\\&|");
            }
            
            if(preProcessedMessage.contains("|T|2.5.1|"))
            {
                preProcessedMessage = preProcessedMessage.replace("|T|2.5.1|", "|T|2.5|");
            }
            
            if(preProcessedMessage.contains("|D|2.5.1|"))
            {
                preProcessedMessage = preProcessedMessage.replace("|D|2.5.1|", "|D|2.5|");
            }
            
            if(preProcessedMessage.contains("|P|2.5.1|"))
            {
                preProcessedMessage = preProcessedMessage.replace("|P|2.5.1|", "|P|2.5|");
            }
            
            logger.debug("Extracting candidate results...");
            
            List<Node> candidateResults = extractCandidateResults(preProcessedMessage);
            
            if(candidateResults != null && candidateResults.size() != 0)
            {
                logger.debug("Doing message processing");
                newResult = executeMessageCritics(candidateResults, newResult);
            
                if(newResult.getReportResultStatus() != org.openmrs.module.ncd.critic.ReportResult.ReportResultStatus.DO_NOT_REPORT)
                {
                    logger.debug("Iterating through candidate results...");
                    newResult = iterateThroughCandidateResults(candidateResults, newResult, sequenceId);
                }
            }
            
            return newResult;
        }
	
	/**
     * Analyzes a message and returns the results of the analysis.  Use this method if
     * you have no results that you need to retain.
     * 
     * @param message The message to analyze.
     * @return A new ReportResult object with any identified results.
     * @throws MessageProcessorException Thrown if an error occurs during result segment extraction.
     * @throws ResultCriticException Thrown if an error occurs during analysis.
     */
	public ReportResult analyzeMessage(String message)
		throws MessageProcessorException, ResultCriticException {
		ReportResult result = new ReportResult(message, ReportResultStatus.NONE);		
		return analyzeMessage(message, null, result);
	}
	
	/**
	 * Analyzes a single candidate that is composed of one or more result segments.  Use this
	 * method if you have previous results you need to retain.
	 * @param msgSegments A List of Node objects that contain the result segments to be analyzed.
	 * @param reportResult The ReportResult object which contains previously identified results.
	 * @return A ReportResult object with any identified results.
	 * @throws ResultCriticException Thrown if an error occurs during candidate analysis.
	 */
	public ReportResult analyzeCandidate(List<Node> msgSegments, ReportResult reportResult) throws ResultCriticException 
	{
        ReportResult newResult  = new ReportResult(reportResult);
        newResult               = InstitutionStorageHelper.storeAppFacLocAndInstitution(msgSegments, newResult);
        newResult.setReportResultStatus(ReportResultStatus.UNKNOWN);
        
        for (IResultsCritic critic : NCDUtilities.getResultsCritics()) 
        {
            if (critic.doesApply(msgSegments)) 
            {
                newResult                       = critic.shouldReport(msgSegments, newResult);
                ReportResultStatus resultStatus = newResult.getReportResultStatus();
                
                if (resultStatus == ReportResultStatus.REPORT) 
                {
                    if (critic.isDecidedResultCritic()) 
                    {
                        newResult.setFlaggedPositiveByDecidedResultCritic(true);
                    } 
                    else 
                    {
                        newResult.setFlaggedPositiveByCritic(true);
                    }
                    
                    break;
                } 
                else if (resultStatus == ReportResultStatus.DO_NOT_REPORT) 
                {
                    if (critic.isDecidedResultCritic()) 
                    {
                        newResult.setFlaggedNegativeByDecidedResultCritic(true);
                    } 
                    else 
                    {                    
                        newResult.setFlaggedNegativeByCritic(true);
                    }
                    
                    break;
                }
            }
        }
        
        if (newResult.getReportResultStatus() == ReportResultStatus.UNKNOWN) 
        {
            // If by this point, the status is still unknown, then these 
            // segments do not contain a condition requiring notification.
        	newResult.setReportResultStatus(ReportResultStatus.DO_NOT_REPORT);
        	newResult.setReasonForStatus("Marking segment DO_NOT_REPORT due to UNKNOWN status after all processing.");
            // Flag this as a Critic negative since all critics passed on making this set of
            // segments positive.
        	newResult.setFlaggedNegativeByCritic(true);
        }
        
        return newResult;
    }
		

	/**
	 * Analyzes a single candidate that is composed of one or more result segments.  Use this
	 * method if you do not have previous results you need to retain.
	 * @param msgSegments A List of Node objects that contain the result segments to be analyzed.
	 * @return A new ReportResult object with any identified results.
	 * @throws ResultCriticException Thrown if an error occurs during candidate analysis.
	 */
	public ReportResult analyzeCandidate(List<Node> msgSegments) throws ResultCriticException 
	{
		ReportResult newResult = new ReportResult(ReportResultStatus.NONE);
	
		return analyzeCandidate(msgSegments, newResult);
	}

    private void isValidMessage(String message) 
    {
        if (StringUtils.isEmpty(message)) 
        {
            throw new IllegalArgumentException("The message parameter must not be null or empty.");
        }
    }
    
    private String doPreProcessing(String message) 
    {
    	String preProcessedMessage = message;
    	List<MessagePreProcessor> messagePreProcessors = NCDUtilities.getMessagePreProcessors();
	
    	if (messagePreProcessors != null) 
    	{	    	
	    	for (MessagePreProcessor messagePreProcessor : messagePreProcessors) 
	    	{
	    		preProcessedMessage = messagePreProcessor.preProcessMessage(preProcessedMessage);
	    	}
    	}
    	
    	return preProcessedMessage;
    }

    private ReportResult executeMessageCritics(List<Node> resultNodes, ReportResult result) throws ResultCriticException 
    {
        List<IResultsCritic> critics = NCDUtilities.getMessageCritics();
        
//System.out.println( "HL7MessageProcessor.executeMessageCritics() critics=" + debugCritics( critics ) );      
        
        for( IResultsCritic msgCritic : critics ) 
        {
            boolean doesApply = msgCritic.doesApply( resultNodes );
            
            if( doesApply ) 
            {
                result = msgCritic.shouldReport(resultNodes, result);
                
                if (result.getReportResultStatus() == ReportResultStatus.DO_NOT_REPORT) 
                {
                	result.setFlaggedNegativeByCritic(true);
                    break;
                } 
                else if (result.getReportResultStatus() == ReportResultStatus.REPORT) 
                {
                	result.setFlaggedPositiveByCritic(true);
                	break;
                }
            }
        }
        
        return result;
    }

    private List<Node> extractCandidateResults(String message) throws MessageProcessorException 
    {
        IResultFinderMap finderMap = (IResultFinderMap) getResultFinderMap();
        List<Node> candidateResults = new ArrayList<Node>();
    
        for (ICandidateResultFinder finder : finderMap.getFinders()) 
        {
            candidateResults.addAll(finder.findCandidateResults(message));
        }
        
        return candidateResults;        
    }

    private ReportResult iterateThroughCandidateResults(List<Node> candidateResults, ReportResult reportResult, String sequenceId) throws MessageProcessorException 
    {
        ListIterator<Node> candidateResultsIterator     = candidateResults.listIterator();
        SegmentSeparator separator                      = new SegmentSeparator();
        List<Node> msgSegments                          = separator.getNextSegmentGroup(candidateResultsIterator);
        ReportResult newResult                          = new ReportResult(reportResult);
    
        while (msgSegments != null) 
        {
            if (!msgSegments.isEmpty()) 
            {
                newResult = processResultSegments(reportResult, sequenceId, msgSegments);                               
                DecidedResultStorageHelper.storeDecidedResultInformation(msgSegments, newResult, resultStorage);
                // combine the two result status values
                newResult.setReportResultStatus(reportResult);
                reportResult = newResult; 
            }
            
            msgSegments = separator.getNextSegmentGroup(candidateResultsIterator);            
        }
        
        return newResult;
    }

    private ReportResult processResultSegments(ReportResult reportResult, String sequenceId, List<Node> msgSegments) throws MessageProcessorException 
    {
        Node resultNode = msgSegments.get(0);
        CodeFrequencyStorageHelper.incrementCodeFrequency(msgSegments, reportResult);        
        ReportResult result = new ReportResult(reportResult);
        
        try 
        {
            logger.debug(createSegmentInfoMessage(resultNode, sequenceId, result));
        } 
        catch (XPathExpressionException e) 
        {
            throw new MessageProcessorException(e);
        } 
        catch (MessageProcessorException e) 
        {
        	// ignore this type of exception
        }
        
        result = analyzeCandidate(msgSegments, reportResult);                
        
        return result;
    }

	private void persistResults(ReportResult result) throws MessageProcessorException, DecidedResultStorageException 
	{	
        try 
        {
            if( result.getNumConditions() == 0 ) 
            {
            	// If there were still no notifiable conditions found, create a warning in the log...
                logger.warn("One or more reportable conditions were found, but no determination as to what those conditions are could be made.\n");
                // and flag the message as containing at least one reportable condition with indeterminate condition
                result.setFlaggedIndeterminateCondition(true);
                
                result.addCondition(null, null, result.getIndicatingCriticId());
            } 
            
            logger.debug("Storing notifiable results.");        
            resultStorage.storeResult(result);            
        } 
        catch (Throwable e) 
        {
            logger.debug("Exception: " + e.getMessage(), e);
            throw new MessageProcessorException(e);
        }        
	}

    

    private String createSegmentInfoMessage(Node msgSegment, String sequenceId, ReportResult result) throws MessageProcessorException, XPathExpressionException 
    {
        final String UNKNOWN = "UNKNOWN";
        IResultSegment resultSegment = null;
        
        try 
        {
        	resultSegment = ResultSegmentFactory.getResultSegment(msgSegment);
        } 
        catch (Exception e) 
        {
        	throw new MessageProcessorException(e);
        }
        
        MessageHeader msh = resultSegment.getMessageHeader();
        StringBuilder sb = new StringBuilder();

        String segmentName = msgSegment.getNodeName();
        String segmentNum = msgSegment.getFirstChild().getTextContent();

        Node zvxNode = XmlUtilities.findHL7Part(ZVX_NODE_EXPRESSION, (Element)msgSegment);
        String mpqNumber = new Zvx(zvxNode).getMpq();

        if (result.getApplicationFacility() == null) 
        {
            InstitutionStorageHelper.retrieveInstitution(msgSegment, result);
        }
        
        HL7Producer appFac = result.getApplicationFacility();

        if (StringUtils.isEmpty((sequenceId))) 
        {
            if (StringUtils.isEmpty((mpqNumber))) 
            {
                mpqNumber = UNKNOWN;
            }
        } 
        else 
        {
            mpqNumber = sequenceId;
        }
        
        String msgControlId = msh.getMessageControlIdentifier();

        sb.append("Analyzing segment ");
        
        if (StringUtils.isEmpty((segmentName)) && StringUtils.isEmpty((segmentNum))) 
        {
            sb.append(UNKNOWN);
        } 
        else 
        {
            sb.append(segmentName);
            sb.append(" ");
            sb.append(segmentNum);
        }
        
        sb.append(" - ");
        sb.append(" MPQ#: ");
        sb.append(mpqNumber);
        sb.append(" Application/Facility: ");

        String sendingApp = MessageHeader.getSendingApplication(result.getMessage());
        String sendingFac = MessageHeader.getSendingFacility(result.getMessage());        
        
        if (appFac != null && StringUtils.isNotEmpty((appFac.getApplicationname()))) 
        {
            sb.append(appFac.getApplicationname());
        } 
        else if (StringUtils.isNotEmpty(sendingApp))
        {        
            sb.append(sendingApp);
        } 
        else 
        {
            sb.append(UNKNOWN);
        }
        
        sb.append("/");
        
        if (appFac != null && StringUtils.isNotEmpty((appFac.getFacilityname()))) 
        {
            sb.append(appFac.getFacilityname());
        } 
        else if (StringUtils.isNotEmpty(sendingFac)) 
        {
            sb.append(sendingFac);
        } 
        else 
        {
            sb.append(UNKNOWN);
        }

        sb.append(" Msg ID: ");
        sb.append(msgControlId);

        return sb.toString();
    }

    private IResultFinderMap getResultFinderMap() 
    {
        HashMap<String, ICandidateResultFinder> finderMap = new HashMap<String, ICandidateResultFinder>();
        String propertyValue = NCDUtilities.getResultFinderMapText().replaceAll(" ", "");

        logger.debug("deblanked resultFinderMapText=\"" + propertyValue + "\"");

        String[] finderPairs = propertyValue.split(",");
        
        for (String thisPair : finderPairs) 
        {
            logger.debug("thisPair=\"" + thisPair + "\"");
            String[] pairElements = thisPair.split("=");
         
            if (pairElements.length != 2 || pairElements[0].length() <= 0 || pairElements[1].length() <= 0) 
            {
                logger.error("expected the value of global property ncd.resultFinderMap " +
                		"to be a comma-separated list of <finder name>=<finder class fullname> pairs.");
            } 
            else 
            {
                String finderName = pairElements[0];
                String className = pairElements[1];
            
                try 
                {
                    if (StringUtils.isNotEmpty(className)) 
                    {
                        logger.debug("attempting to create an instance of class \"" + className + "\"");
                        Class<?> finderClass = Class.forName(className);
                        ICandidateResultFinder finderInstance = (ICandidateResultFinder) finderClass.newInstance();
                        finderMap.put(finderName, finderInstance);
                    }
                } 
                catch (Exception e) 
                {
                    logger.error("cannot create an instance of class \"" + className + "\"", e);
                }
            }
        }

        DefaultResultFinderMap ret = new DefaultResultFinderMap();
        ret.setResultFinderMap(finderMap);
        
        return ret;
    }
    
	private void recordProcessedMessage(String message, ReportResult result) 
	{
		NCDUtilities.getService().countProcessedMessage(
				MessageHeader.getSendingApplication(message),
				MessageHeader.getSendingFacility(message),
				Provider.getSendingLocation(message),
				new Date(),
				Zvx.getMpq(message));
		
		if (result.getReportResultStatus() != ReportResultStatus.REPORT) 
		{
			MessageLoggerFactory.getInstance().logMessage(message);
		}
	}
    
    private void recordMessageCountSummary(String message, ReportResult result) 
    {	
        // Record the message counts for this message
        MessageCountSummary messageCountSummary = new MessageCountSummary();
        messageCountSummary.setProcessedDate(new Date());
        String messageId = Zvx.getMpq(message); 
        
        // Each incoming message is assumed to be potentially reportable, so each message is counted in
        // the "Potentially Reportable" bucket
        //
        // TODO If responsibility for filtering out non-reportables based on LOINC codes is shifted 
        // from outside the NCD to inside the NCD, revisit how this count is managed.
        messageCountSummary.setPotentiallyReportable(1);
        
        // If the decided result critic flagged one or more reportable results, 
        // count the message once in the "Decided Result +" bucket.
        if (result.isFlaggedPositiveByDecidedResultCritic()) 
        {
        	messageCountSummary.setDecidedResultPositive(1);
        }
        
        // If the decided result critic flagged one or more non-reportable results, 
        // count the message once in the "Decided Result -" bucket.
        if (result.isFlaggedNegativeByDecidedResultCritic()) 
        {
        	messageCountSummary.setDecidedResultNegative(1);
        }

        // If a critic other than the decided result critic flagged one or more reportable results, 
        // count the message once in the "Critic +" bucket.
        if (result.isFlaggedPositiveByCritic()) 
        {
        	messageCountSummary.setCriticPositive(1);
        }

        // If a critic other than the decided result critic flagged one or more non-reportable results, 
        // count the message once in the "Critic -" bucket.
        if (result.isFlaggedNegativeByCritic()) 
        {
        	messageCountSummary.setCriticNegative(1);
        }
        
        // If no critic of any kind made a determination to either REPORT, or DO_NOT_REPORT the message,
        // count the message once in the "Critic -" bucket.
        if (
                !result.isFlaggedPositiveByDecidedResultCritic() &&
        		!result.isFlaggedPositiveByCritic() &&
        		!result.isFlaggedNegativeByDecidedResultCritic() &&
        		!result.isFlaggedNegativeByCritic()
           ) 
        {
        	messageCountSummary.setCriticNegative(1);
        }
        
        // If the message was flagged reportable by any critic, but the condition is unknown,
        // count the message once in the "Indeterminate +" bucket.
        if (result.isFlaggedIndeterminateCondition()) 
        {
        	messageCountSummary.setIndeterminate(1);
        }
        
        NCDUtilities.getService().addMessageCountSummary(messageCountSummary);
        logger.info("counts for messageId=" + messageId + 
                    ": potential=" + messageCountSummary.getPotentiallyReportable() + 
                    ", decidedPos=" + messageCountSummary.getDecidedResultPositive() + 
                    ", decidedNeg=" + messageCountSummary.getDecidedResultNegative() + 
                    ", criticPos=" +  messageCountSummary.getCriticPositive() +
                    ", criticNeg=" +  messageCountSummary.getCriticNegative() + 
                    ", indeterminatePos=" +  messageCountSummary.getIndeterminate());
    }
    
    private void initializeContext() 
    {        
        NCDUtilities.authenticate();                
    }
    
    
    // This is diagnostic only.
    @SuppressWarnings("unused")
    private String debugCritics( List<IResultsCritic> critics )
    {
        StringBuilder out = new StringBuilder();
        Iterator<IResultsCritic> it = critics.iterator();
        
        while( it.hasNext() )
        {
            IResultsCritic aCritic = it.next();
            out.append( aCritic.getClass().getCanonicalName() );
            out.append( "\n" );
        }
                
        return out.toString();
    }
}

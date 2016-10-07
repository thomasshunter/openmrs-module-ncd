/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;

import java.util.List;

import org.openmrs.module.ncd.critic.ReportResult;
import org.openmrs.module.ncd.critic.ResultCriticException;
import org.w3c.dom.Node;


/**
 * @author John Brown
 * 
 */
public interface IMessageProcessor
{

    /**
     * This method is called to process an HL7 message to determine if there are
     * reportable results contained within the message.
     * 
     * @param message The HL7 message to process.
     * @param routeId An ID to indicate the most recent stop on the route to the NCD.
     * @param sequeneceId An ID to identify this message.
     * @return A boolean with true indicating that the message was successfully
     *         processed and false indicating the message was not successfully
     *         processed.
     */
    public boolean processMessage(String message, String routeId, String sequenceId) throws MessageProcessorException;
    
    /**
     * This method is called to re-process an HL7 message, such as when a user requests re-processing of an error. 
     * If an error is encountered, this method will update the error if new errors are generated and return false.   
     * If no errors are generated, the error will delete the error row, dismiss the associated alert (if any),
     * and return true.
     * 
     * @param error The error entry that contains the message to be re-processed.
     * @return true if the message successfully reprocessed and false if the message still generates errors.
     */
    public boolean reprocessMessage(org.openmrs.module.ncd.database.Error error);
    
    /**
     * This method analyzes a message using a pre-existing ReportResult object.  Note, you must do your own
     * initialization of the OpenMRS context prior to calling this method.
     * 
     * @param message The HL7 message to process.
     * @param sequenceId An ID to identify this message.
     * @param result A ReportResult object that may contain information previously discovered about the message.
     * @return A new ReportResult object with any new information added to the previous information.
     * @throws MessageProcessorException
     */
    public ReportResult analyzeMessage(String message, String sequenceId, ReportResult result) 
    	throws MessageProcessorException;
    
    /**
     * This method analyzes one or more XML nodes from an HL7 message. Note, you must do your own initialization
     * of the OpenMRS context prior to calling this method.
     * 
     * @param msgSegments A List of Node objects containing segment data.
     * @param result A ReportResult object that may contain information previously discovered about the message
     * that contains the passed in segments.
     * @return A new ReportResult object with any new information added to the previous information.
     * @throws ResultCriticException
     */
    public ReportResult analyzeCandidate(List<Node> msgSegments, ReportResult result) throws ResultCriticException;
}

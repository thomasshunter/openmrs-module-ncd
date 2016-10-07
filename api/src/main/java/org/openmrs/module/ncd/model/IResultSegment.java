/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.model;


import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.module.ncd.critic.ConditionExtractor.CodeSystemPair;
import org.openmrs.module.ncd.database.DecidedResult;
import org.w3c.dom.Node;

/**
 * An interface for result segment model objects to follow.
 */
public interface IResultSegment
{

	public String getConcatenatedTestResultValues(List<Node> msgSegments);		
	
    /**
     * Gets the identifier for the lab or facility that generated this result
     * segment.
     * 
     * @return A String containing the identifier or null if no identifier is
     *         found in the result segment.
     */
    public String getLabIdentifier();

    /**
     * Gets the test date fron the result segment.
     * 
     * @return A Date object that contains the test date or null if no test date
     *         is found in the result segment.
     */
    public Date getTestDate() throws ParseException;

    /**
     * Gets the data type from the result segment.
     * 
     * @return A String containing the test data type or null if no data type is
     *         found in the result segment.
     */
    public String getTestDataType();

    /**
     * Gets the normal range as found in the result segment.
     * 
     * @return A String containing the normal range or null if no range is found
     *         in the result segment.
     */
    public String getNormalRange();

    /**
     * Gets the abnormal flag from the result segment.
     * 
     * @return A String containing the abnormal flag or null if no abnormal flag
     *         is found in the result segment.
     */
    public String getAbnormalFlag();

    /**
     * Gets the identifier from the result segment.
     * 
     * @return A String containing the identifier or null if no identifier is
     *         found in the result segment.
     */
    public String getTestResultIdentifier();

    /**
     * Gets the result name from the result segement.
     * 
     * @return A String containing the result name or null if no name is
     *         available in the result segement.
     */
    public String getTestResultName();

    /**
     * Gets the primary code system used by the result segment.
     * 
     * @return A String containing the code system or null if no code system is
     *         found in the result segment.
     */
    public String getTestResultCodeSystem();

    /**
     * Gets the secondary identifier for the result segment.
     * 
     * @return A String containing the secondary identifier or null if no
     *         secondary identifier is found in the result segment.
     */
    public String getTestResultSubIdentifier();

    /**
     * Gets the result code from the result segment.
     * 
     * @return A String containing the code or null if no code is found in the
     *         result segment.
     */
    public String getTestResultCode();

    /**
     * Gets the result value from the result segement.
     * 
     * @return A String containing the value or null if no value is found in the
     *         result segment.
     */
    public String getTestResultValue();
    
    /**
     * Gets the result value code from the result segment.
     * @return A string containing the value or null if no value is found in the
     *         result segment.
     */
    public String getTestResultValueCode();

    /**
     * Gets the units of the result segment.
     * 
     * @return A String containing the units or null if no unit indication is
     *         found in the result segment.
     */
    public String getTestResultUnits();

    /**
     * Gets the result status from the result segment.
     * 
     * @return A Character that represents the status of the result or null if
     *         no status is found in the result segment.
     */
    public Character getTestResultStatus();

    /**
     * Gets the altenate result code from the result segment.
     * 
     * @return A String containing the alternate code or null if no alternate
     *         code is found in the result segment.
     */
    public String getTestResultAltCode();

    /**
     * Gets the alternate result value from the result segement.
     * 
     * @return A String containing the alternate value or null if no alternate
     *         value is found in the result segment.
     */
    public String getTestResultAltCodeText();

    /**
     * Gets the alternate code system from the result segment.
     * 
     * @return A String containing the alternate code system or null if no
     *         alternate code system is found in the result segment.
     */
    public String getTestResultAltCodeSys();

    /**
     * Returns the order observation object that applies to this result node. If
     * no object applies, returns null.
     * 
     * @return An OrderObservation object.
     */
    public OrderObservation getOrderObservation()
           ;

    /**
     * Returns the patient info object that applies to this result node. If no
     * object applies, returns null.
     * 
     * @return A PatientInfo object.
     */
    public PatientInfo getPatientInfo();
    
    /**
     * Returns the patient additional demographics object that applies to this
     * result node.  If no object applies, returns null.
     * @return
     */
    public PatientAdditionalDemographics getPatientAdditionalDemographics();

    /**
     * Returns the provider object that applies to this result node. If no
     * object applies, returns null.
     * 
     * @return A Provider object.
     */
    public Provider getProvider();

    /**
     * Returns the order common object that applies to this result node. If no
     * object applies, returns null.
     * 
     * @return An OrderCommmon object.
     */
    public OrderCommon getOrderCommon();
    
    /**
     * Returns the message header object that applies to this result node.
     * 
     * @return A MessageHeader object.
     */
    public MessageHeader getMessageHeader();
    
    /**
     * Adds the Decided Result info used as the index to the template.
     * @param template - The DecidedResult object in which the index info is stored.
     */
    public void addDecidedResultIndexInfo(DecidedResult template);
    
    /**
     * Adds the Decided Result info from a result segment to the Decided Result object. 
     * @param template - The DecidedResult object in which the specifics are stored.
     */
    public void addDecidedResultSpecifics(DecidedResult template);
    
    public CodeSystemPair getCodeSystemPair();
    
    public Map<String, Staff> getStaffMap();

}
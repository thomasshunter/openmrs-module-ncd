/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;


import java.util.List;

import org.w3c.dom.Node;

/**
 * Interface used to find the candidate results from a message.
 * 
 * @author jlbrown
 *
 */
public interface ICandidateResultFinder
{

    /**
     * Examine an HL7 message and extract the segments
     * that may contain a reportable condition.
     * @param message The message to examine.
     * @return A list of XML Nodes that are the candidate segments.
     * @throws MessageProcessorException
     */
    public List<Node> findCandidateResults(String message)
            throws MessageProcessorException;

}
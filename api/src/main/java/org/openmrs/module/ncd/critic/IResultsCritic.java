/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.critic;

import java.util.List;

import org.w3c.dom.Node;

/**
 * Interface for message and result critics. The methods in this interface
 * determine if the critic applies to the particular result and makes a
 * determination on the disposition of the result. Possible determinations are
 * reportable, not reportable, and unknown.
 * 
 * @author John Brown
 * 
 */
public interface IResultsCritic {

    /**
     * This method is called to determine if the specified segment contains an
     * indication of a reportable condition.
     * 
     * @param msgSegments The segments to check for the indication of a reportable
     *        condition.
     * @param currentResult The ReportResult object that serves as an encyclopedia
     *        for infomration discovered.
     * @return A ReportResult that specifies whether the segments contain a
     *         reportable condition or not. It is possible that a single result
     *         critic will return an indeterminate result which indicates that
     *         further processing is needed.
     */
    public ReportResult shouldReport(List<Node> msgSegments,
            ReportResult currentResult) throws ResultCriticException;

    /**
     * Allows filtering of critics based on critic-specific criteria (e.g.
     * segment name).
     * 
     * @param msgSegments The list of segment nodes that will be passed into the
     *        critic.
     * @return True if this critic applies to the segment nodes. Otherwise,
     *         returns false.
     */
    public boolean doesApply(List<Node> msgSegments)
            throws ResultCriticException;
    
    /**
     * 
     * Determines if the critic in question is a critic of already determined results.
     * 
     * @return true if this critic is a critic of already determined results,
     * false if not.
     */
    public boolean isDecidedResultCritic();
}

/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao;

import java.io.Serializable;
import java.util.List;

import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.database.filter.SearchFilterReportableResults;
import org.openmrs.module.ncd.database.filter.SearchResult;

public interface IReportableResultDAO
{
    /**
     * Persist the reportable result data to the database.
     * @param reportableResult The reportable result data in the form of
     * a Tblreporableresult object.
     * @return A Serializable that contains the stored data.
     */
    public Serializable saveReportableResults(List<ReportableResult> reportableResult);

    public ReportableResult getReportableResult(long id);
    
    public SearchResult<ReportableResult> findReportableResults(SearchFilterReportableResults filter);
    
    public String exportReportableResults(SearchFilterReportableResults filter);
    
    /**
     * Rejects a reportable result that has been held for manual review.
     * This updates its status, and dismisses the associated dashboard alert.
     * 
     * @param result The reportable result to be rejected.
     */
    public void rejectReportableResult(ReportableResult result);

    /**
     * Releases a reportable result that has been held for manual review.
     * This updates its status, sets its release date, dismisses the associated dashboard alert, 
     * and updates the resultSeq in both the reportable result, and in the associated OpenMRS observation.
     * 
     * @param result The reportable result to be released.
     */
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
    public void reportableResultSentInError(List<ReportableResult> results);
    
    /**
     * Marks all reportable results in the list as not sent in error (if not already marked this way).
     * 
     * @param results The list of reportable results to be marked not sent in error.
     */
    public void reportableResultNotSentInError(List<ReportableResult> results);
}

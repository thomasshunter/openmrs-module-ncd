package org.openmrs.module.ncd.database.dao;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.User;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.AlertType;
import org.openmrs.module.ncd.database.filter.SearchFilterAlertSummary;
import org.openmrs.module.ncd.database.filter.SearchResult;
import org.openmrs.module.ncd.utilities.Pair;

public interface IAlertSummaryDAO {

    /**
     * Record an alert summary.
     * 
     * @param alertSummary The alert summary to be added (or updated).
     */
    public void addAlertSummary(AlertSummary alertSummary);
    
    /** Find alert summaries which match a filter
     * @param filter The search criteria.
     * @return A list of alert summaries that match the search criteria.  Sorted in descending date order.
     */
    public SearchResult<AlertSummary> findAlertSummaries(SearchFilterAlertSummary filter);
    
    /** Dismisses an alert summary with an optional reason
     * 
     * @param alertSummary The alert summary to dismiss.
     * @param user The current logged in user.
     * @param reason An optional reason for the dismissal.
     */
    public void dismissAlertSummary(AlertSummary alertSummary, User user, String reason);
    
    /** Dismisses several alert summaries with an optional reason
     * 
     * @param alerts The alert summaries to dismiss.
     * @param user The current logged in user.
     * @param reason An optional reason for the dismissal.
     */
    public void dismissAlertSummaries(List<AlertSummary> alertSummaries, User user, String reason);
    
    /** Undismisses an alert summary
     * 
     * @param alertSummary The alert summary to dismiss.
     */
    public void undismissAlertSummary(AlertSummary alertSummary);
    
    /** Undismisses several alert summaries
     * 
     * @param alertSummaries The alert summaries to undismiss.
     */
    public void undismissAlertSummaries(List<AlertSummary> alertSummaries);
    
    /**
     * Find an alert type by id
     * @param id
     * @return The AlertType for the specified id.
     */
    public AlertType findAlertTypeById(int id);

    /**
     * Return all alert types
     * @return The alert types as a ArrayList<Pair<Integer,String>>.
     */
    public ArrayList<Pair<Integer, String>> getAlertTypes();
}

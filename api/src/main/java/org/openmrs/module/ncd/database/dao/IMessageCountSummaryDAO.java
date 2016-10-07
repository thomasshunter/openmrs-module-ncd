package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.MessageCountSummary;
import org.openmrs.module.ncd.database.filter.SearchFilterMessageCountSummary;

public interface IMessageCountSummaryDAO {

    /**
     * Record a message count summary.
     * 
     * @param messageCountSummary The message count summary to be added.
     */
    public void addMessageCountSummary(MessageCountSummary messageCountSummary);
    
    /** Find message count summaries which match a filter
     * @param filter The search criteria.
     * @return A list of message count summaries that match the search criteria.  Sorted in descending date order.
     */
    public List<MessageCountSummary> findMessageCountSummaries(SearchFilterMessageCountSummary filter);
    
    /**
     * Resets the message count summaries
     * @return the number of rows deleted.
     */
    public int resetMessageCountSummaries();
}

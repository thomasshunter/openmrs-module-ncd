package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.MessageCountSummary;
import org.openmrs.module.ncd.database.dao.IMessageCountSummaryDAO;
import org.openmrs.module.ncd.database.filter.SearchFilterMessageCountSummary;

public class MessageCountSummaryDAO implements IMessageCountSummaryDAO {

    /** Debugging log */
    private static Log log = LogFactory.getLog(MessageCountSummaryDAO.class);

    /** Hibernate session factory, set by spring. */
    private SessionFactory sessionFactory;

    /**
     * Set session factory. Spring calls this based on the
     * moduleApplicationContext.xml
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Adds or updates the message count summary for the specified date, and also
     * for all dates.
     * 
     * @param messageCountSummary the new message count summary to be recorded.
     */
	public void addMessageCountSummary(MessageCountSummary messageCountSummary) {
    	try {
    		Session session = sessionFactory.getCurrentSession();
    		
    		// Get the row with id=1 (message count summary for all dates)
            Query query = session.createQuery("from MessageCountSummary where id=1");
            MessageCountSummary summaryForAllDates = (MessageCountSummary) query.uniqueResult();
            if (summaryForAllDates == null) {
            	summaryForAllDates = initialSummaryForDate(null);
            }
            
            // Update the message summary counts for all dates
            updateCounts(summaryForAllDates, messageCountSummary);
            session.saveOrUpdate(summaryForAllDates);
            
            // Get the row with processedDate = messageCountSummary.processedDate
            query = session.createQuery("from MessageCountSummary where processeddate=DATE_FORMAT(:theDate, '%Y-%m-%d')")
            	.setParameter("theDate", messageCountSummary.getProcessedDate());
            MessageCountSummary summary = (MessageCountSummary) query.uniqueResult();
            if (summary == null) {
            	summary = initialSummaryForDate(messageCountSummary.getProcessedDate());
            }

            // Update the message summary counts for messageCountSummary.processedDate
            updateCounts(summary, messageCountSummary);
            session.saveOrUpdate(summary);
    	}
    	catch (Exception e) {
    		log.error(e);
    	}
	}

	protected MessageCountSummary initialSummaryForDate(Date d) {
    	MessageCountSummary summaryForDate = new MessageCountSummary();
    	summaryForDate.setProcessedDate(d);
    	summaryForDate.setPotentiallyReportable(0);
    	summaryForDate.setDecidedResultPositive(0);
    	summaryForDate.setDecidedResultNegative(0);
    	summaryForDate.setCriticPositive(0);
    	summaryForDate.setCriticNegative(0);
    	summaryForDate.setIndeterminate(0);
    	return summaryForDate;
	}
	
	protected void updateCounts(MessageCountSummary oldSummary, MessageCountSummary newSummary) {
		oldSummary.setPotentiallyReportable(oldSummary.getPotentiallyReportable() + newSummary.getPotentiallyReportable());
		oldSummary.setDecidedResultPositive(oldSummary.getDecidedResultPositive() + newSummary.getDecidedResultPositive());
		oldSummary.setDecidedResultNegative(oldSummary.getDecidedResultNegative() + newSummary.getDecidedResultNegative());
		oldSummary.setCriticPositive(oldSummary.getCriticPositive() + newSummary.getCriticPositive());
		oldSummary.setCriticNegative(oldSummary.getCriticNegative() + newSummary.getCriticNegative());
		oldSummary.setIndeterminate(oldSummary.getIndeterminate() + newSummary.getIndeterminate());
	}

	/**
	 * Find message count summaries according to the filter options
	 * 
	 * @param filter the filter specifying the message count summaries to be fetched
	 * @return a list of MessageCountSummary matching the filter
	 */
    @SuppressWarnings("unchecked")
	public List<MessageCountSummary> findMessageCountSummaries(SearchFilterMessageCountSummary filter) {
		
    	// This method returns the message count summaries for:
    	//	o all dates
    	//	o dates in the range: [today - (n-2), today], in descending date order
    	//		where
    	//			today = today's date
    	//			n is the number of buckets (including the bucket for all dates)
		try {
    		Session session = sessionFactory.getCurrentSession();
    		List<MessageCountSummary> result = new ArrayList<MessageCountSummary>(filter.getNumberOfBuckets());
			
			// Get the message count summary for all dates, and store it in the result
            Query query = session.createQuery("from MessageCountSummary where id=1");
            MessageCountSummary summaryForAllDates = (MessageCountSummary) query.uniqueResult();
            if (summaryForAllDates == null) {
            	summaryForAllDates = initialSummaryForDate(null);
            }
            result.add(summaryForAllDates);
			
			// Compute the date range used for the query: [today - (n-2), today]
			Calendar c = new GregorianCalendar();
			Calendar c2 = (Calendar) c.clone();
			Date endDate = c.getTime();
			c.add(Calendar.DATE, -(filter.getNumberOfBuckets()-2));
			Date beginDate = c.getTime();
	
			// Get the message count summaries for the computed date range in descending date order
            query = session.createQuery("from MessageCountSummary where processeddate between DATE_FORMAT(:beginDate, '%Y-%m-%d') and DATE_FORMAT(:endDate, '%Y-%m-%d') order by processeddate desc")
            	.setParameter("beginDate", beginDate)
            	.setParameter("endDate", endDate);
            List<MessageCountSummary> queryResult = (List<MessageCountSummary>) query.list();

            // Set the time to midnight in the copy of the calendar for today's date,
            // so it matches the time component on dates read from the database (midnight)
            c2.set(Calendar.HOUR, 0);
            c2.set(Calendar.MINUTE, 0);
            c2.set(Calendar.SECOND, 0);
            c2.set(Calendar.MILLISECOND, 0);
            c2.set(Calendar.AM_PM, Calendar.AM);
            
			// For each date bucket (subtracting one for the "all dates" bucket)
            int listPos = 0;
			for (int i=0; i < filter.getNumberOfBuckets()-1; i++) {
				
				// Compute the expected date for this bucket
				Date expectedDate = c2.getTime(); 
				
				// Update the calendar for the next loop pass
				c2.add(Calendar.DAY_OF_MONTH, -1);

				// If the processed date for the message count summary matches the expected date
				if (listPos < queryResult.size() && queryResult.get(listPos).getProcessedDate().equals(expectedDate)) {
					result.add(queryResult.get(listPos++));
				}
				else {
					result.add(initialSummaryForDate(expectedDate));
				}
			}

			return result;
		} 
    	catch (Exception e) {
    		log.error(e);
    		// this isn't supposed to happen
    		return null;
    	}
	}

    /**
     * Resets (deletes) the message count summaries
     * @return the number of rows deleted.
     */
    public int resetMessageCountSummaries() {
    	int rowCount=0; 
    	try {
    		// Delete all counts except for the row for all dates (id=1)
    		Session session = sessionFactory.getCurrentSession();
            Query query = session.createQuery("delete from MessageCountSummary where id != 1");
            rowCount = query.executeUpdate();
            
    		// Get the row for all dates (id=1), and set the counts to 0
            query = session.createQuery("from MessageCountSummary where id=1");
            MessageCountSummary summaryForAllDates = (MessageCountSummary) query.uniqueResult();
            if (summaryForAllDates != null) {
            	summaryForAllDates.setPotentiallyReportable(0);
            	summaryForAllDates.setDecidedResultPositive(0);
            	summaryForAllDates.setDecidedResultNegative(0);
            	summaryForAllDates.setCriticPositive(0);
            	summaryForAllDates.setCriticNegative(0);
            	summaryForAllDates.setIndeterminate(0);
                session.saveOrUpdate(summaryForAllDates);
            }
    	}
    	catch (Exception e) {
    		log.error(e);
    	}
        return rowCount;
    }
}

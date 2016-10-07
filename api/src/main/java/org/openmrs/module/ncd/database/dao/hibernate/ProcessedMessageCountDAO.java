package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ncd.database.ProcessedMessageCount;
import org.openmrs.module.ncd.database.dao.IProcessedMessageCountDAO;

public class ProcessedMessageCountDAO implements IProcessedMessageCountDAO {

    private static Log log = LogFactory.getLog(ProcessedMessageCountDAO.class);

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

    @SuppressWarnings("unchecked")
	public void countProcessedMessage(String application, String facility,
    		String location, Date processedDateTime, String mpqSeqNumber)
    {
    	// Truncate the processed date time to an hour bucket
    	Calendar bucketCal = Calendar.getInstance();
    	bucketCal.setTime(processedDateTime);
    	bucketCal.set(Calendar.MINUTE, 0);
    	bucketCal.set(Calendar.SECOND, 0);
    	bucketCal.set(Calendar.MILLISECOND, 0);
    	Date bucketDateTime = bucketCal.getTime();
    	log.debug("bucket datetime=" + bucketDateTime);

    	Session session = sessionFactory.getCurrentSession();

    	// NOTE: Attempts to directly use an UPDATE statement to modify the
    	// messageCount and lastMpqSeqNumber fields had the effect of also
    	// munging the processedDateTime to the *current* date and time, not
    	// the bucket date and time.

    	// Find matching rows
        String searchText = "FROM ProcessedMessageCount" +
        " WHERE application = :app" +
        " AND facility = :fac" +
        " AND location = :loc" +
        " AND processedDateTime = :bucket";
        
        Query searchQuery = session.createQuery(searchText)
        	.setParameter("app", application)
        	.setParameter("fac", facility)
        	.setParameter("loc", location)
        	.setParameter("bucket", bucketDateTime);

        List<ProcessedMessageCount> counts = (List<ProcessedMessageCount>) searchQuery.list();

        ProcessedMessageCount c;
        if (counts.isEmpty()) {
        	
        	log.debug("new row");

        	c = new ProcessedMessageCount();
        	c.setApplication(application);
        	c.setFacility(facility);
        	c.setLocation(location);
        	c.setProcessedDateTime(bucketDateTime);
        	c.setMessageCount(1);
        	c.setLastMpqSeqNumber(mpqSeqNumber);
        }
        else {
        	
        	c = counts.get(0);
        	log.debug("old row=" + c.toString());

        	c.setMessageCount(c.getMessageCount() + 1);
        	c.setLastMpqSeqNumber(mpqSeqNumber);
        }

    	session.save(c);
    }
}

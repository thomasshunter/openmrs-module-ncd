package org.openmrs.module.ncd.critic;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.critic.IResultsCritic;
import org.openmrs.module.ncd.critic.ReportResult;
import org.openmrs.module.ncd.critic.ResultCriticException;
import org.openmrs.module.ncd.model.IResultSegment;
import org.openmrs.module.ncd.model.MessageHeader;
import org.openmrs.module.ncd.model.ResultSegmentFactory;
import org.w3c.dom.Node;

/**
 * Base class for message critics.
 * 
 * @author jlbrown
 *
 */
public abstract class MessageCritic implements IResultsCritic 
{		
	private static Log logger = LogFactory.getLog(MessageCritic.class);
	
    public boolean doesApply(List<Node> msgSegments) throws ResultCriticException
    {
        // This is a message critic, so it is always applicable.
        return true;
    }
    
    public boolean isDecidedResultCritic() 
    {
    	return false;
    }
    
    /**
	 * Defined by the subclass.
	 */
	public abstract ReportResult shouldReport(List<Node> msgSegments, ReportResult currentResult) throws ResultCriticException;
    
    protected MessageHeader getMessageHeader(Node msgSegment) 
    {
    	// We'll default to a dummy MessageHeader that returns null for everything.
    	MessageHeader retVal = new MessageHeader(null);    
    	
    	try 
    	{
    		IResultSegment resultSegment  = ResultSegmentFactory.getResultSegment(msgSegment);
    		retVal                        = resultSegment.getMessageHeader();
    	} 
    	catch (Exception e) 
    	{
    		// just log and return the null
    		logger.warn("Error getting result segment.  Reason - " + e.getMessage());
    	}
    	
    	return retVal;
    }
	
}

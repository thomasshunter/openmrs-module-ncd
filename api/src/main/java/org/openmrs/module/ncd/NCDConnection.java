/**
 * 
 */
package org.openmrs.module.ncd;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.utilities.ContextUtilities;

import ca.uhn.hl7v2.llp.HL7Reader;
import ca.uhn.hl7v2.llp.HL7Writer;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;

/**
 * @author jlbrown
 *
 */
public class NCDConnection implements Runnable 
{
	private static Log logger  = LogFactory.getLog(NCDConnection.class);	
	boolean shouldShutdown     = false;	
	Socket socket              = null;
	NCDServer server           = null;		
	IMessageProcessor ncd      = null;
	
	public NCDConnection( NCDServer server, Socket socket ) 
	{
		this.server = server;
		this.socket = socket;
		ncd = MesssageProcessorFactory.createInstance();
	}
	
	public void endConnection() 
	{
		shouldShutdown = true;
	}
	
	public void run() 
	{		
		try 
		{
			LowerLayerProtocol llp   = MinLowerLayerProtocol.makeLLP();
			HL7Reader hl7Reader      = llp.getReader(socket.getInputStream());					
			HL7Writer hl7Writer      = llp.getWriter(socket.getOutputStream());
			boolean connActive       = true;
			String message           = null;
		
			while (!shouldShutdown && connActive) 
			{
				Thread.yield();						
				
				try 
				{
					logger.debug("Receiving message...");
					message = hl7Reader.getMessage();
				
					if (message != null) 
					{
						if (logger.isDebugEnabled()) 
						{
							logger.debug("Message received:\n" + message);
						}
						
						server.incrementMessagesInProcessing();
						ContextUtilities.openSession();
						ncd.processMessage(message, null, null);						
						server.decrementMessageInProcessing();
						logger.debug("Message processing complete.");
						Thread.sleep(1);
					} 
					else 
					{
						connActive = false;
					}
				} 
				catch (MessageProcessorException e) 
				{
					// ignore these.
				    logger.error("MessageProcessorException", e);
				} 
				catch (Exception e) 
				{
					// ignore these for now.							
                    logger.error("Exception", e);
connActive=false;
				} 
				finally 
				{
					Context.closeSession();
					
					if (message != null) 
					{
						try 
						{
							hl7Writer.writeMessage(make_ack(message));								
							logger.debug("Ack sent.");
						} 
						catch (IOException e) 
						{
							//not much we can do if this fails.
						}
					}
					
					message = null;
				}
			}		
		} 
		catch (Throwable e) 
		{
			logger.warn("Unable to use connection established to " + socket.getInetAddress() + ".");
			logger.warn("Reason: " + e.getMessage(), e);			
		} 
		finally 
		{
			logger.debug("Disconnected from " + socket.getInetAddress().getHostAddress() + " on port " + socket.getPort() + ".");	
			
			try 
			{
				socket.close();
				server.removeConnection(this);
				socket = null;
			} 
			catch (IOException e) 
			{
				// not much we can do, so just ignore this exception 
			}
		}
	}
	
	private String make_ack(String msg) throws Exception {		
    	logger.debug("Creating ack...");
		if (!msg.startsWith("MSH")) {
			return null;
		}
		char fld = msg.charAt(3);
		String msh = nthField(msg, "\r", 0);		
		String sendingApp = nthField(msg, fld, 2);
		String sendingFac = nthField(msg, fld, 3);		
		StringBuffer sb = new StringBuffer(msh);
		replaceNthField(sb, fld, 2, nthField(msg, fld, 4));
		replaceNthField(sb, fld, 3, nthField(msg, fld, 5));
		replaceNthField(sb, fld, 4, sendingApp);
		replaceNthField(sb, fld, 5, sendingFac);
		String ack = sb.toString() + "\rMSA" + fld + "AA" + fld
				+ nthField(msg, fld, 9) + "\r";
		logger.debug("Ack created.");
		return ack;
	}

	private String nthField(String s, char ch, int n) {
		return nthField(s, ch + "", n);
	}

	private String nthField(String s, String ch, int n) {// 0 based
		int idx;
		int prev;
		int cnt = (n > 0 ? n : 0);
		int chlen = ch.length();
		for (prev = 0, idx = 0; (idx = s.indexOf(ch, prev)) > 0; prev = idx + chlen) {
			if (--cnt < 0) {
				break;
			}
		}
		if (idx < 0) {			
			return s.substring(prev);
		}

		return s.substring(prev, idx);
	}
	
	private StringBuffer replaceNthField(StringBuffer sb, char ch, int n,
			String newval) {// zero based
		int idx;
		int prev;
		int closer;
		int cnt = (n > 0 ? n : 0);
		// Find the n'th delim
		if (n < 0) {
			return sb;
		}
		for (prev = 0, idx = -1; cnt > 0; prev = idx + 1) {
			idx = indexOf(sb, ch, prev);
			if (idx < 0)
				break;
			cnt--;
		}
		if (cnt > 0) {
			/* then we need to pad input */
			while (cnt-- > 0) {
				sb.append(ch);
			}
			// The new field is just appended
			sb.append(newval);
			// We are done
			return sb;
		}
		// Is there a next delimiter
		closer = indexOf(sb, ch, idx + 1);
		if (closer > 0) {
			// Yup, replace the range
			sb.replace(idx + 1, closer, newval);
		} else {
			// No closer
			sb.replace(idx + 1, sb.length(), newval);
		}
		return sb;
	}
	
	private int indexOf(StringBuffer sb, char ch, int startat) {
		for (int i = startat; i < sb.length(); i++) {
			if (sb.charAt(i) == ch) {
				return i;
			}
		}

		return -1;
	}
}

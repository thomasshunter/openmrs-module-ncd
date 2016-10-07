/**
 * 
 */
package org.openmrs.module.ncd.model;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * @author jlbrown
 *
 */
public class ResultSegmentFactory 
{
	private static Log log = LogFactory.getLog(ResultSegmentFactory.class);
	private static Map<String,String> defaultResultSegmentMap = new HashMap<String,String>();
	
	static 
	{
		defaultResultSegmentMap.put("DG1", "org.openmrs.module.ncd.model.Diagnosis");
		defaultResultSegmentMap.put("OBX", "org.openmrs.module.ncd.model.Observation");		
	}
	
	public static IResultSegment getResultSegment(Node node) throws Exception 
	{
		String nodeName = node.getNodeName();
		String resultSegmentClassName = null;
		
		try 
		{
			Map<String, String> resultSegmentMap = NCDUtilities.getResultSegmentMap();
		
			if (resultSegmentMap == null || resultSegmentMap.isEmpty()) 
			{
				// we'll default to defining result segments for OBX and DG1
				// just in case someone hasn't gotten configuration setup.
				log.warn("Using default Result Segment map.  Is the configuration set up correctly?");
				resultSegmentMap = defaultResultSegmentMap;
			}
			
			resultSegmentClassName = resultSegmentMap.get(nodeName);
			IResultSegment resultSegment = null;
			
			if (resultSegmentClassName != null) 
			{
				log.trace("Getting result segment class: " + resultSegmentClassName);
				Class<?> resultSegmentClass = Class.forName(resultSegmentClassName);
			
				if (resultSegmentClass != null) 
				{	
					Class<?>[] parameterTypes = new Class[]{Node.class};					
					Constructor<?> resultSegmentConstructor = resultSegmentClass.getConstructor(parameterTypes);
				
					if (resultSegmentConstructor != null) 
					{						
						Object[] parameters = new Object[]{node};
						resultSegment = (IResultSegment) resultSegmentConstructor.newInstance(parameters);
					}
				}
			}		
			
			return resultSegment;
		} 
		catch (ClassNotFoundException e) 
		{
			throw new Exception("Could not find result segment class: " + resultSegmentClassName, e);
		}
	}
}

package org.openmrs.module.ncd.critic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.model.Observation;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 * 
 * This is a base class for critics which only examine OBX segments.
 * 
 * @author jlbrown
 *
 */
public abstract class ObxCritic implements IResultsCritic 
{
	private final static String LOINC_CODE_TYPE="LN";
	
	/**
	 * Returns true if the first segment in the list of segment nodes is an OBX segment, and
	 * returns false otherwise.
	 * 
	 * @exception ResultCriticException Thrown if a null or empty list of segment nodes is passed in.
	 */
	public boolean doesApply(List<Node> msgSegments) throws ResultCriticException 
	{
		if (msgSegments == null || msgSegments.isEmpty())
        {
            throw new ResultCriticException( "A null or empty list of segment nodes was passed into doesApply.");
        }
		
        Node firstNode  = msgSegments.get(0);
        String nodeName = firstNode.getNodeName();        
        
        return (nodeName.equals("OBX") ? true : false);
	}

	/**
	 * You can't be an OBX Critic and a Decided Result critic, so always return false.
	 */
	public boolean isDecidedResultCritic() 
	{
		return false;
	}

	/**
	 * Defined by the subclass.
	 */
	public abstract ReportResult shouldReport(List<Node> msgSegments, ReportResult currentResult) throws ResultCriticException;
	
	/**
	 * Get the set of Conditions to which a given LOINC code maps.
	 * @param loincCode The LOINC code for which we want the set of Conditions.
	 * @return The Set of Condition objects to which the given LOINC code maps.
	 */
	public static Set<Condition> getConditionSetForCode(String loincCode) 
	{
		Set<Condition> retVal             = new HashSet<Condition>(0);
        List<CodeCondition> loincCodeRows = NCDUtilities.getService().findByCodeAndSystem(loincCode, LOINC_CODE_TYPE);
                
        //System.out.println( "ObxCritic.getConditionSetForCode(), for loincCode=" + loincCode + ", pulled the following loincCodeRows=" + diagnosticDisplayLoincCodeRows( loincCodeRows ) );        
    
        if (loincCodeRows != null) 
        {
            for (CodeCondition loincCodeRow : loincCodeRows) 
            {
            	Condition condition = loincCodeRow.getCondition();
            	retVal.add(condition);            	
            }
        }
        
        return retVal;
	}
	
	/**
	 * Get the set of Conditions which could be found in this OBX segment.
	 * @param segment An OBX segment node.
	 * @return The Set of Condition objects to which the given segment maps.
	 */
	protected Set<Condition> getConditionSetForSegment(Node segment) 
	{
	    Observation obx    = new Observation(segment);
	    String loincCode   = obx.getLoincCode();
    
        return getConditionSetForCode(loincCode);
	}
	
	/**
	 * Get the set of reportable Conditions to which a given LOINC code maps.
	 * @param loincCode The LOINC code for which we want the set of Conditions.
	 * @return The Set of reportable Condition objects to which the given LOINC code maps.
	 */
	public static Set<Condition> getReportableConditionSetForCode(String loincCode) 
	{
        Set<Condition> retVal               = getConditionSetForCode(loincCode);
        List<Condition> conditionsToRemove  = new ArrayList<Condition>();
    
        for( Condition cond : retVal ) 
        {
        	if( !cond.isReportable() ) 
        	{
        		conditionsToRemove.add(cond);
        	}
        }   
        
        retVal.removeAll(conditionsToRemove);
        
        return retVal;
    }

	/**
	 * Get the set of reportable Conditions which could be found in this OBX segment.
	 * @param segment An OBX segment node.
	 * @return The Set of Condition objects to which the given segment maps.
	 */
	protected Set<Condition> getReportableConditionSetForSegment(Node segment) 
	{        
        Observation obx     = new Observation(segment);
        String loincCode    = obx.getLoincCode();
        
        //System.out.println( "ObxCritic.getReportableConditionSetForSegment(), about to get reportable conditions for lincCode=" + loincCode );        
    
        return getReportableConditionSetForCode(loincCode);               
    }        
	
	/**
	 * Get the set of non-reportable Conditions to which a given LOINC code maps.
	 * @param loincCode The LOINC code for which we want the set of Conditions.
	 * @return The Set of non-reportable Condition objects to which the given LOINC code maps.
	 */
	public static Set<Condition> getNonReportableConditionSetForCode(String loincCode) 
	{
        Set<Condition> retVal               = getConditionSetForCode(loincCode);
        List<Condition> conditionsToRemove  = new ArrayList<Condition>();
    
        for (Condition cond : retVal) 
        {
        	if (cond.isReportable()) 
        	{
        		conditionsToRemove.add(cond);
        	}
        }
        
        retVal.removeAll(conditionsToRemove);
        
        return retVal;
    }
	
	/**
	 * Get the set of non-reportable Conditions which could be found in this OBX segment.
	 * @param segment An OBX segment node.
	 * @return The Set of non-reportable Condition objects to which the given segment maps.
	 */
	protected Set<Condition> getNonReportableConditionSetForSegment(Node segment) 
	{        
        Observation obx     = new Observation(segment);
        String loincCode    = obx.getLoincCode();
    
        return getNonReportableConditionSetForCode(loincCode);               
    } 
	
	
	/** This is a diagnostic method that should not be run in production. */
	@SuppressWarnings("unused")
    private static String diagnosticDisplayLoincCodeRows( List<CodeCondition> loincCodeRows )
	{
	    StringBuilder out = new StringBuilder();
	    
	    if( loincCodeRows != null )
	    {
	        Iterator<CodeCondition> loincCodeRowsIt = loincCodeRows.iterator();
	        while( loincCodeRowsIt.hasNext() )
	        {
	            CodeCondition aCodeCondition = loincCodeRowsIt.next();
	            
	            out.append( "\n" + aCodeCondition.getCondition() );
	        }
	    }
	    	    
	    return out.toString();
	}
	
}

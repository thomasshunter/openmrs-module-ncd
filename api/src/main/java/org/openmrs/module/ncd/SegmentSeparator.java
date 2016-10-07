/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ncd.model.Observation;
import org.openmrs.module.ncd.model.OrderObservation;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;


/**
 * @author jlbrown
 * 
 */
public class SegmentSeparator
{

    private class ObxSeparationHelper
    {
        private String firstLoinc = null;
        private OrderObservation currentObr = null;
        
        public ObxSeparationHelper(String loinc, OrderObservation obr)
        {
            firstLoinc = loinc;
            currentObr = obr;
        }

        public boolean doesNextLoincMatch(Node segment)
        {
            Observation nextObx = new Observation(segment);            
            String nextLoincCode = nextObx
                    .getTestResultCodeByCodeSystem(OBX_CODE_SYSTEM);
            boolean retVal = false;
            if (StringUtils.isNotEmpty(nextLoincCode))
            {
                retVal = nextLoincCode.equals(firstLoinc);
            }
            return retVal;
        }

        public boolean doesObrMatch(Node segment)
        {
            Observation nextObx = new Observation(segment);            
            return nextObx.getOrderObservation().equals(currentObr);
        }
    }
    
    private static final String OBX_CODE_SYSTEM = "LN";
    private Boolean nteProcessingOn = false;
    
    public List<Node> getNextSegmentGroup(
            ListIterator<Node> candidateResultsIterator)
    {
        List<Node> retVal = null;
        
        nteProcessingOn = NCDUtilities.isNTEProcessingFlagOn();

        if( candidateResultsIterator.hasNext() )
        {
            Node firstSegment = candidateResultsIterator.next();
            String nodeName = firstSegment.getNodeName();
            candidateResultsIterator.previous();

            //TODO can we push these details into the model class?
            if( nodeName.equals("OBX") )
            {
                retVal = groupOBXSegments(candidateResultsIterator);
            }
            else if( nodeName.equals("DG1") )
            {
                retVal = groupDG1Segments(candidateResultsIterator);
            }
        }

        return retVal;
    }

    private List<Node> groupOBXSegments(ListIterator<Node> iterator)
    {
        final String OBX_CODE_SYSTEM = "LN";
        List<Node> retVal = new ArrayList<Node>();
        Node segment = iterator.next();        
        Observation firstObx = new Observation(segment);
        OrderObservation obr = firstObx.getOrderObservation();
        String firstLoincCode = firstObx
                .getTestResultCodeByCodeSystem(OBX_CODE_SYSTEM);

        if( firstLoincCode != null )
        {
            // Always add the first segment and any attached NTE segments
            // if we have a LOINC code.
            retVal.add(segment);
            retVal.addAll(getAttachedNTESegments(segment));
            if(iterator.hasNext())
            {                
                segment = iterator.next();
                ObxSeparationHelper sepHelper = new ObxSeparationHelper(firstLoincCode, obr);
                boolean loincMatches = sepHelper.doesNextLoincMatch(segment);
                boolean obrMatches = sepHelper.doesObrMatch(segment);
            
                // Iterate until we reach a LOINC that doesn't match, a new OBR
                // segment, or the end of the list.
                while( loincMatches && obrMatches && iterator.hasNext() )
                {
                    retVal.add(segment);
                    retVal.addAll(getAttachedNTESegments(segment));
                    segment = iterator.next();
                    loincMatches = sepHelper.doesNextLoincMatch(segment);
                    obrMatches = sepHelper.doesObrMatch(segment);
                }

                // Handle the end cases:
                // 1) If we get the end of the list with the same LOINC, we want to
                // add the last segment.
                // 2) If we get to a LOINC that doesn't match or have moved on to a
                // new OBR segment, we need to move the iterator back one segment.
                if( !iterator.hasNext() && loincMatches && obrMatches )
                {
                    retVal.add(segment);
                    retVal.addAll(getAttachedNTESegments(segment));
                }
                else if( !loincMatches || !obrMatches)
                {
                    iterator.previous();
                }
            }
        }

        return retVal;
    }

    private List<Node> groupDG1Segments(ListIterator<Node> iterator)
    {
        List<Node> retVal = new ArrayList<Node>();
        retVal.add(iterator.next());
        return retVal;
    }
    
    private List<Node> getAttachedNTESegments(Node node)
    {                
        List<Node> nteNodes = new ArrayList<Node>();

        // We only want to package the NTE segments if the NTE
        // processing flag in the configuration is on.
        if( nteProcessingOn )
        {
            Node siblingNode = node.getNextSibling();
            while (siblingNode != null)
            {
                if( siblingNode.getNodeName().equals("NTE") &&
                    siblingNode.hasChildNodes() )
                {
                    nteNodes.add(siblingNode);
                }
                siblingNode = siblingNode.getNextSibling();
            }
        }
        
        return nteNodes;
    }

}

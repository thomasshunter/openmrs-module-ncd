/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.model;

import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handles the Order Commmon segment information (ORC)
 * @author jlbrown
 *
 */
public class OrderCommon
{
    private Node orcNode = null;
    
    private final static String PLACER_ZIP_CODE_EXPRESSION = "./ORC.11.5/EI.1/text()";
    private final static String PLACER_ORDER_NUMBER_EXPRESSION = "./ORC.2/EI.1/text()";
    private final static String FILLER_ORDER_NUMBER_EXPRESSION = "./ORC.3/EI.1/text()";
    private final static String PARENT_PLACER_EXPRESSION = "./ORC.8/EIP.1/EI.1/text()";
    private final static String PARENT_FILLER_EXPRESSION = "./ORC.8/EIP.2/EI.1/text()";
    
    public OrderCommon(Node orcNode)
    {
        this.orcNode = orcNode;
    }
    public String getOrderingZip()
    {
        return XmlUtilities.findFieldValue(PLACER_ZIP_CODE_EXPRESSION, (Element)orcNode);
    }
    public String getTestPlacerOrderNumber() 
    {
        return XmlUtilities.findFieldValue(PLACER_ORDER_NUMBER_EXPRESSION, (Element)orcNode);
    }
    
    public String getTestFillerOrderNumber() 
    {
        return XmlUtilities.findFieldValue(FILLER_ORDER_NUMBER_EXPRESSION, (Element)orcNode);
    }
    
    public String getTestParentPlacer() 
    {
        return XmlUtilities.findFieldValue(PARENT_PLACER_EXPRESSION, (Element)orcNode);
    }
    
    public String getTestParentFiller() 
    {
        return XmlUtilities.findFieldValue(PARENT_FILLER_EXPRESSION, (Element)orcNode);
    }
}

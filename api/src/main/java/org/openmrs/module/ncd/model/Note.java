/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.model;

import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author jlbrown
 *
 */
public class Note
{
    private Node nteNode;
    
    private final static String NOTE_VALUE_EXPRESSION = "./NTE.3/text()";
    
    public static String getConcatenatedNoteValues(Node msgSegment) {
    	StringBuilder nteValues = new StringBuilder();
    	Node NTESegment = msgSegment.getNextSibling();    	
    	while (NTESegment != null && NTESegment.getNodeName().equals("NTE")) {
    		Note nte = new Note(NTESegment);
    		nteValues.append(nte.getNoteValue());
    		NTESegment = NTESegment.getNextSibling();
    	}
    	return nteValues.toString();
    }
    
    public Note(Node nte)
    {
        nteNode = nte;
    }
    
    public String getNoteValue()
    {
        return XmlUtilities.findFieldValue(NOTE_VALUE_EXPRESSION, (Element)nteNode);
    }
}

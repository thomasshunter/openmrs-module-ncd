/**
 * 
 */
package org.openmrs.module.ncd.model;

import org.openmrs.module.ncd.model.HL7NameHandler.NamePart;
import org.openmrs.module.ncd.utilities.XmlUtilities;
import org.w3c.dom.Node;

/**
 * Model class for the PD1 segment.
 * @author jlbrown
 *
 */
public class PatientAdditionalDemographics {
	private final static String PATIENT_PRIMARY_CARE_EXPRESSION = "./PD1.4/text()";	
	private Node pd1Node;
	private HL7NameHandler nameHandler;
	private Node primaryCareNode;		
	
	public PatientAdditionalDemographics(Node pd1Segment) {
		pd1Node = pd1Segment;
		nameHandler = NameFactory.getNameHandler(pd1Node);
		primaryCareNode = XmlUtilities.findHL7Part(PATIENT_PRIMARY_CARE_EXPRESSION, pd1Node);
	}
	
	public String getPatientPrimaryCareIdentifier() {		
		return HL7NameHandler.useNameHandler(nameHandler, primaryCareNode, NamePart.ID);
	}
	
	public String getPatientPrimaryCareLastName() {		
		return HL7NameHandler.useNameHandler(nameHandler, primaryCareNode, NamePart.FAMILY_NAME);
	}
	
	public String getPatientPrimaryCareFirstName() {		
		return HL7NameHandler.useNameHandler(nameHandler, primaryCareNode, NamePart.GIVEN_NAME);
	}
	
	public String getPatientPrimaryCareMiddleName() {		
		return HL7NameHandler.useNameHandler(nameHandler, primaryCareNode, NamePart.MIDDLE_NAME);
	}
	
	public String getPatientPrimaryCareSuffixName() {		
		return HL7NameHandler.useNameHandler(nameHandler, primaryCareNode, NamePart.SUFFIX_NAME);
	}
}


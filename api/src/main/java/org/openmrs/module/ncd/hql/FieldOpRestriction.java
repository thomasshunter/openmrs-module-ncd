package org.openmrs.module.ncd.hql;

public class FieldOpRestriction implements Element {

	private String reference;
	private String operator;

	public FieldOpRestriction(String reference, String operator) {
		
		this.reference = reference;
		this.operator = operator;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	/** Constructs and returns the HQL for this element.
	 * 
	 * @return The constructed HQL for this element.
	 */
	public String buildHQL(HQL instance) {
		
		return reference + " " + operator;
	}
}

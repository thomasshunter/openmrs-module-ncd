package org.openmrs.module.ncd.hql;

public class FieldOpFieldRestriction implements Element {

	private String leftRef;
	private String operator;
	private String rightRef;

	public FieldOpFieldRestriction(String leftRef, String operator, String rightRef) {
		
		this.leftRef = leftRef;
		this.operator = operator;
		this.rightRef = rightRef;
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
		
		return leftRef + operator + rightRef;
	}
}

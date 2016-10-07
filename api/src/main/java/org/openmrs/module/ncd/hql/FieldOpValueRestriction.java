package org.openmrs.module.ncd.hql;

public class FieldOpValueRestriction implements Element {

	private String reference;
	private String operator;
	private Object value;

	public FieldOpValueRestriction(String reference, String operator, Object value) {

		this.reference = reference;
		this.operator = operator;
		this.value = value;
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
		
		return reference + operator + instance.bind(value);
	}
}

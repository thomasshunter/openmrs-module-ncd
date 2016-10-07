package org.openmrs.module.ncd.hql;

public class ElementOpElement implements Element {

	private Element leftElement;
	private String operator;
	private Element rightElement;

	public ElementOpElement(Element leftElement, String operator, Element rightElement) {
		
		this.leftElement = leftElement;
		this.operator = operator;
		this.rightElement = rightElement;
	}

	@Override
	public boolean isEmpty() {
		return leftElement.isEmpty() && rightElement.isEmpty();
	}

	/** Constructs and returns the HQL for this element.
	 * 
	 * @return The constructed HQL for this element.
	 */
	public String buildHQL(HQL instance) {

		if (leftElement.isEmpty()) {

			return rightElement.buildHQL(instance);
		}
		else if (rightElement.isEmpty()) {
			
			return leftElement.buildHQL(instance);
		}
		else {
			
			return "(" + leftElement.buildHQL(instance) + ") " + 
				   operator + 
				   " (" + rightElement.buildHQL(instance) + ")";
		}
	}
}

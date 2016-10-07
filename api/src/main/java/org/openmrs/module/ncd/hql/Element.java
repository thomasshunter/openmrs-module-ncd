package org.openmrs.module.ncd.hql;

public interface Element {

	/** Tests if this Element is "empty". An empty element is an element
	 * like a Disjunction with no children, which is trivially true, and
	 * should be omitted by its parent.
	 */
	public boolean isEmpty();

	/** Constructs and returns the HQL for this element.
	 * 
	 * @return The constructed HQL for this element.
	 */
	public String buildHQL(HQL instance);
}

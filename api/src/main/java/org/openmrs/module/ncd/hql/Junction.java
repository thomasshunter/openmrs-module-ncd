package org.openmrs.module.ncd.hql;

import java.util.ArrayList;
import java.util.List;

public class Junction implements Element {

	private String operator;
	private List<Element> children = new ArrayList<Element>();

	protected Junction(String operator) {
		
		this.operator = operator;
	}

	public Junction add(Element element) {
	
		if (!element.isEmpty()) {
			this.children.add(element);
		}
		return this;
	}

	@Override
	public boolean isEmpty() {
		return children.size() == 0;
	}

	/** Constructs and returns the HQL for this element.
	 * 
	 * @return The constructed HQL for this element.
	 */
	public String buildHQL(HQL instance) {
		
		if (children.size() == 0) {
			
			return "";
		}
		else if (children.size() == 1) {
			
			Element child = children.get(0);
			return child.buildHQL(instance);
		}
		else {
			
			StringBuilder buf = new StringBuilder();
			boolean first = true;
			buf.append("(");
			for (Element child : children) {
				if (first) {
					first = false;
				}
				else {
					buf.append(") ");
					buf.append(operator);
					buf.append(" (");
				}
				buf.append(child.buildHQL(instance));
			}
			buf.append(")");
			return buf.toString();
		}
	}
}

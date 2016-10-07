package org.openmrs.module.ncd.careruleclient;

import java.util.ArrayList;
import java.util.List;

public class XmlNode {

	protected String name;
	protected String value;
	protected List<XmlNode> children;
	
	public XmlNode(String name) {
	
		this.name = name;
		this.value = null;
		this.children = new ArrayList<XmlNode>();
	}
	
	public XmlNode(String name, String value) {
		
		this.name = name;
		this.value = value;
		this.children = new ArrayList<XmlNode>();
	}

	public XmlNode(String name, List<XmlNode> children) {
		
		this.name = name;
		this.value = null;
		this.children = children;
	}

	public XmlNode add(XmlNode child) {
		this.children.add(child);
		return child;
	}

	public String toString() {
		StringBuilder out = new StringBuilder();
		
		if (value == null && children.size() == 0) {
			out.append("<");
			out.append(name);
			out.append("/>");
		}
		else {
			out.append("<");
			out.append(name);
			out.append(">");

			if (value != null) {
				out.append(value);
			}
			
			for (XmlNode child : children) {
				out.append(child.toString());
			}

			out.append("</");
			out.append(name);
			out.append(">");
		}
		
		return out.toString();
	}
}

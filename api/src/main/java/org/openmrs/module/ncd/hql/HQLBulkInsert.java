package org.openmrs.module.ncd.hql;

import java.util.ArrayList;
import java.util.List;

public class HQLBulkInsert extends HQL {

	private String intoTable;
	private List<String> intoColumns = new ArrayList<String>();
	private HQLSelect select;
	
	@Override
	public String buildHQL(HQL bindingInstance) {

		StringBuilder buf = new StringBuilder();
		buf.append("INSERT INTO ");
		buf.append(intoTable);
		buf.append(" (");
		buf.append(stringList(intoColumns));
		buf.append(") ");
		buf.append(select.buildHQL(bindingInstance));
		
		return buf.toString();
	}
	
	public HQLBulkInsert into(String intoTable) {
		
		this.intoTable = intoTable;
		return this;
	}
	
	public HQLBulkInsert column(String columnReference) {
		
		this.intoColumns.add(columnReference);
		return this;
	}
	
	public HQLBulkInsert select(HQLSelect select) {
		
		this.select = select;
		return this;
	}
}

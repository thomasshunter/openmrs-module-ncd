package org.openmrs.module.ncd.hql;

import java.util.ArrayList;
import java.util.List;

/** Builder for HQL SELECT statements
 * 
 * Example usage:
 * HQL stmt = HQL.select()
 *				 .setDistinct(true)
 *				 .column("t1.a")
 *				 .column("t2.b")
 *				 .from("table1 t1")
 *				 .from("table2 t2")
 *				 .where(HQL.notEqualProperty("t1.a", "t2.b"));
 * List values = stmt.query().list();
 * 
 * @author Erik Horstkotte
 */
public class HQLSelect extends HQL {

	private boolean distinct = false;
	private List<String> columns = new ArrayList<String>();
	private List<String> tables = new ArrayList<String>();
	private List<HQLSubSelect> subSelects = new ArrayList<HQLSubSelect>();
	private Element where = null;
	
	@Override
	public String buildHQL(HQL bindingInstance) {

		StringBuilder buf = new StringBuilder();
		
		buf.append("SELECT ");
		if (distinct) {
			buf.append("DISTINCT ");
		}
		buf.append(stringList(columns));
		
		if (tables.size() > 0 || subSelects.size() > 0) {
			
			buf.append(" FROM ");
		}

		buf.append(stringList(tables));

		boolean first = true;
		if (tables.size() > 0) {
			first = false;
		}
		
		for (HQLSubSelect subSelect : subSelects) {
			
			if (!first) {
				buf.append(", ");
			}
			first = false;
			buf.append(subSelect.buildHQL());
		}
		
		if (where != null && !where.isEmpty()) {

			buf.append(" WHERE ");
			buf.append(where.buildHQL(bindingInstance));
		}
		
		// group by
		// having
		// order by
		
		return buf.toString();
	}
	
	public HQLSelect setDistinct(boolean distinct) {

		this.distinct = distinct;
		return this;
	}

	public HQLSelect column(String columnReference) {

		columns.add(columnReference);
		return this;
	}
	
	public HQLSelect from(String tableReference) {
		
		tables.add(tableReference);
		return this;
	}
	
	public HQLSelect fromSelect(HQLSelect subSelect) {
		
		subSelects.add((HQLSubSelect) subSelect);
		return this;
	}
	
	public HQLSelect where(Element whereCondition) {
		
		this.where = whereCondition;
		return this;
	}
}

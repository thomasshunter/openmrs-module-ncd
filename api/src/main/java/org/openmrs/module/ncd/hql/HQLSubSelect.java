package org.openmrs.module.ncd.hql;


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
public class HQLSubSelect extends HQLSelect {

	private String alias = null;

	public HQLSubSelect(String alias) {
		super();
		this.alias = alias;
	}
	
	@Override
	public String buildHQL(HQL bindingInstance) {

		StringBuilder buf = new StringBuilder();
		buf.append("(");
		buf.append(super.buildHQL(bindingInstance));
		buf.append(") ");
		buf.append(alias);
		
		return buf.toString();
	}
}

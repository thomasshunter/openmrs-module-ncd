package org.openmrs.module.ncd.hql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;

/**
 * A class for dynamically constructing HQL bulk INSERT statements,
 * including complex nested SELECT statements. The interface to this
 * class is intended to conceptually resemble Hibernate's Critera class,
 * which does not support bulk insert operations.
 * 
 * HQLInsertBuilder b = new HQLInsertBuilder();
 * b.into("table1").column("a1").column("b1")
 *  .select(true).from("table2").column("a2").column("b2");
 * b.query(session).executeUpdate();
 * 
 * @see HQLQueryBuilder
 * 
 * @author Erik Horstkotte
 */
abstract public class HQL {
	
    private Log log = LogFactory.getLog(HQL.class);

	private Map<String, Object> binds = new HashMap<String, Object>();
	
	protected HQL() {
		
	}
	
	public static HQLBulkInsert bulkInsert() {

		return new HQLBulkInsert();
	}
	
	public static Element bulkDelete() {

		// TODO
		return null;
	}
	
	public static Element insert() {

		// TODO
		return null;
	}
	
	public static Element delete() {

		// TODO
		return null;
	}
	
	public static HQLSelect select() {

		return new HQLSelect();
	}
	
	public static HQLSubSelect subSelect(String alias) {

		return new HQLSubSelect(alias);
	}

	public static Element update() {
		
		// TODO
		return null;
	}
	
	public String buildHQL() {
	
		return buildHQL(this);
	}
	
	abstract public String buildHQL(HQL bindingContext);
	
	public Query query(Session session) {
		
		String queryText = buildHQL();
		log.debug("queryText=" + queryText);
		Query query = session.createQuery(queryText);
		for (String paramName : binds.keySet()) {
			Object value = binds.get(paramName);
			log.debug("bind " + paramName + " to " + value);
			query.setParameter(paramName, value);
		}
		return query;
	}

	public static Element and(Element left, Element right) {
		
		return new ElementOpElement(left, "AND", right);
	}
	
	public static Conjunction conjunction() {
		
		return new Conjunction();
	}
	
	public static Disjunction disjunction() {
		
		return new Disjunction();
	}
	
	public static Element equal(Element left, Element right) {
		
		return new ElementOpElement(left, "=", right);
	}
	
	public static Element or(Element left, Element right) {
		
		return new ElementOpElement(left, "OR", right);
	}
	
	public static Element notEqual(Element left, Element right) {
		
		return new ElementOpElement(left, "!=", right);
	}
	
	public static Element equalValue(String reference, Object value) {
		
		return new FieldOpValueRestriction(reference, "=", value);
	}
	
	public static Element notEqualValue(String reference, Object value) {
		
		return new FieldOpValueRestriction(reference, "!=", value);
	}
	
	public static Element equalProperty(String leftRef, String rightRef) {
		
		return new FieldOpFieldRestriction(leftRef, "=", rightRef);
	}
	
	public static Element notEqualProperty(String leftRef, String rightRef) {
		
		return new FieldOpFieldRestriction(leftRef, "!=", rightRef);
	}
	
	public static Element lessThanValue(String reference, Object value) {
		
		return new FieldOpValueRestriction(reference, "<", value);
	}
	
	public static Element isNull(String reference) {
		
		return new FieldOpRestriction(reference, "IS NULL");
	}
	
	protected String bind(Object value) {
		
		String bindName = "p" + binds.size();
		binds.put(bindName, value);
		return ":" + bindName;
	}
	
	protected String stringList(List<String> strings) {

		if (strings == null) {
			
			return "";
		}
		else {
			
			StringBuilder buf = new StringBuilder();
			for (String str : strings) {
			
				if (buf.length() > 0) {
					
					buf.append(", ");
				}

				buf.append(str);
			}
			
			return buf.toString();
		}
	}
}

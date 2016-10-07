package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openmrs.module.ncd.database.filter.SearchTerm;
import org.openmrs.module.ncd.database.filter.SearchTermOpString;

/** A helper class for constructing HQL queries from SearchFilters
 *
 * @author Erik Horstkotte
 */
public class HQLQueryBuilder {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        	    
    protected boolean useWhere = true;
    protected String selectList = null;
    protected StringBuffer fromBuf = new StringBuffer();
    protected StringBuffer prefetchJoins = new StringBuffer();
    protected StringBuffer whereBuf = new StringBuffer();
    protected List<Object> binds = new ArrayList<Object>();
    protected String sortBy = "";
    protected boolean sortAscending = true;

    public HQLQueryBuilder(String selectList, String objectName) {
        
        this.selectList = selectList;
        
        fromBuf.append(" from ");
        fromBuf.append(objectName);
    }
    
    public HQLQueryBuilder(String objectName) {

        fromBuf.append("from ");
        fromBuf.append(objectName);
    }

    // Examples:
    // inner join <object.field>, minimal abbreviation: join <object.field> 
    // left outer join <object.field>, minimal abbreviation: left join <object.field>
    // right outer join <object.field>, minimal abbreviation: right join <object.field>
    public void addJoin(String joinClause) {
    	
    	if (joinClause.charAt(0) != ' ') {
    	    fromBuf.append(" ");
    	}
    	fromBuf.append(joinClause);
    }
    
    // Prefetch joins are joins that exist only to force fetching of
    // objects referred to outside the session. Prefetch joins are not
    // included in count queries.
    public void addPrefetchJoin(String joinClause) {
        
        prefetchJoins.append(" ");
        prefetchJoins.append(joinClause);
    }
    
    public void add(String propertyName, SearchTerm term) {
        
        term.buildQueryClause(propertyName, this);
    }
    
    // TODO: the necessity for this method is now questionable. Using this
    // method for null tests didn't work for "institution name" on the
    // reportable result list page. Added a prefetch for rr.institution did.
    // The question now is whether a single "rr.institution.name IS NULL" test
    // will work or not.
    
    public String nullTest(String propertyRef) {

    	String[] elements = propertyRef.split("\\.");
    	if (elements.length <= 2) {
    		if (elements.length < 2) {
    			log.error("propertyRef doesn't include the table alias");
    		}
    		
    		// The normal case
    		return propertyRef + " IS NULL";
    	}
    	else {
    		
    		// Have to emit null tests for all parents
    		StringBuffer testBuf = new StringBuffer();
    		StringBuffer refBuf = new StringBuffer();
    		refBuf.append(elements[0]);
    		for (int i = 1; i < elements.length; i++) {
    			refBuf.append(".");
    			refBuf.append(elements[i]);
    			if (testBuf.length() > 0) {
    				testBuf.append(" OR ");
    			}
    			testBuf.append(refBuf);
    			testBuf.append(" IS NULL");
    		}
    		return testBuf.toString();
    	}

    	// elements[0] should be the table alias
    	// elements[n-1] should be the leaf property
    	// anything in between is a joined object
    }

    public void add(String propertyName, String operator, Object value) {
        
        if (!" ".equals(operator)) {

            whereBuf.append(andWhere());
            if (SearchTermOpString.OP_CONTAINS.equals(operator)) {
    
                whereBuf.append(propertyName);
                whereBuf.append(' ');
                whereBuf.append("like '%");
                whereBuf.append(escape(value.toString()));
                whereBuf.append("%'");
            }
            else if (SearchTermOpString.OP_IS_MISSING.equals(operator)) {
                
            	whereBuf.append("(");
                whereBuf.append(nullTest(propertyName));
                whereBuf.append(" OR LENGTH(TRIM(");
                whereBuf.append(propertyName);
                whereBuf.append("))=0)");
            }
            else if (SearchTermOpString.OP_IS_PRESENT.equals(operator)) {
                
            	whereBuf.append("(");
                whereBuf.append(propertyName);
                whereBuf.append(" IS NOT NULL AND LENGTH(TRIM(");
                whereBuf.append(propertyName);
                whereBuf.append("))>0)");
            }
            else {
            	
                whereBuf.append(propertyName);
                whereBuf.append(' ');
                whereBuf.append(operator);
                whereBuf.append(" :");
                whereBuf.append(addBind(value));
            }
        }
    }
    
    /** Like add(), but utilizes value as a literal without binding it, and the caller is responsible for 
     * formatting it including quoting and escaping except when using the contains operator */
    public void addLiteral(String propertyName, String operator, String value) {
        
        if (!" ".equals(operator)) {

            whereBuf.append(andWhere());
            whereBuf.append(propertyName);
            whereBuf.append(' ');
            if (operator.equals("contains")) {
    
                whereBuf.append("like '%");
                whereBuf.append(escape(value));
                whereBuf.append("%'");
            }
            else {
                whereBuf.append(operator);
                whereBuf.append(" ");
                whereBuf.append(value);
            }
        }
    }
    
    public void addOr(String propertyName1, String operator1, Object value1, String propertyName2, String operator2, Object value2) {
        
        whereBuf.append(andWhere());
        whereBuf.append("((");
        whereBuf.append(propertyName1);
        whereBuf.append(' ');
        if (operator1.equals("contains")) {

            whereBuf.append("like '%");
            whereBuf.append(escape(value1.toString()));
            whereBuf.append("%'");
        }
        else {
            whereBuf.append(operator1);
            whereBuf.append(" :");
            whereBuf.append(addBind(value1));
        }
        whereBuf.append(") or (");
        whereBuf.append(propertyName2);
        whereBuf.append(' ');
        if (operator2.equals("contains")) {

            whereBuf.append("like '%");
            whereBuf.append(escape(value2.toString()));
            whereBuf.append("%'");
        }
        else {
            whereBuf.append(operator2);
            whereBuf.append(" :");
            whereBuf.append(addBind(value2));
        }
        whereBuf.append("))");
    }
    
    /** Like addOr(), but utilizes values as literals without binding them, and the caller is responsible for 
     * formatting them including quoting and escaping except when using the contains operator */
    public void addOrLiteral(String propertyName1, String operator1, String value1, String propertyName2, String operator2, String value2) {
        
        whereBuf.append(andWhere());
        whereBuf.append("((");
        whereBuf.append(propertyName1);
        whereBuf.append(' ');
        if (operator1.equals("contains")) {

            whereBuf.append("like '%");
            whereBuf.append(escape(value1));
            whereBuf.append("%'");
        }
        else {
            whereBuf.append(operator1);
            whereBuf.append(" ");
            whereBuf.append(value1);
        }
        whereBuf.append(") or (");
        whereBuf.append(propertyName2);
        whereBuf.append(' ');
        if (operator2.equals("contains")) {

            whereBuf.append("like '%");
            whereBuf.append(escape(value2));
            whereBuf.append("%'");
        }
        else {
            whereBuf.append(operator2);
            whereBuf.append(" ");
            whereBuf.append(value2);
        }
        whereBuf.append("))");
    }
    
    public void setSort(String sortBy) {
        
        this.sortBy = " order by " + sortBy;
    }
    
    public void setSortAscending(boolean sortAscending) {
        
        this.sortAscending = sortAscending;
    }
    
    public Query getQuery(Session dbSession) {

        StringBuffer finalBuf = new StringBuffer();
        if (selectList != null) {
            finalBuf.append("select ");
            finalBuf.append(selectList);
        }
        finalBuf.append(fromBuf);
        finalBuf.append(prefetchJoins);
        finalBuf.append(whereBuf);
        finalBuf.append(this.sortBy);
        if (sortBy.length() > 0) {
            if (sortAscending) {
                
                finalBuf.append(" asc");
            }
            else {
                
                
                finalBuf.append(" desc");
            }
        }
        
        return getCoreQuery(dbSession, finalBuf.toString());
    }

    public Query getCountQuery(Session dbSession) {

        StringBuffer finalBuf = new StringBuffer();
        finalBuf.append("select count(*) ");
        finalBuf.append(fromBuf);
        finalBuf.append(whereBuf);
        
        return getCoreQuery(dbSession, finalBuf.toString());
    }
    
    protected Query getCoreQuery(Session dbSession, String queryText) {
        
    	log.debug("queryText=" + queryText);
    	
        Query query = dbSession.createQuery(queryText);

        for (int bindNum = 0; bindNum < binds.size(); bindNum++) {
            
            query.setParameter(getBindName(bindNum), binds.get(bindNum));
        }
        
        return query;
    }

    /** Helper to add WHERE or AND to the statement depending on whether
     * or not the following term is the first.
     */
    public String andWhere() {
        
        if (useWhere) {
            useWhere = false;
            return " where ";
        }
        else {
            return " and ";
        }
    }
    
    /** Constructs an IN list from a String[], and escapes each String value in the list.  Example output: ('a','b','c'). */
    public String makeList(Set<String> strings) {
    	StringBuilder builder = new StringBuilder();
    	builder.append("(");
    	boolean first = true;
    	for (String s : strings) {
    		if (!first) {
    			builder.append(",");
    		}
    		else {
    			first = false;
    		}
    		builder.append("'");
    		builder.append(escape(s));
    		builder.append("'");
    	}
    	builder.append(")");
    	
    	return builder.toString();
    }
    

    public String escape(String s) {

        // Double up single quotes
        return s.replaceAll("'", "''");
    }

    /** Add a bind to the array and return its parameter name */
    protected String addBind(Object value) {
        
        String name = getBindName(binds.size());
        binds.add(value);
        return name;
    }
    
    /** Gets the parameter name for the indexed slot in the bind list */
    protected String getBindName(int n) {
        
        return "p" + n;
    }
}

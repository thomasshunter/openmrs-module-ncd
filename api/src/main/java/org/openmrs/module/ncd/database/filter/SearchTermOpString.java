package org.openmrs.module.ncd.database.filter;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.springframework.validation.BindException;

public class SearchTermOpString extends SearchTerm {

	public static final String OP_CONTAINS = "contains";
    public static final String OP_IS_MISSING = "is missing";
    public static final String OP_IS_PRESENT = "is present";
    
    /** The selected operator */
    protected String op = "";
    /** The entered value */
    protected String value = "";
    
    public SearchTermOpString() {
        // should only be used by spring post handling
    }
    
    public SearchTermOpString(String editNameKey) {
        
        super(editNameKey);
    }
    
    public String getType() {
        
        return "OpString";
    }

    public boolean isSet() {

        return isSet(op) || isSet(value);
    }

    public boolean validate(String path, BindException exceptions) {

        boolean isValid = true;
        
        // If the term has not been set, it's always valid
        if (!isSet()) {
            return true;
        }
        
        // An operator must be selected
        if (!isSet(op)) {
            
            exceptions.rejectValue(path, "ncd.searchterm.opstring.error.noop");
            isValid = false;
            log.debug("validation error: value no operator");
        }

        // If the operator isn't monadic, the string must have at least one character
        if (!operatorIsMonadic() && !isSet(value)) {
            
            exceptions.rejectValue(path, "ncd.searchterm.opstring.error.novalue");
            isValid = false;
            log.debug("validation error: operator no value");
        }
        
        return isValid;
    }
    
    /** Adds clauses to a query under construction to evaluate this term */
    public void buildQueryClause(String propertyName, HQLQueryBuilder builder) {

        if (isSet(op) && (operatorIsMonadic() || isSet(value))) {
            builder.add(propertyName, op, value);
        }
    }

    public void clear() {
        
        this.op = "";
        this.value = "";
    }

    private boolean operatorIsMonadic() {
    
    	if (!isSet(op)) {
    		return false;
    	}
    	
    	return op.equals(OP_IS_MISSING) ||
    		   op.equals(OP_IS_PRESENT);
    }
    
    public static List<String> getOperators() {
       
        List<String> data = new ArrayList<String>();
        
        data.add(" ");
        
        data.add("=");
        data.add("<>");
        data.add("<");
        data.add("<=");
        data.add(">");
        data.add(">=");
        data.add(OP_CONTAINS);

        data.add(OP_IS_MISSING);
        data.add(OP_IS_PRESENT);

        return data;
    }

    /**
     * @return the op
     */
    public String getOp() {
        return op;
    }

    /**
     * @param op the op to set
     */
    public void setOp(String op) {
        this.op = op;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}

package org.openmrs.module.ncd.database.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.dao.hibernate.HQLQueryBuilder;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.springframework.validation.BindException;

public abstract class SearchTerm {

    /** Debugging log */
    protected static Log log = LogFactory.getLog(SearchTerm.class);

    protected String editNameKey;
    // All search terms are visible by default */
    protected boolean visible = true;
    
    public SearchTerm() {

        // Should only be called by the spring post handler
    }
    
    public SearchTerm(String editNameKey) {
        this.editNameKey = editNameKey;
    }
    
    public String getEditNameKey() {
        
        return editNameKey;
    }

    public void setEditNameKey(String editNameKey) {
        this.editNameKey = editNameKey;
    }

    protected boolean isSet(String s) {

        return !StringUtilities.isNullEmptyOrWhitespace(s);
    }
    
    /** Returns the type indicator for this type of SearchTerm, used by
     * the renderer to generate the correct JSP for the term.
     *
     * TODO: I'd love to instead have a render() method that builds the
     * JSP or HTML for the term, but I don't think it's practical.
     * 
     * @return The type indicator for this type of SearchTerm.
     */
    abstract public String getType();
    
    /** Test if the search term is "set", that is, that the user has
     * entered conditions to be matched for the search term.
     */
    abstract public boolean isSet();

    /** Validates the values entered for this search term.
     * 
     * @param path  The spring path for the whole term, for error linkage.
     * @param exceptions    The spring binding exception collection for
     *                      error linkage.
     * @return True iff the term values are not set or correct.
     */
    abstract public boolean validate(String path, BindException exceptions);
    
    /** Adds clauses to a query under construction to evaluate this term */
    abstract public void buildQueryClause(String propertyName, HQLQueryBuilder builder);
    
    /** Resets the term to a "clear" or unset state */
    abstract public void clear();

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}

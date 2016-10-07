package org.openmrs.module.ncd.database.filter;

import java.io.Serializable;

import org.springframework.validation.BindException;

/** A common search filter class
 * @author Erik Horstkotte
 */
public class SearchFilterBase implements Serializable {

    private static final long serialVersionUID = -2794533685580229295L;
    
    public static final int DEFAULT_ROWSPERPAGE = 100;
    public static final int DEFAULT_MAXROWS = 1000;
    
    private String sortFieldName;
    private boolean sortAscending;
    private int rowsPerPage = DEFAULT_ROWSPERPAGE;
    private int maxRows = DEFAULT_MAXROWS;
    private boolean showAll;

    protected SearchFilterBase() {
        
        sortFieldName = null;
        sortAscending = true;
        showAll = false;
    }

    public void clear() {
        
        for (SearchTerm term : getTerms()) {
            
            term.clear();
        }
    }

    public String getSortFieldName() {
        return sortFieldName;
    }

    public void setSortFieldName(String sortFieldName) {
        this.sortFieldName = sortFieldName;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }
    
    public SearchTerm[] getTerms() {
        
        return new SearchTerm[0];
    }

    public boolean validate(BindException exceptions) {
        
        boolean isValid = true;
        
        SearchTerm[] terms = getTerms();
        for (int termNum = 0; termNum < terms.length; termNum++) {
            
            SearchTerm term = terms[termNum];
            String path = "filter.terms[" + termNum + "]";
            if (!term.validate(path, exceptions)) {
                isValid = false;
            }
        }

        if (getRowsPerPage() <= 0) {
            
            exceptions.rejectValue("filter.rowsPerPage", "ncd.filter.rowsperpage.error");
            isValid = false;
        }
        
        if (getMaxRows() <= 0) {
            
            exceptions.rejectValue("filter.maxRows", "ncd.filter.maxrows.error");
            isValid = false;
        }

        return isValid;
    }

	public boolean isShowAll() {
		return showAll;
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}
	
	public void setAllVisibles(boolean visible) {

        for (SearchTerm term : getTerms()) {
            
            term.setVisible(visible);
        }
	}
}

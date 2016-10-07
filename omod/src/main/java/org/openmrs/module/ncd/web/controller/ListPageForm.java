package org.openmrs.module.ncd.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.openmrs.module.ncd.database.filter.ListPageRow;
import org.openmrs.module.ncd.database.filter.SearchFilterBase;
import org.openmrs.module.ncd.database.filter.SearchResult;

/**
 * The template base class for form backing objects for list pages.
 *
 * @param <F> The filter class. Must extend SearchFilterBase.
 * @param <R> The row class. Must implement ListPageRow.
 */
public class ListPageForm<F extends SearchFilterBase, R extends ListPageRow> {

    // TODO: rename editFilter to filterInvalid
    
    private boolean editFilter = false;
    private F filter;
    private SearchResult<R> lastSearchResult;
    private Collection<Long> selectedResults;
    private int firstRowShown;
    
    public ListPageForm(F filter) {
        
        this.filter = filter;
        this.selectedResults = new HashSet<Long>();
        this.firstRowShown = 0;
        this.lastSearchResult = new SearchResult<R>();
        this.lastSearchResult.setResultRows(new ArrayList<R>());
    }

    public void selectAll() {
        
        this.selectedResults = new HashSet<Long>();
        
        for (R thisResult : getVisibleRows()) {
            this.selectedResults.add(thisResult.getId());
        }
    }

    public void selectNone() {
        this.selectedResults = new HashSet<Long>();
    }

    /**
     * Get the zero-based index of the first row displayed on the current
     * page, for display.
     * 
     * @return the firstRowShown
     */
    public int getFirstRowShown() {
        return firstRowShown;
    }

    /**
     * Get the zero-based index of the last row displayed on the current
     * page, for display.
     * 
     * @return The zero-based index of the last row displayed on the
     * current page.
     */
    public int getLastRowShown() {
        
        int lastRowShown = getFirstRowShown() + filter.getRowsPerPage() - 1;
        int fetchedRows = lastSearchResult.getResultRows().size();
        if (lastRowShown > (fetchedRows - 1)) {
            lastRowShown = fetchedRows - 1;
        }
        return lastRowShown;
    }

    /**
     * Change the form to display the page starting with the specified
     * row index.
     */
    public void setFirstRowShown(int firstRowShown) {
        
        int fetchedRows = lastSearchResult.getResultRows().size();

        if (firstRowShown > (fetchedRows - 1)) {
            firstRowShown = fetchedRows - 1;
        }
        
        if (firstRowShown < 0) {
            firstRowShown = 0;
        }
        
        this.firstRowShown = firstRowShown;
    }

    /**
     * Change the form to display the first page.
     */
    public void firstPage() {
        
        setFirstRowShown(0);
    }

    /**
     * Back the form up to display the previous page.
     */
    public void previousPage() {

        if (isPreviousPageVisible()) {
            setFirstRowShown(getFirstRowShown() - filter.getRowsPerPage());
        }
    }

    /**
     * Advance the form to display the next page.
     */
    public void nextPage() {
        
        if (isNextPageVisible()) {
            setFirstRowShown(getFirstRowShown() + filter.getRowsPerPage());
        }
    }

    /**
     * Set the form up to display the last page, which is usually partial.
     */
    public void lastPage() {

        if (isNextPageVisible()) {
            setFirstRowShown(lastSearchResult.getResultRows().size() - filter.getRowsPerPage());
        }
    }

    /**
     * Test if a previous page button should be visible, i.e., if it is
     * possible to retreat to the previous page.
     * 
     * @return true iff it is possible to retreat to the previous page.
     */
    public boolean isPreviousPageVisible() {
        if (getFirstRowShown() > 0) {
            return true;
        }
        else {
            return false;
        }
    }
   
    /**
     * Test if a next page button should be visible, i.e., if it is
     * possible to advance to the next page.
     * 
     * @return true iff it is possible to advance to the next page.
     */
    public boolean isNextPageVisible() {
        
        int fetchedRows = lastSearchResult.getResultRows().size();
        if ((getFirstRowShown() + filter.getRowsPerPage()) >= fetchedRows) {
            return false;
        }
        else {
            return true;
        }
    }
    
    protected boolean isSearchResultEmpty() {

    	return lastSearchResult == null ||
    	       lastSearchResult.getResultRows() == null ||
    	       lastSearchResult.getResultRows().size() <= 0;
    }
    
    public boolean isFirstVisible(String currentId) {

    	long curId = Long.parseLong(currentId);
    	
    	if (isSearchResultEmpty()) {
    		return false;
    	}
    	else if (lastSearchResult.getResultRows().get(0).getId() != curId) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    public String first(String currentId) {
    
    	return lastSearchResult.getResultRows().get(0).getId().toString();
    }
    
    public boolean isPreviousVisible(String currentId) {

    	return isFirstVisible(currentId);
    }

    public int indexOf(String currentId) {
    	
    	if (!isSearchResultEmpty()) {
	    	long curId = Long.parseLong(currentId);
	    	
	    	for (int index = 0; index < lastSearchResult.getResultRows().size(); index++) {
	   		
	    		Long id = lastSearchResult.getResultRows().get(index).getId();
	    		if (curId == id) {
	    			return index;
	    		}
	    	}
    	}
    	
    	return 0;
    }
    
    public int getMaxIndex() {

    	if (lastSearchResult != null && lastSearchResult.getResultRows().size() > 0) {
    		return lastSearchResult.getResultRows().size() - 1;
    	}
    	else {
    		return 0;
    	}
    }
    
    public String previous(String currentId) {
    	
    	int curIndex = indexOf(currentId);
    	if (curIndex > 0) {
    		return lastSearchResult.getResultRows().get(curIndex - 1).getId().toString();
    	}
    	else {
    		return currentId;
    	}
    }

    public boolean isNextVisible(String currentId) {
    	
    	int nextIndex = indexOf(currentId) + 1;
    	if (nextIndex > getMaxIndex()) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }

    public String next(String currentId) {
    	
    	int nextIndex = indexOf(currentId) + 1;
    	if (nextIndex > getMaxIndex()) {
    		nextIndex = getMaxIndex();
    	}
    	if (lastSearchResult != null) {
    		return lastSearchResult.getResultRows().get(nextIndex).getId().toString();
    	}
    	else {
    		return currentId;
    	}
    }

    public boolean isLastVisible(String currentId) {

    	int curIndex = indexOf(currentId);
    	int lastIndex = getMaxIndex();
    	if (curIndex < lastIndex) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    public String last(String currentId) {

    	if (lastSearchResult == null) {
    		return currentId;
    	}
    	else {
    		return lastSearchResult.getResultRows().get(getMaxIndex()).getId().toString();
    	}
    }

    public boolean isSelected(R result) {
        
        return this.selectedResults != null && this.selectedResults.contains(result.getId());
    }

    public boolean isEditFilter() {
        return this.editFilter;
    }
    
    public void setEditFilter(boolean editFilter) {
        this.editFilter = editFilter;
    }
    
    public F getFilter() {
        return this.filter;
    }
    
    public void setFilter(F filter) {
        this.filter = filter;
    }

    /**
     * @return the lastSearchResult
     */
    public SearchResult<R> getLastSearchResult() {
        return lastSearchResult;
    }

    /**
     * @param lastSearchResult the lastSearchResult to set
     */
    public void setLastSearchResult(SearchResult<R> lastSearchResult) {
        this.lastSearchResult = lastSearchResult;
        firstPage();
    }
    
    public Collection<R> getVisibleRows() {
        
        if (getFirstRowShown() <= getLastRowShown()) {
            return this.lastSearchResult.getResultRows().subList(getFirstRowShown(), getLastRowShown() + 1);
        }
        else {
            return new ArrayList<R>();
        }
    }

    /**
     * @return the selectedResults
     */
    public Collection<Long> getSelectedResults() {
        return selectedResults;
    }

    /**
     * @param selectedResults the selectedResults to set
     */
    public void setSelectedResults(Collection<Long> selectedResults) {
        this.selectedResults = selectedResults;
    }
}

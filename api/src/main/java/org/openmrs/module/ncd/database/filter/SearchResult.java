package org.openmrs.module.ncd.database.filter;

import java.util.List;

/** A generic class for results from a "find by filter" method.
 * 
 */
public class SearchResult<T> {

    /** True iff the search operation was successful. */
    protected boolean successful;
    
    /** If not successful, a throwable describing the failure */
    protected Throwable throwable;
    
    /** True iff the search operation found more matching rows that the
     * limit supplied in the SearchFilterBase. */
    protected boolean limited;
    
    /** The total number of rows matching the search filter, regardless
     * of the limit supplied in the SearchFilterBase. */
    protected long rowCount;
    
    /** The matching rows found. If more matching rows exist than the
     * row count limit specified in the SearchFilterBase, only the rows up
     * to the limit are returned. */
    protected List<T> resultRows;

    public SearchResult() {
    }

    public String toString() {

        return "SearchResult(" +
                    "successful=" + successful +
                    ", throwable=" + throwable +
                    ", limited=" + limited +
                    ", rowCount=" + rowCount +
                    ", available=" + resultRows.size() +
               ")";
    }

    /**
     * @return the successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * @param successful the successful to set
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    /**
     * @return the limited
     */
    public boolean isLimited() {
        return limited;
    }

    /**
     * @param limited the limited to set
     */
    public void setLimited(boolean limited) {
        this.limited = limited;
    }

    /**
     * @return the rowCount
     */
    public long getRowCount() {
        return rowCount;
    }

    /**
     * @param rowCount the rowCount to set
     */
    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * @return the resultRows
     */
    public List<T> getResultRows() {
        return resultRows;
    }

    /**
     * @param resultRows the resultRows to set
     */
    public void setResultRows(List<T> resultRows) {
        this.resultRows = resultRows;
    }

    /**
     * @return the throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * @param throwable the throwable to set
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}

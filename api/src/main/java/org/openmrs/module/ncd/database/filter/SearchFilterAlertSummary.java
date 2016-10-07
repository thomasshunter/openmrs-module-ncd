package org.openmrs.module.ncd.database.filter;

import java.util.ArrayList;

import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.Pair;

public class SearchFilterAlertSummary extends SearchFilterBase {

	private static final long serialVersionUID = 6811214924890344180L;

	public enum SortKeys { MESSAGE, ALERTTYPE, OCCURRENCES, FIRSTOCCURRED, LASTOCCURRED, DISMISSEDDATE, DISMISSEDUSER, DISMISSEDREASON };
	
    private SortKeys sortKey;
	
    private SearchTermBoolean dismissed = new SearchTermBoolean("ncd.pages.alertlist.filter.dismissed");
    private SearchTermOpLookup alertType = new SearchTermOpLookup("ncd.pages.alertlist.filter.alerttype", getAlertTypes());
    private SearchTermDateTimeRange firstOccurred = new SearchTermDateTimeRange("ncd.pages.alertlist.filter.firstoccurred");
    private SearchTermDateTimeRange lastOccurred = new SearchTermDateTimeRange("ncd.pages.alertlist.filter.lastoccurred");
    
	/** Set the identity to find an alert summary for a specific identity value */
	private String identity;

    public SearchFilterAlertSummary() {
        super();
    }

    public String getSortKeyLabel() {
    	if (sortKey != null) {
    		return sortKey.toString();
    	}
    	else {
    		return "";
    	}
    }
    
    public SortKeys getSortKey() {
    	return this.sortKey;
    }
    
    public void setSortKey(SortKeys sortKey) {

    	if (sortKey != null && sortKey.equals(this.sortKey)) {
    		setSortAscending(!isSortAscending());
    	}
    	else {
    		this.sortKey = sortKey;
    		
        	switch (sortKey) {
			case MESSAGE:
		        setSortFieldName("summary");
				break;
			case ALERTTYPE:
		        setSortFieldName("alertType.alertType");
				break;
			case OCCURRENCES:
		        setSortFieldName("occurrences");
				break;
			case FIRSTOCCURRED:
		        setSortFieldName("firstDate");
				break;
			case LASTOCCURRED:
		        setSortFieldName("lastDate");
				break;
			case DISMISSEDDATE:
		        setSortFieldName("dismissedDate");
				break;
			case DISMISSEDUSER:
		        setSortFieldName("displayDismissedUserName");
				break;
			case DISMISSEDREASON:
		        setSortFieldName("dismissedReason");
				break;
			default:
				break;
        	}
        	
	        setSortAscending(true);
    	}
    }
    
    @Override
    public SearchTerm[] getTerms() {

        return new SearchTerm[] {

            dismissed,
            alertType,
            firstOccurred,
            lastOccurred
        };
    }

	public SearchTermBoolean getDismissed() {
		return dismissed;
	}

	public void setDismissed(SearchTermBoolean dismissed) {
		this.dismissed = dismissed;
	}

	public SearchTermOpLookup getAlertType() {
		return alertType;
	}

	public void setAlertType(SearchTermOpLookup alertType) {
		this.alertType = alertType;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public SearchTermDateTimeRange getFirstOccurred() {
		return firstOccurred;
	}

	public void setFirstOccurred(SearchTermDateTimeRange firstOccurred) {
		this.firstOccurred = firstOccurred;
	}

	public SearchTermDateTimeRange getLastOccurred() {
		return lastOccurred;
	}

	public void setLastOccurred(SearchTermDateTimeRange lastOccurred) {
		this.lastOccurred = lastOccurred;
	}

	/**
     * Get the alert types for the drop down list in the filter
     * 
     * @return A list of pairs containing the primary key and alert type for each alert type, 
     * and a special entry with key value=0 with a blank representing no selection.
     */
	private ArrayList<Pair<Integer, String>> getAlertTypes() {
    	ArrayList<Pair<Integer, String>> alertTypes = new ArrayList<Pair<Integer, String>>();
    	alertTypes.add(new Pair<Integer, String>(0, ""));
    	alertTypes.addAll(NCDUtilities.getService().getAlertTypes());
    	return alertTypes;
    }
}

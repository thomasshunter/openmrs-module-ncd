package org.openmrs.module.ncd.database.filter;

public class SearchFilterError extends SearchFilterBase {

	public enum SortKeys { MPQSEQNUMBER, LASTERRORDATE, LEVEL, DESCRIPTION };
	
	private static final long serialVersionUID = 6460629811732880313L;
	
    private SortKeys sortKey;
	
	private SearchTermDateTimeRange lastErrorDate = new SearchTermDateTimeRange("ncd.pages.errorlist.filter.lasterrordate");
	private SearchTermOpString description = new SearchTermOpString("ncd.pages.errorlist.filter.description");
	private SearchTermOpString hl7Message = new SearchTermOpString("ncd.pages.errorlist.filter.hl7message");
    private SearchTermBoolean hidden = new SearchTermBoolean("ncd.pages.errorlist.filter.hidden");
    
    public SearchFilterError() {
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
			case MPQSEQNUMBER:
		        setSortFieldName("mpqSeqNumber");
				break;
			case LASTERRORDATE:
		        setSortFieldName("lastErrorDate");
				break;
			case LEVEL:
		        setSortFieldName("level");
				break;
			case DESCRIPTION:
		        setSortFieldName("description");
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

            lastErrorDate,
            description,
            hl7Message,
            hidden
        };
    }

	public SearchTermDateTimeRange getLastErrorDate() {
		return lastErrorDate;
	}

	public void setLastErrorDate(SearchTermDateTimeRange lastErrorDate) {
		this.lastErrorDate = lastErrorDate;
	}

	public SearchTermOpString getDescription() {
		return description;
	}

	public void setDescription(SearchTermOpString description) {
		this.description = description;
	}

	public SearchTermOpString getHl7Message() {
		return hl7Message;
	}

	public void setHl7Message(SearchTermOpString hl7Message) {
		this.hl7Message = hl7Message;
	}

	public SearchTermBoolean getHidden() {
		return hidden;
	}

	public void setHidden(SearchTermBoolean hidden) {
		this.hidden = hidden;
	}
}

package org.openmrs.module.ncd.database.filter;

/** A search filter parameter class for the NCD service method
 * findHL7Producers
 * 
 * @author Erik Horstkotte
 */
public class SearchFilterProducers extends SearchFilterBase {

	public enum SortKeys { APPLICATION, DESCRIPTION, FACILITY, INSTITUTION, LOCATION };
	
	private static final long serialVersionUID = 7039177124439299748L;

    private SortKeys sortKey;
	
    public SearchFilterProducers() {
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
    	else
    	{
    		this.sortKey = sortKey;
    		
        	switch (sortKey) {
        	
			case APPLICATION:
		        setSortFieldName("applicationname");
				break;
				
			case DESCRIPTION:
		        setSortFieldName("description");
				break;
				
			case FACILITY:
		        setSortFieldName("facilityname");
				break;
				
			case INSTITUTION:
		        setSortFieldName("institution.name");
				break;
				
			case LOCATION:
		        setSortFieldName("locationname");
				break;
				
			default:
				break;
        	}
        	
	        setSortAscending(true);
    	}
    }
    
    public SearchTerm[] getTerms() {
        
        return new SearchTerm[0];
    }
}

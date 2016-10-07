package org.openmrs.module.ncd.database.filter;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.ncd.database.CodeSystem;
import org.openmrs.module.ncd.database.CodeType;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.Pair;


/** A search filter parameter class for the NCD service method
 * findDecidedResults
 * 
 * @author Erik Horstkotte
 */
public class SearchFilterCodes extends SearchFilterBase {

	public enum SortKeys { CODE, CODESYSTEM, CODETYPE, DISPLAYTEXT };
	
    private static final long serialVersionUID = 6194809082557598811L;

    private SortKeys sortKey;
	
    private SearchTermStringRange code = new SearchTermStringRange("ncd.pages.codeList.filter.edit.code");
    private SearchTermOpString displayText = new SearchTermOpString("ncd.pages.codeList.filter.edit.displayText");
    private SearchTermOpLookup codeType = new SearchTermOpLookup("ncd.pages.codeList.filter.edit.codeType", getCodeTypes());
    private SearchTermOpLookup codeSystem = new SearchTermOpLookup("ncd.pages.codeList.filter.edit.codeSystem", getCodeSystems());

    public SearchFilterCodes() {
        super();
        codeType.setSelectedIdInteger(false);
        codeSystem.setSelectedIdInteger(false);
        codeSystem.setNoneSelectedId("-1");
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
			case CODE:
		        setSortFieldName("code");
				break;
			case CODESYSTEM:
		        setSortFieldName("codeSystem.name");
				break;
			case CODETYPE:
		        setSortFieldName("codeType.name");
				break;
			case DISPLAYTEXT:
		        setSortFieldName("displayText");
				break;
			default:
				break;
        	}
        	
	        setSortAscending(true);
    	}
    }
    
    public SearchTerm[] getTerms() {
        
        return new SearchTerm[] {
                code,
                displayText,
                codeType,
                codeSystem
        };
    }
    
    private Pair<Integer, String> makePair(Long id, String text) {
        
        return new Pair<Integer, String>(id.intValue(), text);
    }
    
    public ArrayList<Pair<Integer, String>> getCodeTypes() {
        
        ArrayList<Pair<Integer, String>> results = new ArrayList<Pair<Integer, String>>();
        results.add(makePair(0L, ""));
        List<CodeType> codeTypes = NCDUtilities.getService().getAllCodeTypes();
        for (CodeType codeType : codeTypes) {
            results.add(makePair(codeType.getId(), codeType.getName()));
        }
        return results;
    }
    
    public ArrayList<Pair<Integer, String>> getCodeSystems() {
        
        ArrayList<Pair<Integer, String>> results = new ArrayList<Pair<Integer, String>>();
        results.add(makePair(-1L, ""));
        List<CodeSystem> codeSystems = NCDUtilities.getService().getAllCodeSystems();
        for (CodeSystem codeSystem : codeSystems) {
            results.add(makePair(codeSystem.getId(), codeSystem.getName()));
        }
        return results;
    }

    /**
     * @return the code
     */
    public SearchTermStringRange getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(SearchTermStringRange code) {
        this.code = code;
    }

    /**
     * @return the displayText
     */
    public SearchTermOpString getDisplayText() {
        return displayText;
    }

    /**
     * @param displayText the displayText to set
     */
    public void setDisplayText(SearchTermOpString displayText) {
        this.displayText = displayText;
    }

    /**
     * @return the codeType
     */
    public SearchTermOpLookup getCodeType() {
        return codeType;
    }

    /**
     * @param codeType the codeType to set
     */
    public void setCodeType(SearchTermOpLookup codeType) {
        this.codeType = codeType;
    }

    /**
     * @return the codeSystem
     */
    public SearchTermOpLookup getCodeSystem() {
        return codeSystem;
    }

    /**
     * @param codeSystem the codeSystem to set
     */
    public void setCodeSystem(SearchTermOpLookup codeSystem) {
        this.codeSystem = codeSystem;
    }
}

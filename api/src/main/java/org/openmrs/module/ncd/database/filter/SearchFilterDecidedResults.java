package org.openmrs.module.ncd.database.filter;

/** A search filter parameter class for the NCD service method
 * findDecidedResults
 * 
 * @author Erik Horstkotte
 */
public class SearchFilterDecidedResults extends SearchFilterBase {

	public enum SortKeys { DATEADDED, DATECLASSIFIED, CLASSIFIEDBY, REPORT, LOINCCODE, CODE, CODESYSTEM, CODETEXT, RESULTVALUE };
	
    private static final long serialVersionUID = -3166104088047092626L;
    
    private SortKeys sortKey;
	
    private SearchTermOpString resultcode = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.resultcode");
    private SearchTermOpString resultvalue = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.resultvalue");
    private SearchTermOpInt resultcount = new SearchTermOpInt("ncd.pages.decidedresultlist.filter.edit.resultcount");
    private SearchTermDateTimeRange dateadded = new SearchTermDateTimeRange("ncd.pages.decidedresultlist.filter.edit.dateadded");
    private SearchTermDateTimeRange dateclassified = new SearchTermDateTimeRange("ncd.pages.decidedresultlist.filter.edit.dateclassified");
    private SearchTermOpString classifiedbywhom = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.classifiedbywhom");
    private SearchTermOpCondition conditionname = new SearchTermOpCondition("ncd.pages.decidedresultlist.filter.edit.conditionname");
    private SearchTermDateTimeRange lastmodified = new SearchTermDateTimeRange("ncd.pages.decidedresultlist.filter.edit.lastmodified");
    private SearchTermOpString mpqsequencenumber = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.mpqsequencenumber");
    private SearchTermOpString obr = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.obr");
    private SearchTermOpString obrCodeSystem = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.obrCodeSystem");
    private SearchTermOpString obrtext = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.obrtext");
    private SearchTermOpString obx = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.obx");
    private SearchTermOpString obxCodeSystem = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.obxCodeSystem");
    private SearchTermOpString obxtext = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.obxtext");
    private SearchTermOpString nte = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.nte");
    private SearchTermOpString loinccode = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.loinccode");
    private SearchTermOpString disposition = new SearchTermOpString("ncd.pages.decidedresultlist.filter.edit.disposition");
    private SearchTermReportable reportable = new SearchTermReportable("ncd.pages.decidedresultlist.filter.edit.reportable");
    private SearchTermList critic = new SearchTermList("ncd.pages.decidedresultlist.filter.edit.critic", "allCritics");
    private SearchTermBoolean voided = new SearchTermBoolean("ncd.pages.decidedresultlist.filter.edit.voided");
    private SearchTermDateTimeRange dateVoided = new SearchTermDateTimeRange("ncd.pages.decidedresultlist.filter.edit.datevoided");

    public SearchFilterDecidedResults() {
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
    		
    		// special case for descending sort on compound sort keys
			if (sortKey.equals(SortKeys.CODE)) {
	    		if (isSortAscending()) {
	    			setSortFieldName("obr asc, obx");
	    		} else {
	    			setSortFieldName("obr desc, obx");
	    		}
			} else if (sortKey.equals(SortKeys.CODESYSTEM)) {
	    		if (isSortAscending()) {
	    			setSortFieldName("obrCodeSystem asc, obxCodeSystem");
	    		} else {
	    			setSortFieldName("obrCodeSystem desc, obxCodeSystem");
	    		}
			} else if (sortKey.equals(SortKeys.CODETEXT)) {
	    		if (isSortAscending()) {
	    			setSortFieldName("obrText asc, obxText");
	    		} else {
	    			setSortFieldName("obrText desc, obxText");
	    		}
			}
    	}
    	else {
    		this.sortKey = sortKey;
    		
        	switch (sortKey) {
			case DATEADDED:
		        setSortFieldName("dateAdded");
				break;
			case DATECLASSIFIED:
		        setSortFieldName("dateClassified");
				break;
			case CLASSIFIEDBY:
		        setSortFieldName("classifiedByWhom");
				break;
			case REPORT:
		        setSortFieldName("reportable");
				break;
			case LOINCCODE:
		        setSortFieldName("loincCode");
				break;
			case CODE:
		        setSortFieldName("obr, obx");
				break;
			case CODESYSTEM:
		        setSortFieldName("obrCodeSystem, obxCodeSystem");
				break;
			case CODETEXT:
		        setSortFieldName("obrText, obxText");
				break;
			case RESULTVALUE:
		        setSortFieldName("resultValue");
				break;
			default:
				break;
        	}
        	
	        setSortAscending(true);
    	}
    }
    
    public SearchTerm[] getTerms() {
        
        return new SearchTerm[] {
                reportable,
                resultcode,
                resultvalue,
                resultcount,
                dateadded,
                dateclassified,
                classifiedbywhom,
                conditionname,
                lastmodified,
                mpqsequencenumber,
                obr,
                obrCodeSystem,
                obrtext,
                obx,
                obxCodeSystem,
                obxtext,
                nte,
                loinccode,
                disposition,
                critic,
                voided,
                dateVoided
        };
    }

    /**
     * @return the resultcode
     */
    public SearchTermOpString getResultcode() {
        return resultcode;
    }

    /**
     * @param resultcode the resultcode to set
     */
    public void setResultcode(SearchTermOpString resultcode) {
        this.resultcode = resultcode;
    }

    /**
     * @return the resultvalue
     */
    public SearchTermOpString getResultvalue() {
        return resultvalue;
    }

    /**
     * @param resultvalue the resultvalue to set
     */
    public void setResultvalue(SearchTermOpString resultvalue) {
        this.resultvalue = resultvalue;
    }

    /**
     * @return the resultcount
     */
    public SearchTermOpInt getResultcount() {
        return resultcount;
    }

    /**
     * @param resultcount the resultcount to set
     */
    public void setResultcount(SearchTermOpInt resultcount) {
        this.resultcount = resultcount;
    }

    /**
     * @return the dateadded
     */
    public SearchTermDateTimeRange getDateadded() {
        return dateadded;
    }

    /**
     * @param dateadded the dateadded to set
     */
    public void setDateadded(SearchTermDateTimeRange dateadded) {
        this.dateadded = dateadded;
    }

    /**
     * @return the dateclassified
     */
    public SearchTermDateTimeRange getDateclassified() {
        return dateclassified;
    }

    /**
     * @param dateclassified the dateclassified to set
     */
    public void setDateclassified(SearchTermDateTimeRange dateclassified) {
        this.dateclassified = dateclassified;
    }

    /**
     * @return the classifiedbywhom
     */
    public SearchTermOpString getClassifiedbywhom() {
        return classifiedbywhom;
    }

    /**
     * @param classifiedbywhom the classifiedbywhom to set
     */
    public void setClassifiedbywhom(SearchTermOpString classifiedbywhom) {
        this.classifiedbywhom = classifiedbywhom;
    }

    /**
     * @return the conditionname
     */
    public SearchTermOpCondition getConditionname() {
        return conditionname;
    }

    /**
     * @param conditionname the conditionname to set
     */
    public void setConditionname(SearchTermOpCondition conditionname) {
        this.conditionname = conditionname;
    }

    /**
     * @return the lastmodified
     */
    public SearchTermDateTimeRange getLastmodified() {
        return lastmodified;
    }

    /**
     * @param lastmodified the lastmodified to set
     */
    public void setLastmodified(SearchTermDateTimeRange lastmodified) {
        this.lastmodified = lastmodified;
    }

    /**
     * @return the mpqsequencenumber
     */
    public SearchTermOpString getMpqsequencenumber() {
        return mpqsequencenumber;
    }

    /**
     * @param mpqsequencenumber the mpqsequencenumber to set
     */
    public void setMpqsequencenumber(SearchTermOpString mpqsequencenumber) {
        this.mpqsequencenumber = mpqsequencenumber;
    }

    /**
     * @return the obr
     */
    public SearchTermOpString getObr() {
        return obr;
    }

    /**
     * @param obr the obr to set
     */
    public void setObr(SearchTermOpString obr) {
        this.obr = obr;
    }

    /**
     * @return the obrCodeSystem
     */
    public SearchTermOpString getObrCodeSystem() {
        return obrCodeSystem;
    }

    /**
     * @param obrCodeSystem the obrCodeSystem to set
     */
    public void setObrCodeSystem(SearchTermOpString obrCodeSystem) {
        this.obrCodeSystem = obrCodeSystem;
    }

    /**
     * @return the obrtext
     */
    public SearchTermOpString getObrtext() {
        return obrtext;
    }

    /**
     * @param obrtext the obrtext to set
     */
    public void setObrtext(SearchTermOpString obrtext) {
        this.obrtext = obrtext;
    }

    /**
     * @return the obx
     */
    public SearchTermOpString getObx() {
        return obx;
    }

    /**
     * @param obx the obx to set
     */
    public void setObx(SearchTermOpString obx) {
        this.obx = obx;
    }

    /**
     * @return the obxCodeSystem
     */
    public SearchTermOpString getObxCodeSystem() {
        return obxCodeSystem;
    }

    /**
     * @param obxCodeSystem the obxCodeSystem to set
     */
    public void setObxCodeSystem(SearchTermOpString obxCodeSystem) {
        this.obxCodeSystem = obxCodeSystem;
    }

    /**
     * @return the obxtext
     */
    public SearchTermOpString getObxtext() {
        return obxtext;
    }

    /**
     * @param obxtext the obxtext to set
     */
    public void setObxtext(SearchTermOpString obxtext) {
        this.obxtext = obxtext;
    }

    /**
     * @return the nte
     */
    public SearchTermOpString getNte() {
        return nte;
    }

    /**
     * @param nte the nte to set
     */
    public void setNte(SearchTermOpString nte) {
        this.nte = nte;
    }

    /**
     * @return the loinccode
     */
    public SearchTermOpString getLoinccode() {
        return loinccode;
    }

    /**
     * @param loinccode the loinccode to set
     */
    public void setLoinccode(SearchTermOpString loinccode) {
        this.loinccode = loinccode;
    }

    /**
     * @return the disposition
     */
    public SearchTermOpString getDisposition() {
        return disposition;
    }

    /**
     * @param disposition the disposition to set
     */
    public void setDisposition(SearchTermOpString disposition) {
        this.disposition = disposition;
    }

    /**
     * @return the reportable
     */
    public SearchTermReportable getReportable() {
        return reportable;
    }

    /**
     * @param reportable the reportable to set
     */
    public void setReportable(SearchTermReportable reportable) {
        this.reportable = reportable;
    }

    /**
     * @return the critic
     */
    public SearchTermList getCritic() {
        return critic;
    }

    /**
     * @param critic the critic to set
     */
    public void setCritic(SearchTermList critic) {
        this.critic = critic;
    }

    public SearchTermBoolean getVoided() {
        return voided;
    }

    public void setVoided(SearchTermBoolean voided) {
        this.voided = voided;
    }

    public SearchTermDateTimeRange getDateVoided() {
        return dateVoided;
    }

    public void setDateVoided(SearchTermDateTimeRange dateVoided) {
        this.dateVoided = dateVoided;
    }
}

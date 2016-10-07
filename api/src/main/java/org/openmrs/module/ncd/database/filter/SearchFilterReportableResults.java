package org.openmrs.module.ncd.database.filter;

import java.util.ArrayList;

import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.Pair;


// TODO: real implementation

public class SearchFilterReportableResults extends SearchFilterBase {

	public enum SortKeys { DATETIMERCVD, MPQSEQNUMBER, CODE, CONDITION, CRITIC };
	
    private static final long serialVersionUID = -5303284447646161841L;

    private SortKeys sortKey;
	
    private SearchTermOpCondition conditionName = new SearchTermOpCondition("ncd.pages.reportableresultlist.filter.conditionname");
    private SearchTermOpLookup reviewStatus = new SearchTermOpLookup("ncd.pages.reportableresultlist.filter.reviewstatus", getReviewStatuses());
    private SearchTermDateRange releaseDate = new SearchTermDateRange("ncd.pages.reportableresultlist.filter.releasedate");
    private SearchTermBoolean sentInError = new SearchTermBoolean("ncd.pages.reportableresultlist.filter.sentinerror");
    
    private SearchTermOpLoinc loinc = new SearchTermOpLoinc("ncd.pages.reportableresultlist.filter.loinc");
    private SearchTermOpString institution = new SearchTermOpString("ncd.pages.reportableresultlist.filter.institution");
    private SearchTermOpString sendingApplication = new SearchTermOpString("ncd.pages.reportableresultlist.filter.application");
    private SearchTermOpString sendingFacility = new SearchTermOpString("ncd.pages.reportableresultlist.filter.facility");
    private SearchTermOpString sendingLocation = new SearchTermOpString("ncd.pages.reportableresultlist.filter.location");
    private SearchTermOpString county = new SearchTermOpString("ncd.pages.reportableresultlist.filter.county");
    private SearchTermOpString jurisdiction = new SearchTermOpString("ncd.pages.reportableresultlist.filter.jurisdiction");
    private SearchTermOpString institutionidtype = new SearchTermOpString("ncd.pages.reportableresultlist.filter.institutionidtype");
    
    private SearchTermOpString patinstmedrecid = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patinstmedrecid");
    private SearchTermOpString globalpatientid = new SearchTermOpString("ncd.pages.reportableresultlist.filter.globalpatientid");
    private SearchTermOpString patientssn = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientssn");
    private SearchTermOpString patientname = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientname");
    private SearchTermDateRange patientbirth = new SearchTermDateRange("ncd.pages.reportableresultlist.filter.patientbirth");
    private SearchTermOpString patientrace = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientrace");
    private SearchTermOpString patientphone = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientphone");
    private SearchTermOpString patientstreet1 = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientstreet1");
    private SearchTermOpString patientstreet2 = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientstreet2");
    private SearchTermOpString patientcity = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientcity");
    private SearchTermOpString patientcounty = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientcounty");
    private SearchTermOpString patientstate = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientstate");
    private SearchTermOpString patientzip = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientzip");
    private SearchTermOpString patientcountry = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientcountry");
    private SearchTermOpString patientsex = new SearchTermOpString("ncd.pages.reportableresultlist.filter.patientsex");
    
    private SearchTermOpString providername = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providername");
    private SearchTermOpString providernamematched = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providernamematched");
    private SearchTermOpString providerssn = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerssn");
    private SearchTermDateRange providerbirth = new SearchTermDateRange("ncd.pages.reportableresultlist.filter.providerbirth");
    private SearchTermOpString providerpractice = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerpractice");
    private SearchTermOpString providerstreet = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerstreet");
    private SearchTermOpString providercity = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providercity");
    private SearchTermOpString providerstate = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerstate");
    private SearchTermOpString providerzip = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerzip");
    private SearchTermOpString providercounty = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providercounty");
    private SearchTermOpString providerphone = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerphone");
    private SearchTermOpString providerlocalid = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerlocalid");
    private SearchTermOpString providerdeanumber = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerdeanumber");
    private SearchTermOpString providerlicense = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerlicense");
    private SearchTermOpString providernamesource = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providernamesource");
    private SearchTermOpString provideraddresssource = new SearchTermOpString("ncd.pages.reportableresultlist.filter.provideraddresssource");
    private SearchTermOpString providerlocalidsource = new SearchTermOpString("ncd.pages.reportableresultlist.filter.providerlocalidsource");
    
    private SearchTermOpString labname = new SearchTermOpString("ncd.pages.reportableresultlist.filter.labname");
    private SearchTermOpString labid = new SearchTermOpString("ncd.pages.reportableresultlist.filter.labid");
    private SearchTermOpString labphone = new SearchTermOpString("ncd.pages.reportableresultlist.filter.labphone");
    private SearchTermOpString labstreet1 = new SearchTermOpString("ncd.pages.reportableresultlist.filter.labstreet1");
    private SearchTermOpString labstreet2 = new SearchTermOpString("ncd.pages.reportableresultlist.filter.labstreet2");
    private SearchTermOpString labcity = new SearchTermOpString("ncd.pages.reportableresultlist.filter.labcity");
    private SearchTermOpString labstate = new SearchTermOpString("ncd.pages.reportableresultlist.filter.labstate");
    private SearchTermOpString labzip = new SearchTermOpString("ncd.pages.reportableresultlist.filter.labzip");
    
    private SearchTermOpString testid = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testid");
    private SearchTermOpString testname = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testname");
    private SearchTermOpString testcodesys = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testcodesys");
    private SearchTermOpString testplacerordernum = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testplacerordernum");
    private SearchTermOpString testfillerordernum = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testfillerordernum");
    private SearchTermDateRange testdate = new SearchTermDateRange("ncd.pages.reportableresultlist.filter.testdate");
    private SearchTermOpString testdatesource = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testdatesource");
    private SearchTermOpString testparentplacer = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testparentplacer");
    private SearchTermOpString testparentfiller = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testparentfiller");
    private SearchTermOpString testspecimentext = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testspecimentext");
    private SearchTermOpString testdatatype = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testdatatype");
    private SearchTermOpString testnormalrange = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testnormalrange");
    private SearchTermOpString testabnormalflag = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testabnormalflag");
    private SearchTermOpString testcomment = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testcomment");
    private SearchTermDateTimeRange messageReceivedDateTime = new SearchTermDateTimeRange("ncd.pages.reportableresultlist.filter.messagereceiveddatetime");
    private SearchTermOpString mpqSeqNumber = new SearchTermOpString("ncd.pages.reportableresultlist.filter.mpqseqnumber");
    private SearchTermOpString testresultid = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testresultid");
    private SearchTermOpString testresultname = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testresultname");
    private SearchTermOpString testresultcodesys = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testresultcodesys");
    private SearchTermOpString testresultsubid = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testresultsubid");
    private SearchTermOpString testresultcode = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testresultcode");
    private SearchTermOpString testresultvalue = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testresultvalue");
    private SearchTermOpString testresultunits = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testresultunits");
    private SearchTermDateRange testpreviousdate = new SearchTermDateRange("ncd.pages.reportableresultlist.filter.testpreviousdate");
    private SearchTermOpString testplacerordernumsource = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testplacerordernumsource");
    private SearchTermOpString testfillerordernumsource = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testfillerordernumsource");
    private SearchTermOpString testresultstatus = new SearchTermOpString("ncd.pages.reportableresultlist.filter.testresultstatus");
    
    private SearchTermOpString obraltcode = new SearchTermOpString("ncd.pages.reportableresultlist.filter.obraltcode");
    private SearchTermOpString obraltcodetext = new SearchTermOpString("ncd.pages.reportableresultlist.filter.obraltcodetext");
    private SearchTermOpString obraltcodesys = new SearchTermOpString("ncd.pages.reportableresultlist.filter.obraltcodesys");
    private SearchTermOpString obxaltcode = new SearchTermOpString("ncd.pages.reportableresultlist.filter.obxaltcode");
    private SearchTermOpString obxaltcodetext = new SearchTermOpString("ncd.pages.reportableresultlist.filter.obxaltcodetext");
    private SearchTermOpString obxaltcodesys = new SearchTermOpString("ncd.pages.reportableresultlist.filter.obxaltcodesys");
    private SearchTermOpInt obrSetId = new SearchTermOpInt("ncd.pages.reportableresultlist.filter.obrSetId");
    private SearchTermOpInt obxStartSetId = new SearchTermOpInt("ncd.pages.reportableresultlist.filter.obxStartSetId");
    private SearchTermOpInt obxEndSetId = new SearchTermOpInt("ncd.pages.reportableresultlist.filter.obxEndSetId");
    private SearchTermList critic = new SearchTermList("ncd.pages.decidedresultlist.filter.edit.critic", "allCritics");
    
    public SearchFilterReportableResults() {
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
			case DATETIMERCVD:
		        setSortFieldName("messageReceivedDateTime");
				break;
			case MPQSEQNUMBER:
		        setSortFieldName("mpqSeqNumber");
				break;
			case CODE:
		        setSortFieldName("codeDisplay");
				break;
			case CONDITION:
		        setSortFieldName("conditionName");
				break;
			case CRITIC:
		        setSortFieldName("criticDisplay");
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

            conditionName,
            reviewStatus,
            releaseDate,
            sentInError,
            
            loinc,
            institution,
            sendingApplication,
            sendingFacility,
            sendingLocation,
            county,
            jurisdiction,
            institutionidtype,
            
            patinstmedrecid,
            globalpatientid,
            patientssn,
            patientname,
            patientbirth,
            patientrace,
            patientphone,
            patientstreet1,
            patientstreet2,
            patientcity,
            patientcounty,
            patientstate,
            patientzip,
            patientcountry,
            patientsex,
            
            providername,
            providernamematched,
            providerssn,
            providerbirth,
            providerpractice,
            providerstreet,
            providercity,
            providerstate,
            providerzip,
            providercounty,
            providerphone,
            providerlocalid,
            providerdeanumber,
            providerlicense,
            providernamesource,
            provideraddresssource,
            providerlocalidsource,
            
            labname,
            labid,
            labphone,
            labstreet1,
            labstreet2,
            labcity,
            labstate,
            labzip,
            
            testid,
            testname,
            testcodesys,
            testplacerordernum,
            testfillerordernum,
            testdate,
            testdatesource,
            testparentplacer,
            testparentfiller,
            testspecimentext,
            testdatatype,
            testnormalrange,
            testabnormalflag,
            testcomment,
            messageReceivedDateTime,
            mpqSeqNumber,
            testresultid,
            testresultname,
            testresultcodesys,
            testresultsubid,
            testresultcode,
            testresultvalue,
            testresultunits,
            testpreviousdate,
            testplacerordernumsource,
            testfillerordernumsource,
            testresultstatus,
            
            obraltcode,
            obraltcodetext,
            obraltcodesys,
            obxaltcode,
            obxaltcodetext,
            obxaltcodesys,
            obrSetId,
            obxStartSetId,
            obxEndSetId,
            critic
        };
    }

    /**
     * @return the conditionName
     */
    public SearchTermOpCondition getConditionName() {
        return conditionName;
    }

    /**
     * @param conditionName the conditionName to set
     */
    public void setConditionname(SearchTermOpCondition conditionName) {
        this.conditionName = conditionName;
    }

    /**
     * @return the reviewStatus
     */
    public SearchTermOpLookup getReviewStatus() {
        return reviewStatus;
    }

    /**
     * @param reviewStatus the reviewStatus to set
     */
    public void setReviewStatus(SearchTermOpLookup reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    /**
     * @return the loinc
     */
    public SearchTermOpLoinc getLoinc() {
        return loinc;
    }

    /**
     * @param loinc the loinc to set
     */
    public void setLoinc(SearchTermOpLoinc loinc) {
        this.loinc = loinc;
    }

    /**
     * @return the institution
     */
    public SearchTermOpString getInstitution() {
        return institution;
    }

    /**
     * @param institution the institution to set
     */
    public void setInstitution(SearchTermOpString institution) {
        this.institution = institution;
    }

    /**
     * @return the sendingApplication
     */
    public SearchTermOpString getSendingApplication() {
        return sendingApplication;
    }

    /**
     * @param sendingApplication the sendingApplication to set
     */
    public void setSendingApplication(SearchTermOpString sendingApplication) {
        this.sendingApplication = sendingApplication;
    }

    /**
     * @return the sendingFacility
     */
    public SearchTermOpString getSendingFacility() {
        return sendingFacility;
    }

    /**
     * @param sendingFacility the sendingFacility to set
     */
    public void setSendingFacility(SearchTermOpString sendingFacility) {
        this.sendingFacility = sendingFacility;
    }

    /**
     * @return the sendingLocation
     */
    public SearchTermOpString getSendingLocation() {
        return sendingLocation;
    }

    /**
     * @param sendingLocation the sendingLocation to set
     */
    public void setSendingLocation(SearchTermOpString sendingLocation) {
        this.sendingLocation = sendingLocation;
    }

    /**
     * @return the county
     */
    public SearchTermOpString getCounty() {
        return county;
    }

    /**
     * @param county the county to set
     */
    public void setCounty(SearchTermOpString county) {
        this.county = county;
    }

    /**
     * @return the jurisdiction
     */
    public SearchTermOpString getJurisdiction() {
        return jurisdiction;
    }

    /**
     * @param jurisdiction the jurisdiction to set
     */
    public void setJurisdiction(SearchTermOpString jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    /**
     * @return the institutionidtype
     */
    public SearchTermOpString getInstitutionidtype() {
        return institutionidtype;
    }

    /**
     * @param institutionidtype the institutionidtype to set
     */
    public void setInstitutionidtype(SearchTermOpString institutionidtype) {
        this.institutionidtype = institutionidtype;
    }

    /**
     * @return the patinstmedrecid
     */
    public SearchTermOpString getPatinstmedrecid() {
        return patinstmedrecid;
    }

    /**
     * @param patinstmedrecid the patinstmedrecid to set
     */
    public void setPatinstmedrecid(SearchTermOpString patinstmedrecid) {
        this.patinstmedrecid = patinstmedrecid;
    }

    /**
     * @return the globalpatientid
     */
    public SearchTermOpString getGlobalpatientid() {
        return globalpatientid;
    }

    /**
     * @param globalpatientid the globalpatientid to set
     */
    public void setGlobalpatientid(SearchTermOpString globalpatientid) {
        this.globalpatientid = globalpatientid;
    }

    /**
     * @return the patientssn
     */
    public SearchTermOpString getPatientssn() {
        return patientssn;
    }

    /**
     * @param patientssn the patientssn to set
     */
    public void setPatientssn(SearchTermOpString patientssn) {
        this.patientssn = patientssn;
    }

    /**
     * @return the patientname
     */
    public SearchTermOpString getPatientname() {
        return patientname;
    }

    /**
     * @param patientname the patientname to set
     */
    public void setPatientname(SearchTermOpString patientname) {
        this.patientname = patientname;
    }

    /**
     * @return the patientbirth
     */
    public SearchTermDateRange getPatientbirth() {
        return patientbirth;
    }

    /**
     * @param patientbirth the patientbirth to set
     */
    public void setPatientbirth(SearchTermDateRange patientbirth) {
        this.patientbirth = patientbirth;
    }

    /**
     * @return the patientrace
     */
    public SearchTermOpString getPatientrace() {
        return patientrace;
    }

    /**
     * @param patientrace the patientrace to set
     */
    public void setPatientrace(SearchTermOpString patientrace) {
        this.patientrace = patientrace;
    }

    /**
     * @return the patientphone
     */
    public SearchTermOpString getPatientphone() {
        return patientphone;
    }

    /**
     * @param patientphone the patientphone to set
     */
    public void setPatientphone(SearchTermOpString patientphone) {
        this.patientphone = patientphone;
    }

    /**
     * @return the patientstreet1
     */
    public SearchTermOpString getPatientstreet1() {
        return patientstreet1;
    }

    /**
     * @param patientstreet1 the patientstreet1 to set
     */
    public void setPatientstreet1(SearchTermOpString patientstreet1) {
        this.patientstreet1 = patientstreet1;
    }

    /**
     * @return the patientstreet2
     */
    public SearchTermOpString getPatientstreet2() {
        return patientstreet2;
    }

    /**
     * @param patientstreet2 the patientstreet2 to set
     */
    public void setPatientstreet2(SearchTermOpString patientstreet2) {
        this.patientstreet2 = patientstreet2;
    }

    /**
     * @return the patientcity
     */
    public SearchTermOpString getPatientcity() {
        return patientcity;
    }

    /**
     * @param patientcity the patientcity to set
     */
    public void setPatientcity(SearchTermOpString patientcity) {
        this.patientcity = patientcity;
    }

    /**
     * @return the patientcounty
     */
    public SearchTermOpString getPatientcounty() {
        return patientcounty;
    }

    /**
     * @param patientcounty the patientcounty to set
     */
    public void setPatientcounty(SearchTermOpString patientcounty) {
        this.patientcounty = patientcounty;
    }

    /**
     * @return the patientstate
     */
    public SearchTermOpString getPatientstate() {
        return patientstate;
    }

    /**
     * @param patientstate the patientstate to set
     */
    public void setPatientstate(SearchTermOpString patientstate) {
        this.patientstate = patientstate;
    }

    /**
     * @return the patientzip
     */
    public SearchTermOpString getPatientzip() {
        return patientzip;
    }

    /**
     * @param patientzip the patientzip to set
     */
    public void setPatientzip(SearchTermOpString patientzip) {
        this.patientzip = patientzip;
    }

    /**
     * @return the patientcountry
     */
    public SearchTermOpString getPatientcountry() {
        return patientcountry;
    }

    /**
     * @param patientcountry the patientcountry to set
     */
    public void setPatientcountry(SearchTermOpString patientcountry) {
        this.patientcountry = patientcountry;
    }

    /**
     * @return the patientsex
     */
    public SearchTermOpString getPatientsex() {
        return patientsex;
    }

    /**
     * @param patientsex the patientsex to set
     */
    public void setPatientsex(SearchTermOpString patientsex) {
        this.patientsex = patientsex;
    }

    /**
     * @return the providername
     */
    public SearchTermOpString getProvidername() {
        return providername;
    }

    /**
     * @param providername the providername to set
     */
    public void setProvidername(SearchTermOpString providername) {
        this.providername = providername;
    }

    /**
     * @return the providernamematched
     */
    public SearchTermOpString getProvidernamematched() {
        return providernamematched;
    }

    /**
     * @param providernamematched the providernamematched to set
     */
    public void setProvidernamematched(SearchTermOpString providernamematched) {
        this.providernamematched = providernamematched;
    }

    /**
     * @return the providerssn
     */
    public SearchTermOpString getProviderssn() {
        return providerssn;
    }

    /**
     * @param providerssn the providerssn to set
     */
    public void setProviderssn(SearchTermOpString providerssn) {
        this.providerssn = providerssn;
    }

    /**
     * @return the providerbirth
     */
    public SearchTermDateRange getProviderbirth() {
        return providerbirth;
    }

    /**
     * @param providerbirth the providerbirth to set
     */
    public void setProviderbirth(SearchTermDateRange providerbirth) {
        this.providerbirth = providerbirth;
    }

    /**
     * @return the providerpractice
     */
    public SearchTermOpString getProviderpractice() {
        return providerpractice;
    }

    /**
     * @param providerpractice the providerpractice to set
     */
    public void setProviderpractice(SearchTermOpString providerpractice) {
        this.providerpractice = providerpractice;
    }

    /**
     * @return the providerstreet
     */
    public SearchTermOpString getProviderstreet() {
        return providerstreet;
    }

    /**
     * @param providerstreet the providerstreet to set
     */
    public void setProviderstreet(SearchTermOpString providerstreet) {
        this.providerstreet = providerstreet;
    }

    /**
     * @return the providercity
     */
    public SearchTermOpString getProvidercity() {
        return providercity;
    }

    /**
     * @param providercity the providercity to set
     */
    public void setProvidercity(SearchTermOpString providercity) {
        this.providercity = providercity;
    }

    /**
     * @return the providerstate
     */
    public SearchTermOpString getProviderstate() {
        return providerstate;
    }

    /**
     * @param providerstate the providerstate to set
     */
    public void setProviderstate(SearchTermOpString providerstate) {
        this.providerstate = providerstate;
    }

    /**
     * @return the providerzip
     */
    public SearchTermOpString getProviderzip() {
        return providerzip;
    }

    /**
     * @param providerzip the providerzip to set
     */
    public void setProviderzip(SearchTermOpString providerzip) {
        this.providerzip = providerzip;
    }

    /**
     * @return the providercounty
     */
    public SearchTermOpString getProvidercounty() {
        return providercounty;
    }

    /**
     * @param providercounty the providercounty to set
     */
    public void setProvidercounty(SearchTermOpString providercounty) {
        this.providercounty = providercounty;
    }

    /**
     * @return the providerphone
     */
    public SearchTermOpString getProviderphone() {
        return providerphone;
    }

    /**
     * @param providerphone the providerphone to set
     */
    public void setProviderphone(SearchTermOpString providerphone) {
        this.providerphone = providerphone;
    }

    /**
     * @return the providerlocalid
     */
    public SearchTermOpString getProviderlocalid() {
        return providerlocalid;
    }

    /**
     * @param providerlocalid the providerlocalid to set
     */
    public void setProviderlocalid(SearchTermOpString providerlocalid) {
        this.providerlocalid = providerlocalid;
    }

    /**
     * @return the providerdeanumber
     */
    public SearchTermOpString getProviderdeanumber() {
        return providerdeanumber;
    }

    /**
     * @param providerdeanumber the providerdeanumber to set
     */
    public void setProviderdeanumber(SearchTermOpString providerdeanumber) {
        this.providerdeanumber = providerdeanumber;
    }

    /**
     * @return the providerlicense
     */
    public SearchTermOpString getProviderlicense() {
        return providerlicense;
    }

    /**
     * @param providerlicense the providerlicense to set
     */
    public void setProviderlicense(SearchTermOpString providerlicense) {
        this.providerlicense = providerlicense;
    }

    /**
     * @return the providernamesource
     */
    public SearchTermOpString getProvidernamesource() {
        return providernamesource;
    }

    /**
     * @param providernamesource the providernamesource to set
     */
    public void setProvidernamesource(SearchTermOpString providernamesource) {
        this.providernamesource = providernamesource;
    }

    /**
     * @return the provideraddresssource
     */
    public SearchTermOpString getProvideraddresssource() {
        return provideraddresssource;
    }

    /**
     * @param provideraddresssource the provideraddresssource to set
     */
    public void setProvideraddresssource(SearchTermOpString provideraddresssource) {
        this.provideraddresssource = provideraddresssource;
    }

    /**
     * @return the providerlocalidsource
     */
    public SearchTermOpString getProviderlocalidsource() {
        return providerlocalidsource;
    }

    /**
     * @param providerlocalidsource the providerlocalidsource to set
     */
    public void setProviderlocalidsource(SearchTermOpString providerlocalidsource) {
        this.providerlocalidsource = providerlocalidsource;
    }

    /**
     * @return the labname
     */
    public SearchTermOpString getLabname() {
        return labname;
    }

    /**
     * @param labname the labname to set
     */
    public void setLabname(SearchTermOpString labname) {
        this.labname = labname;
    }

    /**
     * @return the labid
     */
    public SearchTermOpString getLabid() {
        return labid;
    }

    /**
     * @param labid the labid to set
     */
    public void setLabid(SearchTermOpString labid) {
        this.labid = labid;
    }

    /**
     * @return the labphone
     */
    public SearchTermOpString getLabphone() {
        return labphone;
    }

    /**
     * @param labphone the labphone to set
     */
    public void setLabphone(SearchTermOpString labphone) {
        this.labphone = labphone;
    }

    /**
     * @return the labstreet1
     */
    public SearchTermOpString getLabstreet1() {
        return labstreet1;
    }

    /**
     * @param labstreet1 the labstreet1 to set
     */
    public void setLabstreet1(SearchTermOpString labstreet1) {
        this.labstreet1 = labstreet1;
    }

    /**
     * @return the labstreet2
     */
    public SearchTermOpString getLabstreet2() {
        return labstreet2;
    }

    /**
     * @param labstreet2 the labstreet2 to set
     */
    public void setLabstreet2(SearchTermOpString labstreet2) {
        this.labstreet2 = labstreet2;
    }

    /**
     * @return the labcity
     */
    public SearchTermOpString getLabcity() {
        return labcity;
    }

    /**
     * @param labcity the labcity to set
     */
    public void setLabcity(SearchTermOpString labcity) {
        this.labcity = labcity;
    }

    /**
     * @return the labstate
     */
    public SearchTermOpString getLabstate() {
        return labstate;
    }

    /**
     * @param labstate the labstate to set
     */
    public void setLabstate(SearchTermOpString labstate) {
        this.labstate = labstate;
    }

    /**
     * @return the labzip
     */
    public SearchTermOpString getLabzip() {
        return labzip;
    }

    /**
     * @param labzip the labzip to set
     */
    public void setLabzip(SearchTermOpString labzip) {
        this.labzip = labzip;
    }

    /**
     * @return the testid
     */
    public SearchTermOpString getTestid() {
        return testid;
    }

    /**
     * @param testid the testid to set
     */
    public void setTestid(SearchTermOpString testid) {
        this.testid = testid;
    }

    /**
     * @return the testname
     */
    public SearchTermOpString getTestname() {
        return testname;
    }

    /**
     * @param testname the testname to set
     */
    public void setTestname(SearchTermOpString testname) {
        this.testname = testname;
    }

    /**
     * @return the testcodesys
     */
    public SearchTermOpString getTestcodesys() {
        return testcodesys;
    }

    /**
     * @param testcodesys the testcodesys to set
     */
    public void setTestcodesys(SearchTermOpString testcodesys) {
        this.testcodesys = testcodesys;
    }

    /**
     * @return the testplacerordernum
     */
    public SearchTermOpString getTestplacerordernum() {
        return testplacerordernum;
    }

    /**
     * @param testplacerordernum the testplacerordernum to set
     */
    public void setTestplacerordernum(SearchTermOpString testplacerordernum) {
        this.testplacerordernum = testplacerordernum;
    }

    /**
     * @return the testfillerordernum
     */
    public SearchTermOpString getTestfillerordernum() {
        return testfillerordernum;
    }

    /**
     * @param testfillerordernum the testfillerordernum to set
     */
    public void setTestfillerordernum(SearchTermOpString testfillerordernum) {
        this.testfillerordernum = testfillerordernum;
    }

    /**
     * @return the testdate
     */
    public SearchTermDateRange getTestdate() {
        return testdate;
    }

    /**
     * @param testdate the testdate to set
     */
    public void setTestdate(SearchTermDateRange testdate) {
        this.testdate = testdate;
    }

    /**
     * @return the testdatesource
     */
    public SearchTermOpString getTestdatesource() {
        return testdatesource;
    }

    /**
     * @param testdatesource the testdatesource to set
     */
    public void setTestdatesource(SearchTermOpString testdatesource) {
        this.testdatesource = testdatesource;
    }

    /**
     * @return the testparentplacer
     */
    public SearchTermOpString getTestparentplacer() {
        return testparentplacer;
    }

    /**
     * @param testparentplacer the testparentplacer to set
     */
    public void setTestparentplacer(SearchTermOpString testparentplacer) {
        this.testparentplacer = testparentplacer;
    }

    /**
     * @return the testparentfiller
     */
    public SearchTermOpString getTestparentfiller() {
        return testparentfiller;
    }

    /**
     * @param testparentfiller the testparentfiller to set
     */
    public void setTestparentfiller(SearchTermOpString testparentfiller) {
        this.testparentfiller = testparentfiller;
    }

    /**
     * @return the testspecimentext
     */
    public SearchTermOpString getTestspecimentext() {
        return testspecimentext;
    }

    /**
     * @param testspecimentext the testspecimentext to set
     */
    public void setTestspecimentext(SearchTermOpString testspecimentext) {
        this.testspecimentext = testspecimentext;
    }

    /**
     * @return the testdatatype
     */
    public SearchTermOpString getTestdatatype() {
        return testdatatype;
    }

    /**
     * @param testdatatype the testdatatype to set
     */
    public void setTestdatatype(SearchTermOpString testdatatype) {
        this.testdatatype = testdatatype;
    }

    /**
     * @return the testnormalrange
     */
    public SearchTermOpString getTestnormalrange() {
        return testnormalrange;
    }

    /**
     * @param testnormalrange the testnormalrange to set
     */
    public void setTestnormalrange(SearchTermOpString testnormalrange) {
        this.testnormalrange = testnormalrange;
    }

    /**
     * @return the testabnormalflag
     */
    public SearchTermOpString getTestabnormalflag() {
        return testabnormalflag;
    }

    /**
     * @param testabnormalflag the testabnormalflag to set
     */
    public void setTestabnormalflag(SearchTermOpString testabnormalflag) {
        this.testabnormalflag = testabnormalflag;
    }

    /**
     * @return the testcomment
     */
    public SearchTermOpString getTestcomment() {
        return testcomment;
    }

    /**
     * @param testcomment the testcomment to set
     */
    public void setTestcomment(SearchTermOpString testcomment) {
        this.testcomment = testcomment;
    }

    /**
     * @return the messageReceivedDateTime
     */
    public SearchTermDateTimeRange getMessageReceivedDateTime() {
        return messageReceivedDateTime;
    }

    /**
     * @param messageReceivedDateTime the messageReceivedDateTime to set
     */
    public void setMessageReceivedDateTime(SearchTermDateTimeRange messageReceivedDateTime) {
        this.messageReceivedDateTime = messageReceivedDateTime;
    }

    /**
     * @return the mpqSeqNumber
     */
    public SearchTermOpString getMpqSeqNumber() {
        return mpqSeqNumber;
    }

    /**
     * @param mpqSeqNumber the mpqSeqNumber to set
     */
    public void setMpqSeqNumber(SearchTermOpString mpqSeqNumber) {
        this.mpqSeqNumber = mpqSeqNumber;
    }

    /**
     * @return the testresultid
     */
    public SearchTermOpString getTestresultid() {
        return testresultid;
    }

    /**
     * @param testresultid the testresultid to set
     */
    public void setTestresultid(SearchTermOpString testresultid) {
        this.testresultid = testresultid;
    }

    /**
     * @return the testresultname
     */
    public SearchTermOpString getTestresultname() {
        return testresultname;
    }

    /**
     * @param testresultname the testresultname to set
     */
    public void setTestresultname(SearchTermOpString testresultname) {
        this.testresultname = testresultname;
    }

    /**
     * @return the testresultcodesys
     */
    public SearchTermOpString getTestresultcodesys() {
        return testresultcodesys;
    }

    /**
     * @param testresultcodesys the testresultcodesys to set
     */
    public void setTestresultcodesys(SearchTermOpString testresultcodesys) {
        this.testresultcodesys = testresultcodesys;
    }

    /**
     * @return the testresultsubid
     */
    public SearchTermOpString getTestresultsubid() {
        return testresultsubid;
    }

    /**
     * @param testresultsubid the testresultsubid to set
     */
    public void setTestresultsubid(SearchTermOpString testresultsubid) {
        this.testresultsubid = testresultsubid;
    }

    /**
     * @return the testresultcode
     */
    public SearchTermOpString getTestresultcode() {
        return testresultcode;
    }

    /**
     * @param testresultcode the testresultcode to set
     */
    public void setTestresultcode(SearchTermOpString testresultcode) {
        this.testresultcode = testresultcode;
    }

    /**
     * @return the testresultvalue
     */
    public SearchTermOpString getTestresultvalue() {
        return testresultvalue;
    }

    /**
     * @param testresultvalue the testresultvalue to set
     */
    public void setTestresultvalue(SearchTermOpString testresultvalue) {
        this.testresultvalue = testresultvalue;
    }

    /**
     * @return the testresultunits
     */
    public SearchTermOpString getTestresultunits() {
        return testresultunits;
    }

    /**
     * @param testresultunits the testresultunits to set
     */
    public void setTestresultunits(SearchTermOpString testresultunits) {
        this.testresultunits = testresultunits;
    }

    /**
     * @return the testpreviousdate
     */
    public SearchTermDateRange getTestpreviousdate() {
        return testpreviousdate;
    }

    /**
     * @param testpreviousdate the testpreviousdate to set
     */
    public void setTestpreviousdate(SearchTermDateRange testpreviousdate) {
        this.testpreviousdate = testpreviousdate;
    }

    /**
     * @return the testplacerordernumsource
     */
    public SearchTermOpString getTestplacerordernumsource() {
        return testplacerordernumsource;
    }

    /**
     * @param testplacerordernumsource the testplacerordernumsource to set
     */
    public void setTestplacerordernumsource(
            SearchTermOpString testplacerordernumsource) {
        this.testplacerordernumsource = testplacerordernumsource;
    }

    /**
     * @return the testfillerordernumsource
     */
    public SearchTermOpString getTestfillerordernumsource() {
        return testfillerordernumsource;
    }

    /**
     * @param testfillerordernumsource the testfillerordernumsource to set
     */
    public void setTestfillerordernumsource(
            SearchTermOpString testfillerordernumsource) {
        this.testfillerordernumsource = testfillerordernumsource;
    }

    /**
     * @return the testresultstatus
     */
    public SearchTermOpString getTestresultstatus() {
        return testresultstatus;
    }

    /**
     * @param testresultstatus the testresultstatus to set
     */
    public void setTestresultstatus(SearchTermOpString testresultstatus) {
        this.testresultstatus = testresultstatus;
    }

    /**
     * @return the obraltcode
     */
    public SearchTermOpString getObraltcode() {
        return obraltcode;
    }

    /**
     * @param obraltcode the obraltcode to set
     */
    public void setObraltcode(SearchTermOpString obraltcode) {
        this.obraltcode = obraltcode;
    }

    /**
     * @return the obraltcodetext
     */
    public SearchTermOpString getObraltcodetext() {
        return obraltcodetext;
    }

    /**
     * @param obraltcodetext the obraltcodetext to set
     */
    public void setObraltcodetext(SearchTermOpString obraltcodetext) {
        this.obraltcodetext = obraltcodetext;
    }

    /**
     * @return the obraltcodesys
     */
    public SearchTermOpString getObraltcodesys() {
        return obraltcodesys;
    }

    /**
     * @param obraltcodesys the obraltcodesys to set
     */
    public void setObraltcodesys(SearchTermOpString obraltcodesys) {
        this.obraltcodesys = obraltcodesys;
    }

    /**
     * @return the obxaltcode
     */
    public SearchTermOpString getObxaltcode() {
        return obxaltcode;
    }

    /**
     * @param obxaltcode the obxaltcode to set
     */
    public void setObxaltcode(SearchTermOpString obxaltcode) {
        this.obxaltcode = obxaltcode;
    }

    /**
     * @return the obxaltcodetext
     */
    public SearchTermOpString getObxaltcodetext() {
        return obxaltcodetext;
    }

    /**
     * @param obxaltcodetext the obxaltcodetext to set
     */
    public void setObxaltcodetext(SearchTermOpString obxaltcodetext) {
        this.obxaltcodetext = obxaltcodetext;
    }

    /**
     * @return the obxaltcodesys
     */
    public SearchTermOpString getObxaltcodesys() {
        return obxaltcodesys;
    }

    /**
     * @param obxaltcodesys the obxaltcodesys to set
     */
    public void setObxaltcodesys(SearchTermOpString obxaltcodesys) {
        this.obxaltcodesys = obxaltcodesys;
    }

    /**
     * @return the obrSetId
     */
    public SearchTermOpInt getObrSetId() {
        return obrSetId;
    }

    /**
     * @param obrSetId the obrSetId to set
     */
    public void setObrSetId(SearchTermOpInt obrSetId) {
        this.obrSetId = obrSetId;
    }

    /**
     * @return the obxStartSetId
     */
    public SearchTermOpInt getObxStartSetId() {
        return obxStartSetId;
    }

    /**
     * @param obxStartSetId the obxStartSetId to set
     */
    public void setObxStartSetId(SearchTermOpInt obxStartSetId) {
        this.obxStartSetId = obxStartSetId;
    }

    /**
     * @return the obxEndSetId
     */
    public SearchTermOpInt getObxEndSetId() {
        return obxEndSetId;
    }

    /**
     * @param obxEndSetId the obxEndSetId to set
     */
    public void setObxEndSetId(SearchTermOpInt obxEndSetId) {
        this.obxEndSetId = obxEndSetId;
    }
    
    /**
     * 
     * @return A TreeMap<Integer,String> containing the primary key and review status for
     * each review status, and a special entry with key value=0 with a blank representing no selection.
     */
    private ArrayList<Pair<Integer, String>> getReviewStatuses() {
    	ArrayList<Pair<Integer, String>> reviewStatuses = new ArrayList<Pair<Integer, String>>();
    	reviewStatuses.add(new Pair<Integer, String>(0, ""));
    	reviewStatuses.addAll(NCDUtilities.getService().getReviewStatusTypes());
    	return reviewStatuses;
    }

    /**
     * @return the releaseDate
     */
	public SearchTermDateRange getReleaseDate() {
		return releaseDate;
	}

    /**
     * @param releaseDate the releaseDate to set
     */
	public void setReleaseDate(SearchTermDateRange releaseDate) {
		this.releaseDate = releaseDate;
	}

	/**
	 * @return the sentInError
	 */
	public SearchTermBoolean getSentInError() {
		return sentInError;
	}

	/**
	 * @param sentInError the sentInError to set
	 */
	public void setSentInError(SearchTermBoolean sentInError) {
		this.sentInError = sentInError;
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

    /**
     * @param conditionName the conditionName to set
     */
    public void setConditionName(SearchTermOpCondition conditionName) {
        this.conditionName = conditionName;
    }
}

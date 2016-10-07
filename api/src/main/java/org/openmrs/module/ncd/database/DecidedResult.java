package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

import java.util.Date;

import org.openmrs.module.ncd.database.filter.ListPageRow;

/**
 * A cached result from critic processing of a candidate result, along with
 * the matching information to compare against future candidate results to
 * determine if it can be reused.
 * 
 * NOTE: All attributes must be nullable, as the DAO findDecidedResults
 * method uses Hibernate a Criteria based on an instance of this class.
 */
public class DecidedResult implements java.io.Serializable, ListPageRow {

    private static final long serialVersionUID = -3310638766208442654L;

    /** The synthetic primary key */
    private Long id;
    /** The HL7 field data type for the resultCode */
    private String dataType;
    /** The result code to be returned */
    private String resultCode;
    /** The result value (OBX.5) from the candidate result (AK1) */
    private String resultValue;
    /** The number of times this cache entry has been matched, initially one */
    private Integer resultCount;
    /** The date and time this cache entry was added to the database */
    private Date dateAdded;
    /** The date and time the candidate result that created this cache entry
     * was classified as reportable or non-reportable. */
    private Date dateClassified;
    /** The name of the human who classified the candidate result that
     * created this cache entry, or "Automatic" if this cache entry was
     * automatically created by the message processor based on critic
     * results. */
    private String classifiedByWhom;
    /** The name of the condition to be reported for candidate results that
     * match this cache entry, or null if the condition name is
     * undetermined. */
    private String conditionName;
    /** The date and time this cache entry was last modified (created or
     * matched). */
    private Date lastModified;
    /** The unique identifier assigned by the upstream pipeline to the message
     * containing the candidate result that created this cache entry. */
    private String mpqSequenceNumber;
    /** The OBR code from the candidate result (AK1) */
    private String obr;
    /** The OBR code system from the candidate result (AK1) */
    private String obrCodeSystem;
    /** The OBR text from the candidate result */ 
    private String obrText;
    /** The OBR alt text from the candidate result */ 
    private String obrAltText;
    /** The OBX code from the candidate result (AK1) */
    private String obx;
    /** The OBX code system from the candidate result (AK1) */
    private String obxCodeSystem;
    /** The OBX text from the candidate result */ 
    private String obxText;                
    /** The OBX alt text from the candidate result */ 
    private String obxAltText;
    /** The NTE text from the candidate result (AK1) */
    private String nte;
    /** The LOINC code to be returned for matches to this cache entry. */ 
    private String loincCode;
    /** Unknown, always "new" */
    private String disposition;
    /** Either "report", if this cache entry signals a reportable result, or
     * "not-reported", if this cache entry signals no reportable result */
    private String reportable;
    /** The raw message that generated this decided result, if this
     * decided result is reportable. */
    private RawMessage rawMessage;
    /** True iff this decided result has been manually reviewed. If true,
     * reportable results created based on this decided result will not
     * be marked for manual review, even if the condition normally
     * requires manual review.
     */
    private Boolean manuallyReviewed = false;

    public DecidedResult() {
    }

    public DecidedResult(String obr, String obx, String loinccode,
            String reportable) {
        this.obr = obr;
        this.obx = obx;
        this.loincCode = loinccode;
        this.reportable = reportable;
    }    
    
    public DecidedResult(DecidedResult toClone)
    {
        if (toClone != null) {
            this.classifiedByWhom = toClone.getClassifiedByWhom();
            this.conditionName = toClone.getConditionName();
            this.dataType = toClone.getDataType();
            this.dateAdded = toClone.getDateAdded();
            this.dateClassified = toClone.getDateClassified();
            this.disposition = toClone.getDisposition();
            this.lastModified = toClone.getLastModified();
            this.loincCode = toClone.getLoincCode();
            this.mpqSequenceNumber = toClone.getMpqSequenceNumber();
            this.nte = toClone.getNte();
            this.obr = toClone.getObr();
            this.obrCodeSystem = toClone.getObrCodeSystem();
            this.obrText = toClone.getObrText();
            this.obrAltText = toClone.getObrAltText();
            this.obx = toClone.getObx();
            this.obxCodeSystem = toClone.getObxCodeSystem();
            this.obxText = toClone.getObxText();
            this.obxAltText = toClone.getObxAltText();
            this.reportable = toClone.getReportable();     
            this.resultCode = toClone.getResultCode();
            this.resultCount = toClone.getResultCount();
            this.resultValue = toClone.getResultValue();
            this.rawMessage = toClone.getRawMessage();
            this.manuallyReviewed = toClone.isManuallyReviewed();
        }
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDataType() {
        return this.dataType;
    }

    public void setDataType(String datatype) {
        this.dataType = datatype;
    }

    public String getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(String resultcode) {
        this.resultCode = resultcode;
    }

    public String getResultValue() {
        return this.resultValue;
    }

    public void setResultValue(String resultvalue) {
        this.resultValue = resultvalue;
    }

    public Integer getResultCount() {
        return this.resultCount;
    }

    public void setResultCount(Integer resultcount) {
        this.resultCount = resultcount;
    }

    public Date getDateAdded() {
        return this.dateAdded;
    }

    public void setDateAdded(Date dateadded) {
        this.dateAdded = dateadded;
    }

    public Date getDateClassified() {
        return this.dateClassified;
    }

    public void setDateClassified(Date dateclassified) {
        this.dateClassified = dateclassified;
    }

    public String getClassifiedByWhom() {
        return this.classifiedByWhom;
    }

    public void setClassifiedByWhom(String classifiedbywhom) {
        this.classifiedByWhom = classifiedbywhom;
    }

    public String getConditionName() {
        return this.conditionName;
    }

    public void setConditionName(String conditionname) {
        this.conditionName = conditionname;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(Date lastmodified) {
        this.lastModified = lastmodified;
    }

    public String getMpqSequenceNumber() {
        return this.mpqSequenceNumber;
    }

    public void setMpqSequenceNumber(String mpqsequencenumber) {
        this.mpqSequenceNumber = mpqsequencenumber;
    }

    public String getObr() {
        return this.obr;
    }

    public void setObr(String obr) {
        this.obr = obr;
    }
    
    public String getObrCodeSystem() {
        return this.obrCodeSystem;
    }
    
    public void setObrCodeSystem(String obrCodeSystem) {
        this.obrCodeSystem = obrCodeSystem;
    }

    public String getObrText() {
        return this.obrText;
    }

    public void setObrText(String obrtext) {
        this.obrText = obrtext;
    }
    
    public String getObrAltText() {
        return this.obrAltText;
    }
    
    public void setObrAltText(String obralttext) {
        this.obrAltText = obralttext;
    }
    
    public String getObxAltText() {
        return this.obxAltText;
    }
    
    public void setObxAltText(String obxalttext) {
        this.obxAltText = obxalttext;
    }

    public String getObx() {
        return this.obx;
    }

    public void setObx(String obx) {
        this.obx = obx;
    }
    
    public String getObxCodeSystem() {
        return this.obxCodeSystem;
    }
    
    public void setObxCodeSystem(String obxCodeSystem) {
        this.obxCodeSystem = obxCodeSystem;
    }

    public String getObxText() {
        return this.obxText;
    }

    public void setObxText(String obxtext) {
        this.obxText = obxtext;
    }

    public String getNte() {
        return this.nte;
    }

    public void setNte(String nte) {
        this.nte = nte;
    }

    public String getLoincCode() {
        return this.loincCode;
    }

    public void setLoincCode(String loinccode) {
        this.loincCode = loinccode;
    }

    public String getDisposition() {
        return this.disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getReportable() {
        return this.reportable;
    }

    public void setReportable(String reportable) {
        this.reportable = reportable;
    }

    public RawMessage getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(RawMessage rawMessage) {
        this.rawMessage = rawMessage;
    }

    public Boolean isManuallyReviewed() {
        return manuallyReviewed;
    }

    public void setManuallyReviewed(Boolean manuallyReviewed) {
        this.manuallyReviewed = manuallyReviewed;
    }
}

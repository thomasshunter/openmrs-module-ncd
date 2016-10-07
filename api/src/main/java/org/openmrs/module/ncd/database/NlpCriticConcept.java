package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

/**
 * One "concept" recognizable by the NLP Critic.
 */
public class NlpCriticConcept implements java.io.Serializable {

    private static final long serialVersionUID = 3104018165117410305L;

    /** The name of the concept to be recognized */
    private String conceptName;

    private String code;
    /** The condition recognized by this concept */
    private Condition condition;

    private String nameCode;

    private String absolutes;

    private String absExcept;

    private Integer altnum;

    private String preConcept;

    private String postConcept;

    private String altCon1;

    private String altCon2;

    private String altCon3;

    private String altExcept;

    private String negExceptCon;

    private String negExNeg;

    private String onOff;

    private String reportExtraction;

    private String sectionExtraction;

    private String sectionStart;

    private String sectionEnd;

    private String specialNegs;

    private String smallWindowNegs;

    private String absoluteNegs;

    private String contexts;
    
    private String paragraph;
    
    private String negationGroup;

    public NlpCriticConcept() {
    }

    public NlpCriticConcept(String conceptName) {
        this.conceptName = conceptName;
    }    

    public NlpCriticConcept(NlpCriticConcept src) {
        this.conceptName = src.conceptName;
        this.code = src.code;
        this.condition = src.condition;
        this.nameCode = src.nameCode;
        this.absolutes = src.absolutes;
        this.absExcept = src.absExcept;
        this.altnum = src.altnum;
        this.preConcept = src.preConcept;
        this.postConcept = src.postConcept;
        this.altCon1 = src.altCon1;
        this.altCon2 = src.altCon2;
        this.altCon3 = src.altCon3;
        this.altExcept = src.altExcept;
        this.negExceptCon = src.negExceptCon;
        this.negExNeg = src.negExNeg;
        this.onOff = src.onOff;
        this.reportExtraction = src.reportExtraction;
        this.sectionExtraction = src.sectionExtraction;
        this.sectionStart = src.sectionStart;
        this.sectionEnd = src.sectionEnd;
        this.specialNegs = src.specialNegs;
        this.smallWindowNegs = src.smallWindowNegs;
        this.absoluteNegs = src.absoluteNegs;
        this.contexts = src.contexts;
        this.paragraph = src.paragraph;
        this.negationGroup = src.negationGroup;
    }

    public String getConceptName() {
        return this.conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Condition getCondition() {
        return this.condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getNameCode() {
        return this.nameCode;
    }

    public void setNameCode(String nameCode) {
        this.nameCode = nameCode;
    }

    public String getAbsolutes() {
        return this.absolutes;
    }

    public void setAbsolutes(String absolutes) {
        this.absolutes = absolutes;
    }

    public String getAbsExcept() {
        return this.absExcept;
    }

    public void setAbsExcept(String absExcept) {
        this.absExcept = absExcept;
    }

    public Integer getAltnum() {
        return this.altnum;
    }

    public void setAltnum(Integer altnum) {
        this.altnum = altnum;
    }

    public String getPreConcept() {
        return this.preConcept;
    }

    public void setPreConcept(String preConcept) {
        this.preConcept = preConcept;
    }

    public String getPostConcept() {
        return this.postConcept;
    }

    public void setPostConcept(String postConcept) {
        this.postConcept = postConcept;
    }

    public String getAltCon1() {
        return this.altCon1;
    }

    public void setAltCon1(String altCon1) {
        this.altCon1 = altCon1;
    }

    public String getAltCon2() {
        return this.altCon2;
    }

    public void setAltCon2(String altCon2) {
        this.altCon2 = altCon2;
    }

    public String getAltCon3() {
        return this.altCon3;
    }

    public void setAltCon3(String altCon3) {
        this.altCon3 = altCon3;
    }

    public String getAltExcept() {
        return this.altExcept;
    }

    public void setAltExcept(String altExcept) {
        this.altExcept = altExcept;
    }

    public String getNegExceptCon() {
        return this.negExceptCon;
    }

    public void setNegExceptCon(String negExceptCon) {
        this.negExceptCon = negExceptCon;
    }

    public String getNegExNeg() {
        return this.negExNeg;
    }

    public void setNegExNeg(String negExNeg) {
        this.negExNeg = negExNeg;
    }

    public String getOnOff() {
        return this.onOff;
    }

    public void setOnOff(String onOff) {
        this.onOff = onOff;
    }

    public String getReportExtraction() {
        return this.reportExtraction;
    }

    public void setReportExtraction(String reportExtraction) {
        this.reportExtraction = reportExtraction;
    }

    public String getSectionExtraction() {
        return this.sectionExtraction;
    }

    public void setSectionExtraction(String sectionExtraction) {
        this.sectionExtraction = sectionExtraction;
    }

    public String getSectionStart() {
        return this.sectionStart;
    }

    public void setSectionStart(String sectionStart) {
        this.sectionStart = sectionStart;
    }

    public String getSectionEnd() {
        return this.sectionEnd;
    }

    public void setSectionEnd(String sectionEnd) {
        this.sectionEnd = sectionEnd;
    }

    public String getSpecialNegs() {
        return this.specialNegs;
    }

    public void setSpecialNegs(String specialNegs) {
        this.specialNegs = specialNegs;
    }

    public String getSmallWindowNegs() {
        return this.smallWindowNegs;
    }

    public void setSmallWindowNegs(String smallWindowNegs) {
        this.smallWindowNegs = smallWindowNegs;
    }

    public String getAbsoluteNegs() {
        return this.absoluteNegs;
    }

    public void setAbsoluteNegs(String absoluteNegs) {
        this.absoluteNegs = absoluteNegs;
    }

    public String getContexts() {
        return this.contexts;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }
    
    public String getParagraph() {
        return this.paragraph;
    }
    
    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }

    public String getNegationGroup() {
        return negationGroup;
    }

    public void setNegationGroup(String negationGroup) {
        this.negationGroup = negationGroup;
    }

}

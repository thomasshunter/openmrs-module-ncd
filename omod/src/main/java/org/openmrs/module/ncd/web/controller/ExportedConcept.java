package org.openmrs.module.ncd.web.controller;

import java.io.Serializable;

import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.NlpCriticConcept;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/**
 * An NlpCriticConcept as it is exported to a flat file.
 */
public class ExportedConcept implements Serializable {

	private static final long serialVersionUID = -6947748448981828776L;
	
	/** The name of the concept to be recognized */
    private String conceptName;
    private String code;
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
    private String conditionName;

	// todo - all members of NlpCriticConcept, replacing conditionid by the condition name.

    public ExportedConcept() {
    }

	public NlpCriticConcept toNlpCriticConcept() {
		
		Condition cond = NCDUtilities.getService().findConditionByName(conditionName);

		NlpCriticConcept ncc = new NlpCriticConcept();
		ncc.setConceptName(this.conceptName);
		ncc.setCode(this.code);
		ncc.setNameCode(this.nameCode);
		ncc.setAbsolutes(this.absolutes);
		ncc.setAbsExcept(this.absExcept);
		ncc.setAltnum(this.altnum);
		ncc.setPreConcept(this.preConcept);
		ncc.setPostConcept(this.postConcept);
		ncc.setAltCon1(this.altCon1);
		ncc.setAltCon2(this.altCon2);
		ncc.setAltCon3(this.altCon3);
		ncc.setAltExcept(this.altExcept);
		ncc.setNegExceptCon(this.negExceptCon);
		ncc.setNegExNeg(this.negExNeg);
		ncc.setOnOff(this.onOff);
		ncc.setReportExtraction(this.reportExtraction);
		ncc.setSectionExtraction(this.sectionExtraction);
		ncc.setSectionStart(this.sectionStart);
		ncc.setSectionEnd(this.sectionEnd);
		ncc.setSpecialNegs(this.specialNegs);
		ncc.setSmallWindowNegs(this.smallWindowNegs);
		ncc.setAbsoluteNegs(this.absoluteNegs);
		ncc.setContexts(this.contexts);
		ncc.setParagraph(this.paragraph);
		ncc.setNegationGroup(this.negationGroup);
		ncc.setCondition(cond);

		return ncc;
	}
	
	public static ExportedConcept fromNlpCriticConcept(NlpCriticConcept concept) {
		
		ExportedConcept ec = new ExportedConcept();
		ec.setConceptName(concept.getConceptName());
		ec.setCode(concept.getCode());
		ec.setNameCode(concept.getNameCode());
		ec.setAbsolutes(concept.getAbsolutes());
		ec.setAbsExcept(concept.getAbsExcept());
		ec.setAltnum(concept.getAltnum());
		ec.setPreConcept(concept.getPreConcept());
		ec.setPostConcept(concept.getPostConcept());
		ec.setAltCon1(concept.getAltCon1());
		ec.setAltCon2(concept.getAltCon2());
		ec.setAltCon3(concept.getAltCon3());
		ec.setAltExcept(concept.getAltExcept());
		ec.setNegExceptCon(concept.getNegExceptCon());
		ec.setNegExNeg(concept.getNegExNeg());
		ec.setOnOff(concept.getOnOff());
		ec.setReportExtraction(concept.getReportExtraction());
		ec.setSectionExtraction(concept.getSectionExtraction());
		ec.setSectionStart(concept.getSectionStart());
		ec.setSectionEnd(concept.getSectionEnd());
		ec.setSpecialNegs(concept.getSpecialNegs());
		ec.setSmallWindowNegs(concept.getSmallWindowNegs());
		ec.setAbsoluteNegs(concept.getAbsoluteNegs());
		ec.setContexts(concept.getContexts());
		ec.setParagraph(concept.getParagraph());
		ec.setNegationGroup(concept.getNegationGroup());
		ec.setConditionName(concept.getCondition().getDisplayText());

		return ec;
	}

	public String getConceptName() {
		return conceptName;
	}

	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getNameCode() {
		return nameCode;
	}

	public void setNameCode(String nameCode) {
		this.nameCode = nameCode;
	}

	public String getAbsolutes() {
		return absolutes;
	}

	public void setAbsolutes(String absolutes) {
		this.absolutes = absolutes;
	}

	public String getAbsExcept() {
		return absExcept;
	}

	public void setAbsExcept(String absExcept) {
		this.absExcept = absExcept;
	}

	public Integer getAltnum() {
		return altnum;
	}

	public void setAltnum(Integer altnum) {
		this.altnum = altnum;
	}

	public String getPreConcept() {
		return preConcept;
	}

	public void setPreConcept(String preConcept) {
		this.preConcept = preConcept;
	}

	public String getPostConcept() {
		return postConcept;
	}

	public void setPostConcept(String postConcept) {
		this.postConcept = postConcept;
	}

	public String getAltCon1() {
		return altCon1;
	}

	public void setAltCon1(String altCon1) {
		this.altCon1 = altCon1;
	}

	public String getAltCon2() {
		return altCon2;
	}

	public void setAltCon2(String altCon2) {
		this.altCon2 = altCon2;
	}

	public String getAltCon3() {
		return altCon3;
	}

	public void setAltCon3(String altCon3) {
		this.altCon3 = altCon3;
	}

	public String getAltExcept() {
		return altExcept;
	}

	public void setAltExcept(String altExcept) {
		this.altExcept = altExcept;
	}

	public String getNegExceptCon() {
		return negExceptCon;
	}

	public void setNegExceptCon(String negExceptCon) {
		this.negExceptCon = negExceptCon;
	}

	public String getNegExNeg() {
		return negExNeg;
	}

	public void setNegExNeg(String negExNeg) {
		this.negExNeg = negExNeg;
	}

	public String getOnOff() {
		return onOff;
	}

	public void setOnOff(String onOff) {
		this.onOff = onOff;
	}

	public String getReportExtraction() {
		return reportExtraction;
	}

	public void setReportExtraction(String reportExtraction) {
		this.reportExtraction = reportExtraction;
	}

	public String getSectionExtraction() {
		return sectionExtraction;
	}

	public void setSectionExtraction(String sectionExtraction) {
		this.sectionExtraction = sectionExtraction;
	}

	public String getSectionStart() {
		return sectionStart;
	}

	public void setSectionStart(String sectionStart) {
		this.sectionStart = sectionStart;
	}

	public String getSectionEnd() {
		return sectionEnd;
	}

	public void setSectionEnd(String sectionEnd) {
		this.sectionEnd = sectionEnd;
	}

	public String getSpecialNegs() {
		return specialNegs;
	}

	public void setSpecialNegs(String specialNegs) {
		this.specialNegs = specialNegs;
	}

	public String getSmallWindowNegs() {
		return smallWindowNegs;
	}

	public void setSmallWindowNegs(String smallWindowNegs) {
		this.smallWindowNegs = smallWindowNegs;
	}

	public String getAbsoluteNegs() {
		return absoluteNegs;
	}

	public void setAbsoluteNegs(String absoluteNegs) {
		this.absoluteNegs = absoluteNegs;
	}

	public String getContexts() {
		return contexts;
	}

	public void setContexts(String contexts) {
		this.contexts = contexts;
	}

	public String getParagraph() {
		return paragraph;
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

	public String getConditionName() {
		return conditionName;
	}

	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}
}


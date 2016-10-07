package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

/**
 * Purpose unclear.
 */
public class ReportingCriterion implements java.io.Serializable {

    private static final long serialVersionUID = 2504316237860717402L;

    private Long id;

    private CodeCondition codeCondition;

    private CodeSystem codeSystem;

    private String resultCode;

    private String resultValue;

    public ReportingCriterion() {
    }

    public ReportingCriterion(CodeCondition tblloinccode, String resultvalue) {
        this.codeCondition = tblloinccode;
        this.resultValue = resultvalue;
    }

    public ReportingCriterion(CodeCondition tblloinccode,
            CodeSystem tblcodesystem, String resultcode, String resultvalue) {
        this.codeCondition = tblloinccode;
        this.codeSystem = tblcodesystem;
        this.resultCode = resultcode;
        this.resultValue = resultvalue;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CodeCondition getCodeCondition() {
        return this.codeCondition;
    }

    public void setCodeCondition(CodeCondition tblloinccode) {
        this.codeCondition = tblloinccode;
    }

    public CodeSystem getCodeSystem() {
        return this.codeSystem;
    }

    public void setCodeSystem(CodeSystem tblcodesystem) {
        this.codeSystem = tblcodesystem;
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

}

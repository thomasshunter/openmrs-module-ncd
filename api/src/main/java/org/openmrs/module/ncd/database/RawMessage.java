package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;

/**
 * An HL7 message received by the NCD message processor.
 */
public class RawMessage implements java.io.Serializable {

    private static final long serialVersionUID = -3490519826215566049L;
    /** The synthetic primary key */
    private Long id;
    /** The message text */
    private String messageText;
    /** The set of processing errors associated with this message, if any */
    private Set<Error> errors = new HashSet<Error>(0);
    /** The set of reportable results associated with this message, if any */
    private Set<ReportableResult> reportableResults = new HashSet<ReportableResult>(
            0);

    public RawMessage() {
    }

    public RawMessage(String messageText) {
        this.messageText = messageText;
    }

    public RawMessage(String messageText, Set<Error> errors,
            Set<ReportableResult> reportableResults) {
        this.messageText = messageText;
        this.errors = errors;
        this.reportableResults = reportableResults;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageText() {
        return this.messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Set<Error> getErrors() {
        return this.errors;
    }

    public void setErrors(Set<Error> errors) {
        this.errors = errors;
    }

    public Set<ReportableResult> getReportableResults() {
        return this.reportableResults;
    }

    public void setReportableResults(
            Set<ReportableResult> reportableResults) {
        this.reportableResults = reportableResults;
    }

}

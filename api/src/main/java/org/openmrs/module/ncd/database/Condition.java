package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;

/**
 * A "thing" that can be recognized by the NCD. Currently the vast majority
 * of "things" are Dwyer Conditions - medical conditions that warrant public
 * health scrutiny, such as communicable diseases or antibiotic resistance.
 */
public class Condition implements java.io.Serializable {

    private static final long serialVersionUID = -2354649632550184633L;

    /** The synthetic primary key */
    private Long id;
    /** The group this condition is a part of, such as "Poisonings" or 
     * "Zoonotic" */
    private ConditionGroup conditionGroup;
    /** The display text for this condition, such as "Lead exposure" */
    private String displayText;
    /** True if this condition needs to be reported to public health agencies,
     * false if not. */
    private boolean reportable;
    /** True if this condition requires review by a human before it is
     * reported to public health agencies. This is intended to be used
     * primarily for conditions so rare and unlikely that positives are
     * almost certainly test or reporting errors (Ebola, etc). */
    private boolean manualReviewRequired;
    /** The set of Code/Condition mappings that might signal this condition. */
    private Set<CodeCondition> codeConditions = new HashSet<CodeCondition>(0);
    /** The set of NLP critic concepts that might correspond to this
     * condition. */
    private Set<NlpCriticConcept> nlpCriticConcepts = new HashSet<NlpCriticConcept>(0);
    /** Whether this condition should be reported all the time, without regard
     * to whether the message contains a positive or negative instance of this condition.
     */
    private boolean reportAll;
    /** True iff this condition has been retired. */
    private boolean retired;

    public Condition() {
    }

    public Condition(ConditionGroup conditionGroup,
            String displayText, boolean reportable) {
        this.conditionGroup = conditionGroup;
        this.displayText = displayText;
        this.reportable = reportable;
    }

    public Condition(ConditionGroup conditionGroup,
            String displayText, boolean reportable,
            Set<CodeCondition> codeConditions) {
        this.conditionGroup = conditionGroup;
        this.displayText = displayText;
        this.reportable = reportable;
        this.codeConditions = codeConditions;
    }

    public Condition(Condition src) {

        this.id = src.id;
        this.conditionGroup = src.conditionGroup;
        this.displayText = src.displayText;
        this.reportable = src.reportable;
        this.manualReviewRequired = src.manualReviewRequired;
        this.codeConditions = new HashSet<CodeCondition>(src.codeConditions);
        this.nlpCriticConcepts = new HashSet<NlpCriticConcept>(src.nlpCriticConcepts);
        this.reportAll = src.reportAll;
        this.retired = src.retired;
    }

    public String toString() {
        
        return "Condition(" +
                    "id=" + id +
                    ", displayText=" + displayText +
                    ", reportable=" + reportable +
                    ", manualReviewRequired=" + manualReviewRequired +
                    ", reportAll=" + reportAll +
                    ", retired=" + retired +
               ")";
    }
    
    public String toString(String indent) {

        return "Condition(\n" +
                    indent + "id=" + id + "\n" +
                    indent + "displayText=" + displayText + "\n" +
                    indent + "reportable=" + reportable + "\n" +
                    indent + "manualReviewRequired=" + manualReviewRequired + "\n" +
                    indent + "reportAll=" + reportAll + "\n" +
                    indent + "retired=" + retired + "\n" +
                    indent + ")";
    }

    public int hashCode() {
        
        return displayText.hashCode();
    }
    
    public boolean equals(Object o) {
        
        if (!(o instanceof Code)) {
            
            return false;
        }
        
        Condition that = (Condition) o;
        return this.displayText.equals(that.displayText);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConditionGroup getConditionGroup() {
        return this.conditionGroup;
    }

    public void setConditionGroup(ConditionGroup conditionGroup) {
        this.conditionGroup = conditionGroup;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public boolean isReportable() {
        return this.reportable;
    }

    public void setReportable(boolean reportable) {
        this.reportable = reportable;
    }
    
    public boolean isManualReviewRequired() {
        return this.manualReviewRequired;
    }
    
    public void setManualReviewRequired(boolean requiresManualReview) {
        this.manualReviewRequired = requiresManualReview;
    }

    public Set<CodeCondition> getCodeConditions() {
        return this.codeConditions;
    }

    public void setCodeConditions(Set<CodeCondition> codeConditions) {
        this.codeConditions = codeConditions;
    }

    public Set<NlpCriticConcept> getNlpCriticConcepts() {
        return nlpCriticConcepts;
    }

    public void setNlpCriticConcepts(Set<NlpCriticConcept> nlpCriticConcepts) {
        this.nlpCriticConcepts = nlpCriticConcepts;
    }

	public boolean isReportAll() {
		return reportAll;
	}
	
	public void setReportAll(boolean reportAll) {
		this.reportAll = reportAll;
	}

	public boolean isRetired() {
		return retired;
	}

	public void setRetired(boolean retired) {
		this.retired = retired;
	}
}

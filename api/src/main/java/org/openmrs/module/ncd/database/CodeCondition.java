package org.openmrs.module.ncd.database;

// Generated Aug 27, 2008 7:12:14 PM by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;

/**
 * Records that detection of the specified indicator string in candidate
 * result with the specified code detects the specified condition. Used by
 * the NLP critic to determine which indicator strings to look for given the
 * code already known, and what condition is signal if the indicator string
 * is found.
 * 
 * Incorrectly used by other code as an abstract code table or LOINC code
 * table. Correcting this is a work in progress.
 * 
 * (was Tblloinccode)
 */
public class CodeCondition implements java.io.Serializable 
{
    private static final long serialVersionUID = -6907769706754624781L;

    /** The synthetic primary key */
    private Long id;
    /** The code that may signal this Condition */
    private Code code;
    /** The condition corresponding to the code and indicator */
    private Condition condition;
    /** A String that *might* appear in the free text to which of many
     * conditions associated with a particular actually applies. */
    private String conditionIndicator;
    /** The set of reporting criteria related to this code */
    private Set<ReportingCriterion> reportingCriteria = new HashSet<ReportingCriterion>(0);
    /** The set of reportable results reporting this code */
    private Set<ReportableResult> reportableResults = new HashSet<ReportableResult>(0);

    public CodeCondition() 
    {
    }

    public CodeCondition(Condition tblconditionname) 
    {
        this.condition = tblconditionname;
    }
    
    public CodeCondition(long id, Code code, String conditionIndicator, Condition condition) 
    {
        this.id                 = id;
        this.condition          = condition;
        this.conditionIndicator = conditionIndicator;
        this.code               = code;
    }

    public CodeCondition(Condition condition, String conditionIndicator, Code code, Set<ReportingCriterion> reportingCriteria, Set<ReportableResult> reportableResults) 
    {
        this.condition          = condition;
        this.conditionIndicator = conditionIndicator;
        this.code               = code;
        this.reportingCriteria  = reportingCriteria;
        this.reportableResults  = reportableResults;
    }
    
    public String toString() 
    {
        StringBuilder out = new StringBuilder();
        
        out.append( "\n------------------------" );
        out.append( "\n       CodeCondition    " );
        out.append( "\n------------------------" );
        out.append( "\n id                    =" + this.id );
        out.append( "\n conditionIndicator    =" + this.conditionIndicator );
        out.append( "\n code                  =" + this.code );
        out.append( "\n condition             =" + this.condition );
        out.append( "\n------------------------" );
        
        return out.toString();
    }

    @Override
    public boolean equals(Object other) 
    {  
        if( other instanceof CodeCondition )
        {
            // Move along. These are not the droids you're looking for.
        }
        else
        {
            return false;
        }
                
        CodeCondition that = (CodeCondition) other;

        if (this.id == null && that.id == null) 
        {
            return this == that;
        }
        
        if (this.id == null || that.id == null) 
        {
            return false;
        }
        
        return this.id.equals(that.id);
    }
    
    @Override
    public int hashCode() 
    {    
        int hash = 7;

        if (this.id != null) 
        {
            hash = this.id.hashCode();
        }
        else 
        {
            hash = super.hashCode();
        }
        
        return hash;
    }

    public Long getId() 
    {
        return this.id;
    }
    public void setId(Long id) 
    {
        this.id = id;
    }

    public Condition getCondition() 
    {
        return this.condition;
    }
    public void setCondition(Condition tblconditionname) 
    {
        this.condition = tblconditionname;
    }

    public String getConditionIndicator() 
    {
        return this.conditionIndicator;
    }
    public void setConditionIndicator(String conditionindicator) 
    {
        this.conditionIndicator = conditionindicator;
    }

    public Set<ReportingCriterion> getReportingCriteria() 
    {
        return this.reportingCriteria;
    }
    public void setReportingCriteria( Set<ReportingCriterion> reportingCriteria ) 
    {
        this.reportingCriteria = reportingCriteria;
    }

    public Set<ReportableResult> getReportableResults() 
    {
        return this.reportableResults;
    }
    public void setReportableResults( Set<ReportableResult> reportableResults ) 
    {
        this.reportableResults = reportableResults;
    }

    public Code getCode() 
    {
        return code;
    }
    public void setCode(Code code) 
    {
        this.code = code;
    }
}

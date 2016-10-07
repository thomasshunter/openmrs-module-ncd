package org.openmrs.module.ncd.output.aggrpt;

import java.util.ArrayList;
import java.util.List;

public class ReportDataConditionGroup {

    private String conditionGroupName;
    private List<ReportDataCondition> conditions;
    
    public ReportDataConditionGroup(String conditionGroupName) {
        
        this.conditionGroupName = conditionGroupName;
        conditions = new ArrayList<ReportDataCondition>();
    }
    
    public void addCondition(ReportDataCondition condition) {
        
        conditions.add(condition);
    }

    public String getConditionGroupName() {
        return conditionGroupName;
    }

    public void setConditionGroupName(String conditionGroupName) {
        this.conditionGroupName = conditionGroupName;
    }

    public List<ReportDataCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<ReportDataCondition> conditions) {
        this.conditions = conditions;
    }
    
    public String toString() {

        return "ReportDataConditionGroup(" +
                    "conditionGroupName=" + conditionGroupName +
                    ", conditions=" + conditions +
                ")";
    }
}

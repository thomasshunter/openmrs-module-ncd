package org.openmrs.module.ncd.database.filter;

public class UnusualConditionRateFilter
    extends SearchFilterBase
    implements java.io.Serializable
{
    private static final long serialVersionUID = 8637036889807372784L;
    /** The number of days of reportable results to be examined to determine
     * the historical average rate for each app/loc/condition triple. */
    public int historyDays;
    /** The fraction of the historical average rate below which the recent
     * rate for a app/loc/condition triple will be reported as "unusual" */
    public double lowRateRatio;
    /** The fraction of the historical average rate above which the recent
     * rate for a app/loc/condition triple will be reported as "unusual" */
    public double highRateRatio;
    
    public UnusualConditionRateFilter() {
        super();
    }
    
    public String toString() {
        
        return "UnusualConditionRateFilter(" +
                    "historyDays=" + historyDays +
                    ", lowRateRatio=" + lowRateRatio +
                    ", highRateRatio=" + highRateRatio +
               ")";
    }

    /**
     * @return the historyDays
     */
    public int getHistoryDays() {
        return historyDays;
    }

    /**
     * @param historyDays the historyDays to set
     */
    public void setHistoryDays(int historyDays) {
        this.historyDays = historyDays;
    }

    /**
     * @return the lowRateRatio
     */
    public double getLowRateRatio() {
        return lowRateRatio;
    }

    /**
     * @param lowRateRatio the lowRateRatio to set
     */
    public void setLowRateRatio(double lowRateRatio) {
        this.lowRateRatio = lowRateRatio;
    }

    /**
     * @return the highRateRatio
     */
    public double getHighRateRatio() {
        return highRateRatio;
    }

    /**
     * @param highRateRatio the highRateRatio to set
     */
    public void setHighRateRatio(double highRateRatio) {
        this.highRateRatio = highRateRatio;
    }
}

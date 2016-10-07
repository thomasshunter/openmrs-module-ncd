package org.openmrs.module.ncd.output.condrates;

import java.util.List;

import org.openmrs.module.ncd.database.filter.UnusualConditionRateFilter;
import org.openmrs.module.ncd.model.ConditionCount;
import org.openmrs.module.ncd.utilities.DateRange;

public class ReportData implements java.io.Serializable {

    private static final long serialVersionUID = 2886802041118805370L;
    
    private int sampleDays;
    private DateRange sampleWindow;
    private DateRange historyWindow;
    private UnusualConditionRateFilter filter;
    private List<ConditionCount> counts;
    
    public ReportData() {
    }

    public String toString() {

        return "ReportData(" +
                    "sampleWindow=" + sampleWindow +
                    ", historyWindow=" + historyWindow +
                    ", sampleDays=" + sampleDays +
                    ", filter=" + filter +
                    ", counts=" + counts +
               ")";
    }
    
    /**
     * @return the sampleWindow
     */
    public DateRange getSampleWindow() {
        return sampleWindow;
    }

    /**
     * @param sampleWindow the sampleWindow to set
     */
    public void setSampleWindow(DateRange sampleWindow) {
        this.sampleWindow = sampleWindow;
    }

    /**
     * @return the filter
     */
    public UnusualConditionRateFilter getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(UnusualConditionRateFilter filter) {
        this.filter = filter;
    }

    /**
     * @return the counts
     */
    public List<ConditionCount> getCounts() {
        return counts;
    }

    /**
     * @param counts the counts to set
     */
    public void setCounts(List<ConditionCount> counts) {
        this.counts = counts;
    }

	public int getSampleDays() {
		return sampleDays;
	}

	public void setSampleDays(int sampleDays) {
		this.sampleDays = sampleDays;
	}

	public DateRange getHistoryWindow() {
		return historyWindow;
	}

	public void setHistoryWindow(DateRange historyWindow) {
		this.historyWindow = historyWindow;
	}
}

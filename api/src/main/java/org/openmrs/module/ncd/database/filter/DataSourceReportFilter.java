package org.openmrs.module.ncd.database.filter;

import org.openmrs.module.ncd.utilities.DateRange;

public class DataSourceReportFilter {

	private DateRange samplingWindow;
	
	public DataSourceReportFilter() {
	}
	
	public DataSourceReportFilter(DateRange samplingWindow) {
		this.samplingWindow = samplingWindow;
	}
	
	public String toString() {
		
		return "DataSourceReportFilter(" +
					"samplingWindow=" + samplingWindow +
			   ")";
	}

	public void setSamplingWindow(DateRange samplingWindow) {
		this.samplingWindow = samplingWindow;
	}

	public DateRange getSamplingWindow() {
		return samplingWindow;
	}
}

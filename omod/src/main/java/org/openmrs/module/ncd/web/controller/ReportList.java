package org.openmrs.module.ncd.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ReportList {

	private ArrayList<ReportSummary> reportSummaries;

	ReportList() {
	}
	
	public ArrayList<ReportSummary> getReportSummaries() {
		return reportSummaries;
	}

	public void setReportSummaries(ArrayList<ReportSummary> reportSummaries) {
		this.reportSummaries = reportSummaries;
	}
	
	public void sortBy(Comparator<ReportSummary> c) {
		Collections.sort(reportSummaries, c);
	}
}

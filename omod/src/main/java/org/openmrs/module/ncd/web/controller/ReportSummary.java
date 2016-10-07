package org.openmrs.module.ncd.web.controller;

import java.util.Comparator;


public class ReportSummary {

	private Boolean selected;
	private Integer id;
	private String name;
	private String type;
	private String enabled;

	public ReportSummary() {
	}

	public static Comparator<ReportSummary> getComparatorByName() {
		
		return new Comparator<ReportSummary>() {

			@Override
			public int compare(ReportSummary left, ReportSummary right) {
				return left.getName().compareToIgnoreCase(right.getName());
			}
		};
	}
	
	public Boolean getSelected() {
		return selected;
	}
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getEnabled() {
		return enabled;
	}
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}
}

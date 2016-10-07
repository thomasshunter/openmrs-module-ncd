package org.openmrs.module.ncd.database;

/**
 * A reportable result that has been exported in a report.
 */
public class ExportedResult implements java.io.Serializable {

	private static final long serialVersionUID = -6540614268700549268L;
	
	/** The synthetic primary key */
	private Long id;
	/** The task run that exported this reportable result */
	private TaskRunStatus taskRunStatus;
	/** The reportable result that was exported */
	private ReportableResult reportableResult;

	public ExportedResult() {
	}
	
	public ExportedResult(TaskRunStatus taskRunStatus, ReportableResult reportableResult) {
		this.taskRunStatus = taskRunStatus;
		this.reportableResult = reportableResult;
	}

	@Override
	public String toString() {
     
		return toString("");
	}

	public String toString(String indent) {

		return "ExportedResult(" +
        		indent + "id=" + id +
        		indent + ", taskRunStatus=" + taskRunStatus.toString() +
        		indent + ", reportableResult=" + reportableResult.toString() +
        		indent + ")";
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TaskRunStatus getTaskRunStatus() {
		return taskRunStatus;
	}

	public void setTaskRunStatus(TaskRunStatus taskRunStatus) {
		this.taskRunStatus = taskRunStatus;
	}

	public ReportableResult getReportableResult() {
		return reportableResult;
	}

	public void setReportableResult(ReportableResult reportableResult) {
		this.reportableResult = reportableResult;
	}
}

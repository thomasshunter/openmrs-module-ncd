package org.openmrs.module.ncd.database;

/**
 * The recipient of an export report notification (via email).
 */
public class ExportRecipient implements java.io.Serializable {

	private static final long serialVersionUID = -2521172916586690662L;

	/** The synthetic primary key */
	private Long id;
	/** The task run that sent a notification to this recipient */
	private TaskRunStatus taskRunStatus;
	/** The recipient's email address */
	private String recipientEmail;
	
	public ExportRecipient() {
	}
	
	public ExportRecipient(TaskRunStatus taskRunStatus, String recipientEmail) {
		this.taskRunStatus = taskRunStatus;
		this.recipientEmail = recipientEmail;
	}

	@Override
	public String toString() {
     
		return toString("");
	}

	public String toString(String indent) {

		return "ExportRecipient(" +
        		indent + "id=" + id +
        		indent + ", taskRunStatus=" + taskRunStatus.toString() +
        		indent + ", recipientEmail=" + recipientEmail +
        		indent + ")";
	}

	public Long getId() {
		return id;
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
	public String getRecipientEmail() {
		return recipientEmail;
	}
	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}
}

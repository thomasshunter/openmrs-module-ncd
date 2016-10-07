package org.openmrs.module.ncd.database;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.scheduler.TaskDefinition;

/**
 * The recorded status of one execution attempt of one scheduled NCD task.
 * Currently only used for reports.
 */
public class TaskRunStatus implements java.io.Serializable {

	private static final long serialVersionUID = -2571906034554465691L;
	
	/** The unique id of this status record */
    private Long id;
    /** The task to which this status record belongs */
    private TaskDefinition task;
    /** The date and time at which this execution attempt began */
    private Date started;
    /** The date and time at which this execution attempt ended */
    private Date ended;
    /** The resultSeq of the first exported reportable record, if any */
    private Long first;
    /** The resultSeq of the last exported reportable record, if any */
    private Long last;
    /** True iff this execution attempt succeeded */
    private boolean succeeded;
    /** A human-readable status message summarizing this execution attempt */
    private String message;
    /** The Set of exported reportable results associated with this task run, if any */
    private Set<ExportedResult> exportedResults = new HashSet<ExportedResult>(0);
    /** The Set of export recipients associated with this task run, if any */
    private Set<ExportRecipient> exportRecipients = new HashSet<ExportRecipient>(0);

    public TaskRunStatus() {
    }

    public TaskRunStatus(TaskDefinition task, boolean succeeded, String message) {
        this.task = task;
        this.started = new Date();
        this.ended = new Date();
        this.first = new Long(0);
        this.last = new Long(0);
        this.succeeded = succeeded;
        this.message = message;
    }
    
    public TaskRunStatus(TaskDefinition task, Date started, Date ended, boolean succeeded, String message) {
        this.task = task;
        this.started = started;
        this.ended = ended;
        this.succeeded = succeeded;
        this.message = message;
    }

    public TaskRunStatus(TaskDefinition task, Date started, Date ended, Long first, Long last, boolean succeeded, String message) {
        this.task = task;
        this.started = started;
        this.ended = ended;
        this.first = first;
        this.last = last;
        this.succeeded = succeeded;
        this.message = message;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the task
     */
    public TaskDefinition getTask() {
        return task;
    }

    /**
     * @param task the task to set
     */
    public void setTask(TaskDefinition task) {
        this.task = task;
    }

    /**
     * @return the started
     */
    public Date getStarted() {
        return started;
    }

    /**
     * @param started the started to set
     */
    public void setStarted(Date started) {
        this.started = started;
    }

    /**
     * @return the ended
     */
    public Date getEnded() {
        return ended;
    }

    /**
     * @param ended the ended to set
     */
    public void setEnded(Date ended) {
        this.ended = ended;
    }

    /**
     * @return the first
     */
    public Long getFirst() {
        return first;
    }

    /**
     * @param first the first to set
     */
    public void setFirst(Long first) {
        this.first = first;
    }

    /**
     * @return the last
     */
    public Long getLast() {
        return last;
    }

    /**
     * @param last the last to set
     */
    public void setLast(Long last) {
        this.last = last;
    }

    /**
     * @return the succeeded
     */
    public boolean isSucceeded() {
        return succeeded;
    }

    /**
     * @param succeeded the succeeded to set
     */
    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

	/**
	 * @return the exportedResults
	 */
	public Set<ExportedResult> getExportedResults() {
		return exportedResults;
	}

	/**
	 * @param exportedResults the exportedResults to set
	 */
	public void setExportedResults(Set<ExportedResult> exportedResults) {
		this.exportedResults = exportedResults;
	}

	/**
	 * @return the exportRecipients
	 */
	public Set<ExportRecipient> getExportRecipients() {
		return exportRecipients;
	}

	/**
	 * @param exportRecipients the exportRecipients to set
	 */
	public void setExportRecipients(Set<ExportRecipient> exportRecipients) {
		this.exportRecipients = exportRecipients;
	}
}

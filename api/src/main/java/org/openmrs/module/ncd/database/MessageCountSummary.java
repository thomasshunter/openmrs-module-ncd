package org.openmrs.module.ncd.database;

import java.io.Serializable;
import java.util.Date;

public class MessageCountSummary implements Serializable {

	private static final long serialVersionUID = 6710969667171989894L;

	/** The synthetic primary key */
    private int id;
    /** The date on which the messages that generated these counts were processed,
     *  or null (special row that has message counts for all dates, always has id=1) */
    private Date processedDate;
    /** The count of messages processed on this date */
    private int potentiallyReportable;
    /** The count of messages processed on this date that had at least one
     * candidate result marked "report" by the decided result critic. */ 
    private int decidedResultPositive;
    /** The count of messages processed on this date that had at least one
     * candidate result marked "not-reportable" by the decided result critic. */ 
    private int decidedResultNegative;
    /** The count of messages processed on this date that had at least one
     * candidate result marked "report" by a critic other than the 
     * decided result critic. */ 
    private int criticPositive;
    /** The count of messages processed on this date that had at least one
     * candidate result marked "not-reportable" by a critic other than the 
     * decided result critic. */ 
    private int criticNegative;
    /** The count of messages processed on this date that had at least one
     * candidate result marked "report" with no identified condition. */ 
    private int indeterminate;
    
    public MessageCountSummary() {
    }
    
    public MessageCountSummary(Date processedDate) {
        
        this.processedDate = processedDate;
    }
    
    public String toString() {

        return "MessageCount(" +
                    "id=" + id +
                    ", processedDate=" + processedDate +
                    ", potentiallyReportable=" + potentiallyReportable +
                    ", decidedResultPositive=" + decidedResultPositive +
                    ", decidedResultNegative=" + decidedResultNegative +
                    ", criticPositive=" + criticPositive +
                    ", criticNegative=" + criticNegative +
                    ", indeterminate=" + indeterminate +
               ")";
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the processedDate
     */
    public Date getProcessedDate() {
        return processedDate;
    }

    /**
     * @param processedDate the processedDate to set
     */
    public void setProcessedDate(Date processedDate) {
        this.processedDate = processedDate;
    }

    /**
     * @return the potentiallyReportable
     */
    public int getPotentiallyReportable() {
        return potentiallyReportable;
    }

    /**
     * @param potentiallyReportable the potentiallyReportable to set
     */
    public void setPotentiallyReportable(int potentiallyReportable) {
        this.potentiallyReportable = potentiallyReportable;
    }

    /**
     * @return the decidedResultPositive
     */
    public int getDecidedResultPositive() {
        return decidedResultPositive;
    }

    /**
     * @param decidedResultPositive the decidedResultPositive to set
     */
    public void setDecidedResultPositive(int decidedResultPositive) {
        this.decidedResultPositive = decidedResultPositive;
    }

    /**
     * @return the decidedResultNegative
     */
    public int getDecidedResultNegative() {
        return decidedResultNegative;
    }

    /**
     * @param decidedResultNegative the decidedResultNegative to set
     */
    public void setDecidedResultNegative(int decidedResultNegative) {
        this.decidedResultNegative = decidedResultNegative;
    }

    /**
     * @return the criticPositive
     */
    public int getCriticPositive() {
        return criticPositive;
    }

    /**
     * @param criticPositive the criticPositive to set
     */
    public void setCriticPositive(int criticPositive) {
        this.criticPositive = criticPositive;
    }

    /**
     * @return the criticNegative
     */
    public int getCriticNegative() {
        return criticNegative;
    }

    /**
     * @param criticNegative the criticNegative to set
     */
    public void setCriticNegative(int criticNegative) {
        this.criticNegative = criticNegative;
    }

    /**
     * @return the indeterminate
     */
    public int getIndeterminate() {
        return indeterminate;
    }

    /**
     * @param indeterminate the indeterminate to set
     */
    public void setIndeterminate(int indeterminate) {
        this.indeterminate = indeterminate;
    }
}

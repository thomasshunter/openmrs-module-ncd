package org.openmrs.module.ncd.database.dao;

import java.util.Map;

import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.output.extract.DataFeedLog;
import org.openmrs.module.ncd.output.extract.DataFeedSink;

public interface IExtractDAO {

    /**
     * Extract real export data based on the supplied filtering
     * parameters, and pass the rows to the supplied sink. This version
     * extracts the data via the "old" tables, principally
     * ncd_reportable_result.
     *
     * @param properties The configured properties to control this extract
     * operation, from the TaskDefinition normally.
     * @param feedLog The DataFeedLog to send information about the status
     * of the extract operation to.
     * @param sink The DataFeedSink to which the extracted rows should be
     * passed. 
     * @param status The TaskRunStatus used to record status information
     * about this extract.
     */
    public void extractOld(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status);
    
    /**
     * Extract daily export data based on the supplied filtering
     * parameters, and pass the rows to the supplied sink.
     *
     * @param properties The configured properties to control this extract
     * operation, from the TaskDefinition normally.
     * @param feedLog The DataFeedLog to send information about the status
     * of the extract operation to.
     * @param sink The DataFeedSink to which the extracted rows should be
     * @param status The TaskRunStatus used to record status information
     * about this extract.
     * passed. 
     */
    public void extractDaily(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status);
}
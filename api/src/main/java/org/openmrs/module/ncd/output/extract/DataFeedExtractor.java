package org.openmrs.module.ncd.output.extract;

import java.util.Map;

import org.openmrs.module.ncd.database.TaskRunStatus;

/**
 * The interface presented by a DataFeedExtractor - an object that
 * provides "reportable result" rows fetched from the database, one
 * per call to next(), based on the parameters passed to open().
 */
public interface DataFeedExtractor {

    /** Opens a data feed based on the specified properties */
    public void configure(Map<String, String> properties, DataFeedLog dataFeedLog);
    
    /** Extracts the data feed, and passes each row to supplied sink */
    public void extract(DataFeedSink sink, TaskRunStatus status);
    
    /** Closes the data feed */
    public void close();
}

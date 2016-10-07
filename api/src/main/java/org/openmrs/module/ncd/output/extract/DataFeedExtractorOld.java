package org.openmrs.module.ncd.output.extract;

import java.util.Map;

import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/** An implementation of DataFeedExtractor that extracts from the ncd_reportable_result table. */
public class DataFeedExtractorOld implements DataFeedExtractor {

    /** The configured extract properties */
    private Map<String,String> properties;

    /** The DataFeedLog to send information and errors to */
	private DataFeedLog dataFeedLog;

    public DataFeedExtractorOld() {
    }

    public void configure(Map<String, String> properties, DataFeedLog dataFeedLog) {
    	this.properties = properties;
        this.dataFeedLog = dataFeedLog;
    }
    
    /** Extracts the data feed, and passes each row to supplied sink */
    public void extract(DataFeedSink sink, TaskRunStatus status) {
        
        NCDUtilities.getService().extractOld(properties, dataFeedLog, sink, status);
    }
    
    public void close() {
        // TODO Auto-generated method stub
    }
}

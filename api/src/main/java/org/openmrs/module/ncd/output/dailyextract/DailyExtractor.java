package org.openmrs.module.ncd.output.dailyextract;

import java.util.Map;

import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.output.extract.DataFeedExtractor;
import org.openmrs.module.ncd.output.extract.DataFeedLog;
import org.openmrs.module.ncd.output.extract.DataFeedSink;
import org.openmrs.module.ncd.utilities.NCDUtilities;

public class DailyExtractor implements DataFeedExtractor {

    /** The configured extract properties */
    private Map<String,String> properties;

    /** The DataFeedLog to send information and errors to */
	private DataFeedLog dataFeedLog;

    public DailyExtractor() {
    }

    public void configure(Map<String, String> properties, DataFeedLog dataFeedLog) {
    	this.properties = properties;
        this.dataFeedLog = dataFeedLog;
    }
    
    /** Extracts the data feed, and passes each row to supplied sink */
    public void extract(DataFeedSink sink, TaskRunStatus status) {
        
        NCDUtilities.getService().extractDaily(properties, dataFeedLog, sink, status);
    }
    
    public void close() {
        // TODO Auto-generated method stub
    }
}

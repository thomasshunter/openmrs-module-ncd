package org.openmrs.module.ncd.output.extract;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openmrs.module.ncd.database.TaskRunStatus;

/** An implementation of DataFeedExtractor that generates results out
 * of thin air. 
 */
public class DataFeedExtractorFake implements DataFeedExtractor {

	@SuppressWarnings("unused")
    private DataFeedLog dataFeedLog;
	private Set<String> dateColumns;
	private int rowCount = 100;
	
    public void configure(Map<String, String> properties, DataFeedLog dataFeedLog) {
        
    	this.dataFeedLog = dataFeedLog;
    	
        // TODO Auto-generated method stub
        dateColumns = new HashSet<String>();
        dateColumns.add(DataFeedExtractorFactory.COLUMN_PAT_BIRTH);
        dateColumns.add(DataFeedExtractorFactory.COLUMN_PROVIDER_BIRTH);
        dateColumns.add(DataFeedExtractorFactory.COLUMN_TEST_DATE);
        dateColumns.add(DataFeedExtractorFactory.COLUMN_TEST_RCVD_DATE_TIME);
        dateColumns.add(DataFeedExtractorFactory.COLUMN_TEST_PREVIOUS_DATE);
    }
    
    /** Extracts the data feed, and passes each row to supplied sink */
    public void extract(DataFeedSink sink, TaskRunStatus status) {
        
        Column[] columnNames = DataFeedExtractorFactory.getOutputColumns();

        Date now = new Date();
        Map<String,Object> row = new HashMap<String,Object>(columnNames.length);
        for (int rowNum = 0; rowNum < rowCount; rowNum++) {

            row.clear();
            for (int colNum = 0; colNum < columnNames.length; colNum++) {
                
                String colName = columnNames[colNum].getName();
                if (dateColumns.contains(colName)) {
                    
                    row.put(colName, now);
                }
                else {

                    row.put(colName, "column #" + (rowNum + 1) + "-" + (colNum + 1));
                }
            }
            
            sink.append(row);
        }
    }
    
    public void close() {
        // TODO Auto-generated method stub
    }
}

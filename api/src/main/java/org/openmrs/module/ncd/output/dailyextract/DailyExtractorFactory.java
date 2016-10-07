package org.openmrs.module.ncd.output.dailyextract;

import java.sql.Types;
import java.util.Map;

import org.openmrs.module.ncd.output.extract.Column;
import org.openmrs.module.ncd.output.extract.DataFeedLog;
import org.openmrs.module.ncd.utilities.MapUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.scheduler.TaskDefinition;

public class DailyExtractorFactory {

    // DailyExtractor properties
    //
    // PROP_EXTRACTOR_CLASS: the implementation class to be created by the factory
	// PROP_EXTRACTOR_DAYS: the number of days of records to include in the extract (default is 7 days)
	// PROP_EXTRACTOR_REPORTABLE: the report status to include in the extract (only affects decided results, default is "false" to include negative decided results) 
    // PROP_EXTRACTOR_MAX_ROWS: the maximum number of rows to extract
    public final static String PROP_EXTRACTOR_CLASS = "DailyExtractorFactory.class";
    public final static String PROP_EXTRACTOR_DAYS = "DailyExtractorFactory.days";
    public final static String PROP_EXTRACTOR_REPORTABLE = "DailyExtractorFactory.reportable";
    public final static String PROP_EXTRACTOR_MAX_ROWS = "DailyExtractorFactory.maxRows";

    // The keys in the Map returned by DailyExtractor.next()
    public final static String COLUMN_ROW = "ROW";
    public final static String COLUMN_TYPE = "TYPE"; 				// R = reportable result, D = decided result
    public final static String COLUMN_REPORTABLE = "REPORTABLE";	// Y = reportable, N = not reportable
    public final static String COLUMN_OBR = "OBR";
    public final static String COLUMN_OBR_TEXT = "OBR_TEXT";
    public final static String COLUMN_OBX = "OBX";
    public final static String COLUMN_OBX_TEXT = "OBX_TEXT";
    public final static String COLUMN_TEST_RESULT_VALUE = "TEST_RESULT_VALUE";
    public final static String COLUMN_NTE = "NTE";
    public final static String COLUMN_CONDITION = "CONDITION";
    public final static String COLUMN_LOINC = "LOINC";
    public final static String COLUMN_ADDED = "ADDED";				// (date without time: mm/dd/yyyy)
    public final static String COLUMN_MPQ_SEQ = "MPQ_SEQ";

    private final static Column[] outputColumns = {
        new Column(DailyExtractorFactory.COLUMN_ROW, Types.VARCHAR, 16),
        new Column(DailyExtractorFactory.COLUMN_TYPE, Types.VARCHAR, 1),
        new Column(DailyExtractorFactory.COLUMN_REPORTABLE, Types.VARCHAR, 1),
        new Column(DailyExtractorFactory.COLUMN_OBR, Types.VARCHAR, 10),
        new Column(DailyExtractorFactory.COLUMN_OBR_TEXT, Types.VARCHAR, 80),
        new Column(DailyExtractorFactory.COLUMN_OBX, Types.VARCHAR, 10),
        new Column(DailyExtractorFactory.COLUMN_OBX_TEXT, Types.VARCHAR, 80),
        new Column(DailyExtractorFactory.COLUMN_TEST_RESULT_VALUE, Types.VARCHAR, Integer.MAX_VALUE),
        new Column(DailyExtractorFactory.COLUMN_NTE, Types.VARCHAR, Integer.MAX_VALUE),
        new Column(DailyExtractorFactory.COLUMN_CONDITION, Types.VARCHAR, 80),
        new Column(DailyExtractorFactory.COLUMN_LOINC, Types.VARCHAR, 8),
        new Column(DailyExtractorFactory.COLUMN_ADDED, Types.TIMESTAMP, 0),
        new Column(DailyExtractorFactory.COLUMN_MPQ_SEQ, Types.VARCHAR, 16)
    };
        
    /** Creates and returns an instance of a class implementing
     * DailyExtractor as selected by the properties supplied.
     */
    public static DailyExtractor getInstance(TaskDefinition taskdef, Map<String, String> properties, DataFeedLog dataFeedLog)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        String className = MapUtilities.get(properties, PROP_EXTRACTOR_CLASS, DailyExtractor.class.getName());
        Class<?> clazz = Class.forName(className);
        DailyExtractor inst = (DailyExtractor) clazz.newInstance();
        inst.configure(properties, dataFeedLog);
        
        return inst;
    }

    public static Column[] getOutputColumns() {
        return outputColumns;
    }

    public static String getOutputColumnNames() {
        String[] columnNames = new String[outputColumns.length];
        for (int colNum = 0; colNum < outputColumns.length; colNum++) {
            columnNames[colNum] = outputColumns[colNum].getName();
        }
        return StringUtilities.toCSV(columnNames);
    }
}

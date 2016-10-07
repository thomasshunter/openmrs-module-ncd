package org.openmrs.module.ncd.output.extract;

import java.io.File;
import java.util.Map;

import org.openmrs.module.ncd.utilities.MapUtilities;

public class DataFeedSinkFactory {

    // DataFeedSink properties
    //
    // PROP_SINK_CLASS: the implementation class to be created by the factory
    // PROP_SINK_DATETIME_FORMAT: the date/time format string for formatting date output columns (defaults to MM/dd/yyyy HH:mm:ss)
    // PROP_SINK_OUTPUT_COLS_TO_INCL: CSV of column names to be included in the output (defaults to all columns)
    // PROP_SINK_DATABASE_TABLE_NAME: set to override the output database table name (defaults to REPORTABLE_RESULTS)
    // PROP_SINK_DATABASE_TEMPLATE_PATHNAME: full pathname to the database template file
	// PROP_SINK_STRIP_FOR_RHITS: true to remove message segments from exported HL7 messages, for use in exporting
	// to RHITs. false or null for normal use.
    public final static String PROP_SINK_CLASS = "DataFeedSinkFactory.class";
    public final static String PROP_SINK_DATETIME_FORMAT = "DataFeedSinkFactory.dateTimeFormat";
    public final static String PROP_SINK_OUTPUT_COLS_TO_INCL = "DataFeedSinkFactory.outputColumnsToInclude";
    public final static String PROP_SINK_DATABASE_TABLE_NAME = "DataFeedSinkFactory.databaseTableName";
    public final static String PROP_SINK_DATABASE_TEMPLATE_PATHNAME = "DataFeedSinkFactory.databaseTemplatePathname";
    public final static String PROP_SINK_STRIP_FOR_RHITS = "DataFeedSinkFactory.stripSegmentsForRHITS";

    /** Creates and returns an instance of a class implementing
     * DataFeedSink as selected by the properties supplied.
     */
    public static DataFeedSink getInstance(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        String className = MapUtilities.get(properties, PROP_SINK_CLASS, DataFeedSinkAccess.class.getName());
        Class<?> clazz = Class.forName(className);
        DataFeedSink inst = (DataFeedSink) clazz.newInstance();
        inst.create(tempFile, properties, dataFeedLog);

        return inst;
    }
    
    /**
     * Returns the default date/time format string (suitable for SimpleDateFormat)
     */
    public static String getDefaultDateTimeFormat() {
    	return "MM/dd/yyyy HH:mm:ss";
    }
}

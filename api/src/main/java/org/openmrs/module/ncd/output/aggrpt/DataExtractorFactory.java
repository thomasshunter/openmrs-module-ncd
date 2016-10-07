package org.openmrs.module.ncd.output.aggrpt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.MapUtilities;

public class DataExtractorFactory {

    private static Log logger = LogFactory.getLog(DataExtractorFactory.class);

    public final static String PROP_CUTOFF_DATE = "DataExtractorFactory.cutoffDate";
    public final static String PROP_EXTRACTOR_CLASS = "DataExtractorFactory.class";
    public final static String DATE_BUCKET_COUNT = "DataExtractorFactory.dateBucketCount";
    public final static int DEFAULT_DATE_BUCKET_COUNT = 4;
    private final static DateFormat fmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    /** Creates and returns an instance of a class implementing
     * DataExtractor as selected by the properties supplied.
     */
    public static DataExtractor getInstance(Map<String, String> properties)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
    	String dateTime = properties.get(PROP_CUTOFF_DATE); 
        Date cutoffDateTime = new Date();
        if (dateTime != null && dateTime.length() > 0) {
            try {
            	if (dateTime.indexOf(':') != -1) {
            		cutoffDateTime = fmt.parse(dateTime);
            	}
            	else {
            		cutoffDateTime = fmt.parse(dateTime + " 23:59:59");
            	}
            }
            catch (Exception e) {
	            logger.error("Unexpected exception: " + e.getMessage(), e);
            }
        }
        String className = MapUtilities.get(properties, PROP_EXTRACTOR_CLASS, DataExtractorImpl.class.getName());
        Class<?> clazz = Class.forName(className);
        DataExtractor inst = (DataExtractor) clazz.newInstance();
        inst.configure(cutoffDateTime, properties);

        return inst;
    }
    
    public static void storeCutoffDateTime(Map<String, String> properties, Date cutoffDateTime) {

        properties.put(PROP_CUTOFF_DATE, fmt.format(cutoffDateTime));
    }
}

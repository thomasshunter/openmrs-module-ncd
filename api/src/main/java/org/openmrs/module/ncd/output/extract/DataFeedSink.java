package org.openmrs.module.ncd.output.extract;

import java.io.File;
import java.util.Map;

/**
 * The interface provided by a DataFeedSink - an object which consumes
 * the "reportable results" extracted by a DataFeedExtractor and sends
 * them to a file, perhaps as an MS Access database or delimited file. 
 */
public interface DataFeedSink {

    /** Records the temporary file and records the storage properties */
    public void create(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog);
    
    /** Appends the specified extracted row to the temporary file */
    public void append(Map<String, Object> row);
    
    /** Closes the temporary file */
    public void close();
    
    /** Tests if zero rows were appended */
    public boolean isEmpty();
    
    /** Gets the MIME type for the type of "file" created by this sink */
    public String getMIMEType();
}

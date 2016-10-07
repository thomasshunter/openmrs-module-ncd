package org.openmrs.module.ncd.output.extract;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.StringUtilities;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

/**
 * A DataFeedSink that writes MS access files.
 */
public class DataFeedSinkAccess implements DataFeedSink {

    private static Log logger = LogFactory.getLog(DataFeedSinkAccess.class);
    private final int BUFFERSIZE=50;
    private Map<String, String> properties;
    private DataFeedLog dataFeedLog;
    private String[] outputColumnNames;
	private File outputFile;
	private Database database;
	private Table outputTable;
	private List <Column> outputColumns;
	private Set <String> outputColumnNamesSet;
	private List <Object[]> rowBuffer;
	private boolean empty = true;

	public void create(File tempFile, Map<String, String> properties, DataFeedLog dataFeedLog) 
	{	
        this.empty          = true;
        this.properties     = properties;
        this.dataFeedLog    = dataFeedLog;

		// Record the output column names
        outputColumnNames   = StringUtilities.fromCSV(properties.get(DataFeedSinkFactory.PROP_SINK_OUTPUT_COLS_TO_INCL));
		
		// Record the temporary file
        outputFile          = tempFile;

    	// Determine the table name for REPORTABLE_RESULTS
    	String tableName   = properties.get(DataFeedSinkFactory.PROP_SINK_DATABASE_TABLE_NAME);
    	
    	if (tableName == null) 
    	{
    		tableName = "REPORTABLE_RESULTS";
    	}
    	
    	// Allocate a row buffer for bulk inserts
    	rowBuffer = new ArrayList<Object[]>(BUFFERSIZE);
    	
    	if (hasProperty(DataFeedSinkFactory.PROP_SINK_DATABASE_TEMPLATE_PATHNAME)) 
    	{
    		// Create the database as a copy of the template database
    	    InputStream sourceStream = null;
    	    
	    	try 
	    	{
	
	    		if (!hasProperty(DataFeedSinkFactory.PROP_SINK_DATABASE_TEMPLATE_PATHNAME)) 
	    		{
					dataFeedLog.error("Missing required property: " + DataFeedSinkFactory.PROP_SINK_DATABASE_TEMPLATE_PATHNAME);
					return;
	    		}
	    		
	    		// Create a buffered stream for the source file
	    		sourceStream = new BufferedInputStream(new FileInputStream(properties.get(DataFeedSinkFactory.PROP_SINK_DATABASE_TEMPLATE_PATHNAME)));
	
	    		// Create a buffered stream for the target file
		    	OutputStream targetStream = new BufferedOutputStream(new FileOutputStream(outputFile));
		    	
		    	// Copy from source to target
		    	int theByte;
		    	while ((theByte = sourceStream.read()) != -1) 
		    	{
		    		targetStream.write(theByte);
		    	}
		    	
		    	// Flush, and close the target file
		    	targetStream.flush();
		    	targetStream.close();
		    	
		    	// Open the database, and get the output table and its columns
		    	if ((database = Database.open(new File(outputFile.getPath()))) != null) 
		    	{
			    	if ((outputTable = database.getTable(tableName)) != null) 
			    	{
			    		outputColumns = outputTable.getColumns();
			    	}
			    	else 
			    	{
			    		dataFeedLog.error("Unable to locate table: " + tableName + " in the template database.");
			    	}
		    	}
		    	else 
		    	{
		    		dataFeedLog.error("Unable to open template database: " + outputFile.getPath());
		    	}
	    	}
	    	catch (FileNotFoundException fnfe) 
	    	{
	            logger.error("Unexpected exception: " + fnfe.getMessage(), fnfe);
	    		dataFeedLog.error("Error copying template database.");
	    		dataFeedLog.error(fnfe.toString());
	    		return;
	    	}
	    	catch (IOException ioe) 
	    	{
	            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
	    		dataFeedLog.error("Error copying template database.");
	    		dataFeedLog.error(ioe.toString());
	    		return;
	    	}
	    	finally
	    	{
	    	    try
	    	    {
	    	        sourceStream.close();
	    	    }
	    	    catch( Exception ssE )
	    	    {
	    	        DataFeedSinkAccess.logger.error( "DataFeedSinkAccess.create() threw an Exception while attempting to close sourceStream" );
	    	    }
	    	}
    	}
    	else 
    	{
    		// Create a new database
        	//
        	// It will contain one table: REPORTABLE_RESULTS.
        	// The user can override the default table name with property: PROP_SINK_DATABASE_TABLE_NAME.
        	//
        	// By default, the table will contain the output columns generated by the data extractor.
        	// The user can override the output column set with the property: PROP_SINK_OUTPUT_COLS_TO_INCL.
        	try {
        		// Create the database
        		database = Database.create(outputFile);
        	
    	    	// Get the output columns provided by the data extractor
    	    	org.openmrs.module.ncd.output.extract.Column[] extractorOutputColumns = DataFeedExtractorFactory.getOutputColumns();
    	    	
    	    	// Construct the table and its columns using builder classes
    	    	TableBuilder tableBuilder = new TableBuilder(tableName);
    	    	for (int i=0; i < extractorOutputColumns.length; i++) {
    	    		if (inOutputSet(extractorOutputColumns[i].getName())) {
    	    			ColumnBuilder cb = new ColumnBuilder(extractorOutputColumns[i].getName());
    	    			if (extractorOutputColumns[i].getDatatype() == Types.VARCHAR) {
    						cb.setSQLType(extractorOutputColumns[i].getDatatype(), extractorOutputColumns[i].getLength());
    						if (extractorOutputColumns[i].getLength() < Integer.MAX_VALUE) {
        						cb.setLengthInUnits(extractorOutputColumns[i].getLength());
    						}
    	    			}
    	    			else {
							cb.setSQLType(extractorOutputColumns[i].getDatatype());
    	    			}
        	    		tableBuilder.addColumn(cb.toColumn());
    	    		}
    	    	}
    	    	
    	    	// Create the table and its columns
    	    	if ((outputTable = tableBuilder.toTable(database)) != null) {
		    		outputColumns = outputTable.getColumns();
		    	}
		    	else {
		    		dataFeedLog.error("Unable to create table: " + tableName + " in the created database.");
		    	}
    	    	
        	}
        	catch (SQLException se) {
	            logger.error("Unexpected exception: " + se.getMessage(), se);
        		dataFeedLog.error("Error creating database.");
        		dataFeedLog.error(se.toString());
        		return;
        	}
        	catch (IOException ioe) {
	            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
        		dataFeedLog.error("Error creating database.");
        		dataFeedLog.error(ioe.toString());
        		return;
        	}
        	catch (Exception e) {
	            logger.error("Unexpected exception: " + e.getMessage(), e);
        		dataFeedLog.error("Error creating database.");
        		dataFeedLog.error(e.getMessage());
        		return;
        	}
    	}
    }

    public void append(Map<String, Object> row) {

		this.empty = false;

    	// Construct an array of Object representing the column values to be inserted
    	Object[] columnValues = new Object[outputColumns.size()];
		ListIterator<Column> i = outputColumns.listIterator();
		int n=0;
		while (i.hasNext()) {
			Column col = i.next();
			columnValues[n] = row.get(col.getName());
			
			// If the column is length limited, and this column value is a string value
			if (col.getLengthInUnits() > 0 && columnValues[n] instanceof String) {
				
				// If the value length exceeds the column length, truncate the value
				String value = (String) columnValues[n];
				if (value.length() > col.getLengthInUnits()) {
					columnValues[n] = value.substring(0, col.getLengthInUnits());
				}
			}
			n++;
		}
		
		// Add the row to the row buffer
		rowBuffer.add(columnValues);
		
		// If the buffer is full, flush it
		if (rowBuffer.size() == BUFFERSIZE) {
			flushRowBuffer();
		}
    }

    public void close() {
    	try {
    		flushRowBuffer();
        	database.close();
    	}
    	catch (IOException ioe) {
            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
    		dataFeedLog.error("Error closing Access database.");
    		dataFeedLog.error(ioe.toString());
    	}
    }
    
    public boolean isEmpty() {
    
    	return this.empty;
    }
    
    private void flushRowBuffer() {
    	
    	try {
    		if (rowBuffer.size() > 0) {
    			outputTable.addRows(rowBuffer);
    	    	rowBuffer.clear();
    		}
    	}
    	catch (IOException ioe) {
            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
    		dataFeedLog.error("Error inserting rows into database.");
    		dataFeedLog.error(ioe.toString());
    	}
    }

    private boolean inOutputSet(String columnName) {
    	
    	// One time initialization of the set
    	if (outputColumnNamesSet == null) {
    		outputColumnNamesSet = new HashSet<String>();
        	for (int i=0; i < outputColumnNames.length; i++) {
        		outputColumnNamesSet.add(outputColumnNames[i]);
        	}
    	}
    	
    	return outputColumnNamesSet.contains(columnName);
    }
    
    private boolean hasProperty(String key) {
    	return (properties.get(key) != null && properties.get(key).length() >= 1);
    }
    
    /** Gets the MIME type for the type of "file" created by this sink. */
    public String getMIMEType() {

    	// As is so often the case with Microsoft and standards, this isn't
    	// an official registered IANA MIME type.
    	return "application/x-msaccess";
    }
}

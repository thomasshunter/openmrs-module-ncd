/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.storage;

import java.util.Date;

import org.openmrs.module.ncd.MessageProcessorException;
import org.openmrs.module.ncd.database.Error;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/**
 * @author jlbrown
 *
 */
public class ErrorStorageHelper
{
    
    public enum ErrorLevel
    {
        INFORMATION,
        WARNING,
        ERROR
    }

    public static void storeError(Exception e, RawMessage msgRow, ErrorLevel level) throws RawMessageStorageException
    {
    	// Try to find an existing error for this raw message (msgRow)
    	Error existingError = NCDUtilities.getService().findErrorByRawMessage(msgRow);
    	if (existingError != null) {
    		// Update the existing error for this raw message (msgRow) with latest error information
    		existingError.setLevel(level.toString());
    		existingError.setDescription(e.getMessage() == null ? "No description" : e.getMessage());
    		existingError.setRawMessage(msgRow);
    		existingError.setAdditionalInfo(getAdditionalInfoFromException(e));
    		existingError.setLastErrorDate(new Date());
    		NCDUtilities.getService().updateError(existingError);
    	}
    	else {
    		// Create a new error for this raw message (msgRow)
	        Error errorRow = new Error(msgRow, level.toString(), e.getMessage());        
	        errorRow.setAdditionalInfo(getAdditionalInfoFromException(e)); 
	        if (errorRow.getDescription() == null) {
	        	errorRow.setDescription("Unknown error.  See additional info for more details.");
	        }
	        NCDUtilities.getService().storeError(errorRow);
    	}
    }
    
    public static void storeError(String errorMsg, String additionalInfo, String rawMessage, ErrorLevel level)
    	throws RawMessageStorageException
    {
    	Error errorRow = new Error();
    	errorRow.setDescription(errorMsg);
    	errorRow.setAdditionalInfo(additionalInfo);
    	errorRow.setLevel(level.toString());
    	
    	RawMessage msgRow = RawMessageStorageHelper.createRawHL7(rawMessage);
    	errorRow.setRawMessage(msgRow);
    	NCDUtilities.getService().storeError(errorRow);
    }
    
    public static void updateError(Error error, Exception e, RawMessage msgRow)
    	throws MessageProcessorException {    	
    	if (error != null) {
    		error.setDescription(e.getMessage() == null ? "No description" : e.getMessage());
    		error.setRawMessage(msgRow);
    		error.setAdditionalInfo(getAdditionalInfoFromException(e));
    		error.setLastErrorDate(new Date());
    		NCDUtilities.getService().updateError(error);
    	} else {
    		throw new MessageProcessorException("Could not find the error ID specified.");
    	}
    }
    
    private static String getAdditionalInfoFromException(Exception e) {
    	StringBuilder addedInfo = new StringBuilder();
        Throwable ex = e;
        while (ex != null)
        {
        	addedInfo.append(ex.getClass().getName());
        	if (ex.getMessage() != null) {
	            addedInfo.append(": ");
	            addedInfo.append(ex.getMessage());
        	}
            addedInfo.append("\n");
            for(StackTraceElement stackTraceElem : ex.getStackTrace())
            {
                addedInfo.append(stackTraceElem.toString());
                addedInfo.append("\n");                
            }            
            ex = ex.getCause();
            if (ex != null) {
            	addedInfo.append("Caused by: ");
            }
        }
        return addedInfo.toString();
    }
}

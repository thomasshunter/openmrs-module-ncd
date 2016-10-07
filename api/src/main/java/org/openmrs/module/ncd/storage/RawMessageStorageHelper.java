/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.storage;

import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/**
 * @author jlbrown
 *
 */
public class RawMessageStorageHelper
{
    public static RawMessage createRawHL7(String message) throws RawMessageStorageException
    {
        ConditionDetectorService cdService = NCDUtilities.getService();
        RawMessage rawHL7 = new RawMessage();
        rawHL7.setMessageText(message);        
        if (cdService.saveRawMessage(rawHL7) == null)
        {
            throw new RawMessageStorageException("Failed to store the raw HL7 message");
        }
        return rawHL7;
    }
}

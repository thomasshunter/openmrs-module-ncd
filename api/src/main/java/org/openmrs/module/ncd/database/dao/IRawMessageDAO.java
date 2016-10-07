/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao;

import java.io.Serializable;

import org.openmrs.module.ncd.database.RawMessage;
import org.springframework.transaction.annotation.Transactional;

public interface IRawMessageDAO
{
    /**
     * Persist the raw message to the database.
     * @param rawHL7 The message data to be stored in the form
     * of a RawMessage object.
     * @return A Serializable that contains the stored row.
     */
    public Serializable saveRawMessage(RawMessage rawHL7);
    
    /**
     * Changes the message if something internal to the NCD alters the messages
     * (e.g. one of the pre-processors).  The RawMessage.hl7 field should already
     * be altered to the new message text prior to calling this method.
     * @param rawHL7 The RawMessage row to alter
     */
    public void updateRawMessage(RawMessage rawHL7);
    
    /**
     * Finds the raw message based on the row id.
     * @param id The raw message id which is being looked up.
     * @return The raw message row that contains the
     * specified message.
     */
    @Transactional(readOnly=true)
    public RawMessage findRawMessageById(Long id);
}

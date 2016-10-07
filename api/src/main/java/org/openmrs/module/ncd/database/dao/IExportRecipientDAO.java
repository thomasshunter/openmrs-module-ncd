package org.openmrs.module.ncd.database.dao;

import org.openmrs.module.ncd.database.ExportRecipient;

public interface IExportRecipientDAO {

	/** Add an export recipient.
     * 
     * @param exportRecipient The new export recipient to be added.
     */
    public abstract void addExportRecipient(ExportRecipient exportRecipient);
}

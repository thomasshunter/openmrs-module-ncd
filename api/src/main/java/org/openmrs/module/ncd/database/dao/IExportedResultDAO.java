package org.openmrs.module.ncd.database.dao;

import org.openmrs.module.ncd.database.ExportedResult;

public interface IExportedResultDAO {

	/** Add an exported result.
     * 
     * @param exportedResult The new exported result to be added.
     */
    public abstract void addExportedResult(ExportedResult exportedResult);
}

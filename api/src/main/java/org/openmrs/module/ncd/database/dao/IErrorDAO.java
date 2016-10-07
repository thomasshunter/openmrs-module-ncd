/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao;

import org.openmrs.module.ncd.database.Error;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.database.filter.SearchFilterError;
import org.openmrs.module.ncd.database.filter.SearchResult;

/**
 * @author jlbrown
 *
 */
public interface IErrorDAO
{
    public void storeError(Error errorRow);
    public void updateError(Error errorRow);
    public void deleteError(Error errorRow, String dismissReason);
    public SearchResult<Error> findErrors(SearchFilterError filter);
    public Error findErrorById(Long id);
    public Error findErrorByRawMessage(RawMessage message);
}

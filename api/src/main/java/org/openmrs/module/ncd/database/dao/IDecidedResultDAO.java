/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.database.DecidedResultArchive;
import org.openmrs.module.ncd.database.filter.SearchFilterDecidedResults;
import org.openmrs.module.ncd.database.filter.SearchResult;

public interface IDecidedResultDAO
{
	/** Persists a single decided result and its archived copy, returning
	 * the archived copy.
	 * 
	 * @param result
	 * @return
	 */
	public DecidedResultArchive saveDecidedResult(DecidedResult result);
	
    /**
     * Persists the listed decideds result to the DB.
     * @param decidedResults The decided results to store in the DB.
     * @return A list of copies of the saved DecidedResults with the
     * changes made by the DB.
     */
    public List<DecidedResultArchive> saveDecidedResults(List<DecidedResult> decidedResults);
    
    /**
     * Determine if a "matching" decided result has been seen before so we can
     * reuse the answer.
     * @param decidedResultTemplate The matching decided result to look for.
     * @return A List of the DecidedResult objects that correspond to the
     * specified decidedResultTemplate, or null if no DB rows correspond.
     */
    public List<DecidedResult> findDecidedResults(DecidedResult decidedResultTemplate);
    
    public DecidedResult getDecidedResult(long id);

    public SearchResult<DecidedResult> findDecidedResults(SearchFilterDecidedResults filter);
    
    public void removeDecidedResults(List<DecidedResult> resultsToRemove);
}

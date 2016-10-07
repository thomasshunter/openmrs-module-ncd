package org.openmrs.module.ncd.database.dao;

import java.util.ArrayList;

import org.openmrs.module.ncd.database.ManualReviewStatusType;
import org.openmrs.module.ncd.utilities.Pair;

public interface IReviewStatusDAO {
    
    /**
     * Find a review status type by id
     * @param id
     * @return The ManualReviewStatusType for the specified id.
     */
    public ManualReviewStatusType findReviewStatusTypeById(int id);

    /**
     * Return all review status types
     * @return The review statuses as a ArrayList<Pair<Integer,String>>.
     */
    public ArrayList<Pair<Integer, String>> getReviewStatusTypes();
}

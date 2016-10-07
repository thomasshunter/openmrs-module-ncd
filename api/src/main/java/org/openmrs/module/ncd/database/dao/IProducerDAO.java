/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.filter.SearchFilterProducers;
import org.openmrs.module.ncd.database.filter.SearchResult;

public interface IProducerDAO
{
    /**
     * Find a message source in the database by application and facility names.
     * @param applicationName The application name from the message.
     * @param facilityName The facility name from the message.
     * @return A HL7Producer object that corresponds to the
     * passed in application and facility names.
     */
    public HL7Producer getProducer(
            String applicationName,
            String facilityName,
            String locationName);
    public HL7Producer getProducerExact(String applicationName, 
    		String facilityName, String locationName);

    public HL7Producer getProducer(long id);
    public void saveProducer(HL7Producer src);    
    public void deleteProducer(HL7Producer src);
    public List<HL7Producer> getAllProducers();
	public List<HL7Producer> getAllUnretiredProducers();
	public SearchResult<HL7Producer> findProducers(SearchFilterProducers filter);
}

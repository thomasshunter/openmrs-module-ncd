/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.jurisdiction;

import org.openmrs.module.ncd.database.County;
import org.openmrs.module.ncd.database.Jurisdiction;

/**
 *  Interface for determining jurisdiction and county/region.
 */
public interface JurisdictionAlgorithm {
    // Method used to determine the jurisdiction based on passed in information.
    public Jurisdiction determineJurisdiction(Object... args);
    // Method used to determine the county/region based on passed in information. 
    public County determineCountyOrRegion(Object... args);
}

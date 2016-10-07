package org.openmrs.module.ncd.jurisdiction;

import java.lang.reflect.Array;

import org.openmrs.module.ncd.database.County;
import org.openmrs.module.ncd.database.Jurisdiction;
import org.openmrs.module.ncd.utilities.NCDUtilities;

public class DefaultJurisdictionAlgorithm implements JurisdictionAlgorithm {

    public County determineCountyOrRegion(Object... args) {
        String zipcode = checkArgsAndGetZip(args);                
        
        County county = NCDUtilities.getService().findCountyByZipcode(zipcode);
        
        return county;
    }

    public Jurisdiction determineJurisdiction(Object... args) {        
        String zipcode = checkArgsAndGetZip(args);
        
        Jurisdiction jurisdiction = NCDUtilities.getService().findJurisdictionByZipcode(zipcode);
        
        return jurisdiction;
    }

    private String checkArgsAndGetZip(Object... args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("Too many arguments.");
        }
        String zipcode = (String)Array.get(args, 0);
        return zipcode;
    }
}

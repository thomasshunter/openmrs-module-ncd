package org.openmrs.module.ncd.database.dao;

import org.openmrs.module.ncd.database.County;
import org.openmrs.module.ncd.database.Jurisdiction;

public interface IZipcodeDAO {

    public County findCountyByZipcode(String zipcode);

    public Jurisdiction findJurisdictionByZipcode(String zipcode);

}
/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.Institution;

/**
 *
 */
public interface IInstitutionDAO {

	public Institution findInstitutionByName(String name);
    
    public List<String> getAllInstitutionNames();
	
    public List<Institution> getAllInstitutions();
    
	public List<Institution> getAllActiveInstitutions();
	
    public Institution getInstitution(long id);
    
    public void saveInstitution(Institution institution);
    
    public void deleteInstitution(Institution institution);
}

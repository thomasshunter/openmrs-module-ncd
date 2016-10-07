package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.CodeSystem;
import org.openmrs.module.ncd.database.CodeType;
import org.openmrs.module.ncd.database.filter.SearchFilterCodes;
import org.openmrs.module.ncd.database.filter.SearchResult;

public interface ICodeDAO {

    /**
     * Gets a list containing all defined codes with matching code type and
     * code system.
     * 
     * @param typeName The name of the code type for matching codes.
     * @param systemName The name of the code system for matching codes.
     * @return A list of matching codes, order unspecified.
     */
    public List<Code> findCodes(String typeName, String systemName);
    public List<Code> findCodesExcludeRetired(String typeName, String systemName);

    /**
     * Gets the unique code with the specified code system and code value.
     * 
     * @param systemName The name of the code system for the matching code.
     * @param codeValue The code value of the code for the matching code.
     * @return The matching code, or null if there is no matching code.
     */
    public Code getCode(String systemName, String codeValue);

    /**
     * Gets the unique code with the specified code system and code value.
     * 
     * @param codeSystem The name of the code system for the matching code.
     * @param codeValue The code value of the code for the matching code.
     * @return The matching code, or null if there is no matching code.
     */
    public Code getCode(CodeSystem codeSystem, String codeValue);
    
    /**
     * Gets the specified code type by internal name
     * 
     * @param name The name of the code type to get, such as "diagnosis",
     * "patientsex", etc.
     * @return The named code type, or null if there is no such code type.
     */
    public CodeType getCodeType(String name);
    
    /**
     * Gets the code system with the specified internal name.
     * 
     * @param name The name of the code system to get, such as "I9" or "LN".
     * @return The named code system, or null if there is no such code system.
     */
    public CodeSystem getCodeSystem(String name);

    /** Find codes which match a filter
     * @param filter The search criteria.
     * @return A list of codes that match the search criteria.
     */
    public SearchResult<Code> findCodes(SearchFilterCodes filter);
    
    public List<CodeType> getAllCodeTypes();
    public List<CodeType> getAllCodeTypesExcludeRetired();
    
    /**
     * Gets a list of all the code systems known, in ascending display text
     * order.
     * 
     * @return a List of all the code systems known, in ascending display
     * text order.
     */
    public List<CodeSystem> getAllCodeSystems();
    public List<CodeSystem> getAllCodeSystemsExcludeRetired();
    
    public void saveCode(Code code);
    public void saveCodeSystem(CodeSystem codeSystem);
    public void saveCodeType(CodeType codeType);
    public Code getCode(Long id);
    public CodeSystem getCodeSystem(Long id);
    public CodeType getCodeType(Long id);
}

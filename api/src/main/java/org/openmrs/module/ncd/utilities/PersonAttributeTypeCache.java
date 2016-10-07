package org.openmrs.module.ncd.utilities;

//import java.util.Map;

import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;

/**
 * A cache of PersonAttributeType searchable by name.
 * 
 * @author Erik Horstkotte
 */
public class PersonAttributeTypeCache {

    public static final String PERSON_ATTR_TYPE_RACE = "Race";
    public static final String PERSON_ATTR_TYPE_GLOBAL_PATIENT_ID = "ncd.GlobalPatientId";
    public static final String PERSON_ATTR_TYPE_PATIENT_MEDREC_ID = "ncd.PatientInstitutionMedicalRecordId";
    public static final String PERSON_ATTR_TYPE_PHONE_NUMBER = "ncd.PhoneNumber";
    public static final String PERSON_ATTR_TYPE_SSN = "ncd.SocialSecurityNumber";
    public static final String PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID = "ncd.ProviderLocalId";
    public static final String PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID_SOURCE = "ncd.ProviderLocalIdSource";
    public static final String PERSON_ATTR_TYPE_PROVIDER_NAME_SOURCE = "ncd.ProviderNameSource";
    
    public static final String PERSON_ATTR_TYPE_HEALTH_DEPT_PATIENT_ID = "ncd.HealthDeptPatId";
    public static final String PERSON_ATTR_TYPE_PROVIDER_BIRTH = "ncd.ProviderBirthDate";
    public static final String PERSON_ATTR_TYPE_PROVIDER_DEA_NUM = "ncd.ProviderDEANumber";
    public static final String PERSON_ATTR_TYPE_PROVIDER_FAX = "ncd.ProviderFax";
    public static final String PERSON_ATTR_TYPE_PROVIDER_LICENSE = "ncd.ProviderLicense";
    public static final String PERSON_ATTR_TYPE_PROVIDER_NAME_MATCHED = "ncd.ProviderNameMatched";
    public static final String PERSON_ATTR_TYPE_PROVIDER_PRACTICE = "ncd.ProviderPractice";
    
    //private static Map<String, PersonAttributeType> cache = null;

    /** Finds a PersonAttributeType by its name.
     * 
     * @param name The name of the PersonAttributeType to find.
     * @return The PersonAttributeType corresponding to that name, or null
     * if no such PersonAttributeType is found.
     */
    public static PersonAttributeType find(String name) {

        return Context.getPersonService().getPersonAttributeTypeByName(name);
/*        if (cache == null) {

            synchronized (PERSON_ATTR_TYPE_RACE) {
                
                Context.addProxyPrivilege("View Person Attribute Types");
                try {
                    PersonService personService = Context.getPersonService();
                    List<PersonAttributeType> attrTypeList = personService.getAllPersonAttributeTypes();
                    Map<String, PersonAttributeType> temp = new HashMap<String, PersonAttributeType>(attrTypeList.size());
                    for (PersonAttributeType type : attrTypeList) {
                        temp.put(type.getName(), type);
                    }
                    cache = temp;
                }
                finally {
                    Context.removeProxyPrivilege("View Person Attribute Types");
                }
            }
        }
        
        return cache.get(name);
*/    }

    /**
     * Registers the NCD-specific PersonAttributeTypes if any is missing.
     * TODO: if possible, this should migrate into
     * moduleApplicationContext.xml
     * TODO: if not, this should migrate into a common XML-based solution
     * with module-specific Concepts.
     */
    public static void startup() {

        boolean mapNeedsRefresh = false;
        PersonService personService = Context.getPersonService();
		User ncdUser = NCDUtilities.getNcdUser();

        if (find(PERSON_ATTR_TYPE_GLOBAL_PATIENT_ID) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("A global identifier for a patient. To be migrated to an identifier type when matching issues are resolved.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_GLOBAL_PATIENT_ID);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }

        if (find(PERSON_ATTR_TYPE_PATIENT_MEDREC_ID) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("An institution-specific medical record id for a patient. Probably to be migrated to an identifier type when matching issues are resolved.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PATIENT_MEDREC_ID);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }

        if (find(PERSON_ATTR_TYPE_PHONE_NUMBER) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("A person's phone number.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PHONE_NUMBER);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }

        if (find(PERSON_ATTR_TYPE_SSN) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("A person's Social Security Number.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_SSN);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }
        
        if (find(PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("An identifier assigned to a provider by an institution(?)");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }
        
        if (find(PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID_SOURCE) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("The HL7 field in which the provider's local id was found.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID_SOURCE);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }
        
        if (find(PERSON_ATTR_TYPE_PROVIDER_NAME_SOURCE) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("The HL7 field in which the provider's name was found.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PROVIDER_NAME_SOURCE);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }
        
        if (find(PERSON_ATTR_TYPE_HEALTH_DEPT_PATIENT_ID) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("The identifier assigned to a patient by a health department.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_HEALTH_DEPT_PATIENT_ID);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }
        
        if (find(PERSON_ATTR_TYPE_PROVIDER_BIRTH) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("The date of birth of a Provider.");
            newType.setFormat("java.lang.Date");
            newType.setName(PERSON_ATTR_TYPE_PROVIDER_BIRTH);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }

        if (find(PERSON_ATTR_TYPE_PROVIDER_DEA_NUM) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("The identifier assigned to a Provider by the DEA.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PROVIDER_DEA_NUM);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }

        if (find(PERSON_ATTR_TYPE_PROVIDER_FAX) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("The facsimile phone number for a Provider.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PROVIDER_FAX);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }

        if (find(PERSON_ATTR_TYPE_PROVIDER_LICENSE) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("The state-assigned license number for a Provider.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PROVIDER_LICENSE);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }

        if (find(PERSON_ATTR_TYPE_PROVIDER_NAME_MATCHED) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("The name of a provider after applying the matching algorithm.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PROVIDER_NAME_MATCHED);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }

        if (find(PERSON_ATTR_TYPE_PROVIDER_PRACTICE) == null) {
            
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("The name of a provider's practice.");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_PROVIDER_PRACTICE);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }
        
        if (find(PERSON_ATTR_TYPE_RACE) == null) {
            PersonAttributeType newType = new PersonAttributeType();
            newType.setDescription("A person's race");
            newType.setFormat("java.lang.String");
            newType.setName(PERSON_ATTR_TYPE_RACE);
            newType.setSearchable(true);
            newType.setCreator(ncdUser);
            personService.savePersonAttributeType(newType);
            mapNeedsRefresh = true;
        }
        
        if (mapNeedsRefresh) {
            
            //cache = null;
        }
    }
}

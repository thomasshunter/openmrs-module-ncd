package org.openmrs.module.ncd.utilities;

import java.util.Date;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

public class ObsBuilder {
    
    private Encounter encounter;
    private Obs primaryObs;
    
    public ObsBuilder(Encounter encounter) {
        this.encounter = encounter;
        this.primaryObs = null;
    }

    private Obs makeObs(String conceptName, boolean isPrimary) {
        
        Obs newObs = new Obs();
        
        if (isPrimary) {
            primaryObs = newObs;
        }
        else if (primaryObs != null) {
            primaryObs.addGroupMember(newObs);
            newObs.setObsGroup(primaryObs);
        }
        
        newObs.setConcept(Context.getConceptService().getConcept(conceptName));
        newObs.setCreator(encounter.getCreator());
        newObs.setDateCreated(encounter.getDateCreated());
        newObs.setEncounter(encounter);
        newObs.setLocation(encounter.getLocation());
        newObs.setObsDatetime(encounter.getEncounterDatetime());
        newObs.setPerson(encounter.getPatient());
        return newObs;
    }
    
    public Obs addPrimaryObs(String conceptName) {
        
        Obs newObs = makeObs(conceptName, true); 
        encounter.addObs(newObs);
        return newObs;
    }

    private Obs addObs(String conceptName, String value, boolean isPrimary) {
        
        Obs newObs = makeObs(conceptName, isPrimary); 
        newObs.setValueText(value);
        encounter.addObs(newObs);
        return newObs;
    }
    
    private Obs addObs(String conceptName, Date value, boolean isPrimary) {
        
        Obs newObs = makeObs(conceptName, isPrimary); 
        newObs.setValueDatetime(value);
        encounter.addObs(newObs);
        return newObs;
    }
    
    private Obs addObs(String conceptName, int value, boolean isPrimary) {
        
        Obs newObs = makeObs(conceptName, isPrimary); 
        newObs.setValueNumeric(new Double(value));
        encounter.addObs(newObs);
        return newObs;
    }
    
    private Obs addObs(String conceptName, long value, boolean isPrimary) {
        
        Obs newObs = makeObs(conceptName, isPrimary); 
        newObs.setValueNumeric(new Double(value));
        encounter.addObs(newObs);
        return newObs;
    }
    
    public Obs addPrimaryObs(String conceptName, String value) {
        
        final String trimmedValue = StringUtilities.trim(value);

        return addObs(conceptName, trimmedValue, true);
    }

    public Obs addObs(String conceptName, String value) {
        
        final String trimmedValue = StringUtilities.trim(value);

        if (trimmedValue != null) {
            return addObs(conceptName, trimmedValue, false);
        }
        else {
            return null;
        }
    }

    public Obs addObs(String conceptName, Long value) {
        
        if (value != null) {
            return addObs(conceptName, value.longValue(), false);
        }
        else {
            return null;
        }
    }

    public Obs addObs(String conceptName, Date value) {
        
        if (value != null) {
            return addObs(conceptName, value, false);
        }
        else {
            return null;
        }
    }

    public Obs addObs(String conceptName, int value) {
        
        return addObs(conceptName, value, false);
    }

    public Obs addObs(String conceptName, Character value) {
        
        if (value != null) {
            return addObs(conceptName, value.toString(), false);
        }
        else {
            return null;
        }
    }
}

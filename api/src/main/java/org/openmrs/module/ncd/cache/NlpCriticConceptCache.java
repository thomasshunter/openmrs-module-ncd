package org.openmrs.module.ncd.cache;

import java.util.HashMap;
import java.util.List;

import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.NlpCriticConcept;
import org.openmrs.module.ncd.utilities.NCDUtilities;

/**
 * A cache containing all NlpCriticConcepts currently defined, with methods
 * to list, add, modify and remove them.
 */
public class NlpCriticConceptCache 
{
    private static List<NlpCriticConcept> concepts;
    private static HashMap<String, NlpCriticConcept> conceptMap;

    public static synchronized void flush() 
    {
    	concepts   = null;
    	conceptMap = null;
    }
    
    public static synchronized NlpCriticConcept get(String name) 
    {    
        for (NlpCriticConcept concept : get()) 
        {
            if (concept.getConceptName().equals(name)) 
            {
                return concept;
            }
        }
        
        return null;
    }
    
    public static synchronized List<NlpCriticConcept> get() 
    {    
        if (concepts == null) 
        {    
            ConditionDetectorService cds = NCDUtilities.getService();
            concepts = cds.listNlpCriticConcepts();
        }

        return concepts;
    }

    public static synchronized HashMap<String, NlpCriticConcept> getMap() 
    {    
        if (conceptMap == null) 
        {
            conceptMap                      = new HashMap<String, NlpCriticConcept>();
            List<NlpCriticConcept> concepts = NlpCriticConceptCache.get();
        
            for (NlpCriticConcept concept : concepts)
            {
                if (concept != null)
                {
                    conceptMap.put(concept.getCondition().getDisplayText(), concept);
                }
            }
        }
        
        return conceptMap;
    }
    
    public static synchronized void save(NlpCriticConcept concept) 
    {    
        ConditionDetectorService cds    = NCDUtilities.getService();
        cds.saveNlpCriticConcept(concept);
        concepts                        = null;
        conceptMap                      = null;
    }
    
    public static synchronized void delete(List<String> conceptNames) 
    {    
    	// set the cache members to null to force the get() method to get a current 
    	// list from hibernate before deleting anything
        concepts = null;
        conceptMap = null;
        List<NlpCriticConcept> concepts = get();
     
        for (NlpCriticConcept concept : concepts) 
        {
            if (conceptNames.contains(concept.getConceptName())) 
            {
                delete(concept);
            }
        }
    }

    public static synchronized void delete(NlpCriticConcept concept) 
    {    
        ConditionDetectorService cds    = NCDUtilities.getService();
        cds.deleteNlpCriticConcept(concept);
        concepts                        = null;
        conceptMap                      = null;
    }
}

package org.openmrs.module.ncd.utilities;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

/** Get the value of an Obs using the right getValueXXXX method based on
 * the Obs's Concept (and that Concept's ConceptDatatype). A lot of work
 * to avoid a switch.
 */
public class ObsValueExtractor {

    private static Log logger = LogFactory.getLog(ObsValueExtractor.class);

    private interface ValueExtractor {
        
        public Object getValue(Obs o);
    }
    
    private class DateValueExtractor implements ValueExtractor {
        
        public Object getValue(Obs o) {
            
            return o.getValueDatetime();
        }
    }
    
    private class NumericValueExtractor implements ValueExtractor {
        
        public Object getValue(Obs o) {
            
            return o.getValueNumeric();
        }
    }
    
    private class TextValueExtractor implements ValueExtractor {
        
        public Object getValue(Obs o) {
            
            return o.getValueText();
        }
    }
    
    /** Map from ConceptDatatype to value extractor helper class */
    private static Map<ConceptDatatype, ValueExtractor> extractorByDatatype =
        new HashMap<ConceptDatatype, ValueExtractor>();
    
    public ObsValueExtractor() {
        
        ConceptService conceptService = Context.getConceptService();
        extractorByDatatype.put(conceptService.getConceptDatatypeByName("Date"), new DateValueExtractor());
        extractorByDatatype.put(conceptService.getConceptDatatypeByName("Datetime"), new DateValueExtractor());
        extractorByDatatype.put(conceptService.getConceptDatatypeByName("Numeric"), new NumericValueExtractor());
        extractorByDatatype.put(conceptService.getConceptDatatypeByName("Text"), new TextValueExtractor());
    }
    
    public Object getValue(Obs o) {
        
        ValueExtractor ve =
            extractorByDatatype.get(o.getConcept().getDatatype());
        
        if (ve == null) {

            logger.error("no ValueExtractor class for data type \"" + o.getConcept().getDatatype().getName() + "\".");
            return null;
        }
        else {
            return ve.getValue(o);
        }
    }
}

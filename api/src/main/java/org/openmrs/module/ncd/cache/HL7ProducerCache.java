/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.utilities.StringUtilities;

/**
 * A map from the alternate key for HL7Producer (app, fac, sometimes loc) to
 * HL7Producer instances.
 * 
 * If the loc value in an HL7Producer instance is null, that indicates that
 * *any* sending location value matches that instance. If the loc value is not
 * null, then a sending location value must match exactly.
 * 
 * @author jlbrown
 */
public class HL7ProducerCache
{
	/**
	 * The alternate key for an HL7 producer, with optional location processing
	 * @author eahorstkotte
	 */
	private class ProducerKey {

		private String app;
		private String fac;
		private String loc;

		public ProducerKey(String app, String fac, String loc) {

			this.app = app;
			this.fac = fac;
			this.loc = loc;
		}

		public ProducerKey(HL7Producer producer) {

			this.app = producer.getApplicationname();
			this.fac = producer.getFacilityname();
			this.loc = producer.getLocationname();
		}

		@Override
		public boolean equals(Object obj) {
			
			if (!(obj instanceof ProducerKey)) {
				return false;
			}

			ProducerKey that = (ProducerKey) obj;

			return StringUtilities.equals(this.app, that.app) &&
				   StringUtilities.equals(this.fac, that.fac) &&
				   StringUtilities.equals(this.loc, that.loc);
		}

		@Override
		public int hashCode() {

			int code = 0;
			if (app != null) {
				code = app.hashCode();
			}
			if (fac != null) {
				code += 37 * fac.hashCode();
			}
			if (loc != null) {
				code += 37 * loc.hashCode();
			}
			return code;
		}

		@Override
		public String toString() {
			
			return "ProducerKey(" +
						"app=" + app +
						", fac=" + fac +
						", loc=" + loc +
				   ")";
		}
	}

    private Map<ProducerKey, HL7Producer> producerByAppFacLoc;
    
    public HL7ProducerCache()
    {
        producerByAppFacLoc = new HashMap<ProducerKey, HL7Producer>();
    }
    
    /**
     * Finds a producer with an exactly matching or wildcard matching
     * app/fac/loc combination.
     * 
     * @param app
     * @param fac
     * @param loc
     * @return The matching producer, or null if none exists.
     */
    public HL7Producer find(String app, String fac, String loc)
    {
    	HL7Producer producer = findExact(app, fac, loc);
    	if (producer == null) {
    		producer = findExact(app, fac, null);
    	}
    	return producer;
    }
    
    /**
     * Finds a producer with an exactly matching matching
     * app/fac/loc combination.
     * 
     * @param app
     * @param fac
     * @param loc
     * @return The matching producer, or null if none exists.
     */
    public HL7Producer findExact(String app, String fac, String loc)
    {
        return producerByAppFacLoc.get(new ProducerKey(app, fac, loc));
    }
    
    public void addItem(HL7Producer item)
    {
        producerByAppFacLoc.put(new ProducerKey(item), item);
    }
    
    public void addCollection(List<HL7Producer> producerList)
    {
        for(HL7Producer item : producerList) {
        	
            addItem(item);
        }
    }
}

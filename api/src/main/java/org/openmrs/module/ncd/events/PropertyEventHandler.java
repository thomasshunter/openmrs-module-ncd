package org.openmrs.module.ncd.events;

import org.openmrs.GlobalProperty;

/** The interface contract for global property change handlers registered with
 * a PropertyEventDispatcher. 
 */
public interface PropertyEventHandler {

    /** Called when the handler is registered with the initial value of the global
     * property. 
     */ 
    public void propertyRegistered(GlobalProperty arg0);
    
    /** Called when the value of the global property is changed.
     */ 
    public void propertyChanged(GlobalProperty arg0);
}

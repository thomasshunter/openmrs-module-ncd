package org.openmrs.module.ncd.events;

import org.openmrs.GlobalProperty;

public abstract class SimplePropertyEventHandler implements PropertyEventHandler {

    /** Called when the handler is registered with the initial value of the global
     * property. 
     */ 
    public void propertyRegistered(GlobalProperty arg0) {
        propertyChanged(arg0);
    }
}

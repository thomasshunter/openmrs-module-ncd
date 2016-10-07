package org.openmrs.module.ncd.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A Map-like class that allows multiple values per key.
 * @author Erik Horstkotte
 */
public class MultiMap<K,V> {

    private Map<K,Collection<V>> content = new HashMap<K,Collection<V>>();
    
    /**
     * Add a value for a specified key.
     * 
     * @param key The key to which the value should be added.
     * @param value The value to be added.
     */
    public void add(K key, V value) {
        
        Collection<V> values = get(key);
        if (values == null) {
            values = new ArrayList<V>();
            content.put(key, values);
        }
        values.add(value);
    }
    
    /**
     * Get the values associated with a given key.
     *  
     * @param key
     * @return A Collection of the values recorded for the specified key.
     */
    public Collection<V> get(K key) {
        
        return content.get(key);
    }
    
    /**
     * Tests if at least one value has been recorded for the given key.
     *  
     * @param key
     * @return true if at least one value for the specified key has been
     * recorded, false if not.
     */
    public boolean containsKey(K key) {

        return content.containsKey(key);
    }
}

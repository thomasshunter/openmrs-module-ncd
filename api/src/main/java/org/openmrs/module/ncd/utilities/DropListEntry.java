package org.openmrs.module.ncd.utilities;

public class DropListEntry<T> {

    private T value;
    private String label;
    
    public DropListEntry() {
    }
    
    public DropListEntry(T value, String label) {
        
        this.value = value;
        this.label = label;
    }

    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
}

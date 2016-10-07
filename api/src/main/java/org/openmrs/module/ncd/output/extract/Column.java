package org.openmrs.module.ncd.output.extract;

public class Column {

    private String name;
    private int datatype;
    private int length;

    public Column(String name, int datatype, int length) {
        this.name = name;
        this.datatype = datatype;
        this.length = length;
    }
    
    public String getName() {
        return name;
    }

    public int getDatatype() {
        return datatype;
    }
    
    public int getLength() {
    	return length;
    }
}

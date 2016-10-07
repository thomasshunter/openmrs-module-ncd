package org.openmrs.module.ncd.output.zerocount;

public class InvalidScheduleException extends Exception {
    private static final long serialVersionUID = 1291179540484481046L;
    
    public InvalidScheduleException() {
        super();
    }
    
    public InvalidScheduleException(String message) {
        super(message);
    }
}

package org.openmrs.module.ncd.database.dao;

import java.util.Date;

public interface IProcessedMessageCountDAO {

    public void countProcessedMessage(String application, String facility,
    		String location, Date processedDateTime, String mpqSeqNumber);
}
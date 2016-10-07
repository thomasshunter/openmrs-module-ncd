package org.openmrs.module.ncd.database.dao.hibernate;

import java.util.List;

import org.openmrs.module.ncd.database.dao.ISystemActivityDAO;
import org.openmrs.module.ncd.database.filter.SystemEventFilter;
import org.openmrs.module.ncd.model.SystemActivityEvent;
import org.openmrs.module.ncd.monitor.SystemActivityAppender;

public class SystemActivityDAO implements ISystemActivityDAO {

    public void addSystemEvent(SystemActivityEvent event) {

        SystemActivityAppender.addEvent(event);
    }

    public List<SystemActivityEvent> findSystemEvents(SystemEventFilter filter) {

        // The filter is currently completely ignored.

        return SystemActivityAppender.getEvents();
    }
}

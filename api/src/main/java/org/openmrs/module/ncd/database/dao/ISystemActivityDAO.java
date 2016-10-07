package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.filter.SystemEventFilter;
import org.openmrs.module.ncd.model.SystemActivityEvent;

public interface ISystemActivityDAO {

    public void addSystemEvent(SystemActivityEvent event);

    public List<SystemActivityEvent> findSystemEvents(SystemEventFilter filter);
}

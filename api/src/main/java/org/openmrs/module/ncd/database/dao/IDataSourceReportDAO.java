package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.filter.DataSourceReportFilter;
import org.openmrs.scheduler.TaskDefinition;

public interface IDataSourceReportDAO {

    public List<DataSourceInfo> findDataSourceInfo(TaskDefinition task, DataSourceReportFilter filter);
}
package org.openmrs.module.ncd.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.MonitoredCondition;
import org.openmrs.module.ncd.output.aggrpt.AggregateSummaryReportTask;
import org.openmrs.module.ncd.output.aggrpt.DataExtractorFactory;
import org.openmrs.module.ncd.output.aggrpt.DataExtractorFakeAll;
import org.openmrs.module.ncd.output.aggrpt.DataFormatterFactory;
import org.openmrs.module.ncd.output.aggrpt.DataFormatterImpl;
import org.openmrs.module.ncd.output.aggrpt.ReportSenderEmailImpl;
import org.openmrs.module.ncd.output.aggrpt.ReportSenderFactory;
import org.openmrs.module.ncd.output.condrates.ConditionRateReportTask;
import org.openmrs.module.ncd.output.dailyextract.DailyExtractTask;
import org.openmrs.module.ncd.output.dailyextract.DailyExtractor;
import org.openmrs.module.ncd.output.dailyextract.DailyExtractorFactory;
import org.openmrs.module.ncd.output.datasource.DataSourceReportTask;
import org.openmrs.module.ncd.output.extract.DataFeedExtractorFactory;
import org.openmrs.module.ncd.output.extract.DataFeedSenderFTP;
import org.openmrs.module.ncd.output.extract.DataFeedSenderFactory;
import org.openmrs.module.ncd.output.extract.DataFeedSenderSFTP;
import org.openmrs.module.ncd.output.extract.DataFeedSinkAccess;
import org.openmrs.module.ncd.output.extract.DataFeedSinkDelimited;
import org.openmrs.module.ncd.output.extract.DataFeedSinkFactory;
import org.openmrs.module.ncd.output.extract.ExtractTask;
import org.openmrs.module.ncd.output.zerocount.ZeroCountConditionReportTask;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;
import org.openmrs.scheduler.TaskDefinition;
import org.springframework.validation.BindException;

public class ReportDefinition {

	private Log log = LogFactory.getLog(this.getClass());
	
	// The TaskDefinition used to populate this ReportDefinition
	TaskDefinition taskdef;
	
	// The old state of enabled before the user edited the task
	private Boolean oldEnabled;
	
	// Copy of TaskDefinition fields
	//
	// We have to make a copy, otherwise, if the form modifies the TaskDefinition directly, hibernate
	// will persist those changes to the database even if the changes are canceled or invalid.
	//
	// The following attributes are not copied, and this is intentional, because they are managed
	// in a way other than the form (e.g. internally by OpenMRS, or explicit OpenMRS API calls):
	//	o startOnStartup
	//	o started
	//	o createdBy
	//	o dateCreated
	//	o changedBy
	//	o dateChanged
	//
	private Integer id;
	private String name;
	private String description;
	private String taskClass;
	private Date startTime;
	private String repeatInterval;		// Long in the TaskDefinition
	private String startTimePattern;
	private Map<String, String> properties;

	// Additional form fields
	private String repeatIntervalUnits;
	private Boolean enabled;
	
	// "Shared" fields. These fields appear in more than one DIV (though at most
	// one DIV is visible at a time) and therefore need special processing to
	// work around some strange spring web UI handling.
	private String ftpHost;
	private String ftpPort;
	private String ftpUsername;
	private String ftpPassword;
	private String ftpPasswordRepeat;

	private String sftpHost;
	private String sftpPort;
	private String sftpUsername;
	private String sftpPassword;
	private String sftpPasswordRepeat;

	private String aggregateReportTitle;
    private String aggregateReportTemplate;
    private String aggregateReportTemplatePathname;
    private String aggregateReportSender;
    private String aggregateReportRecipients;
    private String aggregateReportSubject;

    private String conditionRateReportTitle;
    private String conditionRateReportTemplate;
    private String conditionRateReportTemplatePathname;
    private String conditionRateReportSender;
    private String conditionRateReportRecipients;
    private String conditionRateReportSubject;

    private String zeroCountConditionReportTitle;
    private String zeroCountConditionReportTemplate;
    private String zeroCountConditionReportTemplatePathname;
    private String zeroCountConditionReportSender;
    private String zeroCountConditionReportRecipients;
    private String zeroCountConditionReportSubject;
    
    private String dataSourceReportTitle;
    private String dataSourceReportTemplate;
    private String dataSourceReportTemplatePathname;
    private String dataSourceReportSender;
    private String dataSourceReportRecipients;
    private String dataSourceReportSubject;
    
    private String extractSinkClass;						// DataFeedSinkFactory.class
    private String extractDestinationPathname;				// DataFeedSenderFactory.destinationPathname
    private String extractDestinationExtension;				// DataFeedSenderFactory.destinationExtension
    private String extractAddTimestamp;						// DataFeedSenderFactory.addTimestamp
    private String extractTimestampFormat;					// DataFeedSenderFactory.timestampFormat
    private String extractSender;							// DataFeedSenderFactory.sender
    private String extractSuccessAlertList;					// DataFeedSenderFactory.successAlertList
    private String extractErrorAlertList;					// DataFeedSenderFactory.errorAlertList
    private String extractSenderClass;						// DataFeedSenderFactory.class
    private String extractStripForRHITs;					// DataFeedSinkFactory.stripSegmentsForRHITS

    private String dailyExtractDestinationPathname;			// DataFeedSenderFactory.destinationPathname
    private String dailyExtractDestinationExtension;		// DataFeedSenderFactory.destinationExtension
    private String dailyExtractAddTimestamp;				// DataFeedSenderFactory.addTimestamp
    private String dailyExtractTimestampFormat;				// DataFeedSenderFactory.timestampFormat
    private String dailyExtractSender;						// DataFeedSenderFactory.sender
    private String dailyExtractSuccessAlertList;			// DataFeedSenderFactory.successAlertList
    private String dailyExtractErrorAlertList;				// DataFeedSenderFactory.errorAlertList
    private String dailyExtractSenderClass;					// DataFeedSenderFactory.class

    private String conditionRateExportSinkClass;			// DataFeedSinkFactory.class
    private String conditionRateExportDestinationPathname;	// DataFeedSenderFactory.destinationPathname
    private String conditionRateExportDestinationExtension;	// DataFeedSenderFactory.destinationExtension
    private String conditionRateExportAddTimestamp;			// DataFeedSenderFactory.addTimestamp
    private String conditionRateExportTimestampFormat;		// DataFeedSenderFactory.timestampFormat
    private String conditionRateExportSender;				// DataFeedSenderFactory.sender
    private String conditionRateExportSuccessAlertList;		// DataFeedSenderFactory.successAlertList
    private String conditionRateExportErrorAlertList;		// DataFeedSenderFactory.errorAlertList
    private String conditionRateExportSenderClass;			// DataFeedSenderFactory.class
	
    private String zeroCountConditionExportSinkClass;			// DataFeedSinkFactory.class
    private String zeroCountConditionExportDestinationPathname;	// DataFeedSenderFactory.destinationPathname
    private String zeroCountConditionExportDestinationExtension;	// DataFeedSenderFactory.destinationExtension
    private String zeroCountConditionExportAddTimestamp;			// DataFeedSenderFactory.addTimestamp
    private String zeroCountConditionExportTimestampFormat;		// DataFeedSenderFactory.timestampFormat
    private String zeroCountConditionExportSender;				// DataFeedSenderFactory.sender
    private String zeroCountConditionExportSuccessAlertList;		// DataFeedSenderFactory.successAlertList
    private String zeroCountConditionExportErrorAlertList;		// DataFeedSenderFactory.errorAlertList
    private String zeroCountConditionExportSenderClass;			// DataFeedSenderFactory.class
    
    private String dataSourceExportSinkClass;			// DataFeedSinkFactory.class
    private String dataSourceExportDestinationPathname;	// DataFeedSenderFactory.destinationPathname
    private String dataSourceExportDestinationExtension;	// DataFeedSenderFactory.destinationExtension
    private String dataSourceExportAddTimestamp;			// DataFeedSenderFactory.addTimestamp
    private String dataSourceExportTimestampFormat;		// DataFeedSenderFactory.timestampFormat
    private String dataSourceExportSender;				// DataFeedSenderFactory.sender
    private String dataSourceExportSuccessAlertList;		// DataFeedSenderFactory.successAlertList
    private String dataSourceExportErrorAlertList;		// DataFeedSenderFactory.errorAlertList
    private String dataSourceExportSenderClass;			// DataFeedSenderFactory.class
    
    private String newApplication;
    private String newFacility;
    private String newLocation;
    private String newCondition;
    private List<MonitoredCondition> monitoredConditions;
    private List<Long> selectedMonitoredConditions;
	
	// Constants
	private static final int SECONDSPERDAY=60*60*24;
	private static final int SECONDSPERHOUR=60*60;
	private static final int SECONDSPERMINUTE=60;
	
	public ReportDefinition () {
		
		setRepeatInterval("");
		setRepeatIntervalUnits("s");  // default repeat interval units is seconds
		setEnabled(true);
		setFtpHost("");
		setFtpPort("");
		setFtpUsername("");
		setFtpPassword("");
		setFtpPasswordRepeat("");
		setSftpHost("");
		setSftpPort("");
		setSftpUsername("");
		setSftpPassword("");
		setSftpPasswordRepeat("");
		setAggregateReportTitle("");
		setAggregateReportTemplate("");
		setAggregateReportTemplatePathname("");
		setAggregateReportSender("");
		setAggregateReportRecipients("");
		setAggregateReportSubject("");
        setConditionRateReportTitle("");
        setConditionRateReportTemplate("");
        setConditionRateReportTemplatePathname("");
        setConditionRateReportSender("");
        setConditionRateReportRecipients("");
        setConditionRateReportSubject("");
        setZeroCountConditionReportTitle("");
        setZeroCountConditionReportTemplate("");
        setZeroCountConditionReportTemplatePathname("");
        setZeroCountConditionReportSender("");
        setZeroCountConditionReportRecipients("");
        setZeroCountConditionReportSubject("");
        
        setDataSourceReportTitle("");
        setDataSourceReportTemplate("");
        setDataSourceReportTemplatePathname("");
        setDataSourceReportSender("");
        setDataSourceReportRecipients("");
        setDataSourceReportSubject("");

        setExtractDestinationPathname("");
        setExtractDestinationExtension("");
        setExtractAddTimestamp("");
        setExtractTimestampFormat("");
        setExtractSender("");
        setExtractSuccessAlertList("");
        setExtractErrorAlertList("");
        setExtractSenderClass("");
        setExtractStripForRHITs("");
        setDailyExtractDestinationPathname("");
        setDailyExtractDestinationExtension("");
        setDailyExtractAddTimestamp("");
        setDailyExtractTimestampFormat("");
        setDailyExtractSender("");
        setDailyExtractSuccessAlertList("");
        setDailyExtractErrorAlertList("");
        setDailyExtractSenderClass("");
        setConditionRateExportDestinationPathname("");
        setConditionRateExportDestinationExtension("");
        setConditionRateExportAddTimestamp("");
        setConditionRateExportTimestampFormat("");
        setConditionRateExportSender("");
        setConditionRateExportSuccessAlertList("");
        setConditionRateExportErrorAlertList("");
        setConditionRateExportSenderClass("");
        setZeroCountConditionExportDestinationPathname("");
        setZeroCountConditionExportDestinationExtension("");
        setZeroCountConditionExportAddTimestamp("");
        setZeroCountConditionExportTimestampFormat("");
        setZeroCountConditionExportSender("");
        setZeroCountConditionExportSuccessAlertList("");
        setZeroCountConditionExportErrorAlertList("");
        setZeroCountConditionExportSenderClass("");
        setExtractSinkClass("");
        setConditionRateExportSinkClass("");
        setZeroCountConditionExportSinkClass("");
        setNewApplication("");
        setNewFacility("");
        setNewLocation("");
        setNewCondition("");
        setMonitoredConditions(new ArrayList<MonitoredCondition>());
        setSelectedMonitoredConditions(new ArrayList<Long>());
	}

	// Populates the ReportDefinition fields from the TaskDefinition fields
	public void fromTask(TaskDefinition taskdef) {

		// Remember the TaskDefinition
		this.taskdef = taskdef;
		
		// Remember the state of enabled before user edited it		
		oldEnabled = taskdef.getStarted();

		// Deep copy the TaskDefinition fields into the ReportDefinition fields, so form
		// changes that fail validation won't automatically persist in the TaskDefinntion.
		if (taskdef.getId() != null) {
			id = new Integer(taskdef.getId());
		}
		
		if (taskdef.getName() != null) {
			name = new String(taskdef.getName());
		}
		
		if (taskdef.getDescription() != null) {
			description = new String(taskdef.getDescription());
		}
		
		if (taskdef.getStartTime() != null) {
			startTime = new Date(taskdef.getStartTime().getTime());
		}
		
		if (taskdef.getRepeatInterval() != null) {
			if ((taskdef.getRepeatInterval() % SECONDSPERDAY) == 0) {
				repeatIntervalUnits = "d";	// days
				repeatInterval = Long.toString(taskdef.getRepeatInterval() / SECONDSPERDAY); 
			} else if ((taskdef.getRepeatInterval() % SECONDSPERHOUR) == 0) {
				repeatIntervalUnits = "h";	// hours
				repeatInterval = Long.toString(taskdef.getRepeatInterval() / SECONDSPERHOUR); 
			} else if ((taskdef.getRepeatInterval() % SECONDSPERMINUTE) == 0) {
				repeatIntervalUnits = "m";	// minutes
				repeatInterval = Long.toString(taskdef.getRepeatInterval() / SECONDSPERMINUTE); 
			} else {
				repeatIntervalUnits = "s";	// seconds
				repeatInterval = Long.toString(taskdef.getRepeatInterval()); 
			}
		}
		
		if (taskdef.getStartTimePattern() != null) {
			startTimePattern = new String(taskdef.getStartTimePattern()); 
		}
		
		if (taskdef.getStarted() != null && taskdef.getStarted().booleanValue()) {
			enabled = new Boolean(true);
		} else {
			enabled = new Boolean(false);
		}

		if (taskdef.getProperties() != null) {
		    
			properties = new HashMap<String, String>();
			
			for (String key : taskdef.getProperties().keySet()) {
				properties.put(new String(key), new String(taskdef.getProperties().get(key)));
			}
		}

		// Based on the report type, copy the "shared" fields
		if (taskdef.getTaskClass() != null) {
			taskClass = new String(taskdef.getTaskClass());
			
			if (taskClass.equals(AggregateSummaryReportTask.class.getName())) {
			    
	            aggregateReportTitle = copyString(taskdef.getProperty(DataFormatterFactory.PROP_RECIPIENT));
	            aggregateReportTemplate = copyString(taskdef.getProperty(DataFormatterImpl.PROP_TEMPLATE));
	            aggregateReportTemplatePathname = copyString(taskdef.getProperty(DataFormatterImpl.PROP_TEMPLATE_PATHNAME));
	            aggregateReportSender = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_SENDER));
	            aggregateReportRecipients = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_RECIPIENTS));
	            aggregateReportSubject = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_SUBJECT));
			}
			else if (taskClass.equals(ConditionRateReportTask.class.getName())) {
                
			    conditionRateExportSinkClass = copyString(taskdef.getProperty(DataFeedSinkFactory.PROP_SINK_CLASS));

	            if (conditionRateExportSinkClass.equals("Report")) {

	            	conditionRateReportTitle = copyString(taskdef.getProperty(DataFormatterFactory.PROP_RECIPIENT));
	                conditionRateReportTemplate = copyString(taskdef.getProperty(DataFormatterImpl.PROP_TEMPLATE));
	                conditionRateReportTemplatePathname = copyString(taskdef.getProperty(DataFormatterImpl.PROP_TEMPLATE_PATHNAME));
	                conditionRateReportSender = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_SENDER));
	                conditionRateReportRecipients = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_RECIPIENTS));
	                conditionRateReportSubject = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_SUBJECT));
	            }
	            else if (conditionRateExportSinkClass.equals(DataFeedSinkDelimited.class.getName())) {

	            	conditionRateExportDestinationPathname = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME)); 
				    conditionRateExportDestinationExtension = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_EXT)); 
				    conditionRateExportAddTimestamp = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP));
				    conditionRateExportTimestampFormat = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT));
				    conditionRateExportSender = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SENDER));
				    conditionRateExportSuccessAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST));
				    conditionRateExportErrorAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST));
				    conditionRateExportSenderClass = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_CLASS));

	                getFTPProperties(taskdef, conditionRateExportSenderClass);
	            }
            }
			else if (taskClass.equals(ExtractTask.class.getName())) {

			    extractSinkClass = copyString(taskdef.getProperty(DataFeedSinkFactory.PROP_SINK_CLASS));
			    
			    extractDestinationPathname = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME)); 
			    extractDestinationExtension = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_EXT)); 
			    extractAddTimestamp = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP));
			    extractTimestampFormat = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT));
			    extractSender = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SENDER));
			    extractSuccessAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST));
			    extractErrorAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST));
			    extractSenderClass = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_CLASS));
			    extractStripForRHITs = copyString(taskdef.getProperty(DataFeedSinkFactory.PROP_SINK_STRIP_FOR_RHITS));

                getFTPProperties(taskdef, extractSenderClass);
			}
			else if (taskClass.equals(DailyExtractTask.class.getName())) {

			    dailyExtractDestinationPathname = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME)); 
			    dailyExtractDestinationExtension = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_EXT)); 
			    dailyExtractAddTimestamp = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP));
			    dailyExtractTimestampFormat = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT));
			    dailyExtractSender = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SENDER));
			    dailyExtractSuccessAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST));
			    dailyExtractErrorAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST));
			    dailyExtractSenderClass = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_CLASS));

                getFTPProperties(taskdef, dailyExtractSenderClass);
			}
			else if (taskClass.equals(ZeroCountConditionReportTask.class.getName())) {
                
			    zeroCountConditionExportSinkClass = copyString(taskdef.getProperty(DataFeedSinkFactory.PROP_SINK_CLASS));
			    
	            if (zeroCountConditionExportSinkClass.equals("Report")) {
	            	
	                zeroCountConditionReportTitle = copyString(taskdef.getProperty(DataFormatterFactory.PROP_RECIPIENT));
	                zeroCountConditionReportTemplate = copyString(taskdef.getProperty(DataFormatterImpl.PROP_TEMPLATE));
	                zeroCountConditionReportTemplatePathname = copyString(taskdef.getProperty(DataFormatterImpl.PROP_TEMPLATE_PATHNAME));
	                zeroCountConditionReportSender = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_SENDER));
	                zeroCountConditionReportRecipients = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_RECIPIENTS));
	                zeroCountConditionReportSubject = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_SUBJECT));
	            }
	            else if (zeroCountConditionExportSinkClass.equals(DataFeedSinkDelimited.class.getName())) {

	                zeroCountConditionExportDestinationPathname = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME)); 
				    zeroCountConditionExportDestinationExtension = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_EXT)); 
				    zeroCountConditionExportAddTimestamp = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP));
				    zeroCountConditionExportTimestampFormat = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT));
				    zeroCountConditionExportSender = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SENDER));
				    zeroCountConditionExportSuccessAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST));
				    zeroCountConditionExportErrorAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST));
				    zeroCountConditionExportSenderClass = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_CLASS));

	                getFTPProperties(taskdef, zeroCountConditionExportSenderClass);
	            }
            }
			else if (taskClass.equals(DataSourceReportTask.class.getName())) {
                
			    dataSourceExportSinkClass = copyString(taskdef.getProperty(DataFeedSinkFactory.PROP_SINK_CLASS));
			    
	            if (dataSourceExportSinkClass.equals("Report")) {
	            	
	                dataSourceReportTitle = copyString(taskdef.getProperty(DataFormatterFactory.PROP_RECIPIENT));
	                dataSourceReportTemplate = copyString(taskdef.getProperty(DataFormatterImpl.PROP_TEMPLATE));
	                dataSourceReportTemplatePathname = copyString(taskdef.getProperty(DataFormatterImpl.PROP_TEMPLATE_PATHNAME));
	                dataSourceReportSender = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_SENDER));
	                dataSourceReportRecipients = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_RECIPIENTS));
	                dataSourceReportSubject = copyString(taskdef.getProperty(ReportSenderEmailImpl.PROP_SUBJECT));
	            }
	            else if (dataSourceExportSinkClass.equals(DataFeedSinkDelimited.class.getName())) {

	                dataSourceExportDestinationPathname = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME)); 
				    dataSourceExportDestinationExtension = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_DEST_EXT)); 
				    dataSourceExportAddTimestamp = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP));
				    dataSourceExportTimestampFormat = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT));
				    dataSourceExportSender = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SENDER));
				    dataSourceExportSuccessAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST));
				    dataSourceExportErrorAlertList = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST));
				    dataSourceExportSenderClass = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_CLASS));

	                getFTPProperties(taskdef, dataSourceExportSenderClass);
	            }
            }
		}
	}

	// Populates the TaskDefinition from the ReportDefinition fields and returns it
	//
	// IMPORTANT: if changes or additions are made to the reports and the associated
	// configuration parameters, this method must be kept up to date to ensure all report
	// parameters are copied from form members into TaskDefinition properties.
	//
	// NOTE: the user interface page is comprised of <DIV>s, some of which are visible, and
	// others that are invisible.  Regardless of visible state, on page submit, the values
	// of all fields in all <DIV>s are copied into their associated form members.  Based
	// on the report type, and the configured method for extracting, formatting and sending
	// the report data, this method copies only the relevant subset of the form members
	// into the TaskDefinition properties to avoid cluttering it up with a bunch of 
	// irrelevant properties with empty values.
	public TaskDefinition toTask() {

		taskdef.setName(name);
		taskdef.setDescription(description);
		taskdef.setTaskClass(taskClass);
		taskdef.setStartTime(startTime);

		// repeatInterval
		try {
			if (repeatIntervalUnits.equals("d")) {
				taskdef.setRepeatInterval(Long.parseLong(repeatInterval)*SECONDSPERDAY);
			} else if (repeatIntervalUnits.equals("h")) {
				taskdef.setRepeatInterval(Long.parseLong(repeatInterval)*SECONDSPERHOUR);
			} else if (repeatIntervalUnits.equals("m")) {
				taskdef.setRepeatInterval(Long.parseLong(repeatInterval)*SECONDSPERMINUTE);
			} else {
				taskdef.setRepeatInterval(Long.parseLong(repeatInterval));
			}
		}
		catch (NumberFormatException nfe) {
			log.error(nfe);
		}
		
		taskdef.setStartTimePattern(startTimePattern);
		taskdef.getProperties().clear();
		
		// If this is a new report
		if (taskdef.getId() == null) {

			// Ensure startOnStartup member is not null
			if (taskdef.getStartOnStartup() == null) {
				taskdef.setStartOnStartup(false);
			}
		}

        // If it's an aggregate summary report
        if (taskdef.getTaskClass().equals(AggregateSummaryReportTask.class.getName())) {

        	// The sink and sender classes are fixed for this report type
			storeProperty(taskdef, DataFormatterFactory.PROP_FORMATTER_CLASS, DataFormatterImpl.class.getName());
            storeProperty(taskdef, ReportSenderFactory.PROP_SENDER_CLASS, ReportSenderEmailImpl.class.getName());

            storeProperty(taskdef, DataExtractorFactory.PROP_CUTOFF_DATE, properties.get(DataExtractorFactory.PROP_CUTOFF_DATE));
        	storeProperty(taskdef, DataExtractorFactory.DATE_BUCKET_COUNT, properties.get(DataExtractorFactory.DATE_BUCKET_COUNT));
            storeProperty(taskdef, DataFormatterFactory.PROP_RECIPIENT, aggregateReportTitle);
            storeProperty(taskdef, DataFormatterImpl.PROP_TEMPLATE, aggregateReportTemplate);
            storeProperty(taskdef, DataFormatterImpl.PROP_TEMPLATE_PATHNAME, aggregateReportTemplatePathname);
            storeProperty(taskdef, ReportSenderEmailImpl.PROP_SENDER, aggregateReportSender);
            storeProperty(taskdef, ReportSenderEmailImpl.PROP_RECIPIENTS, aggregateReportRecipients);
            storeProperty(taskdef, ReportSenderEmailImpl.PROP_SUBJECT, aggregateReportSubject);
        	storeProperty(taskdef, DataExtractorFakeAll.CONDITION_COUNT, properties.get(DataExtractorFakeAll.CONDITION_COUNT));
        	storeProperty(taskdef, DataExtractorFakeAll.GROUP_COUNT, properties.get(DataExtractorFakeAll.GROUP_COUNT));
        }
        // If it's an unusual condition rate report
        else if (taskdef.getTaskClass().equals(ConditionRateReportTask.class.getName())) {

			storeProperty(taskdef, ConditionRateReportTask.PROP_NAME_CUTOFF_DATETIME, properties.get(ConditionRateReportTask.PROP_NAME_CUTOFF_DATETIME));
			storeProperty(taskdef, ConditionRateReportTask.PROP_NAME_SAMPLE_DAYS, properties.get(ConditionRateReportTask.PROP_NAME_SAMPLE_DAYS));
			storeProperty(taskdef, ConditionRateReportTask.PROP_NAME_HISTORY_DAYS, properties.get(ConditionRateReportTask.PROP_NAME_HISTORY_DAYS));
			storeProperty(taskdef, ConditionRateReportTask.PROP_NAME_LOW_RATE_RATIO, properties.get(ConditionRateReportTask.PROP_NAME_LOW_RATE_RATIO));
			storeProperty(taskdef, ConditionRateReportTask.PROP_NAME_HIGH_RATE_RATIO, properties.get(ConditionRateReportTask.PROP_NAME_HIGH_RATE_RATIO));
            storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_CLASS, conditionRateExportSinkClass);

            if (conditionRateExportSinkClass.equals("Report")) {
            	
                storeProperty(taskdef, DataFormatterFactory.PROP_RECIPIENT, conditionRateReportTitle);
                storeProperty(taskdef, DataFormatterImpl.PROP_TEMPLATE, conditionRateReportTemplate);
                storeProperty(taskdef, DataFormatterImpl.PROP_TEMPLATE_PATHNAME, conditionRateReportTemplatePathname);
                storeProperty(taskdef, ReportSenderEmailImpl.PROP_SENDER, conditionRateReportSender);
                storeProperty(taskdef, ReportSenderEmailImpl.PROP_RECIPIENTS, conditionRateReportRecipients);
                storeProperty(taskdef, ReportSenderEmailImpl.PROP_SUBJECT, conditionRateReportSubject);
            }
            else if (conditionRateExportSinkClass.equals(DataFeedSinkDelimited.class.getName())) {

                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME, conditionRateExportDestinationPathname);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_EXT, conditionRateExportDestinationExtension);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP, conditionRateExportAddTimestamp);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT, conditionRateExportTimestampFormat);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SENDER, conditionRateExportSender);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST, conditionRateExportSuccessAlertList);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST, conditionRateExportErrorAlertList);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_CLASS, conditionRateExportSenderClass);
                
                storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT, properties.get(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT));

                setFTPProperties(taskdef, conditionRateExportSenderClass);
            }
        }
        // Else if it's an extract report
        else if (taskdef.getTaskClass().equals(ExtractTask.class.getName())) {

			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_INST_TO_SEND, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_INST_TO_SEND));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_SEND, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_SEND));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_NOT_SEND, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_NOT_SEND));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_COUNTIES, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_COUNTIES));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_JURISDICTIONS, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_JURISDICTIONS));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_CODES, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_CODES));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_INCREMENTAL, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_INCREMENTAL));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL));
			if (taskdef.getProperties().containsKey(DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL)) {
				storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL_UNITS, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL_UNITS));
			}
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_START_TIME, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_TIME));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_END_TIME, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_TIME));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION1, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION1), "-Choose condition-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION2, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION2), "-Choose condition-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION3, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION3), "-Choose condition-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION4, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION4), "-Choose condition-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION5, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION5), "-Choose condition-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION1, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION1), "-Choose institution-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION2, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION2), "-Choose institution-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION3, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION3), "-Choose institution-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION4, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION4), "-Choose institution-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION5, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION5), "-Choose institution-");
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES1, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES1));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES2, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES2));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES3, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES3));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES4, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES4));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES5, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES5));

			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_CLASS, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_CLASS));
			storeProperty(taskdef, DataFeedExtractorFactory.PROP_EXTRACTOR_MAX_ROWS, properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_MAX_ROWS));
			storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_CLASS, extractSinkClass);
			storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_OUTPUT_COLS_TO_INCL, properties.get(DataFeedSinkFactory.PROP_SINK_OUTPUT_COLS_TO_INCL));
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME, extractDestinationPathname);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_EXT, extractDestinationExtension);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP, extractAddTimestamp);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT, extractTimestampFormat);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SENDER, extractSender);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST, extractSuccessAlertList);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST, extractErrorAlertList);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_CLASS, extractSenderClass);

            if (extractSinkClass.equals(DataFeedSinkDelimited.class.getName())) {
            	storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT, properties.get(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT));
            } else if (extractSinkClass.equals(DataFeedSinkAccess.class.getName())) {
    			storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_DATABASE_TABLE_NAME, properties.get(DataFeedSinkFactory.PROP_SINK_DATABASE_TABLE_NAME));
    			storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_DATABASE_TEMPLATE_PATHNAME, properties.get(DataFeedSinkFactory.PROP_SINK_DATABASE_TEMPLATE_PATHNAME));
    			storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT, properties.get(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT));
            }
			storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_STRIP_FOR_RHITS, extractStripForRHITs);

            setFTPProperties(taskdef, extractSenderClass);
        }
        // Else if it's a daily extract report
        else if (taskdef.getTaskClass().equals(DailyExtractTask.class.getName())) {

        	// The extractor and sink classes are fixed for this report type
			storeProperty(taskdef, DailyExtractorFactory.PROP_EXTRACTOR_CLASS, DailyExtractor.class.getName());
			storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_CLASS, DataFeedSinkDelimited.class.getName());

        	storeProperty(taskdef, DailyExtractorFactory.PROP_EXTRACTOR_DAYS, properties.get(DailyExtractorFactory.PROP_EXTRACTOR_DAYS));
            storeProperty(taskdef, DailyExtractorFactory.PROP_EXTRACTOR_REPORTABLE, properties.get(DailyExtractorFactory.PROP_EXTRACTOR_REPORTABLE), "-Choose report status-");
            
            storeProperty(taskdef, DailyExtractorFactory.PROP_EXTRACTOR_MAX_ROWS, properties.get(DailyExtractorFactory.PROP_EXTRACTOR_MAX_ROWS));
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME, dailyExtractDestinationPathname);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_EXT, dailyExtractDestinationExtension);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP, dailyExtractAddTimestamp);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT, dailyExtractTimestampFormat);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SENDER, dailyExtractSender);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST, dailyExtractSuccessAlertList);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST, dailyExtractErrorAlertList);
            storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_CLASS, dailyExtractSenderClass);

            storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT, properties.get(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT));

            setFTPProperties(taskdef, dailyExtractSenderClass);
        }
        // If it's a zero count condition report
        else if (taskdef.getTaskClass().equals(ZeroCountConditionReportTask.class.getName())) {

        	storeProperty(taskdef, ZeroCountConditionReportTask.PROP_NAME_PERIOD, properties.get(ZeroCountConditionReportTask.PROP_NAME_PERIOD));
        	storeProperty(taskdef, ZeroCountConditionReportTask.PROP_NAME_CUTOFF_DATETIME, properties.get(ZeroCountConditionReportTask.PROP_NAME_CUTOFF_DATETIME));
        	storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_CLASS, zeroCountConditionExportSinkClass);
        	
            if (zeroCountConditionExportSinkClass.equals("Report")) {
            	
                storeProperty(taskdef, DataFormatterFactory.PROP_RECIPIENT, zeroCountConditionReportTitle);
                storeProperty(taskdef, DataFormatterImpl.PROP_TEMPLATE, zeroCountConditionReportTemplate);
                storeProperty(taskdef, DataFormatterImpl.PROP_TEMPLATE_PATHNAME, zeroCountConditionReportTemplatePathname);
                storeProperty(taskdef, ReportSenderEmailImpl.PROP_SENDER, zeroCountConditionReportSender);
                storeProperty(taskdef, ReportSenderEmailImpl.PROP_RECIPIENTS, zeroCountConditionReportRecipients);
                storeProperty(taskdef, ReportSenderEmailImpl.PROP_SUBJECT, zeroCountConditionReportSubject);
            }
            else if (zeroCountConditionExportSinkClass.equals(DataFeedSinkDelimited.class.getName())) {
            	
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME, zeroCountConditionExportDestinationPathname);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_EXT, zeroCountConditionExportDestinationExtension);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP, zeroCountConditionExportAddTimestamp);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT, zeroCountConditionExportTimestampFormat);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SENDER, zeroCountConditionExportSender);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST, zeroCountConditionExportSuccessAlertList);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST, zeroCountConditionExportErrorAlertList);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_CLASS, zeroCountConditionExportSenderClass);
                
                storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT, properties.get(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT));

                setFTPProperties(taskdef, zeroCountConditionExportSenderClass);
            }
        }
        // If it's a data source report
        else if (taskdef.getTaskClass().equals(DataSourceReportTask.class.getName())) {

        	storeProperty(taskdef, DataSourceReportTask.PROP_NAME_PERIOD, properties.get(DataSourceReportTask.PROP_NAME_PERIOD));
        	storeProperty(taskdef, DataSourceReportTask.PROP_NAME_CUTOFF_DATETIME, properties.get(DataSourceReportTask.PROP_NAME_CUTOFF_DATETIME));
        	storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_CLASS, dataSourceExportSinkClass);
        	
            if (dataSourceExportSinkClass.equals("Report")) {
            	
                storeProperty(taskdef, DataFormatterFactory.PROP_RECIPIENT, dataSourceReportTitle);
                storeProperty(taskdef, DataFormatterImpl.PROP_TEMPLATE, dataSourceReportTemplate);
                storeProperty(taskdef, DataFormatterImpl.PROP_TEMPLATE_PATHNAME, dataSourceReportTemplatePathname);
                storeProperty(taskdef, ReportSenderEmailImpl.PROP_SENDER, dataSourceReportSender);
                storeProperty(taskdef, ReportSenderEmailImpl.PROP_RECIPIENTS, dataSourceReportRecipients);
                storeProperty(taskdef, ReportSenderEmailImpl.PROP_SUBJECT, dataSourceReportSubject);
            }
            else if (dataSourceExportSinkClass.equals(DataFeedSinkDelimited.class.getName())) {
            	
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_PATHNAME, dataSourceExportDestinationPathname);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_DEST_EXT, dataSourceExportDestinationExtension);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ADD_TIMESTAMP, dataSourceExportAddTimestamp);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_TIMESTAMP_FORMAT, dataSourceExportTimestampFormat);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SENDER, dataSourceExportSender);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_SUCCESS_ALERT_LIST, dataSourceExportSuccessAlertList);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_ERROR_ALERT_LIST, dataSourceExportErrorAlertList);
                storeProperty(taskdef, DataFeedSenderFactory.PROP_SENDER_CLASS, dataSourceExportSenderClass);
                
                storeProperty(taskdef, DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT, properties.get(DataFeedSinkFactory.PROP_SINK_DATETIME_FORMAT));

                setFTPProperties(taskdef, dataSourceExportSenderClass);
            }
        }

        return taskdef;
	}

    public boolean valid(BindException exceptions) {
    	
    	boolean valid = true;
    	ConditionDetectorService cds = NCDUtilities.getService();

    	// Report Name: required
    	if (isEmptyString(name)) {
            exceptions.rejectValue("name", "ncd.pages.report.error.required.field");
            valid = false;
    	}

    	// Start Time: required
    	if (startTime == null) {
            exceptions.rejectValue("startTime", "ncd.pages.report.error.required.field");
            valid = false;
    	}
    	
    	// Repeat Interval: required
    	if (isEmptyString(repeatInterval)) {
            exceptions.rejectValue("repeatInterval", "ncd.pages.report.error.required.field");
            valid = false;
    	}

    	// Repeat Interval: must be a positive integer
    	if (!isEmptyString(repeatInterval)) {
    		try {
    			long temp = Long.parseLong(repeatInterval);
    			
    			if (temp < 1) {
        			exceptions.rejectValue("repeatInterval", "ncd.pages.report.error.invalid.repeatinterval");
        			valid = false;
    			}
    		} catch (NumberFormatException nfe) {
    			exceptions.rejectValue("repeatInterval", "ncd.pages.report.error.invalid.repeatinterval");
    			valid = false;
    		}
    	}

		if (taskClass.equals(AggregateSummaryReportTask.class.getName())) {

	    	// This is an aggregate summary report
			
			// Sender Email Address: required
	    	if (isEmptyString(aggregateReportSender)) {
	            exceptions.rejectValue("aggregateReportSender", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
			
			// Recipient Email Addresses: required
	    	if (isEmptyString(aggregateReportRecipients)) {
	            exceptions.rejectValue("aggregateReportRecipients", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
			
			// Email Subject: required
	    	if (isEmptyString(aggregateReportSubject)) {
	            exceptions.rejectValue("aggregateReportSubject", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
		}
		else if (taskClass.equals(ConditionRateReportTask.class.getName())) {

            // This is an unusual condition rate report
            
			// If the cutoff date is specified, it must be a valid date or date / time
			if (!validateDateTime(exceptions, ConditionRateReportTask.PROP_NAME_CUTOFF_DATETIME, false)) {
                valid = false;
			}

            // Number of sample days must be an integer in [1, ...)
            if (!validateInteger(exceptions, ConditionRateReportTask.PROP_NAME_SAMPLE_DAYS, 1, Integer.MAX_VALUE, false)) {
                valid = false;
            }

            // Number of history days must be an integer in [1, ...)
            if (!validateInteger(exceptions, ConditionRateReportTask.PROP_NAME_HISTORY_DAYS, 1, Integer.MAX_VALUE, false)) {
                valid = false;
            }

            // Low rate threshold ratio must be a float in [0.0, 1.0]
            if (!validateDouble(exceptions, ConditionRateReportTask.PROP_NAME_LOW_RATE_RATIO, 0.0, 1.0, false)) {
                valid = false;
            }

            // High rate threshold ratio must be a float in [1.0, ...)
            if (!validateDouble(exceptions, ConditionRateReportTask.PROP_NAME_HIGH_RATE_RATIO, 1.0, null, false)) {
                valid = false;
            }

            // If the sink is "Report"
            if (conditionRateExportSinkClass.equals("Report")) {
            	
	            // Sender Email Address: required
	            if (isEmptyString(conditionRateReportSender)) {
	                exceptions.rejectValue("conditionRateReportSender", "ncd.pages.report.error.required.field");
	                valid = false;
	            }
	            
	            // Recipient Email Addresses: required
	            if (isEmptyString(conditionRateReportRecipients)) {
	                exceptions.rejectValue("conditionRateReportRecipients", "ncd.pages.report.error.required.field");
	                valid = false;
	            }
	            
	            // Email Subject: required
	            if (isEmptyString(conditionRateReportSubject)) {
	                exceptions.rejectValue("conditionRateReportSubject", "ncd.pages.report.error.required.field");
	                valid = false;
	            }
            }
            else {
            	
    			// Output Pathname: required
    	    	if (isEmptyString(conditionRateExportDestinationPathname)) {
    	            exceptions.rejectValue("conditionRateExportDestinationPathname", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
    	    	else {
    	    		// Output Pathname: must not end with /, \ or whitespace
    	    		char lastChar = conditionRateExportDestinationPathname.charAt(conditionRateExportDestinationPathname.length()-1); 
    	    		if (lastChar == '\\' || lastChar == '/' || Character.isWhitespace(lastChar)) {
    		            exceptions.rejectValue("conditionRateExportDestinationPathname", "ncd.pages.report.error.invalid.pathname");
    	    			valid = false;
    	    		}
    	    	}
        		
    			// Output Filename Extension: required
    	    	if (isEmptyString(conditionRateExportDestinationExtension)) {
    	            exceptions.rejectValue("conditionRateExportDestinationExtension", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
        		
    			// Sender's Email Address: required
    	    	if (isEmptyString(conditionRateExportSender)) {
    	            exceptions.rejectValue("conditionRateExportSender", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
        		
    			// Error Email Recipients: required
    	    	if (isEmptyString(conditionRateExportErrorAlertList)) {
    	            exceptions.rejectValue("conditionRateExportErrorAlertList", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
    	    	
    	    	if (conditionRateExportSenderClass.equals(DataFeedSenderFTP.class.getName())) {
    	    		
    	    		// This is the FTP transfer method
    		    	if (!validateFTP(exceptions)) {
    		    		valid = false;
    		    	}
    		    	
    	    	} else if (conditionRateExportSenderClass.equals(DataFeedSenderSFTP.class.getName())) {
    	    		
    	    		// This is the SFTP transfer method
    	    		if (!validateSFTP(exceptions)) {
    	    			valid = false;
    	    		}
    	    	}
            }
		}
		else if (taskClass.equals(ExtractTask.class.getName())) {

			// This is an extract report
			
			// Output Pathname: required
	    	if (isEmptyString(extractDestinationPathname)) {
	            exceptions.rejectValue("extractDestinationPathname", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
	    	else {
	    		// Output Pathname: must not end with /, \ or whitespace
	    		char lastChar = extractDestinationPathname.charAt(extractDestinationPathname.length()-1); 
	    		if (lastChar == '\\' || lastChar == '/' || Character.isWhitespace(lastChar)) {
		            exceptions.rejectValue("extractDestinationPathname", "ncd.pages.report.error.invalid.pathname");
	    			valid = false;
	    		}
	    	}
    		
			// Output Filename Extension: required
	    	if (isEmptyString(extractDestinationExtension)) {
	            exceptions.rejectValue("extractDestinationExtension", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
    		
			// Sender's Email Address: required
	    	if (isEmptyString(extractSender)) {
	            exceptions.rejectValue("extractSender", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
    		
			// Error Email Recipients: required
	    	if (isEmptyString(extractErrorAlertList)) {
	            exceptions.rejectValue("extractErrorAlertList", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
	    	
	    	if (extractSenderClass.equals(DataFeedSenderFTP.class.getName())) {
	    		
	    		// This is the FTP transfer method
		    	if (!validateFTP(exceptions)) {
		    		valid = false;
		    	}
		    	
	    	} else if (extractSenderClass.equals(DataFeedSenderSFTP.class.getName())) {
	    		
	    		// This is the SFTP transfer method
	    		if (!validateSFTP(exceptions)) {
	    			valid = false;
	    		}
	    	}
	    	
	    	// Included Conditions - must be exact match on condition name
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_SEND))) {
	    		
	    		String[] keys = StringUtilities.fromCSV(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_SEND));
	    		ArrayList<String> invalidKeys = new ArrayList<String>();
	    		for (String key : keys) {
    				if (cds.findConditionByName(key) == null) {
    					invalidKeys.add(key);
    				}
	    		}
	    		if (invalidKeys.size() > 0) {
	    			Object[] values = new Object[1];
	    			String[] temp = new String[invalidKeys.size()];
	    			values[0] = StringUtilities.toCSV(invalidKeys.toArray(temp));
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.conditionNamesToSend']", "ncd.pages.report.error.invalid.condition", values, null);
	    			valid = false;
	    		}
	    	}
	    	
	    	// Excluded Conditions - must be exact match on condition name
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_NOT_SEND))) {
	    		
	    		String[] keys = StringUtilities.fromCSV(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_NOT_SEND));
	    		ArrayList<String> invalidKeys = new ArrayList<String>();
	    		for (String key : keys) {
    				if (cds.findConditionByName(key) == null) {
    					invalidKeys.add(key);
    				}
	    		}
	    		if (invalidKeys.size() > 0) {
	    			Object[] values = new Object[1];
	    			String[] temp = new String[invalidKeys.size()];
	    			values[0] = StringUtilities.toCSV(invalidKeys.toArray(temp));
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.conditionNamesToNotSend']", "ncd.pages.report.error.invalid.condition", values, null);
	    			valid = false;
	    		}
	    	}
	    	
	    	// Included Institutions - must be exact match on institution name
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_INST_TO_SEND))) {
	    		
	    		String[] keys = StringUtilities.fromCSV(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_INST_TO_SEND));
	    		ArrayList<String> invalidKeys = new ArrayList<String>();
	    		for (String key : keys) {
    				if (cds.findInstitutionByName(key) == null) {
    					invalidKeys.add(key);
    				}
	    		}
	    		if (invalidKeys.size() > 0) {
	    			Object[] values = new Object[1];
	    			String[] temp = new String[invalidKeys.size()];
	    			values[0] = StringUtilities.toCSV(invalidKeys.toArray(temp));
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.institutionsToSend']", "ncd.pages.report.error.invalid.institution", values, null);
	    			valid = false;
	    		}
	    	}
	    	
	    	// Included Counties - must be exact match on county name and state abbreviation
	    	// E.g. Orange(IN),Marion(OH)
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_COUNTIES))) {
	    		
	    		String[] keys = StringUtilities.fromCSV(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_COUNTIES));
	    		ArrayList<String> invalidKeys = new ArrayList<String>();
	    		for (String key : keys) {
	    			int leftParenIndex = key.lastIndexOf('(');
	    			if (leftParenIndex != -1 && key.endsWith(")")) {
	    				String state = key.substring(leftParenIndex + 1, key.length() - 1);
	    				String name = key.substring(0, leftParenIndex);
	    				if (cds.findCountyByNameAndState(name, state) == null) {
	    					invalidKeys.add(key);
	    				}
	    			} else {
    					invalidKeys.add(key);
	    			}
	    		}
	    		if (invalidKeys.size() > 0) {
	    			Object[] values = new Object[1];
	    			String[] temp = new String[invalidKeys.size()];
	    			values[0] = StringUtilities.toCSV(invalidKeys.toArray(temp));
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.counties']", "ncd.pages.report.error.invalid.county", values, null);
	    			valid = false;
	    		}
	    	}
	    	
	    	// Included Jurisdictions - must be exact match on jurisdiction name
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_JURISDICTIONS))) {
	    		
	    		String[] keys = StringUtilities.fromCSV(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_JURISDICTIONS));
	    		ArrayList<String> invalidKeys = new ArrayList<String>();
	    		for (String key : keys) {
    				if (cds.findJurisdictionByName(key) == null) {
    					invalidKeys.add(key);
    				}
	    		}
	    		if (invalidKeys.size() > 0) {
	    			Object[] values = new Object[1];
	    			String[] temp = new String[invalidKeys.size()];
	    			values[0] = StringUtilities.toCSV(invalidKeys.toArray(temp));
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.jurisdictions']", "ncd.pages.report.error.invalid.jurisdiction", values, null);
	    			valid = false;
	    		}
	    	}

	    	// Included Codes - must be at least one match on code and code system
	    	if (!validateCodes(exceptions, 
	    			cds, 
	    			DataFeedExtractorFactory.PROP_EXTRACTOR_CODES, 
	    			"properties['DataFeedExtractorFactory.codes']", 
	    			"ncd.pages.report.error.invalid.code")) {
	    		valid=false;
	    	}
	    	
	    	// Include Only New Results: must be a positive integer
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL))) {
	    		try {
	    			long temp = Long.parseLong(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL));
	    			
	    			if (temp < 1) {
	        			exceptions.rejectValue("properties['DataFeedExtractorFactory.recentResultInterval']", "ncd.pages.report.error.invalid.recentresultinterval");
	        			valid = false;
	    			}
	    		} catch (NumberFormatException nfe) {
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.recentResultInterval']", "ncd.pages.report.error.invalid.recentresultinterval");
	    			valid = false;
	    		}
	    	}
	    	
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	    	dateFormat.setLenient(false);
	    	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	    	timeFormat.setLenient(false);
	    	
	    	// Start date must be the correct format
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE))) {
	    		
	    		try {
	    			dateFormat.parse(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE));
	    		} catch (ParseException pe) {
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.startDate']", "ncd.pages.report.error.invalid.date");
	    			valid = false;
	    		}
	    	}
	    	
	    	// Start time must be the correct format
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_TIME))) {
	    		
	    		try {
	    			timeFormat.parse(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_TIME));
	    		} catch (ParseException pe) {
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.startTime']", "ncd.pages.report.error.invalid.time");
	    			valid = false;
	    		}
	    	}
	    	
	    	// If start time entered, must enter a start date
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_TIME)) &&
	    			isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE))) {
    			exceptions.rejectValue("properties['DataFeedExtractorFactory.startDate']", "ncd.pages.report.error.missing.start.date");
    			valid = false;
	    	}
	    	
	    	// End date must be the correct format
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE))) {
	    		
	    		try {
	    			dateFormat.parse(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE));
	    		} catch (ParseException pe) {
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.endDate']", "ncd.pages.report.error.invalid.date");
	    			valid = false;
	    		}
	    	}
	    	
	    	
	    	// End time must be the correct format
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_TIME))) {
	    		
	    		try {
	    			timeFormat.parse(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_TIME));
	    		} catch (ParseException pe) {
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.endTime']", "ncd.pages.report.error.invalid.time");
	    			valid = false;
	    		}
	    	}
	    	
	    	// If end time entered, must enter an end date
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_TIME)) &&
	    			isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE))) {
    			exceptions.rejectValue("properties['DataFeedExtractorFactory.endDate']", "ncd.pages.report.error.missing.end.date");
    			valid = false;
	    	}
	    	
	    	// End date/time must be after Start date/time
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE)) &&
	    			!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE))) {
	    		
	    		try {

	    			SimpleDateFormat datetimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	    			
	    			Date startDate;
	    	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_TIME))) {
	    	    		startDate = datetimeFormat.parse(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE) + " " + 
	    	    				properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_TIME));
	    	    	} else {
	    	    		startDate = datetimeFormat.parse(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE) + " 00:00:00");
	    	    	}
	    	    	
	    			Date endDate;
	    	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_TIME))) {
	    	    		endDate = datetimeFormat.parse(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE) + " " + 
	    	    				properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_TIME));
	    	    	} else {
	    	    		endDate = datetimeFormat.parse(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE) + " 23:59:59");
	    	    	}
	    	    	
	    	    	if (endDate.before(startDate)) {
	    	    		if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE))) {
	    	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.endDate']", "ncd.pages.report.error.end.date.before.start.date");
	    	    		}
	    	    		if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_TIME))) {
	    	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.endTime']", "ncd.pages.report.error.end.time.before.start.time");
	    	    		}
	        			valid = false;
	    	    	}
	    		} catch (ParseException pe) {
	    			// do nothing
	    		}
	    	}

	    	// Cannot combine multiple date filters
	    	int dateFilterCount=0;
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_INCREMENTAL))) {
	    		dateFilterCount++;
	    	}
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL))) {
	    		dateFilterCount++;
	    	}
	    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE)) ||
	    		!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE))) {
	    		dateFilterCount++;
	    	}
	    	if (dateFilterCount > 1) {
		    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_INCREMENTAL))) {
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.incremental']", "ncd.pages.report.error.cannot.combine.filters");
		    		valid=false;
		    	}
		    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL))) {
	    			exceptions.rejectValue("properties['DataFeedExtractorFactory.recentResultInterval']", "ncd.pages.report.error.cannot.combine.filters");
		    		valid=false;
		    	}
		    	if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE)) ||
		    		!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE))) {
		    		if (!isEmptyString(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE))) {
		    			exceptions.rejectValue("properties['DataFeedExtractorFactory.startDate']", "ncd.pages.report.error.cannot.combine.filters");
		    			valid=false;
		    		} else {
		    			exceptions.rejectValue("properties['DataFeedExtractorFactory.endDate']", "ncd.pages.report.error.cannot.combine.filters");
		    			valid=false;
		    		}
		    	}
	    	}
	    	
	    	// Exclude from Institution
	    	if (!validateExcludeFromInstitution(exceptions, cds,
	    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION1,
	    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES1,
	    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION1,
	    				"properties['DataFeedExtractorFactory.excludeCondition1']",
	    				"properties['DataFeedExtractorFactory.excludeCodes1']",
	    				"properties['DataFeedExtractorFactory.excludeInstitution1']")) {
	    		valid=false;	    		
	    	}
	    	
	    	if (!validateExcludeFromInstitution(exceptions, cds,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION2,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES2,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION2,
    				"properties['DataFeedExtractorFactory.excludeCondition2']",
    				"properties['DataFeedExtractorFactory.excludeCodes2']",
    				"properties['DataFeedExtractorFactory.excludeInstitution2']")) {
	    		valid=false;	    		
	    	}
	    	
	    	if (!validateExcludeFromInstitution(exceptions, cds,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION3,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES3,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION3,
    				"properties['DataFeedExtractorFactory.excludeCondition3']",
    				"properties['DataFeedExtractorFactory.excludeCodes3']",
    				"properties['DataFeedExtractorFactory.excludeInstitution3']")) {
	    		valid=false;	    		
	    	}
	    	
	    	if (!validateExcludeFromInstitution(exceptions, cds,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION4,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES4,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION4,
    				"properties['DataFeedExtractorFactory.excludeCondition4']",
    				"properties['DataFeedExtractorFactory.excludeCodes4']",
    				"properties['DataFeedExtractorFactory.excludeInstitution4']")) {
	    		valid=false;	    		
	    	}
	    	
	    	if (!validateExcludeFromInstitution(exceptions, cds,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION5,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES5,
    				DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION5,
    				"properties['DataFeedExtractorFactory.excludeCondition5']",
    				"properties['DataFeedExtractorFactory.excludeCodes5']",
    				"properties['DataFeedExtractorFactory.excludeInstitution5']")) {
	    		valid=false;	    		
	    	}
	    	
	    	if (!extractStripForRHITs.isEmpty() && !"true".equals(extractStripForRHITs) && !"false".equals(extractStripForRHITs)) {
	            exceptions.rejectValue("extractStripForRHITs", "ncd.pages.report.error.invalid.boolean");
	    		valid = false;
	    	}
		}
		else if (taskClass.equals(DailyExtractTask.class.getName())) {

			// This is a daily extract report
			
			// Output Pathname: required
	    	if (isEmptyString(dailyExtractDestinationPathname)) {
	            exceptions.rejectValue("dailyExtractDestinationPathname", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
	    	else {
	    		// Output Pathname: must not end with /, \ or whitespace
	    		char lastChar = dailyExtractDestinationPathname.charAt(dailyExtractDestinationPathname.length()-1); 
	    		if (lastChar == '\\' || lastChar == '/' || Character.isWhitespace(lastChar)) {
		            exceptions.rejectValue("dailyExtractDestinationPathname", "ncd.pages.report.error.invalid.pathname");
	    			valid = false;
	    		}
	    	}
    		
			// Output Filename Extension: required
	    	if (isEmptyString(dailyExtractDestinationExtension)) {
	            exceptions.rejectValue("dailyExtractDestinationExtension", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
    		
			// Sender's Email Address: required
	    	if (isEmptyString(dailyExtractSender)) {
	            exceptions.rejectValue("dailyExtractSender", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
    		
			// Error Email Recipients: required
	    	if (isEmptyString(dailyExtractErrorAlertList)) {
	            exceptions.rejectValue("dailyExtractErrorAlertList", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
	    	
	    	if (dailyExtractSenderClass.equals(DataFeedSenderFTP.class.getName())) {
	    		
	    		// This is the FTP transfer method
	    		if (!validateFTP(exceptions)) {
	    			valid = false;
	    		}
		    	
	    	} else if (dailyExtractSenderClass.equals(DataFeedSenderSFTP.class.getName())) {
	    		
	    		// This is the SFTP transfer method
	    		if (!validateSFTP(exceptions)) {
	    			valid = false;
	    		}
	    	}
	    	
            // Number of days must be an integer in [1, 7]
            if (!validateInteger(exceptions, DailyExtractorFactory.PROP_EXTRACTOR_DAYS, 1, 7, false)) {
                valid = false;
            }
		}
		else if (taskClass.equals(ZeroCountConditionReportTask.class.getName())) {

            // This is a zero count condition report/export

			// Validate the sampling period (int, >0, optional)
            if (!validateInteger(exceptions, ZeroCountConditionReportTask.PROP_NAME_PERIOD, 1, null, false)) {
                valid = false;
            }
            
			// Validate the cutoff date/time (date/time, past, optional)
            if (!validateDateTime(exceptions, ZeroCountConditionReportTask.PROP_NAME_CUTOFF_DATETIME, false)) {
                valid = false;
            }

            // If the sink is "Report"
            if (zeroCountConditionExportSinkClass.equals("Report")) {
            	
	            // Sender Email Address: required
	            if (isEmptyString(zeroCountConditionReportSender)) {
	                exceptions.rejectValue("zeroCountConditionReportSender", "ncd.pages.report.error.required.field");
	                valid = false;
	            }
	            
	            // Recipient Email Addresses: required
	            if (isEmptyString(zeroCountConditionReportRecipients)) {
	                exceptions.rejectValue("zeroCountConditionReportRecipients", "ncd.pages.report.error.required.field");
	                valid = false;
	            }
	            
	            // Email Subject: required
	            if (isEmptyString(zeroCountConditionReportSubject)) {
	                exceptions.rejectValue("zeroCountConditionReportSubject", "ncd.pages.report.error.required.field");
	                valid = false;
	            }
            }
            else {
            	
    			// Output Pathname: required
    	    	if (isEmptyString(zeroCountConditionExportDestinationPathname)) {
    	            exceptions.rejectValue("zeroCountConditionExportDestinationPathname", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
    	    	else {
    	    		// Output Pathname: must not end with /, \ or whitespace
    	    		char lastChar = zeroCountConditionExportDestinationPathname.charAt(zeroCountConditionExportDestinationPathname.length()-1); 
    	    		if (lastChar == '\\' || lastChar == '/' || Character.isWhitespace(lastChar)) {
    		            exceptions.rejectValue("zeroCountConditionExportDestinationPathname", "ncd.pages.report.error.invalid.pathname");
    	    			valid = false;
    	    		}
    	    	}
        		
    			// Output Filename Extension: required
    	    	if (isEmptyString(zeroCountConditionExportDestinationExtension)) {
    	            exceptions.rejectValue("zeroCountConditionExportDestinationExtension", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
        		
    			// Sender's Email Address: required
    	    	if (isEmptyString(zeroCountConditionExportSender)) {
    	            exceptions.rejectValue("zeroCountConditionExportSender", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
        		
    			// Error Email Recipients: required
    	    	if (isEmptyString(zeroCountConditionExportErrorAlertList)) {
    	            exceptions.rejectValue("zeroCountConditionExportErrorAlertList", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
    	    	
    	    	if (zeroCountConditionExportSenderClass.equals(DataFeedSenderFTP.class.getName())) {
    	    		
    	    		// This is the FTP transfer method
    		    	if (!validateFTP(exceptions)) {
    		    		valid = false;
    		    	}
    		    	
    	    	} else if (zeroCountConditionExportSenderClass.equals(DataFeedSenderSFTP.class.getName())) {
    	    		
    	    		// This is the SFTP transfer method
    	    		if (!validateSFTP(exceptions)) {
    	    			valid = false;
    	    		}
    	    	}
            }
		}
		else if (taskClass.equals(DataSourceReportTask.class.getName())) {

            // This is a zero count condition report/export

			// Validate the sampling period (int, >0, optional)
            if (!validateInteger(exceptions, DataSourceReportTask.PROP_NAME_PERIOD, 1, null, false)) {
                valid = false;
            }
            
			// Validate the cutoff date/time (date/time, past, optional)
            if (!validateDateTime(exceptions, DataSourceReportTask.PROP_NAME_CUTOFF_DATETIME, false)) {
                valid = false;
            }

            // If the sink is "Report"
            if (dataSourceExportSinkClass.equals("Report")) {
            	
	            // Sender Email Address: required
	            if (isEmptyString(dataSourceReportSender)) {
	                exceptions.rejectValue("dataSourceReportSender", "ncd.pages.report.error.required.field");
	                valid = false;
	            }
	            
	            // Recipient Email Addresses: required
	            if (isEmptyString(dataSourceReportRecipients)) {
	                exceptions.rejectValue("dataSourceReportRecipients", "ncd.pages.report.error.required.field");
	                valid = false;
	            }
	            
	            // Email Subject: required
	            if (isEmptyString(dataSourceReportSubject)) {
	                exceptions.rejectValue("dataSourceReportSubject", "ncd.pages.report.error.required.field");
	                valid = false;
	            }
            }
            else {
            	
    			// Output Pathname: required
    	    	if (isEmptyString(dataSourceExportDestinationPathname)) {
    	            exceptions.rejectValue("dataSourceExportDestinationPathname", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
    	    	else {
    	    		// Output Pathname: must not end with /, \ or whitespace
    	    		char lastChar = dataSourceExportDestinationPathname.charAt(dataSourceExportDestinationPathname.length()-1); 
    	    		if (lastChar == '\\' || lastChar == '/' || Character.isWhitespace(lastChar)) {
    		            exceptions.rejectValue("dataSourceExportDestinationPathname", "ncd.pages.report.error.invalid.pathname");
    	    			valid = false;
    	    		}
    	    	}
        		
    			// Output Filename Extension: required
    	    	if (isEmptyString(dataSourceExportDestinationExtension)) {
    	            exceptions.rejectValue("dataSourceExportDestinationExtension", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
        		
    			// Sender's Email Address: required
    	    	if (isEmptyString(dataSourceExportSender)) {
    	            exceptions.rejectValue("dataSourceExportSender", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
        		
    			// Error Email Recipients: required
    	    	if (isEmptyString(dataSourceExportErrorAlertList)) {
    	            exceptions.rejectValue("dataSourceExportErrorAlertList", "ncd.pages.report.error.required.field");
    	            valid = false;
    	    	}
    	    	
    	    	if (dataSourceExportSenderClass.equals(DataFeedSenderFTP.class.getName())) {
    	    		
    	    		// This is the FTP transfer method
    		    	if (!validateFTP(exceptions)) {
    		    		valid = false;
    		    	}
    		    	
    	    	} else if (dataSourceExportSenderClass.equals(DataFeedSenderSFTP.class.getName())) {
    	    		
    	    		// This is the SFTP transfer method
    	    		if (!validateSFTP(exceptions)) {
    	    			valid = false;
    	    		}
    	    	}
            }
		}
	    	
    	return valid;
    }
    
    private boolean isEmptyString(String s) {
    	return (StringUtils.isEmpty(s) || StringUtils.isEmpty(s.trim()));
    }
    
    private boolean isEmptyString(String s, String ignoredValue) {
    	return (StringUtils.isEmpty(s) || StringUtils.isEmpty(s.trim()) || s.equals(ignoredValue));
    }
    
    private boolean validateFTP(BindException exceptions) {
    	
    	boolean valid = true;
    	
		// FTP Host: required
    	if (isEmptyString(ftpHost)) {
            exceptions.rejectValue("ftpHost", "ncd.pages.report.error.required.field");
            valid = false;
    	}

    	// If there is a FTP password
    	if (!isEmptyString(ftpPassword)) {
	    	
    		// Repeat FTP Password: required
	    	if (isEmptyString(ftpPasswordRepeat)) {
	            exceptions.rejectValue("ftpPasswordRepeat", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
	    	else {
	    	
		    	// FTP Password and Repeat FTP Password: must be equal
		    	if (!ftpPassword.equals(ftpPasswordRepeat)) {
		            exceptions.rejectValue("ftpPassword", "ncd.pages.report.error.password.mismatch");
		            exceptions.rejectValue("ftpPasswordRepeat", "ncd.pages.report.error.password.mismatch");
		            valid = false;
		    	}
	    	}
    	}
    	else {
    		
    		// If there is a Repeat FTP Password
    		if (!isEmptyString(ftpPasswordRepeat)) {

    			// Repeat FTP Password: required
		    	if (isEmptyString(ftpPassword)) {
		            exceptions.rejectValue("ftpPassword", "ncd.pages.report.error.required.field");
		            valid = false;
		    	}
    		}
    	}
    	
    	return valid;
    }
    
    private boolean validateSFTP(BindException exceptions) {
    	
    	boolean valid = true;
    	
		// SFTP Host: required
    	if (isEmptyString(sftpHost)) {
            exceptions.rejectValue("sftpHost", "ncd.pages.report.error.required.field");
            valid = false;
    	}
    	
		// SFTP Username: required
    	if (isEmptyString(sftpUsername)) {
            exceptions.rejectValue("sftpUsername", "ncd.pages.report.error.required.field");
            valid = false;
    	}
    	
		// SFTP Password: required
    	if (isEmptyString(sftpPassword)) {
            exceptions.rejectValue("sftpPassword", "ncd.pages.report.error.required.field");
            valid = false;
    	}
    	else {
    	
    		// Repeat SFTP Password: required
	    	if (isEmptyString(sftpPasswordRepeat)) {
	            exceptions.rejectValue("sftpPasswordRepeat", "ncd.pages.report.error.required.field");
	            valid = false;
	    	}
	    	else {
		    	// SFTP Password and Repeat SFTP Password: must be equal
		    	if (!sftpPassword.equals(sftpPasswordRepeat)) {
		            exceptions.rejectValue("sftpPassword", "ncd.pages.report.error.password.mismatch");
		            exceptions.rejectValue("sftpPasswordRepeat", "ncd.pages.report.error.password.mismatch");
		            valid = false;
		    	}
	    	}
    	}

    	return valid;
    }
    	
    private boolean validateCodes(BindException exceptions, ConditionDetectorService cds, String propertyName, String formPath, String messageKey) {
    	
    	boolean valid=true;
    	if (!isEmptyString(properties.get(propertyName))) {
    		
    		String[] keys = StringUtilities.fromCSV(properties.get(propertyName));
    		ArrayList<String> invalidKeys = new ArrayList<String>();
    		for (String key : keys) {
    			int leftParenIndex = key.lastIndexOf('(');
    			if (leftParenIndex != -1 && key.endsWith(")")) {
    				String system = key.substring(leftParenIndex + 1, key.length() - 1);
    				String code = key.substring(0, leftParenIndex);
    				List<CodeCondition> matches = cds.findByCodeAndSystem(code, system); 
    				if (matches == null || matches.size() == 0) {
    					invalidKeys.add(key);
    				}
    			} else {
					invalidKeys.add(key);
    			}
    		}
    		if (invalidKeys.size() > 0) {
    			Object[] values = new Object[1];
    			String[] temp = new String[invalidKeys.size()];
    			values[0] = StringUtilities.toCSV(invalidKeys.toArray(temp));
    			exceptions.rejectValue(formPath, messageKey, values, null);
    			valid = false;
    		}
    	}

    	return valid;
    }

    private boolean validateExcludeFromInstitution(BindException exceptions, 
    		ConditionDetectorService cds, 
    		String conditionPropertyName, 
    		String codesPropertyName, 
    		String institutionPropertyName, 
    		String conditionFormPath, 
    		String codesFormPath, 
    		String institutionFormPath) {
    	
    	boolean valid=true;

    	// Code and code system must match
    	if (!validateCodes(exceptions,
    			cds, 
    			codesPropertyName, 
    			codesFormPath, 
    			"ncd.pages.report.error.invalid.code")) {
    		valid=false;
    	}
    	
    	// Must select institution, and condition or code(s)
    	if (!isEmptyString(properties.get(conditionPropertyName), "-Choose condition-") ||
	    		!isEmptyString(properties.get(codesPropertyName)) ||
		    	!isEmptyString(properties.get(institutionPropertyName), "-Choose institution-")) {
	    		
    		if (isEmptyString(properties.get(institutionPropertyName), "-Choose institution-")) {
    			exceptions.rejectValue(institutionFormPath, "ncd.pages.report.error.choose.institution");
    			valid = false;
    		}
    		
    		if ((isEmptyString(properties.get(conditionPropertyName), "-Choose condition-") &&
    	    	 isEmptyString(properties.get(codesPropertyName))) ||
    	    	 (!isEmptyString(properties.get(conditionPropertyName), "-Choose condition-") &&
    	    	  !isEmptyString(properties.get(codesPropertyName)))) {
    			exceptions.rejectValue(conditionFormPath, "ncd.pages.report.error.condition.or.codes");
    			exceptions.rejectValue(codesFormPath, "ncd.pages.report.error.condition.or.codes");
    			valid = false;
    		}
	    }

    	return valid;
    }

    /**
     * Validate a Date-valued property.
     * 
     * 1. If the property is missing but required, complain.
     * 2. If the property is parseable as a date and time, accept.
     * 3. complain.
     * 
     * @param exceptions
     * @param propname
     * @param required
     * @param timeRequired
     * @return
     */
    private boolean validateDateTime(BindException exceptions, String propname, 
            boolean required)
    {
        String propValue = properties.get(propname);
        String propPath = "properties['" + propname + "']";
        if (isEmptyString(propValue)) {
            
            if (required) {
                exceptions.rejectValue(propPath, "ncd.pages.report.error.required.field");
                return false;
            }
            
            return true;
        }

        Date value = DateUtilities.parseDateTime(propValue);
        if (value != null) {
        	
        	return true;
        }
        else {
        	
            exceptions.rejectValue(propPath, "ncd.pages.report.error.invalid.datetime");
            return false;
        }
    }

    /**
     * Validate an integer-valued property.
     * 
     * 1. If the property is missing but required, complain.
     * 2. If the property is present but not parseable as an integer, complain.
     * 3. If the parsed property value is too low, complain.
     * 4. If the parsed property value is too high, complain.
     * 
     * @param exceptions
     * @param propname
     * @param min
     * @param max
     * @param required
     * @return
     */
    private boolean validateInteger(BindException exceptions, String propname, 
            Integer min, Integer max, boolean required)
    {
        String propValue = properties.get(propname);
        String propPath = "properties['" + propname + "']";
        if (isEmptyString(propValue)) {
            
            if (required) {
                exceptions.rejectValue(propPath, "ncd.pages.report.error.required.field");
                return false;
            }
            
            return true;
        }
        
        int v;
        try {
            
            v = Integer.parseInt(propValue);
        }
        catch (Exception e) {
            
            exceptions.rejectValue(propPath, "ncd.pages.report.error.invalid.integer");
            return false;
        }
        
        if (min != null && v < min) {
            
            exceptions.rejectValue(propPath, "ncd.pages.report.error.invalid.toolow", new Object[] { min }, null);
            return false;
        }
        
        if (max != null && v > max) {
            
            exceptions.rejectValue(propPath, "ncd.pages.report.error.invalid.toohigh", new Object[] { max }, null);
            return false;
        }
        
        return true;
    }
    
    private boolean validateDouble(BindException exceptions, String propname, 
            Double min, Double max, boolean required)
    {
        String propValue = properties.get(propname);
        String propPath = "properties['" + propname + "']";
        if (isEmptyString(propValue)) {
            
            if (required) {
                exceptions.rejectValue(propPath, "ncd.pages.report.error.required.field");
                return false;
            }
            
            return true;
        }
        
        Double v;
        try {
            
            v = Double.parseDouble(propValue);
        }
        catch (Exception e) {
            
            exceptions.rejectValue(propPath, "ncd.pages.report.error.invalid.float");
            return false;
        }
        
        if (min != null && v < min) {
            
            exceptions.rejectValue(propPath, "ncd.pages.report.error.invalid.toolow", new Object[] { min }, null);
            return false;
        }
        
        if (max != null && v > max) {
            
            exceptions.rejectValue(propPath, "ncd.pages.report.error.invalid.toohigh", new Object[] { max }, null);
            return false;
        }
        
        return true;
    }

    // Stores a value as a property if the value is not empty
    private void storeProperty(TaskDefinition taskDef, String propName, String propValue) {
        
        if (!isEmptyString(propValue)) {
            
            taskDef.setProperty(propName, propValue);
        }
    }

    // Stores a value as a property if the value is not empty, and not equal to the ignored value
    private void storeProperty(TaskDefinition taskDef, String propName, String propValue, String ignoredValue) {
        
        if (!isEmptyString(propValue) && !propValue.equals(ignoredValue)) {
            
            taskDef.setProperty(propName, propValue);
        }
    }

    // Set the FTP / SFTP properties in the taskDef properties
    private void setFTPProperties(TaskDefinition taskDef, String senderClass) {

		if (DataFeedSenderFTP.class.getName().equals(senderClass)) {
			taskdef.setProperty(DataFeedSenderFactory.PROP_SENDER_FTP_HOST, ftpHost);
			taskdef.setProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PORT, ftpPort);
			taskdef.setProperty(DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME, ftpUsername);
			taskdef.setProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD, ftpPassword);
		}
		else if (DataFeedSenderSFTP.class.getName().equals(senderClass)) {
			taskdef.setProperty(DataFeedSenderFactory.PROP_SENDER_FTP_HOST, sftpHost);
			taskdef.setProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PORT, sftpPort);
			taskdef.setProperty(DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME, sftpUsername);
			taskdef.setProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD, sftpPassword);
		}
    }

    // Get the FTP / SFTP properties from the taskDef properties
    private void getFTPProperties(TaskDefinition taskDef, String senderClass) {

		if (DataFeedSenderFTP.class.getName().equals(senderClass)) {
			ftpHost = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_HOST));
			ftpPort = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PORT));
			ftpUsername = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME));
			ftpPassword = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD));
			ftpPasswordRepeat = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD));
		}
		else if (DataFeedSenderSFTP.class.getName().equals(senderClass)) {
			sftpHost = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_HOST));
			sftpPort = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PORT));
			sftpUsername = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_USERNAME));
			sftpPassword = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD));
			sftpPasswordRepeat = copyString(taskdef.getProperty(DataFeedSenderFactory.PROP_SENDER_FTP_PASSWORD));
		}
    }

	public String getRepeatIntervalUnits() {
		return repeatIntervalUnits;
	}

	public void setRepeatIntervalUnits(String repeatIntervalUnits) {
		this.repeatIntervalUnits = repeatIntervalUnits;
	}

	public String getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(String repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getFtpHost() {
		return ftpHost;
	}

	public void setFtpHost(String ftpHost) {
		this.ftpHost = ftpHost;
	}

	public String getFtpPort() {
		return ftpPort;
	}

	public void setFtpPort(String ftpPort) {
		this.ftpPort = ftpPort;
	}

	public String getFtpUsername() {
		return ftpUsername;
	}

	public void setFtpUsername(String ftpUsername) {
		this.ftpUsername = ftpUsername;
	}

	public String getFtpPassword() {
		return ftpPassword;
	}

	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	public String getSftpHost() {
		return sftpHost;
	}

	public void setSftpHost(String sftpHost) {
		this.sftpHost = sftpHost;
	}

	public String getSftpPort() {
		return sftpPort;
	}

	public void setSftpPort(String sftpPort) {
		this.sftpPort = sftpPort;
	}

	public String getSftpUsername() {
		return sftpUsername;
	}

	public void setSftpUsername(String sftpUsername) {
		this.sftpUsername = sftpUsername;
	}

	public String getSftpPassword() {
		return sftpPassword;
	}

	public void setSftpPassword(String sftpPassword) {
		this.sftpPassword = sftpPassword;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTaskClass() {
		return taskClass;
	}

	public void setTaskClass(String taskClass) {
		this.taskClass = taskClass;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getStartTimePattern() {
		return startTimePattern;
	}

	public void setStartTimePattern(String startTimePattern) {
		this.startTimePattern = startTimePattern;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public TaskDefinition getTaskdef() {
		return taskdef;
	}

	public Boolean getOldEnabled() {
		return oldEnabled;
	}

    /**
     * @return the aggregateReportTitle
     */
    public String getAggregateReportTitle() {
        return aggregateReportTitle;
    }

    /**
     * @param reportTitle the aggregateReportTitle to set
     */
    public void setAggregateReportTitle(String reportTitle) {
        this.aggregateReportTitle = reportTitle;
    }

    /**
     * @return the aggregateReportTemplate
     */
    public String getAggregateReportTemplate() {
        return aggregateReportTemplate;
    }

    /**
     * @param template the aggregateReportTemplate to set
     */
    public void setAggregateReportTemplate(String template) {
        this.aggregateReportTemplate = template;
    }

    /**
     * @return the aggregateReportTemplatePathname
     */
    public String getAggregateReportTemplatePathname() {
        return aggregateReportTemplatePathname;
    }

    /**
     * @param templatePathname the aggregateReportTemplatePathname to set
     */
    public void setAggregateReportTemplatePathname(String templatePathname) {
        this.aggregateReportTemplatePathname = templatePathname;
    }

    /**
     * @return the aggregateReportSender
     */
    public String getAggregateReportSender() {
        return aggregateReportSender;
    }

    /**
     * @param sender the aggregateReportSender to set
     */
    public void setAggregateReportSender(String sender) {
        this.aggregateReportSender = sender;
    }

    /**
     * @return the aggregateReportRecipients
     */
    public String getAggregateReportRecipients() {
        return aggregateReportRecipients;
    }

    /**
     * @param recipients the aggregateReportRecipients to set
     */
    public void setAggregateReportRecipients(String recipients) {
        this.aggregateReportRecipients = recipients;
    }

    /**
     * @return the aggregateReportSubject
     */
    public String getAggregateReportSubject() {
        return aggregateReportSubject;
    }

    /**
     * @param subject the aggregateReportSubject to set
     */
    public void setAggregateReportSubject(String subject) {
        this.aggregateReportSubject = subject;
    }

    /**
     * @return the conditionRateReportTitle
     */
    public String getConditionRateReportTitle() {
        return conditionRateReportTitle;
    }

    /**
     * @param conditionRateReportTitle the conditionRateReportTitle to set
     */
    public void setConditionRateReportTitle(String conditionRateReportTitle) {
        this.conditionRateReportTitle = conditionRateReportTitle;
    }

    /**
     * @return the conditionRateReportTemplate
     */
    public String getConditionRateReportTemplate() {
        return conditionRateReportTemplate;
    }

    /**
     * @param conditionRateReportTemplate the conditionRateReportTemplate to set
     */
    public void setConditionRateReportTemplate(String conditionRateReportTemplate) {
        this.conditionRateReportTemplate = conditionRateReportTemplate;
    }

    /**
     * @return the conditionRateReportTemplatePathname
     */
    public String getConditionRateReportTemplatePathname() {
        return conditionRateReportTemplatePathname;
    }

    /**
     * @param conditionRateReportTemplatePathname the conditionRateReportTemplatePathname to set
     */
    public void setConditionRateReportTemplatePathname(
            String conditionRateReportTemplatePathname) {
        this.conditionRateReportTemplatePathname = conditionRateReportTemplatePathname;
    }

    /**
     * @return the conditionRateReportSender
     */
    public String getConditionRateReportSender() {
        return conditionRateReportSender;
    }

    /**
     * @param conditionRateReportSender the conditionRateReportSender to set
     */
    public void setConditionRateReportSender(String conditionRateReportSender) {
        this.conditionRateReportSender = conditionRateReportSender;
    }

    /**
     * @return the conditionRateReportRecipients
     */
    public String getConditionRateReportRecipients() {
        return conditionRateReportRecipients;
    }

    /**
     * @param conditionRateReportRecipients the conditionRateReportRecipients to set
     */
    public void setConditionRateReportRecipients(
            String conditionRateReportRecipients) {
        this.conditionRateReportRecipients = conditionRateReportRecipients;
    }

    /**
     * @return the conditionRateReportSubject
     */
    public String getConditionRateReportSubject() {
        return conditionRateReportSubject;
    }

    /**
     * @param conditionRateReportSubject the conditionRateReportSubject to set
     */
    public void setConditionRateReportSubject(String conditionRateReportSubject) {
        this.conditionRateReportSubject = conditionRateReportSubject;
    }

	public String getExtractDestinationPathname() {
		return extractDestinationPathname;
	}

	public void setExtractDestinationPathname(String extractDestinationPathname) {
		this.extractDestinationPathname = extractDestinationPathname;
	}

	public String getExtractDestinationExtension() {
		return extractDestinationExtension;
	}

	public void setExtractDestinationExtension(String extractDestinationExtension) {
		this.extractDestinationExtension = extractDestinationExtension;
	}

	public String getExtractAddTimestamp() {
		return extractAddTimestamp;
	}

	public void setExtractAddTimestamp(String extractAddTimestamp) {
		this.extractAddTimestamp = extractAddTimestamp;
	}

	public String getExtractTimestampFormat() {
		return extractTimestampFormat;
	}

	public void setExtractTimestampFormat(String extractTimestampFormat) {
		this.extractTimestampFormat = extractTimestampFormat;
	}

	public String getExtractSender() {
		return extractSender;
	}

	public void setExtractSender(String extractSender) {
		this.extractSender = extractSender;
	}

	public String getExtractSuccessAlertList() {
		return extractSuccessAlertList;
	}

	public void setExtractSuccessAlertList(String extractSuccessAlertList) {
		this.extractSuccessAlertList = extractSuccessAlertList;
	}

	public String getExtractErrorAlertList() {
		return extractErrorAlertList;
	}

	public void setExtractErrorAlertList(String extractErrorAlertList) {
		this.extractErrorAlertList = extractErrorAlertList;
	}

	public String getExtractSenderClass() {
		return extractSenderClass;
	}

	public void setExtractSenderClass(String extractSenderClass) {
		this.extractSenderClass = extractSenderClass;
	}

	public String getDailyExtractDestinationPathname() {
		return dailyExtractDestinationPathname;
	}

	public void setDailyExtractDestinationPathname(
			String dailyExtractDestinationPathname) {
		this.dailyExtractDestinationPathname = dailyExtractDestinationPathname;
	}

	public String getDailyExtractDestinationExtension() {
		return dailyExtractDestinationExtension;
	}

	public void setDailyExtractDestinationExtension(
			String dailyExtractDestinationExtension) {
		this.dailyExtractDestinationExtension = dailyExtractDestinationExtension;
	}

	public String getDailyExtractAddTimestamp() {
		return dailyExtractAddTimestamp;
	}

	public void setDailyExtractAddTimestamp(String dailyExtractAddTimestamp) {
		this.dailyExtractAddTimestamp = dailyExtractAddTimestamp;
	}

	public String getDailyExtractTimestampFormat() {
		return dailyExtractTimestampFormat;
	}

	public void setDailyExtractTimestampFormat(String dailyExtractTimestampFormat) {
		this.dailyExtractTimestampFormat = dailyExtractTimestampFormat;
	}

	public String getDailyExtractSender() {
		return dailyExtractSender;
	}

	public void setDailyExtractSender(String dailyExtractSender) {
		this.dailyExtractSender = dailyExtractSender;
	}

	public String getDailyExtractSuccessAlertList() {
		return dailyExtractSuccessAlertList;
	}

	public void setDailyExtractSuccessAlertList(String dailyExtractSuccessAlertList) {
		this.dailyExtractSuccessAlertList = dailyExtractSuccessAlertList;
	}

	public String getDailyExtractErrorAlertList() {
		return dailyExtractErrorAlertList;
	}

	public void setDailyExtractErrorAlertList(String dailyExtractErrorAlertList) {
		this.dailyExtractErrorAlertList = dailyExtractErrorAlertList;
	}

	public String getDailyExtractSenderClass() {
		return dailyExtractSenderClass;
	}

	public void setDailyExtractSenderClass(String dailyExtractSenderClass) {
		this.dailyExtractSenderClass = dailyExtractSenderClass;
	}

	public String getFtpPasswordRepeat() {
		return ftpPasswordRepeat;
	}

	public void setFtpPasswordRepeat(String ftpPasswordRepeat) {
		this.ftpPasswordRepeat = ftpPasswordRepeat;
	}

	public String getSftpPasswordRepeat() {
		return sftpPasswordRepeat;
	}

	public void setSftpPasswordRepeat(String sftpPasswordRepeat) {
		this.sftpPasswordRepeat = sftpPasswordRepeat;
	}

	public String getConditionRateExportDestinationPathname() {
		return conditionRateExportDestinationPathname;
	}

	public void setConditionRateExportDestinationPathname(
			String conditionRateExportDestinationPathname) {
		this.conditionRateExportDestinationPathname = conditionRateExportDestinationPathname;
	}

	public String getConditionRateExportDestinationExtension() {
		return conditionRateExportDestinationExtension;
	}

	public void setConditionRateExportDestinationExtension(
			String conditionRateExportDestinationExtension) {
		this.conditionRateExportDestinationExtension = conditionRateExportDestinationExtension;
	}

	public String getConditionRateExportAddTimestamp() {
		return conditionRateExportAddTimestamp;
	}

	public void setConditionRateExportAddTimestamp(
			String conditionRateExportAddTimestamp) {
		this.conditionRateExportAddTimestamp = conditionRateExportAddTimestamp;
	}

	public String getConditionRateExportTimestampFormat() {
		return conditionRateExportTimestampFormat;
	}

	public void setConditionRateExportTimestampFormat(
			String conditionRateExportTimestampFormat) {
		this.conditionRateExportTimestampFormat = conditionRateExportTimestampFormat;
	}

	public String getConditionRateExportSender() {
		return conditionRateExportSender;
	}

	public void setConditionRateExportSender(String conditionRateExportSender) {
		this.conditionRateExportSender = conditionRateExportSender;
	}

	public String getConditionRateExportSuccessAlertList() {
		return conditionRateExportSuccessAlertList;
	}

	public void setConditionRateExportSuccessAlertList(
			String conditionRateExportSuccessAlertList) {
		this.conditionRateExportSuccessAlertList = conditionRateExportSuccessAlertList;
	}

	public String getConditionRateExportErrorAlertList() {
		return conditionRateExportErrorAlertList;
	}

	public void setConditionRateExportErrorAlertList(
			String conditionRateExportErrorAlertList) {
		this.conditionRateExportErrorAlertList = conditionRateExportErrorAlertList;
	}

	public String getConditionRateExportSenderClass() {
		return conditionRateExportSenderClass;
	}

	public void setConditionRateExportSenderClass(
			String conditionRateExportSenderClass) {
		this.conditionRateExportSenderClass = conditionRateExportSenderClass;
	}

	public String getExtractSinkClass() {
		return extractSinkClass;
	}

	public void setExtractSinkClass(String extractSinkClass) {
		this.extractSinkClass = extractSinkClass;
	}

	public String getConditionRateExportSinkClass() {
		return conditionRateExportSinkClass;
	}

	public void setConditionRateExportSinkClass(String conditionRateExportSinkClass) {
		this.conditionRateExportSinkClass = conditionRateExportSinkClass;
	}

	public String getZeroCountConditionReportTitle() {
		return zeroCountConditionReportTitle;
	}

	public void setZeroCountConditionReportTitle(
			String zeroCountConditionReportTitle) {
		this.zeroCountConditionReportTitle = zeroCountConditionReportTitle;
	}

	public String getZeroCountConditionReportTemplate() {
		return zeroCountConditionReportTemplate;
	}

	public void setZeroCountConditionReportTemplate(
			String zeroCountConditionReportTemplate) {
		this.zeroCountConditionReportTemplate = zeroCountConditionReportTemplate;
	}

	public String getZeroCountConditionReportTemplatePathname() {
		return zeroCountConditionReportTemplatePathname;
	}

	public void setZeroCountConditionReportTemplatePathname(
			String zeroCountConditionReportTemplatePathname) {
		this.zeroCountConditionReportTemplatePathname = zeroCountConditionReportTemplatePathname;
	}

	public String getZeroCountConditionReportSender() {
		return zeroCountConditionReportSender;
	}

	public void setZeroCountConditionReportSender(
			String zeroCountConditionReportSender) {
		this.zeroCountConditionReportSender = zeroCountConditionReportSender;
	}

	public String getZeroCountConditionReportRecipients() {
		return zeroCountConditionReportRecipients;
	}

	public void setZeroCountConditionReportRecipients(
			String zeroCountConditionReportRecipients) {
		this.zeroCountConditionReportRecipients = zeroCountConditionReportRecipients;
	}

	public String getZeroCountConditionReportSubject() {
		return zeroCountConditionReportSubject;
	}

	public void setZeroCountConditionReportSubject(
			String zeroCountConditionReportSubject) {
		this.zeroCountConditionReportSubject = zeroCountConditionReportSubject;
	}

	public String getZeroCountConditionExportSinkClass() {
		return zeroCountConditionExportSinkClass;
	}

	public void setZeroCountConditionExportSinkClass(
			String zeroCountConditionExportSinkClass) {
		this.zeroCountConditionExportSinkClass = zeroCountConditionExportSinkClass;
	}

	public String getZeroCountConditionExportDestinationPathname() {
		return zeroCountConditionExportDestinationPathname;
	}

	public void setZeroCountConditionExportDestinationPathname(
			String zeroCountConditionExportDestinationPathname) {
		this.zeroCountConditionExportDestinationPathname = zeroCountConditionExportDestinationPathname;
	}

	public String getZeroCountConditionExportDestinationExtension() {
		return zeroCountConditionExportDestinationExtension;
	}

	public void setZeroCountConditionExportDestinationExtension(
			String zeroCountConditionExportDestinationExtension) {
		this.zeroCountConditionExportDestinationExtension = zeroCountConditionExportDestinationExtension;
	}

	public String getZeroCountConditionExportAddTimestamp() {
		return zeroCountConditionExportAddTimestamp;
	}

	public void setZeroCountConditionExportAddTimestamp(
			String zeroCountConditionExportAddTimestamp) {
		this.zeroCountConditionExportAddTimestamp = zeroCountConditionExportAddTimestamp;
	}

	public String getZeroCountConditionExportTimestampFormat() {
		return zeroCountConditionExportTimestampFormat;
	}

	public void setZeroCountConditionExportTimestampFormat(
			String zeroCountConditionExportTimestampFormat) {
		this.zeroCountConditionExportTimestampFormat = zeroCountConditionExportTimestampFormat;
	}

	public String getZeroCountConditionExportSender() {
		return zeroCountConditionExportSender;
	}

	public void setZeroCountConditionExportSender(
			String zeroCountConditionExportSender) {
		this.zeroCountConditionExportSender = zeroCountConditionExportSender;
	}

	public String getZeroCountConditionExportSuccessAlertList() {
		return zeroCountConditionExportSuccessAlertList;
	}

	public void setZeroCountConditionExportSuccessAlertList(
			String zeroCountConditionExportSuccessAlertList) {
		this.zeroCountConditionExportSuccessAlertList = zeroCountConditionExportSuccessAlertList;
	}

	public String getZeroCountConditionExportErrorAlertList() {
		return zeroCountConditionExportErrorAlertList;
	}

	public void setZeroCountConditionExportErrorAlertList(
			String zeroCountConditionExportErrorAlertList) {
		this.zeroCountConditionExportErrorAlertList = zeroCountConditionExportErrorAlertList;
	}

	public String getZeroCountConditionExportSenderClass() {
		return zeroCountConditionExportSenderClass;
	}

	public void setZeroCountConditionExportSenderClass(
			String zeroCountConditionExportSenderClass) {
		this.zeroCountConditionExportSenderClass = zeroCountConditionExportSenderClass;
	}

	public String getNewApplication() {
		return newApplication;
	}

	public void setNewApplication(String newApplication) {
		this.newApplication = newApplication;
	}

	public String getNewFacility() {
		return newFacility;
	}

	public void setNewFacility(String newFacility) {
		this.newFacility = newFacility;
	}

	public String getNewLocation() {
		return newLocation;
	}

	public void setNewLocation(String newLocation) {
		this.newLocation = newLocation;
	}

	public String getNewCondition() {
		return newCondition;
	}

	public void setNewCondition(String newCondition) {
		this.newCondition = newCondition;
	}

	public List<MonitoredCondition> getMonitoredConditions() {
		return monitoredConditions;
	}

	public void setMonitoredConditions(List<MonitoredCondition> monitoredConditions) {
		// The conversion to ArrayList is because some List implementations do
		// do have a functioning remove(Object o) method, which is required by
		// the report page.
		this.monitoredConditions = new ArrayList<MonitoredCondition>(monitoredConditions);
	}

	public List<Long> getSelectedMonitoredConditions() {
		return selectedMonitoredConditions;
	}

	public void setSelectedMonitoredConditions(
			List<Long> selectedMonitoredConditions) {
		this.selectedMonitoredConditions = selectedMonitoredConditions;
	}
	private String copyString(String s) {
		if (s==null) {
			return null;
		}
		else {
			return new String(s);
		}
	}

	public void setDataSourceReportTitle(String dataSourceReportTitle) {
		this.dataSourceReportTitle = dataSourceReportTitle;
	}

	public String getDataSourceReportTitle() {
		return dataSourceReportTitle;
	}

	public void setDataSourceReportTemplate(String dataSourceReportTemplate) {
		this.dataSourceReportTemplate = dataSourceReportTemplate;
	}

	public String getDataSourceReportTemplate() {
		return dataSourceReportTemplate;
	}

	public void setDataSourceReportTemplatePathname(
			String dataSourceReportTemplatePathname) {
		this.dataSourceReportTemplatePathname = dataSourceReportTemplatePathname;
	}

	public String getDataSourceReportTemplatePathname() {
		return dataSourceReportTemplatePathname;
	}

	public void setDataSourceReportSender(String dataSourceReportSender) {
		this.dataSourceReportSender = dataSourceReportSender;
	}

	public String getDataSourceReportSender() {
		return dataSourceReportSender;
	}

	public void setDataSourceReportRecipients(String dataSourceReportRecipients) {
		this.dataSourceReportRecipients = dataSourceReportRecipients;
	}

	public String getDataSourceReportRecipients() {
		return dataSourceReportRecipients;
	}

	public void setDataSourceReportSubject(String dataSourceReportSubject) {
		this.dataSourceReportSubject = dataSourceReportSubject;
	}

	public String getDataSourceReportSubject() {
		return dataSourceReportSubject;
	}

	public String getDataSourceExportSinkClass() {
		return dataSourceExportSinkClass;
	}

	public void setDataSourceExportSinkClass(String dataSourceExportSinkClass) {
		this.dataSourceExportSinkClass = dataSourceExportSinkClass;
	}

	public String getDataSourceExportDestinationPathname() {
		return dataSourceExportDestinationPathname;
	}

	public void setDataSourceExportDestinationPathname(
			String dataSourceExportDestinationPathname) {
		this.dataSourceExportDestinationPathname = dataSourceExportDestinationPathname;
	}

	public String getDataSourceExportDestinationExtension() {
		return dataSourceExportDestinationExtension;
	}

	public void setDataSourceExportDestinationExtension(
			String dataSourceExportDestinationExtension) {
		this.dataSourceExportDestinationExtension = dataSourceExportDestinationExtension;
	}

	public String getDataSourceExportAddTimestamp() {
		return dataSourceExportAddTimestamp;
	}

	public void setDataSourceExportAddTimestamp(String dataSourceExportAddTimestamp) {
		this.dataSourceExportAddTimestamp = dataSourceExportAddTimestamp;
	}

	public String getDataSourceExportTimestampFormat() {
		return dataSourceExportTimestampFormat;
	}

	public void setDataSourceExportTimestampFormat(
			String dataSourceExportTimestampFormat) {
		this.dataSourceExportTimestampFormat = dataSourceExportTimestampFormat;
	}

	public String getDataSourceExportSender() {
		return dataSourceExportSender;
	}

	public void setDataSourceExportSender(String dataSourceExportSender) {
		this.dataSourceExportSender = dataSourceExportSender;
	}

	public String getDataSourceExportSuccessAlertList() {
		return dataSourceExportSuccessAlertList;
	}

	public void setDataSourceExportSuccessAlertList(
			String dataSourceExportSuccessAlertList) {
		this.dataSourceExportSuccessAlertList = dataSourceExportSuccessAlertList;
	}

	public String getDataSourceExportErrorAlertList() {
		return dataSourceExportErrorAlertList;
	}

	public void setDataSourceExportErrorAlertList(
			String dataSourceExportErrorAlertList) {
		this.dataSourceExportErrorAlertList = dataSourceExportErrorAlertList;
	}

	public String getDataSourceExportSenderClass() {
		return dataSourceExportSenderClass;
	}

	public void setDataSourceExportSenderClass(String dataSourceExportSenderClass) {
		this.dataSourceExportSenderClass = dataSourceExportSenderClass;
	}

	public String getExtractStripForRHITs() {
		return extractStripForRHITs;
	}

	public void setExtractStripForRHITs(String extractStripForRHITs) {
		this.extractStripForRHITs = extractStripForRHITs;
	}
}

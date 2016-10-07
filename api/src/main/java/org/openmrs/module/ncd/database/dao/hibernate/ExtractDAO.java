/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.critic.ReportResult.ReportResultStatus;
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.database.ExportedResult;
import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.Institution;
import org.openmrs.module.ncd.database.RawMessage;
import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.database.TaskRunStatus;
import org.openmrs.module.ncd.database.dao.IExtractDAO;
import org.openmrs.module.ncd.output.dailyextract.DailyExtractorFactory;
import org.openmrs.module.ncd.output.extract.DataFeedExtractorFactory;
import org.openmrs.module.ncd.output.extract.DataFeedLog;
import org.openmrs.module.ncd.output.extract.DataFeedSink;
import org.openmrs.module.ncd.utilities.MapUtilities;
import org.openmrs.module.ncd.utilities.NCDConcepts;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.module.ncd.utilities.PersonAttributeTypeCache;

/** Hibernate implementation of data feed extraction logic
 * 
 * Note: Remember that there is only one instance of the DAO. It must be thread-safe, since there could be multiple
 * calls into the DAO at the same time. This is the reason the ExtractDAOHelper class exists - a separate instance
 * of that class is created for each call into the DAO, so it does not have to be thread-safe.
 */
public class ExtractDAO implements IExtractDAO {

    private static Log logger = LogFactory.getLog(ExtractDAO.class);

    /** Hibernate session factory */
    private SessionFactory sessionFactory;

    /** The number of exclude by institution buckets */
    @SuppressWarnings("unused")
    private static final int excludeByInstitutionBuckets=5;

    // ********************
    // One-time-init things
    // ********************

    /** The cached NCD concept RESULT_SEQUENCE_NUMBER */
    private static Concept resultSequenceConcept = null;
    /** The cached NCD concept CONDITION_NAME */
    private static Concept conditionNameConcept = null;
    /** The cached NCD concept INSTITUTION_NAME */
    private static Concept institutionNameConcept = null;
    /** The cached NCD concept RAW_HL7_ID */
    private static Concept rawHL7IdConcept = null;
    /** A map from NCD concept names to extract column names */
    private static Map<String,String> columnNameByConceptName = null;
    /** A map from patient attribute names to extract column names */
    private static Map<String,String> columnNameByPatientAttrName = null;
    /** A map from provider attribute names to extract column names */
    private static Map<String,String> columnNameByProviderAttrName = null;

    /** Private helper class created for each call into the DAO, so that we can do things that aren't
     * thread-safe. */
    private class ExtractDAOHelper {
    	
        /** True iff this extract is incremental, that is, includes only
         * reportable results after the last extract for the same
         * organization. */
        private boolean incremental = false;
        /** The recent result interval, if any */
        private int recentResultInterval;
        /** The recent result interval units, if any */
        private String recentResultIntervalUnits = null;
        /** The start date/time */
        Date startDate;
        /** The end date/time */
        Date endDate;
        /** The set of institution names to include reportable result for,
         * or null to include reportable results for all institutions */
        private Set<String> includedInstitutionNames = null;
        /** The set of condition names to include reportable result for,
         * or null to include reportable results with any condition */
        private Set<String> includedConditionNames = null;
        /** The set of condition names to exclude reportable result for,
         * or null to not exclude any condition */
        private Set<String> excludedConditionNames = null;
        /** The maximum number of rows to extract */
        private Long maxRowsToExtract = Long.MAX_VALUE;
        /** The row being constructed */
        private Map<String, Object> row = null;
        /** The set of counties to include reportable results for,
         * or null to include reportable results for all counties */
        private Set<String> includedCounties = null;
        /** The set of jurisdictions to include reportable results for,
         * or null to include reportable results for all jurisdictions  */
        private Set<String> includedJurisdictions = null;
        /** The set of codes to include reportable results for,
         * or null to include reportable results for all codes */
        private Set<String> includedCodes = null;
        /** A condition name (in combination with institution) to exclude reportable results for,
         * or null to not exclude. */
        private ArrayList<String> excludedConditions = null;
        /** A set of codes (in combination with institution) to exclude reportable results for,
         * or null to not exclude. */
        private ArrayList<Set<String>> excludedCodes = null;
        /** An institution (in combination with a condition or codes) to exclude reportable results for,
         * or null to not exclude. */
        private ArrayList<String> excludedInstitutions = null;

        // NOTE: This method blows up with JVM out of memory errors if more than about 8-9k results are selected.
        // See extractMany for a different implementation that attempts to solve this problem.
        public void extract(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status) {
            
            setup(properties);

            logger.info("extract begin");

            row = new HashMap<String,Object>();

            logger.debug("incremental=" + incremental);

            long firstResultSeq = 0;
            long lastResultSeq = 0;
            long rowsExtracted = 0;
            
            // If incremental, fetch the last result sequence number sent.
            //
            // The caller is responsible for populating the property: PROP_EXTRACTOR_LAST_RECORD_SENT
            // containing the last record sent (from the most recent successful task run).  The property
            // won't be set if the task has never been completed successfully before.
            if (incremental) {
    	        if (properties.containsKey(DataFeedExtractorFactory.PROP_EXTRACTOR_LAST_RECORD_SENT)) {
    	        	lastResultSeq = Long.valueOf(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_LAST_RECORD_SENT));
    	            logger.debug("lastResultSeq=" + lastResultSeq);
    	        }
            }

            try {

            	ConditionDetectorService cds = NCDUtilities.getService();
                HQLQueryBuilder builder = new HQLQueryBuilder("ReportableResult result");
                builder.addJoin("join result.rawMessage");
                builder.addJoin("join result.manualReviewStatusType as status with status.id in (" + 
                	ConditionDetectorService.reviewStatusTypeNoReviewRequired + "," + 
                	ConditionDetectorService.reviewStatusTypeReleased + ")");
                
                if (includedInstitutionNames != null) {
                    builder.addJoin("join result.institution as institution with institution.name in " + builder.makeList(includedInstitutionNames));
                }
                
                if (includedCounties != null) {
                    builder.addJoin("join result.county as county with county.county || '(' || county.state || ')' in " + builder.makeList(includedCounties));
                }
                
                if (includedJurisdictions != null) {
                    builder.addJoin("join result.jurisdiction as jurisdiction with jurisdiction.jurisdiction in " + builder.makeList(includedJurisdictions));
                }
                
                if (includedCodes != null) {
                    builder.addJoin("join result.code as code");
                    builder.addJoin("join code.codeSystem as codeSystem");
                    builder.addLiteral("code.code || '(' || codeSystem.name || ')'", "in", builder.makeList(includedCodes));
                }
                
                int i=0;
                for (String excludedInstitution : excludedInstitutions) {
                	if (excludedInstitution != null) {
                		builder.addJoin("left join result.institution as institution" + (i+1));
                		
                		if (excludedCodes.get(i) != null) {
                            builder.addJoin("left join result.code as code" + (i+1));
                            builder.addJoin("left join code" + (i+1) + ".codeSystem as codeSystem" + (i+1));
                		}
                	}
                	i++;
                }
               
                // Do not export anything flagged as "sent in error"
                builder.add("result.sentInError", "=", false);
                
                if (incremental) {
                	builder.add("result.resultSeq", ">", new Long(lastResultSeq));
                } else if (recentResultInterval > 0) {
                	Calendar calendar = new GregorianCalendar();
                	calendar.setTime(new Date());
                	
                	if (recentResultIntervalUnits.equals("m")) {
                		calendar.add(Calendar.MINUTE, -recentResultInterval);
                	} else if (recentResultIntervalUnits.equals("h")) {
                		calendar.add(Calendar.HOUR, -recentResultInterval);
                	} else if (recentResultIntervalUnits.equals("d")) {
                		calendar.add(Calendar.DAY_OF_MONTH, -recentResultInterval);
                	}
                	
                	Date startDate = calendar.getTime();
                	builder.add("result.releaseDate", ">=", startDate);
                } else if (startDate != null || endDate != null) {
                	if (startDate != null) {
                		builder.add("result.releaseDate", ">=", startDate);
                	}
                	if (endDate != null) {
                		builder.add("result.releaseDate", "<=", endDate);
                	}
                }
                
                if (includedConditionNames != null || excludedConditionNames != null) {

                	if (includedConditionNames != null) {
                		builder.addLiteral("result.conditionName", "in",  builder.makeList(includedConditionNames));
                	}
                	else {
                		builder.addLiteral("result.conditionName", "not in",  builder.makeList(excludedConditionNames));
                	}
                }

                // Exclude condition name or code from institution (variable number of buckets)
                //
                // Joins are as follows:
                //	left join result.institution as institution<i>
                //	left join result.codeCondition as codeCondition<i>
                //	left join codeCondition<i>.code as code<i>
                //	left join code<i>.codeSystem as codeSystem<i>
                //
                // Where clause is as follows:
                //
                //	when excluding condition name from institution:
                //		and ((institution<i>.name != <value>) or (result.conditionName != <value>))
                //
                //	when excluding codes from institution:
                //		and ((institution<i>.name != <value>) or (code<i>.code || '(' || codeSystem<i>.name || ')' not in (<listvalue>)))
                i=0;
                for (String excludedInstitution : excludedInstitutions) {
                	if (excludedInstitution != null) {
                		if (excludedConditions.get(i) != null) {
                    		builder.addOr("institution" + (i+1) + ".name", "!=", excludedInstitution, "result.conditionName", "!=", excludedConditions.get(i));
                		}
                		else {
                    		builder.addOrLiteral("institution" + (i+1) + ".name", 
                    							 "!=",
                    							 "'" + builder.escape(excludedInstitution) + "'",
                    							 "code" + (i+1) + ".code || '(' || codeSystem" + (i+1) + ".name || ')'",
                    							 "not in",
                    							 builder.makeList(excludedCodes.get(i)));
                		}
                	}
                	i++;
                }
                
                
                // sort by sequence number
                builder.setSort("result.resultSeq");
                builder.setSortAscending(true);

                // run query and iterate the results
                Session session = sessionFactory.getCurrentSession();
                Query query = builder.getQuery(session);
                ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
                while (results.next() && rowsExtracted < maxRowsToExtract) {

                    row.clear();

                    Object[] tuple = results.get();
                    ReportableResult reportableResult = (ReportableResult) tuple[0];
                    RawMessage rawhl7 = (RawMessage) tuple[1];
                    
                    // Update the first and last result seq we've seen
                    long thisResultSeq = reportableResult.getResultSeq().longValue();
                    if (firstResultSeq == 0) {
                        firstResultSeq = thisResultSeq;
                    }
                    lastResultSeq = thisResultSeq;
                    
                    // Extract columns from the reportable result
                    extractReportableResult(reportableResult);

                    // Extract columns from the raw hl7 message
                    extractRawHL7(rawhl7);

                    // Write to the output, whatever it may be.
                    sink.append(row);
                    
                    // Mark the result as exported.
                    ExportedResult exportedResult = new ExportedResult(status, reportableResult);
                	cds.addExportedResult(exportedResult);

                	// Count the extracted row
                    rowsExtracted++;
                	
                	// Get stuff out of the Hibernate cache to avoid running out of memory.
                	if ((rowsExtracted % 500) == 0) {
                        logger.info("  extracted " + rowsExtracted + " results, clearing the hibernate session cache...");
	                	session.flush();
	                	session.clear();
                	}
                }

                feedLog.setFirstRowSent(firstResultSeq);
                feedLog.setLastRowSent(lastResultSeq);
                feedLog.setRowCount(rowsExtracted);
            }
            catch (Exception e) {

                logger.error("Exception: " + e.getMessage(), e);
                feedLog.error("Exception: " + e.getMessage());
            }
            
            logger.info("extract end: " + rowsExtracted + " rows extracted");

            row = null;
        }

        /**
         * Set the values for the bind parameters in a prepared statement.
         * 
         * @param stmt The prepared statement for which the bind parameter values should be set.
         * @param values A List of the values to be set.
         * @throws SQLException
         */
        @SuppressWarnings({ "unused", "rawtypes" })
        protected void bindParameterValues(PreparedStatement stmt, List values) throws SQLException {
        	
        	int parameterIndex = 1;
        	for (Object value : values) {
        		stmt.setObject(parameterIndex++, value);
        	}
        }

        @SuppressWarnings("unused")
        protected List<Long> findInstitutionIds(Set<String> institutionNames) {
        	
        	ConditionDetectorService cds = NCDUtilities.getService();
        	ArrayList<Long> ids = new ArrayList<Long>(institutionNames.size());
        	for (String name : institutionNames) {
        		ids.add(cds.findInstitutionByName(name).getId());
        	}
        	return ids;
        }

        @SuppressWarnings("unused")
        protected void appendAndFieldInIdList(StringBuilder buf, String fieldName, List<String> ids) {
        	
        	boolean first = true;
        	buf.append(" AND ");
        	buf.append(fieldName);
        	buf.append(" IN (");
        	for (Object id : ids) {
        		if (!first) {
        			buf.append(",");
        		}
        		first = false;
        		buf.append(id.toString());
        	}
        	buf.append(")");
        }
/*
        public void extractMany(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status) {
            
            setup(properties);

            logger.info("extract begin");

            row = new HashMap<String,Object>();

            logger.debug("incremental=" + incremental);

            long firstResultSeq = 0;
            long lastResultSeq = 0;
            long rowsExtracted = 0;
            
            // If incremental, fetch the last result sequence number sent.
            //
            // The caller is responsible for populating the property: PROP_EXTRACTOR_LAST_RECORD_SENT
            // containing the last record sent (from the most recent successful task run).  The property
            // won't be set if the task has never been completed successfully before.
            if (incremental) {
    	        if (properties.containsKey(DataFeedExtractorFactory.PROP_EXTRACTOR_LAST_RECORD_SENT)) {
    	        	lastResultSeq = Long.valueOf(properties.get(DataFeedExtractorFactory.PROP_EXTRACTOR_LAST_RECORD_SENT));
    	            logger.debug("lastResultSeq=" + lastResultSeq);
    	        }
            }

            try {

            	// (1) Build a JDBC query using a scrolling result set, which *only* filters on columns of
            	//     ncd_reportable_result, and only returns primary key values.
            	ConditionDetectorService cds = NCDUtilities.getService();
            	ArrayList bindParameters = new ArrayList();
            	StringBuilder outerSql = new StringBuilder();
            	outerSql.append("SELECT rr.id FROM ncd_reportable_result rr");
            	
            	outerSql.append(" WHERE rr.reviewStatusTypeId IN (" + 
            			ConditionDetectorService.reviewStatusTypeNoReviewRequired + "," + 
                    	ConditionDetectorService.reviewStatusTypeReleased + ")");
            	
                if (includedInstitutionNames != null) {
                	appendAndFieldInIdList(outerSql, "rr.institutionid", findInstitutionIds(includedInstitutionNames));
                }

            	outerSql.append(" ORDER BY rr.id ASC");
            	
            	// (2) Iterate over the results from the query.
            	//     (a) Use hibernate to get the ReportableResult
            	//     (b) Do the rest of the filtering in Java/Hibernate.
            	Session session = sessionFactory.getCurrentSession();
            	Connection conn = session.connection();
            	PreparedStatement stmt = conn.prepareStatement(outerSql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            	bindParameterValues(stmt, bindParameters);
            	ResultSet outerResults = stmt.executeQuery(outerSql.toString());
            	while (outerResults.next() && rowsExtracted < maxRowsToExtract) {
            		long reportableResultId = outerResults.getLong(0);
            		ReportableResult reportableResult = cds.getReportableResult(reportableResultId);

                    row.clear();

                    RawMessage rawhl7 = reportableResult.getRawMessage();
                    
                    // Update the first and last result seq we've seen
                    long thisResultSeq = reportableResult.getResultSeq().longValue();
                    if (firstResultSeq == 0) {
                        firstResultSeq = thisResultSeq;
                    }
                    lastResultSeq = thisResultSeq;
                    
                    // Extract columns from the reportable result
                    extractReportableResult(reportableResult);

                    // Extract columns from the raw hl7 message
                    extractRawHL7(rawhl7);

                    sink.append(row);
                    ExportedResult exportedResult = new ExportedResult(status, reportableResult);
                	cds.addExportedResult(exportedResult);
                	session.flush();
                	session.evict(exportedResult);
                    
                    rowsExtracted++;
            	}

                if (includedCounties != null) {
                    builder.addJoin("join result.county as county with county.county || '(' || county.state || ')' in " + builder.makeList(includedCounties));
                }
                
                if (includedJurisdictions != null) {
                    builder.addJoin("join result.jurisdiction as jurisdiction with jurisdiction.jurisdiction in " + builder.makeList(includedJurisdictions));
                }
                
                if (includedCodes != null) {
                    builder.addJoin("join result.code as code");
                    builder.addJoin("join code.codeSystem as codeSystem");
                    builder.addLiteral("code.code || '(' || codeSystem.name || ')'", "in", builder.makeList(includedCodes));
                }
                
                int i=0;
                for (String excludedInstitution : excludedInstitutions) {
                	if (excludedInstitution != null) {
                		builder.addJoin("left join result.institution as institution" + (i+1));
                		
                		if (excludedCodes.get(i) != null) {
                            builder.addJoin("left join result.code as code" + (i+1));
                            builder.addJoin("left join code" + (i+1) + ".codeSystem as codeSystem" + (i+1));
                		}
                	}
                	i++;
                }
               
                // Do not export anything flagged as "sent in error"
                builder.add("result.sentInError", "=", false);
                
                if (incremental) {
                	builder.add("result.resultSeq", ">", new Long(lastResultSeq));
                } else if (recentResultInterval > 0) {
                	Calendar calendar = new GregorianCalendar();
                	calendar.setTime(new Date());
                	
                	if (recentResultIntervalUnits.equals("m")) {
                		calendar.add(Calendar.MINUTE, -recentResultInterval);
                	} else if (recentResultIntervalUnits.equals("h")) {
                		calendar.add(Calendar.HOUR, -recentResultInterval);
                	} else if (recentResultIntervalUnits.equals("d")) {
                		calendar.add(Calendar.DAY_OF_MONTH, -recentResultInterval);
                	}
                	
                	Date startDate = calendar.getTime();
                	builder.add("result.releaseDate", ">=", startDate);
                } else if (startDate != null || endDate != null) {
                	if (startDate != null) {
                		builder.add("result.releaseDate", ">=", startDate);
                	}
                	if (endDate != null) {
                		builder.add("result.releaseDate", "<=", endDate);
                	}
                }
                
                if (includedConditionNames != null || excludedConditionNames != null) {

                	if (includedConditionNames != null) {
                		builder.addLiteral("result.conditionName", "in",  builder.makeList(includedConditionNames));
                	}
                	else {
                		builder.addLiteral("result.conditionName", "not in",  builder.makeList(excludedConditionNames));
                	}
                }

                // Exclude condition name or code from institution (variable number of buckets)
                //
                // Joins are as follows:
                //	left join result.institution as institution<i>
                //	left join result.codeCondition as codeCondition<i>
                //	left join codeCondition<i>.code as code<i>
                //	left join code<i>.codeSystem as codeSystem<i>
                //
                // Where clause is as follows:
                //
                //	when excluding condition name from institution:
                //		and ((institution<i>.name != <value>) or (result.conditionName != <value>))
                //
                //	when excluding codes from institution:
                //		and ((institution<i>.name != <value>) or (code<i>.code || '(' || codeSystem<i>.name || ')' not in (<listvalue>)))
                i=0;
                for (String excludedInstitution : excludedInstitutions) {
                	if (excludedInstitution != null) {
                		if (excludedConditions.get(i) != null) {
                    		builder.addOr("institution" + (i+1) + ".name", "!=", excludedInstitution, "result.conditionName", "!=", excludedConditions.get(i));
                		}
                		else {
                    		builder.addOrLiteral("institution" + (i+1) + ".name", 
                    							 "!=",
                    							 "'" + builder.escape(excludedInstitution) + "'",
                    							 "code" + (i+1) + ".code || '(' || codeSystem" + (i+1) + ".name || ')'",
                    							 "not in",
                    							 builder.makeList(excludedCodes.get(i)));
                		}
                	}
                	i++;
                }
                
                
                // sort by sequence number
                builder.setSort("result.resultSeq");
                builder.setSortAscending(true);

                // run query and iterate the results
                Query query = builder.getQuery(sessionFactory.getCurrentSession());
                ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
                while (results.next() && rowsExtracted < maxRowsToExtract) {

                    row.clear();

                    Object[] tuple = results.get();
                    ReportableResult reportableResult = (ReportableResult) tuple[0];
                    RawMessage rawhl7 = (RawMessage) tuple[1];
                    
                    // Update the first and last result seq we've seen
                    long thisResultSeq = reportableResult.getResultSeq().longValue();
                    if (firstResultSeq == 0) {
                        firstResultSeq = thisResultSeq;
                    }
                    lastResultSeq = thisResultSeq;
                    
                    // Extract columns from the reportable result
                    extractReportableResult(reportableResult);

                    // Extract columns from the raw hl7 message
                    extractRawHL7(rawhl7);

                    sink.append(row);
                	cds.addExportedResult(new ExportedResult(status, reportableResult));
                    
                    rowsExtracted++;
                }

                feedLog.setFirstRowSent(firstResultSeq);
                feedLog.setLastRowSent(lastResultSeq);
                feedLog.setRowCount(rowsExtracted);
            }
            catch (Exception e) {

                logger.error("Exception: " + e.getMessage(), e);
                feedLog.error("Exception: " + e.getMessage());
            }
            
            logger.info("extract end: " + rowsExtracted + " rows extracted");

            row = null;
        }
        */
        
        @SuppressWarnings("unchecked")
        public void extractDaily(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status) {
            
            logger.info("extract begin");

            row = new HashMap<String,Object>();

            long rowsExtracted = 0;

            maxRowsToExtract = MapUtilities.get(properties, DailyExtractorFactory.PROP_EXTRACTOR_MAX_ROWS, Long.MAX_VALUE);

            try {

            	// First, extract the reportable results and send them all to the sink, because the primary sort
            	// for the report is type (with reportable results first, and decided results second)
            	
                HQLQueryBuilder builder = new HQLQueryBuilder("ReportableResult result");
                
                // Must be not requiring a manual review, or released 
                builder.addJoin("join result.manualReviewStatusType as status with status.id in (" + 
                	ConditionDetectorService.reviewStatusTypeNoReviewRequired + "," + 
                	ConditionDetectorService.reviewStatusTypeReleased + ")");

                // Do not export anything flagged as "sent in error"
                builder.add("result.sentInError", "=", false);
                
                // Get the number of days to include (default is 7 days)
                int nbrDays=7;
                if (properties.get(DailyExtractorFactory.PROP_EXTRACTOR_DAYS) != null) {
                	try {
                		nbrDays = Integer.parseInt(properties.get(DailyExtractorFactory.PROP_EXTRACTOR_DAYS));
                	}
                	catch (NumberFormatException nfe) {
                		logger.error("Invalid numeric format stored in " + DailyExtractorFactory.PROP_EXTRACTOR_DAYS);
                	}
                }

                // Compute a [startDate, endDate] range such that:
                // 	startDate = n days ago, at midnight
                //	endDate = yesterday, at 23:59:59.999
                // The filter should include the new records added during the prior n days, beginning with yesterday
               	Calendar calendar = new GregorianCalendar();
               	calendar.setTime(new Date());
               	
               	// endDate
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                calendar.set(Calendar.HOUR, 11);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                calendar.set(Calendar.AM_PM, Calendar.PM);
                Date endDate = calendar.getTime();
                
                // startDate
                calendar.add(Calendar.DAY_OF_MONTH, -(nbrDays - 1));
                calendar.set(Calendar.HOUR, 12);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.AM_PM, Calendar.AM);
                Date startDate =  calendar.getTime(); 

                // Add the filter: startDate <= result.releasedate <= endDate 
           		builder.add("result.releaseDate", ">=", startDate);
           		builder.add("result.releaseDate", "<=", endDate);
                
                // sort by condition, obr, obx, test result value
                builder.setSort("result.conditionName, result.obrAltCode, result.obxAltCode, result.testResultValue");
                builder.setSortAscending(true);

                // run query and iterate the results
                Query query = builder.getQuery(sessionFactory.getCurrentSession());
                Iterator<Object[]> results = query.iterate();
                while (results.hasNext() && rowsExtracted < maxRowsToExtract) {

                    row.clear();

                    Object[] tuple = results.next();
                    ReportableResult reportableResult = (ReportableResult) tuple[0];

                    // Extract columns from the reportable result
                    extractReportableResultForDaily(reportableResult);
                    
                    // Add additional columns
                    row.put(DailyExtractorFactory.COLUMN_ROW, new Long(rowsExtracted+1));
                    row.put(DailyExtractorFactory.COLUMN_TYPE, "R");
                    row.put(DailyExtractorFactory.COLUMN_REPORTABLE, "Y");

                    sink.append(row);
                    
                    rowsExtracted++;
                }
                
            	// Second, extract the decided results and send them all to the sink, because the primary sort
            	// for the report is type (with reportable results first, and decided results second)
            	
                builder = new HQLQueryBuilder("DecidedResult dr");
                
                // Add the filter: startDate <= dr.dateAdded <= endDate 
           		builder.add("dr.dateAdded", ">=", startDate);
           		builder.add("dr.dateAdded", "<=", endDate);

           		if (properties.get(DailyExtractorFactory.PROP_EXTRACTOR_REPORTABLE) != null) {
           			builder.add("dr.reportable", "=", properties.get(DailyExtractorFactory.PROP_EXTRACTOR_REPORTABLE));
           		}
           		
                // sort by condition, obr, obx, resultValue
                builder.setSort("dr.conditionName, dr.obr, dr.obx, dr.resultValue");
                builder.setSortAscending(true);

                // run query and iterate the results
                query = builder.getQuery(sessionFactory.getCurrentSession());
                Iterator<DecidedResult> resultsDR = query.list().iterator();
                while (resultsDR.hasNext() && rowsExtracted < maxRowsToExtract) {

                    row.clear();

                    DecidedResult decidedResult = (DecidedResult) resultsDR.next();

                    // Extract columns from the decided result
                    addCol(DailyExtractorFactory.COLUMN_OBR, decidedResult.getObr());
                    addCol(DailyExtractorFactory.COLUMN_OBR_TEXT, decidedResult.getObrText());
                    addCol(DailyExtractorFactory.COLUMN_OBX, decidedResult.getObx());
                    addCol(DailyExtractorFactory.COLUMN_OBX_TEXT, decidedResult.getObxText());
                    addCol(DailyExtractorFactory.COLUMN_TEST_RESULT_VALUE, decidedResult.getResultValue());
                    addCol(DailyExtractorFactory.COLUMN_NTE, decidedResult.getNte());
                    addCol(DailyExtractorFactory.COLUMN_CONDITION, decidedResult.getConditionName());
                    addCol(DailyExtractorFactory.COLUMN_LOINC, decidedResult.getLoincCode());
                    addCol(DailyExtractorFactory.COLUMN_ADDED, decidedResult.getDateAdded());
                    addCol(DailyExtractorFactory.COLUMN_MPQ_SEQ, decidedResult.getMpqSequenceNumber());
                    
                    // Add additional columns
                    row.put(DailyExtractorFactory.COLUMN_ROW, new Long(rowsExtracted+1));
                    row.put(DailyExtractorFactory.COLUMN_TYPE, "D");
                    row.put(DailyExtractorFactory.COLUMN_REPORTABLE, (decidedResult.getReportable().equals(ReportResultStatus.REPORT.getText()) ? "Y" : "N"));

                    sink.append(row);
                    
                    rowsExtracted++;
                }

                feedLog.setRowCount(rowsExtracted);
            }
            catch (Exception e) {

                logger.error("Exception: " + e.getMessage(), e);
                feedLog.error("Exception: " + e.getMessage());
            }
            
            logger.info("extract end: " + rowsExtracted + " rows extracted");

            row = null;
        }
        
        // ********************************
        // Private implementation methods *
        // ********************************

        /** Break the extraction configuration parameters from the task properties. */
        private void setup(Map<String,String> properties) {

            // Break out the properties
            incremental = MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_INCREMENTAL, false);
            maxRowsToExtract = MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_MAX_ROWS, Long.MAX_VALUE);
            includedInstitutionNames = MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_INST_TO_SEND);
            includedConditionNames = MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_SEND);
            excludedConditionNames = MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_COND_TO_NOT_SEND);
            recentResultInterval = MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL, 0);
            recentResultIntervalUnits = MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_RECENT_RESULT_INTERVAL_UNITS, "m");
            startDate = MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_START_DATE, DataFeedExtractorFactory.PROP_EXTRACTOR_START_TIME, "00:00:00", null);
            endDate = MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_END_DATE, DataFeedExtractorFactory.PROP_EXTRACTOR_END_TIME, "23:59:59", null);
            includedCounties = MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_COUNTIES);
            includedJurisdictions = MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_JURISDICTIONS);
            includedCodes = MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_CODES);
            excludedConditions = new ArrayList<String>();
            excludedConditions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION1, (String) null));
            excludedConditions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION2, (String) null));
            excludedConditions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION3, (String) null));
            excludedConditions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION4, (String) null));
            excludedConditions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CONDITION5, (String) null));
            excludedCodes = new ArrayList<Set<String>>();
            excludedCodes.add(MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES1));
            excludedCodes.add(MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES2));
            excludedCodes.add(MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES3));
            excludedCodes.add(MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES4));
            excludedCodes.add(MapUtilities.getCSVStringSet(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_CODES5));
            excludedInstitutions = new ArrayList<String>();
            excludedInstitutions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION1, (String) null));
            excludedInstitutions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION2, (String) null));
            excludedInstitutions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION3, (String) null));
            excludedInstitutions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION4, (String) null));
            excludedInstitutions.add(MapUtilities.get(properties, DataFeedExtractorFactory.PROP_EXTRACTOR_EXCLUDE_INSTITUTION5, (String) null));
        }

        /** Add a column/value pair to the row map if the value isn't null */
        private void addCol(String colName, Object value) {

            if (value != null) {
                
                row.put(colName, value);
            }
        }

        /** Extract export columns from the reportable result
         * @param reportableResult The reportable result to extract from. 
         */
        private void extractReportableResult(ReportableResult reportableResult) {

            HL7Producer appfac = reportableResult.getProducer();
            if (appfac != null) {
                addCol(DataFeedExtractorFactory.COLUMN_APPLICATION, appfac.getApplicationname());
                addCol(DataFeedExtractorFactory.COLUMN_FACILITY, appfac.getFacilityname());
            }

            Institution inst = reportableResult.getInstitution();
            if (inst != null) {
                addCol(DataFeedExtractorFactory.COLUMN_SOURCE_INSTITUTION, inst.getName());
            }
            
            if (reportableResult.getPatientBirth() != null) {
                addCol(DataFeedExtractorFactory.COLUMN_PAT_BIRTH, reportableResult.getPatientBirth());
            }

            if (reportableResult.getProviderBirth() != null) {
                addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_BIRTH, reportableResult.getProviderBirth());
            }
            
            if (reportableResult.getTestDate() != null) {
                addCol(DataFeedExtractorFactory.COLUMN_TEST_DATE, reportableResult.getTestDate());
            }
            
            Code testLoincCode = reportableResult.getCode();
            if (testLoincCode != null) {
                addCol(DataFeedExtractorFactory.COLUMN_TEST_LOINC_CODE, testLoincCode.getCode());
                addCol(DataFeedExtractorFactory.COLUMN_MAPPED_LOINC, testLoincCode.getId());
            }
            
            if (reportableResult.getMpqSeqNumber() != null) {
                addCol(DataFeedExtractorFactory.COLUMN_TEST_MPQ_SEQ_NUMBER, reportableResult.getMpqSeqNumber());
            }
            
            if (reportableResult.getTestPreviousDate() != null) {
                addCol(DataFeedExtractorFactory.COLUMN_TEST_PREVIOUS_DATE, reportableResult.getTestPreviousDate());
            }

            addCol(DataFeedExtractorFactory.COLUMN_UNIQUE_RECORD_NUM, reportableResult.getResultSeq().toString());
            addCol(DataFeedExtractorFactory.COLUMN_INSTITUTION_ID_TYPE, reportableResult.getInstitutionIdType());
            
            addCol(DataFeedExtractorFactory.COLUMN_PAT_INST_MED_REC_ID, reportableResult.getPatientInstitutionMedicalRecordId());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_GLOBAL_ID, reportableResult.getGlobalPatientId());
            //addCol(DataFeedExtractorFactory.COLUMN_UNIQUE_REGISTRY_NUM, ...);
            addCol(DataFeedExtractorFactory.COLUMN_PAT_SOCSEC, reportableResult.getPatientSSN());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_NAME, reportableResult.getPatientName());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_SEX, reportableResult.getPatientSex());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_RACE, reportableResult.getPatientRace());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_PHONE, reportableResult.getPatientPhone());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_STREET1, reportableResult.getPatientStreet1());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_STREET2, reportableResult.getPatientStreet2());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_CITY, reportableResult.getPatientCity());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_COUNTY, reportableResult.getPatientCounty());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_STATE, reportableResult.getPatientState());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_ZIP, reportableResult.getPatientZip());
            addCol(DataFeedExtractorFactory.COLUMN_PAT_COUNTRY, reportableResult.getPatientCountry());
            
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_NAME, reportableResult.getProviderName());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_NAME_MATCHED, reportableResult.getProviderNameMatched());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_SSN, reportableResult.getProviderSSN());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_PRACTICE, reportableResult.getProviderPractice());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_STREET, reportableResult.getProviderStreet());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_CITY, reportableResult.getProviderCity());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_STATE, reportableResult.getProviderState());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_ZIP, reportableResult.getProviderZip());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_COUNTY, reportableResult.getProviderCounty());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_PHONE, reportableResult.getProviderPhone());
            //addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_FAX, ...
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_LOCAL_ID, reportableResult.getProviderLocalId());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_DEA_NUM, reportableResult.getProviderDEANumber());
            addCol(DataFeedExtractorFactory.COLUMN_PROVIDER_LICENSE, reportableResult.getProviderLicense());
            
            addCol(DataFeedExtractorFactory.COLUMN_LAB_NAME, reportableResult.getLabName());
            addCol(DataFeedExtractorFactory.COLUMN_LAB_IDENTIFIER, reportableResult.getLabId());
            addCol(DataFeedExtractorFactory.COLUMN_LAB_PHONE, reportableResult.getLabPhone());
            addCol(DataFeedExtractorFactory.COLUMN_LAB_STREET1, reportableResult.getLabStreet1());
            addCol(DataFeedExtractorFactory.COLUMN_LAB_STREET2, reportableResult.getLabStreet2());
            addCol(DataFeedExtractorFactory.COLUMN_LAB_CITY, reportableResult.getLabCity());
            addCol(DataFeedExtractorFactory.COLUMN_LAB_STATE, reportableResult.getLabState());
            addCol(DataFeedExtractorFactory.COLUMN_LAB_ZIP, reportableResult.getLabZip());
            
            addCol(DataFeedExtractorFactory.COLUMN_TEST_IDENTIFIER, reportableResult.getTestId());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_NAME, reportableResult.getTestName());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_CODESYS, reportableResult.getTestCodeSystem());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_PLACER_ORDER_NUM, reportableResult.getTestPlacerOrderNum());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_FILLER_ORDER_NUM, reportableResult.getTestFillerOrderNum());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_PARENT_PLACER, reportableResult.getTestParentPlacer());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_PARENT_FILLER, reportableResult.getTestParentFiller());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_SPECIMEN_TEXT, reportableResult.getTestSpecimenText());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_DATA_TYPE, reportableResult.getTestDataType());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_NORMAL_RANGE, reportableResult.getTestNormalRange());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_ABNORMAL_FLAG, reportableResult.getTestAbnormalFlag());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_COMMENT, reportableResult.getTestComment());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RCVD_DATE_TIME, reportableResult.getMessageReceivedDateTime());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_IDENTIFIER, reportableResult.getTestResultId());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_NAME, reportableResult.getTestResultName());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODESYS, reportableResult.getTestResultCodeSystem());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_SUBID, reportableResult.getTestResultSubId());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_LOINC_CODE, reportableResult.getTestResultCode());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODE, reportableResult.getTestResultCode());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_VALUE, reportableResult.getTestResultValue());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_UNITS, reportableResult.getTestResultUnits());
            addCol(DataFeedExtractorFactory.COLUMN_TEST_RESULT_STATUS, reportableResult.getTestResultStatus());
            addCol(DataFeedExtractorFactory.COLUMN_DWYER_CONDITION_NAME, reportableResult.getConditionName());
            //addCol(DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_AGENCY, ...
            //addCol(DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_PATIENT_ID, ...
            //addCol(DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_CASE_ID, ...
            addCol(DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE, reportableResult.getObrAltCode());
            addCol(DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_TEXT, reportableResult.getObrAltCodeText());
            addCol(DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_SYS, reportableResult.getObrAltCodeSystem());
            addCol(DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE, reportableResult.getObxAltCode());
            addCol(DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_TEXT, reportableResult.getObxAltCodeText());
            addCol(DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_SYS, reportableResult.getObxAltCodeSystem());
            
            addCol(DataFeedExtractorFactory.COLUMN_OBR_SET_ID, reportableResult.getObrSetId());
            addCol(DataFeedExtractorFactory.COLUMN_OBX_START_SET_ID, reportableResult.getObxStartSetId());
            addCol(DataFeedExtractorFactory.COLUMN_OBX_END_SET_ID, reportableResult.getObxEndSetId());
        }

        /** Extract export columns from the raw HL7 message.
         * 
         * @param rawhl7 The raw HL7 message to extract from.
         */
        private void extractRawHL7(RawMessage rawhl7) {
            addCol(DataFeedExtractorFactory.COLUMN_HL7, rawhl7.getMessageText());
        }

        /** Extract export columns from the reportable result (minimized to those needed for the daily report)
         * @param reportableResult The reportable result to extract from. 
         */
        private void extractReportableResultForDaily(ReportableResult reportableResult) {

            addCol(DailyExtractorFactory.COLUMN_OBR, reportableResult.getTestId());
            addCol(DailyExtractorFactory.COLUMN_OBR_TEXT, reportableResult.getTestName());
            addCol(DailyExtractorFactory.COLUMN_OBX, reportableResult.getTestResultId());
            addCol(DailyExtractorFactory.COLUMN_OBX_TEXT, reportableResult.getTestResultName());
            addCol(DailyExtractorFactory.COLUMN_TEST_RESULT_VALUE, reportableResult.getTestResultValue());
            addCol(DailyExtractorFactory.COLUMN_NTE, reportableResult.getTestComment());
            addCol(DailyExtractorFactory.COLUMN_CONDITION, reportableResult.getConditionName());
           
            Code code = reportableResult.getCode();
            if (code != null) {
                addCol(DailyExtractorFactory.COLUMN_LOINC, code.getCode());
            }

            addCol(DailyExtractorFactory.COLUMN_ADDED, reportableResult.getReleaseDate());
            
            if (reportableResult.getMpqSeqNumber() != null) {
                addCol(DailyExtractorFactory.COLUMN_MPQ_SEQ, reportableResult.getMpqSeqNumber());
            }
        }
    }

    // **********************
    // Spring-only methods *
    // **********************

    /**
     * Set session factory. Spring calls this based on the
     * moduleApplicationContext.xml
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // *****************************************
    // Public interface implementation methods *
    // *****************************************

    /**
     * @see org.openmrs.module.ncd.database.dao.IExtractDAO#extractOld(java.lang.String, org.openmrs.module.ncd.output.extract.DataFeedSink)
     */
    public void extractOld(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status) {

        oneTimeInit();
    	ExtractDAOHelper helper = new ExtractDAOHelper();
    	helper.extract(properties, feedLog, sink, status);
    }
    
    public void extractDaily(Map<String,String> properties, DataFeedLog feedLog, DataFeedSink sink, TaskRunStatus status) {

    	ExtractDAOHelper helper = new ExtractDAOHelper();
    	helper.extractDaily(properties, feedLog, sink, status);
    }
    
    // ********************************
    // Private implementation methods *
    // ********************************
    
    /** One-time initialization method */
    private synchronized void oneTimeInit() {
        
        ConceptService conceptService = Context.getConceptService();
        
        if (resultSequenceConcept == null) {
            
            resultSequenceConcept = 
                conceptService.getConceptByName(NCDConcepts.RESULT_SEQUENCE_NUMBER);
        }
        
        if (conditionNameConcept == null) {
            
            conditionNameConcept = 
                conceptService.getConceptByName(NCDConcepts.CONDITION_NAME);
        }
        
        if (institutionNameConcept == null) {
            
            institutionNameConcept = 
                conceptService.getConceptByName(NCDConcepts.INSTITUTION_NAME);
        }

        if (rawHL7IdConcept == null) {
            
            rawHL7IdConcept = 
                conceptService.getConceptByName(NCDConcepts.RAW_HL7_ID);
        }

        // TODO: Is there some way to encode this mapping as Concept attributes?
        if (columnNameByConceptName == null) {
            
            columnNameByConceptName = new HashMap<String,String>();
            
            columnNameByConceptName.put(NCDConcepts.APPLICATION,
                    DataFeedExtractorFactory.COLUMN_APPLICATION);
            columnNameByConceptName.put(NCDConcepts.CONDITION_NAME,
                    DataFeedExtractorFactory.COLUMN_DWYER_CONDITION_NAME);
            columnNameByConceptName.put(NCDConcepts.FACILITY,
                    DataFeedExtractorFactory.COLUMN_FACILITY);
            columnNameByConceptName.put(NCDConcepts.HEALTH_DEPT_AGENCY,
                    DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_AGENCY);
            columnNameByConceptName.put(NCDConcepts.HEALTH_DEPT_CASE_ID,
                    DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_CASE_ID);
            columnNameByConceptName.put(NCDConcepts.INSTITUTION_ID_TYPE,
                    DataFeedExtractorFactory.COLUMN_INSTITUTION_ID_TYPE);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_CITY,
                    DataFeedExtractorFactory.COLUMN_LAB_CITY);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_ID,
                    DataFeedExtractorFactory.COLUMN_LAB_IDENTIFIER);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_NAME,
                    DataFeedExtractorFactory.COLUMN_LAB_NAME);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_PHONE,
                    DataFeedExtractorFactory.COLUMN_LAB_PHONE);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_STATE,
                    DataFeedExtractorFactory.COLUMN_LAB_STATE);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_ADDRESS1,
                    DataFeedExtractorFactory.COLUMN_LAB_STREET1);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_ADDRESS2,
                    DataFeedExtractorFactory.COLUMN_LAB_STREET2);
            columnNameByConceptName.put(NCDConcepts.LABORATORY_ZIP,
                    DataFeedExtractorFactory.COLUMN_LAB_ZIP);
            columnNameByConceptName.put(NCDConcepts.LOINC_CODE_ID,
                    DataFeedExtractorFactory.COLUMN_MAPPED_LOINC);
            columnNameByConceptName.put(NCDConcepts.OBR_ALT_CODE,
                    DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE);
            columnNameByConceptName.put(NCDConcepts.OBR_ALT_CODE_SYS,
                    DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_SYS);
            columnNameByConceptName.put(NCDConcepts.OBR_ALT_CODE_TEXT,
                    DataFeedExtractorFactory.COLUMN_OBR_ALT_CODE_TEXT);
            columnNameByConceptName.put(NCDConcepts.OBX_ALT_CODE,
                    DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE);
            columnNameByConceptName.put(NCDConcepts.OBX_ALT_CODE_SYS,
                    DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_SYS);
            columnNameByConceptName.put(NCDConcepts.OBX_ALT_CODE_TEXT,
                    DataFeedExtractorFactory.COLUMN_OBX_ALT_CODE_TEXT);
            columnNameByConceptName.put(NCDConcepts.INSTITUTION_NAME,
                    DataFeedExtractorFactory.COLUMN_SOURCE_INSTITUTION);
            columnNameByConceptName.put(NCDConcepts.TEST_ABNORMAL_FLAG,
                    DataFeedExtractorFactory.COLUMN_TEST_ABNORMAL_FLAG);
            columnNameByConceptName.put(NCDConcepts.TEST_CODE_SYSTEM,
                    DataFeedExtractorFactory.COLUMN_TEST_CODESYS);
            columnNameByConceptName.put(NCDConcepts.TEST_COMMENT,
                    DataFeedExtractorFactory.COLUMN_TEST_COMMENT);
            columnNameByConceptName.put(NCDConcepts.TEST_DATA_TYPE,
                    DataFeedExtractorFactory.COLUMN_TEST_DATA_TYPE);
            columnNameByConceptName.put(NCDConcepts.TEST_DATE,
                    DataFeedExtractorFactory.COLUMN_TEST_DATE);
            columnNameByConceptName.put(NCDConcepts.TEST_FILLER_ORDER_NUMBER,
                    DataFeedExtractorFactory.COLUMN_TEST_FILLER_ORDER_NUM);
            columnNameByConceptName.put(NCDConcepts.TEST_ID,
                    DataFeedExtractorFactory.COLUMN_TEST_IDENTIFIER);
            columnNameByConceptName.put(NCDConcepts.TEST_ID,
                    DataFeedExtractorFactory.COLUMN_TEST_LOINC_CODE);
            columnNameByConceptName.put(NCDConcepts.TEST_MPQ_SEQ_NUMBER,
                    DataFeedExtractorFactory.COLUMN_TEST_MPQ_SEQ_NUMBER);
            columnNameByConceptName.put(NCDConcepts.TEST_NAME,
                    DataFeedExtractorFactory.COLUMN_TEST_NAME);
            columnNameByConceptName.put(NCDConcepts.TEST_NORMAL_RANGE,
                    DataFeedExtractorFactory.COLUMN_TEST_NORMAL_RANGE);
            columnNameByConceptName.put(NCDConcepts.TEST_PARENT_FILLER,
                    DataFeedExtractorFactory.COLUMN_TEST_PARENT_FILLER);
            columnNameByConceptName.put(NCDConcepts.TEST_PARENT_PLACER,
                    DataFeedExtractorFactory.COLUMN_TEST_PARENT_PLACER);
            columnNameByConceptName.put(NCDConcepts.TEST_PLACER_ORDER_NUMBER,
                    DataFeedExtractorFactory.COLUMN_TEST_PLACER_ORDER_NUM);
            columnNameByConceptName.put(NCDConcepts.TEST_PREVIOUS_DATE,
                    DataFeedExtractorFactory.COLUMN_TEST_PREVIOUS_DATE);
            columnNameByConceptName.put(NCDConcepts.TEST_RECEIVED_DATE_TIME,
                    DataFeedExtractorFactory.COLUMN_TEST_RCVD_DATE_TIME);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_CODE,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODE);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_CODE_SYSTEM,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_CODESYS);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_ID,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_IDENTIFIER);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_CODE,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_LOINC_CODE);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_NAME,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_NAME);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_STATUS,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_STATUS);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_SUB_ID,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_SUBID);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_UNITS,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_UNITS);
            columnNameByConceptName.put(NCDConcepts.TEST_RESULT_VALUE,
                    DataFeedExtractorFactory.COLUMN_TEST_RESULT_VALUE);
            columnNameByConceptName.put(NCDConcepts.TEST_SPECIMEN_TEXT,
                    DataFeedExtractorFactory.COLUMN_TEST_SPECIMEN_TEXT);
            columnNameByConceptName.put(NCDConcepts.RESULT_SEQUENCE_NUMBER,
                    DataFeedExtractorFactory.COLUMN_UNIQUE_RECORD_NUM);
            columnNameByConceptName.put(NCDConcepts.UNIQUE_REGISTRY_NUM,
                    DataFeedExtractorFactory.COLUMN_UNIQUE_REGISTRY_NUM);
        }

        if (columnNameByPatientAttrName == null) {
            
            columnNameByPatientAttrName = new HashMap<String, String>();
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_GLOBAL_PATIENT_ID,
                    DataFeedExtractorFactory.COLUMN_PAT_GLOBAL_ID);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PATIENT_MEDREC_ID,
                    DataFeedExtractorFactory.COLUMN_PAT_INST_MED_REC_ID);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PHONE_NUMBER,
                    DataFeedExtractorFactory.COLUMN_PAT_PHONE);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_RACE,
                    DataFeedExtractorFactory.COLUMN_PAT_RACE);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_SSN,
                    DataFeedExtractorFactory.COLUMN_PAT_SOCSEC);
            columnNameByPatientAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_HEALTH_DEPT_PATIENT_ID,
                    DataFeedExtractorFactory.COLUMN_HEALTH_DEPT_PATIENT_ID);
        }
        
        if (columnNameByProviderAttrName == null) {
            
            columnNameByProviderAttrName = new HashMap<String, String>();
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_LOCAL_ID,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_LOCAL_ID);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PHONE_NUMBER,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_PHONE);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_SSN,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_SSN);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_BIRTH,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_BIRTH);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_DEA_NUM,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_DEA_NUM);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_FAX,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_FAX);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_LICENSE,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_LICENSE);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_NAME_MATCHED,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_NAME_MATCHED);
            columnNameByProviderAttrName.put(PersonAttributeTypeCache.PERSON_ATTR_TYPE_PROVIDER_PRACTICE,
                    DataFeedExtractorFactory.COLUMN_PROVIDER_PRACTICE);
        }
    }
}

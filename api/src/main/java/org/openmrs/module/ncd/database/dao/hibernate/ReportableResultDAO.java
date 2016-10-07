/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.database.dao.hibernate;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.AlertSummary;
import org.openmrs.module.ncd.database.Code;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.database.dao.IReportableResultDAO;
import org.openmrs.module.ncd.database.filter.SearchFilterAlertSummary;
import org.openmrs.module.ncd.database.filter.SearchFilterReportableResults;
import org.openmrs.module.ncd.database.filter.SearchResult;
import org.openmrs.module.ncd.output.extract.DataFeedExtractorFactory;
import org.openmrs.module.ncd.output.extract.DataFeedLog;
import org.openmrs.module.ncd.output.extract.DataFeedSinkDelimited;
import org.openmrs.module.ncd.output.extract.DataFeedSinkFactory;
import org.openmrs.module.ncd.output.extract.ReportableResultExtractor;
import org.openmrs.module.ncd.storage.DecidedResultStorageHelper;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageService;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class ReportableResultDAO implements IReportableResultDAO
{
    /** Debugging log */
    private static Log log = LogFactory.getLog(ReportableResultDAO.class);

    /** Hibernate session factory, set by spring. */
    private SessionFactory sessionFactory;

    /**
     * Set session factory. Spring calls this based on the
     * moduleApplicationContext.xml
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.database.dao.IReportableResultDAO#saveReportableResult(org.openmrs.module.ncd.database.ReportableResult)
     */
    public Serializable saveReportableResults(List<ReportableResult> reportableResult)
    {
        // TODO: Is there a more efficient way to do this?
        Session session = sessionFactory.getCurrentSession();
        session.clear();
        
        try
        {
            for (ReportableResult result : reportableResult) 
            {
                Code reportableResultCode   = result.getCode();
                Long codeId                 = reportableResultCode.getId();
                String code                 = reportableResultCode.getCode();
                ReportableResultDAO.log.info( "\nReportableResultDAO.saveReportableResults() about to save code=" + code + " (" + codeId + ")" );
                
                try
                {
                    Query query                             = session.createQuery( "from CodeCondition where codeid = :codeid").setParameter( "codeid", codeId );
                    List<CodeCondition> foundConditions     = query.list();
                
                    if( foundConditions == null || foundConditions.size() == 0 )
                    {
                        ReportableResultDAO.log.info( "\nReportableResultDAO.saveReportableResults() no foundConditions" );
                    }
                    else
                    {
                        Iterator<CodeCondition> foundConditionsIt = foundConditions.iterator();
                        
                        while( foundConditionsIt.hasNext() )
                        {
                            CodeCondition aCodeCondition    = foundConditionsIt.next();
                            Long aCodeConditionId           = aCodeCondition.getId();
                            
                            reportableResultCode.setId( aCodeConditionId );
                            break;
                        }
                    }
                }
                catch( Exception ee )
                {
                    ReportableResultDAO.log.info( "ReportableResultDAO.saveReportableResults() threw an Exception while attempting to get the targetCode, codeId=" + codeId );
                }
                
                session.saveOrUpdate(result);
            }
        }
        catch( Exception e )
        {
            ReportableResultDAO.log.error( "ReportableResultsDAO.saveReportableResults() threw an exception e=" + e );
        }
        
        return null;
    }

    public ReportableResult getReportableResult(long id) 
    {
        Session dbSession       = sessionFactory.getCurrentSession();
        Query query             = dbSession.createQuery("from ReportableResult where id = :id").setParameter("id", id);
        ReportableResult result = (ReportableResult) query.uniqueResult();
        
        return result;
    }

    /*
     * TODO: determine how to extend this to exporting in forms other
     * than delimited text (CSV).
     */
    public String exportReportableResults(SearchFilterReportableResults filter) 
    { 
        log.debug("export begin");

        SearchResult<ReportableResult> results  = findReportableResults(filter);
        log.debug("search successful: " + results.toString());

        Map<String, String> properties          = new HashMap<String, String>();
        properties.put(DataFeedSinkFactory.PROP_SINK_OUTPUT_COLS_TO_INCL, DataFeedExtractorFactory.getOutputColumnNames());
        ReportableResultExtractor extractor     = new ReportableResultExtractor(properties);
        StringWriter out                        = new StringWriter();
        DataFeedLog feedLog                     = new DataFeedLog();
        DataFeedSinkDelimited feedSink          = new DataFeedSinkDelimited();
        feedSink.create(out, properties, feedLog);

        for (ReportableResult result : results.getResultRows()) 
        {
            Map<String, Object> row = extractor.extract(result);
            feedSink.append(row);
        }
        
        log.debug("export end");

        return out.toString();
    }

    @SuppressWarnings("unchecked")
    public SearchResult<ReportableResult> findReportableResults(SearchFilterReportableResults filter) 
    {
        Session dbSession                       = sessionFactory.getCurrentSession();
        SearchResult<ReportableResult> results  = new SearchResult<ReportableResult>();
        results.setSuccessful(false);
        
        try 
        {
            HQLQueryBuilder builder = new HQLQueryBuilder("rr", "ReportableResult rr");
            
            if (filter.getCritic().isSet()) 
            {
                builder.addJoin("left join rr.decidedResult as dr");
            }
            
            builder.addPrefetchJoin("left join fetch rr.jurisdiction");
            builder.addPrefetchJoin("left join fetch rr.institution");
            builder.addPrefetchJoin("left join fetch rr.decidedResult");
            builder.addPrefetchJoin("left join fetch rr.critic");
            builder.addPrefetchJoin("left join fetch rr.county");
            builder.addPrefetchJoin("left join fetch rr.code");
            
            builder.add("rr.conditionName", filter.getConditionName());
            builder.add("rr.manualReviewStatusType.id", filter.getReviewStatus());
            builder.add("rr.releaseDate", filter.getReleaseDate());
            builder.add("rr.sentInError", filter.getSentInError());

            builder.add("rr.code.code", filter.getLoinc());
            builder.add("rr.institution.name", filter.getInstitution());
            builder.add("rr.sendingApplication", filter.getSendingApplication());
            builder.add("rr.sendingFacility", filter.getSendingFacility());
            builder.add("rr.sendingLocation", filter.getSendingLocation());
            builder.add("rr.county.county", filter.getCounty());
            builder.add("rr.jurisdiction.jurisdiction", filter.getJurisdiction());
            builder.add("rr.institutionIdType", filter.getInstitutionidtype());
            
            builder.add("rr.patientInstitutionMedicalRecordId", filter.getPatinstmedrecid());
            builder.add("rr.globalPatientId", filter.getGlobalpatientid());
            builder.add("rr.patientSSN", filter.getPatientssn());
            builder.add("rr.patientName", filter.getPatientname());
            builder.add("rr.patientBirth", filter.getPatientbirth());
            builder.add("rr.patientRace", filter.getPatientrace());
            builder.add("rr.patientPhone", filter.getPatientphone());
            builder.add("rr.patientStreet1", filter.getPatientstreet1());
            builder.add("rr.patientStreet2", filter.getPatientstreet2());
            builder.add("rr.patientCity", filter.getPatientcity());
            builder.add("rr.patientCounty", filter.getPatientcounty());
            builder.add("rr.patientState", filter.getPatientstate());
            builder.add("rr.patientZip", filter.getPatientzip());
            builder.add("rr.patientCountry", filter.getPatientcountry());
            builder.add("rr.patientSex", filter.getPatientsex());
            
            builder.add("rr.providerName", filter.getProvidername());
            builder.add("rr.providerNameMatched", filter.getProvidernamematched());
            builder.add("rr.providerSSN", filter.getProviderssn());
            builder.add("rr.providerBirth", filter.getProviderbirth());
            builder.add("rr.providerPractice", filter.getProviderpractice());
            builder.add("rr.providerStreet", filter.getProviderstreet());
            builder.add("rr.providerCity", filter.getProvidercity());
            builder.add("rr.providerState", filter.getProviderstate());
            builder.add("rr.providerZip", filter.getProviderzip());
            builder.add("rr.providerCounty", filter.getProvidercounty());
            builder.add("rr.providerPhone", filter.getProviderphone());
            builder.add("rr.providerLocalId", filter.getProviderlocalid());
            builder.add("rr.providerDEANumber", filter.getProviderdeanumber());
            builder.add("rr.providerLicense", filter.getProviderlicense());
            builder.add("rr.providerNameSource", filter.getProvidernamesource());
            builder.add("rr.providerAddressSource", filter.getProvideraddresssource());
            builder.add("rr.providerLocalIdSource", filter.getProviderlocalidsource());
            
            builder.add("rr.labName", filter.getLabname());
            builder.add("rr.labId", filter.getLabid());
            builder.add("rr.labPhone", filter.getLabphone());
            builder.add("rr.labStreet1", filter.getLabstreet1());
            builder.add("rr.labStreet2", filter.getLabstreet2());
            builder.add("rr.labCity", filter.getLabcity());
            builder.add("rr.labState", filter.getLabstate());
            builder.add("rr.labZip", filter.getLabzip());
            
            builder.add("rr.testId", filter.getTestid());
            builder.add("rr.testName", filter.getTestname());
            builder.add("rr.testCodeSystem", filter.getTestcodesys());
            builder.add("rr.testPlacerOrderNum", filter.getTestplacerordernum());
            builder.add("rr.testFillerOrderNum", filter.getTestfillerordernum());
            builder.add("rr.testDate", filter.getTestdate());
            builder.add("rr.testDateSource", filter.getTestdatesource());
            builder.add("rr.testParentPlacer", filter.getTestparentplacer());
            builder.add("rr.testParentFiller", filter.getTestparentfiller());
            builder.add("rr.testSpecimenText", filter.getTestspecimentext());
            builder.add("rr.testDataType", filter.getTestdatatype());
            builder.add("rr.testNormalRange", filter.getTestnormalrange());
            builder.add("rr.testAbnormalFlag", filter.getTestabnormalflag());
            builder.add("rr.testComment", filter.getTestcomment());
            builder.add("rr.messageReceivedDateTime", filter.getMessageReceivedDateTime());
            builder.add("rr.mpqSeqNumber", filter.getMpqSeqNumber());
            builder.add("rr.testResultId", filter.getTestresultid());
            builder.add("rr.testResultName", filter.getTestresultname());
            builder.add("rr.testResultCodeSystem", filter.getTestresultcodesys());
            builder.add("rr.testResultSubId", filter.getTestresultsubid());
            builder.add("rr.testResultCode", filter.getTestresultcode());
            builder.add("rr.testResultValue", filter.getTestresultvalue());
            builder.add("rr.testResultUnits", filter.getTestresultunits());
            builder.add("rr.testPreviousDate", filter.getTestpreviousdate());
            builder.add("rr.testPlacerOrderNumSource", filter.getTestplacerordernumsource());
            builder.add("rr.testFillerOrderNumSource", filter.getTestfillerordernumsource());
            builder.add("rr.testResultStatus", filter.getTestresultstatus());
            
            builder.add("rr.obrAltCode", filter.getObraltcode());
            builder.add("rr.obrAltCodeText", filter.getObraltcodetext());
            builder.add("rr.obrAltCodeSystem", filter.getObraltcodesys());
            builder.add("rr.obxAltCode", filter.getObxaltcode());
            builder.add("rr.obxAltCodeText", filter.getObxaltcodetext());
            builder.add("rr.obxAltCodeSystem", filter.getObxaltcodesys());
            builder.add("rr.obrSetId", filter.getObrSetId());
            builder.add("rr.obxStartSetId", filter.getObxStartSetId());
            builder.add("rr.obxEndSetId", filter.getObxEndSetId());
            
            if (filter.getCritic().isSet()) 
            {
                builder.addOr("rr.critic.name", "=", filter.getCritic().getValue(), "dr.classifiedByWhom", "=", filter.getCritic().getValue());
            }

            if (filter.getSortFieldName() != null) 
            {    
                builder.setSort("rr." + filter.getSortFieldName());
                builder.setSortAscending(filter.isSortAscending());
            }

            Query query = builder.getQuery(dbSession).setMaxResults(filter.getMaxRows() + 1);
            
            List<ReportableResult> rows = (List<ReportableResult>) query.list();
            
            results.setSuccessful(true);
            results.setThrowable(null);
            results.setLimited(rows.size() > filter.getMaxRows());
            results.setRowCount(rows.size());   // bogus
            
            if (results.isLimited()) 
            {    
                results.setResultRows(rows.subList(0, filter.getMaxRows()));

                // Rerun the query without the row limit, only
                // counting rows, to correctly set rowCount.

                Query query2 = builder.getCountQuery(dbSession);
                results.setRowCount((Long) query2.uniqueResult());
            }
            else 
            {
                results.setResultRows(rows);
            }
        }
        catch (Exception e) 
        {
            results.setSuccessful(false);
            results.setThrowable(e);
            results.setLimited(false);
            results.setRowCount(0);
            results.setResultRows(new ArrayList<ReportableResult>());
            
            log.error("exception: " + e.getMessage(), e);
        }

        return results;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.database.dao.IReportableResultDAO#rejectReportableResult(org.openmrs.module.ncd.database.ReportableResult)
     */
    public void rejectReportableResult(ReportableResult result) 
    {    
        reportOrRejectReportableResult(
                result, 
                ConditionDetectorService.reviewStatusTypeRejected, 
                null, 
                "Reviewed and rejected");
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.database.dao.IReportableResultDAO#releaseReportableResult(org.openmrs.module.ncd.database.ReportableResult)
     */
    public void releaseReportableResult(ReportableResult result) 
    {    
        reportOrRejectReportableResult(
                result, 
                ConditionDetectorService.reviewStatusTypeReleased, 
                new Date(), 
                "Reviewed and reported");
    }
    
    private void reportOrRejectReportableResult(ReportableResult result, int reviewStatus, Date releaseDate, String dismissReason) 
    {    
        log.info("enter");
        
        ConditionDetectorService cds = NCDUtilities.getService();
        Session session = sessionFactory.getCurrentSession();
        
        // Set the manual review status
        result.setManualReviewStatusType(cds.findReviewStatusTypeById(reviewStatus));

        if (releaseDate != null) 
        {    
            // Set the release date
            result.setReleaseDate(new Date());
        }
        
        // Assign a new resultSeq
        long resultSeq = NCDUtilities.nextReportableResultSeq();
        result.setResultSeq(resultSeq);

        try 
        {
            // Create, attach and save a decided result
            result.setDecidedResult(DecidedResultStorageHelper.storeReviewedDecidedResult(result));
        }
        catch (Exception e) 
        {    
            log.error("Exception: ", e);
        }
        
        // Save it
        session.saveOrUpdate(result);
        
        // Find the associated, undismissed, dashboard alert summary by identity
        SearchFilterAlertSummary filter = new SearchFilterAlertSummary();
        filter.getDismissed().setValue("false");
        filter.setIdentity(ConditionDetectorService.alertIdentityManualReview + "[id=" + result.getId() + "]");
        SearchResult<AlertSummary> alertSummaries = cds.findAlertSummaries(filter);
        
        // Dismiss the associated alert summary (filter will return 1 in a list)
        cds.dismissAlertSummaries(alertSummaries.getResultRows(), dismissReason);

        log.info("exit");
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.database.dao.IReportableResultDAO#reportableResultSentInError(List<org.openmrs.module.ncd.database.ReportableResult>)
     */
    @SuppressWarnings("deprecation")
    public void reportableResultSentInError(List<ReportableResult> results) 
    {
        Session session = sessionFactory.getCurrentSession();
        
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        
        for (ReportableResult result : results) 
        {	
        	if (!result.getSentInError()) 
        	{	
        		result.setSentInError(true);
        		
        		if (sb.length() > 1) 
        		{
        			sb.append(",");
        		}
        		
        		sb.append(result.getId().toString());
        		
		    	// Save the reportable result changes
		    	session.saveOrUpdate(result);
        	}
        }

        sb.append(")");
        String sentInErrorInList = sb.toString();
        
        // Query to find the set of distinct combinations of recipient email address and
        // reportable result resultSeq for those recipients who received notification of a
        // successful export containing the reportable results that were sent in error.
        String queryText =
        	"select recipient.recipientEmail, rr.resultSeq as resultSeq, count(*) as instances" +
        	" from ncd_task_run_status status" +
        	" join ncd_export_recipient recipient on (recipient.taskrunstatusid = status.id)" +
        	" join ncd_exported_result result on (result.taskrunstatusid = status.id)" +
        	" join ncd_reportable_result rr on (rr.id = result.reportableResultId)" +
        	" where status.succeeded=1" +
        	" and rr.id in " + sentInErrorInList +
        	" group by recipient.recipientEmail, rr.resultSeq";

        Query query = session.createSQLQuery(queryText)
                             .addScalar("RECIPIENTEMAIL", Hibernate.STRING)
                             .addScalar("RESULTSEQ", Hibernate.LONG)
                             .addScalar("INSTANCES", Hibernate.INTEGER);

        @SuppressWarnings("unchecked")
        Iterator<Object[]> queryResults = (Iterator<Object[]>) query.list().iterator();
        
        // Allocate a map that will contain the mapping from export recipient email address,
        // and the set of reportable results flagged as sent in error which were present in an
        // export that was completed sucessfully, and notified to the recipient.
        //
        // key = export recipient's email address
        // value = array of reportable result resultSeq's that were sent in error to this recipient
        Map<String, ArrayList<Long>> reportableResultRecipients = new HashMap<String, ArrayList<Long>>();

        // For each query result row
        while (queryResults.hasNext()) 
        {    
            Object[] result         = queryResults.next();   
            String recipientEmail   = (String) result[0];
            Long resultSeq          = (Long) result[1];
            Integer count           = (Integer) result[2];
            
            log.debug("adding (" 
                    + recipientEmail 
                    + "," 
                    + resultSeq.toString() 
                    + "," 
                    + count 
                    + ").");

            ArrayList<Long> resultSeqs = reportableResultRecipients.get(recipientEmail);
            
            if (resultSeqs == null) 
            {
            	resultSeqs = new ArrayList<Long>();
            	reportableResultRecipients.put(recipientEmail, resultSeqs);
            }
            
            resultSeqs.add(resultSeq);
        }
        
        // For each recipient
        MessageService messageService           = Context.getMessageService();
		AdministrationService adminService      = Context.getAdministrationService();
		String sentInErrorSender                = adminService.getGlobalProperty("ncd.sentInErrorSender");
        String sentInErrorSubject               = adminService.getGlobalProperty("ncd.sentInErrorSubject");
        Set<String> recipientEmails             = reportableResultRecipients.keySet();
        
        for (String recipientEmail : recipientEmails) 
        {	
        	// Construct the sent in error notification message for this recipient
        	sb = new StringBuilder();
        	sb.append("To Whom It May Concern:\n\nThe Notifiable Condition Detector (NCD) previously notified you about one or more exports containing reportable results with the following identifiers (UNIQUE_RECORD_NUM):\n\n");
        	
        	for (Long id : reportableResultRecipients.get(recipientEmail)) 
        	{
        		sb.append("\t" + id.toString() + "\n");
        	}
        	
        	sb.append("\nPlease be advised that the reportable results listed above were sent in error.\n");
        	
        	// Email the sent in error notification to the recipient.
        	//
        	// This is done on a best effort basis, with no further retry or notifications beyond what
        	// is built into the message service itself and/or providing by the SMTP relay.
    		
        	try 
        	{
    			// Use the OpenMRS message service to create and send the email
    			String message = sb.toString();
    			messageService.sendMessage(messageService.createMessage(recipientEmail, sentInErrorSender, sentInErrorSubject, message));
    		}
    		catch (MessageException me) 
        	{
    			log.warn("Error sending 'sent in error' email to: " + recipientEmail);
                log.warn("Error sending 'sent in error' email: " + me.getMessage(), me);
    		}
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmrs.module.ncd.database.dao.IReportableResultDAO#reportableResultNotSentInError(List<org.openmrs.module.ncd.database.ReportableResult>)
     */
    public void reportableResultNotSentInError(List<ReportableResult> results) 
    {
        Session session = sessionFactory.getCurrentSession();
        
        for (ReportableResult result : results) 
        {	
        	if (result.getSentInError()) 
        	{	
        		result.setSentInError(false);
        		
		    	// Save the reportable result changes
		    	session.saveOrUpdate(result);
        	}
        }
    }
}

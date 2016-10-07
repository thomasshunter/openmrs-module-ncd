package org.openmrs.module.ncd.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSet;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.critic.IResultsCritic;
import org.openmrs.module.ncd.events.PropertyEventDispatcher;
import org.openmrs.module.ncd.events.PropertyEventHandler;
import org.openmrs.module.ncd.events.SimplePropertyEventHandler;
import org.openmrs.module.ncd.jurisdiction.JurisdictionAlgorithm;
import org.openmrs.module.ncd.nlp.INlpAnalyzer;
import org.openmrs.module.ncd.preprocessing.MessagePreProcessor;
import org.openmrs.module.ncd.storage.CodeFrequencyStorageException;

/**
 * Utility methods related to the NCD as an OpenMRS module.
 * 
 * TODO: This class is mostly a collection of static global variables. Think of a
 * better way.
 * 
 * @author Erik Horstkotte
 * @author andyt
 */
public class NCDUtilities 
{
    private static Log log                                              = LogFactory.getLog(NCDUtilities.class);

    /** The OpenMRS global property event dispatcher */ 
    private static PropertyEventDispatcher dispatcher                   = new PropertyEventDispatcher();

    /** Set once NCDUtilities.startup() has been called, at which point the
     * NCD service should be available */
    private static boolean started                                      = false;

    private static List<String> reportableAbnormalFlags                 = null;
    private static List<String> notReportableAbnormalFlags              = null;
    private static int maxLoincFrequencyCacheSize                       = 25000;
    /** The cached value of the global property controlling whether NTE segments are processed */
    private static boolean nteProcessingFlagOn                          = false;
    /** The cached value of the global property controlling how much of a condition text must be equal to be considered a match. */
    private static int matchThresholdPercentage                         = 80;
    /** The cached value of the global property controlling the list of negation strings recognized. */
    private static List<String> negationStrings                         = null;
    /** HL7 message types that MessageTypeCritic will accept */
    private static List<String> allowableHL7MessageTypes                = null;
    private static List<String> allowedProcessingIds                    = null;
    private static HashMap<String, INlpAnalyzer> nlpAnalyzer            = null;
    private static List<String> candidateSegmentNames                   = null;
    /** The collection of configured message critics */
    private static ArrayList<IResultsCritic> messageCritics             = null;
    /** The collection of configured results critics */
    private static ArrayList<IResultsCritic> resultsCritics             = null;
    /** The collection of message pre-processors. */
    private static ArrayList<MessagePreProcessor> messagePreProcessors  = null;
    /** The comma-separated list of result finder map entries */
    private static String resultFinderMapText                           = null;
    /** The next "reportable result sequence number" value */
    private static GlobalProperty reportableResultSeqProp               = null;
    private static long reportableResultSeq                             = 1;
    private static JurisdictionAlgorithm jurisdictionAlgorithm          = null;
    private static String dataTypeTransforms                            = null;
    private static Map<String, String> resultSegmentMap                 = null;
    private static String messageTypeTransforms                         = null;
    public static final String ROWS_PER_PAGE_PROPNAME                   = "ncd.rowsPerPage";
    public static final String MAX_ROWS_TO_FETCH_PROPNAME               = "ncd.maxRowsToFetch";

          
    /** Authenticate to the configured ncd service, if possible */
    public static void authenticate() 
    {    
    	log.info("authenticate: enter");
     
    	if (Context.isAuthenticated()) 
    	{
            log.debug("already authenticated, not authenticating again");
        }
        else 
        {
            try 
            {
                AdministrationService adminService  = Context.getAdministrationService();
                String username                     = adminService.getGlobalProperty("ncd.username");
                String password                     = adminService.getGlobalProperty("ncd.password");
                Context.authenticate(username, password);    
            }
            catch (Throwable t) 
            {
                log.error("Authentication failed. Did you set up the ncd user?");
            }
        }
    	
        log.info("authenticate: exit");
    }

    /** Get and return the configured ncd user, if possible */
    public static User getNcdUser() 
    {	
    	log.info("getNcdUser: enter");
    	User user = null;
        
    	try 
    	{
            AdministrationService adminService  = Context.getAdministrationService();
            String username                     = adminService.getGlobalProperty("ncd.username");
            UserService userService             = Context.getUserService();
            user                                = userService.getUserByUsername( username );
        }
        catch (Throwable t) 
    	{
            log.error("Lookup of ncd.username via UserService failed. Did you set up the ncd user?");
        }
        finally 
        {
	        log.info("getNcdUser: exit");
        }
        
    	return user;
    }
    
    /** Gets the current ConditionDetectorService instance, purely as a shorthand */
    public static ConditionDetectorService getService() 
    {    
        return (ConditionDetectorService) Context.getService(ConditionDetectorService.class);
    }

    /**
     * Adds a property event handler to the dispatcher.
     * 
     * @param propertyName The name of the global property to listen for changes
     *        to.
     * @param handler The class to handle changes to the property.
     */
    public static void addPropertyEventHandler(String propertyName, PropertyEventHandler handler) 
    {
        dispatcher.add(propertyName, handler);
    }

    /**
     * Adds a property event handler to the dispatcher.
     * 
     * @param propertyName The name of the global property to listen for changes
     *        to.
     * @param handler The class to handle changes to the property.
     */
    public static void addPropertyEventHandler(String propertyName, PropertyEventHandler handler, AdministrationService adminService) 
    {
        dispatcher.add(propertyName, handler, adminService);
    }
    
    // Getters, storage, and change handlers for simple global properties.
    // WARNING: Don't add handling for a global property here unless no other
    // code needs to be actively notified that the value of the property has changed.
    public static List<String> getReportableAbnormalFlags() 
    {
        return reportableAbnormalFlags;
    } 
    public static void setReportableAbnormalFlags(List<String> value) 
    {
        reportableAbnormalFlags = value;
    }
    
    private static class ReportableAbnormalFlagsHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            reportableAbnormalFlags = csvToStringList(arg0.getPropertyValue());
        }
    }
    
    public static List<String> getNotReportableAbnormalFlags() 
    {
        return notReportableAbnormalFlags;
    }
    public static void setNotReportableAbnormalFlags(List<String> value) 
    {
        notReportableAbnormalFlags = value;
    }
    
    private static class NotReportableAbnormalFlagsHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            notReportableAbnormalFlags = csvToStringList(arg0.getPropertyValue());
        }
    }

    public static int getMaxLoincFrequencyCacheSize() 
    {
        return maxLoincFrequencyCacheSize;
    }
    public static void setMaxLoincFrequencyCacheSize(int value) 
    {
        maxLoincFrequencyCacheSize = value;
    }
    
    private static class MaxLoincFrequencyCacheSizeHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            try 
            {
                maxLoincFrequencyCacheSize = Integer.parseInt(arg0.getPropertyValue());
            }
            catch (Exception e) 
            {
                log.error("expected the value of global property " + arg0.getProperty() + " to be an integer: " + e.getMessage(), e);
            }
        }
    }

    public static boolean isNTEProcessingFlagOn() 
    {
        return nteProcessingFlagOn;
    }
    public static void setNTEProcessingFlag(boolean value) 
    {
        nteProcessingFlagOn = value;
    }
    
    private static class NTEProcessingFlagHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            try 
            {
                nteProcessingFlagOn = Boolean.parseBoolean(arg0.getPropertyValue());
            } 
            catch (Exception e) 
            {
            }
        }
    }

    public static int getMatchThresholdPercentage() 
    {    
        return matchThresholdPercentage;
    }
    
    private static class MatchThresholdHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            
            try 
            {
                matchThresholdPercentage = Integer.parseInt(arg0.getPropertyValue());
            } 
            catch (Exception e) 
            {
            }
        }
    }

    public static List<String> getNegationStrings() 
    {    
        return negationStrings;        
    }
    public static void setNegationStrings(List<String> value) 
    {
        negationStrings = value;
    }

    private static class NegationStringsHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            negationStrings = csvToStringList(arg0.getPropertyValue());
        }
    }
    
    public static List<String> getAllowableHL7MessageTypes() 
    {
        return allowableHL7MessageTypes;
    }
    public static void setAllowableHL7MessageTypes(List<String> value) 
    {
        allowableHL7MessageTypes = value;
    }
    
    private static class AllowableHL7MessageTypesHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            allowableHL7MessageTypes = csvToStringList(arg0.getPropertyValue());
        }
    }

    public static List<String> getAllowedProcessingIds() 
    {
        return allowedProcessingIds;
    }
    public static void setAllowedProcessingIds(List<String> value) 
    {
        allowedProcessingIds = value;
    }
    
    private static class AllowedProcessingIdsHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            allowedProcessingIds = csvToStringList(arg0.getPropertyValue());
        }
    }

    public static INlpAnalyzer getNlpAnalyzer(String name) 
    {
        INlpAnalyzer retVal = null;
        
        if (nlpAnalyzer != null) 
        {
            retVal = nlpAnalyzer.get(name);
        }
        
        return retVal;
    }
    
    public static void setNlpAnalyzer(HashMap<String, INlpAnalyzer> newAnalyzer) 
    {
        nlpAnalyzer = newAnalyzer;
    }
    public static void addNlpAnalyzer(String name, INlpAnalyzer analyzer) 
    {
        if (nlpAnalyzer == null) 
        {
            nlpAnalyzer = new HashMap<String, INlpAnalyzer>();            
        }
        
        nlpAnalyzer.put(name, analyzer);
    }
    
    private static class NlpAnalyzerHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            
            try 
            {                
                String analyzerProperty = arg0.getPropertyValue();
            
                if (StringUtils.isNotEmpty(analyzerProperty)) 
                {
                    nlpAnalyzer = loadAnalyzers(arg0.getPropertyValue());
                } 
                else 
                {
                    // We didn't have a property value, so let's add an empty map
                    // so that others can add analyzers as needed.
                    nlpAnalyzer = new HashMap<String, INlpAnalyzer>();
                }
                    
            }
            catch (Exception e) 
            {
                log.error("failed to create nlp analyzer instance: " + e.getMessage(), e);
            }
        }
    }

    public static List<String> getCandidateSegmentNames() 
    {
        return candidateSegmentNames;
    }
    
    private static class CandidateSegmentNamesHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            candidateSegmentNames = csvToStringList(arg0.getPropertyValue());
        }
    }
    
    public static ArrayList<IResultsCritic> getMessageCritics() 
    {
        return messageCritics;
    }
    
    public static void setMessageCritics(ArrayList<IResultsCritic> value) 
    {
        messageCritics = value;
    }
    
    private static class MessageCriticHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            messageCritics = loadCritics(arg0.getPropertyValue());
        }
    }

    public static ArrayList<IResultsCritic> getResultsCritics() 
    {
        return resultsCritics;
    }
    public static void setResultsCritics(ArrayList<IResultsCritic> value) 
    {
        resultsCritics = value;
    }
    
    private static class ResultsCriticHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            resultsCritics = loadCritics(arg0.getPropertyValue());
        }
    }
    
    public static ArrayList<MessagePreProcessor> getMessagePreProcessors() 
    {
    	return messagePreProcessors;
    }
    public static void setMessagePreProcessors(ArrayList<MessagePreProcessor> preProcessors) 
    {
    	messagePreProcessors = preProcessors;
    }
    
    private static class MessagePreProcessorHandler extends SimplePropertyEventHandler 
    {
    	public void propertyChanged(GlobalProperty arg0) 
    	{
    		log.debug("property=" + arg0);
    		messagePreProcessors = loadPreProcessors(arg0.getPropertyValue());
    	}
    }
        
    public static String getResultFinderMapText() 
    {
        return resultFinderMapText;
    }
    public static void setResultFinderMapText(String value) 
    {
        resultFinderMapText = value;
    }
    
    private static class ResultFinderMapHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            resultFinderMapText = arg0.getPropertyValue();
        }
    }
    
    /** Gets the next reportable result sequence number and increments it. This is far from the most efficient code in the world. */
    public static long nextReportableResultSeq() 
    {
        long next = reportableResultSeq++;
    
        if (reportableResultSeqProp != null) 
        {
            log.debug("reportableResultSeq update to " + reportableResultSeq);
            AdministrationService adminService = Context.getAdministrationService();
            reportableResultSeqProp.setPropertyValue(Long.toString(reportableResultSeq));
            adminService.saveGlobalProperty(reportableResultSeqProp);
        }
        
        return next;
    }
    
    public static void setReportableResultSeq(int value) 
    {
        reportableResultSeq = value;
    }
    
    private static class ReportableResultSeqHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            log.debug("property=" + arg0);
            reportableResultSeqProp = arg0;
            
            try 
            {
                reportableResultSeq = Long.parseLong(arg0.getPropertyValue());
            } 
            catch (Exception e) 
            {
            }
        }
    }
    
    public static JurisdictionAlgorithm getJurisdictionAlgorithm() 
    {
        return jurisdictionAlgorithm;
    }
    public static void setJurisdictionAlgorithm(JurisdictionAlgorithm algo) 
    {
        jurisdictionAlgorithm = algo;
    }
    
    private static class JurisdictionAlgorithmHandler extends SimplePropertyEventHandler 
    {
        public void propertyChanged(GlobalProperty arg0) 
        {
            try 
            {
                String className = arg0.getPropertyValue();
                Class<?> jurisdictionClass = Class.forName(className);
                jurisdictionAlgorithm = (JurisdictionAlgorithm) jurisdictionClass.newInstance();
            } 
            catch (Exception e) 
            {                
            }
        }
    }
    
    public static String getDataTypeTransforms() 
    {
    	return dataTypeTransforms;
    }
    public static void setDataTypeTransforms(String transforms) 
    {
    	dataTypeTransforms = transforms;
    }
    
    private static class DataTypeTransformsHandler extends SimplePropertyEventHandler 
    {
    	public void propertyChanged(GlobalProperty arg0) 
    	{
    		try 
    		{
    			String transforms = arg0.getPropertyValue();
    			log.debug("dataTypeTransforms property = " + transforms);
    			dataTypeTransforms = transforms;
    		} 
    		catch (Exception e) 
    		{    			
    		}
    	}
    }
    
    public static Map<String, String> getResultSegmentMap() 
    {
    	return resultSegmentMap;
    }
    public static void setResultSegmentMap(Map<String, String> newResultSegmentMap) 
    {
    	resultSegmentMap = newResultSegmentMap;
    }
    
    public static void addResultSegmentMapping(String segmentName, String className) 
    {
    	if (resultSegmentMap == null) 
    	{
    		resultSegmentMap = new HashMap<String,String>();
    	}
    	
    	resultSegmentMap.put(segmentName, className);
    }
    
    private static class ResultSegmentMapHandler extends SimplePropertyEventHandler 
    {
    	public void propertyChanged(GlobalProperty arg0) 
    	{
    		try 
    		{
    			resultSegmentMap                 = new HashMap<String, String>();
    			String rawResultSegmentMap       = arg0.getPropertyValue();
    			String[] rawResultSegmentItems   = rawResultSegmentMap.split(",");
    		
    			for (int itemIdx = 0; itemIdx < rawResultSegmentItems.length; itemIdx++) 
    			{
    				String resultSegmentName    = StringUtils.substringBefore(rawResultSegmentItems[itemIdx], "=");
    				String resultSegmentClass   = StringUtils.substringAfter(rawResultSegmentItems[itemIdx], "=");
    				resultSegmentMap.put(resultSegmentName, resultSegmentClass);
    			}
    		} 
    		catch (Exception e) 
    		{    		
    		}
    	}
    }
    
    public static String getMessageTypeTransforms() 
    {
    	return messageTypeTransforms;
    }
    public static void setMessageTypeTransforms(String transforms) 
    {
    	messageTypeTransforms = transforms;
    }    
    private static class MessageTypeTransformsHandler extends SimplePropertyEventHandler 
    {
		public void propertyChanged(GlobalProperty arg0) 
		{
			try 
			{
				String transforms = arg0.getPropertyValue();
    			log.debug("messageTypeTransforms property = " + transforms);
    			messageTypeTransforms = transforms;
			} 
			catch (Exception e) 
			{			
			}
		}    	
    }

    public static int getRowsPerPage() throws PropertyNotFoundException 
    {        
        return PropertyManager.getInt(ROWS_PER_PAGE_PROPNAME);
    }
    
    public static void setRowsPerPage(int value) throws PropertyNotFoundException 
    {
        PropertyManager.set(ROWS_PER_PAGE_PROPNAME, value);
    }
    
    public static int getMaxRowsToFetch() throws PropertyNotFoundException 
    {    
        return PropertyManager.getInt(MAX_ROWS_TO_FETCH_PROPNAME);
    }
    
    public static void setMaxRowsToFetch(int value) throws PropertyNotFoundException 
    {    
        PropertyManager.set(MAX_ROWS_TO_FETCH_PROPNAME, value);
    }

    public static void start() 
    {    
        try 
        {	
        	// Force a load of the code/condition mapping table in
        	// CodeConditionDAO
        	getService().getCodeConditionDAO().findByCodeAndSystem("U", "NCD");
        	
            AdministrationService adminService = Context.getAdministrationService();
            
            PropertyManager.create(ROWS_PER_PAGE_PROPNAME, "100", "The number of rows to display per page on list page.");
            PropertyManager.create(MAX_ROWS_TO_FETCH_PROPNAME, "1000", "The maximum number of rows to fetch on list pages.");

            adminService.addGlobalPropertyListener(dispatcher);
            dispatcher.add("ncd.reportableAbnormalFlags",       new ReportableAbnormalFlagsHandler(), adminService);
            dispatcher.add("ncd.notReportableAbnormalFlags",    new NotReportableAbnormalFlagsHandler(), adminService);
            dispatcher.add("ncd.loincFrequencyCacheSize",       new MaxLoincFrequencyCacheSizeHandler(), adminService);
            dispatcher.add("ncd.enableNTEprocessing",           new NTEProcessingFlagHandler(), adminService);
            dispatcher.add("ncd.matchThreshold",                new MatchThresholdHandler(), adminService);
            dispatcher.add("ncd.negationStrings",               new NegationStringsHandler(), adminService);
            dispatcher.add("ncd.allowableMessageTypes",         new AllowableHL7MessageTypesHandler(), adminService);
            dispatcher.add("ncd.allowableProcessingIds",        new AllowedProcessingIdsHandler(), adminService);
            dispatcher.add("ncd.nlpAnalyzers",                  new NlpAnalyzerHandler(), adminService);
            dispatcher.add("ncd.candidateSegmentNames",         new CandidateSegmentNamesHandler(), adminService);
            dispatcher.add("ncd.messageCritics",                new MessageCriticHandler(), adminService);
            dispatcher.add("ncd.resultsCritics",                new ResultsCriticHandler(), adminService);
            dispatcher.add("ncd.resultFinderMap",               new ResultFinderMapHandler(), adminService);
            dispatcher.add("ncd.reportableResultSeq",           new ReportableResultSeqHandler(), adminService);
            dispatcher.add("ncd.jurisdictionAlgorithm",         new JurisdictionAlgorithmHandler(), adminService);
            dispatcher.add("ncd.messagePreProcessors",          new MessagePreProcessorHandler(), adminService);
            dispatcher.add("ncd.dataTypeTransforms",            new DataTypeTransformsHandler(), adminService);
            dispatcher.add("ncd.resultSegmentMap",              new ResultSegmentMapHandler(), adminService);
            dispatcher.add("ncd.messageTypeTransforms",         new MessageTypeTransformsHandler(), adminService);
            
            manageConcepts(adminService, Context.getConceptService());
            
            // Create the MessageLogger properties.
            MessageLoggerFactory.getInstance().createProperties();
        }
        catch (Throwable t) 
        {
            log.error("start failed: " + t.getMessage(), t);
        }
        
        started = true;
    }

    public static void stop() 
    {    
        started = false;
        AdministrationService adminService = Context.getAdministrationService();
        adminService.removeGlobalPropertyListener(dispatcher);
        
        try 
        {
			getService().saveCodeFrequencyMap();
		} 
        catch (CodeFrequencyStorageException e) 
        {
			log.error("exception saving code frequency map: " + e.getMessage(), e);
		}
    }
    
    public static boolean isStarted() 
    {
        return started;
    }

    // ****************
    // Helpers
    // ****************
    
    /** Converts a String containing a comma-separated list of values into a List
     * of strings, with one entry per value.
     * 
     * @param csv
     * @return
     */
    private static List<String> csvToStringList(String csv) 
    {
        if (StringUtils.isEmpty(csv))
        {
            return null;
        }
    
        return Arrays.asList(csv.replaceAll(" ", "").split(","));
    }
    
    /**
     * Splits the supplied comma-separated list of critic classnames at commas
     * into a collection of classnames, creates an instance of each named class,
     * and returns the instances in an ArrayList.
     * 
     * TODO: Does this belong in CommunicableDiseaseProcessor?
     * 
     * @param propertyValue The name of the global property to take the
     *        comma-separated list of classnames from.
     * @return An ArrayList<IResultsCritic> containing the created class
     *         instances.
     */
    private static ArrayList<IResultsCritic> loadCritics(String propertyValue) 
    {
        ArrayList<IResultsCritic> critics    = new ArrayList<IResultsCritic>();
        String[] classNames                  = propertyValue.split(",");
        log.debug("classNames=" + Arrays.asList(classNames));
     
        for (String className : classNames) 
        {
            String trimmedClassName = className.trim();
            
            try 
            {
                if (trimmedClassName.length() > 0) 
                {
                    log.debug("attempting to create an instance of class \"" + trimmedClassName + "\"");
                    Class<?> criticClass = Class.forName(trimmedClassName);
                    critics.add((IResultsCritic) criticClass.newInstance());
                }
            } catch (Exception e) 
            {
                log.error("cannot create an instance of class \"" + trimmedClassName + "\"", e);
            }
        }
        
        return critics;
    }
    
    private static ArrayList<MessagePreProcessor> loadPreProcessors(String propertyValue) 
    {
    	ArrayList<MessagePreProcessor> preProcessors = new ArrayList<MessagePreProcessor>();
    
    	if (propertyValue != null) 
    	{
	        String[] classNames = propertyValue.split(",");
	        log.debug("classNames=" + Arrays.asList(classNames));
	        
	        for (String className : classNames) 
	        {
	            String trimmedClassName = className.trim();
	            
	            try 
	            {
	                if (trimmedClassName.length() > 0) 
	                {
	                    log.debug("attempting to create an instance of class \"" + trimmedClassName + "\"");
	                    Class<?> preProcessorClass = Class.forName(trimmedClassName);
	                    preProcessors.add((MessagePreProcessor) preProcessorClass.newInstance());
	                }
	            } 
	            catch (Exception e) 
	            {
	                log.error("cannot create an instance of class \"" + trimmedClassName + "\"", e);
	            }
	        }
    	}
    	
        return preProcessors;
    }
    
    /**
     * 
     * Parses the nlpAnalyzer property which is of the format:
     * <name>=<analyzer class>
     * 
     * e.g. text=org.openmrs.module.ncd.nlp.NlpTextAnalyzer
     * 
     * @param propertyValue The value that of the nlpAnalyzer property.
     * @return A HashMap that maps from the String name to the INlpAnalyzer object.
     */
    private static HashMap<String, INlpAnalyzer> loadAnalyzers(String propertyValue) 
    {
        HashMap<String, INlpAnalyzer> analyzers = new HashMap<String, INlpAnalyzer>();
        String[] analyzerClassTuples = propertyValue.split(",");
        log.debug("analyzers = " + Arrays.asList(analyzerClassTuples));
        
        for (String analyzerClassTuple : analyzerClassTuples) 
        {
            String trimmedTuple = analyzerClassTuple.trim();
            String[] tupleParts = null;
            
            try 
            {
                if (trimmedTuple.length() > 0) 
                {
                    tupleParts = trimmedTuple.split("=");
                    Class<?> analyzer = Class.forName(tupleParts[1]);
                    analyzers.put(tupleParts[0], (INlpAnalyzer)analyzer.newInstance());
                }
            } 
            catch (Exception e) 
            {
                log.error("cannot create instance of class \"" + tupleParts[1] + "\"", e);
            }
        }
        
        return analyzers;
    }
    
    /**
     * Manages the module specific concepts.  It will create them if they don't exist, and/or
     * update them depending on the current installed concept version (stored as a global property).
     * 
     * TODO: The complexity of the updates here is getting rather too
     * high. At this point, we should consider if it's better just to
     * wipe the slate clean, delete all our data and our concepts, and
     * recreate them from scratch correctly.
     * 
     * @param adminService Provides API for administrative services.
     * @param conceptService Provides API for concept management services.
     */
    private static void manageConcepts(AdministrationService adminService, ConceptService conceptService) 
    {
    	try 
    	{	
    		Context.addProxyPrivilege("Manage Concepts");
    		
    		User ncdUser = NCDUtilities.getNcdUser();
    		
	    	// Get the current concept version (stored as a global property)
	    	List<GlobalProperty> globalProperties = adminService.getAllGlobalProperties();
	    	ListIterator<GlobalProperty> i = globalProperties.listIterator();
	    	GlobalProperty conceptVersion = null;
	    	
	    	while (i.hasNext()) 
	    	{
	    		GlobalProperty prop = i.next();
	    	
	    		if (prop.getProperty().equals("ncd.conceptVersion")) 
	    		{
	    			conceptVersion = prop;
	    			break;
	    		}
	    	}
	    	
	    	// Parse the major and minor concept version numbers
	    	int conceptVersionMajor = 0;
	    	int conceptVersionMinor = 0;
	        log.debug("ncd.conceptVersion=" + conceptVersion.getPropertyValue());
	        
	        try 
	        {
	        	String[] temp = conceptVersion.getPropertyValue().split("\\.");
	        	conceptVersionMajor = Integer.parseInt(temp[0]);
	        	conceptVersionMinor = Integer.parseInt(temp[1]);
	        } 
	        catch (Exception e) 
	        {
	        	log.error("invalid global property value for ncd.conceptVersion - expected x.y (e.g. 1.0)");
	        }
	
			// Get the concept data type
			ConceptDatatype textDatatype = conceptService.getConceptDatatypeByName("Text");
	        ConceptDatatype dateTimeDatatype = conceptService.getConceptDatatypeByName("Datetime");
	        ConceptDatatype numericDatatype = conceptService.getConceptDatatypeByName("Numeric");
	        ConceptDatatype noneDatatype = conceptService.getConceptDatatypeByName("N/A");
	
			// Get the Java Locale for English
			Locale locale = new Locale("en");
			
	        // Create the 1.0 concepts
	        if (conceptVersionMajor < 1 || conceptVersionMinor < 0) 
	        {
	        	// Try to get the concept class
	    		ConceptClass conceptClass = conceptService.getConceptClassByName("NCD");
	    		
	    		if (conceptClass == null) 
	    		{
		    		// Create the concept class
	    			log.debug("NCD concept class not found; creating it");
		    		conceptClass = new ConceptClass();
		    		conceptClass.setCreator(ncdUser);
		    		conceptClass.setName("NCD");
		    		conceptClass.setDescription("Module specific concepts for ncd");
		    		conceptService.saveConceptClass(conceptClass);
		    		
		    		// Get the concept class just created
		    		conceptClass = conceptService.getConceptClassByName("NCD");
	    		}
	    		
	    		// Create the concepts
	    		conceptService.saveConcept(makeSimpleConcept("NCD INSTITUTION NAME", null, "The name of the institution that sent the HL7 message which contained this reportable condition.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD LABORATORY NAME", null, "The name of the laboratory that performed the test.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD LABORATORY ID", null, "Laboratory's id.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD LABORATORY PHONE", null, "Laboratory's phone number.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD LABORATORY ADDRESS 1", null, "Laboratory's street address line 1.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD LABORATORY ADDRESS 2", null, "Laboratory's street address line 2.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD LABORATORY CITY", null, "Laboratory's city.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD LABORATORY STATE", null, "Laboratory's state.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD LABORATORY ZIP", null, "Laboratory's zip code.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST ID", null, "The identifier assigned to the test by the laboratory.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST NAME", null, "The name of the test performed.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST CODE SYSTEM", null, "The name of the coding system used to report the test results.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST PLACER ORDER NUMBER", null, "The order number assigned to this test by the person or organization who ordered the test.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST FILLER ORDER NUMBER", null, "The order number assigned to this test by the person or organization who performed the test.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST DATE", null, "The date on which the test was performed.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST PARENT PLACER", null, "(pulled straight from HL7 message: ORU/ORC.8/EIP.1/EI.1)", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST PARENT FILLER", null, "(pulled straight from HL7 message: ORU/ORC.8/EIP.2/EI.1)", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST SPECIMEN TEXT", null, "(pulled straight from HL7 message: ORU/OBR.15/SPS.1/CE.1)", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST DATA TYPE", null, "(pulled straight from HL7 message: ORU/OBX.2)", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST NORMAL RANGE", null, "The normal range of values for the result of this test.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST ABNORMAL FLAG", null, "A short code flag for an 'abnormal' test result.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST COMMENT", null, "Free-form comments about this test.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST RECEIVED DATE TIME", null, "?", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST MPQ SEQ NUMBER", null, "?", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST RESULT ID", null, "The code for the type of test result, usually a LOINC code.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST RESULT NAME", null, "?", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST RESULT CODE SYSTEM", null, "The identifier for the coding system used in the source HL7 message for test results, usually LN for LOINC.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST RESULT SUB ID", null, "?", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST RESULT CODE", null, "For test results which are coded, the code for the test result (HbA1C, etc.). For test results which are not coded, a free form description of the test result.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST RESULT VALUE", null, "For test results which are coded, the value of the test result. The test results which are not coded, a free form description of the test results.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST RESULT UNITS", null, "For test results which are coded, the units in which the test result value is reported in TEST RESULT VALUE.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST RESULT STATUS", null, "?", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST PREVIOUS DATE", null, "?", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD CONDITION NAME", null, "The name of the detected condition.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD APPLICATION", null, "The name of the application that sent the HL7 message that contained this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD FACILITY", null, "The name of the facility (within the institution) that sent the HL7 message that contained this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD LOINC CODE ID", null, "(debugging information: the primary key of the tblloinccode row that was matched to signal this reportable result)", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD PREVIOUS REPORTABLE RESULT ID", null, "?", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD RAW HL7 ID", null, "Identifies the raw version of the incoming HL7 message that contained this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST DATE SOURCE", null, "The HL7 message field from which the value of TEST DATE was taken (debugging information?).", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST PLACER ORDER NUMBER SOURCE", null, "The HL7 message field from which the value of TEST PLACER ORDER NUMBER was taken (debugging information?).", locale, conceptClass, textDatatype, ncdUser));
	    		conceptService.saveConcept(makeSimpleConcept("NCD TEST FILLER ORDER NUMBER SOURCE", null, "The HL7 message field from which the value of TEST FILLER ORDER NUMBER was taken (debugging information?).", locale, conceptClass, textDatatype, ncdUser));
	
	    		// Update conceptVersion to 1.0
	    		conceptVersion.setPropertyValue("1.0");
	    		adminService.saveGlobalProperty(conceptVersion);
	    		conceptVersionMajor = 1;
	    		conceptVersionMinor = 0;
	    	}
	
	        // Make the 1.1 concept changes
	        if (conceptVersionMajor < 1 || conceptVersionMinor < 1) 
	        {    
	            fixDateConcept(conceptService, "NCD TEST DATE", dateTimeDatatype);
	            fixDateConcept(conceptService, "NCD TEST RECEIVED DATE TIME", dateTimeDatatype);
	            fixDateConcept(conceptService, "NCD TEST PREVIOUS DATE", dateTimeDatatype);
	
	            // Update conceptVersion to 1.1
	            conceptVersion.setPropertyValue("1.1");
	            adminService.saveGlobalProperty(conceptVersion);
	            conceptVersionMajor = 1;
	            conceptVersionMinor = 1;
	        }
	
	        // Make the 1.2 concept changes
	        if (conceptVersionMajor < 1 || conceptVersionMinor < 2) 
	        {    
	            ConceptClass conceptClass = conceptService.getConceptClassByName("NCD");
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.RESULT_SEQUENCE_NUMBER, null, "The sequence number for this reportable result.", locale, conceptClass, numericDatatype, ncdUser));
	
	            // Update conceptVersion to 1.2
	            conceptVersion.setPropertyValue("1.2");
	            adminService.saveGlobalProperty(conceptVersion);
	            conceptVersionMajor = 1;
	            conceptVersionMinor = 2;
	        }
	
	        // Make the 1.3 concept changes
	        if (conceptVersionMajor < 1 || conceptVersionMinor < 3) 
	        {    
	            ConceptClass conceptClass = conceptService.getConceptClassByName("NCD");
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.HEALTH_DEPT_AGENCY, null, "A health department agency associated with this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.HEALTH_DEPT_CASE_ID, null, "A health department agency case number associated with this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.INSTITUTION_ID_TYPE, null, "The type of institution associated with this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.OBR_ALT_CODE, null, "The value of the alternate OBR code for this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.OBR_ALT_CODE_SYS, null, "The code system of the alternate OBR code for this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.OBR_ALT_CODE_TEXT, null, "The free text of the alternate OBR code for this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.OBX_ALT_CODE, null, "The value of the alternate OBX code for this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.OBX_ALT_CODE_SYS, null, "The code system of the alternate OBX code for this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.OBX_ALT_CODE_TEXT, null, "The free text of the alternate OBX code for this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	            conceptService.saveConcept(makeSimpleConcept(NCDConcepts.UNIQUE_REGISTRY_NUM, null, "The unique registry number for this reportable result.", locale, conceptClass, textDatatype, ncdUser));
	
	            // Update conceptVersion to 1.3
	            conceptVersion.setPropertyValue("1.3");
	            adminService.saveGlobalProperty(conceptVersion);
	            conceptVersionMajor = 1;
	            conceptVersionMinor = 3;
	        }
	        
	        // Make the 1.4 concept changes
	        if (conceptVersionMajor < 1 || conceptVersionMinor < 4) 
	        {
	            ConceptClass ncdConceptClass = conceptService.getConceptClassByName("NCD");
	            Concept ncdResultConcept = conceptService.getConceptByName(NCDConcepts.REPORTABLE_RESULT);
	            
	            if (ncdResultConcept == null) 
	            {
	                // Create the NCD reportable result concept
	                ncdResultConcept =  makeSimpleConcept(NCDConcepts.REPORTABLE_RESULT, null, "A reportable result detected by the Notifiable Condition Detector", locale, ncdConceptClass, noneDatatype, ncdUser);
	                ncdResultConcept.setSet(true);
	                conceptService.saveConcept(ncdResultConcept);
	            }
	
	            int sortWeight = 1;
	
	            // A collection of the names of all pre-1.4 NCD concepts.
	            // The order of the concepts in this collection is the order
	            // they will sort in within the reportable result concept set.
	            Collection<String> ncdConceptNames = new ArrayList<String>();
	            ncdConceptNames.add(NCDConcepts.CONDITION_NAME);
	            ncdConceptNames.add(NCDConcepts.APPLICATION);
	            ncdConceptNames.add(NCDConcepts.FACILITY);
	            ncdConceptNames.add(NCDConcepts.INSTITUTION_NAME);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_NAME);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_ID);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_PHONE);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_ADDRESS1);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_ADDRESS2);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_CITY);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_STATE);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_ZIP);
	            ncdConceptNames.add(NCDConcepts.TEST_ID);
	            ncdConceptNames.add(NCDConcepts.TEST_NAME);
	            ncdConceptNames.add(NCDConcepts.TEST_CODE_SYSTEM);
	            ncdConceptNames.add(NCDConcepts.TEST_PLACER_ORDER_NUMBER);
	            ncdConceptNames.add(NCDConcepts.TEST_FILLER_ORDER_NUMBER);
	            ncdConceptNames.add(NCDConcepts.TEST_DATE);
	            ncdConceptNames.add(NCDConcepts.TEST_PARENT_PLACER);
	            ncdConceptNames.add(NCDConcepts.TEST_PARENT_FILLER);
	            ncdConceptNames.add(NCDConcepts.TEST_SPECIMEN_TEXT);
	            ncdConceptNames.add(NCDConcepts.TEST_DATA_TYPE);
	            ncdConceptNames.add(NCDConcepts.TEST_NORMAL_RANGE);
	            ncdConceptNames.add(NCDConcepts.TEST_ABNORMAL_FLAG);
	            ncdConceptNames.add(NCDConcepts.TEST_COMMENT);
	            ncdConceptNames.add(NCDConcepts.TEST_RECEIVED_DATE_TIME);
	            ncdConceptNames.add(NCDConcepts.TEST_MPQ_SEQ_NUMBER);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_ID);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_NAME);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_CODE_SYSTEM);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_SUB_ID);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_CODE);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_VALUE);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_UNITS);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_STATUS);
	            ncdConceptNames.add(NCDConcepts.TEST_PREVIOUS_DATE);
	            ncdConceptNames.add(NCDConcepts.TEST_DATE_SOURCE);
	            ncdConceptNames.add(NCDConcepts.TEST_PLACER_ORDER_NUMBER_SOURCE);
	            ncdConceptNames.add(NCDConcepts.TEST_FILLER_ORDER_NUMBER_SOURCE);
	            ncdConceptNames.add(NCDConcepts.LOINC_CODE_ID);
	            ncdConceptNames.add(NCDConcepts.PREVIOUS_REPORTABLE_RESULT_ID);
	            ncdConceptNames.add(NCDConcepts.RAW_HL7_ID);
	            ncdConceptNames.add(NCDConcepts.RESULT_SEQUENCE_NUMBER);
	            ncdConceptNames.add(NCDConcepts.HEALTH_DEPT_AGENCY);
	            ncdConceptNames.add(NCDConcepts.HEALTH_DEPT_CASE_ID);
	            ncdConceptNames.add(NCDConcepts.INSTITUTION_ID_TYPE);
	            ncdConceptNames.add(NCDConcepts.OBR_ALT_CODE);
	            ncdConceptNames.add(NCDConcepts.OBR_ALT_CODE_SYS);
	            ncdConceptNames.add(NCDConcepts.OBR_ALT_CODE_TEXT);
	            ncdConceptNames.add(NCDConcepts.OBX_ALT_CODE);
	            ncdConceptNames.add(NCDConcepts.OBX_ALT_CODE_SYS);
	            ncdConceptNames.add(NCDConcepts.OBX_ALT_CODE_TEXT);
	            ncdConceptNames.add(NCDConcepts.UNIQUE_REGISTRY_NUM);
	            
	            // For all pre-1.4 NCD concepts...
	            Set<ConceptSet> conceptSets = new HashSet<ConceptSet>();
	            
	            for (String conceptName : ncdConceptNames) 
	            {
	                // Get the concept
	                Concept c = conceptService.getConceptByName(conceptName);
	
	                // Add the concept to the reportable result
	                // concept as a set member
	                ConceptSet resultConceptSet = new ConceptSet();
	                resultConceptSet.setConcept(c);
	                resultConceptSet.setConceptSet(ncdResultConcept);
	                resultConceptSet.setCreator(ncdUser);
	                resultConceptSet.setDateCreated(new Date());
	                resultConceptSet.setSortWeight(new Double(sortWeight));
	                conceptSets.add(resultConceptSet);
	                c.getConceptSets().clear();
	                conceptService.saveConcept(c);
	
	                sortWeight++;
	            }
	            
	            ncdResultConcept.getConceptSets().clear();
	            ncdResultConcept.getConceptSets().addAll(conceptSets);
	            conceptService.saveConcept(ncdResultConcept);
	
	            // Recompute the derived concepts (not that any should change)
	            // TODO uncomment the next line when derived concept sets are fixed in OpenMRS 1.5.0
	            //      currently, the underlying table: concept_set_derived appears to be missing the uuid column,
	            //		and this causes an exception when attempting to update it.
	            //conceptService.updateConceptSetDerived(ncdResultConcept);
	
	            // Update conceptVersion to 1.4
	            conceptVersion.setPropertyValue("1.4");
	            adminService.saveGlobalProperty(conceptVersion);
	            conceptVersionMajor = 1;
	            conceptVersionMinor = 4;
	        }
	        
	        // Make the 1.5 concept changes
	        if (conceptVersionMajor < 1 || conceptVersionMinor < 5) 
	        {
	            ConceptClass ncdConceptClass = conceptService.getConceptClassByName("NCD");
	            
	            // Get the reportable result concept
	            Concept ncdResultConcept = conceptService.getConceptByName(NCDConcepts.REPORTABLE_RESULT);
	            
	            // Create the reportable result id concept
	            Concept resultIdConcept = makeSimpleConcept(NCDConcepts.REPORTABLE_RESULT_ID, null, "The fixed unique internal identifier assigned to a single reportable result", locale, ncdConceptClass, textDatatype, ncdUser);
	            conceptService.saveConcept(resultIdConcept);
	
	            int sortWeight = 1;
	
	            // A collection of the names of all pre-1.4 NCD concepts.
	            // The order of the concepts in this collection is the order
	            // they will sort in within the reportable result concept set.
	            Collection<String> ncdConceptNames = new ArrayList<String>();
	            ncdConceptNames.add(NCDConcepts.CONDITION_NAME);
	            ncdConceptNames.add(NCDConcepts.APPLICATION);
	            ncdConceptNames.add(NCDConcepts.FACILITY);
	            ncdConceptNames.add(NCDConcepts.INSTITUTION_NAME);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_NAME);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_ID);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_PHONE);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_ADDRESS1);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_ADDRESS2);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_CITY);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_STATE);
	            ncdConceptNames.add(NCDConcepts.LABORATORY_ZIP);
	            ncdConceptNames.add(NCDConcepts.TEST_ID);
	            ncdConceptNames.add(NCDConcepts.TEST_NAME);
	            ncdConceptNames.add(NCDConcepts.TEST_CODE_SYSTEM);
	            ncdConceptNames.add(NCDConcepts.TEST_PLACER_ORDER_NUMBER);
	            ncdConceptNames.add(NCDConcepts.TEST_FILLER_ORDER_NUMBER);
	            ncdConceptNames.add(NCDConcepts.TEST_DATE);
	            ncdConceptNames.add(NCDConcepts.TEST_PARENT_PLACER);
	            ncdConceptNames.add(NCDConcepts.TEST_PARENT_FILLER);
	            ncdConceptNames.add(NCDConcepts.TEST_SPECIMEN_TEXT);
	            ncdConceptNames.add(NCDConcepts.TEST_DATA_TYPE);
	            ncdConceptNames.add(NCDConcepts.TEST_NORMAL_RANGE);
	            ncdConceptNames.add(NCDConcepts.TEST_ABNORMAL_FLAG);
	            ncdConceptNames.add(NCDConcepts.TEST_COMMENT);
	            ncdConceptNames.add(NCDConcepts.TEST_RECEIVED_DATE_TIME);
	            ncdConceptNames.add(NCDConcepts.TEST_MPQ_SEQ_NUMBER);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_ID);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_NAME);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_CODE_SYSTEM);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_SUB_ID);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_CODE);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_VALUE);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_UNITS);
	            ncdConceptNames.add(NCDConcepts.TEST_RESULT_STATUS);
	            ncdConceptNames.add(NCDConcepts.TEST_PREVIOUS_DATE);
	            ncdConceptNames.add(NCDConcepts.TEST_DATE_SOURCE);
	            ncdConceptNames.add(NCDConcepts.TEST_PLACER_ORDER_NUMBER_SOURCE);
	            ncdConceptNames.add(NCDConcepts.TEST_FILLER_ORDER_NUMBER_SOURCE);
	            ncdConceptNames.add(NCDConcepts.LOINC_CODE_ID);
	            ncdConceptNames.add(NCDConcepts.PREVIOUS_REPORTABLE_RESULT_ID);
	            ncdConceptNames.add(NCDConcepts.RAW_HL7_ID);
	            ncdConceptNames.add(NCDConcepts.HEALTH_DEPT_AGENCY);
	            ncdConceptNames.add(NCDConcepts.HEALTH_DEPT_CASE_ID);
	            ncdConceptNames.add(NCDConcepts.INSTITUTION_ID_TYPE);
	            ncdConceptNames.add(NCDConcepts.OBR_ALT_CODE);
	            ncdConceptNames.add(NCDConcepts.OBR_ALT_CODE_SYS);
	            ncdConceptNames.add(NCDConcepts.OBR_ALT_CODE_TEXT);
	            ncdConceptNames.add(NCDConcepts.OBX_ALT_CODE);
	            ncdConceptNames.add(NCDConcepts.OBX_ALT_CODE_SYS);
	            ncdConceptNames.add(NCDConcepts.OBX_ALT_CODE_TEXT);
	            ncdConceptNames.add(NCDConcepts.UNIQUE_REGISTRY_NUM);
	            ncdConceptNames.add(NCDConcepts.REPORTABLE_RESULT_ID);
	            
	            // For all pre-1.5 NCD concepts...
	            Set<ConceptSet> conceptSets = new HashSet<ConceptSet>();
	            
	            for (String conceptName : ncdConceptNames) 
	            {
	                // Get the concept
	                Concept c = conceptService.getConceptByName(conceptName);
	
	                // Add the concept to the reportable result
	                // concept as a set member
	                ConceptSet resultConceptSet = new ConceptSet();
	                resultConceptSet.setConcept(c);
	                resultConceptSet.setConceptSet(ncdResultConcept);
	                resultConceptSet.setCreator(ncdUser);
	                resultConceptSet.setDateCreated(new Date());
	                resultConceptSet.setSortWeight(new Double(sortWeight));
	                conceptSets.add(resultConceptSet);
	                c.getConceptSets().clear();
	                conceptService.saveConcept(c);
	
	                sortWeight++;
	            }
	            
	            ncdResultConcept.getConceptSets().clear();
	            ncdResultConcept.getConceptSets().addAll(conceptSets);
	            conceptService.saveConcept(ncdResultConcept);
	
	            // Recompute the derived concepts (not that any should change)
	            // TODO uncomment the next line when derived concept sets are fixed in OpenMRS 1.5.0
	            //      currently, the underlying table: concept_set_derived appears to be missing the uuid column,
	            //		and this causes an exception when attempting to update it.
	            //conceptService.updateConceptSetDerived(ncdResultConcept);
	
	            // Update conceptVersion to 1.5
	            conceptVersion.setPropertyValue("1.5");
	            adminService.saveGlobalProperty(conceptVersion);
	            conceptVersionMajor = 1;
	            conceptVersionMinor = 5;
	        }
    	}
    	finally 
    	{
    		Context.removeProxyPrivilege("Manage Concepts");
    	}
    }
    
    /**
     * Construct and return a simple Concept.
     * 
     * @param name The concept name.
     * @param shortName The concept short name.  Currently not used.
     * @param description The concept description.  Currently not used.
     * @param locale The locale for this version of the concept.
     * @param conceptClass The concept class.
     * @param conceptDatatype The concept data type.
     * @param ncdUser The User to be set as the creator field for new objects.
     * @return
     */
    private static Concept makeSimpleConcept(String name, String shortName, String description, Locale locale, ConceptClass conceptClass, ConceptDatatype conceptDatatype, User ncdUser) 
    {
    	Concept concept = new Concept();
    	ConceptName conceptName = new ConceptName(name, locale);
    	conceptName.setCreator(ncdUser);
    	concept.addName(conceptName);
    	concept.setConceptClass(conceptClass);
    	concept.setDatatype(conceptDatatype);
    	concept.setCreator(ncdUser);
    
    	return concept;
    }
    
    private static void fixDateConcept(ConceptService conceptService, String conceptName, ConceptDatatype newType) 
    {
        log.debug("fixing date concept \"" + conceptName + "\"");

        Concept concept = conceptService.getConceptByName(conceptName);
        concept.setDatatype(newType);
        conceptService.saveConcept(concept);
        
        // Get all obs with values using that concept
        ObsService obsService = Context.getObsService();
        List<Concept> conceptList = new ArrayList<Concept>();
        conceptList.add(concept);
        List<Obs> affectedObs = obsService.getObservations(null, null, conceptList, null, null, null, null, null, null, null, null, true);

        if (log.isDebugEnabled()) 
        {
            log.debug("affected obs=" + affectedObs);
        }

        // Build a DateFormat to accept the old string date/time format
        DateFormat fmt = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        fmt.setLenient(true);
        
        // For each affected obs
        for (Obs o : affectedObs) 
        {
            if (o.getValueText() != null && o.getValueDatetime() == null) 
            {    
                try 
                {    
                    log.debug("text value=\"" + o.getValueText() + "\"");
    
                    // Convert the valueText to a date/time
                    Date newDateTime = fmt.parse(o.getValueText());
        
                    log.debug("date value=" + newDateTime);
    
                    // Null out the valueText and store the valueDatetime
                    o.setValueText(null);
                    o.setValueDatetime(newDateTime);
                    
                    // Save it
                    obsService.saveObs(o, "Concept data type changed");
                }
                catch (Exception pe) 
                {
                    log.error("Exception converting obs value \"" + o.getValueText() + "\"", pe);
                }
            }
        }
    }
    
    public static boolean debugging() 
    {
        AdministrationService adminService = Context.getAdministrationService();
        String value = adminService.getGlobalProperty("ncd.debug");
    
        return value.equalsIgnoreCase("true");
    }        
}

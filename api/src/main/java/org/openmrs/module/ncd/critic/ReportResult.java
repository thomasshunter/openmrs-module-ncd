/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.critic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.module.ncd.database.HL7Producer;
import org.openmrs.module.ncd.database.Condition;
import org.openmrs.module.ncd.database.CriticDef;
import org.openmrs.module.ncd.database.DecidedResult;
import org.openmrs.module.ncd.database.Institution;
import org.openmrs.module.ncd.database.CodeCondition;
import org.openmrs.module.ncd.model.Observation;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;


/**
 * This class serves as a memory store of facts known
 * about a particular segment.  It includes the
 * entire message being examined; whether the condition
 * is reportable, not reportable, or unknown; and the
 * condition if known.
 * @author jlbrown
 *
 */
public class ReportResult
{
    /**
     * An enumeration of the possible values received from a result critic.
     * Values and an explanation of their use follows:
     * NONE  - Initial value for all result processing.  This value
     * will not normally be returned by result critics.
     * DO_NOT_REPORT - Indicates that the result critic has found
     * indication of a condition that is not reported.
     * REPORT - Indicates that the result critic has found
     * indication that the segment being processed contains
     * a reportable condition.
     * UNKNOWN - Indicates that the result critic has not
     * found conclusive indication of whether the condition is
     * reportable or not reportable.
     * 
     * @author John Brown
     *
     */
    public enum ReportResultStatus
    {
        NONE("none"), 
        DO_NOT_REPORT("not-reported"), 
        REPORT("report"), 
        UNKNOWN("unknown");
        
        private final String text;
        
        ReportResultStatus(String statusText)
        {
            text = statusText;
        }
        
        public String getText()
        {
            return text;
        }
        
        public static ReportResultStatus textToStatus(String text)
        {
            ReportResultStatus retVal = null;
            for (ReportResultStatus status : ReportResultStatus.values())
            {
                if (text.equals(status.getText()))
                {
                    retVal = status;
                }
            }
            return retVal;
        }
        
        public static ReportResultStatus joinResultStatus(ReportResultStatus firstStatus, 
                ReportResultStatus secondStatus)
        {
            ReportResultStatus retVal = ReportResultStatus.UNKNOWN;
            
            if (firstStatus == REPORT || secondStatus == REPORT) {
                // Report has priority over Do_Not_Report, Unknown, and None
                retVal = REPORT;
            } else if (firstStatus == DO_NOT_REPORT || secondStatus == DO_NOT_REPORT) {
                // Do_Not_Report has priority over Unknown and None
                retVal = DO_NOT_REPORT;
            } else {
            	// We default to Unknown since None is an initial value only.
            	retVal = UNKNOWN;
            }
            
            return retVal;
        }
    }
    
    public class ResultInfo
    {
        int obrLoc;
        int obxStartLoc;
        int obxEndLoc;
        List<Node> resultNodes;
        CriticDef criticThatFoundResult;
        DecidedResult decidedResult;
        
        public ResultInfo(CriticDef critic)
        {
            criticThatFoundResult = critic;
            obrLoc = 0;
            obxStartLoc = 0;
            obxEndLoc = 0;
            resultNodes = null;
            decidedResult = null;
        }
        
        public ResultInfo(int obr, int obxStart, int obxEnd, CriticDef critic, List<Node> newResultSegments)
        {
            obrLoc = obr;
            obxStartLoc = obxStart;
            obxEndLoc = obxEnd;
            criticThatFoundResult = critic;
            decidedResult = null;
            if (newResultSegments != null) {
            	resultNodes = new ArrayList<Node>(newResultSegments);
            } else {
            	resultNodes = null;
            }
        }
        
        public ResultInfo(int obr, int obxStart, int obxEnd, CriticDef critic, 
        		DecidedResult newDecidedResult, List<Node> newResultSegments)
        {
            obrLoc = obr;
            obxStartLoc = obxStart;
            obxEndLoc = obxEnd;
            criticThatFoundResult = critic;
            decidedResult = newDecidedResult;
            if (newResultSegments != null) {
            	resultNodes = new ArrayList<Node>(newResultSegments);
            } else {
            	resultNodes = null;
            }
        }

        public int getObrLoc() {
            return obrLoc;
        }

        public void setObrLoc(int obrLoc) {
            this.obrLoc = obrLoc;
        }

        public int getObxEndLoc() {
            return obxEndLoc;
        }

        public void setObxEndLoc(int obxEndLoc) {
            this.obxEndLoc = obxEndLoc;
        }

        public int getObxStartLoc() {
            return obxStartLoc;
        }

        public void setObxStartLoc(int obxStartLoc) {
            this.obxStartLoc = obxStartLoc;
        }                
        
        public CriticDef getCriticThatFoundResult() {
            return criticThatFoundResult;           
        }
        
        public void setCriticThatFoundResult(CriticDef critic) {
            this.criticThatFoundResult = critic;
        }
        
        public DecidedResult getDecidedResult() {
            return this.decidedResult;
        }
        
        public void setDecidedResult(DecidedResult decidedResult) {
            this.decidedResult = decidedResult;
        }
        
        public List<Node> getResultNodes() {
			return resultNodes;
		}

		public void setResultNodes(List<Node> resultSegments) {
			this.resultNodes = resultSegments;
		}
    }

    private ReportResultStatus reportResultStatus;
    private String reasonForStatus;
    private HL7Producer applicationFacility;
    private Institution institution;
    private CodeCondition loincCode;
    private Set<Condition> conditionsFound = new HashSet<Condition>();
    private String message;
    private String sequenceId;
    private String routingId;
    private Map<String, ResultInfo> resultLocationMap = new HashMap<String, ResultInfo>();
    private Long indicatingCriticId;
    private boolean flaggedPositiveByDecidedResultCritic;
    private boolean flaggedNegativeByDecidedResultCritic;
    private boolean flaggedPositiveByCritic;
    private boolean flaggedNegativeByCritic;
    private boolean flaggedIndeterminateCondition;
       
    /**
     * Constructor to create a ReportResult object
     * and specify the message.
     * @param msg The message to store in the ReportResult
     * object being constructed.
     */
    public ReportResult(String msg)
    {
        message = msg;
    }

    /**
     * Copy constructor.
     * @param copy The ReportResult object to copy.
     */
    public ReportResult(ReportResult copy)
    {
        message = copy.getMessage();
        conditionsFound = copy.getConditions();
        reportResultStatus = copy.getReportResultStatus();
        applicationFacility = copy.getApplicationFacility();
        institution = copy.getInstitution();
        loincCode = copy.getLoincCode();
        reasonForStatus = copy.getReasonForStatus();
        resultLocationMap = copy.getResultLocationMap();
        flaggedPositiveByDecidedResultCritic = copy.isFlaggedPositiveByDecidedResultCritic();
        flaggedNegativeByDecidedResultCritic = copy.isFlaggedNegativeByDecidedResultCritic();
        flaggedPositiveByCritic = copy.isFlaggedPositiveByCritic();
        flaggedNegativeByCritic = copy.isFlaggedNegativeByCritic();
        flaggedIndeterminateCondition = copy.isFlaggedIndeterminateCondition(); 
    }

    /**
     * Constructor to create a ReportResult object
     * and specify the status.
     * @param resultStatus The status to store in the
     * ReportResult object being constructed.
     */
    public ReportResult(ReportResultStatus resultStatus)
    {
        reportResultStatus = resultStatus;
    }

    /**
     * Constructor that creates a ReportResult message
     * with the specified message and status.
     * @param msg The message to store in the ReportResult
     * object being constructed.
     * @param resultStatus The status to store in the
     * ReportResult object being constructed.
     */
    public ReportResult(String msg, ReportResultStatus resultStatus)
    {
        message = msg;
        reportResultStatus = resultStatus;
    }

    /**
     * Constructor that creates a ReportResult message
     * with the specified message, status, sequence ID, and routing ID.
     * @param msg The message to store in the ReportResult
     * object being constructed.
     * @param resultStatus The status to store in the
     * ReportResult object being constructed.
     * @param sequence The sequence ID for the message.
     * @param routing The routing ID for the message.
     */
    public ReportResult(String msg, ReportResultStatus resultStatus,
            String sequence, String routing)
    {
        message = msg;
        reportResultStatus = resultStatus;
        sequenceId = sequence;
        routingId = routing;
    }

    /**
     * Full constructor to specify all elements of the
     * ReportResult object during construction.
     * @param msg The message to store in the ReportResult
     * object being constructed.
     * @param resultStatus The status to store in the
     * ReportResult object being constructed.
     * @param cond The Condition object that is associated
     * with the segment under examination.
     * @param appFac The HL7Producer object that is
     * associated with the segment under examination.
     * @param institute The Institution object that is
     * associated with the segment under examination.
     * @param loinc The CodeCondition object that is associated
     * with the segment under examination.
     */
    public ReportResult(String msg, ReportResultStatus resultStatus,
            Condition cond, HL7Producer appFac,
            Institution institute, CodeCondition loinc, String statusReason,
            String sequence, String routing, Map<String, ResultInfo> resultLoc)
    {
        message = msg;
        reportResultStatus = resultStatus;
        applicationFacility = appFac;
        if (cond != null) {
        	conditionsFound.add(cond);
        }
        institution = institute;
        loincCode = loinc;
        reasonForStatus = statusReason;
        sequenceId = sequence;
        routingId = routing;
        if (resultLoc != null) {
        	resultLocationMap = resultLoc;
        } else {
        	resultLocationMap = new HashMap<String, ResultInfo>();
        }
    }

    public HL7Producer getApplicationFacility()
    {
        return applicationFacility;
    }

    public void setApplicationFacility(
            HL7Producer applicationFacility)
    {
        this.applicationFacility = applicationFacility;
    }

    public Institution getInstitution()
    {
        return institution;
    }

    public void setInstitution(Institution institution)
    {
        this.institution = institution;
    }

    public CodeCondition getLoincCode()
    {
        return loincCode;
    }

    public void setLoincCode(CodeCondition loincCode)
    {
        this.loincCode = loincCode;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public ReportResultStatus getReportResultStatus()
    {
        return reportResultStatus;
    }

    public void setReportResultStatus(ReportResultStatus reportResultStatus)
    {
        this.reportResultStatus = reportResultStatus;
    }
    
    public void setReportResultStatus(ReportResult otherResult) {
    	ReportResultStatus otherResultStatus = otherResult.getReportResultStatus();    	
    	ReportResultStatus newStatus = ReportResultStatus.joinResultStatus(reportResultStatus, otherResultStatus);
    	setReportResultStatus(newStatus);
    }

    public Set<Condition> getConditions()
    {
        return conditionsFound;
    }
    
    public int getNumConditions()
    {
        return conditionsFound.size();
    }
    
    public void clearConditionList()
    {
        conditionsFound.clear();
    }
    
    public void addCondition(Condition condition, int obr, int obxStart, int obxEnd,
            long criticId, DecidedResult decidedResult, List<Node> resultSegments)
    {
        if (!conditionsFound.contains(condition))
        {
            conditionsFound.add(condition);
            CriticDef critic = NCDUtilities.getService().findCriticById(criticId);
            ResultInfo loc = new ResultInfo(obr, obxStart, obxEnd, critic, decidedResult, resultSegments);
            resultLocationMap.put(condition!=null ? condition.getDisplayText() : "Unknown", loc);
        }
    }
    
    public void addCondition(Condition condition, List<Node> obxNodes, long criticId, DecidedResult decidedResult)
    {
    	if (obxNodes != null && ! obxNodes.isEmpty() && obxNodes.get(0).getNodeName().equals("OBX")) {
	        Observation firstObx = new Observation(obxNodes.get(0));
	        int obxStart = firstObx.getTestSetId();
	        int obr = firstObx.getOrderObservation().getTestSetId();
	        Observation lastObx = new Observation(obxNodes.get(obxNodes.size() - 1));
	        int obxEnd = lastObx.getTestSetId();
	        
	        addCondition(condition, obr, obxStart, obxEnd, criticId, decidedResult, obxNodes);
    	} else {
    		addCondition(condition, 0, 0, 0, criticId, decidedResult, null);
    	}
    }

    public void addCondition(Condition condition, int obr, int obxStart, int obxEnd, Long criticId, List<Node> resultNodes)
    {
        if (!conditionsFound.contains(condition))
        {
            conditionsFound.add(condition);
            CriticDef critic = null;
            if (criticId != null) 
            {
                critic = NCDUtilities.getService().findCriticById(criticId);
            }
            ResultInfo loc = new ResultInfo(obr, obxStart, obxEnd, critic, resultNodes);
            resultLocationMap.put(condition!=null ? condition.getDisplayText() : "Unknown", loc);
        }
    }
    
    public void addCondition(Condition condition, List<Node> obxNodes, Long criticId)
    {        
    	if (obxNodes != null && ! obxNodes.isEmpty() && obxNodes.get(0).getNodeName().equals("OBX")) {
	        Observation firstObx = new Observation(obxNodes.get(0));
	        int obxStart = firstObx.getTestSetId();
	        int obr = firstObx.getOrderObservation().getTestSetId();
	        Observation lastObx = new Observation(obxNodes.get(obxNodes.size() - 1));
	        int obxEnd = lastObx.getTestSetId();
	        addCondition(condition, obr, obxStart, obxEnd, criticId, obxNodes);
    	} else {
    		addCondition(condition, 0, 0, 0, criticId, obxNodes);
    	}        
    }        

    public String getReasonForStatus()
    {
        return reasonForStatus;
    }

    public void setReasonForStatus(String reason)
    {
        this.reasonForStatus = reason;
    }

    public String getSequenceId()
    {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId)
    {
        this.sequenceId = sequenceId;
    }

    public String getRoutingId()
    {
        return routingId;
    }

    public void setRoutingId(String routingId)
    {
        this.routingId = routingId;
    }

    public Map<String, ResultInfo> getResultLocationMap() {
        return resultLocationMap;
    }

    public void setResultLocationMap(Map<String, ResultInfo> resultLocationMap) {
        this.resultLocationMap = resultLocationMap;
    }       
    
    public ResultInfo getResultLocationForCondition(String condition)
    {
    	ResultInfo retVal = null;
    	if (resultLocationMap != null) {
    		retVal = resultLocationMap.get(condition);
    	}
        return retVal;
    }

    public Long getIndicatingCriticId() {
        return indicatingCriticId;
    }

    public void setIndicatingCriticId(Long indicatingCriticId) {
        this.indicatingCriticId = indicatingCriticId;
    }

	public boolean isFlaggedPositiveByDecidedResultCritic() {
		return flaggedPositiveByDecidedResultCritic;
	}

	public void setFlaggedPositiveByDecidedResultCritic(
			boolean flaggedPositiveByDecidedResultCritic) {
		this.flaggedPositiveByDecidedResultCritic = flaggedPositiveByDecidedResultCritic;
	}

	public boolean isFlaggedNegativeByDecidedResultCritic() {
		return flaggedNegativeByDecidedResultCritic;
	}

	public void setFlaggedNegativeByDecidedResultCritic(
			boolean flaggedNegativeByDecidedResultCritic) {
		this.flaggedNegativeByDecidedResultCritic = flaggedNegativeByDecidedResultCritic;
	}

	public boolean isFlaggedPositiveByCritic() {
		return flaggedPositiveByCritic;
	}

	public void setFlaggedPositiveByCritic(boolean flaggedPositiveByCritic) {
		this.flaggedPositiveByCritic = flaggedPositiveByCritic;
	}

	public boolean isFlaggedNegativeByCritic() {
		return flaggedNegativeByCritic;
	}

	public void setFlaggedNegativeByCritic(boolean flaggedNegativeByCritic) {
		this.flaggedNegativeByCritic = flaggedNegativeByCritic;
	}

	public boolean isFlaggedIndeterminateCondition() {
		return flaggedIndeterminateCondition;
	}

	public void setFlaggedIndeterminateCondition(
			boolean flaggedIndeterminateCondition) {
		this.flaggedIndeterminateCondition = flaggedIndeterminateCondition;
	}
}

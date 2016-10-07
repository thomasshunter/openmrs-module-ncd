/**
 * Copyright 2008 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.nlp;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.ncd.database.NlpDiscreteTerm;
import org.openmrs.module.ncd.model.MessageHeader;
import org.openmrs.module.ncd.model.Observation;
import org.openmrs.module.ncd.utilities.NCDUtilities;
import org.w3c.dom.Node;

/**
 *
 */
public class ReportCategorizer {
    
    static private HashMap<String, String> reportTypeClues = null;    
    
    static final private String FormattedText = "FT";
    static final private String Numeric = "NM";
    static final private String StructuredNumeric = "SN";
    
    static final private String TEXT = "text";
    static final private String NUM = "numeric";
    static final private String DISCRETE = "discrete";
    // Maximum length in characters in order for a result value to be considered discrete.
    // Note, the result value must also contain a discrete term and not have a potential numeric value
    // to be categorized as discrete.
    static final private int MAX_DISCRETE_LENGTH = 150; 
    static final private int MAX_NUMERIC_LENGTH = 6;
    
    static {
        loadReportTypeCluesMap();        
    }
    
    static private void loadReportTypeCluesMap() {
        reportTypeClues = new HashMap<String, String>();
        //TODO replace with loading from text file or DB.
        reportTypeClues.put(" RAD ", "radiology");
        reportTypeClues.put(" RADIOLOGY ", "radiology");
        reportTypeClues.put(" Pathologist: ", "pathology");
        reportTypeClues.put(" CYTOTECHNOLOGIST: ", "pathology");
        reportTypeClues.put(" AMERIPATH ", "pathology");
        reportTypeClues.put(" PATHOLOGIST: ", "pathology");
        reportTypeClues.put(" COPATHPLUS ", "pathology");
        reportTypeClues.put(" DISCHARGE ", "discharge");
        reportTypeClues.put(" ADMISSION ", "admit");
        reportTypeClues.put(" HISTORY AND PHYSICAL ", "admit");
        reportTypeClues.put(" ADMITTING NOTE ", "admit");
        reportTypeClues.put(" CLINIC ", "clinical");
        reportTypeClues.put(" CONSULTATION ", "consult");
        reportTypeClues.put(" OPERATIVE ", "operative");
        reportTypeClues.put(" PROCEDURE ", "operative");
        reportTypeClues.put(" MIC ", "microbiology");
        reportTypeClues.put(" CULTURE ", "microbiology");
        reportTypeClues.put(" EMERGENCY ROOM ", "emergency");            
    }           
    
    static public String determineFormat(Observation obx) {
        boolean binary = false;
        // gather data
        String obx5Lower = obx.getTestResultValue().toLowerCase();
        obx5Lower = StringNormalizer.removePunctuationAndPutSpacesAroundDashPercentAndAngleBrackets(obx5Lower);
        obx5Lower = " " + obx5Lower + " ";// pad

        String testFormat = "";
        String anyNumber = "[0-9]"; // number somewhere
        boolean numberFound = false;
        Pattern pExc3 = Pattern.compile(anyNumber);
        Matcher m2 = pExc3.matcher(obx5Lower);
        numberFound = m2.find();        

        // getting binary criteria
        List<NlpDiscreteTerm> discreteTermList = NCDUtilities.getService().getAllNlpDiscreteTerms();
        for(NlpDiscreteTerm discreteTerm : discreteTermList) {
            // is discrete term in obx or obx in discrete term? 
            // check if 'this is positve' has 'positive' in it or 'pos' (equals avoids none = none detected error)
        	String term = discreteTerm.getTerm();
            if (obx5Lower.contains(term) || term.equals(obx5Lower)) { 
                binary = true;
                break;
            }
        }

        // rules
        // RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
        // obx2 rules first ("sure thing")
        testFormat = "";
        String obx2 = obx.getTestDataType();
        if (obx2.equalsIgnoreCase(StructuredNumeric) || obx2.equalsIgnoreCase(Numeric)) {
            testFormat = NUM;
        }
        else if (obx2.equalsIgnoreCase(FormattedText)) {
            testFormat = TEXT;
        }
        else { // anything but a SN or NM or FT
        	int obx5Length = obx5Lower.trim().length();
            if (obx5Length > MAX_DISCRETE_LENGTH) { 
            	// extremely long obx5 probably text
                testFormat = TEXT; 
            }     
            else if (numberFound && obx5Length <= MAX_NUMERIC_LENGTH) { 
            	// number(s) found in a short result value, probably a numeric
                testFormat = NUM;
            }
            else if (binary) {  
            	// falls within the maximum discrete length and contains a discrete term, probably a discrete
                testFormat = DISCRETE;
            }
            else {
            	// default to categorizing this as a text result
                testFormat = TEXT;
            }
        }        

        return testFormat;
    }
    
    static public String determineType(List<Node> obxNodes, String reportFormat, String message) {        
        String reportType = ""; // reset
        
        if (reportFormat.equals(TEXT)) {
            reportType = processTextReports(obxNodes, message);// determine what kind of report - rad, path, etc
        } else if (reportFormat.equals(NUM)
                || reportFormat.equals(DISCRETE)) {
            reportType = null;
        }
        return reportType;
    }
    
    static private String processTextReports(List<Node> obxNodes, String message) {
        // determine what kind of report- rad, path, etc
        String reportType = null;
        
        String appFac = getAppFacText(message);
        appFac = " " + appFac + " ";
        appFac = appFac.toUpperCase();
        for (String clue : reportTypeClues.keySet()) {
            if (appFac.contains(clue)) {
                reportType = reportTypeClues.get(clue);
                break;
            }
        }
        
        if (reportType == null)
        {
            for (Node obxNode : obxNodes) {
                Observation obx = new Observation(obxNode);
                String obxText = obx.getTestResultValue();
                obxText = " " + obxText + " ";
                obxText = obxText.toUpperCase();
                for (String clue : reportTypeClues.keySet()) {
                    if (obxText.contains(clue)) {
                        reportType = reportTypeClues.get(clue);
                        break;
                    }
                }
            }
        }
        
        return reportType;
    }
    
    static private String getAppFacText(String message)
    {                
        return MessageHeader.getSendingApplication(message) + " " + MessageHeader.getSendingFacility(message);
    }
}

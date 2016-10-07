package org.openmrs.module.ncd.web.controller;

import org.openmrs.module.ncd.ConditionDetectorService;
import org.openmrs.module.ncd.database.ManualReviewStatusType;
import org.openmrs.module.ncd.database.ReportableResult;
import org.openmrs.module.ncd.utilities.NCDUtilities;

public class ReportableResultDetailForm {

	/** The ReportableResult used to populate this ReportableResultDetailForm */
	private ReportableResult result;
	/** The URL to return to on "success" */
	private String returnURL;
	private boolean showNavigationButtons = false;
	
	public ReportableResult getResult() {
		return result;
	}
	
	public void setResult(ReportableResult result) {
		this.result = result;
	}
	
	public int getManualReviewStatusTypeId() {
		return result.getManualReviewStatusType().getId();
	}
	
	public void getManualReviewStatusTypeId(int id) {
		ConditionDetectorService cds = NCDUtilities.getService();
		ManualReviewStatusType manualReviewStatus = cds.findReviewStatusTypeById(id);
		result.setManualReviewStatusType(manualReviewStatus);
		// TODO - save or update result
	}

    public String getReturnURL() {
        return returnURL;
    }

    public void setReturnURL(String returnURL) {
        this.returnURL = returnURL;
    }

    public boolean isShowNavigationButtons() {
		return showNavigationButtons;
	}

	public void setShowNavigationButtons(boolean showNavigationButtons) {
		this.showNavigationButtons = showNavigationButtons;
	}
}

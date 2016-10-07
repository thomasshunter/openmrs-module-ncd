package org.openmrs.module.ncd.web.controller;

import org.openmrs.module.ncd.database.Error;

public class ErrorDetailForm {
	/** The Error used to populate this ErrorDetailForm */
	private Error error;
	/** The URL to return to on "success" */
	private String returnURL;
	
	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

    public String getReturnURL() {
        return returnURL;
    }

    public void setReturnURL(String returnURL) {
        this.returnURL = returnURL;
    }
}

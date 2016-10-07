package org.openmrs.module.ncd.critic;

public class AmbiguousConditionException extends ResultCriticException {

	private static final long serialVersionUID = 6314220830682731460L;
	private String code;
	private String scaleType;
	
	public AmbiguousConditionException(String code, String scaleType) {
		super("LOINC code " + code + " has scale type " + scaleType + " but maps to more than one condition");
		this.code = code;
		this.scaleType = scaleType;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getScaleType() {
		return scaleType;
	}

	public void setScaleType(String scaleType) {
		this.scaleType = scaleType;
	}
}

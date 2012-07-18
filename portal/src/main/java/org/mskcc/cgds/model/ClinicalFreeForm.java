package org.mskcc.cgds.model;

/**
 * Represents a single row in the clinical_free_form table.
 * 
 * @author Selcuk Onur Sumer
 */
public class ClinicalFreeForm
{
	private int cancerStudyId;
	private String caseId;
	private String paramName;
	private String paramValue;
	
	public ClinicalFreeForm(int cancerStudyId, String caseId,
			String paramName, String paramValue)
	{
		super();
		this.cancerStudyId = cancerStudyId;
		this.caseId = caseId;
		this.paramName = paramName;
		this.paramValue = paramValue;
	}

	
	public int getCancerStudyId() {
		return cancerStudyId;
	}
	
	public String getCaseId() {
		return caseId;
	}
	
	public String getParamName() {
		return paramName;
	}
	
	public String getParamValue() {
		return paramValue;
	}
}

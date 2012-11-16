package org.mskcc.mondrian.client;

/**
 * Encapsulates clinical data returned by the cBio service getClinicalData
 * 
 * @author Dazhi Jiao
 */
public class ClinicalData {
	private String id;
	private double overallSurvivalMonths;
	private String overallSurvivalStatus;
	private double diseaseFreeSurvivalMonths;
	private String diseaseFreeSurvivalStatus;
	private double ageAtDiagnosis;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getOverallSurvivalMonths() {
		return overallSurvivalMonths;
	}
	public void setOverallSurvivalMonths(double overallSurvivalMonths) {
		this.overallSurvivalMonths = overallSurvivalMonths;
	}
	public String getOverallSurvivalStatus() {
		return overallSurvivalStatus;
	}
	public void setOverallSurvivalStatus(String overallSurvivalStatus) {
		this.overallSurvivalStatus = overallSurvivalStatus;
	}
	public double getDiseaseFreeSurvivalMonths() {
		return diseaseFreeSurvivalMonths;
	}
	public void setDiseaseFreeSurvivalMonths(double diseaseFreeSurvivalMonths) {
		this.diseaseFreeSurvivalMonths = diseaseFreeSurvivalMonths;
	}
	public String getDiseaseFreeSurvivalStatus() {
		return diseaseFreeSurvivalStatus;
	}
	public void setDiseaseFreeSurvivalStatus(String diseaseFreeSurvivalStatus) {
		this.diseaseFreeSurvivalStatus = diseaseFreeSurvivalStatus;
	}
	public double getAgeAtDiagnosis() {
		return ageAtDiagnosis;
	}
	public void setAgeAtDiagnosis(double ageAtDiagnosis) {
		this.ageAtDiagnosis = ageAtDiagnosis;
	}
	public String toString() {
		return id;
	}
}

package org.mskcc.cgds.model;

/**
 * Encapsulates Clinical Data.
 *
 * @author Ethan Cerami.
 */
public class ClinicalData {
    private String caseId;
    private Double overallSurvivalMonths;
    private String overallSurvivalStatus;
    private Double diseaseFreeSurvivalMonths;
    private String diseaseFreeSurvivalStatus;
    private Double ageAtDiagnosis;

    /**
     * Constructor.
     *
     * @param caseId                    Case ID.
     * @param overallSurvivalMonths     Overall Survival Months.
     * @param overallSurvivalStatus     Overall Survival Status.
     * @param diseaseFreeSurvivalMonths Disease Free Survival Months.
     * @param diseaseFreeSurvivalStatus Disease Free Survival Status.
     */
    public ClinicalData(String caseId, Double overallSurvivalMonths,
            String overallSurvivalStatus, Double diseaseFreeSurvivalMonths,
            String diseaseFreeSurvivalStatus, Double ageAtDiagnosis) {
        this.caseId = caseId;
        this.overallSurvivalMonths = overallSurvivalMonths;
        this.overallSurvivalStatus = overallSurvivalStatus;
        this.diseaseFreeSurvivalMonths = diseaseFreeSurvivalMonths;
        this.diseaseFreeSurvivalStatus = diseaseFreeSurvivalStatus;
        this.ageAtDiagnosis = ageAtDiagnosis;
    }

    /**
     * Gets the Case ID.
     * @return Case ID.
     */
    public String getCaseId() {
        return caseId;
    }

    /**
     * Gets the Overall Survival in Months.
     * @return overall survival in months.  A null object indicates no data.
     */
    public Double getOverallSurvivalMonths() {
        return overallSurvivalMonths;
    }

    /**
     * Sets the Overall Survival in Months.
     * @param overallSurvivalMonths overall survival in months.  A null object indicates no data.
     */
    public void setOverallSurvivalMonths(Double overallSurvivalMonths) {
        this.overallSurvivalMonths = overallSurvivalMonths;
    }

    /**
     * Gets the Overall Survival Status.
     * @return overall survival status.  A null object indicates no data.
     */
    public String getOverallSurvivalStatus() {
        return overallSurvivalStatus;
    }

    /**
     * Gets the Disease Free Survival in Months.
     * @return disease free survival in months.  A null object indicates no data.
     */
    public Double getDiseaseFreeSurvivalMonths() {
        return diseaseFreeSurvivalMonths;
    }

    /**
     * Gets the Disease Free Survival Stauts.
     * @return disease free survival status.  A null object indicates no data.
     */
    public String getDiseaseFreeSurvivalStatus() {
        return diseaseFreeSurvivalStatus;
    }

    /**
     * Gets the Age at Diagnosis.
     * @return age at diagnosis.  A null object indicates no data.
     */
    public Double getAgeAtDiagnosis() {
        return ageAtDiagnosis;
    }
}

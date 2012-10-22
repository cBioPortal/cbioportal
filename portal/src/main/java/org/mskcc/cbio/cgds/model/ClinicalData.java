/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.model;

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

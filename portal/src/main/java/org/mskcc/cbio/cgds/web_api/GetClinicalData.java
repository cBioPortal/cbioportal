package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoClinicalData;
import org.mskcc.cgds.model.ClinicalData;

import java.util.ArrayList;
import java.util.Set;

/**
 * Utility class to get clinical data
 */
public class GetClinicalData {
    private static final String NA = "NA";
    private static final String TAB = "\t";

    /**
     * Gets Clinical Data for the Specific Cases.
     *
     * @param caseIdList Target Case IDs.
     * @return String of Output.
     * @throws DaoException Database Error.
     */
    public static String getClinicalData(Set<String> caseIdList)
            throws DaoException {
        DaoClinicalData daoClinical = new DaoClinicalData();
        ArrayList<ClinicalData> caseSurvivalList = daoClinical.getCases(caseIdList);

        StringBuilder buf = new StringBuilder();
        if (caseSurvivalList.size() > 0) {
            buf.append("case_id\toverall_survival_months\toverall_survival_status\t"
                    + "disease_free_survival_months\tdisease_free_survival_status\tage_at_diagnosis\n");

            for (ClinicalData caseSurvival : caseSurvivalList) {
                buf.append(caseSurvival.getCaseId()).append(TAB);

                //  Make sure to specify NAs
                if (caseSurvival.getOverallSurvivalMonths() == null) {
                    buf.append(NA);
                } else {
                    buf.append(caseSurvival.getOverallSurvivalMonths());
                }
                buf.append(TAB);

                if (caseSurvival.getOverallSurvivalStatus() == null) {
                    buf.append(NA);
                } else {
                    buf.append(caseSurvival.getOverallSurvivalStatus());
                }
                buf.append(TAB);

                if (caseSurvival.getDiseaseFreeSurvivalMonths() == null) {
                    buf.append(NA);
                } else {
                    buf.append(caseSurvival.getDiseaseFreeSurvivalMonths());
                }
                buf.append(TAB);

                if (caseSurvival.getDiseaseFreeSurvivalStatus() == null) {
                    buf.append(NA);
                } else {
                    buf.append(caseSurvival.getDiseaseFreeSurvivalStatus());
                }
                buf.append(TAB);

                if (caseSurvival.getAgeAtDiagnosis() == null) {
                    buf.append(NA);
                } else {
                    buf.append(caseSurvival.getAgeAtDiagnosis());
                }
                buf.append("\n");
            }
            return buf.toString();
        } else {
            buf.append("Error:  No clinical data available for the case set or " 
                    + "case lists specified.  Number of cases:  ")
                    .append(caseIdList.size()).append("\n");
            return buf.toString();
        }
    }
}

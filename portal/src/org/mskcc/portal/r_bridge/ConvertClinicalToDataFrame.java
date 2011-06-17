package org.mskcc.portal.r_bridge;

import org.mskcc.portal.model.ClinicalData;
import org.mskcc.portal.model.ProfileDataSummary;

import java.util.ArrayList;

/**
 * Converts a List of ClinicalData Objects to an R Data Frame.
 *
 * @author Ethan Cerami.
 */
public class ConvertClinicalToDataFrame {
    private ArrayList<ClinicalData> clinicalDataList;
    private ProfileDataSummary dataSummary;
    private String QUOTE = "\"";

    /**
     * Constructor.
     *
     * @param clinicalDataList ArrayList of Clinical Data Objects.
     */
    public ConvertClinicalToDataFrame(ArrayList<ClinicalData> clinicalDataList,
            ProfileDataSummary dataSummary) {
        this.clinicalDataList = clinicalDataList;
        this.dataSummary = dataSummary;
    }

    /**
     * Gets the R Code to Generate a Data Frame of this Clinical Data.
     *
     * @return
     */
    public String getRCode() {
        int numItems = clinicalDataList.size();
        StringBuffer rCode = new StringBuffer();
        rCode.append("df <- data.frame(CASE_ID=rep(\"NA\"," + numItems + "), "
                + " OS_MONTHS=rep(NA, " + numItems + "), "
                + " OS_STATUS=rep(NA, " + numItems + "), "
                + " DFS_MONTHS=rep(NA, " + numItems + "), "
                + " DFS_STATUS=rep(NA, " + numItems + "), "
                + " GENE_SET_ALTERED=rep(NA, " + numItems + "), "
                + " stringsAsFactors=FALSE)\n");
        for (int i = 0; i < clinicalDataList.size(); i++) {
            ClinicalData clinicalData = clinicalDataList.get(i);
            int rIndex = i + 1;

            // status = 1 (Died from Disease)
            // status = 0 (Still alive at last follow-up)
            rCode.append("df[" + rIndex + ", ] <- list(\"" + clinicalData.getCaseId() + "\",");
            if (clinicalData.getOverallSurvivalMonths() == null) {
                rCode.append("NA");
            } else {
                rCode.append(clinicalData.getOverallSurvivalMonths());
            }
            rCode.append(", ");
            String osStatus = clinicalData.getOverallSurvivalStatus();
            if (osStatus == null || osStatus.length() == 0) {
                rCode.append("NA");
            } else {
                if (osStatus.equalsIgnoreCase("DECEASED")) {
                    rCode.append("1");
                } else if (osStatus.equalsIgnoreCase("LIVING")) {
                    rCode.append("0");
                } else {
                    throw new IllegalArgumentException("Could not parse OS status:  " +
                            clinicalData.getOverallSurvivalStatus());
                }
            }
            rCode.append(", ");
            if (clinicalData.getDiseaseFreeSurvivalMonths() == null) {
                rCode.append("NA");
            } else {
                rCode.append(clinicalData.getDiseaseFreeSurvivalMonths());
            }
            rCode.append(", ");
            String dfsStatus = clinicalData.getDiseaseFreeSurvivalStatus();
            if (dfsStatus == null || dfsStatus.length() == 0) {
                rCode.append("NA");
            } else {
                if (dfsStatus.equalsIgnoreCase("Recurred/Progressed")
                        || dfsStatus.equalsIgnoreCase("Recurred")) {
                    rCode.append("1");
                } else if (dfsStatus.equalsIgnoreCase("DiseaseFree")) {
                    rCode.append("0");
                } else {
                    throw new IllegalArgumentException("Could not parse DFS status:  " +
                            dfsStatus);
                }
            }

            rCode.append(", ");
            boolean caseIsAltered = dataSummary.isCaseAltered(clinicalData.getCaseId());
            if (caseIsAltered) {
                rCode.append("TRUE");
            } else {
                rCode.append("FALSE");
            }

            rCode.append(")\n");
        }
        rCode.append("df = transform (df, OS_MONTHS=as.double(OS_MONTHS))\n");
        rCode.append("df = transform (df, DFS_MONTHS=as.double(DFS_MONTHS))\n");
        return rCode.toString();
    }
}

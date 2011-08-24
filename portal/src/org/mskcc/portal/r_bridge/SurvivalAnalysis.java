package org.mskcc.portal.r_bridge;

import org.mskcc.portal.model.ProfileDataSummary;
import org.mskcc.cgds.model.ClinicalData;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import java.util.ArrayList;
import java.io.IOException;

/**
 * Performs Survival Analysis via R.
 *
 * @author Ethan Cerami.
 */
public class SurvivalAnalysis {
    private StringBuffer rCode;
    private double osLogRankPValue;
    private String osSurvivalTable;
    private double dfsLogRankPValue;
    private String dfsSurvivalTable;
    private int numGroups = 0;
    private int osError = 0;
    private int dfsError = 0;
    public static final int PLOT_WIDTH = 600;
    public static final int PLOT_HEIGHT = 600;

    /**
     * Constructor.
     * @param clinicalDataList  Array List of Clinical Data Objects.
     * @param dataSummary       Profile Data Summary Object.
     * @throws IOException      IO Error.
     */
    public SurvivalAnalysis (ArrayList<ClinicalData> clinicalDataList,
            ProfileDataSummary dataSummary) throws IOException,
            REXPMismatchException, REngineException {
        ConvertClinicalToDataFrame rConverter = new ConvertClinicalToDataFrame
                (clinicalDataList, dataSummary);

        rCode = new StringBuffer();
        String rDataFrame = rConverter.getRCode();
        rCode.append (rDataFrame);

        RTemplate rTemplate = new RTemplate("survival_no_plots.txt");
        String rSurvivalCode = rTemplate.getRTemplate();
        rCode.append(rSurvivalCode);

        RConnection c = new RConnection();
        c.parseAndEval(rCode.toString());
        osError = c.parseAndEval("os_error").asInteger();
        dfsError = c.parseAndEval("dfs_error").asInteger();
        numGroups = c.parseAndEval("num_groups").asInteger();
        if (osError == 0 && numGroups > 0) {
            osLogRankPValue = c.parseAndEval("os_p_val").asDouble();
            osSurvivalTable = c.eval("paste(capture.output(print(os_surv_fit)),collapse=\"\\n\")").asString();
        }

        if (dfsError == 0 && numGroups > 0) {
            dfsLogRankPValue = c.parseAndEval("dfs_p_val").asDouble();
            dfsSurvivalTable = c.eval("paste(capture.output(print(dfs_surv_fit)),collapse=\"\\n\")").asString();
        }
        c.close();
    }

    /**
     * Gets the R Code for Executing the Survival Analysis.
     * @return R Code.
     */
    public String getRCode() {
        return rCode.toString();
    }

    public double getOsLogRankPValue() {
        return osLogRankPValue;
    }

    public String getOsSurvivalTable() {
        return osSurvivalTable;
    }

    public double getDfsLogRankPValue() {
        return dfsLogRankPValue;
    }

    public String getDfsSurvivalTable() {
        return dfsSurvivalTable;
    }

    public int numGroups() {
        return numGroups;
    }

    public int getOsError() {
        return osError;
    }

    public int getDfsError() {
        return dfsError;
    }
}

class RException extends Exception {
    public RException(String msg) {
        super("R error: \""+msg+"\"");
    }
}

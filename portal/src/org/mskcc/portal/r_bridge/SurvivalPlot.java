package org.mskcc.portal.r_bridge;

import org.mskcc.cgds.model.ClinicalData;
import org.mskcc.portal.model.ProfileDataSummary;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.REXP;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Generates a Survival Plot.
 *
 * @author Ethan Cerami
 */
public class SurvivalPlot {
    public static enum SurvivalPlotType {OS, DFS}
    public static final int PLOT_WIDTH = 600;
    public static final int PLOT_HEIGHT = 600;

    public SurvivalPlot (SurvivalPlotType plotType, ArrayList<ClinicalData> clinicalDataList,
            ProfileDataSummary dataSummary, String format, HttpServletResponse response) throws IOException {
        try {
            ConvertClinicalToDataFrame rConverter = new ConvertClinicalToDataFrame
                    (clinicalDataList, dataSummary);
            StringBuffer rCode = new StringBuffer();

            if (format == null || ! format.equalsIgnoreCase("pdf")) {
                format = "png";
            }

            //  Create Temp File
            String tmpfile = "tmp" + String.valueOf(System.currentTimeMillis()) + "." + format;
            rCode.append("library(Cairo);\n");
            if (format.equals("png")) {
                rCode.append("Cairo(width=" + PLOT_WIDTH + ", height="
                    + PLOT_HEIGHT + ", file='" + tmpfile + "', type='" + format + "', units=\"px\")\n");
            } else {
                rCode.append("pdf(width=6, height=6, file='" + tmpfile + "')\n");
            }
            String rDataFrame = rConverter.getRCode();
            rCode.append (rDataFrame);

            if (plotType == SurvivalPlotType.OS) {
                RTemplate rTemplate = new RTemplate("os_plot.txt");
                String rSurvivalCode = rTemplate.getRTemplate();
                rCode.append(rSurvivalCode);
            } else {
                RTemplate rTemplate = new RTemplate("dfs_plot.txt");
                String rSurvivalCode = rTemplate.getRTemplate();
                rCode.append(rSurvivalCode);
            }

            rCode.append ("dev.off();");
            RConnection c = new RConnection();
            System.out.println (rCode.toString());

            c.parseAndEval(rCode.toString());

            if (format.equals("png")) {
                response.setContentType("image/png");
            } else {
                response.setContentType("application/pdf");
            }
            // There is no I/O API in REngine because it's actually more efficient to use R for this
            // we limit the file size to 1MB which should be sufficient and we delete the file as well
            REXP xp = c.parseAndEval("r=readBin('" + tmpfile
                    + "','raw',1024*1024); unlink('" + tmpfile + "'); r;");

            // now this is pretty boring AWT stuff - create an image from the data and display it ...
            byte[] imageBytes = xp.asBytes();
            response.setContentLength(imageBytes.length);
            response.getOutputStream().write(imageBytes);
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            // if something goes wrong, send image redirect.
            if (format.equals("png")) {
                response.sendRedirect("images/plots_na.png");
            } else {
                response.sendRedirect("images/plots_na.pdf");
            }
        }
    }
}

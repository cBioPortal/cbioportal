package org.mskcc.portal.servlet;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.owasp.validator.html.PolicyException;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.ArrayList;

/**
 * Generates Plots via RServe.
 *
 * @author Anders Jacobsen, Ethan Cerami.
 */
public class PlotServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(PlotServlet.class);
    public static final String SKIN = "skin";
    public static final String SKIN_COL_GROUP = "skin_col_gp";
    public static final String SKIN_NORMALS = "skin_normals";
    public static final int PLOT_WIDTH = 600;
    public static final int PLOT_HEIGHT = 600;
    private static final String UNDEFINED = "undefined";

    private static ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException (e);
        }
    }

    /**
     * Processes GET Request.
     *
     * @param req       Http Servlet Request.
     * @param res       http Servlet Response.
     * @throws ServletException Servlet Error.
     * @throws IOException IO Error.
     */
    public void doGet(HttpServletRequest req,
                      HttpServletResponse res)
            throws ServletException, IOException {
        try {

            // Get All Parameters Safely
            Pattern p = Pattern.compile(",");
            // TODO: Later: ACCESS CONTROL: change to cancer study, etc.
            String cancerTypeId = servletXssUtil.getCleanInput(req, QueryBuilder.CANCER_STUDY_ID);
            String[] genesList = p.split(servletXssUtil.getCleanInput(req, QueryBuilder.GENE_LIST));
            String[] geneticProfilesList = p.split
                    (servletXssUtil.getCleanInput(req, QueryBuilder.GENETIC_PROFILE_IDS));
            String skin = servletXssUtil.getCleanInput(req, SKIN);
            String caseSetId = servletXssUtil.getCleanInput(req, QueryBuilder.CASE_SET_ID);
            String caseIds = servletXssUtil.getCleanInput(req, QueryBuilder.CASE_IDS);            
            String format = servletXssUtil.getCleanInput(req, QueryBuilder.FORMAT);
            String skinColGroup = servletXssUtil.getCleanInput(req, SKIN_COL_GROUP);
            String skinNormals = servletXssUtil.getCleanInput(req, SKIN_NORMALS);

            if (format == null || !format.equals("pdf")) {
                format = "png"; // default is png
            }

            // Split Gene List
            String genes = "";
            for (String s : genesList) {
                genes += "'" + s + "',";
            }

            genes = genes.substring(0, genes.length() - 1);
            String geneticProfiles = "";
            for (String s : geneticProfilesList) {
                geneticProfiles += "'" + s + "',";
            }

            geneticProfiles = geneticProfiles.substring(0, geneticProfiles.length() - 1);
            RConnection c = new RConnection();

            if (format.equals("pdf")) {
                res.setContentType("application/pdf");
            } else {
                res.setContentType("image/png");
            }

            String tmpfile = "tmp" + String.valueOf(System.currentTimeMillis() + "." + format);

            // Must use Cairo Library, so that we can generate Images without GUI
            StringBuffer plot = new StringBuffer("library(cgdsr);\n");

            plot.append("library(Cairo);\n");
            if (format.equals("png")) {
                plot.append("Cairo(width=" + PLOT_WIDTH + ", height="
                    + PLOT_HEIGHT + ", file='" + tmpfile + "', type='" + format + "', units=\"px\")\n");
            } else {
                plot.append("pdf(width=6, height=6, file='" + tmpfile + "')\n");
            }

            String currentUrl = req.getRequestURL().toString();
            logger.debug("Current URL is:  " + currentUrl);
            // locate host name to replace
            int startOfHostname = currentUrl.indexOf("//") + 2;
            int endOfHostname = currentUrl.indexOf(":", startOfHostname);
            if (endOfHostname == -1) {
                endOfHostname = currentUrl.indexOf("/", startOfHostname) - 1;
            }
            String hostname = currentUrl.substring(startOfHostname, endOfHostname);
            String cgdsUrl = currentUrl.replaceAll("plot.(do|pdf)", "");
            cgdsUrl = cgdsUrl.replace(hostname, "127.0.0.1");
            logger.debug("Web API URL is:  " + cgdsUrl);

            plot.append ("c = CGDS('" + cgdsUrl + "',TRUE);\n");
            if (caseSetId != null && !caseSetId.equals("-1")) {
                plot.append (String.format("plot(c, '%s', c(%s), c(%s), '%s', skin='%s' ",
                        cancerTypeId, genes, geneticProfiles, caseSetId, skin));
            } else {
                ArrayList <String> caseList = new ArrayList<String>();
                for (String currentCase : caseIds.split("[\\s,]+")) {
                    currentCase = currentCase.trim();
                    if (currentCase.length() > 0) {
                        caseList.add(currentCase);
                    }
                }
                StringBuffer caseBuffer = new StringBuffer();
                for (int i=0; i<caseList.size(); i++) {
                    caseBuffer.append ("\"" + caseList.get(i) + "\"");
                    if (i < caseList.size() -1) {
                        caseBuffer.append (",");
                    }
                }

                plot.append (String.format("plot(c, '%s', c(%s), c(%s), cases=c(%s), skin='%s' ",
                        cancerTypeId, genes, geneticProfiles, caseBuffer.toString(), skin));
            }
            if (skinColGroup != null && !skinColGroup.equals(UNDEFINED)) {
                plot.append (", skin.col.gp=c(");
                if (skinColGroup.contains(",")) {
                    String colGroups [] = skinColGroup.split(",");
                    for (int i=0; i<colGroups.length; i++) {
                        plot.append ("'" + colGroups[i] +"'");
                        if (i < colGroups.length -1) {
                            plot.append (",");
                        }
                    }
                }
                else {
                    plot.append ("'" + skinColGroup + "'");
                }
                plot.append (")");
            }

            if (skinNormals != null) {
                plot.append (", skin.normals='" + skinNormals + "'");   
            }
            plot.append (");\n");
            plot.append ("dev.off();\n");

            logger.debug("Call to R Follows:");
            logger.debug(plot.toString());

            // open device
            c.parseAndEval(plot.toString());

            // There is no I/O API in REngine because it's actually more efficient to use R for this
            // we limit the file size to 1MB which should be sufficient and we delete the file as well
            REXP xp = c.parseAndEval("r=readBin('" + tmpfile
                    + "','raw',1024*1024); unlink('" + tmpfile + "'); r;");

            // now this is pretty boring AWT stuff - create an image from the data and display it ...
            byte[] imageBytes = xp.asBytes();
            res.setContentLength(imageBytes.length);
            res.getOutputStream().write(imageBytes);
            c.close();
        } catch (Exception e) {
            //  In the event of an exception, redirect to the Plot NA Image.
            logger.error(e);
            res.sendRedirect("images/plots_na.png");
        }
    }
}


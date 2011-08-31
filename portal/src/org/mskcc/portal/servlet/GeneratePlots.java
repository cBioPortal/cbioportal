package org.mskcc.portal.servlet;

import org.owasp.validator.html.PolicyException;
import org.mskcc.portal.remote.GetCaseSets;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.dao.DaoException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.net.URLEncoder;

/**
 * Generates an HTML Page with Embedded Plot Images.
 *
 * @author Ethan Cerami
 */
public class GeneratePlots extends HttpServlet {
    public static final String GENE = "gene";
    public static final String MRNA_PROFILE_ID = "mrnra_profile_id";
    public static final String NORMAL_CASE_SET_ID = "normal_case_set_id";
    public static final String CNA_PROFILE_ID = "cna_profile_id";
    public static final String METHYLATION_PROFILE_ID = "methylation_profile_id";
    public static final String RPPA_PROTEIN_PROFILE_ID = "rppa_protein_profile_id";
    public static final String MUTATION_PROFILE_ID = "mutation_profile_id";
    public static final String INCLUDE_NORMALS = "include_normals";
    public static final String R_INSTALLED = "r_installed";
    private static ServletXssUtil servletXssUtil;
    private boolean rInstalled = true;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        rInstalled = Boolean.parseBoolean(getInitParameter(R_INSTALLED));
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException (e);
        }
    }

    /**
     * Processes Get Request.
     * 
     * @param req   HttpServletRequest Object.
     * @param res   HttpServletResponse Object.
     * @throws ServletException Servlet Error.
     * @throws IOException IO Error.
     */
    public void doGet(HttpServletRequest req,
                      HttpServletResponse res)
            throws ServletException, IOException {
        try {
            PrintWriter writer = res.getWriter();

            //  Get HTTP Parameters Safely
            //  All of these parameters are required.
            String gene = servletXssUtil.getCleanInput (req, GENE);
            String cancerTypeId = servletXssUtil.getCleanInput(req, QueryBuilder.CANCER_STUDY_ID);
            String mutationProfileId = servletXssUtil.getCleanInput (req, MUTATION_PROFILE_ID);
            String mRNAProfileId = servletXssUtil.getCleanInput (req, MRNA_PROFILE_ID);
            String cnaProfileId = servletXssUtil.getCleanInput (req, CNA_PROFILE_ID);
            String methylationProfileId = servletXssUtil.getCleanInput (req, METHYLATION_PROFILE_ID);
            String rppaProteinProfileId = servletXssUtil.getCleanInput (req, RPPA_PROTEIN_PROFILE_ID);
            String caseSetId = servletXssUtil.getCleanInput(req, QueryBuilder.CASE_SET_ID);
            String normalCaseSetId = servletXssUtil.getCleanInput(req, NORMAL_CASE_SET_ID);
            String includeNormals = servletXssUtil.getCleanInput(req, INCLUDE_NORMALS);
            String caseIds = servletXssUtil.getCleanInput(req, QueryBuilder.CASE_IDS);
            String plotType = servletXssUtil.getCleanInput(req, QueryBuilder.PLOT_TYPE);
            String xdebug = servletXssUtil.getCleanInput(req, QueryBuilder.XDEBUG);

            res.setContentType("text/html");
            writer.print("<html><body>");

            //  Validate that required parameters are specified
            ArrayList<String> errorList = new ArrayList<String>();
            validateParameterExists(gene, GENE, errorList);
            validateParameterExists(cancerTypeId, QueryBuilder.CANCER_STUDY_ID, errorList);
            validateParameterExists(mRNAProfileId, MRNA_PROFILE_ID, errorList);
            validateParameterExists(cnaProfileId, CNA_PROFILE_ID, errorList);
            validateParameterExists(caseSetId, QueryBuilder.CASE_SET_ID, errorList);

            //  Output Possible Errors
            if (errorList.size() > 0) {
                writer.println ("<P>Please fix the following errors:");
                writer.println ("<ul>");
                for (String errorMessage: errorList) {
                    writer.println ("<li> " + errorMessage);
                }
                writer.println ("</ul>");
            } else {
               // TODO: Later: ACCESS CONTROL: change to cancer study, etc.
                //  Plot Image URLs look like this:
                //  plot.do?cancer_type_id=ova&gene_list=MDM2&genetic_profile_ids=ova_rae,
                //  ova_mrna_unified&data_types=disc,cont&case_set_id=ova_4way_complete
                ArrayList<CaseList> caseSets = GetCaseSets.getCaseSets(cancerTypeId);
                String caseSetName = "User-defined Case List";
                if (caseSetId != null) {
                    for (CaseList caseSet:  caseSets) {
                        if (caseSet.getStableId().equalsIgnoreCase(caseSetId)) {
                            caseSetName = caseSet.getName();
                        }
                    }
                }

                //  Output mRNA by CNA Box Plot
                if (mRNAProfileId != null && cnaProfileId != null && plotType.equalsIgnoreCase("mrna_cna")) {
                    StringBuffer url1 = new StringBuffer ();
                    if (!rInstalled) {
                        url1.append("http://172.21.218.47:8080/cgx/");
                    }
                    url1.append("plot.do?" + QueryBuilder.CANCER_STUDY_ID + "="
                        + cancerTypeId);
                    writer.append("<table cellspacing=15><tr><td>");
                    url1.append ("&" + QueryBuilder.GENE_LIST + "=" + gene);
                    url1.append ("&" + QueryBuilder.GENETIC_PROFILE_IDS + "=");
                    url1.append (cnaProfileId + "," + mRNAProfileId);
                    url1.append ("&" + PlotServlet.SKIN + "=cna_mrna_mut");
                    if (mutationProfileId != null) {
                        url1.append ("&" + PlotServlet.SKIN_COL_GROUP + "=" + mutationProfileId);
                    }
                    if (includeNormals != null && includeNormals.equalsIgnoreCase("INCLUDE_NORMALS")
                        && normalCaseSetId != null && normalCaseSetId.length() > 0) {
                        url1.append("&" + PlotServlet.SKIN_NORMALS + "=" + normalCaseSetId);
                    }
                    url1.append ("&" + QueryBuilder.CASE_SET_ID + "=" + caseSetId);
                    url1.append ("&" + QueryBuilder.CASE_IDS + "=" + URLEncoder.encode(caseIds));
                    if (xdebug != null) {
                        writer.append ("URL:  " + url1.toString());
                    }
                    String pdfUrl = url1.toString().replace("plot.do", "plot.pdf") + "&format=pdf";
                    writer.append("<B>" + gene.toUpperCase() + ":  "
                            + "mRNA Expression ("
                            + caseSetName + ") v. CNA");
                    writer.append (" [<a href='" + pdfUrl + "'>PDF</a>]");
                    writer.append ("</B><BR>");
                    writer.append ("<img width=600 height=600 src='" + url1.toString() + "'>");
                    writer.append("</td></tr></table>");
                }

                //  Output mRNA by Methylation Scatter Plot
                if (mRNAProfileId != null && methylationProfileId != null
                        && !methylationProfileId.equals("undefined") && plotType.equalsIgnoreCase("mrna_methylation")) {
                    StringBuffer url1 = new StringBuffer ();
                    if (!rInstalled) {
                        url1.append("http://172.21.218.47:8080/cgx/");
                    }
                    url1.append ("plot.do?" + QueryBuilder.CANCER_STUDY_ID + "=" + cancerTypeId);
                    writer.append("<table cellspacing=15><tr><td>");
                    url1.append ("&" + QueryBuilder.GENE_LIST + "=" + gene);
                    url1.append ("&" + QueryBuilder.GENETIC_PROFILE_IDS + "=");
                    url1.append (methylationProfileId + "," + mRNAProfileId);
                    url1.append ("&" + PlotServlet.SKIN + "=meth_mrna_cna_mut");
                    url1.append ("&" + QueryBuilder.CASE_SET_ID + "=" + caseSetId);
                    url1.append ("&" + QueryBuilder.CASE_IDS + "=" + URLEncoder.encode(caseIds));
                    if (mutationProfileId != null && cnaProfileId != null) {
                       url1.append ("&" + PlotServlet.SKIN_COL_GROUP + "=" + cnaProfileId
                        + "," + mutationProfileId);
                    }
                    if (includeNormals != null && includeNormals.equalsIgnoreCase("INCLUDE_NORMALS")
                        && normalCaseSetId != null && normalCaseSetId.length() > 0) {
                        url1.append("&" + PlotServlet.SKIN_NORMALS + "=" + normalCaseSetId);
                    }
                    String pdfUrl = url1.toString().replace("plot.do", "plot.pdf") + "&format=pdf";
                    writer.append("<B>" + gene.toUpperCase() + ":  "
                            + "mRNA Expression ("
                            + caseSetName + ") v. DNA Methylation");
                    writer.append (" [<a href='" + pdfUrl + "'>PDF</a>]");
                    writer.append("</B><BR>");
                    if (xdebug != null) {
                        writer.append ("URL:  " + url1.toString());
                    }
                    writer.append ("<img width=600 height=600 src='" + url1.toString() + "'>");
                    writer.append("</td></tr></table>");
                }

                //  Output mRNA by RPPA protein level Scatter Plot
                if (mRNAProfileId != null && rppaProteinProfileId != null
                        && plotType.equalsIgnoreCase("mrna_rppa_protein")) {
                    StringBuffer url1 = new StringBuffer ();
                    if (!rInstalled) {
                        url1.append("http://172.21.218.47:8080/cgx/");
                    }
                    url1.append ("plot.do?" + QueryBuilder.CANCER_STUDY_ID + "=" + cancerTypeId);
                    writer.append("<table cellspacing=15><tr><td>");
                    url1.append ("&" + QueryBuilder.GENE_LIST + "=" + gene);
                    url1.append ("&" + QueryBuilder.GENETIC_PROFILE_IDS + "=");
                    url1.append (mRNAProfileId + "," + rppaProteinProfileId);
                    url1.append ("&" + PlotServlet.SKIN + "=meth_mrna_cna_mut");
                    url1.append ("&" + QueryBuilder.CASE_SET_ID + "=" + caseSetId);
                    url1.append ("&" + QueryBuilder.CASE_IDS + "=" + URLEncoder.encode(caseIds));
                    if (mutationProfileId != null && cnaProfileId != null) {
                       url1.append ("&" + PlotServlet.SKIN_COL_GROUP + "=" + cnaProfileId
                        + "," + mutationProfileId);
                    }
                    if (includeNormals != null && includeNormals.equalsIgnoreCase("INCLUDE_NORMALS")
                        && normalCaseSetId != null && normalCaseSetId.length() > 0) {
                        url1.append("&" + PlotServlet.SKIN_NORMALS + "=" + normalCaseSetId);
                    }
                    String pdfUrl = url1.toString().replace("plot.do", "plot.pdf") + "&format=pdf";
                    writer.append("<B>" + gene.toUpperCase() + ":  "
                            + "RPPA protein level v. mRNA Expression ("+ caseSetName + ")");
                    writer.append (" [<a href='" + pdfUrl + "'>PDF</a>]");
                    writer.append("</B><BR>");
                    if (xdebug != null) {
                        writer.append ("URL:  " + url1.toString());
                    }
                    writer.append ("<img width=600 height=600 src='" + url1.toString() + "'>");
                    writer.append("</td></tr></table>");
                }
            }
            //  Output HTTP Parameters (only if XDEBUG is set)
            if (xdebug != null) {
                writer.print("<P>Plot parameters:");
                if (caseIds == null || caseIds.length() == 0) {
                    caseIds = "[None specified]";
                }
                writer.print("<ul>");
                writer.println("<li>Gene:  " + gene);
                writer.println("<li>Cancer Type ID:  " + cancerTypeId);
                writer.println("<li>Case Set ID:  " + caseSetId);
                writer.println("<li>Case IDs:  " + caseIds);
                writer.println("<li>Normal Case Set ID:  " + normalCaseSetId);
                writer.println("<li>Include Normals:  " + includeNormals);
                writer.println("<li>Mutation Profile ID:  " + mutationProfileId);
                writer.println("<li>mRNA Profile ID:  " + mRNAProfileId);
                writer.println("<li>CNA Profile ID:  " + cnaProfileId);
                writer.println("<li>Methylation Profile ID:  " + methylationProfileId);
                writer.println("<li>RPPA protein Profile ID:  " + rppaProteinProfileId);
                writer.println("</ul>");
            }
            writer.println("</body></html>");
        } catch (DaoException e) {
            throw new ServletException (e);
        }
    }

    // Verifies that the Specified Parameter Exists
    private void validateParameterExists (String parameterValue,
        String parameterName, ArrayList<String> errorList) {
        if (parameterValue == null || parameterValue.length() ==0) {
            errorList.add("You must specify parameter:  " + parameterName);
        }
    }
}

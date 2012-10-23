package org.mskcc.cbio.portal.servlet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.taskdefs.Java;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.GeneticAlterationUtil;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.cgds.web_api.GetProfileData;
import org.mskcc.cbio.portal.model.GeneWithScore;
import org.mskcc.cbio.portal.model.GeneticEvent;
import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.model.ProfileDataSummary;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.mskcc.cbio.portal.util.*;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.*;

public class GeneAlterationsJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
    public static final String GENE_LIST = "gene_list";
    public static final String ACTION_NAME = "Action";
    // todo: can these strings be referenced directly from QueryBuilder itself?

    private static Log log = LogFactory.getLog(GisticJSON.class);

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String cancer_study_id = request.getParameter("cancer_study_id");

        String geneList = request.getParameter("genes");
        // list of genes separated by a space

        String _cases = request.getParameter("cases");
        // list of cases separated by a case.  This is so
        // that you can query by an arbitrary set of cases
        // separated by a space

        String _caseIds = request.getParameter("caseIds");
        // case ids of previously defined case sets
        // separated by a space

        String _geneticProfileIds = request.getParameter("geneticProfileIds");
        // list of geneticProfileIds separated by a space

        // todo: how should this *not* be hard coded?
        double zScoreThreshold = ZScoreUtil.Z_SCORE_THRESHOLD_DEFAULT;
        double rppaScoreThreshold = ZScoreUtil.RPPA_SCORE_THRESHOLD_DEFAULT;

        // ... do a bunch of work to get the matrix, basically copying out of QueryBuilder
        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(geneList,
                        geneticProfileIdSet, profileList, zScoreThreshold, rppaScoreThreshold);

        ArrayList<String> listOfGenes =
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
        String[] listOfGeneNames = new String[listOfGenes.size()];
        listOfGeneNames = listOfGenes.toArray(listOfGeneNames);

        ProfileDataSummary dataSummary = new ProfileDataSummary(mergedProfile,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold);

        GeneticEvent unsortedMatrix[][] = ConvertProfileDataToGeneticEvents.convert
			(dataSummary, listOfGeneNames,
			 theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold);

        // out.write the matrix

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
    }

    /**
     * Just in case the request changes from GET to POST
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doGet(request, response);
    }
}

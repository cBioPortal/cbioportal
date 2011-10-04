package org.mskcc.portal.servlet;

import org.mskcc.portal.util.XDebug;
import org.mskcc.portal.model.LinkOutRequest;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.remote.GetCaseSets;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.CategorizedGeneticProfileSet;
import org.mskcc.cgds.model.AnnotatedCaseSets;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.web_api.ProtocolException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Central Servlet for Stable LinkOuts.
 */
public class LinkOut extends HttpServlet {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException      IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles HTTP POST Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException      IO Error.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug(httpServletRequest);
        xdebug.startTimer();

        PrintWriter writer = httpServletResponse.getWriter();
        try {
            LinkOutRequest linkOutRequest = new LinkOutRequest(httpServletRequest);
            String cancerStudyId = linkOutRequest.getCancerStudyId();
            String output = linkOutRequest.getReport();
            String geneList = linkOutRequest.getGeneListUrlEncoded();
            HashMap<String, GeneticProfile> defaultGeneticProfileSet = getDefaultGeneticProfileSet(cancerStudyId);
            CaseList defaultCaseList = getDefaultCaseList(cancerStudyId);
            String url = createForwardingUrl(cancerStudyId, geneList, defaultGeneticProfileSet, defaultCaseList,
                output);
            // writer.write(url);
            ServletContext context = getServletContext();
            RequestDispatcher dispatcher = context.getRequestDispatcher(url);
            dispatcher.forward(httpServletRequest, httpServletResponse);
        } catch(ProtocolException e) {
            writer.write("Link out error:  " + e.getMsg());
        } catch (Exception e) {
            writer.write("Link out error:  " + e.toString());
        }
    }

    private String createForwardingUrl(String cancerStudyId, String geneList,
            HashMap<String, GeneticProfile> defaultGeneticProfileSet, CaseList defaultCaseList, String output) {
        StringBuffer url = new StringBuffer("/index.do?");
        appendParameter(QueryBuilder.GENE_LIST , geneList, url);
        appendParameter(QueryBuilder.CANCER_STUDY_ID, cancerStudyId, url);
        appendParameter(QueryBuilder.CASE_SET_ID, defaultCaseList.getStableId(), url);

        for (String geneticProfileId:  defaultGeneticProfileSet.keySet()) {
            appendParameter(QueryBuilder.GENETIC_PROFILE_IDS, geneticProfileId, url);
        }
        appendParameter(QueryBuilder.ACTION_NAME, QueryBuilder.ACTION_SUBMIT, url);
        appendParameter(QueryBuilder.TAB_INDEX, QueryBuilder.TAB_VISUALIZE, url);
        if (output.toLowerCase().equals(LinkOutRequest.REPORT_ONCOPRINT_HTML)) {
            appendParameter(QueryBuilder.OUTPUT, "html", url);
        }
        return url.toString();
    }

    private void appendParameter(String paramName, String paramValue, StringBuffer url) {
        url.append(paramName + "=" + paramValue + "&");
    }

    private CaseList getDefaultCaseList(String cancerStudyId) throws DaoException {
        ArrayList<CaseList> caseSetList = GetCaseSets.getCaseSets(cancerStudyId);
        AnnotatedCaseSets annotatedCaseSets = new AnnotatedCaseSets(caseSetList);
        CaseList defaultCaseList = annotatedCaseSets.getDefaultCaseList();
        if (defaultCaseList == null) {
            throw new DaoException("Could not determine case set for:  " + cancerStudyId);
        }
        return defaultCaseList;
    }

    private HashMap<String, GeneticProfile> getDefaultGeneticProfileSet(String cancerStudyId) throws DaoException {
        ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles (cancerStudyId);
        CategorizedGeneticProfileSet categorizedGeneticProfileSet =
                new CategorizedGeneticProfileSet(geneticProfileList);
        return categorizedGeneticProfileSet.getDefaultMutationAndCopyNumberMap();
    }
}
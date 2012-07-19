package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.portal.util.XDebug;
import org.mskcc.cbio.portal.model.LinkOutRequest;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.model.CategorizedGeneticProfileSet;
import org.mskcc.cbio.cgds.model.AnnotatedCaseSets;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.web_api.ProtocolException;
import org.apache.commons.collections15.iterators.IteratorEnumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
            String geneList = linkOutRequest.getGeneList();
            HashMap<String, GeneticProfile> defaultGeneticProfileSet = getDefaultGeneticProfileSet(cancerStudyId);
            CaseList defaultCaseList = getDefaultCaseList(cancerStudyId);
            ForwardingRequest forwardingRequest = new ForwardingRequest(httpServletRequest);
            createForwardingUrl(forwardingRequest, cancerStudyId, geneList, defaultGeneticProfileSet,
                defaultCaseList, output);
            ServletContext context = getServletContext();
            RequestDispatcher dispatcher = context.getRequestDispatcher("/index.do");
            dispatcher.forward(forwardingRequest, httpServletResponse);
        } catch(ProtocolException e) {
            writer.write("Link out error:  " + e.getMsg());
        } catch (Exception e) {
            writer.write("Link out error:  " + e.toString());
        }
    }

    private void createForwardingUrl(ForwardingRequest forwardingRequest, String cancerStudyId, String geneList,
            HashMap<String, GeneticProfile> defaultGeneticProfileSet, CaseList defaultCaseList, String output) {
        forwardingRequest.setParameterValue(QueryBuilder.GENE_LIST , geneList);
        forwardingRequest.setParameterValue(QueryBuilder.CANCER_STUDY_ID, cancerStudyId);
        forwardingRequest.setParameterValue(QueryBuilder.CASE_SET_ID, defaultCaseList.getStableId());

        List<String> geneticProfileList = new ArrayList<String>();
        for (String geneticProfileId:  defaultGeneticProfileSet.keySet()) {
            geneticProfileList.add(geneticProfileId);
        }
        forwardingRequest.setParameterValues(QueryBuilder.GENETIC_PROFILE_IDS,
                geneticProfileList.toArray(new String[geneticProfileList.size()]));

        forwardingRequest.setParameterValue(QueryBuilder.ACTION_NAME, QueryBuilder.ACTION_SUBMIT);
        forwardingRequest.setParameterValue(QueryBuilder.TAB_INDEX, QueryBuilder.TAB_VISUALIZE);
        if (output.toLowerCase().equals(LinkOutRequest.REPORT_ONCOPRINT_HTML)) {
            forwardingRequest.setParameterValue(QueryBuilder.OUTPUT, "html");
        }
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

// Forwarding Request, so that we can clear out all request parameters.
class ForwardingRequest extends HttpServletRequestWrapper {
    private Map<String, String[]> parameterMap = new HashMap<String, String[]>();

    public ForwardingRequest(HttpServletRequest request) {
        super(request);
    }

    public Map getParameterMap() {
        return parameterMap;
    }

    public void setParameterValue(String key, String value) {
        String values[] = new String[1];
        values[0] = value;
        parameterMap.put(key, values);
    }

    public void setParameterValues(String key, String[] values) {
        parameterMap.put(key, values);
    }

    public Enumeration getParameterNames() {
        return new IteratorEnumeration(parameterMap.keySet().iterator());
    }

    public String getParameter(String s) {
        String values[] = parameterMap.get(s);
        if (values != null && values.length > 0) {
            return values[0];
        } else {
            return null;
        }
    }

    public String[] getParameterValues(String s) {
        return parameterMap.get(s);
    }
}
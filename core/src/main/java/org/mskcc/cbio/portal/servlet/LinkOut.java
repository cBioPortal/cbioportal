/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.web_api.*;
import org.mskcc.cbio.portal.util.XDebug;
import org.mskcc.cbio.portal.dao.DaoException;

import org.apache.commons.collections15.iterators.IteratorEnumeration;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

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
            if (linkOutRequest.isIsCrossCancerQuery()) {
                handleCrossCancerLink(linkOutRequest, httpServletRequest, httpServletResponse);
            } else {
                handleStudySpecificLink(linkOutRequest, httpServletRequest, httpServletResponse);
            }
        } catch (Exception e) {
            writer.write("Link out error:  " + e.getMessage());
        }
    }
    
    private void handleCrossCancerLink(LinkOutRequest linkOutRequest,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        String geneList = linkOutRequest.getGeneList();
        ForwardingRequest forwardingRequest = new ForwardingRequest(httpServletRequest);
        createCrossCancerForwardingUrl(forwardingRequest, geneList);
        ServletContext context = getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/cross_cancer.do");
        dispatcher.forward(forwardingRequest, httpServletResponse);
    }

    private void createCrossCancerForwardingUrl(ForwardingRequest forwardingRequest, String geneList) {
        forwardingRequest.setParameterValue(QueryBuilder.GENE_LIST , geneList);
        forwardingRequest.setParameterValue(QueryBuilder.ACTION_NAME, QueryBuilder.ACTION_SUBMIT);
    }
    
    private void handleStudySpecificLink(LinkOutRequest linkOutRequest,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        String cancerStudyId = linkOutRequest.getCancerStudyId();
        String output = linkOutRequest.getReport();
        String geneList = linkOutRequest.getGeneList();
        HashMap<String, GeneticProfile> defaultGeneticProfileSet = getDefaultGeneticProfileSet(cancerStudyId);
        PatientList defaultCaseList = getDefaultPatientList(cancerStudyId);
        ForwardingRequest forwardingRequest = new ForwardingRequest(httpServletRequest);
        createStudySpecificForwardingUrl(forwardingRequest, cancerStudyId, geneList, defaultGeneticProfileSet,
            defaultCaseList, output);
        ServletContext context = getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/index.do");
        dispatcher.forward(forwardingRequest, httpServletResponse);
    }

    private void createStudySpecificForwardingUrl(ForwardingRequest forwardingRequest, String cancerStudyId, String geneList,
            HashMap<String, GeneticProfile> defaultGeneticProfileSet, PatientList defaultPatientList, String output) {
        forwardingRequest.setParameterValue(QueryBuilder.GENE_LIST , geneList);
        forwardingRequest.setParameterValue(QueryBuilder.CANCER_STUDY_ID, cancerStudyId);
        forwardingRequest.setParameterValue(QueryBuilder.CASE_SET_ID, defaultPatientList.getStableId());

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

    private PatientList getDefaultPatientList(String cancerStudyId) throws DaoException {
        ArrayList<PatientList> patientSetList = GetPatientLists.getPatientLists(cancerStudyId);
        AnnotatedPatientSets annotatedPatientSets = new AnnotatedPatientSets(patientSetList);
        PatientList defaultPatientList = annotatedPatientSets.getDefaultPatientList();
        if (defaultPatientList == null) {
            throw new DaoException("Could not determine patient set for:  " + cancerStudyId);
        }
        return defaultPatientList;
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
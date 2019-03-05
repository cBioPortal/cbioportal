/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.collections15.iterators.IteratorEnumeration;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.util.XDebug;
import org.mskcc.cbio.portal.web_api.*;

/**
 * Central Servlet for Stable LinkOuts.
 */
public class LinkOut extends HttpServlet {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servlet Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException      IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
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

    /**
     * Handles HTTP POST Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servlet Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException      IO Error.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException,
        IOException {
        doGet(httpServletRequest, httpServletResponse);
    }

    private void handleCrossCancerLink(LinkOutRequest linkOutRequest,
                                       HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws Exception {
        
        String hostURL;
        String geneList = linkOutRequest.getGeneList();
        if (httpServletRequest.getRequestURL().indexOf("/ln") != -1) {
            hostURL = httpServletRequest.getRequestURL().substring(0, httpServletRequest.getRequestURL().indexOf("/ln"));
        } else if (httpServletRequest.getRequestURL().indexOf("/link.do") != -1) {
            hostURL = httpServletRequest.getRequestURL().substring(0, httpServletRequest.getRequestURL().indexOf("/link.do"));
        } else {
            hostURL = "";
        }
        String redirectURL = createCrossCancerForwardingUrl(hostURL, geneList);
        httpServletRequest.setAttribute("redirect_url", redirectURL);
        RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher("/WEB-INF/jsp/linkoutRedirect.jsp");
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private String createCrossCancerForwardingUrl(String hostURL, String geneList) {
        String ret = hostURL + "/results/cancerTypesSummary?";
        ret += QueryBuilder.GENE_LIST+"="+geneList;
        ret += "&";
        ret += QueryBuilder.ACTION_NAME+"="+QueryBuilder.ACTION_SUBMIT;
        ret += "&";
        ret += QueryBuilder.CANCER_STUDY_ID+"=all";
        ret += "&";
        AccessControl accessControl = SpringUtil.getAccessControl();
        StringBuilder cancerStudyListBuilder = new StringBuilder();
        try {
            for (CancerStudy cs: accessControl.getCancerStudies()) {
                if (!cs.getCancerStudyStableId().equals("all") && cs.getCancerStudyStableId().contains("pan_can_atlas") ) {
                    cancerStudyListBuilder.append(",");
                    cancerStudyListBuilder.append(cs.getCancerStudyStableId());
                }
            }
        } catch (Exception e) {
        }
        String cancerStudyList = cancerStudyListBuilder.substring(1);
        ret += QueryBuilder.CANCER_STUDY_LIST+"="+cancerStudyList;
        ret += "&case_set_id=all&tab_index=tab_visualize";
       
        return ret;
    }

    private void createCrossCancerForwardingRequest(ForwardingRequest forwardingRequest, String geneList) {
        forwardingRequest.setParameterValue(QueryBuilder.GENE_LIST , geneList);
        forwardingRequest.setParameterValue(QueryBuilder.ACTION_NAME, QueryBuilder.ACTION_SUBMIT);
        if (forwardingRequest.getParameter(QueryBuilder.CANCER_STUDY_LIST) == null) {
            AccessControl accessControl = SpringUtil.getAccessControl();
            StringBuilder cancerStudyListBuilder = new StringBuilder();
            try {
                for (CancerStudy cs: accessControl.getCancerStudies()) {
                    cancerStudyListBuilder.append(",");
                    cancerStudyListBuilder.append(cs.getCancerStudyStableId());
                }
                forwardingRequest.setParameterValue(QueryBuilder.CANCER_STUDY_LIST, cancerStudyListBuilder.substring(1));
                forwardingRequest.setParameterValue(QueryBuilder.CANCER_STUDY_ID, "all");
            } catch (Exception e) {
            }

        }
    }

    private void handleStudySpecificLink(LinkOutRequest linkOutRequest,
                                         HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws Exception {
        String cancerStudyId = linkOutRequest.getCancerStudyId();
        String output = linkOutRequest.getReport();
        String geneList = linkOutRequest.getGeneList();
        HashMap<String, GeneticProfile> defaultGeneticProfileSet = getDefaultGeneticProfileSet(cancerStudyId);
        SampleList defaultCaseList = getDefaultSampleList(cancerStudyId);
        httpServletResponse.sendRedirect(createStudySpecificForwardingUrl(cancerStudyId, geneList,
            defaultGeneticProfileSet, defaultCaseList, output));
    }

    private String createStudySpecificForwardingUrl(String cancerStudyId, String geneList,
                                                    HashMap<String, GeneticProfile> defaultGeneticProfileSet, SampleList defaultSampleList, String output) {
        String ret = "results?";
        ret += QueryBuilder.GENE_LIST+"="+geneList;
        ret += "&";
        ret += QueryBuilder.ACTION_NAME+"="+QueryBuilder.ACTION_SUBMIT;
        ret += "&";
        ret += QueryBuilder.CANCER_STUDY_ID+"="+cancerStudyId;
        ret += "&";
        ret += QueryBuilder.CASE_SET_ID+"="+defaultSampleList.getStableId();
        ret += "&";
        String geneticProfiles = "";
        for (String geneticProfileId: defaultGeneticProfileSet.keySet()) {
            ret += QueryBuilder.GENETIC_PROFILE_IDS+"_PROFILE_"+defaultGeneticProfileSet.get(geneticProfileId).getGeneticAlterationType().name();
            ret += "="+geneticProfileId;
            ret += "&";
        }
        ret += QueryBuilder.TAB_INDEX+"="+QueryBuilder.TAB_VISUALIZE;
        if (output.toLowerCase().equals(LinkOutRequest.REPORT_ONCOPRINT_HTML)) {
            ret += "&";
            ret += QueryBuilder.OUTPUT+"=html";
        }
        return ret;
    }

    private void createStudySpecificForwardingRequest(ForwardingRequest forwardingRequest, String cancerStudyId, String geneList,
                                                      HashMap<String, GeneticProfile> defaultGeneticProfileSet, SampleList defaultSampleList, String output) {
        forwardingRequest.setParameterValue(QueryBuilder.GENE_LIST , geneList);
        forwardingRequest.setParameterValue(QueryBuilder.CANCER_STUDY_ID, cancerStudyId);
        forwardingRequest.setParameterValue(QueryBuilder.CASE_SET_ID, defaultSampleList.getStableId());

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

    private SampleList getDefaultSampleList(String cancerStudyId) throws DaoException {
        ArrayList<SampleList> sampleSetList = GetSampleLists.getSampleLists(cancerStudyId);
        AnnotatedSampleSets annotatedSampleSets = new AnnotatedSampleSets(sampleSetList);
        SampleList defaultSampleList = annotatedSampleSets.getDefaultSampleList();
        if (defaultSampleList == null) {
            throw new DaoException("Could not determine patient set for:  " + cancerStudyId);
        }
        return defaultSampleList;
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

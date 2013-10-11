/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.web_api.GetCaseLists;
import org.mskcc.cbio.portal.web_api.GetGeneticProfiles;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.model.ProfileDataSummary;
import org.mskcc.cbio.portal.model.CaseList;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.CategorizedGeneticProfileSet;
import org.mskcc.cbio.portal.model.AnnotatedCaseSets;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.web_api.GetProfileData;
import org.owasp.validator.html.PolicyException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.net.URLEncoder;

/**
 * Central Servlet for Summarizing One Cancer in a Cross-Cancer Summary.
 *
 * @author Ethan Cerami.
 */
public class CrossCancerSummaryServlet extends HttpServlet {
    public static final String DEFAULT_GENETIC_PROFILES = "DEFAULT_GENETIC_PROFILES";
    public static final String CANCER_STUDY_DETAILS_URL = "CANCER_STUDY_DETAILS_URL";

    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws javax.servlet.ServletException Servlet Init Error.
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
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
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
     * @throws java.io.IOException            IO Error.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug();
        xdebug.startTimer();

        // In order to process request, we must have a gene list, and a cancer type
        try {
            String geneList = servletXssUtil.getCleanInput(httpServletRequest, QueryBuilder.GENE_LIST);
            String cancerStudyId = httpServletRequest.getParameter(QueryBuilder.CANCER_STUDY_ID);

            //  Get all Genetic Profiles Associated with this Cancer Study ID.
            ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId);

            //  Get all Case Lists Associated with this Cancer Study ID.
            ArrayList<CaseList> caseSetList = GetCaseLists.getCaseLists(cancerStudyId);

            httpServletRequest.setAttribute(QueryBuilder.PROFILE_LIST_INTERNAL, geneticProfileList);
            httpServletRequest.setAttribute(QueryBuilder.CASE_SETS_INTERNAL, caseSetList);

            //  Get priority settings
            Integer dataTypePriority;
            try {
                dataTypePriority
                        = Integer.parseInt(httpServletRequest.getParameter(QueryBuilder.DATA_PRIORITY).trim());
            } catch (NumberFormatException e) {
                dataTypePriority = 0;
            }
            httpServletRequest.setAttribute(QueryBuilder.DATA_PRIORITY, dataTypePriority);

            //  Get the default case set
            AnnotatedCaseSets annotatedCaseSets = new AnnotatedCaseSets(caseSetList, dataTypePriority);
            CaseList defaultCaseSet = annotatedCaseSets.getDefaultCaseList();
            httpServletRequest.setAttribute(QueryBuilder.CASE_SET_ID, defaultCaseSet.getStableId());

            //  Get the default genomic profiles
            CategorizedGeneticProfileSet categorizedGeneticProfileSet =
                    new CategorizedGeneticProfileSet(geneticProfileList);
            HashMap<String, GeneticProfile> defaultGeneticProfileSet = null;
            switch (dataTypePriority) {
                case 2:
                    defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultCopyNumberMap();
                    break;
                case 1:
                    defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultMutationMap();
                    break;
                case 0:
                default:
                    defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultMutationAndCopyNumberMap();
            }
            httpServletRequest.setAttribute(DEFAULT_GENETIC_PROFILES, defaultGeneticProfileSet);

            //  Create URL for Cancer Study Details
            String cancerStudyDetailsUrl = createCancerStudyDetailsUrl(cancerStudyId,
                    defaultGeneticProfileSet, defaultCaseSet, geneList);
            httpServletRequest.setAttribute(CANCER_STUDY_DETAILS_URL, cancerStudyDetailsUrl);

            getGenomicData(cancerStudyId, defaultGeneticProfileSet, defaultCaseSet, geneList, caseSetList,
						   httpServletRequest,
						   httpServletResponse, xdebug);
            RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher("/WEB-INF/jsp/cross_cancer_summary.jsp");
            dispatcher.forward(httpServletRequest, httpServletResponse);
        } catch (DaoException e) {
            throw new ServletException (e);
        }
    }

    /**
     * Creates URL for Cancer Study Details.
     *
     * @param cancerStudyId             Cancer Study ID.
     * @param defaultGeneticProfileSet  Default Genetic Profile Set.
     * @param defaultCaseSet            Default Case Set.
     * @param geneList                  Gene List from User.
     */
    private String createCancerStudyDetailsUrl(String cancerStudyId,
            HashMap<String, GeneticProfile> defaultGeneticProfileSet, CaseList defaultCaseSet,
            String geneList) {
        String AMP = "&";

        //  Create the URL for Cancer Study Details
        StringBuffer detailsUrl = new StringBuffer(QueryBuilder.INDEX_PAGE + "?");

        //  Append Cancer Study ID
        detailsUrl.append(QueryBuilder.CANCER_STUDY_ID + "=" + URLEncoder.encode(cancerStudyId));
        
        //  Append all Genomic Profiles
        for (GeneticProfile geneticProfile:  defaultGeneticProfileSet.values()) {
            detailsUrl.append(AMP + QueryBuilder.GENETIC_PROFILE_IDS + "="
                    + URLEncoder.encode(geneticProfile.getStableId()));
        }

        //  Append Case Set ID
        detailsUrl.append(AMP + QueryBuilder.CASE_SET_ID + "="
                + URLEncoder.encode(defaultCaseSet.getStableId()));

        //  Append Genes
        detailsUrl.append(AMP + QueryBuilder.GENE_LIST + "="
                + URLEncoder.encode(geneList));

        //  Append action parameters
        detailsUrl.append("&"+QueryBuilder.ACTION_NAME+"="+QueryBuilder.ACTION_SUBMIT+"&tab_index=tab_visualize");

        return detailsUrl.toString();
    }

    /**
     * Gets all Genomic Data.
     */
    private void getGenomicData(String cancerStudyId, HashMap<String, GeneticProfile> defaultGeneticProfileSet,
            CaseList defaultCaseSet, String geneListStr, ArrayList<CaseList> caseList,
            HttpServletRequest request,
            HttpServletResponse response, XDebug xdebug) throws IOException,
            ServletException, DaoException {

        request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);        

        // parse geneList, written in the OncoPrintSpec language (except for changes by XSS clean)
        double zScore = ZScoreUtil.getZScore(new HashSet<String>(defaultGeneticProfileSet.keySet()),
                new ArrayList<GeneticProfile>(defaultGeneticProfileSet.values()), request);
        double rppaScore = ZScoreUtil.getRPPAScore(request);

        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(geneListStr,
                        new HashSet<String>(defaultGeneticProfileSet.keySet()),
                        new ArrayList<GeneticProfile>(defaultGeneticProfileSet.values()), zScore, rppaScore);

        ArrayList<String> geneList = new ArrayList<String>();
        geneList.addAll(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes());

        ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();
        Set<String> warningUnion = new HashSet<String>();

        String caseIds = defaultCaseSet.getCaseListAsString();

        for (GeneticProfile profile : defaultGeneticProfileSet.values()) {
            xdebug.logMsg(this, "Getting data for:  " + profile.getProfileName());
            xdebug.logMsg(this, "Using gene list:  " + geneList);
            GetProfileData remoteCall = new GetProfileData(profile, geneList, caseIds);
            ProfileData pData = remoteCall.getProfileData();
            warningUnion.addAll(remoteCall.getWarnings());
            profileDataList.add(pData);
        }

        xdebug.logMsg(this, "Merging Profile Data");
        ProfileMerger merger = new ProfileMerger(profileDataList);
        ProfileData mergedProfile = merger.getMergedProfile();

        xdebug.logMsg(this, "Merged Profile, Number of genes:  "
                + mergedProfile.getGeneList().size());
        xdebug.logMsg(this, "Merged Profile, Number of cases:  "
                + mergedProfile.getCaseIdList().size());

        request.setAttribute(QueryBuilder.MERGED_PROFILE_DATA_INTERNAL, mergedProfile);
        request.setAttribute(QueryBuilder.WARNING_UNION, warningUnion);
        String oncoPrintHtml = MakeOncoPrint.makeOncoPrint(cancerStudyId,
														   geneListStr,
														   mergedProfile,
														   caseList,
														   defaultCaseSet.getStableId(),
														   zScore,
														   rppaScore,
														   new HashSet<String>(defaultGeneticProfileSet.keySet()),
														   new ArrayList<GeneticProfile>(defaultGeneticProfileSet.values()),
														   false);
        ProfileDataSummary dataSummary = new ProfileDataSummary(mergedProfile,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScore, rppaScore);
        request.setAttribute(QueryBuilder.PROFILE_DATA_SUMMARY, dataSummary);
        request.setAttribute(QueryBuilder.ONCO_PRINT_HTML, oncoPrintHtml);
    }
}

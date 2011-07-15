package org.mskcc.portal.servlet;

import org.mskcc.portal.model.*;
import org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.portal.remote.GetCaseSets;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.remote.GetProfileData;
import org.mskcc.portal.util.*;
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

/**
 * Central Servlet for Summarizing One Cancer in a Cross-Cancer Summary.
 *
 * @author Ethan Cerami.
 */
public class CrossCancerSummaryServlet extends HttpServlet {
    public static final String DEFAULT_GENETIC_PROFILES = "DEFAULT_GENETIC_PROFILES";

    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws javax.servlet.ServletException Servlet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        String cgdsUrl = getInitParameter(QueryBuilder.CGDS_URL_PARAM);
        GlobalProperties.setCgdsUrl(cgdsUrl);
        String pathwayCommonsUrl = getInitParameter(QueryBuilder.PATHWAY_COMMONS_URL_PARAM);
        GlobalProperties.setPathwayCommonsUrl(pathwayCommonsUrl);
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

        //  This delay code is temporarily, and is used to test the AJAX spinners.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // In order to process request, we must have a gene list, and a cancer type
        String geneList = servletXssUtil.getCleanInput(httpServletRequest, QueryBuilder.GENE_LIST);
        String cancerStudyId = httpServletRequest.getParameter(QueryBuilder.CANCER_STUDY_ID);

        //  Get all Genetic Profiles Associated with this Cancer Study ID.
        ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId, xdebug);

        //  Get all Case Lists Associated with this Cancer Study ID.
        ArrayList<CaseSet> caseSetList = GetCaseSets.getCaseSets(cancerStudyId, xdebug);

        httpServletRequest.setAttribute(QueryBuilder.PROFILE_LIST_INTERNAL, geneticProfileList);
        httpServletRequest.setAttribute(QueryBuilder.CASE_SETS_INTERNAL, caseSetList);

        //  Get the default case set
        CaseSet defaultCaseSet = getDefaultCaseSet(caseSetList);
        httpServletRequest.setAttribute(QueryBuilder.CASE_SET_ID, defaultCaseSet.getId());
        
        //  Get the default genomic profiles
        HashMap<String, GeneticProfile> defaultGeneticProfileSet = getDefaultGeneticProfiles(geneticProfileList);
        httpServletRequest.setAttribute(DEFAULT_GENETIC_PROFILES, defaultGeneticProfileSet);

        getGenomicData (defaultGeneticProfileSet, defaultCaseSet, geneList, caseSetList,
                httpServletRequest,
                httpServletResponse, xdebug);
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/cross_cancer_summary.jsp");
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    /**
     * Gets all Genomic Data.
     */
    private void getGenomicData(HashMap<String, GeneticProfile> defaultGeneticProfileSet,
            CaseSet defaultCaseSet, String geneListStr, ArrayList<CaseSet> caseList,
            HttpServletRequest request,
            HttpServletResponse response, XDebug xdebug) throws IOException, ServletException {

        request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);        
        boolean showAlteredColumnsBool = true;

        // parse geneList, written in the OncoPrintSpec language (except for changes by XSS clean)
        double zScore = ZScoreUtil.getZScore(new HashSet<String>(defaultGeneticProfileSet.keySet()),
                new ArrayList<GeneticProfile>(defaultGeneticProfileSet.values()), request);
        double zScoreThreshold = ZScoreUtil.getZScore
                (new HashSet<String>(defaultGeneticProfileSet.keySet()),
                        new ArrayList<GeneticProfile>(defaultGeneticProfileSet.values()), request);

        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(geneListStr,
                        new HashSet<String>(defaultGeneticProfileSet.keySet()),
                        new ArrayList<GeneticProfile>(defaultGeneticProfileSet.values()), zScore);

        ArrayList<String> geneList = new ArrayList<String>();
        geneList.addAll(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes());

        ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();
        Set<String> warningUnion = new HashSet<String>();

        String caseIds = defaultCaseSet.getCaseListAsString();

        for (GeneticProfile profile : defaultGeneticProfileSet.values()) {
            xdebug.logMsg(this, "Getting data for:  " + profile.getName());
            xdebug.logMsg(this, "Using gene list:  " + geneList);
            GetProfileData remoteCall = new GetProfileData();
            ProfileData pData = remoteCall.getProfileData(profile, geneList, caseIds, xdebug);
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

        response.setContentType("text/html");
        MakeOncoPrint.OncoPrintType theOncoPrintType = MakeOncoPrint.OncoPrintType.HTML;
        MakeOncoPrint.makeOncoPrint(geneListStr, mergedProfile, caseList, defaultCaseSet.getId(),
                zScoreThreshold, theOncoPrintType, showAlteredColumnsBool,
                new HashSet<String>(defaultGeneticProfileSet.keySet()),
                new ArrayList<GeneticProfile>(defaultGeneticProfileSet.values()));

        ProfileDataSummary dataSummary = new ProfileDataSummary(mergedProfile,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold);
        request.setAttribute(QueryBuilder.PROFILE_DATA_SUMMARY, dataSummary);
    }

    /**
     * This code makes an attempts at selecting the "best" default case set.
     *
     * @param caseSetList List of all Case Sets.
     * @return the "best" default case set.
     */
    private CaseSet getDefaultCaseSet(ArrayList<CaseSet> caseSetList) {
        for (CaseSet caseSet : caseSetList) {
            String name = caseSet.getName();
            if (name.startsWith("All Complete Tumors")) {
                return caseSet;
            } else if (name.startsWith("All Tumors")) {
                return caseSet;
            }
        }

        // If there are no matches, return the 0th in the list.
        return caseSetList.get(0);
    }

    /**
     * This code makes an attempt at selecting the "best" default genomic profiles.
     *
     * @param geneticProfileList List of Genomic Profiles.
     * @return list of "best" default genomic profiles.
     */
    private HashMap<String, GeneticProfile> getDefaultGeneticProfiles(ArrayList<GeneticProfile> geneticProfileList) {
        HashMap<String, GeneticProfile> defaultSet = new HashMap<String, GeneticProfile>();
        boolean cnaChosen = false;
        for (GeneticProfile geneticProfile : geneticProfileList) {
            GeneticAlterationType geneticAlterationType = geneticProfile.getAlterationType();
            String name = geneticProfile.getName();
            if (geneticAlterationType.equals(GeneticAlterationType.MUTATION_EXTENDED)) {
                defaultSet.put(geneticProfile.getId(), geneticProfile);
            }
            if (name.contains("GISTIC") && cnaChosen == false) {
                defaultSet.put(geneticProfile.getId(), geneticProfile);
                cnaChosen = true;
            }
            if (name.contains("RAE") && cnaChosen == false) {
                defaultSet.put(geneticProfile.getId(), geneticProfile);
                cnaChosen = true;
            }
        }
        return defaultSet;
    }
}
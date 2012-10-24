package org.mskcc.cbio.portal.servlet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.cgds.web_api.GetProfileData;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.util.*;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class GeneAlterationsJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
    public static final String GENE_LIST = "gene_list";
    public static final String ACTION_NAME = "Action";
    // todo: can these strings be referenced directly from QueryBuilder itself?

    public static final String HUGO_GENE_SYMBOL = "hugoGeneSymbol";
    public static final String SAMPLE = "sample";
    public static final String UNALTERED_SAMPLE = "unaltered_sample";
    public static final String ALTERATION = "alteration";

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
     * Maps the matrix to a JSONArray of alterations
     * @param geneticEvents matrix M[case][gene]
     * @return
     */
    public JSONArray mapGeneticEventMatrix(GeneticEvent[][] geneticEvents) throws ServletException {
        JSONArray array = new JSONArray();

        for (GeneticEvent[] gene : geneticEvents) {
            
            Map map = new HashMap();
            JSONArray alterations = new JSONArray();
            map.put(HUGO_GENE_SYMBOL, gene[0].caseCaseId());        // just get it from the first element
            
            for (GeneticEvent case_ : gene) {      // case has another meaning...

                if (!map.get("hugoGeneSymbol").equals(case_.caseCaseId())) {
                    throw new ServletException("a matrix column normally " +
                            "representing a single gene has multiple genes");
                }
                
                Map caseJSON = new HashMap();
                caseJSON.put(SAMPLE, case_.caseCaseId());
                if (0 == case_.getCnaValue().compareTo(GeneticEventImpl.CNA.NONE)) {
                    caseJSON.put(UNALTERED_SAMPLE, true);
                } else {
                    caseJSON.put(UNALTERED_SAMPLE, false);
                }
                
//                caseJSON.put(ALTERATION, case_.getCnaValue()
//                        // shown or not shown?
//                        + "|" + case_.getMrnaValue()
//                        + "|" +  case_.getRPPAValue()

            }
        }

        return array;
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

        String _geneList = request.getParameter("genes");
        // list of genes separated by a space

        String caseIds = request.getParameter("cases");
        // list of cases separated by a space.  This is so
        // that you can query by an arbitrary set of cases
        // separated by a space

        String _geneticProfileIds = request.getParameter("geneticProfileIds");
        // list of geneticProfileIds separated by a space
        // e.g. gbm_mutations, gbm_cna_consensus

        HashSet<String> geneticProfileIdSet = new HashSet<String>(Arrays.asList(_geneticProfileIds.split(" ")));
        
        // map geneticProfileIds -> geneticProfiles
        Iterator<String> gpSetIterator =  geneticProfileIdSet.iterator();
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        if (gpSetIterator.hasNext()) {
            String gp_str = gpSetIterator.next();
            try {
                GeneticProfile gp = daoGeneticProfile.getGeneticProfileByStableId(gp_str);
                profileList.add(gp);
                // pointer to gp is local, but gets added to profileList which is outside
            } catch (DaoException e) {
                throw new ServletException(e);
            }
        }

        // todo: how should this *not* be hard coded?
        double zScoreThreshold = ZScoreUtil.Z_SCORE_THRESHOLD_DEFAULT;
        double rppaScoreThreshold = ZScoreUtil.RPPA_SCORE_THRESHOLD_DEFAULT;

        // ... do a bunch of work to get the matrix, basically copying out of QueryBuilder ...
        // todo: this is code duplication!
        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(_geneList,
                        geneticProfileIdSet, profileList, zScoreThreshold, rppaScoreThreshold);

        ArrayList<String> listOfGenes =
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
        String[] listOfGeneNames = new String[listOfGenes.size()];
        listOfGeneNames = listOfGenes.toArray(listOfGeneNames);

        ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();
        Iterator<String> profileIterator = geneticProfileIdSet.iterator();

        XDebug xdebug = new XDebug(request);
        while (profileIterator.hasNext()) {
            String profileId = profileIterator.next();
            GeneticProfile profile = GeneticProfileUtil.getProfile(profileId, profileList);
            if( null == profile ){
                continue;
            }

            xdebug.logMsg(this, "Getting data for:  " + profile.getProfileName());

            ArrayList<String> geneList = new ArrayList<String>(Arrays.asList(_geneList.split("\\s+")));
            GetProfileData remoteCall = null;
            try {
                remoteCall = new GetProfileData(profile, geneList, caseIds);
            } catch (DaoException e) {
                throw new ServletException(e);
            }
            ProfileData pData = remoteCall.getProfileData();
            if(pData == null){
                System.err.println("pData == null");
            } else {
                if (pData.getGeneList() == null ) {
                    System.err.println("pData.getValidGeneList() == null");
                }
            }
            if (pData != null) {
                xdebug.logMsg(this, "Got number of genes:  " + pData.getGeneList().size());
                xdebug.logMsg(this, "Got number of cases:  " + pData.getCaseIdList().size());
            }
            xdebug.logMsg(this, "Number of warnings received:  " + remoteCall.getWarnings().size());
            profileDataList.add(pData);
        }

        xdebug.logMsg(this, "Merging Profile Data");
        ProfileMerger merger = new ProfileMerger(profileDataList);
        ProfileData mergedProfile = merger.getMergedProfile();

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

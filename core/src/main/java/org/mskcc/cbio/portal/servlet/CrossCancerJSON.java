/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.portal.servlet;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.cgds.util.AccessControl;
import org.mskcc.cbio.cgds.web_api.GetProfileData;
import org.mskcc.cbio.cgds.web_api.ProtocolException;
import org.mskcc.cbio.portal.model.GeneWithScore;
import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.model.ProfileDataSummary;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.mskcc.cbio.portal.util.*;
import org.owasp.validator.html.PolicyException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CrossCancerJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CrossCancerJSON.class);

    // class which process access control to cancer studies
    private AccessControl accessControl;

    private ServletXssUtil servletXssUtil;


    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
        accessControl = (AccessControl)context.getBean("accessControl");

        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        XDebug xdebug = new XDebug();
        xdebug.startTimer();

        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();

        try {
            List resultsList = new LinkedList();

            // Get the gene list
            String geneList = servletXssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);

            // Get the priority
            Integer dataTypePriority;
            try {
                dataTypePriority
                        = Integer.parseInt(request.getParameter(QueryBuilder.DATA_PRIORITY).trim());
            } catch (NumberFormatException e) {
                dataTypePriority = 0;
            }

            //  Cancer All Cancer Studies
            List<CancerStudy> cancerStudiesList = accessControl.getCancerStudies();
            for (CancerStudy cancerStudy : cancerStudiesList) {
                String cancerStudyId = cancerStudy.getCancerStudyStableId();
                if(cancerStudyId.equalsIgnoreCase("all"))
                    continue;

                Map cancerMap = new LinkedHashMap();
                cancerMap.put("studyId", cancerStudyId);
                resultsList.add(cancerMap);

                //  Get all Genetic Profiles Associated with this Cancer Study ID.
                ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId);

                //  Get all Case Lists Associated with this Cancer Study ID.
                ArrayList<CaseList> caseSetList = GetCaseSets.getCaseSets(cancerStudyId);

                //  Get the default case set
                AnnotatedCaseSets annotatedCaseSets = new AnnotatedCaseSets(caseSetList, dataTypePriority);
                CaseList defaultCaseSet = annotatedCaseSets.getDefaultCaseList();

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

                cancerMap.put("caseSetId", defaultCaseSet.getStableId());
                cancerMap.put("caseSetLength", defaultCaseSet.getCaseList().size());

                ProfileDataSummary genomicData = getGenomicData(
                        cancerStudyId,
                        defaultGeneticProfileSet,
                        defaultCaseSet,
                        geneList,
                        caseSetList,
                        request,
                        response
                );

                ArrayList<GeneWithScore> geneFrequencyList = genomicData.getGeneFrequencyList();
                ArrayList<String> genes = new ArrayList<String>();
                for (GeneWithScore geneWithScore : geneFrequencyList)
                    genes.add(geneWithScore.getGene());
                int noOfMutated = 0,
                        noOfCnaUp = 0,
                        noOfCnaDown = 0,
                        noOfOther = 0,
                        noOfAll = 0;

                boolean skipStudy = defaultGeneticProfileSet.isEmpty();
                if(!skipStudy) {
                    for (String caseId: defaultCaseSet.getCaseList()) {
                        if(!genomicData.isCaseAltered(caseId)) continue;

                        boolean isAnyMutated = false,
                                isAnyCnaUp = false,
                                isAnyCnaDown = false;

                        for (String gene : genes) {
                            isAnyMutated |= genomicData.isGeneMutated(gene, caseId);
                            GeneticTypeLevel cnaLevel = genomicData.getCNALevel(gene, caseId);
                            boolean isCnaUp = cnaLevel != null && cnaLevel.equals(GeneticTypeLevel.Amplified);
                            isAnyCnaUp |= isCnaUp;
                            boolean isCnaDown = cnaLevel != null && cnaLevel.equals(GeneticTypeLevel.HomozygouslyDeleted);
                            isAnyCnaDown |= isCnaDown;
                        }

                        boolean isAnyCnaChanged = isAnyCnaUp || isAnyCnaDown;
                        if(isAnyMutated && !isAnyCnaChanged)
                            noOfMutated++;
                        else if(isAnyMutated && isAnyCnaChanged)
                            noOfOther++;
                        else if(isAnyCnaUp)
                            noOfCnaUp++;
                        else if(isAnyCnaDown)
                            noOfCnaDown++;

                        noOfAll++;
                    }
                }

                Map alterations = new LinkedHashMap();
                cancerMap.put("alterations", alterations);
                alterations.put("all", noOfAll);
                alterations.put("mutation", noOfMutated);
                alterations.put("cnaUp", noOfCnaUp);
                alterations.put("cnaDown", noOfCnaDown);
                alterations.put("other", noOfOther);
                cancerMap.put("genes", genes);
                cancerMap.put("skipped", skipStudy);
            }

            JSONValue.writeJSONString(resultsList, writer);
        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (ProtocolException e) {
            throw new ServletException(e);
        } finally {
            writer.close();
        }
    }

    /**
     * Gets all Genomic Data.
     */
    private ProfileDataSummary getGenomicData(String cancerStudyId, HashMap<String, GeneticProfile> defaultGeneticProfileSet,
                                              CaseList defaultCaseSet, String geneListStr, ArrayList<CaseList> caseList,
                                              HttpServletRequest request,
                                              HttpServletResponse response) throws IOException,
            ServletException, DaoException {

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
            GetProfileData remoteCall = new GetProfileData(profile, geneList, caseIds);
            ProfileData pData = remoteCall.getProfileData();
            warningUnion.addAll(remoteCall.getWarnings());
            profileDataList.add(pData);
        }

        ProfileMerger merger = new ProfileMerger(profileDataList);
        ProfileData mergedProfile = merger.getMergedProfile();

        request.setAttribute(QueryBuilder.MERGED_PROFILE_DATA_INTERNAL, mergedProfile);
        request.setAttribute(QueryBuilder.WARNING_UNION, warningUnion);
        ProfileDataSummary dataSummary = new ProfileDataSummary(mergedProfile,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScore, rppaScore);
        return dataSummary;
    }
}

/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.*;
import org.mskcc.cbio.portal.web_api.*;
import org.mskcc.cbio.portal.util.*;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class CrossCancerJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CrossCancerJSON.class);

    // class which process access control to cancer studies
    private AccessControl accessControl;

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
            String geneList = request.getParameter(QueryBuilder.GENE_LIST);

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
                cancerMap.put("typeOfCancer", cancerStudy.getTypeOfCancerId());
                resultsList.add(cancerMap);

                //  Get all Genetic Profiles Associated with this Cancer Study ID.
                ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId);

                //  Get all Patient Lists Associated with this Cancer Study ID.
                ArrayList<PatientList> patientSetList = GetPatientLists.getPatientLists(cancerStudyId);

                //  Get the default patient set
                AnnotatedPatientSets annotatedPatientSets = new AnnotatedPatientSets(patientSetList, dataTypePriority);
                PatientList defaultPatientSet = annotatedPatientSets.getDefaultPatientList();

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

                String mutationProfile = "", cnaProfile = "";
                for (GeneticProfile geneticProfile : defaultGeneticProfileSet.values()) {
                    GeneticAlterationType geneticAlterationType = geneticProfile.getGeneticAlterationType();
                    if(geneticAlterationType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION)) {
                        cnaProfile = geneticProfile.getStableId();
                    } else if(geneticAlterationType.equals(GeneticAlterationType.MUTATION_EXTENDED)) {
                        mutationProfile = geneticProfile.getStableId();
                    }
                }

                cancerMap.put("mutationProfile", mutationProfile);
                cancerMap.put("cnaProfile", cnaProfile);

                cancerMap.put("caseSetId", defaultPatientSet.getStableId());
                cancerMap.put("caseSetLength", defaultPatientSet.getPatientList().size());


                ProfileDataSummary genomicData = getGenomicData(
                        cancerStudyId,
                        defaultGeneticProfileSet,
                        defaultPatientSet,
                        geneList,
                        patientSetList,
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
                        noOfCnaLoss = 0,
                        noOfCnaGain = 0,
                        noOfOther = 0,
                        noOfAll = 0;

                boolean skipStudy = defaultGeneticProfileSet.isEmpty();
                if(!skipStudy) {
                    for (String patientId: defaultPatientSet.getPatientList()) {
                        if(!genomicData.isCaseAltered(patientId)) continue;

                        boolean isAnyMutated = false,
                                isAnyCnaUp = false,
                                isAnyCnaDown = false,
                                isAnyCnaLoss = false,
                                isAnyCnaGain = false
                        ;

                        for (String gene : genes) {
                            isAnyMutated |= genomicData.isGeneMutated(gene, patientId);
                            GeneticTypeLevel cnaLevel = genomicData.getCNALevel(gene, patientId);
                            boolean isCnaUp = cnaLevel != null && cnaLevel.equals(GeneticTypeLevel.Amplified);
                            isAnyCnaUp |= isCnaUp;
                            boolean isCnaDown = cnaLevel != null && cnaLevel.equals(GeneticTypeLevel.HomozygouslyDeleted);
                            isAnyCnaDown |= isCnaDown;
                            boolean isCnaLoss = cnaLevel != null && cnaLevel.equals(GeneticTypeLevel.HemizygouslyDeleted);
                            isAnyCnaLoss |= isCnaLoss;
                            boolean isCnaGain = cnaLevel != null && cnaLevel.equals(GeneticTypeLevel.Gained);
                            isAnyCnaGain |= isCnaGain;
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
                        else if(isAnyCnaGain)
                            noOfCnaGain++;
                        else if(isAnyCnaLoss)
                            noOfCnaLoss++;

                        noOfAll++;
                    }
                }

                Map alterations = new LinkedHashMap();
                cancerMap.put("alterations", alterations);
                alterations.put("all", noOfAll);
                alterations.put("mutation", noOfMutated);
                alterations.put("cnaUp", noOfCnaUp);
                alterations.put("cnaDown", noOfCnaDown);
                alterations.put("cnaLoss", noOfCnaLoss);
                alterations.put("cnaGain", noOfCnaGain);
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
                                              PatientList defaultPatientSet, String geneListStr, ArrayList<PatientList> patientList,
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

        for (GeneticProfile profile : defaultGeneticProfileSet.values()) {
            GetProfileData remoteCall =
                new GetProfileData(profile, geneList,
                                   StringUtils.join(StableIdUtil.getStableSampleIdsFromPatientIds(profile.getCancerStudyId(), defaultPatientSet.getPatientList()), " "));
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

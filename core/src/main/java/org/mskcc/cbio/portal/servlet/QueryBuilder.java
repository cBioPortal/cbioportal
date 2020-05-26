/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.web_api.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudy;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudyData;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudySamples;
import org.owasp.validator.html.PolicyException;

import org.springframework.security.core.userdetails.UserDetails;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.*;
import javax.servlet.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.rmi.RemoteException;


/**
 * Central Servlet for building queries.
 */
public class QueryBuilder extends HttpServlet {
    public static final String CLIENT_TRANSPOSE_MATRIX = "transpose_matrix";
    public static final String CANCER_TYPES_INTERNAL = "cancer_types";
    public static final String PROFILE_LIST_INTERNAL = "profile_list";
    public static final String CASE_SETS_INTERNAL = "case_sets";
    public static final String CANCER_STUDY_ID = "cancer_study_id";
    public static final String CANCER_STUDY_LIST = "cancer_study_list";
    public static final String HAS_SURVIVAL_DATA = "has_survival_data";
    public static final String GENETIC_PROFILE_IDS = "genetic_profile_ids";
    public static final String GENE_SET_CHOICE = "gene_set_choice";
    public static final String CASE_SET_ID = "case_set_id";
    public static final String CASE_IDS = "case_ids";
    public static final String CASE_IDS_KEY = "case_ids_key";
    public static final String SET_OF_CASE_IDS = "set_of_case_ids";
    public static final String CLINICAL_PARAM_SELECTION = "clinical_param_selection";
    public static final String GENE_LIST = "gene_list";
    public static final String GENESET_LIST = "geneset_list";
    public static final String ACTION_NAME = "Action";
    public static final String XDEBUG = "xdebug";
    public static final String ACTION_SUBMIT = "Submit";
    public static final String STEP1_ERROR_MSG = "step1_error_msg";
    public static final String STEP2_ERROR_MSG = "step2_error_msg";
    public static final String STEP3_ERROR_MSG = "step3_error_msg";
    public static final String STEP4_ERROR_MSG = "step4_error_msg";
    public static final String PROFILE_DATA_SUMMARY = "profile_data_summary";
    public static final String DOWNLOAD_LINKS = "download_links";
    public static final String OUTPUT = "output";
    public static final String HTML_TITLE = "html_title";
    public static final String TAB_INDEX = "tab_index";
    public static final String TAB_DOWNLOAD = "tab_download";
    public static final String TAB_VISUALIZE = "tab_visualize";
    public static final String USER_ERROR_MESSAGE = "user_error_message";
    public static final String ATTRIBUTE_URL_BEFORE_FORWARDING = "ATTRIBUTE_URL_BEFORE_FORWARDING";
    public static final String Z_SCORE_THRESHOLD = "Z_SCORE_THRESHOLD";
    public static final String RPPA_SCORE_THRESHOLD = "RPPA_SCORE_THRESHOLD";
    public static final String MRNA_PROFILES_SELECTED = "MRNA_PROFILES_SELECTED";
    public static final String COMPUTE_LOG_ODDS_RATIO = "COMPUTE_LOG_ODDS_RATIO";
    public static final int MUTATION_DETAIL_LIMIT = 100;
    public static final String MUTATION_DETAIL_LIMIT_REACHED = "MUTATION_DETAIL_LIMIT_REACHED";
    public static final String XDEBUG_OBJECT = "xdebug_object";
    public static final String ONCO_PRINT_HTML = "oncoprint_html";
    public static final String INDEX_PAGE = "index.do";
    public static final String DATA_PRIORITY = "data_priority";
    public static final String SELECTED_PATIENT_SAMPLE_ID_MAP = "selected_patient_sample_id_map";
    public static final String DB_VERSION = "db_version";
    public static final String DB_ERROR = "db_error";
    public static final String IS_VIRTUAL_STUDY = "is_virtual_study";
    private static final String DB_CONNECT_ERROR = ("An error occurred while trying to connect to the database." +
        "  This could happen if the database does not contain any cancer studies.");

    private static Log LOG = LogFactory.getLog(QueryBuilder.class);

    public static final String HAS_CANCER_TYPES = "has_cancer_types";

    private ServletXssUtil servletXssUtil;

    // class which process access control to cancer studies
    private AccessControl accessControl;

    private SessionServiceUtil sessionServiceUtil = new SessionServiceUtil();

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
            accessControl = SpringUtil.getAccessControl();
        } catch (PolicyException e) {
            throw new ServletException (e);
        }
    }

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servlet Response Object.
     * @throws ServletException Servlet Error.
     * @throws IOException      IO Error.
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
     * @param httpServletResponse Http Servlet Response Object.
     * @throws ServletException Servlet Error.
     * @throws IOException      IO Error.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException,
        IOException {

        XDebug xdebug = new XDebug( httpServletRequest );
        xdebug.startTimer();

        xdebug.logMsg(this, "Attempting to initiate new user query.");

        if (httpServletRequest.getRequestURL() != null) {
            httpServletRequest.setAttribute(ATTRIBUTE_URL_BEFORE_FORWARDING,
                httpServletRequest.getRequestURL().toString());
        }


        //  Get User Selected Genetic Profiles
        HashSet<String> geneticProfileIdSet = getGeneticProfileIds(httpServletRequest, xdebug);

        String dbPortalExpectedSchemaVersion = null;
        String dbActualSchemaVersion = null;
        //  Get all Cancer Types
        try {
            //  Get User Selected Action
            String action = httpServletRequest.getParameter(ACTION_NAME);

            //  Get User Defined Gene List
            String geneList = httpServletRequest.getParameter(GENE_LIST);
            if (httpServletRequest instanceof XssRequestWrapper) {
                geneList = ((XssRequestWrapper)httpServletRequest).getRawParameter(GENE_LIST);
            }
            geneList = servletXssUtil.getCleanInput(geneList);
            httpServletRequest.setAttribute(GENE_LIST, geneList);

            // Get data priority
            Integer dataTypePriority;
            try {
                dataTypePriority
                    = Integer.parseInt(httpServletRequest.getParameter(DATA_PRIORITY).trim());
            } catch (NumberFormatException | NullPointerException e) {
                dataTypePriority = 0;
            }
            httpServletRequest.setAttribute(DATA_PRIORITY, dataTypePriority);

            httpServletRequest.setAttribute(XDEBUG_OBJECT, xdebug);

            dbPortalExpectedSchemaVersion = GlobalProperties.getDbVersion();
            dbActualSchemaVersion = DaoInfo.getVersion();
            LOG.info("version - "+dbPortalExpectedSchemaVersion);
            LOG.info("version - "+dbActualSchemaVersion);
//            if (!dbPortalExpectedSchemaVersion.equals(dbActualSchemaVersion))
//            {
//                String extraMessage = "";
//                //extra message for the cases where property is missing (will happen often in transition period to this new versioning model):
//                if (dbPortalExpectedSchemaVersion.equals("0")) {
//                    extraMessage = "The db.version property also not found in your portal.properties file. This new property needs to be added by the administrator.";
//                }
//                String finalMessage = "Current DB schema version: " + dbActualSchemaVersion + "<br/>" +
//                    "DB schema version expected by Portal: " + dbPortalExpectedSchemaVersion + "<br/>" + extraMessage;
//                LOG.warn(finalMessage);
//                if (!GlobalProperties.isSuppressSchemaVersionMismatchErrors()) {
//                    throw new DbVersionException(finalMessage);
//                }
//            }

            // Get the example study queries configured as a skin property
            String[] exampleStudyQueries = GlobalProperties.getExampleStudyQueries().split("\n");
            httpServletRequest.setAttribute("exampleStudyQueries", exampleStudyQueries);

            // Validate query
//            boolean errorsExist = validateForm(action, cohortDetails, 
//                geneticProfileIdSet, sampleSetId, sampleIds, httpServletRequest);
            boolean errorsExist = false;

            // Query single / multiple studies?
            // --> Single study
            // -----> regular study
            // -----> virtual study
            // --> Multiple studies
            // -----> always build temporary cohort
            String cancerStudyId = httpServletRequest.getParameter(CANCER_STUDY_ID);
            String cancerStudyList = httpServletRequest.getParameter(CANCER_STUDY_LIST);
            Boolean _isVirtualStudy = false;
            Boolean addProfiles = false;
            
            //this is a common case for most of the requests from study view when the there is only one study
            if (cancerStudyId != null && cancerStudyId.equals("all") && cancerStudyList != null && cancerStudyList.split(",").length == 1) {
                cancerStudyId = cancerStudyList.split(",")[0];
                addProfiles = true;
            }

            //redirect requests with cancerStudyId="all" and cancerStudyList="all" to home page
            // handle different possible scenarios in https://github.com/cBioPortal/cbioportal/issues/3431
            if (!(cancerStudyId != null
                    && cancerStudyId.equals("all") 
                    && cancerStudyList != null 
                    && cancerStudyList.equals("all") )) {
                
                if (cancerStudyId != null || cancerStudyList != null) {
                    CancerStudy cancerStudy = null;
                    if(cancerStudyId != null) {
                        cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
                    }
                    
                        //single regular study
                    if(cancerStudy != null) {
                        cancerStudyList = null;
                        if(addProfiles) {
                            cancerStudy.getGeneticProfiles();
                            geneticProfileIdSet = new HashSet<String>();
                            GeneticProfile cnaProfile      = cancerStudy.getCopyNumberAlterationProfile(true);
                            GeneticProfile mutationProfile =  cancerStudy.getMutationProfile();
                            if(cnaProfile != null) {
                                geneticProfileIdSet.add(cnaProfile.getStableId());
                            }
                            if(mutationProfile != null) {
                                geneticProfileIdSet.add(mutationProfile.getStableId());
                            }
                            httpServletRequest.setAttribute(GENETIC_PROFILE_IDS, geneticProfileIdSet);
                        }
                    } 
                    //multiple and virtual study
                    else {
                        _isVirtualStudy = true;
                        if (cancerStudyId != null && !cancerStudyId.equals("all")) {
                            cancerStudyList = null;
                        } else {
                            if ((cancerStudyId == null || cancerStudyId.equals("all")) 
                            && (cancerStudyList!= null && !cancerStudyList.equals("all"))) {
                                cancerStudyId = "all";
                            }
                        }
                        
                    }
                    httpServletRequest.setAttribute(IS_VIRTUAL_STUDY, _isVirtualStudy);
                    if(httpServletRequest.getParameter(CASE_SET_ID) != null) {
                    	    httpServletRequest.setAttribute(CASE_SET_ID, httpServletRequest.getParameter(CASE_SET_ID));
                    	    if (httpServletRequest.getParameter(CASE_SET_ID).equals("-1")) {
                                httpServletRequest
                                    .setAttribute(CASE_IDS,httpServletRequest
                                                                .getParameter(CASE_IDS)
                                                                .replaceAll("\\\\n", "\n")
                                                                .replaceAll("\\\\t", "\t"));
                            }
                    	
                    }
                }
            }
            httpServletRequest.setAttribute(CANCER_STUDY_ID, cancerStudyId);
            httpServletRequest.setAttribute(CANCER_STUDY_LIST, cancerStudyList);
            
            if(cancerStudyId != null) {
                String queriedStudies = cancerStudyId;
                if (cancerStudyId.equals("all") 
                    && cancerStudyList != null) {
                    queriedStudies = cancerStudyList;
                }
                    
                Set<String> inputCohortIds = new HashSet<>(Arrays.asList(queriedStudies.split(",")));
                Set<VirtualStudy> studies = getStudies(inputCohortIds);
                Set<String> unKnownStudies = new HashSet<>();
                    
                if(studies.isEmpty()) {
                    unKnownStudies = inputCohortIds;
                } else {
                    unKnownStudies =  
                            studies
                                .stream()
                                .filter(obj -> {
                                    if(inputCohortIds.contains(obj.getId())) {
                                        for (VirtualStudySamples _study : obj.getData().getStudies()) {
                                            try {
                                                if (accessControl.isAccessibleCancerStudy(_study.getId()).size() != 1)
                                                   return true;
                                            } catch (Exception e) {
                                                return true;
                                            }
                                        }
                                            return false;
                                        } else {
                                            return true;
                                        }
                                })
                                .map(obj -> obj.getId())
                                .collect(Collectors.toSet());
                }
                
                errorsExist = errorsExist || unKnownStudies.size() > 0;
                // Dispatch to query result page
                if (action != null && action.equals(ACTION_SUBMIT) && (!errorsExist)) {
                    
                        Map<String, Set<String>> studySampleMap = new HashMap<>();
                        
                        studies
                            .stream()
                            .forEach(obj -> {
                                for (VirtualStudySamples cohortStudyCasesMap : obj.getData().getStudies()) {
                                    if (studySampleMap.containsKey(cohortStudyCasesMap.getId())) {
                                        String _sharedStudyId = cohortStudyCasesMap.getId();
                                        if (studySampleMap.get(_sharedStudyId).size() != 0) { // original entry does not contain all samples
                                            Set<String> mergedSet = mergeSets(studySampleMap.get(_sharedStudyId), cohortStudyCasesMap.getSamples());
                                            studySampleMap.put(cohortStudyCasesMap.getId(), mergedSet);
                                        } // otherwise keep the value as empty set
                                        //create new entry/key
                                    } else {
                                        studySampleMap.put(cohortStudyCasesMap.getId(), cohortStudyCasesMap.getSamples());
                                    }
                                }
                            });
                    
                    processData(studySampleMap, geneList, geneticProfileIdSet,
                        httpServletRequest.getParameter(CASE_SET_ID),
                        httpServletRequest.getParameter(CASE_IDS_KEY),
                        httpServletRequest.getParameter(CASE_IDS),
                        dataTypePriority, getServletContext(),
                        httpServletRequest, httpServletResponse, xdebug);
                    // Dispatch to home page (main query form)
                } else {
                    if (errorsExist) {
                        httpServletRequest.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "Please fix the errors below.");
                    }
                    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp");
                    dispatcher.forward(httpServletRequest, httpServletResponse);
                }
            } else {
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp");
                dispatcher.forward(httpServletRequest, httpServletResponse);
            }
            


        } catch (RemoteException e) {
            xdebug.logMsg(this, "Got Remote Exception:  " + e.getMessage());
            forwardToErrorPage(httpServletRequest, httpServletResponse,
                DB_CONNECT_ERROR, xdebug);
        } catch (DaoException e) {
            String errorMessage = "";
            if (dbPortalExpectedSchemaVersion != null && !dbPortalExpectedSchemaVersion.equals(dbActualSchemaVersion)) {
                errorMessage += "Error could also be related to incompatible DB schema version: DB is at schema version " + dbActualSchemaVersion +
                    " while portal expects schema version " + dbPortalExpectedSchemaVersion + ". ";
            }
            errorMessage += "Got Database Exception:  " + e.getMessage();
            xdebug.logMsg(this, errorMessage);
            forwardToErrorPage(httpServletRequest, httpServletResponse,
                DB_CONNECT_ERROR + " " + errorMessage, xdebug);
//        } catch (DbVersionException e) {
//            String errorMessage = "Mismatch between current DB schema version and DB schema version expected by portal. " + e.getMessage();
//            xdebug.logMsg(this, errorMessage);
//            forwardToErrorPage(httpServletRequest, httpServletResponse,
//                errorMessage, xdebug);
        }
        /*catch (ProtocolException e) {
            xdebug.logMsg(this, "Got Protocol Exception:  " + e.getMessage());
            forwardToErrorPage(httpServletRequest, httpServletResponse,
                               DB_CONNECT_ERROR, xdebug);
        }*/

    }

    /**
     * Gets all Genetic Profile IDs.
     *
     * These values are passed with parameter names like this:
     *
     * genetic_profile_ids
     * genetic_profile_ids_MUTATION
     * genetic_profile_ids_MUTATION_EXTENDED
     * genetic_profile_ids_COPY_NUMBER_ALTERATION
     * genetic_profile_ids_MRNA_EXPRESSION
     *
     *
     * @param httpServletRequest HTTPServlet Request.
     * @return HashSet of GeneticProfileIDs.
     */
    private HashSet<String> getGeneticProfileIds(HttpServletRequest httpServletRequest,
                                                 XDebug xdebug) {
        HashSet<String> geneticProfileIdSet = new HashSet<String>();
        Enumeration nameEnumeration = httpServletRequest.getParameterNames();
        while (nameEnumeration.hasMoreElements()) {
            String currentName = (String) nameEnumeration.nextElement();
            if (currentName.startsWith(GENETIC_PROFILE_IDS)) {
                String geneticProfileIds[] = httpServletRequest.getParameterValues(currentName);
                if (geneticProfileIds != null && geneticProfileIds.length > 0) {
                    for (String geneticProfileId : geneticProfileIds) {
                        xdebug.logMsg (this, "Received Genetic Profile ID:  "
                            + currentName + ":  " + geneticProfileId);
                        geneticProfileIdSet.add(geneticProfileId);
                    }
                }
            }
        }
        httpServletRequest.setAttribute(GENETIC_PROFILE_IDS, geneticProfileIdSet);
        return geneticProfileIdSet;
    }

    /**
     * process a good request
     *
     */
    private void processData(Map<String, Set<String>> inputStudySampleMap,
                             String geneList,
                             HashSet<String> geneticProfileIdSet,
                             String sampleSetId,
                             String sampleIdsKey,
                             String sampleIdsStr, //raw string from "user custom case ids" box
                             Integer dataTypePriority,
                             ServletContext servletContext,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             XDebug xdebug) throws IOException, ServletException, DaoException {

        String sampleSetName = "User-defined Patient List";
        String sampleSetDescription = "User defined patient list.";
        HashMap<String, GeneticProfile> geneticProfileMap = new HashMap<>();
        Map<String,List<String>> studySampleMap = new HashMap<>();
        Boolean showIGVtab = false;
        Boolean hasMrna = false;
        Boolean hasMethylation = false;
        Boolean hasCopyNo = false;
        Boolean hasSurvival = false;
        String decodedGeneList = URLDecoder.decode(geneList, "UTF-8");
        String singleStudyId = (String) request.getAttribute(CANCER_STUDY_ID);
        if (inputStudySampleMap.keySet().size() == 1 && inputStudySampleMap.containsKey(singleStudyId)) { // single study
            String _cancerStudyId = inputStudySampleMap.keySet().iterator().next();
            if (!sampleSetId.equals("-1")) {
                for (SampleList sampleSet : GetSampleLists.getSampleLists(_cancerStudyId)) {
                    if (sampleSet.getStableId().equals(sampleSetId)) {
                        sampleIdsStr = sampleSet.getSampleListAsString();
                        sampleSetName = sampleSet.getName();
                        sampleSetDescription = sampleSet.getDescription();
                        break;
                    }
                }
                List<String> samplesList = new ArrayList<>(Arrays.asList(sampleIdsStr.split(" ")));
                studySampleMap.put(_cancerStudyId,samplesList);
            } else if (sampleSetId.equals("-1") && sampleIdsKey != null) {
                sampleIdsStr = SampleSetUtil.getSampleIds(sampleIdsKey);
                List<String> samplesList = new ArrayList<>(Arrays.asList(sampleIdsStr.split(" ")));
                studySampleMap.put(_cancerStudyId,samplesList);
            } else if (sampleSetId.equals("-1") && sampleIdsKey == null &&
                sampleIdsStr != null && sampleIdsStr.length() > 0) {
                studySampleMap = parseCaseIdsTextBoxStr(sampleIdsStr);
            } else {
                redirectStudyUnavailable(request, response);
            }
        } else { // multiple studies OR single virtual study
            if (sampleSetId.equals("-1") && sampleIdsStr != null && sampleIdsStr.length() > 0) { //using user customized case list
                studySampleMap = parseCaseIdsTextBoxStr(sampleIdsStr);
            } else {
            	
            	    SampleSet sampleSet = SampleSet.get(sampleSetId);
                sampleSet = (sampleSet == null) ?  SampleSet.ALL : sampleSet;
                
                request.setAttribute(CASE_SET_ID, sampleSet.getSampleSet());
                
                final SampleListCategory sampleListCategory = sampleSet.getSampleListCategory();
                final Map<String,List<String>> studySampleMapConcurrent = new ConcurrentHashMap<>();

                inputStudySampleMap.keySet().parallelStream().forEach((String _cancerStudyId) -> {
                    try {
                        for(SampleList sampleList:GetSampleLists.getSampleLists(_cancerStudyId)) {
							if (sampleList.getSampleListCategory().equals(sampleListCategory)
									|| (sampleListCategory.equals(SampleListCategory.ALL_CASES_IN_STUDY)
											&& sampleList.getStableId().equals(_cancerStudyId + "_all"))) {
								List<String> sampleIds = sampleList.getSampleList();
								if (inputStudySampleMap.get(_cancerStudyId).size() > 0) {
									sampleIds.retainAll(inputStudySampleMap.get(_cancerStudyId));
								}
								studySampleMapConcurrent.put(_cancerStudyId, sampleIds);
								break;
							}
                        }
                    } catch (DaoException e) {
                        e.printStackTrace();
                        return;
                    }
                });
                studySampleMap = studySampleMapConcurrent;
            }
        }
        
        //always set sampleIdsKey when there is only one real study in query
        if (studySampleMap.keySet().size() == 1 && sampleIdsKey == null) {
            String sampleIds = studySampleMap
                                    .values()
                                    .stream()
                                    .flatMap(List::stream)
                                    .collect(Collectors.joining("\n"));
            sampleIdsKey = SampleSetUtil.shortenSampleIds(sampleIds);
        }


        // retrieve genetic profiles
        for (String cancerStudyId : inputStudySampleMap.keySet()) {
            CancerStudy selectedCancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
            ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId);
            showIGVtab = showIGVtab || selectedCancerStudy.hasCnaSegmentData();
            hasMrna = hasMrna || countProfiles(geneticProfileList, GeneticAlterationType.MRNA_EXPRESSION) > 0;
            hasMethylation = hasMethylation || countProfiles(geneticProfileList, GeneticAlterationType.METHYLATION) > 0;
            hasCopyNo = hasCopyNo || countProfiles(geneticProfileList, GeneticAlterationType.COPY_NUMBER_ALTERATION) > 0;
            hasSurvival = hasSurvival || selectedCancerStudy.hasSurvivalData();
            for(String profileId : geneticProfileIdSet){
                if (profileId != null && profileId.length() != 0)
                    geneticProfileMap.put(profileId, GeneticProfileUtil.getProfile(profileId, geneticProfileList));
            }
            if (inputStudySampleMap.size()>1 || !inputStudySampleMap.containsKey(singleStudyId)) {
                HashMap<String, GeneticProfile> defaultGeneticProfileSet = null;
                CategorizedGeneticProfileSet categorizedGeneticProfileSet = new CategorizedGeneticProfileSet(
                    geneticProfileList);
                if (dataTypePriority != null) {
                    // Get the default genomic profiles
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
                } else {
                    defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultMutationAndCopyNumberMap();
                }
                // geneticProfileMap.putAll(defaultGeneticProfileSet);
                for (String pId: defaultGeneticProfileSet.keySet()) {
                    geneticProfileMap.put(pId, defaultGeneticProfileSet.get(pId));
                }

            }
        }
        // this will create a key even if the patient set is a predefined set,
        // because it is required to build a patient id string in any case
        request.setAttribute(CASE_IDS_KEY, sampleIdsKey);
        request.setAttribute("case_set_name", sampleSetName);
        request.setAttribute("case_set_description", sampleSetDescription);
        request.setAttribute("showIGVtab", showIGVtab);
        request.setAttribute("hasMrna", hasMrna);
        request.setAttribute("hasMethylation", hasMethylation);
        request.setAttribute("hasCopyNo", hasCopyNo);
        request.setAttribute("hasSurvival", hasSurvival);

        ObjectMapper mapper = new ObjectMapper();
        String studySampleMapString = mapper.writeValueAsString(studySampleMap);
        request.setAttribute("STUDY_SAMPLE_MAP", studySampleMapString);

        // retrieve information about the cancer types
        List<String> samples = new ArrayList<>();
        for (List<String> samplesList : studySampleMap.values()) {
            samples.addAll(samplesList);
        }
        Boolean showCancerTypesSummary = false;
        Map<String, Set<String>> cancerTypesMap = DaoClinicalData.getCancerTypeInfoBySamples(samples);
        if(cancerTypesMap.keySet().size() > 1) {
            showCancerTypesSummary = true;
        }
        else if (cancerTypesMap.keySet().size() == 1 && cancerTypesMap.values().iterator().next().size() > 1 )  {
            showCancerTypesSummary = true;
        }
        request.setAttribute(HAS_CANCER_TYPES, showCancerTypesSummary);

        String tabIndex = request.getParameter(QueryBuilder.TAB_INDEX);
        if (tabIndex != null && tabIndex.equals(QueryBuilder.TAB_VISUALIZE)) {
            HashSet<String> geneticProfileIds = new HashSet<String>(geneticProfileMap.keySet());
            ArrayList<GeneticProfile> geneticProfiles = new ArrayList<>(geneticProfileMap.values());
            double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIds, geneticProfiles, request);
            double rppaScoreThreshold = ZScoreUtil.getRPPAScore(request);
            request.setAttribute(GENETIC_PROFILE_IDS, geneticProfileIds);
            request.setAttribute(Z_SCORE_THRESHOLD, zScoreThreshold);
            request.setAttribute(RPPA_SCORE_THRESHOLD, rppaScoreThreshold);

            // Store download links in session (for possible future retrieval).
            RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/visualize.jsp");
            dispatcher.forward(request, response);
        } else if (tabIndex != null && tabIndex.equals(QueryBuilder.TAB_DOWNLOAD)) {
            // include downloadable data in session
            ArrayList<DownloadLink> downloadLinkSet = new ArrayList<>();
            for(GeneticProfile profile : geneticProfileMap.values()){
                String _sampleIdsStr = StringUtils.join(studySampleMap.get(DaoCancerStudy.getCancerStudyByInternalId(profile.getCancerStudyId()).getCancerStudyStableId()), " ");
                if (_sampleIdsStr != null && _sampleIdsStr.length() != 0) {
                    GetProfileData remoteCall =
                        new GetProfileData(profile, new ArrayList<>(Arrays.asList(decodedGeneList.split("( )|(\\n)"))), _sampleIdsStr);
                    DownloadLink downloadLink = new DownloadLink(profile, new ArrayList<>(Arrays.asList(decodedGeneList.split("( )|(\\n)"))), sampleIdsStr,
                        remoteCall.getRawContent());
                    downloadLinkSet.add(downloadLink);
                }
            }
            request.getSession().setAttribute(DOWNLOAD_LINKS, downloadLinkSet);

            ShowData.showDataAtSpecifiedIndex(servletContext, request,
                response, 0, xdebug);
        }
    }

    private void redirectStudyUnavailable(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "The selected cancer study is currently being updated, please try back later.");
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp");
        dispatcher.forward(request, response);
    }

    private Map<String,List<String>> parseCaseIdsTextBoxStr(String _inputStr) {
        Map<String,List<String>> _resultMap = new HashMap<>();
        _inputStr = _inputStr.replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");
        _inputStr = _inputStr.replaceAll("\n", "+").replaceAll("\t", ":");
        String[] _segs = _inputStr.split("\\+"); // _seg => a line in "Case Ids" box: study_id:sampleId
        for (String _seg: _segs) {
            String _studyId = _seg.split(":")[0];
            String _sampleId = _seg.split(":")[1];
            if (_resultMap.containsKey(_studyId)) {
                List<String> _tmpSampleList = _resultMap.get(_studyId);
                if (!_tmpSampleList.contains(_sampleId)) {
                    _tmpSampleList.add(_sampleId);
                }
                _resultMap.put(_studyId, _tmpSampleList);
            } else {
                List<String> _tmpSampleList = new ArrayList<>(Arrays.asList(_sampleId));
                _resultMap.put(_studyId, _tmpSampleList);
            }
        }
        return _resultMap;
    }

    /**
     * validate the portal web input form.
     * @throws ProtocolException
     */
    private boolean validateForm(String action,
                                 Map<String, Set<String>> studySampleMap,
                                 HashSet<String> geneticProfileIdSet,
                                 String sampleSetId, String sampleIds,
                                 HttpServletRequest httpServletRequest) throws DaoException {
        boolean errorsExist = false;
        String tabIndex = httpServletRequest.getParameter(QueryBuilder.TAB_INDEX);
        if (action != null) {
            if (action.equals(ACTION_SUBMIT)) {

                if (studySampleMap.keySet().size() == 0) {
                    httpServletRequest.setAttribute(STEP1_ERROR_MSG,
                        //"You are not authorized to view the cancer study with id: '" +
                        //cohortDetails.getCohortId() + "'. ");
                        "You are not authorized to view the cancer study."); //TODO: how to specify the Id(s) here?
                    errorsExist = true;
                }
                else {
                    UserDetails ud = accessControl.getUserDetails();
                    if (ud != null) {
                        LOG.info("QueryBuilder.validateForm: Query initiated by user: " + ud.getUsername());
                    }
                }

                if(studySampleMap.keySet().size() == 1) {

                    if (geneticProfileIdSet.size() == 0) {
                        if (tabIndex == null || tabIndex.equals(QueryBuilder.TAB_DOWNLOAD)) {
                            httpServletRequest.setAttribute(STEP2_ERROR_MSG,
                                "Please select a genetic profile below. ");
                        } else {
                            httpServletRequest.setAttribute(STEP2_ERROR_MSG,
                                "Please select one or more genetic profiles below. ");
                        }
                        errorsExist = true;
                    }

                    // user-defined patient set
                    if (sampleIds != null &&
                        sampleSetId != null &&
                        sampleSetId.equals("-1"))
                    {
                        // empty patient list
                        if (sampleIds.trim().length() == 0)
                        {
                            httpServletRequest.setAttribute(STEP3_ERROR_MSG,
                                "Please enter at least one ID below. ");

                            errorsExist = true;
                        }
                        else
                        {
                            List<String> invalidSamples = SampleSetUtil
                                                            .validateSampleSet(
                                                            		studySampleMap.keySet().iterator().next(),
                                                            		sampleIds);

                            String sampleSetErrMsg = "Invalid samples(s) for the selected cancer study:";

                            // non-empty list, but contains invalid patient IDs
                            if (invalidSamples.size() > 0)
                            {
                                // append patient ids to the message
                                for (String sampleId : invalidSamples)
                                {
                                    sampleSetErrMsg += " " + sampleId;
                                }

                                httpServletRequest.setAttribute(STEP3_ERROR_MSG,
                                    sampleSetErrMsg);

                                errorsExist = true;
                            }
                        }
                    }


                }
            }
        }

        return errorsExist;
    }

    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response,
                                    String userMessage, XDebug xdebug)
        throws ServletException, IOException {
        request.setAttribute("xdebug_object", xdebug);
        request.setAttribute(USER_ERROR_MESSAGE, userMessage);
        RequestDispatcher dispatcher =
            getServletContext().getRequestDispatcher("/WEB-INF/jsp/error.jsp");
        dispatcher.forward(request, response);
    }

    private int countProfiles (ArrayList<GeneticProfile> profileList, GeneticAlterationType type) {
        int counter = 0;
        for (int i = 0; i < profileList.size(); i++) {
            GeneticProfile profile = profileList.get(i);
            if (profile.getGeneticAlterationType() == type) {
                counter++;
            }
        }
        return counter;
    }
    
    private Set<VirtualStudy> getStudies(Set<String> inputCohortIds) throws DaoException {
        Set<VirtualStudy> studies = new HashSet<>();
        SessionServiceUtil sessionServiceUtil = new SessionServiceUtil();
        for (String inputCohortId: inputCohortIds) {
            try {
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(inputCohortId);
                // is virtual study
                if (cancerStudy == null) {
                    VirtualStudy virtualStudy = sessionServiceUtil.getVirtualStudyData(inputCohortId);
                    if (virtualStudy != null && virtualStudy.getData().getStudies().size() > 0) {
                        studies.add(virtualStudy);
                    }
                    // is regular study
                } else {
                    VirtualStudy study = new VirtualStudy();
                    VirtualStudySamples studySamples = new VirtualStudySamples();
                    studySamples.setId(inputCohortId);
                    studySamples.setSamples(new HashSet<>());
                    VirtualStudyData studyData = new VirtualStudyData();
                    studyData.setStudies(Collections.singleton(studySamples));
                    study.setId(cancerStudy.getCancerStudyStableId());
                    study.setData(studyData);
                    studies.add(study);
                }
            } catch (DaoException e) {
                throw e;
            }
        }
        return studies;
    }
    
    private Set<String> mergeSets(Set<String> a, Set<String> b) {
        Set<String> resultSet = new HashSet<String>(a.size() + b.size());
        for (String i : a) { resultSet.add(i); }
        for (String i : b) { resultSet.add(i); }
        return resultSet;
    }
}

enum SampleSet {
	ALL("all",SampleListCategory.ALL_CASES_IN_STUDY),
    W_MUT_CNA("w_mut_cna",SampleListCategory.ALL_CASES_WITH_MUTATION_AND_CNA_DATA),
    W_MUT("w_mut",SampleListCategory.ALL_CASES_WITH_MUTATION_DATA),
    W_CNA("w_cna",SampleListCategory.ALL_CASES_WITH_CNA_DATA);
	
	private SampleListCategory sampleListCategory;
    
	private String sampleSet;

	SampleSet(String sampleSet, SampleListCategory sampleListCategory) {
		this.sampleSet = sampleSet;
		this.sampleListCategory = sampleListCategory;
	}

	public String getSampleSet() {
		return sampleSet;
	}
	
	public SampleListCategory getSampleListCategory() {
		return sampleListCategory;
	}
	
    public static SampleSet get(String sampleSet) {
        return Arrays.stream(values())
          .filter(obj -> obj.sampleSet.equalsIgnoreCase(sampleSet))
          .findFirst()
          .orElse(null);
    }
}

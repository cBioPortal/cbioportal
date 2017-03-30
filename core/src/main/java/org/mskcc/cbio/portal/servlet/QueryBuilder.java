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

import org.owasp.validator.html.PolicyException;

import org.springframework.security.core.userdetails.UserDetails;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.rmi.RemoteException;
import org.codehaus.jackson.map.ObjectMapper;


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

    public static final String CANCER_TYPES_MAP = "cancer_types_map"; 

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

        try {

            //  Get User Selected Action
            String action = httpServletRequest.getParameter(ACTION_NAME);

            //  Get User Selected Genetic Profiles
            HashSet<String> geneticProfileIdSet = getGeneticProfileIds(httpServletRequest, xdebug);

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

            // Check current if database version is up to date
            String dbPortalVersion = GlobalProperties.getDbVersion();
            String dbVersion = DaoInfo.getVersion();
            LOG.info("version - "+dbPortalVersion);
            LOG.info("version - "+dbVersion);
            if (!dbPortalVersion.equals(dbVersion)) {
                //extra message for the cases where property is missing (will happen often in transition period to this new versioning model):
                String extraMessage = "";
                if (dbPortalVersion.equals("0"))
                    extraMessage = "The db.version property also not found in your portal.properties file. " +
                        "This new property needs to be added by the administrator.";
                httpServletRequest.setAttribute(DB_ERROR, "Current DB Version: " + dbVersion + "<br/>" +
                    "DB version expected by Portal: " + dbPortalVersion + "<br/>" + extraMessage);
            }

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
            if (cancerStudyId != null || cancerStudyList != null) {
                // is single study
                System.out.println("cancerStudyId: " + cancerStudyId);
                System.out.println("cancerStudyList: " + cancerStudyList);
                if (!cancerStudyId.equals("all")) {
                    CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
                    // is virtual study
                    if (cancerStudy == null) _isVirtualStudy = true;
                    // is regular study
                    else _isVirtualStudy = false;
                    httpServletRequest.setAttribute(CANCER_STUDY_ID, cancerStudyId);
                    httpServletRequest.setAttribute(CANCER_STUDY_LIST, null);
                // multi studies
                } else {
                    _isVirtualStudy = true;
                    httpServletRequest.setAttribute(CANCER_STUDY_ID, "all");
                    httpServletRequest.setAttribute(CANCER_STUDY_LIST, cancerStudyList);
                }
                System.out.println("_isVirtualStudy: " + _isVirtualStudy);
                httpServletRequest.setAttribute(IS_VIRTUAL_STUDY, _isVirtualStudy);
                httpServletRequest.setAttribute(CASE_SET_ID, httpServletRequest.getParameter(CASE_SET_ID));
                httpServletRequest.setAttribute(CASE_IDS,
                    (httpServletRequest.getParameter(CASE_IDS)).replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t"));
            }
            
            // Dispatch to query result page
            if (action != null && action.equals(ACTION_SUBMIT) && (!errorsExist)) {
                CohortDetails cohortDetails;
                if (httpServletRequest.getParameter(CANCER_STUDY_ID).equals("all") &&
                    httpServletRequest.getParameter(CANCER_STUDY_LIST) != null) {
                    cohortDetails = new CohortDetails(
                        httpServletRequest.getParameter(CANCER_STUDY_LIST).split(","),
                        _isVirtualStudy
                    );
                } else {
                    String[] _arr = new String[1];
                    _arr[0] = httpServletRequest.getParameter(CANCER_STUDY_ID);
                    cohortDetails = new CohortDetails(_arr, _isVirtualStudy);
                }
                processData(cohortDetails, geneList, geneticProfileIdSet, 
                    httpServletRequest.getParameter(CASE_SET_ID),
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
            
        } catch (RemoteException e) {
            xdebug.logMsg(this, "Got Remote Exception:  " + e.getMessage());
            forwardToErrorPage(httpServletRequest, httpServletResponse,
                               DB_CONNECT_ERROR, xdebug);
        } catch (DaoException e) {
            xdebug.logMsg(this, "Got Database Exception:  " + e.getMessage());
            forwardToErrorPage(httpServletRequest, httpServletResponse,
                               DB_CONNECT_ERROR, xdebug);
        }
        
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
    private void processData(CohortDetails cohortDetails,
                             String geneList,
							 HashSet<String> geneticProfileIdSet,
							 String sampleSetId, String sampleIds,
							 Integer dataTypePriority,
							 ServletContext servletContext, 
                             HttpServletRequest request,
							 HttpServletResponse response,
							 XDebug xdebug) throws IOException, ServletException, DaoException {
        
        String sampleIdsKey = null;
        String sampleSetName = "User-defined Patient List";
        String sampleSetDescription = "User defined patient list.";
        HashMap<String, GeneticProfile> geneticProfileMap = new HashMap<>();
        Map<String,List<String>> studySampleMap = new HashMap<>();
        Boolean showIGVtab = false;
        Boolean hasMrna = false;
        Boolean hasMethylation = false;
        Boolean hasCopyNo = false;
        Boolean hasSurvival = false;
        
        // user-specified patients, but patient_ids parameter is missing,
        // so try to retrieve sample_ids by using sample_ids_key parameter.
        // this is required for survival plot requests  
        Map<String, List<String>> cancerTypeInfo = new HashMap<>();
        Map<String, Set<String>> inputStudySampleMap = cohortDetails.getStudySampleMap();
		for (String cancerStudyId : inputStudySampleMap.keySet()) {
			CancerStudy selectedCancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
			ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId);
			ArrayList<SampleList> sampleSetList = GetSampleLists.getSampleLists(cancerStudyId);
			showIGVtab = showIGVtab || selectedCancerStudy.hasCnaSegmentData();
			hasMrna = hasMrna || countProfiles(geneticProfileList, GeneticAlterationType.MRNA_EXPRESSION) > 0;
			hasMethylation = hasMethylation || countProfiles(geneticProfileList, GeneticAlterationType.METHYLATION) > 0;
		    hasCopyNo = hasCopyNo || countProfiles(geneticProfileList, GeneticAlterationType.COPY_NUMBER_ALTERATION) > 0;
		    hasSurvival = hasSurvival || selectedCancerStudy.hasSurvivalData();
            
            // single regular study
			if (!cohortDetails.getIsVirtualStudy()) {
				if (sampleSetId.equals("-1") && sampleIds == null) { 
					sampleIdsKey = request.getParameter(CASE_IDS_KEY);
					if (sampleIdsKey != null) {
						sampleIds = SampleSetUtil.getSampleIds(sampleIdsKey);
					}
				}
				if (!sampleSetId.equals("-1")) {
					for (SampleList sampleSet : GetSampleLists.getSampleLists(cancerStudyId)) {
						if (sampleSet.getStableId().equals(sampleSetId)) {
							sampleIds = sampleSet.getSampleListAsString();
							sampleSetName = sampleSet.getName();
							sampleSetDescription = sampleSet.getDescription();
							break;
						}
					}
				}
				// if user specifies patients, add these to hashset, and send to
				// GetMutationData
				else if (sampleIds != null && sampleIds.length()>0) {
					sampleIds = sampleIds.replaceAll("\\s+", " ");
				}
				else {
					redirectStudyUnavailable(request, response);
				}
				for(String profileId : geneticProfileIdSet){
					geneticProfileMap.put(profileId, GeneticProfileUtil.getProfile(profileId, geneticProfileList));
				}
				
				List<String> samplesList = new ArrayList<>(Arrays.asList(sampleIds.split(" ")));
				studySampleMap.put(cancerStudyId,samplesList);
				
				if (sampleIdsKey == null)
		        {
		            sampleIdsKey = SampleSetUtil.shortenSampleIds(sampleIds);
		        }

		        // retrieve information about the cancer types
		       cancerTypeInfo = DaoClinicalData.getCancerTypeInfo(selectedCancerStudy.getInternalId());
            
            // multiple studies OR single virtual study
            } else { 
				if (dataTypePriority != null) {
					AnnotatedSampleSets annotatedSampleSets = new AnnotatedSampleSets(sampleSetList, dataTypePriority);
					SampleList defaultSampleSet = annotatedSampleSets.getDefaultSampleList();
					if (defaultSampleSet == null) {
						continue;
					}
					List<String> sampleList = defaultSampleSet.getSampleList();
					if(inputStudySampleMap.get(cancerStudyId).size()>0){
						sampleList.retainAll(inputStudySampleMap.get(cancerStudyId));
					}
					studySampleMap.put(cancerStudyId, sampleList);
					// Get the default genomic profiles
					CategorizedGeneticProfileSet categorizedGeneticProfileSet = new CategorizedGeneticProfileSet(
							geneticProfileList);
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
					geneticProfileMap.putAll(defaultGeneticProfileSet);
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
        
        ArrayList<DownloadLink> downloadLinkSet = new ArrayList<>();
        for(GeneticProfile profile : geneticProfileMap.values()){
        	GetProfileData remoteCall =
                    new GetProfileData(profile, new ArrayList<>(Arrays.asList(geneList.split("( )|(\\n)"))), StringUtils.join(studySampleMap.get(DaoCancerStudy.getCancerStudyByInternalId(profile.getCancerStudyId()).getCancerStudyStableId()), " "));
                DownloadLink downloadLink = new DownloadLink(profile, new ArrayList<>(Arrays.asList(geneList.split("( )|(\\n)"))), sampleIds,
                    remoteCall.getRawContent());
                downloadLinkSet.add(downloadLink);
        }
        
        request.setAttribute(CANCER_TYPES_MAP, cancerTypeInfo);

        request.getSession().setAttribute(DOWNLOAD_LINKS, downloadLinkSet);
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

    /**
     * validate the portal web input form.
     * @throws ProtocolException 
     */
    private boolean validateForm(String action, CohortDetails cohortDetails,
                                 HashSet<String> geneticProfileIdSet,
                                 String sampleSetId, String sampleIds,
                                 HttpServletRequest httpServletRequest) throws DaoException, ProtocolException {
        boolean errorsExist = false;
        String tabIndex = httpServletRequest.getParameter(QueryBuilder.TAB_INDEX);
        if (action != null) {
            if (action.equals(ACTION_SUBMIT)) {

            	if (cohortDetails.getStudySampleMap().keySet().size() == 0) {
                    httpServletRequest.setAttribute(STEP1_ERROR_MSG,
													//"You are not authorized to view the cancer study with id: '" +
													//		cohortDetails.getCohortId() + "'. ");
                                                    "You are not authorized to view the cancer study."); //TODO: how to specify the Id(s) here?
					errorsExist = true;
				}
                else {
                    UserDetails ud = accessControl.getUserDetails();
                    if (ud != null) {
                        LOG.info("QueryBuilder.validateForm: Query initiated by user: " + ud.getUsername());
                    }
                }
	            
	            if(!cohortDetails.getIsVirtualStudy()) {
						
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
                		List<String> invalidSamples = SampleSetUtil.validateSampleSet(
                				cohortDetails.getStudySampleMap().keySet().iterator().next(), sampleIds);
                		
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
}

class CohortDetails {
    
	private Map<String, Set<String>> studySampleMap = new HashMap<>(); // <cancer study Id: set of samples>
	private String cohortId;
	private Boolean isVirtualStudy = false;

	public CohortDetails(String[] inputCohortIds, Boolean _isVirtualStudy) {
        isVirtualStudy = _isVirtualStudy;
		studySampleMap = filterStudySampleMap(getInputStudySampleMap(inputCohortIds));
	}

	private Map<String, Set<String>> getInputStudySampleMap(String[] inputCohortIds) {
		Map<String, Set<String>> studySampleMap = new HashMap<>();
        SessionServiceUtil sessionServiceUtil = new SessionServiceUtil();
        for (String inputCohortId: inputCohortIds) {
            try {
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(inputCohortId);
                // is virtual study
                if (cancerStudy == null) { 
                    Cohort virtualCohort = sessionServiceUtil.getVirtualCohortData(inputCohortId);
                    if (virtualCohort != null && virtualCohort.getCohortStudyCasesMap().size() > 0) {
                        for (CohortStudyCasesMap cohortStudyCasesMap : virtualCohort.getCohortStudyCasesMap()) {
                            if (studySampleMap.containsKey(cohortStudyCasesMap.getStudyID())) {
                                String _sharedStudyId = cohortStudyCasesMap.getStudyID();
                                if (studySampleMap.get(_sharedStudyId).size() != 0) { // original entry does not contain all samples
                                    Set<String> mergedSet = mergeSets(studySampleMap.get(_sharedStudyId), cohortStudyCasesMap.getSamples());
                                    studySampleMap.put(cohortStudyCasesMap.getStudyID(), mergedSet);                                        
                                } // otherwise keep the value as empty set
                            //create new entry/key
                            } else { 
                                studySampleMap.put(cohortStudyCasesMap.getStudyID(), cohortStudyCasesMap.getSamples());
                            }
                        }
                    }
                // is regular study
                } else { 
                    studySampleMap.put(cancerStudy.getCancerStudyStableId(), new HashSet<String>());
                }
            } catch (DaoException e) {
                e.printStackTrace();
            }
        }
		return studySampleMap;
	}

    private static Set<String> mergeSets(Set<String> a, Set<String> b) {
        Set<String> resultSet = new HashSet<String>(a.size() + b.size());
        for (String i : a) { resultSet.add(i); }
        for (String i : b) { resultSet.add(i); }
        return resultSet;
    }

	private Map<String, Set<String>> filterStudySampleMap(Map<String, Set<String>> studySampleMap) {
		Map<String, Set<String>> resultMap = new HashMap<>();
		for (String studyId : studySampleMap.keySet()) {
			try {
				if (SpringUtil.getAccessControl().isAccessibleCancerStudy(studyId).size() > 0) {
					resultMap.put(studyId, studySampleMap.get(studyId));
				}
			} catch (DaoException e) {
				return new HashMap<>();
			}

		}
		return resultMap;
	}

	public Map<String, Set<String>> getStudySampleMap() {
		return studySampleMap;
	}

	public void setStudySampleMap(Map<String, Set<String>> studySampleMap) {
		this.studySampleMap = studySampleMap;
	}

	public String getCohortId() {
		return cohortId;
	}

	public void setCohortId(String cohortId) {
		this.cohortId = cohortId;
	}

	public Boolean getIsVirtualStudy() {
		return isVirtualStudy;
	}
    
    public void setIsVirtualStudy(Boolean _is) { 
        this.isVirtualStudy = _is; 
    }

}

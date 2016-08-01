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
import org.mskcc.cbio.portal.util.AccessControl;

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


/**
 * Central Servlet for building queries.
 */
public class QueryBuilder extends HttpServlet {
    public static final String CLIENT_TRANSPOSE_MATRIX = "transpose_matrix";
    public static final String CANCER_TYPES_INTERNAL = "cancer_types";
    public static final String PROFILE_LIST_INTERNAL = "profile_list";
    public static final String CASE_SETS_INTERNAL = "case_sets";
    public static final String CANCER_STUDY_ID = "cancer_study_id";
    public static final String PATIENT_CASE_SELECT = "patient_case_select";
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
    private static final String DB_CONNECT_ERROR = ("An error occurred while trying to connect to the database." +
                                                    "  This could happen if the database does not contain any cancer studies.");

    private static Log LOG = LogFactory.getLog(QueryBuilder.class);

    public static final String CANCER_TYPES_MAP = "cancer_types_map"; 

    private ServletXssUtil servletXssUtil;

	// class which process access control to cancer studies
	private AccessControl accessControl;

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

        //  Get User Selected Action
        String action = httpServletRequest.getParameter(ACTION_NAME);
        
        String patientCaseSelect = httpServletRequest.getParameter(PATIENT_CASE_SELECT);

        //  Get User Selected Cancer Type
        String cancerTypeId = httpServletRequest.getParameter(CANCER_STUDY_ID);

        //  Get User Selected Genetic Profiles
        HashSet<String> geneticProfileIdSet = getGeneticProfileIds(httpServletRequest, xdebug);

        //  Get User Defined Gene List
	    String geneList = httpServletRequest.getParameter(GENE_LIST);
	    if (httpServletRequest instanceof XssRequestWrapper) {
		    geneList = ((XssRequestWrapper)httpServletRequest).getRawParameter(GENE_LIST);
	    }
        geneList = servletXssUtil.getCleanInput(geneList);
        httpServletRequest.setAttribute(GENE_LIST, geneList);

        //  Get all Cancer Types
        try {
			List<CancerStudy> cancerStudyList = accessControl.getCancerStudies();

            if (cancerTypeId == null) {
                cancerTypeId = cancerStudyList.get(0).getCancerStudyStableId();
            }
            
            httpServletRequest.setAttribute(CANCER_STUDY_ID, cancerTypeId);
            httpServletRequest.setAttribute(CANCER_TYPES_INTERNAL, cancerStudyList);

            //  Get Genetic Profiles for Selected Cancer Type
            ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles
                (cancerTypeId);
            httpServletRequest.setAttribute(PROFILE_LIST_INTERNAL, profileList);

            //  Get Patient Sets for Selected Cancer Type
            xdebug.logMsg(this, "Using Cancer Study ID:  " + cancerTypeId);
            ArrayList<SampleList> sampleSets = GetSampleLists.getSampleLists(cancerTypeId);
            xdebug.logMsg(this, "Total Number of Patient Sets:  " + sampleSets.size());
            SampleList sampleSet = new SampleList();
            sampleSet.setName("User-defined Patient List");
            sampleSet.setDescription("User defined patient list.");
            sampleSet.setStableId("-1");
            sampleSets.add(sampleSet);
            httpServletRequest.setAttribute(CASE_SETS_INTERNAL, sampleSets);

            //  Get User Selected Patient Set
            String sampleSetId = httpServletRequest.getParameter(CASE_SET_ID);
            if (sampleSetId != null) {
                httpServletRequest.setAttribute(CASE_SET_ID, sampleSetId);
            } else {
                if (sampleSets.size() > 0) {
                    SampleList zeroSet = sampleSets.get(0);
                    httpServletRequest.setAttribute(CASE_SET_ID, zeroSet.getStableId());
                }
            }
            String sampleIds = httpServletRequest.getParameter(CASE_IDS);
	        // TODO allowing only new line and tab chars, getRawParameter may be vulnerable here...
	        if (sampleIds != null)
	        {
		        sampleIds = sampleIds.replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");
	        }
         
            httpServletRequest.setAttribute(XDEBUG_OBJECT, xdebug);
            
            String dbPortalVersion = GlobalProperties.getDbVersion();
            String dbVersion = DaoInfo.getVersion();
            LOG.info("version - "+dbPortalVersion);
            LOG.info("version - "+dbVersion);
            if (!dbPortalVersion.equals(dbVersion))
            {
            	String extraMessage = "";
            	//extra message for the cases where property is missing (will happen often in transition period to this new versioning model):
            	if (dbPortalVersion.equals("0"))
            		extraMessage = "The db.version property also not found in your portal.properties file. This new property needs to be added by the administrator.";
                httpServletRequest.setAttribute(DB_ERROR, "Current DB Version: " + dbVersion + "<br/>" + "DB version expected by Portal: " + dbPortalVersion + "<br/>" + extraMessage);
            }

            // Get the example study queries configured as a skin property
            String[] exampleStudyQueries = GlobalProperties.getExampleStudyQueries().split("\n");
            httpServletRequest.setAttribute(
                    "exampleStudyQueries",
                    exampleStudyQueries);

            boolean errorsExist = validateForm(action, profileList, geneticProfileIdSet,
                                               sampleSetId, sampleIds, httpServletRequest);
            if (action != null && action.equals(ACTION_SUBMIT) && (!errorsExist)) {

                processData(cancerTypeId, geneList, geneticProfileIdSet, profileList, sampleSetId,
                            sampleIds, sampleSets, patientCaseSelect, getServletContext(), httpServletRequest,
                            httpServletResponse, xdebug);
            } else {
                if (errorsExist) {
                   httpServletRequest.setAttribute(QueryBuilder.USER_ERROR_MESSAGE,
                           "Please fix the errors below.");
                }
                RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp");
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
        } catch (ProtocolException e) {
            xdebug.logMsg(this, "Got Protocol Exception:  " + e.getMessage());
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
    private void processData(String cancerStudyStableId,
                             String geneList,
							 HashSet<String> geneticProfileIdSet,
							 ArrayList<GeneticProfile> profileList,
							 String sampleSetId, String sampleIds,
							 ArrayList<SampleList> sampleSetList,
                                                         String patientCaseSelect,
							 ServletContext servletContext, HttpServletRequest request,
							 HttpServletResponse response,
							 XDebug xdebug) throws IOException, ServletException, DaoException {
        
        request.setAttribute(PATIENT_CASE_SELECT, patientCaseSelect);
        
        HashSet<String> setOfSampleIds = null;
        
        String sampleIdsKey = null;

        // user-specified patients, but patient_ids parameter is missing,
        // so try to retrieve sample_ids by using sample_ids_key parameter.
        // this is required for survival plot requests  
        if (sampleSetId.equals("-1") &&
        	sampleIds == null)
        {
        	sampleIdsKey = request.getParameter(CASE_IDS_KEY);
        	
        	if (sampleIdsKey != null)
        	{
        		sampleIds = SampleSetUtil.getSampleIds(sampleIdsKey);
        	}
        }
        
        if (!sampleSetId.equals("-1"))
        {
            for (SampleList sampleSet : sampleSetList) {
                if (sampleSet.getStableId().equals(sampleSetId)) {
                    sampleIds = sampleSet.getSampleListAsString();
                    setOfSampleIds = new HashSet<String>(sampleSet.getSampleList());
                    break;
                }
            }
        }
        //if user specifies patients, add these to hashset, and send to GetMutationData
        else if (sampleIds != null)
        {           
            String[] sampleIdSplit = sampleIds.split("\\s+");
            setOfSampleIds = new HashSet<String>();
            
            for (String sampleID : sampleIdSplit){
                if (null != sampleID){
                   setOfSampleIds.add(sampleID);
                }
            }
            
            sampleIds = sampleIds.replaceAll("\\s+", " ");
        }
        
		if (setOfSampleIds == null || setOfSampleIds.isEmpty()) {
			redirectStudyUnavailable(request, response);
		}
                
        request.setAttribute(SET_OF_CASE_IDS, sampleIds);
        
        // Map user selected samples Ids to patient Ids
        HashMap<String, String> patientSampleIdMap = new HashMap<String, String>();
        CancerStudy selectedCancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
        int cancerStudyInternalId = selectedCancerStudy.getInternalId();
        Iterator<String> itr = setOfSampleIds.iterator();
        while(itr.hasNext()){
            String sampleId = itr.next();
            ArrayList<String> sampleIdList = new ArrayList<String>();
            sampleIdList.add(sampleId);
            
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyInternalId, sampleId);
                Patient patient = DaoPatient.getPatientById(sample.getInternalPatientId());
                patientSampleIdMap.put(sampleId, patient.getStableId());
            }
        request.setAttribute(SELECTED_PATIENT_SAMPLE_ID_MAP, patientSampleIdMap);
         
        if (sampleIdsKey == null)
        {
            sampleIdsKey = SampleSetUtil.shortenSampleIds(sampleIds);
        }

        // retrieve information about the cancer types
        Map<String, List<String>> cancerTypeInfo = DaoClinicalData.getCancerTypeInfo(cancerStudyInternalId);
        request.setAttribute(CANCER_TYPES_MAP, cancerTypeInfo);

        // this will create a key even if the patient set is a predefined set,
        // because it is required to build a patient id string in any case
        request.setAttribute(CASE_IDS_KEY, sampleIdsKey);

        Iterator<String> profileIterator = geneticProfileIdSet.iterator();
        ArrayList<DownloadLink> downloadLinkSet = new ArrayList<>();
        while (profileIterator.hasNext()) {
            String profileId = profileIterator.next();
            GeneticProfile profile = GeneticProfileUtil.getProfile(profileId, profileList);
            if( null == profile ){
                continue;
            }
            GetProfileData remoteCall =
                new GetProfileData(profile, new ArrayList<>(Arrays.asList(geneList.split("( )|(\\n)"))), StringUtils.join(setOfSampleIds, " "));
            DownloadLink downloadLink = new DownloadLink(profile, new ArrayList<>(Arrays.asList(geneList.split("( )|(\\n)"))), sampleIds,
                remoteCall.getRawContent());
            downloadLinkSet.add(downloadLink);
        }

        request.getSession().setAttribute(DOWNLOAD_LINKS, downloadLinkSet);
        String tabIndex = request.getParameter(QueryBuilder.TAB_INDEX);
        if (tabIndex != null && tabIndex.equals(QueryBuilder.TAB_VISUALIZE)) {
            double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
            double rppaScoreThreshold = ZScoreUtil.getRPPAScore(request);
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
     */
    private boolean validateForm(String action,
                                ArrayList<GeneticProfile> profileList,
                                 HashSet<String> geneticProfileIdSet,
                                 String sampleSetId, String sampleIds,
                                 HttpServletRequest httpServletRequest) throws DaoException {
        boolean errorsExist = false;
        String tabIndex = httpServletRequest.getParameter(QueryBuilder.TAB_INDEX);
        if (action != null) {
            if (action.equals(ACTION_SUBMIT)) {
				// is user authorized for the study
				String cancerStudyIdentifier = (String)httpServletRequest.getAttribute(CANCER_STUDY_ID);
	            cancerStudyIdentifier = StringEscapeUtils.escapeJavaScript(cancerStudyIdentifier);

	            if (accessControl.isAccessibleCancerStudy(cancerStudyIdentifier).size() != 1) {
                    httpServletRequest.setAttribute(STEP1_ERROR_MSG,
													"You are not authorized to view the cancer study with id: '" +
													cancerStudyIdentifier + "'. ");
					errorsExist = true;
				}
                else {
                    UserDetails ud = accessControl.getUserDetails();
                    if (ud != null) {
                        LOG.info("QueryBuilder.validateForm: Query initiated by user: " + ud.getUsername());
                    }
                }
						
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
                				cancerStudyIdentifier, sampleIds);
                		
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

                //  Additional validation rules
                //  If we have selected mRNA Expression Data Check Box, but failed to
                //  select an mRNA profile, this is an error.
                String mRNAProfileSelected = httpServletRequest.getParameter(
                        QueryBuilder.MRNA_PROFILES_SELECTED);
                if (mRNAProfileSelected != null && mRNAProfileSelected.equalsIgnoreCase("on")) {

                    //  Make sure that at least one of the mRNA profiles is selected
                    boolean mRNAProfileRadioSelected = false;
                    for (int i = 0; i < profileList.size(); i++) {
                        GeneticProfile geneticProfile = profileList.get(i);
                        if (geneticProfile.getGeneticAlterationType()
                                == GeneticAlterationType.MRNA_EXPRESSION
                                && geneticProfileIdSet.contains(geneticProfile.getStableId())) {
                            mRNAProfileRadioSelected = true;
                        }
                    }
                    if (mRNAProfileRadioSelected == false) {
                        httpServletRequest.setAttribute(STEP2_ERROR_MSG,
                                "Please select an mRNA profile.");
                        errorsExist = true;
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
}

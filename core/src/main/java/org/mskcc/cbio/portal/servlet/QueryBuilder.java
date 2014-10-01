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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.web_api.*;
import org.mskcc.cbio.portal.validate.gene.*;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.*;

import org.apache.commons.lang.*;

import org.owasp.validator.html.PolicyException;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.rmi.RemoteException;

// import org.codehaus.jackson.node.*;
// import org.codehaus.jackson.JsonNode;
// import org.codehaus.jackson.map.ObjectMapper;

/**
 * Central Servlet for building queries.
 */
public class QueryBuilder extends HttpServlet {
    public static final String CLIENT_TRANSPOSE_MATRIX = "transpose_matrix";
    public static final String CANCER_TYPES_INTERNAL = "cancer_types";
    public static final String PROFILE_LIST_INTERNAL = "profile_list";
    public static final String CASE_SETS_INTERNAL = "case_sets";
    public static final String CANCER_STUDY_ID = "cancer_study_id";
    public static final String HAS_SURVIVAL_DATA = "has_survival_data";
    public static final String GENETIC_PROFILE_IDS = "genetic_profile_ids";
    public static final String GENE_SET_CHOICE = "gene_set_choice";
    public static final String CASE_SET_ID = "case_set_id";
    public static final String CASE_IDS = "case_ids";
    public static final String CASE_IDS_KEY = "case_ids_key";
    public static final String SET_OF_CASE_IDS = "set_of_case_ids";
    public static final String CLINICAL_PARAM_SELECTION = "clinical_param_selection";
    public static final String GENE_LIST = "gene_list";
    public static final String RAW_GENE_STR = "raw_gene_str";
    public static final String ACTION_NAME = "Action";
    public static final String OUTPUT = "output";
    public static final String FORMAT = "format";
    public static final String PLOT_TYPE = "plot_type";
    public static final String OS_SURVIVAL_PLOT = "os_survival_plot";
    public static final String DFS_SURVIVAL_PLOT = "dfs_survival_plot";
    public static final String XDEBUG = "xdebug";
    public static final String ACTION_SUBMIT = "Submit";
    public static final String STEP1_ERROR_MSG = "step1_error_msg";
    public static final String STEP2_ERROR_MSG = "step2_error_msg";
    public static final String STEP3_ERROR_MSG = "step3_error_msg";
    public static final String STEP4_ERROR_MSG = "step4_error_msg";
    public static final String MERGED_PROFILE_DATA_INTERNAL = "merged_profile_data";
    public static final String PROFILE_DATA_SUMMARY = "profile_data_summary";
    public static final String WARNING_UNION = "warning_union";
    public static final String DOWNLOAD_LINKS = "download_links";
    public static final String NETWORK = "network";
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
    public static final String INTERNAL_EXTENDED_MUTATION_LIST = "INTERNAL_EXTENDED_MUTATION_LIST";
    public static final String DATA_PRIORITY = "data_priority";
    public static final String SELECTED_PATIENT_SAMPLE_ID_MAP = "selected_patient_sample_id_map";
    private static final String DB_CONNECT_ERROR = ("An error occurred while trying to connect to the database." +
                                                    "  This could happen if the database does not contain any cancer studies.");
    

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
			ApplicationContext context = 
				new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
			accessControl = (AccessControl)context.getBean("accessControl");
        } catch (PolicyException e) {
            throw new ServletException (e);
        }
    }

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
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
     * @param httpServletResponse Http Servelt Response Object.
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

        //  Get User Selected Cancer Type
        String cancerTypeId = httpServletRequest.getParameter(CANCER_STUDY_ID);

        //  Get User Selected Genetic Profiles
        HashSet<String> geneticProfileIdSet = getGeneticProfileIds(httpServletRequest, xdebug);

        //  Get User Defined Gene List
	    // we need the raw gene list...
	    String geneList = httpServletRequest.getParameter(GENE_LIST);

	    if (httpServletRequest instanceof XssRequestWrapper)
	    {
		    geneList = ((XssRequestWrapper)httpServletRequest).getRawParameter(GENE_LIST);
	    }

        geneList = servletXssUtil.getCleanInput(geneList);

        // save the raw gene string as it was entered for other things to work on
        httpServletRequest.setAttribute(RAW_GENE_STR, geneList);

        xdebug.logMsg(this, "Gene List is set to:  " + geneList);

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
            ArrayList<PatientList> patientSets = GetPatientLists.getPatientLists(cancerTypeId);
            xdebug.logMsg(this, "Total Number of Patient Sets:  " + patientSets.size());
            PatientList patientSet = new PatientList();
            patientSet.setName("User-defined Patient List");
            patientSet.setDescription("User defined patient list.");
            patientSet.setStableId("-1");
            patientSets.add(patientSet);
            httpServletRequest.setAttribute(CASE_SETS_INTERNAL, patientSets);

            //  Get User Selected Patient Set
            String patientSetId = httpServletRequest.getParameter(CASE_SET_ID);
            if (patientSetId != null) {
                httpServletRequest.setAttribute(CASE_SET_ID, patientSetId);
            } else {
                if (patientSets.size() > 0) {
                    PatientList zeroSet = patientSets.get(0);
                    httpServletRequest.setAttribute(CASE_SET_ID, zeroSet.getStableId());
                }
            }
            String patientIds = httpServletRequest.getParameter(CASE_IDS);
	        // TODO allowing only new line and tab chars, getRawParameter may be vulnerable here...
	        if (patientIds != null)
	        {
		        patientIds = patientIds.replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");
	        }

            httpServletRequest.setAttribute(XDEBUG_OBJECT, xdebug);

            boolean errorsExist = validateForm(action, profileList, geneticProfileIdSet, geneList,
                                               patientSetId, patientIds, httpServletRequest);
            if (action != null && action.equals(ACTION_SUBMIT) && (!errorsExist)) {

                processData(cancerTypeId, geneticProfileIdSet, profileList, geneList, patientSetId,
                            patientIds, patientSets, getServletContext(), httpServletRequest,
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
    private void processData(String cancerTypeId,
							 HashSet<String> geneticProfileIdSet,
							 ArrayList<GeneticProfile> profileList,
							 String geneListStr,
							 String patientSetId, String patientIds,
							 ArrayList<PatientList> patientSetList,
							 ServletContext servletContext, HttpServletRequest request,
							 HttpServletResponse response,
							 XDebug xdebug) throws IOException, ServletException, DaoException {

       // parse geneList, written in the OncoPrintSpec language (except for changes by XSS clean)
       double zScore = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
       double rppaScore = ZScoreUtil.getRPPAScore(request);
       
       ParserOutput theOncoPrintSpecParserOutput =
               OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver( geneListStr,
                geneticProfileIdSet, profileList, zScore, rppaScore );
       
        ArrayList<String> geneList = new ArrayList<String>();
        geneList.addAll( theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes());
        ArrayList<String> tempGeneList = new ArrayList<String>();
        for (String gene : geneList){
            tempGeneList.add(gene);
        }
        geneList = tempGeneList;
        request.setAttribute(GENE_LIST, geneList);

        xdebug.logMsg(this, "Using gene list geneList.toString():  " + geneList.toString());
        
        HashSet<String> setOfPatientIds = null;
        
        String patientIdsKey = null;
        
        // user-specified patients, but patient_ids parameter is missing,
        // so try to retrieve patient_ids by using patient_ids_key parameter.
        // this is required for survival plot requests  
        if (patientSetId.equals("-1") &&
        	patientIds == null)
        {
        	patientIdsKey = request.getParameter(CASE_IDS_KEY);
        	
        	if (patientIdsKey != null)
        	{
        		patientIds = PatientSetUtil.getPatientIds(patientIdsKey);
        	}
        }
        
        if (!patientSetId.equals("-1"))
        {
            for (PatientList patientSet : patientSetList) {
                if (patientSet.getStableId().equals(patientSetId)) {
                    patientIds = patientSet.getPatientListAsString();
                    setOfPatientIds = new HashSet<String>(patientSet.getPatientList());
                }
            }
        }
        //if user specifies patients, add these to hashset, and send to GetMutationData
        else if (patientIds != null)
        {
            String[] patientIdSplit = patientIds.split("\\s+");
            setOfPatientIds = new HashSet<String>();
            
            for (String patientID : patientIdSplit){
                if (null != patientID){
                   setOfPatientIds.add(patientID);
                }
            }
        }

        patientIds = patientIds.replaceAll("\\s+", " ");
        request.setAttribute(SET_OF_CASE_IDS, patientIds);
        
        // Map user selected samples Ids to patient Ids
        HashMap<String, String> patientSampleIdMap = new HashMap<String, String>();
        CancerStudy selectedCancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerTypeId);
        Integer cancerStudyInternalId = selectedCancerStudy.getInternalId();
        Iterator<String> itr = setOfPatientIds.iterator();
        while(itr.hasNext()){
            String tmpPatientId = itr.next();
            ArrayList<String> tmpPatientIdList = new ArrayList<String>();
            tmpPatientIdList.add(tmpPatientId);
            List<String> tmpSampleIdsArr =
              StableIdUtil.getStableSampleIdsFromPatientIds(cancerStudyInternalId,
                                                            tmpPatientIdList);  
            for (String tmpSampleId : tmpSampleIdsArr) {
                patientSampleIdMap.put(tmpSampleId, tmpPatientId);
            }    
        }
        request.setAttribute(SELECTED_PATIENT_SAMPLE_ID_MAP, patientSampleIdMap);

        if (patientIdsKey == null)
        {
            patientIdsKey = PatientSetUtil.shortenPatientIds(patientIds);
        }
        
        // this will create a key even if the patient set is a predefined set,
        // because it is required to build a patient id string in any case
        request.setAttribute(CASE_IDS_KEY, patientIdsKey);

        Iterator<String> profileIterator = geneticProfileIdSet.iterator();
        ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();

        Set<String> warningUnion = new HashSet<String>();
        ArrayList<DownloadLink> downloadLinkSet = new ArrayList<DownloadLink>();
        ArrayList<ExtendedMutation> mutationList = new ArrayList<ExtendedMutation>();

        while (profileIterator.hasNext()) {
            String profileId = profileIterator.next();
            GeneticProfile profile = GeneticProfileUtil.getProfile(profileId, profileList);
            if( null == profile ){
               continue;
            } 
            
            List<String> stableSampleIds =
              StableIdUtil.getStableSampleIdsFromPatientIds(profile.getCancerStudyId(),
                                                            new ArrayList(setOfPatientIds));
         
            xdebug.logMsg(this, "Getting data for:  " + profile.getProfileName());
            GetProfileData remoteCall =
              new GetProfileData(profile, geneList, StringUtils.join(stableSampleIds, " "));
            ProfileData pData = remoteCall.getProfileData();
            DownloadLink downloadLink = new DownloadLink(profile, geneList, patientIds,
                    remoteCall.getRawContent());
            downloadLinkSet.add(downloadLink);
            warningUnion.addAll(remoteCall.getWarnings());
            if( pData == null ){
               System.err.println( "pData == null" );
            }else{
               if( pData.getGeneList() == null ){
                  System.err.println( "pData.getValidGeneList() == null" );
               }
            }
            if (pData != null) {
                xdebug.logMsg(this, "Got number of genes:  " + pData.getGeneList().size());
                xdebug.logMsg(this, "Got number of cases:  " + pData.getCaseIdList().size());
            }
            xdebug.logMsg(this, "Number of warnings received:  " + remoteCall.getWarnings().size());
            profileDataList.add(pData);

            //  Optionally, get Extended Mutation Data.
            if (profile.getGeneticAlterationType().equals
                    (GeneticAlterationType.MUTATION_EXTENDED)) {
                if (geneList.size() <= MUTATION_DETAIL_LIMIT) {
                    xdebug.logMsg(this, "Number genes requested is <= " + MUTATION_DETAIL_LIMIT);
                    xdebug.logMsg(this, "Therefore, getting extended mutation data");
                    GetMutationData remoteCallMutation = new GetMutationData();
                    List<ExtendedMutation> tempMutationList =
                            remoteCallMutation.getMutationData(profile, geneList, new HashSet(stableSampleIds), xdebug);
                    if (tempMutationList != null && tempMutationList.size() > 0) {
                        xdebug.logMsg(this, "Total number of mutation records retrieved:  "
                            + tempMutationList.size());
                        mutationList.addAll(tempMutationList);
                    }
                } else {
                    request.setAttribute(MUTATION_DETAIL_LIMIT_REACHED, Boolean.TRUE);
                }
            }
        }
        
        //  Store Extended Mutations
        request.setAttribute(INTERNAL_EXTENDED_MUTATION_LIST, mutationList);

        // Store download links in session (for possible future retrieval).
        request.getSession().setAttribute(DOWNLOAD_LINKS, downloadLinkSet);

        String tabIndex = request.getParameter(QueryBuilder.TAB_INDEX);
        if (tabIndex != null && tabIndex.equals(QueryBuilder.TAB_VISUALIZE)) {
            xdebug.logMsg(this, "Merging Profile Data");
            ProfileMerger merger = new ProfileMerger(profileDataList);
            ProfileData mergedProfile = merger.getMergedProfile();

            xdebug.logMsg(this, "Merged Profile, Number of genes:  "
                    + mergedProfile.getGeneList().size());
            ArrayList<String> mergedProfileGeneList = mergedProfile.getGeneList();
            for (String currentGene:  mergedProfileGeneList) {
                xdebug.logMsg(this, "Merged Profile Gene:  " + currentGene);
            }
            xdebug.logMsg(this, "Merged Profile, Number of cases:  "
                    + mergedProfile.getCaseIdList().size());
            request.setAttribute(MERGED_PROFILE_DATA_INTERNAL, mergedProfile);
            request.setAttribute(WARNING_UNION, warningUnion);

            String output = request.getParameter(OUTPUT);
            String format = request.getParameter(FORMAT);
            double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
            double rppaScoreThreshold = ZScoreUtil.getRPPAScore(request);
            request.setAttribute(Z_SCORE_THRESHOLD, zScoreThreshold);
            request.setAttribute(RPPA_SCORE_THRESHOLD, rppaScoreThreshold);

            if (output != null) {
				if (output.equals("text")) {
                    outputPlainText(response, mergedProfile, theOncoPrintSpecParserOutput,
                            zScoreThreshold, rppaScoreThreshold);
                }
            } else {

                // Store download links in session (for possible future retrieval).
                request.getSession().setAttribute(DOWNLOAD_LINKS, downloadLinkSet);
                RequestDispatcher dispatcher =
                        getServletContext().getRequestDispatcher("/WEB-INF/jsp/visualize.jsp");
                dispatcher.forward(request, response);
            }
        } else {
            ShowData.showDataAtSpecifiedIndex(servletContext, request,
                    response, 0, xdebug);
        }
    }

    private void outputPlainText(HttpServletResponse response, ProfileData mergedProfile,
            ParserOutput theOncoPrintSpecParserOutput, double zScoreThreshold, double rppaScoreThreshold) throws IOException {
        response.setContentType("text/plain");
        ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold );
        PrintWriter writer = response.getWriter();
        writer.write("" + dataSummary.getPercentCasesAffected());
        writer.flush();
        writer.close();
    }

    /**
     * validate the portal web input form.
     */
    private boolean validateForm(String action,
                                ArrayList<GeneticProfile> profileList,
                                 HashSet<String> geneticProfileIdSet,
                                 String geneList, String patientSetId, String patientIds,
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
                if (patientIds != null &&
                	patientSetId != null &&
                	patientSetId.equals("-1"))
                {
                	// empty patient list
                	if (patientIds.trim().length() == 0)
                	{
                		httpServletRequest.setAttribute(STEP3_ERROR_MSG,
                				"Please enter at least one patient ID below. ");
                		
                		errorsExist = true;
                	}
                	else
                	{
                		List<String> invalidPatients = PatientSetUtil.validatePatientSet(
                				cancerStudyIdentifier, patientIds);
                		
                		String patientSetErrMsg = "Invalid patient(s) for the selected cancer study:";
                		
                		// non-empty list, but contains invalid patient IDs
                		if (invalidPatients.size() > 0)
                		{
                			// append patient ids to the message
                    		for (String patientId : invalidPatients)
                    		{
                    			patientSetErrMsg += " " + patientId;
                    		}
                    		
                			httpServletRequest.setAttribute(STEP3_ERROR_MSG,
                					patientSetErrMsg);
                    		
                    		errorsExist = true;
                		}
                	}
                }

                errorsExist = validateGenes(geneList, httpServletRequest, errorsExist);

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
        if( errorsExist ){
           httpServletRequest.setAttribute( GENE_LIST, geneList );
       }
        return errorsExist;
    }

    private boolean validateGenes(String geneList, HttpServletRequest httpServletRequest,
            boolean errorsExist) throws DaoException {
        try {
            GeneValidator geneValidator = new GeneValidator(geneList);
        } catch (GeneValidationException e) {
            errorsExist = true;
            httpServletRequest.setAttribute(STEP4_ERROR_MSG, e.getMessage());
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

package org.mskcc.portal.servlet;


import java.io.IOException;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mskcc.portal.model.*;
import org.mskcc.portal.network.*;
import org.mskcc.portal.oncoPrintSpecLanguage.CallOncoPrintSpecParser;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintLangException;
import org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.portal.remote.*;
import org.mskcc.portal.util.*;
import org.mskcc.portal.r_bridge.SurvivalPlot;
import org.owasp.validator.html.PolicyException;
import org.apache.log4j.Logger;

/**
 * Central Servlet for building queries.
 */
public class QueryBuilder extends HttpServlet {
    public static final boolean INCLUDE_NETWORKS = true;
    public static final String CGDS_URL_PARAM = "cgds_url";
    public static final String PATHWAY_COMMONS_URL_PARAM = "pathway_commons_url";
    public static final String CANCER_TYPES_INTERNAL = "cancer_types";
    public static final String PROFILE_LIST_INTERNAL = "profile_list";
    public static final String CASE_SETS_INTERNAL = "case_sets";
    public static final String CANCER_TYPE_ID = "cancer_type_id";
    public static final String CLINICAL_DATA_LIST = "clinical_data_list";
    public static final String GENETIC_PROFILE_IDS = "genetic_profile_ids";
    public static final String GENE_SET_CHOICE = "gene_set_choice";
    public static final String CASE_SET_ID = "case_set_id";
    public static final String CASE_IDS = "case_ids";
    public static final String GENE_LIST = "gene_list";
    public static final String ACTION = "action";
    public static final String OUTPUT = "output";
    public static final String FORMAT = "format";
    public static final String PLOT_TYPE = "plot_type";
    public static final String OS_SURVIVAL_PLOT = "os_survival_plot";
    public static final String DFS_SURVIVAL_PLOT = "dfs_survival_plot";
    public static final String XDEBUG = "xdebug";
    public static final String ACTION_SUBMIT = "Submit";
    public static final String STEP2_ERROR_MSG = "step2_error_msg";
    public static final String STEP3_ERROR_MSG = "step3_error_msg";
    public static final String STEP4_ERROR_MSG = "step4_error_msg";
    public static final String MERGED_PROFILE_DATA_INTERNAL = "merged_profile_data";
    public static final String WARNING_UNION = "warning_union";
    public static final String DOWNLOAD_LINKS = "download_links";
    public static final String NETWORK_SIF = "network_sif";
    public static final String HTML_TITLE = "html_title";
    public static final String TAB_INDEX = "tab_index";
    public static final String TAB_DOWNLOAD = "tab_download";
    public static final String TAB_VISUALIZE = "tab_visualize";
    public static final String USER_ERROR_MESSAGE = "user_error_message";
    public static final String ATTRIBUTE_URL_BEFORE_FORWARDING = "ATTRIBUTE_URL_BEFORE_FORWARDING";
    public static final String Z_SCORE_THRESHOLD = "Z_SCORE_THRESHOLD";
    public static final String MRNA_PROFILES_SELECTED = "MRNA_PROFILES_SELECTED";
    public static final String COMPUTE_LOG_ODDS_RATIO = "COMPUTE_LOG_ODDS_RATIO";
    public static final String MUTATION_MAP = "MUTATION_MAP";
    public static final int MUTATION_DETAIL_LIMIT = 10;
    public static final String MUTATION_DETAIL_LIMIT_REACHED = "MUTATION_DETAIL_LIMIT_REACHED";
    public static final int MAX_NUM_GENES = 100;
    
    private static final String HGNC = "HGNC";
    private static final String NODE_ATTR_IN_QUERY = "IN_QUERY";
    private static final String NODE_ATTR_IN_PORTAL = "IN_PORTAL";
    private static final String NODE_ATTR_PERCENTAGE_ALTERED = "PERCENTAGE_ALTERED";
    private static final String NODE_ATTR_PERCENTAGE_MUTATED = "PERCENTAGE_MUTATED";
    
    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        String cgdsUrl = getInitParameter(CGDS_URL_PARAM);
        GlobalProperties.setCgdsUrl(cgdsUrl);
		String pathwayCommonsUrl = getInitParameter(PATHWAY_COMMONS_URL_PARAM);
        GlobalProperties.setPathwayCommonsUrl(pathwayCommonsUrl);
        try {
            servletXssUtil = ServletXssUtil.getInstance();
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
        XDebug xdebug = new XDebug();
        xdebug.startTimer();
        boolean userIsAuthorized = false;
        if (SkinUtil.usersMustAuthenticate()) {
            if (AuthenticateUser.userIsAuthenticated(httpServletRequest)) {
                userIsAuthorized = true;
            }
        } else {
           userIsAuthorized = true;
        }

        if (userIsAuthorized) {
            xdebug.logMsg(this, "Attempting to initiate new user query.");

            if (httpServletRequest.getRequestURL() != null) {
                httpServletRequest.setAttribute(ATTRIBUTE_URL_BEFORE_FORWARDING,
                        httpServletRequest.getRequestURL().toString());
            }

            //  Get User Selected Action
            String action = servletXssUtil.getCleanInput (httpServletRequest, ACTION);

            //  Get User Selected Cancer Type
            String cancerTypeId = servletXssUtil.getCleanInput(httpServletRequest, CANCER_TYPE_ID);

            //  Get User Selected Genetic Profiles
            String geneticProfileIds[] = httpServletRequest.getParameterValues(GENETIC_PROFILE_IDS);
            HashSet<String> geneticProfileIdSet = new HashSet<String>();
            if (geneticProfileIds != null && geneticProfileIds.length > 0) {
                for (String geneticProfileIdDirty : geneticProfileIds) {
                    String geneticProfileIdClean = servletXssUtil.getCleanInput(geneticProfileIdDirty);
                    geneticProfileIdSet.add(geneticProfileIdClean);
                }
            }
            httpServletRequest.setAttribute(GENETIC_PROFILE_IDS, geneticProfileIdSet);

            //  Get User Defined Gene List
            String geneList = servletXssUtil.getCleanInput (httpServletRequest, GENE_LIST);
            xdebug.logMsg(this, "Gene List is set to:  " + geneList);

            //  Get all Cancer Types
            try {
                ArrayList<CancerType> cancerTypeList = GetCancerTypes.getCancerTypes(xdebug);

                if (cancerTypeId == null) {
                    cancerTypeId = cancerTypeList.get(0).getCancerTypeId();
                }
                httpServletRequest.setAttribute(CANCER_TYPE_ID, cancerTypeId);

                httpServletRequest.setAttribute(CANCER_TYPES_INTERNAL, cancerTypeList);

                //  Get Genetic Profiles for Selected Cancer Type
                ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles
                        (cancerTypeId, xdebug);
                httpServletRequest.setAttribute(PROFILE_LIST_INTERNAL, profileList);

                //  Get Case Sets for Selected Cancer Type
                ArrayList<CaseSet> caseSets = GetCaseSets.getCaseSets(cancerTypeId, xdebug);
                CaseSet caseSet = new CaseSet();
                caseSet.setName("User-defined Case List");
                caseSet.setDescription("User defined case list.");
                caseSet.setId("-1");
                caseSets.add(caseSet);
                httpServletRequest.setAttribute(CASE_SETS_INTERNAL, caseSets);

                //  Get User Selected Case Set
                String caseSetId = servletXssUtil.getCleanInput(httpServletRequest, CASE_SET_ID);
                if (caseSetId != null) {
                    httpServletRequest.setAttribute(CASE_SET_ID, caseSetId);
                } else {
                    if (caseSets.size() > 0) {
                        CaseSet zeroSet = caseSets.get(0);
                        httpServletRequest.setAttribute(CASE_SET_ID, zeroSet.getId());
                    }
                }
                String caseIds = servletXssUtil.getCleanInput(httpServletRequest, CASE_IDS);
                httpServletRequest.setAttribute("xdebug_object", xdebug);

                boolean errorsExist = validateForm(action, profileList, geneticProfileIdSet, geneList,
                        caseSetId, caseIds, httpServletRequest);
                if (action != null && action.equals(ACTION_SUBMIT) && (!errorsExist)) {

                   processData(geneticProfileIdSet, profileList, geneList, caseSetId,
                            caseIds, caseSets, getServletContext(), httpServletRequest,
                            httpServletResponse, xdebug);
                } else {
                    RequestDispatcher dispatcher =
                            getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp");
                    dispatcher.forward(httpServletRequest, httpServletResponse);
                }
            } catch (RemoteException e) {
                xdebug.logMsg(this, "Got Remote Exception:  " + e.getMessage());
                forwardToErrorPage(httpServletRequest, httpServletResponse,
                        "The Cancer Genomics Data Server is not currently "
                                + "available. <br/><br/>Please check back later.", xdebug);

            }
        } else {
            RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher("/WEB-INF/jsp/sign_in.jsp");
            dispatcher.forward(httpServletRequest, httpServletResponse);
        }
    }



    // This method checks the user information sent in the Authorization
    // header against the database of users maintained in the users Hashtable.
    protected boolean allowUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String auth = request.getHeader("Authorization");
        if (auth == null) {
            askForPassword (response);
            return false;
        } else {
            String userInfo = auth.substring(6).trim();
            // TODO: replace unsupported library
            /*
             * but sun.misc.* isn't supported (see http://java.sun.com/products/jdk/faq/faq-sun-packages.html);
             * replace it with 
             * import org.apache.commons.codec.binary.Base64;
             * 
            String nameAndPassword = new String(Base64.decodeBase64(userInfo.getBytes())); 
             * 
             */
            sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
            String nameAndPassword = new String (decoder.decodeBuffer(userInfo));
            if (nameAndPassword != null) {
                int index = nameAndPassword.indexOf(":");
                if (index > -1) {
                    String user = nameAndPassword.substring(0, index);
                    String password = nameAndPassword.substring(index+1);

                    // Check our user list to see if that user and password are "allowed"
                    if (user.equals("tcga_user") && password.equals("tcga123")) {
                        return true;
                    } else {
                        askForPassword(response);
                        return false;
                    }
                } else {
                    askForPassword(response);
                    return false;
                }
            } else {
                askForPassword(response);
                return false;
            }
        }
    }

    private void askForPassword (HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", "BASIC realm=\"users\"");
        response.sendError(response.SC_UNAUTHORIZED);
    }
    
    /**
     * process a good request
     * 
     * @param geneticProfileIdSet  User Selected Genetic Profiles
     * @param profileList  Genetic Profiles for Selected Cancer Type
     * @param geneListStr  User Defined Gene List, i.e., input in the 'gene symbols / OQL' text box
     * @param caseSetId  
     * @param caseIds  User Selected Case Set
     * @param caseSetList  Case Sets for Selected Cancer Type
     * @param servletContext
     * @param request  the HTTP Request
     * @param response the HTTP Response
     * @param xdebug
     * 
     * @throws IOException
     * @throws ServletException
     */
    private void processData(HashSet<String> geneticProfileIdSet,
                                ArrayList<GeneticProfile> profileList, 
                                String geneListStr,
                                String caseSetId, String caseIds,
                                ArrayList<CaseSet> caseSetList,
                                ServletContext servletContext, HttpServletRequest request,
                                HttpServletResponse response,
                                XDebug xdebug)
            throws IOException, ServletException {

       // parse geneList, written in the OncoPrintSpec language (except for changes by XSS clean)
       ParserOutput theOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver( geneListStr, 
                geneticProfileIdSet, profileList, ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request) );
       
        ArrayList<String> geneList = new ArrayList<String>();
        geneList.addAll( theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes() );

        xdebug.logMsg(this, "Using gene list geneList.toString():  " + geneList.toString());
        if (!caseSetId.equals("-1")) {
            for (CaseSet caseSet : caseSetList) {
                if (caseSet.getId().equals(caseSetId)) {
                    caseIds = caseSet.getCaseListAsString();
                }
            }
        }

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
            xdebug.logMsg(this, "Getting data for:  " + profile.getName());
            Date startTime = new Date();
            GetProfileData remoteCall = new GetProfileData();
            ProfileData pData = remoteCall.getProfileData(profile, geneList, caseIds, xdebug);
            Date stopTime = new Date();
            long timeElapsed = stopTime.getTime() - startTime.getTime();
            xdebug.logMsg(this, "Total Time for Connection to Web API:  " + timeElapsed + " ms.");
            DownloadLink downloadLink = new DownloadLink(profile, geneList, caseIds,
                    remoteCall.getContent());
            downloadLinkSet.add(downloadLink);
            warningUnion.addAll(remoteCall.getWarnings());
            if( pData == null ){
               System.err.println( "pData == null" );
            }else{
               if( pData.getGeneList() == null ){
                  System.err.println( "pData.getGeneList() == null" );
               }
            }
            xdebug.logMsg(this, "URI:  " + remoteCall.getURI());
            if (pData != null) {
                xdebug.logMsg(this, "Got number of genes:  " + pData.getGeneList().size());
                xdebug.logMsg(this, "Got number of cases:  " + pData.getCaseIdList().size());
            }
            xdebug.logMsg(this, "Number of warnings received:  " + remoteCall.getWarnings().size());
            profileDataList.add(pData);

            //  Optionally, get Extended Mutation Data.
            if (profile.getAlterationType().equals(GeneticAlterationType.MUTATION_EXTENDED)) {
                if (geneList.size() <= MUTATION_DETAIL_LIMIT) {
                    xdebug.logMsg(this, "Number genes requested is <= " + MUTATION_DETAIL_LIMIT);
                    startTime = new Date();
                    xdebug.logMsg(this, "Therefore, getting extended mutation data");
                    GetMutationData remoteCallMutation = new GetMutationData();
                    ArrayList<ExtendedMutation> tempMutationList =
                            remoteCallMutation.getMutationData(profile,
                                    geneList, caseIds, xdebug);
                    if (tempMutationList != null && tempMutationList.size() > 0) {
                        mutationList.addAll(tempMutationList);
                    }
                    stopTime = new Date();
                    timeElapsed = stopTime.getTime() - startTime.getTime();
                    xdebug.logMsg(this, "Total Time for Connection to Web API:  " + timeElapsed + " ms.");
                } else {
                    request.setAttribute(MUTATION_DETAIL_LIMIT_REACHED, Boolean.TRUE);
                }
            }
        }
        
        //  Store Extended Mutations
        ExtendedMutationMap mutationMap = new ExtendedMutationMap(mutationList);
        request.setAttribute(MUTATION_MAP, mutationMap);

        // Store download links in session (for possible future retrieval).
        request.getSession().setAttribute(DOWNLOAD_LINKS, downloadLinkSet);

        String tabIndex = servletXssUtil.getCleanInput(request, QueryBuilder.TAB_INDEX);
        if (tabIndex != null && tabIndex.equals(QueryBuilder.TAB_VISUALIZE)) {
            xdebug.logMsg(this, "Merging Profile Data");
            ProfileMerger merger = new ProfileMerger(profileDataList);
            ProfileData mergedProfile = merger.getMergedProfile();

            //  Get Clinical Data
            xdebug.logMsg(this, "Getting Clinical Data:");
            ArrayList <ClinicalData> clinicalDataList =
                    GetClinicalData.getClinicalData(caseIds, xdebug);
            xdebug.logMsg(this, "Got Clinical Data for:  " + clinicalDataList.size()
                +  " cases.");
            request.setAttribute(CLINICAL_DATA_LIST, clinicalDataList);

            xdebug.logMsg(this, "Merged Profile, Number of genes:  "
                    + mergedProfile.getGeneList().size());
            xdebug.logMsg(this, "Merged Profile, Number of cases:  "
                    + mergedProfile.getCaseIdList().size());
            request.setAttribute(MERGED_PROFILE_DATA_INTERNAL, mergedProfile);
            request.setAttribute(WARNING_UNION, warningUnion);

            String output = servletXssUtil.getCleanInput(request, OUTPUT);
            String format = servletXssUtil.getCleanInput(request, FORMAT);
            double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);

            if (output != null) {

                String showAlteredColumns = servletXssUtil.getCleanInput(request, "showAlteredColumns");
                boolean showAlteredColumnsBool = false;
                if( showAlteredColumns != null && showAlteredColumns.equals("true")) {
                    showAlteredColumnsBool = true;
                }
                if (output.equalsIgnoreCase("svg")) {
                    response.setContentType("image/svg+xml");
                    MakeOncoPrint.OncoPrintType theOncoPrintType = MakeOncoPrint.OncoPrintType.SVG;
                    MakeOncoPrint.makeOncoPrint(request, response, theOncoPrintType, showAlteredColumnsBool, geneticProfileIdSet, profileList );
                    
                } else if (output.equalsIgnoreCase("html")) {
                    response.setContentType("text/html");
                    MakeOncoPrint.OncoPrintType theOncoPrintType = MakeOncoPrint.OncoPrintType.HTML;
                    MakeOncoPrint.makeOncoPrint(request, response, theOncoPrintType, showAlteredColumnsBool, geneticProfileIdSet, profileList );

                } else if (output.equals("text")) {
                    response.setContentType("text/plain");

                    ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile,
                            theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold );
                    PrintWriter writer = response.getWriter();
                    writer.write("" + dataSummary.getPercentCasesAffected());
                    writer.flush();
                    writer.close();
                } else if (output.equals(OS_SURVIVAL_PLOT)) {
                    ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile,
                            theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold );
                    SurvivalPlot survivalPlot = new SurvivalPlot(SurvivalPlot.SurvivalPlotType.OS,
                            clinicalDataList, dataSummary, format, response);
                } else if (output.equals(DFS_SURVIVAL_PLOT)) {
                    ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile,
                            theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold );
                    SurvivalPlot survivalPlot = new SurvivalPlot(SurvivalPlot.SurvivalPlotType.DFS, 
                            clinicalDataList, dataSummary, format, response);
                }
            } else {
                //  (Optionally) Get Network of Interest
                if (INCLUDE_NETWORKS) {
                    Network network;
                    try {
                        network = GetPathwayCommonsNetwork.getNetwork(geneList, xdebug);
                    } catch (Exception e) {
                        xdebug.logMsg(this, "Failed retrieving networks from cPath2\n"+e.getMessage());
                        network = new Network(); // send an empty network instead
                    }
                    
                    // add attribute is_query to indicate if a node is in query genes
                    HashSet<String> queryGenes = new HashSet<String>(mergedProfile.getGeneList());
                    // and get the list of newly imported genes
                    ArrayList<String> newGenes = new ArrayList();
                    for (Node node : network.getNodes()) {
                        Set<String> ngnc = node.getXref(HGNC);
                    
                        Boolean in_query = Boolean.FALSE;
                        if (!ngnc.isEmpty()) { 
                            if(Collections.disjoint(ngnc, queryGenes)) {
                                newGenes.add(ngnc.iterator().next());
                            } else {
                                in_query = Boolean.TRUE;
                            }
                        } 
                        node.addAttribute(NODE_ATTR_IN_QUERY, in_query);
                    }
                    newGenes.removeAll(mergedProfile.getGeneList());
                    
                    // retrieve profiles from CGDS for new genes
                    ArrayList<ProfileData> newProfileDataList = new ArrayList<ProfileData>();
                    newProfileDataList.addAll(profileDataList);
                    for (String profileId : geneticProfileIdSet) {                        
                        GeneticProfile profile = GeneticProfileUtil.getProfile(profileId, profileList);
                        if( null == profile ){
                           continue;
                        }
                        xdebug.logMsg(this, "Getting data for:  " + profile.getName());
                        Date startTime = new Date();
                        GetProfileData remoteCall = new GetProfileData();
                        ProfileData pData = remoteCall.getProfileData(profile, newGenes, caseIds, xdebug);
                        Date stopTime = new Date();
                        long timeElapsed = stopTime.getTime() - startTime.getTime();
                        xdebug.logMsg(this, "Total Time for Connection to Web API:  " + timeElapsed + " ms.");
                        if( pData == null ){
                           System.err.println( "pData == null" );
                        }else{
                           if( pData.getGeneList() == null ){
                              System.err.println( "pData.getGeneList() == null" );
                           }
                        }
                        xdebug.logMsg(this, "URI:  " + remoteCall.getURI());
                        if (pData != null) {
                            xdebug.logMsg(this, "Got number of genes:  " + pData.getGeneList().size());
                            xdebug.logMsg(this, "Got number of cases:  " + pData.getCaseIdList().size());
                        }
                        xdebug.logMsg(this, "Number of warnings received:  " + remoteCall.getWarnings().size());
                        newProfileDataList.add(pData);

                    }
                    
                    merger = new ProfileMerger(newProfileDataList);
                    ProfileData newMergedProfile = merger.getMergedProfile();
                    ProfileDataSummary dataSummary = new ProfileDataSummary(newMergedProfile,
                            theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold );
                    
                    // add attributes
                    for (String gene : newMergedProfile.getGeneList()) {
                        for (Node node : network.getNodesByXref(HGNC, gene)) {
                            node.addAttribute(NODE_ATTR_PERCENTAGE_ALTERED, dataSummary.getPercentCasesWhereGeneIsAltered(gene));
                            node.addAttribute(NODE_ATTR_PERCENTAGE_MUTATED, dataSummary.getPercentCasesWhereGeneIsMutated(gene));
                        }
                    }
                    
                    
                    String graphML = NetworkIO.writeNetwork2GraphML(network, new NetworkIO.NodeLabelHandler() {
                        // using HGNC gene symbol as label if available
                        public String getLabel(Node node) {
                            Set<String> ngnc = node.getXref(HGNC);
                            if (ngnc.isEmpty())
                                return node.getId();
                            return ngnc.iterator().next();
                        }
                    });
                    request.setAttribute(NETWORK_SIF, graphML);
                }

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
    
    /**
     * validate the portal web input form.
     * @param action
     * @param profileList
     * @param geneticProfileIdSet
     * @param geneList the list of genes, possibly annotated with the OncoPrintSpec language
     * @param caseSetId
     * @param caseIds
     * @param httpServletRequest
     * @return true, if the form contains fatal error(s) that require it be resubmitted
     */
    private boolean validateForm(String action, ArrayList<GeneticProfile> profileList,
                                 HashSet<String> geneticProfileIdSet,
                                 String geneList, String caseSetId, String caseIds,
                                 HttpServletRequest httpServletRequest) {
        boolean errorsExist = false;
        String tabIndex = servletXssUtil.getCleanInput(httpServletRequest, QueryBuilder.TAB_INDEX);
        if (action != null) {
            if (action.equals(ACTION_SUBMIT)) {
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
                if (geneList != null && geneList.trim().length() == 0) {
                    httpServletRequest.setAttribute(STEP4_ERROR_MSG,
                            "Please enter at least one gene symbol below. ");
                    errorsExist = true;
                }
                if (caseSetId.equals("-1") && caseIds.trim().length() == 0) {
                    httpServletRequest.setAttribute(STEP3_ERROR_MSG,
                            "Please enter at least one case ID below. ");
                    errorsExist = true;
                }
                if (geneList != null && geneList.trim().length() > 0) {
                    String geneSymbols[] = geneList.split("\\s");
                    int numGenes = 0;
                    for (String gene : geneSymbols) {
                        if (gene.trim().length() > 0) {
                            numGenes++;
                        }
                    }

                    if (numGenes > MAX_NUM_GENES) {
                        httpServletRequest.setAttribute(STEP4_ERROR_MSG,
                                "Please restrict your request to " + MAX_NUM_GENES + " genes or less.");
                        errorsExist = true;
                    }
                    
                    // output any errors generated by the parser
                    ParserOutput theOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver( geneList, 
                             geneticProfileIdSet, profileList, ZScoreUtil.getZScore(geneticProfileIdSet, profileList, httpServletRequest ) );                    
                    
                    if( 0<theOncoPrintSpecParserOutput.getSyntaxErrors().size() || 0<theOncoPrintSpecParserOutput.getSemanticsErrors().size() ){
                       StringBuffer sb = new StringBuffer();
                       for( String e: theOncoPrintSpecParserOutput.getSyntaxErrors() ){
                          sb.append(e+"<br>");
                       }
                       for( OncoPrintLangException e: theOncoPrintSpecParserOutput.getSemanticsErrors() ){
                          sb.append(e.getMessage()+"<br>");
                       }
                       httpServletRequest.setAttribute(STEP4_ERROR_MSG, sb.toString() );
                       errorsExist = true;
                    }
                }

                //  Additional validation rules
                //  If we have selected mRNA Expression Data Check Box, but failed to
                //  select an mRNA profile, this is an error.
                String mRNAProfileSelected = servletXssUtil.getCleanInput(httpServletRequest,
                        QueryBuilder.MRNA_PROFILES_SELECTED);
                if (mRNAProfileSelected != null && mRNAProfileSelected.equalsIgnoreCase("on")) {

                    //  Make sure that at least one of the mRNA profiles is selected
                    boolean mRNAProfileRadioSelected = false;
                    for (int i = 0; i < profileList.size(); i++) {
                        GeneticProfile geneticProfile = profileList.get(i);
                        if (geneticProfile.getAlterationType()
                                == GeneticAlterationType.MRNA_EXPRESSION
                                && geneticProfileIdSet.contains(geneticProfile.getId())) {
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

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.cbio.portal.model.*" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.CallOncoPrintSpecParser" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintSpecification" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.Utilities" %>
<%@ page import="org.mskcc.cbio.portal.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.portal.model.CaseList" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticAlterationType" %>
<%@ page import="org.mskcc.cbio.portal.model.Patient" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoGeneticProfile" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.lang.reflect.Array" %>
<%@ page import="static org.mskcc.cbio.portal.servlet.QueryBuilder.INTERNAL_EXTENDED_MUTATION_LIST" %>
<%@ page import="org.mskcc.cbio.portal.util.*" %>

<%
    //Security Filter Instance
    ServletXssUtil servletXssUtil = ServletXssUtil.getInstance();


    ArrayList<GeneticProfile> profileList =
            (ArrayList<GeneticProfile>) request.getAttribute
                    (QueryBuilder.PROFILE_LIST_INTERNAL);
    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute
            (QueryBuilder.GENETIC_PROFILE_IDS);
    ServletXssUtil xssUtil = ServletXssUtil.getInstance();
    double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
    double rppaScoreThreshold = ZScoreUtil.getRPPAScore(request);
    ArrayList<CaseList> caseSets = (ArrayList<CaseList>)
            request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String caseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String caseIds = xssUtil.getCleanerInput(request, QueryBuilder.CASE_IDS);
    ArrayList<CancerStudy> cancerStudies = (ArrayList<CancerStudy>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
    String cancerTypeId = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);



    /**
     * Put together global parameters for injection as javascript variables
     */
    // put geneticProfileIds into the proper form for the JSON request
    String geneticProfiles = StringUtils.join(geneticProfileIdSet.iterator(), " ");
    geneticProfiles = xssUtil.getCleanerInput(geneticProfiles.trim());

    String caseIdsKey = (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY);
    caseIdsKey = xssUtil.getCleanerInput(caseIdsKey);

    // get cases
    String cases = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
    cases = xssUtil.getCleanerInput(cases);

    ProfileData mergedProfile = (ProfileData)
            request.getAttribute(QueryBuilder.MERGED_PROFILE_DATA_INTERNAL);
    String oql = xssUtil.getCleanerInput(request, QueryBuilder.GENE_LIST);

    ParserOutput theOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver( oql,
            (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS),
            (ArrayList<GeneticProfile>) request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL),
            zScoreThreshold, rppaScoreThreshold );

    ArrayList<String> listOfGenes = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();

    boolean showIGVtab = false;
    String[] cnaTypes = {"_gistic", "_cna", "_consensus", "_rae"};
    for (int lc = 0; lc < cnaTypes.length; lc++) {
        String cnaProfileID = cancerTypeId + cnaTypes[lc];
        if (DaoGeneticProfile.getGeneticProfileByStableId(cnaProfileID) != null){
            showIGVtab = true;
            break;
        }
    }

    OncoPrintSpecification theOncoPrintSpecification = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
    ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile, theOncoPrintSpecification, zScoreThreshold, rppaScoreThreshold );

    DecimalFormat percentFormat = new DecimalFormat("###,###.#%");
    String geneSetChoice = xssUtil.getCleanInput(request, QueryBuilder.GENE_SET_CHOICE);
    if (geneSetChoice == null) {
        geneSetChoice = "user-defined-list";
    }
    GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
    ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
    Set<String> warningUnion = (Set<String>) request.getAttribute(QueryBuilder.WARNING_UNION);


    ArrayList <GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();
    ArrayList<String> mergedCaseList = mergedProfile.getCaseIdList();

    String siteTitle = GlobalProperties.getTitle();
    String bitlyUser = GlobalProperties.getBitlyUser();
    String bitlyKey = GlobalProperties.getBitlyApiKey();

    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Results");

    boolean computeLogOddsRatio = true;

    Boolean mutationDetailLimitReached = (Boolean)
            request.getAttribute(QueryBuilder.MUTATION_DETAIL_LIMIT_REACHED);

    ArrayList <Patient> clinicalDataList = (ArrayList<Patient>)
            request.getAttribute(QueryBuilder.CLINICAL_DATA_LIST);

    boolean rppaExists = countProfiles(profileList, GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) > 0;

    boolean has_rppa = countProfiles(profileList, GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) > 0;
    boolean has_mrna = countProfiles(profileList, GeneticAlterationType.MRNA_EXPRESSION) > 0;
    boolean has_methylation = countProfiles(profileList, GeneticAlterationType.METHYLATION) > 0;
    boolean has_copy_no = countProfiles(profileList, GeneticAlterationType.COPY_NUMBER_ALTERATION) > 0;

    boolean includeNetworks = GlobalProperties.includeNetworks();

    /**
     * Put together parameters for an AJAX call to GeneAlterations.json
     */
    // put gene string into a form that javascript can swallow
    String genes = (String) request.getAttribute(QueryBuilder.RAW_GENE_STR);
    genes = StringEscapeUtils.escapeJavaScript(genes);

    // get cases
    String samples = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
    samples = StringEscapeUtils.escapeJavaScript(samples);


    //////////////from network.jsp
    String zScoreThesholdStr4Network =
            xssUtil.getCleanerInput(request.getAttribute(QueryBuilder.Z_SCORE_THRESHOLD).toString());
    String genes4Network = StringUtils.join((List)request.getAttribute(QueryBuilder.GENE_LIST)," ");
    String geneticProfileIds4Network = xssUtil.getCleanerInput(StringUtils.join(geneticProfileIdSet," "));
    String cancerTypeId4Network = xssUtil.getCleanerInput((String)request.getAttribute(QueryBuilder.CANCER_STUDY_ID));
// 	String caseIds4Network = ((String)request.getAttribute(QueryBuilder.CASE_IDS)).
// 			replaceAll("\\s", " ").trim(); // convert white spaces to space (to prevent network tab to crash)
    String caseIdsKey4Network = xssUtil.getCleanerInput((String)request.getAttribute(QueryBuilder.CASE_IDS_KEY));
    String caseSetId4Network = xssUtil.getCleanerInput((String)request.getAttribute(QueryBuilder.CASE_SET_ID));

    //////////////from plots_tab.jsp
    String cancer_study_id = xssUtil.getCleanerInput(request, "cancer_study_id");
    String case_set_id = xssUtil.getCleanerInput(request, "case_set_id");
    String genetic_profile_id = xssUtil.getCleanerInput(request, "genetic_profile_id");
    //Translate Onco Query Language
    ArrayList<String> _listOfGenes = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
    String tmpGeneStr = "";
    for(String gene: _listOfGenes) {
        tmpGeneStr += gene + " ";
    }
    tmpGeneStr = tmpGeneStr.trim();


    //////////from protein_exp.jsp
    String cancerStudyId_RPPA = xssUtil.getCleanerInput(
            (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID));

%>

<script type="text/javascript">
    window.PortalGlobals = {
        getCases: function() { return '<%= cases %>'; },
        getCaseIdsKey: function() { return '<%= caseIdsKey %>'; },
        getOqlString: (function() {
            var oql = '<%=oql%>'
                    .replace("&gt;", ">", "gm")
                    .replace("&lt;", "<", "gm")
                    .replace("&eq;", "=", "gm")
                    .replace(/[\r\n]/g, "\\n");

            return function() { return oql; };
        })(),
        getGeneListString: function() { return '<%=StringUtils.join(listOfGenes, " ")%>'},
        getGeneticProfiles: function() { return '<%=geneticProfiles%>'; },
        getZscoreThreshold: function() { return window.zscore_threshold; },
        getRppaScoreThreshold: function() { return window.rppa_score_threshold; }
    };

    // raw gene list (as it is entered by the user, it may contain onco query language)
    var genes = "<%=genes%>";

    // gene list after being processed by the onco query language parser
    var geneList = "<%=getGeneList(theOncoPrintSpecParserOutput)%>";

    // list of samples (case ids)
    var samples = "<%=samples%>";

    // genetic profile ids
    var geneticProfiles = "<%=geneticProfiles%>";



    /////from plots_tab.jsp
    var cancer_study_id = "<%out.print(cancer_study_id);%>",
            case_set_id = "<%out.print(case_set_id);%>";
    case_ids_key = "";
    if (case_set_id === "-1") {
        case_ids_key = "<%out.print(caseIdsKey);%>";
    }
    var genetic_profile_id = "<%out.print(genetic_profile_id);%>";
    var gene_list_str = "<%out.print(tmpGeneStr);%>";
    var gene_list = gene_list_str.split(/\s+/);

    //////////from protain_exp.jsp
    //var case_set_id = "<%out.print(case_set_id);%>";
    //case_ids_key = "";
    //if (case_set_id === "-1") {
    //    case_ids_key = "<%out.print(caseIdsKey);%>";
    //}



</script>

<%!
    public int countProfiles (ArrayList<GeneticProfile> profileList, GeneticAlterationType type) {
        int counter = 0;
        for (int i = 0; i < profileList.size(); i++) {
            GeneticProfile profile = profileList.get(i);
            if (profile.getGeneticAlterationType() == type) {
                counter++;
            }
        }
        return counter;
    }

    public String getGeneList(ParserOutput oncoPrintSpecParserOutput)
    {
        // translate Onco Query Language
        ArrayList<String> geneList =
                oncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();

        String genes = "";

        for(String gene: geneList)
        {
            genes += gene + " ";
        }

        return genes.trim();
    }
%>

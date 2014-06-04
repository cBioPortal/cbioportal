<!-- Collection of all global variables for the result pages of single cancer study query-->

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
    //Security Instance
    ServletXssUtil xssUtil = ServletXssUtil.getInstance();

    //Info about Genetic Profiles
    ArrayList<GeneticProfile> profileList = (ArrayList<GeneticProfile>) request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL);
    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS);
    ProfileData mergedProfile = (ProfileData)request.getAttribute(QueryBuilder.MERGED_PROFILE_DATA_INTERNAL);
    // put geneticProfileIds into the proper form for the JSON request
    String geneticProfiles = StringUtils.join(geneticProfileIdSet.iterator(), " ");
    geneticProfiles = xssUtil.getCleanerInput(geneticProfiles.trim());

    //Info about threshold settings
    double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
    double rppaScoreThreshold = ZScoreUtil.getRPPAScore(request);

    //Onco Query Language Parser Instance
	String oql = request.getParameter(QueryBuilder.GENE_LIST);

	// onco print spec parser needs the raw parameter
	if (request instanceof XssRequestWrapper)
	{
		oql = ((XssRequestWrapper)request).getRawParameter(QueryBuilder.GENE_LIST);
	}

    ParserOutput theOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver( oql,
            (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS),
            (ArrayList<GeneticProfile>) request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL),
            zScoreThreshold, rppaScoreThreshold );
    OncoPrintSpecification theOncoPrintSpecification = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
	// make the oql variable script-safe after processing
	//oql = StringEscapeUtils.escapeJavaScript(oql);
	oql = xssUtil.getCleanerInput(oql);

    //Info from data analysis/summary
    ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile, theOncoPrintSpecification, zScoreThreshold, rppaScoreThreshold );
    DecimalFormat percentFormat = new DecimalFormat("###,###.#%");
    String percentCasesAffected = percentFormat.format(dataSummary.getPercentCasesAffected());

    //Info about queried cancer study
    ArrayList<CancerStudy> cancerStudies = (ArrayList<CancerStudy>)request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
    String cancerTypeId = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);
    CancerStudy cancerStudy = cancerStudies.get(0);
    for (CancerStudy cs : cancerStudies){
        if (cancerTypeId.equals(cs.getCancerStudyStableId())){
            cancerStudy = cs;
            break;
        }
    }
    String cancerStudyName = cancerStudy.getName();

    //Info about Genes
    ArrayList<String> listOfGenes = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
    String geneSetChoice = xssUtil.getCleanInput(request, QueryBuilder.GENE_SET_CHOICE);
    if (geneSetChoice == null) {
        geneSetChoice = "user-defined-list";
    }
    GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
    ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
    ArrayList <GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();
    String geneSetName = "";
    for (GeneSet geneSet:  geneSetList) {
        if (geneSetChoice.equals(geneSet.getId())) {
            geneSetName = geneSet.getName();
        }
    }
    String genes = (String) request.getAttribute(QueryBuilder.RAW_GENE_STR);
    genes = StringEscapeUtils.escapeJavaScript(genes);

    //Info about Case Set(s)/Cases
    ArrayList<CaseList> caseSets = (ArrayList<CaseList>)request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    ArrayList<String> mergedCaseList = mergedProfile.getCaseIdList();
    int mergedCaseListSize = mergedCaseList.size();
    String caseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String caseSetName = "";
    for (CaseList caseSet:  caseSets) {
        if (caseSetId.equals(caseSet.getStableId())) {
            caseSetName = caseSet.getName();
        }
    }
    String cases = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
    //cases = xssUtil.getCleanerInput(cases);
    String caseIdsKey = (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY);
    //caseIdsKey = xssUtil.getCleanerInput(caseIdsKey);

    //Vision Control Tokens
    boolean showIGVtab = cancerStudy.hasCnaSegmentData();
    boolean has_rppa = countProfiles(profileList, GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) > 0;
    boolean has_mrna = countProfiles(profileList, GeneticAlterationType.MRNA_EXPRESSION) > 0;
    boolean has_methylation = countProfiles(profileList, GeneticAlterationType.METHYLATION) > 0;
    boolean has_copy_no = countProfiles(profileList, GeneticAlterationType.COPY_NUMBER_ALTERATION) > 0;
    boolean has_survival = cancerStudy.hasSurvivalData();
    boolean includeNetworks = GlobalProperties.includeNetworks();
    Set<String> warningUnion = (Set<String>) request.getAttribute(QueryBuilder.WARNING_UNION);
    boolean computeLogOddsRatio = true;
    Boolean mutationDetailLimitReached = (Boolean)request.getAttribute(QueryBuilder.MUTATION_DETAIL_LIMIT_REACHED);

    //General site info
    String siteTitle = GlobalProperties.getTitle();
    String bitlyUser = GlobalProperties.getBitlyUser();
    String bitlyKey = GlobalProperties.getBitlyApiKey();

    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Results");

    //Escape quotes in the returned strings
    cases = cases.replaceAll("'", "\\'");
    cases = cases.replaceAll("\"", "\\\"");
    caseSetName = caseSetName.replaceAll("'", "\\'");
    caseSetName = caseSetName.replaceAll("\"", "\\\"");

    //check if show co-expression tab
    boolean showCoexpTab = false;
    GeneticProfile final_gp = CoExpUtil.getPreferedGeneticProfile(cancerTypeId);
    if (final_gp != null) {
        showCoexpTab = true;
    } 
%>

<script type="text/javascript">
    window.PortalGlobals = {
        getCancerStudyId: function() { return '<%=cancerTypeId%>'},
        getGenes: function() { return '<%=genes%>'},  // raw gene list (as it is entered by the user, it MAY CONTAIN onco query language)
        getGeneListString: function() {  // gene list WITHOUT onco query language
            return '<%=StringUtils.join(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes(), " ")%>'
        },
        getGeneList: function() {
            var _geneList = '<%=StringUtils.join(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes(), " ")%>';
            return _geneList.split(/\s+/);    //Gene Id list without onco query language
        },
        getCaseSetId: function() { return '<%= caseSetId %>';},  //Id for user chosen standard case set
        getCaseSetName: function() { return '<%= caseSetName %>'},  //Name for user chose standard case set
        getCaseIdsKey: function() { return '<%= caseIdsKey %>'; },   //A key arrsigned to use build case set
        getCases: function() { return '<%= cases %>'; }, // list of queried case ids
        getMergedCases: function() { return '<%=  mergedCaseList %>'; },
        getOqlString: (function() {     // raw gene list (as it is entered by the user, it may contain onco query language)
            var oql = '<%=oql%>'
                    .replace("&gt;", ">", "gm")
                    .replace("&lt;", "<", "gm")
                    .replace("&eq;", "=", "gm")
                    .replace(/[\r\n]/g, "\\n");
            return function() { return oql; };
        })(),
        getGeneticProfiles: function() { return '<%=geneticProfiles%>'; },
        getZscoreThreshold: function() { return '<%=zScoreThreshold%>'; },
        getRppaScoreThreshold: function() { return '<%=rppaScoreThreshold%>'; }
    };
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
%>

<!------------------- Duplicate Code ------------------------->
<%
    //////////////from network.jsp
//    String zScoreThesholdStr4Network =
//            xssUtil.getCleanerInput(request.getAttribute(QueryBuilder.Z_SCORE_THRESHOLD).toString());
//    String genes4Network = StringUtils.join((List)request.getAttribute(QueryBuilder.GENE_LIST)," ");
//    String geneticProfileIds4Network = xssUtil.getCleanerInput(StringUtils.join(geneticProfileIdSet," "));
//    String cancerTypeId4Network = xssUtil.getCleanerInput((String)request.getAttribute(QueryBuilder.CANCER_STUDY_ID));
//    String caseIdsKey4Network = xssUtil.getCleanerInput((String)request.getAttribute(QueryBuilder.CASE_IDS_KEY));
//    String caseSetId4Network = xssUtil.getCleanerInput((String)request.getAttribute(QueryBuilder.CASE_SET_ID));

    //////////////from plots_tab.jsp
    String cancer_study_id = request.getParameter("cancer_study_id");
    String case_set_id = request.getParameter("case_set_id");
    String genetic_profile_id = request.getParameter("genetic_profile_id");
    //Translate Onco Query Language
    ArrayList<String> _listOfGenes = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
    String tmpGeneStr = "";
    for(String gene: _listOfGenes) {
        tmpGeneStr += gene + " ";
    }
    tmpGeneStr = tmpGeneStr.trim();

    //////////from protein_exp.jsp
    String cancerStudyId_RPPA =
            (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);

%>
<script type="text/javascript">
    // raw gene list (as it is entered by the user, it may contain onco query language)
    var genes = "<%=genes%>";

    // gene list after being processed by the onco query language parser
    var geneList = "<%=StringUtils.join(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes(), " ")%>";

    // list of samples (case ids)
    var samples = "<%=cases%>";

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

    //////////from protein_exp.jsp
    //var case_set_id = "<//%out.print(case_set_id);%>";
    //case_ids_key = "";
    //if (case_set_id === "-1") {
    //    case_ids_key = "<//%out.print(caseIdsKey);%>";
    //}
</script>


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
<%@ page import="org.mskcc.cbio.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.cgds.model.CaseList" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticAlterationType" %>
<%@ page import="org.mskcc.cbio.cgds.model.Patient" %>
<%@ page import="org.mskcc.cbio.cgds.dao.DaoGeneticProfile" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.lang.reflect.Array" %>
<%@ page import="static org.mskcc.cbio.portal.servlet.QueryBuilder.INTERNAL_EXTENDED_MUTATION_LIST" %>
<%@ page import="org.mskcc.cbio.portal.util.*" %>

<%
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
    String caseIds = xssUtil.getCleanInput(request, QueryBuilder.CASE_IDS);
    ArrayList<CancerStudy> cancerStudies = (ArrayList<CancerStudy>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
    String cancerTypeId = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);


    /**
     * Put together global parameters for injection as javascript variables
     *
     */
    // put geneticProfileIds into the proper form for the JSON request
    String geneticProfiles = StringUtils.join(geneticProfileIdSet.iterator(), " ");
    geneticProfiles = geneticProfiles.trim();

    String caseIdsKey = (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY);

    // get cases
    String cases = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
    cases = StringEscapeUtils.escapeJavaScript(cases);

    ProfileData mergedProfile = (ProfileData)
            request.getAttribute(QueryBuilder.MERGED_PROFILE_DATA_INTERNAL);

    String oql = xssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);
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
    String geneSetChoice = request.getParameter(QueryBuilder.GENE_SET_CHOICE);
    if (geneSetChoice == null) {
        geneSetChoice = "user-defined-list";
    }
    GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
    ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
    Set<String> warningUnion = (Set<String>) request.getAttribute(QueryBuilder.WARNING_UNION);


    ArrayList <GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();
    ArrayList<String> mergedCaseList = mergedProfile.getCaseIdList();

    Config globalConfig = Config.getInstance();
    String siteTitle = SkinUtil.getTitle();
    String bitlyUser = SkinUtil.getBitlyUser();
    String bitlyKey = SkinUtil.getBitlyApiKey();

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

    boolean includeNetworks = SkinUtil.includeNetworks();

    // put gene string into a form that javascript can swallow
    String genes = (String) request.getAttribute(QueryBuilder.RAW_GENE_STR);
    genes = StringEscapeUtils.escapeJavaScript(genes);

    // get cases
    String samples = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
    samples = StringEscapeUtils.escapeJavaScript(samples);
%>

<script>
    window.PortalGlobals = {
        getCases: function() { return '<%= cases %>'; },
        getCaseIdsKey: function() { return '<%= caseIdsKey %>'; },
        getOqlString: (function() {
            var oql = '<%=StringEscapeUtils.escapeJavaScript(oql)%>'
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


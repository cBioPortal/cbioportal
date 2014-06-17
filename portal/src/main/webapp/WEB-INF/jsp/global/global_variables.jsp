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
<%@ page import="org.mskcc.cbio.portal.model.PatientList" %>
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
<%@ page import="org.codehaus.jackson.node.*" %>
<%@ page import="org.codehaus.jackson.JsonNode" %>
<%@ page import="org.codehaus.jackson.JsonParser" %>
<%@ page import="org.codehaus.jackson.JsonFactory" %>
<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>


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

    //Info about Patient Set(s)/Patients
    ArrayList<PatientList> patientSets = (ArrayList<PatientList>)request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    ArrayList<String> mergedPatientList = mergedProfile.getCaseIdList();
    int mergedPatientListSize = mergedPatientList.size();
    String patientSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String patientSetName = "";
    for (PatientList patientSet:  patientSets) {
        if (patientSetId.equals(patientSet.getStableId())) {
            patientSetName = patientSet.getName();
        }
    }
    String patients = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
    //cases = xssUtil.getCleanerInput(cases);
    String patientIdsKey = (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY);
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
    patients = patients.replaceAll("'", "\\'");
    patients = patients.replaceAll("\"", "\\\"");
    patientSetName = patientSetName.replaceAll("'", "\\'");
    patientSetName = patientSetName.replaceAll("\"", "\\\"");

    //check if show co-expression tab
    boolean showCoexpTab = false;
    GeneticProfile final_gp = CoExpUtil.getPreferedGeneticProfile(cancerTypeId);
    if (final_gp != null) {
        showCoexpTab = true;
    } 

    Object patientSampleIdMap = request.getAttribute(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP);
    //Get the patient/sample Id map
    // ObjectMapper mapper = new ObjectMapper();
    // JsonFactory factory = mapper.getFactory();
    // JsonParser parser = factory.createParser(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP);
    // JsonNode patientSampleIdMap = mapper.readTree(parser);

    // String jsonString = "{\"k1\":\"v1\",\"k2\":\"v2\"}";
    // ObjectMapper mapper = new ObjectMapper();
    // JsonNode patientSampleIdMap = mapper.readTree(jsonString);

    // String jsonString = "{\"k1\":\"v1\",\"k2\":\"v2\"}";
    // JsonFactory factory = new JsonFactory();
    // JsonParser parser = factory.createJsonParser(jsonString);
    // JsonNode patientSampleIdMap = mapper.readTree(parser);

    // ObjectMapper mapper = new ObjectMapper(); 
    // String json = mapper.writeValueAsString(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP);
    // JsonNode patientSampleIdMap = mapper.readTree(json);

    //ObjectMapper mapper = new ObjectMapper();
    // JsonFactory factory = new JsonFactory(); // since 2.1 use mapper.getFactory() instead
    // JsonParser jp = factory.createJsonParser("{\"k1\":\"v1\"}");
    // JsonNode patientSampleIdMap = mapper.readTree(jp);

    // ObjectMapper mapper = new ObjectMapper();
    // JsonFactory factory = mapper.getFactory();
    // JsonParser parser = factory.createParser(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP);
    // JsonNode patientSampleIdMap = mapper.readTree(parser);

    //JsonNode patientSampleIdMap = mapper.readTree(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP);
    //JsonNode patientSampleIdMap = mapper.createObjectNode();
    //patientSampleIdMap = mapper.valueToTree(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP);
    //patientSampleIdMap = mapper.convertValue(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP, JsonNode.class);
    //patientSampleIdMap = request.getAttribute(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP);

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
        getCaseSetId: function() { return '<%= patientSetId %>';},  //Id for user chosen standard case set
        getCaseSetName: function() { return '<%= patientSetName %>'},  //Name for user chose standard case set
        getCaseIdsKey: function() { return '<%= patientIdsKey %>'; },   //A key arrsigned to use build case set
        getCases: function() { return '<%= patients %>'; }, // list of queried case ids
        getMergedCases: function() { return '<%=  mergedPatientList %>'; },
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
        getRppaScoreThreshold: function() { return '<%=rppaScoreThreshold%>'; },
        getPatientIds: function() { return '<%=patients%>'; },
        getPatientSampleIdMap: function() { 
            var _tmpPatientSampleIdMap = '<%=patientSampleIdMap%>'; 
            var tmpPatientSampleIdMap = _tmpPatientSampleIdMap.substring(1, _tmpPatientSampleIdMap.length-1);
            var _arrPatientSampleMap = tmpPatientSampleIdMap.split(",");
            var result = {};
            $.each(_arrPatientSampleMap, function(index, obj) {
                var _arr = obj.split("=");
                result[(_arr[0].replace(/\s+/, ""))] = (_arr[1].replace(/\s+/, ""));
            });
            return result;
        }
    };

    //Global Data Objects -- to be re-used
    var PortalDataColl = (function() {
        var oncoprintData = null;
        return {
            setOncoprintData : function(obj) { 
                if (oncoprintData === null) {
                    oncoprintData = obj;    
                }
                PortalDataCollManager.fire("oncoprint-data-fetched");
            },
            getOncoprintData : function() { return oncoprintData; }
        }
    }());

    var PortalDataCollManager = (function() {
        var fns_oncoprint = [];
        var subscribeOncoprint = function(fn){
            fns_oncoprint.push(fn);
        };

        return {
            subscribeOncoprint: subscribeOncoprint,
            fire: function(o) {
                if (o === "oncoprint-data-fetched") {
                    fns_oncoprint.forEach(
                        function(el) {
                            el.call();
                        }
                    );
                }
            }
        }

    }());

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
    // plots_tab.jsp
    String cancer_study_id = request.getParameter("cancer_study_id");
    String patient_set_id = request.getParameter("case_set_id");
    String genetic_profile_id = request.getParameter("genetic_profile_id");

    // Translate Onco Query Language
    ArrayList<String> _listOfGenes = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
    String tmpGeneStr = "";
    for(String gene: _listOfGenes) {
        tmpGeneStr += gene + " ";
    }
    tmpGeneStr = tmpGeneStr.trim();

    // protein_exp.jsp
    String cancerStudyId_RPPA =
            (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);

%>
<script type="text/javascript">
    // raw gene list (as it is entered by the user, it may contain onco query language)
    var genes = "<%=genes%>";

    // gene list after being processed by the onco query language parser
    var geneList = "<%=StringUtils.join(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes(), " ")%>";

    // list of samples (patient ids)
    var samples = "<%=patients%>";

    // genetic profile ids
    var geneticProfiles = "<%=geneticProfiles%>";

    // plots_tab.jsp
    var cancer_study_id = "<%out.print(cancer_study_id);%>",
            patient_set_id = "<%out.print(patient_set_id);%>";
    patient_ids_key = "";
    if (patient_set_id === "-1") {
        patient_ids_key = "<%out.print(patientIdsKey);%>";
    }
    var genetic_profile_id = "<%out.print(genetic_profile_id);%>";
    var gene_list_str = "<%out.print(tmpGeneStr);%>";
    var gene_list = gene_list_str.split(/\s+/);

</script>


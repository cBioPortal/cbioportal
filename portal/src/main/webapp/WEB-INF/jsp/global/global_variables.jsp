<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

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
<%@ page import="org.mskcc.cbio.portal.model.SampleList" %>
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
    GeneticProfile mutationProfile = cancerStudy.getMutationProfile();
    String mutationProfileID = mutationProfile==null ? null : mutationProfile.getStableId();

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
    ArrayList<SampleList> sampleSets = (ArrayList<SampleList>)request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    ArrayList<String> mergedSampleList = mergedProfile.getCaseIdList();
    int mergedSampleListSize = mergedSampleList.size();
    String sampleSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String sampleSetName = "";
    String sampleSetDescription = "";
    for (SampleList sampleSet:  sampleSets) {
        if (sampleSetId.equals(sampleSet.getStableId())) {
            sampleSetName = sampleSet.getName();
            sampleSetDescription = sampleSet.getDescription();
        }
    }
    String samples = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
    //cases = xssUtil.getCleanerInput(cases);
    String sampleIdsKey = (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY);
    //caseIdsKey = xssUtil.getCleanerInput(caseIdsKey);

    //Vision Control Tokens
    boolean showIGVtab = cancerStudy.hasCnaSegmentData();
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

    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Results");

    //Escape quotes in the returned strings
    samples = samples.replaceAll("'", "\\'");
    samples = samples.replaceAll("\"", "\\\"");
    sampleSetName = sampleSetName.replaceAll("'", "\\'");
    sampleSetName = sampleSetName.replaceAll("\"", "\\\"");

    //check if show co-expression tab
    boolean showCoexpTab = false;
    GeneticProfile final_gp = CoExpUtil.getPreferedGeneticProfile(cancerTypeId);
    if (final_gp != null) {
        showCoexpTab = true;
    } 
    Object patientSampleIdMap = request.getAttribute(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP);
    
    String patientCaseSelect = (String)request.getAttribute(QueryBuilder.PATIENT_CASE_SELECT);
    //list of altered & unaltered sample ids
    ArrayList<String> alteredSampleIdList = new ArrayList<String>();
    ArrayList<String> unalteredSampleIdList = new ArrayList<String>();
    for (String patientId : mergedSampleList) {
        if (dataSummary.isCaseAltered(patientId)) {
            alteredSampleIdList.add(patientId);
        } else {
            unalteredSampleIdList.add(patientId);
        }
    }
    String alteredSampleIdsStr = StringUtils.join(alteredSampleIdList, " ");
    String unalteredSampleIdsStr = StringUtils.join(unalteredSampleIdList, " ");

%>

<!--Global Data Objects Manager-->
<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>">
    //needed for data manager
</script>
<script type="text/javascript" src="js/lib/oql/oql-parser.js"></script>
<script type="text/javascript" src="js/api/cbioportal-datamanager.js"></script>
<script type="text/javascript">
    var PortalDataColl = (function() {
        var oncoprintData = null,
            oncoprintStat = null;
        return {
            setOncoprintData : function(obj) { 
                if (oncoprintData === null) {
                    oncoprintData = obj;    
                    PortalDataCollManager.fire("oncoprint-data-fetched");
                }
            },
            setOncoprintStat : function(obj) {
                if (oncoprintStat === null) {
                    oncoprintStat = obj;
                    PortalDataCollManager.fire("oncoprint-stat-fetched");
                }
            },
            getOncoprintData : function() { 
                //TODO: sort the data by sample Id
                return oncoprintData; 
            },
            getOncoprintStat : function() { return oncoprintStat; }
        };
    }());

    var PortalDataCollManager = (function() {
        var fns_oncoprint = [],
            fns_oncoprint_stat = [];
        
        var subscribeOncoprint = function(fn){
            fns_oncoprint.push(fn);
        };

        var subscribeOncoprintStat = function(fn) {
            fns_oncoprint_stat.push(fn);
        };

        return {
            //to subscribe the functions that would re-use oncoprint data -- by subscribing, once the oncoprint
            //data is fetched, the functions would be called/executed. 
            subscribeOncoprint: subscribeOncoprint, 
            subscribeOncoprintStat: subscribeOncoprintStat,
            fire: function(o) {
                if (o === "oncoprint-data-fetched") {
                    fns_oncoprint.forEach(
                        function(el) {
                            el.call();
                        }
                    );
                } else if(o === "oncoprint-stat-fetched") {
                    fns_oncoprint_stat.forEach(
                        function(el) {
                            el.call();
                        }
                    );
                }
            }
        };

    }());
</script>

<!-- Global variables : basic information about the main query -->
<script type="text/javascript">

    var num_total_cases = 0, num_altered_cases = 0;
    var global_gene_data = {}, global_sample_ids = [];
    var patientSampleIdMap = {};
    var patientCaseSelect;

    window.PortalGlobals = {

        //cancer study
        getCancerStudyId: function() { return '<%=cancerTypeId%>'},
        getCancerStudyName: function() { return '<%=cancerStudyName%>'},

        //query genes
        getGenes: function() { return '<%=genes%>'},  // raw gene list (as it is entered by the user, it MAY CONTAIN onco query language)
        getGeneListString: function() { // gene list WITHOUT onco query language
            return '<%=StringUtils.join(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes(), " ")%>'
        },
        getGeneList: function() { //Gene Id list without onco query language
            var _geneList = '<%=StringUtils.join(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes(), " ")%>';
            return _geneList.split(/\s+/);
        },
        getGeneSetName: function() { return '<%=geneSetName%>'; },
        getOqlString: (function() {     // raw gene list (as it is entered by the user, it may contain onco query language)
            var oql = '<%=oql%>'
                    .replace("&gt;", ">", "gm")
                    .replace("&lt;", "<", "gm")
                    .replace("&eq;", "=", "gm")
                    .replace(/[\r\n]/g, "\\n");
            return function() { return oql; };
        })(),

        //cases
        getCaseSetId: function() { return '<%= sampleSetId %>';},  //Id for user chosen standard case set
        getCaseSetName: function() { return '<%= sampleSetName %>'},  //Name for user chose standard case set
        getCaseIdsKey: function() { return '<%= sampleIdsKey %>'; },   //A key arrsigned to use build case set
        getCases: function() { return '<%= samples %>'; }, // list of queried case ids
        getMergedCases: function() { return '<%=  mergedSampleList %>'; },

        //samples
        setSampleIds: function(_inputArr) { global_sample_ids = _inputArr; },
        getSampleIds: function() { return global_sample_ids; },
        
        setPatientSampleIdMap: function(_patientSampleIdMap) {patientSampleIdMap = _patientSampleIdMap;},

    
        getPatientCaseSelect: function() {return '<%=patientCaseSelect%>';},
        
        //samples
        getSampleSetName: function() { return '<%=sampleSetName%>'; },
        getSampleSetDescription: function() {
            var _str = '<%=sampleSetDescription%>';
            _str = _str.substring(0, _str.indexOf("("));
            return _str;
        },
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
        },
        getSampleIds: function() { return '<%=samples%>'; },

        //profiles
        getMutationProfileId: function() { return <%=(mutationProfileID==null?"null":("'"+mutationProfileID+"'"))%>},
        getGeneticProfiles: function() { return '<%=geneticProfiles%>'; },

        //altered vs. unaltered
        getAlteredSampleIdList: function() { return '<%=alteredSampleIdsStr%>'; },
        getUnalteredSampleIdList: function() { return '<%=unalteredSampleIdsStr%>'; },
        getAlteredSampleIdArray: function() {
            var _str = '<%=alteredSampleIdsStr%>';
            return _str.split(/\s+/);

        },
        getUnalteredSampleIdArray: function() {
            var _str = '<%=unalteredSampleIdsStr%>';
            return _str.split(/\s+/);
        },
        getNumOfTotalCases: function() { return num_total_cases; },
        getNumOfAlteredCases: function() { return num_altered_cases; },
        getPercentageOfAlteredCases: function() { return ((num_altered_cases / num_total_cases) * 100).toFixed(1); },

        //thresholds
        getZscoreThreshold: function() { return '<%=zScoreThreshold%>'; },
        getRppaScoreThreshold: function() { return '<%=rppaScoreThreshold%>'; },

        //gene data
        setGeneData: function(_inputObj) { global_gene_data = _inputObj; },
        getGeneData: function() { return global_gene_data; }

    };
    (function setUpQuerySession() {
        var oql_html_conversion_vessel = document.createElement("div");
        oql_html_conversion_vessel.innerHTML = '<%=oql%>'.trim();
        var converted_oql = oql_html_conversion_vessel.textContent.trim();
        window.QuerySession = window.initDatamanager('<%=geneticProfiles%>'.trim().split(/\s+/),
                                                            converted_oql,
                                                            ['<%=cancerTypeId%>'.trim()],
                                                            '<%=samples%>'.trim().split(/\s+/),
                                                            parseFloat('<%=zScoreThreshold%>'),
                                                            parseFloat('<%=rppaScoreThreshold%>'),
                                                            {
                                                                case_set_id: '<%=sampleSetId%>',
                                                                case_ids_key: '<%=sampleIdsKey%>',
                                                                case_set_name: '<%=sampleSetName%>',
                                                                case_set_description: '<%=sampleSetDescription%>'
                                                            },
                                                            ['<%=cancerStudyName%>'],
                                                            {
                                                                mutation_profile_id: <%=(mutationProfileID==null?"null":("'"+mutationProfileID+"'"))%>
                                                            });
    })();
</script>

<script>
//Jiaojiao Dec/21/2015
//The program won't be able to get clicked checkbox elements before they got initialized and displayed. 
//Need to check every 5ms to see if checkboxes are ready or not. 
//If not ready keep waiting, if ready, then scroll to the first selected study

 function waitForElementToDisplay(selector, time) {
        if(document.querySelector(selector) !== null) {
            
           var chosenElements = document.getElementsByClassName('jstree-clicked');
            if(chosenElements.length > 0)
            {
                var treeDiv = document.getElementById('jstree');
                var topPos = chosenElements[0].offsetTop;
                var originalPos = treeDiv.offsetTop;
                treeDiv.scrollTop = topPos - originalPos;
            }
           
            return;
        }
        else {
            setTimeout(function() {
                waitForElementToDisplay(selector, time);
            }, time);
        }
    }
    
$(document).ready(function() {
    $.when(window.QuerySession.getAlteredSamples(), window.QuerySession.getUnalteredSamples(), window.QuerySession.getPatientSampleIdMap()).then(function(altered_samples, unaltered_samples, sample_patient_map) {
        PortalDataCollManager.subscribeOncoprint(function() {

            //calculate total alteration
            var _dataArr = PortalDataColl.getOncoprintData();
            num_total_cases = _dataArr.length;
            $.each(_dataArr, function(outerIndex, outerObj) {
                $.each(outerObj.values, function(innerIndex, innerObj) {
                    if(Object.keys(innerObj).length > 2) { // has more than 2 fields -- indicates existence of alteration
                        num_altered_cases += 1;
                        return false;
                    }
                });
            });     

            var _sampleIds = window.QuerySession.getSampleIds();
            window.PortalGlobals.setSampleIds(_sampleIds);
            
            var altered_samples_percentage = (100 * altered_samples.length / _sampleIds.length).toFixed(1);

            //Configure the summary line of alteration statstics
            var _stat_smry = "<h3 style='color:#686868;font-size:14px;'>Gene Set / Pathway is altered in <b>" + altered_samples.length + " (" + altered_samples_percentage + "%)" + "</b> of queried samples</h3>";
            $("#main_smry_stat_div").append(_stat_smry);

            //Configure the summary line of query
            var _query_smry = "<h3 style='font-size:110%;'><a href='study.do?cancer_study_id=" + 
                window.QuerySession.getCancerStudyIds()[0] + "' target='_blank'>" + 
                window.QuerySession.getCancerStudyNames()[0] + "</a><br>" + " " +  
                "<small>" + window.QuerySession.getSampleSetName() + " (<b>" + _sampleIds.length + "</b> samples)" + " / " + 
                "<b>" + window.QuerySession.getQueryGenes().length + "</b>" + " Gene" + (window.QuerySession.getQueryGenes().length===1 ? "" : "s") + "<br></small></h3>"; 
            $("#main_smry_query_div").append(_query_smry);

            //Append the modify query button
            var _modify_query_btn = "<button type='button' class='btn btn-primary' data-toggle='button' id='modify_query_btn'>Modify Query</button>";
            $("#main_smry_modify_query_btn").append(_modify_query_btn);

            //Set Event listener for the modify query button (expand the hidden form)
            $("#modify_query_btn").click(function () {
                $("#query_form_on_results_page").toggle();
                if($("#modify_query_btn").hasClass("active")) {
                    $("#modify_query_btn").removeClass("active");
                } else {
                    $("#modify_query_btn").addClass("active");    
                }
                 waitForElementToDisplay('.jstree-clicked', '5');
            });
            $("#toggle_query_form").click(function(event) {
                event.preventDefault();
                $('#query_form_on_results_page').toggle();
                //  Toggle the icons
                $(".query-toggle").toggle();
            });

            var uniqStrings = function(arr_of_strings) {
                var uniq = [];
                var seen = {};
                for (var i=0; i<arr_of_strings.length; i++) {
                    var str = arr_of_strings[i];
                    if (!seen[str]) {
                        uniq.push(str);
                        seen[str] = true;
                    }
                }
                return uniq;
            };
            var patientIdArray = uniqStrings(_sampleIds.map(function(s) { return sample_patient_map[s]; }));

            //Oncoprint summary lines
            $("#oncoprint_sample_set_description").append(window.QuerySession.getSampleSetDescription() + 
                "("+patientIdArray.length + " patients / " + _sampleIds.length + " samples)");
            $("#oncoprint_sample_set_name").append(window.QuerySession.getSampleSetName());
            $("#oncoprint_num_of_altered_cases").append(altered_samples.length);
            $("#oncoprint_percentage_of_altered_cases").append(altered_samples_percentage);
            if (patientIdArray.length !== _sampleIds.length) {
                $("#switchPatientSample").show();
            }
            
        });
   
         
        $("#toggle_query_form").click(function(event) {
            event.preventDefault();
            $('#query_form_on_results_page').toggle();
            //  Toggle the icons
            $(".query-toggle").toggle();
        });

        });
    });


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
    String sample_set_id = request.getParameter("case_set_id");
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
    var samples = "<%=samples%>";

    // genetic profile ids
    var geneticProfiles = "<%=geneticProfiles%>";

    // plots_tab.jsp
    var cancer_study_id = "<%out.print(cancer_study_id);%>",
            sample_set_id = "<%out.print(sample_set_id);%>";
    sample_ids_key = "";
    if (sample_set_id === "-1") {
        sample_ids_key = "<%out.print(sampleIdsKey);%>";
    }
    var genetic_profile_id = "<%out.print(genetic_profile_id);%>";
    var gene_list_str = "<%out.print(tmpGeneStr);%>";
    var gene_list = gene_list_str.split(/\s+/);

</script>

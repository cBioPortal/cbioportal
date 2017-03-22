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
    String geneticProfiles = StringUtils.join(geneticProfileIdSet.iterator(), " ");
    geneticProfiles = xssUtil.getCleanerInput(geneticProfiles.trim());

    //Info about threshold settings
    double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
    double rppaScoreThreshold = ZScoreUtil.getRPPAScore(request);

    //Onco Query Language Parser Instance
    String oql = request.getParameter(QueryBuilder.GENE_LIST);
    if (request instanceof XssRequestWrapper) {
        oql = ((XssRequestWrapper)request).getRawParameter(QueryBuilder.GENE_LIST);
    }
    oql = xssUtil.getCleanerInput(oql);

    //Info about queried cancer study
    ArrayList<CancerStudy> cancerStudies = (ArrayList<CancerStudy>)request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
    String cancerStudyId = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);
    CancerStudy cancerStudy = cancerStudies.get(0);
    for (CancerStudy cs : cancerStudies){
        if (cancerStudyId.equals(cs.getCancerStudyStableId())){
            cancerStudy = cs;
            break;
        }
    }
    String cancerStudyName = cancerStudy.getName(); 
    GeneticProfile mutationProfile = cancerStudy.getMutationProfile();
    String mutationProfileID = mutationProfile==null ? null : mutationProfile.getStableId();

    //Info about Patient Set(s)/Patients
    ArrayList<SampleList> sampleSets = (ArrayList<SampleList>)request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String studySampleMapJson = (String)request.getAttribute("STUDY_SAMPLE_MAP");
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
    String sampleIdsKey = (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY);

    //Vision Control Tokens
    boolean showIGVtab = cancerStudy.hasCnaSegmentData();
    boolean has_mrna = countProfiles(profileList, GeneticAlterationType.MRNA_EXPRESSION) > 0;
    boolean has_methylation = countProfiles(profileList, GeneticAlterationType.METHYLATION) > 0;
    boolean has_copy_no = countProfiles(profileList, GeneticAlterationType.COPY_NUMBER_ALTERATION) > 0;
    boolean has_survival = cancerStudy.hasSurvivalData();
    boolean includeNetworks = GlobalProperties.includeNetworks();
    boolean computeLogOddsRatio = true;
    Boolean mutationDetailLimitReached = (Boolean)request.getAttribute(QueryBuilder.MUTATION_DETAIL_LIMIT_REACHED);

    //are we using session service for bookmarking?
    boolean useSessionServiceBookmark = !StringUtils.isBlank(GlobalProperties.getSessionServiceUrl());

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
    GeneticProfile final_gp = CoExpUtil.getPreferedGeneticProfile(cancerStudyId);
    if (final_gp != null) {
        showCoexpTab = true;
    } 
    Object patientSampleIdMap = request.getAttribute(QueryBuilder.SELECTED_PATIENT_SAMPLE_ID_MAP);
    
    String patientCaseSelect = (String)request.getAttribute(QueryBuilder.PATIENT_CASE_SELECT);

%>

<!--Global Data Objects Manager-->
<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>">
    //needed for data manager
</script>
<script type="text/javascript" src="js/lib/oql/oql-parser.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/api/cbioportal-datamanager.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/oql/oqlfilter.js?<%=GlobalProperties.getAppVersion()%>"></script>

<!-- Global variables : basic information about the main query -->
<script type="text/javascript">

    var num_total_cases = 0, num_altered_cases = 0;
    var global_gene_data = {}, global_sample_ids = [];
    var patientSampleIdMap = {};
    var patientCaseSelect;

    window.PortalGlobals = {
        setPatientSampleIdMap: function(_patientSampleIdMap) {patientSampleIdMap = _patientSampleIdMap;},
    };
    
    (function setUpQuerySession() {
        var oql_html_conversion_vessel = document.createElement("div");
        oql_html_conversion_vessel.innerHTML = '<%=oql%>'.trim();
        var converted_oql = oql_html_conversion_vessel.textContent.trim();
        window.QuerySession = window.initDatamanager('<%=geneticProfiles%>'.trim().split(/\s+/),
                                                            converted_oql,
                                                            ['<%=cancerStudyId%>'.trim()],
                                                            JSON.parse('<%=studySampleMapJson%>'),
                                                            parseFloat('<%=zScoreThreshold%>'),
                                                            parseFloat('<%=rppaScoreThreshold%>'),
                                                            {
                                                                case_set_id: '<%=sampleSetId%>',
                                                                case_ids_key: '<%=sampleIdsKey%>',
                                                                case_set_name: '<%=sampleSetName%>',
                                                                case_set_description: '<%=sampleSetDescription%>'
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
    $.when(window.QuerySession.getAlteredSamples(), window.QuerySession.getPatientIds(), window.QuerySession.getCancerStudyNames()).then(function(altered_samples, patient_ids, cancer_study_names) {
            var sample_ids = window.QuerySession.getSampleIds();
            
            var altered_samples_percentage = (100 * altered_samples.length / sample_ids.length).toFixed(1);

            //Configure the summary line of alteration statstics
            var _stat_smry = "<h3 style='color:#686868;font-size:14px;'>Gene Set / Pathway is altered in <b>" + altered_samples.length + " (" + altered_samples_percentage + "%)" + "</b> of queried samples</h3>";
            $("#main_smry_stat_div").append(_stat_smry);

            //Configure the summary line of query
            var _query_smry = "<h3 style='font-size:110%;'><a href='study?id=" + 
                window.QuerySession.getCancerStudyIds()[0] + "' target='_blank'>" + 
                cancer_study_names[0] + "</a><br>" + " " +  
                "<small>" + window.QuerySession.getSampleSetName() + " (<b>" + sample_ids.length + "</b> samples)" + " / " + 
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
            //Oncoprint summary lines
            $("#oncoprint_sample_set_description").append("Case Set: " + window.QuerySession.getSampleSetName()
							+ " "
							+ "("+patient_ids.length + " patients / " + sample_ids.length + " samples)");
            $("#oncoprint_sample_set_name").append("Case Set: "+window.QuerySession.getSampleSetName());
            if (patient_ids.length !== sample_ids.length) {
                $("#switchPatientSample").css("display", "inline-block");
            }
            
        });
   
         
        $("#toggle_query_form").click(function(event) {
            event.preventDefault();
            $('#query_form_on_results_page').toggle();
            //  Toggle the icons
            $(".query-toggle").toggle();
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

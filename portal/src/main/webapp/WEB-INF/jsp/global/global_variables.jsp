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

<%@ include file="selected_study_variables.jsp" %>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.cbio.portal.model.*" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.*" %>
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
<%@ page import="org.mskcc.cbio.portal.dao.DaoMutation" %>

<%
    //Security Instance
    ServletXssUtil xssUtil = ServletXssUtil.getInstance();

    //Info about Genetic Profiles
    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS);
    String geneticProfiles = StringUtils.join(geneticProfileIdSet.iterator(), " ");
    geneticProfiles = xssUtil.getCleanerInput(geneticProfiles.trim());

    //Info about threshold settings
    double zScoreThreshold = Double.parseDouble(String.valueOf(request.getAttribute(QueryBuilder.Z_SCORE_THRESHOLD)));
    double rppaScoreThreshold = Double.parseDouble(String.valueOf(request.getAttribute(QueryBuilder.RPPA_SCORE_THRESHOLD)));

    //Onco Query Language Parser Instance
    String oql = request.getParameter(QueryBuilder.GENE_LIST);
    if (request instanceof XssRequestWrapper) {
        oql = ((XssRequestWrapper)request).getRawParameter(QueryBuilder.GENE_LIST);
    }
    oql = xssUtil.getCleanerInput(oql);

    String studySampleMapJson = (String)request.getAttribute("STUDY_SAMPLE_MAP");
    String sampleSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String sampleSetName = request.getAttribute("case_set_name") != null ? (String) request.getAttribute("case_set_name") : "User-defined Patient List";
    String sampleSetDescription = request.getAttribute("case_set_description") != null ? (String) request.getAttribute("case_set_description") : "User-defined Patient List.";
    String sampleIdsKey = request.getAttribute(QueryBuilder.CASE_IDS_KEY) != null ? (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY) : "";

    Boolean showIGVtab = (Boolean) request.getAttribute("showIGVtab");
    Boolean has_mrna = (Boolean) request.getAttribute("hasMrna");
    Boolean has_methylation = (Boolean) request.getAttribute("hasMethylation");
    Boolean has_copy_no = (Boolean) request.getAttribute("hasCopyNo");
    Boolean has_survival = (Boolean) request.getAttribute("hasSurvival");
    boolean includeNetworks = GlobalProperties.includeNetworks();
    boolean computeLogOddsRatio = true;
    Boolean mutationDetailLimitReached = (Boolean)request.getAttribute(QueryBuilder.MUTATION_DETAIL_LIMIT_REACHED);
    boolean showCoexpTab = false;

    //are we using session service for bookmarking?
    boolean useSessionServiceBookmark = !StringUtils.isBlank(GlobalProperties.getSessionServiceUrl());

    //General site info
    String siteTitle = GlobalProperties.getTitle();

    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Results");

    sampleSetName = sampleSetName.replaceAll("'", "\\'");
    sampleSetName = sampleSetName.replaceAll("\"", "\\\"");

    //check if show co-expression tab
    if(!isVirtualStudy){
        GeneticProfile final_gp = CoExpUtil.getPreferedGeneticProfile(cancerStudyId);
        if (final_gp != null) {
            showCoexpTab = true;
        }
    }        
    String sessionServiceUrl = (GlobalProperties.getSessionServiceUrl() == null) ? "" : GlobalProperties.getSessionServiceUrl();
%>

<!--Global Data Objects Manager-->
<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>">
    //needed for data manager
</script>
<script type="text/javascript" src="js/lib/oql/oql-parser.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/api/HotspotSet.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/api/cbioportal-datamanager.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/oql/oqlfilter.js?<%=GlobalProperties.getAppVersion()%>"></script>

<!-- Global variables : basic information about the main query -->
<script type="text/javascript">

    var patientSampleIdMap = {};
    window.PortalGlobals = {
        setPatientSampleIdMap: function(_patientSampleIdMap) {patientSampleIdMap = _patientSampleIdMap;},
    };

    (function setUpQuerySession() {
        var oql_html_conversion_vessel = document.createElement("div");
        oql_html_conversion_vessel.innerHTML = '<%=oql%>'.trim();
        var converted_oql = oql_html_conversion_vessel.textContent.trim();
        var studySampleObj = JSON.parse('<%=studySampleMapJson%>');
        var studyIdsList = Object.keys(studySampleObj);
        window.QuerySession = window.initDatamanager('<%=geneticProfiles%>'.trim().split(/\s+/),
                                                            converted_oql,
							    studyIdsList,
							    studySampleObj,
							    parseFloat('<%=zScoreThreshold%>'),
							    parseFloat('<%=rppaScoreThreshold%>'),
							    {
								case_set_id: '<%=sampleSetId%>',
								case_ids_key: '<%=sampleIdsKey%>',
								case_set_name: '<%=sampleSetName%>',
								case_set_description: '<%=sampleSetDescription%>'
							    }, <%=GlobalProperties.enableDriverAnnotations()%>,
                                                            <%=GlobalProperties.showBinaryCustomDriverAnnotation()%>,
                                                            <%=DaoMutation.hasDriverAnnotations(normalizedCancerStudyIdListStr)%>,
                                                            <%=DaoMutation.numTiers(normalizedCancerStudyIdListStr)%>,
                                                            '<%=GlobalProperties.enableOncoKBandHotspots()%>',
                                                            <%=GlobalProperties.showTiersCustomDriverAnnotation()%>,
                                                            <%=GlobalProperties.enableTiers()%>,
                                                            <%=GlobalProperties.hidePassengerMutations()%>);
    })();


</script>

<script>

    $(document).ready(function() {
        window.sessionServiceUrl = '<%=sessionServiceUrl%>';
        if(window.sessionServiceUrl) {
            window.sessionServiceUrl = window.sessionServiceUrl.replace(/http(s)*:\/+/, '');
        }
        var getCohortName = function(){
            var def = new $.Deferred();
            if (window.isVirtualStudy) {
                if (window.cohortIdsList.length === 1) {
                    $.ajax({
                        method: 'GET',
                        url: 'proxy/' + window.sessionServiceUrl + 'virtual_study/' + window.cohortIdsList[0]
                    }).done(function(response){
                        response = JSON.parse(response);
                        def.resolve(response['data']['name']);
                    }).fail(function () {
                        def.resolve([]);
                    });                    
                } else {
                    def.resolve([]);
                }
            } else {
                window.QuerySession.getCancerStudyNames().then(function (_studies) {
                    def.resolve(_studies[0]);
                }).fail(function () {
                    def.resolve([]);
                });
            }
            return def.promise();
        }
        
        window.goToStudySummary = function() {
            // try to use formSubmit, it exists if the new frontend code has been loaded
            // if not, just navigate in the standard way
            var cohortsParam = window.cohortIdsList.join(",");
            if (window.formSubmit) {
                window.formSubmit("study", {id:cohortsParam}, "_blank", (cohortsParam.length > 1800 ? "post" : "get"));
            } else {
                window.open("study?id="+cohortsParam, "_blank");
            }
        };

        $.when(window.QuerySession.getAlteredSamples(), window.QuerySession.getStudyPatientMap(), getCohortName()).then(function(altered_samples, studyPatientMap, studyName) {

            // alteration statistics summary line to the right     
            var sampleLength = 0;
            var sample_ids = window.QuerySession.getSampleIds();
            var altered_samples_percentage = (100 * altered_samples.length / sample_ids.length).toFixed(1);
            $.each(window.QuerySession.getStudySampleMap(), function(studyId,cases){
                sampleLength += cases.length;
            })
            var patientLength = 0;
            $.each(studyPatientMap, function(studyId,cases){
                patientLength += cases.length;
            })
            var altered_samples_percentage = (100 * altered_samples.length / sampleLength).toFixed(1);
            var _stat_smry = "<h3 style='color:#686868;font-size:14px;'>Gene Set / Pathway is altered in <b>" + altered_samples.length + " (" + altered_samples_percentage + "%)" + "</b> of queried samples</h3>";
            $("#main_smry_stat_div").append(_stat_smry);

            // Query summary line to the left
            var cohortDisplayName = "";
            if (window.cohortIdsList.length > 1) {
                cohortDisplayName = "Selected Studies";
            } else {
                cohortDisplayName = studyName;
            }
            var _query_smry = "<h3 style='font-size:110%;'><a onclick='goToStudySummary()' style='cursor:pointer;'>" + cohortDisplayName + "</a><br>" +
                "<small>" + window.QuerySession.getSampleSetName() + " (<b>" + sample_ids.length + "</b> samples)" + " / " +
                "<b>" + window.QuerySession.getQueryGenes().length + "</b>" + " Gene" + (window.QuerySession.getQueryGenes().length===1 ? "" : "s") + "<br></small></h3>";
            $("#main_smry_query_div").append(_query_smry);

            // "Modify Query" button
            var _tmpVCinserted = false; // flag to indicate if tmp VC is inserted to jsTree
            var _modify_query_btn = "<button type='button' class='btn btn-primary' data-toggle='button' id='modify_query_btn'>Modify Query</button>";
            $("#main_smry_modify_query_btn").append(_modify_query_btn);
            $("#modify_query_btn").click(function () {
                $("#query_form_on_results_page").toggle();
                if($("#modify_query_btn").hasClass("active")) {
                    $("#modify_query_btn").removeClass("active");
                } else {
                    $("#modify_query_btn").addClass("active");
                }
                //_setJsTreeSelection();
                if (window.cohortIdsList.length === 1 && window.isVirtualStudy) { // check if this is a temporary cohort
                    var _vcs = JSON.parse(localStorage.getItem('virtual-cohorts'));
                    var _vcIds = _.pluck(_vcs, 'virtualCohortID');
                    if (!_.contains(_vcIds, window.cohortIdsList[0])) { // is temporary cohort
                        // insert node for temporary cohort and set selection
                        if (!_tmpVCinserted) {
                            var _treeLoadTimer = setInterval(inspectTree, 1000);
                            function inspectTree() {
                                var _elem = document.getElementsByClassName('jstree');
                                if (_elem.length > 0) {
                                    clearInterval(_treeLoadTimer);
                                    if ($("#virtual-study-group").length === 0) {
                                        $("#jstree").jstree().create_node($("#tissue_anchor"), {id: "virtual-study-group", text: "Virtual Study"}, "inside", null);
                                    }
                                    $('#jstree').jstree().create_node($("#virtual-study-group"), {
                                        'id': window.cohortIdsList[0],
                                        'text': studyName,
                                        'li_attr': { description: "Temporary cohort from previous selection" }
                                    }, "last", null);
                                    $("#jstree").jstree("select_node", "#" + window.cohortIdsList[0]);
                                    _tmpVCinserted = true;
                                }
                            }
                        }
                    }
                }
                _setJsTreeSelection();
            });

            function _setJsTreeSelection() {
                var _timer = setInterval(inspectTreeSel, 1000);
                function inspectTreeSel() {
                    if (document.querySelector(".jstree-clicked") !== null) {
                        var _elems = document.getElementsByClassName('jstree-clicked');
                        if (_elems.length > 0) {
                            clearInterval(_timer);
                            _.each(window.cohortIdsList, function(_cid) {
                                $("#jstree").jstree("select_node", "#" + _cid);
                            });
                            var treeDiv = document.getElementById('jstree');
                            var topPos = _elems[0].offsetTop;
                            var originalPos = treeDiv.offsetTop;
                            treeDiv.scrollTop = topPos - originalPos;
                        }
                    }
                }
            }

            $("#toggle_query_form").click(function(event) {
                event.preventDefault();
                $('#query_form_on_results_page').toggle();
                //  Toggle the icons
                $(".query-toggle").toggle();
            });

            //Oncoprint summary lines
            $("#oncoprint_sample_set_description").append("Case Set: " + window.QuerySession.getSampleSetName()
                + " "
                + "("+patientLength + " patients / " + sampleLength + " samples)");
            $("#oncoprint_sample_set_name").append("Case Set: "+ window.QuerySession.getSampleSetName());
            if (patientLength !== sampleLength) {
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

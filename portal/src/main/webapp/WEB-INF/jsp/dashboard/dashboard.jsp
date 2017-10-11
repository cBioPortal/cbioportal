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

<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="org.json.simple.JSONValue" %>
<%@ page import="org.mskcc.cbio.portal.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.servlet.*" %>
<%@ page import="java.util.List" %>
<%@page import="java.util.Set"%>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%
    request.setAttribute("standard-js-css", true);
    String isDemoMode = request.getParameter("demo");
    boolean showPlaceHoder;
    if (isDemoMode!=null) {
        showPlaceHoder = isDemoMode.equalsIgnoreCase("on");
    } else {
        showPlaceHoder = GlobalProperties.showPlaceholderInPatientView();
    }

    String reqCohortIds = StringUtils.join((Set<String>)request.getAttribute(CancerStudyView.ID),",");
    String studySampleMap = (String)request.getAttribute(CancerStudyView.STUDY_SAMPLE_MAP);
    String cancerStudyViewError = (String)request.getAttribute(CancerStudyView.ERROR);

    if (cancerStudyViewError!=null) {
        out.print(cancerStudyViewError);
    } else {
%>
<span class="studyContainer">
<jsp:include page="../global/header.jsp" flush="true" />

<table width="100%" id="show_study_details" style="margin: 8px 2px 5px 2px;display:none;">
    <tr>
        <td class="study-view-header-first-row-td">
            <b><u id="study_name"></u></b>
            <form method="post" action="index.do">
                <input type="hidden" id="cancer_study_id" name="cancer_study_id">
                <input type="hidden" id="cancer_study_list">
                <input type="button" id="submit_button" value="Query this study" class="btn btn-primary btn-xs">
            </form>
            <form id="study-view-header-download-all-data" method="get" action="">
                <input type="hidden" name="raw" value="ture">
                <button class="btn btn-default btn-xs">Download data</button>
            </form>
        </td>
    </tr>
    <tr>
        <td id="study_desc"></td>
    </tr>
</table>


<div id="study-tabs">
    <ul>
        <li id="li-1"><a href='#summary' id='study-tab-summary-a' class='study-tab' title='Study Summary'>Study Summary</a></li>
        <!--<li><a href='#clinical-plots' class='study-tab' title='DC Plots'>Study Summary</a></li>-->
        <li><a href='#clinical' id='study-tab-clinical-a' class='study-tab' title='Clinical Data'>Clinical Data</a></li>
    </ul>

    <div class="study-section" id="summary">
        <%@ include file="dcplots.jsp" %>
    </div>

    <div class="study-section" id="clinical">
        <%@ include file="clinical.jsp" %>
    </div>

    <div class="study-section" id="mutations" style="display:none;">
        <%@ include file="mutations.jsp" %>
    </div>

    <div class="study-section" id="cna"  style="display:none;">
        <%@ include file="cna.jsp" %>
    </div>

</div>
<%
    }
%>
</div>
</td>
</tr>

<tr>
    <td colspan="3">
        <jsp:include page="../global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<span id="ruler"></span>
<jsp:include page="../global/xdebug.jsp" flush="true" />


<script type="text/javascript">
    document.title = 'Summary'
    $("#study-tabs-loading-wait").css('display', 'inline-block');

</script>

<style type="text/css">
    @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/bootstrap-chzn.css?<%=GlobalProperties.getAppVersion()%>";
    .ColVis {
        float: left;
        margin-bottom: 0
    }
    .dataTables_length {
        width: auto;
        float: right;
    }
    .dataTables_info {
        clear: none;
        width: auto;
        float: right;
    }
    .dataTables_filter {
        width: 40%;
    }
    .div.datatable-paging {
        width: auto;
        float: right;
    }
    .data-table-name {
        float: left;
        font-weight: bold;
        font-size: 120%;
        vertical-align: middle;
    }
    .study-view-header-first-row-td>* {
        float: left;
        margin-right: 5px;
    }
    #study-view-header-download-all-data {
        display: none;
    }
    #study-tabs>ul{
        margin-right: 7px;
    }
    #iviz-header-left-1:disabled {
        background: #dddddd !important;
        cursor:not-allowed !important;
    }
</style>

<script src="js/src/dashboard/iviz-vendor.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/model/dataProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/api/cbioportal-client.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script src="js/lib/jquery.tipTip.minified.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/mailme.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/jquery-ui.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/FileSaver.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/bootstrap-dropdown-checkbox.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/ZeroClipboard.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/EnhancedFixedDatatable.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/MutatedGeneCNATable.js?<%=GlobalProperties.getAppVersion()%>"></script>

<link rel="stylesheet" href="css/bootstrap-dropdown-checkbox.css?<%=GlobalProperties.getAppVersion()%>"/>
<link rel="stylesheet" href="css/fixed-data-table.min.css?<%=GlobalProperties.getAppVersion()%>"/>
<link rel="stylesheet" href="css/study-view.css?<%=GlobalProperties.getAppVersion()%>"/>
<link rel="stylesheet" href="css/dashboard/iviz-vendor.css?<%=GlobalProperties.getAppVersion()%>"/>
<link rel="stylesheet" href="css/dashboard/iviz.css?<%=GlobalProperties.getAppVersion()%>"/>

<script src="js/src/dashboard/model/StudyViewProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/controller/StudyViewParams.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/controller/StudyViewClinicalTabController.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/controller/StudyViewMutationsTabController.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/controller/StudyViewCNATabController.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/view/StudyViewInitClinicalTab.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/view/StudyViewInitMutationsTab.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/view/StudyViewInitCNATab.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/iviz.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/cbio-util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/download-util.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">
    var username = $('#header_bar_table span').text()||'';
    var studyCasesMap = '<%=studySampleMap%>';
    studyCasesMapTemp = JSON.parse(studyCasesMap);
    studyCasesMap = {};
    _.each(studyCasesMapTemp,function(casesList,studyId){
        studyCasesMap[studyId] = {};
        studyCasesMap[studyId].samples = casesList;
    });
    var cohortIdsList = '<%=reqCohortIds%>';
    cohortIdsList = cohortIdsList.split(',');
    var appVersion = '<%=GlobalProperties.getAppVersion()%>';
    var hasMutation = false;
    var hasCnaSegmentData = false;
    var cnaProfileId = '';
    var mutationProfileId = '';
    var appendMutationTab = function(){
        hasMutation = true;
        $("#study-tabs ul").append("<li><a href='#mutations' id='study-tab-mutations-a' class='study-tab' title='Mutations'>Mutated Genes</a></li>");
        $('#study-tabs > #mutations').css('display','block');
        $('#study-tab-mutations-a').click(function(){
            if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
                StudyViewMutationsTabController.init(function() {
                    $(this).addClass("tab-clicked");
                    StudyViewMutationsTabController.getDataTable().fnAdjustColumnSizing();
                });
            }
            window.location.hash = '#mutations';
        });
    }
    var appendCnaTab = function(){
        hasCnaSegmentData = true;
        $("#study-tabs ul").append("<li><a href='#cna' id='study-tab-cna-a' class='study-tab' title='Copy Number Alterations'>Copy Number Alterations</a></li>");
        $('#study-tabs > #cna').css('display','block');
        $('#study-tab-cna-a').click(function(){
            if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
                StudyViewCNATabController.init(function() {
                    $(this).addClass("tab-clicked");
                    StudyViewCNATabController.getDataTable().fnAdjustColumnSizing();
                });
            }
            window.location.hash = '#cna';
        });
    }

    $(document).ready(function () {
        //this is for testing, once done this should be commented/deleted
        //window.cbioURL = '';
        window.iviz = {};
        //commented for thesing
        window.cbioURL = window.location.origin + window.location.pathname.substring(0, window.location.pathname.indexOf("/",2)) + '/';
        window.cbioResourceURL = 'js/src/dashboard/resources/';
        window.iviz.datamanager = new DataManagerForIviz.init(window.cbioURL, studyCasesMap);

        $.when(window.cbioportal_client.getStudies({ study_ids: cohortIdsList}), window.iviz.datamanager.getGeneticProfiles())
            .then(function(_cancerStudies, _geneticProfiles){
                if(cohortIdsList.length === 1){
                    if(_cancerStudies.length === 1){
                        $("#show_study_details").css('display','block');
                        var _cancerStudy = _cancerStudies[0]
                        document.title = _cancerStudy.name
                        $("#study_name").html(_cancerStudy.name);
                        $("#cancer_study_id").val(_cancerStudy.id);
                        $("#cancer_study_list").val(_cancerStudy.id);
                        var _desc = _cancerStudy.description;
                        if(_cancerStudy.pmid !== null){
                            _desc += '&nbsp;<a href="http://www.ncbi.nlm.nih.gov/pubmed/'+_cancerStudy.pmid+'">PubMed</a>';
                        }
                        $("#study_desc").html(_desc);

                        var _mutationProfiles = _.filter(_geneticProfiles, function (_profile) {
                            return _profile.study_id + '_mutations' === _profile.id;
                        });
                        if(_mutationProfiles.length>0){
                            appendMutationTab();
                        }
                        var _cnaProfiles = _.filter(_geneticProfiles, function (_profile) {
                            return _profile.study_id + '_gistic' === _profile.id;
                        });
                        if(_cnaProfiles.length>0){
                            appendCnaTab();
                        }

                        // TODO changed mutationProfileId to mutationProfileIds when mutations tab support multi-studies
                        StudyViewParams.params = {
                            studyId: _cancerStudy.id,
                            mutationProfileId: _mutationProfiles.length>0?_mutationProfiles[0].id:'',
                            hasMutSig: hasMutation,
                            cnaProfileId: _cnaProfiles.length>0?_cnaProfiles[0].id:''
                        };
                        window.mutationProfileId = StudyViewParams.params.mutationProfileId ;
                        window.cnaProfileId = StudyViewParams.params.cnaProfileId;
                        window.case_set_id = -1;

                    }else{
                        // TODO : Right now we are just showing the cohort name and description for virtual cohort.
                        // in future we the other visualizations support virtual cohort the update this to 
                        // show submit button
                        if (vcSession.URL !== undefined) {
                            $.ajax({
                                method: 'GET',
                                url: vcSession.URL + '/' + cohortIdsList[0]
                            }).done(function(response){
                                if (typeof response === 'string') {
                                    response = JSON.parse(response); 
                                }
                                $("#show_study_details").css('display','block');
                                $("#study_name").html(response['data']['studyName']);
                                $("#study_desc").html(response['data']['description']);
                                //$("#submit_button").css('display','none');
                                $("#cancer_study_list").val(cohortIdsList[0]);
                                var studyName = response['data']['studyName'];
                                document.title = studyName?studyName:'Summary';
                            });
                        }
                    }
                    $("#submit_button").click(function(){
                        iViz.submitForm(true);
                    });
                } else if (cohortIdsList.length >= 2) {
                    var study_name = 'Combined Studies';
                    var study_description = 'Total ' + cohortIdsList.length + ' studies.';
                    var collapse_study_name = _cancerStudies.map(function(study) { 
                        // Remove html tags in study.description in case title of <a> not work 
                        return '<a href="' + window.cbioURL + 'study?id=' + study.id + '" title="' + 
                            study.description.replace(/(<([^>]+)>)/ig, '') + '" target="_blank">' + 
                            study.name + '</a>'; }).join("<br />");
                    
                    study_description += '<span class="truncated"><br />' + collapse_study_name + '</span>';
                    
                    $("#show_study_details").css('display','block');
                    $("#study_name").append(study_name);
                    $("#study_desc ").append(study_description);

                    $('.truncated').hide()                       // Hide the text initially
                        .after('<i class="fa fa-plus-circle" aria-hidden="true"></i>') // Create toggle button
                        .next().on('click', function(){          // Attach behavior
                        $(this).toggleClass("fa-minus-circle")   // Swap the icon
                            .prev().toggle();                    // Hide/show the text
                    });
                }

                $("#study-tabs").tabs({disabled: true});
                $('#study-tab-summary-a').click(function () {
                    if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
                        $("#study-tabs-loading-wait").css('display', 'none');
                        if(!_.isObject(window.iviz.datamanager.initialSetupResult)) {
                            $.when(window.iviz.datamanager.initialSetup(), window.iviz.datamanager.getConfigs() ).then(function(_data, configs){
                                var opts = {};
                                if(_.isObject(configs)) {
                                    opts = configs;
                                }
                                initdcplots(_data, opts);
                            });
                        }else {
                            $.when(window.iviz.datamanager.getConfigs()).then(function(configs){
                                var opts = {};
                                if(_.isObject(configs)) {
                                    opts = configs;
                                }
                                initdcplots(window.iviz.datamanager.initialSetupResult, opts);
                            });
                        }
                        $('#study-tab-summary-a').addClass("tab-clicked");
                    }
                    window.location.hash = '#summary';
                });
                $('#study-tab-clinical-a').click(function(){
                    if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
                        //First time: adjust the width of data table;
                        $("#clinical-data-table-loading-wait").css('display', 'inline-block');
                        $("#clinical-data-table-div").css('display','none');

                        if(!_.isObject(window.iviz.datamanager.initialSetupResult)) {
                            $.when(window.iviz.datamanager.initialSetup()).then(function(_data){
                                StudyViewClinicalTabController.init(function() {
                                    $("#clinical-data-table-div").css('display','inline-block');
                                    $("#clinical-data-table-loading-wait").css('display', 'none');
                                    $('#study-tab-clinical-a').addClass("tab-clicked");
                                });
                            });
                        }else {
                            StudyViewClinicalTabController.init(function() {
                                $("#clinical-data-table-div").css('display','inline-block');
                                $("#clinical-data-table-loading-wait").css('display', 'none');
                                $('#study-tab-clinical-a').addClass("tab-clicked");
                            });
                        }
                    }
                    window.location.hash = '#clinical';
                });


                StudyViewProxy.ivizLoad();

                iViz.vue.manage.init();

                // This is used to indicate how to disable two buttons. By default, they are set to true.
                if(vcSession.URL !== undefined) {
                    iViz.vue.manage.getInstance().showSaveButton=false;
                    iViz.vue.manage.getInstance().showShareButton=true;
                    iViz.vue.manage.getInstance().showManageButton=true;
                    if(username !== '') {
                        iViz.vue.manage.getInstance().loadUserSpecificCohorts = true;
                    }
                }


                var urlHash = window.location.hash;
                for (var i = 0, tabsL = $('#study-tabs').find('li').length; i < tabsL; i++) {
                    $('#study-tabs').tabs('enable', i);
                }
                if (!_.isUndefined(urlHash)) {
                    switch (urlHash) {
                        case '#cna':
                            if ($('#study-tab-cna-a').length == 0) {
                                $('#study-tab-summary-a').click();
                            } else {
                                $('#study-tab-cna-a').click();
                            }
                            break;
                        case '#mutations':
                            if ($('#study-tab-mutations-a').length == 0) {
                                $('#study-tab-summary-a').click();
                            } else {
                                $('#study-tab-mutations-a').click();
                            }
                            break;
                        case '#clinical':
                            if ($('#study-tab-clinical-a').length == 0) {
                                $('#study-tab-summary-a').click();
                            } else {
                                $('#study-tab-clinical-a').click();
                            }
                            break;
                        case '#summary':
                            $('#study-tab-summary-a').click();
                            break;
                        default:
                            $('#study-tab-summary-a').click();
                            break;
                    }
                } else {
                    $('#study-tab-summary-a').click();
                }

                if(cohortIdsList.length === 1) {
                    window.cbio.util.getDatahubStudiesList()
                        .then(function(data) {
                            if(_.isObject(data) && data.hasOwnProperty(cohortIdsList[0])) {
                                $('#study-view-header-download-all-data').attr('action', data[cohortIdsList[0]].htmlURL);
                                $('#study-view-header-download-all-data').css('display', 'block');
                                $('#study-view-header-download-all-data>button').qtip({
                                    content: {text: 'Download all genomic and clinical data files of this study.'},
                                    style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
                                    show: {event: 'mouseover'},
                                    hide: {fixed: true, delay: 100, event: 'mouseout'},
                                    position: {
                                        my: 'bottom center',
                                        at: 'top center',
                                        viewport: $(window)
                                    }
                                });
                            }
                        }).fail(function(error) {
                        console.log(error);
                    });
                }
            });
    });
</script>
    
    
    </span>
</body>
</html>

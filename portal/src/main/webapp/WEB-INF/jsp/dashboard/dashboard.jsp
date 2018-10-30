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

<%@page import="org.springframework.security.authentication.AnonymousAuthenticationToken"%>
<%@page import="org.springframework.security.core.context.SecurityContextHolder"%>
<%@page import="org.springframework.security.core.Authentication"%>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.simple.JSONValue" %>
<%@ page import="org.mskcc.cbio.portal.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.servlet.*" %>
<%@ page import="java.util.Set" %>
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

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean showShareButton = true;
    boolean showSaveButton = false;
    if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
        showSaveButton= true;
    }
    
    if (cancerStudyViewError!=null) {
        out.print(cancerStudyViewError);
    } else {
%>

<jsp:include page="../global/legacy_head.jsp" flush="true" />


<jsp:include page="../global/header.jsp" flush="true" />
<span class="studyContainer">

<table width="100%" id="show_study_details" style="margin: 8px 2px 5px 2px;display:none;">
    <tr>
        <td class="study-view-header-first-row-td">
            <b><u id="study_name"></u></b>
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
        margin-bottom: 10px;
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
    
    .legacy .fixedWidth .contentWidth {
        width:1300px;
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
<script src="js/lib/clipboard.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
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
<script src="js/src/dashboard/hijackSubmission.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/cbio-util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/download-util.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">
    var username = $('#header_bar_table span').text()||'';
    var studyCasesMap = '<%=studySampleMap%>';
    var showShareButton = <%=showShareButton%>;
    var showSaveButton = <%=showSaveButton%>;
    var userEmailAddress = '<%=GlobalProperties.getAuthenticatedUserName()%>';
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

    function getOriginStudies(virtualStudy) {
        var def = new $.Deferred();
        var promises = _.filter(virtualStudy.data.origin, function(study) {
            return !iviz.datamanager.data.studies.hasOwnProperty(study);
        }).map(function(t) {
            return iviz.datamanager.getVirtualStudy(t);
        });
        $.when.apply($, promises)
            .then(function() {
                def.resolve(virtualStudy.data.origin.map(function(studyId) {
                    var studyMetaData = iviz.datamanager.getStudyById(studyId);
                    var info = {
                        id: studyId
                    };
                    if (studyMetaData) {
                        info.name = studyMetaData.studyType === 'vs' ? studyMetaData.data.name : studyMetaData.name;
                        info.description = studyMetaData.studyType === 'vs' ? studyMetaData.data.description : studyMetaData.description;
                    }
                    return info;
                }));
            });
        return def.promise();
    }

    $(document).ready(function() {
        //this is for testing, once done this should be commented/deleted
        //window.cbioURL = '';
        window.iviz = {};
        //commented for thesing
        window.cbioURL = window.location.origin + window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2)) + '/';
        window.cbioResourceURL = 'js/src/dashboard/resources/';
        window.iviz.datamanager = new DataManagerForIviz.init(window.cbioURL, studyCasesMap);

        var emailContact_ = '<%=GlobalProperties.getEmailContact()%>';

        if (emailContact_) {
            var span = $.parseHTML(emailContact_);
            $(span).mailme(true);
            emailContact_ = $(span).prop('outerHTML');
        }
        
        var studyIds = Object.keys(studyCasesMap);

        var getVirtualStudy = function(id){
            var def = new $.Deferred();
            $.get(window.cbioURL+'api-legacy/proxy/session/virtual_study/'+id)
            .done(function(response){
            	    def.resolve(response)
            })
            .fail(function(error) {
                def.reject(error);
            });
            return def.promise();
        }
        
        var getSelectableStudyIds = function() {
            var def = new $.Deferred();
            $.when(window.iviz.datamanager.getAllPhysicalStudies(), window.iviz.datamanager.getAllVirtualStudies()).then(function(physicalStudies, virtualStudies) {
                var physicalStudyIds = _.pluck(physicalStudies,'studyId');
                var virtualStudyIds = _.pluck(virtualStudies,'id');
                def.resolve( physicalStudyIds.concat(virtualStudyIds))
            }).fail(function(error) {
                def.reject(error);
            });
            return def.promise();
        }
        
        $.when(window.cbioportal_client.getStudies({ study_ids: studyIds}), window.iviz.datamanager.getGeneticProfiles(),
            window.iviz.datamanager.getAllPhysicalStudies(), window.iviz.datamanager.getAllVirtualStudies())
            .then(function(_cancerStudies, _geneticProfiles,physicalStudies,virtualStudies){
                if(cohortIdsList.length === 1 ) {
                    if(JSON.stringify(cohortIdsList) === JSON.stringify(studyIds)) {
                            $("#show_study_details").css('display','');
                            var _cancerStudy = _cancerStudies[0]
                        document.title = _cancerStudy.name
                        $("#study_name").html(_cancerStudy.name);
                        var _desc = _cancerStudy.description;
                        if(_cancerStudy.pmid !== undefined && _cancerStudy.pmid !== null){
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
                            return _profile.genetic_alteration_type === 'COPY_NUMBER_ALTERATION' && _profile.datatype === 'DISCRETE';
                        });
                        if(_cnaProfiles.length>0){
                            appendCnaTab();
                        }
                        
                        // TODO changed mutationProfileId to mutationProfileIds when mutations tab support multi-studies
                        StudyViewParams.params = {
                            studyId: _cancerStudy.id,
                            mutationProfileId: _mutationProfiles.length>0?_mutationProfiles[0].id:'',
                            hasMutSig: hasMutation,
                            caseSetId: _cancerStudy.id + '_all',
                            cnaProfileId: _cnaProfiles.length>0?_cnaProfiles[0].id:''
                        };
                        window.mutationProfileId = StudyViewParams.params.mutationProfileId ;
                        window.cnaProfileId = StudyViewParams.params.cnaProfileId;
                        window.case_set_id = StudyViewParams.params.caseSetId;
                        } else {
                            var response = _.findWhere(virtualStudies, {id: cohortIdsList[0]})
                         if (response) {
                                 var name = response['data']['name'];
                                 $("#show_study_details").css('display','');
                                 $("#study_name").html(name);
                                 $("#cancer_study_list").val(cohortIdsList[0]);
                                 document.title = name;
                                 getOriginStudies(response)
                                     .done(function(data) {
                                         cbio.util.showVShtmlDescription('#study_desc', response['data']['description'], data);
                                     });
                         } else {
                             $.when(iviz.datamanager.getVirtualStudy(cohortIdsList[0])).then(function(vs){
                                    var name = vs['data']['name'];
                                    $("#show_study_details").css('display','');
                                    $("#study_name").html(name);
                                    $("#cancer_study_list").val(cohortIdsList[0]);
                                    document.title = name;
                                     getOriginStudies(vs)
                                         .done(function(data) {
                                             cbio.util.showVShtmlDescription('#study_desc', vs['data']['description'], data);
                                         });
                                }).fail(function() {
                                    $("#show_study_details").css('display','');
                                    cbio.util.showCombinedStudyNameAndDescription("#study_name", "#study_desc", _cancerStudies, '', '');
                                });
                            }
                        }
                } else {
                    $("#show_study_details").css('display','');
                    cbio.util.showCombinedStudyNameAndDescription("#study_name", "#study_desc", _cancerStudies, '', '');
                }
                $("#submit_button").click(function(){
                    iViz.submitForm(true);
                });

                $("#study-tabs").tabs({disabled: true});
                $('#study-tab-summary-a').click(function () {
                    if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
                        $("#study-tabs-loading-wait").css('display', 'none');
                        if(!_.isObject(window.iviz.datamanager.initialSetupResult)) {
                            $.when(window.iviz.datamanager.getConfigs(), getSelectableStudyIds())
                                .done(function(configs, selectableIds) {
                                    var opts = {};

                                    if (_.isObject(configs)) {
                                        opts = configs;
                                    }
                                    if (emailContact_) {
                                        opts.emailContact = emailContact_;
                                    }
                                    $.when(
                                        window.iviz.datamanager.initialSetup()
                                    ).done(function(_data) {
                                        initdcplots(_data, opts, selectableIds);
                                    }).fail(function(error) {
                                        iViz.vue.manage.getInstance().failedToInit.status = true;
                                        if (error) {
                                            iViz.vue.manage.getInstance().failedToInit.message = error
                                        }
                                        iViz.vue.manage.getInstance().isloading = false;
                                    });
                                })
                                .fail(function() {
                                    iViz.vue.manage.getInstance().failedToInit.status = true;
                                    iViz.vue.manage.getInstance().failedToInit.message = 'Failed to load study view configurations.';
                                    iViz.vue.manage.getInstance().isloading = false;
                                });
                        }else {
                            $.when(window.iviz.datamanager.getConfigs(), getSelectableStudyIds()).done(function(configs, selectableIds){
                                var opts = {};

                                if (_.isObject(configs)) {
                                    opts = configs;
                                }
                                if (emailContact_) {
                                    opts.emailContact = emailContact_;
                                }
                                initdcplots(window.iviz.datamanager.initialSetupResult, opts, selectableIds);
                            }).fail(function() {
                                iViz.vue.manage.getInstance().failedToInit.status = true;
                                iViz.vue.manage.getInstance().failedToInit.message = 'Failed to load study view configurations.';
                                iViz.vue.manage.getInstance().isloading = false;
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
                if(window.sessionServiceAvailable) {
                    iViz.vue.manage.getInstance().showShareButton=showShareButton;
                    iViz.vue.manage.getInstance().showSaveButton=showSaveButton;
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
                        .then(function(studies) {
                            if(_.isArray(studies) && studies.indexOf(cohortIdsList[0]) > -1) {
                                $('#study-view-header-download-all-data').attr('action', 'http://download.cbioportal.org/' + cohortIdsList[0] + '.tar.gz');
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

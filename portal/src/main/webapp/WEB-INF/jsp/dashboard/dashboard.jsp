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

<%
    request.setAttribute("standard-js-css", true);
    String isDemoMode = request.getParameter("demo");
    boolean showPlaceHoder;
    if (isDemoMode!=null) {
        showPlaceHoder = isDemoMode.equalsIgnoreCase("on");
    } else {
        showPlaceHoder = GlobalProperties.showPlaceholderInPatientView();
    }

    CancerStudy cancerStudy = (CancerStudy)request.getAttribute(CancerStudyView.CANCER_STUDY);
    String cancerStudyViewError = (String)request.getAttribute(CancerStudyView.ERROR);

    String caseSetId = (String)request.getAttribute(QueryBuilder.CASE_SET_ID);
    List<String> caseIds = (List<String>)request.getAttribute(QueryBuilder.CASE_IDS);
    String jsonCaseIds = JSONValue.toJSONString(caseIds);

    GeneticProfile mutationProfile = (GeneticProfile)request.getAttribute(CancerStudyView.MUTATION_PROFILE);
    boolean hasMutation = mutationProfile!=null;

    boolean hasMutSig = cancerStudy!=null && cancerStudy.hasMutSigData();
    boolean showMutationsTab = hasMutation;

    GeneticProfile cnaProfile = (GeneticProfile)request.getAttribute(CancerStudyView.CNA_PROFILE);
    boolean hasCNA = cnaProfile!=null;

    boolean hasGistic = cancerStudy!=null && cancerStudy.hasGisticData();
    boolean showCNATab = hasGistic;

    String mutationProfileStableId = null;
    String cnaProfileStableId = null;
    if (mutationProfile!=null) {
        mutationProfileStableId = mutationProfile.getStableId();
    }
    if (cnaProfile!=null) {
        cnaProfileStableId = cnaProfile.getStableId();
    }

    boolean hasCnaSegmentData = cancerStudy!=null && cancerStudy.hasCnaSegmentData();

    if (cancerStudyViewError!=null) {
        out.print(cancerStudyViewError);
    } else {
%>

<jsp:include page="../global/header.jsp" flush="true" />

<table width="100%" style="margin: 8px 2px 5px 2px">
    <tr>
        <td class="study-view-header-first-row-td">
            <b><u><%=cancerStudy.getName()%>
            </u></b>
            <form method="post" action="index.do">
                <input type="hidden" name="cancer_study_id" value="<%=cancerStudy.getCancerStudyStableId()%>">
                <input type="hidden" name="<%=QueryBuilder.CANCER_STUDY_LIST%>"
                       value="<%=cancerStudy.getCancerStudyStableId()%>">
                <input type="submit" value="Query this study" class="btn btn-primary btn-xs">
            </form>
            <form id="study-view-header-download-all-data" method="get" action="">
                <input type="hidden" name="raw" value="ture">
                <button class="btn btn-default btn-xs">Download data</button>
            </form>
        </td>
    </tr>
    <tr>
        <td id="study-desc"><%=cancerStudy.getDescription()%>
            <%if (null!=cancerStudy.getPmid()) {%>
            &nbsp;<a href="http://www.ncbi.nlm.nih.gov/pubmed/<%=cancerStudy.getPmid()%>">PubMed</a>
            <%}%>
        </td>
    </tr>
</table>


<div id="study-tabs">
    <ul>

        <li id="li-1"><a href='#summary' id='study-tab-summary-a' class='study-tab' title='Study Summary'>Study Summary</a></li>
        <!--<li><a href='#clinical-plots' class='study-tab' title='DC Plots'>Study Summary</a></li>-->
        <li><a href='#clinical' id='study-tab-clinical-a' class='study-tab' title='Clinical Data'>Clinical Data</a></li>

        <%if(showMutationsTab){%>
        <li><a href='#mutations' id='study-tab-mutations-a' class='study-tab' title='Mutations'>Mutated Genes</a></li>
        <%}%>

        <%if(showCNATab){%>
        <li><a href='#cna' id='study-tab-cna-a' class='study-tab' title='Copy Number Alterations'>Copy Number Alterations</a></li>
        <%}%>

        <%-- Always start with tab.  JS in browser will remove if not needed. --%>
        <li id="study-tab-heatmap-li"><a href="#heatmap" id="study-tab-heatmap-a" class="study-tab">Heatmap</a></li>

    </ul>

    <div class="study-section" id="summary">
        <%@ include file="dcplots.jsp" %>
    </div>

    <div class="study-section" id="clinical">
        <%@ include file="clinical.jsp" %>
    </div>

    <%if(showMutationsTab){%>
    <div class="study-section" id="mutations">
        <%@ include file="mutations.jsp" %>
    </div>
    <%}%>

    <%if(showCNATab){%>
    <div class="study-section" id="cna">
        <%@ include file="cna.jsp" %>
    </div>
    <%}%>

    <%-- Always start with this.  JS in browser will remove if not needed. --%>
    <div class="study-section" id="heatmap">
        <%@ include file="mdacc_heatmap_viewer.jsp" %>
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
    var cancerStudyId = '<%=cancerStudy.getCancerStudyStableId()%>';
    var cancerStudyName = '<%=StringEscapeUtils.escapeJavaScript(cancerStudy.getName())%>';
    var mutationProfileId = <%=mutationProfileStableId==null%>?null:'<%=mutationProfileStableId%>';
    var cnaProfileId = <%=cnaProfileStableId==null%>?null:'<%=cnaProfileStableId%>';
    var hasCnaSegmentData = <%=hasCnaSegmentData%>;
    var hasMutSig = <%=hasMutSig%>;
    var caseSetId = '<%=caseSetId%>';
    var caseIds = <%=jsonCaseIds%>;
    var cancer_study_id = cancerStudyId; //Some components using this as global ID
    var appVersion = '<%=GlobalProperties.getAppVersion()%>';
    var hasMutation = <%=hasMutation%>;
    var hasCNA = <%=hasCNA%>;

    $("#study-tabs").tabs({disabled: true});
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
</style>

<script src="js/src/dashboard/iviz-vendor.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/src/dashboard/vc-session.js?<%=GlobalProperties.getAppVersion()%>"></script>
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
<link rel="stylesheet" href="css/vc-session.css?<%=GlobalProperties.getAppVersion()%>"/>
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

    $('#study-tab-summary-a').click(function () {
        if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
            $("#study-tabs-loading-wait").css('display', 'none');
            if(_.isUndefined(window.iviz.datamanager)) {
                window.iviz.datamanager = new DataManagerForIviz.init(window.cbioURL, studyCasesMap);
                $.when(
                    window.iviz.datamanager.initialSetup(),
                    window.iviz.datamanager.getStyleVars()
                ).then(function(_data, styles) {
                    var opts = {};

                    if(_.isObject(styles)) {
                        opts.styles = styles;
                    }
                    initdcplots(_data, opts);
                });
            }else {
                $.when(window.iviz.datamanager.getStyleVars()).then(function(styles){
                    var opts = {};

                    if(_.isObject(styles)) {
                        opts.styles = styles;
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

            if(_.isUndefined(window.iviz.datamanager)) {
                window.iviz.datamanager = new DataManagerForIviz.init(window.cbioURL, studyCasesMap);
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

    $('#study-tab-mutations-a').click(function(){
        if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
            StudyViewMutationsTabController.init(function() {
                $(this).addClass("tab-clicked");
                StudyViewMutationsTabController.getDataTable().fnAdjustColumnSizing();
            });
        }
        window.location.hash = '#mutations';
    });

    $('#study-tab-cna-a').click(function(){
        if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
            StudyViewCNATabController.init(function() {
                $(this).addClass("tab-clicked");
                StudyViewCNATabController.getDataTable().fnAdjustColumnSizing();
            });
        }
        window.location.hash = '#cna';
    });

    $(document).ready(function () {
        // All temporory fixes, need to do the refacotoring with new iViz code
        StudyViewParams.params = {
            studyId: cancerStudyId,
            caseIds: caseIds,
            cnaProfileId: cnaProfileId,
            mutationProfileId: mutationProfileId,
            caseSetId: caseSetId,
            hasMutSig: hasMutSig
        };
        StudyViewProxy.ivizLoad();

        iViz.vue.manage.init();

        // This is used to indicate how to disable two buttons. By default, they are set to true.
        iViz.vue.manage.getInstance().showManageButton = false;
        iViz.vue.manage.getInstance().showSaveButton = false;

        //this is for testing, once done this should be commented/deleted
        window.cbioURL = '';
        window.cbioResourceURL = 'js/src/dashboard/resources/';
        //commented for thesing
        //window.cbioURL = window.location.origin + window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
        window.mutationProfileId = window.mutationProfileId;
        window.cnaProfileId = window.cnaProfileId;
        window.case_set_id = window.caseSetId;
        window.studyCasesMap = {};
        window.studyCasesMap[window.cancerStudyId]={};
        window.iviz = {};

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

        window.cbio.util.getDatahubStudiesList()
            .then(function(data) {
                if(_.isObject(data) && data.hasOwnProperty(cancerStudyId)) {
                    $('#study-view-header-download-all-data').attr('action', data[cancerStudyId].htmlURL);
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
    });
</script>

</body>
</html>

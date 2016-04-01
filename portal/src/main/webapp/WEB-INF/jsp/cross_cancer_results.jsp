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

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="org.mskcc.cbio.portal.util.XssRequestWrapper" %>

<%
    String siteTitle = GlobalProperties.getTitle();
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    ServletXssUtil servletXssUtil = ServletXssUtil.getInstance();

    // Get priority settings
    Integer dataPriority;
    try {
        dataPriority
                = Integer.parseInt(request.getParameter(QueryBuilder.DATA_PRIORITY).trim());
    } catch (Exception e) {
        dataPriority = 0;
    }

    String geneList = request.getParameter(QueryBuilder.GENE_LIST);
    //String cancerStudyList = request.getParameter(QueryBuilder.CANCER_STUDY_LIST);

    // we need the raw gene list
    if (request instanceof XssRequestWrapper)
    {
        geneList = ((XssRequestWrapper)request).getRawParameter(QueryBuilder.GENE_LIST);
        //cancerStudyList = ((XssRequestWrapper)request).getRawParameter(QueryBuilder.CANCER_STUDY_LIST);
    }

    geneList = geneList.replaceAll("\n", " ").replaceAll("\r", "").replaceAll("/", "_");
    geneList = servletXssUtil.getCleanerInput(geneList);


    String oncokbUrl = (String) GlobalProperties.getOncoKBUrl();
    boolean showMyCancerGenomeUrl = (Boolean) GlobalProperties.showMyCancerGenomeUrl();
    String oncokbGeneStatus = (String) GlobalProperties.getOncoKBGeneStatus();
    boolean showHotspot = (Boolean) GlobalProperties.showHotspot();
    String userName = GlobalProperties.getAuthenticatedUserName();

%>

<jsp:include page="global/header.jsp" flush="true"/>

<!-- for now, let's include these guys here and prevent clashes with the rest of the portal -->
<script type="text/javascript" src="js/src/OncoKBConnector.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/crosscancer.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cross-cancer-plotly-plots.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/plots-tab/util/stylesheet.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/plotsUtil.js"></script>
<link href="css/bootstrap-dialog.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />

<link href="css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/mutationMapper.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/crosscancer.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />


<%
    // Means that user landed on this page with the old way.
    if(geneList != null) {
%>

<%--
<script type="text/javascript">
    //var cancerStudyList = window.location.hash.split("/")[4];
    //window.location.hash = window.location.hash || "crosscancer/overview/<%=dataPriority%>/<%=geneList%>/<%=cancerStudyList%>";
</script>
--%>

<%
    }
%>

<table>
    <tr>
        <td>

            <div id="results_container">
                <div id='modify_query' style='margin:20px;'>
                    <button type='button' class='btn btn-primary' data-toggle='button' id='modify_query_btn'>
                        Modify Query
                    </button>
                    <div style="margin-left:5px;display:none;" id="query_form_on_results_page">
                        <%@ include file="query_form.jsp" %>
                    </div>
                </div>
                <div id="crosscancer-container">
                </div>
            </div>
            <!-- end results container -->
       
        </td>
    </tr>
</table>

<script>
    var oncokbGeneStatus = <%=oncokbGeneStatus%>;
    var showHotspot = <%=showHotspot%>;
    var userName = '<%=userName%>';
    var enableMyCancerGenome = <%=showMyCancerGenomeUrl%>;

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
        OncoKB.setUrl('<%=oncokbUrl%>');
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

        $("a.result-tab").click(function(){
            if($(this).attr("href")=="#bookmark_email") {
                $("#bookmark-link").attr("href",window.location.href);
            }
        });

        $("#bitly-generator").click(function() {
            bitlyURL(window.location.href);
        });

    });

</script>

<!-- Crosscancer templates -->
<script type="text/template" id="cross-cancer-main-tmpl">
    <div id="tabs">
        <ul>
            <li>
                <a href="#cc-overview" id="cc-overview-link" title="Compact visualization of genomic alterations">Overview</a>
            </li>
            <li>
                <a href="#cc-mutations" id="cc-mutations-link" title="Mutation details, including mutation type, predicted functional consequence">Mutations</a>
            </li>
            <li>
                <a href="#cc-plots" id="cc-plots-link" title="Plots with mRNA expression data (TCGA provisional only)">Expression</a>
            </li>
            <li>
                <a href="#cc-download" id="cc-download-link" title="Download all alterations or copy and paste into Excel">Download</a>
            </li>
            <li>
                <a href='#cc-bookmark' class='result-tab' title="Bookmark or generate a URL for email">
                    Bookmark
                </a>
            </li>
        </ul>
        <div class="section" id="cc-overview" style="display:none">
            <div id="headerBar" style="display:none">
                <div id="cctitlecontainer" style="margin-left:200px;float:left"></div>
                <div>
                    <button id="histogram-download-pdf" class='diagram-to-pdf'>PDF</button>
                    <button id="histogram-download-svg" class='diagram-to-svg'>SVG</button>
                </div>

                <div style="margin-top:10px;margin-bottom:10px;">
                    <div style="float:left;margin-right:20px;">
                        Y-Axis value:
                        <select id="yAxis"><option value="Frequency">Alteration frequency</option><option value="Count">Absolute counts</option></select>
                    </div>
                    <div style="float:left;margin-right:20px;">
                        <span style="float:left;" class="diagram-general-slider-text" id="sliderLabel">Min. % altered samples:</span>
                        <div style="float:left;width:60px;margin-top:4px;margin-right:4px;margin-left:8px;" id="sliderMinY"></div>
                        <input style="float:left;" id="minY" size="3" type="text">
                    </div>
                    <div style="float:left;margin-right:20px;">
                        <span style="float:left;" class="diagram-general-slider-text" >Min. # total samples:</span>
                        <div style="float:left;width:60px;margin-top:4px;margin-right:4px;margin-left:8px;" id="totalSampleSlider"></div>
                        <input style="float:left;" id="minTotal" size="3" type="text">
                    </div>
                    <div style="float:left;margin-right:20px;">
                        <input type="checkbox" id="histogram-show-colors" checked> Show alteration types
                    </div>
                    <div style="float:left;margin-right:20px;">
                        <input type="checkbox" id="sortBy"> Sort alphabetically
                    </div>
                </div>

                <div style="display:none">
                    <div id="show-hide-studies">
                        <span class="triangle ui-icon ui-icon-triangle-1-e cc-triangle"></span>
                        <span class="triangle ui-icon ui-icon-triangle-1-s cc-triangle cc-hide"></span>
                        <b id="show-hide-studies-toggle">Select studies</b>
                        <br/>
                    </div>
                    <div id="cancerbycancer-controls" class="cc-hide">
                        (Select <a href="#" id="cc-select-all">all</a> / <a href="#" id="cc-select-none">none</a>)
                        <br>
                        <br>
                    </div>
                </div>
            </div>
            <div id="customize-controls" class="ui-widget cc-hide" style="display:none">
                <div class="close-customize">
                    <a href="#">&times;</a>
                </div>
                <h3>Customize histogram</h3>
                
            </div>

            <div id="cchistogram" style="width: 1100px; height: 820px;position:relative;margin-top:30px;">
                <img src="images/ajax-loader.gif"/>
            </div>

            <div id="studies-with-no-data">
            </div>
        </div>

        <div class="section" id="cc-mutations">
            <div id="mutation_details" class="mutation-details-content">
                <img src="images/ajax-loader.gif"/>
            </div>
        </div>
        <div class="section" id="cc-plots">
            <jsp:include page="cross_cancer_plots_tab.jsp" />
        </div>
        <div class="section" id="cc-download">
            <div class='copy_tables'>
                <br>
                <h4>Contents can be copied and pasted into Excel.</h4>
                <p>Frequency of Alteration Across Studies:<p/>
                <textarea rows="30" cols="40" id="cc-download-text">
                </textarea>
            </div>
        </div>

        <div class="section" id="cc-bookmark">
            <h4>Right click</b> on the link below to bookmark your results or send by email:</h4>
            <br/>
            <a  id="bookmark-link" href="#">
                <%=request.getAttribute(QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING)%>?...
            </a>
            <br/>
            <br/>

            If you would like to use a <b>shorter URL that will not break in email postings</b>, you can use the<br><a href='https://bitly.com/'>bitly.com</a> service below:<BR>
            <BR><button type="button" id="bitly-generator">Shorten URL</button>
            <div id='bitly'></div>
        </div>

    </div>
</script>

<script type="text/template" id="cc-remove-study-tmpl">
    <div class="cc-remove-single-study">
        <label>
            <input type="checkbox" data-studyID="{{studyId}}" data-altered="{{altered}}" id="histogram-remove-study-{{studyId}}" {{checked ? "checked" : ""}}>
            {{name}}
        </label>
    </div>
</script>


<script type="text/template" id="studies-with-no-data-item-tmpl">
    <li>{{name}}</li>
</script>

<script type="text/template" id="study-link-tmpl">
    <a href="index.do?tab_index=tab_visualize&cancer_study_id={{study.studyId}}&genetic_profile_ids_PROFILE_MUTATION_EXTENDED={{study.mutationProfile}}&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION={{study.cnaProfile}}&Z_SCORE_THRESHOLD=2.0&case_set_id={{study.caseSetId}}&case_ids=&gene_list={{genes}}&gene_set_choice=user-defined-list&Action=Submit" target="_blank">
        click to view details &raquo;
    </a>
</script>

<script type="text/template" id="study-tip-tmpl">
    <div>
        <div class="cc-study-tip">
            <b class="cc-tip-header">{{name}}</b><br>
            <p>
                Gene set altered in <b>{{allFrequency}}%</b> of {{caseSetLength}} cases.
                <br>
                ({{studyLink}})
            </p>
            <table class="cc-tip-table">
                <thead>
                <tr>
                    <th>Alteration</th>
                    <th>Frequency</th>
                </tr>
                </thead>
                <tbody>
                <tr class='{{ mutationCount > 0 ? "cc-mutation" : "cc-hide"}}'>
                    <td class="cc-alt-type">Mutation</td>
                    <td>{{mutationFrequency}}% ({{mutationCount}} cases)</td>
                </tr>
                <tr class='{{ lossCount > 0 ? "cc-loss" : "cc-hide"}}'>
                    <td class="cc-alt-type">Heterozygous loss</td>
                    <td>{{lossFrequency}}% ({{lossCount}} cases)</td>
                </tr>
                <tr class='{{ deletionCount > 0 ? "cc-del" : "cc-hide"}}'>
                    <td class="cc-alt-type">Deletion</td>
                    <td>{{deletionFrequency}}% ({{deletionCount}} cases)</td>
                </tr>
                <tr class='{{ gainCount > 0 ? "cc-gain" : "cc-hide"}}'>
                    <td class="cc-alt-type">Gain</td>
                    <td>{{gainFrequency}}% ({{gainCount}} cases)</td>
                </tr>
                <tr class='{{ amplificationCount > 0 ? "cc-amp" : "cc-hide"}}'>
                    <td class="cc-alt-type">Amplification</td>
                    <td>{{amplificationFrequency}}% ({{amplificationCount}} cases)</td>
                </tr>
                <tr class='{{ multipleCount > 0 ? "cc-mtpl" : "cc-hide"}}'>
                    <td class="cc-alt-type">Multiple alterations</td>
                    <td>{{multipleFrequency}}% ({{multipleCount}} cases)</td>
                </tr>
                </tbody>
            </table>

        </div>
    </div>
</script>

<script type="text/template" id="mutation_table_annotation_template">
    <span class='oncokb oncokb_alteration oncogenic' oncokbId='{{oncokbId}}'>
        <img class='oncokb oncogenic loader' width="13" height="13" class="loader" src="images/ajax-loader.gif"/>
    </span>
    <span class='oncokb oncokb_column' oncokbId='{{oncokbId}}'></span>
    <span class='mcg' alt='{{mcgAlt}}'>
        <img src='images/mcg_logo.png'>
    </span>
    <span class='chang_hotspot' alt='{{changHotspotAlt}}'>
        <img width='13' height='13' src='images/oncokb-flame.svg'>
    </span>
</script>

<script type="text/template" id="studies-with-no-data-tmpl">
    <div class="ui-state-highlight ui-corner-all">
        <p>
            <span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em; margin-left: .3em"></span>
            Since the data priority was set to '{{ priority == 1 ? "Only Mutation" : "Only CNA" }}', the following
            <b>{{hiddenStudies.length}} cancer studies</b>
            that do not have {{ priority == 1 ? "mutation" : "CNA" }} data were excluded from this view: <br>
        </p>
        <ul id="not-shown-studies">
        </ul>
        <p></p>
    </div>
</script>

<script type="text/template" id="crosscancer-title-tmpl">
    <b class="cctitle">
        Cross-cancer alteration summary for {{genes}} ({{numOfStudies}} studies / {{numOfGenes}} gene{{numOfGenes > 1 ? "s" : ""}})
    </b>
</script>

<!-- Mutation views -->
<jsp:include page="mutation_views.jsp" flush="true"/>
<!-- mutation views end -->

<script type="text/template" id="cross-cancer-main-empty-tmpl">
    <h1>Default cross-cancer view</h1>
</script>

<script>
    $(document).ready(function() {
        var _cc_plots_gene_list = "";
        var tmp = setInterval(function () {timer();}, 1000);
        function timer() {
            if (window.ccQueriedGenes !== undefined) {
                clearInterval(tmp);
                var cc_plots_tab_init = false;
                if ($("#cc-plots").is(":visible")) {
                    _cc_plots_gene_list = _cc_plots_gene_list;
                    _.each(window.ccQueriedGenes, function (_gene) {
                        $("#cc_plots_gene_list").append(
                            "<option value='" + _gene + "'>" + _gene + "</option>");
                    });
                    ccPlots.init();
                    cc_plots_tab_init = true;
                } else {
                    $(window).trigger("resize");
                }
                $("#tabs").bind("tabsactivate", function(event, ui) {
                    if (ui.newTab.text().trim().toLowerCase() === "expression") {
                        if (cc_plots_tab_init === false) {
                            _cc_plots_gene_list = _cc_plots_gene_list;
                            _.each(window.ccQueriedGenes, function (_gene) {
                                $("#cc_plots_gene_list").append(
                                    "<option value='" + _gene + "'>" + _gene + "</option>");
                            });
                            ccPlots.init();
                            cc_plots_tab_init = true;
                            $(window).trigger("resize");
                        } else {
                            $(window).trigger("resize");
                        }
                    }
                });
            }
        }
    });
</script>

</div>
</td>
</tr>
<tr>
    <td colspan="3">
        <jsp:include page="global/footer.jsp" flush="true"/>
    </td>
</tr>
</table>
</center>
</div>



<jsp:include page="global/xdebug.jsp" flush="true"/>


</body>
</html>

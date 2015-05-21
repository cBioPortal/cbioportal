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

        

    String bitlyUser = GlobalProperties.getBitlyUser();
    String bitlyKey = GlobalProperties.getBitlyApiKey();
%>

<jsp:include page="global/header.jsp" flush="true"/>

<!-- for now, let's include these guys here and prevent clashes with the rest of the portal -->
<script type="text/javascript" src="js/src/crosscancer.js?<%=GlobalProperties.getAppVersion()%>"></script>
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
    //Set Event listener for the modify query button (expand the hidden form)
    $("#modify_query_btn").click(function () {
        $("#query_form_on_results_page").toggle();
        if($("#modify_query_btn").hasClass("active")) {
            $("#modify_query_btn").removeClass("active");
        } else {
            $("#modify_query_btn").addClass("active");    
        }
    });
    $("#toggle_query_form").click(function(event) {
        event.preventDefault();
        $('#query_form_on_results_page').toggle();
        //  Toggle the icons
        $(".query-toggle").toggle();
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
                <a href="#cc-mutations" id="cc-mutations-link" title="Mutation details, including mutation type,amino acid change, validation status and predicted functional consequence">Mutations</a>
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
        <div class="section" id="cc-overview">

            <div id="cctitlecontainer"></div>

            <div id="customize-controls" class="ui-widget cc-hide">
                <div class="close-customize">
                    <a href="#">&times;</a>
                </div>
                <h3>Customize histogram</h3>
                <table>
                    <tr>
                        <td>
                            <span id="no-alterations-control">
                                <input type="checkbox" id="histogram-remove-notaltered">
                                <label for="histogram-remove-notaltered">Hide studies with no alteration</label>
                            </span>
                        </td>
                        <td>
                            <span id="no-colors-control">
                                <input type="checkbox" id="histogram-show-colors" checked>
                                <label for="histogram-show-colors">Show alteration types</label>
                            </span>
                        </td>
                        <td>
                            <span id="sort-by-control">
                                Sort by:
                                <select id="histogram-sort-by">
                                    <option value="alteration">Alteration frequency</option>
                                    <option value="name">Cancer study name</option>
                                </select>
                            </span>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3">
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
                        </td>
                    </tr>
                </table>
            </div>

            <div id="cchistogram">
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
            <a href="<%=request.getAttribute(QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING)%>?tab_index=tab_visualize&cancer_study_id=all&gene_list={{genes}}&data_priority={{priority}}&Action=Submit">
                <%=request.getAttribute(QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING)%>?...
            </a>
            <br/>
            <br/>

            If you would like to use a <b>shorter URL that will not break in email postings</b>, you can use the<br><a href='https://bitly.com/'>bitly.com</a> service below:<BR>
            <BR><form><input type="button" onClick="bitlyURL('<%=request.getAttribute(QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING)%>?tab_index=tab_visualize&cancer_study_id=all&gene_list={{genes}}&data_priority={{priority}}&Action=Submit', '<%=bitlyUser%>', '<%=bitlyKey%>')" value="Shorten URL"></form>
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
    <button id="histogram-download-pdf" class='diagram-to-pdf'>PDF</button>
    <button id="histogram-download-svg" class='diagram-to-svg'>SVG</button>
    <button id="histogram-customize">Customize histogram</button>
</script>

<!-- Mutation views -->
<jsp:include page="mutation_views.jsp" flush="true"/>
<!-- mutation views end -->

<script type="text/template" id="cross-cancer-main-empty-tmpl">
    <h1>Default cross-cancer view</h1>
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

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%
    String siteTitle = GlobalProperties.getTitle();
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);

    // Get priority settings
    Integer dataPriority;
    try {
        dataPriority
                = Integer.parseInt(request.getParameter(QueryBuilder.DATA_PRIORITY).trim());
    } catch (Exception e) {
        dataPriority = 0;
    }
    ServletXssUtil servletXssUtil = ServletXssUtil.getInstance();
    String geneList = servletXssUtil.getCleanInput(request, QueryBuilder.GENE_LIST).replaceAll("\n", " ");

    String bitlyUser = GlobalProperties.getBitlyUser();
    String bitlyKey = GlobalProperties.getBitlyApiKey();
%>

<jsp:include page="global/header.jsp" flush="true"/>

<!-- for now, let's include these guys here and prevent clashes with the rest of the portal -->
<script type="text/javascript" src="js/src/mutation_model.js"></script>
<script type="text/javascript" src="js/src/crosscancer.js"></script>
<link href="css/data_table_ColVis.css" type="text/css" rel="stylesheet" />
<link href="css/data_table_jui.css" type="text/css" rel="stylesheet" />
<link href="css/crosscancer.css" type="text/css" rel="stylesheet" />

<%
    // Means that user landed on this page with the old way.
    if(geneList != null) {
%>

<script type="text/javascript">
    window.location.hash = "crosscancer/overview/<%=dataPriority%>/<%=geneList%>";
</script>

<%
    }
%>

<table>
    <tr>
        <td>

            <div id="results_container">
                <p><a href=""
                      title="Modify your original query.  Recommended over hitting your browser's back button."
                      id="toggle_query_form">
                    <span class='query-toggle ui-icon ui-icon-triangle-1-e'
                          style='float:left;'></span>
                    <span class='query-toggle ui-icon ui-icon-triangle-1-s'
                          style='float:left; display:none;'></span><b>Modify Query</b></a>

                <p/>

                <div style="margin-left:5px;display:none;" id="query_form_on_results_page">
                    <%@ include file="query_form.jsp" %>
                </div>

                <div id="crosscancer-container">
                </div>
            </div>
            <!-- end results container -->
        </td>
    </tr>
</table>


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
                    <tr class='{{ deletionCount > 0 ? "cc-del" : "cc-hide"}}'>
                        <td class="cc-alt-type">Deletion</td>
                        <td>{{deletionFrequency}}% ({{deletionCount}} cases)</td>
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
    <form style="display:inline-block"
          action='svgtopdf.do'
          method='post'
          class='svg-to-pdf-form'>
        <input type='hidden' name='svgelement'>
        <input type='hidden' name='filetype' value='pdf'>
        <input type='hidden' name='filename' value='crosscancerhistogram.pdf'>
    </form>
    <form style="display:inline-block"
          action='svgtopdf.do'
          method='post'
          class='svg-to-file-form'>
        <input type='hidden' name='svgelement'>
        <input type='hidden' name='filetype' value='svg'>
        <input type='hidden' name='filename' value='crosscancerhistogram.svg'>
    </form>
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
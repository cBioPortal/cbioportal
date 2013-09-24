<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>

<%
    String siteTitle = SkinUtil.getTitle();
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
%>

<jsp:include page="global/header.jsp" flush="true"/>

<!-- for now, let's include these guys here and prevent clashes with the rest of the portal -->
<script type="text/javascript" src="js/src/crosscancer.js"></script>
<script type="text/javascript" src="js/src/mutation_model.js"></script>
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

                <br/>

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
                <a href="#cc-overview">Overview</a>
            </li>
            <li>
                <a href="#cc-mutations">Mutations</a>
            </li>
        </ul>
        <div class="section" id="cc-overview">
            <div id="cctitlecontainer"></div>
            <div id="cchistogram">
                <img src="images/ajax-loader.gif"/>
            </div>
        </div>

        <div class="section" id="cc-mutations">
            <div id="ccmutationdetails">
                <img src="images/ajax-loader.gif"/>
            </div>
        </div>

    </div>
</script>

<script type="text/template" id="crosscancer-title-tmpl">
    <b class="cctitle">
        Cross-cancer alteration summary for {{genes}} ({{numOfStudies}} studies<sup id="cc-study-help">*</sup> / {{numOfGenes}} gene{{numOfGenes > 1 ? "s" : ""}})
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
    <button class='diagram-to-pdf'>PDF</button>
    <button class='diagram-to-svg'>SVG</button>
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
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
    String geneList = servletXssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);
%>

<jsp:include page="global/header.jsp" flush="true"/>

<!-- for now, let's include these guys here and prevent clashes with the rest of the portal -->
<script type="text/javascript" src="js/src/crosscancer.js"></script>
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
                <hr align="left" class="crosscancer-hr"/>

                <div id="crosscancer-container">
                </div>
            </div>
            <!-- end results container -->
        </td>
    </tr>
</table>


<!-- Crosscancer templates -->
<script type="text/template" id="cross-cancer-main-tmpl">
    <h1>Hullo world!</h1>
    <table>
        <tr>
            <th>Tab</th>
            <td>{{tab}}</td>
        </tr>
        <tr>
            <th>Priority</th>
            <td>{{priority}}</td>
        </tr>
        <tr>
            <th>Genes</th>
            <td>{{genes}}</td>
        </tr>
    </table>

    <div id="cchistogram">
    </div>
</script>

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
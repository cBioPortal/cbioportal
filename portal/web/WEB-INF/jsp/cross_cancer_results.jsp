<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.model.CancerType" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.Utilities" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>

<%
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");

    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    ArrayList<CancerType> cancerTypes = (ArrayList<CancerType>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);

    ServletXssUtil servletXssUtil = ServletXssUtil.getInstance();
    String geneList = servletXssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);

    //  Prepare gene list for URL.
    //  Extra spaces must be removed.  Otherwise OMA Links will not work.
    geneList = Utilities.appendSemis(geneList);
    geneList = geneList.replaceAll("\\s+", " ");
    geneList = URLEncoder.encode(geneList);

%>

<jsp:include page="global/header.jsp" flush="true" />

<script type="text/javascript">
$(document).ready(function(){
    <%
    //  Iterate through each Cancer Study
    //  For each cancer study, init AJAX
    for (CancerType cancerType:  cancerTypes) {
    %>
    $("#study_<%= cancerType.getCancerTypeId() %>").load('cross_cancer_summary.do?gene_list=<%= geneList %>&cancer_study_id=<%= cancerType.getCancerTypeId() %>');
    <% } %>
});
</script>

	<table>
        <tr>
            <td>

            <div id="results_container">

<h1>Cross-Cancer Study Results</h1>

    <%
        for (CancerType cancerType:  cancerTypes) {
            out.println ("<div class=\"cross_cancer_panel\">");
            out.println ("<h2>" + cancerType.getCancerName() + "</h2>");
            out.println ("<div class='cross_cancer_ajax' id=\"study_" + cancerType.getCancerTypeId() + "\">");
            out.println ("<img src='images/ajax-loader2.gif'>");
            out.println ("</div>");
            out.println ("</div>");
        }
    %>

            </div>  <!-- end results container -->
            </td>
        </tr>
    </table>
    </div>
    </td>
   <!-- <td width="172">

    </td>   -->
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true" />    

</body>
</html>

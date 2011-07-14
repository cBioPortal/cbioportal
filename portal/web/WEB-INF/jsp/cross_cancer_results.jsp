<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.model.CancerType" %>
<%@ page import="java.util.ArrayList" %>

<%
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");

    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    ArrayList<CancerType> cancerTypes = (ArrayList<CancerType>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
%>

<jsp:include page="global/header.jsp" flush="true" />

<script type="text/javascript">
$(document).ready(function(){
    <%
    //  Iterate through each Cancer Study
    //  For each cancer study, init AJAX
    for (CancerType cancerType:  cancerTypes) {
    %>
    $("#study_<%= cancerType.getCancerTypeId() %>").load('cross_cancer_summary.do?gene_list=BRCA1&cancer_study_id=<%= cancerType.getCancerTypeId() %>');
    <% } %>
});
</script>

<h1>X-Cancer Study Results</h1>

    <%
        for (CancerType cancerType:  cancerTypes) {
            out.println ("<div class=\"cross_cancer_panel\">");
            out.println ("<B>" + cancerType.getCancerName() + "</B>");
            out.println ("<div class='cross_cancer_ajax' id=\"study_" + cancerType.getCancerTypeId() + "\">");
            out.println ("<img src='images/ajax-loader2.gif'>");
            out.println ("</div>");
            out.println ("</div>");
        }
    %>

    <!-- End DIV for id="content" -->
    </div>

    </td>
    <td width="172">
	<jsp:include page="global/right_column.jsp" flush="true" />
    </td>
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="global/footer.jsp" flush="true" />    
    </td>
  </tr>
</table>
</div>
</center>
<jsp:include page="global/xdebug.jsp" flush="true" />
</body>
</html>

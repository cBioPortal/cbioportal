<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.portal.model.*" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.portal.util.*" %>


<%
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");
    String popeye = globalConfig.getProperty("popeye");

    if (popeye == null) {
        popeye = "preview.jsp";
    } 
    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
%>

<%
    ServletXssUtil xssUtil = ServletXssUtil.getInstance();
    ArrayList<CancerType> cancerTypeList = (ArrayList<CancerType>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
    ArrayList<GeneticProfile> profileList = (ArrayList<GeneticProfile>) request.getAttribute
            (QueryBuilder.PROFILE_LIST_INTERNAL);
    String cancerTypeId = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);
    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute
            (QueryBuilder.GENETIC_PROFILE_IDS);

    ArrayList<CaseSet> caseSets = (ArrayList<CaseSet>)
            request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String caseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String caseIds = xssUtil.getCleanInput(request, QueryBuilder.CASE_IDS);
    String geneList = xssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);
    String geneSetChoice = request.getParameter(QueryBuilder.GENE_SET_CHOICE);
    if (geneSetChoice == null) {
        geneSetChoice = "custom";
    }

    String tabIndex = xssUtil.getCleanInput(request, QueryBuilder.TAB_INDEX);
    if (tabIndex == null) {
        tabIndex = QueryBuilder.TAB_VISUALIZE;
    } else {
        tabIndex = URLEncoder.encode(tabIndex);
    }

    request.setAttribute("index.jsp", Boolean.TRUE);
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    String cgdsUrl = GlobalProperties.getCgdsUrl();
    String cgdsUrlHome = cgdsUrl.replace("webservice.do", "");
    String xdebug = xssUtil.getCleanInput(request, QueryBuilder.XDEBUG);
    String userMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
%>

<script type="text/javascript">

    function swapTabs(param) {
        var form = YAHOO.util.Dom.get("main_form");
        var hiddenTab = YAHOO.util.Dom.get("<%= QueryBuilder.TAB_INDEX %>");
        hiddenTab.value = param;
        form.submit();
    }
    // Store the current selected cancer study ID as a global variable.
    window.cancer_study_id_selected = '<%= cancerTypeId%>';

</script>

<jsp:include page="global/header.jsp" flush="true" />

<!-- Include Dynamic Query Javascript -->
<script type="text/javascript" src="js/dynamicQuery.js"></script>

    <%
    if (xdebug != null) {
    %>
    <form id="main_form" action="index.do" method="get">
    <% } else { %>
    <form id="main_form" action="index.do" method="get">
    <% } %>
    <input type="hidden" id="<%= QueryBuilder.TAB_INDEX %>" name="<%= QueryBuilder.TAB_INDEX %>"
           value="<%= tabIndex %>">
    <%
        if (xdebug != null) {
        %>
            <input type="hidden" name="xdebug" value="<%= xdebug %>">
        <%
        }
    %>

    <table cellspacing="2px">
        <tr>
            <td>
            <div class="welcome">
                <table>
                <tr>
                   <td style="width: 350px">
                      <P><%= SkinUtil.getBlurb() %></p>
                      <p>The portal is developed and maintained by
                      the <a href="http://cbio.mskcc.org/">Computational Biology Center</a>
                      at <br><a href="http://www.mskcc.org/">Memorial Sloan-Kettering Cancer Center</a>. </p>
                   </td>
                   <td style="width: 300px">
                       <jsp:include page="<%= popeye %>" flush="true" />
                   </td>
                </tr>
                </table>
            </div>

            <%
            if (userMessage != null) {
                out.println ("<div class='user_message'>" + userMessage + "</div>");
            }
            %>                  
            <div class="main_query_panel">
                <%@ include file="step1_json.jsp" %>
                <%@ include file="step2_json.jsp" %>
                <%@ include file="step3_json.jsp" %>
                <%@ include file="step4_json.jsp" %>
                <%@ include file="step5_json.jsp" %>
                <p/>
                <input type=submit name="<%= QueryBuilder.ACTION%>" value="<%= QueryBuilder.ACTION_SUBMIT %>"/>

                <p><small><a id='json_cancer_studies' href="">Toggle Experimental JSON Results</a></small></p>
                <div class="markdown" style="display:none;" id="cancer_results">
                </div>
            </div>
            </td>
        </tr>
    </table>
    </td>
    <td width="172">
	<jsp:include page="global/right_column.jsp" flush="true" />
    </td>
  </tr>
  <tr>
    <td colspan="3">
        <script type="text/javascript">
            $(document).ready(function() {
               window.sessionStorage.clear(); 
            });
        </script>
	<jsp:include page="global/footer.jsp" flush="true" />    
    </td>
  </tr>
</table>
</center>
</div>
</form>
<jsp:include page="global/xdebug.jsp" flush="true" />
</body>
</html>
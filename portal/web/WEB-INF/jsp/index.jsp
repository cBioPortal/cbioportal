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
        geneSetChoice = "user-defined-list";
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

<jsp:include page="global/header.jsp" flush="true" />

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

            <small>
            Developer Notes:
            <ul>
                <li>Data Download Tab will be added back soon...</li>
                <li>Form validation is still a work in progress...</li>
                <li>Losing state upon back button is an open, unresolved issue...</li>
                <li>See other <a href="http://code.google.com/p/cbio-cancer-genomics-portal/issues/list">open issues</a>...</li>
            </ul>
            </small>
            <p>

            <%@ include file="query_form.jsp" %>

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
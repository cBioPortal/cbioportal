<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>

<%
    org.mskcc.portal.servlet.ServletXssUtil xssUtil = ServletXssUtil.getInstance();
    Config globalConfig = Config.getInstance();
    String siteTitle = SkinUtil.getTitle();
    String popeye = globalConfig.getProperty("popeye");

    if (popeye == null) {
        popeye = "preview.jsp";
    } 
    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
    String tabIndex = xssUtil.getCleanInput(request, QueryBuilder.TAB_INDEX);
    if (tabIndex == null) {
        tabIndex = QueryBuilder.TAB_VISUALIZE;
    } else {
        tabIndex = URLEncoder.encode(tabIndex);
    }
%>

<%
    request.setAttribute("index.jsp", Boolean.TRUE);
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    String userMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
%>

<jsp:include page="global/header.jsp" flush="true" />

    <table cellspacing="2px">
        <tr>
            <td>
            <div class="welcome">
                <table>
                <tr>
                   <td valign=top>
                      <P><%= SkinUtil.getBlurb() %></p>
                      <p>The portal is developed and maintained by
                      the <a href="http://cbio.mskcc.org/">Computational Biology Center</a>
                      at <br><a href="http://www.mskcc.org/">Memorial Sloan-Kettering Cancer Center</a>. </p>
                   </td>
                   <td valign=top>
                       <jsp:include page="<%= popeye %>" flush="true" />
                   </td>
                </tr>
                </table>
            </div>

            <%
            if (userMessage != null) { %>
                <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:95%;margin-top:10px;margin-bottom:20px">
                    <p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
                    <strong><%= userMessage %></strong></p>
                </div>
            <% } %>

            <%
                //  Outputs Query and Download Tabs
                if (tabIndex.equals(QueryBuilder.TAB_VISUALIZE)) {
                    out.println ("<span class='tab_active'>Query</span>");
                    out.println ("<span class='tab_inactive'><a id='download_tab' href=''>Download Data</a></span>");
                } else {
                    out.println ("<span class='tab_inactive'><a id='query_tab' href=''>Query</a></span></span>");
                    out.println ("<span class='tab_active'>Download Data</span>");
                }
            %>
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
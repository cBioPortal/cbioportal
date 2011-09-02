<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.GlobalProperties" %>
<%@ page import="org.mskcc.portal.util.Config" %>

<%
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");

    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
%>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Web Interface"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
<div id="main">
    <table cellspacing="2px">
        <tr>
            <td>
            <h1>Web Interface</h1>
            <div class="markdown">
            <P><jsp:include page="content/web_api.html" flush="true" /></p>

            </div>
            </td>
        </tr>
    </table>
</div>
    </td>
    <td width="172">
	<jsp:include page="WEB-INF/jsp/global/right_column.jsp" flush="true" />
    </td>
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</center>
</div>
</form>
<jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />
</body>
</html>

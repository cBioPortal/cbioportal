<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>

<%
    Config globalConfig = Config.getInstance();
    String siteTitle = SkinUtil.getTitle();
%>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::OncoPrints"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
    <table cellspacing="2px">
        <tr>
            <td>
            <div class="markdown">
            <P><jsp:include page="content/oncoprints.html" flush="true" /></p>

            </div>
            </td>
        </tr>
    </table>
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
<jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />
</body>
</html>

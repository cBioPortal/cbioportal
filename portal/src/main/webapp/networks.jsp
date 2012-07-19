<%
    Config globalConfig = Config.getInstance();
    String siteTitle = SkinUtil.getTitle();
%>

<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>


<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::What's New"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
<div id="main">
    <table cellspacing="2px">
        <tr>
            <td>
                <h1>Networks for Cancer Genomics Analysis</h1>
                
            <div class="markdown">

            <P><jsp:include page="content/networks.html" flush="true" /></p>

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

<%
    String siteTitle = SkinUtil.getTitle();
%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.Config" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>


<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::CGDS R Library"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
   <div id="main">
       <table cellspacing="2px">
        <tr>
            <td>
            <h1>R/MATLAB Packages</h1>
            <div class="markdown">
            <p><jsp:include page="content/cgds_r.html" flush="true" /></p>
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

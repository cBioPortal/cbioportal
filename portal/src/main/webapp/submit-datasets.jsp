<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%
    String siteTitle = GlobalProperties.getTitle();
%>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Web Interface"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
<div id="main">
    <table cellspacing="2px">
        <tr>
            <td>
            <h1>Data submission</h1>
            <div>
		Please contact us at <a href="mailto:cbioportal@cbio.mskcc.org" title="Contact us">cbioportal@cbio.mskcc.org</a>
		to submit or suggest published cancer genomics data sets for the cBioPortal.<br/>
		Data sets before publication can also be submitted to a access controlled instance of the cBioPortal.

            </div>
            </td>
        </tr>
    </table>
</div>
<br>
<br>
<br>
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

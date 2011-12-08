<%
    Config globalConfig = Config.getInstance();
    String aboutHtml = globalConfig.getProperty("about");
    String siteTitle = SkinUtil.getTitle();

    if (aboutHtml == null) {
        aboutHtml = "content/about_us.html";
    } else {
        aboutHtml = "content/" + aboutHtml;
    }

%>

<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>


<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::About Us"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />

    <div id="main">
      <table cellspacing="2px">
        <tr>
            <td>
                <h1>About Us</h1>
                <div class="markdown">
                <P><jsp:include page="<%= aboutHtml %>" flush="true" /></p>
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
</div>
</center>
</div>
</form>
<jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />
</body>
</html>

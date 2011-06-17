<%@ page import="org.mskcc.portal.util.SkinUtil" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>

<%


    String userMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");
    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
%>

<jsp:include page="global/header.jsp" flush="true" />
    <table cellspacing="2px">
        <tr>
            <td>
            <h1><%= SkinUtil.getTitle() %></h1>

            <P><%= SkinUtil.getBlurb() %></p>

            <div class="main_panel">
            <h3>Please Log in Below:</h3>

            <%
            if (userMessage != null) {
                out.println ("<div class='user_message'>" + userMessage + "</div>");
            }
            %>
            <form action="authenticate.do" method="POST">
            <table>
                <tr valign="top">
                    <td><P>User Name:</P></td>
                    <td><P><input type="text" name="user_name" size="20"/></P></td>
                </tr>
                <tr valign="top">
                    <td><P>Password:</P></td>
                    <td><P><input type="password" name="password" size="20"/></P></td>
                </tr>
                <tr valign="top">
                    <td>&nbsp;</td>
                    <td><input type="submit" value="Login"/></td>
                </tr>
            </table>
            </form>
            </div>
            </td>
        </tr>
    </table>
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
</center>
</div>
</form>
<jsp:include page="global/xdebug.jsp" flush="true" />
</body>
</html>

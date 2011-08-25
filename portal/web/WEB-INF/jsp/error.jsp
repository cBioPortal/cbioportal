<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.XDebugMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.util.XDebug" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>
<%
    request.setAttribute(QueryBuilder.HTML_TITLE, "cBio Cancer Genomics Pathway Portal::Error");
    String userErrorMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
%>

<jsp:include page="global/header.jsp" flush="true" />

    <table cellspacing="2px">
        <tr>
            <td>
            <h1>Cancer Genomics Pathway Portal</h1>

            <% if (userErrorMessage != null) {
                out.println ("<h4>" + userErrorMessage + "</h4>");
            } else { %>
            <h4>An Error Has Occurred.  Please try again or send email to <%= SkinUtil.getEmailContact() %>.</h4>
              <% } %>
            <br/>
            </td>
        </tr>
    </table>

    <div class="map">
    <table width="100%">
    <%
        XDebug xdebug = (XDebug) request.getAttribute("xdebug_object");
        ArrayList messages = xdebug.getDebugMessages();
        for (int msgIndex = 0; msgIndex < messages.size(); msgIndex++) {
            XDebugMessage msg = (XDebugMessage) messages.get(msgIndex);
    %>
        <tr bgcolor="#ccccff" valign="top">
			<td>Diagnostic:</td>
			<td colspan="2">
				<font color="<%= msg.getColor() %>">
				<%= msg.getMessage() %>
				</font>
            </td>
		</tr>
    <% } %>
    </table>
    </div>
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

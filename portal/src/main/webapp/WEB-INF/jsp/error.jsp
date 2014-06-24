<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%
    request.setAttribute(QueryBuilder.HTML_TITLE, "cBio Cancer Genomics Pathway Portal::Error");
    String userMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
%>

<jsp:include page="global/header.jsp" flush="true" />
            <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:95%;margin-top:10px;margin-bottom:20px">
            <p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
            <% if (userMessage != null) { %>
                <strong>Oops!  <%= userMessage %></strong></p>
            <% } else {%>
                Oops!  An Error Has occurred while processing your request.
              <% } %>
            &nbsp;Please try again or send email to <%= GlobalProperties.getEmailContact() %> if this is any error in this portal.
            </div>
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

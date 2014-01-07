<%
    String siteTitle = GlobalProperties.getTitle();

%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>


<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::What People Are Saying"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />

    <div id="main">
      <table cellspacing="2px">
        <tr>
            <td>
                <div class="markdown">
                    <div id="all_testimonials">
                        <h1>What People Are Saying</h1>
                        <P><jsp:include page="/WEB-INF/jsp/testimonials.jsp" flush="true" /></p>
                    </div>
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

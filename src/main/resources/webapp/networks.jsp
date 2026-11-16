<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - SPDX-License-Identifier: Apache-2.0
 --%>

<%
    String siteTitle = GlobalProperties.getTitle();
%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>


<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::What's New"); %>
<jsp:include page="src/main/webapp/jsp/global/header.jsp" flush="true" />
<div id="main">
    <table cellspacing="2px">
        <tr>
            <td>
                <h1>Networks for Cancer Genomics Analysis</h1>
                
            <div class="markdown">

            <P><%@ include file="content/networks.html" %></p>

            </div>
            </td>
        </tr>
    </table>
</div>
    </td>
    <td width="172">
	<jsp:include page="src/main/webapp/jsp/global/right_column.jsp" flush="true" />
    </td>
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="src/main/webapp/jsp/global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</center>
</div>
</form>
<jsp:include page="src/main/webapp/jsp/global/xdebug.jsp" flush="true" />
</body>
</html>

<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%
    String siteTitle = GlobalProperties.getTitle();
%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<!-- js files: -->
<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/underscore-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/backbone-min.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript" src="js/lib/showdown.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/showdown-github.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/url_based_content.js?<%=GlobalProperties.getAppVersion()%>"></script>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::About Us"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />

    <div id="main">
      <table cellspacing="2px">
        <tr>
            <td>
                <div id="aboutPage" class="markdown"></div>
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

<script>
    $(document).ready( function() {
        // retrieve link for About Us and generate the page
        var aboutLink = '<%= GlobalProperties.getAboutHtml()%>';
        var baseUrl = '<%= GlobalProperties.getBaseUrl()%>';
        var markdownDocumentation = '<%= GlobalProperties.isMarkdownDocumentation()%>';
        var generatePage = new GeneratePage(baseUrl, aboutLink, markdownDocumentation, "#aboutPage");
        generatePage.init();
    });
</script>

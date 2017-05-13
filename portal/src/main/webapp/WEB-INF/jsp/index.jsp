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

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.SessionServiceRequestWrapper" %>
<%@ page import="org.mskcc.cbio.portal.util.*" %>
<%@ page import="java.net.URLEncoder" %>

<%
    //String siteTitle = GlobalProperties.getTitle();
%>

<%
    //request.setAttribute("index.jsp", Boolean.TRUE);
    //request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    //String userMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
%>
    
<%--<jsp:include page="global/header.jsp" flush="true" />--%>


<%--<script>--%>
<%--window.appVersion = '<%=GlobalProperties.getAppVersion()%>';--%>
    <%----%>
<%--window.historyType = 'memory';--%>

<%--// Set API root variable for cbioportal-frontend repo--%>
<%--<%--%>
<%--String url = request.getRequestURL().toString();--%>
<%--String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();--%>
<%--baseURL = baseURL.replace("https://", "").replace("http://", "");--%>
<%--%>--%>
<%--__API_ROOT__ = '<%=baseURL%>';--%>
    <%----%>
<%--window.loadReactApp({ defaultRoute: 'home' });--%>

<%--window.onReactAppReady(function(){--%>
    <%--window.renderQuerySelector(document.getElementById("querySelector"));--%>
<%--});   --%>

    <%----%>
<%--</script>--%>

<%--<script type="text/javascript">--%>

<%--// non-jQuery function to display IE warning message--%>
<%--// TODO we may want to move this function into cbio.util--%>
<%--(function() {--%>
	<%--if (cbio.util.browser.msie)--%>
	<%--{--%>
		<%--var detectIE = function()--%>
		<%--{--%>
			<%--var version = cbio.util.browser.version;--%>
			<%--//version = /^([0-9]+)/.exec(version);--%>
			<%--if (version && version.length && parseInt(version) <= 10)--%>
			<%--{--%>
				<%--// show warning messages for IE 8 or below--%>
				<%--document.getElementById("ie10-warning").style.display = "block";--%>
			<%--}--%>
		<%--};--%>

		<%--if (window.addEventListener)--%>
		<%--{--%>
			<%--window.addEventListener('load', detectIE, false);--%>
		<%--}--%>
		<%--else if (window.attachEvent)--%>
		<%--{--%>
			<%--window.attachEvent('onload', detectIE);--%>
		<%--}--%>
	<%--}--%>
<%--})();--%>
<%--</script>--%>
    
<%--<p id="ie10-warning" style="background-color:red;display:none;">--%>
    <%--<img src="images/warning.gif" alt="warning"/>--%>
    <%--You are using an old version of Internet Explorer. For better performance, we recommend--%>
    <%--using <b>Google Chrome, Firefox, Safari, or Internet Explorer V11 to visit this web site</b>.--%>
<%--</p>--%>

<%--<%--%>
<%--String dbError = (String) request.getAttribute(QueryBuilder.DB_ERROR);--%>
<%--if (dbError != null && userMessage != null) {  %>--%>
<%--<p id="db-warning" style="background-color:red;display:block;">--%>
    <%--<img src="images/warning.gif" alt="warning"/>--%>
    <%--The version of the portal is out of sync with the database! Please contact the site administrator to update the database.<br/><%= dbError %>--%>
<%--</p>--%>
<%--<% }--%>
<%--String species = GlobalProperties.getSpecies();--%>
<%--if (!(species.equals("human") || species.equals("mouse"))) { %>--%>
<%--<p id="species-warning" style="background-color:red;display:block;">--%>
    <%--<img src="images/warning.gif" alt="warning"/>--%>
    <%--The species defined is not supported. Please check the portal.properties file.<br/><%= species %>--%>
<%--</p>--%>
<%--<% }--%>
<%--String sessionError = (String) request.getAttribute(SessionServiceRequestWrapper.SESSION_ERROR);--%>
<%--if (sessionError != null) {  %>--%>
<%--<p id="session-warning" style="background-color:red;display:block;">--%>
    <%--<img src="images/warning.gif"/>--%>
    <%--<%= sessionError %>--%>
<%--</p>--%>
<%--<% } %>--%>

    <%--<table cellspacing="2px">--%>
        <%--<tr>--%>
            <%--<td>--%>
            <%--<div class="welcome">--%>
                <%--<!-- added width style attribute to keep the image at the right side -->--%>
                <%--<table style="width: 100%">--%>
                <%--<tr>--%>
                   <%--<td valign=top>--%>
	              <%--<div style="position: relative; z-index: 999;">--%>
                      <%--<!-- removed hard coded part and added it to the blurb text-->--%>
                        <%--<p><%= GlobalProperties.getBlurb() %></p>--%>

                        <%--</div>--%>
                        <%--<br/>--%>
                   <%--</td>--%>
                   <%--<td valign=top>--%>
			<%--<jsp:include page="<%= popeye %>" flush="true" />--%>
                   <%--</td>--%>
                <%--</tr>--%>
                <%--</table>--%>
            <%--</div>--%>

            <%--<%--%>
            <%--if (userMessage != null) { %>--%>
                <%--<div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:95%;margin-top:10px;margin-bottom:20px">--%>
                    <%--<p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>--%>
                    <%--<strong><%= userMessage %></strong></p>--%>
                <%--</div>--%>
            <%--<% } %>--%>
    <%----%>
            <%--</td>--%>
        <%--</tr>--%>
    <%--</table>--%>


    <%--<div id="reactRoot" class="cbioportal-frontend hidden"></div>--%>
    <%--<div id="querySelector" class="cbioportal-frontend"></div>--%>


    <%--</td>--%>
    <%--<td>--%>
	<%--<jsp:include page="global/right_column.jsp" flush="true" />--%>
    <%--</td>--%>
  <%--</tr>--%>
  <%--<tr>--%>
    <%--<td colspan="3">--%>
        <%--<script type="text/javascript">--%>
          <%----%>
        <%--</script>--%>
	<%--<jsp:include page="global/footer.jsp" flush="true" />    --%>
    <%--</td>--%>
  <%--</tr>--%>
<%--</table>--%>
<%--</center>--%>
<%--</div>--%>
<%--</form>--%>

    <%----%>
    <%----%>
    <%----%>
    <%----%>
<%--</body>--%>
<%--</html>--%>


<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:template title="My page">

    <jsp:attribute name="head_area">
        <script>

        window.historyType = 'memory';
        
        window.loadReactApp({ defaultRoute: 'home' });
        
        
        window.onReactAppReady(function(){
            var el = document.getElementById("querySelector");
            console.log(el);
            window.renderQuerySelector(document.getElementById("querySelector"));
        });
        </script>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <div id="reactRoot" class="hidden"></div>
        <div id="querySelector"></div>
    </jsp:attribute>


    
</t:template>
    
<jsp:include page="global/xdebug.jsp" flush="true" />
     
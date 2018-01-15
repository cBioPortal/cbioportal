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
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%
    String siteTitle = GlobalProperties.getTitle()+"::Onco Query Language (OQL) Instructions";
    String appVersion = GlobalProperties.getAppVersion();
%>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:template title="<%= siteTitle %>" defaultRightColumn="false" fixedWidth="true" cssClass="newsPage">

    <jsp:attribute name="head_area">
        <link rel="stylesheet" type="text/css" href="css/gfm.css"/>
        <!-- js files: -->
        <script type="text/javascript" src="js/lib/jquery.min.js?${appVersion}"></script>
        <script type="text/javascript" src="js/lib/underscore-min.js?${appVersion}"></script>
        <script type="text/javascript" src="js/lib/backbone-min.js?${appVersion}"></script>

        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/showdown/1.8.5/showdown.min.js?${appVersion}"></script>
        <script type="text/javascript" src="js/lib/showdown-github.min.js?${appVersion}"></script>
        <script type="text/javascript" src="js/src/url_based_content.js?${appVersion}"></script>
        
        <script>
        window.loadReactApp({ defaultRoute: 'blank' });
        </script>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <div id="oqlSpec" class="markdown-body"></div>
        <div id="reactRoot" class="hidden"></div>
    </jsp:attribute>


</t:template>



<!-- Initialization script -->
<script>
    $(document).ready( function() {
        // retrieve link for News and generate the page
        var oqlLink = '<%= GlobalProperties.getOqlHtml()%>';
        var baseUrl = '<%= GlobalProperties.getBaseUrl()%>';
        var markdownDocumentation = '<%= GlobalProperties.isMarkdownDocumentation()%>';
        var generatePage = new GeneratePage(baseUrl, oqlLink, markdownDocumentation, "#oqlSpec");
        generatePage.init();
    });
</script>


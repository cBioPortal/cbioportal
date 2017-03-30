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
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.DrugsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="org.mskcc.cbio.portal.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import= "java.net.URL" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="org.mskcc.cbio.portal.util.IGVLinking" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
    
<jsp:include page="../global/xdebug.jsp" flush="true" />

<!DOCTYPE HTML>
<html lang="eng" class="cbioportal-frontend">
<head>
    
<title><%= request.getAttribute(QueryBuilder.HTML_TITLE)%></title>
    
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">


<title><%= request.getAttribute(QueryBuilder.HTML_TITLE)%></title>

<script src="/js/src/load-frontend.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script>

window.loadReactApp({ defaultRoute: 'patient' });
    
</script>
    
    
<script type="text/javascript">

    window.appVersion = '<%=GlobalProperties.getAppVersion()%>';

    // Set API root variable for cbioportal-frontend repo
    <%
    String url = request.getRequestURL().toString();
    String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
    baseURL = baseURL.replace("https://", "").replace("http://", "");
    %>
    __API_ROOT__ = '<%=baseURL%>' + '/api';

</script>
    
<link rel="stylesheet" href="/css/header.css?<%=GlobalProperties.getAppVersion()%>" />

</head>

<body>


<div class="pageTopContainer">
    <div class="contentWidth">
        <jsp:include page="../global/header_bar.jsp" flush="true" />
    </div>
</div>


<div id="reactRoot"></div>
    
<jsp:include page="../global/footer.jsp" flush="true" />

</body>
</html>

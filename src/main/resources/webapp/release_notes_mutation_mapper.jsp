<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - SPDX-License-Identifier: Apache-2.0
 --%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%
    String siteTitle = GlobalProperties.getTitle();
%>

<head>
<link href="css/cancergenomics.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/style.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
</head>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::MutationMapper"); %>
<body>

    <div id="instructions">
    <div class="markdown">
            <p><%@ include file="content/release_notes_mutation_mapper.html" %></p>
    </div>
    <jsp:include page="src/main/webapp/jsp/global/footer.jsp" flush="true" />
    </div>
</body>
</html>

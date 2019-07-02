<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/xml; charset=UTF-8" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.parser.*" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%

if (GlobalProperties.showSitemaps() == false) {
    response.setStatus(404);
} else {
    response.setHeader("X-Robots-Tag", "noindex");

String protocol = (request.isSecure()) ? "https" : "http";
String studyId = request.getParameter("studyId");
pageContext.setAttribute("studyId", request.getParameter("studyId"));
pageContext.setAttribute("serverRoot", protocol + "://" + request.getServerName());

%>


<c:if test = "${GlobalProperties.showSitemaps()}">
   <c:import var="patientJson" url="${serverRoot}/api/studies/${studyId}/patients"/>
</c:if>

<%

if (GlobalProperties.showSitemaps()) {
    String json = (String)pageContext.getAttribute("patientJson");
    Object obj = new JSONParser().parse(json);
    JSONArray ja = (JSONArray) obj;   
    pageContext.setAttribute("patientList", ja);
} else {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
}

%>

<c:if test = "${GlobalProperties.showSitemaps()}">
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <c:forEach items="${patientList}" var="patient">
        <url>
            <loc><%=protocol%>://<%=request.getServerName()%>/patient?studyId=${studyId}&amp;caseId=<c:out value="${patient.get('patientId')}"/></loc>
          </url>
    </c:forEach>
</urlset>
</c:if>

<% } %>

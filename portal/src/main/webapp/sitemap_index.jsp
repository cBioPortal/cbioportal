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
String protocol = (request.isSecure()) ? "https" : "http";
pageContext.setAttribute("serverRoot", protocol + "://" + request.getServerName());

if (GlobalProperties.showSitemaps() == false) {
    response.setStatus(404);
} else {

%>

<c:if test = "${GlobalProperties.showSitemaps()}">
    <c:import var="dataJson" url="${serverRoot}/api/studies"/>
</c:if>
<%

if (GlobalProperties.showSitemaps()) {
     String json = (String)pageContext.getAttribute("dataJson");
       Object obj = new JSONParser().parse(json);
       JSONArray ja = (JSONArray) obj;   
       pageContext.setAttribute("mylist", ja);
} else {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
}

%>
<c:if test = "${GlobalProperties.showSitemaps()}">
   <?xml version="1.0" encoding="UTF-8"?>
   <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
       <c:forEach items="${mylist}" var="obj">
           <sitemap>
                 <loc><%=protocol%>://<%=request.getServerName()%>/sitemap_study.xml?studyId=<c:out value="${obj.get('studyId')}"/></loc>
            </sitemap>
       </c:forEach>
   </sitemapindex>

</c:if>

<% } %>


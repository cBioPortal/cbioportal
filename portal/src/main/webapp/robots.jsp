<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/plain; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%
String protocol = (request.isSecure()) ? "https" : "http";
if (!GlobalProperties.showSitemaps()) {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
}
%>
<c:if test = "${GlobalProperties.showSitemaps()}">
Sitemap: <%=protocol%>://<%=request.getServerName()%>/sitemap_index.xml
</c:if>

<c:if test = "${GlobalProperties.showSitemaps()==false}">
User-agent: *
Disallow: /
</c:if>



<%@ tag import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ tag import="org.mskcc.cbio.portal.servlet.CheckDarwinAccessServlet" %>

<!DOCTYPE html>
<%@tag description="Simple Template" pageEncoding="UTF-8" %>

<%@attribute name="title" %>
<%@attribute name="head_area" fragment="true" %>
<%@attribute name="body_area" fragment="true" %>

<html class="cbioportal-frontend">
<head>
    
<title>${title}</title>

<link rel="stylesheet" href="css/header.css?<%=GlobalProperties.getAppVersion()%>" />
    
<script type="text/javascript">

window.enableDarwin = <%=CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties()%>;

window.appVersion = '<%=GlobalProperties.getAppVersion()%>';

// Set API root variable for cbioportal-frontend repo
<%
String url = request.getRequestURL().toString();
String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
baseURL = baseURL.replace("https://", "").replace("http://", "");
%>
__API_ROOT__ = '<%=baseURL%>';

</script>

<script src="js/src/load-frontend.js?<%=GlobalProperties.getAppVersion()%>"></script>

<jsp:invoke fragment="head_area"/>
    
    
</head>
<body>
    <div class="pageTopContainer">
    <div class="contentWidth">
        <jsp:include page="/WEB-INF/jsp/global/header_bar.jsp" />
    </div>
    </div>
    
    <div class="contentWrapper">
        <div class="mainContent"><jsp:invoke fragment="body_area"/></div>
        <div class="rightBar"></div>
    </div>
    
    
    <jsp:include page="/WEB-INF/jsp/global/footer.jsp" />
    
    </body>
</html>
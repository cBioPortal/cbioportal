<%@ tag import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ tag import="org.mskcc.cbio.portal.servlet.CheckDarwinAccessServlet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<%@tag description="Simple Template" pageEncoding="UTF-8" %>

<%@attribute name="title" %>
<%@attribute name="defaultRightColumn" %>
<%@attribute name="fixedWidth" %>
<%@attribute name="twoColumn" %>
<%@attribute name="noMargin" %>
<%@attribute name="cssClass" %>

    <%@attribute name="head_area" fragment="true" %>
<%@attribute name="body_area" fragment="true" %>
    <%@attribute name="right_column" fragment="true" %>

<html class="cbioportal-frontend">
<head>
    
<title>${title}</title>

<link rel="stylesheet" href="css/header.css?<%=GlobalProperties.getAppVersion()%>" />
    
<script type="text/javascript">

window.enableDarwin = <%=CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties()%>;

window.appVersion = '<%=GlobalProperties.getAppVersion()%>';
    
// this prevents react router from messing with hash in a way that could is unecessary (static pages)
// or could conflict
window.historyType = 'memory';

// Set API root variable for cbioportal-frontend repo
<%
String url = request.getRequestURL().toString();
String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
baseURL = baseURL.replace("https://", "").replace("http://", "");

String bodyClasses = "";

if (fixedWidth == "true") {
    bodyClasses += "fixedWidth";
} 
if (twoColumn == "true" ||  defaultRightColumn == "true") {
    bodyClasses += " twoColumn";
} 
if (noMargin == "true") {
    bodyClasses += " noMargin";
} 
if (cssClass != null) {
    bodyClasses += " " + cssClass;
}

%>
__API_ROOT__ = '<%=baseURL%>';
    

</script>

<script src="js/src/load-frontend.js?<%=GlobalProperties.getAppVersion()%>"></script>
    
<jsp:invoke fragment="head_area"/>
    
</head>
    
    <body class="<%=bodyClasses.trim()%>">

    
    <div class="pageTopContainer">
    <div class="contentWidth">
        <jsp:include page="/WEB-INF/jsp/global/header_bar.jsp" />
    </div>
    </div>
    
    <div class="contentWrapper">
            <div class="contentWidth">
            <div id="mainColumn"><jsp:invoke fragment="body_area"/></div>
            
            <c:if test="${defaultRightColumn == true || twoColumn==true}">
                <div id="rightColumn"><jsp:invoke fragment="right_column"/></div>
            </c:if>
           
            </div>
    </div>
    
    </div>
    
    <jsp:include page="/WEB-INF/jsp/global/footer.jsp" />

    <c:if test="${defaultRightColumn == true}">
        <script>

        window.renderRightBar(document.getElementById('rightColumn'));

        </script>
    </c:if>
    
    </body>
</html>
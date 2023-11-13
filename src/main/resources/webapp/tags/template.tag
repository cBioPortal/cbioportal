<%@ tag import="org.mskcc.cbio.portal.util.GlobalProperties" %>
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
    
<link rel="icon" href="images/cbioportal_icon.png"/>
<jsp:include page="/WEB-INF/jsp/global/frontend_config.jsp" />
<link rel="stylesheet" href="css/header.css?<%=GlobalProperties.getAppVersion()%>" />
<script src="js/src/load-frontend.js?<%=GlobalProperties.getAppVersion()%>"></script>

<jsp:invoke fragment="head_area"/>

<%
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
    
    
    <script>
    // mark nav item as selected
    $(document).ready(function() {
        var pathname = window.location.pathname;
        var start = pathname.lastIndexOf("/")+1;
        var filename = pathname.substring(start);
        $('#main-nav ul li').each(function(index) {
            var currentPage = $(this).find('a').attr('href');
            if (currentPage === filename) {
                $('#main-nav ul li').removeClass('selected');
                $(this).addClass('selected');
                return false;
            }
        });
    });
    </script>

    <jsp:include page="/WEB-INF/jsp/global/js_include_analytics_and_email.jsp" />
    
    </body>
</html>

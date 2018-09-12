<%
    String url = request.getRequestURL().toString();
    String baseUrl = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
    baseUrl = baseUrl.replace("https://", "").replace("http://", "");
%>

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%
    String principal = "";
    String authenticationMethod = GlobalProperties.authenticationMethod();
    pageContext.setAttribute("authenticationMethod", authenticationMethod);
    if (authenticationMethod.equals("openid") || authenticationMethod.equals("ldap")) {
        principal = "principal.name";
    }
    else if (authenticationMethod.equals("googleplus") ||
	    		authenticationMethod.equals("saml") ||
	    		authenticationMethod.equals("ad") ||
	    		authenticationMethod.equals("social_auth")) {
        principal = "principal.username";
    }
    pageContext.setAttribute("principal", principal);
%>

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<!DOCTYPE html>
<html class="cbioportal-frontend">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>
    <link rel="icon" href="/images/cbioportal_icon.png"/>
    <title>cBioPortal for Cancer Genomics</title>
    <script>
        window.frontendConfig = {
            configurationServiceUrl:"//" + '<%=baseUrl%>' +  "/config_service.jsp",
            sessionServiceUrl:'http://www.cbioportal.org/rc/api-legacy/proxy/session/',
            frontendUrl: '//'+ '<%=baseUrl%>/', 
            appVersion: '<%=GlobalProperties.getAppVersion()%>',
            apiRoot: '//'+ '<%=baseUrl%>/',
            baseUrl: '<%=baseUrl%>',
            basePath: '<%=request.getContextPath()%>',
            googleAnalyticsProfile:'UA-85438068-3',
            skinFaqSourceURL:"https://docs.google.com/document/d/e/2PACX-1vSWTtIJZF2tuBimihr8ke-d00DpKh7fydFIQb5xYpE_bMYM9hZyY9OP1Vz1Ts0ow7ob-3h2S19cuB5O/pub?embedded=true",
              // customTabs:[
                        //     {
                        //         "title": "Custom Tab",
                        //         "location": "RESULTS_PAGE",
                        //         "mountCallbackName": "renderCustomTab1",
                        //         "pathsToJs":["http://127.0.0.1:8080/customTab1.js"],
                        //         "showWithMultipleStudies": true,
                        //         "customParameters": { example:1 },
                        //         "unmountOnHide":false
                        //     }
                        // ]
        };
        
        window.localdev = localStorage.localdev === 'true';
        window.localdist = localStorage.localdist === 'true';
        window.heroku = localStorage.heroku;
        
        if (window.localdev) {
            window.frontendConfig.frontendUrl = "//localhost:3000/"
            localStorage.setItem("e2etest", "true");
        } else if (window.heroku) {
            window.frontendConfig.frontendUrl = ['//',localStorage.heroku,'.herokuapp.com','/'].join('');
            localStorage.setItem("e2etest", "true");
        } 
        
        <sec:authorize access="!hasRole('ROLE_ANONYMOUS')">
             window.frontendConfig.authUserName = ${principal};
            <c:choose>
                <c:when test="${authenticationMethod == 'saml'}">
                     window.frontendConfig.authLogoutUrl = "/saml/logout"};
                </c:when>
                <c:otherwise>
                     window.frontendConfig.authLogoutUrl = "j_spring_security_logout";
                </c:otherwise>
            </c:choose>
        </sec:authorize>
        
      
        
        <% if (authenticationMethod.equals("social_auth")) { %>

            <sec:authorize access="hasRole('ROLE_ANONYMOUS')">
                window.frontendConfig.authGoogleLogin = true; 
            </sec:authorize>
    
        <% } %> 
        

    </script>
    <script type="text/javascript">
    function openSoicalAuthWindow() {
        var _window = open('login.jsp', '', 'width=1000, height=800');
    
        var interval = setInterval(function() {
            try {
                if (_window.closed) {
                    clearInterval(interval);
                } else if (_window.document.URL.includes(location.origin) &&
                            !_window.document.URL.includes(location.origin + '/auth') &&
                            !_window.document.URL.includes('login.jsp')) {
                    _window.close();
    
                    setTimeout(function() {
                        clearInterval(interval);
                        if(window.location.pathname.includes('/study')) {
                            $('#rightHeaderContent').load(' #rightHeaderContent');
                            iViz.vue.manage.getInstance().showSaveButton= true
                        } else {
                            location.reload();
                        }
                    }, 500);
                }
            } catch (err) {
                console.log('Error while monitoring the Login window: ', err);
            }
        }, 500);
    };
    </script>
    <script type="text/javascript" src="//<%=baseUrl%>/js/src/load-frontend.js?<%=GlobalProperties.getAppVersion()%>"></script>       
    <script>
        window.frontendConfig.customTabs && window.frontendConfig.customTabs.forEach(function(tab){
            if (tab.pathsToJs) {
                tab.pathsToJs.forEach(function(src){
                    document.write('<scr'+'ipt type="text/javascript" src="'+ src +'"></sc'+'ript>');
                });
            }
        });
    </script>

    <script>
            loadReactApp(window.frontendConfig);
    </script>

</head>

<body>
    <div id="reactRoot"></div>
    
    
    
</body>
</html>

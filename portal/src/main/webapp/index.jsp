<%
    String url = request.getRequestURL().toString();
    String baseUrl = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
    baseUrl = baseUrl.replace("https://", "").replace("http://", "");
%>

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<!DOCTYPE html>
<html class="cbioportal-frontend">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>
    <link rel="icon" href="/images/cbioportal_icon.png"/>
    <title>cBioPortal for Cancer Genomics</title>
    <script type="text/javascript" src="//<%=baseUrl%>/js/src/load-frontend.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script>
        

        window.frontendConfig = {
            configurationServiceUrl:"http://www.cbioportal.org/rc/config_service.jsp",
            frontendUrl: '//localhost:8080/', 
            baseUrl: '<%=baseUrl%>',
            basePath: '<%=request.getContextPath()%>',
            apiRoot:'www.cbioportal.org/rc',
            sessionServiceUrl:'http://www.cbioportal.org/rc/api-legacy/proxy/session/',
            disabledTabs:[],
            skinShowDataSetsTab:true,
            skinShowWebAPITab:true,
            skinShowRmatLABTab:true,
            skinShowTutorialsTab:true,
            skinShowAboutTab:true,
            skinIsMarkdownDocumentation:true,
            skinDocumentationBaseUrl:"https://raw.githubusercontent.com/cbioportal/cbioportal/release-1.15.0/docs",
            skinFaqSourceURL:"https://docs.google.com/document/d/e/2PACX-1vSWTtIJZF2tuBimihr8ke-d00DpKh7fydFIQb5xYpE_bMYM9hZyY9OP1Vz1Ts0ow7ob-3h2S19cuB5O/pub?embedded=true",
            skinRightNavWhatsNewBlurb : '<p> &bull;<a href="news"> <b>New data and features released</b></a><br/> &bull;<a href="visualize"> <b>New tools released</b></a> </p> <form action="http://groups.google.com/group/cbioportal-news/boxsubscribe"> &nbsp;&nbsp;&nbsp;&nbsp;<b>Sign up for low-volume email news alerts:</b></br> &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="email"> <input type="submit" name="sub" value="Subscribe"> </form> &nbsp;&nbsp;&nbsp;&nbsp;<b>Or follow us <a href="http://www.twitter.com/cbioportal"><i>@cbioportal</i></a> on Twitter</b>',
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
        
        if (localStorage.localdev === 'true') {
            window.frontendConfig.frontendUrl = "//localhost:3000/"
            localStorage.setItem("e2etest", "true");
        } else if (localStorage.heroku) {
            window.frontendConfig.frontendUrl = ['//',localStorage.heroku,'.herokuapp.com','/'].join('');
            localStorage.setItem("e2etest", "true");
        } 

    </script>
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

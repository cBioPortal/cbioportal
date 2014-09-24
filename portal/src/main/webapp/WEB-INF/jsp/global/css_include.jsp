<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%
    String authenticationMethod = GlobalProperties.authenticationMethod();
    String global_style = GlobalProperties.getProperty("global_css");
    String special_style = GlobalProperties.getProperty("special_css");
    if (global_style == null) {
        global_style = "css/global_portal.css?"+GlobalProperties.getAppVersion();
    } else {
        global_style = "css/" + global_style+"?"+GlobalProperties.getAppVersion();
    }
    if (special_style != null) {
        special_style = "css/" + special_style+"?"+GlobalProperties.getAppVersion();
    }
    String globalStyleCSSPath = (authenticationMethod.equals("saml")) ?
        "/" + global_style : global_style;
    pageContext.setAttribute("globalStyleCSSPath", globalStyleCSSPath);
    String bootstrapCSSPath = "css/bootstrap.min.css"+"?"+GlobalProperties.getAppVersion();
    bootstrapCSSPath = (authenticationMethod.equals("saml")) ?
        "/" + bootstrapCSSPath : bootstrapCSSPath;
    pageContext.setAttribute("bootstrapCSSPath", bootstrapCSSPath);
%>

<!-- Include Global Style Sheets -->
<link rel="icon" href="images/cbioportal_icon.png"/>
<link href="css/responsiveslides.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/tipTip.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/jquery.qtip.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="<c:url value="${globalStyleCSSPath}"/>" type="text/css" rel="stylesheet" />
<% if (special_style != null) { %>
    <link href="<%= special_style %>" type="text/css" rel="stylesheet" />
<% } %>
<link href="css/smoothness/jquery-ui-1.10.3.custom.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/data_table.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/custom_case_set.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>
<link href="css/ui.dropdownchecklist.themeroller.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>
<link href="css/chosen.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>
<link href="<c:url value="${bootstrapCSSPath}"/>" type="text/css" rel="stylesheet"/>

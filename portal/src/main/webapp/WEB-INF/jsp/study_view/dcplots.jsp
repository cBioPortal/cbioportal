<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<link rel="stylesheet" type="text/css" href="css/study-view.css?<%=GlobalProperties.getAppVersion()%>">
<link rel="stylesheet" type="text/css" href="css/introjs.min.css?<%=GlobalProperties.getAppVersion()%>">
<link rel="stylesheet" type="text/css" href="css/introjs-rtl.min.css?<%=GlobalProperties.getAppVersion()%>">

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script src="js/lib/packery.pkgd.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/draggabilly.pkgd.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/intro.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/crossfilter.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/dataTables.fixedColumns.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/dataTables.tableTools.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/jquery.dataTables.fnSetFilteringDelay.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/dc.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/d3.layout.cloud.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script data-main="js/src/study-view/main.js?<%=GlobalProperties.getAppVersion()%>" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>

<div id="dc-plots-loading-wait">
    <img src="images/ajax-loader.gif"/>
</div>

<div id="study-view-main" style="display: none;">
    <div id="study-view-header-function"></div>
    <div id="study-view-top-wrapper"></div>
    
    <div id="study-view-charts"></div>
    
    <div id="study-view-update"></div>
    
    <div id='data-table-chart'></div>

</div>
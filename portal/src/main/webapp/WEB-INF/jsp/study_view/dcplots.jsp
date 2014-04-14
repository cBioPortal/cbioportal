<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>

<link rel="stylesheet" type="text/css" href="css/study-view.css">
<link rel="stylesheet" type="text/css" href="css/introjs.min.css">
<link rel="stylesheet" type="text/css" href="css/introjs-rtl.min.css">

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script src="js/src/study-view/packery.pkgd.min.js"></script>
<script src="js/src/study-view/draggabilly.pkgd.min.js"></script>
<script src="js/src/study-view/intro.min.js"></script>
<script data-main="js/src/study-view/main.js" src="js/require.js"></script>

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
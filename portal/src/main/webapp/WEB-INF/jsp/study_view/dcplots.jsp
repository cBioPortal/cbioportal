<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<link rel="stylesheet" type="text/css" href="css/study-view.css?<%=GlobalProperties.getAppVersion()%>">
<link rel="stylesheet" type="text/css" href="css/introjs.min.css?<%=GlobalProperties.getAppVersion()%>">
<link rel="stylesheet" type="text/css" href="css/introjs-rtl.min.css?<%=GlobalProperties.getAppVersion()%>">
<link rel="stylesheet" type="text/css" href="css/bootstrap-alert.css?<%=GlobalProperties.getAppVersion()%>">
<link rel="stylesheet" type="text/css" href="css/animate.css?<%=GlobalProperties.getAppVersion()%>">
<link rel="stylesheet" type="text/css" href="css/fixed-data-table.min.css?<%=GlobalProperties.getAppVersion()%>">
<link rel="stylesheet" type="text/css" href="css/bootstrap-dropdown-checkbox.css?<%=GlobalProperties.getAppVersion()%>">
<script src="js/lib/bootstrap-notify.min.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript" src="js/lib/backbone-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/packery.pkgd.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/draggabilly.pkgd.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/intro.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/crossfilter.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/dataTables.fixedColumns.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/dataTables.tableTools.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/jquery.dataTables.fnSetFilteringDelay.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/dc.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/d3.layout.cloud.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/react.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/react-dom.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/fixed-data-table.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/react-chosen.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/ZeroClipboard.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/bootstrap-dropdown-checkbox.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script src="js/lib/EnhancedFixedDatatable.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script data-main="js/src/study-view/main.js?<%=GlobalProperties.getAppVersion()%>" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>



<div id="summary-loading-wait">
    <img src="images/ajax-loader.gif" alt="loading" />
</div>

<div id="study-view-main" style="display: none;">
    <div id="study-view-header-function"></div>

    <div id="study-view-charts"></div>

    <div id="study-view-update"></div>

    <div id='data-table-chart'></div>

</div>

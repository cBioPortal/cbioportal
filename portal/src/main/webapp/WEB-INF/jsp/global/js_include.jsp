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

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<!-- Include Global List of Javascript Files to Load -->

<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/bootstrap.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/bootstrap-dialog.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery-migrate.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type='text/javascript' src='https://www.google.com/jsapi?<%=GlobalProperties.getAppVersion()%>'></script>
<script type="text/javascript" src="js/lib/jquery.cookie.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.address.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.expander.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.tipTip.minified.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery-ui.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/responsiveslides.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.dataTables.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.dataTables.two_button.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.quovolver.mini.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.dataTables.ColVis.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.dataTables.fnSetFilteringDelay.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.dataTables.tableTools.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.scrollTo.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.qtip.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/chosen.jquery.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/ui.tabs.paging.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/FileSaver.min.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript" src="js/lib/mailme.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/json2.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/ui.dropdownchecklist-1.4-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/underscore-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/backbone-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/d3.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/plotly.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/igv_webstart.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/genomespace.js"></script>
<script type="text/javascript" src="js/lib/jstat.min.js"></script>
<%--<script src="https://gsui.genomespace.org/jsui/upload/gsuploadwindow.js" type="text/javascript"></script>-->


<%--<script type="text/javascript" src="js/lib/jsmol/JSmol.min.nojq.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jmol/JmolCore.js"></script>
<script type="text/javascript" src="js/lib/jmol/JmolApplet.js"></script>
<script type="text/javascript" src="js/lib/jmol/JmolControls.js"></script>
<script type="text/javascript" src="js/lib/jmol/JmolApi.js"></script>--%>

<script type="text/javascript" src="js/lib/3Dmol-nojquery-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/mutationMapper.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">
    // This is for the moustache-like templates
    // prevents collisions with JSP tags
    _.templateSettings = {
        interpolate : /\{\{(.+?)\}\}/g
    };
</script>

<script type="text/javascript" src="js/src/Models.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cgx_jquery.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/dynamicQuery.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/gene_set.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/QueryGeneData.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cbio-util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cbio-stat.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/download-util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/customCaseSet.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/global-tabs.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/load-frontend.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/gene-symbol-validator.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/d3.right-menu-stats.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/DataProxyFactory.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/download-data-form-validation.js"></script>
<script type="text/javascript" src="js/lib/jstree.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/api/cbioportal-client.js?<%=GlobalProperties.getAppVersion()%>"></script>

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<!-- Include Global List of Javascript Files to Load -->

<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery-migrate.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type='text/javascript' src='https://www.google.com/jsapi'></script>
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
<script type="text/javascript" src="js/lib/jquery.scrollTo.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.qtip.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/chosen.jquery.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/ui.tabs.paging.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript" src="js/lib/mailme.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/json2.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/ui.dropdownchecklist-1.4-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/underscore-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/backbone-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/d3.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/igv_webstart.js?<%=GlobalProperties.getAppVersion()%>"></script>
<!--script type="text/javascript" src="js/lib/rainbowvis.js?<%=GlobalProperties.getAppVersion()%>"></script-->

<script type="text/javascript" src="js/lib/jsmol/JSmol.min.nojq.js?<%=GlobalProperties.getAppVersion()%>"></script>
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
<script type="text/javascript" src="js/src/DataManagerFactory.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cbio-util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/customCaseSet.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/global-tabs.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/gene-symbol-validator.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/d3.right-menu-stats.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/DataProxyFactory.js?<%=GlobalProperties.getAppVersion()%>"></script>

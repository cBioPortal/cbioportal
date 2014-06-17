<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<!-- Include Global List of Javascript Files to Load -->

<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery-migrate.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type='text/javascript' src='https://www.google.com/jsapi?<%=GlobalProperties.getAppVersion()%>'></script>
<script type="text/javascript" src="js/lib/jquery.cookie.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.address.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.expander.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.tipTip.minified.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery-ui.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/responsiveslides.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.dataTables.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
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
<script type="text/javascript" src="js/lib/genomespace.js"></script>
<script src="https://gsui.genomespace.org/jsui/upload/gsuploadwindow.js" type="text/javascript"></script>

<script type="text/javascript" src="js/lib/jmol/JmolCore.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jmol/JmolApplet.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jmol/JmolControls.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jmol/JmolApi.js?<%=GlobalProperties.getAppVersion()%>"></script>

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
<script type="text/javascript" src="js/src/AdvancedDataTable.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/DataTableUtil.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/download-data-form-validation.js"></script>

<script type="text/javascript" src="js/src/mutation/component/mutation_diagram.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/component/mutation_details_table.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/component/mutation_3d_viewer.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/component/mutation_pdb_panel.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/component/mutation_pdb_table.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/model/MutationModel.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/model/PdbAlignmentModel.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/model/PdbChainModel.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/model/PdbModel.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/model/Pileup.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/MutationDetailsUtil.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/MutationViewsUtil.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/MutationDetailsTableFormatter.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/MergedAlignmentSegmentor.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/PdbDataUtil.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/PileupUtil.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/JmolWrapper.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/JSmolWrapper.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/MolScriptGenerator.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/JmolScriptGenerator.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/util/PymolScriptGenerator.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/data/PdbDataProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/data/MutationDataProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/controller/MutationDetailsEvents.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/controller/MainMutationController.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/controller/Mutation3dController.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/controller/MutationDetailsTableController.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/controller/MutationDiagramController.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/controller/MutationDetailsController.js?<%=GlobalProperties.getAppVersion()%>"></script>




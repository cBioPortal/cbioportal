<div class='section' id='mutation_details'>
	<img src='images/ajax-loader.gif'/>
</div>

<style type="text/css" title="currentStyle">
	@import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
	@import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
	@import "css/mutationMapper.min.css?<%=GlobalProperties.getAppVersion()%>";
</style>

<script type="text/javascript">

// TODO 3d Visualizer should be initialized before document get ready
// ...due to incompatible Jmol initialization behavior
var _mut3dVis = null;
_mut3dVis = new Mutation3dVis("default3dView");
_mut3dVis.init();

// Set up Mutation View
$(document).ready(function() {
	var sampleArray = _.keys(PortalGlobals.getPatientSampleIdMap());
	var mutationProxy = DataProxyFactory.getDefaultMutationDataProxy();

	// init default mutation details view

	var options = {
		el: "#mutation_details",
		data: {
			geneList: mutationProxy.getRawGeneList(),
			sampleList: sampleArray
		},
		proxy: {
			mutation: {
				instance: mutationProxy
			}
		}
	};

	var defaultView = MutationViewsUtil.initMutationMapper("#mutation_details",
		options,
		"#tabs",
		"Mutations",
		_mut3dVis);
});

</script>
<div class='section' id='mutation_details'>
	<img src='images/ajax-loader.gif'/>
</div>

<style type="text/css" title="currentStyle">
	@import "css/data_table_jui.css";
	@import "css/data_table_ColVis.css";
	@import "css/mutation_details.css";
</style>

<script type="text/javascript">

// TODO 3d Visualizer should be initialized before document get ready
// ...due to incompatible Jmol initialization behavior
var _mut3dVis = null;
_mut3dVis = new Mutation3dVis("default3dView", {});
_mut3dVis.init();

// Set up Mutation View
$(document).ready(function(){
	// TODO accessing global "samples" variable...
	var sampleArray = samples.trim().split(/\s+/);

	var servletParams = {geneticProfiles: geneticProfiles,
		caseList: samples};

	var servletName = "getMutationData.json";

	// init mutation data proxy with the data servlet config
	var proxy = new MutationDataProxy(geneList);
	proxy.initWithoutData(servletName, servletParams);

	// init default mutation details view

	var model = {mutationProxy: proxy,
		sampleArray: sampleArray};

	var defaultView = new MutationDetailsView(
		{el: "#mutation_details", model: model, mut3dVis: _mut3dVis});

	defaultView.render();
});

</script>
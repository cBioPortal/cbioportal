<div class='section' id='mutation_details'>
	<img src='images/ajax-loader.gif'/>
</div>

<style type="text/css" title="currentStyle">
	@import "css/data_table_jui.css";
	@import "css/data_table_ColVis.css";
	@import "css/mutation/mutation_details.css";
	@import "css/mutation/mutation_table.css";
	@import "css/mutation/mutation_3d.css";
	@import "css/mutation/mutation_diagram.css";
	@import "css/mutation/mutation_pdb_panel.css";
	@import "css/mutation/mutation_pdb_table.css";
</style>

<script type="text/javascript">

// TODO 3d Visualizer should be initialized before document get ready
// ...due to incompatible Jmol initialization behavior
var _mut3dVis = null;
_mut3dVis = new Mutation3dVis("default3dView", {});
_mut3dVis.init();

// Set up Mutation View
$(document).ready(function(){
	var sampleArray = PortalGlobals.getCases().trim().split(/\s+/);

	// init default mutation details view

	var model = {mutationProxy: DataProxyFactory.getDefaultMutationDataProxy(),
		sampleArray: sampleArray};

	var options = {el: "#mutation_details",
		model: model,
		mut3dVis: _mut3dVis};

	var defaultView = MutationViewsUtil.initMutationDetailsView("#mutation_details",
		options,
		"#tabs",
		"Mutations");
});

</script>
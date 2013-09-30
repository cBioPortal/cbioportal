<!-- TODO include these js files in the global js include? -->
<script type="text/javascript" src="js/src/mutation_histogram.js"></script>
<!--script type="text/javascript" src="js/lib/jsmol/JSmol.min.nojq.js"></script-->
<script type="text/javascript" src="js/lib/jmol/JmolCore.js"></script>
<script type="text/javascript" src="js/lib/jmol/JmolApplet.js"></script>
<script type="text/javascript" src="js/lib/jmol/JmolControls.js"></script>
<script type="text/javascript" src="js/lib/jmol/JmolApi.js"></script>
<script type="text/javascript" src="js/src/mutation_3d_viewer.js"></script>
<script type="text/javascript" src="js/src/mutation_pdb_panel.js"></script>

<div class='section' id='mutation_details'>
	<img src='images/ajax-loader.gif'/>
</div>

<style type="text/css" title="currentStyle">
	@import "css/data_table_jui.css";
	@import "css/data_table_ColVis.css";
	#mutation_details .ColVis {
		float: left;
		padding-right:25%;
		margin-bottom: 0;
	}
	#mutation_details .unknown {
		background: #E0E0E0;
		color:black;
		padding-left:5px;
		padding-right:5px;
	}
	#mutation_details .protein_change {
		font-weight: bold;
		font-style: italic;
	}
	#mutation_details .best_effect_transcript {
		color: red;
	}
	#mutation_details .mutation_details_table {
		font-size: 90%;
		color: black;
	}
	#mutation_details .mutation_details_table td {
		font-size: 100%;
	}
	/*
	th.mutation-details-qtip-style {
		/*font-size: 115%;
	}
	*/
	#mutation_details .mutation_table_cosmic {
		/*cursor: pointer;*/
		color: #1974B8;
	}
	#mutation_details .dataTables_filter {
		width: 200px;
	}
	#mutation_details .dataTables_filter input {
		width: 120px;
	}
	#mutation_details .dataTables_info {
		width: 300px;
	}
	#mutation_details .msa-img, #mutation_details .pdb-img {
		vertical-align: bottom;
		margin-left: 3px;
	}
	.diagram-lollipop-tip, .diagram-region-tip, .pdb-chain-tip {
		font-size: 12px;
	}
	.mutation-details-tooltip {
		font-size: 11px !important;
	}
	.mutation_datatables_filter {
		float: right;
		padding-top:3px;
	}
	.mutation_datatables_info {
		float: left;
		padding-top:5px;
		font-size:90%;
	}
	.missense_mutation {
		color: green;
		font-weight: bold;
	}
	.trunc_mutation {
		color: red;
		font-weight: bold;
	}
	.inframe_mutation {
		color: black;
		font-weight: bold;
	}
	.other_mutation {
		color: gray;
		font-weight: bold;
	}
	.diagram_toolbar {
		padding-top: 10px;
		padding-left: 10px;
	}
	.mutation-details-filter-info {
		font-size: 14px;
		font-family: verdana,arial,sans-serif;
		color: red;
		margin-bottom: 10px;
	}
	.mutation-details-filter-reset {
		color: #1974B8 !important;
		cursor: pointer;
	}
	.mutation-details-tabs-ref {
		font-size: 11px !important;
	}
	.mutation-table-highlight {
		background-color: #E9E900 !important;
	}
	.mutation-table-container {
		margin-bottom: 40px;
	}
	.mutation-table-header {
		font-weight: bold !important;
	}
	.mutation-3d-container {
		position: fixed;
		float: right;
		right: 0;
		top: 0;
		z-index: 100;
		border-style: outset;
		border-color: #BABDB6;
		background-color: #FFFFFF;
		padding: 5px 10px 10px;
	}
	.mutation-3d-vis-header {
		padding-bottom: 5px;
	}
	.mutation-3d-info {
		font-size: 14px;
	}
	.mutation-3d-close {
		float: right;
		cursor: pointer;
	}
	.mutation-3d-pdb-id, .mutation-3d-chain-id {
		font-weight: bold;
		font-style: italic;
	}
	.mutation-3d-spin {
		margin-left: 20px;
	}
	.mutation-3d-vis img{
		width: 24px;
		height: 24px
	}
	.cosmic-details-tip-info {
		padding-bottom: 5px;
	}
	.cosmic-details-table {
		font-size: 11px !important;
	}
	.igv-link {
		cursor: pointer;
	}
	.left-align-td {
		text-align: left;
	}
	/* This is to fix ui.tabs.paging plugin style,
	we may need to remove this after updating jQuery */
	.ui-tabs-paging-prev, .ui-tabs-paging-next {
		background: none !important;
		border: none !important;
		line-height: 95%;
	}
	.ui-tabs-paging-next {
		padding-right: 0 !important;
	}
	.ui-tabs-paging-disabled {
		/* do not show button if no cycle */
		display: none;
	}
	/* This is also to fix ui.tabs.paging behavior */
	.mutation-details-content {
		min-width: 480px;
	}

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

	/**
	 * Processes the raw mutation data returned from the servlet, and
	 * initializes the mutation view.
	 *
	 * @param data  raw mutation data returned from the servlet
	 */
	var initMutationView = function(data)
	{
		var model = {mutations: data,
			sampleArray: sampleArray};

		var defaultView = new MutationDetailsView(
			{el: "#mutation_details", model: model, mut3dVis: _mut3dVis});

		defaultView.render();
	};

	// TODO getting these params from global variables defined in visualize.jsp
	// we should refactor/redefine these global variables in a better way

	var params = {geneList: geneList,
		geneticProfiles: geneticProfiles,
		caseList: samples};

	// get mutation data & init view for the current gene and case lists
	$.post("getMutationData.json", params, initMutationView, "json");
});

</script>
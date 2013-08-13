<!-- TODO include these js files in the global js include? -->
<script type="text/javascript" src="js/src/mutation_histogram.js"></script>
<!--script type="text/javascript" src="js/lib/jsmol/JSmol.min.nojq.js"></script-->
<script type="text/javascript" src="js/lib/jmol/JmolCore.js"></script>
<script type="text/javascript" src="js/lib/jmol/JmolApplet.js"></script>
<script type="text/javascript" src="js/lib/jmol/JmolControls.js"></script>
<script type="text/javascript" src="js/lib/jmol/JmolApi.js"></script>

<script type="text/javascript">
	var _singletonJmolApplet = null;

	// TODO temporary test function, may need to create a proper class for 3d viewer
	function _initJmolForMutationDetails()
	{
		var appletName = 'mutation_details_viewer';
		var callbackfun = function(applet) {/*alert('add your callback functions here');*/};

		var jsmolOpts = {
			width: 300,
			height: 180,
			debug: false,
			color: "white",
			//use: "HTML5",
			//j2sPath: "js/jsmol/j2s",
			//script: "load ="+pdbid+";",
			//defaultModel: "$dopamine",
			// TODO this is a fixed test script, a different pdb id should be loaded for each gene
			script: "load =2bq0",
			jarPath: "js/lib/jmol/",
			jarFile: "JmolAppletSigned.jar",
			disableJ2SLoadMonitor: true,
			disableInitialConsole: true
		};

		if (jQuery.isFunction(callbackfun)) {
			jsmolOpts['readyFunction'] = callbackfun;
		}

		return Jmol.getApplet(appletName, jsmolOpts);
	}
</script>

<div id='mutation_3d_structure'>
	<script type="text/javascript">
		// TODO find a better way to init jmol applet without embedding script into a div

		// init jmol only once
		if (!_singletonJmolApplet)
		{
			console.log("initializing jmol applet...");
			_singletonJmolApplet = _initJmolForMutationDetails();
			//Jmol.scriptWait(_singletonJmolApplet, "load =2bq0");
		}
	</script>
</div>

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
	.diagram-lollipop-tip, .diagram-region-tip {
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
	.mutation-diagram-container {
		margin-bottom: 10px;
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
	.mutation-table-highlight {
		background-color: #E9E900 !important;
	}
	.mutation-table-container {
		margin-bottom: 40px;
	}
	.mutation-table-header {
		font-weight: bold !important;
	}
	.cosmic-details-tip-info {
		padding-bottom: 5px;
	}
	.left-align-td {
		text-align: left;
	}

</style>

<script type="text/javascript">
// initially hide the 3d viewer
$("#mutation_3d_structure").hide();

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
			{el: "#mutation_details", model: model});

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

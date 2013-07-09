<!-- TODO include these js files in the global js include? -->
<script type="text/javascript" src="js/mutation_model.js"></script>
<script type="text/javascript" src="js/mutation_diagram.js"></script>
<script type="text/javascript" src="js/mutation_table.js"></script>

<div class='section' id='mutation_details'></div>

<style type="text/css" title="currentStyle">
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
        #mutation_details .ColVis {
                float: left;
                padding-right:25%;
                margin-bottom: 0;
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
        .mutation-table-container {
	        margin-bottom: 40px;
        }
        .mutation-table-header {
	        font-weight: bold !important;
        }
</style>

<script type="text/javascript">
    
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
	var params = {geneList: genes,
		geneticProfiles: geneticProfiles,
		caseList: samples};

	// get mutation data & init view for the current gene and case lists
	$.post("getMutationData.json", params, initMutationView, "json");
});

</script>

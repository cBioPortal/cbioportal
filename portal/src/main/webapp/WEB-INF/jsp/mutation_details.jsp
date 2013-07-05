<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>
<%@ page import="org.mskcc.cbio.cgds.model.ExtendedMutation" %>
<%@ page import="org.mskcc.cbio.portal.model.ExtendedMutationMap" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.MutationCounter" %>
<%@ page import="org.mskcc.cbio.portal.mut_diagram.MutationTableProcessor" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.StringWriter" %>

<!-- TODO include these js files in the global js include? -->
<script type="text/javascript" src="js/mutation_model.js"></script>
<script type="text/javascript" src="js/mutation_diagram.js"></script>
<script type="text/javascript" src="js/mutation_table.js"></script>

<%
    // TODO completely remove this block after refactoring
	ArrayList<ExtendedMutation> extendedMutationList = (ArrayList<ExtendedMutation>)
            request.getAttribute(QueryBuilder.INTERNAL_EXTENDED_MUTATION_LIST);
    ExtendedMutationMap mutationMap = new ExtendedMutationMap(extendedMutationList,
            mergedProfile.getCaseIdList());

    MutationTableProcessor mutationTableProcessor = new MutationTableProcessor();
%>
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

		// TODO completely remove this part after refactoring the mutation table
		var tableMutations;
		<%
		for (GeneWithScore geneWithScore : geneWithScoreList) {
			String geneStr = geneWithScore.getGene();
			if (mutationMap.getNumExtendedMutations(geneStr) > 0) {
				String mutationTableStr =
					mutationTableProcessor.processMutationTable(geneStr,
					converMutations(geneWithScore, mutationMap, mergedCaseList));
		%>
				tableMutations = <%= mutationTableStr %>;
				delayedMutationTable(tableMutations);
		<%
			}
		}
		%>
	};

	// TODO getting these params from global variables defined in visualize.jsp
	// we should refactor/redefine these global variables in a better way
	var params = {geneList: genes,
		geneticProfiles: geneticProfiles,
		caseList: samples};

	// get mutation data & init view for the current gene and case lists
	$.post("getMutationData.json", params, initMutationView, "json");
});

/**
 * Toggles the diagram between lollipop view and histogram view.
 *
 * @param geneId    id of the target diagram
 */
function _toggleMutationDiagram(geneId)
{
    var option = $("#mutation_diagram_select_" + geneId).val();

    if (option == "diagram")
    {
	    $("#mutation_diagram_" + geneId).show();
	    $("#mutation_histogram_" + geneId).hide();
    }
    else if (option == "histogram")
    {
	    $("#mutation_diagram_" + geneId).hide();
	    $("#mutation_histogram_" + geneId).show();
    }
}

</script>


<%!
	// TODO remove all methods after refactoring
    private List<ExtendedMutation> converMutations(GeneWithScore geneWithScore,
                                                       ExtendedMutationMap mutationMap,
                                                       ArrayList<String> mergedCaseList)
    {
        List<ExtendedMutation> mutations = new ArrayList<ExtendedMutation>();

        for (String caseId : mergedCaseList)
        {
            List<ExtendedMutation> list = mutationMap.getExtendedMutations(
                    geneWithScore.getGene(), caseId);

            if (list != null)
            {
                mutations.addAll(list);
            }
        }

        return mutations;
    }
%>

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

<!--script type="text/javascript" src="js/raphael/raphael.js"></script-->
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
<div class='section' id='mutation_details'>
</div>


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
		.diagram_uniprot_link {
                padding-top: 10px;
                padding-left: 10px;
		}
		.mutation-diagram-container {
			margin-bottom: 10px;
		}
</style>

<script type="text/javascript">
    
// Set up Mutation View
$(document).ready(function(){
	// TODO accessing global "samples" variable...
	var sampleArray = samples.trim().split(/\s+/);

	/**
	 * Initializes the mutation diagram view.
	 *
	 * @param gene          hugo gene symbol
	 * @param mutationData  mutation data (array of JSON objects)
	 * @param sequenceData  sequence data (as a JSON object)
	 */
	var drawMutationDiagram = function(gene, mutationData, sequenceData)
	{
		// create a backbone collection for the given data
		var mutationColl = new MutationCollection(mutationData);

		var mutationDiagram = new MutationDiagram(gene,
			{el: "mutation_diagram_" + gene.toUpperCase()},
			mutationColl);

		mutationDiagram.initDiagram(sequenceData);
	};

	/**
	 * Processes the raw mutation data returned from the servlet, and
	 * initializes the mutation view.
	 *
	 * @param data  raw mutation data returned from the servlet
	 */
	var initMutationView = function(data)
	{
		// TODO check if there is mutation data & display no mutation info
		// "<p>There are no mutation details available for the gene set entered.</p><br><br>"
		var mainDivSelector = $("#mutation_details");

		var util = new MutationUtil(new MutationCollection(data));
		var mutationMap = util.getMutationGeneMap();

		/**
		 * Function to init mutation view for the given gene.
		 *
		 * @param gene          hugo gene symbol
		 * @param util     mutation util with helper functions
		 */
		var initView = function(gene, util)
		{
			var mutationMap = util.getMutationGeneMap();

			// callback function to init view after retrieving
			// sequence information.
			var init = function(response)
			{
				// calculate somatic & germline mutation rates
				var mutationCount = util.countMutations(gene, sampleArray);
				// generate summary string for the calculated mutation count values
				var summary = util.generateSummary(mutationCount);

				var mutationInfo = {geneSymbol: gene,
					mutationSummary: summary,
					uniprotId : response.identifier};

				var mainView = new MainMutationView({
					el: "#mutation_details_" + gene,
					model: mutationInfo});

				mainView.render();

				drawMutationDiagram(gene, mutationMap[gene], response);
				// TODO draw mutation table for each gene
			};

			$.getJSON("getPfamSequence.json", {geneSymbol: gene}, init);
		};

		// init main view for each gene
		for (var key in mutationMap)
		{
			// TODO also factor this out to a backbone view?
			mainDivSelector.append("<div id='mutation_details_" + key +"'></div>");
			initView(key, util);
		}
	};

	// TODO getting these params from global variables defined in visualize.jsp
	// we should refactor/redefine these global variables in a better way
	var params = {geneList: genes,
		geneticProfiles: geneticProfiles,
		caseList: samples};

	// get mutation data & init view for the current gene and case lists
	$.post("getMutationData.json", params, initMutationView, "json");

	// TODO use data retrieved from getMutationData.json, create backbone view for the table
	var tableMutations;
	<%
	// TODO completely remove the for block after refactoring mutation table
    for (GeneWithScore geneWithScore : geneWithScoreList) {
        String geneStr = geneWithScore.getGene();
        if (mutationMap.getNumExtendedMutations(geneStr) > 0) {
            String mutationTableStr = mutationTableProcessor.processMutationTable(
                geneStr,
                converMutations(geneWithScore, mutationMap, mergedCaseList)
            );
    %>
			tableMutations = <%= mutationTableStr %>;
			delayedMutationTable(tableMutations);

        <% } %>
    <% } %>
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

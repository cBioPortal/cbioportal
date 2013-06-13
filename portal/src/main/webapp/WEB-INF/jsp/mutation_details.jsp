<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>
<%@ page import="org.mskcc.cbio.cgds.model.ExtendedMutation" %>
<%@ page import="org.mskcc.cbio.portal.model.ExtendedMutationMap" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.MutationCounter" %>
<%@ page import="org.mskcc.cbio.portal.mut_diagram.MutationDataProcessor" %>
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
    ArrayList<ExtendedMutation> extendedMutationList = (ArrayList<ExtendedMutation>)
            request.getAttribute(QueryBuilder.INTERNAL_EXTENDED_MUTATION_LIST);
    ExtendedMutationMap mutationMap = new ExtendedMutationMap(extendedMutationList,
            mergedProfile.getCaseIdList());

    MutationTableProcessor mutationTableProcessor = new MutationTableProcessor();
	MutationDataProcessor mutationDataProcessor = new MutationDataProcessor();
%>
<div class='section' id='mutation_details'>

<%
    if (mutationMap.getNumGenesWithExtendedMutations() > 0) {
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            //outputGeneTable(geneWithScore, mutationMap, out, mergedCaseList);
	        MutationCounter mutationCounter = new MutationCounter(
			        geneWithScore.getGene(), mutationMap);

	        if (mutationMap.getNumExtendedMutations(geneWithScore.getGene()) > 0)
	        {
		        outputHeader(out, geneWithScore, mutationCounter);
	        }
        }
    } else {
        outputNoMutationDetails(out);
    }
%>

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
    
//  Set up Mutation Diagrams
$(document).ready(function(){
	var tableMutations;
    var mutationData;
	var gene;
	<%
    for (GeneWithScore geneWithScore : geneWithScoreList) {
        String geneStr = geneWithScore.getGene();
        if (mutationMap.getNumExtendedMutations(geneStr) > 0) {

            String mutationDataStr = mutationDataProcessor.getMutationData(
                geneStr,
                mutationMap.getExtendedMutations(geneStr)
            );

            String mutationTableStr = mutationTableProcessor.processMutationTable(
                geneStr,
                converMutations(geneWithScore, mutationMap, mergedCaseList)
            );
    %>
			mutationData = <%= mutationDataStr %>;
			gene = "<%= geneStr %>";
            _drawMutationDiagram(gene, mutationData);

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

/**
 * Initializes the mutation diagram view.
 *
 * @param gene  hugo gene symbol
 * @param data  mutation data (array of JSON objects)
 */
function _drawMutationDiagram(gene, data)
{
	var init = function(response) {
		// create a backbone collection for the given data
		var mutationColl = new MutationCollection(data);

		// get the uniprot identifier
		var identifier = response.identifier;

		var mutationDiagram = new MutationDiagram(gene,
			{el: "mutation_diagram_" + gene.toUpperCase()},
			mutationColl);

		mutationDiagram.initDiagram(response);

		// TODO may change after refactoring diagram data
		$("#uniprot_link_" + gene.toUpperCase()).html('<a href="' +
				'http://www.uniprot.org/uniprot/' + identifier +
				'" target="_blank">' +
				identifier + '</a>');
	};

	$.getJSON("getPfamSequence.json",
		{geneSymbol: gene},
		init);
}

</script>


<%!
	private String outputMutationsJson(final GeneWithScore geneWithScore, final ExtendedMutationMap mutationMap) {
		ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        List<ExtendedMutation> mutations = mutationMap.getExtendedMutations(geneWithScore.getGene());
        try {
            objectMapper.writeValue(stringWriter, mutations);
        }
        catch (Exception e) {
            // ignore
        }
        return stringWriter.toString().replace("\"", "\\\"");
    }

	private String outputMutationsJson(GeneWithScore geneWithScore,
			ExtendedMutationMap mutationMap,
			ArrayList<String> mergedCaseList)
	{
		ObjectMapper objectMapper = new ObjectMapper();
		StringWriter stringWriter = new StringWriter();
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

		try {
			objectMapper.writeValue(stringWriter, mutations);
		}
		catch (Exception e) {
			// ignore
		}

		return stringWriter.toString().replace("\"", "\\\"");
	}

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

    private void outputHeader(JspWriter out, GeneWithScore geneWithScore,
            MutationCounter mutationCounter) throws IOException {
        out.print("<h4>" + geneWithScore.getGene().toUpperCase() + ": ");
        out.println(mutationCounter.getTextSummary());
        out.println("</h4>");
	    // TODO histogram is disabled (will be enabled in the next release)
//	    out.println("<select class='mutation_diagram_toggle' " +
//	                "id='mutation_diagram_select_" + geneWithScore.getGene().toUpperCase() + "'" +
//	                "onchange='_toggleMutationDiagram(\"" + geneWithScore.getGene().toUpperCase() + "\")'>" +
//	               "<option value='diagram'>Lollipop Diagram</option>" +
//	               "<option value='histogram'>Histogram</option>" +
//	               "</select>");
	    out.println("<div id='uniprot_link_" + geneWithScore.getGene().toUpperCase() + "' " +
	                "class='diagram_uniprot_link'></div>");
        out.println("<div id='mutation_diagram_" + geneWithScore.getGene().toUpperCase() +"' " +
                    "class='mutation-diagram-container'></div>");
	    out.println("<div id='mutation_histogram_" + geneWithScore.getGene().toUpperCase() + "'></div>");
	    out.println("<div id='mutation_table_" + geneWithScore.getGene().toUpperCase() + "'>" +
	                "<img src='images/ajax-loader.gif'/>" +
	                "</div>");
    }

    private void outputNoMutationDetails(JspWriter out) throws IOException {
        out.println("<p>There are no mutation details available for the gene set entered.</p>");
        out.println("<br><br>");
    }
%>

<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>
<%@ page import="org.mskcc.cbio.cgds.model.ExtendedMutation" %>
<%@ page import="org.mskcc.cbio.portal.html.MutationTableUtil" %>
<%@ page import="org.mskcc.cbio.portal.model.ExtendedMutationMap" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.MutationCounter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="org.mskcc.cbio.portal.mut_diagram.MutationDiagramProcessor" %>
<%@ page import="org.mskcc.cbio.portal.mut_diagram.MutationTableProcessor" %>

<script type="text/javascript" src="js/raphael/raphael.js"></script>
<script type="text/javascript" src="js/mutation_diagram.js"></script>
<script type="text/javascript" src="js/mutation_table.js"></script>

<%
    ArrayList<ExtendedMutation> extendedMutationList = (ArrayList<ExtendedMutation>)
            request.getAttribute(QueryBuilder.INTERNAL_EXTENDED_MUTATION_LIST);
    ExtendedMutationMap mutationMap = new ExtendedMutationMap(extendedMutationList,
            mergedProfile.getCaseIdList());

    MutationDiagramProcessor mutationDiagramProcessor = new MutationDiagramProcessor();
    MutationTableProcessor mutationTableProcessor = new MutationTableProcessor();
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
</style>

<script type="text/javascript">
    
//  Set up Mutation Diagrams
$(document).ready(function(){
	var tableMutations;
    var diagramSequence;

	<%
    for (GeneWithScore geneWithScore : geneWithScoreList) {
        String geneStr = geneWithScore.getGene();
        if (mutationMap.getNumExtendedMutations(geneStr) > 0) {
        String mutationDiagramStr = mutationDiagramProcessor.getMutationDiagram(
                geneStr,
                mutationMap.getExtendedMutations(geneStr)
        );

        String mutationTableStr = mutationTableProcessor.processMutationTable(
                geneStr,
                converMutations(geneWithScore, mutationMap, mergedCaseList)
        );
    %>
	        tableMutations = <%= mutationTableStr %>;
            diagramSequence = <%= mutationDiagramStr %>;

            drawMutationDiagram(diagramSequence);
            delayedMutationTable(tableMutations);

        <% } %>
    <% } %>
});

/**
 * Toggles the diagram between lollipop view and histogram view.
 *
 * @param geneId    id of the target diagram
 */
function toggleMutationDiagram(geneId)
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
//	                "onchange='toggleMutationDiagram(\"" + geneWithScore.getGene().toUpperCase() + "\")'>" +
//	               "<option value='diagram'>Lollipop Diagram</option>" +
//	               "<option value='histogram'>Histogram</option>" +
//	               "</select>");
	    out.println("<div id='uniprot_link_" + geneWithScore.getGene().toUpperCase() + "' " +
	                "class='diagram_uniprot_link'></div>");
        out.println("<div id='mutation_diagram_" + geneWithScore.getGene().toUpperCase() + "'></div>");
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

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

<script type="text/javascript" src="js/jsmol/JSmol.min.nojq.js"></script>
<script type="text/javascript" src="js/raphael/raphael.js"></script>
<script type="text/javascript" src="js/mutation_diagram.js"></script>
<script type="text/javascript" src="js/mutation_table.js"></script>

<script type="text/javascript">

function _initJsmol(geneSymbol)
{
	var pdbid = "1crn"; // TODO get from servlet
	var appletName = 'applet_' + geneSymbol;
	var callbackfun = function(applet) {/*alert('add your callback functions here');*/};

    var jsmolOpts = {
        width: 300,
        height: 200,
        debug: false,
        color: "white",
        use: "HTML5",
        j2sPath: "js/jsmol/j2s",
        script: "load ="+pdbid+";",
        //defaultModel: "$dopamine",
        disableJ2SLoadMonitor: true,
        disableInitialConsole: true
    };

    if (jQuery.isFunction(callbackfun)) {
	    jsmolOpts['readyFunction'] = callbackfun;
    }

	Jmol.getApplet(appletName, jsmolOpts);
}
</script>

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
            drawMutationTable(tableMutations);

//                var str="<script>alert('loading 3d...');callJsmol('1crn', 'applet_"+geneSymbol+"', function(applet) {/*alert('add your callback functions here');*/});";
//                    str+="<";
//                    str+="/script>";
//                $("#mutation_3d_structure_"+geneSymbol).append(str);


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
        out.println("<table><tr><td>");
        out.print("<h4>" + geneWithScore.getGene().toUpperCase() + ": ");
        out.println(mutationCounter.getTextSummary());
        out.println("</h4>");
        String geneSymbol = geneWithScore.getGene().toUpperCase();
	    // TODO histogram is disabled (will be enabled in the next release)
//	    out.println("<select class='mutation_diagram_toggle' " +
//	                "id='mutation_diagram_select_" + geneSymbol + "'" +
//	                "onchange='toggleMutationDiagram(\"" + geneSymbol + "\")'>" +
//	               "<option value='diagram'>Lollipop Diagram</option>" +
//	               "<option value='histogram'>Histogram</option>" +
//	               "</select>");
        out.println("<div id='uniprot_link_" + geneSymbol + "' " +
                    "class='diagram_uniprot_link'></div>");
        out.println("<div id='mutation_diagram_" + geneSymbol + "'></div>");
        out.println("<div id='mutation_histogram_" + geneSymbol + "'></div>");
        out.println("</td><td>");
        outputJsmolContainer(out, geneSymbol);
        out.println("</td></tr></table>");
        out.println("<div id='mutation_table_" + geneSymbol + "'>" +
                    "<img src='images/ajax-loader.gif'/>" +
                    "</div>");
    }

    private void outputNoMutationDetails(JspWriter out) throws IOException {
        out.println("<p>There are no mutation details available for the gene set entered.</p>");
        out.println("<br><br>");
    }

	private void outputJsmolContainer(JspWriter out, String geneSymbol) throws IOException {
		// TODO Jsmol doesn't work unless it is defined inside in a script inside a div...
		out.println("<div id='mutation_3d_structure_" + geneSymbol + "'>");
		out.println("<script type='text/javascript'>_initJsmol('" + geneSymbol + "');</script>");
		out.println("</div>");
	}
%>

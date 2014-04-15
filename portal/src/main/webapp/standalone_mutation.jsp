<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle() + "::Mutation Analyzer"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true"/>

<script type="text/javascript" src="js/src/mutation/util/MutationInputParser.js"></script>
<link href="css/data_table_ColVis.css" type="text/css" rel="stylesheet" />
<link href="css/data_table_jui.css" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_details.css" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_table.css" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_3d.css" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_diagram.css" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_pdb_panel.css" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_pdb_table.css" type="text/css" rel="stylesheet" />

<jsp:include page="WEB-INF/jsp/mutation_views.jsp" flush="true"/>

<script type="text/javascript">

// TODO 3d Visualizer should be initialized before document get ready
// ...due to incompatible Jmol initialization behavior
var _mut3dVis = null;
_mut3dVis = new Mutation3dVis("default3dView", {});
_mut3dVis.init();

// Set up Mutation View
$(document).ready(function() {
	$("#submit_mutations").click(function() {
		//var sampleArray = PortalGlobals.getCases().trim().split(/\s+/);
		var parser = new MutationInputParser();

		// TODO pass the actual input field value
		var mutationData = parser.parseInput(
				$("#mutation-file-example").val());

		var sampleArray = parser.getSampleArray();

		var geneList = parser.getGeneList();
		geneList = geneList.join(" ");

		// init mutation data proxy with full data
		var proxy = new MutationDataProxy(geneList);
		proxy.initWithData(mutationData);

		var model = {mutationProxy: proxy,
			sampleArray: sampleArray};

		// TODO add tableOpts to initially show only the columns included in the input
		var options = {el: "#standalone_mutation_details",
			model: model,
			mut3dVis: _mut3dVis};

		var view = new MutationDetailsView(options);
		view.render();
	});
});

</script>
<body>
	<div id="standalone_mutation_input">
		<textarea id="mutation-file-example" rows="25" cols="80"><jsp:include
				page="WEB-INF/jsp/mutation/mutation-file-example.txt"></jsp:include></textarea>
		<button id="submit_mutations"
		        type="button">Submit</button>
	</div>

	<div id="standalone_mutation_details">

	</div>
</div>
</td></tr></table>
<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
</div>
</body>
</html>

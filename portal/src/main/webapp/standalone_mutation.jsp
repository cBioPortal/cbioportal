<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle() + "::Mutation Analyzer"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true"/>

<style type="text/css">
	.submit-custom-mutations {
		padding: 10px;
		margin-top: 10px;
		margin-bottom: 10px;
		font-size: 16px;
	}
	.mutation-file-form {
		margin-top: 10px;
	}
	.standalone-mutation-visualizer h1 {
		font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;
	}
	.full-list-of-headers {
		margin-bottom: 10px;
		max-width: 700px;
	}
	.standalone-mutation-visualizer .triangle {
		float: left;
		cursor: pointer;
	}
	.mutation-file-example {
		margin-top: 15px;
		white-space: pre;
		word-wrap: normal;
	}
	.mutation-input-field-expander {
		font-size: 16px;
		font-weight: bold;
		margin-bottom: 15px;
	}
</style>

<script type="text/javascript" src="js/src/mutation/util/MutationInputParser.js?<%=GlobalProperties.getAppVersion()%>"></script>
<link href="css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_details.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_table.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_3d.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_diagram.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_pdb_panel.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/mutation/mutation_pdb_table.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />

<jsp:include page="WEB-INF/jsp/mutation_views.jsp" flush="true"/>
<jsp:include page="WEB-INF/jsp/mutation/standalone_mutation_view.jsp" flush="true"/>

<script type="text/javascript">

// TODO 3d Visualizer should be initialized before document get ready
// ...due to incompatible Jmol initialization behavior
var _mut3dVis = null;
_mut3dVis = new Mutation3dVis("default3dView", {});
_mut3dVis.init();

// Set up Mutation View
$(document).ready(function() {
	function processInput(input)
	{
		//var sampleArray = PortalGlobals.getCases().trim().split(/\s+/);
		var parser = new MutationInputParser();

		// parse the provided input string
		var mutationData = parser.parseInput(input);

		var sampleArray = parser.getSampleArray();

		var geneList = parser.getGeneList();

		// No data to visualize...
		if (geneList.length == 0)
		{
			$("#standalone_mutation_details").html(
					"No data to visualize. Please make sure your input format is valid.");

			return;
		}

		// init mutation data proxy with full data
		var proxy = new MutationDataProxy(geneList.join(" "));
		proxy.initWithData(mutationData);

		// customized table options
		var tableOpts = {
			columnVisibility: {
				startPos: function (util, gene) {
					if (util.containsStartPos(gene)) {
						return "visible";
					}
					else {
						return "hidden";
					}
				},
				endPos: function (util, gene) {
					if (util.containsEndPos(gene)) {
						return "visible";
					}
					else {
						return "hidden";
					}
				},
				variantAllele: function (util, gene) {
					if (util.containsVarAllele(gene)) {
						return "visible";
					}
					else {
						return "hidden";
					}
				},
				referenceAllele: function (util, gene) {
					if (util.containsRefAllele(gene)) {
						return "visible";
					}
					else {
						return "hidden";
					}
				},
				chr: function (util, gene) {
					if (util.containsChr(gene)) {
						return "visible";
					}
					else {
						return "hidden";
					}
				}
			},
			columnRender: {
				caseId: function(datum) {
					var mutation = datum.mutation;
					var caseIdFormat = MutationDetailsTableFormatter.getCaseId(mutation.caseId);
					var vars = {};
					vars.linkToPatientView = mutation.linkToPatientView;
					vars.caseId = caseIdFormat.text;
					vars.caseIdClass = caseIdFormat.style;
					vars.caseIdTip = caseIdFormat.tip;

					if (mutation.linkToPatientView)
					{
						return _.template(
								$("#mutation_table_case_id_template").html(), vars);
					}
					else
					{
						return _.template(
								$("#standalone_mutation_case_id_template").html(), vars);
					}
				}
			}
		};

		var model = {mutationProxy: proxy,
			sampleArray: sampleArray,
			tableOpts: tableOpts};

		var options = {el: "#standalone_mutation_details",
			model: model,
			mut3dVis: _mut3dVis};

		var view = new MutationDetailsView(options);
		view.render();
	}

	var standaloneView = new StandaloneMutationView({el: "#standalone_mutation_view"});
	standaloneView.render();
	standaloneView.addInitCallback(processInput);
});

</script>
<body>
	<div id="standalone_mutation_view"></div>
	<div id="standalone_mutation_details"></div>
</div>
</td></tr></table>
<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
</div>
</body>
</html>

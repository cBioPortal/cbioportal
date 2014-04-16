<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle() + "::Mutation Analyzer"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true"/>

<style type="text/css">
	#submit_mutations {
		padding: 10px;
		margin-top: 10px;
		margin-bottom: 10px;
		font-size: 16px;
	}

	#mutation-file-form {
		margin-top: 10px;
	}

	#standalone_mutation_input h1
	{
		font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;
	}
</style>

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
	$("#full_list_of_headers").hide();

	$("#toggle_full_header_list").click(function() {
		$("#full_list_of_headers").slideToggle();
	});

	// TODO this is a duplicate code: see oncoprint/custom-boilerplate.js
	function postFile(url, formData, callback)
	{
		$.ajax({
			url: url,
			type: 'POST',
			success: callback,
			data: formData,
			//Options to tell jQuery not to process data or worry about content-type.
			cache: false,
			contentType: false,
			processData: false
		});
	}

	function processInput(input) {
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

		var model = {mutationProxy: proxy,
			sampleArray: sampleArray};

		// TODO add tableOpts to initially show only the columns included in the input
		var options = {el: "#standalone_mutation_details",
			model: model,
			mut3dVis: _mut3dVis};

		var view = new MutationDetailsView(options);
		view.render();
	}

	$("#submit_mutations").click(function() {
		var mutationForm = $("#mutation-file-form");

		postFile('echofile', new FormData(mutationForm[0]), function(data) {
			var textArea = $("#mutation-file-example").val();

			// if no file selected, use the text area input
			var input = _.isEmpty(data) ? textArea : data.mutation;

			// process the user input
			processInput(input);
		});
	});
});

</script>
<body>
	<div id="standalone_mutation_input">
		<h1>Mutation Visualizer</h1>

		<div id="#mutation_input_format_info">
			<p>
				You can either copy and paste your input into the text field below or
				select an input file to upload your mutation data.<br>
				Mutation files should be tab delimited, and should at least have the
				following headers on the first line:
			</p>
			<ul>
				<li>Hugo_Symbol</li>
				<li>Protein_Change</li>
			</ul>
			<br>
			<p>
				All other headers are optional.
				Click <a id="toggle_full_header_list" href="#">here</a>
				to see the full list of valid input headers.
			</p>
		</div>

		<div id="full_list_of_headers">
			<table>
				<tr>
					<th>Column Header</th>
					<th>Description</th>
					<th>Example</th>
				</tr>
				<tr>
					<td>Hugo_Symbol</td>
					<td>HUGO symbol for the gene</td>
					<td>TP53</td>
				</tr>
				<tr>
					<td>...</td>
					<td>...</td>
					<td>...</td>
				</tr>
			</table>
		</div>

		<textarea id="mutation-file-example" rows="25" cols="80"><jsp:include
				page="WEB-INF/jsp/mutation/mutation-file-example.txt"></jsp:include></textarea>

		<form id="mutation-file-form" class="form-horizontal" enctype="multipart/form-data" method="post">
			<div class="control-group">
				<label class="control-label" for="mutation">Upload your own mutation file...</label>
				<div class="controls">
					<input id="mutation" name="mutation" type="file">
				</div>
			</div>
		</form>

		<button id="submit_mutations"
		        class="ui-button ui-widget ui-state-default ui-corner-all"
		        type="button">Visualize</button>
	</div>

	<div id="standalone_mutation_details"></div>
</div>
</td></tr></table>
<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
</div>
</body>
</html>

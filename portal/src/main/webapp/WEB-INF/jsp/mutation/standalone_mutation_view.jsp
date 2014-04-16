<script type="text/template" id="standalone_mutation_view_template">
	<div class="standalone-mutation-visualizer">
		<h1>Mutation Visualizer</h1>

		<div class="mutation-input-format-info">
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
				Click <a class="toggle-full-header-list" href="#">here</a>
				to see the full list of valid input headers.
			</p>
		</div>

		<div class="full-list-of-headers">
			<table class="display header-details-table"
			       cellpadding='0' cellspacing='0' border='0'></table>
		</div>

		<textarea class="mutation-file-example"
		          rows="25"
		          cols="40"><jsp:include page="mutation-file-example.txt"/></textarea>

		<form class="form-horizontal mutation-file-form"
		      enctype="multipart/form-data"
		      method="post">
			<div class="control-group">
				<label class="control-label" for="mutation">
					Upload your own Mutation File
				</label>
				<div class="controls">
					<input id="mutation" name="mutation" type="file">
				</div>
			</div>
		</form>

		<button class="ui-button ui-widget ui-state-default ui-corner-all submit-custom-mutations"
		        type="button">Visualize</button>
	</div>
</script>

<script type="text/javascript" src="js/src/mutation/view/StandaloneMutationView.js"></script>
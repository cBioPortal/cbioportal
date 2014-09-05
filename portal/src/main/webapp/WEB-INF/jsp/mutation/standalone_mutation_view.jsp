<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<script type="text/template" id="standalone_mutation_view_template">
	<div class="standalone-mutation-visualizer">
        <h1 style="display:inline;">MutationMapper</h1>
        interprets mutations with protein annotations
		(<a href="release_notes_mutation_mapper.jsp" class="standalone-release-notes">v1.0</a>)
		<div class='mutation-input-field-expander' style="margin-top:10px">
			<span class="triangle triangle-right ui-icon ui-icon-triangle-1-e"></span>
			<span class="triangle triangle-down ui-icon ui-icon-triangle-1-s"></span>
			<a href="#" class='toggle-mutation-input-field'>Modify Input</a>
		</div>

		<div class="standalone-mutation-input">
			<div class="mutation-input-format-info">
				<p>
					Please input <b>tab-delimited</b> mutation data.
					<span class="mutation-data-format"><a href="#" class='toggle-full-header-list'>Data Format</a></span>
					<span class="load-example-data"><a href="#" class="load-example-data-link">Load example data</a></span>
				</p>
			</div>
			<table class="data-format-expander">
				<!--tr class='full-header-list-expander'>
					<td>
						<span class="triangle triangle-right ui-icon ui-icon-triangle-1-e"></span>
						<span class="triangle triangle-down ui-icon ui-icon-triangle-1-s"></span>
						<a href="#" class='toggle-full-header-list'>Data Format</a>
					</td>
				</tr-->
				<tr>
					<td>
						<div class="mutation-data-info">
							<div class="mutation-input-format-info">
								<p>
									You can either copy and paste your input into the text field below or
									select a file with mutation data for upload.<br>
									Mutation files should be tab delimited, and should at least have the
									following headers in the first line:
								</p>
								<ul>
									<li>Hugo_Symbol</li>
									<li>Protein_Change</li>
								</ul>
							</div>
							<p>
								List of valid input headers:
							</p>
							<div class="full-list-of-headers">
								<table class="display header-details-table"
								       cellpadding='0' cellspacing='0' border='0'></table>
							</div>
						</div>
					</td>
				</tr>
			</table>

			<textarea class="mutation-file-example"
			          rows="15"
			          cols="20"></textarea>

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
	</div>
</script>

<script type="text/template" id="example_mutation_data_template">
	<jsp:include page="mutation-file-example.txt"/>
</script>

<!-- Customized Mutation Table components -->

<script type="text/template" id="standalone_mutation_case_id_template">
	<b alt="{{caseIdTip}}" class="{{caseIdClass}}">{{caseId}}</b>
</script>

<script type="text/javascript" src="js/src/mutation/view/StandaloneMutationView.js?<%=GlobalProperties.getAppVersion()%>"></script>
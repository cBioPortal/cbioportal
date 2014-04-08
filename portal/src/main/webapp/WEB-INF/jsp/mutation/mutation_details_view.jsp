<script type="text/template" id="default_mutation_details_template">
	<div class='mutation-3d-container'></div>
	<div class='mutation-details-loader'>
		<img src='{{loaderImage}}'/>
	</div>
	<div class='mutation-details-content'>
		<ul>
			{{listContent}}
		</ul>
		{{mainContent}}
	</div>
</script>

<script type="text/template" id="default_mutation_details_main_content_template">
	<div id='mutation_details_{{geneId}}'>
		<img src='{{loaderImage}}'/>
	</div>
</script>

<script type="text/template" id="default_mutation_details_list_content_template">
	<li>
		<a href="#mutation_details_{{geneId}}"
		   class="mutation-details-tabs-ref"
		   title="{{geneSymbol}} mutations">
			<span>{{geneSymbol}}</span>
		</a>
	</li>
</script>

<script type="text/template" id="default_mutation_details_info_template">
	<p>There are no mutation details available for the gene set entered.</p>
	<br>
	<br>
</script>

<script type="text/template" id="default_mutation_details_gene_info_template">
	<p>There are no mutation details available for this gene.</p>
	<br>
	<br>
</script>

<script type="text/template" id="mutation_view_template">
	<h4>{{geneSymbol}}: {{mutationSummary}}</h4>
	<div>
		<table>
			<tr>
				<td>
					<div class='mutation-diagram-view'></div>
				</td>
				<td valign="bottom">
					<div class="mutation-3d-initializer"></div>
				</td>
			</tr>
		</table>
	</div>
	<div class="mutation-pdb-panel-view"></div>
	<div class='mutation-details-filter-info'>
		Current view shows filtered results.
		Click <a class='mutation-details-filter-reset'>here</a> to reset all filters.
	</div>
	<div class='mutation-table-container'>
		<img src='images/ajax-loader.gif'/>
	</div>
</script>

<script type="text/javascript" src="js/src/mutation/view/MainMutationView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/MutationDetailsView.js"></script>


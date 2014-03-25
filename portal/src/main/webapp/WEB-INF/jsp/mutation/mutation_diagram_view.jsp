<script type="text/template" id="mutation_diagram_view_template">
	<div class='mutation-diagram-toolbar'>
		<a href='http://www.uniprot.org/uniprot/{{uniprotId}}'
		   class='mutation-details-uniprot-link'
		   target='_blank'>{{uniprotId}}</a>
		<form style="display:inline-block"
		      action='svgtopdf.do'
		      method='post'
		      class='svg-to-pdf-form'>
			<input type='hidden' name='svgelement'>
			<input type='hidden' name='filetype' value='pdf'>
			<input type='hidden' name='filename' value='mutation_diagram_{{geneSymbol}}.pdf'>
		</form>
		<form style="display:inline-block"
		      action='svgtopdf.do'
		      method='post'
		      class='svg-to-file-form'>
			<input type='hidden' name='svgelement'>
			<input type='hidden' name='filetype' value='svg'>
			<input type='hidden' name='filename' value='mutation_diagram_{{geneSymbol}}.svg'>
		</form>
		<span class='mutation-diagram-toolbar-buttons'>
			<button class='diagram-to-pdf'>PDF</button>
			<button class='diagram-to-svg'>SVG</button>
			<button class="diagram-customize">Customize</button>
		</span>
	</div>
	<div class="mutation-diagram-customize ui-widget"></div>
	<div class='mutation-diagram-container'></div>
</script>

<script type="text/template" id="mutation_customize_panel_template">
	<div class="diagram-customize-close">
		<a href="#">&times;</a>
	</div>
	<h4>Customize</h4>
	<table>
		<tr>
			<td>
				<div class="diagram-y-axis-slider-area">
					<div class="diagram-slider-title"><label>max y-axis value</label></div>
					<table>
						<tr>
							<td width="90%" valign="top">
								<div class="diagram-y-axis-slider"></div>
								<span class="diagram-slider-min-label">{{minY}}</span>
								<span class="diagram-slider-max-label">{{maxY}}</span>
							</td>
							<td valign="top">
								<input class="diagram-y-axis-limit-input" size="2" type='text'>
							</td>
						</tr>
					</table>
				</div>
			</td>
		</tr>
	</table>
</script>


<script type="text/template" id="mutation_details_region_tip_template">
	<span class="diagram-region-tip">
		{{identifier}} {{type}}, {{description}} ({{start}} - {{end}})
	</span>
</script>

<script type="text/template" id="mutation_details_lollipop_tip_template">
	<div>
		<div class='diagram-lollipop-tip'>
			<b>{{count}} {{mutationStr}}</b>
			<br/>AA Change: {{label}}
			<div class="lollipop-stats">
				<table>
					<thead>
					<tr>
						<th>Cancer Type</th>
						<th>Count</th>
					</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</script>

<script type="text/template" id="mutation_details_lollipop_tip_stats_template">
	<tr>
		<td>{{cancerType}}</td>
		<td>{{count}}</td>
	</tr>
</script>

<script type="text/javascript" src="js/src/mutation/view/MutationCustomizePanelView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/MutationDiagramView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/RegionTipView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/LollipopTipStatsView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/LollipopTipView.js"></script>

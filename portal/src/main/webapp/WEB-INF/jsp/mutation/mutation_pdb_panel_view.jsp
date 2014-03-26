<script type="text/template" id="pdb_panel_view_template">
	<table class='mutation-pdb-main-container'>
		<tr>
			<td valign="top">
				<div class='mutation-pdb-panel-container'></div>
			</td>
		</tr>
		<tr>
			<td valign="top" align="center">
				<button class='expand-collapse-pdb-panel'
				        title='Expand/Collapse PDB Chains'></button>
			</td>
		</tr>
		<tr class='pdb-table-controls'>
			<td>
				<span class="triangle triangle-right ui-icon ui-icon-triangle-1-e"></span>
				<span class="triangle triangle-down ui-icon ui-icon-triangle-1-s"></span>
				<a href="#" class='init-pdb-table'>PDB Chain Table</a>
			</td>
		</tr>
		<tr>
			<td>
				<div class='pdb-table-wrapper'>
					<div class="mutation-pdb-table-view"></div>
				</div>
			</td>
		</tr>
	</table>
</script>

<script type="text/template" id="mutation_details_pdb_chain_tip_template">
	<span class='pdb-chain-tip'>
		PDB
		<a href="http://www.rcsb.org/pdb/explore/explore.do?structureId={{pdbId}}"
		   target="_blank">
			<b>{{pdbId}}</b>
		</a>
		<span class="chain-rectangle-tip-pdb-info">{{pdbInfo}}</span><br>
		Chain <b>{{chainId}}</b>
		<span class="chain-rectangle-tip-mol-info">{{molInfo}}</span>
	</span>
</script>

<script type="text/template" id="mutation_details_pdb_help_tip_template">
	<span class='pdb-chain-tip'>
		This panel displays a list of PDB chains for the corresponding uniprot ID.
		PDB chains are ranked with respect to their sequence similarity ratio,
		and aligned to the y-axis of the mutation diagram.
		Highly ranked chains have darker color than the lowly ranked ones.<br>
		<br>
		Each chain is represented by a single rectangle.
		Gaps within the chains are represented by a thin line connecting the segments of the chain.<br>
		<br>
		By default, only a first few rows are displayed.
		To see more chains, use the scroll bar next to the panel.
		To see the detailed list of all available PDB chains in a table
		click on the link below the panel.<br>
		<br>
		To select a chain, simply click on it.
		Selected chain is highlighted with a different frame color.
		You can also select a chain by clicking on a row in the table.
		Selecting a chain reloads the PDB data for the 3D structure visualizer.
	</span>
</script>

<script type="text/javascript" src="js/src/mutation/view/PdbChainTipView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/PdbPanelView.js"></script>
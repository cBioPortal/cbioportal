<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<script type="text/template" id="pdb_table_view_template">
	<div class='pdb-chain-table-loader'>
		<img src='{{loaderImage}}'/>
	</div>
	<table>
		<tr>
			<td valign="top" class='pdb-chain-table-container'>
				<table class='display pdb-chain-table'
				       cellpadding='0' cellspacing='0' border='0'>
				</table>
			</td>
			<td></td>
		</tr>
	</table>
</script>

<!-- PDB Table components
-- These components are intended to be used within PDB table cells.
-->

<script type="text/template" id="mutation_pdb_table_pdb_cell_template">
	<a href="http://www.rcsb.org/pdb/explore/explore.do?structureId={{pdbId}}"
	   target="_blank"><b>{{pdbId}}</b></a>
</script>

<script type="text/template" id="mutation_pdb_table_chain_cell_template">
	<span class="pbd-chain-table-chain-cell">
		<label>{{chainId}}</label>
		<a href="#" class="pdb-table-3d-link">
			<span alt="Click to update the 3D view with this chain"
			      class="pdb-table-3d-icon">3D</span>
		</a>
	</span>
</script>

<script type="text/template" id="mutation_pdb_table_summary_cell_template">
	<b>pdb:</b> {{summary}} <br>
	<b>chain:</b> {{molecule}}
</script>

<script type="text/javascript" src="js/src/mutation/view/PdbTableView.js?<%=GlobalProperties.getAppVersion()%>"></script>
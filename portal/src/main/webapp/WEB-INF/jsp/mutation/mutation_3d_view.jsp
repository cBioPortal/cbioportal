<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<script type="text/template" id="mutation_3d_view_template">
	<button class='mutation-3d-vis'>
		<label>3D Structure &#187</label>
	</button>
</script>

<script type="text/template" id="mutation_3d_vis_info_template">
	<div class='mutation-3d-info-title'>
		3D Structure
	</div>
	<div class='mutation-3d-info-main'>
		PDB
		<span class='mutation-3d-pdb-id'>
			<a href="http://www.rcsb.org/pdb/explore/explore.do?structureId={{pdbId}}"
			   target="_blank">
				{{pdbId}}
			</a>
		</span>
		<span class='mutation-3d-pdb-info'>: {{pdbInfo}}</span><br>
		Chain
		<span class='mutation-3d-chain-id'>{{chainId}}</span>
		<span class='mutation-3d-mol-info'>: {{molInfo}}</span>
	</div>
</script>

<script type="text/template" id="mutation_3d_vis_template">
	<div class='mutation-3d-vis-header'>
		<span class='mutation-3d-close ui-icon ui-icon-circle-close' title='close'></span>
		<span class='mutation-3d-minimize ui-icon ui-icon-circle-minus' title='minimize'></span>
		<div class='mutation-3d-info'></div>
	</div>
	<div class='mutation-3d-residue-warning'>
		<span class="mutation-3d-unmapped-info">Selected mutation</span>
		cannot be mapped onto this structure.
	</div>
	<div class='mutation-3d-nomap-warning'>
		None of the mutations can be mapped onto this structure.
	</div>
	<div class='mutation-3d-vis-loader'>
		<img src='{{loaderImage}}'/>
	</div>
	<div class='mutation-3d-vis-container'></div>
	<div class='mutation-3d-vis-toolbar'>
		<div class='mutation-3d-vis-help-init'>
			<table>
				<tr>
					<td align="left">
						<button class='mutation-3d-pymol-dload'>PML</button>
					</td>
					<td align="right">
						<a class='mutation-3d-vis-help-open' href="#">how to pan/zoom/rotate?</a>
					</td>
				</tr>
			</table>
		</div>
		<div class='mutation-3d-vis-help-content'>
			<div class="mutation-3d-vis-help-close">
				<a href="#"><b>&times;</b></a>
			</div>
			<h4>3D visualizer basic interaction</h4>
			<b>Zoom in/out:</b> Press and hold the SHIFT key and the left mouse button,
			and then move the mouse backward/forward.<br>
			<b>Pan:</b> Press and hold the SHIFT key, double click and hold the left mouse button,
			and then move the mouse in the desired direction.<br>
			<b>Rotate:</b> Press and hold the left mouse button, and then move the mouse in the desired
			direction to rotate along the x and y axes. To be able to rotate along the z-axis, you need to
			press and hold the SHIFT key and the left mouse button, and then move the mouse left or right.<br>
			<b>Reset:</b> Press and hold the SHIFT key, and then double click on the background
			to reset the orientation and the zoom level to the initial state.
		</div>
		<table>
			<tr>
				<!--td>
					<input class='mutation-3d-spin' type='checkbox'>
					<label>Spin</label>
				</td>
				<td class='mutation-3d-buttons'>
					<button class='mutation-3d-button mutation-3d-center-selected'
							alt='Center the view on the highlighted residue'></button>
					<button class='mutation-3d-button mutation-3d-center-default'
							alt='Restore the view to its default center'></button>
				</td>
				<td class='mutation-3d-zoom-label'>
					<label>Zoom</label>
				</td>
				<td>
					<div class='mutation-3d-zoom-slider'></div>
				</td-->
			</tr>
		</table>
		<table cellpadding="0">
			<tr>
				<td class='mutation-3d-protein-style-menu' valign='top'>
					<div class='mutation-3d-style-header'>
						<label>Protein Style</label>
					</div>
					<table cellpadding='0'>
						<tr>
							<td>
								<label>
									<input class='mutation-3d-display-non-protein'
									       type='checkbox'
									       checked='checked'>
									Display bound molecules
								</label>
								<img class='display-non-protein-help' src='{{helpImage}}'/>
							</td>
						</tr>
						<tr>
							<td>
								<label>Scheme:</label>
								<select class='mutation-3d-protein-style-select'>
									<option value='cartoon'
									        title='Switch to the Cartoon Scheme'>cartoon</option>
									<option value='spaceFilling'
									        title='Switch to the Space-filling Scheme'>space-filling</option>
									<option value='trace'
									        title='Switch to the Trace Scheme'>trace</option>
								</select>
							</td>
						</tr>
						<tr>
							<td>
								<label>Color:</label>
								<select class='mutation-3d-protein-color-select'>
									<option value='uniform'
									        title='Uniform Color'>uniform</option>
									<option value='bySecondaryStructure'
									        title='Color by Secondary Structure'>secondary structure</option>
									<option value='byChain'
									        title='Color by Rainbow Gradient'>N-C rainbow</option>
									<option value='byAtomType'
									        title='Color by Atom Type'
									        disabled='disabled'>atom type</option>
								</select>
								<img class='protein-struct-color-help' src='{{helpImage}}'/>
							</td>
						</tr>
					</table>
				</td>
				<td class='mutation-3d-mutation-style-menu' valign='top'>
					<div class='mutation-3d-style-header'>
						<label>Mutation Style</label>
					</div>
					<table cellpadding="0">
						<tr>
							<td>
								<!--label>
									<input class='mutation-3d-side-chain'
									       type='checkbox'
									       checked='checked'>
									Display side chain
								</label-->
								<label>Side chain:</label>
								<select class='mutation-3d-side-chain-select'>
									<option value='all'
									        title='Display side chain for all mapped residues'>all</option>
									<option value='highlighted'
									        selected='selected'
									        title='Display side chain for highlighted residues only'>selected</option>
									<option value='none'
									        title='Do not display side chains'>none</option>
								</select>
								<img class='display-side-chain-help' src='{{helpImage}}'/>
							</td>
						</tr>
						<tr>
							<td>
								<!--table cellpadding="0">
									<tr>
										<td>
											<label>Color:</label>
										</td>
										<td>
											<label>
												<input class='mutation-3d-mutation-color-by-type'
												       type='checkbox'
												       checked='checked'>
												mutation type
											</label>
											<img class='mutation-type-color-help' src='{{helpImage}}'/>
										</td>
									</tr>
									<tr>
										<td></td>
										<td>
											<label>
												<input class='mutation-3d-mutation-color-by-atom'
												       type='checkbox'>
												atom type
											</label>
										</td>
									</tr>
								</table-->
								<label>Color:</label>
								<select class='mutation-3d-mutation-color-select'>
									<option value='uniform'
									        title='Uniform color'>uniform</option>
									<option value='byMutationType'
									        selected='selected'
									        title='Color by mutation type'>mutation type</option>
									<option value='none'
									        title='Do not color'>none</option>
								</select>
								<img class='mutation-type-color-help' src='{{helpImage}}'/>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</div>
</script>

<script type="text/template" id="mutation_3d_type_color_tip_template">
	Color options for the mapped mutations.<br>
	<br>
	<b>Uniform:</b> Colors all mutated residues with a
	<span class='uniform_mutation'>single color</span>.<br>
	<b>Mutation type:</b> Enables residue coloring by mutation type.
	Mutation types and corresponding color codes are as follows:
	<ul>
		<li><span class='missense_mutation'>Missense Mutations</span></li>
		<li><span class='trunc_mutation'>Truncating Mutations</span>
			(Nonsense, Nonstop, FS del, FS ins)</li>
		<li><span class='inframe_mutation'>Inframe Mutations</span>
			(IF del, IF ins)</li>
		<li>
			Residues colored with <span class='mutation-3d-tied'>purple</span> indicate residues
			that are affected by different mutation types at the same proportion.
		</li>
	</ul>
	<b>None:</b> Disables coloring of the mutated residues
	except for manually selected (highlighted) residues.<br>
	<br>
	Highlighted residues are colored with <span class='mutation-3d-highlighted'>yellow</span>.
</script>

<script type="text/template" id="mutation_3d_structure_color_tip_template">
	Color options for the protein structure.<br>
	<br>
	<b>Uniform:</b> Colors the entire protein structure with a
	<span class='mutation-3d-loop'>single color</span>.<br>
	<b>Secondary structure:</b> Colors the protein by secondary structure.
	Assigns different colors for <span class='mutation-3d-alpha-helix'>alpha helices</span>,
	<span class='mutation-3d-beta-sheet'>beta sheets</span>, and
	<span class='mutation-3d-loop'>loops</span>.
	This color option is not available for the space-filling protein scheme.<br>
	<b>N-C rainbow:</b> Colors the protein with a rainbow gradient
	from red (N-terminus) to blue (C-terminus).<br>
	<b>Atom Type:</b> Colors the structure with respect to the atom type (CPK color scheme).
	This color option is only available for the space-filling protein scheme.<br>
	<br>
	The selected chain is always displayed with full opacity while the rest of the structure
	has some transparency to help better focusing on the selected chain.
</script>

<script type="text/template" id="mutation_3d_side_chain_tip_template">
	Display options for the side chain atoms.<br>
	<br>
	<b>All:</b> Displays the side chain atoms for every mapped residue.<br>
	<b>Selected:</b> Displays the side chain atoms only for the selected mutations.<br>
	<b>None:</b> Hides the side chain atoms.<br>
	<br>
	This option has no effect for the space-filling protein scheme.
</script>

<script type="text/template" id="mutation_3d_non_protein_tip_template">
	Displays co-crystalized molecules.
	This option has no effect if the current structure
	does not contain any co-crystalized bound molecules.
</script>

<script type="text/javascript" src="js/src/mutation/view/Mutation3dView.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/view/Mutation3dVisView.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/view/Mutation3dVisInfoView.js?<%=GlobalProperties.getAppVersion()%>"></script>

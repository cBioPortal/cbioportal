<%@ taglib prefix="sql_rt" uri="http://java.sun.com/jstl/sql_rt" %>

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

<script type="text/template" id="default_mutation_details_main_content_template">
	<div id='mutation_details_{{geneSymbol}}'>
		<img src='{{loaderImage}}'/>
	</div>
</script>

<script type="text/template" id="default_mutation_details_list_content_template">
	<li>
		<a href="#mutation_details_{{geneSymbol}}"
		   id="mutation_details_tab_{{geneSymbol}}"
		   class="mutation-details-tabs-ref"
		   title="{{geneSymbol}} mutations">
			<span>{{geneSymbol}}</span>
		</a>
	</li>
</script>

<script type="text/template" id="mutation_view_template">
	<h4>{{geneSymbol}}: {{mutationSummary}}</h4>
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
		<button class='diagram-to-pdf'>PDF</button>
		<button class='diagram-to-svg'>SVG</button>
		<button class="diagram-customize">Customize</button>
	</div>
	<div class="mutation-diagram-customize ui-widget"></div>
	<div>
		<table>
			<tr>
				<td>
					<div class='mutation-diagram-container'></div>
				</td>
				<td>
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
		Chain <span class='mutation-3d-chain-id'>{{chainId}}</span> of PDB
		<span class='mutation-3d-pdb-id'>
			<a href="http://www.rcsb.org/pdb/explore/explore.do?structureId={{pdbId}}"
			   target="_blank">
				{{pdbId}}
			</a>
		</span>
		<span class='mutation-3d-pdb-info'>: {{pdbInfo}}</span>
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
			<a href="#">how to pan/zoom/rotate?</a>
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

<script type="text/template" id="pdb_panel_view_template">
	<table>
		<tr>
			<td valign="top">
				<div class='mutation-pdb-panel-container'></div>
			</td>
			<td></td>
		</tr>
		<tr>
			<td valign="top" align="center">
				<div class='mutation-pdb-panel-controls'>
					<button class='expand-collapse-pdb-panel'
					        title='Expand/Collapse PDB Chains'></button>
				</div>
			</td>
			<td></td>
		</tr>
	</table>
</script>

<script type="text/template" id="mutation_details_table_template">
	<table class='display mutation_details_table'
	       cellpadding='0' cellspacing='0' border='0'>
		<thead>{{tableHeaders}}</thead>
		<tbody>{{tableRows}}</tbody>
		<tfoot>{{tableHeaders}}</tfoot>
	</table>
</script>

<script type="text/template" id="mutation_details_table_data_row_template">
	<tr id='{{mutationId}}' class="{{mutationSid}}">
		<td>{{mutationId}}-{{mutationSid}}</td>
		<td>
			<a href='{{linkToPatientView}}' target='_blank'>
				<b alt="{{caseIdTip}}" class="{{caseIdClass}}">{{caseId}}</b>
			</a>
		</td>
        <td>
            <a href='{{cancerStudyLink}}' target='_blank'>
                <b title="{{cancerStudy}}" alt="{{cancerStudy}}" class="cc-short-study-name">{{cancerStudyShort}}</b>
            </a>
        </td>
		<td>
			<span class='{{tumorTypeClass}}' alt='{{tumorTypeTip}}'>
				{{tumorType}}
			</span>
		</td>
		<td>
			<span class='{{proteinChangeClass}}' alt='{{proteinChangeTip}}'>
				{{proteinChange}}
			</span>
		</td>
		<td>
			<span class='{{mutationTypeClass}}'>
				<label>{{mutationTypeText}}</label>
			</span>
		</td>
		<td>
			<label class='{{cosmicClass}}' alt='{{mutationId}}'><b>{{cosmicCount}}</b></label>
		</td>
		<td>
			<span class='{{omaClass}} {{fisClass}}' alt='{{fisValue}}|{{xVarLink}}'>
				<label>{{fisText}}</label>
			</span>
		</td>
		<td>
			<a href='{{msaLink}}' target='_blank'>
				<span style="background-color:#88C;color:white">
					&nbsp;msa&nbsp;
				</span>
			</a>
		</td>
		<td>
			<a href='#' class="pdb-link" alt="{{pdbMatchId}}">
				<span style="background-color:#88C;color:white">
					&nbsp;3D&nbsp;
				</span>
			</a>
		</td>
		<td>
			<span alt='{{mutationStatusTip}}' class='simple-tip {{mutationStatusClass}}'>
				<label>{{mutationStatusText}}</label>
			</span>
		</td>
		<td>
			<span alt='{{validationStatusTip}}' class="simple-tip {{validationStatusClass}}">
				<label>{{validationStatusText}}</label>
			</span>
		</td>
		<td>{{sequencingCenter}}</td>
		<td>{{chr}}</td>
		<td>
			<label class='{{startPosClass}}'>{{startPos}}</label>
		</td>
		<td>
			<label class='{{endPosClass}}'>{{endPos}}</label>
		</td>
		<td>{{referenceAllele}}</td>
		<td>{{variantAllele}}</td>
		<td>
			<label alt='<b>{{tumorAltCount}}</b> variant reads out of <b>{{tumorTotalCount}}</b> total'
			       class='{{tumorFreqClass}} {{tumorFreqTipClass}}'>{{tumorFreq}}</label>
		</td>
		<td>
			<label alt='<b>{{normalAltCount}}</b> variant reads out of <b>{{normalTotalCount}}</b> total'
			       class='{{normalFreqClass}} {{normalFreqTipClass}}'>{{normalFreq}}</label>
		</td>
		<td>
			<label class='{{tumorRefCountClass}}'>{{tumorRefCount}}</label>
		</td>
		<td>
			<label class='{{tumorAltCountClass}}'>{{tumorAltCount}}</label>
		</td>
		<td>
			<label class='{{normalRefCountClass}}'>{{normalRefCount}}</label>
		</td>
		<td>
			<label class='{{normalAltCountClass}}'>{{normalAltCount}}</label>
		</td>
		<td>
			<a class='igv-link' alt='{{igvLink}}'>
				<span style="background-color:#88C;color:white">
					&nbsp;IGV&nbsp;
				</span>
			</a>
		</td>
		<td>
			<label alt='{{cnaTip}}' class='simple-tip-left {{cnaClass}}'>{{cna}}</label>
		</td>
		<td>
			<label class='{{mutationCountClass}}'>{{mutationCount}}</label>
		</td>
	</tr>
</script>

<script type="text/template" id="mutation_details_table_header_row_template">
	<th alt='Mutation ID' class='mutation-table-header'>Mutation ID</th>
	<th alt='Case ID' class='mutation-table-header'>Case ID</th>
    <th alt='Cancer Study' class='mutation-table-header'>Cancer Study</th>
	<th alt='Tumor Type' class='mutation-table-header'>Tumor Type</th>
    <th alt='Protein Change' class='mutation-table-header'>AA Change</th>
	<th alt='Mutation Type' class='mutation-table-header'>Type</th>
	<th alt='Overlapping mutations in COSMIC' class='mutation-table-header'>COSMIC</th>
	<th alt='Predicted Functional Impact Score (via Mutation Assessor) for missense mutations'
	    class='mutation-table-header'>FIS</th>
	<th alt='Conservation' class='mutation-table-header'>Cons</th>
	<th alt='3D Structure' class='mutation-table-header'>3D</th>
	<th alt='Mutation Status' class='mutation-table-header'>MS</th>
	<th alt='Validation Status' class='mutation-table-header'>VS</th>
	<th alt='Sequencing Center' class='mutation-table-header'>Center</th>
	<!--th alt='NCBI Build Number' class='mutation-table-header'>Build</th-->
	<th alt='Chromosome' class='mutation-table-header'>Chr</th>
	<th alt='Start Position' class='mutation-table-header'>Start Pos</th>
	<th alt='End Position' class='mutation-table-header'>End Pos</th>
	<th alt='Reference Allele' class='mutation-table-header'>Ref</th>
	<th alt='Variant Allele' class='mutation-table-header'>Var</th>
	<th alt='Variant allele frequency<br> in the tumor sample'
	    class='mutation-table-header'>Allele Freq (T)</th>
	<th alt='Variant allele frequency<br> in the normal sample'
	    class='mutation-table-header'>Allele Freq (N)</th>
	<th alt='Variant Ref Count' class='mutation-table-header'>Var Ref</th>
	<th alt='Variant Alt Count' class='mutation-table-header'>Var Alt</th>
	<th alt='Normal Ref Count' class='mutation-table-header'>Norm Ref</th>
	<th alt='Normal Alt Count' class='mutation-table-header'>Norm Alt</th>
	<th alt='Link to BAM file' class='mutation-table-header'>BAM</th>
	<th alt='Copy-number status of the mutated gene' class='mutation-table-header'>Copy #</th>
	<th alt='Total number of<br> nonsynonymous mutations<br> in the sample'
	    class='mutation-table-header'>#Mut in Sample</th>
</script>

<script type="text/template" id="mutation_details_cosmic_tip_template">
	<div class='cosmic-details-tip-info'>
		<b>{{cosmicTotal}} occurrences of {{mutationKeyword}} mutations in COSMIC</b>
	</div>
	<table class='cosmic-details-table display'
	       cellpadding='0' cellspacing='0' border='0'>
		<thead>
			<tr>
				<th>COSMIC ID</th>
				<th>Protein Change</th>
				<th>Count</th>
			</tr>
		</thead>
		<tbody>{{cosmicDataRows}}</tbody>
	</table>
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

<script type="text/template" id="mutation_details_region_tip_template">
	<span class="diagram-region-tip">
		{{identifier}} {{type}}, {{description}} ({{start}} - {{end}})
	</span>
</script>

<script type="text/template" id="mutation_details_pdb_chain_tip_template">
	<span class='pdb-chain-tip'>
		<b>PDB id:</b> {{pdbId}}<br>
		<b>Chain:</b> {{chainId}}<br>
		{{pdbInfo}}
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
		To see more chains, click on the expand/collapse button below the panel.<br>
		<br>
		To select a chain, simply click on it.
		Selected chain is highlighted with a different frame color.
		Selecting a chain reloads the PDB data for the 3D structure visualizer.
	</span>
</script>

<script type="text/template" id="mutation_details_fis_tip_template">
	Predicted impact score: <b>{{impact}}</b>
	<div class='mutation-assessor-link'>
		<a href='{{linkOut}}' target='_blank'>
			<img height=15 width=19 src='images/ma.png'>
			Go to Mutation Assessor
		</a>
	</div>
</script>

<script type="text/javascript" src="js/src/mutation/view/CosmicTipView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/LollipopTipStatsView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/LollipopTipView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/MainMutationView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/Mutation3dView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/Mutation3dVisView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/Mutation3dVisInfoView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/MutationCustomizePanelView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/MutationDetailsTableView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/MutationDetailsView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/PdbChainTipView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/PdbPanelView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/PredictedImpactTipView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/RegionTipView.js"></script>
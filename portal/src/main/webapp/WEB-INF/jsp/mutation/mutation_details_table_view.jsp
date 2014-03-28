<script type="text/template" id="mutation_details_table_template">
	<table class='display mutation_details_table'
	       cellpadding='0' cellspacing='0' border='0'>
	</table>
</script>

<!-- Mutation Table components
  -- These components are intended to be used within the mutation table cells.
  -->

<script type="text/template" id="mutation_table_case_id_template">
	<a href='{{linkToPatientView}}' target='_blank'>
		<b alt="{{caseIdTip}}"
		   class="{{caseIdClass}}">{{caseId}}</b>
	</a>
</script>

<script type="text/template" id="mutation_table_cancer_study_template">
	<a href='{{cancerStudyLink}}' target='_blank'>
		<b title="{{cancerStudy}}"
		   alt="{{cancerStudy}}"
		   class="cc-short-study-name">{{cancerStudyShort}}</b>
	</a>
</script>

<script type="text/template" id="mutation_table_tumor_type_template">
	<span class='{{tumorTypeClass}}' alt='{{tumorTypeTip}}'>
		{{tumorType}}
	</span>
</script>

<script type="text/template" id="mutation_table_protein_change_template">
	<span class='{{proteinChangeClass}}' alt='{{proteinChangeTip}}'>
		<a>{{proteinChange}}</a>
	</span>
	<a href='#' class="mutation-table-3d-link" alt="{{pdbMatchId}}">
		<span class="mutation-table-3d-icon">3D</span>
	</a>
</script>

<script type="text/template" id="mutation_table_mutation_type_template">
	<span class='{{mutationTypeClass}}'>
		<label>{{mutationTypeText}}</label>
	</span>
</script>

<script type="text/template" id="mutation_table_cna_template">
	<label alt='{{cnaTip}}' class='simple-tip {{cnaClass}}'>{{cna}}</label>
</script>

<script type="text/template" id="mutation_table_cosmic_template">
	<label class='{{cosmicClass}}' alt='{{mutationId}}'>{{cosmicCount}}</label>
</script>

<script type="text/template" id="mutation_table_mutation_status_template">
	<span alt='{{mutationStatusTip}}' class='simple-tip {{mutationStatusClass}}'>
		<label>{{mutationStatusText}}</label>
	</span>
</script>

<script type="text/template" id="mutation_table_validation_status_template">
	<span alt='{{validationStatusTip}}' class="simple-tip {{validationStatusClass}}">
		<label>{{validationStatusText}}</label>
	</span>
</script>

<script type="text/template" id="mutation_table_mutation_assessor_template">
	<span class='{{omaClass}} {{fisClass}}' alt='{{fisValue}}|{{mutationId}}'>
		<label>{{fisText}}</label>
	</span>
</script>

<script type="text/template" id="mutation_table_start_pos_template">
	<label class='{{startPosClass}}'>{{startPos}}</label>
</script>

<script type="text/template" id="mutation_table_end_pos_template">
	<label class='{{endPosClass}}'>{{endPos}}</label>
</script>

<script type="text/template" id="mutation_table_tumor_freq_template">
	<label alt='<b>{{tumorAltCount}}</b> variant reads out of <b>{{tumorTotalCount}}</b> total'
	       class='{{tumorFreqClass}} {{tumorFreqTipClass}}'>{{tumorFreq}}</label>
</script>

<script type="text/template" id="mutation_table_normal_freq_template">
	<label alt='<b>{{normalAltCount}}</b> variant reads out of <b>{{normalTotalCount}}</b> total'
	       class='{{normalFreqClass}} {{normalFreqTipClass}}'>{{normalFreq}}</label>
</script>

<script type="text/template" id="mutation_table_tumor_ref_count_template">
	<label class='{{tumorRefCountClass}}'>{{tumorRefCount}}</label>
</script>

<script type="text/template" id="mutation_table_tumor_alt_count_template">
	<label class='{{tumorAltCountClass}}'>{{tumorAltCount}}</label>
</script>

<script type="text/template" id="mutation_table_normal_ref_count_template">
	<label class='{{normalRefCountClass}}'>{{normalRefCount}}</label>
</script>

<script type="text/template" id="mutation_table_normal_alt_count_template">
	<label class='{{normalAltCountClass}}'>{{normalAltCount}}</label>
</script>

<script type="text/template" id="mutation_table_igv_link_template">
	<a class='igv-link' alt='{{igvLink}}'>
		<span class="mutation-table-igv-icon">IGV</span>
	</a>
</script>

<script type="text/template" id="mutation_table_mutation_count_template">
	<label class='{{mutationCountClass}}'>{{mutationCount}}</label>
</script>

<!-- TODO remove data_row_template and header_row_template after refactoring -->

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
				<a>{{proteinChange}}</a>
			</span>
			<a href='#' class="mutation-table-3d-link" alt="{{pdbMatchId}}">
				<span class="mutation-table-3d-icon">3D</span>
			</a>
		</td>
		<td>
			<span class='{{mutationTypeClass}}'>
				<label>{{mutationTypeText}}</label>
			</span>
		</td>
		<td>
			<label alt='{{cnaTip}}' class='simple-tip {{cnaClass}}'>{{cna}}</label>
		</td>
		<td>
			<label class='{{cosmicClass}}' alt='{{mutationId}}'>{{cosmicCount}}</label>
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
		<td>
			<span class='{{omaClass}} {{fisClass}}' alt='{{fisValue}}|{{mutationId}}'>
				<label>{{fisText}}</label>
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
				<span class="mutation-table-igv-icon">IGV</span>
			</a>
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
	<th alt='Copy-number status of the mutated gene' class='mutation-table-header'>Copy #</th>
	<th alt='Overlapping mutations in COSMIC' class='mutation-table-header'>COSMIC</th>
	<th alt='Mutation Status' class='mutation-table-header'>MS</th>
	<th alt='Validation Status' class='mutation-table-header'>VS</th>
	<th alt='Predicted Functional Impact Score (via Mutation Assessor) for missense mutations'
	    class='mutation-table-header'>Mutation Assessor</th>
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

<script type="text/template" id="mutation_details_fis_tip_template">
	Predicted impact score: <b>{{impact}}</b>
	<div class='mutation-assessor-main-link mutation-assessor-link'>
		<a href='{{linkOut}}' target='_blank'>
			<img height=15 width=19 src='images/ma.png'>
			Go to Mutation Assessor
		</a>
	</div>
	<div class='mutation-assessor-msa-link mutation-assessor-link'>
		<a href='{{msaLink}}' target='_blank'>
			<span class="ma-msa-icon">msa</span>
			Multiple Sequence Alignment
		</a>
	</div>
	<div class='mutation-assessor-3d-link mutation-assessor-link'>
		<a href='{{pdbLink}}' target='_blank'>
			<span class="ma-3d-icon">3D</span>
			Mutation Assessor 3D View
		</a>
	</div>
</script>

<script type="text/javascript" src="js/src/mutation/view/MutationDetailsTableView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/PredictedImpactTipView.js"></script>
<script type="text/javascript" src="js/src/mutation/view/CosmicTipView.js"></script>
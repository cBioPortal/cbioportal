<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
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
		   class="cc-short-study-name simple-tip">{{cancerStudyShort}}</b>
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
	<a href='{{pdbMatchLink}}' class="mutation-table-3d-link">
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
	<label class='{{cosmicClass}}'>{{cosmicCount}}</label>
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
	<span class='{{omaClass}} {{fisClass}}'>
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
	<a href='{{igvLink}}' class='igv-link'>
		<span class="mutation-table-igv-icon">IGV</span>
	</a>
</script>

<script type="text/template" id="mutation_table_mutation_count_template">
	<label class='{{mutationCountClass}}'>{{mutationCount}}</label>
</script>

<!-- end Mutation Table components -->

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

<script type="text/javascript" src="js/src/mutation/view/MutationDetailsTableView.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/view/PredictedImpactTipView.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/view/CosmicTipView.js?<%=GlobalProperties.getAppVersion()%>"></script>
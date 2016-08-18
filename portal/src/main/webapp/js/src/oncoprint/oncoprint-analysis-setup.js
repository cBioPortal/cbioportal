// This is for the moustache-like templates
// prevents collisions with JSP tags
$(document).ready(function() {
	_.templateSettings = {
		interpolate: /\{\{(.+?)\}\}/g
	};
	$('#oncoprint_controls').html(_.template($('#main-controls-template').html())());

	
	$.when(window.QuerySession.getGenomicEventData(), window.QuerySession.getPatientSampleIdMap(), window.QuerySession.getCombinedPatientGenomicEventData()).then(function(sample_genomic_event_data, sample_patient_map, patient_genomic_event_data) {
			(function invokeOldDataManagers() {
				var genes = window.QuerySession.getQueryGenes();
				window.PortalDataColl.setOncoprintData(utils.process_data(sample_genomic_event_data, genes));
				PortalDataColl.setOncoprintStat(utils.alteration_info(sample_genomic_event_data));
			})();
			$('#outer_loader_img').hide();
			$('#oncoprint #everything').show();
			window.onc_obj = setUpOncoprint('oncoprint_body', {
				sample_to_patient: sample_patient_map,
				sample_gene_data: sample_genomic_event_data,
				patient_gene_data: patient_genomic_event_data,
				toolbar_selector: '#oncoprint-diagram-toolbar-buttons',
				toolbar_fade_hitzone_selector: '#oncoprint',
				sample_list: window.QuerySession.getSampleIds(),
				cancer_study_id: cancer_study_id_selected,
				gene_order: window.QuerySession.getQueryGenes(),

				load_clinical_tracks: true,
				swap_patient_sample: true,
				sort_by: true,

				link_out_in_tooltips: true,
				percent_altered_indicator_selector: '#altered_value',
			});
		});
});
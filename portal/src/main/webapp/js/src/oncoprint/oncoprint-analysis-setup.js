// This is for the moustache-like templates
// prevents collisions with JSP tags
$(document).ready(function() {
	_.templateSettings = {
		interpolate: /\{\{(.+?)\}\}/g
	};
	$('#oncoprint_controls').html(_.template($('#main-controls-template').html())());

	
	window.QuerySession.getGenomicEventData().then(function(response) {
			(function invokeOldDataManagers() {
				var genes = window.QuerySession.getQueryGenes();
				window.PortalGlobals.setGeneData(response)
				window.PortalDataColl.setOncoprintData(utils.process_data(response, genes));
				PortalDataColl.setOncoprintStat(utils.alteration_info(response));
			})();
			$('#outer_loader_img').hide();
			$('#oncoprint #everything').show();
			window.onc_obj = setUpOncoprint('oncoprint_body', {
				sample_to_patient: window.PortalGlobals.getPatientSampleIdMap(),
				gene_data: response,
				toolbar_selector: '#oncoprint-diagram-toolbar-buttons',
				toolbar_fade_hitzone_selector: '#oncoprint',
				sample_list: window.PortalGlobals.getCases().trim().split(/\s+/),
				cancer_study_id: cancer_study_id_selected,
				gene_order: window.PortalGlobals.getGeneListString().split(/\s+/),

				load_clinical_tracks: true,
				swap_patient_sample: true,
				sort_by: true,

				link_out_in_tooltips: true,
				percent_altered_indicator_selector: '#altered_value',
			});
		});
});
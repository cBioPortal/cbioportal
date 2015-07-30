// This is for the moustache-like templates
// prevents collisions with JSP tags
_.templateSettings = {
	interpolate: /\{\{(.+?)\}\}/g
};
$('#oncoprint_controls').html(_.template($('#main-controls-template').html())());

window.makeOncoprinter = (function() {
	var processData = function(str) {
		var lines = str.split('\n');
		for (var i=1; i<lines.length; i++) {
			// start at i=1 to skip header line
			var sline = line.split(/\s+/);
			var sample = sline[0];
			var gene = sline[1];
			var alteration = sline[2];
		}
	};
	return function (str) {
		var data = processData(str);
		window.onc_obj = setUpOncoprint('oncoprint', {
			gene_data: data,
			toolbar_selector: '#oncoprint-diagram-toolbar-buttons',
			cancer_study_id: cancer_study_id_selected,
			load_clinical_tracks: false,
			swap_patient_sample: false,
			sort_by: false,
			link_out_in_tooltips: false
		});
	};
})();
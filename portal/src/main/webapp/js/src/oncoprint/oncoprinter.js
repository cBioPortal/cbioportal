// This is for the moustache-like templates
// prevents collisions with JSP tags
_.templateSettings = {
	interpolate: /\{\{(.+?)\}\}/g
};
$('#oncoprint_controls').html(_.template($('#main-controls-template').html())());

var makeOncoprinter = (function() {
	var processData = function(str) {
		var gene_to_sample_to_datum = {};
		var cna = {'AMP':'AMPLIFIED','GAIN':'GAINED', 'HETLOSS':'HETLOSS', 'HOMDEL':'HOMODELETED'};
		var mrna = {'UP':'UPREGULATED', 'DOWN':'DOWNREGULATED'};
		var lines = str.split('\n');
		var samples = {};
		for (var i=1; i<lines.length; i++) {
			// start at i=1 to skip header line
			var sline = lines[i].trim().split(/\s+/);
			var sample = sline[0].trim();
			if (sample === '') {
				continue;
			}
			samples[sample] = true;
			if (sline.length === 1) {
				continue;
			}
			var gene = sline[1].trim();
			var alteration = sline[2].trim();
			gene_to_sample_to_datum[gene] = gene_to_sample_to_datum[gene] || {};
			gene_to_sample_to_datum[gene][sample] = gene_to_sample_to_datum[gene][sample] || {'gene':gene, 'sample':sample};
			
			if (cna.hasOwnProperty(alteration)) {
				gene_to_sample_to_datum[gene][sample].cna = cna[alteration];
			} else if (mrna.hasOwnProperty(alteration)) {
				gene_to_sample_to_datum[gene][sample].mrna = mrna[alteration];
			} else {
				gene_to_sample_to_datum[gene][sample].mutation = alteration;
			}
		}
		var ret = [];
		_.each(gene_to_sample_to_datum, function(sample_data, gene) {
			_.each(Object.keys(samples), function(sample) {
				// pad out data
				if (!sample_data.hasOwnProperty(sample)) {
					ret.push({'gene':gene, 'sample':sample});
				}
			});
			_.each(sample_data, function(datum, sample) {
				ret.push(datum);
			});
		});
		return ret;
	};
	return function (str) {
		var data = processData(str);
		var gene_order = $('#filter_example').val().trim().split(/\s+/);
		if (gene_order[0] === '') {
			gene_order = undefined;
		}
		console.log(data);
		if (window.onc_obj) {
			window.onc_obj.destroy();
		}
		window.onc_obj = window.setUpOncoprint('oncoprint_body', {
			gene_data: data,
			toolbar_selector: '#oncoprint-diagram-toolbar-buttons',
			load_clinical_tracks: false,
			swap_patient_sample: false,
			sort_by: false,
			link_out_in_tooltips: false,
			gene_order: gene_order
		});
		$('#oncoprint-diagram-toolbar-buttons').show();
	};
})();

$('#create_oncoprint').click(function() {
	makeOncoprinter($('#mutation-file-example').val());
});

$('#mutation-form #mutation').change(function() { $('#mutation-file-example').html("") });
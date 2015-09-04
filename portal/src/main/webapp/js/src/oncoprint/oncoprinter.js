// This is for the moustache-like templates
// prevents collisions with JSP tags
_.templateSettings = {
	interpolate: /\{\{(.+?)\}\}/g
};
$('#oncoprint_controls').html(_.template($('#main-controls-template').html())());

var makeOncoprinter = (function() {
	var processData = function(str) {
		var gene_to_sample_to_datum = {};
		var cna = {'AMP':'AMPLIFIED','GAIN':'GAINED', 'HETLOSS':'HEMIZYGOUSLYDELETED', 'HOMDEL':'HOMODELETED'};
		var mrna = {'UP':'UPREGULATED', 'DOWN':'DOWNREGULATED'};
		var rppa = {'PROT-UP': 'UPREGULATED', 'PROT-DOWN':'DOWNREGULATED'};
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
			} else if (rppa.hasOwnProperty(alteration)) {
				gene_to_sample_to_datum[gene][sample].rppa = rppa[alteration];
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
		if (window.onc_obj) {
			window.onc_obj.destroy();
		}
		window.onc_obj = window.setUpOncoprint('oncoprint_body', {
			gene_data: data,
			toolbar_selector: '#oncoprint-diagram-toolbar-buttons',
			toolbar_fade_hitzone_selector: '#oncoprint',
			load_clinical_tracks: false,
			swap_patient_sample: false,
			sort_by: false,
			link_out_in_tooltips: false,
			gene_order: gene_order
		});
		$('#oncoprint-diagram-toolbar-buttons').show();
	};
})();

var isInputValid = function(str) {
	var lines = _.map(str.split('\n'), function(x) { return x.trim(); });
	if (lines[0].split(/\s+/).length !== 3) {
		return false;
	}
	for (var i=1; i<lines.length; i++) {
		var split_line_length = lines[i].split(/\s+/).length;
		if (split_line_length !== 1 && split_line_length !== 3) {
			return false;
		}
	}
	return true;
};
var postFile = function (url, formData, callback) {
	$.ajax({
		url: url,
		type: 'POST',
		success: callback,
		data: formData,
		//Options to tell jQuery not to process data or worry about content-type.
		cache: false,
		contentType: false,
		processData: false
	});
};

$(document).ready(function() {
	$('#create_oncoprint').click(function() {
		var textarea_input = $('#mutation-file-example').val().trim();
		if (textarea_input.length > 0) {
			if (isInputValid(textarea_input)) {
				makeOncoprinter($('#mutation-file-example').val());
				$('#error_msg').hide();
			} else {
				$('#error_msg').html("Error in input data");
				$('#error_msg').show();
			}
		} else if ($('#mutation-form #mutation').val().trim().length > 0) {
			postFile('echofile', new FormData($('#mutation-form')[0]), function(mutationResponse) {
				var input = mutationResponse.mutation.trim();
				if (isInputValid(input)) {
					makeOncoprinter(input);
					$('#error_msg').hide();
				} else {
					$('#error_msg').html("Error in input data");
					$('#error_msg').show();
				}
			});
		} else {
			$('#error_msg').html("Please input data or select a file.");
			$('#error_msg').show();
		}
	});
	$('#mutation-form #mutation').change(function() { $('#mutation-file-example').val(""); });
});
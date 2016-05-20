var processData = function (str) {
    var gene_to_sample_to_datum = {};
    var cna = {'AMP': 'AMPLIFIED', 'GAIN': 'GAINED', 'HETLOSS': 'HEMIZYGOUSLYDELETED', 'HOMDEL': 'HOMODELETED'};
    var mrna = {'UP': 'UPREGULATED', 'DOWN': 'DOWNREGULATED'};
    var rppa = {'PROT-UP': 'UPREGULATED', 'PROT-DOWN': 'DOWNREGULATED'};
    var lines = str.split('\n');
    var samples = {};
    for (var i = 1; i < lines.length; i++) {
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
	var type = sline[3].trim();

	gene_to_sample_to_datum[gene] = gene_to_sample_to_datum[gene] || {};
	gene_to_sample_to_datum[gene][sample] = gene_to_sample_to_datum[gene][sample] || {'gene': gene, 'sample': sample};

	if (cna.hasOwnProperty(alteration)) {
	    gene_to_sample_to_datum[gene][sample].cna = cna[alteration];
	} else if (mrna.hasOwnProperty(alteration)) {
	    gene_to_sample_to_datum[gene][sample].mrna = mrna[alteration];
	} else if (rppa.hasOwnProperty(alteration)) {
	    gene_to_sample_to_datum[gene][sample].rppa = rppa[alteration];
	} else {
	    gene_to_sample_to_datum[gene][sample].mutation = alteration;
	    gene_to_sample_to_datum[gene][sample].mut_type = type;
	}
    }
    var data_by_gene = {};
    var altered_by_gene = {};
    _.each(gene_to_sample_to_datum, function (sample_data, gene) {
	data_by_gene[gene] = [];
	altered_by_gene[gene] = [];
	_.each(Object.keys(samples), function (sample) {
	    // pad out data
	    if (!sample_data.hasOwnProperty(sample)) {
		data_by_gene[gene].push({'gene': gene, 'sample': sample});
	    }
	});
	_.each(sample_data, function (datum, sample) {
	    data_by_gene[gene].push(datum);
	    altered_by_gene[gene].push(sample);
	});
    });
    
    return {'data_by_gene':data_by_gene, 'altered_by_gene':altered_by_gene};
};

var isInputValid = function(str) {
	var lines = _.map(str.split('\n'), function(x) { return x.trim(); });
	if (lines[0].split(/\s+/).length !== 4) {
		return false;
	}
	for (var i=1; i<lines.length; i++) {
		var split_line_length = lines[i].split(/\s+/).length;
		if (split_line_length !== 1 && split_line_length !== 4) {
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


$(document).ready(function () {
    $('#oncoprint_controls').html(_.template($('#main-controls-template').html())());
    
    var updateOncoprinter = CreateOncoprinterWithToolbar('#oncoprint #oncoprint_body', '#oncoprint #oncoprint-diagram-toolbar-buttons');
    $('#create_oncoprint').click(function() {
	var textarea_input = $('#mutation-file-example').val().trim();
	var gene_order = $('#gene_order').val().trim().split(/\s+/g);
	if (gene_order.length === 0 || gene_order[0].length === 0) {
	    gene_order = null;
	}
	
	var sample_order = $('#sample_order').val().trim().split(/\s+/g);
	if (sample_order.length === 0 || sample_order[0].length === 0) {
	    sample_order = null;
	}
	if (textarea_input.length > 0) {
	    if (isInputValid(textarea_input)) {
		var process_result = processData($('#mutation-file-example').val());
		updateOncoprinter(process_result.data_by_gene, 'sample', process_result.altered_by_gene, sample_order, gene_order);
		$('#error_msg').hide();
	    } else {
		$('#error_msg').html("Error in input data");
		$('#error_msg').show();
	    }
	} else if ($('#mutation-form #mutation').val().trim().length > 0) {
	    var file = $('#mutation-form #mutation')[0].files[0];
	    var reader = new FileReader();
	    reader.readAsText(file);
	    reader.onload = function(e) {
		var input = reader.result.trim().replace(/\r/g, "\n");
		if (isInputValid(input)) {
		    var process_result = processData(input);
		    updateOncoprinter(process_result.data_by_gene, 'sample', process_result.altered_by_gene, sample_order, gene_order);
		    $('#error_msg').hide();
		} else {
		    $('#error_msg').html("Error in input data");
		    $('#error_msg').show();
		}
	    };
	    /*postFile('echofile', new FormData($('#mutation-form')[0]), function (mutationResponse) {
		var input = mutationResponse.mutation.trim();
		if (isInputValid(input)) {
		    var process_result = processData(input);
		    updateOncoprinter(process_result.data_by_gene, 'sample', process_result.altered_by_gene, sample_order, gene_order);
		    $('#error_msg').hide();
		} else {
		    $('#error_msg').html("Error in input data");
		    $('#error_msg').show();
		}
	    });*/
	} else {
	    $('#error_msg').html("Please input data or select a file.");
	    $('#error_msg').show();
	}
    });
});

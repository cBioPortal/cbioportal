var processData = function (str) {
    // Need to mock webservice data to be compatible with tooltip
    var gene_to_sample_to_datum = {};
    var cna = {'AMP': 'amp', 'GAIN': 'gain', 'HETLOSS': 'hetloss', 'HOMDEL': 'homdel'};
    var cna_int = {'AMP': '2', 'GAIN': '1', 'HETLOSS': '-1', 'HOMDEL': '-2'};
    var mrna = {'UP': 'up', 'DOWN': 'down'};
    var mrna_int = {'UP': 1, 'DOWN': -1};
    var prot = {'PROT-UP': 'up', 'PROT-DOWN': 'down'};
    var prot_int = {'PROT-UP': 1, 'PROT-DOWN': -1};
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
	var type = sline[3].trim().toLowerCase();

	gene_to_sample_to_datum[gene] = gene_to_sample_to_datum[gene] || {};
	gene_to_sample_to_datum[gene][sample] = gene_to_sample_to_datum[gene][sample] || {'gene': gene, 'sample': sample, 'data':[]};

	if (cna.hasOwnProperty(alteration)) {
	    gene_to_sample_to_datum[gene][sample].data.push({
		genetic_alteration_type: 'COPY_NUMBER_ALTERATION',
		profile_data: cna_int[alteration]
	    });
	    gene_to_sample_to_datum[gene][sample].disp_cna = cna[alteration];
	} else if (mrna.hasOwnProperty(alteration)) {
	    gene_to_sample_to_datum[gene][sample].data.push({
		genetic_alteration_type: 'MRNA_EXPRESSION',
		oql_regulation_direction: mrna_int[alteration]
	    });
	    gene_to_sample_to_datum[gene][sample].disp_mrna = mrna[alteration];
	} else if (prot.hasOwnProperty(alteration)) {
	    gene_to_sample_to_datum[gene][sample].disp_prot = prot[alteration];
	    gene_to_sample_to_datum[gene][sample].data.push({
		genetic_alteration_type: 'PROTEIN_LEVEL',
		oql_regulation_direction: prot_int[alteration]
	    });
	} else {
		var ws_datum = {
		genetic_alteration_type: 'MUTATION_EXTENDED',
		amino_acid_change: alteration,
	    };
	    if (type === "fusion") {
	    	ws_datum.oncoprint_mutation_type = "fusion";
	    	gene_to_sample_to_datum[gene][sample].disp_fusion = true;
	    } else {
	    	gene_to_sample_to_datum[gene][sample].disp_mut = type;
	    }
	    gene_to_sample_to_datum[gene][sample].data.push(ws_datum);
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
		data_by_gene[gene].push({'gene': gene, 'sample': sample, 'data': []});
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

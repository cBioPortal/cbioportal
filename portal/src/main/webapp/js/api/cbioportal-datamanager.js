window.initDatamanager = function (genetic_profile_ids, oql_query, cancer_study_ids, sample_ids, z_score_threshold, rppa_score_threshold,
	case_set_properties, cancer_study_names, profile_ids) {

    var deepCopyObject = function (obj) {
	return $.extend(true, ($.isArray(obj) ? [] : {}), obj);
    };
    var objectValues = function (obj) {
	return Object.keys(obj).map(function (key) {
	    return obj[key];
	});
    };
    var objectKeyDifference = function (from, by) {
	var ret = {};
	var from_keys = Object.keys(from);
	for (var i = 0; i < from_keys.length; i++) {
	    if (!by[from_keys[i]]) {
		ret[from_keys[i]] = true;
	    }
	}
	return ret;
    };
    var objectKeyValuePairs = function (obj) {
	return Object.keys(obj).map(function (key) {
	    return [key, obj[key]];
	});
    };
    var objectKeyUnion = function (list_of_objs) {
	var union = {};
	for (var i = 0; i < list_of_objs.length; i++) {
	    var keys = Object.keys(list_of_objs[i]);
	    for (var j = 0; j < keys.length; j++) {
		union[keys[j]] = true;
	    }
	}
	return union;
    };
    var objectKeyIntersection = function(list_of_objs) {
	var intersection = {};
	for (var i = 0; i < list_of_objs.length; i++) {
	    if (i === 0) {
		var keys = Object.keys(list_of_objs[0]);
		for (var j = 0; j < keys.length; j++) {
		    intersection[keys[j]] = true;
		}
	    } else {
		var obj = list_of_objs[i];
		var keys = Object.keys(intersection);
		for (var j=0; j<keys.length; j++) {
		    if (!obj[keys[j]]) {
			delete intersection[keys[j]];
		    }
		}
	    }
	}
	return intersection;
    };
    var stringListToObject = function (list) {
	var ret = {};
	for (var i = 0; i < list.length; i++) {
	    ret[list[i]] = true;
	}
	return ret;
    };
    var stringListDifference = function (from, by) {
	return Object.keys(
		objectKeyDifference(
			stringListToObject(from),
			stringListToObject(by)));
    };
    var stringListUnion = function (list_of_string_lists) {
	return Object.keys(
		objectKeyUnion(
			list_of_string_lists.map(function (string_list) {
			    return stringListToObject(string_list);
			})
			));
    };
    var stringListUnique = function (list) {
	return Object.keys(stringListToObject(list));
    };
    var getSimplifiedMutationType = function (type) {
	var ret = null;
	type = type.toLowerCase();
	switch (type) {
	    case "missense_mutation":
	    case "missense":
	    case "missense_variant":
		ret = "missense";
		break;
	    case "frame_shift_ins":
	    case "frame_shift_del":
	    case "frameshift":
	    case "frameshift_deletion":
	    case "frameshift_insertion":
	    case "de_novo_start_outofframe":
	    case "frameshift_variant":
		ret = "frameshift";
		break;
	    case "nonsense_mutation":
	    case "nonsense":
	    case "stopgain_snv":
		ret = "nonsense";
		break;
	    case "splice_site":
	    case "splice":
	    case "splice site":
	    case "splicing":
	    case "splice_site_snp":
	    case "splice_site_del":
	    case "splice_site_indel":
		ret = "splice";
		break;
	    case "translation_start_site":
	    case "start_codon_snp":
	    case "start_codon_del":
		ret = "nonstart";
		break;
	    case "nonstop_mutation":
		ret = "nonstop";
		break;
	    case "fusion":
		ret = "fusion";
		break;
	    case "in_frame_del":
	    case "in_frame_ins":
	    case "indel":
	    case "nonframeshift_deletion":
	    case "nonframeshift":
	    case "nonframeshift insertion":
	    case "nonframeshift_insertion":
	    case "targeted_region":
		ret = "inframe";
		break;
	    default:
		ret = "other";
		break;
	}
	return ret;
    };

    var getCBioPortalMutationCounts = function (webservice_data) {
	/* In: - webservice_data, a list of data obtained from the webservice API
	 * Out: Map from gene+","+start_pos+","+end_pos to cbioportal mutation count for that position range and gene
	 */
	var counts_map = {};
	var def = new $.Deferred();
	var to_query = {};
	for (var i = 0; i < webservice_data.length; i++) {
	    var datum = webservice_data[i];
	    if (datum.genetic_alteration_type !== "MUTATION_EXTENDED") {
		continue;
	    }
	    var gene = datum.hugo_gene_symbol;
	    var start_pos = datum.protein_start_position;
	    var end_pos = datum.protein_end_position;
	    if (gene && start_pos && end_pos && !isNaN(start_pos) && !isNaN(end_pos)) {
		to_query[gene + ',' + parseInt(start_pos, 10) + ',' + parseInt(end_pos, 10)] = true;
	    }
	}
	var queries = Object.keys(to_query).map(function (x) {
	    var splitx = x.split(',');
	    return {
		gene: splitx[0],
		start_pos: splitx[1],
		end_pos: splitx[2]
	    };
	});
	var genes = queries.map(function (q) {
	    return q.gene;
	});
	var starts = queries.map(function (q) {
	    return q.start_pos;
	});
	var ends = queries.map(function (q) {
	    return q.end_pos;
	});

	window.cbioportal_client.getMutationCounts({
	    'type': 'count',
	    'per_study': false,
	    'gene': genes,
	    'start': starts,
	    'end': ends,
	    'echo': ['gene', 'start', 'end']
	}).then(function (counts) {
	    for (var i = 0; i < counts.length; i++) {
		var gene = counts[i].gene;
		var start = parseInt(counts[i].start, 10);
		var end = parseInt(counts[i].end, 10);
		counts_map[gene + ',' + start + ',' + end] = parseInt(counts[i].count, 10);
	    }
	    def.resolve(counts_map);
	}).fail(function () {
	    def.reject();
	});
	return def.promise();
    };
    var getOncoKBAnnotations = function(webservice_data) {
	var is_oncogenic = {}; // #{gene}&#{alteration.toUpperCase()}&{tumor_type.toUpperCase()} is key, boolean is value
	var query = {
	    "geneStatus": "Complete",
	    "source": "cbioportal",
	    "evidenceTypes": "GENE_SUMMARY,GENE_BACKGROUND,ONCOGENIC,MUTATION_EFFECT,VUS,STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY,STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE,INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY",
	    "queries": [
		{
		    "hugoSymbol": "BRAF",
		    "alteration": "V600E",
		    "tumorType": "Melanoma"
		}
	    ],
	    "levels": [
		"LEVEL_1",
		"LEVEL_2A",
		"LEVEL_3A",
		"LEVEL_R1"
	    ]
	}
    };
    var annotateCBioPortalMutationCount = function (webservice_data) {
	/* in-place, idempotent
	 * In: - webservice_data, a list of data obtained from the webservice API
	 * Out: promise, which resolves with the data which has been in-place modified,
	 *	    the mutation data given the integer attribute 'cbioportal_position_recurrence'
	 */
	var def = new $.Deferred();
	var attribute_name = 'cbioportal_mutation_count';
	getCBioPortalMutationCounts(webservice_data).then(function (counts_map) {
	    for (var i = 0; i < webservice_data.length; i++) {
		var datum = webservice_data[i];
		if (datum.genetic_alteration_type !== "MUTATION_EXTENDED") {
		    continue;
		}
		var gene = datum.hugo_gene_symbol;
		var start_pos = datum.protein_start_position;
		var end_pos = datum.protein_end_position;
		if (gene && start_pos && end_pos && !isNaN(start_pos) && !isNaN(end_pos)) {
		    datum[attribute_name] = counts_map[gene + ',' + parseInt(start_pos, 10) + ',' + parseInt(end_pos, 10)];
		}
	    }
	    def.resolve(webservice_data);
	});
	return def.promise();
    };
    var annotateOncoKBMutationOncogenic = function(webservice_data) {
	/* in-place, idempotent
	 * In: - webservice_data, a list of data obtained from the webservice API
	 * Out: promise, which resolves with the data which has been in-place modified,
	 *	    the mutation data given the boolean attribute 'oncokb_oncogenic'
	 */
	
    };
    var makeOncoprintClinicalData = function (webservice_clinical_data, attr_id, source_sample_or_patient, target_sample_or_patient,
	    target_ids, sample_to_patient_map, datatype_number_or_string, na_or_zero) {
	na_or_zero = na_or_zero || "na";
	var id_to_datum = {};
	var id_attribute = source_sample_or_patient + '_id'; // sample_id or patient_id
	for (var i = 0, _len = webservice_clinical_data.length; i < _len; i++) {
	    var d = webservice_clinical_data[i];
	    var id = d[id_attribute];
	    if (source_sample_or_patient === "sample" && target_sample_or_patient === "patient") {
		id = sample_to_patient_map[id];
	    }
	    if (!id_to_datum[id]) {
		id_to_datum[id] = {'attr_id': attr_id, 'attr_val_counts': {}};
	    }
	    if (typeof d.attr_val !== 'undefined') {
		id_to_datum[id].attr_val_counts[d.attr_val] = id_to_datum[id].attr_val_counts[d.attr_val] || 0;
		id_to_datum[id].attr_val_counts[d.attr_val] += 1;
	    }
	}
	var data = [];
	var datatype_is_number = (datatype_number_or_string.toLowerCase() === "number");
	for (var i = 0; i < target_ids.length; i++) {
	    var datum_to_add = {};
	    var existing_datum = {};
	    if (source_sample_or_patient === "patient" && target_sample_or_patient === "sample") {
		existing_datum = id_to_datum[sample_to_patient_map[target_ids[i]]];
	    } else {
		existing_datum = id_to_datum[target_ids[i]];
	    }
	    if (existing_datum) {
		datum_to_add = deepCopyObject(existing_datum);
	    } else {
		datum_to_add = {'attr_id': attr_id, 'attr_val_counts': {}};
	    }
	    datum_to_add[target_sample_or_patient] = target_ids[i];

	    var values = Object.keys(datum_to_add.attr_val_counts);
	    if (datatype_is_number) {
		values = values.map(function (x) {
		    return (isNaN(x) ? x : parseFloat(x));
		});
	    }

	    var disp_attr_val = undefined;
	    if (values.length === 0) {
		if (na_or_zero === "na") {
		    datum_to_add.na = true;
		} else if (na_or_zero === "zero") {
		    datum_to_add.attr_val_counts[0] = 1;
		    disp_attr_val = 0;
		}
	    } else if (values.length === 1) {
		disp_attr_val = values[0];
	    } else {
		if (datatype_is_number) {
		    var avg = 0;
		    var total = 0;
		    for (var j = 0; j < values.length; j++) {
			var multiplicity = datum_to_add.attr_val_counts[values[j]];
			avg += values[j] * multiplicity;
			total += multiplicity;
		    }
		    disp_attr_val = ((total > 0) ? (avg / total) : 0);
		} else {
		    disp_attr_val = "Mixed";
		}
	    }
	    if (typeof disp_attr_val !== "undefined") {
		datum_to_add.attr_val = disp_attr_val;
	    }
	    data.push(datum_to_add);
	}
	return data;
    };
    var makeOncoprintData = function (webservice_data, genes, ids, sample_or_patient, sample_to_patient_map) {
	// To fill in for non-existent data, need genes and samples to do so for
	genes = genes || [];
	ids = ids || []; // to make blank data
	// Gather data by id and gene
	var gene_and_id_to_datum = {};
	for (var i = 0; i < genes.length; i++) {
	    var gene = genes[i].toUpperCase();
	    for (var j = 0; j < ids.length; j++) {
		var id = ids[j];
		var new_datum = {};
		new_datum['gene'] = gene;
		new_datum[sample_or_patient] = id;
		new_datum['data'] = [];
		gene_and_id_to_datum[gene + "," + id] = new_datum;
	    }
	}
	for (var i = 0; i < webservice_data.length; i++) {
	    var datum = webservice_data[i];
	    var gene = datum.hugo_gene_symbol.toUpperCase();
	    var id = (sample_or_patient === "patient" ? sample_to_patient_map[datum.sample_id] : datum.sample_id);
	    var gene_id_datum = gene_and_id_to_datum[gene + "," + id];
	    if (gene_id_datum) {
		gene_id_datum.data.push(datum);
	    }
	}

	// Compute display parameters
	var data = objectValues(gene_and_id_to_datum);
	var cna_profile_data_to_string = {
	    "-2": "homdel", 
	    "-1": "hetloss",
	    "0": undefined,
	    "1": "gain",
	    "2": "amp"
	};
	var mut_rendering_priority = {'fusion': 0, 'trunc': 1, 'inframe': 2, 'missense': 3};
	var cna_rendering_priority = {'amp': 0, 'homdel': 0, 'gain': 1, 'hetloss': 1};
	var mrna_rendering_priority = {'up': 0, 'down': 0};
	var prot_rendering_priority = {'up': 0, 'down': 0};

	var selectDisplayValue = function (counts_obj, rendering_priority_obj) {
	    var options = objectKeyValuePairs(counts_obj);
	    if (options.length > 0) {
		options.sort(function (kv1, kv2) {
		    var rendering_priority_diff = rendering_priority_obj[kv1[0]] - rendering_priority_obj[kv2[0]];
		    if (rendering_priority_diff < 0) {
			return -1;
		    } else if (rendering_priority_diff > 0) {
			return 1;
		    } else {
			if (kv1[1] < kv2[1]) {
			    return 1;
			} else if (kv1[1] > kv2[1]) {
			    return -1;
			} else {
			    return 0;
			}
		    }
		});
		return options[0][0];
	    } else {
		return undefined;
	    }
	};
	for (var i = 0; i < data.length; i++) {
	    var datum = data[i];
	    var datum_events = datum.data;

	    var disp_cna_counts = {};
	    var disp_mrna_counts = {};
	    var disp_prot_counts = {};
	    var disp_mut_counts = {};
	    var disp_mut_has_rec = {};

	    for (var j = 0; j < datum_events.length; j++) {
		var event = datum_events[j];
		if (event.genetic_alteration_type === "COPY_NUMBER_ALTERATION") {
		    var cna_event = cna_profile_data_to_string[event.profile_data];
		    disp_cna_counts[cna_event] = disp_cna_counts[cna_event] || 0;
		    disp_cna_counts[cna_event] += 1;
		} else if (event.genetic_alteration_type === "MRNA_EXPRESSION") {
		    if (datum.oql_regulation_direction) {
			var mrna_event = (datum.oql_regulation_direction === 1 ? "up" : "down");
			disp_mrna_counts[mrna_event] = disp_mrna_counts[mrna_event] || 0;
			disp_mrna_counts[mrna_event] += 1;
		    }
		} else if (event.genetic_alteration_type === "PROTEIN_LEVEL") {
		    if (datum.oql_regulation_direction) {
			var prot_event = (datum.oql_regulation_direction === 1 ? "up" : "down");
			disp_prot_counts[prot_event] = disp_prot_counts[prot_event] || 0;
			disp_prot_counts[prot_event] += 1;
		    }
		} else if (event.genetic_alteration_type === "MUTATION_EXTENDED") {
		    var mutation_type = event.simplified_mutation_type;
		    // clamp all mutation types into one of the following four
		    var oncoprint_mutation_type = (["missense", "inframe", "fusion"].indexOf(mutation_type) > -1 ? mutation_type : "trunc");
		    disp_mut_counts[oncoprint_mutation_type] = disp_mut_counts[oncoprint_mutation_type] || 0;
		    disp_mut_counts[oncoprint_mutation_type] += 1;

		    if (event.cbioportal_mutation_count > 10) {
			disp_mut_has_rec[oncoprint_mutation_type] = true;
		    }
		}
	    }
	    datum.disp_cna = selectDisplayValue(disp_cna_counts, cna_rendering_priority);
	    datum.disp_mrna = selectDisplayValue(disp_mrna_counts, mrna_rendering_priority);
	    datum.disp_prot = selectDisplayValue(disp_prot_counts, prot_rendering_priority);
	    var disp_mut = selectDisplayValue(disp_mut_counts, mut_rendering_priority);
	    datum.disp_mut = disp_mut;
	    if (disp_mut) {
		datum.disp_mut += (disp_mut_has_rec[disp_mut] ? '_rec' : '');
	    }
	}
	return data;
    };
    var makeOncoprintSampleData = function (webservice_data, genes, sample_ids) {
	return makeOncoprintData(webservice_data, genes, sample_ids, "sample");
    };
    var makeOncoprintPatientData = function (webservice_data, genes, patient_ids, sample_to_patient_map) {
	return makeOncoprintData(webservice_data, genes, patient_ids, "patient", sample_to_patient_map);
    };

    var default_oql = '';
    var getClinicalData = function (self, attr_ids, target_sample_or_patient) {
	// Helper function for getSampleClinicalData and getPatientClinicalData
	var def = new $.Deferred();
	// special cases: '# mutations' and 'FRACTION_GENOME_ALTERED', both are sample attributes
	var special_sample_data = [];
	var special_sample_data_promises = [];
	attr_ids = attr_ids.slice();
	if (attr_ids.indexOf('# mutations') > -1) {
	    var clinicalMutationColl = new ClinicalMutationColl();
	    var num_mutations_promise = new $.Deferred();
	    special_sample_data_promises.push(num_mutations_promise.promise());
	    clinicalMutationColl.fetch({
		type: "POST",
		data: {
		    mutation_profile: self.getMutationProfileId(),
		    cmd: "count_mutations",
		    case_ids: self.getSampleIds().join(" ")
		},
		success: function (response) {
		    response = response.toJSON();
		    for (var i = 0; i < response.length; i++) {
			response[i].sample_id = response[i].sample; // standardize
		    }
		    special_sample_data = special_sample_data.concat(response);
		    num_mutations_promise.resolve();
		},
		error: function () {
		    num_mutations_promise.reject();
		}
	    });
	}
	if (attr_ids.indexOf('FRACTION_GENOME_ALTERED') > -1) {
	    var clinicalCNAColl = new ClinicalCNAColl();
	    var fraction_genome_altered_promise = new $.Deferred();
	    special_sample_data_promises.push(fraction_genome_altered_promise.promise());
	    clinicalCNAColl.fetch({
		type: "POST",
		data: {
		    cancer_study_id: self.getCancerStudyIds()[0],
		    cmd: "get_cna_fraction",
		    case_ids: self.getSampleIds().join(" ")
		},
		success: function (response) {
		    response = response.toJSON();
		    for (var i = 0; i < response.length; i++) {
			response[i].sample_id = response[i].sample; // standardize
		    }
		    special_sample_data = special_sample_data.concat(response);
		    fraction_genome_altered_promise.resolve();
		},
		error: function () {
		    fraction_genome_altered_promise.reject();
		}
	    });
	}
	$.when(self.sortClinicalAttributesByDataType(attr_ids), self.getPatientIds()).then(function (sorted_attrs, patient_ids) {
	    if (attr_ids.indexOf('# mutations') > -1) {
		attr_ids.splice(attr_ids.indexOf('# mutations'), 1);
	    }
	    if (attr_ids.indexOf('FRACTION_GENOME_ALTERED') > -1) {
		attr_ids.splice(attr_ids.indexOf('FRACTION_GENOME_ALTERED'), 1);
	    }
	    $.when(window.cbioportal_client.getSampleClinicalData({study_id: [self.getCancerStudyIds()[0]], attribute_ids: Object.keys(sorted_attrs.sample), sample_ids: self.getSampleIds()}),
		    window.cbioportal_client.getPatientClinicalData({study_id: [self.getCancerStudyIds()[0]], attribute_ids: Object.keys(sorted_attrs.patient), patient_ids: patient_ids}),
		    self.getPatientSampleIdMap(),
		    $.when.apply($, special_sample_data_promises)).then(function (sample_data, patient_data, sample_to_patient_map) {
		var sample_data_by_attr_id = {};
		var patient_data_by_attr_id = {};
		sample_data = sample_data.concat(special_sample_data);
		for (var i = 0; i < sample_data.length; i++) {
		    var attr_id = sample_data[i].attr_id;
		    sample_data_by_attr_id[attr_id] = sample_data_by_attr_id[attr_id] || [];
		    sample_data_by_attr_id[attr_id].push(sample_data[i]);
		}
		for (var i = 0; i < patient_data.length; i++) {
		    var attr_id = patient_data[i].attr_id;
		    patient_data_by_attr_id[attr_id] = patient_data_by_attr_id[attr_id] || [];
		    patient_data_by_attr_id[attr_id].push(patient_data[i]);
		}
		var data = [];
		var sample_attr_ids = Object.keys(sample_data_by_attr_id);
		var patient_attr_ids = Object.keys(patient_data_by_attr_id);
		var target_ids = (target_sample_or_patient === "sample" ? self.getSampleIds() : patient_ids);
		for (var i = 0; i < sample_attr_ids.length; i++) {
		    var attr_id = sample_attr_ids[i];
		    var na_or_zero = (attr_id === "# mutations" ? "zero" : "na");
		    var datatype = sorted_attrs.sample[attr_id].datatype.toLowerCase();
		    data = data.concat(makeOncoprintClinicalData(sample_data_by_attr_id[attr_id], attr_id, "sample", target_sample_or_patient, target_ids, sample_to_patient_map, datatype, na_or_zero));
		}
		for (var i = 0; i < patient_attr_ids.length; i++) {
		    var attr_id = patient_attr_ids[i];
		    var na_or_zero = (attr_id === "# mutations" ? "zero" : "na");
		    var datatype = sorted_attrs.patient[attr_id].datatype.toLowerCase();
		    data = data.concat(makeOncoprintClinicalData(patient_data_by_attr_id[attr_id], attr_id, "patient", target_sample_or_patient, target_ids, sample_to_patient_map, datatype, na_or_zero));
		}
		def.resolve(data);
	    });
	});
	return def.promise();
    };

    var makeCachedPromiseFunction = function (fetcher, should_deep_copy_result) {
	// In: fetcher, a function that takes a promise as an argument, and resolves it with the desired data
	// Out: a function which returns a promise that resolves with the desired data, deep copied
	//	The idea is that the fetcher is only ever called once, even if the output function
	//	of this method is called again while it's still waiting.
	var fetch_promise = new $.Deferred();
	var fetch_initiated = false;
	return function () {
	    var def = new $.Deferred();
	    if (!fetch_initiated) {
		fetch_initiated = true;
		fetcher(this, fetch_promise);
	    }
	    fetch_promise.then(function (data) {
		def.resolve((should_deep_copy_result ? data.map(deepCopyObject) : data));
	    });
	    return def.promise();
	};
    };

    return {
	'oql_query': oql_query,
	'cancer_study_ids': cancer_study_ids,
	'sample_ids': sample_ids,
	'genetic_profile_ids': genetic_profile_ids,
	'mutation_counts': {},
	'getOQLQuery': function () {
	    return this.oql_query;
	},
	'getQueryGenes': function () {
	    return OQL.genes(this.oql_query);
	},
	'getGeneticProfileIds': function () {
	    return this.genetic_profile_ids;
	},
	'getSampleIds': function () {
	    return this.sample_ids;
	},
	'getPatientIds': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    var patients = {};
		    window.cbioportal_client.getSamples({study_id: [self.getCancerStudyIds()[0]], sample_ids: self.getSampleIds()}).then(function (samples) {
			for (var i = 0; i < samples.length; i++) {
			    patients[samples[i].patient_id] = true;
			}
			fetch_promise.resolve(Object.keys(patients));
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getCancerStudyIds': function () {
	    return this.cancer_study_ids;
	},
	'getSampleSelect': function () {
	    return this.sample_select;
	},
	'getAlteredGenes': function() {
	    // A gene is "altered" if, after OQL filtering, there is a datum for it
	    var def = new $.Deferred();
	    this.getWebServiceGenomicEventData().then(function(data) {
		var altered_genes = {};
		for (var i=0; i<data.length; i++) {
		    altered_genes[data[i].hugo_gene_symbol] = true;
		}
		def.resolve(Object.keys(altered_genes));
	    }).fail(function() {
		def.reject();
	    });
	    return def.promise();
	},
	'getAlteredGenesSetBySample': function() {
	    var def = new $.Deferred();
	    this.getWebServiceGenomicEventData().then(function(data) {
		var ret = {};
		for (var i=0; i<data.length; i++) {
		    var sample = data[i].sample_id;
		    var gene = data[i].hugo_gene_symbol;
		    ret[sample] = ret[sample] || {};
		    ret[sample][gene] = true;
		}
		def.resolve(ret);
	    });
	    return def.promise();
	},
	'getWebServiceGenomicEventData': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    var profile_types = {};
		    window.cbioportal_client.getGeneticProfiles({genetic_profile_ids: self.getGeneticProfileIds()}).fail(function () {
			fetch_promise.reject();
		    }).then(function (gp_response) {
			for (var i = 0; i < gp_response.length; i++) {
			    profile_types[gp_response[i].id] = gp_response[i].genetic_alteration_type;
			}
			(function setDefaultOQL() {
			    var all_profile_types = objectValues(profile_types);
			    var default_oql_uniq = {};
			    for (var i = 0; i < all_profile_types.length; i++) {
				var type = all_profile_types[i];
				switch (type) {
				    case "MUTATION_EXTENDED":
					default_oql_uniq["MUT"] = true;
					break;
				    case "COPY_NUMBER_ALTERATION":
					default_oql_uniq["AMP"] = true;
					default_oql_uniq["HOMDEL"] = true;
					break;
				    case "MRNA_EXPRESSION":
					default_oql_uniq["EXP>=" + z_score_threshold] = true;
					default_oql_uniq["EXP<=-" + z_score_threshold] = true;
					break;
				    case "PROTEIN_LEVEL":
					default_oql_uniq["PROT>=" + rppa_score_threshold] = true;
					default_oql_uniq["PROT<=-" + rppa_score_threshold] = true;
					break;
				}
			    }
			    default_oql = Object.keys(default_oql_uniq).join(" ");
			})();
		    }).fail(function () {
			fetch_promise.reject();
		    }).then(function () {
			var genetic_profile_ids = self.getGeneticProfileIds();
			var num_calls = genetic_profile_ids.length;
			var all_data = [];
			for (var i = 0; i < self.getGeneticProfileIds().length; i++) {
			    (function (I) {
				window.cbioportal_client.getGeneticProfileDataBySample({
				    'genetic_profile_ids': [genetic_profile_ids[I]],
				    'genes': self.getQueryGenes().map(function(x) { return x.toUpperCase(); }),
				    'sample_ids': self.getSampleIds()
				}).fail(function () {
				    fetch_promise.reject();
				}).then(function (data) {
				    var genetic_alteration_type = profile_types[genetic_profile_ids[I]];
				    if (genetic_alteration_type === "MUTATION_EXTENDED") {
					for (var j = 0; j < data.length; j++) {
					    data[j].simplified_mutation_type = getSimplifiedMutationType(data[j].mutation_type);
					    data[j].genetic_alteration_type = genetic_alteration_type;
					}
				    } else {
					for (var j = 0; j < data.length; j++) {
					    data[j].genetic_alteration_type = genetic_alteration_type;
					}
				    }
				    all_data = all_data.concat(data);

				    num_calls -= 1;
				    if (num_calls === 0) {
					var webservice_genomic_event_data = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), all_data, default_oql, false);
					annotateCBioPortalMutationCount(webservice_genomic_event_data).then(function () {
					    fetch_promise.resolve(webservice_genomic_event_data);
					});
				    }
				});
			    })(i);
			}
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getGeneAggregatedOncoprintSampleGenomicEventData': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getWebServiceGenomicEventData().then(function (ws_data) {
			var filtered_ws_data = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), ws_data, default_oql, false);
			fetch_promise.resolve(makeOncoprintSampleData(filtered_ws_data, self.getQueryGenes(), self.getSampleIds()));
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getOncoprintSampleGenomicEventData': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getWebServiceGenomicEventData().then(function (ws_data) {
			var ws_data_by_oql_line = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), ws_data, default_oql, true);
			for (var i = 0; i < ws_data_by_oql_line.length; i++) {
			    var line = ws_data_by_oql_line[i];
			    line.oncoprint_data = makeOncoprintSampleData(line.data, [line.gene], self.getSampleIds());
			    line.altered_samples = line.oncoprint_data.filter(function (datum) {
				return datum.data.length > 0;
			    })
				    .map(function (datum) {
					return datum.sample;
				    });
			    line.unaltered_samples = stringListDifference(self.getSampleIds(), line.altered_samples);
			}
			var oncoprint_sample_genomic_event_data = ws_data_by_oql_line;
			fetch_promise.resolve(oncoprint_sample_genomic_event_data.map(deepCopyObject));
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getAlteredSamples': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getOncoprintSampleGenomicEventData().then(function (data_by_line) {
			var altered_samples = stringListUnion(data_by_line.map(function (line) {
			    return line.altered_samples;
			}));
			fetch_promise.resolve(altered_samples);
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getUnalteredSamples': function () {
	    var def = new $.Deferred();
	    var self = this;
	    this.getAlteredSamples().then(function (altered_samples) {
		def.resolve(stringListDifference(self.getSampleIds(), altered_samples));
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getMutualAlterationCounts': makeCachedPromiseFunction(
		function(self, fetch_promise) {
			self.getAlteredSampleSetsByGene().then(function(altered_samples_by_gene) {
			    var genes = Object.keys(altered_samples_by_gene);
			    var all_samples_set = stringListToObject(self.getSampleIds());
			    var ret = [];
			    for (var i=0; i<genes.length; i++) {
				for (var j=i+1; j<genes.length; j++) {
				    var count_object = {};
				    var geneA = genes[i];
				    var geneB = genes[j];
				    count_object.geneA = geneA;
				    count_object.geneB = geneB;
				    var alteredA = altered_samples_by_gene[geneA];
				    var alteredB = altered_samples_by_gene[geneB];
				    count_object.both = Object.keys(objectKeyIntersection([alteredA, alteredB])).length;
				    count_object.A_not_B = Object.keys(objectKeyDifference(alteredA, alteredB)).length;
				    count_object.B_not_A = Object.keys(objectKeyDifference(alteredB, alteredA)).length;
				    count_object.neither = Object.keys(
								objectKeyDifference(all_samples_set, 
										    objectKeyUnion([alteredA, alteredB])
										)
									).length;
				    ret.push(count_object);
				}
			    }
			    fetch_promise.resolve(ret);
			}).fail(function() {
			    fetch_promise.reject();
			});
		}),
	'getAlteredSampleSetsByGene': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    self.getWebServiceGenomicEventData().then(function(ws_data) {
			var altered_samples_by_gene = {};
			var genes = self.getQueryGenes();
			for (var i=0; i<genes.length; i++) {
			    altered_samples_by_gene[genes[i]] = {};
			}
			for (var i=0; i<ws_data.length; i++) {
			    var gene = ws_data[i].hugo_gene_symbol.toUpperCase();
			    var sample = ws_data[i].sample_id;
			    altered_samples_by_gene[gene] && (altered_samples_by_gene[gene][sample] = true);
			}
			fetch_promise.resolve(altered_samples_by_gene);
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getOncoprintPatientGenomicEventData': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    $.when(self.getWebServiceGenomicEventData(), self.getPatientIds(), self.getPatientSampleIdMap()).then(function (ws_data, patient_ids, sample_to_patient_map) {
			var ws_data_by_oql_line = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), ws_data, default_oql, true);
			for (var i = 0; i < ws_data_by_oql_line.length; i++) {
			    var line = ws_data_by_oql_line[i];
			    line.oncoprint_data = makeOncoprintPatientData(ws_data_by_oql_line[i].data, [ws_data_by_oql_line[i].gene], patient_ids, sample_to_patient_map);
			    line.altered_patients = line.oncoprint_data.filter(function (datum) {
				return datum.data.length > 0;
			    })
				    .map(function (datum) {
					return datum.patient;
				    });
			    line.unaltered_patients = stringListDifference(patient_ids, line.altered_patients);
			}
			var oncoprint_patient_genomic_event_data = ws_data_by_oql_line;
			fetch_promise.resolve(oncoprint_patient_genomic_event_data.map(deepCopyObject));
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getAlteredPatients': function () {
	    var def = new $.Deferred();
	    $.when(this.getAlteredSamples(), this.getPatientSampleIdMap()).then(function (altered_samples, sample_to_patient_map) {
		def.resolve(stringListUnique(altered_samples.map(function (s) {
		    return sample_to_patient_map[s];
		})));
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getUnalteredPatients': function () {
	    var def = new $.Deferred();
	    $.when(this.getAlteredPatients(), this.getPatientIds()).then(function (altered_patients, patient_ids) {
		def.resolve(stringListDifference(patient_ids, altered_patients));
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getSampleClinicalAttributes': function () {
	    var def = new $.Deferred();
	    this.getSampleClinicalAttributesSet().then(function (set) {
		def.resolve(objectValues(set));
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getSampleClinicalData': function (attribute_ids) {
	    // TODO: handle more than one study
	    //
	    return getClinicalData(this, attribute_ids, "sample");
	},
	'getPatientClinicalAttributes': function () {
	    var def = new $.Deferred();
	    this.getPatientClinicalAttributesSet().then(function (set) {
		def.resolve(objectValues(set));
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getSampleClinicalAttributesSet': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    // TODO: handle more than one study
		    window.cbioportal_client.getSampleClinicalAttributes({study_id: [self.getCancerStudyIds()[0]], sample_ids: self.getSampleIds()}).then(function (response) {
			var sample_clinical_attributes_set = {};
			for (var i = 0; i < response.length; i++) {
			    sample_clinical_attributes_set[response[i].attr_id] = response[i];
			}
			if (self.getMutationProfileId() !== null) {
			    sample_clinical_attributes_set['# mutations'] = {attr_id: "# mutations",
				datatype: "NUMBER",
				description: "Number of mutations",
				display_name: "Total mutations",
				is_patient_attribute: "0"
			    };
			}
			if (self.getCancerStudyIds().length > 0) {
			    sample_clinical_attributes_set["FRACTION_GENOME_ALTERED"] = {attr_id: "FRACTION_GENOME_ALTERED",
				datatype: "NUMBER",
				description: "Fraction Genome Altered",
				display_name: "Fraction Genome Altered",
				is_patient_attribute: "0"
			    };
			}
			fetch_promise.resolve(deepCopyObject(sample_clinical_attributes_set));
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getPatientClinicalAttributesSet': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getPatientIds().then(function (patient_ids) {
			window.cbioportal_client.getPatientClinicalAttributes({study_id: [self.getCancerStudyIds()[0]], patient_ids: patient_ids}).then(function (attrs) {
			    var patient_clinical_attributes_set = {};
			    for (var i = 0; i < attrs.length; i++) {
				patient_clinical_attributes_set[attrs[i].attr_id] = attrs[i];
			    }
			    fetch_promise.resolve(patient_clinical_attributes_set);
			}).fail(function () {
			    fetch_promise.reject();
			});
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'sortClinicalAttributesByDataType': function (attr_ids) {
	    var def = new $.Deferred();
	    $.when(this.getSampleClinicalAttributesSet(), this.getPatientClinicalAttributesSet()).then(function (sample_set, patient_set) {
		var sorted = {'sample': {}, 'patient': {}};
		for (var i = 0; i < attr_ids.length; i++) {
		    if (sample_set.hasOwnProperty(attr_ids[i])) {
			sorted.sample[attr_ids[i]] = sample_set[attr_ids[i]];
		    } else if (patient_set.hasOwnProperty(attr_ids[i])) {
			sorted.patient[attr_ids[i]] = patient_set[attr_ids[i]];
		    }
		}
		def.resolve(sorted);
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getClinicalAttributes': function () {
	    var def = new $.Deferred();
	    $.when(this.getSampleClinicalAttributes(), this.getPatientClinicalAttributes()).then(function (sample_attrs, patient_attrs) {
		def.resolve(sample_attrs.concat(patient_attrs));
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getPatientClinicalData': function (attribute_ids) {
	    // TODO: handle more than one study
	    return getClinicalData(this, attribute_ids, "patient");
	},
	'getCaseSetId': function () {
	    return case_set_properties.case_set_id;
	},
	'getCaseIdsKey': function () {
	    return case_set_properties.case_ids_key;
	},
	'getSampleSetName': function () {
	    return case_set_properties.case_set_name;
	},
	'getSampleSetDescription': function () {
	    return case_set_properties.case_set_description;
	},
	'getPatientSampleIdMap': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    var sample_to_patient = {};
		    window.cbioportal_client.getSamples({study_id: [self.getCancerStudyIds()[0]], sample_ids: self.getSampleIds()}).then(function (data) {
			for (var i = 0; i < data.length; i++) {
			    sample_to_patient[data[i].id] = data[i].patient_id;
			}
			fetch_promise.resolve(sample_to_patient);
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getCancerStudyNames': function () {
	    return cancer_study_names;
	},
	'getMutationProfileId': function () {
	    return profile_ids.mutation_profile_id;
	}

    };
};

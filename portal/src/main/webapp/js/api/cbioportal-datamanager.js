window.initDatamanager = function (genetic_profile_ids, oql_query, cancer_study_ids, study_sample_map, z_score_threshold, rppa_score_threshold,
	case_set_properties, customDriverAnnotationsByDefault, showDriverAnnotation, hasDriverAnnotations, numTiers, enableOncoKBandHotspots,
	showTierAnnotation, enableTiers, hidePassenger) {

    var signOfDiff = function(a,b) {
	if (a < b) {
	    return -1;
	} else if (a > b) {
	    return 1;
	} else {
	    return 0;
	}
    };
    
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
    var objectKeyIntersection = function (list_of_objs) {
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
		for (var j = 0; j < keys.length; j++) {
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
    var flatten = function (list_of_lists) {
	return list_of_lists.reduce(function (a, b) {
	    return a.concat(b);
	}, []);
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
    var getOncoprintMutationType = function (type) {
	// In: output of getSimplifiedMutationType
	// Out: Everything that's not missense, inframe, or fusion becomes trunc
	type = type.toLowerCase();
	return (["missense", "inframe", "fusion"].indexOf(type) > -1 ? type : "trunc");
    };
    var insertionIndex = function (sorted_list, target) {
	/* In: sorted_list, a sorted list of unique numbers
	 *     target, a number
	 * Out: the index of the smallest element >= target
	 */
	var lower_inc = 0;
	var upper_exc = sorted_list.length;
	while (lower_inc < upper_exc) {
	    var proposed = Math.floor((lower_inc + upper_exc) / 2);
	    if (sorted_list[proposed] === target) {
		return proposed;
	    } else if (sorted_list[proposed] < target) {
		lower_inc = proposed + 1;
	    } else if (sorted_list[proposed] > target) {
		upper_exc = proposed;
	    }
	}
	return upper_exc;
    };

    var getGeneticProfileDataForQueryCases = function (self, genetic_profile_id, genes) {
	var def= new $.Deferred();
	var case_set_id = self.getCaseSetId();
	var study_ids = self.getCancerStudyIds();
	if (study_ids.length === 1 && case_set_id && case_set_id !== "-1") {
	    def = window.cbioportal_client.getGeneticProfileDataBySample({
		'genetic_profile_ids': [genetic_profile_id],
		'genes': genes.map(function(x) {
		    return x.toUpperCase();
		}),
		'sample_list_id': [case_set_id]
	    });
	} else {
	    $.when(window.cbioportal_client.getGeneticProfiles({genetic_profile_ids:[genetic_profile_id]}), self.getStudySampleMap()).then(function(gp, study_sample_map) {
		var study = gp[0].study_id;
		var samples = study_sample_map[study];
		if (!samples) {
		    def.reject();
		} else {
		    window.cbioportal_client.getGeneticProfileDataBySample({
			'genetic_profile_ids': [genetic_profile_id],
			'genes': genes.map(function (x) {
			    return x.toUpperCase();
			}),
			'sample_ids': samples
		    }).then(function(data) {
			def.resolve(data);
		    }).fail(function() {
			def.reject();
		    });
		}
	    }).fail(function() {
		def.reject();
	    });
	}
	return def;
    };
    
    var getOncoKBAnnotations = function (webservice_data) {
	/* In: - webservice_data, a list of data obtained from the webservice API,
	 *	 modified to have the attribute oncokb_mutation_id
	 * Out: Promise which resolves with map from oncokbMutationId to one of ['unknown', 'likely neutral', 'likely oncogenic', 'predicted oncogenic', 'oncogenic']
	 */
	var def = new $.Deferred();
	var oncogenic = {}; // See Out above
	var cna_to_alteration = {
	    '-2': 'deletion',
	    '2': 'amplification'
	};
	// Collect queries
	var queries = {};
	for (var i = 0; i < webservice_data.length; i++) {
	    var datum = webservice_data[i];
	    if (datum.genetic_alteration_type === "MUTATION_EXTENDED" && 
		    (datum.oncoprint_mutation_type === "missense" || datum.oncoprint_mutation_type === "inframe"
		    || datum.oncoprint_mutation_type === "trunc")) {
		queries[datum.oncokb_query_id] = {
		    'hugoSymbol': datum.hugo_gene_symbol.toUpperCase(),
		    'alteration': datum.amino_acid_change,
		    'consequence': datum.mutation_type,
		    'proteinStart': datum.protein_start_position,
		    'proteinEnd': datum.protein_end_position,
		    'id': datum.oncokb_query_id
		};
	    } else if (datum.genetic_alteration_type === "COPY_NUMBER_ALTERATION") {
		var alteration = cna_to_alteration[datum.profile_data];
		if (alteration) {
		    queries[datum.oncokb_query_id] = {
			'hugoSymbol': datum.hugo_gene_symbol.toUpperCase(),
			'alteration': alteration, 
			'id': datum.oncokb_query_id
		    };
		}
	    }
	}
	queries = objectValues(queries);
	if (queries.length > 0) {
	    // Execute query
	    var query = {
		"geneStatus": "Complete",
		"source": "cbioportal",
		"evidenceTypes": "ONCOGENIC",
		"queries": queries,
		"levels": [
		    "LEVEL_1",
		    "LEVEL_2A",
		    "LEVEL_3A",
		    "LEVEL_R1"
		]
	    }
	    $.ajax({
		type: "POST",
		url: "api-legacy/proxy/oncokb",
		contentType: "application/json",
		data: JSON.stringify(query)
	    }).then(function (response) {
		response = JSON.parse(response);
		for (var i = 0; i < response.length; i++) {
		    oncogenic[response[i].query.id] = response[i].oncogenic.toLowerCase();
		}
		def.resolve(oncogenic);
	    }).fail(function () {
		def.reject();
	    });
	} else {
	    def.resolve({});
	}
	return def.promise();
    };
    var addOncoKBQueryId = function(webservice_data) {
	/* in-place, idempotent
	 * In: webservice_data, a list of data obtained from the webservice API
	 * Out: webservice_data, modified in-place, with the new added attribute
	 *	oncokb_query_id,
	 *	which uniquely identifies a mutation for oncokb querying
	 */
	
	for (var i=0; i<webservice_data.length; i++) {
	    var datum = webservice_data[i];
	    var genetic_alteration_type = datum.genetic_alteration_type;
	    if (genetic_alteration_type === "MUTATION_EXTENDED") {
		datum.oncokb_query_id = [datum.hugo_gene_symbol, datum.amino_acid_change, datum.mutation_type,
					datum.protein_start_position, datum.protein_end_position].join(",");
	    } else if (genetic_alteration_type === "COPY_NUMBER_ALTERATION") {
		datum.oncokb_query_id = [datum.hugo_gene_symbol, datum.profile_data].join(",");
	    }
	    
	}
	return webservice_data;
    };
    
    var annotateOncoKBMutationOncogenic = function (self, webservice_data) {
	/* in-place, idempotent
	 * In: - webservice_data, a list of data obtained from the webservice API,
	 * Out: promise, which resolves when it's done (data is modified in place, with
	 *	the mutation data given the string attribute 'oncokb_oncogenic', with value
	 *	in ['unknown', 'likely neutral', 'likely oncogenic', 'predicted oncogenic', 'oncogenic']
	 *	The data now also has the attribute 'oncokb_query_id'
	 */
	var def = new $.Deferred();
	var attribute_name = 'oncokb_oncogenic';
	getOncoKBAnnotations(addOncoKBQueryId(webservice_data)).then(function (oncogenic) {
	    for (var i = 0; i < webservice_data.length; i++) {
		var datum = webservice_data[i];
		var oncokb_query_id = datum.oncokb_query_id;
		var datum_oncogenic_value = oncokb_query_id && oncogenic[oncokb_query_id];
		if (datum_oncogenic_value) {
		    datum[attribute_name] = datum_oncogenic_value;
		}
	    }
	    self.external_data_status.oncokb = true;
	    def.resolve();
	}).fail(function() {
	    self.setKnownMutationSettings({ recognize_oncokb_oncogenic: false });
	    def.resolve();
	});
	return def.promise();
    };
    
    var annotateHotSpots = function (self, webservice_data) {
	/* in-place, idempotent
	 * In: - webservice_data, a list of data obtained from the webservice API
	 * Out: promise, which resolves when the data has been in-place modified,
	 *	    the mutation data given the boolean attribute 'cancer_hotspots_hotspot'
	 */
	var def = new $.Deferred();
	var genes = {};
	var annotatable_webservice_data = webservice_data.filter(function (d) {
	    var ret = d.genetic_alteration_type === "MUTATION_EXTENDED" && 
		    (d.oncoprint_mutation_type === "missense" || d.oncoprint_mutation_type === "inframe");
	    if (ret) {
		genes[d.hugo_gene_symbol] = true;
	    }
	    return ret;
	});
	genes = Object.keys(genes);
	
	if (annotatable_webservice_data.length > 0) {
	    var attribute_name = 'cancer_hotspots_hotspot';
	    var get_promise = $.ajax({
		type: 'GET',
		url: 'api-legacy/proxy/cancerHotSpots',
	    });
	    get_promise.then(function (response) {
		response = JSON.parse(response);
		// Gather hotspots into data structures for querying
		var gene_to_hotspots = {};// { [hugoSymbol:string]:{ missense: [number,number][], indel: [number,number][] } }
		for (var i=0; i<genes.length; i++) {
		    gene_to_hotspots[genes[i]] = { missense:[], indel:[]};
		}
		for (var i=0; i<response.length; i++) {
		    var gene = response[i].hugoSymbol;
		    if (gene_to_hotspots.hasOwnProperty(gene)) {
			var position = response[i].aminoAcidPosition;
			var range = [position.start, position.end];
			if (response[i].type === "single residue") {
			    gene_to_hotspots[gene].missense.push(range);
			} else if (response[i].type === "in-frame indel") {
			    gene_to_hotspots[gene].indel.push(range);
			}
		    }   
		}
		for (var i=0; i<genes.length; i++) {
		    var gene = genes[i];
		    gene_to_hotspots[gene] = { 
			missense: new HotspotSet(gene_to_hotspots[gene].missense),
			indel: new HotspotSet(gene_to_hotspots[gene].indel)
		    };
		}
		// Annotate
		for (var i = 0; i < annotatable_webservice_data.length; i++) {
		    var datum = annotatable_webservice_data[i];
		    var gene = datum.hugo_gene_symbol;
		    gene && (gene = gene.toUpperCase());
		    var start_pos = datum.protein_start_position;
		    var end_pos = datum.protein_end_position;
		    if (gene && !isNaN(start_pos) && !isNaN(end_pos)) {
			var hotspot_sets = gene_to_hotspots[gene];
			if ((datum.oncoprint_mutation_type === "missense" && hotspot_sets.missense.check(start_pos))
			    || (datum.oncoprint_mutation_type === "inframe" && hotspot_sets.indel.check(start_pos, end_pos))) {
			    datum[attribute_name] = true;
			}
		    }
		    datum[attribute_name] = !!datum[attribute_name]; // ensure all are labeled true or false
		}
		self.external_data_status.hotspots = true;
		def.resolve();
	    });
	    get_promise.fail(function () {
		self.setKnownMutationSettings({ recognize_hotspot: false });
		def.resolve();
	    });
	} else {
	    def.resolve();
	}
	return def.promise();
    };
    var makeOncoprintClinicalData = function (webservice_clinical_data, attr_id, study_id, source_sample_or_patient, target_sample_or_patient,
	    target_ids, sample_to_patient_map, case_uid_map, datatype, na_or_zero) {
	na_or_zero = na_or_zero || "na";

	// First collect all the data by id
	var id_to_data = {};
	var id_attribute = source_sample_or_patient + '_id'; // sample_id or patient_id
	for (var i = 0, _len = webservice_clinical_data.length; i < _len; i++) {
	    var d = webservice_clinical_data[i];
	    var id = d[id_attribute];
	    if (source_sample_or_patient === "sample" && target_sample_or_patient === "patient") {
		id = sample_to_patient_map[study_id][id];
	    }
	    if (!id_to_data[id]) {
		id_to_data[id] = [];
	    }
	    id_to_data[id].push(d);
	}
	// Then combine it
	var data = [];
	for (var i = 0; i < target_ids.length; i++) {
	    var datum_to_add = {'attr_id': attr_id, 'study_id': study_id, 'uid': case_uid_map[study_id][target_ids[i]], 'attr_val_counts': {}};
	    datum_to_add[target_sample_or_patient] = target_ids[i];
	    var data_to_combine;
	    if (source_sample_or_patient === "patient" && target_sample_or_patient === "sample") {
		data_to_combine = id_to_data[sample_to_patient_map[study_id][target_ids[i]]];
	    } else {
		data_to_combine = id_to_data[target_ids[i]];
	    }
	    data_to_combine = data_to_combine || [];
	    if (data_to_combine.length === 0) {
		if (na_or_zero === "na") {
		    datum_to_add.na = true;
		} else if (na_or_zero === "zero") {
		    datum_to_add.attr_val_counts[0] = 1;
		    datum_to_add.attr_val = 0;
		}
	    } else if (data_to_combine.length === 1) {
		if (datatype.toLowerCase() === "number") {
		    var attr_val = parseFloat(data_to_combine[0].attr_val);
		    if (!isNaN(attr_val)) {
			datum_to_add.attr_val = attr_val;
			datum_to_add.attr_val_counts[attr_val] = 1;
		    } else {
			datum_to_add.na = true;
		    }
		} else if (datatype.toLowerCase() === "string") {
		    datum_to_add.attr_val = data_to_combine[0].attr_val;
		    datum_to_add.attr_val_counts[datum_to_add.attr_val] = 1;
		} else if (datatype.toLowerCase() === "counts_map") {
		    datum_to_add.attr_val_counts = data_to_combine[0].attr_val;
		    for (var k in datum_to_add.attr_val_counts) {
			if (typeof datum_to_add.attr_val_counts[k] !== "undefined") {
			    var count = parseFloat(datum_to_add.attr_val_counts[k]);
			    if (!isNaN(count)) {
				datum_to_add.attr_val_counts[k] = count;
			    }
			}
		    }
		}
	    } else {
		if (datatype.toLowerCase() === "number") {
		    var avg = 0;
		    var total = 0;
		    for (var j = 0; j < data_to_combine.length; j++) {
			if (typeof data_to_combine[j].attr_val !== "undefined") {
			    var attr_val = parseFloat(data_to_combine[j].attr_val)
			    if (!isNaN(attr_val)) {
				avg += attr_val;
				total += 1;
			    }
			}
		    }
		    datum_to_add.attr_val = ((total > 0) ? (avg / total) : 0);
		} else if (datatype.toLowerCase() === "string") {
		    for (var j = 0; j < data_to_combine.length; j++) {
			if (typeof data_to_combine[j].attr_val !== "undefined") {
			    datum_to_add.attr_val_counts[data_to_combine[j].attr_val] = datum_to_add.attr_val_counts[data_to_combine[j].attr_val] || 0;
			    datum_to_add.attr_val_counts[data_to_combine[j].attr_val] += 1;
			}
		    }
		    var attr_vals = Object.keys(datum_to_add.attr_val_counts);
		    if (attr_vals.length > 1) {
			datum_to_add.attr_val = "Mixed";
		    } else if (attr_vals.length === 1) {
			datum_to_add.attr_val = attr_vals[0];
		    }
		} else if (datatype.toLowerCase() === "counts_map") {
		    for (var j = 0; j < data_to_combine.length; j++) {
			for (var k in data_to_combine[j].attr_val) {
			    if (typeof data_to_combine[j].attr_val[k] !== "undefined") {
				var count = parseFloat(data_to_combine[j].attr_val[k]);
				if (!isNaN(count)) {
				    datum_to_add.attr_val_counts[k] = datum_to_add.attr_val_counts[k] || 0;
				    datum_to_add.attr_val_counts[k] += count;
				}
			    }
			}
		    }
		}
	    }
	    if (datatype.toLowerCase() === "counts_map") {
		// if all 0, then change to 'na'
		var all_0 = true;
		for (var k in datum_to_add.attr_val_counts) {
		    if (typeof datum_to_add.attr_val_counts[k] !== "undefined" && !isNaN(datum_to_add.attr_val_counts[k])) {
			if (datum_to_add.attr_val_counts[k] !== 0) {
			    all_0 = false;
			    break;
			}
		    }
		}
		if (all_0) {
		    datum_to_add.na = true;
		}
		datum_to_add.attr_val = datum_to_add.attr_val_counts;
	    }
	    data.push(datum_to_add);
	}
	return data;
    };
    var makeOncoprintData = function (webservice_data, genes, study_to_id_map, sample_or_patient, sample_to_patient_map, case_uid_map, sequencing_data, use_putative_driver) {
	// To fill in for non-existent data, need genes and samples to do so for
	genes = genes || [];
	study_to_id_map = study_to_id_map || {}; // to make blank data
	// Gather data by id and gene
	var gene_id_study_to_datum = {};
	var studies = Object.keys(study_to_id_map);
	for (var i = 0; i < genes.length; i++) {
	    var gene = genes[i].toUpperCase();
	    for (var j = 0; j < studies.length; j++) {
		var study = studies[j];
		var ids = study_to_id_map[study];
		for (var h = 0; h < ids.length; h++) {
		    var id = ids[h];
		    var new_datum = {};
		    new_datum['gene'] = gene;
		    new_datum[sample_or_patient] = id;
		    new_datum['data'] = [];
		    new_datum['study_id'] = study;
		    new_datum['uid'] = case_uid_map[study][id];

		    if (typeof sequencing_data === "undefined") {
			new_datum['coverage'] = undefined;
		    } else if (typeof sequencing_data[study][id] === "undefined" ||
			typeof sequencing_data[study][id][gene] === "undefined") {
			new_datum['na'] = true;
		    } else {
			new_datum['coverage'] = Object.keys(sequencing_data[study][id][gene]);
		    }
		    gene_id_study_to_datum[gene+','+id+','+study] = new_datum;
		}
	    }
	}
	for (var i = 0; i < webservice_data.length; i++) {
	    var datum = webservice_data[i];
	    var gene = datum.hugo_gene_symbol.toUpperCase();
	    var study = datum.study_id;
	    var id = (sample_or_patient === "patient" ? sample_to_patient_map[study][datum.sample_id] : datum.sample_id);
	    var gene_id_datum = gene_id_study_to_datum[gene + "," + id + "," + study];
	    if (gene_id_datum) {
		gene_id_datum.data.push(datum);
	    }
	}

	// Compute display parameters
	var data = objectValues(gene_id_study_to_datum);
	var cna_profile_data_to_string = {
	    "-2": "homdel",
	    "-1": "hetloss",
	    "0": undefined,
	    "1": "gain",
	    "2": "amp"
	};
	var mut_rendering_priority = {'trunc_rec':1, 'inframe_rec':2, 'missense_rec':3, 'trunc': 4, 'inframe': 5, 'missense': 6};
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

	    var disp_fusion = false;
	    var disp_cna_counts = {};
	    var disp_mrna_counts = {};
	    var disp_prot_counts = {};
	    var disp_mut_counts = {};

	    for (var j = 0; j < datum_events.length; j++) {
		var event = datum_events[j];
		if (event.genetic_alteration_type === "COPY_NUMBER_ALTERATION") {
		    var cna_event = cna_profile_data_to_string[event.profile_data];
		    disp_cna_counts[cna_event] = disp_cna_counts[cna_event] || 0;
		    disp_cna_counts[cna_event] += 1;
		} else if (event.genetic_alteration_type === "MRNA_EXPRESSION") {
		    if (event.oql_regulation_direction) {
			var mrna_event = (event.oql_regulation_direction === 1 ? "up" : "down");
			disp_mrna_counts[mrna_event] = disp_mrna_counts[mrna_event] || 0;
			disp_mrna_counts[mrna_event] += 1;
		    }
		} else if (event.genetic_alteration_type === "PROTEIN_LEVEL") {
		    if (event.oql_regulation_direction) {
			var prot_event = (event.oql_regulation_direction === 1 ? "up" : "down");
			disp_prot_counts[prot_event] = disp_prot_counts[prot_event] || 0;
			disp_prot_counts[prot_event] += 1;
		    }
		} else if (event.genetic_alteration_type === "MUTATION_EXTENDED") {
		    var oncoprint_mutation_type = event.oncoprint_mutation_type;
		    if (use_putative_driver && event.putative_driver) {
			oncoprint_mutation_type += "_rec";
		    }
		    if (oncoprint_mutation_type === "fusion") {
			disp_fusion = true;
		    } else {
			disp_mut_counts[oncoprint_mutation_type] = disp_mut_counts[oncoprint_mutation_type] || 0;
			disp_mut_counts[oncoprint_mutation_type] += 1;
		    }
		}
	    }
	    if (disp_fusion) {
		datum.disp_fusion = true;
	    }
	    datum.disp_cna = selectDisplayValue(disp_cna_counts, cna_rendering_priority);
	    datum.disp_mrna = selectDisplayValue(disp_mrna_counts, mrna_rendering_priority);
	    datum.disp_prot = selectDisplayValue(disp_prot_counts, prot_rendering_priority);
	    datum.disp_mut = selectDisplayValue(disp_mut_counts, mut_rendering_priority);
	}
	return data;
    };
    var makeOncoprintSampleData = function (webservice_data, genes, study_sample_map, case_uid_map, sequenced_samples_by_gene, use_putative_driver) {
	return makeOncoprintData(webservice_data, genes, study_sample_map, "sample", null, case_uid_map, sequenced_samples_by_gene, use_putative_driver);
    };
    var makeOncoprintPatientData = function (webservice_data, genes, study_patient_map, sample_to_patient_map, case_uid_map, sequenced_patients_by_gene, use_putative_driver) {
	return makeOncoprintData(webservice_data, genes, study_patient_map, "patient", sample_to_patient_map, case_uid_map, sequenced_patients_by_gene, use_putative_driver);
    };

    var default_oql = '';
    var getClinicalData = function (self, attr_ids, target_sample_or_patient) {
	// Helper function for getSampleClinicalData and getPatientClinicalData
	var def = new $.Deferred();
	// special cases: '# mutations' and 'FRACTION_GENOME_ALTERED', both are sample attributes
	var fetch_promises = [];
	var clinical_data = [];
	var special_case_attr_ids = {
	    '# mutations': true, 
	    'FRACTION_GENOME_ALTERED': true, 
	    'NO_CONTEXT_MUTATION_SIGNATURE': true, 
	    'CANCER_STUDY': true
	};
	$.when(self.sortClinicalAttributesByDataType(attr_ids.filter(function(x) { return !special_case_attr_ids[x]; })), self.getGeneticProfiles(), self.getStudySampleMap(), self.getStudyPatientMap(), self.getPatientSampleIdMap(), self.getCaseUIDMap(), self.getStudyNameMap()).then(function (sorted_attrs, genetic_profiles, study_sample_map, study_patient_map, sample_to_patient_map, case_uid_map, studyNameMap) {
	    var study_target_ids_map = (target_sample_or_patient === "sample" ? study_sample_map : study_patient_map);
	    if (attr_ids.indexOf('# mutations') > -1) {
		var clinicalMutationColl = new ClinicalMutationColl();
		fetch_promises = fetch_promises.concat(
			genetic_profiles.filter(function (gp) {
			    return gp.genetic_alteration_type === "MUTATION_EXTENDED";
			})
			.map(function (mutation_gp) {
			    var _def = new $.Deferred();
			    clinicalMutationColl.fetch({
				type: "POST",
				data: {
				    mutation_profile: mutation_gp.id,
				    cmd: "count_mutations",
				    case_ids: study_sample_map[mutation_gp.study_id].join(" ")
				},
				success: function (response) {
				    response = response.toJSON();
				    for (var i = 0; i < response.length; i++) {
					response[i].sample_id = response[i].sample; // standardize
					response[i].study_id = mutation_gp.study_id;
				    }
				    clinical_data = clinical_data.concat(makeOncoprintClinicalData(response, "# mutations", mutation_gp.study_id, "sample", target_sample_or_patient, study_target_ids_map[mutation_gp.study_id],
					    sample_to_patient_map, case_uid_map, "number", "zero"));
				    _def.resolve();
				},
				error: function () {
				    def.reject();
				}
			    });
			    return _def.promise();
			}));
	    }
	    if (attr_ids.indexOf('FRACTION_GENOME_ALTERED') > -1) {
		var clinicalCNAColl = new ClinicalCNAColl();
		fetch_promises = fetch_promises.concat(
			self.getCancerStudyIds().map(function (cancer_study_id) {
		    var _def = new $.Deferred();
		    clinicalCNAColl.fetch({
			type: "POST",
			data: {
			    cancer_study_id: cancer_study_id,
			    cmd: "get_cna_fraction",
			    case_ids: study_sample_map[cancer_study_id].join(" ")
			},
			success: function (response) {
			    response = response.toJSON();
			    for (var i = 0; i < response.length; i++) {
				response[i].sample_id = response[i].sample; // standardize
				response[i].study_id = cancer_study_id;
			    }
			    clinical_data = clinical_data.concat(makeOncoprintClinicalData(response, "FRACTION_GENOME_ALTERED", cancer_study_id, "sample", target_sample_or_patient, study_target_ids_map[cancer_study_id],
				    sample_to_patient_map, case_uid_map, "number", "na"));
			    _def.resolve();
			},
			error: function () {
			    def.reject();
			}
		    });
		    return _def.promise();
		}));
	    }
	    if (attr_ids.indexOf('NO_CONTEXT_MUTATION_SIGNATURE') > -1) {
		fetch_promises = fetch_promises.concat(self.getCancerStudyIds().map(function(study_id) {
		    var _def = new $.Deferred();
		    $.ajax({
			type: 'POST',
			url: 'api-legacy/mutational-signature',
			data: ['study_id=', study_id, '&', 'sample_ids=', study_sample_map[study_id].join(",")].join(""),
			dataType: 'json'
		    }).then(function (response) {
			for (var i = 0; i < response.length; i++) {
			    // standardize
			    var types = response[i].mutationTypes;
			    response[i].sample_id = response[i].sample;
			    response[i].study_id = study_id;
			    response[i].attr_val = response[i].counts.reduce(function (map, count, index) {
				map[types[index]] = count;
				return map;
			    }, {});
			}
			clinical_data = clinical_data.concat(makeOncoprintClinicalData(response, "NO_CONTEXT_MUTATION_SIGNATURE", study_id, "sample", target_sample_or_patient, study_target_ids_map[study_id], sample_to_patient_map, case_uid_map, "counts_map", "na"));
			_def.resolve();
		    }).fail(function () {
			_def.reject();
		    });
		    return _def.promise();
		}));
	    }
	    if (attr_ids.indexOf('CANCER_STUDY') > -1) {
		var studySampleMap = self.getStudySampleMap();
		var studies = Object.keys(studySampleMap);
		for (var i=0; i<studies.length; i++) {
		    var study_id = studies[i];
		    var samples = studySampleMap[study_id];
		    var response = samples.map(function(sample_id) {
			return {
			    'sample_id': sample_id,
			    'study_id': study_id,
			    'attr_val': studyNameMap[study_id]
			};
		    });
		    clinical_data = clinical_data.concat(makeOncoprintClinicalData(response, 'CANCER_STUDY', study_id, "sample", target_sample_or_patient, study_target_ids_map[study_id], sample_to_patient_map, case_uid_map, "string", "na"));
		}
	    }

	    fetch_promises = fetch_promises.concat(self.getCancerStudyIds().map(function (cancer_study_id) {
		var _def = new $.Deferred();
		var attribute_ids = Object.keys(sorted_attrs.sample);
		window.cbioportal_client.getSampleClinicalData({study_id: [cancer_study_id], attribute_ids: attribute_ids, sample_ids: study_sample_map[cancer_study_id]})
			.then(function (data) {
			    var sample_data_by_attr_id = {};
			    for (var i = 0; i<attribute_ids.length; i++) {
				sample_data_by_attr_id[attribute_ids[i]] = [];
			    }
			    for (var i = 0; i < data.length; i++) {
				var attr_id = data[i].attr_id;
				sample_data_by_attr_id[attr_id].push(data[i]);
			    }
			    for (var i = 0; i < attribute_ids.length; i++) {
				var attr_id = attribute_ids[i];
				clinical_data = clinical_data.concat(makeOncoprintClinicalData(sample_data_by_attr_id[attr_id] || [], attr_id, cancer_study_id,
					"sample", target_sample_or_patient, study_target_ids_map[cancer_study_id], sample_to_patient_map, case_uid_map, sorted_attrs.sample[attr_id].datatype.toLowerCase(), "na"));
			    }
			    _def.resolve();
			}).fail(function () {
		    def.reject();
		});
		return _def.promise();
	    })).concat(self.getCancerStudyIds().map(function (cancer_study_id) {
		var _def = new $.Deferred();
		var attribute_ids = Object.keys(sorted_attrs.patient);
		window.cbioportal_client.getPatientClinicalData({study_id: [cancer_study_id], attribute_ids: attribute_ids, patient_ids: study_patient_map[cancer_study_id]})
			.then(function (data) {
			    var patient_data_by_attr_id = {};
			    for (var i = 0; i<attribute_ids.length; i++) {
				patient_data_by_attr_id[attribute_ids[i]] = [];
			    }
			    for (var i = 0; i < data.length; i++) {
				var attr_id = data[i].attr_id;
				patient_data_by_attr_id[attr_id].push(data[i]);
			    }
			    for (var i = 0; i < attribute_ids.length; i++) {
				var attr_id = attribute_ids[i];
				clinical_data = clinical_data.concat(makeOncoprintClinicalData(patient_data_by_attr_id[attr_id] || [], attr_id, cancer_study_id,
					"patient", target_sample_or_patient, study_target_ids_map[cancer_study_id], sample_to_patient_map, case_uid_map, sorted_attrs.patient[attr_id].datatype.toLowerCase(), "na"));
			    }
			    _def.resolve();
			}).fail(function () {
		    def.reject();
		});
		return _def.promise();
	    }));

	    $.when.apply($, fetch_promises).then(function () {
		def.resolve(clinical_data);
	    });
	});
	return def.promise();
    };

    var makeCachedPromiseFunction = function (fetcher) {
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
		def.resolve(data);
	    });
	    return def.promise();
	};
    };
    
    var makeCachedPromiseFunctionWithSessionFilterOption = function(fetcher) {
	// In: fetcher, take (self, promise, use_session_filters) as arguments
	// Out: A function which takes a single boolean argument - if true, it gives back
	//	the result of session_filter_fetcher, otherwise it gives back the result of fetcher.
	//	The result of fetcher is cached permanently after the first time it's called.
	//	The result of session_filter_fetcher is cached but re-computed after every time
	//	session filters are updated.
	
	var fetch_promise;
	var session_filter_fetch_promise;
	session_filter_change_callbacks.push(function() {
	    session_filter_fetch_promise = undefined;
	});
	return function(use_session_filters) {
	    var def = new $.Deferred();
	    if (!use_session_filters) {
		if (typeof fetch_promise === "undefined") {
		    fetch_promise = new $.Deferred();
		    fetcher(this, fetch_promise, false);
		}
		fetch_promise.then(function(data) {
		    def.resolve(data);
		});
	    } else {
		if (typeof session_filter_fetch_promise === "undefined") {
		    session_filter_fetch_promise = new $.Deferred();
		    fetcher(this, session_filter_fetch_promise, true);
		}
		session_filter_fetch_promise.then(function(data) {
		    def.resolve(data);
		});
	    }
	    return def.promise();
	};
    };
    
    var getTiersMap = function () {
	var driver_tiers_filter_values = {};
	var checkboxes = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"]');
	for (var i=0; i < checkboxes.length; i++) {
		if (checkboxes[i].name.lastIndexOf("driver_tiers_filter_") != -1) {
		    driver_tiers_filter_values[checkboxes[i].name] = checkboxes[i].value;
		}
	}
	return driver_tiers_filter_values;
    }
    
    var applyKnownMutationSettings = function (ws_data, known_mutation_settings, getters) {
	var filtered_mutation_types = ["missense", "inframe", "trunc"];
	var oncogenic = ["likely oncogenic", "predicted oncogenic", "oncogenic"];
	return ws_data.filter(function (d) {
	    if (d.genetic_alteration_type !== "MUTATION_EXTENDED"
		    || (filtered_mutation_types.indexOf(d.oncoprint_mutation_type) === -1)) {
		return true;
	    } else {
		var ret = (known_mutation_settings.recognize_hotspot && d.cancer_hotspots_hotspot)
			|| (known_mutation_settings.recognize_oncokb_oncogenic && (typeof d.oncokb_oncogenic !== "undefined") && (oncogenic.indexOf(d.oncokb_oncogenic) > -1));
		
		if (known_mutation_settings.recognize_cbioportal_count) {
		    var cbioportal_mutation_count = getters.cbioportalCount(d);
		    d.cbioportal_mutation_count = cbioportal_mutation_count;
		    ret = ret || ((typeof cbioportal_mutation_count !== "undefined") && (cbioportal_mutation_count >= known_mutation_settings.cbioportal_count_thresh));
		}
		if (known_mutation_settings.recognize_cosmic_count) {
		    var cosmic_count = getters.cosmicCount(d);
		    d.cosmic_count = cosmic_count;
		    ret = ret || ((typeof cosmic_count !== "undefined") && (cosmic_count >= known_mutation_settings.cosmic_count_thresh));
		}
		if (known_mutation_settings.recognize_driver_filter) {
		    if (d.driver_filter == "Putative_Driver") {
			ret = true; //Set the element to be a driver
		    }
		}
		var collectTiersMap = getTiersMap();
		for (var tier in collectTiersMap) {
		    if (collectTiersMap.hasOwnProperty(tier) &&
			    known_mutation_settings.recognize_driver_tiers[tier] &&
			    collectTiersMap[tier] == d.driver_tiers_filter) {
			ret = true;
			}
		    }
		
		if (ret) {
		    d.putative_driver = true;
		} else {
		    d.putative_driver = false;
		}
		return ret || !known_mutation_settings.ignore_unknown;
	    }
	});
    };

    var session_filter_change_callbacks = [];
    var triggerSessionFilterChangeCallbacks = function () {
	for (var i = 0; i < session_filter_change_callbacks.length; i++) {
	    session_filter_change_callbacks[i]();
	}
    };
   
    var getHeatmapData = function (self, genetic_profile_id, genes, sample_or_patient) {
	var def = new $.Deferred();
	var deferred_cases = sample_or_patient === "sample" ? self.getSamples() : self.getPatients();
	genes = genes || [];
	sample_or_patient = sample_or_patient || "sample";
	$.when(getGeneticProfileDataForQueryCases(self, genetic_profile_id, genes.map(function(x) { return x.toUpperCase(); }).filter(function(x) { return x.length > 0;})),
	    self.getPatientSampleIdMap(),
	    deferred_cases,
	    window.cbioportal_client.getGeneticProfiles({genetic_profile_ids:[genetic_profile_id]})
	).then(function (client_sample_data,
		sample_to_patient_map,
		cases,
		genetic_profile) {
	    // create an object for each sample or patient in each gene
	    var interim_data = {};
	    for (var i = 0; i < genes.length; i++) {
		var gene = genes[i].toUpperCase();
		interim_data[gene] = {};
		for (var j = 0; j < cases.length; j++) {
		    var case_id = cases[j].id;
		    interim_data[gene][case_id] = {};
		    interim_data[gene][case_id].hugo_gene_symbol = gene;
		    interim_data[gene][case_id].study = study_id;
		    interim_data[gene][case_id][sample_or_patient] = case_id;
		    // index the UID map by sample or patient as appropriate
		    interim_data[gene][case_id].uid = cases[j].uid;
		    interim_data[gene][case_id].profile_data = null;
		    interim_data[gene][case_id].na = true;
		}
	    }
	    // fill profile_data properties with scores
	    var study_id = genetic_profile[0].study_id;
	    for (var i = 0; i < client_sample_data.length; i++) {
		var receive_datum = client_sample_data[i];
		var gene = receive_datum.hugo_gene_symbol.toUpperCase();
		var sample_id = receive_datum.sample_id;
		var case_id = (sample_or_patient === "sample" ? sample_id : sample_to_patient_map[study_id][sample_id]);
		var interim_datum = interim_data[gene][case_id];
		if (!interim_datum) {
			// if this case wasnt requested, dont create data for it
			continue;
		} 
		interim_datum.na = false;
		if (interim_datum.profile_data === null) {
		    // set the initial value for this sample or patient
		    interim_datum.profile_data = parseFloat(receive_datum.profile_data);
		} else if (sample_or_patient === "sample") {
		    // this would be a programming error (unexpected output from getGeneticProfileDataBySample)
		    throw Error("Unexpectedly received multiple heatmap profile data for one sample");
		} else {
		    // aggregate samples for this patient by selecting the highest absolute (Z-)score
		    if (Math.abs(parseFloat(receive_datum.profile_data)) >
			    Math.abs(interim_datum.profile_data)) {
			interim_datum.profile_data = parseFloat(receive_datum.profile_data);
		    }
		}
	    }
	    // construct the list to be returned
	    var ret = [];
	    for (var i = 0; i < genes.length; i++) {
		var track_data = {
		    genetic_profile_id: genetic_profile[0].id,
		    datatype: genetic_profile[0].datatype,
		    genetic_alteration_type: genetic_profile[0].genetic_alteration_type
		};
		var gene = genes[i].toUpperCase();
		track_data.gene = gene;
		var oncoprint_data = [];
		for (var j = 0; j < cases.length; j++) {
		    var case_id = cases[j].id;
		    var datum = interim_data[gene][case_id];
		    if (datum.profile_data === null) {
			datum.na = true;
		    }
		    oncoprint_data.push(datum);
		}
		track_data.oncoprint_data = oncoprint_data;
		ret.push(track_data);
	    }
	    def.resolve(ret);
	}).fail(function () {
	    def.reject();
	});
	return def.promise();
    };
    
    var getHeatmapDataCached = (function() {
	var sample_cache = {};
	var patient_cache = {};
	var fetchData = function (self, genetic_profile_id, genes) {
	    var sample_def = new $.Deferred();
	    var patient_def = new $.Deferred();
	    getHeatmapData(self, genetic_profile_id, genes, 'sample').then(function (data) {
		for (var i = 0; i < data.length; i++) {
		    var gp_id = data[i].genetic_profile_id;
		    var gene = data[i].gene;
		    sample_cache[gp_id] = sample_cache[gp_id] || {};
		    sample_cache[gp_id][gene] = data[i];
		}
		sample_def.resolve();
	    }).fail(function () {
		sample_def.resolve();
	    });
	    getHeatmapData(self, genetic_profile_id, genes, 'patient').then(function (data) {
		for (var i = 0; i < data.length; i++) {
		    var gp_id = data[i].genetic_profile_id;
		    var gene = data[i].gene;
		    patient_cache[gp_id] = patient_cache[gp_id] || {};
		    patient_cache[gp_id][gene] = data[i];
		}
		patient_def.resolve();
	    }).fail(function () {
		patient_def.resolve();
	    });
	    return $.when(sample_def, patient_def);
	};
	var getDataFromCache = function(cache, genetic_profile_id, genes) {
	    return genes.map(function(gene) {
		return cache[genetic_profile_id] && cache[genetic_profile_id][gene];
	    }).filter(function(data) {
		return !!data;
	    });
	};
	return function(self, genetic_profile_id, genes, sample_or_patient) {
	    var def = new $.Deferred();
	    var cache = (sample_or_patient === 'sample' ? sample_cache : patient_cache);
	    var genes_to_query = genes.filter(function(gene) {
		return (!cache[genetic_profile_id]) || (!cache[genetic_profile_id][gene]);
	    });
	    
	    if (genes_to_query.length === 0) {
		def.resolve(getDataFromCache(cache, genetic_profile_id, genes));
	    } else {
		fetchData(self, genetic_profile_id, genes_to_query).then(function() {
		    def.resolve(getDataFromCache(cache, genetic_profile_id, genes));
		});
	    }
	    return def.promise();
	};
    })();

    function setKnownMutations (enableOncoKBandHotspots, showDriverAnnotation, hasDriverAnnotations,
	                        showTierAnnotation, numTiers, enableTiers, hidePassenger){
	    var oncoKBandHotspots = true;
            if (enableOncoKBandHotspots == "custom") {
                if (hasDriverAnnotations || numTiers > 0) {
                    oncoKBandHotspots = false;
                } else {
                    oncoKBandHotspots = true;
                }
            } else if (enableOncoKBandHotspots == "false") {
                oncoKBandHotspots = false;
            }
            var driverFilter = false;
            if (showDriverAnnotation && hasDriverAnnotations) {
                driverFilter = customDriverAnnotationsByDefault;
            }
            
            var driverTiers = {};
            for (tier=0; tier < numTiers; tier++) {
                driverTiers["driver_tiers_filter_"+tier] = false;
            }
            if (showTierAnnotation && numTiers > 0) {
                for (tier=0; tier < numTiers; tier++) {
                    driverTiers["driver_tiers_filter_"+tier] = enableTiers;
                }
            }
            
            return {
                'ignore_unknown': hidePassenger,
                'recognize_cbioportal_count': false,
                'cbioportal_count_thresh': 10,
                'recognize_cosmic_count': false,
                'cosmic_count_thresh': 10,
                'recognize_hotspot': oncoKBandHotspots,
                'recognize_oncokb_oncogenic': oncoKBandHotspots,
                'recognize_driver_filter': driverFilter,
                'recognize_driver_tiers': driverTiers
            }
    }
    
    return {
	'known_mutation_settings': setKnownMutations (enableOncoKBandHotspots, showDriverAnnotation, hasDriverAnnotations,
                showTierAnnotation, numTiers, enableTiers, hidePassenger),
	'external_data_status': {
	    'oncokb':false,
	    'hotspots':false,
	},
	'oql_query': oql_query,
	'cancer_study_ids': cancer_study_ids,
	'study_sample_map': study_sample_map,
	'genetic_profile_ids': genetic_profile_ids,
	'mutation_counts': {},
	'getKnownMutationSettings': function () {
	    return deepCopyObject(this.known_mutation_settings);
	},
	'setKnownMutationSettings': function (new_settings_obj) {
	    var new_settings = Object.keys(new_settings_obj);
	    for (var i = 0; i < new_settings.length; i++) {
		this.known_mutation_settings[new_settings[i]] = new_settings_obj[new_settings[i]];
	    }
	    triggerSessionFilterChangeCallbacks();
	},
	'onSessionFilterChange': function (callback) {
	    session_filter_change_callbacks.push(callback);
	},
	'getOQLQuery': function () {
	    return this.oql_query;
	},
	'getQueryGenes': function () {
	    return window.frontendVars.oqlGenes(this.oql_query);
	},
	'getGeneticProfileIds': function () {
	    return this.genetic_profile_ids;
	},
	'getHeatmapProfiles': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    var all_profiles = [];
		    $.when.apply($, self.getCancerStudyIds().map(function(study_id) {
			var def = new $.Deferred();
			window.cbioportal_client.getGeneticProfiles({'study_id':[study_id]})
			    .then(function (profiles) {
				all_profiles = all_profiles.concat(profiles);
				def.resolve();
			}).fail(function() {
			    def.reject();
			});
			return def.promise();
		    })).then(function() {
			fetch_promise.resolve(all_profiles.filter(function(profile) {
				    return (profile.genetic_alteration_type === "MRNA_EXPRESSION" ||
					    profile.genetic_alteration_type === "PROTEIN_LEVEL") &&
					    profile.show_profile_in_analysis_tab === "1";
				}));
		    }).fail(function() {
			fetch_promise.reject();
		    });
	    }),
	'getSampleUIDs': function (opt_study_id) {
	    var def = new $.Deferred();
	    var study_sample_map = this.getStudySampleMap();
	    $.when(this.getCaseUIDMap()).then(function (case_uid_map) {
		if (typeof opt_study_id !== "undefined") {
		    def.resolve(study_sample_map[opt_study_id].map(function(sample_id) {
			return case_uid_map[opt_study_id][sample_id];
		    }) || []);
		} else {
		    def.resolve(flatten(Object.keys(study_sample_map).map(function(study) {
			return study_sample_map[study].map(function(sample_id) {
			    return case_uid_map[study][sample_id];
			});
		    })));
		}
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getSampleIds': function (opt_study_id) {
	    if (typeof opt_study_id !== "undefined") {
		return this.study_sample_map[opt_study_id].slice() || [];
	    } else {
		return stringListUnique(flatten(objectValues(this.study_sample_map)));
	    }
	},
	'getSamples': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    $.when(self.getStudySampleMap(), self.getCaseUIDMap()).then(function(study_sample_map, case_uid_map) {
			var studies = Object.keys(study_sample_map);
			var ret = [];
			for (var i=0; i<studies.length; i++) {
			    var study = studies[i];
			    ret = ret.concat(study_sample_map[study].map(function(sample_id) { 
				return {
				    'study': study,
				    'type':'sample',
				    'id': sample_id,
				    'uid': case_uid_map[study][sample_id]
				}
			    }));
			}
			fetch_promise.resolve(ret);
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getPatients': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    $.when(self.getStudyPatientMap(), self.getCaseUIDMap()).then(function(study_patient_map, case_uid_map) {
			var studies = Object.keys(study_patient_map);
			var ret = [];
			for (var i=0; i<studies.length; i++) {
			    var study = studies[i];
			    ret = ret.concat(study_patient_map[study].map(function(patient_id) { 
				return {
				    'study': study,
				    'type': 'patient',
				    'id': patient_id,
				    'uid': case_uid_map[study][patient_id]
				}
			    }));
			}
			fetch_promise.resolve(ret);
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'computeUIDMaps': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    self.getStudyPatientMap().then(function (study_patient_map) {
		var study_sample_map = self.getStudySampleMap();
		var counter = 0;

		var case_to_uid = {};
		var uid_to_case = {};
		var ret = {
		    'case_to_uid': case_to_uid,
		    'uid_to_case': uid_to_case
		};
		for (var study in study_sample_map) {
		    if (typeof study_sample_map[study] !== "undefined") {
			case_to_uid[study] = case_to_uid[study] || {};
			var study_ids = case_to_uid[study];
			var samples = study_sample_map[study];
			for (var i = 0; i < samples.length; i++) {
			    var new_uid = counter + "";
			    study_ids[samples[i]] = new_uid;
			    uid_to_case[new_uid] = samples[i];
			    counter += 1;
			}
		    }
		}
		for (var study in study_patient_map) {
		    if (typeof study_patient_map[study] !== "undefined") {
			case_to_uid[study] = case_to_uid[study] || {};
			var study_ids = case_to_uid[study];
			var patients = study_patient_map[study];
			for (var i = 0; i < patients.length; i++) {
			    var new_uid = counter + "";
			    study_ids[patients[i]] = new_uid;
			    uid_to_case[new_uid] = patients[i];
			    counter += 1;
			}
		    }
		}
		fetch_promise.resolve(ret);
	    }).fail(function () {
		fetch_promise.reject();
	    });
		}),
	'getCaseUIDMap': function () {
	    var def = new $.Deferred();
	    this.computeUIDMaps().then(function(uid_maps) {
		def.resolve(uid_maps.case_to_uid);
	    }).fail(function() {
		def.reject();
	    });
	    return def.promise();
	},
	'getUIDToCaseMap': function() {
	    var def = new $.Deferred();
	    this.computeUIDMaps().then(function(uid_maps) {
		def.resolve(uid_maps.uid_to_case);
	    }).fail(function() {
		def.reject();
	    });
	    return def.promise();
	},
	'getSequencedSamples': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    self.getSampleSequencingData().then(function(sample_sequencing_data) {
			var study_sample_map = self.getStudySampleMap();
			var ret = [];
			var studies = Object.keys(study_sample_map);
			for (var i=0; i<studies.length; i++) {
			    var study = studies[i];
			    var samples = study_sample_map[study];
			    for (var j=0; j<samples.length; j++) {
				if (Object.keys(sample_sequencing_data[study][samples[j]]).length > 0) { // at least one gene sequenced
				    ret.push({'study': study, 'sample': samples[j]});
				}
			    }
			}
			fetch_promise.resolve(ret);
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getSequencedPatients': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    $.when(self.getSequencedSamples(), self.getPatientSampleIdMap()).then(function(sequenced_samples, sample_to_patient) {
			var sequenced_patients = {};
			// a patient is sequenced if at least one of its samples is sequenced
			for (var i=0; i<sequenced_samples.length; i++) {
			    var sample = sequenced_samples[i];
			    var patient = sample_to_patient[sample.study][sample.sample];
			    if (patient) {
				sequenced_patients[sample.study] = sequenced_patients[sample.study] || {};
				sequenced_patients[sample.study][patient] = true;
			    }
			}
			fetch_promise.resolve(objectKeyValuePairs(sequenced_patients).reduce(
				function(acc, next) {
				    return acc.concat(Object.keys(next[1]).map(function(patient_id) { return {'study': next[0], 'patient': patient_id}; }));
				}, []
			));
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getSampleSequencingData': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    // returns study -> sample -> gene -> set of (gene panel id | the number 1, indicating whole exome sequenced)
		    self.getGenePanelProfiles().then(function(profiles) {
			var sequencing_data = {};
			$.when.apply($, profiles.map(function(profile) {
			    var def = new $.Deferred();
			    $.ajax({
				type: "GET",
				url: "api-legacy/genepanel/data",
				contentType: "application/json",
				data: ["profile_id="+profile.id, "genes="+self.getQueryGenes().join(",")].join("&")
			    }).then(function(response) {
				for (var i = 0; i < response.length; i++) {
				    var panel = response[i];
				    var genes = panel.genes.map(function (g) {
					return g.hugoGeneSymbol;
				    });
				    var gene_panel_id = panel.stableId;
				    var samples = panel.samples;
				    sequencing_data[profile.study_id] = sequencing_data[profile.study_id] || {};
				    for (var h = 0; h < samples.length; h++) {
					sequencing_data[profile.study_id][samples[h]] = sequencing_data[profile.study_id][samples[h]] || {};
					var gene_info = sequencing_data[profile.study_id][samples[h]];
					for (var j = 0; j < genes.length; j++) {
					    var gene = genes[j];
					    gene_info[gene] = gene_info[gene] || {};
					    gene_info[gene][gene_panel_id] = true;
					}
				    }
				}
				def.resolve();
			    }).fail(function() {
				def.reject();
			    });
			    return def.promise();
			})).then(function() {
			    var study_sample_map = self.getStudySampleMap();
			    var whole_exome_sequenced_map = self.getQueryGenes().reduce(function(map, gene) { map[gene] = {'1': true}; return map; }, {});
			    var studies = self.getCancerStudyIds();
			    for (var i=0; i<studies.length; i++) {
				sequencing_data[studies[i]] = sequencing_data[studies[i]] || {};
				var samples = study_sample_map[studies[i]];
				for (var j=0; j<samples.length; j++) {
				    // If no gene panel data recorded for sample up to this point, then
				    //  by our convention, that means it's whole exome sequenced.
				    sequencing_data[studies[i]][samples[j]] = sequencing_data[studies[i]][samples[j]] || whole_exome_sequenced_map;
				}
			    }
			    fetch_promise.resolve(sequencing_data);
			}).fail(function() {
			    fetch_promise.reject();
			});
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getPatientSequencingData': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    $.when(self.getPatientSampleIdMap(), self.getSampleSequencingData(), self.getStudySampleMap()).then(function(sample_to_patient, sample_sequencing_data) {
			var sequenced_info = {};
			var studies = Object.keys(sample_sequencing_data);
			for (var i=0; i<studies.length; i++) {
			    var study = studies[i];
			    var samples = Object.keys(sample_sequencing_data[study]);
			    sequenced_info[study] = sequenced_info[study] || {};
			    for (var j=0; j<samples.length; j++) {
				var sample = samples[j];
				var patient = sample_to_patient[study][sample];
				if (patient) {
				    sequenced_info[study][patient] = sequenced_info[study][patient] || {};
				    var sequenced_genes = Object.keys(sample_sequencing_data[study][sample]);
				    for (var h = 0; h < sequenced_genes.length; h++) {
					var gene = sequenced_genes[h];
					sequenced_info[study][patient][gene] = objectKeyUnion([(sequenced_info[study][patient][gene] || {}), sample_sequencing_data[study][sample][gene]]);
				    }
				}
			    }
			}
			fetch_promise.resolve(sequenced_info);
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getStudySampleListMap': function() {
	    // TODO: will this ever be used?
	    var ret = {};
	    ret[this.getCancerStudyIds()[0]] = this.getCaseSetId();
	    return ret;
	},
	'getStudySampleMap': function() {
	    return deepCopyObject(this.study_sample_map);
	},
	'getStudyPatientMap': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    var study_patient_map = {};
		    var cancer_study_ids = self.getCancerStudyIds();
		    var study_done_promises = cancer_study_ids.map(function () {
			return new $.Deferred();
		    });
		    for (var i = 0; i < cancer_study_ids.length; i++) {
			(function (I) {
			    var cancer_study_id = cancer_study_ids[I];
			    window.cbioportal_client.getSamples({study_id: [cancer_study_id], sample_ids: self.study_sample_map[cancer_study_id]}).then(function (samples) {
				study_patient_map[cancer_study_id] = [];
				for (var j = 0; j < samples.length; j++) {
				    study_patient_map[cancer_study_id].push(samples[j].patient_id);
				}
				study_patient_map[cancer_study_id] = stringListUnique(study_patient_map[cancer_study_id]);
				study_done_promises[I].resolve();
			    }).fail(function () {
				fetch_promise.reject();
			    });
			})(i);
		    }
		    $.when.apply($, study_done_promises).then(function () {
			fetch_promise.resolve(study_patient_map);
		    });
		}),
	'getPatientUIDs': function (opt_study_id) {
	    var def = new $.Deferred();
	    $.when(this.getCaseUIDMap(), this.getStudyPatientMap()).then(function (case_uid_map, study_patient_map) {
		if (typeof opt_study_id !== "undefined") {
		    def.resolve(study_patient_map[opt_study_id].map(function(patient_id) {
			return case_uid_map[opt_study_id][patient_id];
		    }) || []);
		} else {
		    def.resolve(flatten(Object.keys(study_patient_map).map(function(study) {
			return study_patient_map[study].map(function(patient_id) {
			    return case_uid_map[study][patient_id];
			});
		    })));
		}
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getPatientIds': function (opt_study_id) {
	    var def = new $.Deferred();
	    this.getStudyPatientMap().then(function (study_patient_map) {
		if (typeof opt_study_id !== "undefined") {
		    def.resolve(study_patient_map[opt_study_id] || []);
		} else {
		    def.resolve(stringListUnique(flatten(objectValues(study_patient_map))));
		}
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getCancerStudyIds': function () {
	    return this.cancer_study_ids;
	},
	'getSampleSelect': function () {
	    return this.sample_select;
	},
	'getAlteredGenes': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
		    // A gene is "altered" if, after OQL filtering, there is a datum for it
		    (use_session_filters ? self.getSessionFilteredWebServiceGenomicEventData() : self.getWebServiceGenomicEventData()).then(function (data) {
			var altered_genes = {};
			for (var i = 0; i < data.length; i++) {
			    altered_genes[data[i].hugo_gene_symbol] = true;
			}
			fetch_promise.resolve(Object.keys(altered_genes));
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getAlteredGenesSetBySample': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    (use_session_filters ? self.getSessionFilteredWebServiceGenomicEventData() : self.getWebServiceGenomicEventData()).then(function (data) {
		var ret = {};
		for (var i = 0; i < data.length; i++) {
		    var sample = data[i].sample_id;
		    var gene = data[i].hugo_gene_symbol;
		    ret[sample] = ret[sample] || {};
		    ret[sample][gene] = true;
		}
		fetch_promise.resolve(ret);
	    });
	}),
	'getSampleHeatmapData': function(genetic_profile_id, genes) {
	    return getHeatmapDataCached(this, genetic_profile_id, genes, 'sample');
	},
	'getPatientHeatmapData': function(genetic_profile_id, genes) {
	    return getHeatmapDataCached(this, genetic_profile_id, genes, 'patient');
	},
	'getExternalDataStatus': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    self.getWebServiceGenomicEventData().then(function() {
			fetch_promise.resolve(deepCopyObject(self.external_data_status));
		    }).fail(function() {
			fetch_promise.resolve(deepCopyObject(self.external_data_status));
		    });
		}),
	'getClusteringOrder': function (track_uid_map, heatmapData, cases) {
			//cases is output of either getSamples() or getPatients()
			var def = new $.Deferred();
			//prepare input:
			var cluster_input = {};
			for (var i = 0; i < cases.length; i++) {
				//iterate over genetic entities and get the sample data (heatmap data has genetic entityId as key):
				for (var j = 0; j < heatmapData.length; j++) {
					var entityId = heatmapData[j].gene;
					var caseId = heatmapData[j].oncoprint_data[i].uid;
					//small validation/defensive programming:
					if (!entityId) {
						throw new Error("Unexpected error during getClusteringOrder: attribute 'gene' not found");
					}
					var value = heatmapData[j].oncoprint_data[i].profile_data;
					if (!cluster_input[caseId]) {
						//initialize with case ids as key:
						cluster_input[caseId] = {};
					}
					cluster_input[caseId][entityId] = value;
				}					
			}
			//do hierarchical clustering in background:
			$.when(this.getCaseUIDMap(), cbio.stat.hclusterCases(cluster_input), cbio.stat.hclusterGeneticEntities(cluster_input)).then(
					function (case_uid_map, sampleClusterOrder, entityClusterOrder) {
						//get result in "uid format":
						var uids = sampleClusterOrder.map(function(x) { return x.caseId; });
						/*for (var i = 0; i < sampleClusterOrder.length; i++) {
							var uid_case = case_uid_map[study_id][sampleClusterOrder[i].caseId];
							uids.push(uid_case);
						}*/
						//get entityUids in order:
						var entityUids = [];
						for (var i = 0; i < entityClusterOrder.length; i++) {
							var trackUid = track_uid_map[entityClusterOrder[i].entityId];
							entityUids.push(trackUid);
						}
						//setup and resolve result object:
						var result = new Object();
						result.sampleUidsInClusteringOrder = uids;
						result.entityUidsInClusteringOrder = entityUids;
						def.resolve(result);
					}
			);
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
					default_oql_uniq["FUSION"] = true;
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
				getGeneticProfileDataForQueryCases(self,
				    [genetic_profile_ids[I]],
				    self.getQueryGenes().map(function(x) { return x.toUpperCase();})
				).fail(function () {
				    fetch_promise.reject();
				}).then(function (data) {
				    var genetic_alteration_type = profile_types[genetic_profile_ids[I]];
				    if (genetic_alteration_type === "MUTATION_EXTENDED") {
					for (var j = 0; j < data.length; j++) {
					    data[j].simplified_mutation_type = getSimplifiedMutationType(data[j].mutation_type);
					    if (data[j].amino_acid_change.toLowerCase() === "promoter") {
						data[j].oncoprint_mutation_type = "promoter";
					    } else {
						data[j].oncoprint_mutation_type = getOncoprintMutationType(data[j].simplified_mutation_type);
					    }
					    data[j].genetic_alteration_type = genetic_alteration_type;
					    if (data[j].driver_filter == "Putative_Driver") {
						
					    }
					}
				    } else {
					for (var j = 0; j < data.length; j++) {
					    data[j].genetic_alteration_type = genetic_alteration_type;
					}
				    }
				    all_data = all_data.concat(data);

				    num_calls -= 1;
				    if (num_calls === 0) {
					var webservice_genomic_event_data = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), all_data, default_oql, false, false);
					$.when(annotateOncoKBMutationOncogenic(self, webservice_genomic_event_data),
						annotateHotSpots(self, webservice_genomic_event_data)).then(function () {
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
	'getSessionFilteredWebServiceGenomicEventData': function () {
	    var def = new $.Deferred();
	    var self = this;
	    var getters = {
		'cbioportalCount': function() { return undefined; },
		'cosmicCount': function() { return undefined; }
	    };
	    var fetch_promises = [];
	    if (self.known_mutation_settings.recognize_cbioportal_count) {
		var cbioportal_promise = new $.Deferred();
		self.getCBioPortalMutationCounts().then(function(counts_map) {
		    getters.cbioportalCount = function(datum) {
			if (datum.genetic_alteration_type !== "MUTATION_EXTENDED" || 
				(datum.oncoprint_mutation_type !== "missense" && datum.oncoprint_mutation_type !== "inframe")) {
			    return undefined;
			}
			var gene = datum.hugo_gene_symbol;
			gene && (gene = gene.toUpperCase());
			var start_pos = datum.protein_start_position;
			var end_pos = datum.protein_end_position;
			if (gene && start_pos && end_pos && !isNaN(start_pos) && !isNaN(end_pos)) {
			    return counts_map[gene + ',' + parseInt(start_pos, 10) + ',' + parseInt(end_pos, 10)];
			} else {
			    return undefined;
			}
		    };
		    cbioportal_promise.resolve();
		}).fail(function() {
		    cbioportal_promise.resolve();
		});
		fetch_promises.push(cbioportal_promise);
	    }
	    if (self.known_mutation_settings.recognize_cosmic_count) {
		var cosmic_promise = new $.Deferred();
		self.getCOSMICCounts().then(function (cosmic_counts) {
		    getters.cosmicCount = function (datum) {
			if (datum.genetic_alteration_type === "MUTATION_EXTENDED" && 
				(datum.oncoprint_mutation_type === "missense" || datum.oncoprint_mutation_type === "inframe") && 
				typeof cosmic_counts[datum.keyword] !== "undefined") {
			    return cosmic_counts[datum.keyword].map(function (count_record) {
				return parseInt(count_record.count, 10);
			    })
				    .reduce(function (x, y) {
					return x + y;
				    }, 0);
			} else {
			    return undefined;
			}
		    };
		    cosmic_promise.resolve();
		}).fail(function() {
		    cosmic_promise.resolve();
		});
		fetch_promises.push(cosmic_promise);
	    }
	    $.when(this.getWebServiceGenomicEventData(), $.when.apply(null, fetch_promises)).then(function (data) {
		def.resolve(applyKnownMutationSettings(data, self.known_mutation_settings, getters));
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	},
	'getCOSMICCounts': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    self.getWebServiceGenomicEventData().then(function (webservice_data) {
			var keywords = webservice_data.filter(function (datum) {
			    return datum.genetic_alteration_type === "MUTATION_EXTENDED" && 
				    (datum.oncoprint_mutation_type === "missense" || datum.oncoprint_mutation_type === "inframe") && 
				    typeof datum.keyword !== 'undefined' && datum.keyword !== null;
			})
				.map(function (mutation_datum_with_keyword) {
				    return mutation_datum_with_keyword.keyword;
				});
			if (keywords.length > 0) {
			    var counts = {};
			    $.ajax({
				type: 'POST',
				url: 'api-legacy/cosmic_count',
				data: 'keywords=' + keywords.join(",")
			    }).then(function (cosmic_count_records) {
				for (var i = 0; i < cosmic_count_records.length; i++) {
				    var keyword = cosmic_count_records[i].keyword;
				    counts[keyword] = counts[keyword] || [];
				    counts[keyword].push(cosmic_count_records[i]);
				}
				fetch_promise.resolve(counts);
			    }).fail(function() {
				fetch_promise.reject();
			    });
			} else {
			    fetch_promise.resolve({});
			}
		    });
		}),
	'getCBioPortalMutationCounts': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    // Out: Promise which resolves with map from gene+","+start_pos+","+end_pos to cbioportal mutation count for that position range and gene
		    self.getWebServiceGenomicEventData().then(function (webservice_data) {
			var counts_map = {};
			var to_query = {};
			for (var i = 0; i < webservice_data.length; i++) {
			    var datum = webservice_data[i];
			    if (datum.genetic_alteration_type !== "MUTATION_EXTENDED" || 
				    (datum.oncoprint_mutation_type !== "missense" && datum.oncoprint_mutation_type !== "inframe")) {
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

			if (queries.length > 0) {
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
				fetch_promise.resolve(counts_map);
			    }).fail(function () {
				fetch_promise.reject();
			    });
			} else {
			    fetch_promise.resolve({});
			}
		    });
	}),
	'getGeneAggregatedOncoprintSampleGenomicEventData': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    $.when((use_session_filters ? self.getSessionFilteredWebServiceGenomicEventData() : self.getWebServiceGenomicEventData()), self.getStudySampleMap(), self.getCaseUIDMap(), self.getSampleSequencingData(),
		    self.getGenePanelProfileIds()).then(function (ws_data, study_sample_map, case_uid_map, sample_sequencing_data, gene_panel_profile_ids) {
		var filtered_ws_data = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), ws_data, default_oql, false, false);
		fetch_promise.resolve(makeOncoprintSampleData(filtered_ws_data, self.getQueryGenes(), study_sample_map, case_uid_map, gene_panel_profile_ids.length === 0 ? undefined : sample_sequencing_data, use_session_filters));
	    }).fail(function () {
		fetch_promise.reject();
	    });
	}),
	'getOncoprintSampleGenomicEventData': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    $.when((use_session_filters ? self.getSessionFilteredWebServiceGenomicEventData() : self.getWebServiceGenomicEventData()), 
	    self.getStudySampleMap(), 
	    self.getCaseUIDMap(),
	    self.getSampleUIDs(),
	    self.getSampleSequencingData(),
	    self.getGenePanelProfileIds()).then(function (ws_data, study_sample_map, case_uid_map, sample_uids, sample_sequencing_data, gene_panel_profile_ids) {
		var ws_data_by_oql_line = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), ws_data, default_oql, true, true);
		for (var i = 0; i < ws_data_by_oql_line.length; i++) {
		    var line = ws_data_by_oql_line[i];
		    var oncoprint_data = makeOncoprintSampleData(line.data, [line.gene], study_sample_map, case_uid_map, gene_panel_profile_ids.length === 0 ? undefined : sample_sequencing_data, use_session_filters);
		    var sequenced_samples = {};
		    var altered_samples = {};
		    var altered_sample_uids = {};
		    var unaltered_samples = {};
		    var unaltered_sample_uids = {};
		    for (var j=0; j<oncoprint_data.length; j++) {
			var datum = oncoprint_data[j];
			if (!datum.na) {
			    sequenced_samples[datum.sample] = true;
			}
			if (datum.data.length > 0) {
			    altered_samples[datum.sample] = true;
			    altered_sample_uids[datum.uid] = true;
			} else {
			    unaltered_samples[datum.sample] = true;
			    unaltered_sample_uids[datum.uid] = true;
			}
		    }
		    
		    line.oncoprint_data = oncoprint_data;
		    line.sequenced_samples = Object.keys(sequenced_samples);
		    line.altered_samples = Object.keys(altered_samples);
		    line.unaltered_samples = Object.keys(unaltered_samples);
		    line.altered_sample_uids = Object.keys(altered_sample_uids);
		    line.unaltered_sample_uids = Object.keys(unaltered_sample_uids);
		}
		var oncoprint_sample_genomic_event_data = ws_data_by_oql_line;
		fetch_promise.resolve(oncoprint_sample_genomic_event_data);
	    }).fail(function () {
		fetch_promise.reject();
	    });
	    return fetch_promise.promise();
	}),
	'getAlteredSampleUIDs': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    self.getOncoprintSampleGenomicEventData(use_session_filters).then(function (data_by_line) {
		var altered_samples = stringListUnion(data_by_line.map(function (line) {
		    return line.altered_sample_uids;
		}));
		fetch_promise.resolve(altered_samples);
	    }).fail(function () {
		fetch_promise.reject();
	    });
	    return fetch_promise.promise();
	}),
	'getAlteredSamples': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    self.getOncoprintSampleGenomicEventData(use_session_filters).then(function (data_by_line) {
		var altered_samples = stringListUnion(data_by_line.map(function (line) {
		    return line.altered_samples;
		}));
		fetch_promise.resolve(altered_samples);
	    }).fail(function () {
		fetch_promise.reject();
	    });
	    return fetch_promise.promise();
	}),
	'getUnalteredSampleUIDs': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    $.when(self.getSampleUIDs(), self.getAlteredSampleUIDs(use_session_filters)).then(function (sample_uids, altered_samples) {
		fetch_promise.resolve(stringListDifference(sample_uids, altered_samples));
	    }).fail(function () {
		fetch_promise.reject();
	    });
	    return fetch_promise.promise();
	}),
	'getUnalteredSamples': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    self.getAlteredSamples(use_session_filters).then(function (altered_samples) {
		fetch_promise.resolve(stringListDifference(self.getSampleIds(), altered_samples));
	    }).fail(function () {
		fetch_promise.reject();
	    });
	    return fetch_promise.promise();
	}),
	'getMutualAlterationCounts': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    self.getAlteredSampleSetsByGene(use_session_filters).then(function (altered_samples_by_gene) {
		var genes = Object.keys(altered_samples_by_gene);
		var all_samples_set = stringListToObject(self.getSampleIds());
		var ret = [];
		for (var i = 0; i < genes.length; i++) {
		    for (var j = i + 1; j < genes.length; j++) {
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
	    }).fail(function () {
		fetch_promise.reject();
	    });
	    return fetch_promise.promise();
	}),
	'getAlteredSampleSetsByGene': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    (use_session_filters ? self.getSessionFilteredWebServiceGenomicEventData() : self.getWebServiceGenomicEventData()).then(function (ws_data) {
		var altered_samples_by_gene = {};
		var genes = self.getQueryGenes();
		for (var i = 0; i < genes.length; i++) {
		    altered_samples_by_gene[genes[i]] = {};
		}
		for (var i = 0; i < ws_data.length; i++) {
		    var gene = ws_data[i].hugo_gene_symbol.toUpperCase();
		    var sample = ws_data[i].sample_id;
		    altered_samples_by_gene[gene] && (altered_samples_by_gene[gene][sample] = true);
		}
		fetch_promise.resolve(altered_samples_by_gene);
	    }).fail(function () {
		fetch_promise.reject();
	    });
	}),
	'getOncoprintPatientGenomicEventData': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    $.when((use_session_filters ? self.getSessionFilteredWebServiceGenomicEventData() : self.getWebServiceGenomicEventData()), 
	    self.getStudyPatientMap(), 
	    self.getPatientSampleIdMap(), 
	    self.getCaseUIDMap(),
	    self.getPatientSequencingData(),
	    self.getGenePanelProfileIds()).then(function (ws_data, study_patient_map, sample_to_patient_map, case_uid_map, patient_sequencing_data, gene_panel_profile_ids) {
		var ws_data_by_oql_line = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), ws_data, default_oql, true, true);
		for (var i = 0; i < ws_data_by_oql_line.length; i++) {
		    var line = ws_data_by_oql_line[i];
		    var oncoprint_data = makeOncoprintPatientData(ws_data_by_oql_line[i].data, [ws_data_by_oql_line[i].gene], study_patient_map, sample_to_patient_map, case_uid_map, gene_panel_profile_ids.length === 0 ? undefined : patient_sequencing_data, use_session_filters);
		    var sequenced_patients = {};
		    var altered_patients = {};
		    var altered_patient_uids = {};
		    var unaltered_patients = {};
		    var unaltered_patient_uids = {};
		    for (var j=0; j<oncoprint_data.length; j++) {
			var datum = oncoprint_data[j];
			if (!datum.na) {
			    sequenced_patients[datum.patient] = true;
			}
			if (datum.data.length > 0) {
			    altered_patients[datum.patient] = true;
			    altered_patient_uids[datum.uid] = true;
			} else {
			    unaltered_patients[datum.patient] = true;
			    unaltered_patient_uids[datum.uid] = true;
			}
		    }
		    
		    line.oncoprint_data = oncoprint_data;
		    line.sequenced_patients = Object.keys(sequenced_patients);
		    line.altered_patients = Object.keys(altered_patients);
		    line.unaltered_patients = Object.keys(unaltered_patients);
		    line.altered_patient_uids = Object.keys(altered_patient_uids);
		    line.unaltered_patient_uids = Object.keys(unaltered_patient_uids);
		}
		var oncoprint_patient_genomic_event_data = ws_data_by_oql_line;
		fetch_promise.resolve(oncoprint_patient_genomic_event_data);
	    }).fail(function () {
		fetch_promise.reject();
	    });
	}),
	'getAlteredPatientUIDs': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    self.getOncoprintPatientGenomicEventData(use_session_filters).then(function (data_by_line) {
		var altered_patients = stringListUnion(data_by_line.map(function (line) {
		    return line.altered_patient_uids;
		}));
		fetch_promise.resolve(altered_patients);
	    }).fail(function () {
		fetch_promise.reject();
	    });
	}),
	'getUnalteredPatientUIDs': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    $.when(self.getAlteredPatientUIDs(use_session_filters), self.getPatientUIDs()).then(function (altered_patients, patient_uids) {
		fetch_promise.resolve(stringListDifference(patient_uids, altered_patients));
	    }).fail(function () {
		fetch_promise.reject();
	    });
	}),
	'getAlteredPatients': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    self.getOncoprintPatientGenomicEventData(use_session_filters).then(function (data_by_line) {
		var altered_patients = stringListUnion(data_by_line.map(function (line) {
		    return line.altered_patients;
		}));
		fetch_promise.resolve(altered_patients);
	    }).fail(function () {
		fetch_promise.reject();
	    });
	}),
	'getUnalteredPatients': makeCachedPromiseFunctionWithSessionFilterOption(
		function (self, fetch_promise, use_session_filters) {
	    $.when(self.getAlteredPatients(use_session_filters), self.getPatientIds()).then(function (altered_patients, patient_ids) {
		fetch_promise.resolve(stringListDifference(patient_ids, altered_patients));
	    }).fail(function () {
		fetch_promise.reject();
	    });
	}),
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
		    var study_sample_map = self.getStudySampleMap();
		    var sample_clinical_attributes_set = {};
		    var requests = self.getCancerStudyIds().map(
			    function (cancer_study_id) {
				var def = new $.Deferred();
				window.cbioportal_client.getSampleClinicalAttributes({study_id: [cancer_study_id]}).then(function (attrs) {
				    for (var i = 0; i < attrs.length; i++) {
					sample_clinical_attributes_set[attrs[i].attr_id] = attrs[i];
				    }
				    def.resolve();
				}).fail(function () {
				    fetch_promise.reject();
				});
				return def.promise();
			    });
		    $.when.apply($, requests).then(function () {
			self.getMutationProfileIds().then(function (mutation_profile_ids) {
			    if (mutation_profile_ids.length > 0) {
				sample_clinical_attributes_set['# mutations'] = {attr_id: "# mutations",
				    datatype: "NUMBER",
				    description: "Number of mutations",
				    display_name: "Total mutations",
				    is_patient_attribute: "0"
				};
				sample_clinical_attributes_set['NO_CONTEXT_MUTATION_SIGNATURE'] = {
				 attr_id: "NO_CONTEXT_MUTATION_SIGNATURE",
				 datatype: "COUNTS_MAP",
				 description: "Number of point mutations in the sample counted by different types of nucleotide changes.",
				 display_name: "Mutation spectrum",
				 is_patient_attribute: "0",
				 categories: ["C>A", "C>G", "C>T", "T>A", "T>C", "T>G"],
				 fills: ['#3D6EB1', '#8EBFDC', '#DFF1F8', '#FCE08E', '#F78F5E', '#D62B23']
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
			    if (self.getCancerStudyIds().length > 1) {
				sample_clinical_attributes_set['CANCER_STUDY'] = {
				    attr_id: 'CANCER_STUDY',
				    datatype: 'STRING',
				    description: 'Study which the sample is a part of.',
				    display_name: 'Study of origin',
				    is_patient_attribute: '0'
				};
			    }
			    fetch_promise.resolve(sample_clinical_attributes_set);
			}).fail(function () {
			    fetch_promise.reject();
			});
		    });
		}),
	'getPatientClinicalAttributesSet': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getStudyPatientMap().then(function (study_patient_map) {
			var patient_clinical_attributes_set = {};
			var requests = self.getCancerStudyIds().map(
				function (cancer_study_id) {
				    var def = new $.Deferred();
				    window.cbioportal_client.getPatientClinicalAttributes({study_id: [cancer_study_id]}).then(function (attrs) {
					for (var i = 0; i < attrs.length; i++) {
					    patient_clinical_attributes_set[attrs[i].attr_id] = attrs[i];
					}
					def.resolve();
				    }).fail(function () {
					fetch_promise.reject();
				    });
				    return def.promise();
				});
			$.when.apply($, requests).then(function () {
			    fetch_promise.resolve(patient_clinical_attributes_set);
			});
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
		//when querying virtual study with once real study case_set_id is all, in this case set case_set_id to -1
	    return case_set_properties.case_set_id == "all" ? "-1" : case_set_properties.case_set_id;
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
		    var sample_to_patient = {}; // study->sample->patient
		    $.when.apply($, self.getCancerStudyIds().map(function(study) {
			var def = new $.Deferred();
			window.cbioportal_client.getSamples({study_id: [study], sample_ids: self.getSampleIds()}).then(function (data) {
			    sample_to_patient[study] = sample_to_patient[study] || {};
			    for (var i = 0; i < data.length; i++) {
				sample_to_patient[study][data[i].id] = data[i].patient_id;
			    }
			    def.resolve();
			}).fail(function () {
			    def.reject();
			});
			return def.promise();
		    })).then(function() {
			fetch_promise.resolve(sample_to_patient);
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getSimplePatientSampleIdMap': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    var sample_to_patient = {}; // study->sample->patient
		    $.when.apply($, self.getCancerStudyIds().map(function(study) {
			var def = new $.Deferred();
			window.cbioportal_client.getSamples({study_id: [study], sample_ids: self.getSampleIds()}).then(function (data) {
			    for (var i = 0; i < data.length; i++) {
				sample_to_patient[data[i].id] = data[i].patient_id;
			    }
			    def.resolve();
			}).fail(function () {
			    def.reject();
			});
			return def.promise();
		    })).then(function() {
			fetch_promise.resolve(sample_to_patient);
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getCancerStudyNames': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getStudyNameMap().then(function(map) {
			fetch_promise.resolve(self.getCancerStudyIds().map(function(id) { return map[id]; }));
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getStudyNameMap': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    window.cbioportal_client.getStudies({study_ids: self.cancer_study_ids}).then(function (studies) {
			fetch_promise.resolve(studies.reduce(function(map, s) {
			    map[s.id] = s.name;
			    return map;
			}, {}));
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getGeneticProfiles': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    window.cbioportal_client.getGeneticProfiles({genetic_profile_ids: self.genetic_profile_ids}).then(function(gps) {
			fetch_promise.resolve(gps);
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getGeneticProfileIdToSamplesMap': makeCachedPromiseFunction(
		function(self, fetch_promise) {
		    $.when(self.getGeneticProfiles(), self.getStudySampleMap()).then(function(gps, study_sample_map) {
			fetch_promise.resolve(gps.reduce(function(map, next_gp) {
			    map[next_gp.id] = study_sample_map[next_gp.study_id];
			    return map;
			}, {}));
		    }).fail(function() {
			fetch_promise.reject();
		    });
		}),
	'getMutationProfiles': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getGeneticProfiles().then(function (profiles) {
			fetch_promise.resolve(
				profiles
				.filter(function (p) {
				    return p.genetic_alteration_type === "MUTATION_EXTENDED";
				})
				);
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getMutationProfileIds': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getMutationProfiles().then(function (profiles) {
			fetch_promise.resolve(
				profiles
				.map(function (p) {
				    return p.id;
				})
				);
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getGenePanelProfiles': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getGeneticProfiles().then(function (profiles) {
			var gene_panel_profiles = profiles.filter(function(p) {
			    return p.genetic_alteration_type === "MUTATION_EXTENDED" ||
				    p.genetic_alteration_type === "COPY_NUMBER_ALTERATION";
			});
			var priority = {"MUTATION_EXTENDED":0, "COPY_NUMBER_ALTERATION":1};
			gene_panel_profiles = _.sortBy(gene_panel_profiles, function(p) { return priority[p.genetic_alteration_type]; });
			fetch_promise.resolve(gene_panel_profiles);
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getGenePanelProfileIds': makeCachedPromiseFunction(
		function (self, fetch_promise) {
		    self.getGenePanelProfiles().then(function (profiles) {
			fetch_promise.resolve(profiles.map(function(p) { return p.id; }));
		    }).fail(function () {
			fetch_promise.reject();
		    });
		}),
	'getZScoreThreshold':function() {
	    return z_score_threshold;
	},
	'getRppaScoreThreshold':function() {
	    return rppa_score_threshold;
	}
    };
};

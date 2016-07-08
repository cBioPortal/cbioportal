var utils = {
    'timeoutSeparatedLoop': function (array, loopFn) {
	// loopFn is function(elt, index, array) {
	var finished_promise = new $.Deferred();
	var loopBlock = function (i) {
	    if (i >= array.length) {
		finished_promise.resolve();
		return;
	    }

	    loopFn(array[i], i, array);
	    setTimeout(function () {
		loopBlock(i + 1);
	    }, 0);
	};
	loopBlock(0);
	return finished_promise.promise();
    },
    'sign': function (x) {
	if (x > 0) {
	    return 1;
	} else if (x < 0) {
	    return -1;
	} else {
	    return 0;
	}
    },
    'invertArray': function (arr) {
	var ret = {};
	for (var i = 0; i < arr.length; i++) {
	    ret[arr[i]] = i;
	}
	return ret;
    },
    'makeSVGElement': function (tag, attrs) {
	var el = document.createElementNS('http://www.w3.org/2000/svg', tag);
	for (var k in attrs) {
	    if (attrs.hasOwnProperty(k)) {
		el.setAttribute(k, attrs[k]);
	    }
	}
	return el;
    },
    'objectValues': function(obj) {
	return Object.keys(obj).map(function(k) { return obj[k]; });
    },
    'proportionToPercentString': function(p) {
	var percent = 100*p;
	if (p < 0.03) {
	    // if less than 3%, use one decimal figure
	    percent = Math.round(10*percent)/10;
	} else {
	    percent = Math.round(percent);
	}
	return percent+'%';
    }
};

var tooltip_utils = {
    'sampleViewAnchorTag': function (study_id, sample_id) {
	var href = cbio.util.getLinkToSampleView(study_id, sample_id);
	return '<a href="' + href + '">' + sample_id + '</a>';
    },
    'patientViewAnchorTag': function(study_id, patient_id) {
	var href = cbio.util.getLinkToPatientView(study_id, patient_id);
	return '<a href="' + href + '">' + patient_id + '</a>';
    },
    'makeGeneticTrackTooltip':function(data_type, link_id) {
	return function (d) {
	    var ret = '';
	    var contains_recurrent_mutation = false;
	    var mutations = [];
	    var cna = [];
	    var mrna = [];
	    var prot = [];
	    for (var i=0; i<d.data.length; i++) {
		var datum = d.data[i];
		if (datum.genetic_alteration_type === "MUTATION_EXTENDED") {
		    mutations.push(datum.amino_acid_change);
		    if (datum.cbioportal_mutation_count > 10) {
			contains_recurrent_mutation = true;
		    }
		} else if (datum.genetic_alteration_type === "COPY_NUMBER_ALTERATION") {
		    var disp_cna = {'-2': 'HOMODELETED', '-1': 'HETLOSS', '1': 'GAIN', '2': 'AMPLIFIED'};
		    if (disp_cna.hasOwnProperty(datum.profile_data)) {
			cna.push(disp_cna[datum.profile_data]);
		    }
		} else if (datum.genetic_alteration_type === "MRNA_EXPRESSION" || datum.genetic_alteration_type === "PROTEIN_LEVEL") {
		    if (datum.oql_regulation_direction) {
			(datum.genetic_alteration_type === "MRNA_EXPRESSION" ? mrna : prot)
				.push(datum.oql_regulation_direction === 1 ? "UPREGULATED" : "DOWNREGULATED");
		    }
		}
	    }
	    if (mutations.length > 0) {
		ret += 'Mutation: <b>' + mutations.join(", ")+'</b><br>';
		if (contains_recurrent_mutation) {
		    ret += '<i>Contains mutation at a recurrently mutated position.</i><br>';
		}
	    }
	    if (cna.length > 0) {
		ret += 'Copy Number Alteration: <b>'+cna.join(", ")+'</b><br>';
	    }
	    if (mrna.length > 0) {
		ret += 'MRNA: <b>' + mrna.join(", ") + '</b><br>';
	    }
	    if (prot.length > 0) {
		ret += 'PROT: <b>' + prot.join(", ") + '</b><br>';
	    }
	    ret += (data_type === 'sample' ? (link_id ? tooltip_utils.sampleViewAnchorTag(d.study_id, d.sample) : d.sample) : (link_id ? tooltip_utils.patientViewAnchorTag(d.study_id, d.patient) : d.patient));
	    return ret;
	}
    },
    'makeClinicalTrackTooltip':function(data_type, link_id) {
	return function(d) {
	    var ret = '';
	    var attr_vals = ((d.attr_val_counts && Object.keys(d.attr_val_counts)) || []);
	    if (attr_vals.length > 1) {
		ret += 'values:<br>';
		for (var i=0; i<attr_vals.length; i++) {
		    var val = attr_vals[i];
		    ret += '<b>' + val + '</b>: '+d.attr_val_counts[val]+'<br>';
		}
	    } else if (attr_vals.length === 1) {
		ret += 'value: <b>'+attr_vals[0]+'</b><br>';
	    }
	    ret += (link_id ? (data_type === 'sample' ? tooltip_utils.sampleViewAnchorTag(d.study_id, d.sample) : tooltip_utils.patientViewAnchorTag(d.study_id, d.patient))
			    : (data_type === 'sample' ? d.sample : d.patient));
	    return ret;
	};
    }
};

var makeComparatorMetric = function(array_spec) {
    var metric = {};
    for (var i=0; i<array_spec.length; i++) {
	var equiv_values = [].concat(array_spec[i]);
	for (var j=0; j<equiv_values.length; j++) {
	    metric[equiv_values[j]] = i;
	}
    }
    return metric;
};
var comparator_utils = {
    'makeGeneticComparator': function (distinguish_mutation_types, distinguish_recurrent) {
	var cna_key = 'disp_cna';
	var cna_order = makeComparatorMetric(['amp', 'homdel', 'gain', 'hetloss', 'diploid', undefined]);
	var mut_type_key = 'disp_mut';
	var mut_order = (function () {
	    var _order;
	    if (!distinguish_mutation_types && !distinguish_recurrent) {
		return function (m) {
		    if (m === 'fusion') {
			return 0;
		    } else {
			return ({'true': 1, 'false': 2})[!!m];
		    }
		    //return +(typeof m === 'undefined');
		}
	    } else if (!distinguish_mutation_types && distinguish_recurrent) {
		_order = makeComparatorMetric([['inframe_rec', 'missense_rec'], ['fusion', 'fusion_rec', 'inframe', 'missense', 'trunc', 'trunc_rec'], undefined]); 
	    } else if (distinguish_mutation_types && !distinguish_recurrent) {
		_order = makeComparatorMetric([['fusion', 'fusion_rec'], ['trunc', 'trunc_rec'], ['inframe','inframe_rec'], ['missense', 'missense_rec'], undefined, true, false]);
	    } else if (distinguish_mutation_types && distinguish_recurrent) {
		_order = makeComparatorMetric([['fusion', 'fusion_rec'], ['trunc', 'trunc_rec'], 'inframe_rec', 'missense_rec', 'inframe', 'missense',  undefined, true, false]);
	    }
	    return function(m) {
		return _order[m];
	    }
	})();
	var mrna_key = 'disp_mrna';
	var rppa_key = 'disp_prot';
	var regulation_order = makeComparatorMetric(['up', 'down', undefined]);

	return function (d1, d2) {
	    var cna_diff = utils.sign(cna_order[d1[cna_key]] - cna_order[d2[cna_key]]);
	    if (cna_diff !== 0) {
		return cna_diff;
	    }

	    var mut_type_diff = utils.sign(mut_order(d1[mut_type_key]) - mut_order(d2[mut_type_key]));
	    if (mut_type_diff !== 0) {
		return mut_type_diff;
	    }

	    var mrna_diff = utils.sign(regulation_order[d1[mrna_key]] - regulation_order[d2[mrna_key]]);
	    if (mrna_diff !== 0) {
		return mrna_diff;
	    }

	    var rppa_diff = utils.sign(regulation_order[d1[rppa_key]] - regulation_order[d2[rppa_key]]);
	    if (rppa_diff !== 0) {
		return rppa_diff;
	    }

	    return 0;
	};
    },
    'numericalClinicalComparator': function (d1, d2) {
	if (d1.na && d2.na) {
	    return 0;
	} else if (d1.na && !d2.na) {
	    return 2;
	} else if (!d1.na && d2.na) {
	    return -2;
	} else {
	    return (d1.attr_val < d2.attr_val ? -1 : (d1.attr_val === d2.attr_val ? 0 : 1));
	}
    },
    'stringClinicalComparator': function (d1, d2) {
	if (d1.na && d2.na) {
	    return 0;
	} else if (d1.na && !d2.na) {
	    return 2;
	} else if (!d1.na && d2.na) {
	    return -2;
	} else {
	    return d1.attr_val.localeCompare(d2.attr_val);
	}
    }
	
};

	
window.CreateCBioPortalOncoprintWithToolbar = function (ctr_selector, toolbar_selector) {
    
    $('#oncoprint #everything').show();
    $('#oncoprint #oncoprint-diagram-toolbar-buttons').show();
    
    $(ctr_selector).css({'position':'relative'});
    
    var LoadingBar = (function() {
	var $loading_bar_svg = $('<svg width="100" height="50"></svg><br>')
				.appendTo(ctr_selector)
				.append(utils.makeSVGElement("rect", {
							    "width":100, 
							    "height":25, 
							    "stroke":"black",
							    "fill":"white"
							}));
	$loading_bar_svg.append(utils.makeSVGElement("rect", {
							    "width":100, 
							    "height":25, 
							    "stroke":"black",
							    "fill":"white"
							}));
	var $loading_bar = $(utils.makeSVGElement("rect", {
							"width":0, 
							"height":25, 
							"fill":"green", 
							"stroke":"dark green"}))
				.appendTo($loading_bar_svg);
	var $loading_bar_msg = $(utils.makeSVGElement("text", {
					    'x': 2,
					    'y':15,
					    'font-size':11,
					    'font-family':'Arial',
					    'font-weight':'normal',
					    'text-anchor':'start',
					}))
		.appendTo($loading_bar_svg);

	return {
	    'hide': function() {
		$loading_bar_svg.hide();
	    },
	    'show': function() {
		$loading_bar_svg.show();
	    },
	    'msg': function(str) {
		$loading_bar_msg[0].textContent = str;
	    },
	    'update': function(proportion) {
		$loading_bar.attr('width', proportion*parseFloat($loading_bar_svg.attr('width')));
	    },
	    'DOWNLOADING_MSG': 'Downloading data..'
	};
    })();
    
    var oncoprint = new window.Oncoprint(ctr_selector, 1050);
    var toolbar_fade_out_timeout;
    $(ctr_selector).add(toolbar_selector).on("mouseover", function(evt) {
	$(toolbar_selector).fadeIn('fast');
	clearTimeout(toolbar_fade_out_timeout);
    });
    $(ctr_selector).add(toolbar_selector).on("mouseleave", function(evt) {
	clearTimeout(toolbar_fade_out_timeout);
	toolbar_fade_out_timeout = setTimeout(function() {
	    $(toolbar_selector).fadeOut();
	}, 700);
    });
    
    var URL = (function() {
	var changeURLParam = function (param, new_value, url) {
	    var index = url.indexOf(param + '=');
	    var before_url, after_url;
	    if (index === -1) {
		before_url = url;
		if (before_url[before_url.length-1] !== "&") {
		    before_url = before_url + "&";
		}
		after_url = "";
		index = url.length;
	    } else {
		before_url = url.substring(0, index);
		var next_amp = url.indexOf("&", index);
		if (next_amp === -1) {
		    next_amp = url.length;
		}
		after_url = url.substring(next_amp + 1);
	    }
	    return before_url 
		    + (new_value.length > 0 ? (param + '=' + new_value + "&") : "") 
		    + after_url;
	};
	var currURL = function() {
	    return window.location.href;
	};
	var getParamValue = function(param) {
	    var url = currURL();
	    var param_index = url.indexOf(param+"=");
	    if (param_index > -1) {
		var param_start = param_index + (param+"=").length;
		var param_end = url.indexOf("&", param_index);
		if (param_end === -1) {
		    param_end = url.length;
		}
		return url.substring(param_start, param_end);
	    } else {
		return null;
	    }
	};
	
	var init_show_samples = getParamValue("show_samples");
	var init_clinical_attrs = getParamValue("clinicallist");
	var CLINICAL_ATTRS_PARAM = "clinicallist";
	var SAMPLE_DATA_PARAM = "show_samples";
	return {
	    'update': function() {
		var new_url = currURL();
		new_url = changeURLParam(CLINICAL_ATTRS_PARAM, 
					    State.used_clinical_attributes
						    .map(function(attr) { return encodeURIComponent(attr.attr_id);})
						    .join(","),
					    new_url);
		new_url = changeURLParam(SAMPLE_DATA_PARAM,
					State.using_sample_data+'',
					new_url);
		window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", new_url);
	    },
	    'getInitDataType': function() {
		if (init_show_samples === null) {
		    return null;
		} else {
		    return (init_show_samples === 'true' ? 'sample' : 'patient');
		}
	    },
	    'getInitUsedClinicalAttrs': function() {
		if (init_clinical_attrs === null) {
		    return null;
		} else {
		    return init_clinical_attrs.trim().split(",").map(decodeURIComponent);
		}
	    },
	    'getDataType': function() {
		var using_sample_data = getParamValue(SAMPLE_DATA_PARAM);
		if (using_sample_data === null) {
		    return null;
		} else {
		    return (using_sample_data ? 'sample' : 'patient');
		}
	    },
	    'getUsedClinicalAttrs': function() {
		var clinical_attr_id_list = getParamValue(CLINICAL_ATTRS_PARAM);
		if (clinical_attr_id_list === null) {
		    return null;
		} else {
		    return clinical_attr_id_list.trim().split(",").map(decodeURIComponent);
		}
	    }
	};
    })();
    
    var State = (function () {
	var populateSampleData = function() {
	    var done = new $.Deferred();
	    oncoprint.hideIds([], true);
	    var clinical_attrs = utils.objectValues(State.clinical_tracks);
	    LoadingBar.show();
	    LoadingBar.msg(LoadingBar.DOWNLOADING_MSG);
	    $.when(QuerySession.getOncoprintSampleGenomicEventData(),
		    QuerySession.getUnalteredSamples(),
		    ClinicalData.getSampleData(clinical_attrs))
		    .then(function (oncoprint_data_by_line,
				    unaltered_samples,
				    clinical_data) {
					
			if (State.unaltered_cases_hidden) {
			    oncoprint.hideIds(unaltered_samples, true);
			}
			LoadingBar.msg("Loading oncoprint");
			oncoprint.suppressRendering();
			oncoprint.keepSorted(false);
			
			var total_tracks_to_add = Object.keys(State.genetic_alteration_tracks).length
						+ Object.keys(State.clinical_tracks).length;
			
			utils.timeoutSeparatedLoop(Object.keys(State.genetic_alteration_tracks), function (track_line, i) {
			    var track_id = State.genetic_alteration_tracks[track_line];
			    oncoprint.setTrackData(track_id, oncoprint_data_by_line[track_line].oncoprint_data, 'uid');
			    oncoprint.setTrackInfo(track_id, utils.proportionToPercentString(oncoprint_data_by_line[track_line].altered_samples.length/window.QuerySession.getSampleIds().length));
			    oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeGeneticTrackTooltip('sample', true));
			    LoadingBar.update(i / total_tracks_to_add);
			}).then(function() {
			    return utils.timeoutSeparatedLoop(Object.keys(State.clinical_tracks), function(track_id, i) {
				var attr = State.clinical_tracks[track_id];
				oncoprint.setTrackData(track_id, clinical_data[attr.attr_id], 'uid');
				oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeClinicalTrackTooltip('sample', true));
				LoadingBar.update((i + Object.keys(State.genetic_alteration_tracks).length) / total_tracks_to_add);
			    });
			}).then(function () {
			    oncoprint.keepSorted();
			    oncoprint.releaseRendering();
			    LoadingBar.msg("");
			    LoadingBar.hide();
			    done.resolve();
			});
		    }).fail(function() {
			done.reject();
		    });
	    return done.promise();
	};
	
	var populatePatientData = function() {
	    var done = new $.Deferred();
	    oncoprint.hideIds([], true);
	    var clinical_attrs = utils.objectValues(State.clinical_tracks);
	    
	    LoadingBar.show();
	    LoadingBar.msg(LoadingBar.DOWNLOADING_MSG);
	    $.when(QuerySession.getOncoprintPatientGenomicEventData(),
		    QuerySession.getUnalteredPatients(),
		    ClinicalData.getPatientData(clinical_attrs),
		    QuerySession.getPatientIds())
		    .then(function (oncoprint_data_by_line, 
				    unaltered_patients,
				    clinical_data,
				    patient_ids) {
					
			if (State.unaltered_cases_hidden) {
			    oncoprint.hideIds(unaltered_patients, true);
			}
			LoadingBar.msg("Loading oncoprint");
			oncoprint.suppressRendering();
			oncoprint.keepSorted(false);
			
			var total_tracks_to_add = Object.keys(State.genetic_alteration_tracks).length
						+ Object.keys(State.clinical_tracks).length;
			
			utils.timeoutSeparatedLoop(Object.keys(State.genetic_alteration_tracks), function (track_line, i) {
			    var track_id = State.genetic_alteration_tracks[track_line];
			    oncoprint.setTrackData(track_id, oncoprint_data_by_line[track_line].oncoprint_data, 'uid');
			    oncoprint.setTrackInfo(track_id, utils.proportionToPercentString(oncoprint_data_by_line[track_line].altered_patients.length/patient_ids.length));
			    oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeGeneticTrackTooltip('patient', true));
			    LoadingBar.update(i / total_tracks_to_add);
			}).then(function() {
			    return utils.timeoutSeparatedLoop(Object.keys(State.clinical_tracks), function(track_id, i) {
				var attr = State.clinical_tracks[track_id];
				oncoprint.setTrackData(track_id, clinical_data[attr.attr_id], 'uid');
				oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeClinicalTrackTooltip('patient', true));
				LoadingBar.update((i + Object.keys(State.genetic_alteration_tracks).length) / total_tracks_to_add);
			    });
			}).then(function () {
			    oncoprint.keepSorted();
			    oncoprint.releaseRendering();
			    LoadingBar.msg("");
			    LoadingBar.hide();
			    done.resolve();
			});
		    }).fail(function() {
			done.reject();
		    });
	    return done.promise();
	};
	
	var populateClinicalTrack = function(track_id) {
	    var done = new $.Deferred();
	    var attr = State.clinical_tracks[track_id];
	    ClinicalData[State.using_sample_data ? 'getSampleData' : 'getPatientData'](attr).then(function(data) {
		data = data[attr.attr_id];
		oncoprint.setTrackData(track_id, data, "uid");
		oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeClinicalTrackTooltip((State.using_sample_data ? 'sample' : 'patient'), true));
		done.resolve();
	    }).fail(function() {
		done.reject();
	    });
	    return done.promise();
	};
	
	var makeRemoveAttributeHandler = function(attr) {
	    return function (track_id) {
		delete State.clinical_tracks[track_id];
		State.unuseAttribute(attr.attr_id);
		Toolbar.refreshClinicalAttributeSelector();
		if (Object.keys(State.clinical_tracks).length === 0) {
		    $(toolbar_selector + ' #oncoprint-diagram-showlegend-icon').hide();
		}
	    }
	};
	var setSortOrder = function(order) {
	    oncoprint.setSortConfig({'type':'order', 'order':order});
	};
	
	var getPercent = function(proportion) {
	    return Math.round(proportion*100) + '%';
	};
	
	var updateAlteredPercentIndicator = function(state) {
	    $.when(QuerySession.getAlteredSamples(), QuerySession.getAlteredPatients(), QuerySession.getPatientIds())
		    .then(function(altered_samples, altered_patients, patient_ids) {
			var text = "Altered in ";
			text += (state.using_sample_data ? altered_samples.length : altered_patients.length);
			text += " (";
			text += utils.proportionToPercentString((state.using_sample_data ? (altered_samples.length / QuerySession.getSampleIds().length) : (altered_patients.length / patient_ids.length)));
			text +=") of ";
			text += (state.using_sample_data ? QuerySession.getSampleIds().length : patient_ids.length);
			text += " ";
			text += (state.using_sample_data ? "samples" : "cases/patients");
			$('#altered_value').text(text);
	    });
	};
	
	var State = {
	    'first_genetic_alteration_track': null,
	    'genetic_alteration_tracks': {}, // track_id -> gene
	    'clinical_tracks': {}, // track_id -> attr
	    
	    'used_clinical_attributes': [],
	    'unused_clinical_attributes': [],
	    'clinical_attributes_fetched': new $.Deferred(),
	    'clinical_attr_id_to_sample_data': {},
	    'clinical_attr_id_to_patient_data': {},
	    
	    'cell_padding_on': true,
	    'using_sample_data': (URL.getInitDataType() === 'sample'),
	    'unaltered_cases_hidden': false,
	    'clinical_track_legends_shown': false,
	    'mutations_colored_by_type': true,
	    'sorted_by_mutation_type': true,
	    
	    'patient_order_loaded': new $.Deferred(),
	    'patient_order': [],
	    
	    'sortby': 'data',
	    'sortby_type': true,
	    'sortby_recurrence': false,
	    'colorby_type': true,
	    'colorby_recurrence': false,
	    
	    'sorting_by_given_order': false,
	    'sorting_alphabetically': false,
	    
	    'useAttribute': function (attr_id) {
		var index = this.unused_clinical_attributes.findIndex(function (attr) {
		    return attr.attr_id === attr_id;
		});
		var ret = null;
		if (index > -1) {
		    var attr = this.unused_clinical_attributes[index];
		    this.unused_clinical_attributes.splice(index, 1);
		    this.used_clinical_attributes.push(attr);
		    ret = attr;
		}
		URL.update();
		return ret;
	    },
	    'unuseAttribute': function (attr_id) {
		var index = this.used_clinical_attributes.findIndex(function (attr) {
		    return attr.attr_id === attr_id;
		});
		if (index > -1) {
		    var attr = this.used_clinical_attributes[index];
		    this.used_clinical_attributes.splice(index, 1);
		    this.unused_clinical_attributes.push(attr);
		}
		URL.update();
	    },
	    'setDataType': function (sample_or_patient) {
		var def = new $.Deferred();
		var self = this;
		QuerySession.getCaseUIDMap().then(function (case_uid_map) {
		    // TODO: assume multiple studies
		    var study_id = QuerySession.getCancerStudyIds()[0];
		    var getUID = function (id) {
			return case_uid_map[study_id][id];
		    };
		    var proxy_promise;
		    if (sample_or_patient === 'sample') {
			self.using_sample_data = true;
			URL.update();
			updateAlteredPercentIndicator(self);
			proxy_promise = populateSampleData();
		    } else if (sample_or_patient === 'patient') {
			self.using_sample_data = false;
			URL.update();
			updateAlteredPercentIndicator(this);
			proxy_promise = populatePatientData();
		    }
		    self.patient_order_loaded.then(function () {
			var id_order = (self.using_sample_data ? QuerySession.getSampleIds() : self.patient_order).slice();
			if (self.sorting_alphabetically) {
			    id_order = id_order.sort();
			}
			if (self.sorting_alphabetically || self.sorting_by_given_order) {
			    setSortOrder(id_order.map(getUID));
			}
			proxy_promise.then(function () {
			    def.resolve();
			}).fail(function () {
			    def.fail();
			});
		    });
		});
		return def.promise();
	    },
	    'addGeneticTracks': function (oncoprint_data_by_line) {
		oncoprint.suppressRendering();
		var track_ids = [];
		for (var i = 0; i < oncoprint_data_by_line.length; i++) {
		    var track_params = {
			'rule_set_params': this.getGeneticRuleSetParams(),
			'label': oncoprint_data_by_line[i].gene,
			'target_group': 1,
			'sortCmpFn': this.getGeneticComparator(),
			'removable': true,
			'description': oncoprint_data_by_line[i].oql_line,
		    };
		    var new_track_id = oncoprint.addTracks([track_params])[0];
		    track_ids.push(new_track_id);
		    State.genetic_alteration_tracks[i] = new_track_id;
		    if (State.first_genetic_alteration_track === null) {
			State.first_genetic_alteration_track = new_track_id;
		    } else {
			oncoprint.shareRuleSet(State.first_genetic_alteration_track, new_track_id);
		    }
		}
		oncoprint.releaseRendering();
		return track_ids;
	    },
	    'useAndAddAttribute': function(attr_id) {
		var attr = this.useAttribute(attr_id);
		this.addClinicalTracks(attr);
	    },
	    'addClinicalTracks': function (attrs) {
		attrs = [].concat(attrs);
		oncoprint.suppressRendering();
		var track_ids = [];
		for (var i = 0; i < attrs.length; i++) {
		    var attr = attrs[i];
		    var track_params;
		    if (attr.attr_id === '# mutations') {
			track_params = {
			    'rule_set_params': {
				'type': 'bar',
				'log_scale': true,
				'value_key': 'attr_val',
			    }
			};
		    } else if (attr.attr_id === 'FRACTION_GENOME_ALTERED') {
			track_params = {
			    'rule_set_params': {
				'type': 'bar',
				'value_key': 'attr_val',
				'value_range': [0,1]
			    }
			};
		    } else {
			track_params = {};
			if (attr.datatype.toLowerCase() === 'number') {
			    track_params['rule_set_params'] = {
				'type': 'bar',
				'value_key': 'attr_val'
			    };
			} else {
			    track_params['rule_set_params'] = {
				'type': 'categorical',
				'category_key': 'attr_val'
			    };
			}
		    }

		    track_params['rule_set_params']['legend_label'] = attr.display_name;
		    track_params['rule_set_params']['exclude_from_legend'] = !State.clinical_track_legends_shown;
		    track_params['label'] = attr.display_name;
		    track_params['description'] = attr.description;
		    track_params['removable'] = true;
		    track_params['removeCallback'] = makeRemoveAttributeHandler(attr);
		    track_params['sortCmpFn'] = (attr.datatype.toLowerCase() === 'number' ? 
						    comparator_utils.numericalClinicalComparator :
						    comparator_utils.stringClinicalComparator);
		    track_params['sort_direction_changeable'] = true;
		    track_params['init_sort_direction'] = 0;
		    track_params['target_group'] = 0;
		    
		    var new_track_id = oncoprint.addTracks([track_params])[0];
		    track_ids.push(new_track_id);
		    State.clinical_tracks[new_track_id] = attr;
		}
		oncoprint.releaseRendering();
		return track_ids;
	    },
	    'addAndPopulateClinicalTracks': function(attrs) {
		var def = new $.Deferred();
		var track_ids = this.addClinicalTracks(attrs);
		var promises = track_ids.map(populateClinicalTrack);
		($.when.apply(null, promises)).then(function() {
		    def.resolve();
		}).fail(function() {
		    def.reject();
		});
		return def.promise();
	    },
	    'getGeneticComparator': function() {
		return comparator_utils.makeGeneticComparator(this.colorby_type && this.sortby_type, this.colorby_recurrence && this.sortby_recurrence);
	    },
	    'getGeneticRuleSetParams': function() {
		if (this.colorby_type) {
		    if (this.colorby_recurrence) {
			return window.geneticrules.genetic_rule_set_different_colors_recurrence;
		    } else {
			return window.geneticrules.genetic_rule_set_different_colors_no_recurrence;
		    }
		} else {
		    if (this.colorby_recurrence) {
			return window.geneticrules.genetic_rule_set_same_color_for_all_recurrence;
		    } else {
			return window.geneticrules.genetic_rule_set_same_color_for_all_no_recurrence;
		    }
		}
	    }
	};
	
	(function loadPatientOrder(state) {
	    if (state.patient_order_loaded.state() === "resolved") {
		return;
	    } else {
		QuerySession.getPatientSampleIdMap().then(function(sample_to_patient) {
		    var patients = QuerySession.getSampleIds().map(function(s) { return sample_to_patient[s];});
		    var patient_added_to_order = {};
		    var patient_order = [];
		    for (var i=0; i<patients.length; i++) {
			if (!patient_added_to_order[patients[i]]) {
			    patient_added_to_order[patients[i]] = true;
			    patient_order.push(patients[i]);
			}
		    }
		    state.patient_order = patient_order;
		    state.patient_order_loaded.resolve();
		});
	    }
	})(State);
	
	return State;
    })();
    
    var ClinicalData = (function() {
	var sample_clinical_data = {};// attr_id -> list of data
	var patient_clinical_data = {};// attr_id -> list of data
	
	var fetchData = function(attr) {
	    var def = new $.Deferred();
	    $.when(QuerySession.getSampleClinicalData([attr.attr_id]), QuerySession.getPatientClinicalData([attr.attr_id]))
		    .then(function (sample_data, patient_data) {
			sample_clinical_data[attr.attr_id] = sample_data;
			patient_clinical_data[attr.attr_id] = patient_data;
			def.resolve();
		    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	};
	return {
	    getSampleData: function(attrs) {
		attrs = [].concat(attrs);
		var def = new $.Deferred();
		var ret = {};
		if (attrs.length === 0) {
		    def.resolve({});
		}
		for (var i = 0; i < attrs.length; i++) {
		    var attr = attrs[i];
		    if (sample_clinical_data.hasOwnProperty(attr.attr_id)) {
			ret[attr.attr_id] = sample_clinical_data[attr.attr_id];
			if (Object.keys(ret).length === attrs.length) {
			    def.resolve(ret);
			}
		    } else {
			fetchData(attr).then((function(_attr) {
			    return function () {
				ret[_attr.attr_id] = sample_clinical_data[_attr.attr_id];
				if (Object.keys(ret).length === attrs.length) {
				    def.resolve(ret);
				}
			    };
			})(attr)).fail(function () {
			    def.reject();
			});
		    }
		}
		return def.promise();
	    },
	    getPatientData: function (attrs) {
		attrs = [].concat(attrs);
		var def = new $.Deferred();
		var ret = {};
		if (attrs.length === 0) {
		    def.resolve({});
		}
		for (var i = 0; i < attrs.length; i++) {
		    var attr = attrs[i];
		    if (patient_clinical_data.hasOwnProperty(attr.attr_id)) {
			ret[attr.attr_id] = patient_clinical_data[attr.attr_id];
			if (Object.keys(ret).length === attrs.length) {
			    def.resolve(ret);
			}
		    } else {
			fetchData(attr).then((function(_attr) {
			    return function () {
				ret[_attr.attr_id] = patient_clinical_data[_attr.attr_id];
				if (Object.keys(ret).length === attrs.length) {
				    def.resolve(ret);
				}
			    };
			})(attr)).fail(function () {
			    def.reject();
			});
		    }
		}
		return def.promise();
	    },
	    NUMBER_MUTATIONS_ATTRIBUTE: {attr_id: "# mutations",
		datatype: "NUMBER",
		description: "Number of mutations",
		display_name: "Total mutations",
		is_patient_attribute: "0"
	    },
	    FRACTION_GENOME_ALTERED_ATTRIBUTE: {attr_id: "FRACTION_GENOME_ALTERED",
		datatype: "NUMBER",
		description: "Fraction Genome Altered",
		display_name: "Fraction Genome Altered",
		is_patient_attribute: "0"
	    },
	
	};
    })();
    
    var Toolbar = (function() {
	var events = [];
	
	return {
	    'addEventHandler': function($elt, evt, callback) {
		$elt.on(evt, callback);
		events.push({'$elt':$elt, 'evt':evt, 'callback':callback});
	    },
	    'onMouseDownAndClick': function($elt, mousedown_callback, click_callback) {
		this.addEventHandler($elt, 'mousedown', mousedown_callback);
		this.addEventHandler($elt, 'click', click_callback);
	    },
	    'onHover': function($elt, enter_callback, leave_callback) {
		this.addEventHandler($elt, 'mouseenter', enter_callback);
		this.addEventHandler($elt, 'mouseleave', leave_callback);
	    },
	    'onClick': function($elt, callback) {
		this.addEventHandler($elt, 'click', callback);
	    },
	    'destroy': function() {
		// Destroy events
		for (var i=0; i<events.length; i++) {
		    var event = events[i];
		    event['$elt'].off(event['evt'], event['callback']);
		}
		
		// Destroy qtips
		
		// Destroy elements
	    },
	    'refreshClinicalAttributeSelector': function() {
		var attributes_to_populate = State.unused_clinical_attributes;
		attributes_to_populate.sort(function(attrA, attrB) {
		    return attrA.display_order - attrB.display_order;
		});
		var $selector = $(toolbar_selector + ' #select_clinical_attributes');
		$selector.empty();
		for (var i = 0; i < attributes_to_populate.length; i++) {
		    $("<option></option>").appendTo($selector)
			.attr("value", attributes_to_populate[i].attr_id)
			.text(attributes_to_populate[i].display_name);
		}
		$(toolbar_selector + " #select_clinical_attributes").val('');
		$(toolbar_selector + " #select_clinical_attributes").trigger("liszt:updated");
		$(toolbar_selector + " #select_clinical_attributes_chzn").addClass("chzn-with-drop");
	    }
	};
    })();

    State.clinical_attributes_fetched.then(function () {
	State.unused_clinical_attributes.sort(function (attrA, attrB) {
	    if (attrA.attr_id === "FRACTION_GENOME_ALTERED") {
		return -1;
	    } else if (attrA.attr_id === '# mutations') {
		return (attrB.attr_id === "FRACTION_GENOME_ALTERED" ? 1 : -1);
	    } else if (attrB.attr_id === "FRACTION_GENOME_ALTERED") {
		return 1;
	    } else if (attrB.attr_id === '# mutations') {
		return (attrA.attr_id === 'FRACTION_GENOME_ALTERED' ? -1 : 1);
	    } else {
		return attrA.display_name.localeCompare(attrB.display_name);
	    }
	});

	for (var i = 0, _len = State.unused_clinical_attributes.length; i < _len; i++) {
	    State.unused_clinical_attributes[i].display_order = i;
	}
	
	var url_clinical_attr_ids = URL.getInitUsedClinicalAttrs() || [];
	for (var i=0; i<url_clinical_attr_ids.length; i++) {
	    State.useAttribute(url_clinical_attr_ids[i]);
	}
	
	Toolbar.refreshClinicalAttributeSelector();
	$(toolbar_selector + ' #select_clinical_attributes').chosen({width: "330px", "font-size": "12px", search_contains: true});
	// add a title to the text input fields generated by Chosen for
	// Section 508 accessibility compliance
	$("div.chzn-search > input:first-child").attr("title", "Search");

	Toolbar.onClick($(toolbar_selector + ' #select_clinical_attributes_chzn .chzn-search input'), function(e) { e.stopPropagation(); });
	
	$(toolbar_selector + " #select_clinical_attributes_chzn").mouseenter(function () {
	    $(toolbar_selector + " #select_clinical_attributes_chzn .chzn-search input").focus();
	});
	$(toolbar_selector + " #select_clinical_attributes_chzn").addClass("chzn-with-drop");
	
	Toolbar.addEventHandler($(toolbar_selector + ' #select_clinical_attributes'), 'change', function(evt) {
	    if ($(toolbar_selector + ' #select_clinical_attributes').val().trim() === '') {
		evt && evt.stopPropagation();
		return;
	    }
	    var attr_id = $(toolbar_selector + ' #select_clinical_attributes option:selected').attr("value");
	    $(toolbar_selector + ' #select_clinical_attributes').val('').trigger('liszt:updated');
	    $(toolbar_selector + ' #clinical_dropdown').dropdown('toggle');
	    addClinicalAttributeTrack(attr_id);
	});
    });
    
    var addClinicalAttributeTrack = function(attr_id) {
	$(toolbar_selector + ' #oncoprint-diagram-showlegend-icon').show();
	var index = State.unused_clinical_attributes.findIndex(function(attr) {
	    return attr.attr_id === attr_id;
	});
	if (index === -1) {
	    return;
	}
	var attr = State.unused_clinical_attributes[index];
	State.useAttribute(attr_id);
	Toolbar.refreshClinicalAttributeSelector();
	
	return State.addAndPopulateClinicalTracks(attr);
    };

    (function initOncoprint() {
	LoadingBar.show();
	LoadingBar.msg(LoadingBar.DOWNLOADING_MSG);
	var def = new $.Deferred();
	oncoprint.setCellPaddingOn(State.cell_padding_on);
	$.when(QuerySession.getWebServiceGenomicEventData(), QuerySession.getOncoprintSampleGenomicEventData()).then(function (ws_data, oncoprint_data) {
	    State.addGeneticTracks(oncoprint_data);
	}).fail(function() {
	    def.reject();
	}).then(function() {
	    var url_clinical_attrs = URL.getInitUsedClinicalAttrs() || [];
	    if (url_clinical_attrs.length > 0) {
		$(toolbar_selector + ' #oncoprint-diagram-showlegend-icon').show();
		var attr_ids_to_query = [];
		var local_attrs = [];
		for (var i=0; i<url_clinical_attrs.length; i++) {
		    if (url_clinical_attrs[i] === '# mutations') {
			local_attrs.push(ClinicalData.NUMBER_MUTATIONS_ATTRIBUTE);
		    } else if (url_clinical_attrs[i] === 'FRACTION_GENOME_ALTERED') {
			local_attrs.push(ClinicalData.FRACTION_GENOME_ALTERED_ATTRIBUTE);
		    } else {
			attr_ids_to_query.push(url_clinical_attrs[i]);
		    }
		}
		cbioportal_client.getClinicalAttributes({'attr_ids':attr_ids_to_query}).then(function(attrs) {
		    State.addClinicalTracks(attrs.concat(local_attrs));
		    def.resolve();
		});
	    } else {
		def.resolve();
	    }
	}).fail(function() {
	    def.reject();
	});
	return def.promise();
    })().then(function() {
        var populate_data_promise = State.setDataType(State.using_sample_data ? 'sample' : 'patient');
	    
        $.when(QuerySession.getPatientIds(), QuerySession.getAlteredSamples(), QuerySession.getAlteredPatients(), QuerySession.getCaseUIDMap(), populate_data_promise).then(function(patient_ids, altered_samples, altered_patients, case_uid_map) {
	    if ((State.using_sample_data ? window.QuerySession.getSampleIds() : patient_ids).length > 200) {
		// TODO: assume multiple studies
		var study_id = QuerySession.getCancerStudyIds()[0];
		var getUID = function(id) {
		    return case_uid_map[study_id][id];
		};
		oncoprint.setHorzZoomToFit(State.using_sample_data ? altered_samples.map(getUID) : altered_patients.map(getUID));
	    }
	    oncoprint.scrollTo(0);
	});
	
	return populate_data_promise;
    }).then(function() {
	(function fetchClinicalAttributes() {
	    // For some reason $.when isn't working
	    QuerySession.getClinicalAttributes().then(function(attrs) {
		State.unused_clinical_attributes = attrs;
		State.clinical_attributes_fetched.resolve();
	    }).fail(function() {
		State.clinical_attributes_fetched.reject();
	    });
	})();
    });
    window.oncoprint = oncoprint;

    (function setUpToolbar() {
	var zoom_discount = 0.7;
	var to_remove_on_destroy = [];
	var to_remove_qtip_on_destroy = [];


	var appendTo = function ($elt, $target) {
	    $elt.appendTo($target);
	    to_remove_on_destroy.push($elt);
	};
	var addQTipTo = function ($elt, qtip_params) {
	    $elt.qtip(qtip_params);
	    to_remove_qtip_on_destroy.push($elt);
	};
	
	var setUpHoverEffect = function ($elt) {
	    $elt.hover(function () {
		$(this).css({'fill': '#0000FF',
		    'font-size': '18px',
		    'cursor': 'pointer'});
	    },
		    function () {
			$(this).css({'fill': '#87CEFA',
			    'font-size': '12px'});
		    }
	    );
	};


	var setUpButton = function ($elt, img_urls, qtip_descs, index_fn, callback) {
	    index_fn = index_fn || function() { return 0; };
	    var updateButton = function () {
		if (img_urls.length > 0) {
		    $elt.find('img').attr('src', img_urls[index_fn()]);
		}
		$elt.css({'background-color':'#efefef'});
	    };
	    var hoverButton = function () {
		if (img_urls.length > 0) {
		    $elt.find('img').attr('src', img_urls[(index_fn() + 1) % img_urls.length]);
		}
		$elt.css({'background-color':'#d9d9d9'});
	    };
	    if (qtip_descs.length > 0) {
		addQTipTo($elt, {
		    content: {text: function () {
			    return qtip_descs[index_fn()];
			}},
		    position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		    style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		    show: {event: "mouseover"},
		    hide: {fixed: true, delay: 100, event: "mouseout"}
		});
	    }
	    Toolbar.onHover($elt, function() {
		hoverButton();
	    }, function() {
		updateButton();
	    });
	    Toolbar.onMouseDownAndClick($elt, function() {
		$elt.css({'background-color':'#c7c7c7'});
	    },
	    function() {
		callback();
		updateButton();
	    });
	    updateButton();
	};
	var $zoom_slider = (function setUpZoom() {
	    var zoom_elt = $(toolbar_selector + ' #oncoprint_diagram_slider_icon');
	    var $slider = $('<input>', {
		id: "oncoprint_zoom_slider",
		type: "range",
		min: 0,
		max: 1,
		step: 0.0001,
		value: 1,
		change: function (evt) {
		    if (evt.originalEvent) {
			this.value = oncoprint.setHorzZoom(parseFloat(this.value));
		    } else {
			this.value = oncoprint.getHorzZoom();
		    }
		},
	    });
	    
	    $('#oncoprint_zoom_scale_input').on("keypress", function(e) {
		if (e.keyCode === 13) {
		    // 'Enter' key
		    var new_zoom = parseFloat($('#oncoprint_zoom_scale_input').val())/100;
		    new_zoom = Math.min(1, new_zoom);
		    new_zoom = Math.max(0, new_zoom);
		    oncoprint.setHorzZoom(new_zoom);
		}
	    });
	    oncoprint.onHorzZoom(function() {
		$zoom_slider.trigger('change');
		$('#oncoprint_zoom_scale_input').val(Math.round(10000*oncoprint.getHorzZoom())/100);
	    });

	    appendTo($slider, zoom_elt);
	    addQTipTo($slider, {
		id: 'oncoprint_zoom_slider_tooltip',
		prerender: true,
		content: {text: 'Zoom in/out of oncoprint'},
		position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		show: {event: "mouseover"},
		hide: {fixed: true, delay: 100, event: "mouseout"}
	    });
	    // use aria-labelledby instead of aria-describedby, as Section 508
	    // requires that inputs have an explicit label for accessibility
	    $slider.attr('aria-labelledby', 'qtip-oncoprint_zoom_slider_tooltip');
	    $slider.removeAttr('aria-describedby');
	    setUpHoverEffect($slider);

	    setUpButton($(toolbar_selector + ' #oncoprint_zoomout'), [], ["Zoom out of oncoprint"], null, function () {
		oncoprint.setHorzZoom(oncoprint.getHorzZoom()*zoom_discount);
	    });
	    setUpButton($(toolbar_selector + ' #oncoprint_zoomin'), [], ["Zoom in to oncoprint"], null, function () {
		oncoprint.setHorzZoom(oncoprint.getHorzZoom()/zoom_discount);
	    });

	    return $slider;
	})();
	
	(function setUpToggleCellPadding() {
	    var $show_whitespace_checkbox = $(toolbar_selector).find('#oncoprint_diagram_view_menu')
		    .find('input[type="checkbox"][name="show_whitespace"]');
	    $show_whitespace_checkbox[0].checked = State.cell_padding_on;
	    $show_whitespace_checkbox.change(function() {
		State.cell_padding_on = $show_whitespace_checkbox.is(":checked");
		oncoprint.setCellPaddingOn(State.cell_padding_on);
	    });
	})();
	(function setUpHideUnalteredCases() {
	    $.when(QuerySession.getUnalteredSamples(), QuerySession.getUnalteredPatients()).then(function (unaltered_samples, unaltered_patients) {
		var $show_unaltered_checkbox = $(toolbar_selector).find('#oncoprint_diagram_view_menu')
		    .find('input[type="checkbox"][name="show_unaltered"]');
		$show_unaltered_checkbox[0].checked = !State.unaltered_cases_hidden;
		$show_unaltered_checkbox.change(function() {
		    State.unaltered_cases_hidden = !($show_unaltered_checkbox.is(":checked"));
		    if (State.unaltered_cases_hidden) {
			oncoprint.hideIds((State.using_sample_data ? unaltered_samples : unaltered_patients), true);
		    } else {
			oncoprint.hideIds([], true);
		    }
		});
	    });
	})();
	(function setUpZoomToFit() {
	    setUpButton($(toolbar_selector + ' #oncoprint_zoomtofit'), [], ["Zoom to fit altered cases in screen"], null, function () {
		$.when(QuerySession.getAlteredSamples(), QuerySession.getAlteredPatients(), QuerySession.getCaseUIDMap()).then(function (altered_samples, altered_patients, case_uid_map) {
		    // TODO: assume multiple studies
		    var study_id = QuerySession.getCancerStudyIds()[0];
		    var getUID = function (id) {
			return case_uid_map[study_id][id];
		    };
		    oncoprint.setHorzZoomToFit(State.using_sample_data ? altered_samples.map(getUID) : altered_patients.map(getUID));
		    oncoprint.scrollTo(0);
		});
	    });
	})();
	(function setUpSortByAndColorBy() {
	    $('#oncoprint_diagram_showmutationcolor_icon').hide();
	    var updateSortByForm = function() {
		var sortby_type_checkbox = $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="type"]');;
		var sortby_recurrence_checkbox = $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="recurrence"]');
		if ((State.sortby !== "data") || !State.colorby_type) {
		    sortby_type_checkbox.attr("disabled","disabled");
		} else {
		    sortby_type_checkbox.removeAttr("disabled");
		}
		
		if ((State.sortby !== "data") || !State.colorby_recurrence) {
		    sortby_recurrence_checkbox.attr("disabled","disabled");
		} else {
		    sortby_recurrence_checkbox.removeAttr("disabled");
		}
	    };
	    
	    var updateRuleSets = function() {
		var rule_set_params = State.getGeneticRuleSetParams();
		var genetic_alteration_track_ids = utils.objectValues(State.genetic_alteration_tracks);
		oncoprint.setRuleSet(genetic_alteration_track_ids[0], rule_set_params);
		for (var i = 1; i < genetic_alteration_track_ids.length; i++) {
		    oncoprint.shareRuleSet(genetic_alteration_track_ids[0], genetic_alteration_track_ids[i]);
		}
	    };
	    var updateSortComparators = function() {
		var comparator = State.getGeneticComparator();
		oncoprint.keepSorted(false);
		var genetic_alteration_track_ids = utils.objectValues(State.genetic_alteration_tracks);
		for (var i = 0; i < genetic_alteration_track_ids.length; i++) {
		    oncoprint.setTrackSortComparator(genetic_alteration_track_ids[i], comparator);
		}
		oncoprint.keepSorted();
	    };
	    var updateSortConfig = function() {
		if (State.sortby === "data") {
		    oncoprint.setSortConfig({'type':'tracks'});
		    State.sorting_by_given_order = false;
		    State.sorting_alphabetically = false;
		} else if (State.sortby === "id") {
		    State.sorting_by_given_order = false;
		    State.sorting_alphabetically = true;
		    // TODO: assume multiple studies
		    $.when(QuerySession.getCaseUIDMap(), State.patient_order_loaded).then(function (case_uid_map) {
			var study_id = QuerySession.getCancerStudyIds()[0];
			var getUID = function (id) {
			    return case_uid_map[study_id][id];
			};
			oncoprint.setSortConfig({'type': 'order', order: (State.using_sample_data ? QuerySession.getSampleIds().slice().sort().map(getUID) : State.patient_order.slice().sort().map(getUID))});
		    });
		} else if (State.sortby === "custom") {
		    State.sorting_by_given_order = true;
		    State.sorting_alphabetically = false;
		    // TODO: assume multiple studies
		    $.when(QuerySession.getCaseUIDMap(), State.patient_order_loaded).then(function (case_uid_map) {
			var study_id = QuerySession.getCancerStudyIds()[0];
			var getUID = function (id) {
			    return case_uid_map[study_id][id];
			};
			oncoprint.setSortConfig({'type': 'order', order: (State.using_sample_data ? QuerySession.getSampleIds().map(getUID) : State.patient_order.map(getUID))});
		    });
		}
	    };
	    $('#oncoprint_diagram_sortby_group').find('input[name="sortby"]').change(function() {
		State.sortby = $('#oncoprint_diagram_sortby_group').find('input[name="sortby"]:checked').val();
		updateSortByForm();
		updateSortConfig();
	    });
	    $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="type"]').change(function() {
		State.sortby_type = $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="type"]').is(":checked");
		updateSortComparators();
	    });
	    $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="recurrence"]').change(function() {
		State.sortby_recurrence = $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="recurrence"]').is(":checked");
		updateSortComparators();
	    });
	    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"]').change(function() {
		State.colorby_type = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="type"]').is(":checked");
		State.colorby_recurrence = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="recurrence"]').is(":checked");
		updateSortByForm();
		updateRuleSets();
	    });
	    (function initFormsFromState() {
		$('#oncoprint_diagram_sortby_group').find('input[name="sortby"][value="'+State.sortby+'"]').prop("checked", true);
		$('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="type"]').prop("checked", State.sortby_type);
		$('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="recurrence"]').prop("checked", State.sortby_recurrence);
		
		$('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="type"]').prop("checked", State.colorby_type);
		$('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="recurrence"]').prop("checked", State.colorby_recurrence);
		
		updateSortByForm();
	    })();
	})();
	(function setUpShowClinicalLegendsBtn() {
	    // set initial state
	    var $show_clinical_legends_checkbox = $(toolbar_selector).find('#oncoprint_diagram_view_menu')
		    .find('input[type="checkbox"][name="show_clinical_legends"]');
	    $show_clinical_legends_checkbox[0].checked = State.clinical_track_legends_shown;
	    $show_clinical_legends_checkbox.change(function() {
		State.clinical_track_legends_shown = $show_clinical_legends_checkbox.is(":checked");
		var clinical_track_ids = Object.keys(State.clinical_tracks);
		if (State.clinical_track_legends_shown) {
		    oncoprint.showTrackLegends(clinical_track_ids);
		} else {
		    oncoprint.hideTrackLegends(clinical_track_ids);
		}
	    });
	})();
	(function setUpTogglePatientSampleBtn() {
	    var $header_btn = $('#switchPatientSample');
	    
	    $header_btn.click(function() {
		$(toolbar_selector).find('#oncoprint_diagram_view_menu')
				   .find('input[type="radio"][name="datatype"]').trigger('change');
	    });
	    
	    addQTipTo($header_btn, {
		content: {text: function () {
			if (State.using_sample_data) {
			    return 'Each sample for each patient is in a separate column. Click to show only one column per patient'
			} else {
			    return 'All samples from a patient are merged into one column. Click to split samples into multiple columns.';
			}
		    }},
		position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		show: {event: "mouseover"},
		hide: {fixed: true, delay: 100, event: "mouseout"}
	    });
	    
	    var updateHeaderBtnText = function() {
		if (State.using_sample_data) {
		    $header_btn.text('Show only one column per patient');
		} else {
		    $header_btn.text('Show all samples');
		}
	    };
	    
	    var updateDownloadIdOrderText = function() {
		$('oncoprint-sample-download').text((State.using_sample_data ? "Sample" : "Patient") + " order");
	    };
	    
	    updateHeaderBtnText();
	    updateDownloadIdOrderText();
	    
	    // initialize radio buttons
	    $(toolbar_selector).find('#oncoprint_diagram_view_menu')
				   .find('input[type="radio"][name="datatype"][value="'+
				   (State.using_sample_data ? "sample" : "patient") + '"]')
				   .prop("checked", true);
	    
	    $(toolbar_selector).find('#oncoprint_diagram_view_menu')
				   .find('input[type="radio"][name="datatype"]').change(function(e) {
		State.using_sample_data = $(toolbar_selector).find('#oncoprint_diagram_view_menu')
				   .find('input[type="radio"][name="datatype"]:checked').val() === 'sample';
		if (State.using_sample_data) {
		    State.setDataType('sample');
		} else {
		    State.setDataType('patient');
		}
		updateHeaderBtnText();
		updateDownloadIdOrderText();
	    });
	})();
	(function setUpDownload() {
	    $('body').on('click', '.oncoprint-diagram-download', function () {
		var fileType = $(this).attr("type");
		var two_megabyte_limit = 2000000;
		if (fileType === 'pdf')
		{
		    var svg = oncoprint.toSVG(true);
		    var serialized = cbio.download.serializeHtml(svg);
		    if (serialized.length > two_megabyte_limit) {
			alert("Oncoprint too big to download as PDF - please download as SVG");
			return;
		    }
		    cbio.download.initDownload(serialized, {
			filename: "oncoprint.pdf",
			contentType: "application/pdf",
			servletName: "svgtopdf.do"
		    });
		} else if (fileType === 'svg')
		{
		    cbio.download.initDownload(oncoprint.toSVG(), {filename: "oncoprint.svg"});
		} else if (fileType === 'png')
		{
		    var img = oncoprint.toCanvas(function (canvas, truncated) {
			canvas.toBlob(function (blob) {
			    if (truncated) {
				alert("Oncoprint too large - PNG truncated to " + canvas.getAttribute("width") + " by " + canvas.getAttribute("height"));
			    }
			    saveAs(blob, "oncoprint.png");
			}, 'image/png');
		    }, 2);
		}
	    });

	    $('body').on('click', '.oncoprint-sample-download', function () {
		var idTypeStr = (State.using_sample_data ? "Sample" : "Patient");
		var content = idTypeStr + " order in the Oncoprint is: \n";
		content += oncoprint.getIdOrder().join('\n');
		var downloadOpts = {
		    filename: 'OncoPrint' + idTypeStr + 's.txt',
		    contentType: "text/plain;charset=utf-8",
		    preProcess: false};

		// send download request with filename & file content info
		cbio.download.initDownload(content, downloadOpts);
	    });
	})();
    })();
}

window.CreateOncoprinterWithToolbar = function (ctr_selector, toolbar_selector) {
    
    $('#oncoprint #everything').show();
    $('#oncoprint #oncoprint-diagram-toolbar-buttons').show();
    
    $(ctr_selector).css({'position':'relative'});
    
    var LoadingBar = (function() {
	var $loading_bar_svg = $('<svg width="100" height="50"></svg><br>')
				.appendTo(ctr_selector)
				.append(utils.makeSVGElement("rect", {
							    "width":100, 
							    "height":25, 
							    "stroke":"black",
							    "fill":"white"
							}));
	$loading_bar_svg.append(utils.makeSVGElement("rect", {
							    "width":100, 
							    "height":25, 
							    "stroke":"black",
							    "fill":"white"
							}));
	var $loading_bar = $(utils.makeSVGElement("rect", {
							"width":0, 
							"height":25, 
							"fill":"green", 
							"stroke":"dark green"}))
				.appendTo($loading_bar_svg);
	var $loading_bar_msg = $(utils.makeSVGElement("text", {
					    'x': 2,
					    'y':15,
					    'font-size':11,
					    'font-family':'Arial',
					    'font-weight':'normal',
					    'text-anchor':'start',
					}))
		.appendTo($loading_bar_svg);

	return {
	    'hide': function() {
		$loading_bar_svg.hide();
	    },
	    'show': function() {
		$loading_bar_svg.show();
	    },
	    'msg': function(str) {
		$loading_bar_msg[0].textContent = str;
	    },
	    'update': function(proportion) {
		$loading_bar.attr('width', proportion*parseFloat($loading_bar_svg.attr('width')));
	    },
	    'DOWNLOADING_MSG': 'Downloading data..'
	};
    })();
    
    LoadingBar.hide();
    
    var oncoprint = new window.Oncoprint(ctr_selector, 1050);
    var toolbar_fade_out_timeout;
    $(ctr_selector).add(toolbar_selector).on("mouseover", function(evt) {
	$(toolbar_selector).fadeIn('fast');
	clearTimeout(toolbar_fade_out_timeout);
    });
    $(ctr_selector).add(toolbar_selector).on("mouseleave", function(evt) {
	clearTimeout(toolbar_fade_out_timeout);
	toolbar_fade_out_timeout = setTimeout(function() {
	    $(toolbar_selector).fadeOut();
	}, 700);
    });
    
    var State = (function () {
	
	var setSortOrder = function(order) {
	    oncoprint.setSortConfig({'type':'order', 'order':order});
	};
	
	var getPercent = function(proportion) {
	    return Math.round(proportion*100) + '%';
	}
	
	return {
	    'first_genetic_alteration_track': null,
	    'genetic_alteration_tracks': {}, // track_id -> gene

	    'cell_padding_on': true,
	    'unaltered_cases_hidden': false,
	    'mutations_colored_by_type': true,
	    'sorted_by_mutation_type': true,
	    
	    'user_specified_order': null,
	    
	    'altered_ids': [],
	    'unaltered_ids': [],
	    'ids':[],
	    
	    
	    'addGeneticTracks': function (genes) {
		genes = [].concat(genes);
		oncoprint.suppressRendering();
		var track_ids = [];
		for (var i = 0; i < genes.length; i++) {
		    var track_params = {
			'rule_set_params': (this.mutations_colored_by_type ? 
					    window.geneticrules.genetic_rule_set_different_colors_no_recurrence : 
					    window.geneticrules.genetic_rule_set_same_color_for_all_no_recurrence),
			'label': genes[i],
			'target_group': 1,
			'sortCmpFn': comparator_utils.makeGeneticComparator(this.sorted_by_mutation_type),
			'tooltipFn': tooltip_utils.makeGeneticTrackTooltip('sample', false),
		    };
		    var new_track_id = oncoprint.addTracks([track_params])[0];
		    track_ids.push(new_track_id);
		    State.genetic_alteration_tracks[new_track_id] = genes[i];
		    if (State.first_genetic_alteration_track === null) {
			State.first_genetic_alteration_track = new_track_id;
		    } else {
			oncoprint.shareRuleSet(State.first_genetic_alteration_track, new_track_id);
		    }
		}
		oncoprint.releaseRendering();
		return track_ids;
	    },
	    setData: function(data_by_gene, id_key, altered_ids_by_gene, id_order, gene_order) {
		if (id_order) {
		    oncoprint.setSortConfig({'type':'order', 'order':id_order});
		} else {
		    oncoprint.setSortConfig({'type': 'tracks'});
		}
		
		LoadingBar.show();
		LoadingBar.update(0);
		
		oncoprint.removeAllTracks();
		this.first_genetic_alteration_track = null;
		this.genetic_alteration_tracks = {};
		this.addGeneticTracks(Object.keys(data_by_gene));
		
		var present_ids = {};
		for (var gene in data_by_gene) {
		    if (data_by_gene.hasOwnProperty(gene)) {
			var data = data_by_gene[gene];
			for (var i=0; i<data.length; i++) {
			    present_ids[data[i][id_key]] = true;
			}
		    }
		}
		this.ids = Object.keys(present_ids);
		
		var altered_percentage_by_gene = {};
		for (var gene in altered_ids_by_gene) {
		    if (altered_ids_by_gene.hasOwnProperty(gene)) {
			altered_percentage_by_gene[gene] = Math.round(100*altered_ids_by_gene[gene].length/this.ids.length);
		    }
		}
		
		var altered_ids = {};
		for (var gene in altered_ids_by_gene) {
		    if (altered_ids_by_gene.hasOwnProperty(gene)) {
			var _altered_ids = altered_ids_by_gene[gene];
			for (var i=0; i<_altered_ids.length; i++) {
			    altered_ids[_altered_ids[i]] = true;
			}
		    }
		}
		this.altered_ids = Object.keys(altered_ids);
		
		this.unaltered_ids = [];
		for (var i=0; i<this.ids.length; i++) {
		    if (!altered_ids[this.ids[i]]) {
			this.unaltered_ids.push(this.ids[i]);
		    }
		}
		
		oncoprint.suppressRendering();
		oncoprint.keepSorted(false);
		var tracks_done = 0;
		var tracks_total = Object.keys(this.genetic_alteration_tracks).length;
		for (var track_id in this.genetic_alteration_tracks) {
		    if (this.genetic_alteration_tracks.hasOwnProperty(track_id)) {
			var gene = this.genetic_alteration_tracks[track_id];
			oncoprint.setTrackData(track_id, data_by_gene[gene], id_key);
			oncoprint.setTrackInfo(track_id, altered_percentage_by_gene[gene] + '%');
			LoadingBar.update(tracks_done / tracks_total);
		    }
		}
		
		if (gene_order) {
		    var gene_to_track = {};
		    for (var track_id in this.genetic_alteration_tracks) {
			if (this.genetic_alteration_tracks.hasOwnProperty(track_id)) {
			    gene_to_track[this.genetic_alteration_tracks[track_id]] = parseInt(track_id,10);
			}
		    }
		    for (var i=0; i<gene_order.length; i++) {
			var gene = gene_order[i];
			if (i === 0) {
			    oncoprint.moveTrack(gene_to_track[gene], null);
			} else {
			    var prev_gene = gene_order[i-1];
			    oncoprint.moveTrack(gene_to_track[gene], gene_to_track[prev_gene]);
			}
		    }
		}
		oncoprint.keepSorted();
		oncoprint.releaseRendering();
		
		oncoprint.setHorzZoomToFit(this.altered_ids);
		oncoprint.scrollTo(0);
		
		LoadingBar.hide();
	    },
	    getIds: function() {
		return this.ids.slice();
	    },
	    getUnalteredIds: function() {
		return unaltered_ids;
	    },
	};
    })();
    
    var Toolbar = (function() {
	var events = [];
	var qtips = [];
	var elements = [];
	
	return {
	    'addEventHandler': function($elt, evt, callback) {
		$elt.on(evt, callback);
		events.push({'$elt':$elt, 'evt':evt, 'callback':callback});
	    },
	    'onMouseDownAndClick': function($elt, mousedown_callback, click_callback) {
		this.addEventHandler($elt, 'mousedown', mousedown_callback);
		this.addEventHandler($elt, 'click', click_callback);
	    },
	    'onHover': function($elt, enter_callback, leave_callback) {
		this.addEventHandler($elt, 'mouseenter', enter_callback);
		this.addEventHandler($elt, 'mouseleave', leave_callback);
	    },
	    'onClick': function($elt, callback) {
		this.addEventHandler($elt, 'click', callback);
	    },
	    'destroy': function() {
		// Destroy events
		for (var i=0; i<events.length; i++) {
		    var event = events[i];
		    event['$elt'].off(event['evt'], event['callback']);
		}
		
		// Destroy qtips
		
		// Destroy elements
	    },
	};
    })();
    
    oncoprint.setCellPaddingOn(State.cell_padding_on);

    (function setUpToolbar() {
	var zoom_discount = 0.7;
	var to_remove_on_destroy = [];
	var to_remove_qtip_on_destroy = [];


	var appendTo = function ($elt, $target) {
	    $elt.appendTo($target);
	    to_remove_on_destroy.push($elt);
	};
	var addQTipTo = function ($elt, qtip_params) {
	    $elt.qtip(qtip_params);
	    to_remove_qtip_on_destroy.push($elt);
	};
	
	var setUpHoverEffect = function ($elt) {
	    $elt.hover(function () {
		$(this).css({'fill': '#0000FF',
		    'font-size': '18px',
		    'cursor': 'pointer'});
	    },
		    function () {
			$(this).css({'fill': '#87CEFA',
			    'font-size': '12px'});
		    }
	    );
	};

	var setUpButton = function ($elt, img_urls, qtip_descs, index_fn, callback) {
	    index_fn = index_fn || function() { return 0; };
	    var updateButton = function () {
		if (img_urls.length > 0) {
		    $elt.find('img').attr('src', img_urls[index_fn()]);
		}
		$elt.css({'background-color':'#efefef'});
	    };
	    var hoverButton = function () {
		if (img_urls.length > 0) {
		    $elt.find('img').attr('src', img_urls[(index_fn() + 1) % img_urls.length]);
		}
		$elt.css({'background-color':'#d9d9d9'});
	    };
	    if (qtip_descs.length > 0) {
		addQTipTo($elt, {
		    content: {text: function () {
			    return qtip_descs[index_fn()];
			}},
		    position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		    style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		    show: {event: "mouseover"},
		    hide: {fixed: true, delay: 100, event: "mouseout"}
		});
	    }
	    Toolbar.onHover($elt, function() {
		hoverButton();
	    }, function() {
		updateButton();
	    });
	    Toolbar.onMouseDownAndClick($elt, function() {
		$elt.css({'background-color':'#c7c7c7'});
	    },
	    function() {
		callback();
		updateButton();
	    });
	    updateButton();
	};
	var $zoom_slider = (function setUpZoom() {
	    var zoom_elt = $(toolbar_selector + ' #oncoprint_diagram_slider_icon');
	    var $slider = $('<input>', {
		id: "oncoprint_zoom_slider",
		type: "range",
		min: 0,
		max: 1,
		step: 0.0001,
		value: 1,
		change: function (evt) {
		    if (evt.originalEvent) {
			this.value = oncoprint.setHorzZoom(parseFloat(this.value));
		    } else {
			this.value = oncoprint.getHorzZoom();
		    }
		},
	    });
	    
	    $('#oncoprint_zoom_scale_input').on("keypress", function(e) {
		if (e.keyCode === 13) {
		    // 'Enter' key
		    var new_zoom = parseFloat($('#oncoprint_zoom_scale_input').val())/100;
		    new_zoom = Math.min(1, new_zoom);
		    new_zoom = Math.max(0, new_zoom);
		    oncoprint.setHorzZoom(new_zoom);
		}
	    });
	    oncoprint.onHorzZoom(function() {
		$zoom_slider.trigger('change');
		$('#oncoprint_zoom_scale_input').val(Math.round(10000*oncoprint.getHorzZoom())/100);
	    });

	    appendTo($slider, zoom_elt);
	    addQTipTo($slider, {
		id: 'oncoprint_zoom_slider_tooltip',
		prerender: true,
		content: {text: 'Zoom in/out of oncoprint'},
		position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		show: {event: "mouseover"},
		hide: {fixed: true, delay: 100, event: "mouseout"}
	    });
	    // use aria-labelledby instead of aria-describedby, as Section 508
	    // requires that inputs have an explicit label for accessibility
	    $slider.attr('aria-labelledby', 'qtip-oncoprint_zoom_slider_tooltip');
	    $slider.removeAttr('aria-describedby');
	    setUpHoverEffect($slider);

	    setUpButton($(toolbar_selector + ' #oncoprint_zoomout'), [], ["Zoom out of oncoprint"], null, function () {
		oncoprint.setHorzZoom(oncoprint.getHorzZoom()*zoom_discount);
	    });
	    setUpButton($(toolbar_selector + ' #oncoprint_zoomin'), [], ["Zoom in to oncoprint"], null, function () {
		oncoprint.setHorzZoom(oncoprint.getHorzZoom()/zoom_discount);
	    });

	    return $slider;
	})();
	
	(function setUpSortBySelector() {
	    $(toolbar_selector + ' #by_data_a').click(function () {
		oncoprint.setSortConfig({'type':'tracks'});
		State.sorting_by_given_order = false;
	    });
	    $(toolbar_selector + ' #alphabetically_first_a').click(function() {
		oncoprint.setSortConfig({'type':'alphabetical'});
		State.sorting_by_given_order = false;
	    });
	    $(toolbar_selector + ' #user_defined_first_a').click(function() {
		State.sorting_by_given_order = true;
		State.patient_order_loaded.then(function() {
		    oncoprint.setSortConfig({'type':'order', order: State.user_specified_order});
		});
	    });
	})();
	
	
	(function setUpToggleCellPadding() {
	    setUpButton($(toolbar_selector + ' #oncoprint-diagram-removeWhitespace-icon'),
		    ['images/unremoveWhitespace.svg','images/removeWhitespace.svg'],
		    ["Remove whitespace between columns", "Show whitespace between columns"],
		    function () {
			return (State.cell_padding_on ? 0 : 1);
		    },
		    function () {
			State.cell_padding_on = !State.cell_padding_on;
			oncoprint.setCellPaddingOn(State.cell_padding_on);
		    });
	})();
	(function setUpHideUnalteredCases() {
	    setUpButton($(toolbar_selector + ' #oncoprint-diagram-removeUCases-icon'),
		    ['images/unremoveUCases.svg', 'images/removeUCases.svg'],
		    ['Hide unaltered cases', 'Show unaltered cases'],
		    function () {
			return (State.unaltered_cases_hidden ? 1 : 0);
		    },
		    function () {
			State.unaltered_cases_hidden = !State.unaltered_cases_hidden;
			if (State.unaltered_cases_hidden) {
			    oncoprint.hideIds(State.unaltered_ids, true);
			} else {
			    oncoprint.hideIds([], true);
			}
		    });
	})();
	(function setUpZoomToFit() {
	    setUpButton($(toolbar_selector + ' #oncoprint_zoomtofit'), [], ["Zoom to fit altered cases in screen"], null, function () {
		oncoprint.setHorzZoomToFit(State.altered_ids);
		oncoprint.scrollTo(0);
	    });
	})();
	(function setUpChangeMutationRuleSet() {
	    $('#oncoprint_diagram_showmutationcolor_icon').show();
	    $('#oncoprint_diagram_mutation_color').hide();
	    var setGeneticAlterationTracksRuleSet = function(rule_set_params) {
		var genetic_alteration_track_ids = Object.keys(State.genetic_alteration_tracks);
		oncoprint.setRuleSet(genetic_alteration_track_ids[0], rule_set_params);
		for (var i = 1; i < genetic_alteration_track_ids.length; i++) {
		    oncoprint.shareRuleSet(genetic_alteration_track_ids[0], genetic_alteration_track_ids[i]);
		}
	    };
	    
	    setUpButton($(toolbar_selector + ' #oncoprint_diagram_showmutationcolor_icon'),
		    ['images/colormutations.svg', 'images/uncolormutations.svg','images/mutationcolorsort.svg'],
		    ['Show all mutations with the same color', 'Color-code mutations but don\'t sort by type', 'Color-code mutations and sort by type', ],
		    function () {
			if (State.mutations_colored_by_type && State.sorted_by_mutation_type) {
			    return 0;
			} else if (!State.mutations_colored_by_type) {
			    return 1;
			} else if (State.mutations_colored_by_type && !State.sorted_by_mutation_type) {
			    return 2;
			}
		    },
		    function () {
			oncoprint.keepSorted(false);
			oncoprint.suppressRendering();
			var genetic_alteration_track_ids = Object.keys(State.genetic_alteration_tracks);
			if (State.mutations_colored_by_type && !State.sorted_by_mutation_type) {
			    State.sorted_by_mutation_type = true;
			    for (var i=0; i<genetic_alteration_track_ids.length; i++) {
				oncoprint.setTrackSortComparator(genetic_alteration_track_ids[i], comparator_utils.makeGeneticComparator(true));
			    }
			} else if (State.mutations_colored_by_type && State.sorted_by_mutation_type) {
			    State.mutations_colored_by_type = false;
			    setGeneticAlterationTracksRuleSet(window.geneticrules.genetic_rule_set_same_color_for_all_no_recurrence);
			} else if (!State.mutations_colored_by_type) {
			    State.mutations_colored_by_type = true;
			    State.sorted_by_mutation_type = false;
			    setGeneticAlterationTracksRuleSet(window.geneticrules.genetic_rule_set_different_colors_no_recurrence);
			    for (var i=0; i<genetic_alteration_track_ids.length; i++) {
				oncoprint.setTrackSortComparator(genetic_alteration_track_ids[i], comparator_utils.makeGeneticComparator(false));
			    }
			}
			oncoprint.keepSorted();
			oncoprint.releaseRendering();
		    });
	})();
	(function setUpDownload() {
	    var xml_serializer = new XMLSerializer();
	    addQTipTo($(toolbar_selector + ' #oncoprint-diagram-downloads-icon'), {
				//id: "#oncoprint-diagram-downloads-icon-qtip",
				style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
				show: {event: "mouseover"},
				hide: {fixed: true, delay: 100, event: "mouseout"},
				position: {my: 'top center', at: 'bottom center', viewport: $(window)},
				content: {
					text: function() {
						return "<button class='oncoprint-diagram-download' type='pdf' style='cursor:pointer;width:90px;'>PDF</button> <br/>" +
							"<button class='oncoprint-diagram-download' type='png' style='cursor:pointer;width:90px;'>PNG</button> <br/>" +
							"<button class='oncoprint-diagram-download' type='svg' style='cursor:pointer;width:90px;'>SVG</button> <br/>" +
							"<button class='oncoprint-sample-download'  type='txt' style='cursor:pointer;width:90px;'>"+(State.using_sample_data ? "Sample" : "Patient")+" order</button>";
					    }
				},
				events: {
					render: function (event) {
						$('body').on('click', '.oncoprint-diagram-download', function () {
							var fileType = $(this).attr("type");
							var two_megabyte_limit = 2000000;
							if (fileType === 'pdf')
							{
							    var svg = oncoprint.toSVG(true);
							    var serialized = cbio.download.serializeHtml(svg);
							    if (serialized.length > two_megabyte_limit) {
								alert("Oncoprint too big to download as PDF - please download as SVG");
								return;
							    }
							    cbio.download.initDownload(serialized, {
								filename: "oncoprint.pdf",
								contentType: "application/pdf",
								servletName: "svgtopdf.do"
							    });
							}
							else if (fileType === 'svg')
							{
								cbio.download.initDownload(oncoprint.toSVG(), {filename: "oncoprint.svg"});
							} else if (fileType === 'png')
							{
							    var img = oncoprint.toCanvas(function(canvas, truncated) {
								canvas.toBlob(function(blob) {
								    if (truncated) {
									alert("Oncoprint too large - PNG truncated to "+canvas.getAttribute("width")+" by "+canvas.getAttribute("height"));
								    }
								    saveAs(blob, "oncoprint.png");
								}, 'image/png');
							    }, 2);
							}
						});

						$('body').on('click', '.oncoprint-sample-download', function () {
							var idTypeStr = (State.using_sample_data ? "Sample" : "Patient");
							var content = idTypeStr + " order in the Oncoprint is: \n";
							content += oncoprint.getIdOrder().join('\n');
							var downloadOpts = {
								filename: 'OncoPrint' + idTypeStr + 's.txt',
								contentType: "text/plain;charset=utf-8",
								preProcess: false};

							// send download request with filename & file content info
							cbio.download.initDownload(content, downloadOpts);
						});
					}
				}
	    });
	})();
    })();
    return function(data_by_gene, id_key, altered_ids_by_gene, id_order, gene_order) {
	State.setData(data_by_gene, id_key, altered_ids_by_gene, id_order, gene_order);
    };
}
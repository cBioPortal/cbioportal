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
};

var tooltip_utils = {
    'sampleViewAnchorTag': function (sample_id) {
	var href = cbio.util.getLinkToSampleView(QuerySession.getCancerStudyIds()[0], sample_id);
	return '<a href="' + href + '">' + sample_id + '</a>';
    },
    'patientViewAnchorTag': function(patient_id) {
	var href = cbio.util.getLinkToPatientView(QuerySession.getCancerStudyIds()[0], patient_id);
	return '<a href="' + href + '">' + patient_id + '</a>';
    },
    'makeGeneticTrackTooltip':function(data_type, link_id) {
	return function (d) {
	    var ret = '';
	    if (d.mutation) {
		ret += 'Mutation: <b>' + d.mutation + '</b><br>';
	    }
	    if (d.cna) {
		ret += 'Copy Number Alteration: <b>' + d.cna + '</b><br>';
	    }
	    if (d.mrna) {
		ret += 'MRNA: <b>' + d.mrna + '</b><br>';
	    }
	    if (d.rppa) {
		ret += 'RPPA: <b>' + d.rppa + '</b><br>';
	    }
	    ret += (data_type === 'sample' ? (link_id ? tooltip_utils.sampleViewAnchorTag(d.sample) : d.sample) : (link_id ? tooltip_utils.patientViewAnchorTag(d.patient) : d.patient));
	    return ret;
	}
    },
    'makeClinicalTrackTooltip':function(data_type, link_id) {
	if (data_type === 'sample') {
	    return function(d) {
		var ret = '';
		ret += 'value: <b>' + d.attr_val + '</b><br>';
		ret += (link_id ? tooltip_utils.sampleViewAnchorTag(d.sample) : d.sample);
		return ret;
	    };
	} else {
	    return function(d) {
		var ret = '';
		if (d.attr_vals) {
		    for (var val in d.attr_vals) {
			if (typeof d.attr_vals[val] !== 'undefined') {
			    ret += '<b>' + val + '</b>: '+d.attr_vals[val]+'<br>';
			}
		    }
		} else {
		    ret += 'value: <b>' + d.attr_val + '</b><br>';
		}
		ret += (link_id ? tooltip_utils.patientViewAnchorTag(d.patient) : d.patient);
		return ret;
	    };
	}
    }
};

var comparator_utils = {
    'makeGeneticComparator': function (distinguish_mutations) {
	var cna_key = 'cna';
	var cna_order = utils.invertArray(['AMPLIFIED', 'HOMODELETED', 'GAINED', 'HEMIZYGOUSLYDELETED', 'DIPLOID', undefined]);
	var mut_type_key = 'mut_type';
	var mut_order = (function () {
	    var _order = utils.invertArray(['FUSION', 'TRUNC', 'INFRAME', 'MISSENSE', undefined, true, false]);
	    if (!distinguish_mutations) {
		return function (m) {
		    if (m === 'FUSION') {
			return 0;
		    } else {
			return _order[!!m];
		    }
		    //return +(typeof m === 'undefined');
		}
	    } else {
		return function (m) {
		    return _order[m];
		}
	    }
	})();
	var mrna_key = 'mrna';
	var rppa_key = 'rppa';
	var regulation_order = utils.invertArray(['UPREGULATED', 'DOWNREGULATED', undefined]);

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
	var $loading_bar_msg = $(utils.makeSVGElement("text", {
							    'x': 2,
							    'y':15,
							    'font-size':11,
							    'font-family':'Arial',
							    'font-weight':'normal',
							    'text-anchor':'start',
							}))
				.appendTo($loading_bar_svg);
	var $loading_bar = $(utils.makeSVGElement("rect", {
							"width":0, 
							"height":25, 
							"fill":"green", 
							"stroke":"dark green"}))
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
	    return before_url + param + '=' + new_value + "&" + after_url;
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
		if (State.used_clinical_attributes.length > 0) {
		new_url = changeURLParam(CLINICAL_ATTRS_PARAM, 
					    State.used_clinical_attributes
						    .map(function(attr) { return encodeURIComponent(attr.attr_id);})
						    .join(","),
					    new_url);
		}
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
	    $.when(QuerySession.getGenomicEventData(), 
		    QuerySession.getAlteredSamplesWholePercentageByGene(), 
		    QuerySession.getUnalteredSamples(),
		    ClinicalData.getSampleData(clinical_attrs))
		    .then(function (genetic_data, 
				    altered_sample_percentage_by_gene, 
				    unaltered_samples,
				    clinical_data) {
					
			if (State.unaltered_cases_hidden) {
			    oncoprint.hideIds(unaltered_samples, true);
			}
			var data_by_gene = {};
			for (var i = 0; i < genetic_data.length; i++) {
			    var d = genetic_data[i];
			    if (!data_by_gene[d.gene]) {
				data_by_gene[d.gene] = [];
			    }
			    data_by_gene[d.gene].push(d);
			}
			LoadingBar.msg("");
			oncoprint.suppressRendering();
			oncoprint.keepSorted(false);
			
			var total_tracks_to_add = Object.keys(State.genetic_alteration_tracks).length
						+ Object.keys(State.clinical_tracks).length;
			
			utils.timeoutSeparatedLoop(Object.keys(State.genetic_alteration_tracks), function (track_id, i) {
			    var gene = State.genetic_alteration_tracks[track_id];
			    oncoprint.setTrackData(track_id, data_by_gene[gene], 'sample');
			    oncoprint.setTrackInfo(track_id, altered_sample_percentage_by_gene[gene] + '%');
			    oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeGeneticTrackTooltip('sample', true));
			    LoadingBar.update(i / total_tracks_to_add);
			}).then(function() {
			    return utils.timeoutSeparatedLoop(Object.keys(State.clinical_tracks), function(track_id, i) {
				var attr = State.clinical_tracks[track_id];
				oncoprint.setTrackData(track_id, clinical_data[attr.attr_id], 'sample');
				oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeClinicalTrackTooltip('sample', true));
				LoadingBar.update((i + Object.keys(State.genetic_alteration_tracks).length) / total_tracks_to_add);
			    });
			}).then(function () {
			    oncoprint.keepSorted();
			    oncoprint.releaseRendering();
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
	    $.when(QuerySession.getCombinedPatientGenomicEventData(), 
		    QuerySession.getAlteredPatientsWholePercentageByGene(), 
		    QuerySession.getUnalteredPatients(),
		    ClinicalData.getPatientData(clinical_attrs))
		    .then(function (genetic_data, 
				    altered_patient_percentage_by_gene, 
				    unaltered_patients,
				    clinical_data) {
					
			if (State.unaltered_cases_hidden) {
			    oncoprint.hideIds(unaltered_patients, true);
			}
			var data_by_gene = {};
			for (var i = 0; i < genetic_data.length; i++) {
			    var d = genetic_data[i];
			    if (!data_by_gene[d.gene]) {
				data_by_gene[d.gene] = [];
			    }
			    data_by_gene[d.gene].push(d);
			}
			LoadingBar.msg("");
			oncoprint.suppressRendering();
			oncoprint.keepSorted(false);
			
			var total_tracks_to_add = Object.keys(State.genetic_alteration_tracks).length
						+ Object.keys(State.clinical_tracks).length;
			
			utils.timeoutSeparatedLoop(Object.keys(State.genetic_alteration_tracks), function (track_id, i) {
			    var gene = State.genetic_alteration_tracks[track_id];
			    oncoprint.setTrackData(track_id, data_by_gene[gene], 'patient');
			    oncoprint.setTrackInfo(track_id, altered_patient_percentage_by_gene[gene] + '%');
			    oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeGeneticTrackTooltip('patient', true));
			    LoadingBar.update(i / total_tracks_to_add);
			}).then(function() {
			    return utils.timeoutSeparatedLoop(Object.keys(State.clinical_tracks), function(track_id, i) {
				var attr = State.clinical_tracks[track_id];
				oncoprint.setTrackData(track_id, clinical_data[attr.attr_id], 'patient');
				oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeClinicalTrackTooltip('patient', true));
				LoadingBar.update((i + Object.keys(State.genetic_alteration_tracks).length) / total_tracks_to_add);
			    });
			}).then(function () {
			    oncoprint.keepSorted();
			    oncoprint.releaseRendering();
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
		oncoprint.setTrackData(track_id, data, (State.using_sample_data ? 'sample' : 'patient'));
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
	var loadPatientOrder = function(state) {
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
			text += getPercent(state.using_sample_data ? (altered_samples.length / QuerySession.getSampleIds().length) : (altered_patients.length / patient_ids.length));
			text +=") of ";
			text += (state.using_sample_data ? QuerySession.getSampleIds().length : patient_ids.length);
			text += " ";
			text += (state.using_sample_data ? "samples" : "cases/patients");
			$('#altered_value').text(text);
	    });
	};
	
	return {
	    'first_genetic_alteration_track': null,
	    'genetic_alteration_tracks': {}, // track_id -> gene
	    'clinical_tracks': {}, // track_id -> attr
	    
	    'used_clinical_attributes': [],
	    'unused_clinical_attributes': [],
	    'clinical_attributes_fetched': new $.Deferred(),
	    'clinical_attr_id_to_sample_data': {},
	    'clinical_attr_id_to_patient_data': {},
	    
	    'cell_padding_on': false,
	    'using_sample_data': (URL.getInitDataType() === 'sample'),
	    'unaltered_cases_hidden': false,
	    'clinical_track_legends_shown': false,
	    'mutations_colored_by_type': true,
	    'sorted_by_mutation_type': true,
	    
	    'patient_order_loaded': new $.Deferred(),
	    'patient_order': [],
	    
	    'sorting_by_given_order': false,
	    
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
		if (sample_or_patient === 'sample') {
		    this.using_sample_data = true;
		    URL.update();
		    if (this.sorting_by_given_order) {
			setSortOrder(QuerySession.getSampleIds());
		    }
		    updateAlteredPercentIndicator(this);
		    return populateSampleData();
		} else if (sample_or_patient === 'patient') {
		    this.using_sample_data = false;
		    URL.update();
		    loadPatientOrder(this);
		    if (this.sorting_by_given_order) {
			var self = this;
			this.patient_order_loaded.then(function() {
			    setSortOrder((self.using_sample_data ? QuerySession.getSampleIds() : self.patient_order));
			});
		    }
		    updateAlteredPercentIndicator(this);
		    return populatePatientData();
		}
	    },
	    'addGeneticTracks': function (genes) {
		genes = [].concat(genes);
		oncoprint.suppressRendering();
		var track_ids = [];
		for (var i = 0; i < genes.length; i++) {
		    var track_params = {
			'rule_set_params': {
			    'type': 'gene',
			    'legend_label': 'Genetic Alteration'
			},
			'label': genes[i],
			'target_group': 1,
			'sortCmpFn': comparator_utils.makeGeneticComparator(true)
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
		    track_params['rule_set_params']['exclude_from_legend'] = true;
		    track_params['label'] = attr.display_name;
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
	    }
	};
    })();
    
    var ClinicalData = (function() {
	var sample_clinical_data = {};// attr_id -> list of data
	var patient_clinical_data = {};// attr_id -> list of data
	
	var makeSampleDataFromPatientAttrData = function(data) {
	    var def = new $.Deferred();
	    QuerySession.getPatientSampleIdMap().then(function(sample_to_patient) {
		var sample_data = [];
		var patient_to_datum = {};
		for (var i=0; i<data.length; i++) {
		    patient_to_datum[data[i].patient] = data[i];
		}
		var samples = QuerySession.getSampleIds();
		for (var i=0; i<samples.length; i++) {
		    var patient = sample_to_patient[samples[i]];
		    var patient_datum = patient_to_datum[patient];
		    if (patient_datum) {
			sample_data.push({'sample':samples[i], 'attr_id':patient_datum.attr_id, 'attr_val':patient_datum.attr_val});
		    }
		}
		def.resolve(sample_data);
	    }).fail(function() {
		def.reject();
	    });
	    return def.promise();
	};
	
	var makePatientDataFromSampleAttrData = function(data, combination_type) {
	    var def = new $.Deferred();
	    QuerySession.getPatientSampleIdMap().then(function(sample_to_patient) {
		var patient_to_data = {};
		// first gather the data by patient
		for (var i=0; i<data.length; i++) {
		    var datum = data[i];
		    if (typeof sample_to_patient[datum.sample] === 'undefined') {
			continue;
		    }
		    patient_to_data[sample_to_patient[datum.sample]] = patient_to_data[sample_to_patient[datum.sample]] || [];
		    patient_to_data[sample_to_patient[datum.sample]].push(datum);
		}
		// now combine the data
		var patient_data = [];
		for (var patient in patient_to_data) {
		    if (typeof patient_to_data[patient] !== 'undefined') {
			var datum = {'patient':patient};
			if (combination_type === 'average') {
			    datum.attr_val = 0;
			    for (var j=0; j<patient_to_data[patient].length; j++) {
				if (j===0) {
				    datum.attr_id = patient_to_data[patient][j].attr_id;
				}
				datum.attr_val += patient_to_data[patient][j].attr_val;
			    }
			    datum.attr_val /= patient_to_data[patient].length;
			} else if (combination_type === 'category') {
			    var attr_vals = {};
			    for (var j=0; j<patient_to_data[patient].length; j++) {
				attr_vals[patient_to_data[patient][j].attr_val] = attr_vals[patient_to_data[patient][j].attr_val] || 0;
				attr_vals[patient_to_data[patient][j].attr_val] += 1;
			    }
			    if (Object.keys(attr_vals).length > 1) {
				datum.attr_val = "Mixed";
				datum.attr_vals = attr_vals;
			    } else {
				datum.attr_val = Object.keys(attr_vals)[0];
			    }
			}
			patient_data.push(datum);
		    }
		}
		def.resolve(patient_data);
	    }).fail(function() {
		def.reject();
	    });
	    return def.promise();
	};
	var addBlankSampleData = function(attr_id, data) {
	    // Add blank data for missing ids
	    var ret = data.slice();
	    var present = {};
	    for (var i = 0; i < data.length; i++) {
		present[data[i].sample] = true;
	    }
	    var to_add = QuerySession.getSampleIds().filter(function (id) {
		return !present[id];
	    });
	    for (var i = 0; i < to_add.length; i++) {
		ret.push({'sample': to_add[i], 'attr_id':attr_id, 'na': true});
	    }
	    return ret;
	};
	var addBlankPatientData = function(attr_id, data) {
	    // Add blank data for missing ids
	    var def = new $.Deferred();
	    var present = {};
	    QuerySession.getPatientIds().then(function (ids) {
		var ret = data.slice();
		for (var i = 0; i < data.length; i++) {
		    present[data[i].patient] = true;
		}
		var to_add = ids.filter(function (id) {
		    return !present[id];
		});
		for (var i = 0; i < to_add.length; i++) {
		    ret.push({'patient': to_add[i], 'attr_id': attr_id, 'na': true});
		}
		def.resolve(ret);
	    }).fail(function() {
		def.reject();
	    });
	    return def.promise();
	};
	var fetchData = function(attr) {
	    var def = new $.Deferred();
	    if (attr.attr_id === '# mutations') {
		var clinicalMutationColl = new ClinicalMutationColl();
		clinicalMutationColl.fetch({
		    type: "POST",
		    data: {
			mutation_profile: window.QuerySession.getMutationProfileId(),
			cmd: "count_mutations",
			case_ids: QuerySession.getSampleIds().join(" ")
		    },
		    success: function (response) {
			response = response.toJSON();
			var sample_data = addBlankSampleData(attr.attr_id, response);
			sample_clinical_data[attr.attr_id] = sample_data;
			makePatientDataFromSampleAttrData(response, 'average').then(function(patient_data) {
			    addBlankPatientData(attr.attr_id, patient_data).then(function(completed_patient_data) {
				patient_clinical_data[attr.attr_id] = completed_patient_data;
				def.resolve();
			    });
			});
		    },
		    error: function() {
			def.reject();
		    }
		});
	    } else if (attr.attr_id === 'FRACTION_GENOME_ALTERED') {
		var clinicalCNAColl = new ClinicalCNAColl();
		clinicalCNAColl.fetch({
		    type: "POST",
		    data: {
			cancer_study_id: QuerySession.getCancerStudyIds()[0],
			cmd: "get_cna_fraction",
			case_ids: QuerySession.getSampleIds().join(" ")
		    },
		    success: function (response) {
			response = response.toJSON();
			var sample_data = addBlankSampleData(attr.attr_id, response);
			sample_clinical_data[attr.attr_id] = sample_data;
			makePatientDataFromSampleAttrData(response, 'average').then(function(patient_data) {
			    addBlankPatientData(attr.attr_id, patient_data).then(function(completed_patient_data) {
				patient_clinical_data[attr.attr_id] = completed_patient_data;
				def.resolve();
			    })
			});
		    },
		    error: function() {
			def.reject();
		    }
		});
	    } else {
		if (attr.is_patient_attribute === "0") {
		    QuerySession.getSampleClinicalData([attr.attr_id]).then(function(sample_data) {
			sample_clinical_data[attr.attr_id] = addBlankSampleData(attr.attr_id, sample_data);
			makePatientDataFromSampleAttrData(sample_data, 'category').then(function(patient_data) {
			    addBlankPatientData(attr.attr_id, patient_data).then(function(completed_patient_data) {
				patient_clinical_data[attr.attr_id] = completed_patient_data;
				def.resolve();
			    }).fail(function() {
				def.reject();
			    })
			}).fail(function() {
			    def.reject();
			});
		    }).fail(function() {
			def.reject();
		    });
		} else if (attr.is_patient_attribute === "1") {
		    QuerySession.getPatientClinicalData([attr.attr_id]).then(function(patient_data) {
			var calls_completed = 0;
			addBlankPatientData(attr.attr_id, patient_data).then(function(completed_patient_data) {
			    patient_clinical_data[attr.attr_id] = completed_patient_data;
			    calls_completed += 1;
			    if (calls_completed === 2) {
				def.resolve();
			    }
			}).fail(function() {
			    def.reject();
			});
			makeSampleDataFromPatientAttrData(patient_data).then(function(sample_data) {
			    sample_clinical_data[attr.attr_id] = addBlankSampleData(attr.attr_id, sample_data);
			    calls_completed += 1;
			    if (calls_completed === 2) {
				def.resolve();
			    }
			}).fail(function() {
			    def.reject();
			});
		    });
		}
	    }
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
		display_name: "# mutations",
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
	    return attrA.display_name.localeCompare(attrB.display_name);
	});

	if (QuerySession.getMutationProfileId() !== null) {
	    State.unused_clinical_attributes.unshift(ClinicalData.NUMBER_MUTATIONS_ATTRIBUTE);
	}

	if (QuerySession.getCancerStudyIds().length > 0) {
	    State.unused_clinical_attributes.unshift(ClinicalData.FRACTION_GENOME_ALTERED_ATTRIBUTE);
	}

	for (var i = 0, _len = State.unused_clinical_attributes.length; i < _len; i++) {
	    State.unused_clinical_attributes[i].display_order = i;
	}
	
	var url_clinical_attr_ids = URL.getInitUsedClinicalAttrs() || [];
	for (var i=0; i<url_clinical_attr_ids.length; i++) {
	    State.useAttribute(url_clinical_attr_ids[i]);
	}
	
	Toolbar.refreshClinicalAttributeSelector();
	$(toolbar_selector + ' #select_clinical_attributes').chosen({width: "330px", "font-size": "12px", search_contains: true});

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
	QuerySession.getGenomicEventData().then(function (data) {
	    var genes = window.QuerySession.getQueryGenes();
	    (function invokeOldDataManagers() {
		window.PortalDataColl.setOncoprintData(window.OncoprintUtils.process_data(data, genes));
		PortalDataColl.setOncoprintStat(window.OncoprintUtils.alteration_info(data));
	    })();
	    State.addGeneticTracks(genes);
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
	    
        $.when(QuerySession.getAlteredSamples(), QuerySession.getAlteredPatients(), populate_data_promise).then(function(altered_samples, altered_patients) {
	    oncoprint.setHorzZoomToFit(State.using_sample_data ? altered_samples : altered_patients);
	    oncoprint.scrollTo(0);
	});
	
	return populate_data_promise;
    }).then(function() {
	(function fetchClinicalAttributes() {
	    // For some reason $.when isn't working
	    var clinical_attributes_calls_returned = 0;
	    QuerySession.getSampleClinicalAttributes().then(function (sample_attrs) {
		for (var i = 0; i < sample_attrs.length; i++) {
		    sample_attrs[i].type = 'sample';
		}
		State.unused_clinical_attributes = State.unused_clinical_attributes.concat(sample_attrs);
		clinical_attributes_calls_returned += 1;
		if (clinical_attributes_calls_returned === 2) {
		    State.clinical_attributes_fetched.resolve();
		}
	    }).fail(function () {
		State.clinical_attributes_fetched.reject();
	    });
	    QuerySession.getPatientClinicalAttributes().then(function (patient_attrs) {
		for (var i = 0; i < patient_attrs.length; i++) {
		    patient_attrs[i].type = 'patient';
		}
		State.unused_clinical_attributes = State.unused_clinical_attributes.concat(patient_attrs);
		clinical_attributes_calls_returned += 1;
		if (clinical_attributes_calls_returned === 2) {
		    State.clinical_attributes_fetched.resolve();
		}
	    }).fail(function () {
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
		content: {text: 'Zoom in/out of oncoprint'},
		position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		show: {event: "mouseover"},
		hide: {fixed: true, delay: 100, event: "mouseout"}
	    });
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
	    /*$(toolbar_selector + ' #genes_first_a').click(function () {
		oncoprint.setTrackGroupSortPriority([1, 0]);
	    });
	    $(toolbar_selector + ' #clinical_first_a').click(function () {
		oncoprint.setTrackGroupSortPriority([0, 1]);
	    });*/
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
		    oncoprint.setSortConfig({'type':'order', order: (State.using_sample_data ? QuerySession.getSampleIds() : State.patient_order)});
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
	    $.when(QuerySession.getUnalteredSamples(), QuerySession.getUnalteredPatients()).then(function (unaltered_samples, unaltered_patients) {
		setUpButton($(toolbar_selector + ' #oncoprint-diagram-removeUCases-icon'),
			['images/unremoveUCases.svg','images/removeUCases.svg'],
			['Hide unaltered cases', 'Show unaltered cases'],
			function () {
			    return (State.unaltered_cases_hidden ? 1 : 0);
			},
			function () {
			    State.unaltered_cases_hidden = !State.unaltered_cases_hidden;
			    if (State.unaltered_cases_hidden) {
				oncoprint.hideIds((State.using_sample_data ? unaltered_samples : unaltered_patients), true);
			    } else {
				oncoprint.hideIds([], true);
			    }
			});
	    });
	})();
	(function setUpZoomToFit() {
	    $.when(QuerySession.getAlteredSamples(), QuerySession.getAlteredPatients()).then(function(altered_samples, altered_patients) {
		setUpButton($(toolbar_selector + ' #oncoprint_zoomtofit'), [], ["Zoom to fit altered cases in screen"], null, function() {
		    oncoprint.setHorzZoomToFit(State.using_sample_data ? altered_samples : altered_patients);
		    oncoprint.scrollTo(0);
		});
	    });
	})();
	(function setUpChangeMutationRuleSet() {
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
			    setGeneticAlterationTracksRuleSet({'type':'gene', 'legend_label':'Genetic Alteration', 'dont_distinguish_mutations':true});
			} else if (!State.mutations_colored_by_type) {
			    State.mutations_colored_by_type = true;
			    State.sorted_by_mutation_type = false;
			    setGeneticAlterationTracksRuleSet({'type':'gene', 'legend_label':'Genetic Alteration'});
			    for (var i=0; i<genetic_alteration_track_ids.length; i++) {
				oncoprint.setTrackSortComparator(genetic_alteration_track_ids[i], comparator_utils.makeGeneticComparator(false));
			    }
			}
			oncoprint.keepSorted();
			oncoprint.releaseRendering();
		    });
	})();
	(function setUpShowClinicalLegendsBtn() {
	    setUpButton($(toolbar_selector + ' #oncoprint-diagram-showlegend-icon'),
			['images/hidelegend.svg', 'images/showlegend.svg'],
			['Show legends for clinical attribute tracks', 'Hide legends for clinical attribute tracks'],
			function() { return +State.clinical_track_legends_shown; },
			function() {
			    State.clinical_track_legends_shown = !State.clinical_track_legends_shown;
			    var clinical_track_ids = Object.keys(State.clinical_tracks);
			    if (State.clinical_track_legends_shown) {
				oncoprint.showTrackLegends(clinical_track_ids);
			    } else {
				oncoprint.hideTrackLegends(clinical_track_ids);
			    }
			});
	})();
	(function setUpTogglePatientSampleBtn() {
	    var previous_switch_done = new $.Deferred();
	    previous_switch_done.resolve();
	    var $header_btn = $('#switchPatientSample');
	    
	    $header_btn.click(function() {
		$(toolbar_selector + ' #oncoprint_diagram_topatientid_icon').trigger('click');
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
	    
	    updateHeaderBtnText();
	    setUpButton($(toolbar_selector + ' #oncoprint_diagram_topatientid_icon'),
			['images/cool2.svg', 'images/cool.svg'],
			['Show events per patient', 'Show events per sample'],
			function() { return +(!State.using_sample_data); },
			function() {
			    if (previous_switch_done.state() !== "resolved") {
				return;
			    }
			    if (State.using_sample_data) {
				State.setDataType('patient');
			    } else {
				State.setDataType('sample');
			    }
			    updateHeaderBtnText();
			}
	    );
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
							if (fileType === 'pdf')
							{
								var svg = oncoprint.toSVG();
								if (xml_serializer.serializeToString(svg).length > 2000000) {
								    alert("Oncoprint too big to download as PDF - please download as SVG, then convert to PDF using your program of choice.");
								    return;
								}
								var downloadOptions = {
									filename: "oncoprint.pdf",
									contentType: "application/pdf",
									servletName: "svgtopdf.do"
								};

								cbio.download.initDownload(svg, downloadOptions);
							}
							else if (fileType === 'svg')
							{
								cbio.download.initDownload(oncoprint.toSVG(), {filename: "oncoprint.svg"});
							} else if (fileType === 'png')
							{
							    var svg = oncoprint.toSVG(true);
							    if (xml_serializer.serializeToString(svg).length > 2000000) {
								    alert("Oncoprint too big to download as PNG - please download as SVG, then convert to PNG using your program of choice.");
								    return;
							    }
							    var downloadOptions = {
									filename: "oncoprint.png",
									contentType: "application/png",
									servletName: "svgtopdf.do"
							    };
							    
							    cbio.download.initDownload(svg, downloadOptions);
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
	var $loading_bar_msg = $(utils.makeSVGElement("text", {
							    'x': 2,
							    'y':15,
							    'font-size':11,
							    'font-family':'Arial',
							    'font-weight':'normal',
							    'text-anchor':'start',
							}))
				.appendTo($loading_bar_svg);
	var $loading_bar = $(utils.makeSVGElement("rect", {
							"width":0, 
							"height":25, 
							"fill":"green", 
							"stroke":"dark green"}))
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

	    'cell_padding_on': false,
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
			'rule_set_params': {
			    'type': 'gene',
			    'legend_label': 'Genetic Alteration'
			},
			'label': genes[i],
			'target_group': 1,
			'sortCmpFn': comparator_utils.makeGeneticComparator(true),
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
	    setData: function(data_by_gene, id_key, altered_ids_by_gene, id_order) {
		if (id_order) {
		    this.user_specified_order = id_order;
		} else {
		    this.user_specified_order = null;
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
		content: {text: 'Zoom in/out of oncoprint'},
		position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		show: {event: "mouseover"},
		hide: {fixed: true, delay: 100, event: "mouseout"}
	    });
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
			    setGeneticAlterationTracksRuleSet({'type':'gene', 'legend_label':'Genetic Alteration', 'dont_distinguish_mutations':true});
			} else if (!State.mutations_colored_by_type) {
			    State.mutations_colored_by_type = true;
			    State.sorted_by_mutation_type = false;
			    setGeneticAlterationTracksRuleSet({'type':'gene', 'legend_label':'Genetic Alteration'});
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
							if (fileType === 'pdf')
							{
								var svg = oncoprint.toSVG();
								if (xml_serializer.serializeToString(svg).length > 2000000) {
								    alert("Oncoprint too big to download as PDF - please download as SVG, then convert to PDF using your program of choice.");
								    return;
								}
								var downloadOptions = {
									filename: "oncoprint.pdf",
									contentType: "application/pdf",
									servletName: "svgtopdf.do"
								};

								cbio.download.initDownload(svg, downloadOptions);
							}
							else if (fileType === 'svg')
							{
								cbio.download.initDownload(oncoprint.toSVG(), {filename: "oncoprint.svg"});
							} else if (fileType === 'png')
							{
							    var svg = oncoprint.toSVG(true);
							    if (xml_serializer.serializeToString(svg).length > 2000000) {
								    alert("Oncoprint too big to download as PNG - please download as SVG, then convert to PNG using your program of choice.");
								    return;
							    }
							    var downloadOptions = {
									filename: "oncoprint.png",
									contentType: "application/png",
									servletName: "svgtopdf.do"
							    };
							    
							    cbio.download.initDownload(svg, downloadOptions);
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
    return function(data_by_gene, id_key, altered_ids_by_gene, id_order) {
	State.setData(data_by_gene, id_key, altered_ids_by_gene, id_order);
    };
}
var invertArray = function (arr) {
    var ret = {};
    for (var i = 0; i < arr.length; i++) {
	ret[arr[i]] = i;
    }
    return ret;
}
var sign = function (x) {
    if (x > 0) {
	return 1;
    } else if (x < 0) {
	return -1;
    } else {
	return 0;
    }
}

var timeoutSeparatedLoop = function(array, loopFn) {
    // loopFn is function(elt, index, array) {
    var finished_promise = new $.Deferred();
    var loopBlock = function(i) {
	if (i >= array.length) {
	    finished_promise.resolve();
	    return;
	}
	
	loopFn(array[i], i, array);
	setTimeout(function() {
	    loopBlock(i+1);
	}, 0);
    };
    loopBlock(0);
    return finished_promise.promise();
}

var makeGeneticAlterationComparator = function (distinguish_mutations) {
    var cna_key = 'cna';
    var cna_order = invertArray(['AMPLIFIED', 'HOMODELETED', 'GAINED', 'HEMIZYGOUSLYDELETED', 'DIPLOID', undefined]);
    var mut_type_key = 'mut_type';
    var mut_order = (function () {
	var _order = invertArray(['FUSION', 'TRUNC', 'INFRAME', 'MISSENSE', undefined, true, false]);
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
    var regulation_order = invertArray(['UPREGULATED', 'DOWNREGULATED', undefined]);

    return function (d1, d2) {
	var cna_diff = sign(cna_order[d1[cna_key]] - cna_order[d2[cna_key]]);
	if (cna_diff !== 0) {
	    return cna_diff;
	}

	var mut_type_diff = sign(mut_order(d1[mut_type_key]) - mut_order(d2[mut_type_key]));
	if (mut_type_diff !== 0) {
	    return mut_type_diff;
	}

	var mrna_diff = sign(regulation_order[d1[mrna_key]] - regulation_order[d2[mrna_key]]);
	if (mrna_diff !== 0) {
	    return mrna_diff;
	}

	var rppa_diff = sign(regulation_order[d1[rppa_key]] - regulation_order[d2[rppa_key]]);
	if (rppa_diff !== 0) {
	    return rppa_diff;
	}

	return 0;
    };
};
var sampleViewAnchorTag = function (sample_id) {
    var href = cbio.util.getLinkToSampleView(window.cancer_study_id_selected, sample_id);
    return '<a href="' + href + '">' + sample_id + '</a>';
};
var patientViewAnchorTag = function(patient_id) {
    var href = cbio.util.getLinkToPatientView(window.cancer_study_id_selected, patient_id);
    return '<a href="' + href + '">' + patient_id + '</a>';
};

var clinicalToolTip = function(d) {
    var ret = '';
    ret += 'value: <b>' + d.attr_val + '</b><br>';
    ret += sampleViewAnchorTag(d.sample);
    return ret;
};

var makeSVGElement = function (tag, attrs) {
    var el = document.createElementNS('http://www.w3.org/2000/svg', tag);
    for (var k in attrs) {
	if (attrs.hasOwnProperty(k)) {
	    el.setAttribute(k, attrs[k]);
	}
    }
    return el;
};

	
window.CreateCBioPortalOncoprintWithToolbar = function (ctr_selector, toolbar_selector) {
    
    $('#oncoprint #everything').show();
    $('#oncoprint #oncoprint-diagram-toolbar-buttons').show();
    
    $(ctr_selector).css({'position':'relative'});
    var $loading_bar_svg = $('<svg width="100" height="50"></svg><br>').appendTo(ctr_selector);
    $loading_bar_svg.append(makeSVGElement("rect", {"width":100, "height":25, "stroke":"black","fill":"white"}));
    var $loading_bar = $(makeSVGElement("rect", {"width":0, "height":25, "fill":"green", "stroke":"dark green"})).appendTo($loading_bar_svg);
    
    var oncoprint = new window.Oncoprint(ctr_selector, 1050);
    
    
    var genetic_alteration_track_ids = [];
    var gene_to_track_id = {};
    var clinical_track_ids = [];

    var used_clinical_attributes = [];
    var unused_clinical_attributes = [];
    var clinical_attributes_init_fetched = new $.Deferred();
    var clinical_attribute_selector_ready = new $.Deferred();
    
    var to_remove_evt_on_destroy = [];
    var using_sample_data = true;
    
    var makeGeneticAlterationTooltip = function(sample_data) {
	return function (d) {
	    var ret = '';
	    if (d.mutation) {
		ret += 'Mutation: <b>' + d.mutation + '</b><br>';
	    }
	    if (d.cna) {
		ret += 'Copy Number Alteration: <b>' + d.cna + '</b><br>';
	    }
	    if (d.mrna) {
		ret += '<b>MRNA: <b>' + d.mrna + '</b><br>';
	    }
	    if (d.rppa) {
		ret += '<b>RPPA: <b>' + d.rppa + '</b><br>';
	    }
	    ret += (sample_data ? sampleViewAnchorTag(d.sample) : patientViewAnchorTag(d.patient));
	    return ret;
	}
    };

    
    
    var mutation_count_clinical_data = [];
    var fraction_genome_altered_clinical_data = [];

    var addEventHandler = function ($elt, evt, callback) {
	$elt.on(evt, callback);
	to_remove_evt_on_destroy.push({'$elt':$elt, 'evt': evt});
    };
    
    var onClick = function($elt, callback) {
	addEventHandler($elt, 'click', callback);
    };
    var onMouseDownAndClick = function($elt, mousedown_callback, click_callback) {
	addEventHandler($elt, 'mousedown', mousedown_callback);
	addEventHandler($elt, 'click', click_callback);
    };
    var onHover = function($elt, enter_callback, leave_callback) {
	addEventHandler($elt, 'mouseenter', enter_callback);
	addEventHandler($elt, 'mouseleave', leave_callback);
    };

    var refreshClinicalAttributeSelector = function() {
	//var none_option = {display_name: 'none', attr_id: undefined, display_order:-1};
	//var attributes_to_populate = [none_option].concat(unused_clinical_attributes);
	var attributes_to_populate = unused_clinical_attributes;
	attributes_to_populate.sort(function(attrA, attrB) {
	    return attrA.display_order - attrB.display_order;
	});
	var $selector = $(toolbar_selector + ' #select_clinical_attributes');
	$selector.empty();
	for (var i=0; i<attributes_to_populate.length; i++) {
	    $("<option></option>").appendTo($selector)
		    .attr("value", attributes_to_populate[i].attr_id)
		    .text(attributes_to_populate[i].display_name);
	}
	$(toolbar_selector + " #select_clinical_attributes").val('');
	$(toolbar_selector + " #select_clinical_attributes").trigger("liszt:updated");
	$(toolbar_selector + " #select_clinical_attributes_chzn").addClass("chzn-with-drop");
    };
    
    var moveAttributeFromUsedToUnused = function(attr_id) {
	var attr = null;
	var index = -1;
	for (var i=0; i<used_clinical_attributes.length; i++) {
	    if (used_clinical_attributes[i].attr_id === attr_id) {
		attr = used_clinical_attributes[i];
		index = i;
		break;
	    }
	}
	if (attr !== null) {
	    used_clinical_attributes.splice(index, 1);
	    unused_clinical_attributes.push(attr);
	}
    };
    
    (function fetchClinicalAttributes() {
	// For some reason $.when isn't working so I need to do this weirdly
	var clinical_attributes_calls_returned = 0;
	QuerySession.getSampleClinicalAttributes().then(function (sample_attrs) {
	    unused_clinical_attributes = unused_clinical_attributes.concat(sample_attrs);
	    clinical_attributes_calls_returned += 1;
	    if (clinical_attributes_calls_returned === 1) {
		clinical_attributes_init_fetched.resolve();
	    }
	});
	/*QuerySession.getPatientClinicalAttributes().then(function (patient_attrs) {
	    unused_clinical_attributes = unused_clinical_attributes.concat(patient_attrs);
	    clinical_attributes_calls_returned += 1;
	    if (clinical_attributes_calls_returned === 2) {
		clinical_attributes_init_fetched.resolve();
	    }
	});*/
    })();

    clinical_attributes_init_fetched.then(function () {
	unused_clinical_attributes.sort(function (attrA, attrB) {
	    return attrA.display_name.localeCompare(attrB.display_name);
	});

	if (QuerySession.getMutationProfileId() !== null) {
	    unused_clinical_attributes.unshift({attr_id: "# mutations",
		datatype: "NUMBER",
		description: "Number of mutations",
		display_name: "# mutations",
	    });
	}

	if (QuerySession.getCancerStudyIds().length > 0) {
	    unused_clinical_attributes.unshift({attr_id: "FRACTION_GENOME_ALTERED",
		datatype: "NUMBER",
		description: "Fraction Genome Altered",
		display_name: "Fraction Genome Altered"
	    });
	}

	for (var i = 0, _len = unused_clinical_attributes.length; i < _len; i++) {
	    unused_clinical_attributes[i].display_order = i;
	}
	refreshClinicalAttributeSelector();
	$(toolbar_selector + ' #select_clinical_attributes').chosen({width: "330px", "font-size": "12px", search_contains: true});

	onClick($(toolbar_selector + ' #select_clinical_attributes_chzn .chzn-search input'), function(e) { e.stopPropagation(); });
	
	$(toolbar_selector + " #select_clinical_attributes_chzn").mouseenter(function () {
	    $(toolbar_selector + " #select_clinical_attributes_chzn .chzn-search input").focus();
	});
	$(toolbar_selector + " #select_clinical_attributes_chzn").addClass("chzn-with-drop");
	clinical_attribute_selector_ready.resolve();
    });
    
    clinical_attribute_selector_ready.then(function() {
	addEventHandler($(toolbar_selector + ' #select_clinical_attributes'), 'change', function(evt) {
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
	var addBlankData = function(data) {
	    var present = {};
	    for (var i=0; i<data.length; i++) {
		present[data[i].sample] = true;
	    }
	    var to_add = QuerySession.getSampleIds().filter(function(id) { return !present[id]; });
	    for (var i=0; i<to_add.length; i++) {
		data.push({'sample':to_add[i], 'na':true});
	    }
	    return data;
	}
	var numericalSortFn = function(d1, d2) {
	    if (d1.na && d2.na) {
		return 0;
	    } else if (d1.na && !d2.na) {
		return 2;
	    } else if (!d1.na && d2.na) {
		return -2;
	    } else {
		return (d1.attr_val < d2.attr_val ? -1 : (d1.attr_val === d2.attr_val ? 0 : 1));
	    }
	};
	var stringSortFn = function(d1, d2) {
	    if (d1.na && d2.na) {
		return 0;
	    } else if (d1.na && !d2.na) {
		return 2;
	    } else if (!d1.na && d2.na) {
		return -2;
	    } else {
		return d1.attr_val.localeCompare(d2.attr_val);
	    }
	};
	var attr = null;
	var index = -1;
	for (var i=0; i<unused_clinical_attributes.length; i++) {
	    if (unused_clinical_attributes[i].attr_id === attr_id) {
		attr = unused_clinical_attributes[i];
		index = i;
		break;
	    }
	}
	if (attr === null) {
	    return;
	}
	used_clinical_attributes.push(attr);
	unused_clinical_attributes.splice(index, 1);
	refreshClinicalAttributeSelector();
	
	var removeAttributeHandler = function(track_id) {
	    clinical_track_ids.splice(clinical_track_ids.indexOf(track_id), 1);
	    moveAttributeFromUsedToUnused(attr_id);
	    refreshClinicalAttributeSelector();
	    if (clinical_track_ids.length === 0) {
		$(toolbar_selector + ' #oncoprint-diagram-showlegend-icon').hide();
	    }
	};
	// TODO: replace mutation count and cna fraction with api service
	if (attr_id === '# mutations') {
	    var data_fetched = new $.Deferred();
	    if (mutation_count_clinical_data.length > 0) {
		data_fetched.resolve();
	    } else {
		var clinicalMutationColl = new ClinicalMutationColl();
		clinicalMutationColl.fetch({
		    type: "POST",
		    data: {
			mutation_profile: window.QuerySession.getMutationProfileId(),
			cmd: "count_mutations",
			case_ids: QuerySession.getSampleIds().join(" ")
		    },
		    success: function (response) {
			// TODO: add blank data
			mutation_count_clinical_data = addBlankData(response.toJSON());
			data_fetched.resolve();
		    }
		});
	    }
	    data_fetched.then(function() {
		clinical_track_ids = clinical_track_ids.concat(oncoprint.addTracks([{'data': mutation_count_clinical_data, 
					'label': '# mutations', 
					'sortCmpFn': numericalSortFn, 
					'rule_set_params': {'type':'bar', 'value_key': 'attr_val', 'value_range':[0,undefined], 'legend_label':attr.display_name, 'exclude_from_legend':true}, 
					'data_id_key':'sample', 'target_group':0,
					'removable':true, 'removeCallback':removeAttributeHandler, 'sort_direction_changeable':true, 'init_sort_direction':0, 'tooltipFn':clinicalToolTip}]));
	    });
	} else if (attr_id === 'FRACTION_GENOME_ALTERED') {
	    var data_fetched = new $.Deferred();
	    if (fraction_genome_altered_clinical_data.length > 0) {
		data_fetched.resolve();
	    } else {
		var clinicalCNAColl = new ClinicalCNAColl();
		clinicalCNAColl.fetch({
		    type: "POST",
		    data: {
			cancer_study_id: QuerySession.getCancerStudyIds()[0],
			cmd: "get_cna_fraction",
			case_ids: QuerySession.getSampleIds().join(" ")
		    },
		    success: function (response) {
			// TODO: add blank data
			fraction_genome_altered_clinical_data = addBlankData(response.toJSON());
			data_fetched.resolve();
		    }
		});
	    }
	    data_fetched.then(function() {
		clinical_track_ids = clinical_track_ids.concat(oncoprint.addTracks([{'data': fraction_genome_altered_clinical_data, 'label': 'Fraction Genome Altered', 'sortCmpFn': numericalSortFn, 'rule_set_params': {'type':'bar', 'value_key':'attr_val', 'value_range':[0,1], 'legend_label':attr.display_name, 'exclude_from_legend':true}, 'data_id_key':'sample', 'removable':true, 'removeCallback':removeAttributeHandler, 'sort_direction_changeable':true, 'init_sort_direction':0, 'tooltipFn':clinicalToolTip}]));
	    });
	} else {
	    QuerySession.getSampleClinicalData([attr_id]).then(function(data) {
		var rule_set_params;
		var sortCmpFn;
		if (attr.datatype === 'number') {
		    rule_set_params = {'type':'bar', 'value_key':'attr_val'};
		    sortCmpFn = numericalSortFn;
		} else {
		    rule_set_params = {'type':'categorical', 'category_key':'attr_val'};
		    sortCmpFn = stringSortFn;
		}
		rule_set_params['exclude_from_legend'] = true;
		rule_set_params.legend_label = attr.display_name;
		clinical_track_ids = clinical_track_ids.concat(oncoprint.addTracks([{'data':addBlankData(data), 'label':attr.display_name, 'sortCmpFn':sortCmpFn, 'rule_set_params':rule_set_params, 'data_id_key':'sample','removable':true, 'removeCallback':removeAttributeHandler, 'sort_direction_changeable':true, 'init_sort_direction':0, 'tooltipFn':clinicalToolTip}]));
	    });
	}
    };

    var initOncoprint = function() {
	var def = new $.Deferred();
	QuerySession.getGenomicEventData().then(function (data) {
	    var genes = window.QuerySession.getQueryGenes();
	    (function invokeOldDataManagers() {
		window.PortalDataColl.setOncoprintData(window.OncoprintUtils.process_data(data, genes));
		PortalDataColl.setOncoprintStat(window.OncoprintUtils.alteration_info(data));
	    })();
	    
	    oncoprint.suppressRendering();
	    
	    for (var i=0; i<genes.length; i++) {
		var track_params = {'rule_set_params': {'type':'gene', 'legend_label':'Genetic Alteration'}, 'label': genes[i],
			'sortCmpFn': makeGeneticAlterationComparator(true), 'target_group': 1};
		var new_track_id = oncoprint.addTracks([track_params])[0];
		gene_to_track_id[genes[i]] = new_track_id;
		genetic_alteration_track_ids.push(new_track_id);
		if (i > 0) {
		    oncoprint.shareRuleSet(genetic_alteration_track_ids[0], new_track_id);
		}
	    }
	    oncoprint.releaseRendering();
	    def.resolve();
	});
	return def.promise();
    };
    var loadSampleDataOncoprint = function() {
	var done = new $.Deferred();
	$.when(QuerySession.getGenomicEventData(), QuerySession.getAlteredSamplesWholePercentageByGene()).then(function(data, altered_sample_percentage_by_gene) {
	    var data_by_gene = {};
	    for (var i=0; i<data.length; i++) {
		var d = data[i];
		if (!data_by_gene[d.gene]) {
		    data_by_gene[d.gene] = [];
		}
		data_by_gene[d.gene].push(d);
	    }
	    $loading_bar_svg.show();
	    oncoprint.suppressRendering();
	    oncoprint.keepSorted(false);
	    timeoutSeparatedLoop(Object.keys(data_by_gene), function (gene, i, array) {
		var track_id = gene_to_track_id[gene];
		oncoprint.setTrackData(track_id, data_by_gene[gene], 'sample');
		oncoprint.setTrackInfo(track_id, altered_sample_percentage_by_gene[gene] + '%');
		oncoprint.setTrackTooltipFn(track_id, makeGeneticAlterationTooltip(true));
		$loading_bar.attr("width", (i / array.length) * parseFloat($loading_bar_svg.attr("width")));
	    }).then(function () {
		oncoprint.keepSorted();
		oncoprint.releaseRendering();
		$loading_bar_svg.hide();
		done.resolve();
	    });
	});
	return done.promise();
    };
    var loadPatientDataOncoprint = function() {
	var done = new $.Deferred();
	$.when(QuerySession.getCombinedPatientGenomicEventData(), QuerySession.getAlteredPatientsWholePercentageByGene()).then(function(data, altered_patient_percentage_by_gene) {
	    var data_by_gene = {};
	    for (var i=0; i<data.length; i++) {
		var d = data[i];
		if (!data_by_gene[d.gene]) {
		    data_by_gene[d.gene] = [];
		}
		data_by_gene[d.gene].push(d);
	    }
	    $loading_bar_svg.show();
	    oncoprint.suppressRendering();
	    oncoprint.keepSorted(false);
	    timeoutSeparatedLoop(Object.keys(data_by_gene), function (gene, i, array) {
		var track_id = gene_to_track_id[gene];
		oncoprint.setTrackData(track_id, data_by_gene[gene], 'patient');
		oncoprint.setTrackInfo(track_id, altered_patient_percentage_by_gene[gene] + '%');
		oncoprint.setTrackTooltipFn(track_id, makeGeneticAlterationTooltip(false));
		$loading_bar.attr("width", (i / array.length) * parseFloat($loading_bar_svg.attr("width")));
	    }).then(function () {
		oncoprint.keepSorted();
		oncoprint.releaseRendering();
		$loading_bar_svg.hide();
		done.resolve();
	    });
	});
	return done.promise();
    };
    initOncoprint().then((using_sample_data ? loadSampleDataOncoprint : loadPatientDataOncoprint));
    window.oncoprint = oncoprint;

    (function setUpToolbar() {
	var unaltered_cases_hidden = false;
	var zoom_discount = 0.7;
	var cell_padding_on = true;
	var unaltered_cases_hidden = false;
	var clinical_track_legends_shown = false;

	var mutations_colored_by_type = true;
	var sorted_by_mutation_type = true;

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
	    onHover($elt, function() {
		hoverButton();
	    }, function() {
		updateButton();
	    });
	    onMouseDownAndClick($elt, function() {
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
	    
	    oncoprint.onHorzZoom(function() {
		$zoom_slider.trigger('change');
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
	    });
	    $(toolbar_selector + ' #alphabetically_first_a').click(function() {
		oncoprint.setSortConfig({'type':'alphabetical'});
	    });
	    $(toolbar_selector + ' #user_defined_first_a').click(function() {
		oncoprint.setSortConfig({'type':'order', order:QuerySession.getSampleIds()});
	    });
	})();
	
	
	(function setUpToggleCellPadding() {
	    setUpButton($(toolbar_selector + ' #oncoprint-diagram-removeWhitespace-icon'),
		    ['images/unremoveWhitespace.svg','images/removeWhitespace.svg'],
		    ["Remove whitespace between columns", "Show whitespace between columns"],
		    function () {
			return (cell_padding_on ? 0 : 1);
		    },
		    function () {
			cell_padding_on = !cell_padding_on;
			oncoprint.setCellPaddingOn(cell_padding_on);
		    });
	})();
	(function setUpHideUnalteredCases() {
	    $.when(QuerySession.getUnalteredSamples(), QuerySession.getUnalteredPatients()).then(function (unaltered_samples, unaltered_patients) {
		setUpButton($(toolbar_selector + ' #oncoprint-diagram-removeUCases-icon'),
			['images/unremoveUCases.svg','images/removeUCases.svg'],
			['Hide unaltered cases', 'Show unaltered cases'],
			function () {
			    return (unaltered_cases_hidden ? 1 : 0);
			},
			function () {
			    unaltered_cases_hidden = !unaltered_cases_hidden;
			    if (unaltered_cases_hidden) {
				oncoprint.hideIds((using_sample_data ? unaltered_samples : unaltered_patients), true);
			    } else {
				oncoprint.hideIds([], true);
			    }
			});
	    });
	})();
	(function setUpZoomToFit() {
	    $.when(QuerySession.getAlteredSamples(), QuerySession.getAlteredPatients()).then(function(altered_samples, altered_patients) {
		setUpButton($(toolbar_selector + ' #oncoprint_zoomtofit'), [], ["Zoom to fit altered cases in screen"], null, function() {
		    oncoprint.setHorzZoom(oncoprint.getZoomToFitHorz((using_sample_data ? altered_samples : altered_patients)));
		    oncoprint.scrollTo(0);
		});
	    });
	})();
	(function setUpChangeMutationRuleSet() {
	    var setGeneticAlterationTracksRuleSet = function(rule_set_params) {
		oncoprint.setRuleSet(genetic_alteration_track_ids[0], rule_set_params);
		for (var i = 1; i < genetic_alteration_track_ids.length; i++) {
		    oncoprint.shareRuleSet(genetic_alteration_track_ids[0], genetic_alteration_track_ids[i]);
		}
	    };
	    
	    setUpButton($(toolbar_selector + ' #oncoprint_diagram_showmutationcolor_icon'),
		    ['images/colormutations.svg', 'images/uncolormutations.svg','images/mutationcolorsort.svg'],
		    ['Show all mutations with the same color', 'Color-code mutations but don\'t sort by type', 'Color-code mutations and sort by type', ],
		    function () {
			if (mutations_colored_by_type && sorted_by_mutation_type) {
			    return 0;
			} else if (!mutations_colored_by_type) {
			    return 1;
			} else if (mutations_colored_by_type && !sorted_by_mutation_type) {
			    return 2;
			}
		    },
		    function () {
			oncoprint.keepSorted(false);
			oncoprint.suppressRendering();
			if (mutations_colored_by_type && !sorted_by_mutation_type) {
			    sorted_by_mutation_type = true;
			    for (var i=0; i<genetic_alteration_track_ids.length; i++) {
				oncoprint.setTrackSortComparator(genetic_alteration_track_ids[i], makeGeneticAlterationComparator(true));
			    }
			} else if (mutations_colored_by_type && sorted_by_mutation_type) {
			    mutations_colored_by_type = false;
			    setGeneticAlterationTracksRuleSet({'type':'gene', 'legend_label':'Genetic Alteration', 'dont_distinguish_mutations':true});
			} else if (!mutations_colored_by_type) {
			    mutations_colored_by_type = true;
			    sorted_by_mutation_type = false;
			    setGeneticAlterationTracksRuleSet({'type':'gene', 'legend_label':'Genetic Alteration'});
			    for (var i=0; i<genetic_alteration_track_ids.length; i++) {
				oncoprint.setTrackSortComparator(genetic_alteration_track_ids[i], makeGeneticAlterationComparator(false));
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
			function() { return +clinical_track_legends_shown; },
			function() {
			    clinical_track_legends_shown = !clinical_track_legends_shown;
			    for (var i=0; i<clinical_track_ids.length; i++) {
				if (clinical_track_legends_shown) {
				    oncoprint.showTrackLegend(clinical_track_ids[i]);
				} else {
				    oncoprint.hideTrackLegend(clinical_track_ids[i]);
				}
			    }
			});
	})();
	(function setUpTogglePatientSampleBtn() {
	    var previous_switch_done = new $.Deferred();
	    previous_switch_done.resolve();
	    setUpButton($(toolbar_selector + ' #oncoprint_diagram_topatientid_icon'),
			['images/cool2.svg', 'images/cool.svg'],
			['Show events per patient', 'Show events per sample'],
			function() { return +(!using_sample_data); },
			function() {
			    if (previous_switch_done.state() !== "resolved") {
				return;
			    }
			    using_sample_data = !using_sample_data;
			    if (using_sample_data) {
				previous_switch_done = loadSampleDataOncoprint();
			    } else {
				previous_switch_done = loadPatientDataOncoprint();
			    }
			}
	    );
	    /*
	    if (!config.swap_patient_sample) {
		return;
	    }
	    var toolbar_btn = $(toolbar_selector + ' #oncoprint_diagram_topatientid_icon');
	    var header_btn = $('#switchPatientSample');
	    var imgs = ['images/cool2.svg', 'images/cool.svg'];
	    var toolbar_descs = ['Show events per sample', 'Show events per patient'];
	    var header_text = ['Show all samples', 'Show only one column per patient'];
	    var header_descs = ['All samples from a patient are merged into one column. Click to split samples into multiple columns.',
		'Each sample for each patient is in a separate column. Click to show only one column per patient'];
	    var updateBtn = function () {
		toolbar_btn.find('img').attr('src', imgs[+using_sample_data]);
		header_btn.text(header_text[+using_sample_data]);
	    };
	    var updateURL = function () {
		var new_url = window.location.href;
		new_url = changeURLParam("show_samples", using_sample_data, new_url);
		window.history.pushState({"html": window.location.html, "pageTitle": window.location.pageTitle}, "", new_url);
	    };
	    toolbar_btn.add(header_btn).click(function () {
		using_sample_data = !using_sample_data;
		updateBtn();
		oncoprintFadeTo(0.5).then(function () {
		    if (!using_sample_data) {
			oncoprint.setIdOrder([]);
			_.each(genetic_alteration_tracks, function (track_id) {
			    oncoprint.setTrackDatumIdKey(track_id, 'patient');
			    oncoprint.setTrackTooltip(track_id, gaTooltipPatient);
			    oncoprint.setTrackData(track_id, patient_data__gene[track_data_name[track_id]]);
			});
			_.each(clinical_tracks, function (track_id) {
			    oncoprint.setTrackDatumIdKey(track_id, 'patient');
			    oncoprint.setTrackTooltip(track_id, cTooltipPatient);
			    oncoprint.setTrackData(track_id, patient_data__clinical[track_data_name[track_id]]);
			});
			updatePercentAlteredIndicator();
			$(toolbar_selector + ' .oncoprint-sample-download').text("Patient order");
		    } else {
			oncoprint.setIdOrder([]);
			_.each(genetic_alteration_tracks, function (track_id) {
			    oncoprint.setTrackDatumIdKey(track_id, 'sample');
			    oncoprint.setTrackTooltip(track_id, gaTooltipSample);
			    oncoprint.setTrackData(track_id, sample_data__gene[track_data_name[track_id]]);
			});
			_.each(clinical_tracks, function (track_id) {
			    oncoprint.setTrackDatumIdKey(track_id, 'sample');
			    oncoprint.setTrackTooltip(track_id, cTooltipSample);
			    oncoprint.setTrackData(track_id, sample_data__clinical[track_data_name[track_id]]);
			});
			updatePercentAlteredIndicator();
			$(toolbar_selector + ' .oncoprint-sample-download').text("Sample order");
		    }
		    if (unaltered_cases_hidden) {
			hideUnalteredIds();
		    }
		    oncoprint.sort();
		    oncoprintFadeIn();
		    updateURL();
		});
	    });
	    updateBtn();
	    to_click_remove.push(toolbar_btn);
	    to_click_remove.push(header_btn);
	    toolbar_btn.qtip({
		content: {text: function () {
			return toolbar_descs[+using_sample_data];
		    }},
		position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		show: {event: "mouseover"},
		hide: {fixed: true, delay: 100, event: "mouseout"}
	    });
	    header_btn.qtip({
		content: {text: function () {
			return header_descs[+using_sample_data];
		    }},
		position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		show: {event: "mouseover"},
		hide: {fixed: true, delay: 100, event: "mouseout"}
	    });
	    to_qtip_destroy.push(toolbar_btn);
	    to_qtip_destroy.push(header_btn);*/
	})();
	(function setUpDownload() {
	    addQTipTo($(toolbar_selector + ' #oncoprint-diagram-downloads-icon'), {
				//id: "#oncoprint-diagram-downloads-icon-qtip",
				style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
				show: {event: "mouseover"},
				hide: {fixed: true, delay: 100, event: "mouseout"},
				position: {my: 'top center', at: 'bottom center', viewport: $(window)},
				content: {
					text: function() {
						return "<button class='oncoprint-diagram-download' type='pdf' style='cursor:pointer;width:90px;'>PDF</button> <br/>" +
							"<button class='oncoprint-diagram-download' type='svg' style='cursor:pointer;width:90px;'>SVG</button> <br/>" +
							"<button class='oncoprint-sample-download'  type='txt' style='cursor:pointer;width:90px;'>"+(using_sample_data ? "Sample" : "Patient")+" order</button>"
					    }
				},
				events: {
					render: function (event) {
						$('.oncoprint-diagram-download').click(function () {
							var fileType = $(this).attr("type");
							if (fileType === 'pdf')
							{
								var downloadOptions = {
									filename: "oncoprint.pdf",
									contentType: "application/pdf",
									servletName: "svgtopdf.do"
								};

								cbio.download.initDownload(oncoprint.toSVG(), downloadOptions);
							}
							else if (fileType === 'svg')
							{
								cbio.download.initDownload(oncoprint.toSVG(), {filename: "oncoprint.svg"});
							}
						});

						$('.oncoprint-sample-download').click(function () {
							var idTypeStr = (using_sample_data ? "Sample" : "Patient");
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


$(document).ready(function () {
    CreateCBioPortalOncoprintWithToolbar('#oncoprint #everything', '#oncoprint #oncoprint-diagram-toolbar-buttons');
});
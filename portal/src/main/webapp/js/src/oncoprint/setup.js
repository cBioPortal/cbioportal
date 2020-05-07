/* globals cbio, QuerySession */
/* jshint devel: true, laxbreak: true*/
var stringListUnique = function(list) {
    var seen = {};
    var ret = [];
    for (var i=0; i<list.length; i++) {
	var string = list[i];
	if (!seen[string]) {
	    seen[string] = true;
	    ret.push(string);
	}
    }
    return ret;
};

var utils = {
    'isWebGLAvailable': function() {
	var canvas = document.createElement("canvas");
	var gl = canvas.getContext("webgl")
	  || canvas.getContext("experimental-webgl");
	return (gl && gl instanceof WebGLRenderingContext);
    },
    'getUnavailableMessageHTML': function() {
	return 'Oncoprint cannot be displayed because <a href="https://get.webgl.org/">your browser does not support WebGL</a>.';
    },
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
    'sign_of_diff': function(a,b) {
	if (a < b) {
	    return -1;
	} else if (a === b) {
	    return 0;
	} else if (a > b) {
	    return 1;
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
    },
    'getIntegerRange': function(list_of_numbers) {
	var max = Number.NEGATIVE_INFINITY;
	var min = Number.POSITIVE_INFINITY;
	for (var i=0; i<list_of_numbers.length; i++) {
	    max = Math.max(list_of_numbers[i], max);
	    min = Math.min(list_of_numbers[i], min);
	}
	return [Math.floor(min), Math.ceil(max)];
    },
    'deepCopyObject': function (obj) {
	return $.extend(true, ($.isArray(obj) ? [] : {}), obj);
    }
};

var tooltip_utils = {
    'sampleViewAnchorTag': function (study_id, sample_id) {
	var href = cbio.util.getLinkToSampleView(study_id, sample_id);
	return '<a href="' + href + '" target="_blank">' + sample_id + '</a>';
    },
    'patientViewAnchorTag': function(study_id, patient_id) {
	var href = cbio.util.getLinkToPatientView(study_id, patient_id);
	return '<a href="' + href + '" target="_blank">' + patient_id + '</a>';
    },
    'makeGenePanelPopupLink': function(gene_panel_id) {
	var anchor = $('<a href="#" oncontextmenu="return false;">'+gene_panel_id+'</a>');
	anchor.ready(anchor.click(function() {
	    window.cbioportal_client.getGenePanelsByPanelId({panel_id:[gene_panel_id]}).then(function(response) {
		var genes = response[0].genes.map(function(g) { return g.hugoGeneSymbol; }).sort();
		var popup = open("", "_blank", "width=500,height=500");
		var div = popup.document.createElement("div");
		popup.document.body.appendChild(div);
		
		$('<h3 style="text-align:center;">'+gene_panel_id+'</h3><br>').appendTo(div);
		$('<span>'+genes.join("<br>")+'</span>').appendTo(div);
	    });
	}));
	return anchor;
    },
    'makeHeatmapTrackTooltip': function(genetic_alteration_type, data_type, link_id) {
	return function (d) {
	    var data_header = '';
	    var profile_data = 'NaN';
	    if (genetic_alteration_type === "MRNA_EXPRESSION") {
		data_header = 'MRNA: ';
	    } else if (genetic_alteration_type === "PROTEIN_LEVEL") {
		data_header = 'PROT: ';
	    }
	    if (d.profile_data) {
		profile_data = d.profile_data.toString();
	    }
	    var ret = data_header + '<b>' + profile_data + '</b><br>';
	    ret += (data_type === 'sample' ? (link_id ? tooltip_utils.sampleViewAnchorTag(d.study, d.sample) : d.sample) : (link_id ? tooltip_utils.patientViewAnchorTag(d.study, d.patient) : d.patient));
	    return ret;
	};
    },
    'makeGeneticTrackTooltip':function(data_type, link_id) {
	var listOfMutationOrFusionDataToHTML = function(data) {
	    return data.map(function(d) {
		var ret = $('<span>');
		ret.append('<b>'+d.amino_acid_change+'</b>');
		if (d.cancer_hotspots_hotspot) {
		    ret.append('<img src="images/cancer-hotspots.svg" title="Hotspot" style="height:11px; width:11px; margin-left:3px"/>');
		}
		if (d.oncokb_oncogenic) {
		    ret.append('<img src="images/oncokb-oncogenic-1.svg" title="'+d.oncokb_oncogenic+'" style="height:11px; width:11px;margin-left:3px"/>');
		}
		//If we have data for the binary custom driver annotations, append an icon to the tooltip with the annotation information
		if (d.driver_filter && showBinaryCustomDriverAnnotation === "true") {
		    ret.append('<img src="images/driver.png" title="'+d.driver_filter+': '+d.driver_filter_annotation+'" alt="driver filter" style="height:11px; width:11px;margin-left:3px"/>');
		}
		//If we have data for the class custom driver annotations, append an icon to the tooltip with the annotation information
		if (d.driver_tiers_filter && showTiersCustomDriverAnnotation === "true") {
		    ret.append('<img src="images/driver_tiers.png" title="'+d.driver_tiers_filter+': '+d.driver_tiers_filter_annotation+'" alt="driver tiers filter" style="height:11px; width:11px;margin-left:3px"/>');
		}
		return ret;
	    });
	};
	var listOfCNAToHTML = function(data) {
	    return data.map(function(d) {
		var ret = $('<span>');
		ret.append('<b>'+d.cna+'</b>');
		if (d.oncokb_oncogenic) {
		    ret.append('<img src="images/oncokb-oncogenic-1.svg" title="'+d.oncokb_oncogenic+'" style="height:11px; width:11px;margin-left:3px"/>');
		}
		return ret;
	    });
	};
	var oncogenic = ["likely oncogenic", "predicted oncogenic", "oncogenic"];
	return function (d) {
	    var ret = $('<div>');
	    var mutations = [];
	    var cna = [];
	    var mrna = [];
	    var prot = [];
	    var fusions = [];
	    for (var i = 0; i < d.data.length; i++) {
		var datum = d.data[i];
		if (datum.genetic_alteration_type === "MUTATION_EXTENDED") {
		    var tooltip_datum = {'amino_acid_change': datum.amino_acid_change, 'driver_filter': datum.driver_filter,
			                 'driver_filter_annotation': datum.driver_filter_annotation, 'driver_tiers_filter': datum.driver_tiers_filter,
			                 'driver_tiers_filter_annotation': datum.driver_tiers_filter_annotation};
		    if (datum.cancer_hotspots_hotspot) {
			tooltip_datum.cancer_hotspots_hotspot = true;
		    }
		    if (typeof datum.oncokb_oncogenic !== "undefined" && oncogenic.indexOf(datum.oncokb_oncogenic) > -1) {
			tooltip_datum.oncokb_oncogenic = datum.oncokb_oncogenic;
		    }
		    (datum.oncoprint_mutation_type === "fusion" ? fusions : mutations).push(tooltip_datum);
		} else if (datum.genetic_alteration_type === "COPY_NUMBER_ALTERATION") {
		    var disp_cna = {'-2': 'HOMODELETED', '-1': 'HETLOSS', '1': 'GAIN', '2': 'AMPLIFIED'};
		    if (disp_cna.hasOwnProperty(datum.profile_data)) {
			var tooltip_datum = {
			    cna: disp_cna[datum.profile_data]
			};
			if (typeof datum.oncokb_oncogenic !== "undefined" && oncogenic.indexOf(datum.oncokb_oncogenic) > -1) {
			    tooltip_datum.oncokb_oncogenic = datum.oncokb_oncogenic;
			}
			cna.push(tooltip_datum);
		    }
		} else if (datum.genetic_alteration_type === "MRNA_EXPRESSION" || datum.genetic_alteration_type === "PROTEIN_LEVEL") {
		    if (datum.oql_regulation_direction) {
			(datum.genetic_alteration_type === "MRNA_EXPRESSION" ? mrna : prot)
				.push(datum.oql_regulation_direction === 1 ? "UPREGULATED" : "DOWNREGULATED");
		    }
		}
	    }
	    if (fusions.length > 0) {
		ret.append('Fusion: ');
		fusions = listOfMutationOrFusionDataToHTML(fusions);
		for (var i = 0; i < fusions.length; i++) {
		    if (i > 0) {
			ret.append(",");
		    }
		    ret.append(fusions[i]);
		}
		ret.append('<br>');
	    }
	    if (mutations.length > 0) {
		ret.append('Mutation: ');
		mutations = listOfMutationOrFusionDataToHTML(mutations);
		for (var i = 0; i < mutations.length; i++) {
		    if (i > 0) {
			ret.append(",");
		    }
		    ret.append(mutations[i]);
		}
		ret.append('<br>');
	    }
	    if (cna.length > 0) {
		ret.append('Copy Number Alteration: ');
		cna = listOfCNAToHTML(cna);
		for (var i = 0; i < cna.length; i++) {
		    if (i > 0) {
			ret.append(",");
		    }
		    ret.append(cna[i]);
		}
		ret.append('<br>');
	    }
	    if (mrna.length > 0) {
		ret.append('MRNA: <b>' + mrna.join(", ") + '</b><br>');
	    }
	    if (prot.length > 0) {
		ret.append('PROT: <b>' + prot.join(", ") + '</b><br>');
	    }
	    if (typeof d.coverage !== "undefined") {
		ret.append("Coverage: ");
		var coverage_elts = d.coverage.filter(function (x) {
		    return x !== "1"; /* code for whole-exome seq */
		}).map(function (id) {
		    return tooltip_utils.makeGenePanelPopupLink(id);
		});
		if (d.coverage.indexOf("1") > -1) {
		    coverage_elts.push("Whole-Exome Sequencing");
		}
		for (var i = 0; i < coverage_elts.length; i++) {
		    if (i > 0) {
			ret.append(",");
		    }
		    ret.append(coverage_elts[i]);
		}
		ret.append("<br>");
	    }
	    if (d.na) {
		ret.append('Not sequenced');
		ret.append('<br>');
	    }
	    ret.append((data_type === 'sample' ? (link_id ? tooltip_utils.sampleViewAnchorTag(d.study_id, d.sample) : d.sample) : (link_id ? tooltip_utils.patientViewAnchorTag(d.study_id, d.patient) : d.patient)));
	    return ret;
	};
    },
    'makeClinicalTrackTooltip':function(attr, data_type, link_id) {
	return function(d) {
	    var ret = '';
	    if (attr.attr_id === "NO_CONTEXT_MUTATION_SIGNATURE") {
		for (var i=0; i<attr.categories.length; i++) {
		    ret += '<span style="color:' + attr.fills[i] + ';font-weight:bold;">'+attr.categories[i]+'</span>: '+d.attr_val_counts[attr.categories[i]]+'<br>';
		}
	    } else {
		var attr_vals = ((d.attr_val_counts && Object.keys(d.attr_val_counts)) || []);
		if (attr_vals.length > 1) {
		    ret += 'values:<br>';
		    for (var i = 0; i < attr_vals.length; i++) {
			var val = attr_vals[i];
			ret += '<b>' + val + '</b>: ' + d.attr_val_counts[val] + '<br>';
		    }
		} else if (attr_vals.length === 1) {
		    ret += 'value: <b>' + attr_vals[0] + '</b><br>';
		}
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
	var fusion_key = 'disp_fusion';
	var cna_key = 'disp_cna';
	var cna_order = makeComparatorMetric(['amp', 'homdel', 'gain', 'hetloss', 'diploid', undefined]);
	var mut_type_key = 'disp_mut';
	var mut_order = (function () {
	    var _order;
	    if (!distinguish_mutation_types && !distinguish_recurrent) {
		return function (m) {
		    return ({'true': 1, 'false': 2})[!!m];
		}
	    } else if (!distinguish_mutation_types && distinguish_recurrent) {
		_order = makeComparatorMetric([['inframe_rec', 'missense_rec', 'promoter_rec', 'trunc_rec', 'inframe', 'promoter', 'trunc',], 'missense', undefined]); 
	    } else if (distinguish_mutation_types && !distinguish_recurrent) {
		_order = makeComparatorMetric([['trunc', 'trunc_rec'], ['inframe','inframe_rec'], ['promoter', 'promoter_rec'], ['missense', 'missense_rec'], undefined, true, false]);
	    } else if (distinguish_mutation_types && distinguish_recurrent) {
		_order = makeComparatorMetric(['trunc_rec', 'inframe_rec', 'promoter_rec', 'missense_rec', 'trunc', 'inframe', 'promoter', 'missense',  undefined, true, false]);
	    }
	    return function(m) {
		return _order[m];
	    }
	})();
	var mrna_key = 'disp_mrna';
	var rppa_key = 'disp_prot';
	var regulation_order = makeComparatorMetric(['up', 'down', undefined]);

	var mandatory = function(d1, d2) {
	    // Test fusion
	    if (d1[fusion_key] && !(d2[fusion_key])) {
		return -1;
	    } else if (!(d1[fusion_key]) && d2[fusion_key]) {
		return 1;
	    }
	    
	    // Next, CNA
	    var cna_diff = utils.sign(cna_order[d1[cna_key]] - cna_order[d2[cna_key]]);
	    if (cna_diff !== 0) {
		return cna_diff;
	    }

	    // Next, mutation type
	    var mut_type_diff = utils.sign(mut_order(d1[mut_type_key]) - mut_order(d2[mut_type_key]));
	    if (mut_type_diff !== 0) {
		return mut_type_diff;
	    }

	    // Next, mrna expression
	    var mrna_diff = utils.sign(regulation_order[d1[mrna_key]] - regulation_order[d2[mrna_key]]);
	    if (mrna_diff !== 0) {
		return mrna_diff;
	    }

	    // Next, protein expression
	    var rppa_diff = utils.sign(regulation_order[d1[rppa_key]] - regulation_order[d2[rppa_key]]);
	    if (rppa_diff !== 0) {
		return rppa_diff;
	    }

	    // If we reach this point, there's no order difference
	    return 0;
	}
	var preferred = function (d1, d2) {
	    // First, test if either is not sequenced
	    var ns_diff = utils.sign(+(!!d1.na) - (+(!!d2.na)));
	    if (ns_diff !== 0) {
		return ns_diff;
	    }
	    
	    return mandatory(d1, d2);
	};
	return {
	    preferred: preferred,
	    mandatory: mandatory
	};
    },
    'makeNumericalComparator': function (value_key) {
	return function (d1, d2) {
	    if (d1.na && d2.na) {
		return 0;
	    } else if (d1.na && !d2.na) {
		return 2;
	    } else if (!d1.na && d2.na) {
		return -2;
	    } else {
		return (d1[value_key] < d2[value_key] ? -1 : (d1[value_key] === d2[value_key] ? 0 : 1));
	    }
	};
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
    },
    'makeCountsMapClinicalComparator': function(categories) {
	return function (d1, d2) {
	    if (d1.na && d2.na) {
		return 0;
	    } else if (d1.na && !d2.na) {
		return 2;
	    } else if (!d1.na && d2.na) {
		return -2;
	    } else {
		var d1_total = 0;
		var d2_total = 0;
		for (var i = 0; i < categories.length; i++) {
		    d1_total += (d1.attr_val[categories[i]] || 0);
		    d2_total += (d2.attr_val[categories[i]] || 0);
		}
		if (d1_total === 0 && d2_total === 0) {
		    return 0;
		} else if (d1_total === 0) {
		    return 1;
		} else if (d2_total === 0) {
		    return -1;
		} else {
		    var d1_max_category = 0;
		    var d2_max_category = 0;
		    for (var i=0; i<categories.length; i++) {
			if (d1.attr_val[categories[i]] > d1.attr_val[categories[d1_max_category]]) {
			    d1_max_category = i;
			}
			if (d2.attr_val[categories[i]] > d2.attr_val[categories[d2_max_category]]) {
			    d2_max_category = i;
			}
		    }
		    if (d1_max_category < d2_max_category) {
			return -1;
		    } else if (d1_max_category > d2_max_category) {
			return 1;
		    } else {
			var cmp_category = categories[d1_max_category];
			var d1_prop = d1.attr_val[cmp_category]/d1_total;
			var d2_prop = d2.attr_val[cmp_category]/d2_total;
			return utils.sign(d1_prop - d2_prop);
		    }
		}
	    }
	}
    }
	
};
comparator_utils.numericalClinicalComparator = comparator_utils.makeNumericalComparator('attr_val');
comparator_utils.heatmapComparator = comparator_utils.makeNumericalComparator('profile_data');

	
window.CreateCBioPortalOncoprintWithToolbar = function (ctr_selector, toolbar_selector) {
    
    $('#oncoprint #everything').show();
    $('#oncoprint #oncoprint-diagram-toolbar-buttons').show();
    
    if (!utils.isWebGLAvailable()) {
	$(ctr_selector).append("<p>"+utils.getUnavailableMessageHTML()+"</p>");
	$(toolbar_selector).hide();
	return;
    }
    
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
	    'update': function(proportion, color) {
		$loading_bar.attr('width', proportion*parseFloat($loading_bar_svg.attr('width')));
		if (color) {
			$loading_bar.attr('stroke', 'black');
			$loading_bar.attr('fill', color);
		}
	    },
	    'DOWNLOADING_MSG': 'Downloading data..'
	};
    })();
    
    LoadingBar.hide();
    
    var oncoprint = new window.Oncoprint(ctr_selector, 1050);
    var toolbar_fade_out_timeout;
    $(toolbar_selector).css({'visibility':'visible'});
    $(ctr_selector).add(toolbar_selector).on("mouseover", function(evt) {
	$(toolbar_selector).stop().animate({opacity:1});
	clearTimeout(toolbar_fade_out_timeout);
    });
    $(ctr_selector).add(toolbar_selector).on("mouseleave", function(evt) {
	clearTimeout(toolbar_fade_out_timeout);
	toolbar_fade_out_timeout = setTimeout(function() {
	    $(toolbar_selector).stop().animate({opacity:0});
	}, 700);
    });
    
    var URL = (function() {
	var changeURLParam = function (param, new_value, url) {
		var index = url.indexOf(param + '=');
		var before_url, after_url;
		if (index === -1) {
			before_url = url;
			var indexOfQuestionMark = url.indexOf('?');
			if (indexOfQuestionMark === -1) {
				before_url += "?";
			} else if (before_url[before_url.length - 1] !== "&") {
				before_url += "&";
			}
			after_url = "";
			index = url.length;
		} else {
			before_url = url.substring(0, index);
			var next_amp = url.indexOf("&", index);
			if (next_amp === -1) {
				next_amp = url.length;
			}
			after_url= url.substring(next_amp);
		}
		return before_url
			+ (new_value.length > 0 ? (param + '=' + new_value) : "")
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
	
	var CLINICAL_ATTRS_PARAM = "clinicallist";
	var SAMPLE_DATA_PARAM = "show_samples";
	var HEATMAP_TRACK_GROUPS_PARAM = "heatmap_track_groups";
	
	var init_show_samples = getParamValue(SAMPLE_DATA_PARAM);
	var init_clinical_attrs = getParamValue(CLINICAL_ATTRS_PARAM);
	var init_heatmap_track_groups = getParamValue(HEATMAP_TRACK_GROUPS_PARAM);
	
	var getHeatmapParamValue = function(heatmap_track_groups) {
	    var track_index = oncoprint.getTracks().reduce(function(map, next_id, index) {
		map[next_id] = index;
		return map;
	    }, {});
	    var encodeHeatmapGroup = function(group) {
		var sorted_genes = Object.keys(group.gene_to_track_id).map(function(gene) {
		    return {gene: gene, track_id: group.gene_to_track_id[gene]};
		}).sort(function(pairA, pairB) {
		    return track_index[pairA.track_id] - track_index[pairB.track_id];
		}).map(function(pair) {
		    return pair.gene;
		});
		return [group.genetic_profile_id].concat(sorted_genes).join(",");
	    };
	    var heatmap_groups_in_order = Object.keys(heatmap_track_groups).map(function (gp_id) {
		return State.heatmap_track_groups[gp_id];
	    }).sort(function (grpA, grpB) {
		return grpA.track_group_id - grpB.track_group_id;
	    });
	    return heatmap_groups_in_order.map(encodeHeatmapGroup).join(";");
	};
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
		new_url = changeURLParam(HEATMAP_TRACK_GROUPS_PARAM,
					encodeURIComponent(getHeatmapParamValue(State.heatmap_track_groups)),
					new_url);
		window.history.replaceState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", new_url);
	    },
	    'getInitDataType': function() {
		if (init_show_samples === null) {
		    // If no parameter is specified in the url, take the configured value
		    return defaultOncoprintView;
		} else {
		    return (init_show_samples === 'true' ? 'sample' : 'patient');
		}
	    },
	    'getInitUsedClinicalAttrs': function() {
		var attrs = (init_clinical_attrs || "").trim().split(",").map(decodeURIComponent);
		if (attrs.indexOf("CANCER_STUDY") === -1 &&
			QuerySession.getCancerStudyIds().length > 1) {
		    attrs.push("CANCER_STUDY");
		}
		if (!attrs.length) {
		    return null;
		} else {
		    return attrs;
		}
	    },
	    'getInitHeatmapTrackGroups': function() {
		if (init_heatmap_track_groups === null) {
		    return null;
		} else {
		    return decodeURIComponent(init_heatmap_track_groups.trim()).split(";").map(function(heatmap_section) {
			return heatmap_section.trim().split(",").filter(function(x) { return x.length > 0; });
		    }).map(function (heatmap_section) {
			return {'genetic_profile_id': heatmap_section[0],
				'genes': heatmap_section.slice(1)};
		    });
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
	var oncoprintDatumIsAltered = function(datum) {
	    return ['disp_mut', 'disp_cna', 'disp_mrna', 'disp_prot', 'disp_fusion']
		    .map(function(x) { return (typeof datum[x] !== "undefined"); })
		    .reduce(function(x,y) { return x || y; }, false);
	};
	var populateSampleData = function() {
	    var done = new $.Deferred();
	    var clinical_attrs = utils.objectValues(State.clinical_tracks);
	    LoadingBar.show();
	    LoadingBar.msg(LoadingBar.DOWNLOADING_MSG);
	    console.log("in populateSampleData, loading initial data");
	    $.when(QuerySession.getOncoprintSampleGenomicEventData(true),
		    ClinicalData.getSampleData(clinical_attrs),
		    HeatmapData.getSampleData(),
		    QuerySession.getAlteredSampleUIDs(true),
		    QuerySession.getUnalteredSampleUIDs(true))
		    .then(function (oncoprint_data_by_line,
				    clinical_data,
				    heatmap_data,
				    altered_sample_uids,
				    unaltered_sample_uids) {
		console.log("in populateSampleData, initial data loaded, starting Oncoprint");
			LoadingBar.msg("Loading oncoprint");
			oncoprint.suppressRendering();
			oncoprint.hideIds([], true);
			oncoprint.keepSorted(false);
			
			var total_tracks_to_add = Object.keys(State.genetic_alteration_tracks).length
						+ heatmap_data.length
						+ Object.keys(State.clinical_tracks).length;
			
			utils.timeoutSeparatedLoop(Object.keys(State.genetic_alteration_tracks), function (track_line, i) {
			    var track_id = State.genetic_alteration_tracks[track_line];
			    var oncoprint_data_frame = oncoprint_data_by_line[track_line];
			    var track_data = utils.deepCopyObject(oncoprint_data_frame.oncoprint_data);
			    console.log("populating sample data for alteration track " + oncoprint_data_by_line[track_line].gene);
			    oncoprint.setTrackData(track_id, track_data, 'uid');
			    var track_info;
			    if (oncoprint_data_frame.sequenced_samples.length > 0) { 
				track_info = utils.proportionToPercentString(oncoprint_data_frame.altered_samples.length / oncoprint_data_frame.sequenced_samples.length);
			    } else {
				track_info = "N/S";
			    }
			    oncoprint.setTrackInfo(track_id, track_info);
			    oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeGeneticTrackTooltip('sample', true));
			    LoadingBar.update(i / total_tracks_to_add);
			}).then(function () {
				return utils.timeoutSeparatedLoop(heatmap_data, function(heatmap_track_data, i) {
					var track_id = State.heatmap_track_groups[heatmap_track_data.genetic_profile_id].gene_to_track_id[heatmap_track_data.gene];
					console.log("heatmap data retrieved, populating sample data for track " + heatmap_track_data.gene);
					oncoprint.setTrackData(track_id, heatmap_track_data.oncoprint_data, 'uid');
					oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeHeatmapTrackTooltip(heatmap_track_data.genetic_alteration_type, 'sample', true));
					LoadingBar.update((i + Object.keys(State.genetic_alteration_tracks).length) / total_tracks_to_add);
				    });
			}).then(function () {
			    return utils.timeoutSeparatedLoop(Object.keys(State.clinical_tracks), function(track_id, i) {
				var attr = State.clinical_tracks[track_id];
				console.log("populating sample data for clinical track " + attr.attr_id);
				oncoprint.setTrackData(track_id, clinical_data[attr.attr_id], 'uid');
				oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeClinicalTrackTooltip(attr, 'sample', true));
				oncoprint.setTrackInfo(track_id, "");
				LoadingBar.update((i + heatmap_data.length + Object.keys(State.genetic_alteration_tracks).length) / total_tracks_to_add);
			    });
			}).then(function () {
			    console.log("sample data populated, releasing rendering");
			    oncoprint.keepSorted();
			    if (State.unaltered_cases_hidden) {
				oncoprint.hideIds(unaltered_sample_uids, true);
			    }
			    oncoprint.releaseRendering();
			    LoadingBar.msg("");
			    LoadingBar.hide();
			    updateAlteredPercentIndicator(State);
			    oncoprint.updateHorzZoomToFitIds(altered_sample_uids);
			    done.resolve();
			});
		    }).fail(function() {
			done.reject();
		    });
	    return done.promise();
	};
	
	var populatePatientData = function() {
	    var done = new $.Deferred();
	    var clinical_attrs = utils.objectValues(State.clinical_tracks);
	    LoadingBar.show();
	    LoadingBar.msg(LoadingBar.DOWNLOADING_MSG);
	    console.log("in populatePatientData, loading initial data");
	    $.when(QuerySession.getOncoprintPatientGenomicEventData(true),
		    ClinicalData.getPatientData(clinical_attrs),
		    HeatmapData.getPatientData(),
		    QuerySession.getPatientIds(),
		    QuerySession.getAlteredPatientUIDs(true),
		    QuerySession.getUnalteredPatientUIDs(true))
		    .then(function (oncoprint_data_by_line, 
				    clinical_data,
				    heatmap_data,
				    patient_ids,
				    altered_patient_uids,
				    unaltered_patient_uids) {
			console.log("in populatePatientData, initial data loaded, starting Oncoprint");
			LoadingBar.msg("Loading oncoprint");
			oncoprint.suppressRendering();
			oncoprint.hideIds([], true);
			oncoprint.keepSorted(false);
			
			var total_tracks_to_add = Object.keys(State.genetic_alteration_tracks).length
						+ heatmap_data.length
						+ Object.keys(State.clinical_tracks).length;
			
			utils.timeoutSeparatedLoop(Object.keys(State.genetic_alteration_tracks), function (track_line, i) {
			    var track_id = State.genetic_alteration_tracks[track_line];
			    var oncoprint_data_frame = oncoprint_data_by_line[track_line];
			    var track_data = utils.deepCopyObject(oncoprint_data_frame.oncoprint_data);
			    console.log("populating patient data for alteration track " + oncoprint_data_by_line[track_line].gene);
			    oncoprint.setTrackData(track_id, track_data, 'uid'); 
			    var track_info;
			    if (oncoprint_data_frame.sequenced_patients.length > 0) { 
				track_info = utils.proportionToPercentString(oncoprint_data_frame.altered_patients.length / oncoprint_data_frame.sequenced_patients.length);
			    } else {
				track_info = "N/S";
			    }
			    oncoprint.setTrackInfo(track_id, track_info);
			    oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeGeneticTrackTooltip('patient', true));
			    LoadingBar.update(i / total_tracks_to_add);
			}).then(function () {
			    return utils.timeoutSeparatedLoop(heatmap_data, function(heatmap_track_data, i) {
					var track_id = State.heatmap_track_groups[heatmap_track_data.genetic_profile_id].gene_to_track_id[heatmap_track_data.gene];
					console.log("heatmap data retrieved, populating sample data for track " + heatmap_track_data.gene);
					oncoprint.setTrackData(track_id, heatmap_track_data.oncoprint_data, 'uid');
					oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeHeatmapTrackTooltip(heatmap_track_data.genetic_alteration_type, 'patient', true));
					LoadingBar.update((i + Object.keys(State.genetic_alteration_tracks).length) / total_tracks_to_add);
				    });
			}).then(function () {
			    return utils.timeoutSeparatedLoop(Object.keys(State.clinical_tracks), function(track_id, i) {
				var attr = State.clinical_tracks[track_id];
				console.log("populating patient data for clinical track " + attr.attr_id);
				oncoprint.setTrackData(track_id, clinical_data[attr.attr_id], 'uid');
				oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeClinicalTrackTooltip(attr, 'patient', true));
				oncoprint.setTrackInfo(track_id, "");
				LoadingBar.update((i + heatmap_data.length + Object.keys(State.genetic_alteration_tracks).length) / total_tracks_to_add);
			    });
			}).then(function () {
			    console.log("patient data populated");
			    console.log("sorting");
			    oncoprint.keepSorted();
			    if (State.unaltered_cases_hidden) {
				console.log("hiding unaltered cases");
				oncoprint.hideIds(unaltered_patient_uids, true);
			    }
			    console.log("releasing rendering");
			    oncoprint.releaseRendering();
			    LoadingBar.msg("");
			    LoadingBar.hide();
			    updateAlteredPercentIndicator(State);   					
			    console.log("setting horz zoom to fit");
			    oncoprint.updateHorzZoomToFitIds(altered_patient_uids);
			    done.resolve();
			});
		    }).fail(function() {
			done.reject();
		    });
	    return done.promise();
	};
	
	var populateHeatmapTrack = function(genetic_profile_id, gene, track_id) {
	    var done = new $.Deferred();
	    QuerySession[State.using_sample_data ? 'getSampleHeatmapData' : 'getPatientHeatmapData'](genetic_profile_id, [gene]).then(function(data) {
		data = data[0];
		oncoprint.setTrackData(track_id, data.oncoprint_data, 'uid');
		oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeHeatmapTrackTooltip(data.genetic_alteration_type, (State.using_sample_data ? 'sample' : 'patient'), true));
		done.resolve();
	    }).fail(function() {
		done.resolve();
	    });
	    return done.promise();
	};
	
	var populateClinicalTrack = function(track_id) {
	    var done = new $.Deferred();
	    var attr = State.clinical_tracks[track_id];
	    ClinicalData[State.using_sample_data ? 'getSampleData' : 'getPatientData'](attr).then(function(data) {
		data = data[attr.attr_id];
		oncoprint.setTrackData(track_id, data, "uid");
		oncoprint.setTrackTooltipFn(track_id, tooltip_utils.makeClinicalTrackTooltip(attr, (State.using_sample_data ? 'sample' : 'patient'), true));
		oncoprint.setTrackInfo(track_id, "");
		done.resolve();
	    }).fail(function() {
		done.reject();
	    });
	    return done.promise();
	};
	
	var makeRemoveHeatmapHandler = function(genetic_profile_id, gene) {
	    return function(track_id) {
		var track_group = State.heatmap_track_groups[genetic_profile_id];
		//update State.trackIdsInOriginalOrder (if this has been initialized before by clustering):
		var heatmap_track_group_id = track_group.track_group_id;
		if (State.trackIdsInOriginalOrder[heatmap_track_group_id]) {
		    var idx = State.trackIdsInOriginalOrder[heatmap_track_group_id].indexOf(track_id);
		    State.trackIdsInOriginalOrder[heatmap_track_group_id].splice(idx, 1);
		}
		//update track_group
		delete track_group.gene_to_track_id[gene];
		if (Object.keys(track_group.gene_to_track_id).length === 0) {
		    //if no more tracks are left, delete the whole group
		    delete State.heatmap_track_groups[genetic_profile_id];
		    delete State.trackIdsInOriginalOrder[heatmap_track_group_id];
		}
		URL.update();
	    };
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
	
	var updateAlteredPercentIndicator = function(state) {
	    $.when(QuerySession.getSequencedSamples(), QuerySession.getSequencedPatients(), QuerySession.getPatientIds())
		    .then(function(sequenced_samples, sequenced_patients, patients) {
			var altered_ids = state.getAlteredIds();
			var sequenced_ids = (state.using_sample_data ? sequenced_samples : sequenced_patients);
			var text = "Altered in ";
			text += altered_ids.length;
			text += " (";
			text += utils.proportionToPercentString(altered_ids.length / sequenced_ids.length);
			text +=") of ";
			text += sequenced_ids.length;
			text += " sequenced ";
			text += (state.using_sample_data ? "samples" : "cases/patients");
			text += " ("+(state.using_sample_data ? QuerySession.getSampleIds().length : patients.length)+" total)";
			$('#altered_value').text(text);
	    });
	};
	
	var local_storage_minimap_var = "oncoprintState.is_minimap_shown";
	var saveToLocalStorage = function(state) {
	    if (Storage) {
		//localStorage.setItem(local_storage_minimap_var, state.is_minimap_shown);
	    }
	};
	var loadFromLocalStorage = function(state) {
	    if (Storage) {
		//state.is_minimap_shown = (localStorage.getItem(local_storage_minimap_var) === "true");
	    }
	};
	
	var State = {
	    'first_genetic_alteration_track': null,
	    'genetic_alteration_tracks': {}, // oql line -> track_id
	    'heatmap_track_groups': {}, // genetic_profile_id -> {genetic_profile_id, track_group_id, gene_to_track_id}
	    'clinical_tracks': {}, // track_id -> attr
	    
	    'used_clinical_attributes': [],
	    'unused_clinical_attributes': [],
	    'clinical_attributes_fetched': new $.Deferred(),
	    'clinical_attr_id_to_sample_data': {},
	    'clinical_attr_id_to_patient_data': {},
	    
	    'cell_padding_on': true,
	    'using_sample_data': (URL.getInitDataType() === 'sample'),
	    'unaltered_cases_hidden': false,
	    'clinical_track_legends_shown': true,
	    'mutations_colored_by_type': true,
	    'sorted_by_mutation_type': true,
	    
	    'trackIdsInOriginalOrder': {},
	    
	    'sortby': 'data',
	    'sortby_type': true,
	    'sortby_recurrence': true,
	    
	    'colorby_type': true,
	    'colorby_knowledge': true,
	    
	    
	    'sorting_by_given_order': false,
	    'sorting_alphabetically': false,
	    
	    'is_minimap_shown': false,
	    
	    'getAnyHeatmapTrackId': function() {
		var ret = null;
		var heatmap_profile_ids = Object.keys(this.heatmap_track_groups);
		for (var i=0; i<heatmap_profile_ids.length; i++) {
		    var gene_to_track_id = this.heatmap_track_groups[heatmap_profile_ids[i]].gene_to_track_id;
		    var genes_with_tracks = Object.keys(gene_to_track_id);
		    if (genes_with_tracks.length > 0) {
			ret = gene_to_track_id[genes_with_tracks[0]];
			break;
		    }
		}
		return ret;
	    },
	    'getNewTrackGroupId': function() {
		// Return 1 more than the max heatmap track group id, minimum 2
		var heatmap_genetic_profiles = Object.keys(this.heatmap_track_groups);
		return Math.max(Math.max.apply(null, heatmap_genetic_profiles.map(function(id) { return State.heatmap_track_groups[id].track_group_id; })) + 1, 2);
	    },	    
	    
	    'toggleMinimapShown': function(opt_val) {
		if (typeof opt_val !== "undefined") {
		    this.is_minimap_shown = opt_val;
		} else {
		    this.is_minimap_shown = !this.is_minimap_shown;
		}
		saveToLocalStorage(this);
	    },
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
	    'refreshData': function() {
		if (this.using_sample_data) {
		    return populateSampleData();
		} else {
		    return populatePatientData();
		}
	    },
	    'setDataType': function (sample_or_patient) {
		console.log("in setDataType");
		var def = new $.Deferred();
		var self = this;
		$.when(QuerySession.getSamples(), QuerySession.getPatients()).then(function (samples, patients) {
		    // TODO: assume multiple studies
		    var proxy_promise;
		    if (sample_or_patient === 'sample') {
			self.using_sample_data = true;
			URL.update();
			updateAlteredPercentIndicator(self);
			console.log("in setDataType, calling populateSampleData()");
			proxy_promise = populateSampleData();
		    } else if (sample_or_patient === 'patient') {
			self.using_sample_data = false;
			URL.update();
			updateAlteredPercentIndicator(self);
			console.log("in setDataType, calling populatePatientData()");
			proxy_promise = populatePatientData();
		    }
		    var id_order = (self.using_sample_data ? samples : patients);
		    if (self.sorting_alphabetically) {
			id_order = _.sortBy(id_order, function(x) { return x.id; });
		    }
		    if (self.sorting_alphabetically || self.sorting_by_given_order) {
			setSortOrder(id_order.map(function(x) { return x.uid;}));
		    }
		    proxy_promise.then(function () {
			def.resolve();
		    }).fail(function () {
			def.fail();
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
			'description': oncoprint_data_by_line[i].oql_line,
			'na_z': 1.1
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
	    'addHeatmapTrack': function (genetic_profile_id, gene) {
		oncoprint.suppressRendering();
		State.heatmap_track_groups[genetic_profile_id] = State.heatmap_track_groups[genetic_profile_id] || {
		    genetic_profile_id: genetic_profile_id,
		    track_group_id: State.getNewTrackGroupId(),
		    gene_to_track_id: {}
		};
		var heatmap_track_group = State.heatmap_track_groups[genetic_profile_id];
		    var track_params = {
			'rule_set_params': {
			    'type': 'gradient',
			    'legend_label': "Heatmap",
			    'value_key': 'profile_data',
			    'value_range': [-3, 3],
			    'colors': [[0,0,255,1],[0,0,0,1],[255,0,0,1]],
			    'value_stop_points': [-3,0,3],
			    'null_color': 'rgba(224,224,224,1)'
			},
			'has_column_spacing': false,
			'track_padding': 0,
			'label': gene,
			'target_group': heatmap_track_group.track_group_id,
			'removable': true,
			'removeCallback': makeRemoveHeatmapHandler(genetic_profile_id, gene),
			'sort_direction_changeable': true,
			'sortCmpFn': comparator_utils.heatmapComparator,
			'init_sort_direction': 0,
			'description': gene + ' data from ' + genetic_profile_id,
			//'track_group_header': genetic_profile_id
		    };
		    var any_hm_id = State.getAnyHeatmapTrackId();
		    var new_hm_id = oncoprint.addTracks([track_params])[0];
		    heatmap_track_group.gene_to_track_id[gene] = new_hm_id;
		    if (any_hm_id !== null) {
			oncoprint.shareRuleSet(any_hm_id, new_hm_id);
		    }
		URL.update();
		oncoprint.releaseRendering();
		return new_hm_id;
	    },
	    'addAndPopulateNonexistingHeatmapTracks': function(genetic_profile_id, genes) {
		var self = this;
		return $.when.apply(null, genes.map(function(gene) {
		    // If this track already exists, don't add it again
		    if (State.heatmap_track_groups[genetic_profile_id] && State.heatmap_track_groups[genetic_profile_id].gene_to_track_id.hasOwnProperty(gene)) {
			return $.when();
		    } else {
			var track_id = self.addHeatmapTrack(genetic_profile_id, gene);
			//update State.trackIdsInOriginalOrder (if this has been initialized before by clustering):
			var heatmap_track_group_id = State.heatmap_track_groups[genetic_profile_id].track_group_id;
			if (State.trackIdsInOriginalOrder[heatmap_track_group_id]) {
			    State.trackIdsInOriginalOrder[heatmap_track_group_id].push(track_id);
			}
			return populateHeatmapTrack(genetic_profile_id, gene, track_id);
		    }
		})).then(function () {
		    // Give the optionally sortable heatmap track groups a
		    // higher sort-by-data priority than the inherently sorted
		    // alteration track groups
		    var ordered_group_ids = (oncoprint.model.getTrackGroups()
			.map(function (__, group_index) { return group_index; })
			.filter(function(group_index) { return group_index != 1; }));
		    ordered_group_ids.push(1);
		    oncoprint.setTrackGroupSortPriority(ordered_group_ids);
		});
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
		    } else if (attr.attr_id === 'NO_CONTEXT_MUTATION_SIGNATURE') {
			track_params = {
			    'rule_set_params': {
				'type': 'stacked_bar',
				'value_key': 'attr_val_counts',
				'categories': attr.categories,
				'fills': attr.fills
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
		    track_params['sort_direction_changeable'] = true;
		    track_params['track_info'] = "\u23f3";
		    
		    if (attr.datatype.toLowerCase() === "number") {
			track_params['sortCmpFn'] = comparator_utils.numericalClinicalComparator;
		    } else if (attr.datatype.toLowerCase() === "string") {
			track_params['sortCmpFn'] = comparator_utils.stringClinicalComparator;
		    } else if (attr.datatype.toLowerCase() === "counts_map") {
			track_params['sortCmpFn'] = comparator_utils.makeCountsMapClinicalComparator(attr.categories);
		    }
		    
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
		return comparator_utils.makeGeneticComparator(this.colorby_type && this.sortby_type, this.colorby_knowledge && this.sortby_recurrence);
	    },
	    'getGeneticRuleSetParams': function() {
		if (this.colorby_type) {
		    if (this.colorby_knowledge) {
			return window.geneticrules.genetic_rule_set_different_colors_recurrence;
		    } else {
			return window.geneticrules.genetic_rule_set_different_colors_no_recurrence;
		    }
		} else {
		    if (this.colorby_knowledge) {
			return window.geneticrules.genetic_rule_set_same_color_for_all_recurrence;
		    } else {
			return window.geneticrules.genetic_rule_set_same_color_for_all_no_recurrence;
		    }
		}
	    },
	    'getAlteredIds': function() {
		var track_ids = utils.objectValues(State.genetic_alteration_tracks);
		var altered = {};
		for (var i=0; i<track_ids.length; i++){
		    var data = oncoprint.getTrackData(track_ids[i]);
		    var data_id_key = oncoprint.getTrackDataIdKey(track_ids[i]);
		    var altered_ids = data.filter(oncoprintDatumIsAltered).map(function(x) { return x[data_id_key]; });
		    for (var j=0; j<altered_ids.length; j++) {
			altered[altered_ids[j]] = true;
		    }
		}
		return Object.keys(altered);
	    },
	    'getUnalteredIds': function() {
		var track_ids = utils.objectValues(State.genetic_alteration_tracks);
		var unaltered = {};
		for (var i=0; i<track_ids.length; i++){
		    var data = oncoprint.getTrackData(track_ids[i]);
		    var data_id_key = oncoprint.getTrackDataIdKey(track_ids[i]);
		    if (i === 0) {
			var unaltered_ids = data.filter(function (d) {
			    return !oncoprintDatumIsAltered(d);
			}).map(function (x) {
			    return x[data_id_key];
			});
			for (var j = 0; j < unaltered_ids.length; j++) {
			    unaltered[unaltered_ids[j]] = true;
			}
		    } else {
			var altered_ids = data.filter(oncoprintDatumIsAltered)
				.map(function(x) { return x[data_id_key]; });
			for (var j=0; j<altered_ids.length; j++) {
			    unaltered[altered_ids[j]] = false;
			}
		    }
		}
		return Object.keys(unaltered).filter(function(x) { return !!unaltered[x]; });
	    },
	};
	
	loadFromLocalStorage(State);
	
	return State;
    })();
    
    var HeatmapData = {
	'getSampleData': function() {
	    var def = new $.Deferred();
	    var data = [];
	     $.when.apply(null, Object.keys(State.heatmap_track_groups).map(function(genetic_profile_id) {
		var grp = State.heatmap_track_groups[genetic_profile_id];
		var inner_def = new $.Deferred();
		QuerySession.getSampleHeatmapData(grp.genetic_profile_id, Object.keys(grp.gene_to_track_id)).then(function(d) {
		    data = data.concat(d);
		    inner_def.resolve();
		}).fail(function() {
		    inner_def.resolve();
		});
		return inner_def.promise();
	    })).then(function() {
		def.resolve(data);
	    }).fail(function() {
		def.reject();
	    });
	    return def.promise();
	},
	'getPatientData': function() {
	    var def = new $.Deferred();
	    var data = [];
	     $.when.apply(null, Object.keys(State.heatmap_track_groups).map(function(genetic_profile_id) {
		var grp = State.heatmap_track_groups[genetic_profile_id];
		var inner_def = new $.Deferred();
		QuerySession.getPatientHeatmapData(grp.genetic_profile_id, Object.keys(grp.gene_to_track_id)).then(function(d) {
		    data = data.concat(d);
		    inner_def.resolve();
		}).fail(function() {
		    inner_def.resolve();
		});
		return inner_def.promise();
	    })).then(function() {
		def.resolve(data);
	    }).fail(function() {
		def.reject();
	    });
	    return def.promise();
	}
    };
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
	    var set_attribute_order = ["FRACTION_GENOME_ALTERED", "# mutations", "CANCER_STUDY", "NO_CONTEXT_MUTATION_SIGNATURE"];
	    var attrA_index = set_attribute_order.indexOf(attrA.attr_id);
	    var attrB_index = set_attribute_order.indexOf(attrB.attr_id);
	    if (attrA_index < 0) {
		attrA_index = set_attribute_order.length;
	    }
	    if (attrB_index < 0) {
		attrB_index = set_attribute_order.length;
	    }
	    if (attrA_index === attrB_index) {
		return attrA.display_name.localeCompare(attrB.display_name);
	    } else {
		return utils.sign_of_diff(attrA_index, attrB_index);
	    }
	});

	for (var i = 0, _len = State.unused_clinical_attributes.length; i < _len; i++) {
	    State.unused_clinical_attributes[i].display_order = i;
	}
	
	var url_clinical_attr_ids = URL.getInitUsedClinicalAttrs() || [];
	for (var i=0; i<url_clinical_attr_ids.length; i++) {
	    State.useAttribute(url_clinical_attr_ids[i]);
	}
	
	if (url_clinical_attr_ids.length > 0) {
	    $(toolbar_selector + ' #oncoprint-diagram-showlegend-icon').show();
	    State.addClinicalTracks(State.used_clinical_attributes.filter(function (attr) {
		return url_clinical_attr_ids.indexOf(attr.attr_id) > -1;
	    }));
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
    
    var isAnyDriverLabellingDataSourceSelected = function() {
	result = true;
	var known_mutation_settings = QuerySession.getKnownMutationSettings();
	    var tiers = false;
	    Object.keys(known_mutation_settings.recognize_driver_tiers).forEach(function(tier) {
		if (known_mutation_settings.recognize_driver_tiers[tier] === true) {
		    tiers = true;
		}
	    });
	    if (!known_mutation_settings.recognize_hotspot && !known_mutation_settings.recognize_cbioportal_count
		    && !known_mutation_settings.recognize_cosmic_count && !known_mutation_settings.recognize_oncokb_oncogenic 
		    && !known_mutation_settings.recognize_driver_filter && !tiers) {
		result = false;
	    }
	return result;
    };

    (/**
      * Initializes the OncoPrint tracks, populates them and scrolls back.
      *
      * @returns {Promise} - promise that gets fulfilled when the
      * initialization process is complete, or rejected if data
      * could not be retrieved.
      */
     function initOncoprint() {
	LoadingBar.show();
	LoadingBar.msg(LoadingBar.DOWNLOADING_MSG);
	oncoprint.suppressRendering();
	oncoprint.setCellPaddingOn(State.cell_padding_on);
	console.log("in initOncoprint, fetching genomic event data");
	return QuerySession.getOncoprintSampleGenomicEventData()
	.then(function (oncoprint_data) {
	    if (!isAnyDriverLabellingDataSourceSelected()) {
		// If no data sources selected, turn off driver/passenger labeling..
		State.colorby_knowledge = false;
		// .. and filtering
		QuerySession.setKnownMutationSettings({ignore_unknown: false});
	    }
	    console.log("in initOncoprint, adding genomic event tracks");
	    State.addGeneticTracks(oncoprint_data);
	}).then(function () {
	    var promises = (URL.getInitHeatmapTrackGroups() || []).map(function(group) {
		return State.addAndPopulateNonexistingHeatmapTracks(group.genetic_profile_id, group.genes);
	    });
	    var ret = $.when.apply(null, promises);
	    return ret;
	}).then(function fetchClinicalAttributes() {
	    console.log("in initOncoprint, fetching clinical attributes");
	    QuerySession.getClinicalAttributes()
	    .then(function (attrs) {
		console.log("in initOncoprint, clinical attributes fetched");
		State.unused_clinical_attributes = attrs;
		State.clinical_attributes_fetched.resolve();
	    }).fail(function () {
		State.clinical_attributes_fetched.reject();
	    });
	    return State.clinical_attributes_fetched.promise();
	}).then(function () {
	    // specify sample or patient and populate data as appropriate
	    console.log("in initOncoprint, setting the data type to " + (State.using_sample_data ? 'sample' : 'patient'));
	    var dataPopulatedPromise = State.setDataType(State.using_sample_data ? 'sample' : 'patient');

	    // zoom out if many columns are selected
	    console.log("in initOncoprint, fetching altered cases while waiting for data to be populated");
	    return $.when(QuerySession.getPatientIds(),
		    QuerySession.getAlteredSampleUIDs(),
		    QuerySession.getAlteredPatientUIDs(),
		    dataPopulatedPromise)
	    .then(function (patient_ids,
		    altered_sample_uids,
		    altered_patient_uids) {
		console.log("in initOncoprint, altered cases fetched, setting zoom level");
		if ((State.using_sample_data ? QuerySession.getSampleIds() : patient_ids).length > 200) {
		    oncoprint.setHorzZoomToFit(State.using_sample_data ? altered_sample_uids : altered_patient_uids);
		}
		oncoprint.scrollTo(0);
	    });
	}).then(function() {
	    console.log("releasing rendering at end of oncoprint init");
	    oncoprint.releaseRendering();
	    console.log("setting minimap visible");
	    oncoprint.setMinimapVisible(State.is_minimap_shown);
	});
    })();
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

	var setUpToggleButton = function($elt, qtip_descs, is_on_fn, callback) {
	    is_on_fn = is_on_fn || function() { return false; };
	    var unhover_color = '#efefef';
	    var hover_color = '#d9d9d9';
	    var click_color = '#c7c7c7';
	    if (qtip_descs.length > 0) {
		addQTipTo($elt, {
		    content: {text: function () {
			    return qtip_descs[+(is_on_fn())];
			}},
		    position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		    style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		    show: {event: "mouseover"},
		    hide: {fixed: true, delay: 100, event: "mouseout"}
		});
	    }
	    var updateButton = function() {
		$elt.css({'background-color':(is_on_fn() ? click_color : unhover_color)});
	    };
	    Toolbar.onHover($elt, function() {
		$elt.css({'background-color': hover_color});
	    }, function() {
		updateButton();
	    });
	    Toolbar.onMouseDownAndClick($elt, function() {
		$elt.css({'background-color': click_color});
	    }, function() {
		callback();
		updateButton();
	    });
	    updateButton();
	    return updateButton;
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

	var resetHeatmapTracks = function() {
		//reset all tracks of heatmaps that were clustered to their original order:
		for (var clusteredHeatmapGroup in State.trackIdsInOriginalOrder) {
			if (State.trackIdsInOriginalOrder.hasOwnProperty(clusteredHeatmapGroup)) {
				oncoprint.setTrackGroupOrder(clusteredHeatmapGroup, State.trackIdsInOriginalOrder[clusteredHeatmapGroup]);
			}
		}
	};

	(function setUpHeatmap() {
	    if (QuerySession.getCancerStudyIds().length > 1) {
		// hide for multiple studies
		$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu').hide();
		return;
	    }
	    QuerySession.getHeatmapProfiles().then(function (profiles) {
		// Make a copy to modify here
		profiles = utils.deepCopyObject(profiles);
		// Sort, mRNA first
		profiles.sort(function(a,b) {
		    var order = {'MRNA_EXPRESSION':0, 'PROTEIN_LEVEL':1};
		    return order[a.genetic_alteration_type] - order[b.genetic_alteration_type];
		});
		// Add profile dropdown options
		if (profiles.length === 0) {
		    // Hide menu if no heatmaps available
		    $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu').hide();
		    return;
		}
		// See if any of the heatmap profiles have been queried
		// If so, select it automatically - prefer mRNA to protein
		var profile_to_select = null;
		var queried_genetic_profile_ids = QuerySession.getGeneticProfileIds();
		for (var i = 0; i < profiles.length; i++) {
		    if (queried_genetic_profile_ids.indexOf(profiles[i].id) > -1) {
			if (profiles[i].genetic_alteration_type === "MRNA_EXPRESSION") {
			    // If mRNA expression, use it and break
			    profile_to_select = profiles[i].id;
			    break;
			} else if (profiles[i].genetic_alteration_type === "PROTEIN_LEVEL") {
			    // If we've gotten here, then we haven't seen an mRNA one
			    profile_to_select = profiles[i].id;
			    // Keep looking
			}
		    }
		}
		profile_to_select = profile_to_select || profiles[0];
		for (var i = 0; i < profiles.length; i++) {
		    (function (profile) {
			var $option = $("<option>").attr({"value": profile.id, "title": profile.description}).text(profile.name);
			if (profile_to_select && profile_to_select === profile.id) {
			    $option.prop("selected", true);
			}
			$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #oncoprint_diagram_heatmap_profiles').append($option);
		    })(profiles[i]);
		}

		$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #add_genes_input').val(QuerySession.getQueryGenes().join(" "));
		$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #oncoprint_diagram_heatmap_profiles').click(function (evt) {
		    // suppress dropdown hiding
		    evt.stopPropagation();
		});
		$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #remove_heatmaps_btn').click(function (evt) {
		    // suppress dropdown hiding and form submit
		    evt.preventDefault();
		    evt.stopPropagation();

		    // remove all heatmaps
		    var gp_ids = Object.keys(State.heatmap_track_groups);
		    var tracks_to_remove = [];
		    for (var i = 0; i < gp_ids.length; i++) {
			var track_ids = Object.keys(State.heatmap_track_groups[gp_ids[i]].gene_to_track_id).map(function (gene) {
			    return State.heatmap_track_groups[gp_ids[i]].gene_to_track_id[gene];
			});
			tracks_to_remove = tracks_to_remove.concat(track_ids);
		    }
		    oncoprint.removeTracks(tracks_to_remove);
		    
		    //uncheck clustering option:
		    var clusteringChk = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #cluster_heatmap_chk');
		    clusteringChk.prop("checked", false);
		    clusteringChk.prop("disabled", "disabled");
		    //unset clustering state object that stores, a.o., which trackGroups should be reset in resetHeatmapTracks:
		    State.trackIdsInOriginalOrder = {};
		    //restore sort to "data" sorting:
		    updateSortOption("data");
		});
		
		var updateSortOption = function (sortOption) {
			State.sortby = sortOption;
			var sortby_radio = $('#oncoprint_diagram_sortby_group').find('input[type="radio"][name="sortby"][value="'+ sortOption + '"]');
			if (sortOption === "clustering") {
				sortby_radio.removeAttr("disabled");
			    sortby_radio.click();
			    sortby_radio.attr("disabled","disabled");
			} else {
				sortby_radio.click();
				resetHeatmapTracks();
			}
		};
		$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #cluster_heatmap_chk').change(function (evt) {
			var genetic_profile_id = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #oncoprint_diagram_heatmap_profiles').val();
			if (genetic_profile_id) {
				// cluster heatmap for selected profile:
				var grp = State.heatmap_track_groups[genetic_profile_id];
				var heatmap_track_group_id = grp.track_group_id;
				//when checkbox selected:
				if (this.checked) {
					//sort according to order found in the clustering results:
					State.sorting_by_given_order = true;
					//store original order before clustering:
					var trackIdsInOriginalOrder = oncoprint.model.getTrackGroups()[heatmap_track_group_id];
					State.trackIdsInOriginalOrder[heatmap_track_group_id] = trackIdsInOriginalOrder;
					//get heatmap data:
					var heatmap_data_deferred = State.using_sample_data ? QuerySession.getSampleHeatmapData(grp.genetic_profile_id, Object.keys(grp.gene_to_track_id)) : QuerySession.getPatientHeatmapData(grp.genetic_profile_id, Object.keys(grp.gene_to_track_id));
					var cases_deferred =  State.using_sample_data ? QuerySession.getSamples() : QuerySession.getPatients();
					//process data, call clustering:
					$.when(grp.gene_to_track_id, heatmap_data_deferred, cases_deferred).then(
							function (track_uid_map, heatmap_data, cases) {
								$.when(QuerySession.getClusteringOrder(track_uid_map, heatmap_data, cases)).then(
										function (clusteringResult) {
											LoadingBar.update(0.9, "green");
											oncoprint.setSortConfig({'type': 'order', order: clusteringResult.sampleUidsInClusteringOrder});
											oncoprint.setTrackGroupOrder(heatmap_track_group_id, clusteringResult.entityUidsInClusteringOrder);
											LoadingBar.hide();
											updateSortOption("clustering");
										});
								//show progress bar:
								LoadingBar.show();
								LoadingBar.update(0.2, "yellow");
							    LoadingBar.msg("Clustering...");
							    //update progress bar every 0.2s:
							    window.setInterval(function() {
								    var d = new Date();
								    var n = d.getMilliseconds();
								    LoadingBar.update(n/1000, "yellow");
							    }, 200);
							});
			    } else {
					//restore original order
					LoadingBar.update(0.9, "green");
					updateSortOption("data");
					LoadingBar.hide();
			    }
			}
		});

		$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #add_genes_btn').click(function (evt) {
		    // suppress dropdown hiding and form submit
		    evt.preventDefault();
		    evt.stopPropagation();

		    // add genes
		    var genetic_profile_id = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #oncoprint_diagram_heatmap_profiles').val();
		    if (genetic_profile_id) {
			var input = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #add_genes_input').val().trim().toUpperCase();
			if (input.length > 0) {
			    State.addAndPopulateNonexistingHeatmapTracks(genetic_profile_id, input.split(/[,\s]+/));
			    
			    //re-enable clustering checkbox, if disabled:
			    var clusteringChk = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #cluster_heatmap_chk');
			    clusteringChk.removeAttr("disabled");
			}
		    }
		});

		var updateButtons = function () {
		    var add_genes_btn = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #add_genes_btn');
		    var input = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #add_genes_input')
		    var dropdown = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #oncoprint_diagram_heatmap_profiles');
		    var clusteringChk = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #cluster_heatmap_chk');

		    add_genes_btn.prop("disabled", (!dropdown.val()) || (input.val().length === 0));
		    //if there is a genetic profile selected:
		    if (dropdown.val()) {
		    	var genetic_profile_id = dropdown.val();
		    	//and if there are heatmap tracks, then enable clustering option:
		    	if (State.heatmap_track_groups[genetic_profile_id]) {
		    		clusteringChk.prop("disabled", false);
		    	} else {
		    		clusteringChk.prop("disabled", true);
		    	}
		    }
		    else {
		    	clusteringChk.prop("disabled", true);
		    }
		};
		$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu').click(updateButtons);
		$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #oncoprint_diagram_heatmap_profiles').change(updateButtons);
		$(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #add_genes_input').on('input', updateButtons);
		updateButtons();
	    });
	})();
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
	(function setUpShowMinimap() {
	    var $btn = $(toolbar_selector + ' #oncoprint_show_minimap');
	    var update = setUpToggleButton($btn, ['Show minimap and zoom controls.', 'Hide minimap and zoom controls.'],
				function() { return State.is_minimap_shown; },
				function() {
				    State.toggleMinimapShown();
				    oncoprint.setMinimapVisible(State.is_minimap_shown);
				});
	    oncoprint.onMinimapClose(function() {
		State.toggleMinimapShown(false);
		update();
	    });
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
	    var $show_unaltered_checkbox = $(toolbar_selector).find('#oncoprint_diagram_view_menu')
		    .find('input[type="checkbox"][name="show_unaltered"]');
	    $show_unaltered_checkbox[0].checked = !State.unaltered_cases_hidden;
	    $show_unaltered_checkbox.change(function () {
		State.unaltered_cases_hidden = !($show_unaltered_checkbox.is(":checked"));
		if (State.unaltered_cases_hidden) {
		    (State.using_sample_data ? QuerySession.getUnalteredSampleUIDs(true) : QuerySession.getUnalteredPatientUIDs(true)).then(function (unaltered_uids) {
			oncoprint.hideIds(unaltered_uids, true);
		    });
		} else {
		    oncoprint.hideIds([], true);
		}
	    });
	})();
	(function setUpZoomToFit() {
	    setUpButton($(toolbar_selector + ' #oncoprint_zoomtofit'), [], ["Zoom to fit altered cases in screen"], null, function () {
		// TODO: assume multiple studies
		oncoprint.setHorzZoomToFit(State.getAlteredIds());
		oncoprint.scrollTo(0);
	    });
	})();
	(function setUpSortByAndColorBy() {
	    QuerySession.getExternalDataStatus().then(function (external_data_status) {
		if (!external_data_status.hotspots) {
		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="hotspots"]').attr("disabled", true);
		}
		if (!external_data_status.oncokb) {
		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="oncokb"]').attr("disabled", true);
		}
		if (!isAnyDriverLabellingDataSourceSelected()) {
		    // If no data sources selected, turn off driver/passenger labeling..
		    State.colorby_knowledge = false;
		    // .. and filtering
		    QuerySession.setKnownMutationSettings({ignore_unknown: false});
		}
		
		$('#oncoprint_diagram_showmutationcolor_icon').hide();
		var updateSortByForm = function () {
		    var sortby_type_checkbox = $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="type"]');
		    ;
		    var sortby_recurrence_checkbox = $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="recurrence"]');
		    if ((State.sortby !== "data") || !State.colorby_type) {
			sortby_type_checkbox.attr("disabled", "disabled");
		    } else {
			sortby_type_checkbox.removeAttr("disabled");
		    }

		    if ((State.sortby !== "data") || !State.colorby_knowledge) {
			sortby_recurrence_checkbox.attr("disabled", "disabled");
		    } else {
			sortby_recurrence_checkbox.removeAttr("disabled");
		    }
		    
		    if ((State.sortby !== "clustering")) {
				//some logic if the sorting options are not clustering and clustering checkbox is checked (which means 
				//sorting config is moving away from clustering):
				var clusteringChk = $(toolbar_selector + ' #oncoprint_diagram_heatmap_menu #cluster_heatmap_chk');
		    	if (clusteringChk.is(':checked')) {
		    		//uncheck clustering option, reorder tracks as well:
		    		clusteringChk.prop("checked", false);
		    		console.log("unchecking clustering option, reorder tracks as well");
					resetHeatmapTracks();
		    	}
			}		    
		};
		
		var getTiersMap = function () {
			var tiers = {};
			var checkboxes = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"]');
			for (var i=0; i < checkboxes.length; i++) {
				if (checkboxes[i].name.lastIndexOf("driver_tiers_filter_") != -1) {
				    tiers[checkboxes[i].name] = checkboxes[i].value;
				}
			}
			return tiers;
		    }

		var updateMutationColorForm = function () {
		    var colorby_knowledge_checkbox = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="recurrence"]');
		    var colorby_hotspots_checkbox = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="hotspots"]');
		    var colorby_cbioportal_checkbox = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="cbioportal"]');
		    var colorby_cosmic_checkbox = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="cosmic"]');
		    var colorby_oncokb_checkbox = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="oncokb"]');
		    var colorby_binary_checkbox = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="driver_filter"]');
		    var colorby_multi_values_checkboxes = {};
		    for (var value in getTiersMap()) {
			colorby_multi_values_checkboxes[value] = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name='+value+']');
		    }
		    var hide_unknown_checkbox = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="hide_unknown"]');
		    var cosmic_threshold_input = $('#oncoprint_diagram_mutation_color').find('#cosmic_threshold');
		    var cbioportal_threshold_input = $('#oncoprint_diagram_mutation_color').find('#cbioportal_threshold');

		    var known_mutation_settings = window.QuerySession.getKnownMutationSettings();
		    colorby_knowledge_checkbox.prop('checked', State.colorby_knowledge);
		    colorby_hotspots_checkbox.prop('checked', known_mutation_settings.recognize_hotspot);
		    colorby_cbioportal_checkbox.prop('checked', known_mutation_settings.recognize_cbioportal_count);
		    colorby_cosmic_checkbox.prop('checked', known_mutation_settings.recognize_cosmic_count);
		    colorby_oncokb_checkbox.prop('checked', known_mutation_settings.recognize_oncokb_oncogenic);
		    hide_unknown_checkbox.prop('checked', known_mutation_settings.ignore_unknown);
		    colorby_binary_checkbox.prop('checked', known_mutation_settings.recognize_driver_filter);
		    for (var value in getTiersMap()) {
			colorby_multi_values_checkboxes[value].prop('checked', known_mutation_settings.recognize_driver_tiers[value]);
		    }

		    if (!State.colorby_knowledge) {
			hide_unknown_checkbox.attr('disabled', 'disabled');
		    } else {
			hide_unknown_checkbox.removeAttr('disabled');
		    }

		    cbioportal_threshold_input.val(known_mutation_settings.cbioportal_count_thresh);
		    cosmic_threshold_input.val(known_mutation_settings.cosmic_count_thresh);
		};

		var updateRuleSets = function () {
		    var rule_set_params = State.getGeneticRuleSetParams();
		    var genetic_alteration_track_ids = utils.objectValues(State.genetic_alteration_tracks);
		    oncoprint.setRuleSet(genetic_alteration_track_ids[0], rule_set_params);
		    for (var i = 1; i < genetic_alteration_track_ids.length; i++) {
			oncoprint.shareRuleSet(genetic_alteration_track_ids[0], genetic_alteration_track_ids[i]);
		    }
		};
		var updateSortComparators = function () {
		    var comparator = State.getGeneticComparator();
		    oncoprint.keepSorted(false);
		    var genetic_alteration_track_ids = utils.objectValues(State.genetic_alteration_tracks);
		    for (var i = 0; i < genetic_alteration_track_ids.length; i++) {
			oncoprint.setTrackSortComparator(genetic_alteration_track_ids[i], comparator);
		    }
		    oncoprint.keepSorted();
		};
		var updateSortConfig = function () {
		    if (State.sortby === "data") {
			oncoprint.setSortConfig({'type': 'tracks'});
			State.sorting_by_given_order = false;
			State.sorting_alphabetically = false;
		    } else if (State.sortby === "id") {
			State.sorting_by_given_order = false;
			State.sorting_alphabetically = true;
			$.when(QuerySession.getSamples(), QuerySession.getPatients()).then(function (samples, patients) {
			    oncoprint.setSortConfig({'type': 'order', order: _.sortBy((State.using_sample_data ? samples : patients), function(x) { return x.id; }).map(function(x) { return x.uid; })});
			});
		    } else if (State.sortby === "custom") {
			State.sorting_by_given_order = true;
			State.sorting_alphabetically = false;
			$.when(QuerySession.getSamples(), QuerySession.getPatients()).then(function (samples, patients) {
			    oncoprint.setSortConfig({'type': 'order', order: (State.using_sample_data ? samples : patients).map(function(x) { return x.uid; })});
			});
		    }
		};
		var updateOncoPrint = function () {
		    updateMutationColorForm();
		    updateSortByForm();
		    oncoprint.suppressRendering();
		    updateRuleSets();
		    updateSortComparators();
		    State.refreshData();
		    oncoprint.releaseRendering();
		};
		$('#oncoprint_diagram_sortby_group').find('input[name="sortby"]').change(function () {
		    State.sortby = $('#oncoprint_diagram_sortby_group').find('input[name="sortby"]:checked').val();
		    updateSortByForm();
		    updateSortConfig();
		});
		$('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="type"]').change(function () {
		    State.sortby_type = $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="type"]').is(":checked");
		    updateSortComparators();
		});
		$('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="recurrence"]').change(function () {
		    State.sortby_recurrence = $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="recurrence"]').is(":checked");
		    updateSortComparators();
		});
		
		var updateColorKnowledge = function (new_known_mutation_settings) {
		    var tiers = false;
		    for (var tier in getTiersMap()) {
			if (new_known_mutation_settings.recognize_driver_tiers[tier]) {
			    tiers = true;
			}
		    }
		    if (new_known_mutation_settings.recognize_hotspot || new_known_mutation_settings.recognize_cbioportal_count
			    || new_known_mutation_settings.recognize_cosmic_count || new_known_mutation_settings.recognize_oncokb_oncogenic 
			    || new_known_mutation_settings.recognize_driver_filter || tiers) {
			// If at least one data source selected, update State
			State.colorby_knowledge = true;
		    } else {
			// If no data sources selected, turn off driver/passenger labeling..
			State.colorby_knowledge = false;
			// .. and filtering
			new_known_mutation_settings.ignore_unknown = false;
		    }
		    return new_known_mutation_settings;
		}
		$('#oncoprint_diagram_mutation_color').find('input[type="checkbox"]').change(function (e) {
		    if (e.originalEvent === undefined) {
			return true;
		    }
		    State.colorby_type = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="type"]').is(":checked");
		    var old_colorby_knowledge = State.colorby_knowledge;
		    State.colorby_knowledge = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="recurrence"]').is(":checked");

		    var new_known_mutation_settings = {
			recognize_hotspot: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="hotspots"]').is(":checked"),
			recognize_cbioportal_count: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="cbioportal"]').is(":checked"),
			recognize_cosmic_count: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="cosmic"]').is(":checked"),
			recognize_oncokb_oncogenic: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="oncokb"]').is(":checked"),
			ignore_unknown: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="hide_unknown"]').is(":checked"),
			recognize_driver_filter: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="driver_filter"]').is(":checked"),
			recognize_driver_tiers: {}
		    };
		    for (var value in getTiersMap()) {
			new_known_mutation_settings.recognize_driver_tiers[value] = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name='+value+']').is(":checked");
		    }

		    if (!old_colorby_knowledge && State.colorby_knowledge) {
			// If driver/passenger has just been selected, set defaults
			new_known_mutation_settings.recognize_hotspot = true;
			new_known_mutation_settings.recognize_cbioportal_count = true;
			new_known_mutation_settings.recognize_cosmic_count = true;
			new_known_mutation_settings.recognize_oncokb_oncogenic = true;
			new_known_mutation_settings.recognize_driver_filter = true;
			for (var value in getTiersMap()) {
				new_known_mutation_settings.recognize_driver_tiers[value] = true;
			    }
		    } else if (old_colorby_knowledge && !State.colorby_knowledge) {
			// If driver/passenger has just been deselected, set all to false
			new_known_mutation_settings.recognize_hotspot = false;
			new_known_mutation_settings.recognize_cbioportal_count = false;
			new_known_mutation_settings.recognize_cosmic_count = false;
			new_known_mutation_settings.recognize_oncokb_oncogenic = false;
			new_known_mutation_settings.recognize_driver_filter = false;
			for (var value in getTiersMap()) {
				new_known_mutation_settings.recognize_driver_tiers[value] = false;
			    }
		    }
		    
		    new_known_mutation_settings.recognize_hotspot = new_known_mutation_settings.recognize_hotspot && external_data_status.hotspots;
		    new_known_mutation_settings.recognize_oncokb_oncogenic = new_known_mutation_settings.recognize_oncokb_oncogenic && external_data_status.oncokb;

		    new_known_mutation_settings = updateColorKnowledge(new_known_mutation_settings);

		    window.QuerySession.setKnownMutationSettings(new_known_mutation_settings);

		    updateOncoPrint();
		});
		
		$('#tiers').find('input[type="checkbox"]').change(function (e) {
		    var multiValuesMap = getTiersMap();
		    var new_known_mutation_settings = {
				recognize_hotspot: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="hotspots"]').is(":checked"),
				recognize_cbioportal_count: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="cbioportal"]').is(":checked"),
				recognize_cosmic_count: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="cosmic"]').is(":checked"),
				recognize_oncokb_oncogenic: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="oncokb"]').is(":checked"),
				ignore_unknown: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="hide_unknown"]').is(":checked"),
				recognize_driver_filter: $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="driver_filter"]').is(":checked"),
				recognize_driver_tiers: {}
			    };
			    for (var value in multiValuesMap) {
				new_known_mutation_settings.recognize_driver_tiers[value] = $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name='+value+']').is(":checked");
			    }
		    
		    for (var value in multiValuesMap) {
			if ($('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name='+value+']').is(":checked")) {
			    State.colorby_knowledge = true;
			}
		    }
		    
		    new_known_mutation_settings = updateColorKnowledge(new_known_mutation_settings);
		    
		    window.QuerySession.setKnownMutationSettings(new_known_mutation_settings);

		    updateOncoPrint();
		});
		$('#oncoprint_diagram_mutation_color').find('#cosmic_threshold').change(function () {
		    window.QuerySession.setKnownMutationSettings({
			cosmic_count_thresh: parseInt($('#oncoprint_diagram_mutation_color').find('#cosmic_threshold').val(), 10) || 0
		    });
		    State.refreshData();
		});
		$('#oncoprint_diagram_mutation_color').find('#cbioportal_threshold').change(function () {
		    window.QuerySession.setKnownMutationSettings({
			cbioportal_count_thresh: parseInt($('#oncoprint_diagram_mutation_color').find('#cbioportal_threshold').val(), 10) || 0
		    });
		    State.refreshData();
		});
		(function initFormsFromState() {
		    var known_mutation_settings = window.QuerySession.getKnownMutationSettings();
		    $('#oncoprint_diagram_sortby_group').find('input[name="sortby"][value="' + State.sortby + '"]').prop("checked", true);
		    $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="type"]').prop("checked", State.sortby_type);
		    $('#oncoprint_diagram_sortby_group').find('input[type="checkbox"][name="recurrence"]').prop("checked", State.sortby_recurrence);

		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="type"]').prop("checked", State.colorby_type);
		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="recurrence"]').prop("checked", State.colorby_knowledge);
		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="hotspots"]').prop("checked", !!known_mutation_settings.recognize_hotspot);
		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="cbioportal"]').prop("checked", !!known_mutation_settings.recognize_cbioportal_count);
		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="cosmic"]').prop("checked", !!known_mutation_settings.recognize_cosmic_count);
		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="oncokb"]').prop("checked", !!known_mutation_settings.recognize_oncokb_oncogenic);
		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="hide_unknown"]').prop("checked", known_mutation_settings.ignore_unknown);
		    $('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name="driver_filter"]').prop("checked", !!known_mutation_settings.driver_filter);
		    for (var value in getTiersMap()) {
			$('#oncoprint_diagram_mutation_color').find('input[type="checkbox"][name='+value+']').prop("checked", !!known_mutation_settings[value]);
		    }
		    $('#oncoprint_diagram_mutation_color').find('#cosmic_threshold').val(known_mutation_settings.cosmic_count_thresh);
		    $('#oncoprint_diagram_mutation_color').find('#cbioportal_threshold').val(known_mutation_settings.cbioportal_count_thresh);

		    updateMutationColorForm();
		    updateSortByForm();
		})();
		(function initKnowledgeTooltipAndLinkout() {
		    $('#oncoprint_diagram_mutation_color').find('#colorby_hotspot_info').click(function () {
			window.open("http://www.cancerhotspots.org");
		    });
		    $('#oncoprint_diagram_mutation_color').find('#colorby_oncokb_info').click(function () {
			window.open("https://www.oncokb.org");
		    });
		    addQTipTo($('#oncoprint_diagram_mutation_color').find('#putative_driver_info_icon'), {
			content: {text: "For missense, inframe, and truncating mutations."},
			position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
			style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
			show: {event: "mouseover"},
			hide: {fixed: true, delay: 100, event: "mouseout"}
		    });
		    addQTipTo($('#oncoprint_diagram_mutation_color').find('#colorby_hotspot_info'), {
			content: {text: function () {
				return $("<p>Identified as a recurrent hotspot (statistically significant) in a " +
					"population-scale cohort of tumor samples of various cancer types using " +
					"methodology based in part on <a href='http://www.ncbi.nlm.nih.gov/pubmed/26619011' target='_blank'>Chang et al., Nat Biotechnol, 2016.</a>" +
					"\n" +
					"Explore all mutations at <a href='https://www.cancerhotspots.org' target='_blank'>https://www.cancerhotspots.org</a></p>" +
					(external_data_status.hotspots ? "" : "<p>Currently unavailable.</p>"));
			    }},
			position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
			style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
			show: {event: "mouseover"},
			hide: {fixed: true, delay: 100, event: "mouseout"}
		    });
		    addQTipTo($('#oncoprint_diagram_mutation_color').find('#colorby_oncokb_info'), {
			content: {text: "Oncogenicity from OncoKB" + (external_data_status.oncokb ? "" : ". Currently unavailable.")},
			position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
			style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
			show: {event: "mouseover"},
			hide: {fixed: true, delay: 100, event: "mouseout"}
		    });
		})();
	    });
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
		var curr_selection = $(toolbar_selector).find('#oncoprint_diagram_view_menu')
				   .find('input[type="radio"][name="datatype"]:checked').val();
		$(toolbar_selector).find('#oncoprint_diagram_view_menu')
				   .find('input[type="radio"][name="datatype"][value="' + (curr_selection === "sample" ? "patient" : "sample") + '"]').prop("checked", true);
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
		$('.oncoprint-sample-download').text((State.using_sample_data ? "Sample" : "Patient") + " order");
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
		window.QuerySession.getUIDToCaseMap().then(function (uid_to_case) {
		    var idTypeStr = (State.using_sample_data ? "Sample" : "Patient");
		    var content = idTypeStr + " order in the Oncoprint is: \n";
		    content += oncoprint.getIdOrder().map(function (uid) {
			return uid_to_case[uid];
		    }).join('\n');
		    var downloadOpts = {
			filename: 'OncoPrint' + idTypeStr + 's.txt',
			contentType: "text/plain;charset=utf-8",
			preProcess: false};

		    // send download request with filename & file content info
		    cbio.download.initDownload(content, downloadOpts);
		});
	    });
	    
	    $('body').on('click', '.oncoprint-tabular-download', function () {
		//Get caseNames (either patients or samples)
		window.QuerySession.getUIDToCaseMap().then(function (uid_to_case) {
		    var caseNames = [];
		    oncoprint.getIdOrder().map(function (uid) {
			caseNames.push(uid_to_case[uid]);
		    });
		    var prefixName = (State.using_sample_data ? "SAMPLE_DATA_" : "PATIENT_DATA_"); //Name depending on the type of case

		    //Gather all the Oncoprint data
		    var oncoprintData = {
			    'CLINICAL': {},
			    'CNA': {},
			    'MUTATIONS': {},
			    'MRNA': {},
			    'PROTEIN': {},
			    'FUSION': {},
		    };
		    
		    //Create maps for genetic data
		    var cnaMap = {
			    'amp': 'Amplification',
			    'gain': 'Gain',
			    'hetloss': 'Shallow Deletion',
			    'homdel': 'Deep Deletion'
		    };
		    var mutationMap = {};
		    if (State.colorby_knowledge) {
			mutationMap = {
				    'inframe': 'Inframe Mutation (putative passenger)',
				    'inframe_rec': 'Inframe Mutation (putative driver)',
				    'missense': 'Missense Mutation (putative passenger)',
				    'missense_rec': 'Missense Mutation (putative driver)',
				    'promoter': 'Promoter Mutation',
				    'promoter_rec': 'Promoter Mutation',
				    'trunc': 'Truncating mutation (putative passenger)',
				    'trunc_rec': 'Truncating mutation (putative driver)'
			    };
		    } else {
			mutationMap = {
				    'inframe': 'Inframe Mutation',
				    'inframe_rec': 'Inframe Mutation',
				    'missense': 'Missense Mutation',
				    'missense_rec': 'Missense Mutation',
				    'promoter': 'Promoter Mutation',
				    'promoter_rec': 'Promoter Mutation',
				    'trunc': 'Truncating mutation',
				    'trunc_rec': 'Truncating mutation'
			    };
		    }
		    var mrnaMap = {
			    'up': 'mRNA Upregulation',
			    'down': 'mRNA Downregulation'
		    };
		    var proteinMap = {
			    'down': 'Protein Downregulation',
			    'up': 'Protein Upregulation'
		    };
		    var fusionMap = {
			    'true': 'Fusion'
		    };

		    //Add genetic data
		    var track_ids = utils.objectValues(State.genetic_alteration_tracks);
		    patientOrSample = (State.using_sample_data ? "sample" : "patient");
		    for (var track_id=0; track_id<track_ids.length; track_id++){
			var currentTrackData = oncoprint.getTrackData(track_ids[track_id]);
			var currentGeneName = currentTrackData[0].gene; //The gene is the same for all entries of the track
			//Add the currentGeneName to the oncoprintData if it does not exist
			if (oncoprintData.CNA[currentGeneName] === undefined) {
			    oncoprintData.CNA[currentGeneName] = {};
			}
			if (oncoprintData.MUTATIONS[currentGeneName] === undefined) {
			    oncoprintData.MUTATIONS[currentGeneName] = {};
			}
			if (oncoprintData.MRNA[currentGeneName] === undefined) {
			    oncoprintData.MRNA[currentGeneName] = {};
			}
			if (oncoprintData.PROTEIN[currentGeneName] === undefined) {
			    oncoprintData.PROTEIN[currentGeneName] = {};
			}
			if (oncoprintData.FUSION[currentGeneName] === undefined) {
			    oncoprintData.FUSION[currentGeneName] = {};
			}
			//Iterate over all patients/samples of the track and add them to oncoprintData
			for (var currentGeneticTrackData=0; currentGeneticTrackData<currentTrackData.length; currentGeneticTrackData++) {
			    oncoprintData.CNA[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = "";
			    oncoprintData.MUTATIONS[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = "";
			    oncoprintData.MRNA[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = "";
			    oncoprintData.PROTEIN[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = "";
			    oncoprintData.FUSION[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = "";
			    if (currentTrackData[currentGeneticTrackData].disp_cna !== undefined) {
				oncoprintData.CNA[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = cnaMap[currentTrackData[currentGeneticTrackData].disp_cna] ? cnaMap[currentTrackData[currentGeneticTrackData].disp_cna] : currentTrackData[currentGeneticTrackData].disp_cna;
			    }
			    if (currentTrackData[currentGeneticTrackData].disp_fusion !== undefined) {
				oncoprintData.FUSION[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = fusionMap[currentTrackData[currentGeneticTrackData].disp_fusion] ? fusionMap[currentTrackData[currentGeneticTrackData].disp_fusion] : currentTrackData[currentGeneticTrackData].disp_fusion;
			    }
			    if (currentTrackData[currentGeneticTrackData].disp_mrna !== undefined) {
				oncoprintData.MRNA[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = mrnaMap[currentTrackData[currentGeneticTrackData].disp_mrna] ? mrnaMap[currentTrackData[currentGeneticTrackData].disp_mrna] : currentTrackData[currentGeneticTrackData].disp_mrna;
			    }
			    if (currentTrackData[currentGeneticTrackData].disp_prot !== undefined) {
				oncoprintData.PROTEIN[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = proteinMap[currentTrackData[currentGeneticTrackData].disp_prot] ? proteinMap[currentTrackData[currentGeneticTrackData].disp_prot] : currentTrackData[currentGeneticTrackData].disp_prot;
			    }
			    if (currentTrackData[currentGeneticTrackData].disp_mut !== undefined) {
				oncoprintData.MUTATIONS[currentGeneName][currentTrackData[currentGeneticTrackData][patientOrSample]] = mutationMap[currentTrackData[currentGeneticTrackData].disp_mut] ? mutationMap[currentTrackData[currentGeneticTrackData].disp_mut] : currentTrackData[currentGeneticTrackData].disp_mut;
			    }
			}
		    }
		    
		  //Add clinical data
		    //var clinical_track_ids = utils.objectValues(State.clinical_tracks);
		    for (var clinical_track_id in State.clinical_tracks){
			if (State.clinical_tracks.hasOwnProperty(clinical_track_id)) {
			    var currentClinicalTrackData = oncoprint.getTrackData(clinical_track_id);
			    var currentAttributeName = State.clinical_tracks[clinical_track_id].display_name;
			    //Add the currentAttributeName to the oncoprintData if it does not exist
			    if (oncoprintData.CLINICAL[currentAttributeName] === undefined) {
				oncoprintData.CLINICAL[currentAttributeName] = {};
			    }
			    //Iterate over all patients/samples of the track and add them to oncoprintData
			    for (var currentClinicalCase=0; currentClinicalCase<currentClinicalTrackData.length; currentClinicalCase++) {
				oncoprintData.CLINICAL[currentAttributeName][currentClinicalTrackData[currentClinicalCase][patientOrSample]] = "";
				if (currentClinicalTrackData[currentClinicalCase].attr_val !== undefined) {
				    oncoprintData.CLINICAL[currentAttributeName][currentClinicalTrackData[currentClinicalCase][patientOrSample]] = currentClinicalTrackData[currentClinicalCase].attr_val;
				}
			    }
			}
		    }
		    
		    //Add heatmap data
		    var heatmapPromise = State.using_sample_data ? HeatmapData.getSampleData(): HeatmapData.getPatientData();
		    heatmapPromise.then(function(heatmapData) {
			//Put the heatmapData information in oncoprintData if it exists
			for (var heatmapTrack=0; heatmapTrack<heatmapData.length; heatmapTrack++) {
			    currentHeatmapGene = heatmapData[heatmapTrack].gene;
			    currentHeatmapType = "HEATMAP "+heatmapData[heatmapTrack].genetic_alteration_type +' '+ heatmapData[heatmapTrack].datatype;
			    currentHeatmapTrackData = heatmapData[heatmapTrack].oncoprint_data;
			    for (var caseId=0; caseId<currentHeatmapTrackData.length; caseId++) {
				if (oncoprintData[currentHeatmapType] === undefined) {
				    oncoprintData[currentHeatmapType] = {};
				}
				if (oncoprintData[currentHeatmapType][currentHeatmapGene] === undefined) {
				    oncoprintData[currentHeatmapType][currentHeatmapGene] = {};
				}
				oncoprintData[currentHeatmapType][currentHeatmapGene][currentHeatmapTrackData[caseId][patientOrSample]] = currentHeatmapTrackData[caseId].profile_data === null ? "" : currentHeatmapTrackData[caseId].profile_data;
			    }
			}
			
			//Put all the information of the oncoprintData in a variable with tabular form
			var content = 'track_name\ttrack_type';
			//Add the cases to the content
			for (var i=0; i<caseNames.length; i++) {
			    content += '\t'+caseNames[i];
			}
			//Add final header line
			content += '\n';

			//Iterate over oncoprintData and write it to content
			Object.keys(oncoprintData).forEach(function (j) {
			    Object.keys(oncoprintData[j]).forEach(function(k) {
				content += k+'\t'+j;
				for (var l=0; l<caseNames.length; l++) {
				    content += '\t'+oncoprintData[j][k][caseNames[l]];
				}
				content += '\n';
			    });
			});

			var downloadOpts = {
				filename: prefixName + 'oncoprint.tsv',
				contentType: "text/plain;charset=utf-8",
				preProcess: false};

			// send download request with filename & file content info
			cbio.download.initDownload(content, downloadOpts);
		    });
		});
	    });
	})();
    })();
};

window.CreateOncoprinterWithToolbar = function (ctr_selector, toolbar_selector) {
    
    $('#oncoprint #everything').show();
    $('#oncoprint #oncoprint-diagram-toolbar-buttons').show();
    
    if (!utils.isWebGLAvailable()) {
	$(ctr_selector).append("<p>"+utils.getUnavailableMessageHTML()+"</p>");
	$(toolbar_selector).hide();
	$("#inner-container").hide();
	return;
    }
    
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
    $(toolbar_selector).css({'visibility':'visible'});
    $(ctr_selector).add(toolbar_selector).on("mouseover", function(evt) {
	$(toolbar_selector).stop().animate({opacity:1});
	clearTimeout(toolbar_fade_out_timeout);
    });
    $(ctr_selector).add(toolbar_selector).on("mouseleave", function(evt) {
	clearTimeout(toolbar_fade_out_timeout);
	toolbar_fade_out_timeout = setTimeout(function() {
	    $(toolbar_selector).stop().animate({opacity:0});
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
	    
	    'trackIdsInOriginalOrder': {},
	    
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
			'na_z': 1.1
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
			    present_ids[data[i][id_key]] = false;
			}
		    }
		}
		id_order = id_order || Object.keys(present_ids);
		for (var i=0; i<id_order.length; i++) {
		    if (present_ids.hasOwnProperty(id_order[i])) {
			present_ids[id_order[i]] = true;
		    }
		}
		this.ids = Object.keys(present_ids).filter(function(x) { return !!present_ids[x]; });
		
		var altered_percentage_by_gene = {};
		for (var gene in altered_ids_by_gene) {
		    if (altered_ids_by_gene.hasOwnProperty(gene)) {
			var altered_id_count = altered_ids_by_gene[gene].filter(function(x) { return !!present_ids[x]; }).length;
			altered_percentage_by_gene[gene] = Math.round(100*altered_id_count/this.ids.length);
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
		this.altered_ids = Object.keys(altered_ids).filter(function(x) { return !!present_ids[x]; });
		
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

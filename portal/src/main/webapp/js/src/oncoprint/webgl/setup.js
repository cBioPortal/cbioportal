var invertArray = function(arr) {
    var ret = {};
    for (var i=0; i<arr.length; i++) {
	ret[arr[i]] = i;
    }
    return ret;
}
var sign = function(x) {
    if (x > 0) {
	return 1;
    } else if (x < 0) {
	return -1;
    } else {
	return 0;
    }
}
    
var makeGeneticAlterationComparator = function(distinguish_mutations) {
		var cna_key = 'cna';
		var cna_order = invertArray(['AMPLIFIED', 'HOMODELETED', 'GAINED', 'HEMIZYGOUSLYDELETED', 'DIPLOID', undefined]);
		var mut_type_key = 'mut_type';
		var mut_order = (function() {
			var _order = invertArray(['FUSION', 'TRUNC', 'INFRAME', 'MISSENSE', undefined, true, false]); 
			if (!distinguish_mutations) {
				return function(m) {
					if (m === 'FUSION') {
					    return 0;
					} else {
					    return _order[!!m];
					}
					//return +(typeof m === 'undefined');
				}
			} else { 
				return function(m) {
					return _order[m];
				}
			}
		})();
		var mrna_key = 'mrna';
		var rppa_key = 'rppa';
		var regulation_order = invertArray(['UPREGULATED', 'DOWNREGULATED', undefined]);

		return function(d1, d2) {
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
	
window.CreateCBioPortalOncoprintWithToolbar = function(ctr_selector, toolbar_selector) {
    $('#oncoprint #everything').show();
    //$('#oncoprint #oncoprint-diagram-toolbar-buttons').show();
    var oncoprint = new window.Oncoprint(ctr_selector, 1050);
    QuerySession.getGenomicEventData().then(function(data) {
	var data_by_gene = {};
	for (var i=0; i<data.length; i++) {
	    var d = data[i];
	    if (!data_by_gene[d.gene]) {
		data_by_gene[d.gene] = [];
	    }
	    data_by_gene[d.gene].push(d);
	}
	var rule_set_params = {
		type: 'gene',
	};
	//oncoprint.suppressRendering();
	oncoprint.addTracks(Object.keys(data_by_gene).map(function(gene) {
	       return {'data':data_by_gene[gene], 'rule_set_params': rule_set_params, 'data_id_key':'sample', 'label':gene,
		    'sortCmpFn': makeGeneticAlterationComparator(true)
	    };
	}));
	//oncoprint.releaseRendering();
    });
    window.oncoprint = oncoprint;
    
    
    
    
    (function setUpToolbar() {
	var unaltered_cases_hidden = false;
	var zoom = 1.0;
	var zoom_increment = 0.2;
	var cell_padding_on = true;
	var unaltered_cases_hidden = false;
	
	var to_remove_on_destroy = [];
	var to_remove_qtip_on_destroy = [];
	var to_remove_click_evt_on_destroy = [];
	
	var appendTo = function($elt, $target) {
	    $elt.appendTo($target);
	    to_remove_on_destroy.push($elt);
	};
	var addQTipTo = function($elt, qtip_params) {
	    $elt.qtip(qtip_params);
	    to_remove_qtip_on_destroy.push($elt);
	};
	window.onClick = function($elt, callback) {
	    $elt.click(callback);
	    to_remove_click_evt_on_destroy.push($elt);
	};
	var setUpHoverEffect = function($elt) {
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
	
	
	var setUpButton = function($elt, img_urls, qtip_descs, index_fn, callback) {
	    var updateButton = function() {
		$elt.find('img').attr('src', img_urls[index_fn()]);
	    };
	    addQTipTo($elt, {
		content: {text: function () {
			return qtip_descs[index_fn()];
		    }},
		position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
		style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
		show: {event: "mouseover"},
		hide: {fixed: true, delay: 100, event: "mouseout"}
	    });
	    onClick($elt, function() {
		callback();
		updateButton();
	    });
	    updateButton();
	};
	(function setUpZoom() {
	    var zoom_elt = $(toolbar_selector + ' #oncoprint_diagram_slider_icon');
	    var slider = $('<input>', {
		id: "oncoprint_zoom_slider",
		type: "range",
		min: 0.1,
		max: 1,
		step: 0.01,
		value: 1,
		change: function () {
		    this.value = oncoprint.setHorzZoom(parseFloat(this.value));
		}
	    });
	    
	    appendTo(slider, zoom_elt);
	    addQTipTo(slider, {
				content: {text: 'Zoom in/out of oncoprint'},
				position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
				style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
				show: {event: "mouseover"},
				hide: {fixed: true, delay: 100, event: "mouseout"}
	    });
	    setUpHoverEffect(slider);
	    
	    onClick($(toolbar_selector + ' #oncoprint_zoomout'), function() {
		slider[0].value = oncoprint.setHorzZoom(oncoprint.getHorzZoom() - zoom_increment);
		slider.trigger('change');
	    });
	    onClick($(toolbar_selector + ' #oncoprint_zoomin'), function() {
		slider[0].value = oncoprint.setHorzZoom(oncoprint.getHorzZoom() + zoom_increment);
		slider.trigger('change');
	    });
	})();
	(function setUpToggleCellPadding() {
	    setUpButton($(toolbar_selector + ' #oncoprint-diagram-removeWhitespace-icon'),
	    ['images/removeWhitespace.svg', 'images/unremoveWhitespace.svg'],
	    ["Remove whitespace between columns", "Show whitespace between columns"],
	    function() { return (cell_padding_on ? 0 : 1);},
	    function() {
		cell_padding_on = !cell_padding_on;
		oncoprint.setCellPaddingOn(cell_padding_on);
	    });
	})();
	(function setUpHideUnalteredCases() {
	    QuerySession.getUnalteredSamples().then(function(unaltered_samples) {
		setUpButton($(toolbar_selector + ' #oncoprint-diagram-removeUCases-icon'),
			['images/removeUCases.svg', 'images/unremoveUCases.svg'],
			['Hide unaltered cases', 'Show unaltered cases'],
			function() { return (unaltered_cases_hidden ? 1 : 0); },
			function() {
			    unaltered_cases_hidden = !unaltered_cases_hidden;
			    if (unaltered_cases_hidden) {
				oncoprint.hideIds(unaltered_samples, true);
			    } else {
				oncoprint.hideIds([], true);
			    }
			});
	    });
	})();
    })();
}


$(document).ready(function() {
    CreateCBioPortalOncoprintWithToolbar('#oncoprint #everything', '#oncoprint #oncoprint-diagram-toolbar-buttons');
});
// This is for the moustache-like templates
// prevents collisions with JSP tags
_.templateSettings = {
	interpolate : /\{\{(.+?)\}\}/g
};
$('#oncoprint_controls').html(_.template($('#main-controls-template').html())());
$('#oncoprint').css('position','relative');

var oncoprintFadeTo;
var oncoprintFadeIn;
var oncoprint;
var genetic_alteration_tracks = [];
var clinical_tracks = [];
var clinical_legends_visible = false;

(function () {
	var oncoprint_covering_block = $('<div>').appendTo('#oncoprint');;
	oncoprint_covering_block.css({'position':'absolute', 'left':'0px', 'top': '0px', 'display':'none'});
	
	oncoprintFadeTo = function(f) {
		oncoprint_covering_block.css({'display':'block', 'width':$('#oncoprint').width()+'px', 'height':$('#oncoprint').height()+'px'});
		$('#oncoprint').fadeTo('fast', f);
	};
	oncoprintFadeIn = function() {
		oncoprint_covering_block.css('display','none');
		$('#oncoprint').fadeTo('fast', 1);
	}
}());


var setUpToolbarBtnHover = function($elt) {
	$elt.hover(function() {
		$(this).css({'fill':'#0000FF',
			'font-size': '18px',
			'cursor': 'pointer'});
		},
		function () {
			$(this).css({'fill': '#87CEFA',
				'font-size': '12px'});
		});
};
	
var mutation_count_tooltip = function(d) {
	var ret = '';
	ret += '<b>'+d.attr_val+' mutations</b><br>';
	ret += '<a href="'+sampleViewUrl(d.sample)+'">'+d.sample+'</a>';
	return ret;
};
var genetic_alteration_tooltip = function(d) {
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
	ret += '<a href="'+sampleViewUrl(d.sample)+'">'+d.sample+'</a>';
	return ret;
};
var clinical_tooltip = function(d) {
	var ret = '';
	ret += 'value: <b>'+d.attr_val+'</b><br>'
	ret += '<a href="'+sampleViewUrl(d.sample)+'">'+d.sample+'</a>';
	return ret;
};

var utils = window.OncoprintUtils;
(function setUpClinicalAttributesSelector(cancer_study_id, case_list) {
	var clinicalAttributesColl = new ClinicalAttributesColl();
	var clinicalData = {};
	var clinicalAttributes;
	var currentClinicalAttributes;
	var populateSelectorChosen = function() {
		utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), currentClinicalAttributes);
		$("#select_clinical_attributes").val('').trigger("liszt:updated");
	};
	clinicalAttributesColl.fetch({
		type: 'POST',
		data: { cancer_study_id: cancer_study_id,
			case_list: case_list 
		},
		success: function(attrs) {
			clinicalAttributes = attrs.toJSON();
			clinicalAttributes = _.sortBy(clinicalAttributes, function(o) { return o.display_name; })
			if(window.PortalGlobals.getMutationProfileId()!==null){
			    clinicalAttributes.unshift({attr_id: "# mutations", 
							datatype: "NUMBER",
							description: "Number of mutations", 
							display_name: "# mutations"});
			}

			if(window.PortalGlobals.getCancerStudyId()!==null){
			    clinicalAttributes.unshift({attr_id: "FRACTION_GENOME_ALTERED", 
							datatype: "NUMBER",
							description: "Fraction Genome Altered", 
							display_name: "Fraction Genome Altered"});
			}

			currentClinicalAttributes = clinicalAttributes.slice();
			populateSelectorChosen();
			$('#select_clinical_attributes').chosen({width: "330px", "font-size": "12px", search_contains: true});

			$('#select_clinical_attributes_chzn .chzn-search input').click(
			    function(e){
				e.stopPropagation();
			    }
			);

			$("#select_clinical_attributes_chzn").mouseenter(function() {
			    $("#select_clinical_attributes_chzn .chzn-search input").focus();
			});
			$("#select_clinical_attributes_chzn").addClass("chzn-with-drop");
		}
	});
	
	var addClinicalTrack = function(clinical_attr) {
		var new_track;
		if (clinical_attr.attr_id === "# mutations") {
			var mutation_count_data = annotatePatientIds(clinicalData[clinical_attr.attr_id]);
			new_track = oncoprint.addTrack({label: '# Mutations (Log scale)', tooltip: mutation_count_tooltip, cell_height: 15.33}, 0);
			oncoprint.setRuleSet(new_track, window.Oncoprint.BAR_CHART, {
				data_key: 'attr_val',
				fill: '#c97894',
				legend_label: '# Mutations',
				scale: 'log',
				na_color: '#d3d3d3'
			});
			oncoprint.setTrackData(new_track, mutation_count_data);
			oncoprint.sort();
		} else {
			var data = annotatePatientIds(clinicalData[clinical_attr.attr_id]);
			var new_track = oncoprint.addTrack({label:clinical_attr.display_name, tooltip: clinical_tooltip, cell_height: 15.33}, 0);
			if (clinical_attr.datatype.toLowerCase() === "number") {	
				oncoprint.setRuleSet(new_track, window.Oncoprint.GRADIENT_COLOR, {
					data_key: 'attr_val',
					color_range: ['#ffffff', '#c97894'],
					legend_label: clinical_attr.display_name,
					na_color: '#d3d3d3'
				});
			} else {
				oncoprint.setRuleSet(new_track, window.Oncoprint.CATEGORICAL_COLOR, {
					legend_label: clinical_attr.display_name,
					getCategory: function(d) {
						return d.attr_val;
					},
					color: {
						'NA': '#D3D3D3'
					},
				});
			}
			oncoprint.setTrackData(new_track, data);
			oncoprint.sort();
		}
		var attr_index = _.indexOf(_.pluck(currentClinicalAttributes, 'attr_id'), clinical_attr.attr_id);
		currentClinicalAttributes.splice(attr_index, 1);
		
		populateSelectorChosen();
		
		clinical_tracks.push(new_track);
		oncoprint.setLegendVisible(new_track, clinical_legends_visible);
	};
	
	$('#oncoprint_diagram_showmorefeatures_icon').qtip({
                        content: {text:'Add another clinical attribute track'},
                        position: {my:'bottom middle', at:'top middle', viewport: $(window)},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
                        show: {event: "mouseover"},
                        hide: {fixed: true, delay: 100, event: "mouseout"}
	});
	$('#oncoprint_diagram_showmorefeatures_icon').click(function(){
            $('#select_clinical_attributes_chzn').addClass("chzn-with-drop");
        });
	$('#select_clinical_attributes').change(function() {
		oncoprintFadeTo(0.5);
		$(oncoprint).one('finished_rendering.oncoprint', function() {
			$('#oncoprint-diagram-toolbar-buttons #clinical_first').css('display','inline');
			$('#oncoprint-diagram-showlegend-icon').css('display','inline');
			oncoprintFadeIn();
		});
		var clinicalAttribute = $('#select_clinical_attributes option:selected')[0].__data__;
		if (clinicalAttribute.attr_id === undefined) {
			// selected "none"
		} else {
			if (clinicalData.hasOwnProperty(clinicalAttribute.attr_id)) {
				addClinicalTrack(clinicalAttribute);
			} else {
				if (clinicalAttribute.attr_id === "# mutations") {
					var clinicalMutationColl = new ClinicalMutationColl();
					clinicalMutationColl.fetch({
						type: "POST",
						data: {
							mutation_profile: window.PortalGlobals.getMutationProfileId(),
							cmd: "count_mutations",
							case_ids: case_list
						},
						success: function(response) {
							clinicalData[clinicalAttribute.attr_id] = addBlankDataToClinicalData(response.toJSON(), clinicalAttribute.attr_id, 'sample', case_list.trim().split(/\s+/));
							addClinicalTrack(clinicalAttribute);
						}
					});
				} else if (clinicalAttribute.attr_id === "FRACTION_GENOME_ALTERED") {
					var clinicalCNAColl = new ClinicalCNAColl();
					clinicalCNAColl.fetch({
						type: "POST",
						data: {
							cancer_study_id: cancer_study_id,
							cmd: "get_cna_fraction",
							case_ids: case_list
						},
						success: function(response) {
							clinicalData[clinicalAttribute.attr_id] = addBlankDataToClinicalData(response.toJSON(), clinicalAttribute.attr_id, 'sample', case_list.trim().split(/\s+/));
							addClinicalTrack(clinicalAttribute);
						}
					});
				} else {
					var clinicalColl = new ClinicalColl();
					clinicalColl.fetch({
						type: "POST",
						data: {
							cancer_study_id: cancer_study_id,
							attribute_id: clinicalAttribute.attr_id,
							case_list: case_list
						},
						success: function(response) {
							clinicalData[clinicalAttribute.attr_id] = addBlankDataToClinicalData(response.toJSON(), clinicalAttribute.attr_id, 'sample', case_list.trim().split(/\s+/));
							addClinicalTrack(clinicalAttribute);
						}
					});
				}
			}
		}
	});
})(cancer_study_id_selected, window.PortalGlobals.getCases());

(function setUpShowLegendBtn() {
	var imgs = ['images/showlegend.svg', 'images/hidelegend.svg'];
	var qtip_text = ['Show legends for clinical attribute tracks', 'Hide legends for clinical attribute tracks'];
	$('#oncoprint-diagram-showlegend-icon').click(function() {
		clinical_legends_visible = !clinical_legends_visible;
		$('#oncoprint-diagram-showlegend-icon img').attr('src', imgs[+clinical_legends_visible]);
		oncoprint.setLegendVisible(clinical_tracks, clinical_legends_visible);	
	});
	$('#oncoprint-diagram-showlegend-icon').qtip({
            content: {
		    text:function() {
			    return qtip_text[+clinical_legends_visible];
		    }
            },
            position: {my:'bottom middle', at:'top middle', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
        }); 
	setUpToolbarBtnHover($('#oncoprint-diagram-showlegend-icon'));
})();
(function setUpSortBySelector(cases) {
	$('#oncoprint-diagram-toolbar-buttons #genes_first_a').click(function(){
		oncoprint.setTrackGroupSortOrder([1,0]);
		oncoprint.setSortConfig({type: 'track'});
		oncoprint.sort();
        });
        $('#oncoprint-diagram-toolbar-buttons #clinical_first_a').click(function(){
		oncoprint.setTrackGroupSortOrder([0,1]);
		oncoprint.setSortConfig({type: 'track'});
		oncoprint.sort();
        });
        $('#oncoprint-diagram-toolbar-buttons #alphabetically_first_a').click(function(){
		oncoprint.setSortConfig({type: 'id'});
		oncoprint.sort();
        });
        $('#oncoprint-diagram-toolbar-buttons #user_defined_first_a').click(function(){
		oncoprint.setIdOrder(cases.trim().split(/\s+/));
		oncoprint.setSortConfig({});
        });
})(window.PortalGlobals.getCases());

(function setUpMutationSettingsBtn() {
	// TODO: are we aware that these icon names are 100% unintelligible?
	var settings = [{color: true, order: false, next_setting_img:'images/colormutations.svg', next_setting_desc: 'Color-code mutations and sort by type'},  
			{color:true, order: true, next_setting_img:'images/uncolormutations.svg', next_setting_desc: 'Show all mutations with the same color'},
			{color:false, order: false, next_setting_img:'images/mutationcolorsort.svg', next_setting_desc: 'Color-code mutations but don\'t sort by type'}];
	var setting_index = 0;
	var updateBtn = function() {
		$('#oncoprint_diagram_showmutationcolor_icon').qtip('destroy', true);
		$('#oncoprint_diagram_showmutationcolor_icon img').attr('src', settings[setting_index].next_setting_img);
		$('#oncoprint_diagram_showmutationcolor_icon').qtip({
			content: {text: settings[setting_index].next_setting_desc},
			position: {my:'bottom middle', at:'top middle', viewport: $(window)},
			style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
			show: {event: "mouseover"},
			hide: {fixed: true, delay: 100, event: "mouseout"}
		}); 
	};
	$('#oncoprint_diagram_showmutationcolor_icon').click(function() {
		setting_index = (setting_index + 1) % settings.length;
		var new_params = {dont_distinguish_mutation_color: !settings[setting_index].color, dont_distinguish_mutation_order: !settings[setting_index].order};
		_.each(genetic_alteration_tracks, function(track_id, ind) {
			if (ind === 0) {
				oncoprint.setRuleSet(track_id, window.Oncoprint.GENETIC_ALTERATION, new_params);
			} else {
				oncoprint.useSameRuleSet(track_id, genetic_alteration_tracks[0]);
			}
		});
		updateBtn();
		oncoprint.sort();
	});
	updateBtn();
	setUpToolbarBtnHover($('#oncoprint_diagram_showmutationcolor_icon'));
})();

$("#oncoprint-diagram-toolbar-buttons").show();

var sampleViewUrl = function(sample_id) {
        var href = cbio.util.getLinkToSampleView(window.cancer_study_id_selected,sample_id);
	return href;
};

var setupOncoprint = function(container_selector_string, cancer_study_id, oql, cases, genetic_profile_ids, z_score_threshold, rppa_score_threshold) {
	var geneDataColl = new GeneDataColl();
	oncoprint = window.oncoprint = window.Oncoprint.create(container_selector_string);
	oncoprint.setTrackGroupSortOrder([1,0]);
	
	
	geneDataColl.fetch({
		type: "POST",
		data: {
			cancer_study_id: cancer_study_id,
			oql: oql,
			case_list: cases,
			geneticProfileIds: genetic_profile_ids,
			z_score_threshold: z_score_threshold,
			rppa_score_threshold: rppa_score_threshold
		},
		success: function(response) {
			var genes = {};
			_.each(response.models, function(d) {
				genes[d.attributes.gene] = true;
			});
			genes = Object.keys(genes);
			var geneData = {};
			_.each(response.models, function(d) {
				var gene = d.attributes.gene;
				geneData[gene] = geneData[gene] || [];
				geneData[gene].push(d.attributes);
			});
			var track_created = false;
			oncoprint.suppressRendering();
			var numDataPts = _.reduce(_.map(Object.keys(geneData), function(gene) {
				return geneData[gene].length;
			}), function(a,b) { return a+b;}, 0);
			var numDataPtsAdded = 0;
			$('#outer_loader_img').hide();
			var updateProgressIndicator = function(done_adding) {
				if (done_adding) {
					document.getElementById('oncoprint_progress_indicator_text').innerHTML = "Rendering...";
					document.getElementById('oncoprint_progress_indicator_rect').setAttribute('width', '200px');
					document.getElementById('oncoprint_progress_indicator_rect').setAttribute('fill','#00ff00');
				} else {
					document.getElementById('oncoprint_progress_indicator_text').innerHTML = "Adding data points..";
					document.getElementById('oncoprint_progress_indicator_rect').setAttribute('width', Math.ceil(200*numDataPtsAdded/numDataPts)+'px');
				}
			};
			updateProgressIndicator();
			var geneIndex = 0;
			var addGeneData = function(gene) {
				// We do it like this, recursive and with setTimeouts, because we want the browser to
				//	render the progress message, and if we do this in a loop or do a recursive call
				//	in the same thread, then the browser doesn't actually do the rendering. We need
				//	to force it to render by putting the recursive call on the back of the execution queue.
				var _data = geneData[gene];
				var new_track = oncoprint.addTrack({label: gene, tooltip: genetic_alteration_tooltip});
				if (track_created === false) {
					oncoprint.setRuleSet(new_track, window.Oncoprint.GENETIC_ALTERATION);
					track_created = new_track;
				} else {
					oncoprint.useSameRuleSet(new_track, track_created);
				}
				genetic_alteration_tracks.push(new_track);
				_data = annotateMutationTypes(_data);
				_data = annotatePatientIds(_data);
				oncoprint.setTrackData(new_track, annotateMutationTypes(_data));
				numDataPtsAdded += _data.length;
				updateProgressIndicator();
				geneIndex += 1;
				if (geneIndex < genes.length) {
					setTimeout(function() {
						addGeneData(genes[geneIndex]);
					}, 0);
				} else {
					updateProgressIndicator(true);
					setTimeout(function() {
						oncoprint.releaseRendering();
						$('#oncoprint #everything').show();
						$('#oncoprint_progress_indicator').hide();
						oncoprint.setSortConfig({type:'track'});
						oncoprint.sort();
					}, 0);
				};
			}
			addGeneData(genes[geneIndex]);
		}
	});
	
	(function setUpZoom() {
		var zoom_elt = $('#oncoprint_whole_body #oncoprint_diagram_slider_icon');
		var slider = $('<input>', {
						id: "oncoprint_zoom_slider",
						type: "range",
						width: "80",
						height: "16",
						min: 0,
						max: 1,
						step: 0.01,
						value: 1,
						change: function() {
							oncoprint.setZoom(this.value);
						}
					});
		zoom_elt.append(slider);
		setUpToolbarBtnHover(slider);
		slider.qtip({
			content: {text: 'Zoom in/out of oncoprint'},
			position: {my:'bottom middle', at:'top middle', viewport: $(window)},
			style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
			show: {event: "mouseover"},
			hide: {fixed: true, delay: 100, event: "mouseout"}
		});
		var zoomStep = 0.05;
		$('#oncoprint_whole_body #oncoprint_zoomout').click(function () {
			var slider = $('#oncoprint_whole_body #oncoprint_zoom_slider')[0];
			var currentZoom = parseFloat(slider.value);
			var newZoom = currentZoom - zoomStep;
			slider.value = Math.max(0, newZoom);
			$(slider).trigger('change');
		});
		$('#oncoprint_whole_body #oncoprint_zoomin').click(function () {
			var slider = $('#oncoprint_whole_body #oncoprint_zoom_slider')[0];
			var currentZoom = parseFloat(slider.value);
			var newZoom = currentZoom + zoomStep;
			slider.value = Math.min(1, newZoom);
			$(slider).trigger('change');
		});
	})();
	
	(function setUpToggleWhitespaceBtn() {
		var btn = $('#oncoprint-diagram-removeWhitespace-icon');
		var btn_img = $('#oncoprint-diagram-removeWhitespace-icon img')[0];
		var img_urls = ['images/removeWhitespace.svg', 'images/unremoveWhitespace.svg'];
		var curr_img_url_index = 0;
		btn.click(function() {
			oncoprint.toggleCellPadding();
			curr_img_url_index = +!curr_img_url_index;
			btn_img.attributes.src.value = img_urls[curr_img_url_index];
		});
		setUpToolbarBtnHover(btn);
		btn.qtip({
		content: {text: function() {
				if (curr_img_url_index === 0) {
					return "Remove whitespace between columns";
				} else {
					return "Show whitespace between columns";
				}
			}},
			position: {my: 'bottom middle', at: 'top middle', viewport: $(window)},
			style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'},
			show: {event: "mouseover"},
			hide: {fixed: true, delay: 100, event: "mouseout"}
		});   
	})();
	
	(function setUpRemoveUnalteredCasesBtn() {
		var btn = $('#oncoprint-diagram-removeUCases-icon');
		var imgs = ['images/removeUCases.svg', 'images/unremoveUCases.svg'];
		var descs = ['Hide unaltered cases', 'Show unaltered cases'];
		var unaltered_cases_hidden = false;
		btn.click(function() {
			unaltered_cases_hidden = !unaltered_cases_hidden;
			btn.find('img').attr('src', imgs[+unaltered_cases_hidden]);
			if (!unaltered_cases_hidden) {
				oncoprint.showIds();
			} else {
				var unaltered_ids = oncoprint.getFilteredIdOrder(function(d_list) {
					return _.filter(d_list, function(d) {
						// unaltered gene data iff only keys are gene, sample, patient
						return Object.keys(d).length > 3;
					}).length === 0;
				}, genetic_alteration_tracks);
				oncoprint.hideIds(unaltered_ids);
			}
		});
		btn.qtip({
			content: {text: function() {
					return descs[+unaltered_cases_hidden];
				}},
			position: {my:'bottom middle', at:'top middle', viewport: $(window)},
			style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
			show: {event: "mouseover"},
			hide: {fixed: true, delay: 100, event: "mouseout"}
		});
	})();
}

var annotateMutationTypes = function(data) {
	var ret = _.map(data, function(d) {
		if (d.mutation) {
			var mutations = d.mutation.split(",");
			var hasIndel = false;
			if (mutations.length > 1) {
				for (var i=0, _len = mutations.length; i<_len; i++) {
					if (/\bfusion\b/i.test(mutations[i])) {
						d.mut_type = 'FUSION';
					} else if(!(/^[A-z]([0-9]+)[A-z]$/g).test(mutations[i])) {
						d.mut_type = 'TRUNC';
					} else if ((/^([A-Z]+)([0-9]+)((del)|(ins))$/g).test(mutations[i])) {
						hasIndel = true;
                                        }
				}
				d.mut_type = d.mut_type || (hasIndel ? 'INFRAME' : 'MISSENSE');
			} else {
				if (/\bfusion\b/i.test(mutations)) {
					d.mut_type = 'FUSION';
				} else if((/^[A-z]([0-9]+)[A-z]$/g).test(mutations)) {
					d.mut_type = 'MISSENSE';
				} else if((/^([A-Z]+)([0-9]+)((del)|(ins))$/g).test(mutations)) {
					d.mut_type = 'INFRAME';
				} else {
					d.mut_type = 'TRUNC';
				}
			}
		}
		return d;
	});
	return ret;
};

var annotatePatientIds = function(data) {
	var sampleToPatientId = PortalGlobals.getPatientSampleIdMap();
	return _.map(data, function(d) {
		d.patient = sampleToPatientId[d.sample];
		return d;
	});
};

var addBlankDataToClinicalData = function(data, attr_id, id_key, ids) {
	var ret = data.slice();
	var seen = {};
	_.each(ids, function(id) {
		seen[id] = false;
	});
	_.each(data, function(d) {
		seen[d[id_key]] = true;
	});
	_.each(seen, function(val, id) {
		if (!val) {
			var new_datum = {attr_id: attr_id, attr_val: 'NA'};
			new_datum[id_key] = id;
			ret.push(new_datum);
		}
	});
	return ret;
};

setupOncoprint('#oncoprint_body', 
		cancer_study_id_selected, 
		$('#gene_list').val(), 
		window.PortalGlobals.getCases(), 
		window.PortalGlobals.getGeneticProfiles(), 
		window.PortalGlobals.getZscoreThreshold(),
		window.PortalGlobals.getRppaScoreThreshold()
		);
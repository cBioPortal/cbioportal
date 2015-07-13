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

var mutation_count_tooltip = function(d) {
	var ret = '';
	ret += '<b>'+d.attr_val+' mutations</b><br>';
	ret += '<a href="'+sampleViewUrl(d.sample)+'">'+d.sample+'</a>';
	return ret;
};
var genetic_alteration_tooltip = function(d) {
		var ret = '';
		if (d.mutation) {
			ret += '<b>Mutation: </b>' + d.mutation + '<br>';
		}
		if (d.cna) {
			ret += '<b>Copy Number Alteration: </b>' + d.cna + '<br>';
		}
		if (d.mrna) {
			ret += '<b>MRNA: </b>' + d.mrna + '<br>';
		}
		if (d.rppa) {
			ret += '<b>RPPA: </b>' + d.rppa + '<br>';
		}
		ret += '<a href="'+sampleViewUrl(d.sample)+'">'+d.sample+'</a>';
		return ret;
	};


var utils = window.OncoprintUtils;
var setUpClinicalAttributesSelector = function(cancer_study_id, case_list) {
	var clinicalAttributesColl = new ClinicalAttributesColl();
	var clinicalData = {};
	var clinicalAttributes;
	var originalClinicalAttributes;
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

			originalClinicalAttributes = clinicalAttributes.slice();
			utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), clinicalAttributes);
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
	var MUTATION_COUNT_ATTR_ID = "# mutations";
	var addClinicalTrack = function(clinical_attr) {
		if (clinical_attr.attr_id === MUTATION_COUNT_ATTR_ID) {
			var mutation_count_data = annotatePatientIds(clinicalData[clinical_attr.attr_id]);
			var mutation_count_track = oncoprint.addTrack({label: '# Mutations (Log scale)', tooltip: mutation_count_tooltip}, 0);
			oncoprint.setRuleSet(mutation_count_track, window.Oncoprint.BAR_CHART, {
			data_key: 'attr_val',
				fill: '#c97894',
				legend_label: '# Mutations',
				scale: 'log'
			});
			oncoprint.setTrackData(mutation_count_track, mutation_count_data);
		}
	};
	$('#select_clinical_attributes').change(function() {
		oncoprintFadeTo(0.5);
		var clinicalAttribute = $('#select_clinical_attributes option:selected')[0].__data__;
		if (clinicalAttribute.attr_id === undefined) {
			// selected "none"
		} else {
			if (clinicalData.hasOwnProperty(clinicalAttribute.attr_id)) {
				addClinicalTrack(clinicalAttribute);
			} else {
				if (clinicalAttribute.attr_id === MUTATION_COUNT_ATTR_ID) {
					var currentUrl = window.location.href;
					var clinicalMutationColl = new ClinicalMutationColl();
					clinicalMutationColl.fetch({
						type: "POST",
						data: {
							mutation_profile: window.PortalGlobals.getMutationProfileId(),
							cmd: "count_mutations",
							case_ids: case_list
						},
						success: function(response) {
							clinicalData[clinicalAttribute.attr_id] = response.toJSON();
							addClinicalTrack(clinicalAttribute);
						}
					});
				}
			}
		}
		oncoprintFadeIn();
	});
};
setUpClinicalAttributesSelector(cancer_study_id_selected, window.PortalGlobals.getCases());
$("#oncoprint-diagram-toolbar-buttons").show();















var sampleViewUrl = function(sample_id) {
        var href = cbio.util.getLinkToSampleView(window.cancer_study_id_selected,sample_id);
	return href;
};

var setupOncoprint = function(container_selector_string, cancer_study_id, oql, cases, genetic_profile_ids, z_score_threshold, rppa_score_threshold) {
	var geneDataColl = new GeneDataColl();
	oncoprint = window.oncoprint = window.Oncoprint.create(container_selector_string);
	
	
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
			document.getElementById('oncoprint_progress_indicator').innerHTML = "Adding data points: "+numDataPtsAdded+"/"+numDataPts;
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
				_data = annotateMutationTypes(_data);
				_data = annotatePatientIds(_data);
				oncoprint.setTrackData(new_track, annotateMutationTypes(_data));
				numDataPtsAdded += _data.length;
				document.getElementById('oncoprint_progress_indicator').innerHTML = "Adding data points: "+numDataPtsAdded+"/"+numDataPts;
				geneIndex += 1;
				if (geneIndex < genes.length) {
					setTimeout(function() {
						addGeneData(genes[geneIndex]);
					}, 0);
				} else {
					document.getElementById('oncoprint_progress_indicator').innerHTML = "Rendering...";
					setTimeout(function() {
						oncoprint.releaseRendering();
						$('#outer_loader_img').hide();
						$('#oncoprint #everything').show();
						$('#oncoprint_progress_indicator').hide();
						oncoprint.sort();
					}, 0);
				};
			}
			addGeneData(genes[geneIndex]);
		}
	});
	
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
	
	(function setUpToggleWhitespace() {
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

setupOncoprint('#oncoprint_body', 
		cancer_study_id_selected, 
		$('#gene_list').val(), 
		window.PortalGlobals.getCases(), 
		window.PortalGlobals.getGeneticProfiles(), 
		window.PortalGlobals.getZscoreThreshold(),
		window.PortalGlobals.getRppaScoreThreshold()
		);
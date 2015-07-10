// This is for the moustache-like templates
// prevents collisions with JSP tags
_.templateSettings = {
	interpolate : /\{\{(.+?)\}\}/g
};
$('#oncoprint_controls').html(_.template($('#main-controls-template').html())());

var clinicalAttributesColl = new ClinicalAttributesColl();
var clinicalData = {};














var sampleViewUrl = function(sample_id) {
        var href = cbio.util.getLinkToSampleView(window.cancer_study_id_selected,sample_id);
	return href;
};


var setupOncoprint = function(container_selector_string, cancer_study_id, oql, cases, genetic_profile_ids, z_score_threshold, rppa_score_threshold) {
	var geneDataColl = new GeneDataColl();
	var oncoprint = window.oncoprint = window.Oncoprint.create(container_selector_string);
	
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
			/*_.each(geneData, function(_data, gene) {
				$('#oncoprint_progress_indicator').text(numDataPtsAdded+"/"+numDataPts);
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
				$('#oncoprint_progress_indicator').text(numDataPtsAdded+"/"+numDataPts);
				oncoprint.sort(new_track, window.oncoprint_defaults.genetic_alteration_comparator);
			});
			//oncoprint.releaseRendering();
			$('#outer_loader_img').hide();
                        $('#oncoprint #everything').show();
			oncoprint.sort();*/
		}
	});
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
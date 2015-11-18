window.initDatamanager = function (genetic_profile_ids, oql_query, cancer_study_ids, sample_ids, z_score_threshold, rppa_score_threshold) {
	var oql_parser = window.oql_parser;
	var OQLHandler = (function (config) {
		var default_config = {
			gene_key:"gene",
			cna_key:"cna",
			mutation_key:"mutation",
			mutation_type_key:"mut_type",
			mutation_pos_start_key:"mut_start_position",
			mutation_pos_end_key:"mut_end_position",
			prot_key:"rppa",
			exp_key:"mrna",
			default_oql:""
		};
		config = config || default_config;

		var parse = function(oql_query) {
			var parsed = oql_parser.parse(oql_query);
			
			var datatypes_alterations = false;
			for (var i=0; i<parsed.length; i++) {
				if (parsed[i].gene === "DATATYPES") {
					datatypes_alterations = parsed[i].alterations;
				} else if (datatypes_alterations && !parsed[i].alterations) {
					parsed[i].alterations = datatypes_alterations;
				}
			}
			for (var i=0; i<parsed.length; i++) {
				if (!parsed[i].alterations) {
					parsed[i].alterations = oql_parser.parse("DUMMYGENE:"+config.default_oql+";")[0].alterations;
				}
			};
			return parsed;
		};
		// Helper Functions
		var isCNACmd = function (cmd) {
			return cmd.alteration_type === "cna";
		};
		var isMUTCmd = function (cmd) {
			return cmd.alteration_type === "mut";
		};
		var isMUTClassCmd = function(cmd) {
			return cmd.constr_type === "class";
		};
		var isMUTPositionCmd = function(cmd) {
			return cmd.constr_type === "position";
		};
		var isMUTClass = function(mutation_str) {
			return ["missense","nonsense","nonstart","nonstop","frameshift","inframe","splice","trunc"].indexOf(mutation_str.toLowerCase()) > -1;
		};
		var isEXPCmd = function (cmd) {
			return cmd.alteration_type === "exp";
		};
		var isPROTCmd = function (cmd) {
			return cmd.alteration_type === "prot";
		};
		var isDatumAltered = function(d) {
			return d.__altered;
		};
		var markDatumAltered = function(d) {
			d.__altered = true;
		};
		var unmarkDatumAltered = function(d) {
			delete d['__altered'];
		};
		// Filtering
		var maskDatum = function(datum, alteration_cmds, is_patient_data) {
			// to be passed in: datum and OQL alteration commands corresponding to that gene
			var ret = {};
			var id_key = (is_patient_data ? "patient" : "sample");
			ret[config.gene_key] = datum[config.gene_key];
			ret[id_key] = datum[id_key];
			var altered = false;
			var oql_cna_attr_to_oncoprint_cna_attr = {"amp":"AMPLIFIED","homdel":"HOMODELETED","hetloss":"HEMIZYGOUSLYDELETED","gain":"GAINED"};
			
			var cmd;
			for(var i=0, _len=alteration_cmds.length; i<_len; i++) {
				cmd = alteration_cmds[i];
				if (isCNACmd(cmd)) {
					if (datum[config.cna_key] === oql_cna_attr_to_oncoprint_cna_attr[cmd.constr_val.toLowerCase()]) {
						altered = true;
						ret[config.cna_key] = datum[config.cna_key];
					}
				} else if (isMUTCmd(cmd)) {
					var matches = false;
					if (datum[config.mutation_key]) {
						if (!cmd.constr_rel) {
							matches = true;
						} else {
							if (isMUTClassCmd(cmd)) {
								matches = (datum[config.mutation_type_key] === cmd.constr_val);
							} else if (isMUTPositionCmd(cmd)) {
								matches = (datum[config.mutation_pos_start_key] <= cmd.constr_val && datum[config.mutation_pos_end_key] >= cmd.constr_val);
							} else {
								matches = (datum[config.mutation_key].split(",").indexOf(cmd.constr_val) > -1);
							}
						}
					}
					if (cmd.constr_rel === "!=") {
						matches = !matches;
					}
					if (matches) {
						altered = true;
						ret[config.mutation_key] = datum[config.mutation_key];
						ret[config.mutation_type_key] = datum[config.mutation_type_key];
					}
				} else {
					var matches = false;
					var constr_level = cmd.constr_val;
					var relevant_key = isEXPCmd(cmd) ? config.exp_key : config.prot_key;
					var datum_level = datum[relevant_key];
					var direction = "";
					switch (cmd.constr_rel) {
						case "<":
							matches = (datum_level < constr_level);
							direction = "DOWNREGULATED";
							break;
						case "<=":
							matches = (datum_level <= constr_level);
							direction ="DOWNREGULATED";
							break;
						case ">":
							matches = (datum_level > constr_level);
							direction = "UPREGULATED";
							break;
						case ">=":
							matches = (datum_level >= constr_level);
							direction = "UPREGULATED";
							break;
					}
					if (matches) {
						altered = true;
						ret[relevant_key] = direction;
					}
				}
			}
			if (altered) {
				markDatumAltered(ret);
			}
			return ret;
		};
		var maskGeneData = function (data, parsed_oql_query_line, is_patient_data) {
			return data.map(function (d) {
				if (d[config.gene_key] === parsed_oql_query_line.gene) {
					return maskDatum(d, parsed_oql_query_line.alterations, is_patient_data);
				} else {
					return d;
				}
			});
		};
		
		//-------------------

		return {
			getGenes: function (oql_query) {
				var parse_res = parse(oql_query);
				return parse_res.map(function (q) {
					return q.gene;
				});
			},
			isSyntaxValid: function (oql_query) {
				try {
					parse(oql_query);
					return true;
				} catch (e) {
					return false;
				}
			},
			maskData: function (oql_query, data, is_patient_data) {
				var parse_res = parse(oql_query);
				var id_key = (is_patient_data ? "patient" : "sample");
				// Mask all data
				var i, _len;
				for (i = 0, _len = parse_res.length; i < _len; i++) {
					data = maskGeneData(data, parse_res[i], is_patient_data);
				}
				// Collect altered/unaltered groups
				var altered = {};
				var unaltered = {};
				for (i = 0, _len = data.length; i < _len; i++) {
					var d = data[i];
					if (isDatumAltered(d)) {
						altered[d[id_key]] = true;
					}
				}
				for (i = 0, _len = data.length; i < _len; i++) {
					var d = data[i];
					var id = d[id_key]
					unmarkDatumAltered(d);
					if (!altered.hasOwnProperty(id)) {
						unaltered[id] = true;
					}
				}
				return {data: data, altered: Object.keys(altered), unaltered: Object.keys(unaltered)};
			},
			setDefaultOQL: function(alterations) {
				config.default_oql = alterations;
			}
		};
	})();
	var objEach = function (iterable, callback) {
		for (var k in iterable) {
			if (iterable.hasOwnProperty(k)) {
				callback(iterable[k], k);
			}
		}
	};
	var annotateMutationTypesForOncoprint = function (data) {
		var ret = data.map(function (d) {
			if (d.mutation) {
				var mutations = d.mutation.split(",");
				var hasIndel = false;
				if (mutations.length > 1) {
					for (var i = 0, _len = mutations.length; i < _len; i++) {
						if (/\bfusion\b/i.test(mutations[i])) {
							d.mut_type = 'FUSION';
						} else if (!(/^[A-z]([0-9]+)[A-z]$/g).test(mutations[i])) {
							d.mut_type = 'TRUNC';
						} else if ((/^([A-Z]+)([0-9]+)((del)|(ins))$/g).test(mutations[i])) {
							hasIndel = true;
						}
					}
					d.mut_type = d.mut_type || (hasIndel ? 'INFRAME' : 'MISSENSE');
				} else {
					if (/\bfusion\b/i.test(mutations)) {
						d.mut_type = 'FUSION';
					} else if ((/^[A-z]([0-9]+)[A-z]$/g).test(mutations)) {
						d.mut_type = 'MISSENSE';
					} else if ((/^([A-Z]+)([0-9]+)((del)|(ins))$/g).test(mutations)) {
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
			
	return (function() {
		var dm_ret = {
			'oql_query': oql_query,
			'cancer_study_ids': cancer_study_ids,
			'sample_ids': sample_ids,
			'genetic_profile_ids': genetic_profile_ids,
			'getOQLQuery': function() {
				return this.oql_query;
			},
			'getQueryGenes': function() {
				return OQLHandler.getGenes(this.oql_query);
			},
			'getGeneticProfileIds': function() {
				return this.genetic_profile_ids;
			},
			'getSampleIds': function() {
				return this.sample_ids;
			},
			'getCancerStudyIds': function() {
				return this.cancer_study_ids;
			},
                        'getSampleSelect': function() {
                                return this.sample_select;
                        },
			'getGenomicEventData': function() {
				var def = new $.Deferred();
				var self = this;
				fetchOncoprintGeneData().then(function() {
					def.resolve(self.sample_gene_data);
				}).fail(function() {
					def.reject();
				});
				return def.promise();
			},
			'getCombinedPatientGenomicEventData': function() {
				var def = new $.Deferred();
				var self = this;
				fetchOncoprintGeneData().then(function() {
					def.resolve(self.patient_gene_data);
				}).fail(function() {
					def.reject();
				});
				return def.promise();
			},
			'getAlteredSamples': function () {
				var def = new $.Deferred();
				var self = this;
				fetchOncoprintGeneData().then(function () {
					def.resolve(self.altered_samples);
				}).fail(function() {
					def.reject();
				});
				return def.promise();
			},
			'getUnalteredSamples': function () {
				var def = new $.Deferred();
				var self = this;
				fetchOncoprintGeneData().then(function () {
					def.resolve(self.unaltered_samples);
				}).fail(function() {
					def.reject();
				});
				return def.promise();
			},
			'getAlteredPatients': function() {
				var def = new $.Deferred();
				var self = this;
				fetchOncoprintGeneData().then(function () {
					def.resolve(self.altered_patients);
				}).fail(function() {
					def.reject();
				});
				return def.promise();
			},
			'getUnalteredPatients': function () {
				var def = new $.Deferred();
				var self = this;
				fetchOncoprintGeneData().then(function () {
					def.resolve(self.unaltered_patients);
				}).fail(function() {
					def.reject();
				});
				return def.promise();
			},
			'getSampleClinicalAttributes': function () {
				// TODO: handle more than one study
				return window.cbioportal_client.getSampleClinicalAttributes({study_id: [this.getCancerStudyIds()[0]], sample_ids: this.getSampleIds()});
			},
			'getSampleClinicalData': function (attribute_ids) {
				// TODO: handle more than one study
				var def = new $.Deferred();
				window.cbioportal_client.getSampleClinicalData({study_id: [this.getCancerStudyIds()[0]], attribute_ids: attribute_ids, sample_ids: this.getSampleIds()}).then(function(data) {
					def.resolve(makeOncoprintClinicalData(data));
				}).fail(function() {
					def.reject();
				});
				return def.promise();
			},
                        'getPatientSampleIdMap': function () {
                            var def = new $.Deferred();
                            if(getPatientCaseSelect() === "patient")
                                window.cbioportal_client.getSamples({study_id: getCancerStudyIds(),patient_ids: getSampleIds()}).then(function(_sampleMap){
                                    var samplemap = makeSampleMap(_sampleMap);
                                    def.resolve(samplemap);
                                });
                            else
                            {
                                window.cbioportal_clinet.getSample({study_id: getCancerStudyIds(),sample_ids: getSampleIds()}).then(function(_sampleMap){
                                    var samplemap = makeSampleMap(_sampleMap);
                                    def.resolve(samplemap);
                                });
                            }
                            return def.promise();
                        }
                        
                        
		};
		var fetchOncoprintGeneData = (function() {
			var profile_types = {};
			var sample_to_patient = {};
			var data_fetched = false;

			var makeOncoprintSampleData = function(webservice_gp_data) {
				var cna_string = {"-2":"HOMODELETED","-1":"HEMIZYGOUSLYDELETED","0":undefined,"1":"GAINED","2":"AMPLIFIED"};
				var samp_to_gene_to_datum = {};
				for (var i=0, _len=webservice_gp_data.length; i<_len; i++) {
					var d = webservice_gp_data[i];

					var sample = d.sample_id;
					samp_to_gene_to_datum[sample] = samp_to_gene_to_datum[sample] || {};

					var gene = d.hugo_gene_symbol;
					samp_to_gene_to_datum[sample][gene] = samp_to_gene_to_datum[sample][gene] || {sample:sample, gene:gene};

					var datum = samp_to_gene_to_datum[sample][gene];
					var profile_type = profile_types[d.genetic_profile_id];
					switch (profile_type) {
						case "MUTATION_EXTENDED":
							datum.mutation = (datum.mutation ? datum.mutation+","+d.amino_acid_change  : d.amino_acid_change);
							datum.mut_start_position = parseInt(d.protein_start_position);
							datum.mut_end_position = parseInt(d.protein_end_position);
							break;
						case "COPY_NUMBER_ALTERATION":
							var cna_str = cna_string[d.profile_data];
							if (cna_str) {
								datum.cna = cna_str;
							}
							break;
						case "MRNA_EXPRESSION":
							datum.mrna = parseFloat(d.profile_data, 10);
							break;
						case "PROTEIN_LEVEL":
							datum.rppa = parseFloat(d.profile_data, 10);
							break;
					}
				}
				var ret = [];
				var samples = dm_ret.getSampleIds();
				var genes = dm_ret.getQueryGenes();
				var na_sample_gene = {};
				for (var i=0, _len=samples.length; i<_len; i++) {
					na_sample_gene[samples[i]] = {};
					for (var j=0, _geneslen=genes.length; j<_geneslen; j++) {
						na_sample_gene[samples[i]][genes[j]] = true;
					}
				}
				objEach(samp_to_gene_to_datum, function(gene_to_datum, samp) {
					objEach(gene_to_datum, function(datum, gene) {
						ret.push(datum);
						na_sample_gene[samp][gene] = false;
					});
				});
				objEach(na_sample_gene, function(gene_to_is_na, samp) {
					objEach(gene_to_is_na, function(is_na, gene) {
						if (is_na) {
							ret.push({'sample':samp, 'gene':gene});
						}
					});
				});

				return annotateMutationTypesForOncoprint(ret);
			};
			var makeOncoprintPatientData = function(oncoprint_sample_data) {
				var pat_to_gene_to_datum = {};
				var extremeness = {
					cna: {
						'AMPLIFIED': 2,
						'GAINED': 1,
						'HEMIZYGOUSLYDELETED': 1,
						'HOMODELETED': 2,
						undefined: 0
					},
					mrna: {
						'UPREGULATED': 1,
						'DOWNREGULATED': 1,
						undefined: 0
					},
					rppa: {
						'UPREGULATED': 1,
						'DOWNREGULATED': 1,
						undefined: 0
					}
				};
				for (var i=0, _len=oncoprint_sample_data.length; i<_len; i++) {
					var d = oncoprint_sample_data[i];
					var patient_id = sample_to_patient[d.sample];
					var gene = d.gene;
					pat_to_gene_to_datum[patient_id] = pat_to_gene_to_datum[patient_id] || {};
					pat_to_gene_to_datum[patient_id][gene] = pat_to_gene_to_datum[patient_id][gene] || {patient: patient_id, gene:gene};

					var new_datum = pat_to_gene_to_datum[patient_id][gene];
					objEach(d, function (val, key) {
						if (key === 'mutation') {
							new_datum['mutation'] = (new_datum['mutation'] && (new_datum['mutation'] + ',' + val)) || val;
						} else if (extremeness.hasOwnProperty(key)) {
							if (extremeness[key][val] > extremeness[key][new_datum[key]]) {
								new_datum[key] = val;
							}
						}
					});
				};
				var ret = [];
				objEach(pat_to_gene_to_datum, function(gene_to_datum, samp) {
					objEach(gene_to_datum, function(datum, gene) {
						ret.push(datum);
					});
				});
				return annotateMutationTypesForOncoprint(ret);
			};
			var setDefaultOQL = function() {
				var default_oql_uniq = {};
				objEach(profile_types, function(type, profile_id) {
					switch (type) {
						case "MUTATION_EXTENDED":
							default_oql_uniq["MUT"] = true;
							break;
						case "COPY_NUMBER_ALTERATION":
							default_oql_uniq["AMP"] = true;
							default_oql_uniq["HOMDEL"] = true;
							break;
						case "MRNA_EXPRESSION":
							default_oql_uniq["EXP>="+z_score_threshold] = true;
							default_oql_uniq["EXP<=-"+z_score_threshold] = true;
							break;
						case "PROTEIN_LEVEL":
							default_oql_uniq["PROT>="+rppa_score_threshold] = true;
							default_oql_uniq["PROT<=-"+rppa_score_threshold] = true;
							break;
					}
				});
				var default_oql = Object.keys(default_oql_uniq).join(" ");
				OQLHandler.setDefaultOQL(default_oql);
			};
			return function() {
				var def = $.Deferred();
				try {
					if (data_fetched) {
						def.resolve();
					} else {
						$.when(window.cbioportal_client.getGeneticProfiles({genetic_profile_ids: dm_ret.getGeneticProfileIds()}),
							window.cbioportal_client.getSamples({study_id: [cancer_study_ids[0]], sample_ids: sample_ids}))
						.then(function(gp_response, sample_response) {
							for (var i = 0; i < gp_response.length; i++) {
								profile_types[gp_response[i].id] = gp_response[i].genetic_alteration_type;
							}
							setDefaultOQL();

							for (var i = 0; i < sample_response.length; i++) {
								sample_to_patient[sample_response[i].id] = sample_response[i].patient_id;
							}
						}).fail(function() {
							def.reject();
						}).then(function() {
							return window.cbioportal_client.getGeneticProfileData({genetic_profile_ids: dm_ret.getGeneticProfileIds(), genes: dm_ret.getQueryGenes(), sample_ids: dm_ret.getSampleIds()});
						}).fail(function() {
							def.reject();
						}).then(function(response) {
							var oql_process_result = OQLHandler.maskData(dm_ret.getOQLQuery(), makeOncoprintSampleData(response));
							dm_ret.sample_gene_data = oql_process_result.data;
							dm_ret.altered_samples = oql_process_result.altered;
							dm_ret.unaltered_samples = oql_process_result.unaltered;

							var oql_process_result_patient = OQLHandler.maskData(dm_ret.getOQLQuery(), makeOncoprintPatientData(dm_ret.sample_gene_data), true);
							dm_ret.patient_gene_data = oql_process_result_patient.data;
							dm_ret.altered_patients = oql_process_result_patient.altered;
							dm_ret.unaltered_patients = oql_process_result_patient.unaltered;

							data_fetched = true;
							def.resolve();
						}).fail(function() {
							def.reject();
						});
					}
				} catch (err) {
					def.reject();
				}
				return def.promise();
			};
		})();
		var makeOncoprintClinicalData = function(webservice_clinical_data) {
			var ret = [];
			for (var i=0, _len=webservice_clinical_data.length; i<_len; i++) {
				var d = webservice_clinical_data[i];
				ret.push({'attr_id':d.attr_id, 'attr_val':d.attr_val, 'sample':d.sample_id});
			}
			return ret;
		};
                var makeSampleMap = function(data) {
                    var all_samples = {};
                    for (var i=0,_len=data.length; i<_len; i++) {
                        var d = data[i];
                        all_samples[d.id]= d.patient_id;
                    };
                    return all_samples;
                };
		

		return dm_ret;
	})();
};
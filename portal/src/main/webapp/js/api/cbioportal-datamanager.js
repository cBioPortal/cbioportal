window.initDatamanager = function (genetic_profile_ids, oql_query, cancer_study_ids, sample_ids, z_score_threshold, rppa_score_threshold,
					case_set_properties, cancer_study_names, profile_ids) {
	var oql_parser = window.oql_parser;
	var OQLHandler = (function (config) {
		var default_config = {
			gene_key:"gene",
			cna_key:"cna",
			mutation_key:"mutation",
			mutation_type_key:"mut_type",
			mutation_types_key: "mut_types",
			mutation_pos_start_key:"mut_start_position",
			mutation_pos_end_key:"mut_end_position",
			mutation_amino_acid_change_key:"amino_acid_change",
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
			
			if (config.default_oql.length > 0) {
				for (var i=0; i<parsed.length; i++) {
					if (!parsed[i].alterations) {
						parsed[i].alterations = oql_parser.parse("DUMMYGENE:"+config.default_oql+";")[0].alterations;
					}
				};
			}
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
		
		var getMatchingMutationsByType = function(mutations, oql_target_type, relation) {
			var ret = [];
			for (var i=0; i<mutations.length; i++) {
			var type = mutations[i][config.mutation_type_key];
			var proposition = (type === oql_target_type || (oql_target_type === "TRUNC" && type !== "MISSENSE" && type !== "INFRAME"));
			if (relation === "!=") {
				proposition = !proposition;
			}
			if (proposition) {
				ret.push(i);
			}
			}
			return ret;
		};
		
		var getMatchingMutationsByPosition = function(mutations, target_pos, relation) {
			var ret = [];
			for (var i=0; i<mutations.length; i++) {
			var proposition = (mutations[i][config.mutation_pos_start_key] <= target_pos && mutations[i][config.mutation_pos_end_key] >= target_pos);
			if (relation === "!=") {
				proposition = !proposition;
			}
			if (proposition) {
				ret.push(i);
			}
			}
			return ret;
		};
		
		var getMatchingMutationsByAminoAcidChange = function(mutations, target_amino_acid_change, relation) {
			var ret = [];
			target_amino_acid_change = target_amino_acid_change.toLowerCase();
			for (var i=0; i<mutations.length; i++) {
			var proposition = (mutations[i][config.mutation_amino_acid_change_key].toLowerCase() === target_amino_acid_change);
			if (relation === "!=") {
				proposition = !proposition;
			}
			if (proposition) {
				ret.push(i);
			}
			}
			return ret;
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
		var getOncoprintMutationType = function(type) {
				var ret = "";
				switch (type) {
				case "MISSENSE":
				case "FUSION":
				case "INFRAME":
					ret = type;
					break;
				default:
					ret = "TRUNC";
					break;
				}
				return ret;
		};
		var maskDatum = function(datum, alteration_cmds, is_patient_data) {
			// to be passed in: datum and OQL alteration commands corresponding to that gene
			var ret = {};
			var id_key = (is_patient_data ? "patient" : "sample");
			ret[config.gene_key] = datum[config.gene_key];
			ret[id_key] = datum[id_key];
			var altered = false;
			var oql_cna_attr_to_oncoprint_cna_attr = {"amp":"AMPLIFIED","homdel":"HOMODELETED","hetloss":"HEMIZYGOUSLYDELETED","gain":"GAINED"};
			var mutation_rendering_priority = {"FUSION":0,"TRUNC":1,"INFRAME":2,"MISSENSE":3}
			
			var cmd;
			var matching_mutations = {};
			for(var i=0, _len=alteration_cmds.length; i<_len; i++) {
				cmd = alteration_cmds[i];
				if (isCNACmd(cmd)) {
					if (datum[config.cna_key] === oql_cna_attr_to_oncoprint_cna_attr[cmd.constr_val.toLowerCase()]) {
						altered = true;
						ret[config.cna_key] = datum[config.cna_key];
					}
				} else if (isMUTCmd(cmd)) {
					var cmd_matching_mutations = [];
					if (datum[config.mutation_key]) {
						if (!cmd.constr_rel) {
							for (var j=0; j< datum[config.mutation_key].length; j++) {
								cmd_matching_mutations.push(j);
							}
						} else {
							if (isMUTClassCmd(cmd)) {
								cmd_matching_mutations = getMatchingMutationsByType(datum[config.mutation_key], cmd.constr_val, cmd.constr_rel);
							} else if (isMUTPositionCmd(cmd)) {
								cmd_matching_mutations = getMatchingMutationsByPosition(datum[config.mutation_key], cmd.constr_val, cmd.constr_rel);
							} else {
								cmd_matching_mutations = getMatchingMutationsByAminoAcidChange(datum[config.mutation_key], cmd.constr_val, cmd.constr_rel);
							}
						}
					}
					if (cmd_matching_mutations.length > 0) {
						altered = true;
						for (var j=0; j<cmd_matching_mutations.length; j++) {
							matching_mutations[cmd_matching_mutations[j]] = true;
							//matching_mutations[j][config.mutation_type_key] = getOncoprintMutationType(matching_mutations[j][config.mutation_type_key]);
						}
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
			matching_mutations = Object.keys(matching_mutations).map(function(x) { 
				var mut = datum[config.mutation_key][x]; 
				var new_mut = {};
				new_mut[config.mutation_type_key] = getOncoprintMutationType(mut[config.mutation_type_key]);
				new_mut[config.mutation_amino_acid_change_key] = mut[config.mutation_amino_acid_change_key];
				return new_mut;
			});
			if (matching_mutations.length > 0) {
				var mutation_type_key = config.mutation_type_key;
				matching_mutations.sort(function(a,b) { return mutation_rendering_priority[a[mutation_type_key]] - mutation_rendering_priority[b[mutation_type_key]]; });
				ret[config.mutation_type_key] = matching_mutations[0][config.mutation_type_key];
				ret[config.mutation_key] = matching_mutations.map(function(m) {
				return m[config.mutation_amino_acid_change_key];
				}).join(",");
			}
			if (altered) {
				markDatumAltered(ret);
			}
			return ret;
		};
		var maskGeneData = function (data, parsed_oql_query_line, is_patient_data) {
			//iterate over data items and mask the data for the items related to the 
			var masked_data = data.slice();
			for (var i = 0; i < masked_data.length; i++) {
				if (masked_data[i][config.gene_key] === parsed_oql_query_line.gene) { //TODO - change to gene_field_name, gene_key is a bit misleading
					masked_data[i] = maskDatum(masked_data[i], parsed_oql_query_line.alterations, is_patient_data);
				}
			}
			return masked_data;
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
				var masked_data = data;
				for (i = 0, _len = parse_res.length; i < _len; i++) {
					masked_data = maskGeneData(masked_data, parse_res[i], is_patient_data);
				}
				// Collect altered/unaltered groups
				var altered = {};
				var unaltered = {};
				for (i = 0, _len = masked_data.length; i < _len; i++) {
					var d = masked_data[i];
					if (isDatumAltered(d)) {
						altered[d[id_key]] = true;
					}
				}
				for (i = 0, _len = masked_data.length; i < _len; i++) {
					var d = masked_data[i];
					var id = d[id_key]
					unmarkDatumAltered(d);
					if (!altered.hasOwnProperty(id)) {
						unaltered[id] = true;
					}
				}
				return {data: masked_data, altered: Object.keys(altered), unaltered: Object.keys(unaltered)};
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
			'getCaseSetId': function() {
				return case_set_properties.case_set_id;
			},
			'getCaseIdsKey': function() {
				return case_set_properties.case_ids_key;
			},
			'getSampleSetName': function() {
				return case_set_properties.case_set_name;
			},
			'getSampleSetDescription': function() {
				return case_set_properties.case_set_description;
			},
						'getPatientSampleIdMap': (function() {
				var sample_to_patient = {};
				var loaded = false;
				return function() {
					var def = new $.Deferred();
					if (loaded) {
						def.resolve(sample_to_patient);
					} else {
						window.cbioportal_client.getSamples({study_id: [this.getCancerStudyIds()[0]], sample_ids: this.getSampleIds()}).then(function(data) {
							for (var i=0; i<data.length; i++) {
								sample_to_patient[data[i].id] = data[i].patient_id;
							}
							loaded = true;
							def.resolve(sample_to_patient);
						}).fail(function() {
							def.reject();
						});
					}
					return def.promise();
				};
			})(),
			'getCancerStudyNames': function() {
				return cancer_study_names;
			},
			'getMutationProfileId': function() {
				return profile_ids.mutation_profile_id;
			}
						
		};
		var fetchOncoprintGeneData = (function() {
			var profile_types = {};
			var sample_to_patient = {};
			var data_fetched = false;
			var data_fetching = false;

			
			var getMutationType = function(d) {
				var ret = "";
				var type = d.mutation_type.toLowerCase();
				switch (type) {
				case "missense_mutation":
				case "missense":
				case "missense_variant":
					ret = "MISSENSE";
					break;
				case "frame_shift_ins":
				case "frame_shift_del":
				case "frameshift":
				case "frameshift_deletion":
				case "frameshift_insertion":
				case "de_novo_start_outofframe":
				case "frameshift_variant":
					ret = "FRAMESHIFT";
					break;
				case "nonsense_mutation":
				case "nonsense":
				case "stopgain_snv":
					ret = "NONSENSE";
					break;
				case "splice_site":
				case "splice":
				case "splice site":
				case "splicing":
				case "splice_site_snp":
				case "splice_site_del":
				case "splice_site_indel":
					ret = "SPLICE";
					break;
				case "translation_start_site":
				case "start_codon_snp":
				case "start_codon_del":
					ret = "NONSTART";
					break;
				case "nonstop_mutation":
					ret = "NONSTOP";
					break;
				case "fusion":
					ret = "FUSION";
					break;
				case "in_frame_del":
				case "in_frame_ins":
				case "indel":
				case "nonframeshift_deletion":
				case "nonframeshift":
				case "nonframeshift insertion":
				case "nonframeshift_insertion":
				case "targeted_region":
					ret = "INFRAME";
					break;
				default:
					ret = "OTHER";
					break;
				}
				return ret;
			};
			
			var makeSampleData = function(webservice_gp_data) {
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
							datum.mutation = datum.mutation || [];
							datum.mutation.push({mut_type: getMutationType(d),
										amino_acid_change: d.amino_acid_change,
										mut_start_position: parseInt(d.protein_start_position),
										mut_end_position: parseInt(d.protein_end_position)});
							/*datum.mutations[mutation_type] = datum.mutations[mutation_type] || [];
							datum.mutations[mutation_type].push(d.amino_acid_change);
							datum.mutation = (datum.mutation ? datum.mutation+","+d.amino_acid_change  : d.amino_acid_change);
							datum.mut_types = (datum.mut_types ? datum.mut_types+","+getOncoprintMutationType(d) : getOncoprintMutationType(d));
							datum.mut_start_position = parseInt(d.protein_start_position);
							datum.mut_end_position = parseInt(d.protein_end_position);*/
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

				return ret;
			};
			var makePatientData = function(oql_process_result) {
				var pat_to_gene_to_datum = {};
				var extremeness = {
					mut_type: {
						'FUSION': 4,
						'TRUNC': 3,
						'INFRAME': 2,
						'MISSENSE': 1,
						undefined: 0
					},
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
								
				//combine data
				var sample_data = oql_process_result.data;
				for (var i=0, _len=sample_data.length; i<_len; i++) {
					var d = sample_data[i];
					var patient_id = sample_to_patient[d.sample];
					var gene = d.gene;
					pat_to_gene_to_datum[patient_id] = pat_to_gene_to_datum[patient_id] || {};
					pat_to_gene_to_datum[patient_id][gene] = pat_to_gene_to_datum[patient_id][gene] || {patient: patient_id, gene:gene};

					var new_datum = pat_to_gene_to_datum[patient_id][gene];
					objEach(d, function (val, key) {
						if (extremeness.hasOwnProperty(key)) {
							if (extremeness[key][val] > extremeness[key][new_datum[key]]) {
								new_datum[key] = val;
							}
						} else if (key === "mutation") {
							new_datum['mutation'] = (new_datum['mutation'] || []).concat(d['mutation']);
						}
					});
				};
				var patient_data = [];
				objEach(pat_to_gene_to_datum, function(gene_to_datum, samp) {
					objEach(gene_to_datum, function(datum, gene) {
						if ("mutation" in datum) {
							datum.mutation = datum.mutation.join(",");
						}
						patient_data.push(datum);
					});
				});

				// combine altered
				// a patient is altered if at least one of its samples is altered
				var altered_samples = oql_process_result.altered;
				var altered_patients = _.uniq(_.map(altered_samples, function(s) {return sample_to_patient[s]}));

				// combine unaltered
				// a patient is unaltered if none of its samples are altered
				var unaltered_samples = oql_process_result.unaltered;
				var unaltered_patients = _.difference(_.uniq(_.map(unaltered_samples, function(s) {return sample_to_patient[s]})), altered_patients);
								
				return {data:patient_data, altered: altered_patients, unaltered:unaltered_patients};
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
			var def = new $.Deferred();
			return function() {
				try {
					if (data_fetched) {
						def.resolve();
					} else {
						if (!data_fetching) {
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
															if (case_set_properties.case_set_id === "-1") {
																// custom case list
																console.log("by samples");
								return window.cbioportal_client.getGeneticProfileDataBySample({genetic_profile_ids: dm_ret.getGeneticProfileIds(), genes: dm_ret.getQueryGenes(), sample_ids: dm_ret.getSampleIds()});
															} else {
																console.log("by sample list");
																return window.cbioportal_client.getGeneticProfileDataBySampleList({genetic_profile_ids: dm_ret.getGeneticProfileIds(), genes: dm_ret.getQueryGenes(), sample_list_id: [case_set_properties.case_set_id]});
															}
							}).fail(function() {
								def.reject();
							}).then(function(response) {
								var unmasked_sample_data = makeSampleData(response);
								var oql_process_result = OQLHandler.maskData(dm_ret.getOQLQuery(), unmasked_sample_data);
								
								dm_ret.sample_gene_data = oql_process_result.data;
								dm_ret.altered_samples = oql_process_result.altered;
								dm_ret.unaltered_samples = oql_process_result.unaltered;

								var oql_process_result_patient = makePatientData(oql_process_result);
								dm_ret.patient_gene_data = oql_process_result_patient.data;
								dm_ret.altered_patients = oql_process_result_patient.altered;
								dm_ret.unaltered_patients = oql_process_result_patient.unaltered;

								data_fetched = true;
								def.resolve();
							}).fail(function() {
								def.reject();
							});
							data_fetching = true;
						}
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
		
		return dm_ret;
	})();
};

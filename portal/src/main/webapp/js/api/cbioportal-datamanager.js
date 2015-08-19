window.initDatamanager = function (genetic_profile_ids, oql_query, cancer_study_ids, sample_ids) {
	var webservice = (function () {
		var makeQueryParams = function (args) {
			var arg_strings = [];
			for (var k in args) {
				if (args.hasOwnProperty(k) && args[k].length > 0) {
					arg_strings.push(k + '=' + args[k].join(","));
				}
			}
			return arg_strings.join('&');
		};
		var apiCall = function (endpt, args) {
			var url = endpt + '?' + makeQueryParams(args);
			return $.getJSON(url);
		};
		return {
			getCancerTypes: function (args) {
				return apiCall('api/meta/cancertypes', args);
			},
			getGenes: function (args) {
				return apiCall('api/meta/genes', args);
			},
			getPatients: function (args) {
				return apiCall('api/meta/patients', args);
			},
			getSamples: function (args) {
				return apiCall('api/meta/samples', args);
			},
			getStudies: function (args) {
				return apiCall('api/meta/studies', args);
			},
			getGeneSets: function (args) {
				return apiCall('api/meta/genesets', args);
			},
			getPatientLists: function (args) {
				return apiCall('api/meta/patientlists', args);
			},
			getGeneticProfiles: function (args) {
				return apiCall('api/meta/profiles', args);
			},
			getPatientClinicalAttributes: function (args) {
				return apiCall('api/meta/clinical/patients', args);
			},
			getSampleClinicalAttributes: function (args) {
				return apiCall('api/meta/clinical/samples', args);
			},
			getPatientClinicalData: function (args) {
				return apiCall('api/data/clinical/patients', args);
			},
			getSampleClinicalData: function (args) {
				return apiCall('api/data/clinical/samples', args);
			},
			getSampleGeneticProfileData: function (args) {
				return apiCall('api/data/profiles', args);
			}
		}
	})();
	var OQLHandler = (function (config) {
		var default_config = {
			gene_key:"gene",
			cna_key:"cna",
			mutation_key:"mutation",
			mutation_type_key:"mut_type",
			prot_key:"rppa",
			exp_key:"mrna",
			default_oql:"MUT HOMDEL AMP"
		};
		config = config || default_config;

		var parse = function(oql_query) {
			var parsed = window.oql_parser.parse(oql_query);
			for (var i=0; i<parsed.length; i++) {
				if (!parsed[i].alterations) {
					parsed[i].alterations = window.oql_parser.parse("DUMMYGENE:"+config.default_oql+";")[0].alterations;
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
			
	return (function() {
		var objEach = function(iterable, callback) {
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
		var samples_by_stable = {};
		var samples_by_internal = {};

		var fetchSamples = (function() {
			var samples_fetched = false;
			return function() {
				var def = new $.Deferred();
				if (samples_fetched) {
					def.resolve();
				} else {
					webservice.getSamples({study_ids: dm_ret.getCancerStudyIds(), sample_ids: dm_ret.getSampleIds()}).then(function(response) {
						for (var i = 0; i < response.length; i++) {
							samples_by_stable[response[i].stable_id] = response[i];
							samples_by_internal[response[i].internal_id] = response[i];
						}
						samples_fetched = true;
						def.resolve();
					});
				}
				return def.promise();
			};
		})();
		var fetchOncoprintGeneData = (function() {
			var profile_types_by_internal = {};
			var genes_by_entrez = {};
			var patients_by_internal = {};
			var data_fetched = false;

			var makeOncoprintSampleData = function(webservice_gp_data) {
				var cna_string = {"-2":"HOMODELETED","-1":"HEMIZYGOUSLYDELETED","0":undefined,"1":"GAINED","2":"AMPLIFIED"};
				var samp_to_gene_to_datum = {};
				for (var i=0, _len=webservice_gp_data.length; i<_len; i++) {
					var d = webservice_gp_data[i];

					var sample = samples_by_internal[d.internal_sample_id].stable_id;
					samp_to_gene_to_datum[sample] = samp_to_gene_to_datum[sample] || {};

					var gene = genes_by_entrez[d.entrez_gene_id].hugoGeneSymbol;
					samp_to_gene_to_datum[sample][gene] = samp_to_gene_to_datum[sample][gene] || {sample:sample, gene:gene};

					var datum = samp_to_gene_to_datum[sample][gene];
					var profile_type = profile_types_by_internal[d.internal_id];
					switch (profile_type) {
						case "MUTATION_EXTENDED":
							datum.mutation = (datum.mutation ? datum.mutation+","+d.amino_acid_change  : d.amino_acid_change);
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
						case "PROTEIN_ARRAY_PROTEIN_LEVEL":
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
					var patient_id = patients_by_internal[samples_by_stable[d.sample].patient_id].stable_id;
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
				objEach(profile_types_by_internal, function(type, internal_id) {
					switch (type) {
						case "MUTATION_EXTENDED":
							default_oql_uniq["MUT"] = true;
							break;
						case "COPY_NUMBER_ALTERATION":
							default_oql_uniq["AMP"] = true;
							default_oql_uniq["HOMDEL"] = true;
							break;
						case "MRNA_EXPRESSION":
							default_oql_uniq["EXP>=2"] = true;
							default_oql_uniq["EXP<=-2"] = true;
							break;
						case "PROTEIN_ARRAY_PROTEIN_LEVEL":
							default_oql_uniq["PROT>=2"] = true;
							default_oql_uniq["PROT<=-2"] = true;
							break;
					}
				});
				var default_oql = Object.keys(default_oql_uniq).join(" ");
				OQLHandler.setDefaultOQL(default_oql);
			};
			return function() {
				var def = $.Deferred();
				if (data_fetched) {
					def.resolve();
				} else {
					var entrez_gene_ids = [];
					var internal_profile_ids = [];
					var internal_sample_ids = [];
					webservice.getGenes({ids: dm_ret.getQueryGenes()}).then(function (response) {
						for (var i = 0; i < response.length; i++) {
							entrez_gene_ids.push(response[i].entrezGeneId);
							genes_by_entrez[response[i].entrezGeneId] = response[i];
						}
						return webservice.getGeneticProfiles({profile_ids: dm_ret.getGeneticProfileIds()});
					}).then(function (response) {
						for (var i = 0; i < response.length; i++) {
							internal_profile_ids.push(response[i].internal_id);
							profile_types_by_internal[response[i].internal_id] = response[i].genetic_alteration_type;
						}
						setDefaultOQL();
						return fetchSamples();
					}).then(function() {
						var patient_ids = {};
						var samples = Object.keys(samples_by_stable).map(function(k) { return samples_by_stable[k];});
						for (var i = 0; i < samples.length; i++) {
							internal_sample_ids.push(samples[i].internal_id);
							patient_ids[samples[i].patient_id] = true;
						}
						return webservice.getPatients({patient_ids: Object.keys(patient_ids)});
					}).then(function(response) {
						for (var i = 0; i < response.length; i++) {
							patients_by_internal[response[i].internal_id] = response[i];
						}
					}).then(function () {
						return webservice.getSampleGeneticProfileData({genes: entrez_gene_ids, profile_ids: internal_profile_ids, sample_ids: internal_sample_ids});
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
					});
				}
				return def.promise();
			};
		})();
		var makeOncoprintClinicalData = function(webservice_clinical_data) {
			var def = new $.Deferred();
			fetchSamples().then(function() {
				var ret = [];
				for (var i=0, _len=webservice_clinical_data.length; i<_len; i++) {
					var d = webservice_clinical_data[i];
					ret.push({'attr_id':d.attr_id, 'attr_val':d.attr_val, 'sample':samples_by_internal[d.sample_id].stable_id});
				}
				def.resolve(ret);
			});
			return def.promise();
		};
		var dm_ret = {};
		dm_ret.oql_query = oql_query;
		dm_ret.cancer_study_ids = cancer_study_ids;
		dm_ret.sample_ids = sample_ids;
		dm_ret.genetic_profile_ids = genetic_profile_ids;
		dm_ret.getOQLQuery = function() {
			return this.oql_query;
		};
		dm_ret.getQueryGenes = function() {
			return OQLHandler.getGenes(this.oql_query);
		};
		dm_ret.getGeneticProfileIds = function() {
			return this.genetic_profile_ids;
		};
		dm_ret.getSampleIds = function() {
			return this.sample_ids;
		};
		dm_ret.getCancerStudyIds = function() {
			return this.cancer_study_ids;
		};
		dm_ret.getGenomicEventData = function() {
			var def = new $.Deferred()
			fetchOncoprintGeneData().then(function() {
				def.resolve(dm_ret.sample_gene_data);
			});
			return def.promise();
		};
		dm_ret.getCombinedPatientGenomicEventData = function() {
			var def = new $.Deferred()
			fetchOncoprintGeneData().then(function() {
				def.resolve(dm_ret.patient_gene_data);
			});
			return def.promise();
		};
		dm_ret.getAlteredSamples = function() {
			var def = new $.Deferred()
			fetchOncoprintGeneData().then(function() {
				def.resolve(dm_ret.altered_samples);
			});
			return def.promise();
		};
		dm_ret.getUnalteredSamples = function() {
			var def = new $.Deferred()
			fetchOncoprintGeneData().then(function() {
				def.resolve(dm_ret.unaltered_samples);
			});
			return def.promise();

		};
		dm_ret.getAlteredPatients = function() {
			var def = new $.Deferred()
			fetchOncoprintGeneData().then(function() {
				def.resolve(dm_ret.altered_patients);
			});
			return def.promise();
		};
		dm_ret.getUnalteredPatients = function() {
			var def = new $.Deferred()
			fetchOncoprintGeneData().then(function() {
				def.resolve(dm_ret.unaltered_patients);
			});
			return def.promise();
		};

		dm_ret.getSampleClinicalAttributes = function() {
			var def = new $.Deferred();
			if (dm_ret.sample_clinical_attrs) {
				def.resolve(dm_ret.sample_clinical_attrs);
			} else {
				webservice.getSampleClinicalAttributes({study_ids: dm_ret.getCancerStudyIds(), sample_ids: dm_ret.getSampleIds()}).then(function(response) {
					dm_ret.sample_clinical_attrs = response;
					def.resolve(dm_ret.sample_clinical_attrs);
				});
			}
			return def.promise();
		};
		dm_ret.getSampleClinicalData = function() {
			var def = new $.Deferred();
			if (dm_ret.sample_clinical_data) {
				def.resolve(dm_ret.sample_clinical_data);
			} else {
				webservice.getSampleClinicalData({study_ids: dm_ret.getCancerStudyIds(), sample_ids: dm_ret.getSampleIds()}).then(function(response) {
					makeOncoprintClinicalData(response).then(function(clin_dat) {
						dm_ret.sample_clinical_data = clin_dat;
						def.resolve(dm_ret.sample_clinical_data);
					});
				});
			}
			return def.promise();
		};

		return dm_ret;
	})();
};



/*var stringSetDifference = function (A, B) {
		// In A and not in B
		var in_A_not_in_B = {};
		var i, _len;
		for (i = 0, _len = A.length; i < _len; i++) {
			in_A_not_in_B[A[i]] = true;
		}
		for (i = 0, _len = B.length; i < _len; i++) {
			in_A_not_in_B[B[i]] = false;
		}
		var ret = [];
		for (i = 0, _len = A.length; i < _len; i++) {
			if (in_A_not_in_B[A[i]]) {
				ret.push(A[i]);
			}
		}
		return ret;
	};
	var stringSetEquals = function(A,B) {
		return ((stringSetDifference(A,B).length + stringSetDifference(B,A).length) === 0);
	};
	function Index(key) {
		var map = {};
		this.addData = function (data, args) {
			var i;
			var _len = data.length;
			// Clear existing data for touched keys
			for (i = 0; i < _len; i++) {
				var datum_key = key(data[i], args);
				map[datum_key] = [];
			}
			// Add data
			for (i = 0; i < _len; i++) {
				var d = data[i];
				map[key(d, args)].push(d);
			}
		};
		this.getData = function (keys, datumFilter) {
			keys = keys || Object.keys(map);
			var i, datum;
			var ret = [], _len = keys.length;
			for (i = 0; i < _len; i++) {
				datum = map[keys[i]];
				if (typeof datum !== 'undefined' && (!datumFilter || datumFilter(datum))) {
					ret = ret.concat(datum);
				}
			}
			return ret;
		};
		
		this.missingKeys = function (keys) {
			return stringSetDifference(keys, Object.keys(map));
		};
	};
	var objEach = function(iterable, callback) {
		for (var k in iterable) {
			if (iterable.hasOwnProperty(k)) {
				callback(iterable[k], k);
			}
		}
	};
	
	var makeConvertArgument = function(source_argument_name, target_argument_name, datum_key, getterServiceName, sourceArgsToGetterArgs, gotDataToNewArg) {
		return function(args) {
			var def = new $.Deferred();
			var getter_args = sourceArgsToGetterArgs(args);
			if (getter_args) {
				window[getterServiceName](getter_args).then(function(response) {
					args[target_argument_name] = gotDataToNewArg(response);
					delete args[source_argument_name];
					def.resolve(args);
				});
			} else {
				def.resolve(args);
			}
		};
	};
	var convertStableStudyIdsToInternal = makeConvertArgument('study_ids', 'internal_study_ids',
								'getStudies',
								function(args) {
									return (args.study_ids && args.study_ids.length > 0 ? {study_ids: args.study_ids} : false);
								},
								function(response) {
									var new_args = {};
									for (var i = 0, _len = response.length; i < _len; i++) {
										new_args[response[i]['internal_id']] = true;
									}
									return Object.keys(new_args);
								});
	var convertStableSampleIdsToInternal = makeConvertArgument('sample_ids', 'internal_sample_ids', 
								'getSamples', 
								function(args) {
									return (args.sample_ids && args.sample_ids.length > 0 ? {study_ids: args.study_ids, sample_ids: args.sample_ids} : false);
								},
								function(response) {
									var new_args = {};
									for (var i = 0, _len = response.length; i < _len; i++) {
										new_args[response[i]['internal_id']] = true;
									}
									return Object.keys(new_args);
								});
	var convertStablePatientIdsToInternal = makeConvertArgument('patient_ids','internal_patient_ids',
								'getPatients',
								function(args) {
									return (args.patient_ids && args.patient_ids.length > 0 ? {study_ids: args.study_ids, patient_ids: args.patient_ids} : false);
								},
								function(response) {
									var new_args = {};
									for (var i = 0, _len = response.length; i < _len; i++) {
										new_args[response[i]['internal_id']] = true;
									}
									return Object.keys(new_args);
								});
				
	var convertHugoGeneSymbolsToEntrezGeneIds = makeConvertArgument('hugo_gene_symbols','entrez_gene_ids',
								'getGenes',
								function(args) {
									return (args.hugo_gene_symbols && args.hugo_gene_symbols.length > 0 ? {hugo_gene_symbols: args.hugo_gene_symbols} : false);
								},
								function(response) {
									var new_args = {};
									for (var i = 0, _len = response.length; i < _len; i++) {
										new_args[response[i]['entrezGeneId']] = true;
									}
									return Object.keys(new_args);
								});
	var convertStableProfileIdsToInternal = makeConvertArgument('profile_ids','internal_profile_ids',
								'getGeneticProfiles',
								function(args) {
									return (args.profile_ids && args.profile_ids.length > 0 ? {profile_ids: args.profile_ids} : false);
								},
								function(response) {
									var new_args = {};
									for (var i = 0, _len = response.length; i < _len; i++) {
										new_args[response[i]['internal_id']] = true;
									}
									return Object.keys(new_args);
								});
	
	var convertStablePatientListIdsToInternal = makeConvertArgument('patient_list_ids', 'internal_patient_list_ids',
									'getPatientLists',
									function(args) {
										return (args.patient_list_ids && args.patient_list_ids.length > 0 ? {patient_list_ids: args.patient_list_ids} : false);
									},
									function(response) {
										var new_args = {};
										for (var i = 0, _len = response.length; i < _len; i++) {
											new_args[response[i]['internal_id']] = true;
										}
										return Object.keys(new_args);
									});
	var convertInternalPatientListIdsToInternalPatientIds = makeConvertArgument('internal_patient_list_ids', 'internal_patient_ids',
								'getPatientLists',
								function(args) {
									return (args.internal_patient_list_ids && args.internal_patient_list_ids.length > 0 ? {internal_patient_list_ids: args.internal_patient_list_ids} : false);
								},
								function(response) {
									var new_args = {};
									for (var i = 0, _len = response.length; i < _len; i++) {
										var patient_ids = response[i].internal_patient_ids;
										if (patient_ids) {
											for (var j=0;j<patient_ids.length;j++) {
												new_args[patient_ids[j]] = true;
											}
										}
									}
									return Object.keys(new_args);
								});
	
	var normalizeArgs = function(args) {
		return convertStableStudyIdsToInternal(args).then(convertStableSampleIdsToInternal).then(convertStablePatientIdsToInternal);
	};
	var makeGetPatientsOrSamples = function (patients) {
		return (function () {
			var stable_id_index = {};
			var internal_id_index = new Index(function (d) {
				return d['internal_id'];
			});
			var internal_study_id_index = new Index(function (d) {
				return d['study_id'];
			});

			var addToStableIdIndex = function (data) {
				for (var i = 0, _len = data.length; i < _len; i++) {
					var datum = data[i];
					stable_id_index[datum.study_id] = stable_id_index[datum.study_id] || {};
					stable_id_index[datum.study_id][datum.stable_id] = datum;
				}
			};
			var getFromStableIdIndex = function (internal_study_id, stable_ids) {
				var ret = [];
				var sub_index = stable_id_index[internal_study_id];
				if (typeof sub_index !== 'undefined') {
					for (var i = 0, _len = stable_ids.length; i < _len; i++) {
						var datum = sub_index[stable_ids[i]];
						if (typeof datum !== 'undefined') {
							ret.push(datum);
						}
					}
				}
				return ret;
			};
			var missingFromStableIdIndex = function (internal_study_id, stable_ids) {
				var ret = [];
				var sub_index = stable_id_index[internal_study_id];
				if (typeof sub_index !== 'undefined') {
					for (var i = 0, _len = stable_ids.length; i < _len; i++) {
						var datum = sub_index[stable_ids[i]];
						if (typeof datum === 'undefined') {
							ret.push(stable_ids[i]);
						}
					}
				} else {
					ret = stable_ids.slice();
				}
				return ret;
			};
			var multiplexOnArgs = function (args) {
				var args_keys = Object.keys(args);
				var def = new $.Deferred();
				if (stringSetEquals(args_keys, ["internal_study_ids", (patients ? "patient_ids" : "sample_ids")])) {
					getByStableCaseIds(args).then(function (response) {
						def.resolve(response);
					});
				} else if (stringSetEquals(args_keys, [(patients ? "internal_patient_ids" : "internal_sample_ids")])) {
					getByInternalCaseIds(args).then(function (response) {
						def.resolve(response);
					});
				} else if (stringSetEquals(args_keys, ["internal_study_ids"])) {
					getByInternalStudyIds(args).then(function (response) {
						def.resolve(response);
					});
				}
				return def.promise();
			};
			var getByInternalCaseIds = function (args) {
				var def = new $.Deferred();
				var to_fetch = internal_id_index.missingKeys(args[(patients ? 'internal_patient_ids' : 'internal_sample_ids')]);
				if (to_fetch.length > 0) {
					webservice.getSamples({sample_ids: to_fetch}).then(function (response) {
						internal_id_index.addData(response);
						addToStableIdIndex(response);
						def.resolve(internal_id_index.getData(args[(patients ? 'internal_patient_ids' : 'internal_sample_ids')]));
					});
				} else {
					def.resolve(internal_id_index.getData(args[(patients ? 'internal_patient_ids' : 'internal_sample_ids')]));
				}
				return def.promise();
			};
			var getByInternalStudyIds = function (args) {
				var def = new $.Deferred();
				var to_fetch = internal_study_id_index.missingKeys(args['internal_study_ids']);
				if (to_fetch.length > 0) {
					webservice[patients ? 'getPatients' : 'getSamples']({study_ids: to_fetch}).then(function (response) {
						internal_id_index.addData(response);
						internal_study_id_index.addData(response);
						addToStableIdIndex(response);
						def.resolve(internal_study_id_index.getData(args['internal_study_ids']));
					});
				} else {
					def.resolve(internal_study_id_index.getData(args['internal_study_ids']));
				}
				return def.promise();
			};
			var getByStableCaseIds = function (args) {
				var def = new $.Deferred();
				var to_fetch = missingFromStableIdIndex(args['internal_study_ids'][0], args[patients ? 'patient_ids' : 'sample_ids']);
				if (to_fetch.length > 0) {
					webservice.getSamples({study_ids: [args['internal_study_ids'][0]], sample_ids: to_fetch}).then(function (response) {
						internal_id_index.addData(response);
						addToStableIdIndex(response);
						def.resolve(getFromStableIdIndex(args['internal_study_ids'][0], args[patients ? 'patient_ids' : 'sample_ids']));
					});
				} else {
					def.resolve(getFromStableIdIndex(args['internal_study_ids'][0], args[patients ? 'patient_ids' : 'sample_ids']));
				}
				return def.promise();
			};
			return function (args) {
				var def = new $.Deferred();
				if (args.hasOwnProperty("study_ids")) {
					convertStableStudyIdsToInternal(args).then(function (args) {
						multiplexOnArgs(args).then(function (result) {
							def.resolve(result);
						});
					});
				} else {
					multiplexOnArgs(args).then(function (result) {
						def.resolve(result);
					});
				}
				return def.promise();
			};
		})();
	};
	var makeServiceCache = function (arg_name_to_index_key_fn, webserviceFnName, returns_all_with_no_arg, normalize_args, arg_name_to_webservice_arg_name) {
		return (function () {
			var indexes = {};
			var arbitrary_index;
			var all_loaded = false;
			(function initializeIndexes() {
				objEach(arg_name_to_index_key_fn, function (index_key_fn, arg_name) {
					indexes[arg_name] = new Index(index_key_fn);
					arbitrary_index = indexes[arg_name];
				});
			})();
			var addDataToIndexes = function (data, args) {
				objEach(indexes, function (index, __unused) {
					index.addData(data, args);
				});
			};
			return function (args) {
				args = args || {};
				var def = new $.Deferred();
				var args_keys = Object.keys(args);
				if (args_keys.length === 0) {
					if (!returns_all_with_no_arg) {
						def.resolve([]);
					} else {
						if (all_loaded) {
							def.resolve(arbitrary_index.getData());
						} else {
							webservice[webserviceFnName]({}).then(function (response) {
								all_loaded = true;
								addDataToIndexes(response, args);
								def.resolve(arbitrary_index.getData());
							});
						}
					}
				} else {
					var getAndReturnData = function(args) {
						var arg_name;
						var args_keys = Object.keys(args);
						for (var i = 0, _len = args_keys.length; i < _len; i++) {
							if (indexes.hasOwnProperty(args_keys[i])) {
								arg_name = args_keys[i];
								break;
							}
						}
						if (typeof arg_name === 'undefined') {
							def.resolve([]);
							return;
						}
						var to_fetch = indexes[arg_name].missingKeys(args[arg_name]);
						if (to_fetch.length === 0) {
							def.resolve(indexes[arg_name].getData(args[arg_name]));
						} else {
							var webservice_args = {};
							var webservice_arg_name = arg_name_to_webservice_arg_name[arg_name] || arg_name;
							webservice_args[webservice_arg_name] = to_fetch;
							webservice[webserviceFnName](webservice_args).then(function (response) {
								addDataToIndexes(response, args);
								def.resolve(indexes[arg_name].getData(args[arg_name]));
							});
						}
					};
					if (normalize_args) {
						normalizeArgs(args).then(getAndReturnData);
					} else {
						getAndReturnData(args);
					}
				}
				return def.promise();
			};
		})();
	};
	
	var getSamples = makeGetPatientsOrSamples(false);
	var getPatients = makeGetPatientsOrSamples(true);
	var getStudies = makeServiceCache({'study_ids':function(d) { return d['id'];},
					'internal_study_ids':function(d) { return d['internal_id'];}},
					'getStudies',
					true,
					false,
					{'study_ids':'ids', 'internal_study_ids':'ids'});
	var getCancerTypes = makeServiceCache({'ids':function(d) { return d['id']; }},
						'getCancerTypes',
						true);
	var getGenes = makeServiceCache({'hugo_gene_symbols': function(d) { return d['hugoGeneSymbol']; },
					'entrez_gene_ids': function(d) { return d['entrezGeneId']; }},
					'getGenes',
					true,
					false,
					{'hugo_gene_symbols':'ids', 'entrez_gene_ids':'ids'});
	var getGeneSets = makeServiceCache({'gene_set_ids': function(d) { return d['id']; }},
					'getGeneSets',
					true,
					false,
					{'gene_set_ids': 'ids'});
	var getPatientLists = makeServiceCache({'internal_study_ids': function(d) { return d['internal_study_id'];},
						'internal_patient_list_ids': function(d) { return d['internal_id'];},
						'patient_list_ids': function(d) { return d['id'];}},
						'getPatientLists',
						true,
						true,
						{'internal_study_ids':'study_ids', 'internal_patient_list_ids': 'patient_list_ids', 'patient_list_ids': 'patient_list_ids'});
	var getGeneticProfiles = makeServiceCache({'internal_profile_ids':function(d) { return d['internal_id']; },
						'profile_ids': function(d) { return d['id']; },
						'internal_study_ids': function(d) { return d['internal_study_id']; }},
						'getGeneticProfiles',
						true,
						true,
						{'internal_profile_ids':'profile_ids', 'profile_ids':'profile_ids', 'internal_study_ids':'study_ids'});

	var getSampleClinicalData = makeServiceCache({'internal_study_ids':function(d) { return d['study_id'];},
							'internal_sample_ids': function(d) { return d['sample_id'];}},
							'getSampleClinicalData',
							false,
							true,
							{'internal_study_ids': 'study_ids', 'internal_sample_ids':'sample_ids'});
	var getPatientClinicalData = makeServiceCache({'internal_study_ids':function(d) { return d['study_id'];},
							'internal_patient_ids': function(d) { return d['sample_id'];}},
							'getPatientClinicalData',
							false,
							true,
							{'internal_study_ids': 'study_ids', 'internal_patient_ids':'patient_ids'});
							*/
window.cbioportal_client = (function() {
	var raw_service = (function() {
		var getApiCallPromise = function(endpt, args) {
			var arg_strings = [];
			for (var k in args) {
				if (args.hasOwnProperty(k)) {
					args[k] = [].concat(args[k]);
					arg_strings.push(k + '=' + args[k].join(","));
				}
			}
			var arg_string = arg_strings.join("&");
			return $.getJSON(endpt+'?'+arg_string);
		};
		var functionNameToEndpoint = {
			'CancerTypes':'api/cancertypes',
			'SampleClinicalData':'api/clinicaldata/samples',
			'PatientClinicalData':'api/clinicaldata/patients',
			'SampleClinicalAttributes':'api/clinicalattributes/samples',
			'PatientClinicalAttributes':'api/clinicalattributes/patients',
			'Genes':'api/genes',
			'GeneticProfiles':'api/geneticprofiles',
			'PatientLists':'api/patientlists',
			'Patients':'api/patients',
			'GeneticProfileData':'api/geneticprofiledata',
			'Samples':'api/samples',
			'Studies':'api/studies'
		};
		var ret = {};
		for (var fn_name in functionNameToEndpoint) {
			if (functionNameToEndpoint.hasOwnProperty(fn_name)) {
				ret['get'+fn_name] = (function(endpt) {
					return function(args) {
						return getApiCallPromise(endpt, args);
					};
				})(functionNameToEndpoint[fn_name]);
			}
		}
		return ret;
	})();
	function Index(key) {
		var map = {};
		var stringSetDifference = function (A, B) {
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
			keys = [].concat(keys);
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
			return stringSetDifference([].concat(keys), Object.keys(map));
		};
	};
	
	var makeOneIndexService = function(arg_name, indexKeyFn, service_fn_name) {
		return (function() {
			var index = new Index(indexKeyFn);
			var loaded_all = false;
			return function(args) {
				args = args || {};
				var def = new $.Deferred();
				if (args.hasOwnProperty(arg_name)) {
					var missing_keys = index.missingKeys(args[arg_name]);
					if (missing_keys.length > 0) {
						var webservice_args = {};
						webservice_args[arg_name] = missing_keys;
						raw_service[service_fn_name](webservice_args).then(function(data) {
							index.addData(data);
							def.resolve(index.getData(args[arg_name]));
						});
					} else {
						def.resolve(index.getData(args[arg_name]));
					}
				} else {
					if (!loaded_all) {
						raw_service[service_fn_name]({}).then(function(data) {
							index.addData(data);
							loaded_all = true;
							def.resolve(index.getData());
						});
					} else {
						def.resolve(index.getData());
					}
				}
				return def.promise();
			}
		})();
	};
	var makeTwoIndexService = function(arg_name1, indexKeyFn1, index1_always_add, arg_name2, indexKeyFn2, index2_always_add, service_fn_name) {
		return (function() {
			var index1 = new Index(indexKeyFn1);
			var index2 = new Index(indexKeyFn2);
			var loaded_all = false;
			return function(args) {
				args = args || {};
				var def = new $.Deferred();
				if (args.hasOwnProperty(arg_name1)) {
					var missing_keys = index1.missingKeys(args[arg_name1]);
					if (missing_keys.length > 0) {
						var webservice_args = {};
						webservice_args[arg_name1] = missing_keys;
						raw_service[service_fn_name](webservice_args).then(function(data) {
							index1.addData(data);
							if (index2_always_add) {
								index2.addData(data);
							}
							def.resolve(index1.getData(args[arg_name1]));
						});
					} else {
						def.resolve(index1.getData(args[arg_name1]));
					}
				} else if (args.hasOwnProperty(arg_name2)) {
					var missing_keys = index2.missingKeys(args[arg_name2]);
					if (missing_keys.length > 0) {
						var webservice_args = {};
						webservice_args[arg_name2] = missing_keys;
						raw_service[service_fn_name](webservice_args).then(function(data) {
							index2.addData(data);
							if (index1_always_add) {
								index1.addData(data);
							}
							def.resolve(index2.getData(args[arg_name2]));
						});
					} else {
						def.resolve(index2.getData(args[arg_name2]));
					}
				} else {
					if (!loaded_all) {
						raw_service[service_fn_name]({}).then(function(data) {
							index1.addData(data);
							index2.addData(data);
							loaded_all = true;
							def.resolve(index1.getData());
						});
					} else {
						def.resolve(index1.getData());
					}
				}
				return def.promise();
			}
		})();
	};
	var makeStudyHierIndexService = function(arg_name, indexKeyFn, service_fn_name) {
		return (function() {
			var index = {};
			return function(args) {
				var def = new $.Deferred();
				var study_id = args.study_id;
				if (args.hasOwnProperty(arg_name)) {
					index[study_id] = index[study_id] || {sub_index: new Index(indexKeyFn), all_loaded: false};
					var missing_keys = index[study_id].sub_index.missingKeys(args[arg_name]);
					if (missing_keys.length > 0) {
						var webservice_args = {};
						webservice_args[arg_name] = missing_keys;
						raw_service[service_fn_name](webservice_args).then(function(data) {
							index[study_id].sub_index.addData(data);
							def.resolve(index[study_id].sub_index.getData(args[arg_name]));
						});
					} else {
						def.resolve(index[study_id].sub_index.getData(args[arg_name]));
					}
				} else {
					index[study_id] = index[study_id] || {sub_index: new Index(indexKeyFn), all_loaded: false};
					if (index[study_id].all_loaded === false) {
						raw_service[service_fn_name]({'study_id': study_id}).then(function(data) {
							index[study_id].sub_index.addData(data);
							index[study_id].all_loaded = true;
							def.resolve(index[study_id].sub_index.getData());
						});
					} else {
						def.resolve(index[study_id].sub_index.getData());
					}
				}
				return def.promise();
			};
		})();	
	};
	var cached_service = {
		getCancerTypes: makeOneIndexService('cancer_type_ids', function(d) { return d.id;}, 'getCancerTypes'),
		getGenes: makeOneIndexService('hugo_gene_symbols', function(d) { return d.hugo_gene_symbol;}, 'getGenes'),
		getStudies: makeOneIndexService('study_ids', function(d) { return d.id;}, 'getStudies'),
		getGeneticProfiles: makeTwoIndexService('study_id', function(d) { return d.study_id;}, false, 'genetic_profile_ids', function(d) {return d.id; }, true, 'getGeneticProfiles'),
		getPatientLists: makeTwoIndexService('study_id', function(d) { return d.study_id;}, false, 'patient_list_ids', function(d) {return d.id; }, true, 'getPatientLists'),
		getSampleClinicalData: makeStudyHierIndexService('sample_ids', function(d) { return d.sample_id; }, 'getSampleClinicalData'),
		getPatientClinicalData: makeStudyHierIndexService('patient_ids', function(d) { return d.patient_id; }, 'getPatientClinicalData'),
		getPatients: makeStudyHierIndexService('patient_ids', function(d) { return d.id; }, 'getPatients'),
		getSamples: makeStudyHierIndexService('sample_ids', function(d) { return d.id; }, 'getSamples'),
		getGeneticProfileData: (function() {
			var index = {};
			return function(args) {
				console.log(index)
				var def = new $.Deferred();
				var genetic_profile_ids = args.genetic_profile_ids;
				var genes = args.genes;
				var missing_genetic_profile_ids = {};
				var missing_genes = {};
				if (args.hasOwnProperty('sample_ids')) {
					var sample_ids = args.sample_ids;
					for (var i=0; i<genetic_profile_ids.length; i++) {
						var gp_id = genetic_profile_ids[i];
						index[gp_id] = index[gp_id] || {};
						for (var j=0; j<genes.length; j++) {
							var gene = genes[j];
							index[gp_id][gene] = index[gp_id][gene] || {sub_index: new Index(function(d) { return d.sample_id; }), loaded_all: false};
							if (index[gp_id][gene].missingKeys(sample_ids).length > 0) {
								missing_genetic_profile_ids[gp_id] = true;
								missing_genes[gene] = true;
							}
						}
					}
				} else {
					for (var i=0; i<genetic_profile_ids.length; i++) {
						var gp_id = genetic_profile_ids[i];
						index[gp_id] = index[gp_id] || {};
						for (var j=0; j<genes.length; j++) {
							var gene = genes[j];
							index[gp_id][gene] = index[gp_id][gene] || {sub_index: new Index(function(d) { return d.sample_id; }), loaded_all: false};
							if (index[gp_id][gene].loaded_all === false) {
								missing_genetic_profile_ids[gp_id] = true;
								missing_genes[gene] = true;
							}
						}
					}
				}
				missing_genetic_profile_ids = Object.keys(missing_genetic_profile_ids);
				missing_genes = Object.keys(missing_genes);
				if (missing_genetic_profile_ids.length === 0 && missing_genes.length === 0) {
					var ret = [];
					for (var i=0; i<genetic_profile_ids.legnth; i++) {
						var gp_id = genetic_profile_ids[i];
						for (var j=0; j<genes.length; j++) {
							var gene = genes[j];
							ret = ret.concat(index[gp_id][gene].sub_index.getData(args.sample_ids));
						}
					}
					def.resolve(ret);
				} else {
					var webservice_args = {};
					webservice_args.genetic_profile_ids = missing_genetic_profile_ids;
					webservice_args.genes = missing_genes;
					if (args.hasOwnProperty('sample_ids')) {
						webservice_args.sample_ids = args.sample_ids;
					}
					console.log(webservice_args);
					raw_service.getGeneticProfileData(webservice_args).then(function(data) {
						for (var i=0; i<data.length; i++) {
							var datum = data[i];
							var gp_id = datum.genetic_profile_id;
							var gene = datum.genes;
							index[gp_id] = index[gp_id] || {};
							index[gp_id][gene] = index[gp_id][gene] || {sub_index: new Index(function(d) { return d.sample_id; }), loaded_all: false};
							index[gp_id][gene].sub_index.addData([datum]);
						}
						var ret = [];
						for (var i=0; i<genetic_profile_ids.length; i++) {
							var gp_id = genetic_profile_ids[i];
							for (var j=0; j<genes.length; j++) {
								var gene = genes[j];
								console.log(index[gp_id][gene].sub_index.getData());
								ret = ret.concat(index[gp_id][gene].sub_index.getData(args.sample_ids));
							}
						}	
						def.resolve(ret);
					});
				}
				return def.promise();
			};
		})(),
		getSampleClinicalAttributes: function(args) {
			return raw_service.getSampleClinicalAttributes(args);
		},
		getPatientClinicalAttributes: function(args) {
			return raw_service.getPatientClinicalAttributes(args);
		}
	};
	var test = function() {
		// getCancerTypes
		// getGenes
		// getStudies
		// getGeneticProfiles
		// getPatientLists
		// getSampleClinicalData
		// getPatientClinicalData
		// getPatients
		// getSamples
		// getGeneticProfileData
	};
	return cached_service;
})();


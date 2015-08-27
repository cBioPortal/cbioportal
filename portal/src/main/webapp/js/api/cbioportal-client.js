window.cbioportal_client = (function() {
	var endpt_wrapper = (function() {
		var getApiCallPromise = function(endpt, args) {
			var arg_strings = [];
			for (var k in args) {
				if (args.hasOwnProperty(k) && args[k].length > 0) {
					arg_strings.push(k + '=' + args[k].join(","));
				}
			}
			var arg_string = arg_strings.join("&");
			return $.post(endpt, arg_string);
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
				var endpoint = functionNameToEndpoint[fn_name];
				ret['get'+fn_name] = function(args) {
					return getApiCallPromise(endpoint, args);
				};
			}
		}
		return ret;
	})();
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
	
	var getCancerTypes = (function() {
		var cancer_type_id_index = new Index(function(d) { return d.id; });
		var loaded_all = false;
		return function(args) {
			args = args || {};
			var def = new $.Deferred();
			var webservice_args = false;
			if (args.hasOwnProperty('cancer_type_ids')) {
				var missing_keys = cancer_type_id_index.missingKeys(args.cancer_type_ids);
				if (missing_keys.length > 0) {
					webservice_args = webservice_args || {};
					webservice_args.cancer_type_ids = missing_keys;
				}
			} else {
				if (!loaded_all) {
					webservice_args = webservice_args || {};
					loaded_all = true;
				}
			}
			if (webservice_args) {
				endpt_wrapper.getCancerTypes(webservice_args).then(function(data) {
					cancer_type_id_index.addData(data);
					def.resolve(cancer_type_id_index.getData(args.cancer_type_ids));
				});
			} else {
				def.resolve(cancer_type_id_index.getData(args.cancer_type_ids));
			}
			return def.promise();
		};
	})();
	var getSampleClinicalData = (function() {
		var index = {};
		return function(args) {
			args = args || {};
			var def = new $.Deferred();
			if (Object.keys(args).length === 2 && args.hasOwnProperty('study_id') && args.hasOwnProperty('sample_ids')) {
				index[args.study_id] = index[args.study_id] || {sample_index: new Index(function(d) { return d.sample_id;}), all_loaded: false};
				var missing_keys = index[args.study_id].sample_index.missingKeys(args.sample_ids);
				if (missing_keys.length > 0) {
					endpt_wrapper.getSampleClinicalData({study_id: args.study_id, sample_ids: missing_keys}).then(function(data) {
						index[args.study_id].sample_index.addData(data);
						def.resolve(index[args.study_id].getData(args.sample_ids));
					});
				} else {
					def.resolve(index[args.study_id].getData(args.sample_ids));
				}
			} else if (Object.keys(args).length === 1 && args.hasOwnProperty('study_id')) {
				index[args.study_id] = index[args.study_id] || {sample_index: new Index(function(d) { return d.sample_id;}), all_loaded: false};
				if (index[args.study_id].all_loaded === false) {
					endpt_wrapper.getSampleClinicalData({study_id: args.study_id}).then(function(data) {
						index[args.study_id].sample_index.addData(data);
						index[args.study_id].all_loaded = true;
						def.resolve(index[args.study_id].getData());
					});
				} else {
					def.resolve(index[args.study_id].getData());
				}
			}
			return def.promise();
		};
	})();
	var getGeneticProfiles = (function() {
		var study_index = new Index(function(d) { return d.study_id; });
		var id_index = new Index(function(d) { return d.id; });
		var loaded_all = false;
		return function(args) {
			args = args || {};
			var def = new $.Deferred();
			if (Object.keys(args).length === 0) {
				if (loaded_all) {
					def.resolve(id_index.getData());
				} else {
					endpt_wrapper.getGeneticProfiles({}).then(function(data) {
						id_index.addData(data);
						study_index.addData(data);
						loaded_all = true;
						def.resolve(id_index.getData());
					});
				}
			} else if (Object.keys(args).length === 1 && args.hasOwnProperty('study_id')) {
				
			}
			return def.promise();
		};
	})();
})();
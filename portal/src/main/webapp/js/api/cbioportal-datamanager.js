var log = function(data) { console.log(data); };
dataman = (function() {
	// TODO: put 'all' into fraemwork of caches so you dont need additional history object
	// DELETE:
	var missingKeys = function(){ return 0;};
	var mapSlice = function() { return 0;};
	// end DELETE
	// CLASS DEFS
	function Cache() {
		this.data = [];
		this.indexes = {};
	}
	Cache.prototype = {
		constructor: Cache,
		getAll: function() {
			return this.data;
		},
		addIndex: function(name, cacheBy) {
			this.indexes[name] = new Index(cacheBy, this.data);
		},
		addData: function(data, indexNames) {
			this.data = this.data.concat(data);
			indexNames = indexNames || Object.keys(this.indexes);
			for (var i=0; i<indexNames.length; i++) {
				this.indexes[indexNames[i]].add(data);
			}
		}
	}
	function Index(cacheBy, data, mapType) {
		this.map = {};
		this.cacheBy = cacheBy;
		this.mapType = mapType;
	}
	Index.mapType = {ONE_TO_ONE:0, ONE_TO_MANY:1, TWO_TO_ONE:2};
	Index.prototype = {
		constructor: Index,
		add: function(objs) {
			for (var i=0; i<objs.length; i++) {
				if (this.mapType === Index.mapType.ONE_TO_ONE) {
					this.map[this.cacheBy(objs[i])] = objs[i];
				} else if (this.mapType === Index.mapType.ONE_TO_MANY) {
					var key = this.cacheBy(objs[i]);
					this.map[key] = this.map[key] || [];
					this.map[key].push(objs[i]); 
				} else if (this.mapType === Index.mapType.TWO_TO_ONE) {
					var keys = this.cacheBy(objs[i]);
					var key1 = keys[0];
					var key2 = keys[1];
					this.map[key1] = this.map[key1] || {};
					this.map[key1][key2] = objs[i];
				}
			}
		},
		missingKeys: function(keys) {
			var ret = [];
			if (this.mapType === Index.mapType.ONE_TO_ONE || this.mapType === Index.mapType.ONE_TO_MANY) {
				for (var i=0; i<keys.length; i++) {
					if (!(keys[i] in this.map)) {
						ret.push(keys[i]);
					} 
				}
			} else if (this.mapType === Index.mapType.TWO_TO_ONE) {
				for (var i=0; i<keys.length; i++) {
					if (!(keys[i][0] in this.map)) {
						ret.push(keys[i]);
					} else if (!(keys[i][1] in this.map[keys[i][0]])) {
						ret.push(keys[i]);
					}
				}
			}
			return ret;
		},
		get: function(keys) {
			var ret = [];
			if (this.mapType === Index.mapType.ONE_TO_ONE) {
				for (var i=0; i<keys.length; i++) {
					if (keys[i] in this.map) {
						ret.push(this.map[keys[i]]);
					}
				}
			} else if (this.mapType === Index.mapType.ONE_TO_MANY) {
				for (var i=0; i<keys.length; i++) {
					if (keys[i] in this.map) {
						ret = ret.concat(this.map[keys[i]]);
					}
				}
			} else if (this.mapType === Index.mapType.TWO_TO_ONE) {
				for (var i=0; i<keys.length; i++) {
					if ((keys[i][0] in this.map) && (keys[i][1] in this.map[keys[i][0]])) {
						ret.push(this.map[keys[i][0]][keys[i][1]]);
					}
				}
			}
			return ret;
		}
	}

	// CACHE VARIABLE DECL/INIT
	var cache = {meta:{}, data:{}};
	var history = {meta:{}, data:{}};
	var metacalls = ['cancerTypes', 'genes', 'patients', 'samples', 'studies', 'patientLists',
					'profiles', 'clinicalPatients', 'clinicalSamples'];
	var datacalls = ['clinicalPatients', 'clinicalSamples', 'patientLists', 'profiles'];
	for (var i=0; i<metacalls.length; i++) {
		cache.meta[metacalls[i]] = new Cache();
		history.meta[metacalls[i]] = {};
	}
	for (var i=0; i<datacalls.length; i++) {
		cache.data[datacalls[i]] = new Cache();
		history.data[datacalls[i]] = {};
	}
	cache.meta.cancerTypes.addIndex('id',function(x) { return x.id; }, Index.mapType.ONE_TO_ONE);

	cache.meta.genes.addIndex('hugo', function(x) { return x.hugoGeneSymbol; }, Index.mapType.ONE_TO_ONE);
	cache.meta.genes.addIndex('entrez', function(x) { return x.entrezGeneId; }, Index.mapType.ONE_TO_ONE);

	cache.meta.patients.addIndex('study', function(x) { return x.study_id; }, Index.mapType.ONE_TO_MANY);
	cache.meta.patients.addIndex('internal_id', function(x) { return x.internal_id; }, Index.mapType.ONE_TO_ONE);
	cache.meta.patients.addIndex('stable_id', function(x) { return x.study_id+"_"+x.stable_id;}, Index.mapType.ONE_TO_ONE);

	cache.meta.samples.addIndex('study', function(x) { return x.study_id; }, Index.mapType.ONE_TO_MANY);
	cache.meta.samples.addIndex('internal_id', function(x) { return x.internal_id; }, Index.mapType.ONE_TO_ONE);
	cache.meta.samples.addIndex('stable_id', function(x) { return x.study_id+"_"+x.stable_id;}, Index.mapType.ONE_TO_ONE);

	cache.meta.studies.addIndex('internal_id', function(x) { return x.internal_id;}, Index.mapType.ONE_TO_ONE);
	cache.meta.studies.addIndex('stable_id', function(x) { return x.stable_id;}, Index.mapType.ONE_TO_ONE);

	cache.meta.profiles.addIndex('internal_id', function(x) { return x.internal_id;}, Index.mapType.ONE_TO_ONE);
	cache.meta.profiles.addIndex('stable_id', function(x) { return x.id;}, Index.mapType.ONE_TO_ONE);
	cache.meta.profiles.addIndex('study', function(x) { return x.internal_study_id;}, Index.mapType.ONE_TO_MANY);

	cache.data.clinicalPatients.addIndex('internal_id', function(x) { return x.patient_id;}, Index.mapType.ONE_TO_MANY);
	cache.data.clinicalPatients.addIndex('study', function(x) { return x.study_id;}, Index.mapType.ONE_TO_MANY);

	cache.data.clinicalSamples.addIndex('internal_id', function(x) { return x.sample_id;}, Index.mapType.ONE_TO_MANY);
	cache.data.clinicalSamples.addIndex('study', function(x) { return x.study_id;}, Index.mapType.ONE_TO_MANY);

	cache.data.profiles.addIndex('geneprofile_patient', function(x) { return [x.entrez_gene_id+"_"+x.internal_id, x.internal_patient_id]}


	// API METHODS
	// -- meta.cancerTypes --
	var getAllCancerTypes = function(callback, fail) {
		if (history.meta.cancerTypes.all) {
			callback(cache.meta.cancerTypes.getAll());
		} else {
			cbio.meta.cancerTypes({}, function(data) {
				cache.meta.cancerTypes.addData(data);
				history.meta.cancerTypes.all = true;
				callback(cache.meta.cancerTypes.getAll());
			}, fail);
		}
	}
	var getCancerTypesById = function(ids, callback, fail) {
		var index = cache.meta.cancerTypes.indexes['id'];
		var toQuery = index.missingKeys(ids);
		if (toQuery.length === 0) {
			callback(index.get(ids));
		} else {
			cbio.meta.cancerTypes({'ids':toQuery}, function(data) {
				cache.meta.cancerTypes.addData(data);
				callback(index.get(ids));
			}, fail);
		}
	}

	// -- meta.genes --
	var getAllGenes = function(callback, fail) {
		if (history.meta.genes.all) {
			callback(cache.meta.genes.getAll());
		} else {
			cbio.meta.genes({}, function(data) {
				cache.meta.genes.addData(data);
				history.meta.genes.all = true;
				callback(cache.meta.genes.getAll());
			}, fail);
		}
	}
	var getGenesHelper = function(ids, indexName, callback, fail) {
		var index = cache.meta.genes.indexes[indexName];
		var toQuery = index.missingKeys(ids);
		if (toQuery.length === 0) {
			callback(index.get(ids));
		} else {
			cbio.meta.genes({'ids':toQuery}, function(data) {
				cache.meta.genes.addData(data);
				callback(index.get(ids));
			}, fail);
		}
	}
	var getGenesByHugoGeneSymbol = function(ids, callback, fail) {
		getGenesHelper(ids, 'hugo', callback, fail);
	}
	var getGenesByEntrezGeneId = function(ids, callback, fail) {
		getGenesHelper(ids, 'entrez', callback, fail);
	}
	
	// -- meta.patients --
	var getPatientsByStableStudyId = function(study_ids, callback, fail) {
		cbio.meta.studies({'ids': study_ids}, function(data) {
			var internal_ids = [];
			for (var i=0; i<data.length; i++) {
				internal_ids.push(data[i].internal_id);
			}
			getPatientsByInternalStudyId(internal_ids, callback, fail);
		}, fail);
	}
	var getPatientsByInternalStudyId = function(study_ids, callback, fail) {
		var index = cache.meta.patients.indexes['study'];
		var toQuery = index.missingKeys(study_ids);
		if (toQuery.length === 0) {
			callback(index.get(study_ids));
		} else {
			cbio.meta.patients({'study_ids': study_ids}, function(data) {
				cache.meta.patients.addData(data);
				callback(index.get(study_ids));
			}, fail);
		}
	}
	var getPatientsByInternalId = function(internal_ids, callback, fail) {
		var index = cache.meta.patients.indexes['internal_id'];
		var toQuery = index.missingKeys(internal_ids);
		if (toQuery.length === 0) {
			callback(index.get(internal_ids));
		} else {
			cbio.meta.patients({'patient_ids': internal_ids}, function(data) {
				cache.meta.patients.addData(data, ['internal_id', 'stable_id']);
				callback(index.get(internal_ids));
			}, fail);
		}
	}
	var getPatientsByStableIdStableStudyId = function(study_id, stable_ids, callback, fail) {
		cbio.meta.studies({'ids':[study_id]}, function(data) {
			getPatientsByStableIdInternalStudyId(data[0].internal_id, stable_ids, callback, fail);
		}, fail);
	}
	var getPatientsByStableIdInternalStudyId = function(study_id, stable_ids, callback, fail) {
		var index = cache.meta.patients.indexes['stable_id'];
		var goodIds = stable_ids.map(function(x) { return study_id+"_"+x;});
		var toQuery = index.missingKeys(goodIds)
		if (toQuery.length === 0) {
			callback(index.get(goodIds));
		} else {
			cbio.meta.patients({'study_ids':[study_id], 'patient_ids':stable_ids}, function(data) {
				cache.meta.patients.addData(data, ['internal_id', 'stable_id']);
				callback(index.get(goodIds));
			}, fail);
		}
	}
	// -- meta.samples --
	var getSamplesByStableStudyId = function(study_ids, callback, fail) {
		cbio.meta.studies({'ids': study_ids}, function(data) {
			var internal_ids = [];
			for (var i=0; i<data.length; i++) {
				internal_ids.push(data[i].internal_id);
			}
			getSamplesByInternalStudyId(internal_ids, callback, fail);
		}, fail);
	}
	var getSamplesByInternalStudyId = function(study_ids, callback, fail) {
		var index = cache.meta.samples.indexes['study'];
		var toQuery = index.missingKeys(study_ids);
		if (toQuery.length === 0) {
			callback(index.get(study_ids));
		} else {
			cbio.meta.samples({'study_ids': study_ids}, function(data) {
				cache.meta.samples.addData(data);
				callback(index.get(study_ids));
			}, fail);
		}
	}
	var getSamplesByInternalId = function(internal_ids, callback, fail) {
		var index = cache.meta.samples.indexes['internal_id'];
		var toQuery = index.missingKeys(internal_ids);
		if (toQuery.length === 0) {
			callback(index.get(internal_ids));
		} else {
			cbio.meta.samples({'sample_ids': internal_ids}, function(data) {
				cache.meta.samples.addData(data, ['internal_id', 'stable_id']);
				callback(index.get(internal_ids));
			}, fail);
		}
	}
	var getSamplesByStableIdStableStudyId = function(study_id, stable_ids, callback, fail) {
		cbio.meta.samples({'ids':[study_id]}, function(data) {
			getSamplesByStableIdInternalStudyId(data[0].internal_id, stable_ids, callback, fail);
		}, fail);
	}
	var getSamplesByStableIdInternalStudyId = function(study_id, stable_ids, callback, fail) {
		var index = cache.meta.samples.indexes['stable_id'];
		var goodIds = stable_ids.map(function(x) { return study_id+"_"+x;});
		var toQuery = index.missingKeys(goodIds)
		if (toQuery.length === 0) {
			callback(index.get(goodIds));
		} else {
			cbio.meta.samples({'study_ids':[study_id], 'sample_ids':stable_ids}, function(data) {
				cache.meta.samples.addData(data, ['internal_id', 'stable_id']);
				callback(index.get(goodIds));
			}, fail);
		}
	}

	// -- meta.studies --
	var getAllStudies = function(callback, fail) {
		if(history.meta.studies.all) {
			callback(cache.meta.studies.getAll());
		} else {
			cbio.meta.studies({}, function(data) {
				cache.meta.studies.addData(data);
				history.meta.studies.all = true;
				callback(cache.meta.studies.getAll());
			}, fail);
		}
	}

	var getStudiesHelper = function(ids, indexName, callback, fail) {
		var index = cache.meta.studies.indexes[indexName];
		var toQuery = index.missingKeys(ids);
		if (toQuery.length === 0) {
			callback(index.get(ids));
		} else {
			cbio.meta.studies({'ids':toQuery}, function(data) {
				cache.meta.studies.addData(data);
				callback(index.get(ids));
			}, fail);
		}
	}
	var getStudiesByStableId = function(ids, callback, fail) {
		getStudiesHelper(ids, 'stable_id', callback, fail);
	}
	var getStudiesByInternalId = function(ids, callback, fail) {
		getStudiesHelper(ids, 'internal_id', callback, fail);
	}


	// -- meta.patientlists and data.patientLists --
	var getAllPatientLists = function(omit_lists, callback, fail) {
		// TODO?: caching
		var namespace = (omit_lists? cbio.meta : cbio.data);
		namespace.patientLists({},callback, fail);
	}
	var getPatientListsByStableId = function(omit_lists, patient_list_ids, callback, fail) {
		// TODO?: caching
		var namespace = (omit_lists? cbio.meta : cbio.data);
		namespace.patientLists({'patient_list_ids': patient_list_ids},callback, fail);
	}
	var getPatientListsByInternalId = function(omit_lists, patient_list_ids, callback, fail) {
		// TODO?: caching
		var namespace = (omit_lists? cbio.meta : cbio.data);
		namespace.patientLists({'patient_list_ids': patient_list_ids},callback, fail);
	}
	var getPatientListsByStudy = function(omit_lists, study_ids, callback, fail) {
		// TODO?: caching
		var namespace = (omit_lists? cbio.meta : cbio.data);
		namespace.patientLists({'study_ids': study_ids},callback, fail);
	}

	// -- meta.profiles --
	var getAllProfiles = function(callback, fail) {
		if(history.meta.profiles.all) {
			callback(cache.meta.profiles.getAll());
		} else {
			cbio.meta.profiles({}, function(data){
				cache.meta.profiles.addData(data);
				history.meta.profiles.all = true;
				callback(cache.meta.studies.getAll());
			}, fail);
		}
	}
	var getProfilesHelper = function(argname, ids, indexName, updateIndexes, callback, fail) {
		var index = cache.meta.profiles.indexes[indexName];
		var toQuery = index.missingKeys(ids);
		if (toQuery.length === 0) {
			callback(index.get(ids));
		} else {
			cbio.meta.profiles({argname: ids}, function(data) {
				cache.meta.profiles.addData(data, updateIndexes);
				callback(index.get(ids));
			}, fail);
		}
	}
	var getProfilesByStableId = function(profile_ids, callback, fail) {
		getProfilesHelper('profile_ids', profile_ids, 'stable_id', ['stable_id', 'internal_id'], callback, fail);
	}
	var getProfilesByInternalId = function(profile_ids, callback, fail) {
		getProfilesHelper('profile_ids', profile_ids, 'internal_id', ['stable_id', 'internal_id'], callback, fail);
	}
	var getProfilesByInternalStudyId = function(study_ids, callback, fail) {
		getProfilesHelper('study_ids', study_ids, 'study', undefined, callback, fail);
	}

	// -- meta.clinicalPatients --
	var getAllClinicalPatientFields = function(callback, fail) {
		//TODO?: caching
		cbio.meta.clinicalPatientsMeta({}, callback, fail);
	}
	var getClinicalPatientFieldsByStudy = function(study_ids, callback, fail) {
		//TODO? caching
		cbio.meta.clinicalPatientsMeta({'study_ids':study_ids}, callback, fail);
	}
	var getClinicalPatientFieldsByInternalId = function(patient_ids, callback, fail) {
		//TODO? caching
		cbio.meta.clinicalPatientsMeta({'patient_ids':patient_ids}, callback, fail);
	}
	var getClinicalPatientFieldsByStableId = function(study_id, patient_ids, callback, fail) {
		//TODO? caching
		cbio.meta.clinicalPatientsMeta({'study_ids':[study_id], 'patient_ids':patient_ids}, callback, fail);
	}

	// -- meta.clinicalSamples --
	var getAllClinicalSampleFields = function(callback, fail) {
		//TODO?: caching
		cbio.meta.clinicalSamplesMeta({}, callback, fail);
	}
	var getClinicalSampleFieldsByStudy = function(study_ids, callback, fail) {
		//TODO? caching
		cbio.meta.clinicalSamplesMeta({'study_ids':study_ids}, callback, fail);
	}
	var getClinicalSampleFieldsByInternalId = function(sample_ids, callback, fail) {
		//TODO? caching
		cbio.meta.clinicalSamplesMeta({'patient_ids':sample_ids}, callback, fail);
	}
	var getClinicalSampleFieldsByStableId = function(study_id, sample_ids, callback, fail) {
		//TODO? caching
		cbio.meta.clinicalSamplesMeta({'study_ids':[study_id], 'sample_ids':sample_ids}, callback, fail);
	}

	// -- data.clinicalPatients --
	var getClinicalPatientDataByInternalStudyId = function(study_ids, callback, fail) {
		var index = cache.data.clinicalPatients.indexes['study'];
		var toQuery = index.missingKeys(study_ids);
		if (toQuery.length === 0) {
			callback(index.get(study_ids));
		} else {
			cbio.data.clinicalPatients({'study_ids': toQuery}, function(data) {
				cache.data.clinicalPatients.addData(data);
				callback(index.get(study_ids));
			}, fail)
		}
	}
	var getClinicalPatientDataByStableStudyId = function(study_ids, callback, fail) {
		getStudiesByStableId(study_ids, function(data) {
			var internal_ids = [];
			for (var i=0; i<data.length; i++) {
				internal_ids.push(data[i].internal_id);
			}
			getClinicalPatientDataByInternalStudyId(internal_ids, callback, fail);
		}, fail);
	}
	var getClinicalPatientDataByInternalId = function(patient_ids, callback, fail) {
		var index = cache.data.clinicalPatients.indexes['internal_id'];
		var toQuery = index.missingKeys(patient_ids);
		if (toQuery.length === 0) {
			callback(index.get(study_ids));
		} else {
			cbio.data.clinicalPatients({'patient_ids': toQuery}, function(data) {
				cache.data.clinicalPatients.addData(data, ['internal_id']);
				callback(index.get(patient_ids));
			}, fail);
		}
		//TODO: caching
		cbio.data.clinicalPatients({'patient_ids': patient_ids}, callback, fail);
	}
	var getClinicalPatientDataByStableId = function(study_id, patient_ids, callback, fail) {
		var fetchFn = (typeof study_id === "number" ? getPatientsByStableIdInternalStudyId : getPatientsByStableIdStableStudyId);
		fetchFn(study_id, patient_ids, function(data) {
			var internal_ids = [];
			for (var i=0; i<data.length; i++) {
				internal_ids.push(data[i].internal_id);
			}
			getClinicalPatientDataByInternalId(internal_ids, callback, fail);
		}, fail);
	}

	// -- data.clinicalSamples --
	var getClinicalSampleDataByInternalStudyId = function(study_ids, callback, fail) {
		var index = cache.data.clinicalSamples.indexes['study'];
		var toQuery = index.missingKeys(study_ids);
		if (toQuery.length === 0) {
			callback(index.get(study_ids));
		} else {
			cbio.data.clinicalSamples({'study_ids': toQuery}, function(data) {
				cache.data.clinicalSamples.addData(data);
				callback(index.get(study_ids));
			}, fail)
		}
	}
	var getClinicalSampleDataByStableStudyId = function(study_ids, callback, fail) {
		getStudiesByStableId(study_ids, function(data) {
			var internal_ids = [];
			for (var i=0; i<data.length; i++) {
				internal_ids.push(data[i].internal_id);
			}
			getClinicalSampleDataByInternalStudyId(internal_ids, callback, fail);
		}, fail);
	}
	var getClinicalSampleDataByInternalId = function(sample_ids, callback, fail) {
		var index = cache.data.clinicalSamples.indexes['internal_id'];
		var toQuery = index.missingKeys(sample_ids);
		if (toQuery.length === 0) {
			callback(index.get(study_ids));
		} else {
			cbio.data.clinicalSamples({'sample_ids': toQuery}, function(data) {
				cache.data.clinicalSamples.addData(data, ['internal_id']);
				callback(index.get(sample_ids));
			}, fail);
		}
		//TODO: caching
		cbio.data.clinicalSamples({'sample_ids': sample_ids}, callback, fail);
	}
	var getClinicalSampleDataByStableId = function(study_id, sample_ids, callback, fail) {
		var fetchFn = (typeof study_id === "number" ? getSamplesByStableIdInternalStudyId : getSamplesByStableIdStableStudyId);
		fetchFn(study_id, sample_ids, function(data) {
			var internal_ids = [];
			for (var i=0; i<data.length; i++) {
				internal_ids.push(data[i].internal_id);
			}
			getClinicalSampleDataByInternalId(internal_ids, callback, fail);
		}, fail);
	}

	// -- data.profiles --
	var cartProd = function(A,B) {
		var ret = [];
		for (var i=0; i<A.length; i++) {
			for (var j=0; j<B.length; j++) {
				ret.push([A[i],B[j]]);
			}
		}
		return ret;
	}
	var getAllProfileData = function(genes, profile_ids, callback, fail) {
		var allKeys = cartProd(genes, profile_ids);
		var toQuery = [];
		for (var i=0; i<allKeys.length; i++) {
			var key = allKeys[i][0]+"_"+allKeys[i][1];
			if (!(key in history.data.profiles)) {
				toQuery.push(allKeys[i]);
			}
		}
		if (toQuery.length === 0) {
			
		}
		var index = cache.data.profiles.indexes['geneprofile_patient'];

		//TODO: caching
		cbio.data.profilesData({'genes': genes, 'profile_ids':profile_ids}, callback, fail);
	}
	var getProfileDataByPatientId = function(genes, profile_ids, patient_ids, callback, fail) {
		//TODO: caching
		cbio.data.profilesData({'genes': genes, 'profile_ids':profile_ids, 'patient_ids':patient_ids}, callback, fail);
	}
	var getProfileDataByPatientListId = function(genes, profile_ids, patient_list_ids, callback, fail) {
		//TODO: caching
		cbio.data.profilesData({'genes': genes, 'profile_ids':profile_ids, 'patient_list_ids':patient_list_ids}, callback, fail);
	}

	return {
		cache: cache,
		getAllCancerTypes: getAllCancerTypes,
		getCancerTypesById: getCancerTypesById,

		getAllGenes: getAllGenes,
		getGenesByHugoGeneSymbol: getGenesByHugoGeneSymbol,
		getGenesByEntrezGeneId: getGenesByEntrezGeneId,

		getPatientsByStableStudyId: getPatientsByStableStudyId,
		getPatientsByInternalStudyId: getPatientsByInternalStudyId,
		getPatientsByInternalId: getPatientsByInternalId,
		getPatientsByStableIdStableStudyId: getPatientsByStableIdStableStudyId,
		getPatientsByStableIdInternalStudyId: getPatientsByStableIdInternalStudyId,

		getSamplesByStableStudyId: getSamplesByStableStudyId,
		getSamplesByInternalStudyId: getSamplesByInternalStudyId,
		getSamplesByInternalId: getSamplesByInternalId,
		getSamplesByStableIdStableStudyId: getSamplesByStableIdStableStudyId,
		getSamplesByStableIdInternalStudyId: getSamplesByStableIdInternalStudyId,

		getAllStudies: getAllStudies,
		getStudiesByStableId: getStudiesByStableId,
		getStudiesByInternalId: getStudiesByInternalId,

		getAllPatientLists: getAllPatientLists,
		getPatientListsByStableId: getPatientListsByStableId,
		getPatientListsByInternalId: getPatientListsByInternalId,
		getPatientListsByStudy: getPatientListsByStudy,

		getAllProfiles: getAllProfiles,
		getProfilesByStableId: getProfilesByStableId,
		getProfilesByInternalId: getProfilesByInternalId,
		getProfilesByInternalStudyId: getProfilesByInternalStudyId,

		getAllClinicalPatientFields: getAllClinicalPatientFields,
		getClinicalPatientFieldsByStudy: getClinicalPatientFieldsByStudy,
		getClinicalPatientFieldsByInternalId: getClinicalPatientFieldsByInternalId,
		getClinicalPatientFieldsByStableId: getClinicalPatientFieldsByStableId,

		getAllClinicalSampleFields: getAllClinicalSampleFields,
		getClinicalSampleFieldsByStudy: getClinicalSampleFieldsByStudy,
		getClinicalSampleFieldsByInternalId: getClinicalSampleFieldsByInternalId,
		getClinicalSampleFieldsByStableId: getClinicalSampleFieldsByStableId,

		getAllProfileData: getAllProfileData,
		getProfileDataByPatientId: getProfileDataByPatientId,
		getProfileDataByPatientListId: getProfileDataByPatientListId,

		getClinicalPatientDataByInternalStudyId: getClinicalPatientDataByInternalStudyId,
		getClinicalPatientDataByStableStudyId: getClinicalPatientDataByStableStudyId,
		getClinicalPatientDataByInternalId: getClinicalPatientDataByInternalId,
		getClinicalPatientDataByStableId: getClinicalPatientDataByStableId,

		getClinicalSampleDataByInternalStudyId: getClinicalSampleDataByInternalStudyId,
		getClinicalSampleDataByStableStudyId: getClinicalSampleDataByStableStudyId,
		getClinicalSampleDataByInternalId: getClinicalSampleDataByInternalId,
		getClinicalSampleDataByStableId: getClinicalSampleDataByStableId,

		getAllProfileData: getAllProfileData,
		getProfileDataByPatientId: getProfileDataByPatientId,
		getProfileDataByPatientListId: getProfileDataByPatientListId,
	};

})();
dataman = (function() {

	var cache = {meta:{}, data:{}};
	var history = {meta:{}, data:{}};
	var metacalls = ['cancerTypes', 'genes', 'patients', 'samples', 'studies', 'patientLists',
					'profiles', 'clinicalPatients', 'clinicalSamples'];
	var datacalls = ['clinicalPatients', 'clinicalSamples', 'patientLists', 'profiles'];
	for (var i=0; i<metacalls.length; i++) {
		cache.meta[metacalls[i]] = {};
		history.meta[metacalls[i]] = {};
	}
	for (var i=0; i<datacalls.length; i++) {
		cache.data[datacalls[i]] = {};
		history.data[datacalls[i]] = {};
	}
	cache.meta.studies.stable = {};
	cache.meta.studies.internal = {};

	// HELPERS
	var mapSlice = function(map, keys) {
		// Returns a list containing all the objects
		//	in the given map corresponding to the given keys
		// If no keys passed in, returns whole map as list
		var ret = [];
		keys = keys || Object.keys(map);
		for (var i=0; i<keys.length; i++) {
			ret.push(map[keys[i]]);
		}
		return ret;
	}
	var missingKeys = function(map, keys) {
		// Returns the sublist of keys that's not in map
		var ret = [];
		for (var i=0; i<keys.length; i++) {
			if (!(keys[i] in map)) {
				ret.push(keys[i]);
			}
		}
		return ret;
	}
	// PUBLIC
	// -- meta.cancerTypes -- 
	var getAllCancerTypes = function(callback, fail) {
		if (history.meta.cancerTypes.all) {
			callback(mapSlice(cache.meta.cancerTypes));
		} else {
			cbio.meta.cancerTypes({}, function(data) {
				for (var i=0; i<data.length; i++) {
					cache.meta.cancerTypes[data[i].id] = data[i];	
				}
				history.meta.cancerTypes.all = true;
				callback(mapSlice(cache.meta.cancerTypes));
			}, fail);
		}
	}
	var getCancerTypesById = function(ids, callback, fail) {
		var toQuery = missingKeys(cache.meta.cancerTypes, ids);
		if (toQuery.length == 0) {
			callback(mapSlice(cache.meta.cancerTypes, ids));
		} else {
			cbio.meta.cancerTypes({'ids':toQuery}, function(data) {
				for(var i=0; i<data.length; i++) {
					cache.meta.cancerTypes[data[i].id] = data[i]
				}
				callback(mapSlice(cache.meta.cancerTypes, ids));
			}, fail);
		}
	}


	// -- meta.genes --
	var getAllGenes = function(callback, fail) {
		// TODO?: caching
		cbio.meta.genes({}, callback, fail);
	}
	var getGenesByHugoGeneSymbol = function(ids, callback, fail) {
		// TODO?: caching
		cbio.meta.genes({'ids':ids}, callback, fail);
	}
	var getGenesByEntrezGeneId = function(ids, callback, fail) {
		// TODO?: caching
		cbio.meta.genes({'ids':ids}, callback, fail);
	}

	// -- meta.patients --
	var getPatientsByStudy = function(study_ids, callback, fail) {
		//TODO?: caching
		cbio.meta.patients({'study_ids': study_ids}, callback, fail);
	}
	var getPatientsByInternalId = function(internal_ids, callback, fail) {
		//TODO?: caching
		cbio.meta.patients({'patient_ids':internal_ids}, callback, fail);
	}
	var getPatientsByStableId = function(study_id, stable_ids, callback, fail) {
		//TODO?: caching
		cbio.meta.patients({'study_ids':[study_id], 'patient_ids':stable_ids}, callback, fail);
	}

	// -- meta.samples --
	var getSamplesByStudy = function(study_ids, callback, fail) {
		//TODO?: caching
		cbio.meta.samples({'study_ids': study_ids}, callback, fail);
	}
	var getSamplesByInternalId = function(internal_ids, callback, fail) {
		//TODO?: caching
		cbio.meta.samples({'sample_ids':internal_ids}, callback, fail);
	}
	var getSamplesByStableId = function(study_id, stable_ids, callback, fail) {
		//TODO?: caching
		cbio.meta.samples({'study_ids':[study_id], 'sample_ids':stable_ids}, callback, fail);
	}

	// -- meta.studies --
	var getAllStudies = function(callback, fail) {
		if (history.meta.studies.all) {
			callback(mapSlice(cache.meta.studies.internal));
		} else {
			cbio.meta.studies({}, function(data) {
				for (var i=0; i<data.length; i++) {
					cache.meta.studies.stable[data[i].id] = data[i];
					cache.meta.studies.internal[data[i].internal_id] = data[i];
				}
				history.meta.studies.all = true;
				callback(mapSlice(cache.meta.studies.internal));
			}, fail);
		}
	}
	var getStudiesByStableId = function(ids, callback, fail) {
		var toQuery = missingKeys(cache.meta.studies.stable, ids);
		if (toQuery.length == 0) {
			callback(mapSlice(cache.meta.studies.stable, ids));
		} else {
			cbio.meta.studies({'ids':ids}, function(data) {
				for (var i=0; i<data.length; i++) {
					cache.meta.studies.stable[data[i].id] = data[i];
					cache.meta.studies.internal[data[i].internal_id] = data[i];
				}
				callback(mapSlice(cache.meta.studies.stable, ids));
			}, fail);
		}
	}
	var getStudiesByInternalId = function(ids, callback, fail) {
		var toQuery = missingKeys(cache.meta.studies.internal, ids);
		if (toQuery.length == 0) {
			callback(mapSlice(cache.meta.studies.internal, ids));
		} else {
			cbio.meta.studies({'ids':ids}, function(data) {
				for (var i=0; i<data.length; i++) {
					cache.meta.studies.stable[data[i].id] = data[i];
					cache.meta.studies.internal[data[i].internal_id] = data[i];
				}
				callback(mapSlice(cache.meta.studies.internal, ids));
			}, fail);
		}
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
		//TODO?: caching
		cbio.meta.profiles({}, callback, fail);
	}
	var getProfilesByStableId = function(profile_ids, callback, fail) {
		//TODO?: caching
		cbio.meta.profiles({'profile_ids': profile_ids}, callback, fail);
	}
	var getProfilesByInternalId = function(profile_ids, callback, fail) {
		//TODO?: caching
		cbio.meta.profiles({'profile_ids': profile_ids}, callback, fail);
	}
	var getProfilesByStudyId = function(study_ids, callback, fail) {
		//TODO?: caching
		cbio.meta.profiles({'study_ids': study_ids}, callback, fail);
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
		//TODO: caching
		cbio.data.clinicalPatients({'study_ids': study_ids}, callback, fail);
	}
	var getClinicalPatientDataByStableStudyId = function(study_ids, callback, fail) {
		//TODO: caching
		cbio.data.clinicalPatients({'study_ids': study_ids}, callback, fail);
	}
	var getClinicalPatientDataByInternalId = function(patient_ids, callback, fail) {
		//TODO: caching
		cbio.data.clinicalPatients({'patient_ids': patient_ids}, callback, fail);
	}
	var getClinicalPatientDataByStableId = function(study_id, patient_ids, callback, fail) {
		//TODO: caching
		cbio.data.clinicalPatients({'study_ids':[study_id], 'patient_ids':patient_ids}, callback, fail);
	}

	// -- data.clinicalSamples --
	var getClinicalSampleDataByInternalStudyId = function(study_ids, callback, fail) {
		//TODO: caching
		cbio.data.clinicalSamples({'study_ids': study_ids}, callback, fail);
	}
	var getClinicalSampleDataByStableStudyId = function(study_ids, callback, fail) {
		//TODO: caching
		cbio.data.clinicalSamples({'study_ids': study_ids}, callback, fail);
	}
	var getClinicalSampleDataByInternalId = function(sample_ids, callback, fail) {
		//TODO: caching
		cbio.data.clinicalSamples({'patient_ids': sample_ids}, callback, fail);
	}
	var getClinicalSampleDataByStableId = function(study_id, sample_ids, callback, fail) {
		//TODO: caching
		cbio.data.clinicalSamples({'study_ids':[study_id], 'sample_ids':patient_ids}, callback, fail);
	}

	// -- data.profiles --
	var getAllProfileData = function(genes, profile_ids, callback, fail) {
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
		getAllCancerTypes: getAllCancerTypes,
		getCancerTypesById: getCancerTypesById,

		getAllGenes: getAllGenes,
		getGenesByHugoGeneSymbol: getGenesByHugoGeneSymbol,
		getGenesByEntrezGeneId: getGenesByEntrezGeneId,

		getPatientsByStudy: getPatientsByStudy,
		getPatientsByInternalId: getPatientsByInternalId,
		getPatientsByStableId: getPatientsByStableId,

		getSamplesByStudy: getSamplesByStudy,
		getSamplesByInternalId: getSamplesByInternalId,
		getSamplesByStableId: getSamplesByStableId,

		getAllStudies: getAllStudies,
		getStudiesByStableId: getStudiesByStableId,
		getStudiesByInternalId: getStudiesByInternalId,

		getAllPatientLists: getAllPatientLists,
		getPatientListsById: getPatientListsById,
		getPatientListsByStudy: getPatientListsBystudy,

		getAllProfiles: getAllProfiles,
		getProfilesByStableId: getProfilesByStableId,
		getProfilesByInternalId: getProfilesByInternalId,
		getProfilesByStudyId: getProfilesByStudyId,

		getAllClinicalPatientFields: getAllClinicalPatientFields,
		getClinicalPatientFieldsByStudy: getClinicalPatientFieldsByStudy,
		getClinicalPatientFieldsByInternalId: getClinicalPatientFieldsByInternalId,
		getClinicalPatientFieldsByStableId: getClinicalPatientFieldsByStableId,

		getAllClinicalSampleFields: getAllClinicalSampleFields,
		getClinicalSampleFieldsByStudy: getClinicalSampleFieldsByStudy,
		getClinicalSampleFieldsByInternalId: getClinicalSampleFieldsByInternalId,
		getClinicalSampleFieldsByStableId: getClinicalSampleFieldsByStableId,

		getAllProfileData: getAllProfileData
		getProfileDataByPatientId: getProfileDataByPatientId,
		getProfileDataByPatientListId: getProfileDataByPatientListId
	};

})();
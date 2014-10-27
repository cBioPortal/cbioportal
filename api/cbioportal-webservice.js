// Depends on jquery

cbio = (function() {
	// TODO: handling argument errors?

	// HELPERS
	var makeArgs = function(args) {
		var url = '?';
		for (var k in args) {
			if (url.length > 1) {
				url += '&';
			}
			url += k + '=' + args[k].join(',');
		}
		return url;
	}
	var getRequest = function(endpt, argstr, callback, fail) {
		var url = endpt + argstr;
		var req = $.getJSON(url, callback);
		if (fail) {
			req.fail(fail);
		}
	}
	var apiCall = function(endpt, args, callback, fail) {
		getRequest(endpt, makeArgs(args), callback, fail);
	}
	// META
	var cancerTypesMeta = function(ids, callback, fail) {
		apiCall('/api/meta/cancertypes', {'ids':ids}, callback, fail);
	}

	var genesMeta = function(ids, callback, fail) {
		apiCall('/api/meta/genes', {'ids':ids}, callback, fail);
	}

	var patientsMeta = function(args, callback, fail) {
		apiCall('/api/meta/patients', args, callback, fail);
	}

	var samplesMeta = function(args, callback, fail) {
		apiCall('/api/meta/samples', args, callback, fail);
	}

	var studiesMeta = function(ids, callback, fail) {
		apiCall('/api/meta/studies', {'ids':ids}, callback, fail);
	}

	var patientListsMeta = function(args, callback, fail) {
		apiCall('/api/meta/patientlists', args, callback, fail);
	}

	var profilesMeta = function(args, callback, fail) {
		apiCall('/api/meta/profiles', args, callback, fail);
	}

	var clinicalPatientsMeta = function(args, callback, fail) {
		apiCall('/api/meta/clinical/patients', args, callback, fail);
	}

	var clinicalSamplesMeta = function(args, callback, fail) {
		apiCall('/api/meta/clinical/samples', args, callback, fail);
	}
	// DATA
	var clinicalPatientsData = function(args, callback, fail) {
		apiCall('/api/data/clinical/patients', args, callback, fail);
	}
	var clinicalSamplesData = function(args, callback, fail) {
		apiCall('/api/data/clinical/samples', args, callback, fail);
	}
	var profilesData = function(args, callback, fail) {
		apiCall('/api/data/profiles', args, callback, fail);
	}


	return {
		meta: {
			cancerTypes: cancerTypesMeta,
			genes: genesMeta,
			patients: patientsMeta,
			samples: samplesMeta,
			studies: studiesMeta,
			patientLists: patientListsMeta,
			profiles: profilesMeta,
			clinicalPatients: clinicalPatientsMeta,
			clinicalSamples: clinicalSamplesMeta,
		},
		data: {
			clinicalPatients: clinicalPatientsData,
			clinicalSamples: clinicalSamplesData,
			profiles: profilesData,
		}
	}
})();
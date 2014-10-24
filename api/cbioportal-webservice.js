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

	var singleArgCall = function(argid, argval, endpt, callback, fail) {
		// [required] callback: a callback function that takes one argument: json data
		// [required] endpt: api endpoint (string)
		// [optional] ids: list
		// [optional] fail: a callback function with no arguments that handles fail case
		var url = endpt;
		if (ids) {
			url += '?'+argid+'='+argval.join(',');
		}
		var req = $.getJSON(url, callback);
		if (fail) {
			req.fail(fail);
		}
	}
	// META
	var cancerTypesMeta = function(ids, callback, fail) {
		var endpt = '/api/meta/cancertypes';
		var argstr = makeArgs({'ids':ids});
		getRequest(endpt, argstr, callback, fail);
	}

	var genes = function(ids, callback, fail) {
		var endpt = '/api/meta/genes';
		var argstr = makeArgs({'ids':ids});
		getRequest(endpt, argstr, callback, fail);
	}

	var studiesMeta = function(ids, callback, fail) {
		singleArgCall('ids',ids, '/api/meta/studies', callback, fail);
	}

	var caseListsMeta = function(args, callback, fail) {
		var url = '/api/meta/caselists';
		var case_list_ids = args.case_list_ids;
		var study_ids = args.study_ids;
		if (case_list_ids) {
			singleArgCall('case_list_ids', case_list_ids, url, callback, fail);
		} else if (study_ids) {
			singleArgCall('case_list_ids', case_list_ids, url, callback, fail);
		}
	}

	var profilesMeta = function(args, callback, fail) {
		var url = '/api/meta/profiles';
		var profile_ids = args.profile_ids;
		var study_ids = args.study_ids;
		if (profile_ids) {
			singleArgCall('profile_ids', profile_ids, url, callback, fail);
		} else if (study_ids) {
			singleArgCall('study_ids', study_ids, url, callback, fail);
		}
	}

	var clinicalMeta = function(args, callback, fail) {
		var url = '/api/meta/clinical';
		var study_ids = args.study_ids;
		var case_list_ids = args.case_list_ids;
		var case_ids = args.case_ids;
		var numArgs = 0;
		if (study_ids || case_list_ids || case_ids) {
			url += '?'
		}
		if (study_ids) {
			url += 'study_ids='+study_ids.join(',');
			numArgs += 1;
		}
		if (case_list_ids) {
			if (numArgs > 0) {
				url += '&';
			}
			url += 'case_list_ids='+case_list_ids.join(',');
			numArgs += 1;
		}
		if (case_ids) {
			if (numArgs > 0) {
				url += '&';
			}
			url += 'case_ids='+case_ids.join(',');
		}
		var req = $.getJSON(url,callback);
		if (fail) {
			req.fail(fail);
		}
	}
	// DATA


	return {
		meta: {
			cancerTypes: cancerTypesMeta,
			genes: genes,
			studies: studiesMeta
		},
		data: {
		}
	}
})();
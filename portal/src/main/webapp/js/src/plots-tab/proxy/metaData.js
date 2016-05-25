var metaData = (function() {
    
	var readyCallBackFunction;
	
    var datum_genetic_profile_meta = {
            type: "",
            id: "",
            name: "",
            description: ""
        },
        datum_clinical_attr_meta = {
            id: "",
            name: "",
            type: "",
            description: ""
        },
        geneticProfiles = {},
        clinicalAttrs = [],
        retrieve_status = -1; //data not yet retrieved (-1), retrieved (1)

    function fetchProfileMetaData() {
        $.when(window.cbioportal_client.getGeneticProfiles({study_id: window.PlotsTab.cancerStudyId}))
        .then(function(gp_response) {
            fetchClinicalAttrMetaData(gp_response)
        });
    }

    function fetchClinicalAttrMetaData(profileMetaDataResult) {
        $.when(window.cbioportal_client.getSampleClinicalAttributes({study_id: [window.PlotsTab.cancerStudyId], sample_ids: window.PlotsTab.caseIds}))
        .then(function(response_attributes) {
                 registerMetaData(response_attributes, profileMetaDataResult);
                    readyCallBackFunction();
        });
    }

    function registerMetaData(clinicalAttrMetaDataResult, profileMetaDataResult) {

        var _profile_arr = [];
        $.each(profileMetaDataResult,function(key,profileData){
            var _datum = jQuery.extend(true, {}, datum_genetic_profile_meta);
            _datum.type = profileData.genetic_alteration_type;
            _datum.id = profileData.id;
            _datum.name = profileData.name;
            _datum.description = profileData.description;
            _profile_arr.push(_datum);

        });
        $.each(window.QuerySession.getQueryGenes(),function(key,gene){
            geneticProfiles[gene] = _profile_arr;
        });
        clinicalAttrs = []
        $.each(clinicalAttrMetaDataResult, function(index, obj) {
            var _datum = jQuery.extend(true, {}, datum_clinical_attr_meta);
            _datum.id = obj.attr_id;
            _datum.type = obj.datatype;
            _datum.name = obj.display_name;
            _datum.description = obj.description;
            clinicalAttrs.push(_datum);
        });

        //sort genetic profiles
        for(var gene in geneticProfiles) {
            var _gene_obj = geneticProfiles[gene];
            $.each(_gene_obj, function(index, _profile_obj) {

                $.each(_gene_obj, function(index, _profile_obj) {
                    if (_profile_obj.id.toLowerCase().indexOf("gistic") !== -1) {
                        bubble_up(_gene_obj, index);
                    }
                });
                
                _gene_obj.sort(function(a, b) {
                    if (genetic_profile_type_priority_list.indexOf(a.type) < genetic_profile_type_priority_list.indexOf(b.type)) {
                        return 1;
                    } else if (genetic_profile_type_priority_list.indexOf(a.type) > genetic_profile_type_priority_list.indexOf(b.type)) {
                        return -1;
                    } else if (genetic_profile_type_priority_list.indexOf(a.type) === genetic_profile_type_priority_list.indexOf(b.type)) {
                        return 0;
                    }
                });
                
                $.each(_gene_obj, function(index, _profile_obj) {
                    if (_profile_obj.id.toLowerCase().indexOf("zscores") !== -1) {
                        bubble_up(_gene_obj, index);
                    }
                });
                $.each(_gene_obj, function(index, _profile_obj) {
                    if (_profile_obj.id.toLowerCase().indexOf("zscores") === -1 &&
                        _profile_obj.id.toLowerCase().indexOf("rna_seq") !== -1) {
                        bubble_up(_gene_obj, index);
                    }
                });
                $.each(_gene_obj, function(index, _profile_obj) {
                    if (_profile_obj.id.toLowerCase().indexOf("zscores") === -1 &&
                        _profile_obj.id.toLowerCase().indexOf("rna_seq_v2") !== -1) {
                        bubble_up(_gene_obj, index);
                    }
                });

            });
            //sort clinical attribute alphabetically 
            clinicalAttrs.sort(function(a, b){
                if((a.name.toLowerCase()) < (b.name.toLowerCase())) return -1;
                if((a.name.toLowerCase()) > (b.name.toLowerCase())) return 1;
                return 0;
            });
        }
        retrieve_status = 1;
    }
    
    return {
    	/**
    	 * Fetch metadata 
    	 * 
    	 * @param readyCallBack: function to call when metadata retrieval is done
    	 */
        fetch: function(readyCallBack) {
        	readyCallBackFunction = readyCallBack;
            retrieve_status = -1;
            fetchProfileMetaData();
        },
        getClinAttrsMeta: function() {
            return clinicalAttrs;
        },
        getGeneticProfilesMeta: function(_gene) {
            return geneticProfiles[_gene];
        },
        getRetrieveStatus: function() {
            return retrieve_status;
        },
        getProfileDescription: function(_gene, attr_id) {
            $.each(metaData.getGeneticProfilesMeta(_gene), function(index, obj) {
                if (obj.id === attr_id) {
                    _result = obj.description;
                }
            });
            return _result;
        },
        getClinicalAttrDescription: function(attr_id) {
            var _result = "";
            $.each(clinicalAttrs, function(index, obj) {
                if (obj.id === attr_id) {
                    _result = obj.description;
                }
            });
            return _result;
        },
        getClinicalAttrType: function(attr_id) {
            var _result = "";
            $.each(clinicalAttrs, function(index, obj) {
                if (obj.id === attr_id) {
                    _result = obj.type;
                }
            });
            return _result;
        }
    };
    
}());

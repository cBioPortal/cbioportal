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
        genesetProfiles = {},
        clinicalAttrs = [],
        retrieve_status = getNrDataTypesExpected();  

    function getNrDataTypesExpected() {
    	var result = 0;
    	if (window.QuerySession.getQueryGenesets() != null) {
			result++;
    	}
    	if (window.QuerySession.getQueryGenes() != null) {
			result++;
    	}
    	return result;
    }
    
    //genes
    function fetchProfileMetaData() {
    	if (window.QuerySession.getQueryGenes() != null) {
	
	        var paramsGetProfiles = {
	            cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
	            case_set_id: window.QuerySession.getCaseSetId(),
	            case_ids_key: window.QuerySession.getCaseIdsKey(),
	            genetic_entity_list: window.QuerySession.getQueryGenes().join(" "),
	            genetic_entity_type: "GENE"
	        };

	        $.post("getGeneticProfile.json", paramsGetProfiles, function(result){
	        	fetchClinicalAttrMetaData(result, "GENE");
	        }, "json");  
    	}
    }
    
    //genesets
    function fetchGenesetsProfileMetaData() {    	
    	
    	//get geneset data to pass to fetchClinicalAttrMetaData
    	if (window.QuerySession.getQueryGenesets() != null) {
	        var paramsGetProfiles = {
	            cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
	            case_set_id: window.QuerySession.getCaseSetId(),
	            case_ids_key: window.QuerySession.getCaseIdsKey(),
	            genetic_entity_list: window.QuerySession.getQueryGenesets().join(" "),
	            genetic_entity_type: "GENESET"
	        };
	        $.post("getGeneticProfile.json", paramsGetProfiles, function(result) {
	        	fetchClinicalAttrMetaData(result, "GENESET");
	        }, "json");
    	}
    }

    function fetchClinicalAttrMetaData(profileMetaDataResult, geneticAlterationType) {
        var paramsGetClinicalAttributes = {
            cmd : "getClinicalData",
            cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
            case_set_id : window.QuerySession.getCaseSetId(),
            case_ids_key: window.QuerySession.getCaseIdsKey(),
            format : "json"
        };
        $.post("webservice.do", paramsGetClinicalAttributes, function(result) {
            var result = registerMetaData(result.attributes, profileMetaDataResult, geneticAlterationType);
            if (result == 0) {
	            //give "ready" signal:
	            readyCallBackFunction();
            }
        }, "json");
    }

    function registerMetaData(clinicalAttrMetaDataResult, profileMetaDataResult, geneticAlterationType) {

        var _tmp_id_arr = []; //temporary instore profile id
        var _profile_arr = [];
        for (var gene in profileMetaDataResult) { //merge all genetic profiles from all queried genes
            var _gene_obj = profileMetaDataResult[gene];
            for (var _profile_name in _gene_obj) {
                var obj = _gene_obj[_profile_name];
                if ($.inArray(obj.STABLE_ID, _tmp_id_arr) === -1)  {
                    var _datum = jQuery.extend(true, {}, datum_genetic_profile_meta);
                    _datum.type = obj.GENETIC_ALTERATION_TYPE;
                    _datum.id = obj.STABLE_ID;
                    _datum.name = obj.NAME;
                    _datum.description = obj.DESCRIPTION;    
                    _profile_arr.push(_datum);
                    _tmp_id_arr.push(obj.STABLE_ID);
                }
            }
        }
        for (var gene in profileMetaDataResult) {
           if (geneticAlterationType == "GENE") {
        	   geneticProfiles[gene] = _profile_arr;
           } else {
        	   genesetProfiles[gene] = _profile_arr;
           }
        }
        
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
        retrieve_status--;
        return retrieve_status;
    }
    
    return {
    	/**
    	 * Fetch metadata 
    	 * 
    	 * @param readyCallBack: function to call when metadata retrieval is done
    	 */
        fetch: function(readyCallBack) {
        	readyCallBackFunction = readyCallBack;
            fetchProfileMetaData();
            fetchGenesetsProfileMetaData();
        },
        getClinAttrsMeta: function() {
            return clinicalAttrs;
        },
        getGeneticProfilesMeta: function(_gene) {
            return geneticProfiles[_gene];
        },
        getGenesetsMeta: function(_gene) {
            return genesetProfiles[_gene];
        },
        getProfileDescription: function(_gene, attr_id) {
            $.each(metaData.getGeneticProfilesMeta(_gene), function(index, obj) {
                if (obj.id === attr_id) {
                    _result = obj.description;
                }
            });
            return _result;
        },
        getGenesetsDescription: function(_gene, attr_id) {
            $.each(metaData.getGenesetsMeta(_gene), function(index, obj) {
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

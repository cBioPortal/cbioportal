var metaData = (function() {
    
    var datum_genetic_profile_meta = {
            type: "",
            id: "",
            name: "",
            description: ""
        },
        datum_clinical_attr_meta = {
            id: "",
            name: "",
            description: ""
        },
        geneticProfiles = {},
        clinicalAttrs = [],
        retrieve_status = -1; //data not yet retrieved (-1), retrieved (1)

    function fetchProfileMetaData() {
        var paramsGetProfiles = {
            cancer_study_id: window.PortalGlobals.getCancerStudyId(),
            case_set_id: window.PortalGlobals.getCaseSetId(),
            case_ids_key: window.PortalGlobals.getCaseIdsKey(),
            gene_list: window.PortalGlobals.getGeneListString()
        };
        $.post("getGeneticProfile.json", paramsGetProfiles, fetchClinicalAttrMetaData, "json");  
    }

    function fetchClinicalAttrMetaData(profileMetaDataResult) {
        var paramsGetClinicalAttributes = {
            cmd : "getClinicalData",
            cancer_study_id: window.PortalGlobals.getCancerStudyId(),
            case_set_id : window.PortalGlobals.getCaseSetId(),
            format : "json"
        };
        $.post("webservice.do", paramsGetClinicalAttributes, function(result) {
            registerMetaData(result.attributes, profileMetaDataResult);
        }, "json");
    }

    function registerMetaData(clinicalAttrMetaDataResult, profileMetaDataResult) {
        for (var gene in profileMetaDataResult) {
            var _gene_obj = profileMetaDataResult[gene];
            var _profile_arr = [];
            for (var _profile_name in _gene_obj) {
                var obj = _gene_obj[_profile_name];
                var _datum = jQuery.extend(true, {}, datum_genetic_profile_meta);
                _datum.type = obj.GENETIC_ALTERATION_TYPE;
                _datum.id = obj.STABLE_ID;
                _datum.name = obj.NAME;
                _datum.description = obj.DESCRIPTION;    
                _profile_arr.push(_datum);
            }
            geneticProfiles[gene] = _profile_arr;
        }
        $.each(clinicalAttrMetaDataResult, function(index, obj) {
            var _datum = jQuery.extend(true, {}, datum_clinical_attr_meta);
            _datum.id = obj.attr_id;
            _datum.name = obj.display_name;
            _datum.description = obj.description;
            clinicalAttrs.push(_datum);
        });
        
        for(var gene in geneticProfiles) {
            var _gene_obj = geneticProfiles[gene];
            $.each(_gene_obj, function(index, _profile_obj) {

                //pass one: sort by profile name (keywords)
                //TODO: find a more efficient way to sort
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
                $.each(_gene_obj, function(index, _profile_obj) {
                    if (_profile_obj.id.toLowerCase().indexOf("gistic") !== -1) {
                        bubble_up(_gene_obj, index);
                    }
                });
                //pass two: sort by profile type
                _gene_obj.sort(function(a, b) {
                    if (genetic_profile_type_priority_list.indexOf(a.type) < genetic_profile_type_priority_list.indexOf(b.type)) {
                        return 1;
                    } else if (genetic_profile_type_priority_list.indexOf(a.type) > genetic_profile_type_priority_list.indexOf(b.type)) {
                        return -1;
                    } else if (genetic_profile_type_priority_list.indexOf(a.type) === genetic_profile_type_priority_list.indexOf(b.type)) {
                        return 0;
                    }
                });

            });
        }
        retrieve_status = 1;
    }
    
    return {
        fetch: function() {
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
        }
    };
    
}());

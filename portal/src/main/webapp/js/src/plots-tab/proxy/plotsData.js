var plotsData = (function() {
    
    var data = {
        x: {
            raw: [],
            stat: false //whether data is retrieved
        },
        y: {
            raw: [],
            stat: false //whether data is retrieved
        }
    };
    
    var ajaxCall = function(axis, callback_func) {
        if ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.genetic) {
            
            var paramsGetProfileData = {  //get profile data
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                gene_list: $("#" + ids.sidebar[axis].gene).val(),
                genetic_profile_id: $("#" + ids.sidebar[axis].profile_name).val(),
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey()
            };
            $.post("getProfileData.json", paramsGetProfileData, inner_profile_callback_func, "json");
            
            function inner_profile_callback_func(profileData) { //get mutation data
                
                var proxy = DataProxyFactory.getDefaultMutationDataProxy();
                proxy.getMutationData($("#" + ids.sidebar[axis].gene).val(), inner_mutation_callback_func);
                
                function inner_mutation_callback_func(mutationData) {
                    var mutationDetailsUtil =
                    new MutationDetailsUtil(new MutationCollection(mutationData));
                    var mutationMap = mutationDetailsUtil.getMutationCaseMap();
                    for(var caseId in profileData[$("#" + ids.sidebar[axis].gene).val()]) {
                        if (mutationMap.hasOwnProperty(caseId.toLowerCase())) { //case has mutation
                            var _mutations = [];
                            $.each(mutationMap[caseId.toLowerCase()], function(index, obj) {
                                var _tmp = mutationTranslator(obj.mutationType);
                                _mutations.push({proteinChange: obj.proteinChange, type: _tmp });
                            });
                            profileData[$("#" + ids.sidebar[axis].gene).val()][caseId].mutation = _mutations;
                        } 
                    }
                    
                    callback_func(axis, profileData);
                }
            }
            
        } else if ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.clin) {
            
            var paramsGetClinicalAttributes = {
                cmd : "getAllClinicalData",
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                case_set_id : window.PortalGlobals.getCaseSetId(),
                format : "json"
            };
            $.post("webservice.do", paramsGetClinicalAttributes, inner_callback_func, "json");
            
            function inner_callback_func(clinicalData) {
                var _tmp = [];
                $.each(clinicalData.data, function(index, obj) {
                    if (obj.attr_id === $("#" + ids.sidebar[axis].clin_attr).val()) {
                        _tmp.push({case_id: obj.sample, val: obj.attr_val});
                    }
                });
                callback_func(axis, _tmp);
            }
        
        }
    };
    

    
    var merge = function(axis, result) {
        data[axis].stat = true;
        data[axis].raw = result;

        if (data.x.stat && data.y.stat) {
            //TODO: design datum and create an array of for each visual element
        }
         
    };

    return {
        fetch: function(axis) {
            
            data[axis].stat = false;
            
            var tmp = setInterval(function () {timer();}, 1000);
            function timer() {
                if (metaData.getRetrieveStatus() !== -1) {
                    clearInterval(tmp);
                    ajaxCall(axis, merge);
                }
            }
        },
        get: function() {
            
        }
    };
    
}());
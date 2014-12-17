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
            },
        stat = {
                x: {
                    min: "",
                    max: "",
                    edge: ""
                },
                y: {
                    min: "",
                    max: "",
                    edge: ""
                }
            },
        datum = { //each associates with one individual dot in the plots 
                caseId : "",
                xVal : "",
                yVal : "",
                mutation : {}  //Mutation ID
            },
        dotsContent = {}; //json of datums -- final recipe for rendering the view
        
    var ajaxCall = function(axis, callback_func) {
        if ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.genetic) {
            
            var paramsGetProfileData = {  //webservice call to get profile data
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                gene_list: $("#" + ids.sidebar[axis].gene).val(),
                genetic_profile_id: $("#" + ids.sidebar[axis].profile_name).val(),
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey()
            };
            $.post("getProfileData.json", paramsGetProfileData, inner_profile_callback_func, "json");
            
            function inner_profile_callback_func(profileData) { 
                var _tmp = {}; //convert to json format
                for (var key in profileData[$("#" + ids.sidebar[axis].gene).val()]) {
                    var _obj = profileData[$("#" + ids.sidebar[axis].gene).val()][key];
                    _tmp[key] = _obj[$("#" + ids.sidebar[axis].profile_name).val()];
                }
                callback_func(axis, _tmp);
            }
            
        } else if ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.clin) {
            
            var paramsGetClinicalAttributes = { //webservice call to get clinical data
                cmd : "getAllClinicalData",
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                case_set_id : window.PortalGlobals.getCaseSetId(),
                format : "json"
            };
            $.post("webservice.do", paramsGetClinicalAttributes, inner_callback_func, "json");
            
            function inner_callback_func(clinicalData) {
                var _tmp = {};
                $.each(clinicalData.data, function(index, obj) { //convert to json format
                    if (obj.attr_id === $("#" + ids.sidebar[axis].clin_attr).val()) {
                        var _tmp_obj = {};
                        _tmp[obj.sample] = obj.attr_val;
                    }
                });
                callback_func(axis, _tmp);
            }
        
        }
    };
    

    
    var merge = function(axis, result) {
        //fill in x, y value
        data[axis].stat = true;
        data[axis].raw = result;
        if (data.x.stat && data.y.stat) {
            for(var xCaseId in data.x.raw) {
                var _obj = data.y.raw;
                if (_obj.hasOwnProperty(xCaseId)) {
                    if (!isEmpty(data.x.raw[xCaseId]) && !isEmpty(data.y.raw[xCaseId])) { //eliminate empty values
                        var _datum = jQuery.extend(true, {}, datum);
                        _datum.caseId = xCaseId;
                        _datum.xVal = data.x.raw[xCaseId];
                        _datum.yVal = data.y.raw[xCaseId];
                        dotsContent[xCaseId] = _datum;
                    }
                }
            }
            //calculate data status
            analyseData();
            //get mutation data
            var _gene_list = "";
            if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.genetic) {
                _gene_list += $("#" + ids.sidebar.x.gene).val();
            }
            if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.genetic &&
                    $("#" + ids.sidebar.y.gene).val() !== $("#" + ids.sidebar.x.gene).val()) {
                _gene_list += " " + $("#" + ids.sidebar.y.gene).val();
            }
            if (_gene_list !== "") {
                var proxy = DataProxyFactory.getDefaultMutationDataProxy();
                proxy.getMutationData(_gene_list, mutationCallback);
            } 
        }
        
    };
    
    function analyseData() {    //pDataX, pDataY: array of single dot objects
        var tmp_xData = [];
        var tmp_yData = [];
        for (var key in dotsContent) {
            tmp_xData.push(parseFloat(dotsContent[key].xVal));
            tmp_yData.push(parseFloat(dotsContent[key].yVal));
        }

        stat.x.min = Math.min.apply(Math, tmp_xData);
        stat.x.max = Math.max.apply(Math, tmp_xData);
        stat.x.edge = (stat.x.max - stat.x.min) * 0.2;
        stat.y.min = Math.min.apply(Math, tmp_yData);
        stat.y.max = Math.max.apply(Math, tmp_yData);
        stat.y.edge = (stat.y.max - stat.y.min) * 0.1;
    }
    
    function mutationCallback(mutationData) {
        var mutationDetailsUtil = new MutationDetailsUtil(new MutationCollection(mutationData));
        var mutationMap = mutationDetailsUtil.getMutationCaseMap();
        for (var key in mutationMap) {
            $.each(mutationMap[key], function(index, obj) {
                if (dotsContent.hasOwnProperty(key.toUpperCase())) {
                    if (typeof(dotsContent[key.toUpperCase()].mutation[obj.geneSymbol]) !== "undefined") {
                        dotsContent[key.toUpperCase()].mutation[obj.geneSymbol].details += "; " + obj.proteinChange;
                        dotsContent[key.toUpperCase()].mutation[obj.geneSymbol].type = mutationTranslator(obj.mutationType);
                    } else {
                        dotsContent[key.toUpperCase()].mutation[obj.geneSymbol] = {
                            "details": obj.proteinChange,
                            "type": mutationTranslator(obj.mutationType)
                        };
                    }                    
                }
            });
        }
    }

    return {
        fetch: function(axis) {
            data[axis].stat = false;
            data[axis].raw.length = 0;
            dotsContent = {}; //need to regenerated dots content 
            
            var tmp = setInterval(function () {timer();}, 1000);
            function timer() {
                if (metaData.getRetrieveStatus() !== -1) {
                    clearInterval(tmp);
                    ajaxCall(axis, merge);
                }
            }
        },
        get: function(callback_func) {
            var tmp = setInterval(function () {timer();}, 1000);
            function timer() {
                if (Object.keys(dotsContent).length !== 0) {
                    clearInterval(tmp);
                    callback_func(dotsContent);
                }
            }
        },
        stat: function() {
            return stat;
        }
    };
    
}());
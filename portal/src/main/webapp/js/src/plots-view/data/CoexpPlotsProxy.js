var CoexpPlotsProxy = (function() {

    var dataArr = [],
        attr = {
            min_x: "",
            max_x: "",
            min_y: "",
            max_y: "",
            profile_name: "",
            profile_description: "",
            mut_x : false, //have case(s) mutated in only gene x
            mut_y : false,  //have case(s) mutated in only gene y
            mut_both: false, //have case(s) mutated in both genes
            pearson: "",
            spearman: ""
        };
    var mutationMap = {};   

    function convertData(_alteration_data_result, geneX, geneY) {
        attr.mut_both = false;
        attr.mut_x = false;
        attr.mut_y = false;
        var geneXArr = _alteration_data_result[geneX];
        var geneYArr = _alteration_data_result[geneY];

        $.each(geneXArr, function(index) {

            var datum = jQuery.extend(true, {}, PlotsBoilerplate.datum);
            var _obj_x = geneXArr[index];
            var _obj_y = geneYArr[index];

            if ((typeof _obj_x !== "undefined") && (typeof _obj_y !== "undefined")) {
                if (!isNaN(_obj_x["value"]) && !isNaN(_obj_y["value"]) &&
                    _obj_x["value"] !== null && _obj_y["value"] !== null) {
                    datum.x_val = _obj_x["value"];  
                    datum.y_val = _obj_y["value"];
                    datum.case_id = _obj_x["caseId"];
                    datum.qtip = "Case ID: <strong><a href='tumormap.do?case_id=" + 
                             _obj_x["caseId"] + "&cancer_study_id=" +
                             window.PortalGlobals.getCancerStudyId() + "' target='_blank'>" + 
                             _obj_x["caseId"] + "</a></strong><br>" + 
                             geneX + ": <strong>" + parseFloat(_obj_x["value"]).toFixed(3) + "</strong><br>" +
                             geneY + ": <strong>" + parseFloat(_obj_y["value"]).toFixed(3) + "</strong>";
                    //Find if having mutation(s)
                    if (mutationMap.hasOwnProperty(_obj_x["caseId"].toLowerCase())) {
                        var _mut_obj = {};
                        $.each(mutationMap[(_obj_x["caseId"]).toLowerCase()], function(index, obj) {
                            var _tmp_obj = {};
                            _tmp_obj["protein_change"] = obj.proteinChange;
                            _tmp_obj["mutation_type"] = obj.mutationType;
                            if (obj.geneSymbol === geneX || obj.geneSymbol === geneY) {
                                if (obj.geneSymbol === geneX) {
                                    attr.mut_x = true;
                                    if (!_mut_obj.hasOwnProperty(geneX)) {
                                        var _tmp_arr = [];
                                        _tmp_arr.length = 0;
                                        _tmp_arr.push(_tmp_obj);
                                        _mut_obj[geneX] = _tmp_arr;   
                                        datum.qtip = datum.qtip + "<br>" + geneX + " Mutation: " + 
                                                     "<strong>" + obj.proteinChange + "</strong>";
                                    } else {
                                        _mut_obj[geneX].push(_tmp_obj);
                                        datum.qtip = datum.qtip + ", " + "<strong>" + obj.proteinChange + "</strong>";
                                    }
                                } else if(obj.geneSymbol === geneY) {
                                    attr.mut_y = true;
                                    if (!_mut_obj.hasOwnProperty(geneY)) {
                                        var _tmp_arr = [];
                                        _tmp_arr.length = 0;
                                        _tmp_arr.push(_tmp_obj);
                                        _mut_obj[geneY] =  _tmp_arr; 
                                        datum.qtip = datum.qtip + "<br>" + geneY + " Mutation: " + 
                                                     "<strong>" + obj.proteinChange + "</strong>";  
                                    } else {
                                        _mut_obj[geneY].push(_tmp_obj);
                                        datum.qtip = datum.qtip + ", " + "<strong>" + obj.proteinChange + "</strong>";
                                    }
                                }
                                datum["mutation"] = _mut_obj;
                                //construct data mut attr
                                if (_mut_obj.hasOwnProperty(geneX) && _mut_obj.hasOwnProperty(geneY)) {
                                    attr.mut_both = true;
                                } else if (_mut_obj.hasOwnProperty(geneX) && !_mut_obj.hasOwnProperty(geneY)) {
                                    attr.mut_x = true;
                                } else if (!_mut_obj.hasOwnProperty(geneX) && _mut_obj.hasOwnProperty(geneY)) {
                                    attr.mut_y = true;
                                } 
                            }
                        }); 
                    }
                    dataArr.push(datum);   
                }             
            } else {
                return true; //skip
            }
        });
    }

    function prioritizeMutatedCases() {
        var nonMutatedData = [];
        var mutatedData= [];
        var dataBuffer = [];
        $.each(dataArr, function(index, obj) {
            if (obj.hasOwnProperty("mutation")) {
                mutatedData.push(obj);
            } else {
                nonMutatedData.push(obj);
            }
        });
        $.each(nonMutatedData, function(index, obj) {
            dataBuffer.push(obj);
        });
        $.each(mutatedData, function(index, obj) {
            dataBuffer.push(obj);
        });
        dataArr = dataBuffer;
    }

    function analyseData() {
        var _yValArr = [];
        var _xValArr = [];
        $.each(dataArr, function(index, val){
            _xValArr.push(val.x_val);
            _yValArr.push(val.y_val);
        });
        attr.min_x = Math.min.apply(Math, _xValArr);
        attr.max_x = Math.max.apply(Math, _xValArr);
        attr.min_y = Math.min.apply(Math, _yValArr);
        attr.max_y = Math.max.apply(Math, _yValArr);
    }

    function getProfile(_result) {
        attr.profile_description = _result["profile_description"]; 
        attr.profile_name = _result["profile_name"];
    }

    function getMutationMaps() {
        var _mutationUtil = DataProxyFactory.getDefaultMutationDataProxy().getMutationUtil();  
        mutationMap = jQuery.extend(true, {}, _mutationUtil.getMutationCaseMap()); 
    }

    return {
        init: function(_alteration_data_result, _geneX, _geneY, _pearson, _spearman) {
            attr.pearson = _pearson;
            attr.spearman = _spearman;
            dataArr.length = 0;
            getMutationMaps();
            convertData(_alteration_data_result, _geneX, _geneY, _pearson, _spearman);
            prioritizeMutatedCases();
            analyseData();
            getProfile(_alteration_data_result);
        },
        getData: function() { return dataArr; },
        getDataAttr: function() { return attr; }
    }

}());
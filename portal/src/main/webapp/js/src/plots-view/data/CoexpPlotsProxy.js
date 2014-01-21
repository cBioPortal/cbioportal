var CoexpPlotsProxy = (function() {

    var dataArr = [],
        attr = {
            min_x: "",
            max_x: "",
            min_y: "",
            max_y: "",
            profile_name: ""
        };
    var mutationMap = {};   

    function convertData(_alteration_data_result, geneX, geneY) {
        var geneXArr = _alteration_data_result[geneX];
        var geneYArr = _alteration_data_result[geneY];

        $.each(geneXArr, function(index) {

            var datum = jQuery.extend(true, {}, PlotsBoilerplate.datum);
            var _obj_x = geneXArr[index];
            var _obj_y = geneYArr[index];

            //Find if having mutation(s)
            if (mutationMap.hasOwnProperty(_obj_x["caseId"].toLowerCase())) {
                var _mut_obj = {};
                $.each(mutationMap[(_obj_x["caseId"]).toLowerCase()], function(index, obj) {
                    var _tmp_obj = {};
                    _tmp_obj["protein_change"] = obj.proteinChange;
                    _tmp_obj["mutation_type"] = obj.mutationType;
                    if (obj.geneSymbol === geneX) {
                        if (!_mut_obj.hasOwnProperty(geneX)) {
                            var _tmp_arr = [];
                            _tmp_arr.push(_tmp_obj);
                            _mut_obj[geneX] = _tmp_arr;   
                        } else {
                            _mut_obj[geneX].push(_tmp_obj);
                        }
                    } else if(obj.geneSymbol === geneY) {
                        if (!_mut_obj.hasOwnProperty(geneY)) {
                            var _tmp_arr = [];
                            _tmp_arr.push(_tmp_obj);
                            _mut_obj[geneY] =  _tmp_arr;   
                        } else {
                            _mut_obj[geneY].push(_tmp_obj);
                        }
                    }
                }); 
                datum["mutation"] = _mut_obj;
            }
            datum.x_val = _obj_x["value"];
            datum.y_val = _obj_y["value"];
            datum.case_id = _obj_x["caseId"];
            datum.qtip = "Case ID : <strong><a href='tumormap.do?case_id=" + 
                         _obj_x["caseId"] + "&cancer_study_id=" +
                         window.PortalGlobals.getCancerStudyId() + "' target='_blank'>" + 
                         _obj_x["caseId"] + "</a></strong><br>" + 
                         geneX + " : <strong>" + parseFloat(_obj_x["value"]).toFixed(3) + "</strong><br>" +
                         geneY + " : <strong>" + parseFloat(_obj_y["value"]).toFixed(3) + "</strong>";
            dataArr.push(datum);
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
        for (var prop in _result) {
            var _tmp = _result[prop];
            attr.profile_name = _tmp[0].profile;
            return;
        }
    }

    function getMutationMaps() {
        var _mutationUtil = DataProxyFactory.getDefaultMutationDataProxy().getMutationUtil();  
        mutationMap = jQuery.extend(true, {}, _mutationUtil.getMutationCaseMap()); 
    }

    return {
        init: function(_alteration_data_result, _geneX, _geneY) {
            dataArr.length = 0;
            getMutationMaps();
            convertData(_alteration_data_result, _geneX, _geneY);
            prioritizeMutatedCases();
            analyseData();
            getProfile(_alteration_data_result);
        },
        getData: function() { return dataArr; },
        getDataAttr: function() { return attr; }
    }

}());
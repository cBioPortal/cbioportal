/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


/**
 *
 * JS functions for processing data for co-expression view
 *
 * @author: yichao S
 * @date: Jan 2014
 *
 */

var CoexpPlotsProxy = (function() {

    var dataArr = [],
        attr = {
            min_x: "",
            max_x: "",
            min_y: "",
            max_y: "",
            entity_x_profile_name: "",
            entity_x_profile_description: "",
            entity_y_profile_name: "",
            entity_y_profile_description: "",
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
                    datum.qtip = "Sample ID: <strong><a href='"
                            +cbio.util.getLinkToSampleView(window.QuerySession.getCancerStudyIds()[0],_obj_x["caseId"])
                            +"' target='_blank'>" + 
                             _obj_x["caseId"] + "</a></strong><br>" + 
                             geneX + ": <strong>" + parseFloat(_obj_x["value"]).toFixed(2) + "</strong><br>" +
                             geneY + ": <strong>" + parseFloat(_obj_y["value"]).toFixed(2) + "</strong>";
                    //Find if having mutation(s)
                    if (mutationMap.hasOwnProperty(_obj_x["caseId"].toLowerCase())) {
                        var _mut_obj = {};
                        $.each(mutationMap[(_obj_x["caseId"]).toLowerCase()], function(index, obj) {
                            var _tmp_obj = {};
                            _tmp_obj["protein_change"] = obj.get("proteinChange");
                            _tmp_obj["mutation_type"] = obj.get("mutationType");
                            if (obj.get("geneSymbol") === geneX || obj.get("geneSymbol") === geneY) {
                                if (obj.get("geneSymbol") === geneX) {
                                    attr.mut_x = true;
                                    if (!_mut_obj.hasOwnProperty(geneX)) {
                                        var _tmp_arr = [];
                                        _tmp_arr.length = 0;
                                        _tmp_arr.push(_tmp_obj);
                                        _mut_obj[geneX] = _tmp_arr;   
                                        datum.qtip = datum.qtip + "<br>" + geneX + " Mutation: " + 
                                                     "<strong>" + obj.get("proteinChange") + "</strong>";
                                    } else {
                                        _mut_obj[geneX].push(_tmp_obj);
                                        datum.qtip = datum.qtip + ", " + "<strong>" + obj.get("proteinChange") + "</strong>";
                                    }
                                } else if(obj.get("geneSymbol") === geneY) {
                                    attr.mut_y = true;
                                    if (!_mut_obj.hasOwnProperty(geneY)) {
                                        var _tmp_arr = [];
                                        _tmp_arr.length = 0;
                                        _tmp_arr.push(_tmp_obj);
                                        _mut_obj[geneY] =  _tmp_arr; 
                                        datum.qtip = datum.qtip + "<br>" + geneY + " Mutation: " + 
                                                     "<strong>" + obj.get("proteinChange") + "</strong>";  
                                    } else {
                                        _mut_obj[geneY].push(_tmp_obj);
                                        datum.qtip = datum.qtip + ", " + "<strong>" + obj.get("proteinChange") + "</strong>";
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

    function setProfileDetails(_result) {
        attr.entity_x_profile_description = _result["entity_x_profile_description"]; 
        attr.entity_x_profile_name = _result["entity_x_profile_name"];
        attr.entity_y_profile_description = _result["entity_y_profile_description"]; 
        attr.entity_y_profile_name = _result["entity_y_profile_name"];
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
            if (CoExpView.has_mutation_data()) {
                getMutationMaps();
            }
            convertData(_alteration_data_result, _geneX, _geneY, _pearson, _spearman);
            prioritizeMutatedCases();
            analyseData();
            setProfileDetails(_alteration_data_result);
        },
        getData: function() { return dataArr; },
        getDataAttr: function() { return attr; }
    };

}());
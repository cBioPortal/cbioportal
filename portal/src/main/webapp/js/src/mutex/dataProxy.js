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
 * Handling data reformating/calculation (odds-ratio, fisher exact test)
 * Reusing data from onco-print
 *
 * @Author: yichao
 * @Date: Jul 2014
 *
 **/

var MutexData = (function() {

	var oncoprintData = {},
		datum = { //template
			geneA: "",
			geneB: "",
			a: 0, //--
			b: 0, //-+
			c: 0, //+-
			d: 0, //++
			odds_ratio: 0,
			log_odds_ratio: 0,
			p_value: 0,
			association: ""
		},
		dataArr = [],
		stat = { //Simple statistics of the result
			num_of_co_oc: 0, //number of co-occurance
			num_of_sig_co_oc: 0, //number of significant co-occurance (p value < 0.05)
			num_of_mutex: 0, //number of mutual exclusive
			num_of_sig_mutex: 0, //number of significant mutual exclusive (p value < 0.05)
			num_of_no_association: 0 //number of no association
		},
		settings = {
			p_val_threshold: 0.05,
			log_odds_ratio_threshold: 0
		},
		label = {
			co_occurance: "Tendency towards co-occurrence",
			mutual_exclusivity: "Tendency towards mutual exclusivity",
			significant: "(Significant)"
		};

	function countEventCombinations() {
            var _geneArr = window.PortalGlobals.getGeneList();
            //eliminate genes with no alteration
            var _gene_arr = []; //only genes with alterations
            $.each(_geneArr, function(index, _gene) {
                var _has_alteration = false;
                $.each(oncoprintData, function(index, _data_obj) {
                    $.each(_data_obj.values, function(_index, _single_gene_obj) {
                        if (_single_gene_obj.gene === _gene) {
                            if (Object.keys(_single_gene_obj).length > 2) {
                                _has_alteration = true;
                            }
                        }
                    });
                });
                if (_has_alteration) _gene_arr.push(_gene);
            });

            $.each(_gene_arr, function(outterIndex, outterObj) {
                for (var innerIndex = outterIndex + 1; innerIndex < _gene_arr.length; innerIndex++) {
                    var _geneA = _gene_arr[outterIndex],
                        _geneB = _gene_arr[innerIndex];
                    var _a = 0, //--
                        _b = 0, //-+
                        _c = 0, //+-
                        _d = 0; //++
                    //Count mutex
                    $.each(oncoprintData, function(singleCaseIndex, singleCaseObj) {
                        var _alteredGeneA = false,
                            _alteredGeneB = false;
                        $.each(singleCaseObj.values, function(singleGeneIndex, singleGeneObj) {
                            if (singleGeneObj.gene === _geneA) {
                                if (Object.keys(singleGeneObj).length > 2) {
                                //if more than two fields(gene and sample) -- meaning there's alterations
                                        _alteredGeneA = true;
                                }
                            } else if(singleGeneObj.gene === _geneB) {
                                if (Object.keys(singleGeneObj).length > 2) {
                                //if more than two fields(gene and sample) -- meaning there's alterations
                                        _alteredGeneB = true;
                                }
                            }
                        });
                        if (_alteredGeneA === true && _alteredGeneB === true) {
                            _d += 1;
                        } else if (_alteredGeneA === true && _alteredGeneB === false) {
                            _c += 1;
                        } else if (_alteredGeneA === false && _alteredGeneB === true) {
                            _b += 1;
                        } else if (_alteredGeneA === false && _alteredGeneB === false) {
                            _a += 1;
                        }
                    });

                    //store the result
                    var _datum = $.extend(true, {}, datum);
                    _datum.geneA = _geneA;
                    _datum.geneB = _geneB;
                    _datum.a = _a;
                    _datum.b = _b;
                    _datum.c = _c;
                    _datum.d = _d;
                    dataArr.push(_datum);
                }
            });
	}

    function calc() {
        //Calculate odds-ratio and p-value
        var params = { params: "" };
        $.each(dataArr, function(index, obj) {
            params.params += obj.a + " " + obj.b + " " + obj.c + " " + obj.d + ":";
        });
        params.params = params.params.substring(0, params.params.length - 1);
            $.post("calcFisherExact.do", params, function(result) {
                result = result.replace(/\"/g, '');
                if (result.split(" ").length === dataArr.length) {
                    $.each(result.split(" "), function(index, value) {
                        var _dataObj = dataArr[index];

                        _dataObj.p_value = parseFloat(value);
                        if (_dataObj.b !== 0 && _dataObj.c !== 0) {
                            _dataObj.odds_ratio = (_dataObj.a * _dataObj.d) / (_dataObj.b * _dataObj.c);
                            _dataObj.log_odds_ratio = Math.log(_dataObj.odds_ratio);

                            //categorize
                            if (_dataObj.log_odds_ratio <= settings.log_odds_ratio_threshold || _dataObj.log_odds_ratio === "-Infinity") {
                                _dataObj.association = label.mutual_exclusivity;
                                if (_dataObj.p_value < settings.p_val_threshold) {
                                    _dataObj.association += label.significant;
                                }
                            } else if (_dataObj.log_odds_ratio > settings.log_odds_ratio_threshold) {
                                _dataObj.association = label.co_occurance;
                                if (_dataObj.p_value < settings.p_val_threshold) {
                                    _dataObj.association += label.significant;
                                }
                            }
                        } else {
                            _dataObj.odds_ratio = "Infinity";
                            _dataObj.log_odds_ratio = "Infinity";
                            _dataObj.association = label.co_occurance;
                            if (_dataObj.p_value < settings.p_val_threshold) {
                                _dataObj.association += label.significant;
                            }
                        }
                    });
                }
            });
        }

    function buildStat() {
        $.each(dataArr, function(index, obj) {
            if (obj.log_odds_ratio <= settings.log_odds_ratio_threshold || obj.log_odds_ratio === "-Infinity") {
                stat.num_of_mutex += 1;
                if (obj.p_value < settings.p_val_threshold) {
                    stat.num_of_sig_mutex += 1;
                }
            } else if (obj.log_odds_ratio > settings.log_odds_ratio_threshold || obj.log_odds_ratio === "Infinity") {
                stat.num_of_co_oc += 1;
                if (obj.p_value < settings.p_val_threshold) {
                    stat.num_of_sig_co_oc += 1;
                }
            }
        });
    }

    return {
        setOncoprintData: function(inputDataObj) {
            oncoprintData = inputDataObj;
        },
        init: function() {

            //eliminate genes with no alteration
            var _gene_arr = []; //only genes with alterations
            $.each(window.PortalGlobals.getGeneList(), function(index, _gene) {
                var _has_alteration = false;
                $.each(oncoprintData, function(index, _data_obj) {
                    $.each(_data_obj.values, function(_index, _single_gene_obj) {
                        if (_single_gene_obj.gene === _gene) {
                            if (Object.keys(_single_gene_obj).length > 2) {
                                _has_alteration = true;
                            }
                        }
                    });
                });
                if (_has_alteration) _gene_arr.push(_gene);
            });

            if (_gene_arr.length > 1) {
                countEventCombinations();
                calc();

                function detectInstance() {
                    if (dataArr.length !== 0) {
                        abortTimer();
                    }
                }
                function abortTimer() {
                    clearInterval(tid);
                    buildStat();
                    MutexView.init();
                }
								
		var tid = setInterval(detectInstance, 600);

            } else {
                $("#mutex").empty();
                $("#mutex").append("Calculation could not be performed.");
            }
        },
        getDataArr: function() {
            return dataArr;
        },
        getDataStat: function() {
            return stat;
        },
        getSignificantLabel: function() {
            return label.significant;
        }
    };

}());

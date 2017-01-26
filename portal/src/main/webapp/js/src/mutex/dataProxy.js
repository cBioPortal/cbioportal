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

    function calc() {
	var def = new $.Deferred();
        //Calculate odds-ratio and p-value
        var params = { params: "" };
        $.each(dataArr, function(index, obj) {
	    // a: not A not B
	    // b: not A, B
	    // c: A, not B
	    // d: both
            params.params += [obj.neither, obj.B_not_A, obj.A_not_B, obj.both].join(" ") + ":";
        });
        params.params = params.params.substring(0, params.params.length - 1);
            $.post("calcFisherExact.do", params, function(result) {
                result = result.replace(/\"/g, '');
                if (result.split(" ").length === dataArr.length) {
                    $.each(result.split(" "), function(index, value) {
                        var _dataObj = dataArr[index];

                        _dataObj.p_value = parseFloat(value);
                        if (_dataObj.B_not_A !== 0 && _dataObj.A_not_B !== 0) {
                            _dataObj.odds_ratio = (_dataObj.neither * _dataObj.both) / (_dataObj.B_not_A * _dataObj.A_not_B);
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
		def.resolve();
            });
	    return def.promise();
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

	    window.QuerySession.getAlteredGenes().then(function (altered_genes) {
		if (altered_genes.length > 1) {
		    window.QuerySession.getMutualAlterationCounts().then(function (counts) {
			dataArr = counts.map(function (count_obj) {
			    count_obj.geneA = count_obj.geneA.toUpperCase();
			    count_obj.geneB = count_obj.geneB.toUpperCase();
			    count_obj.odds_ratio = 0;
			    count_obj.log_odds_ratio = 0;
			    count_obj.p_value = 0;
			    count_obj.association = "";
			    return count_obj;
			});
			return calc();
		    }).then(function() {
			buildStat();
			MutexView.init();
		    });
		} else {
		    $("#mutex").empty();
		    $("#mutex").append("Calculation could not be performed.");
		}
	    });
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

/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
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
			association: "" //
		},
		dataArr = [],
		stat = { //Simple statistics of the result
			num_of_co_oc: 0, //number of co-occurance
			num_of_sig_co_oc: 0, //number of significant co-occurance (p value < 0.05)
			num_of_mutex: 0, //number of mutual exclusive
			num_of_sig_mutex: 0, //number of significant mutual exclusive (p value < 0.05)
			num_of_no_association: 0 //number of no association
		};

	var processData = function() {
		oncoprintData = PortalDataColl.getOncoprintData(); 
		countEventCombinations();
		calc();
	}

	function countEventCombinations() {
		var _geneArr = window.PortalGlobals.getGeneList();
		$.each(_geneArr, function(outterIndex, outterObj) {
			for (var innerIndex = outterIndex + 1; innerIndex < _geneArr.length; innerIndex++) {
				var _geneA = _geneArr[outterIndex],
					_geneB = _geneArr[innerIndex];
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
					
					_dataObj.p_value = parseFloat(value).toFixed(3);
					if (_dataObj.p_value === "0.000") {
						_dataObj.p_value = "<0.001";
					}

					if (_dataObj.b !== 0 && _dataObj.c !== 0) {
						_dataObj.odds_ratio = (_dataObj.a * _dataObj.d) / (_dataObj.b * _dataObj.c);
						
						//Omit the real value of log 
						_dataObj.log_odds_ratio = Math.log(_dataObj.odds_ratio).toFixed(3);
						if (_dataObj.log_odds_ratio < -3 || _dataObj.log_odds_ratio === "-Infinity") {
							_dataObj.log_odds_ratio = "<-3";
						} else if (_dataObj.log_odds_ratio > 3) {
							_dataObj.log_odds_ratio = ">3";
						}

						//categorize
						if (0 <= _dataObj.odds_ratio && _dataObj.odds_ratio <= 0.5) {
							if (_dataObj.p_value < 0.05 || _dataObj.p_value === "<0.001") {
								_dataObj.association = "Significant tendency towards <b>mutual exclusivity</b>";
							} else {
								_dataObj.association = "Tendency towards <b>mutual exclusivity</b>";
							}
						} else if (0.5 < _dataObj.odds_ratio && _dataObj.odds_ratio < 2) {
							_dataObj.association = "No association";
						} else if (2 <= _dataObj.odds_ratio) {
							if (_dataObj.p_value < 0.05 || _dataObj.p_value === "<0.001") {
								_dataObj.association = "Significant tendency towards <b>co-occurrence</b>";
							} else {
								_dataObj.association = "Tendency towards <b>co-occurrence</b>";
							}
						} 
					} else {
						_dataObj.odds_ratio = "--"; 
						_dataObj.log_odds_ratio = "--"; 
					}
				});
			}
			buildStat();
		});
	}

	function buildStat() {
		$.each(dataArr, function(index, obj) {
			if (obj.odds_ratio >= 2) {
				stat.num_of_co_oc += 1;
				if (obj.p_value === "<0.001" ||
					obj.p_value < 0.05) {
					stat.num_of_sig_co_oc += 1;
				}
			} else if (obj.odds_ratio <= 0.5 &&
					   obj.odds_ratio >= 0) {		
				stat.num_of_mutex += 1;
				if (obj.p_value === "<0.001" ||
					obj.p_value < 0.05) {
					stat.num_of_sig_mutex += 1;
				}		
			} else {
				stat.num_of_no_association += 1;
			}
		});
		MutexView.init();
	}

	return {
		init: function() {
	        PortalDataCollManager.subscribeOncoprint(processData);
		},
		getDataArr: function() {
			return dataArr;
		},
		getDataStat: function() {
			return stat;
		}
	}

}());

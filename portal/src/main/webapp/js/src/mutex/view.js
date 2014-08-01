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
 * Render the datatable for the mutex tab
 *
 * @Author: yichao
 * @Date: Jul 2014
 *
 **/

 var MutexView = (function() {

 	var mutexTableInstance = "",
        mutexTableDataArr = [],
        names = {
            tabldId: "mutex-table",
            divId: "mutex-table-div"
        },
        index = {
            geneA : 0,
            geneB : 1,
            pVal : 2,
            oddsRatio : 3,
            association: 4
        },
        colorCode = {
            oddsRatio: "#296CCF",
            pVal: "#296CCF"
        }

    function configTable() {
        $("#mutex-table-div").append(
            "<table id='" + names.tableId + "'>" +
            "<thead style='font-size:70%'>" +
            "<th>Gene A</th>" +
            "<th>Gene B</th>" +
            "<th>p-Value<img src='images/help.png' id='p-value-help'></th>" + 
            "<th>Log Odds Ratio<img src='images/help.png' id='odds-ratio-help'></th>" +
            "<th>Association<img src='images/help.png' id='association-help'></th>" + 
            "</thead>" +
            "<tbody></tbody>" + 
            "</table>"
        );

        mutexTableInstance = $("#" + names.tableId).dataTable({
            "sDom": '<"H"f<"mutex-table-filter">>t<"F"i>',
            "bPaginate": false,
            "sScrollY": "600px",
            "paging": false,
            "scrollCollapse": true,
            "bScrollCollapse": true,
            "bInfo": true,
            "bJQueryUI": true,
            "bAutoWidth": false,
            "aaData" : mutexTableDataArr,
            "aaSorting": [[2, 'asc']], //sort by p-Value
            "aoColumnDefs": [
                {
                    "bSearchable": true,
                    "aTargets": [ index.geneA ],
                    "sWidth": "100px"
                },
                {
                    "bSearchable": true,
                    "aTargets": [ index.geneB ],
                    "sWidth": "100px"
                },
                {
                    "sType": 'mutex-p-value',
                    "bSearchable": false,
                    "aTargets": [ index.pVal ],
                    "sWidth": "150px",
                    "sClass": "classMutexTable"

                },
                {
                    "sType": 'mutex-odds-ratio',
                    "bSearchable": false,
                    "aTargets": [ index.oddsRatio ],
                    "sWidth": "150px",
                    "sClass": "classMutexTable"
                },
                {
                    "bSearchable": false,
                    "aTargets": [ index.association ],
                    "sWidth": "500px"
                }
            ],
            "oLanguage": {
                "sSearch": "Search Gene"
            },
            "fnRowCallback": function(nRow, aData) {
                $('td:eq(' + index.geneA + ')', nRow).css("font-weight", "bold");
                $('td:eq(' + index.geneB + ')', nRow).css("font-weight", "bold");
                $('td:eq(' + index.pVal + ')', nRow).css("color", colorCode.pVal);
                $('td:eq(' + index.oddsRatio + ')', nRow).css("color", colorCode.oddsRatio);
                if (aData[index.pVal] <= 0.05 || aData[index.pVal] === "<0.001") { //significate p value
                    $('td:eq(' + index.pVal + ')', nRow).css("font-weight", "bold");
                }
            }
        }); 

    }

    function convertData() {
    	$.each(MutexData.getDataArr(), function(index, obj){
            if (obj.log_odds_ratio !== "--") {
                var _arr = [];
                _arr.push(obj.geneA);
                _arr.push(obj.geneB);            
                _arr.push(obj.p_value);
                _arr.push(obj.log_odds_ratio);
                _arr.push(obj.association);
                mutexTableDataArr.push(_arr);       
            }
    	});
    }

    function overWriteFilters() {
        jQuery.fn.dataTableExt.oSort['mutex-odds-ratio-desc'] = function(a,b) {
            if (a == "<-3") { a = -3 };
            if (b == "<-3") { b = -3 };
            if (a == ">3") { a = 3 };
            if (b == ">3") { b = 3 };
            if (a > b) return -1;
            else if (a < b) return 1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['mutex-odds-ratio-asc'] = function(a,b) {
            if (a == "<-3") { a = -3 };
            if (b == "<-3") { b = -3 };
            if (a == ">3") { a = 3 };
            if (b == ">3") { b = 3 };
            if (a > b) return 1;
            else if (a < b) return -1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['mutex-p-value-desc'] = function(a,b) {
            if (a == "<0.001") { a = 0.0009 };
            if (b == "<0.001") { b = 0.0009 };
            if (a > b) return -1;
            else if (a < b) return 1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['mutex-p-value-asc'] = function(a,b) {
            if (a == "<0.001") { a = 0.0009 };
            if (b == "<0.001") { b = 0.0009 };
            if (a > b) return 1;
            else if (a < b) return -1;
            else return 0;
        };

    }

    function attachFilter() { 
        $("#mutex-table-div").find('.mutex-table-filter').append(
            "<select id='mutex-table-filter-select'>" +
            "<option value='all'>Show All</option>" +
            "<option value='mutex'>Show Only Mutual Exclusive</option>" +
            "<option value='cooccur'>Show Only Co-occurrence</option>" +
            "</select>");
        $("select#mutex-table-filter-select").change(function () {
            if ($(this).val() === "mutex") {
                mutexTableInstance.fnFilter("-", index.oddsRatio, false);
            } else if ($(this).val() === "cooccur") {
                mutexTableInstance.fnFilter('^[+]?([1-9][0-9]*(?:[\.][0-9]*)?|0*\.0*[1-9][0-9]*)(?:[eE][+-][0-9]+)?$', index.oddsRatio, true);
            } else if ($(this).val() === "all") {
                mutexTableInstance.fnFilter("", index.oddsRatio);
            }
        });
        mutexTableInstance.$('td').qtip({
            content: { attr: 'title' },
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
            show: {event: "mouseover", delay: 0},
            hide: {fixed:true, delay: 10, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window), adjust: {x: -150, y: 10}}
        })
    }   

    function addHeaderQtips() {
        $("#association-help").qtip({
            content: { text:'Log odds ratio < -0.3 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; : Association towards co-occurrence<br>' +
                            '-0.3 < Log odds ratio < 0.3 : No association<br>' +   
                            '0.3 < Log odds ratio &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; : Association towards mutual exclusivity<br>' + 
                            'p-Value < 0.005 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; : Significate association'},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });  
        $("#odds-ratio-help").qtip({
            content: { text:'Quantify how strongly the presence or absence of property A is associated with the presence or absence of property B in a given population.'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });  
        $("#p-value-help").qtip({
            content: { text:'Derived from Fisher Exact Test'},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });  
    }

    function addStatInfo() {
        var _stat = MutexData.getDataStat();
        for (var key in _stat) {
            if (_stat[key] === 0) {
                _stat[key] = "Non"; //replace 0 (text) with "non"
            }
        }
        $("#num_of_mutex").append(_stat.num_of_mutex);
        $("#num_of_sig_mutex").append(_stat.num_of_sig_mutex);
        $("#num_of_co_oc").append(_stat.num_of_co_oc);
        $("#num_of_sig_co_oc").append(_stat.num_of_sig_co_oc);
    }

 	return {
 		init: function() {
 			$("#mutex-loading-image").hide();
 			convertData();
            overWriteFilters();
 			configTable();
            attachFilter();
            addHeaderQtips();
            addStatInfo();
  		},
        resize: function() {
            mutexTableInstance.fnAdjustColumnSizing();
        }
 	}
 }());
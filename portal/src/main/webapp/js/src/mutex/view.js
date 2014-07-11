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

 	var mutexTableDataArr = [];

    function configTable() {
        //Draw out the markdown of the datatable
        $("#mutex-table-div").append(
            "<table id='mutex-table'>" +
            "<thead style='font-size:70%;'>" +
            "<tr>" + 
            "<th>Gene A</th>" +
            "<th>Gene B</th>" +
            "<th>p Value</th>" + 
            "<th>Odds Ratio</th>" +
            "<th>Lower Confidence Interval</th>" + 
            "<th>Upper Confidence Interval</th>" + 
            "</tr>" +
            "</thead>" +
            "<tbody></tbody>" + 
            "</table>"
        );

        //Configure the datatable with  jquery
        mutexTableInstance = $("#mutex-table").dataTable({
            "sDom": '<"H"f>t<"F"ip>>',
            "bPaginate": true,
            "sPaginationType": "two_button",
            "bInfo": true,
            "bJQueryUI": true,
            "bAutoWidth": false,
            "aaData" : mutexTableDataArr,
            "aaSorting": [[2, 'desc']],
            "aoColumnDefs": [
                {
                    "bSearchable": true,
                    "aTargets": [ 0 ],
                    "sWidth": "10%"
                },
                {
                    "bSearchable": true,
                    "aTargets": [ 1 ],
                    "sWidth": "10%"
                },
                {
                    "bSearchable": false,
                    "aTargets": [ 2 ],
                    "sWidth": "15%"
                },
                {
                    "bSearchable": false,
                    "aTargets": [ 3 ],
                    "sWidth": "15%"
                },
                {
                    "bSearchable": false,
                    "aTargets": [ 4 ],
                    "sWidth": "25%"
                },

                {
                    "bSearchable": false,
                    "aTargets": [ 5 ],
                    "sWidth": "25%"
                }
            ],
            "oLanguage": {
                "sSearch": "Search Gene"
            },
            "bScrollCollapse": true,
            "bDeferRender": true,
            "iDisplayLength": 30
        });  
    }

    function convertData() {
    	$.each(MutexData.getDataArr(), function(index, obj){
			var _arr = [];
			_arr.push(obj.geneA);
			_arr.push(obj.geneB);
			_arr.push(obj.p_value);
			_arr.push(obj.odds_ratio);
			if (obj.lower_confidence_interval === "0.000" || obj.lower_confidence_interval === "NaN") {
				_arr.push("--");
			} else {
				_arr.push(obj.lower_confidence_interval);
			}
			if (obj.upper_confidence_interval === "0.000" || obj.upper_confidence_interval === "NaN") {
				_arr.push("--");
			} else {
				_arr.push(obj.upper_confidence_interval);
			}
			mutexTableDataArr.push(_arr); 		
    	});
    }

 	return {
 		init: function() {
 			$("#mutex-loading-image").hide();
 			convertData();
 			configTable();
 		}
 	}
 }());
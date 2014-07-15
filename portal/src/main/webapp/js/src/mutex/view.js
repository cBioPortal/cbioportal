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

 	var mutexTableDataArr = [],
        names = {
            tabldId: "mutex-table",
            divId: "mutex-table-div"
        };

    function configTable() {
        $("#mutex-table-div").append(
            "<table id='" + names.tableId + "'>" +
            "<thead style='font-size:70%'>" +
            "<tr>" + 
            "<th>Gene A</th>" +
            "<th>Gene B</th>" +
            "<th>Odds Ratio (log)</th>" +
            "<th>p Value</th>" + 
            "</tr>" +
            "</thead>" +
            "<tbody></tbody>" + 
            "</table>"
        );

        mutexTableInstance = $("#" + names.tableId).dataTable({
            "sDom": '<"H"f<"mutex-table-filter">>t<"F"ip>',
            "bPaginate": true,
            "sPaginationType": "two_button",
            "bInfo": true,
            "bJQueryUI": true,
            "bAutoWidth": false,
            "aaData" : mutexTableDataArr,
            "aaSorting": [[2, 'desc']], //sort by odds-ratio
            "aoColumnDefs": [
                {
                    "bSearchable": true,
                    "aTargets": [ 0 ],
                    "sWidth": "25%"
                },
                {
                    "bSearchable": true,
                    "aTargets": [ 1 ],
                    "sWidth": "25%"
                },
                {
                    "sType": 'mutex-absolute-value',
                    "bSearchable": false,
                    "aTargets": [ 2 ],
                    "sWidth": "25%"
                },
                {
                    "bSearchable": false,
                    "aTargets": [ 3 ],
                    "sWidth": "25%"
                }
            ],
            "oLanguage": {
                "sSearch": "Search Gene"
            },
            "bScrollCollapse": true,
            "bDeferRender": true,
            "iDisplayLength": 30,
            "fnRowCallback": function(nRow, aData) {
                if (aData[2] < 0) { 
                    $('td:eq(2)', nRow).css("color", "red");
                } else if (aData[2] > 0) {
                    $('td:eq(2)', nRow).css("color", "green");
                }
            }
        });  
    }

    function convertData() {
    	$.each(MutexData.getDataArr(), function(index, obj){
            if (obj.odds_ratio !== "--") {
                var _arr = [];
                _arr.push(obj.geneA);
                _arr.push(obj.geneB);            
                _arr.push(obj.odds_ratio);
                _arr.push(obj.p_value);
                mutexTableDataArr.push(_arr);       
            }
    	});
    }

    function overWriteFilters() {
        jQuery.fn.dataTableExt.oSort['mutex-absolute-value-desc'] = function(a,b) {
            if (Math.abs(a) > Math.abs(b)) return -1;
            else if (Math.abs(a) < Math.abs(b)) return 1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['mutex-absolute-value-asc'] = function(a,b) {
            if (Math.abs(a) > Math.abs(b)) return 1;
            else if (Math.abs(a) < Math.abs(b)) return -1;
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
                mutexTableInstance.fnFilter("-", 2, false);
            } else if ($(this).val() === "cooccur") {
                mutexTableInstance.fnFilter('^[+]?([1-9][0-9]*(?:[\.][0-9]*)?|0*\.0*[1-9][0-9]*)(?:[eE][+-][0-9]+)?$', 2, true);
            } else if ($(this).val() === "all") {
                mutexTableInstance.fnFilter("", 2);
            }
        });
    }      

 	return {
 		init: function() {
 			$("#mutex-loading-image").hide();
 			convertData();
            overWriteFilters();
 			configTable();
            attachFilter();
  		}
 	}
 }());
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


var orTable = function() {
    
    var div_id, table_id, data, titles; //titles is formatted string of column names with html markdown in
    
    var col_index = {};
    
    function configTable(_profile_type) {
        
        //Draw out the markdown of the datatable
        $("#" + table_id).append(
            "<thead style='font-size:65%;'>" +
            "<tr>" + titles + "</tr>" +
            "</thead><tbody></tbody>"
        );

        //Configure the datatable with  jquery
        orTableInstance = $("#" + table_id).dataTable({
            "sDom": '<"H"f>t<"F"ip>',
            "bPaginate": true,
            "sPaginationType": "full_numbers",
            "bInfo": true,
            "bJQueryUI": true,
            "bAutoWidth": false,
            "aaData" : data,
            "aaSorting": [[col_index.p_val, 'desc']],
            "sScrollY": "400px",
            "bScrollCollapse": true,
            "oLanguage": {
                "sSearch": "Search Gene  "
            },
            "fnRowCallback": function(nRow, aData) {
                
                //bold gene names
                $('td:eq(' + col_index.gene + ')', nRow).css("font-weight", "bold");
                
                //bold siginicant pvalue and qvalue
                if (aData[col_index.p_val] === "<0.001" ||
                    aData[col_index.p_val] < 0.005) { //significate p value
                    $('td:eq(' + col_index.p_val + ')', nRow).css("font-weight", "bold");
                }
                if (aData[col_index.q_val] === "<0.001" ||
                    aData[col_index.q_val] < 0.005) { //significate q value
                    $('td:eq(' + col_index.q_val + ')', nRow).css("font-weight", "bold");
                } 
                
                if (_profile_type === orAnalysis.profile_type.copy_num || _profile_type === orAnalysis.profile_type.mutations) {
                    if (aData[col_index.log_ratio] > 0) {
                        $('td:eq('+ col_index.log_ratio +')', nRow).css("color", "#3B7C3B");
                    } else if (aData[col_index.log_ratio] < 0) {
                        $('td:eq(' + col_index.log_ratio + ')', nRow).css("color", "#B40404");
                    } 
                }

            },
            "bDeferRender": true,
            "iDisplayLength": 17
        });  

    }
    
    //sortings
    jQuery.fn.dataTableExt.oSort['or-analysis-p-value-desc'] = function(a,b) {
        if (a === "<0.001") { a = 0.0009; }
        if (b === "<0.001") { b = 0.0009; }
        if (a > b) return -1;
        else if (a < b) return 1;
        else return 0;
    };
    jQuery.fn.dataTableExt.oSort['or-analysis-p-value-asc'] = function(a,b) {
        if (a === "<0.001") { a = 0.0009; }
        if (b === "<0.001") { b = 0.0009; }
        if (a > b) return 1;
        else if (a < b) return -1;
        else return 0;
    };
    jQuery.fn.dataTableExt.oSort['or-analysis-q-value-desc'] = function(a,b) {
        if (a === "<0.001") { a = 0.0009; }
        if (b === "<0.001") { b = 0.0009; }
        if (a > b) return -1;
        else if (a < b) return 1;
        else return 0;
    };
    jQuery.fn.dataTableExt.oSort['or-analysis-q-value-asc'] = function(a,b) {
        if (a === "<0.001") { a = 0.0009; }
        if (b === "<0.001") { b = 0.0009; }
        if (a > b) return 1;
        else if (a < b) return -1;
        else return 0;
    };
    
    function define_titles(_profile_type) {
        
        var _title_str = "";
        
        if (_profile_type === orAnalysis.profile_type.copy_num) {
            
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.gene + "'>Gene &nbsp;<img src='images/help.png' id='gene-help'></th>";
            _title_str += "<th colspan='2'>Percentage of alteration &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.log_ratio + "'>Log Ratio &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.p_val + "'>p-Value &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.q_val + "'>q-Value &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.direction + "'>Direction/Tendency &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "</tr><tr><th width='" + orAnalysis.col_width.altered_pct + "'>in altered group</th>";
            _title_str += "<th width='" + orAnalysis.col_width.unaltered_pct + "'>in unaltered group</th>";

        } else if (_profile_type === orAnalysis.profile_type.mutations) {
                        
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.gene + "'>Gene &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th colspan='2'>Percentage of alteration &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.log_ratio + "'>Log Ratio &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.p_val + "'>p-Value &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.q_val + "'>q-Value &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.direction + "'>Direction/Tendency &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "</tr><tr><th width='" + orAnalysis.col_width.altered_pct + "'>in altered group</th>";
            _title_str += "<th width='" + orAnalysis.col_width.unaltered_pct + "'>in unaltered group</th>";
            
        } else if (_profile_type === orAnalysis.profile_type.mrna) {
            
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.gene + "'>Gene &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th colspan='2'>Mean of alteration &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th colspan='2'>Standard deviation of alteration &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.p_val + "'>p-Value &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.q_val + "'>q-Value &nbsp;<img src='images/help.png' id='mean-alt-help'></th>";
            _title_str += "</tr><tr>";
            _title_str += "<th width='" + orAnalysis.col_width.altered_mean + "'>in altered group</th>";
            _title_str += "<th width='" + orAnalysis.col_width.unaltered_mean + "'>in unaltered group</th>";
            _title_str += "<th width='" + orAnalysis.col_width.altered_stdev + "'>in altered group</th>";
            _title_str += "<th width='" + orAnalysis.col_width.unaltered_stdev + "'>in unaltered group</th>";
        
        }

        return _title_str;
    }
    
    return {
        init: function(_input_data, _div_id, _table_div, _table_id, _table_title, _profile_type) {
            
            if (Object.keys(_input_data).length !== 0 &&
                Object.keys(_input_data)[0] !== orAnalysis.texts.null_result &&
                Object.keys(_input_data)[0] !== "") {
            
                div_id = _div_id;
                table_id = _table_id;
                data = _input_data;
                
                if (_profile_type === orAnalysis.profile_type.copy_num) {
                    col_index = orAnalysis.col_index.copy_num;
                } else if (_profile_type === orAnalysis.profile_type.mutations) {
                    col_index = orAnalysis.col_index.mutations;
                } else if (_profile_type === orAnalysis.profile_type.mrna) {
                    col_index = orAnalysis.col_index.mrna;
                }
                
                titles = define_titles(_profile_type);

                $("#" + _table_div).empty();
                $("#" + _table_div).append("<span style='font-weight:bold;'>" + _table_title + "</span>");
                $("#" + _table_div).append("<table id='" + table_id + "' cellpadding='0' cellspacing='0' border='0' class='" + table_id + "_datatable_class'></table>"); 
                configTable(_profile_type);
                
            } else {
                $("#" + _table_div).remove();
            }

        }
    };
    
}; //close orTable

var orSubTabView = function() {
    
    return {
        init: function(_div_id, _profile_list, _profile_type) {

            //split copy number profile into deep deletion and amplification 
            if (_profile_type === orAnalysis.profile_type.copy_num) {
                var _del_obj = jQuery.extend(true, {}, _profile_list[0]);
                var _amp_obj = jQuery.extend(true, {}, _profile_list[0]);
                _del_obj.STABLE_ID += "_del";
                _amp_obj.STABLE_ID += "_amp";
                _del_obj.NAME += " (Deep Deletion)";
                _amp_obj.NAME += " (Amplification)";
                
                _profile_list.length = 0;
                _profile_list = [];
                _profile_list.push(_del_obj);
                _profile_list.push(_amp_obj);
            }
            
            $.each(_profile_list, function(_index, _profile_obj) {
                
                 //avoid duplicated initiation
                var element = $("." + _profile_obj.STABLE_ID + orAnalysis.postfix.datatable_class);
                if (element.length === 0) {

                    var _table_div = _profile_obj.STABLE_ID + orAnalysis.postfix.datatable_div;
                    var _table_id = _profile_obj.STABLE_ID + orAnalysis.postfix.datatable_id;
                    $("#" + _div_id).append("<div id='" + _table_div + "' style='width: 1200px; display:inline-block; padding: 10px;'></div>");
                    $("#" + _table_div).append("<img style='padding:20px;' src='images/ajax-loader.gif'><br>Calculating on " + _profile_obj.NAME);
                    
                    //init and get calculation result from the server
                    var param = new orAjaxParam(or_tab.getAlteredCaseList(), or_tab.getUnalteredCaseList(), _profile_obj.STABLE_ID);
                    var or_data = new orData();
                    or_data.init(param);
                    var or_table = new orTable();
                    or_data.get(or_table.init, _div_id, _table_div, _table_id, _profile_obj.NAME, _profile_type);
                    
                }
                
            });
            
        }
    };
};



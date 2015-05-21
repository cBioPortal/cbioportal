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
    var col_index, orTableInstance, profile_type;
    
    function configTable() {
        
        //sortings
        jQuery.fn.dataTableExt.oSort['or-analysis-p-value-desc'] = function(a,b) {

            if (profile_type === orAnalysis.profile_type.mrna) {
                if (a.indexOf("up1") !== -1) a = a.replace("<img src=\"images/up1.png\"/>",  "");
                if (a.indexOf("down1") !== -1) a = a.replace("<img src=\"images/down1.png\"/>",  "");
                if (b.indexOf("up1") !== -1) b = b.replace("<img src=\"images/up1.png\"/>",  "");
                if (b.indexOf("down1") !== -1) b = b.replace("<img src=\"images/down1.png\"/>",  "");
            }

            if (a === "<0.001") { a = 0.0009; }
            if (b === "<0.001") { b = 0.0009; }
            if (a > b) return -1;
            else if (a < b) return 1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['or-analysis-p-value-asc'] = function(a,b) {

            if (profile_type === orAnalysis.profile_type.mrna) {
                if (a.indexOf("up1") !== -1) a = a.replace("<img src=\"images/up1.png\"/>",  "");
                if (a.indexOf("down1") !== -1) a = a.replace("<img src=\"images/down1.png\"/>",  "");
                if (b.indexOf("up1") !== -1) b = b.replace("<img src=\"images/up1.png\"/>",  "");
                if (b.indexOf("down1") !== -1) b = b.replace("<img src=\"images/down1.png\"/>",  "");
            }

            if (a === "<0.001") { a = 0.0009; }
            if (b === "<0.001") { b = 0.0009; }

            if (a > b) return 1;
            else if (a < b) return -1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['or-analysis-q-value-desc'] = function(a,b) {

             if (profile_type === orAnalysis.profile_type.mrna) {
                if (a.indexOf("up1") !== -1) a = a.replace("<img src=\"images/up1.png\"/>",  "");
                if (a.indexOf("down1") !== -1) a = a.replace("<img src=\"images/down1.png\"/>",  "");
                if (b.indexOf("up1") !== -1) b = b.replace("<img src=\"images/up1.png\"/>",  "");
                if (b.indexOf("down1") !== -1) b = b.replace("<img src=\"images/down1.png\"/>",  "");
            }

            if (a === "<0.001") { a = 0.0009; }
            if (b === "<0.001") { b = 0.0009; }
            if (a > b) return -1;
            else if (a < b) return 1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['or-analysis-q-value-asc'] = function(a,b) {

            if (profile_type === orAnalysis.profile_type.mrna) {
                if (a.indexOf("up1") !== -1) a = a.replace("<img src=\"images/up1.png\"/>",  "");
                if (a.indexOf("down1") !== -1) a = a.replace("<img src=\"images/down1.png\"/>",  "");
                if (b.indexOf("up1") !== -1) b = b.replace("<img src=\"images/up1.png\"/>",  "");
                if (b.indexOf("down1") !== -1) b = b.replace("<img src=\"images/down1.png\"/>",  "");
            }

            if (a === "<0.001") { a = 0.0009; }
            if (b === "<0.001") { b = 0.0009; }
            if (a > b) return 1;
            else if (a < b) return -1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['or-analysis-log-ratio-desc'] = function(a,b) {
            if (a === "<-3") { a = -10; }
            if (b === "<-3") { b = -10; }
            if (a === ">3") { a = 10; }
            if (b === ">3") { b = 10; }
            if (a < 0 && b < 0) {
                if (Math.abs(a) > Math.abs(b)) return 1;
                else return -1;
            } else {
                if (a > b) return -1;
                else if (a < b) return 1;
                else return 0;
            }
        };
        jQuery.fn.dataTableExt.oSort['or-analysis-log-ratio-asc'] = function(a,b) {
            if (a === "<-3") { a = -10; }
            if (b === "<-3") { b = -10; }
            if (a === ">3") { a = 10; }
            if (b === ">3") { b = 10; }
            if (a < 0 && b < 0) {
                if (Math.abs(a) > Math.abs(b)) return -1;
                else return 1;
            } else {
                if (a > b) return 1;
                else if (a < b) return -1;
                else return 0;
            }
        };
        
        //Draw out the markdown of the datatable
        $("#" + table_id).append(
            "<thead style='font-size:65%;'>" +
            "<tr>" + titles + "</tr>" +
            "</thead><tbody></tbody>"
        );

        //Configure the datatable with  jquery
        orTableInstance = $("#" + table_id).dataTable({
            "sDom": "<'H'f<'" + table_id + "_filter'>>t<'F'ip>",
            "bPaginate": true,
            "sPaginationType": "full_numbers",
            "bInfo": true,
            "bJQueryUI": true,
            "bAutoWidth": false,
            "aaData" : data,
            "aaSorting": [[col_index.p_val, 'asc']],
            "sScrollY": "400px",
            "bScrollCollapse": true,
            "oLanguage": {
                "sSearch": "Search Gene  "
            },
            "aoColumnDefs": [
                {
                    "sType": 'or-analysis-p-value',
                    "bSearchable": false,
                    "aTargets": [ col_index.p_val ]
                },
                {
                    "sType": 'or-analysis-q-value',
                    "bSearchable": false,
                    "aTargets": [ col_index.q_val ]
                },
                {
                    "sType": 'or-analysis-log-ratio',
                    "bSearchable": false,
                    "aTargets": [ col_index.log_ratio ]
                }
            ],
            "fnRowCallback": function(nRow, aData) {
                //bold gene names
                $('td:eq(' + col_index.gene + ')', nRow).css("font-weight", "bold");
                
                if (profile_type === orAnalysis.profile_type.copy_num || profile_type === orAnalysis.profile_type.mutations) {
                    if (aData[col_index.log_ratio] > 0 || aData[col_index.log_ratio] === ">3") {
                        $('td:eq('+ col_index.log_ratio +')', nRow).css("color", "#3B7C3B");
                    } else if (aData[col_index.log_ratio] < 0 || aData[col_index.log_ratio] === "<-3") {
                        $('td:eq(' + col_index.log_ratio + ')', nRow).css("color", "#B40404");
                    } 
                    //bold siginicant pvalue and qvalue
                    if (aData[col_index.p_val] === "<0.001" ||
                        aData[col_index.p_val] < orAnalysis.settings.p_val_threshold) { //significate p value
                        $('td:eq(' + col_index.p_val + ')', nRow).css("font-weight", "bold");
                    }
                    if (aData[col_index.q_val] === "<0.001" ||
                        aData[col_index.q_val] < orAnalysis.settings.p_val_threshold) { //significate q value
                        $('td:eq(' + col_index.q_val + ')', nRow).css("font-weight", "bold");
                    } 
                } else if (profile_type === orAnalysis.profile_type.mrna) {
                    var _p_val = aData[col_index.p_val],
                        _q_val = aData[col_index.q_val];
                    if (_p_val.indexOf("up1") !== -1) _p_val = _p_val.replace("<img src=\"images/up1.png\"/>",  "");
                    if (_p_val.indexOf("down1") !== -1) _p_val = _p_val.replace("<img src=\"images/down1.png\"/>",  "");
                    if (_q_val.indexOf("up1") !== -1) _q_val = _q_val.replace("<img src=\"images/up1.png\"/>",  "");
                    if (_q_val.indexOf("down1") !== -1) _q_val = _q_val.replace("<img src=\"images/down1.png\"/>",  "");
                    
                    if (_p_val === "<0.001" || _p_val < orAnalysis.settings.p_val_threshold) {
                        $('td:eq(' + col_index.p_val + ')', nRow).css("font-weight", "bold");
                    }
                    if (_q_val === "<0.001" || _q_val < orAnalysis.settings.p_val_threshold) {
                        $('td:eq(' + col_index.q_val + ')', nRow).css("font-weight", "bold");
                    }
                }
            },
            "bDeferRender": true,
            "iDisplayLength": 17
        });

    }
    
    function attachFilters() {
        
        $("#" + div_id).find("." + table_id + "_filter").append(
            "<input type='checkbox' class='" + table_id + "-checkbox' checked id='" + table_id + "-checkbox-mutex'>Mutual exclusivity</option> &nbsp;&nbsp;" +
            "<input type='checkbox' class='" + table_id + "-checkbox' checked id='" + table_id + "-checkbox-co-oc'>Co-occurrence</option> &nbsp;&nbsp;" +
            "<input type='checkbox' class='" + table_id + "-checkbox' id='" + table_id + "-checkbox-sig-only'>Significant gene(s)</option> &nbsp; &nbsp;"
        );

        var _sig_only_all_fn = function() {
                orTableInstance.fnFilter("", col_index.log_ratio);
                orTableInstance.fnFilter("", col_index.direction);
                orTableInstance.fnFilter("Significant", col_index.direction);
            },
            _sig_only_mutex_fn = function() {
                orTableInstance.fnFilter("", col_index.direction);
                orTableInstance.fnFilter("", col_index.log_ratio);
                orTableInstance.fnFilter("^(.)*\\sunaltered(.)*Significant$", col_index.direction, true);
            },
            _sig_only_co_oc_fn = function() {
                orTableInstance.fnFilter("", col_index.direction);
                orTableInstance.fnFilter("", col_index.log_ratio);
                orTableInstance.fnFilter("^(.)*\\saltered(.)*Significant$", col_index.direction, true);
            },
            _mutex_fn = function() {
                orTableInstance.fnFilter("", col_index.direction);
                orTableInstance.fnFilter("", col_index.log_ratio);
                orTableInstance.fnFilter("^(.)*\\sunaltered(.)*$", col_index.direction, true);
            },
            _co_oc_fn = function() {
                orTableInstance.fnFilter("", col_index.direction);
                orTableInstance.fnFilter("", col_index.log_ratio);
                orTableInstance.fnFilter("^(.)*\\saltered(.)*$", col_index.direction, true);
            }, 
            _all_fn = function() {
                orTableInstance.fnFilter("", col_index.direction);
                orTableInstance.fnFilter("", col_index.log_ratio);
            },
            _empty_fn = function() {
                orTableInstance.fnFilter("&", col_index.log_ratio);
            };

        $("." + table_id + "-checkbox").change(function () {
        
            var _mutex_checked = false,
                _co_oc_checked = false,
                _sig_checked = false;

            if ($("#" + table_id + "-checkbox-sig-only").is(':checked')) _sig_checked = true;
            if ($("#" + table_id + "-checkbox-mutex").is(':checked')) _mutex_checked = true;
            if ($("#" + table_id + "-checkbox-co-oc").is(':checked')) _co_oc_checked = true;
            
            if (_mutex_checked && _co_oc_checked) {
                if (_sig_checked) _sig_only_all_fn();
                else _all_fn();
            } else if (_mutex_checked && !_co_oc_checked) {
                if (_sig_checked) _sig_only_mutex_fn();
                else _mutex_fn();
            } else if (!_mutex_checked && _co_oc_checked) {
                if (_sig_checked) _sig_only_co_oc_fn();
                else _co_oc_fn();
            } else {
                _empty_fn();
            }            
        });
        
    }
    
    function addHeaderQtips() {
        $("#" + table_id + orAnalysis._title_ids.gene).qtip({
            content: { text:"Select genes you are interested and click 'update query' button to query with the new genes."},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + orAnalysis._title_ids.pct).qtip({
            content: { text:"percentage of alteration in altered and unaltered sample groups"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });  
        $("#" + table_id + orAnalysis._title_ids.log_ratio).qtip({
            content: { text:"log2 based ratio of (pct in altered / pct in unaltered)"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });  
        $("#" + table_id + orAnalysis._title_ids.p_val).qtip({
            content: { text:"derived from fisher exact test"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });        
        $("#" + table_id + orAnalysis._title_ids.q_val).qtip({
            content: { text:"Derived from Benjamini-Hochberg adjust precedure"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + orAnalysis._title_ids.direction).qtip({
            content: { text:'Log odds ratio > 0&nbsp;&nbsp;&nbsp;: Enriched in altered group<br>' +
                            'Log odds ratio <= 0&nbsp;: Enriched in unaltered group<br>' + 
                            'p-Value < 0.05&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: Significant association'},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + orAnalysis._title_ids.mean_alt).qtip({
            content: { text:"Mean of expression values in altered and unaltered group"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + orAnalysis._title_ids.stdev_alt).qtip({
            content: { text:"Standard Deviation in altered and unaltered group"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + orAnalysis._title_ids.p_val_t_test).qtip({
            content: { text:"Derived from student t test"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
    }
    
    function define_titles() {
        
        var _title_str = "";
        
        if (profile_type === orAnalysis.profile_type.copy_num) {

            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.gene + "'>Gene &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.gene + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.cytoband + "'>Cytoband</th>";
            _title_str += "<th colspan='2'>Percentage of alteration &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.pct + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.log_ratio + "'>Log Ratio &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.log_ratio + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.p_val + "'>p-Value &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.p_val + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.q_val + "'>q-Value &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.q_val + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.direction + "'>Direction/Tendency &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.direction + "'></th>";
            _title_str += "</tr><tr><th width='" + orAnalysis.col_width.altered_pct + "'>in altered group</th>";
            _title_str += "<th width='" + orAnalysis.col_width.unaltered_pct + "'>in unaltered group</th>";

        } else if (profile_type === orAnalysis.profile_type.mutations) {

            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.gene + "'>Gene &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.gene + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.cytoband + "'>Cytoband</th>";
            _title_str += "<th colspan='2'>Percentage of alteration &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.pct + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.log_ratio + "'>Log Ratio &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.log_ratio + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.p_val + "'>p-Value &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.p_val + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.q_val + "'>q-Value &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.q_val + "'></th>";
            _title_str += "<th rowspan='2' width='" + orAnalysis.col_width.direction + "'>Direction/Tendency &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.direction + "'></th>";
            _title_str += "</tr><tr><th width='" + orAnalysis.col_width.altered_pct + "'>in altered group</th>";
            _title_str += "<th width='" + orAnalysis.col_width.unaltered_pct + "'>in unaltered group</th>";
            
        } else if (profile_type === orAnalysis.profile_type.mrna) {

            _title_str += "<th rowspan='2' width='100'>Gene &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.gene + "'></th>";
            _title_str += "<th rowspan='2' width='100'>Cytoband</th>";
            _title_str += "<th colspan='2'>Mean of alteration &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.mean_alt + "'></th>";
            _title_str += "<th colspan='2'>Standard deviation of alteration &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.stdev_alt + "'></th>";
            _title_str += "<th rowspan='2' width='100'>p-Value &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.p_val_t_test + "'></th>";
            _title_str += "<th rowspan='2' width='100'>q-Value &nbsp;<img src='images/help.png' id='" + table_id + orAnalysis._title_ids.q_val + "'></th>";
            _title_str += "</tr><tr>";
            _title_str += "<th width='100'>in altered group</th>";
            _title_str += "<th width='100'>in unaltered group</th>";
            _title_str += "<th width='100'>in altered group</th>";
            _title_str += "<th width='100'>in unaltered group</th>";
        
        }

        return _title_str;
    }

    function activateUpdateQueryBtns(btn_id) {

        document.getElementById(table_id + orAnalysis.postfix.datatable_update_query_button).disabled = true;

        //attach event listener to checkboxes
        $("." + table_id + orAnalysis.postfix.datatable_gene_checkbox_class).click(function() {
                var _selected_genes = [];
                $("#" + div_id + " ." + table_id + orAnalysis.postfix.datatable_gene_checkbox_class + ":checked").each(function(){
                    _selected_genes.push($(this).attr("value"));
                });
                if (_selected_genes.length !== 0) {
                    document.getElementById(btn_id).disabled = false;
                } else {
                    document.getElementById(btn_id).disabled = true;
                }
            }
        );


        var _arr_selected_genes = [];
        $("#" + btn_id).click(function() {

            _arr_selected_genes = [];
            $("#" + div_id + " ." + table_id + orAnalysis.postfix.datatable_gene_checkbox_class + ":checked").each(function(){
                _arr_selected_genes.push($(this).attr("value"));
            });

            var _start_pos_gene_list = document.URL.indexOf("gene_list=");
            var _end_pos_gene_list = document.URL.indexOf("&", _start_pos_gene_list);
            var _gene_str = document.URL.substring(_start_pos_gene_list + 10, _end_pos_gene_list);

            var _original_url = document.URL;
            var _new_url = _original_url.replace(_gene_str, _gene_str + "+" + _arr_selected_genes.join("+"));

            if (_arr_selected_genes.length !== 0) {
                window.location.replace(_new_url);
            }

        });
    }
    
    return {
        init: function(_input_data, _div_id, _table_div, _table_id, _table_title, _profile_type) {
            
            if (Object.keys(_input_data).length !== 0 &&
                Object.keys(_input_data)[0] !== orAnalysis.texts.null_result &&
                Object.keys(_input_data)[0] !== "") {
            
                div_id = _div_id;
                table_id = _table_id;
                data = _input_data;
                profile_type = _profile_type;
                
                $("#" + div_id + "_loading_img").empty();
                
                if (profile_type === orAnalysis.profile_type.copy_num) {
                    col_index = orAnalysis.col_index.copy_num;
                } else if (profile_type === orAnalysis.profile_type.mutations) {
                    col_index = orAnalysis.col_index.mutations;
                } else if (profile_type === orAnalysis.profile_type.mrna) {
                    col_index = orAnalysis.col_index.mrna;
                }
                
                titles = define_titles();

                $("#" + _table_div).empty();
                $("#" + _table_div).append("<span style='font-weight:bold;'>" + _table_title +
                      "</span>&nbsp;&nbsp;&nbsp;&nbsp;<button type='button' id='" + table_id +
                      orAnalysis.postfix.datatable_update_query_button + "'>Add checked genes to query</button>");

                $("#" + _table_div).append("<table id='" + table_id + "' cellpadding='0' cellspacing='0' border='0' class='" + table_id + "_datatable_class'></table>"); 
                configTable();
                attachFilters();
                addHeaderQtips();
                activateUpdateQueryBtns(table_id + orAnalysis.postfix.datatable_update_query_button);
                
            } else {
                $("#" + _table_div).remove();
            }

        }
    };
    
}; //close orTable

var orSubTabView = function() {

    var gene_set = "";
    
    return {
        init: function(_div_id, _profile_list, _profile_type, _gene_set) {
            
            //append loading img
            $("#" + _div_id).empty();
            $("#" + _div_id).append("<div id='" + _div_id + "_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif'></div>");

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
                    
                    //init and get calculation result from the server
                    var param = new orAjaxParam(or_tab.getAlteredCaseList(), or_tab.getUnalteredCaseList(), _profile_obj.STABLE_ID, _gene_set);
                    var or_data = new orData();
                    or_data.init(param, _table_id);
                    var or_table = new orTable();
                    or_data.get(or_table.init, _div_id, _table_div, _table_id, _profile_obj.NAME, _profile_type);
                    
                }
                
            });
            
        }
    };
};



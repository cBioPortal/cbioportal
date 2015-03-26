/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var orTable = function() {
    
    var div_id, table_id, data;
    
    function configTable() {
        
        //Draw out the markdown of the datatable
        $("#" + table_id).append(
            "<thead style='font-size:70%;' >" +
            "<tr>" + 
            "<th>Gene</th>" +
            "<th>p-Value</th>" +
            "</tr>" +
            "</thead><tbody></tbody>"
        );

        //Configure the datatable with  jquery
        orTableInstance = $("#" + table_id).dataTable({
            "sDom": '<"H"f>t<"F"ip>',
            "bPaginate": true,
            "sPaginationType": "two_button",
            "bInfo": true,
            "bJQueryUI": true,
            "bAutoWidth": false,
            "aaData" : data,
            "aaSorting": [[1, 'desc']],
            "sScrollY": "600px",
            "bScrollCollapse": true,
            "oLanguage": {
                "sSearch": "Search Gene"
            },
            "bDeferRender": true,
            "iDisplayLength": 30
        });  
    }
    
    function convert_data(_json_data) {
        var table_arr = [];
        $.each(Object.keys(_json_data), function(_index, _key) {
            var _unit = [];
            _unit.push(_key);
            _unit.push(_json_data[_key]);
            table_arr.push(_unit);
        });  
        return table_arr;
    }
    
    return {
        init: function(_input_data, _div_id, _table_id, _table_title) {
            
            div_id = _div_id;
            table_id = _table_id;
            
            data = convert_data(_input_data);
            
            $("#" + _div_id).append(_table_title);
            $("#" + _div_id).append("<table id='" + table_id + "' cellpadding='0' cellspacing='0' border='0' class='" + table_id + "_datatable_class'></table>"); 
            configTable();
        }
    };
    
};

var orSubTabView = function() {
    
    return {
        init: function(_div_id) {
            if (_div_id === orAnalysis.ids.sub_tab_main) {
                var element_gistic_table = $("." + orAnalysis.ids.sub_tab_main + "_gistic_datatable_class");
                var element_mutations_table = $("." + orAnalysis.ids.sub_tab_main + "_mutations_datatable_class");
                if (element_gistic_table.length === 0) {
                    //GISTIC table
                    var param_gistic = new orAjaxParam(or_tab.getAlteredCaseList(), or_tab.getUnalteredCaseList(), window.PortalGlobals.getCancerStudyId() + "_gistic");
                    var or_data_gistic = new orData();
                    or_data_gistic.init(param_gistic);
                    var or_table_gistic = new orTable();
                    or_data_gistic.get(or_table_gistic.init, orAnalysis.ids.sub_tab_main, orAnalysis.ids.sub_tab_main + "_gistic", "Putative copy-number alterations from GISTIC"); 
                }
                if (element_mutations_table.length === 0) {
                    //Mutations table
                    var param_mutations = new orAjaxParam(or_tab.getAlteredCaseList(), or_tab.getUnalteredCaseList(), window.PortalGlobals.getCancerStudyId() + "_mutations");
                    var or_data_mutations = new orData();
                    or_data_mutations.init(param_mutations);
                    var or_table_mutations = new orTable();
                    or_data_mutations.get(or_table_mutations.init, orAnalysis.ids.sub_tab_main, orAnalysis.ids.sub_tab_main + "_mutations", "Mutations"); 
                }
            } else if (_div_id === orAnalysis.ids.sub_tab_mrna_exp) {
                
            } else if (_div_id === orAnalysis.ids.sub_tab_advanced) {
                
            }
        }
    };
};



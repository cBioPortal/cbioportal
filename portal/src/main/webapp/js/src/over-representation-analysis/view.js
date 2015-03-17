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
        init: function(_input_data, _div_id) {
            
            div_id = _div_id;
            table_id = _div_id + "_data_table";
            
            data = convert_data(_input_data);
            
            $("#" + _div_id).append("<table id='" + table_id + "' cellpadding='0' cellspacing='0' border='0'></table>"); 
            configTable();
        }
    };
    
};



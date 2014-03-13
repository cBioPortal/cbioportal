/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var StudyViewInitDataTable = (function() {
    
    var param, data;
    
    function initParam(_param, _data) {
        param = _param;
        data = _data;
    }
    
    function initDataTable() {
        DATATABLE = new DataTable();
        DATATABLE.init(param, data);
    }
    
    return {
        init: function(_param, _data) {
            initParam(_param, _data);
            initDataTable();
        }
    };
})();
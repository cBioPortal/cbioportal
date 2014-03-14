/*
 * View for Data Table 
 * 
 *                                       
 * @authur: Hongxin Zhang
 * @date: Mar. 2014
 * 
 */


var StudyViewInitDataTable = (function() {
    
    var param, data, dataTable;
    
    function initParam(_param, _data) {
        param = _param;
        data = _data;
    }
    
    function initDataTable() {
        dataTable = new DataTable();
        dataTable.init(param, data);
    }
    
    return {
        init: function(_param, _data) {
            initParam(_param, _data);
            initDataTable();
        },
        
        getDataTable: function() {
            return dataTable;
        }
    };
})();
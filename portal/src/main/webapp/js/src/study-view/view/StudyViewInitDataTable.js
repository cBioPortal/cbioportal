/*
 * View for Data Table 
 * 
 *                                       
 * @authur: Hongxin Zhang
 * @date: Mar. 2014
 * 
 */


var StudyViewInitDataTable = (function() {
    
    var data, dataTable;
    
    function initParam(_data) {
        data = _data;
    }
    
    function initDataTable() {
        dataTable = new DataTable();
        dataTable.init(data);
    }
    
    return {
        init: function(_data) {
            initParam(_data);
            initDataTable();
        },
        
        getDataTable: function() {
            return dataTable;
        }
    };
})();
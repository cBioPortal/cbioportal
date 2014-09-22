

var StudyViewInitClinicalTab = (function(){
    
    var dataTable;
    
    
    function init(_tableID, _tableContainerId, _data){
//        tableID = _tableID;
//        aaData = _aaData;
//        aoColumns = _aoColumns;
        dataTable = new DataTable();
        dataTable.init(_tableID, _tableContainerId, _data);
        dataTable.updateFrozedColStyle();
        //initDataTable();
    }
    
//    function initDataTable(){
//        var oTable = $('#'+tableID).dataTable( {
//            "sDom": '<"H"fr>t<"F"<"datatable-paging"pil>>',
//            "bJQueryUI": true,
//            "aoColumns":aoColumns,
//            "aaData": aaData,
//            "sScrollX": "1200px",
//            "bScrollCollapse": true,
//            "iDisplayLength": 25
//        });
//        
//        oTable.css("width","100%");
//        $('.case-id-td').attr("nowrap","nowrap");
//    }
    
    return {
        init: init,
        getDataTable: function() {
            return dataTable;
        }
    };
})();
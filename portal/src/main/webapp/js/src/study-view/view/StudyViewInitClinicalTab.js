

var StudyViewInitClinicalTab = (function(){
    
    var aaData, 
        aoColumns,
        tableID;
    
    
    function init(_tableID, _aoColumns, _aaData){
        tableID = _tableID;
        aaData = _aaData;
        aoColumns = _aoColumns;
        
        initDataTable();
    }
    
    function initDataTable(){
        var oTable = $('#'+tableID).dataTable( {
            "sDom": '<"H"fr>t<"F"<"datatable-paging"pil>>',
            "bJQueryUI": true,
            "aoColumns":aoColumns,
            "aaData": aaData,
            "sScrollX": "1200px",
            "bScrollCollapse": true
        });
        
        oTable.css("width","100%");
        $('.case-id-td').attr("nowrap","nowrap");
    }
    
    return {
        init: init
    };
})();
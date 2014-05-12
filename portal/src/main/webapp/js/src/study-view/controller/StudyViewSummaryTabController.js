

var StudyViewSummaryTabController = (function() {
    var initComponents = function (_data) {
        //StudyViewInitDataTable.init(_data);
        StudyViewInitCharts.init(_data);
        StudyViewInitTopComponents.init();
        //StudyViewInitMiddleComponents.init();
        //StudyViewInitDataTable.getDataTable().resizeTable();
        StudyViewWindowEvents.init();
        $('#dc-plots-loading-wait').hide();
        $('#study-view-main').show();
    };
    
    function initPage(){
        $("#data-table-chart").html("");
        $("#data-table-chart").append(StudyViewBoilerplate.dataTableDiv);
    }
    
    return {
        init: function(_data) {
//            initPage();
            initComponents(_data);
        }
    };

})();


var StudyViewSummaryTabController = (function() {
    var parObject = {
        studyId: "",
        caseIds: "",
        cnaProfileId: "",
        mutationProfileId: "",
        caseSetId: ""
    };
        
    var initComponents = function (_data) {
        StudyViewInitDataTable.init(parObject, _data);
        StudyViewInitCharts.init(parObject, _data);
        StudyViewInitTopComponents.init(parObject);
        StudyViewInitMiddleComponents.init();
        StudyViewInitDataTable.getDataTable().resizeTable();
        StudyViewWindowEvents.init();
    };
    
    function initLocalParameters(o){
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;
        parObject.caseSetId = o.caseSetId;
    }
    
    function initPage(){
        $("#data-table-chart").html("");
        $("#data-table-chart").append(StudyViewBoilerplate.dataTableDiv);
    }
    
    return {
        init: function(_params, _data) {
            initLocalParameters(_params);
            initPage();
            initComponents(_data);
        }
    };

})();
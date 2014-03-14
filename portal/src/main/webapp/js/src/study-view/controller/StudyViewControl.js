/* 
 * This class is designed to control Study View working flow.
 * 1. Get data by using StudyViewProxy
 * 2. Passing data to StudyViewInitCharts and initial all charts
 */


var StudyViewControl = (function() {
    var parObject = {
        studyId: "",
        caseIds: "",
        cnaProfileId: "",
        mutationProfileId: "",
        caseSetId: ""
    };
        
    var callbackFunc = function (_data) {
        StudyViewInitDataTable.init(parObject, _data);
        StudyViewInitCharts.init(parObject, _data);
        StudyViewInitTopComponents.init(parObject);
        StudyViewInitMiddleComponents.init();
        StudyViewInitDataTable.getDataTable().resizeTable();
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
        init: function(o) {
            initLocalParameters(o);
            initPage();
            StudyViewProxy.init(parObject,callbackFunc);
        }
    };

})();
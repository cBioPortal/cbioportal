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
        
    var callbackFunc = function (data) {
        StudyViewInitCharts.init(parObject,data);
    };
    
    function initLocalParameters(o){
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;
        parObject.caseSetId = o.caseSetId;
    }
    
    return {
        init: function(o) {
            initLocalParameters(o);
            StudyViewProxy.init(parObject,callbackFunc);
        }
    };

})();
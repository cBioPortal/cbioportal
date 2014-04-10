/* 
 * This class is designed to control Study View working flow.
 * 1. Get data by using StudyViewProxy
 * 2. Passing data to StudyViewInitCharts and initial all charts
 * 
 * 
 * @author Hongxin Zhang
 */


var StudyViewMainController = (function() {
    var parObject = {
        studyId: "",
        caseIds: "",
        cnaProfileId: "",
        mutationProfileId: "",
        caseSetId: ""
    };
        
    var callbackFunc = function (_data) {
        StudyViewSummaryTabController.init(parObject, _data);
        StudyViewClinicalTabController.init();
        if (mutationProfileId){
            StudyViewMutationsTabController.init(parObject);
        }
        StudyViewCNATabController.init();
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
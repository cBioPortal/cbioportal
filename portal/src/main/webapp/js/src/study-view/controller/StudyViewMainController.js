/* 
 * This class is designed to control Study View working flow.
 * 1. Get data by using StudyViewProxy
 * 2. Passing data to StudyViewInitCharts and initial all charts
 * 
 * 
 * @author Hongxin Zhang
 */


var StudyViewMainController = (function() {
    var callbackFunc = function (_data) {
        StudyViewSummaryTabController.init(_data);
        StudyViewClinicalTabController.init(_data);
        if (StudyViewParams.params.mutationProfileId){
            StudyViewMutationsTabController.init();
        }
        StudyViewCNATabController.init();
    };
    
    return {
        init: function(_params) {
            StudyViewParams.params = _params;
            StudyViewProxy.init(callbackFunc);
        }
    };
})();
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
        for(var i=1, tabsL = $('#study-tabs').find('li').length; i < tabsL; i++) {
            $('#study-tabs').tabs('enable', i);
        }
        StudyViewPrototypes.init();
        if(!(_data.attr.length === 1 && _data.attr[0].attr_id === 'CASE_ID')) {
            StudyViewSummaryTabController.init(_data);
        }else {
            $("#dc-plots-loading-wait").css('display', 'none');
            $("#dc-plots").append("<div style='width:100%'>"+
                "There isn't any information for this study.</div>");
        }
    };
    
    return {
        init: function(_params) {
            StudyViewParams.params = _params;
            StudyViewProxy.init(callbackFunc);
        }
    };
})();
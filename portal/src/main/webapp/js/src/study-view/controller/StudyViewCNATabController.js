


var StudyViewCNATabController = (function() {
    var init = function (){
        StudyViewInitCNATab.init(
            StudyViewProxy.getGisticData()
        );
    };
    
    return {
        init: function() {
            init();
        }
    };
})();



var StudyViewCNATabController = (function() {
    var init = function (){
        StudyViewInitCNATab.init(
            StudyViewProxy.getCNAData()
        );
    };
    
    return {
        init: function() {
            init();
        }
    };
})();



var StudyViewMutationsTabController = (function() {
    var init = function (){
        StudyViewInitMutationsTab.init(
            StudyViewProxy.getMutatedGenesData()
        );
    };
    
    return {
        init: function() {
            init();
        }
    };
})();
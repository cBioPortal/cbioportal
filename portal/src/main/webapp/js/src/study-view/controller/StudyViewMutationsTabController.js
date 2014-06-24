


var StudyViewMutationsTabController = (function() {
    var initComponents = function (){
        var _data = StudyViewProxy.getMutatedGenesData();
        StudyViewInitMutationsTab.init(_data);
    };
    
    return {
        init: function() {
            initComponents();
        }
    };
})();



var StudyViewCNATabController = (function() {
    var initComponents = function (){
        var _data = StudyViewProxy.getCNAData();
        StudyViewInitCNATab.init(_data);
    };
    
    return {
        init: function() {
            initComponents();
        }
    };

})();
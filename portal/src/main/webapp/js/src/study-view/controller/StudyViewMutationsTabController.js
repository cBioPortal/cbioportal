


var StudyViewMutationsTabController = (function() {
    var parObject = {
        studyId: "",
        caseIds: "",
        cnaProfileId: "",
        mutationProfileId: "",
        caseSetId: ""
    };
        
    var initComponents = function (){
        var _data = StudyViewProxy.getMutatedGenesData();
        StudyViewInitMutationsTab.init(_data);
    };
    
    function initLocalParameters(o){
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;
        parObject.caseSetId = o.caseSetId;
    }
    
    return {
        init: function(_params) {
            initLocalParameters(_params);
            initComponents();
        }
    };

})();
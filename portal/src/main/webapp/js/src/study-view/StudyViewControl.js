/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var StudyViewControl = (function() {
    var parObject = {
        studyId: "",
        caseIds: "",
        cnaProfileId: "",
        mutationProfileId: ""
    };
        
    var callbackFunc = function (data) {
        StudyViewInitCharts.init(parObject,data);
    };
    
    function initLocalParameters(o){
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;
    }
    
    return {
        init: function(o) {
            initLocalParameters(o);
            StudyViewProxy.init(parObject,callbackFunc);
        }
    };

})();
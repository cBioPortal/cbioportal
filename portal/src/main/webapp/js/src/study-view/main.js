
//Charts Array, store all pie chart/ bar chart instances
var varChart = [];
var DATATABLE;
var removeKeyIndex = [];
    

$(document).ready(function(){
    var parObject = {
        studyId: cancerStudyId,
        caseIds: caseIds,
        cnaProfileId: cnaProfileId,
        mutationProfileId: mutationProfileId,
        caseSetId: caseSetId
    };
    
    var loadingJSCallBackFunc = function(){
        StudyViewControl.init(parObject);
    };
   
    require(['util/LoadingJS'],function(){
        LoadingJS.init(loadingJSCallBackFunc);
    });
});



//Charts Array, store all pie chart/ bar chart instances

$(document).ready(function(){
    var parObject = {
        studyId: cancerStudyId,
        caseIds: caseIds,
        cnaProfileId: cnaProfileId,
        mutationProfileId: mutationProfileId,
        caseSetId: caseSetId,
        hasMutSig: hasMutSig
    };
    
    var loadingJSCallBackFunc = function(){
        StudyViewMainController.init(parObject);
    };
    
    require(['util/LoadingJS'],function(){
        LoadingJS.init(loadingJSCallBackFunc);
    });
});


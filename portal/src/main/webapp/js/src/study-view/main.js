
//Charts Array, store all pie chart/ bar chart instances

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
    
    $(window).scroll(function(e){ 
        var $el = $('#study-view-header-function'); 
        if ($(this).scrollTop() > 255 && $el.css('position') !== 'fixed'){ 
          $('#study-view-header-function').css({'position': 'fixed', 'top': '0px'}); 
        }
        if ($(this).scrollTop() < 255 && $el.css('position') === 'fixed'){ 
          $('#study-view-header-function').css({'position': 'absolute', 'top': '60px'}); 
        }
    });
});


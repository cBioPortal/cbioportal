
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
        if ($(this).scrollTop() > 255){ 
            var _transformY = Number($(this).scrollTop()) - 255;
            $('#study-view-header-function').css({
                'left': '-13px',
                'width': '1276px',
                'transform': 'translate(0,' + _transformY + 'px)',
                '-webkit-transform': 'translate(0,' + _transformY + 'px)',
                '-ms-transform': 'translate(0,' + _transformY + 'px)'
            });
        }
        if ($(this).scrollTop() < 255){ 
            $('#study-view-header-function').css({
                'top': '60px',
                'left': '',
                'width': '1200px',
                transform: ''
            }); 
        }
    });
});


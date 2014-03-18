
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
        var $el = $('#study-view-header-function'),
            _windowWidth = $(window).width();
        if ($(this).scrollTop() > 255 && $el.css('position') !== 'fixed'){ 
            $('#study-view-header-function').css({
                'position': 'fixed', 
                'top': '0',
                'left': '0',
                'width': _windowWidth
            });
        }
        if ($(this).scrollTop() < 255 && $el.css('position') === 'fixed'){ 
            $('#study-view-header-function').css({
                'position': 'absolute', 
                'top': '60px',
                'left': '',
                'width': '1200px'
            }); 
        }
    });
    
    $( window ).resize(function() {
        var $el = $('#study-view-header-function'),
            _windowWidth = $(window).width();
            
        if($el.css('position') === 'fixed'){
            $('#study-view-header-function').css({
                'width': _windowWidth
            });
        }
    });
});


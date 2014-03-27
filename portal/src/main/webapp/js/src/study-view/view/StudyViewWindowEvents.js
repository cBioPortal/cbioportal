/* 
 *This class is designed to listen all windows events. Exp. window.scroll
 */


var StudyViewWindowEvents = (function(){
    
    var chartsTabHeaderTopInitialized = false,
        chartsTabHeaderTop = 0;
        
    function initEvents(){
        initScrollEvent();
    }
    
    function initScrollEvent(){
        $(window).scroll(function(e){
            
            if(!chartsTabHeaderTopInitialized){
                chartsTabHeaderTop = $("#study-view-header-function").offset().top;
                chartsTabHeaderTopInitialized = true;
            }
            
            if ($(this).scrollTop() > chartsTabHeaderTop){ 
                var _transformY = Number($(this).scrollTop()) - chartsTabHeaderTop;
                $('#study-view-header-function').css({
                    'left': '-13px',
                    'width': '1276px',
                    'transform': 'translate(0,' + _transformY + 'px)',
                    '-webkit-transform': 'translate(0,' + _transformY + 'px)',
                    '-ms-transform': 'translate(0,' + _transformY + 'px)'
                });
            }
            if ($(this).scrollTop() < chartsTabHeaderTop){ 
                $('#study-view-header-function').css({
                    'top': '60px',
                    'left': '',
                    'width': '1200px',
                    transform: ''
                }); 
            }
        });
    }
    
    return {
      init: initEvents 
    };
})();


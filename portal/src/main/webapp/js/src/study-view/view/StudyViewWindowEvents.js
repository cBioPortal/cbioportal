/* 
 *This class is designed to listen all windows events. Exp. window.scroll
 */


var StudyViewWindowEvents = (function(){
    
    var chartsTabHeaderTopInitialized = false,
        chartsTabHeaderTop = 0,
        scrolled = false;
        
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
                
                scrolled = true;
                
                $('#study-view-header-function').css({
                    'left': '-13px',
                    'width': '1276px',
                    'background-color': '#2986e2',
                    'transform': 'translate(0,' + _transformY + 'px)',
                    '-webkit-transform': 'translate(0,' + _transformY + 'px)',
                    '-ms-transform': 'translate(0,' + _transformY + 'px)'
                });
                $('.study-view-header').css({
                    'border-width': '0',
                    'border-radius': '0',
                    'color': 'white',
                    'background-color': '#2986e2'
                });
                
                $('#study-view-header-left-3').css({
                    'left': '0',
                    'top': '50px',
                    'color': 'white',
                    'background-color': '#2986e2',
                    'opacity': '0.8'
                });
            }
            if ($(this).scrollTop() < chartsTabHeaderTop){
                scrolled = false;
                
                $('#study-view-header-function').css({
                    'top': '60px',
                    'left': '',
                    'background-color': 'white',
                    'width': '1200px',
                    'transform': ''
                }); 
                $('.study-view-header').css({
                    'border-width': '1px',
                    'border-radius': '5px',
                    'color': '#2986e2',
                    'background-color': 'white'
                });
                if($("#study-view-header-left-1").css('display') === 'none'){
                    $('#study-view-header-left-3').css({
                        'left': '170px'
                    });
                }else{
                     $('#study-view-header-left-3').css({
                        'left': '410px'
                    });
                }
                $('#study-view-header-left-3').css({
                    'top': '12px',
                    'color': '#2986e2',
                    'background-color': 'white',
                    'opacity': '1'
                });
            }
        });
    }
    return {
      init: initEvents,
      getScrollStatus: function() {
          return scrolled;
      }
    };
})();


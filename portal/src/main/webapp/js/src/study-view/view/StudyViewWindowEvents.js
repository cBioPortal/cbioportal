/* 
 *This class is designed to listen all windows events. Exp. window.scroll
 *
 * @interface: getScrollStatus -- return the flag whether page has been scrolled.
 */


var StudyViewWindowEvents = (function(){
    
    var chartsTabHeaderTopInitialized = false,
        chartsTabHeaderTop = 0,
        
        //Whether the is scrolled, and here is specific to detect whether
        //the header of Charts Tab is on the page top or not.
        //Will be used outside function
        scrolled = false;
        
    function initEvents(){
        initScrollEvent();
    }
    
    function initScrollEvent(){
        $(window).scroll(function(e){
            
            //To get offset position of charts tab header, and only initial once
            if(!chartsTabHeaderTopInitialized){
                chartsTabHeaderTop = $("#study-view-header-function").offset().top;
                chartsTabHeaderTopInitialized = true;
            }
            
            if ($(this).scrollTop() > chartsTabHeaderTop){
                //Use transform to move header
                var _transformY = Number($(this).scrollTop()) - chartsTabHeaderTop;
                
                scrolled = true;
                
                $('#study-view-header-function').css({
                    'left': '-13px',
                    'width': '1276px',
                    'z-index': '99',
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
                    'top': '30px',
                    'color': 'white',
                    'background-color': '#2986e2',
                    'opacity': '0.8'
                });
                
                $('#study-view-tutorial').css('display', 'none');
                $('#study-view-add-chart').css('display', 'none');
            }
            if ($(this).scrollTop() < chartsTabHeaderTop){
                scrolled = false;
                
                $('#study-view-header-function').css({
                    'top': '60px',
                    'left': '',
                    'z-index': '',
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
                    'top': '2px',
                    'color': '#2986e2',
                    'background-color': 'white',
                    'opacity': '1'
                });
                
                $('#study-view-tutorial').css('display', 'block');
                $('#study-view-add-chart').css('display', 'block');
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



var StudyViewInitIntroJS = (function() {
    
    function createDiv() {
        
    }
    
    function main() {
        var introguide = introJs();
        
        introguide.start();
        introguide.onbeforechange(function(targetElement) {
            var _currentID = $(targetElement).attr('id').split('-'),
                _currentIDNumber = _currentID[_currentID.length-1];
                
            if(_currentIDNumber === '3'){
                StudyViewInitCharts.filterChartsByGivingIDs(['000236','000380']);
            }else{
                StudyViewInitCharts.filterChartsByGivingIDs([]);
            }
          });
    }
    return {
      init: function() {
          main();
      }  
    };
})();
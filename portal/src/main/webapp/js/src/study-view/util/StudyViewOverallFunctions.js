/* 
 * This class is designed to put all reusable functions.
 */


var StudyViewOverallFunctions = (function(){
    function showHideDivision(_listenedDivID, _targetDivID){
        $("#" + _targetDivID).css('display', 'none');
        $("#" + _listenedDivID).hover(function(){
            $("#" + _targetDivID).stop().fadeIn('slow', function(){
                $(this).css('display', 'block');
            });
        }, function(){
            $("#" + _targetDivID).stop().fadeOut('slow', function(){
                $(this).css('display', 'none');
            });
        });
    }
    
    return{
        showHideDivision: showHideDivision
    };
})();
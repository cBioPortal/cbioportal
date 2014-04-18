/* 
 * This class is designed to put all reusable functions.
 */


var StudyViewUtil = (function(){
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
    
    function echoWarningMessg(_content) {
        console.log("%c Error: "+ _content, "color:red");
    }
    
    //Input: array and delete item index
    //Output: changed array or false if no item found
    function arrayDeleteByIndex(_array, _index){
        if (_index > -1) {
            _array.splice(_index, 1);
            return _array;
        }else {
            return false;
        }
    }
    
    return{
        showHideDivision: showHideDivision,
        echoWarningMessg: echoWarningMessg,
        arrayDeleteByIndex: arrayDeleteByIndex
    };
})();
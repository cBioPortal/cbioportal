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
    
    function hexToRgb(hex) {
        // Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
        var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
        hex = hex.replace(shorthandRegex, function(m, r, g, b) {
            return r + r + g + g + b + b;
        });

        var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
        return result ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16)
        } : null;
    }
    
    function rgbToHex(r, g, b) {
        return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
    }
    
    function rgbStringConvert(string) {
        var array = string.split("(")[1].split(")")[0].split(",");
        var arrayLength = array.length;
        
        for(var i = 0; i < arrayLength; i++){
            array[i] = Number(array[i].trim());
        }
        return array;
    }
    return{
        showHideDivision: showHideDivision,
        echoWarningMessg: echoWarningMessg,
        arrayDeleteByIndex: arrayDeleteByIndex,
        hexToRgb: hexToRgb,
        rgbToHex: rgbToHex,
        rgbStringConvert: rgbStringConvert
    };
})();
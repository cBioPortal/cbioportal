/* 
 * This class is designed to put all reusable functions.
 */


var StudyViewUtil = (function(){
    function showHideDivision(_listenedDiv, _targetDiv, _time, _parentDiv){
        var _anTime = 0;
        
        if(typeof _time === "undefined") {
            _anTime = 300;
        }else {
            _anTime = _time;
        }
        
        $(_targetDiv).css('display', 'none');
        $(_listenedDiv).hover(function(){
            $(_listenedDiv).css('z-index', '1');
            if(typeof _parentDiv !== "undefined"){
                changePosition(_listenedDiv, _targetDiv, _parentDiv);
            }
            
            $(_targetDiv).stop().fadeIn(_anTime, function(){
                $(this).css('display', 'block');
            });
        }, function(){
            $(_listenedDiv).css('z-index', '0');
            $(_targetDiv).stop().fadeOut(_anTime, function(){
                $(this).css('display', 'none');
            });
        });
    }
    
    function changePosition(_listenedDiv, _targetDiv, _parentDiv) {
        var _parentOffset = $(_parentDiv).offset(),
            _parentWidth = $(_parentDiv).width(),
            _targetWidth = $(_targetDiv).width(),
            _listenedOffset = $(_listenedDiv).offset(),
            _listenedWidth = $(_listenedDiv).width();

        if(_listenedWidth +_targetWidth+ _listenedOffset.left - _parentOffset.left > _parentWidth){
            $(_targetDiv).css({
                'left': -_targetWidth-6+'px',
                'border-left-width': '1px',
                'border-right-width': '0',
                'padding': '5px 0 5px 5px'
            });

            $(_targetDiv).children().css({
                'float': 'right',
                'display': 'block',
                'clear': 'right'
            });
        }else{
            $(_targetDiv).css({
                'left': _listenedWidth+'px',
                'border-left-width': '0',
                'border-right-width': '1px',
                'padding': '5px 5px 5px 0'
            });

            $(_targetDiv).children().css({
                'float': 'left',
                'display': 'block',
                'clear': 'left'
            });
        }
    }
    
    function echoWarningMessg(_content) {
        console.log("%c Error: "+ _content, "color:red");
    }
    
    function testM(_message, _title){
        if(typeof _title === 'undefined'){
            console.log(_message);
        }else{
            console.log(_title + ": " + _message);
        }
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
    
    function arrayDeDuplicate(_array) {
        if(_array instanceof Array) {
            _array = _array.concat();
            for(var i=0; i<_array.length; ++i) {
                for(var j=i+1; j<_array.length; ++j) {
                    if(_array[i] === _array[j])
                        _array.splice(j--, 1);
                }
            }
            return _array;
        }else {
            return [];
        }
    }
    function arrayFindByValue(_array, _value){
        if(_array.indexOf(_value) === -1){
            return false;
        }else{
            return true;
        }
    }
    
    //Input: hex value. Exp: #000000
    //Output: rgb values. Exp: 0, 0, 0
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
    
    //Input: rgb values. Exp: 0, 0, 0
    //Output: hex value. Exp: #000000
    function rgbToHex(r, g, b) {
        return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
    }
    
    //Input: rgb string. Exp: Rgb(0, 0, 0)
    //Output: rgb array. Exp: array(0, 0, 0)
    function rgbStringConvert(string) {
        var array = string.split("(")[1].split(")")[0].split(",");
        var arrayLength = array.length;
        
        for(var i = 0; i < arrayLength; i++){
            array[i] = Number(array[i].trim());
        }
        return array;
    }
    
    function changeTitle(_titleDiv, _text, _overrideFlag) {
        if(typeof _overrideFlag === 'undefined' || _overrideFlag) {
            $(_titleDiv).text(_text);
        }else {
            var _tmp = $(_titleDiv).attr('oValue');
            if(!_text || _text === '' || _text === 'false'){
                $(_titleDiv).text(_tmp);
            }else{
                $(_titleDiv).text(_tmp + " (" + _text + ")");
            }
        }
    }
    
    function addQtip(_text, element, position){
        var _qtip = jQuery.extend(true,{},StudyViewBoilerplate.pieLabelQtip);
        
        _qtip.content.text = _text;
        
        if(typeof position !== 'undefined') {
            _qtip.position = position;
        }
        element.qtip(_qtip);
    }
    
    function addCytobandSorting() {
        jQuery.fn.dataTableExt.oSort['cytoband-base-desc'] = function(a,b) {
            return -cytobanBaseSort(a, b);
        };
        jQuery.fn.dataTableExt.oSort['cytoband-base-asc'] = function(a,b) {
            return cytobanBaseSort(a, b)
        };
    }
    
    function cytobanBaseSort(a, b) {
        var _a = a.toString().trim().split('|')[0].replace(/[\s-]+/gm, '').split(/([pq])/gm),
            _b = b.toString().trim().split('|')[0].replace(/[\s-]+/gm, '').split(/([pq])/gm);

        var _al = _a.length,
            _bl = _b.length;

        if(_al > 5) {
            console.log('Unrecognizable cytoband formate: ' + _a);
            return 1;
        }
        if(_bl > 5) {
            console.log('Unrecognizable cytoband formate: ' + _b);
            return -1;
        }

        for(var i=0; i < _al; i++) {
            if (_a[i] === 'p') {
                if(i+1 < _al && !isNaN(_a[i+1])) {
                    _a[i+1] = -Number(_a[i+1]);
                }
            }
        }

        for(var i=0; i < _bl; i++) {
            if (_b[i] === 'p') {
                if(i+1 < _bl && !isNaN(_b[i+1])) {
                    _b[i+1] = -Math.abs(_b[i+1]);
                }
            }
        }

        if(_al <= _bl) {
            return compare(_a, _b, 0);
        }else{
            return -compare(_b, _a, 0);
        }
    }
    
    function compare(a, b, index) {
        //the length a always small b, otherwise switch
        if(a.length > b.length) {
            var _b = $.expand(true, [], b);
            a = b;
            b = _b;
        }
        
        //if two cytobands are in different arms, show p arm first
        if(index-1 >= 0 && a[index-1] !== b[index-1]) {
            if(a[index-1] === 'q') {
                return 1;
            }else {
                return -1;
            }
        }
        
        //if two cytobands are located on different chromosome, sort numerically,
        //X chromosome treated as relativly bigger chromosome
        
        if(index === 0 && a[index] !== b[index]) {
            if(b[index].toString().toLowerCase() === 'x'){
                return -1;
            }else if(a[index].toString().toLowerCase() === 'x'){
                return 1;
            }else {
                return Number(a[index]) < Number(b[index]) ? -1: 1;
            }
        }
        
        if(Number(a[index]) < Number(b[index])) {
            return -1;
        }else if(Number(a[index]) > Number(b[index])){
            return 1;
        }else {
            if(index + 2 < a.length) {
                return compare(a, b, index+2);
            }
        }
    }
    
    return{
        showHideDivision: showHideDivision,
        echoWarningMessg: echoWarningMessg,
        arrayDeleteByIndex: arrayDeleteByIndex,
        hexToRgb: hexToRgb,
        rgbToHex: rgbToHex,
        rgbStringConvert: rgbStringConvert,
        arrayFindByValue: arrayFindByValue,
        changePosition: changePosition,
        testM: testM,
        changeTitle: changeTitle,
        addQtip: addQtip,
        addCytobandSorting: addCytobandSorting,
        arrayDeDuplicate: arrayDeDuplicate
    };
})();
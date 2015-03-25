/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var orData = function() {
    
    var data = {}, retrieved = false;

    return {
        init: function(_param) {
            $.ajax({
                url: "oranalysis.do",
                method: "POST",
                data: _param
            })  
            .done(function(result) {
                result = result.substring(1, result.length - 2); //remove the quote sign
                var _arr = result.substring(1, result.length).split("|");
                $.each(_arr, function(index, str) {
                    var _tmp = str.split(":");
                    data[_tmp[0]] = _tmp[1];
                });
                retrieved = true;
            })
            .fail(function( jqXHR, textStatus ) {
                alert( "Request failed: " + textStatus );
            }); 
        },
        get: function(callback_func, param) { 
            var tmp = setInterval(function () { timer(); }, 1000);
            function timer() {
                if (retrieved) {
                    clearInterval(tmp);
                    callback_func(data, param);
                }
            }
        }
    };

};

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var orData = function(_param) {
    
    var request = $.ajax({
      url: "oranalysis.do",
      method: "POST",
      data: _param
    });    

    request.done(function(result) {
        console.log(result);
    });

    request.fail(function( jqXHR, textStatus ) {
        alert( "Request failed: " + textStatus );
    });        

};


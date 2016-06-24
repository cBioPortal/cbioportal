/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var SearchOutliers = (function () {
    function findExtremes(data) {

        // Copy the values, rather than operating on references to existing values
        var values = [];
        _.each(data, function(item){
            if($.isNumeric(item))
            values.push(Number(item));
        });

        // Then sort
        values.sort(function (a, b) {
            return a - b;
        });
         
        /* Then find a generous IQR. This is generous because if (values.length / 4) 
         * is not an int, then really you should average the two elements on either 
         * side to find q1.
         */
        var q1 = values[Math.floor((values.length / 4))];
        // Likewise for q3. 
        var q3 = values[Math.ceil((values.length * (3 / 4)))];
        var iqr = q3 - q1;

        // Then find min and max values
        var maxValue, minValue;
        if(q3 < 1){
            maxValue = Number((q3 + iqr * 1.5).toFixed(2));
            minValue = Number((q1 - iqr * 1.5).toFixed(2));
        }else{
            maxValue = Math.ceil(q3 + iqr * 1.5);
            minValue = Math.floor(q1 - iqr * 1.5);
        }
        if(minValue < 0)minValue = 0;    
        // Then return
        return [minValue, maxValue];
    }
    return {
        findExtremes: findExtremes
    };
    
})();


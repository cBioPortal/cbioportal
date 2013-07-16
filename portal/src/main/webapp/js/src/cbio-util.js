var cbio = {};

cbio.util = (function() {

    var toPrecision = function(number, precision, threshold) {
        // round to precision significant figures
        // with threshold being the upper bound on the numbers that are
        // rewritten in exponential notation

        if (0.000001 <= number && number < threshold) {
            return number.toExponential(precision);
        }
        
        var ret = number.toPrecision(precision);
        if (ret.indexOf(".")!==-1)
            ret = ret.replace(/\.?0+$/,'');
        
        return ret;
    };

    return {
        toPrecision: toPrecision
    };

})();

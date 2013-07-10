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
    
    var getObjectLength = function(object) {
        var length = 0;

        for (var i in object) {
          if (Object.prototype.hasOwnProperty.call(object, i)){
            length++;
          }
        }
        return length;
    };
    
    var checkNullOrUndefined = function(o) {
        return o === null || typeof o === "undefined";
    };
    
    var arrayToAssociatedArrayIndices = function(arr, offset) {
        if (checkNullOrUndefined(offset)) offset=0;
        var aa = {};
        for (var i=0, n=arr.length; i<n; i++) {
            aa[arr[i]] = i+offset;
        }
        return aa;
    };

    var alterAxesAttrForPDFConverter = function(xAxisGrp, shiftValueOnX, yAxisGrp, shiftValueOnY, rollback)
    {
        if (rollback)
        {
            shiftValueOnX = -1 * shiftValueOnX;
            shiftValueOnY = -1 * shiftValueOnY;
        }

        var xLabels = xAxisGrp
            .selectAll(".tick")
            .selectAll("text");

        var yLabels = yAxisGrp
            .selectAll(".tick")
            .selectAll("text");

        // TODO:
        // shifting axis tick labels a little bit because of
        // a bug in the PDF converter library (this is a hack!)
        var xy = parseInt(xLabels.attr("y"));
        var yy = parseInt(yLabels.attr("y"));

        xLabels.attr("y", xy + shiftValueOnX);
        yLabels.attr("y", yy + shiftValueOnY);
    };

    return {
        toPrecision: toPrecision,
        getObjectLength: getObjectLength,
        checkNullOrUndefined: checkNullOrUndefined,
        arrayToAssociatedArrayIndices: arrayToAssociatedArrayIndices,
        alterAxesAttrForPDFConverter: alterAxesAttrForPDFConverter
    };

})();

if (!Array.prototype.forEach) {
    Array.prototype.forEach = function(fun /*, thisp*/) {
        var len = this.length >>> 0;
        if (typeof fun !== "function") {
            throw new TypeError();
        }

        var thisp = arguments[1];
        for (var i = 0; i < len; i++) {
            if (i in this) {
                fun.call(thisp, this[i], i, this);
            }
        }
    };
}

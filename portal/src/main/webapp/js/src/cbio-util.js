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

	/**
	 * Determines the longest common starting substring
	 * for the given two strings
	 *
	 * @param str1  first string
	 * @param str2  second string
	 * @return {String} longest common starting substring
	 */
	var lcss = function (str1, str2)
	{
		var i = 0;

		while (i < str1.length && i < str2.length)
		{
			if (str1[i] === str2[i])
			{
				i++;
			}
			else
			{
				break;
			}
		}

		return str1.substring(0, i);
	};

    return {
        toPrecision: toPrecision,
        alterAxesAttrForPDFConverter: alterAxesAttrForPDFConverter,
	    lcss : lcss
    };

})();

var cbio = {};

cbio.util = (function() {

    var round_to_2SF = function(number, threshold) {
        // round to 2 significant figures
        // with threshold being the upper bound on the numbers that are
        // rewritten in exponential notation

        var rounded = number.toPrecision(2);

        0.000001 <= rounded && rounded < threshold ?
            rounded = parseFloat(rounded).toExponential() :
            rounded = rounded;

        return rounded;
    };

    return {
        round_to_2SF: round_to_2SF
    };

})();

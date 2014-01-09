var PlotsUtil = (function() {

    function findMax(inputArr) {
        var max = Math.max.apply(Math, inputArr);
        return {
            max: max
        };
    }

    function findMin(inputArr) {
        var min = Math.min.apply(Math, inputArr);
        return {
            min: min
        };
    }

    return {
        findMax: findMax,
        findMin: findMin
    }

}());
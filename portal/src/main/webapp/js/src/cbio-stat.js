if (cbio === undefined) {
    var cbio = {};
}

cbio.stat = (function () {

    /*
     *
     * From TCGA:
     * A z-score for a sample indicates the number of standard deviations away 
     * from the mean of expression in the reference.  The formula is : 
     * z = (expression in tumor sample - mean expression in reference sample) / 
     * standard deviation of expression in reference sample
     * The reference here is all the diploid samples under a study.
     * 
     * @param _ref: mrna expression data of all diploid samples under queried study
     * @param _input: mrna expression data of all queried samples under queried study
     * @return _zscoreArr: array of zscore of the _input array
     * 
     */
    function zscore(_ref, _input) {

        // mean (µ)
        var _refSum = 0;
        _.each(_ref, function (_num) {
            _refSum += _num;
        });
        var _mean = _refSum / _ref.length;

        // standard deviation (σ)
        var _squaredRef = _.map(_ref, function (_num) {
            return Math.pow((_num - _mean), 2);
        });
        var _squaredRefSum = 0;
        _.each(_squaredRef, function (_num_stDev) {
            _squaredRefSum += _num_stDev;
        });
        var _stDev = Math.sqrt(_squaredRefSum / _ref.length - 1);

        // z = (x - u) / σ
        var _zscoreArr = [];
        _.each(_input, function (_num) {
            _zscoreArr.push((_num - _mean) / _stDev);
        });

        return _zscoreArr;
    }

    return {
        zscore: zscore
    };

})();


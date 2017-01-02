/*
 *  cbio statistics library
 */

if (cbio === undefined) {
    var cbio = {};
}

cbio.stat = (function() {
    
    // mean (µ)
    var mean = function(_inputArr) {
        var _sum = _.reduce(_inputArr, function(memo, num){ return memo + num; }, 0);
        return _sum / _inputArr.length;
    }

    // standard deviation (σ)
    var stDev = function(_inputArr) {
        var _mean = mean(_inputArr);
        var _squaredRef = _.map(_inputArr, function (_inputElem) {
            return Math.pow((_inputElem - _mean), 2);
        });
        var _squaredRefSum = 0;
        _.each(_squaredRef, function (_num_stDev) {
            _squaredRefSum += _num_stDev;
        });
        return Math.sqrt(_squaredRefSum / _inputArr.length - 1);
    }

    // z score
    /*
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
     */
    var zscore = function(_refArr, _inputArr) {
        var _refMean = mean(_refArr);
        var _refStDev = stDev(_refArr);

        // z = (x - u) / σ
        var _zscoreArr = [];
        _.each(_inputArr, function (_inputElem) {
            _zscoreArr.push((_inputElem - _refMean) / _refStDev);
        });

        return _zscoreArr;
    }
    
    return {
        mean: mean,
        stDev: stDev,
        zscore: zscore
    }
    
}());

if (typeof module !== 'undefined'){
    module.exports = cbio.stat;
}

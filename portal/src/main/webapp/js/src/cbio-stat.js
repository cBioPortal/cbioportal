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
    
    /**
     * Executes the clustering of casesAndEntitites in the requested 
     * dimension (CASES or ENTITIES).
     * 
     * @param casesAndEntitites: Object with sample(or patient)Id and map 
     * of geneticEntity/value pairs. Example:
     *  
     * var a =
     *  {
     *    "TCGA-AO-AA98-01":
     *    {
     *    	"TP53": 0.045,
     *    	"BRA1": -0.89
     *    }
     *   },
     *   ...
     *   
     * @return a deferred which gets resolved with the clustering result
     *   when the clustering is done. 
     */
    var _hcluster = function(casesAndEntitites, dimension) {
    	var worker = new Worker("js/src/clustering/clustering-worker.js");
    	var message = new Object();
    	var def = new $.Deferred(); 
    	message.casesAndEntitites = casesAndEntitites;
    	message.dimension = dimension;
    	worker.postMessage(message);
    	worker.onmessage = function(m) {
    		def.resolve(m.data);
    	}
    	return def.promise();
    }
    
    /**
     * Use: cbio.stat.hclusterCases(a);

     * @return a deferred which gets resolved with the clustering result
     *   when the clustering is done. 
     */
    var hclusterCases = function(casesAndEntitites) {
    	return _hcluster(casesAndEntitites, "CASES");
    }
    
    /**
     * Use: hclusterGeneticEntities(a);
     * 
     * @return a deferred which gets resolved with the clustering result
     *   when the clustering is done. 
     */
    var hclusterGeneticEntities = function(casesAndEntitites) {
    	return _hcluster(casesAndEntitites, "ENTITIES");
    }
  	
    return {
        mean: mean,
        stDev: stDev,
        zscore: zscore,
        hclusterCases: hclusterCases,
        hclusterGeneticEntities: hclusterGeneticEntities
    }
    
}());

if (typeof module !== 'undefined'){
    module.exports = cbio.stat;
}

var CoexpPlotsProxy = (function() {

    var dataArr = [],
        attr = {};

    function init(result, geneX, geneY) {
        convertData(result, geneX, geneY);
        analyseData();
    }

    function getData() {
        return dataArr;
    }

    function convertData(result, geneX, geneY) {
        var geneXArr = result[geneX];
        var geneYArr = result[geneY];
        $.each(geneXArr, function(index) {
            var datum = jQuery.extend(true, {}, PlotsBoilerplate.getDatum());
            var _obj_x = geneXArr[index];
            var _obj_y = geneYArr[index];
            datum.x_val = _obj_x["value"];
            datum.y_val = _obj_y["value"];
            datum.case_id = _obj_x["caseId"];
            dataArr.push(datum);
        });
    }

    function analyseData() {
        PlotsUtil.analyseData(dataArr);
    }

    return {
        init: init,
        getData: getData
    }

}());
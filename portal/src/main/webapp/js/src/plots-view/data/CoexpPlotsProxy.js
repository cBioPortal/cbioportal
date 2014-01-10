var CoexpPlotsProxy = (function() {

    var dataArr = [],
        attr = {
            min_x: "",
            max_x: "",
            min_y: "",
            max_y: ""
        };

    function convertData(result, geneX, geneY) {
        var geneXArr = result[geneX];
        var geneYArr = result[geneY];
        $.each(geneXArr, function(index) {
            var datum = jQuery.extend(true, {}, PlotsBoilerplate.datum);
            var _obj_x = geneXArr[index];
            var _obj_y = geneYArr[index];
            datum.x_val = _obj_x["value"];
            datum.y_val = _obj_y["value"];
            datum.case_id = _obj_x["caseId"];
            datum.qtip = "something qtip";
            dataArr.push(datum);
        });
    }

    function analyseData() {
        var _yValArr = [];
        var _xValArr = [];
        $.each(dataArr, function(index, val){
            _xValArr.push(val.x_val);
            _yValArr.push(val.y_val);
        });
        attr.min_x = Math.min.apply(Math, _xValArr);
        attr.max_x = Math.max.apply(Math, _xValArr);
        attr.min_y = Math.min.apply(Math, _yValArr);
        attr.max_y = Math.max.apply(Math, _yValArr);
    }

    return {
        init: function(result, geneX, geneY) {
            convertData(result, geneX, geneY);
            analyseData();
        },
        getData: function() { return dataArr; },
        getDataAttr: function() { return attr; }
    }

}());
var CoexpPlotsProxy = (function() {

    var dataArr = [],
        attr = {
            min_x: "",
            max_x: "",
            min_y: "",
            max_y: "",
            profile_name: ""
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
            datum.qtip = "Case ID : <strong><a href='tumormap.do?case_id=" + 
                         _obj_x["caseId"] + "&cancer_study_id=" +
                         window.PortalGlobals.getCancerStudyId() + "' target='_blank'>" + 
                         _obj_x["caseId"] + "</a></strong><br>" + 
                         geneX + " : <strong>" + parseFloat(_obj_x["value"]).toFixed(3) + "</strong><br>" +
                         geneY + " : <strong>" + parseFloat(_obj_y["value"]).toFixed(3) + "</strong>";
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

    function getProfile(_result) {
        for (var prop in _result) {
            var _tmp = _result[prop];
            attr.profile_name = _tmp[0].profile;
            return;
        }
    }

    return {
        init: function(result, geneX, geneY) {
            dataArr.length = 0;
            convertData(result, geneX, geneY);
            analyseData();
            getProfile(result);
        },
        getData: function() { return dataArr; },
        getDataAttr: function() { return attr; }
    }

}());
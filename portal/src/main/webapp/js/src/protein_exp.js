var rppaPlots = (function() {

    var data = (function() {

        var alteredCaseList =  [],
            unalteredCaseList =  [];

        function init(caseLists, proteinArrayId) {
            alteredCaseList = caseLists.alteredCaseList;
            unalteredCaseList = caseLists.unalteredCaseList;
            getProteinArrayData(proteinArrayId);

        }

        function getProteinArrayData(proteinArrayId) {
            var paramsGetProteinArrayData = {
                cancer_study_id: cancer_study_id,
                case_set_id: case_set_id,
                case_ids_key: case_ids_key,
                protein_array_id: proteinArrayId
            };
            $.post("getProteinArrayData.json", paramsGetProteinArrayData, getProfileDataCallBack, "json");
        }

        function getProfileDataCallBack(result) {
        }

        return {
            init: init,
        }

    }());

    var view = (function() {

        var xLabel = "",
            yLabel = "",
            title = "",
            divName = "";

        function init(_xLabel, _yLabel, _title, _divName) {
            xLabel = _xLabel;
            yLabel = _yLabel;
            title = _title;
            divName = _divName;
            $("#" + divName).append(title);
        }

        return {
            init: init,
        }

    }());



    var util = (function() {

    }());

    function init(xLabel, yLabel, title, caseLists, divName, proteinArrayId) {
        data.init(caseLists, proteinArrayId);
        view.init(xLabel, yLabel, title, divName);
    }

    return {
        init: init,
    }

}());
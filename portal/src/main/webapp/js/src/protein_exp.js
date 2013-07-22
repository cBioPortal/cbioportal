var rppaPlots = (function() {

    var data = (function() {

        var alteredCaseList =  [],
            unalteredCaseList =  [];

        function init(caseLists) {
            alteredCaseList = caseLists.alteredCaseList;
            unalteredCaseList = caseLists.unalteredCaseList;
            getProfileData();

        }

        function getProfileData() {
            var paramsGetProfileData = {
                gene_list: gene,
                genetic_profile_id: genetic_profile_id,
                case_set_id: case_set_id,
                case_ids_key: case_ids_key
            };
            $.post("getProfileData.json", paramsGetProfileData, callback_func, "json");
        }

        function getProfileDataCallBack() {

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
            console.log(xLabel, yLabel, title);
            console.log(divName);
            $("#" + divName).append(title);
        }

        return {
            init: init,
        }

    }());



    var util = (function() {

    }());

    function init(xLabel, yLabel, title, caseLists, divName) {
        data.init(caseLists);
        view.init(xLabel, yLabel, title, divName);
    }

    return {
        init: init,
    }

}());
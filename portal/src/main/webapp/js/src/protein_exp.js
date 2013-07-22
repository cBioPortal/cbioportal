var rppaPlots = (function() {

    var data = (function() {

        var alteredCaseList =  [],
            unalteredCaseList =  [];

        function init(caseLists) {
            alteredCaseList = caseLists.alteredCaseList;
            unalteredCaseList = caseLists.unalteredCaseList;
            console.log(alteredCaseList);
            console.log(unalteredCaseList);
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
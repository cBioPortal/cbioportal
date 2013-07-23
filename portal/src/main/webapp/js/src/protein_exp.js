var rppaPlots = (function() {

    var data = (function() {

        var alteredCaseList =  [],
            unalteredCaseList =  [],
            alteredCases = [],
            unalteredCases = [];

        function setArrayData(proteinArrayData) {
            console.log(Object.keys(proteinArrayData).length);
            for (var key in proteinArrayData) {
                console.log(key);
                if (proteinArrayData.hasOwnProperty(key)) {
                    var _tmp = {
                        "caseId": key,
                        "value": proteinArrayData[key]
                    };
                    if (alteredCaseList.indexOf(key) !== -1) {
                        alteredCases.push(_tmp);
                    } else {
                        unalteredCases.push(_tmp);
                    }
                }
            }
            console.log("ALTERED:");
            console.log(alteredCases);
            console.log("UNALTERED:");
            console.log(unalteredCases);

        }

        function init(proteinArrayDAta) {
            setArrayData(proteinArrayDAta)
        }

        return {
            setCaseLists: function(caseLists) {
                alteredCaseList = caseLists.alteredCaseList;
                unalteredCaseList = caseLists.unalteredCaseList;
            },
            init: init,
            getAlteredCases: function() {
                return alteredCases;
            },
            getUnAlteredCases: function() {
                return unalteredCases;
            }
        }

    }());

    var view = (function() {

        var xLabel = "",
            yLabel = "",
            title = "",
            divName = "",
            singleDot = {
                xVal: "",  //0 --> altered; 1 --> unaltered
                yVal: "",
                caseId: ""
            },
            dotsGroup = [];

        function pDataInit() {
            $.each(data.getAlteredCases(), function(index, val){
                var _singleDot = jQuery.extend(true, {}, singleDot);
                _singleDot.xVal = 0;
                _singleDot.yVal = val.value;
                _singleDot.caseId = val.caseId;
                dotsGroup.push(_singleDot);
            });
            $.each(data.getUnAlteredCases(), function(index, val){
                var _singleDot = jQuery.extend(true, {}, singleDot);
                _singleDot.xVal = 1;
                _singleDot.yVal = val.value;
                _singleDot.caseId = val.caseId;
                dotsGroup.push(_singleDot);
            });
        }

        function drawAxis() {
        }

        function drawPlots() {
        }

        function drawLegends() {
        }

        function init() {
            pDataInit();
            drawAxis();
            drawPlots();
            drawLegends();
        }

        return {
            setAttr: function(_xLabel, _yLabel, _title, _divName) {
                xLabel = _xLabel;
                yLabel = _yLabel;
                title = _title;
                divName = _divName;
            },
            init: init
        }

    }());

    var util = (function() {

    }());

    function generatePlots(proteinArrayId) {
        var paramsGetProteinArrayData = {
            cancer_study_id: cancer_study_id,
            case_set_id: case_set_id,
            case_ids_key: case_ids_key,
            protein_array_id: proteinArrayId
        };
        $.post("getProteinArrayData.json", paramsGetProteinArrayData, getProfileDataCallBack, "json");
    }

    function getProfileDataCallBack(result) {
        data.init(result);
        view.init();

    }


    return {
        init: function(xLabel, yLabel, title, divName, caseLists, proteinArrayId) {
            //Set all the parameters
            data.setCaseLists(caseLists);
            view.setAttr(xLabel, yLabel, title, divName);
            //Get data from server and drawing
            generatePlots(proteinArrayId);
        },
    }

}());
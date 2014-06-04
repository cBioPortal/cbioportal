var SurvivalCurveProxy  = function() {

        var datum = {
                case_id: "",
                time: "",    //num of months
                status: "", //os: DECEASED-->1, LIVING-->0; dfs: Recurred/Progressed --> 1, Disease Free-->0
                num_at_risk: -1,
                survival_rate: 0
            },
            datumArr = [],
            statDatum = {
                pVal: 0,
                num_cases: 0,
                num_of_events_cases: 0,
                median: 0
            },
            statValues = {},
            kmEstimator = "",
            logRankTest = "",
            totalNum = 0,
            caseList = [];

        //First Configure datums by combining AJAX result with caselist 
        //Then order by time, filtered NA cases and add on num of risk for each time point
        function assembleDatums(_result) {
            var _totalNum = 0;
            //Get event and time
            for (var caseId in _result) {
                if (_result.hasOwnProperty(caseId) && _result[caseId] !== "" && caseList.indexOf(caseId) !== -1) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.case_id = _result[caseId].case_id;
                    _datum.time = _result[caseId].months;
                    _datum.status = _result[caseId].status;
                    if (_datum.time !== "NA" && _datum.status !== "NA") {
                        datumArr.push(_datum);
                        _totalNum += 1;
                    }
                }
            }
            //Sort by time
            cbio.util.sortByAttribute(datumArr, "time");
            //Set num at risk
            for (var i in datumArr) {
                datumArr[i].num_at_risk = _totalNum;
                _totalNum += -1;
            }
        }

        function calc() {
            kmEstimator.calc(datumArr);
            var _statDatum = jQuery.extend(true, {}, statDatum);
            if (datumArr.length !== 0) {
                _statDatum.num_cases = datumArr.length;
            } else {
                _statDatum.num_cases = "NA";
            }
            _statDatum.num_of_events_cases = countEvents(datumArr);
            _statDatum.median = calcMedian(datumArr);
            statValues = _statDatum;
            //logRankTest.calc(altered_group, unaltered_group, _callBackFunc);
        }

        function countEvents(inputArr) {
            if (inputArr.length !== 0) {
                var _cnt = 0;
                for (var i in inputArr) {
                    if (inputArr[i].status === "1") {
                        _cnt += 1;
                    }
                }
                return _cnt;
            }
            return "NA";
        }

        function calcMedian(inputArr) {
            if (inputArr.length !== 0) {
                var _mIndex = 0;
                for (var i in inputArr) {
                    if (inputArr[i].survival_rate <= 0.5) {
                        _mIndex = i;
                        break;
                    } else {
                        continue;
                    }
                }
                return parseFloat(inputArr[_mIndex].time).toFixed(2);
            }
            return "NA";
        }

        return {
            init: function(_result, _caseList, _kmEstimator, _logRankTest) {
                caseList = _caseList;
                kmEstimator = _kmEstimator;
                logRankTest = _logRankTest;
                assembleDatums(_result);
                if (datumArr.length === 0) {
                    //$('#tab-survival').hide();
                    // var tab = $('#tabs a').filter(function(){
                    //     return $(this).text() == "Survival";
                    // }).parent();
                    // tab.hide();
                } else {
                    if (datumArr.length !== 0) {
                        calc();
                    } else {
                        //view.errorMsg("os");
                    }
                }
            },
            getData: function() {
                return datumArr;
            },
            getStats: function() {
                return statValues;
            }
        }
    };
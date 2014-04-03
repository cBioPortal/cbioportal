var SurvivalCurveProxy  = function() {

        var datum = {
                case_id: "",
                time: "",    //num of months
                status: "", //os: DECEASED-->1, LIVING-->0; dfs: Recurred/Progressed --> 1, Disease Free-->0
                num_at_risk: -1,
                survival_rate: 0
            },
            altered_group = [],
            unaltered_group = [],
            stat_datum = {
                pVal: 0,
                num_altered_cases: 0,
                num_unaltered_cases: 0,
                num_of_events_altered_cases: 0,
                num_of_events_unaltered_cases: 0,
                altered_median: 0,
                unaltered_median: 0
            },
            stat_values = {},
            kmEstimator = "",
            logRankTest = "";

        var totalAlter = 0,
            totalUnalter = 0;

        //Count the total number of altered and unaltered cases
        function cntAlter(caseLists) {
            for (var key in caseLists) {
                if (caseLists[key] === "altered") totalAlter += 1;
                else if (caseLists[key] === "unaltered") totalUnalter += 1;
            }
        }

        //Settle the overall survival datum group
        //order by time, filtered NA cases and add on num of risk for each time point
        function setGroups(result, caseLists) {
            var _totalAlter = 0,
                _totalUnalter = 0;

            for (var caseId in result) {
                if (result.hasOwnProperty(caseId) && (result[caseId] !== "")) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.case_id = result[caseId].case_id;
                    _datum.time = result[caseId].months;
                    _datum.status = result[caseId].status;
                    if (_datum.time !== "NA" && _datum.status !== "NA") {
                        if (caseLists[caseId] === "altered") {
                            altered_group.push(_datum);
                            _totalAlter += 1;
                        } else if (caseLists[caseId] === "unaltered") {
                            unaltered_group.push(_datum);
                            _totalUnalter += 1;
                        }
                    }
                }
            }
            cbio.util.sortByAttribute(altered_group, "time");
            cbio.util.sortByAttribute(unaltered_group, "time");

            for (var i in altered_group) {
                altered_group[i].num_at_risk = _totalAlter;
                _totalAlter += -1;
            }
            for (var i in unaltered_group) {
                unaltered_group[i].num_at_risk = _totalUnalter;
                _totalUnalter += -1;
            }

        }

        function calc(_callBackFunc) {
            kmEstimator.calc(altered_group);
            kmEstimator.calc(unaltered_group);
            var _stat_datum = jQuery.extend(true, {}, stat_datum);
            if (altered_group.length !== 0) {
                _stat_datum.num_altered_cases = altered_group.length;
            } else {
                _stat_datum.num_altered_cases = "NA";
            }
            if (unaltered_group.length !== 0) {
                _stat_datum.num_unaltered_cases = unaltered_group.length;
            } else {
                _stat_datum.num_unaltered_cases = "NA";
            }
            _stat_datum.num_of_events_altered_cases = countEvents(altered_group);
            _stat_datum.num_of_events_unaltered_cases = countEvents(unaltered_group);
            _stat_datum.altered_median = calcMedian(altered_group);
            _stat_datum.unaltered_median = calcMedian(unaltered_group);
            stat_values = _stat_datum;
            logRankTest.calc(altered_group, unaltered_group, _callBackFunc);
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
            init: function(result, caseLists, _kmEstimator, _logRankTest, _pValCallbackFunc) {
                kmEstimator = _kmEstimator;
                logRankTest = _logRankTest;
                cntAlter(caseLists);
                setGroups(result, caseLists);
                if (altered_group.length === 0 && unaltered_group.length === 0) {
                    //$('#tab-survival').hide();
                    // var tab = $('#tabs a').filter(function(){
                    //     return $(this).text() == "Survival";
                    // }).parent();
                    // tab.hide();
                } else {
                    if (altered_group.length !== 0 || unaltered_group.length !== 0) {
                        calc(_pValCallbackFunc);
                    } else {
                        //view.errorMsg("os");
                    }
                }
            },
            getAlteredData: function() {
                return altered_group;
            },
            getUnalteredData: function() {
                return unaltered_group;
            },
            getStats: function() {
                return stat_values;
            }
        }
    };
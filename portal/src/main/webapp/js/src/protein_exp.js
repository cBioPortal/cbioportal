var rppaPlots = (function() {

    var data = (function() {

        var alteredCaseList =  [],
            unalteredCaseList =  [],
            alteredCases = [],
            unalteredCases = [];

        function setArrayData(proteinArrayData) {
            alteredCases.length = 0;
            unalteredCases.length = 0;
            for (var key in proteinArrayData) {
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
        }

        function init(proteinArrayDAta) {
            setArrayData(proteinArrayDAta)
        }

        return {
            setCaseLists: function(caseLists) {
                alteredCaseList.length = 0;
                unalteredCaseList.length = 0;
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
            xAxisTextSet = ["Altered", "Unaltered"],
            singleDot = {
                xVal: "",  //0 --> altered; 1 --> unaltered
                yVal: "",
                caseId: ""
            },
            dotsArr = [],
            elem = {
                svg : "",
                xScale : "",
                yScale : "",
                xAxis : "",
                yAxis : "",
                dotsGroup : "",   //Group of single Dots
            },
            settings = {
                canvas_width: 720,
                canvas_height: 600
            };


        function pDataInit() {
            dotsArr.length = 0;
            $.each(data.getAlteredCases(), function(index, val){
                var _singleDot = jQuery.extend(true, {}, singleDot);
                _singleDot.xVal = 0;
                _singleDot.yVal = val.value;
                _singleDot.caseId = val.caseId;
                dotsArr.push(_singleDot);
            });
            $.each(data.getUnAlteredCases(), function(index, val){
                var _singleDot = jQuery.extend(true, {}, singleDot);
                _singleDot.xVal = 1;
                _singleDot.yVal = val.value;
                _singleDot.caseId = val.caseId;
                dotsArr.push(_singleDot);
            });
            console.log(dotsArr.length);
        }

        function initCanvas() {
            elem.svg = d3.select("#" + divName)
                .append("svg")
                .attr("width", settings.canvas_width)
                .attr("height", settings.canvas_height);
        }

        function drawAxis() {
            //Init
            elem.xScale = d3.scale.linear()
                .domain([-0.7, 1.7])
                .range([100, 600]);

            var _yValArr = [];
            $.each(dotsArr, function(index, val){
                _yValArr.push(val.yVal);
            });
            var _results = util.analyseData(_yValArr);
            elem.yScale = d3.scale.linear()
                .domain([_results.min, _results.max])
                .range([520, 20]);
            elem.xAxis = d3.svg.axis()
                .scale(elem.xScale)
                .orient("bottom")
            elem.yAxis = d3.svg.axis()
                .scale(elem.yScale)
                .orient("left");
            //Draw
            elem.svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(0, 520)")
                .attr("class", "rppa-plots-x-axis-class")
                .call(elem.xAxis.ticks(xAxisTextSet.length))
                .selectAll("text")
                .data(xAxisTextSet)
                .style("font-family", "sans-serif")
                .style("font-size", "13px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black")
                .text(function(d){return d});
            elem.svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(0, 20)")
                .call(elem.xAxis.orient("bottom").ticks(0));
            elem.svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(100, 0)")
                .attr("class", "rppa-plots-y-axis-class")
                .call(elem.yAxis)
                .selectAll("text")
                .style("font-family", "sans-serif")
                .style("font-size", "13px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black");
            elem.svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(600, 0)")
                .call(elem.yAxis.orient("left").ticks(0));
            //Append Axis Titles
            var axisTitleGroup = elem.svg.append("svg:g");
            axisTitleGroup.append("text")
                .attr("class", "rppa-plots-x-axis-title")
                .attr("x", 350)
                .attr("y", 580)
                .style("text-anchor", "middle")
                .style("font-size", "13px")
                .text(xLabel);
            axisTitleGroup.append("text")
                .attr("class", "rppa-plots-y-axis-title")
                .attr("transform", "rotate(-90)")
                .attr("x", -270)
                .attr("y", 45)
                .style("text-anchor", "middle")
                .style("font-size", "13px")
                .text(yLabel);
        }

        function drawPlots() {
            elem.dotsGroup = elem.svg.append("svg:g");
            elem.dotsGroup.selectAll("path").remove();
            var ramRatio = 80;  //Noise
            elem.dotsGroup.selectAll("path")
                .data(dotsArr)
                .enter()
                .append("svg:path")
                .attr("transform", function(d){
                    return "translate(" + (elem.xScale(d.xVal) + (Math.random() * ramRatio - ramRatio/2)) + ", " + elem.yScale(d.yVal) + ")";
                })
                .attr("d", d3.svg.symbol()
                    .size(20)
                    .type("circle"))
                .attr("fill", "blue")
                .attr("stroke", "black")
                .attr("stroke-width", "1.2");
        }

        function appendHeader() {
            $("#" + divName).empty();
            $("#" + divName).append(title);
        }

        function drawBoxPlot() {
            var boxPlotsElem = elem.svg.append("svg:g");
            for (var i = 0 ; i < 2; i++) {  //Just 0(altered) and 1(unaltered)
                var top;
                var bottom;
                var quan1;
                var quan2;
                var mean;
                var IQR;
                var scaled_y_arr=[];
                var tmp_y_arr = [];
                //Find the middle (vertical) line for one box plot
                var midLine = elem.xScale(i);
                //Find the max/min y value with certain x value;
                $.each(dotsArr, function(index, value) {
                    if (value.xVal === i) {
                        tmp_y_arr.push(parseFloat(value.yVal));
                    }
                });
                tmp_y_arr.sort(function(a, b) { return a - b });
                //Deal with individual data sub group
                if (tmp_y_arr.length === 0) {
                    //Skip: do nothing
                } else if (tmp_y_arr.length === 1) {
                    mean = elem.yScale(tmp_y_arr[0]);
                    boxPlotsElem.append("line")
                        .attr("x1", midLine-30)
                        .attr("x2", midLine+30)
                        .attr("y1", mean)
                        .attr("y2", mean)
                        .attr("stroke-width", 1)
                        .attr("stroke", "grey");
                } else {
                    if (tmp_y_arr.length === 2) {
                        mean = elem.yScale((tmp_y_arr[0] + tmp_y_arr[1]) / 2);
                        quan1 = bottom = elem.yScale(tmp_y_arr[0]);
                        quan2 = top = elem.yScale(tmp_y_arr[1]);
                        IQR = Math.abs(quan2 - quan1);
                    } else {
                        var yl = tmp_y_arr.length;
                        if (yl % 2 === 0) {
                            mean = elem.yScale((tmp_y_arr[(yl / 2)-1] + tmp_y_arr[yl / 2]) / 2);
                            if (yl % 4 === 0) {
                                quan1 = elem.yScale((tmp_y_arr[(yl / 4)-1] + tmp_y_arr[yl / 4]) / 2);
                                quan2 = elem.yScale((tmp_y_arr[(3*yl / 4)-1] + tmp_y_arr[3 * yl / 4]) / 2);
                            } else {
                                quan1 = elem.yScale(tmp_y_arr[Math.floor(yl / 4)]);
                                quan2 = elem.yScale(tmp_y_arr[Math.floor(3 * yl / 4)]);
                            }
                        } else {
                            mean = elem.yScale(tmp_y_arr[Math.floor(yl / 2)]);
                            var tmp_yl = Math.floor(yl / 2) + 1;
                            if (tmp_yl % 2 === 0) {
                                quan1 = elem.yScale((tmp_y_arr[tmp_yl / 2 - 1] + tmp_y_arr[tmp_yl / 2]) / 2);
                                quan2 = elem.yScale((tmp_y_arr[(3 * tmp_yl / 2) - 2] + tmp_y_arr[(3 * tmp_yl / 2) - 1]) / 2);
                            } else {
                                quan1 = elem.yScale(tmp_y_arr[Math.floor(tmp_yl / 2)]);
                                quan2 = elem.yScale(tmp_y_arr[tmp_yl - 1 + Math.floor(tmp_yl / 2)]);
                            }
                        }
                        for (var k = 0 ; k < tmp_y_arr.length ; k++) {
                            scaled_y_arr[k] = parseFloat(elem.yScale(tmp_y_arr[k]));
                        }
                        scaled_y_arr.sort(function(a,b) { return a-b });
                        IQR = Math.abs(quan2 - quan1);
                        var index_top = util.searchIndexTop(scaled_y_arr, (quan2 - 1.5 * IQR));
                        top = scaled_y_arr[index_top];
                        var index_bottom = util.searchIndexBottom(scaled_y_arr, (quan1 + 1.5 * IQR));
                        bottom = scaled_y_arr[index_bottom];
                    }

                    //D3 Drawing
                    boxPlotsElem.append("rect")
                        .attr("x", midLine-40)
                        .attr("y", quan2)
                        .attr("width", 80)
                        .attr("height", IQR)
                        .attr("fill", "none")
                        .attr("stroke-width", 1)
                        .attr("stroke", "#BDBDBD");
                    boxPlotsElem.append("line")
                        .attr("x1", midLine-40)
                        .attr("x2", midLine+40)
                        .attr("y1", mean)
                        .attr("y2", mean)
                        .attr("stroke-width", 1)
                        .attr("stroke", "#BDBDBD");
                    boxPlotsElem.append("line")
                        .attr("x1", midLine-30)
                        .attr("x2", midLine+30)
                        .attr("y1", top)
                        .attr("y2", top)
                        .attr("stroke-width", 1)
                        .attr("stroke", "#BDBDBD");
                    boxPlotsElem.append("line")
                        .attr("x1", midLine-30)
                        .attr("x2", midLine+30)
                        .attr("y1", bottom)
                        .attr("y2", bottom)
                        .attr("stroke", "#BDBDBD")
                        .style("stroke-width", 1);
                    boxPlotsElem.append("line")
                        .attr("x1", midLine)
                        .attr("x2", midLine)
                        .attr("y1", quan1)
                        .attr("y2", bottom)
                        .attr("stroke", "#BDBDBD")
                        .attr("stroke-width", 1);
                    boxPlotsElem.append("line")
                        .attr("x1", midLine)
                        .attr("x2", midLine)
                        .attr("y1", quan2)
                        .attr("y2", top)
                        .attr("stroke", "#BDBDBD")
                        .style("stroke-width", 1);
                }
            }
        }

        function addQtips() {
            elem.dotsGroup.selectAll('path').each(
                function(d) {
                    var content = "<font size='2'>";
                    content += "Case ID: " + "<strong><a href='tumormap.do?case_id=" + d.caseId +
                        "&cancer_study_id=" + cancer_study_id + "'>" + d.caseId + "</a></strong><br>";
                    content += "RPPA score: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>"
                    content = content + "</font>";

                    $(this).qtip(
                        {
                            content: {text: content},
                            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                            hide: { fixed:true, delay: 100},
                            position: {my:'left bottom',at:'top right'}
                        }
                    );

                    var mouseOn = function() {
                        var dot = d3.select(this);
                        dot.transition()
                            .ease("elastic")
                            .duration(600)
                            .delay(100)
                            .attr("d", d3.svg.symbol().size(200).type("circle"));
                    };

                    var mouseOff = function() {
                        var dot = d3.select(this);
                        dot.transition()
                            .ease("elastic")//TODO: default d3 symbol is circle (coincidence!)
                            .duration(600)
                            .delay(100)
                            .attr("d", d3.svg.symbol().size(20).type("circle"));
                    };
                    elem.dotsGroup.selectAll("path").on("mouseover", mouseOn);
                    elem.dotsGroup.selectAll("path").on("mouseout", mouseOff);
                }
            );
        }

        function init() {
            pDataInit();
            appendHeader();
            initCanvas();
            drawAxis();
            drawBoxPlot();
            drawPlots();
            addQtips();
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

        function analyseData(inputArr) {    //pDataX, pDataY: array of single dot objects
            var min = Math.min.apply(Math, inputArr);
            var max = Math.max.apply(Math, inputArr);
            var edge = (max - min) * 0.1;
            min -= edge;
            max += edge;

            return {
                min: min,
                max: max
            };
        }

        function searchIndexBottom(arr, ele) {
            for(var i = 0; i < arr.length; i++) {
                if (parseFloat(ele) > parseFloat(arr[i])) {
                    continue ;
                } else if (parseFloat(ele) == parseFloat(arr[i])) {
                    return i;
                } else {
                    return i - 1;
                }
            }
            return arr.length - 1 ;
        }

        function searchIndexTop(arr, ele) {
            for(var i = 0; i < arr.length; i++) {
                if (ele <= arr[i]) {
                    return i;
                } else {
                    continue;
                }
            }
            return arr.length - 1;
        }

        return {
            analyseData: analyseData,
            searchIndexBottom: searchIndexBottom,
            searchIndexTop: searchIndexTop
        }

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
var SurvivalCurve = function() {

    //Instances
    var elem = "",
        settings = "",
        text = "",
        style = "",
        divs = "",
        vals = "";
    
    //Each curve will have unique ID which will be used to add/remove curve
    var curvesInfo = {};
    
    function initCanvas() {
        $('#' + divs.curveDivId).empty();
        elem.svg = d3.select("#" + divs.curveDivId)
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);
        elem.curve = elem.svg.append("g");
        //elem.dots = elem.svg.append("g"); //the invisible dots
        elem.censoredDots = elem.svg.append("g").attr("id", "crossDots");
    }

    function initAxis(_inputArr) {
        var _dataset = [];
        //tick format changed from .1% to %, requested by Niki, used in
        //Study View
        var formatAsPercentage = d3.format("%");
        $.each(_inputArr, function(index, obj) {
            var data = obj.data;
            $.each(data.getData(), function(index, d) {
                _dataset.push(d.time);
            });
        });
        elem.xScale = d3.scale.linear()
            .domain([0, d3.max(_dataset) + 0.1 * d3.max(_dataset)])
            .range([settings.chart_left, settings.chart_left + settings.chart_width]);
        elem.yScale = d3.scale.linear()
            .domain([-0.03, 1.05]) //fixed to be 0-1
            .range([settings.chart_top + settings.chart_height, settings.chart_top]);
        elem.xAxis = d3.svg.axis()
            .scale(elem.xScale)
            .orient("bottom")
            .tickSize(6, 0, 0);
        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .tickFormat(formatAsPercentage)
            .orient("left")
            .tickSize(6, 0, 0);
    }

    function initLines() {
        elem.line = d3.svg.line()
            .interpolate("step-after")
            .x(function(d) { return elem.xScale(d.time); })
            .y(function(d) { return elem.yScale(d.survival_rate); });
    }

    //Append a virtual point for time zero if needed (no actual data point at time zero, therefore cause the graph not starting from 0)
    function appendZeroPoint(_num_at_risk) {
        var datum = {
            case_id: "",
            time: "",    //num of months
            status: "", //os: DECEASED-->1, LIVING-->0; dfs: Recurred/Progressed --> 1, Disease Free-->0
            num_at_risk: -1,
            survival_rate: 0
        };
        var _datum = jQuery.extend(true, {}, datum);
        _datum.case_id = "NA";
        _datum.time = 0;
        _datum.status = "NA";
        _datum.num_at_risk = _num_at_risk;
        _datum.survival_rate = 1;
        return _datum;
    }

    function drawLines(data, opts, _curveId) {
        if (data !== null && data.length > 0) {
            if (data[0].time !== 0) {
                data.unshift(appendZeroPoint(data[0].num_at_risk));
            }
            elem.curve = elem.svg.append("path")
                .attr('id', _curveId+"-line")
                .attr("d", elem.line(data))
                .style("fill", "none")
                .style("stroke", opts.line_color);
        }
    }

    function drawInvisiableDots(_index, data, _color) {
        elem.dots[_index].selectAll("path")
            .data(data)
            .enter()
            .append("svg:path")
            .attr("d", d3.svg.symbol()
                .size(400)
                .type("circle"))
            .attr("transform", function(d){
                return "translate(" + elem.xScale(d.time) + ", " + elem.yScale(d.survival_rate) + ")";
            })
            .attr("fill", _color)
            .style("opacity", 0);
    }

    function addQtips(_index) {
        elem.dots[_index].selectAll('path').each(
            function(d) {
                var content = "<font size='2'>";
                content += "Case id: " + "<strong><a href='"
                        + cbio.util.getLinkToPatientView(cancer_study_id, d.case_id)
                        + "' target='_blank'>" + d.case_id + "</a></strong><br>";
                content += text.qTips.estimation + ": <strong>" + (d.survival_rate * 100).toFixed(2) + "%</strong><br>";
                if (d.status === "0") { // If censored, mark it
                    content += text.qTips.censoredEvent + ": <strong>" + d.time.toFixed(2) + " </strong>months (censored)<br>";
                } else { // status is 1, means event occured 
                    content += text.qTips.failureEvent + ": <strong>" + d.time.toFixed(2) + " </strong>months<br>";
                }
                content += "</font>";

                $(this).qtip(
                    {
                        content: {text: content},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow qtip-wide'},
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right'}
                    }
                );

            }
        );
        
        var mouseOn = function() {
            var dot = d3.select(this);
            dot.transition()
                .duration(400)
                .style("opacity", .9);
        };

        var mouseOff = function() {
            var dot = d3.select(this);
            dot.transition()
                .duration(400)
                .style("opacity", 0);
        };

        elem.dots[_index].selectAll("path").on("mouseover", mouseOn);
        elem.dots[_index].selectAll("path").on("mouseout", mouseOff);
    }

    function appendAxis(elemAxisX, elemAxisY) {
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .attr("class", "survival-curve-x-axis-class")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, " + (settings.chart_top + settings.chart_height) + ")")
            .call(elemAxisX);
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, " + settings.chart_top + ")")
            .call(elemAxisX.orient("bottom").ticks(0));
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .attr("class", "survival-curve-y-axis-class")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + settings.chart_left + ", 0)")
            .call(elemAxisY);
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + (settings.chart_left + settings.chart_width) + ", 0)")
            .call(elemAxisY.orient("left").ticks(0));
        elem.svg.selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "11px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
    }

    function drawCensoredDots(data, opts) {
        elem.censoredDots.selectAll("path")
            .data(data)
            .enter()
            .append("line")
            .attr("x1", function(d) {return elem.xScale(d.time)})
            .attr("x2", function(d) {return elem.xScale(d.time)})
            .attr("y1", function(d) {return elem.yScale(d.survival_rate) + style.censored_sign_size})
            .attr("y2", function(d) {return elem.yScale(d.survival_rate) - style.censored_sign_size})
            .attr("stroke", opts.line_color)
            .style("opacity", function(d) {
                if (d.status === "1") {
                    return 0; //hidden
                } else if (d.status === "0") { //censored
                    return 1;
                }
            });
        elem.censoredDots.selectAll("path")
            .data(data)
            .enter()
            .append("line")
            .attr("x1", function(d) {return elem.xScale(d.time) + style.censored_sign_size})
            .attr("x2", function(d) {return elem.xScale(d.time) - style.censored_sign_size})
            .attr("y1", function(d) {return elem.yScale(d.survival_rate)})
            .attr("y2", function(d) {return elem.yScale(d.survival_rate)})
            .attr("stroke", opts.line_color)
            .style("opacity", function(d) {
                if (d.status === "1") {
                    return 0; //hidden
                } else if (d.status === "0") { //censored
                    return 1;
                }
            });
    }

    function addLegends(_inputArr) {
        var legends_text = [];
        $.each(_inputArr, function(index, obj) {
            var _tmp = {};
            _tmp.text = obj.settings.legend;
            _tmp.color = obj.settings.line_color;
            legends_text.push(_tmp);
        });

        var legend = elem.svg.selectAll(".legend")
            .data(legends_text)
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function(d, i) {
                return "translate(" + (settings.chart_left + settings.chart_width + 15) + ", " + (70 + i * 15) + ")";
            })

        legend.append("path")
            .attr("width", 18)
            .attr("height", 18)
            .attr("d", d3.svg.symbol()
                .size(60)
                .type(function(d) { return "square"; }))
            .attr("fill", function (d) { return d.color; })
            .attr("stroke", "black")
            .attr("stroke-width",.9);

        legend.append("text")
            .attr("x", 15)
            .attr("y", 4)
            .style("text-anchor", "front")
            .text(function(d) { return d.text });

    }

    function appendAxisTitles(xTitle, yTitle) {
        elem.svg.append("text")
            .attr("class", "label")
            .attr("x", style.axisX_title_pos_x)
            .attr("y", style.axisX_title_pos_y)
            .style("text-anchor", "middle")
            .style("font-size", "11px")
            .style("font-weight","bold")
            .text(xTitle);
        elem.svg.append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("x", style.axisY_title_pos_x)
            .attr("y", style.axisY_title_pos_y)
            .style("text-anchor", "middle")
            .style("font-size", "11px")
            .style("font-weight","bold")
            .text(yTitle);
    }

    function addPvals() {
        elem.svg.append("text")
            .attr("x", settings.chart_left + settings.chart_width + 10)
            .attr("y", 110)
            .style("text-anchor", "front")
            .text("Logrank Test P-Value: " + parseFloat(vals.pVal).toFixed(6));
    }

    function appendInfoTable(_infoTableInputArr) {
        $("#" + divs.infoTableDivId).empty();
        $("#" + divs.infoTableDivId).append("<tr>" +
            "<td style='width: 600px; text-align:left;'></td>" +
            "<td style='width: 200px;'>" + text.infoTableTitles.total_cases + "</td>" + 
            "<td style='width: 200px;'>" + text.infoTableTitles.num_of_events_cases + "</td>" +  
            "<td style='width: 200px;'>" + text.infoTableTitles.median + "</td>" + 
            "</tr>");
        $.each(_infoTableInputArr, function(index, obj) {
            $("#" + divs.infoTableDivId).append("<tr>" + 
                                                "<td>" + obj.groupName + "</td>" + 
                                                "<td><b>" + obj.num_cases + "</b></td>" + 
                                                "<td><b>" + obj.num_of_events_cases + "</b></td>" + 
                                                "<td><b>" + obj.median + "</b></td>" +
                                                "</tr>");
        });
    }

    function appendImgConverter() {
        var pdfConverterForm = "<form class='img_buttons' action='svgtopdf.do' method='post' target='_blank' " +
            "onsubmit=\"this.elements['svgelement'].value=loadSurvivalCurveSVG('" + divs.curveDivId + "');\">" +
            "<input type='hidden' name='svgelement'>" +
            "<input type='hidden' name='filetype' value='pdf'>" +
            "<input type='hidden' name='filename' value='survival_study.pdf'>" +
            "<input type='submit' value='PDF'></form>";
        $('#' + divs.headerDivId).append(pdfConverterForm);
        var svgConverterForm = "<form class='img_buttons' action='svgtopdf.do' method='post' target='_blank' " +
            "onsubmit=\"this.elements['svgelement'].value=loadSurvivalCurveSVG('" + divs.curveDivId + "');\">" +
            "<input type='hidden' name='svgelement'>" +
            "<input type='hidden' name='filetype' value='svg'>" +
            "<input type='hidden' name='filename' value='survival_study.svg'>" +
            "<input type='submit' value='SVG'></form>";
        $('#' + divs.headerDivId).append(svgConverterForm);
    }
    
    function drawCurve(_inputArr, _obj){
        var data = _obj.data;
        var opts = _obj.settings;
        var _curve = {};
        _curve.id = opts.curveId;
        _curve.color = opts.line_color;
        curvesInfo[_curve.id] = _curve;
        elem.dots[_curve.id] = elem.svg.append("g").attr('id', _curve.id+"-dots"); //the invisible dots
        initLines();
        drawLines(data.getData(), opts, _curve.id);
        
        //First element is used to draw lines and its case_id is NA, this dot 
        //will not be needed for drawing censored dots and invisiable dots. 
        //Then remove move it in here.
        data.getData().shift();
        drawCensoredDots(data.getData(), opts, _curve.id);
        drawInvisiableDots(_curve.id, data.getData(), opts.mouseover_color);
        addQtips(_curve.id);
        
        if (settings.include_legend) {
            addLegends(_inputArr);
        }
    }
    
    function removeCurve(_curveId){
        $('#' + _curveId + '-dots').remove();
        $('#' + _curveId + '-line').remove();
        removeCurveCensoredDots(_curveId);
        delete curvesInfo[_curveId];
        //TODO: Add redraw curve lable function
    }
    
    function removeCurveCensoredDots(_curveId){
        var _allDots = $("#" + divs.curveDivId + " #crossDots").find('line');
        
        $.each(_allDots, function(index, value){
            var _currentColor = $(value).attr('stroke');
            if(_currentColor === curvesInfo[_curveId].color){
                $(value).remove();
            }
        });
    }
    
    function addCurve(_obj){
        if(!(_obj.settings.curveId in curvesInfo)){
            drawCurve([_obj], _obj);
        }else{
            console.log("%c Error: Curve ID exists", "color:red");
        }
    }
    
    return {
        init: function(_inputArr, _opts) { 
            //Place parameters
            elem = _opts.elem;
            settings = _opts.settings;
            text = _opts.text;
            style = _opts.style;
            divs = _opts.divs;
            vals = _opts.vals;
            //Init and Render
            initCanvas();
            initAxis(_inputArr);
            appendAxis(elem.xAxis, elem.yAxis);
            appendAxisTitles(text.xTitle, text.yTitle);
            $.each(_inputArr, function(index, obj) {
                drawCurve(_inputArr, obj);
            });
            appendImgConverter();
            if (_opts.settings.include_info_table) {
                var _infoTableInputArr = [];
                $.each(_inputArr, function(index, obj) {
                    var _tmp = jQuery.extend(true, {}, obj.data.getStats());
                    _tmp.groupName = obj.settings.legend;
                    _infoTableInputArr.push(_tmp);
                });
                appendInfoTable(_infoTableInputArr);
            }
            if (_opts.settings.include_pvalue) {
                addPvals();
            }
        },
        
        addCurve: addCurve,
        removeCurve: removeCurve
    };
};

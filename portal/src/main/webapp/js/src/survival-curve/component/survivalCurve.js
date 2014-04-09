var SurvivalCurve = function() {

    var divId = "";
        numOfGroups = 0;
    //Instances
    var elem = "",
        settings = "",
        text = "",
        style = "";

    function initCanvas() {
        $('#' + divId).empty();
        elem.svg = d3.select("#" + divId)
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);
        elem.dots = elem.svg.append("g");
        elem.censoredDots = elem.svg.append("g");
    }

    function initAxis(_inputArr) {
        var _dataset = [];
        var formatAsPercentage = d3.format(".1%");
        $.each(_inputArr, function(index, obj) {
            var data = obj.data;
            $.each(data.getData(), function(index, d) {
                _dataset.push(d.time);
            });
        });
        elem.xScale = d3.scale.linear()
            .domain([0, d3.max(_dataset) + 0.1 * d3.max(_dataset)])
            .range([100, 700]);
        elem.yScale = d3.scale.linear()
            .domain([-0.03, 1.05]) //fixed to be 0-1
            .range([550, 50]);
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

    function drawLines(data, opts) {
        if (data !== null) {
            if (data[0].time !== 0) {
                data.unshift(appendZeroPoint(data[0].num_at_risk));
            }
            elem.svg.append("path")
                .attr("d", elem.line(data))
                .style("fill", "none")
                .style("stroke", opts.line_color);
        }
    }

    function drawInvisiableDots(data) {
        elem.svg.selectAll("path")
            .data(data)
            .enter()
            .append("svg:path")
            .attr("d", d3.svg.symbol()
                .size(400)
                .type("circle"))
            .attr("transform", function(d){
                return "translate(" + elem.xScale(d.time) + ", " + elem.yScale(d.survival_rate) + ")";
            })
            .attr("fill", settings.altered_mouseover_color)
            .style("opacity", 0);
    }

    function addQtips() {
        elem.svg.selectAll('path').each(
            function(d) {
                var content = "<font size='2'>";
                content += "Case id: " + "<strong><a href='tumormap.do?case_id=" + d.case_id +
                           "&cancer_study_id=" + cancer_study_id + "' target='_blank'>" + d.case_id + "</a></strong><br>";
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

                elem.svg.selectAll("path").on("mouseover", mouseOn);
                elem.svg.selectAll("path").on("mouseout", mouseOff);
            }
        );
    }

    function appendAxis(elemAxisX, elemAxisY) {
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .attr("class", "survival-curve-x-axis-class")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 550)")
            .call(elemAxisX);
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 50)")
            .call(elemAxisX.orient("bottom").ticks(0));
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .attr("class", "survival-curve-y-axis-class")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(100, 0)")
            .call(elemAxisY);
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(700, 0)")
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

    function addLegends(svg) {
        var _legends_text = [
            {
                text: text.glyph1,                //altered
                color: settings.altered_line_color
            },
            {
                text: text.glyph2,
                color: settings.unaltered_line_color
            }
        ];

        var legend = svg.selectAll(".legend")
            .data(_legends_text)
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function(d, i) {
                return "translate(715, " + (70 + i * 15) + ")";
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
            .style("font-weight","bold")
            .text(xTitle);
        elem.svg.append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("x", style.axisY_title_pos_x)
            .attr("y", style.axisY_title_pos_y)
            .style("text-anchor", "middle")
            .style("font-weight","bold")
            .text(yTitle);
    }

    function addPvals() {
        elem.svg.append("text")
            .attr("x", 710)
            .attr("y", 110)
            .style("text-anchor", "front")
            .text("Logrank Test P-Value: " + data.getStats().pVal);
    }

    function appendInfo(divName, vals, type) {
        var _m_title = "";
        var _events_title = "";
        if (type === "os") {
            _m_title = "median months survival";
            _events_title = "#cases deceased";
        } else if (type === "dfs") {
            _m_title = "median months disease free";
            _events_title = "#cases relapsed";
        }
        $("#" + divName).empty();
        $("#" + divName).append("<table class='survival_stats'>" +
            "<tr><td></td><td>#total cases</td><td>" + _events_title + "</td><td>" + _m_title + "</td></tr>" +
            "<tr>" +
            "<td style='width: 300px; text-align:left;'>Cases with Alteration(s) in Query Gene(s)</td>" +
            "<td><b>" + vals.num_altered_cases + "</b></td>" +
            "<td><b>" + vals.num_of_events_altered_cases + "</b></td>" +
            "<td><b>" + vals.altered_median + "</b></td>" +
            "</tr><tr>" +
            "<td style='text-align:left;'>Cases without Alteration(s) in Query Gene(s)</td>" +
            "<td><b>" + vals.num_unaltered_cases + "</b></td>" +
            "<td><b>" + vals.num_of_events_unaltered_cases + "</b></td>" + 
            "<td><b>" + vals.unaltered_median + "</b></td>" +
            "</table>");
    }

    function appendImgConverter(divId, svgId) {
        var pdfConverterForm = "<form class='img_buttons' action='svgtopdf.do' method='post' " +
            "onsubmit=\"this.elements['svgelement'].value=loadSurvivalCurveSVG('" + svgId + "');\">" +
            "<input type='hidden' name='svgelement'>" +
            "<input type='hidden' name='filetype' value='pdf'>" +
            "<input type='hidden' name='filename' value='survival_study.pdf'>" +
            "<input type='submit' value='PDF'></form>";
        $('#' + divId).append(pdfConverterForm);
        var svgConverterForm = "<form class='img_buttons' action='svgtopdf.do' method='post' " +
            "onsubmit=\"this.elements['svgelement'].value=loadSurvivalCurveSVG('" + svgId + "');\">" +
            "<input type='hidden' name='svgelement'>" +
            "<input type='hidden' name='filetype' value='svg'>" +
            "<input type='hidden' name='filename' value='survival_study.svg'>" +
            "<input type='submit' value='SVG'></form>";
        $('#' + divId).append(svgConverterForm);
    }

    return {
        init: function(_inputArr, _divId, _opts) { 
            //Place parameters
            divId = _divId;
            elem = _opts.elem;
            settings = _opts.settings;
            text = _opts.text;
            style = _opts.style;
            //Init and Render
            initCanvas();
            initAxis(_inputArr);
            appendAxis(elem.xAxis, elem.yAxis);
            appendAxisTitles(text.xTitle, text.yTitle);
            $.each(_inputDataArr, function(index, obj) {
                var data = obj.data;
                var opts = obj.settings;
                initLines();
                drawLines(data.getData(), opts);
                drawCensoredDots(data.getData(), opts);
                //drawInvisiableDots(data.getData());
                //addQtips();
            });
            //addLegends(elem.svg);
            //addPvals();
            //appendInfo("os_stat_table", data.getStats(), "os");
            //appendImgConverter("os_header", divId);
        }
    }
};

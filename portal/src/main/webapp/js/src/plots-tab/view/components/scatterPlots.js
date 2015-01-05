var scatterPlots = (function() {
    
    var settings = {
            canvas_width: 800,
            canvas_height: 600,
            axis: {
                x: {
                    range_min: 120,
                    range_max: 620,
                    orient: "bottom",
                    title_x: 350,
                    title_y: 590,
                    help_x: 350,
                    help_y: 575
                },
                y: {
                    range_min: 520,
                    range_max: 20,
                    orient: "left",
                    title_x: -240,
                    title_y: 55,
                    help_x: 135,
                    help_y: 125
                }
            }
        },
        div = "",
        elem = {
            svg : "",
            x: {
                scale: "",
                axis: ""
            },
            y: {
                scale: "",
                axis: ""
            },
            dotsGroup : "",   //Group of single Dots
            axisTitleGroup: "",
            boxPlots: ""
        },
        data = [],
        glyphs = [];
    
    function initCanvas() {
        elem.svg = d3.select("#" + div)
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);
        elem.dotsGroup = elem.svg.append("svg:g");
        elem.boxPlots = elem.svg.append("svg:g");
        elem.axisTitleGroup = elem.svg.append("svg:g").attr("class", "axis");
    }
    
    function initAxis(axis) {
        var stat = plotsData.stat();
        var domainMin = stat[axis].min - stat[axis].edge;
        var domainMax = stat[axis].max + stat[axis].edge;
        elem[axis].scale = d3.scale.linear()
            .domain([domainMin, domainMax])
            .range([settings.axis[axis].range_min, settings.axis[axis].range_max]);
        elem[axis].axis = d3.svg.axis()
            .scale(elem[axis].scale)
            .orient(settings.axis[axis].orient)
            .tickSize(6, 0, 0)
            .tickPadding([8]);
    }
    
    function drawAxis(axis) {
        var top_x, top_y, bottom_x, bottom_y;
        if (axis === "x") {
            top_x = 0;
            top_y = settings.axis.y.range_min;
            bottom_x = 0;
            bottom_y = settings.axis.y.range_max;
        } else if (axis === "y") {
            top_x = settings.axis.x.range_min;
            top_y = 0;
            bottom_x = settings.axis.x.range_max;
            bottom_y = 0;
        }
        d3.select("#" + div).select(d3_class[axis].axis).remove();
        elem.svg.append("g")
            .style("stroke-width", 1.5)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + top_x + ", " + top_y + ")")
            .attr("class", d3_class[axis].axis)
            .call(elem[axis].axis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "12px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.svg.append("g")
            .style("stroke-width", 1.5)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + bottom_x + ", " + bottom_y + ")")
            .call(elem[axis].axis.orient(settings.axis[axis].orient).ticks(0));
    }
    
    function drawDots(_apply_box_plots, _box_plots_axis) {
        
        elem.dotsGroup.selectAll("path").remove();

        if ($("input[name=" + ids.sidebar.util.view_switch + "]:checked").val() === "mutation_details") {
            elem.dotsGroup.selectAll("path")
                .data(data)
                .enter()
                .append("svg:path")
                .attr("transform", function(d){
                    $(this).attr("x_pos", elem.x.scale(d.xVal));
                    $(this).attr("y_pos", elem.y.scale(d.yVal));
                    $(this).attr("x_val", d.xVal);
                    $(this).attr("y_val", d.yVal);
                    $(this).attr("case_id", d.caseId);
                    $(this).attr("size", 20);

                    var _x, _y;
                    if (_apply_box_plots) { //apply noise
                        if (_box_plots_axis === "x") {
                            _x = elem.x.scale(d.xVal) + (Math.random() * 30 - 30/2);
                            _y = elem.y.scale(d.yVal);
                        } else {
                            _x = elem.x.scale(d.xVal);
                            _y = elem.y.scale(d.yVal) + (Math.random() * 20 - 20/2);
                        }
                    } else {
                        _x = elem.x.scale(d.xVal);
                        _y = elem.y.scale(d.yVal);
                    }

                    return "translate(" + _x + ", " + _y + ")";
                })
                .attr("d", d3.svg.symbol()
                    .size(20)
                    .type(function(d){
                        return mutationInterpreter.getSymbol(d);
                    }))
                .attr("fill", function(d){
                    return mutationInterpreter.getFill(d);
                })
                .attr("stroke", function(d){
                    return mutationInterpreter.getStroke(d);
                })
                .attr("stroke-width", 1.2);            
        } else if ($("input[name=" + ids.sidebar.util.view_switch + "]:checked").val() === "gistic") {
            elem.dotsGroup.selectAll("path")
                .data(data)
                .enter()
                .append("svg:path")
                .attr("transform", function(d){
                    $(this).attr("x_pos", elem.x.scale(d.xVal));
                    $(this).attr("y_pos", elem.y.scale(d.yVal));
                    $(this).attr("x_val", d.xVal);
                    $(this).attr("y_val", d.yVal);
                    $(this).attr("case_id", d.caseId);
                    $(this).attr("size", 35);

                    var _x, _y;
                    if (_apply_box_plots) { //apply noise
                        if (_box_plots_axis === "x") {
                            _x = elem.x.scale(d.xVal) + (Math.random() * 30 - 30/2);
                            _y = elem.y.scale(d.yVal);
                        } else {
                            _x = elem.x.scale(d.xVal);
                            _y = elem.y.scale(d.yVal) + (Math.random() * 20 - 20/2);
                        }
                    } else {
                        _x = elem.x.scale(d.xVal);
                        _y = elem.y.scale(d.yVal);
                    }

                    return "translate(" + _x + ", " + _y + ")";
                })
                .attr("d", d3.svg.symbol()
                    .size(35)
                    .type(function(d){
                        return gisticStyle.getSymbol(d.cna_anno);
                    }))
                .attr("fill", function(d){
                    return gisticStyle.getFill(d.cna_anno);
                })
                .attr("stroke", function(d){
                    return gisticStyle.getStroke(d.cna_anno);
                })
                .attr("stroke-width", 1.2);
        }

    }
    
    function appendTitle(axis) { //axis titles
        var elt = document.getElementById(ids.sidebar[axis].profile_name);
        var _profile_name = elt.options[elt.selectedIndex].text;
        var _profile_id = elt.options[elt.selectedIndex].value;
        
        var _tmp_attr = (axis === "y")? "rotate(-90)": "";
        d3.select("#" + div).select(d3_class[axis].axis_title).remove();
        elem.axisTitleGroup.append("text")
            .attr("class", d3_class[axis].axis_title)
            .attr("transform", _tmp_attr)
            .attr("x", settings.axis[axis].title_x)
            .attr("y", settings.axis[axis].title_y)
            .style("text-anchor", "middle")
            .style("font-weight","bold")
            .text(_profile_name);
        
        //append help icon (mouseover)
        var _pos_x = (axis==="x")? (settings.axis.x.title_x + _profile_name.length / 2 * 8 + 10): (settings.axis.y.title_y - 10);
        var _pos_y = (axis==="x")? (settings.axis.x.title_y - 10): (settings.axis.y.title_x + 450 - _profile_name.length / 2 * 8 );
        elem.axisTitleGroup.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", d3_class[axis].title_help)
            .attr("x", _pos_x)
            .attr("y", _pos_y)
            .attr("width", "16")
            .attr("height", "16");
        elem.svg.select("." + d3_class[axis].title_help).each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + metaData.getDescription(_profile_id) + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right', viewport: $(window)}
                    }
                );
            }
        );
    }
    
    function appendGlyphs() {
        var legend = elem.svg.selectAll(".legend")
                .data(glyphs)
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) {
                    return "translate(630, " + (25 + i * 15) + ")";
                });

            legend.append("path")
                .attr("width", 18)
                .attr("height", 18)
                .attr("d", d3.svg.symbol()
                    .size(30)
                    .type(function(d) { return d.symbol; }))
                .attr("fill", function (d) { return d.fill; })
                .attr("stroke", function (d) { return d.stroke; })
                .attr("stroke-width", 1.1);

            legend.append("text")
                .attr("dx", ".75em")
                .attr("dy", ".35em")
                .style("text-anchor", "front")
                .text(function(d){return d.legendText;});
    }
    
    function applyMouseover() {
        elem.dotsGroup.selectAll("path").each(function(d) {
            var _content = "<strong><a href='" +
                cbio.util.getLinkToSampleView(window.PortalGlobals.getCancerStudyId(), d.caseId) +
                "' target = '_blank'>" + d.caseId +
                "</a></strong>";
            if (Object.keys(d.mutation).length !== 0) {
                $.each(Object.keys(d.mutation), function(index, gene) {
                    _content += "<br>" + gene + ": " + d.mutation[gene].details;
                });
            }

            $(this).qtip(
                {
                    content: {text: _content},
                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                    show: {event: "mouseover"},
                    hide: {fixed:true, delay: 100, event: "mouseout"},
                    position: {my:'left bottom',at:'top right', viewport: $(window)}
                }
            );
        });
        var mouseOn = function() {
            var dot = d3.select(this);
            dot.transition()
                .ease("elastic")
                .duration(600)
                .delay(100)
                .attr("d", d3.svg.symbol().size(200));
        };
        var mouseOff = function() {
            var dot = d3.select(this);
            dot.transition()
                .ease("elastic")
                .duration(600)
                .delay(100)
                .attr("d", d3.svg.symbol().size(20));
        };
        elem.dotsGroup.selectAll("path").on("mouseover", mouseOn);
        elem.dotsGroup.selectAll("path").on("mouseout", mouseOff);
    }
    
    return {
        init: function(_div, _data, _apply_box_plots, _box_plots_axis) {
            div = _div;
            $("#" + _div).empty();
            //convert input data from JSON to array
            data = [];
            glyphs = [];
            data.length = 0;
            glyphs.length = 0;
            for (var key in _data) {
                data.push(_data[key]);
            }
            //rendering
            initCanvas(div);
            initAxis("x");
            initAxis("y");
            drawAxis("x");
            drawAxis("y");
            if (_apply_box_plots) {
                boxPlots.init(data, _box_plots_axis, elem);
            }
            drawDots(_apply_box_plots, _box_plots_axis);
            applyMouseover();
            appendTitle("x");
            appendTitle("y");
            appendGlyphs();
        },
        addGlyph: function(obj) {
            glyphs.push(obj);
        },
        isGlyphExist: function(type_id) {
            var _result = false;
            $.each(glyphs, function(index, _obj) {
                if(_obj.typeName === type_id) _result = true; 
            });
            return _result;
        }
    };
    
}());
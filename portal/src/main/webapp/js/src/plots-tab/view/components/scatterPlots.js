var scatterPlots = (function() {
    
    var settings = {
            canvas_width: 720,
            canvas_height: 600,
            axis: {
                x: {
                    range_min: 120,
                    range_max: 620,
                    orient: "bottom",
                    title_x: 350,
                    title_y: 590
                },
                y: {
                    range_min: 520,
                    range_max: 20,
                    orient: "left",
                    title_x: -240,
                    title_y: 55
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
            axisTitleGroup: ""
        },
        data = [];
    
    function initCanvas() {
        elem.svg = d3.select("#" + div)
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);
        elem.dotsGroup = elem.svg.append("svg:g");
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
        if (axis === "x") {
            var top_x = 0;
            var top_y = settings.axis.y.range_min;
            var bottom_x = 0;
            var bottom_y = settings.axis.y.range_max;
        } else if (axis === "y") {
            var top_x = settings.axis.x.range_min;
            var top_y = 0;
            var bottom_x = settings.axis.x.range_max;
            var bottom_y = 0;
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
    
    function drawDots() {
        elem.dotsGroup.selectAll("path").remove();
        elem.dotsGroup.selectAll("path")
            .data(data)
            .enter()
            .append("svg:path")
            .attr("transform", function(d){
                $(this).attr("x_pos", elem.x.scale(d.xVal));
                $(this).attr("y_pos", elem.y.scale(d.yVal));
                $(this).attr("x_val", d.xVal);
                $(this).attr("y_val", d.yVal);
                $(this).attr("size", 20);
                return "translate(" + elem.x.scale(d.xVal) + ", " + elem.y.scale(d.yVal) + ")";
            })
            .attr("d", d3.svg.symbol()
                .size(20)
                .type("circle"));
    }
    
    function appendTitle(axis) { //axis titles
        var elt = document.getElementById(ids.sidebar[axis].profile_name);
        var text = elt.options[elt.selectedIndex].text;
        
        var _tmp_attr = (axis === "y")? "rotate(-90)": "";
        d3.select("#" + div).select(d3_class[axis].axis_title).remove();
        elem.axisTitleGroup.append("text")
            .attr("class", d3_class[axis].axis_title)
            .attr("transform", _tmp_attr)
            .attr("x", settings.axis[axis].title_x)
            .attr("y", settings.axis[axis].title_y)
            .style("text-anchor", "middle")
            .style("font-weight","bold")
            .text(text);
        
        //append help icon (mouseover)
        elem.axisTitleGroup.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", d3_class[axis].axis_title)
            .attr("x", 350 + text.length / 2 * 8)
            .attr("y", 567)
            .attr("width", "16")
            .attr("height", "16");
        svg.select("." + d3_class[axis].axis_title).each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + xText + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right', viewport: $(window)}
                    }
                );
            }
        );
            
    }
    
    return {
        init: function(_div, _data) {
            div = _div;
            //convert input data from JSON to array
            data = [];
            data.length = 0;
            for (var key in _data) {
                data.push(_data[key]);
            }
            //rendering
            initCanvas(div);
            initAxis("x");
            initAxis("y");
            drawAxis("x");
            drawAxis("y");
            drawDots();
            appendTitle("x");
            appendTitle("y");
        }
    };
    
}());
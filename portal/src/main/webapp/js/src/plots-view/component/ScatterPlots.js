var ScatterPlots = (function() {

    var style = {},
        canvas = {},
        elem = {},
        text = {};

    function initSettings(_style, _canvas, _elem, _text) { //Init with options
        style = jQuery.extend(true, {}, _style);
        canvas = jQuery.extend(true, {}, _canvas);
        elem = jQuery.extend(true, {}, _elem);
        text = jQuery.extend(true, {}, _text);
    }

    function initSvgCanvas(divName) {
        elem.svg = d3.select("#" + divName).append("svg")
            .attr("width", canvas.width)
            .attr("height", canvas.height);
    }

    function initScales(dataArr) {
        var _yValArr = [];
        var _xValArr = [];
        $.each(dataArr, function(index, val){
            _xValArr.push(val.x_val);
            _yValArr.push(val.y_val);
        });
        elem.xScale = d3.scale.linear()
            .domain([PlotsUtil.findMin(_xValArr), PlotsUtil.findMax(_xValArr)])
            .range([canvas.xLeft, canvas.xRight]);
        elem.yScale = d3.scale.linear()
            .domain([PlotsUtil.findMin(_yValArr), PlotsUtil.findMax(_yValArr)])
            .range([canvas.yBottom, canvas.yTop]);
    }

    function initAxis() {
        elem.xAxis = d3.svg.axis()
            .scale(elem.xScale)
            .orient("bottom");
        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .orient("left");
    }

    function generateAxis() {
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, " + canvas.yBottom + ")")
            .call(elem.xAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "11px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, " + canvas.yTop + ")")
            .call(elem.xAxis.orient("bottom").ticks(0));
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + canvas.xLeft + ", 0)")
            .call(elem.yAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "11px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + canvas.xRight + ", 0)")
            .call(elem.yAxis.orient("left").ticks(0));
    }

    function appendAxisTitles() {
        var axisTitleGroup = elem.svg.append("svg:g");
        axisTitleGroup.append("text")
            .attr("x", canvas.xLeft + (canvas.xRight - canvas.xLeft) / 2)
            .attr("y", canvas.yBottom + 40)
            .style("text-anchor", "middle")
            .style("font-size", "13px")
            .text(text.xTitle);
        axisTitleGroup.append("text")
            .attr("transform", "rotate(-90)")
            .attr("x", (canvas.xLeft - canvas.xRight) / 2 - canvas.yTop)
            .attr("y", canvas.yTop)
            .style("text-anchor", "middle")
            .style("font-size", "13px")
            .text(text.yTitle);
    }

    return {
        init: function(_style, _canvas, _elem, divName, dataArr) {    //Init with options
            initSettings(_style, _canvas, _elem);
            initSvgCanvas(divName);
            initScales(dataArr);
            initAxis();
            generateAxis();
            appendAxisTitles();
        },
        update: function() {   //Update with new options
        }
    }

}());


